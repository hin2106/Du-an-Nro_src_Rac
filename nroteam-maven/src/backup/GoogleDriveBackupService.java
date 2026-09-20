package backup;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.util.List;

/** OAuth Desktop: cap quyen mot lan, refresh token tu dong o cac lan sau. */
public final class GoogleDriveBackupService {

    private static final Path TOKEN_DIR = Path.of("data", "secure", "google-drive-tokens");
    private static final String USER_KEY = "nro-backup-owner";
    private static final GsonFactory JSON = GsonFactory.getDefaultInstance();

    public boolean hasStoredAuthorization() {
        if (!Files.isDirectory(TOKEN_DIR)) return false;
        try (var files = Files.list(TOKEN_DIR)) {
            return files.anyMatch(Files::isRegularFile);
        } catch (IOException e) {
            return false;
        }
    }

    public void clearStoredAuthorization() throws IOException {
        if (!Files.isDirectory(TOKEN_DIR)) return;
        try (var files = Files.walk(TOKEN_DIR)) {
            for (Path path : files.sorted(java.util.Comparator.reverseOrder()).toList()) {
                if (!path.equals(TOKEN_DIR)) Files.deleteIfExists(path);
            }
        }
    }

    public String authorizeInteractive(BackupConfig config) throws Exception {
        Drive drive = createDrive(config, true);
        var about = drive.about().get().setFields("user(displayName,emailAddress)").execute();
        protectTokenFiles();
        if (about.getUser() == null) return "Đã kết nối Google Drive";
        String email = about.getUser().getEmailAddress();
        String name = about.getUser().getDisplayName();
        return (name == null ? "" : name + " - ") + (email == null ? "Google Drive" : email);
    }

    public String testConnection(BackupConfig config) throws Exception {
        if (!hasStoredAuthorization()) {
            throw new IllegalStateException("Google Drive chưa được cấp quyền");
        }
        Drive drive = createDrive(config, false);
        var about = drive.about().get().setFields("user(displayName,emailAddress),storageQuota").execute();
        if (about.getUser() == null) return "Google Drive đã kết nối";
        return "Đã kết nối: " + about.getUser().getEmailAddress();
    }

    public String upload(Path localFile, String category, BackupConfig config) throws Exception {
        if (!hasStoredAuthorization()) {
            throw new IllegalStateException("Không có refresh token Google Drive");
        }
        Drive drive = createDrive(config, false);
        String rootId = ensureRootFolder(drive, config);
        String categoryId = ensureFolder(drive, category, rootId);

        com.google.api.services.drive.model.File metadata =
                new com.google.api.services.drive.model.File();
        metadata.setName(localFile.getFileName().toString());
        metadata.setParents(List.of(categoryId));
        metadata.setDescription("NRO automated backup");

        String mimeType = localFile.getFileName().toString().endsWith(".zip")
                ? "application/zip" : "application/gzip";
        FileContent content = new FileContent(mimeType, localFile.toFile());
        com.google.api.services.drive.model.File uploaded = drive.files()
                .create(metadata, content)
                .setFields("id,name,size,createdTime")
                .execute();
        try {
            cleanupRemote(drive, categoryId, config.googleRetentionDays(), uploaded.getId());
        } catch (IOException cleanupError) {
            utils.Logger.warning("[Backup] Upload thành công nhưng dọn retention Drive thất bại: "
                    + cleanupError.getMessage() + "\n");
        }
        return uploaded.getId();
    }

    private Drive createDrive(BackupConfig config, boolean allowInteractive) throws Exception {
        if (!Files.isRegularFile(config.googleCredentialsPath())) {
            throw new IOException("Không tìm thấy OAuth credentials: " + config.googleCredentialsPath());
        }
        if (!allowInteractive && !hasStoredAuthorization()) {
            throw new IllegalStateException("Google Drive chưa được cấp quyền trong menu Setup");
        }

        var transport = GoogleNetHttpTransport.newTrustedTransport();
        GoogleClientSecrets secrets;
        try (Reader reader = Files.newBufferedReader(config.googleCredentialsPath(), StandardCharsets.UTF_8)) {
            secrets = GoogleClientSecrets.load(JSON, reader);
        }
        Files.createDirectories(TOKEN_DIR);
        SecureFiles.ownerDirectory(TOKEN_DIR);
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                transport, JSON, secrets, List.of(DriveScopes.DRIVE_FILE))
                .setDataStoreFactory(new FileDataStoreFactory(TOKEN_DIR.toFile()))
                .setAccessType("offline")
                .build();

        Credential credential;
        if (allowInteractive) {
            LocalServerReceiver receiver = new LocalServerReceiver.Builder()
                    .setHost("localhost").setPort(8888).build();
            credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize(USER_KEY);
        } else {
            credential = flow.loadCredential(USER_KEY);
            if (credential == null || credential.getRefreshToken() == null) {
                throw new IllegalStateException("Refresh token Google Drive không tồn tại hoặc đã bị xóa");
            }
        }
        return new Drive.Builder(transport, JSON, credential)
                .setApplicationName("NRO Server Backup")
                .build();
    }

    private String ensureRootFolder(Drive drive, BackupConfig config) throws Exception {
        String configured = config.googleFolderId();
        if (!configured.isBlank()) {
            try {
                drive.files().get(configured).setFields("id,trashed").execute();
                return configured;
            } catch (IOException ignored) {
                // Folder bi xoa hoac token da doi, tao lai ben duoi.
            }
        }
        String id = ensureFolder(drive, config.googleFolderName(), null);
        config.set("backup.google.folderId", id);
        config.save();
        return id;
    }

    private String ensureFolder(Drive drive, String name, String parentId) throws IOException {
        String escaped = name.replace("'", "\\'");
        String query = "name='" + escaped + "' and mimeType='application/vnd.google-apps.folder' and trashed=false";
        if (parentId != null) query += " and '" + parentId + "' in parents";
        var list = drive.files().list().setQ(query).setSpaces("drive")
                .setFields("files(id,name)").setPageSize(10).execute();
        if (list.getFiles() != null && !list.getFiles().isEmpty()) {
            return list.getFiles().get(0).getId();
        }
        com.google.api.services.drive.model.File folder =
                new com.google.api.services.drive.model.File();
        folder.setName(name);
        folder.setMimeType("application/vnd.google-apps.folder");
        if (parentId != null) folder.setParents(List.of(parentId));
        return drive.files().create(folder).setFields("id").execute().getId();
    }

    private void cleanupRemote(Drive drive, String parentId, int retentionDays, String keepId) throws IOException {
        java.time.Instant cutoff = java.time.Instant.now().minus(retentionDays, java.time.temporal.ChronoUnit.DAYS);
        String query = "'" + parentId + "' in parents and trashed=false and createdTime < '"
                + cutoff.toString() + "'";
        String pageToken = null;
        do {
            var page = drive.files().list().setQ(query).setSpaces("drive")
                    .setFields("nextPageToken,files(id,name,createdTime)")
                    .setPageToken(pageToken).setPageSize(100).execute();
            if (page.getFiles() != null) {
                for (var file : page.getFiles()) {
                    if (!file.getId().equals(keepId)) drive.files().delete(file.getId()).execute();
                }
            }
            pageToken = page.getNextPageToken();
        } while (pageToken != null);
    }

    private void protectTokenFiles() {
        try (var files = Files.walk(TOKEN_DIR)) {
            files.filter(Files::isRegularFile).forEach(SecureFiles::ownerOnly);
        } catch (IOException ignored) {
        }
    }
}
