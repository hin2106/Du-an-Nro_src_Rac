package data;
import interfaces.ISession;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import network.Message;
import network.MySession;
import network.ResourceSendLimiter;
import server.Manager;
import services.Service;
import skill.NClass;
import skill.Skill;
import system.Template.BgItem;
import system.Template.HeadAvatar;
import system.Template.MapTemplate;
import system.Template.MobTemplate;
import system.Template.NpcTemplate;
import system.Template.SkillTemplate;
import utils.FileIO;
import utils.Logger;

public class DataGame {

    public static byte vsData = 9;
    public static byte vsMap = 2;
    public static byte vsSkill = 1;
    public static byte vsItem = 9;
    public static int vsRes = 1;
    public static short maxSmallVersion = 32767;

    public static String LINK_IP_PORT = "Ngọc Rồng Online:36.50.134.190:14445:0";
    public static Map<Object, Object> MAP_MOUNT_NUM = new HashMap<>();

    private static final ConcurrentHashMap<String, byte[]> FILE_CACHE = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, byte[]> ASSET_VERSION_CACHE = new ConcurrentHashMap<>();

    private static byte[] readCached(String path) {
        return FILE_CACHE.computeIfAbsent(path, p -> FileIO.readFile(p));
    }

    private static byte[] getAssetVersions(String directory, int count) {
        String cacheKey = directory + ':' + count;
        return ASSET_VERSION_CACHE.computeIfAbsent(cacheKey, key -> {
            byte[] versions = new byte[count];
            java.util.Arrays.fill(versions, (byte) 255);
            File assetDirectory = new File(directory);
            File[] files = assetDirectory.listFiles((dir, name) -> name.endsWith(".png"));
            if (files != null) {
                for (File file : files) {
                    String name = file.getName();
                    try {
                        int id = Integer.parseInt(name.substring(0, name.length() - 4));
                        if (id >= 0 && id < count && file.isFile()) {
                            versions[id] = (byte) ((file.length() ^ file.lastModified()) % 251);
                        }
                    } catch (NumberFormatException ignored) {
                        // Asset khong dat ten bang ID thi khong thuoc protocol icon.
                    }
                }
            }
            return versions;
        });
    }

    /**
     * Tao truoc bang version asset luc khoi dong. Neu de den lan dang nhap dau
     * tien moi tao, nguoi choi dau tien se phai cho viec quet hang chuc nghin file.
     */
    public static void warmAssetVersionCaches() {
        long startedAt = System.currentTimeMillis();
        getAssetVersions("data/icon/x4", maxSmallVersion);
        int maxBgImageId = -1;
        for (BgItem bgItem : Manager.BG_ITEMS) {
            maxBgImageId = Math.max(maxBgImageId, bgItem.idImage);
        }
        getAssetVersions("data/item_bg_temp/x4", maxBgImageId + 1);
        Logger.log("Da tao cache version asset x4 trong "
                + (System.currentTimeMillis() - startedAt) + "ms\n");
    }

    public static void sendVersionGame(MySession session) {
        Message msg;
        try {
            msg = Service.gI().messageNotMap((byte) 4);
            msg.writer().writeByte(vsData);
            msg.writer().writeByte(vsMap);
            msg.writer().writeByte(vsSkill);
            msg.writer().writeByte(vsItem);
            msg.writer().writeByte(0);

            long[] smtieuchuan = { 1000L, 3000L, 15000L, 40000L, 90000L, 170000L, 340000L, 700000L,
                    1500000L, 15000000L, 150000000L, 1500000000L, 5000000000L, 10000000000L, 40000000000L,
                    50010000000L, 60010000000L, 70010000000L, 80010000000L, 100010000000L, 1000010000000L,
                    10000010000000L };
            msg.writer().writeByte(smtieuchuan.length);
            for (int i = 0; i < smtieuchuan.length; i++) {
                msg.writer().writeLong(smtieuchuan[i]);
            }
            session.sendMessage(msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }

    // vData
    public static void updateData(MySession session) {
        final byte[] dart = readCached("data/update_data/dart");
        final byte[] arrow = readCached("data/update_data/arrow");
        final byte[] effect = readCached("data/update_data/effect");
        final byte[] image = readCached("data/update_data/image");
        final byte[] part = readCached("data/update_data/part");
        final byte[] skill = readCached("data/update_data/skill");

        Message msg;
        try {
            msg = new Message(-87);
            msg.writer().writeByte(vsData);
            msg.writer().writeInt(dart.length);
            msg.writer().write(dart);
            msg.writer().writeInt(arrow.length);
            msg.writer().write(arrow);
            msg.writer().writeInt(effect.length);
            msg.writer().write(effect);
            msg.writer().writeInt(image.length);
            msg.writer().write(image);
            msg.writer().writeInt(part.length);
            msg.writer().write(part);
            msg.writer().writeInt(skill.length);
            msg.writer().write(skill);

            session.doSendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    // vMap
    public static void updateMap(MySession session) {
        Message msg;
        try {
            msg = Service.gI().messageNotMap((byte) 6);
            msg.writer().writeByte(vsMap);
            msg.writer().writeShort(Manager.MAP_TEMPLATES.length);
            for (MapTemplate temp : Manager.MAP_TEMPLATES) {
                msg.writer().writeUTF(temp.name);
            }
            msg.writer().writeByte(Manager.NPC_TEMPLATES.size());
            for (NpcTemplate temp : Manager.NPC_TEMPLATES) {
                msg.writer().writeUTF(temp.name);
                msg.writer().writeShort(temp.head);
                msg.writer().writeShort(temp.body);
                msg.writer().writeShort(temp.leg);
                msg.writer().writeByte(0);
            }
            msg.writer().writeShort(Manager.MOB_TEMPLATES.size());
            for (MobTemplate temp : Manager.MOB_TEMPLATES) {
                msg.writer().writeByte(temp.type);
                msg.writer().writeUTF(temp.name);
                msg.writer().writeLong(temp.hp);
                msg.writer().writeByte(temp.rangeMove);
                msg.writer().writeByte(temp.speed);
                msg.writer().writeByte(temp.dartType);
            }
            session.doSendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(DataGame.class, e);
        }
    }

    // vSkill
    public static void updateSkill(MySession session) {
        Message msg;
        try {
            msg = new Message(-28);

            msg.writer().writeByte(7);
            msg.writer().writeByte(vsSkill);
            msg.writer().writeByte(0); // count skill option

            msg.writer().writeByte(Manager.NCLASS.size());
            for (NClass nClass : Manager.NCLASS) {
                msg.writer().writeUTF(nClass.name);

                msg.writer().writeByte(nClass.skillTemplatess.size());
                for (SkillTemplate skillTemp : nClass.skillTemplatess) {
                    msg.writer().writeByte(skillTemp.id);
                    msg.writer().writeUTF(skillTemp.name);
                    msg.writer().writeByte(skillTemp.maxPoint);
                    msg.writer().writeByte(skillTemp.manaUseType);
                    msg.writer().writeByte(skillTemp.type);
                    msg.writer().writeShort(skillTemp.iconId);
                    msg.writer().writeUTF(skillTemp.damInfo);
                    msg.writer().writeUTF("NRO");

                    if (skillTemp.id != 0) {
                        msg.writer().writeByte(skillTemp.skillss.size());
                        for (Skill skill : skillTemp.skillss) {
                            msg.writer().writeShort(skill.skillId);
                            msg.writer().writeByte(skill.point);
                            msg.writer().writeLong(skill.powRequire);
                            msg.writer().writeShort(skill.manaUse);
                            msg.writer().writeInt(skill.coolDown);
                            msg.writer().writeShort(skill.dx);
                            msg.writer().writeShort(skill.dy);
                            msg.writer().writeByte(skill.maxFight);
                            msg.writer().writeShort(skill.damage);
                            msg.writer().writeShort(skill.price);
                            msg.writer().writeUTF(skill.moreInfo);
                        }
                    } else {
                        // Thêm 2 skill trống 105, 106
                        msg.writer().writeByte(skillTemp.skillss.size() + 2);
                        for (Skill skill : skillTemp.skillss) {
                            msg.writer().writeShort(skill.skillId);
                            msg.writer().writeByte(skill.point);
                            msg.writer().writeLong(skill.powRequire);
                            msg.writer().writeShort(skill.manaUse);
                            msg.writer().writeInt(skill.coolDown);
                            msg.writer().writeShort(skill.dx);
                            msg.writer().writeShort(skill.dy);
                            msg.writer().writeByte(skill.maxFight);
                            msg.writer().writeShort(skill.damage);
                            msg.writer().writeShort(skill.price);
                            msg.writer().writeUTF(skill.moreInfo);
                        }
                        for (int i = 105; i <= 106; i++) {
                            msg.writer().writeShort(i);
                            msg.writer().writeByte(0);
                            msg.writer().writeLong(0);
                            msg.writer().writeShort(0);
                            msg.writer().writeInt(0);
                            msg.writer().writeShort(0);
                            msg.writer().writeShort(0);
                            msg.writer().writeByte(0);
                            msg.writer().writeShort(0);
                            msg.writer().writeShort(0);
                            msg.writer().writeUTF("<3");

                        }
                    }
                }
            }
            session.doSendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(DataGame.class, e);
        }
    }

    public static void sendDataImageVersion(MySession session) {
        Message msg;
        try {
            // msg = new Message(-111);
            // msg.writer().writeShort(0);
            // msg.writer().writeUTF("NguyenDucVuEntertainment");
            // msg.writer().writeByte(0);
            // msg.writer().writeUTF("NgocRongWhis");
            // msg.writer().writeByte(1);
            // msg.writer().writeUTF("VuDangCapVaiLonRaMaBanDeoBietThoiDitMeBan");
            // msg.writer().writeByte(2);
            // session.doSendMessage(msg);
            // msg.cleanup();
        } catch (Exception e) {
            Logger.logException(DataGame.class, e);
        }
    }

    public static void sendEffectTemplate(MySession session, int id, int... idtemp) {
        int idT = id;
        if (idtemp.length > 0 && idtemp[0] != 0) {
            idT = idtemp[0];
        }
        Message msg;
        try {
            final byte[] effData = FileIO.readFile("data/effdata/DataEffect_" + idT);
            final byte[] effImg = FileIO.readFile("data/effect/x" + session.zoomLevel + "/ImgEffect_" + idT + ".png");
            if (effData == null || effImg == null) {
                return;
            }
            msg = new Message(-66);
            msg.writer().writeShort(id);
            msg.writer().writeInt(effData.length);
            msg.writer().write(effData);
            if (session.version > 220) {
                msg.writer().write(idT == 60 ? 2 : 0);
            }
            msg.writer().writeInt(effImg.length);
            msg.writer().write(effImg);
            session.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendBgItemVersion(MySession session) {
        Message msg;
        try {
            int maxImageId = -1;
            for (BgItem bgItem : Manager.BG_ITEMS) {
                maxImageId = Math.max(maxImageId, bgItem.idImage);
            }
            byte[] versions = getAssetVersions("data/item_bg_temp/x" + session.zoomLevel, maxImageId + 1);
            msg = new Message(-93);
            msg.writer().writeShort(versions.length);
            for (byte version : versions) {
                msg.writer().writeByte(version);
            }
            session.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(DataGame.class, e);
        }
    }

    public static void sendItemBGTemplate(MySession session, int id) {
        Message msg;
        try {
            final byte[] bg_temp = readCached("data/item_bg_temp/x" + session.zoomLevel + "/" + id + ".png");
            if (bg_temp == null) {
                return;
            }
            msg = new Message(-32);
            msg.writer().writeShort(id);
            msg.writer().writeInt(bg_temp.length);
            msg.writer().write(bg_temp);
            session.doSendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(DataGame.class, e);
        }
    }

    public static void sendDataItemBG(MySession session) {
        Message msg;
        try {
            msg = new Message(-31);
            msg.writer().writeShort(Manager.BG_ITEMS.size());
            for (BgItem bgItem : Manager.BG_ITEMS) {
                msg.writer().writeShort(bgItem.idImage);
                msg.writer().writeByte(bgItem.layer);
                msg.writer().writeShort(bgItem.dx);
                msg.writer().writeShort(bgItem.dy);
                msg.writer().writeByte(0);
            }
            session.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public static void sendIcon(MySession session, int id) {
        Message msg;
        try {
            final byte[] icon = readCached("data/icon/x" + session.zoomLevel + "/" + id + ".png");
            if (icon == null) {
                return;
            }
            msg = new Message(-67);
            msg.writer().writeInt(id);
            msg.writer().writeInt(icon.length);
            msg.writer().write(icon);
            session.doSendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public static void sendSmallVersion(MySession session) {
        Message msg;
        try {
            byte[] versions = getAssetVersions("data/icon/x" + session.zoomLevel, maxSmallVersion);
            msg = new Message(-77);
            msg.writer().writeShort(versions.length);
            for (byte version : versions) {
                msg.writer().writeByte(version);
            }
            session.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public static void requestMobTemplate(MySession session, int id) {
        Message msg;
        try {
            // if (!session.check && id > 106) {
            // byte[] mob = FileIO.readFile("data/mob/x" + session.zoomLevel + "/" + 0);
            // msg = new Message(11);
            // msg.writer().writeByte(id);
            // msg.writer().write(mob);
            // session.sendMessage(msg);
            // msg.cleanup();
            // return;
            // }
            final byte[] mobData = readCached("data/mob/Data/" + id);

            final byte[] mobImage = readCached("data/mob/Image/x" + session.zoomLevel + "/" + id + ".png");
            if (mobData == null || mobImage == null) {
                return;
            }
            msg = new Message(11);
            msg.writer().writeShort(id);
                                    msg.writer().writeByte(0);

            msg.writer().writeInt(mobData.length);
                        msg.writer().write(mobData);
              msg.writer().writeInt(mobImage.length);
                        msg.writer().write(mobImage);
                                                            msg.writer().writeByte(0);

                        
            session.doSendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public static void sendTileSetInfo(MySession session) {
        Message msg;
        try {
            final byte[] data = readCached("data/map/tile_set_info");
            msg = new Message(-82);
            msg.writer().write(data);
            session.doSendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    // data vẽ map
    public static void sendMapTemp(MySession session, int id) {
        Message msg;
        try {
            final byte[] data = readCached("data/map/tile_map_data/" + id);
            if (data == null) {
                return;
            }
            msg = new Message(-28);
            msg.writer().writeByte(10);
            msg.writer().write(data);
            session.doSendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(DataGame.class, e);
        }
    }

    // head-avatar
    public static void sendHeadAvatar(Message msg) {
        try {
            msg.writer().writeShort(Manager.HEAD_AVATARS.size());
            for (HeadAvatar ha : Manager.HEAD_AVATARS) {
                msg.writer().writeShort(ha.headId);
                msg.writer().writeShort(ha.avatarId);
            }
        } catch (Exception e) {
        }
    }

    public static void sendImageByName(MySession session, String imgName) {
        Message msg;
        try {
            msg = new Message(66);
            msg.writer().writeUTF(imgName);
            msg.writer().writeByte(Manager.getNFrameImageByName(imgName));
            final byte[] data = readCached("data/img_by_name/x" + session.zoomLevel + "/" + imgName + ".png");
            if (data == null) {
                msg.writer().writeInt(0);
                session.doSendMessage(msg);
                msg.cleanup();
                return;
            }
            msg.writer().writeInt(data.length);
            msg.writer().write(data);
            session.doSendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public static void sendVersionRes(ISession session) {
        Message msg;
        try {
            msg = new Message(-74);
            msg.writer().writeByte(0);
            msg.writer().writeInt(vsRes);
            session.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public static void sendSizeRes(MySession session) {
        Message msg;
        try {
            msg = new Message(-74);
            msg.writer().writeByte(1);
            final File[] files = new File("data/res/x" + session.zoomLevel).listFiles();
            if (files != null) {
                msg.writer().writeShort(files.length);
            } else {
                msg.writer().writeShort(0);
            }
            session.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public static void sendRes(MySession session) {
        if (session == null) {
            return;
        }
        if (!network.ResourceSendLimiter.gI().tryAcquire(5000)) {
            try {
                Message msg = new Message(-74);
                msg.writer().writeByte(4); // Error code: server quá tải
                msg.writer().writeUTF("Server đang quá tải, vui lòng thử lại sau vài giây");
                session.sendMessage(msg);
                msg.cleanup();
            } catch (Exception e) {
                Logger.logException(DataGame.class, e, "Error sending resource overload message");
            }
            Logger.warningln("Resource send rejected - server overloaded. IP: " + session.ipAddress);
            return;
        }

        try {
            File dir = new File("data/res/x" + session.zoomLevel);
            File[] files = dir.listFiles();
            if (files == null || files.length == 0) {
                return;
            }
            for (File fileEntry : files) {
                if (fileEntry == null || !fileEntry.isFile()) {
                    continue;
                }
                Message msg = null;
                try {
                    // Kiểm tra session còn connected không
                    if (!session.isConnected()) {
                        break;
                    }

                    String original = fileEntry.getName();
                    byte[] res = FileIO.readFile(fileEntry.getAbsolutePath());

                    msg = new Message(-74);
                    msg.writer().writeByte(2);
                    msg.writer().writeUTF(original);
                    msg.writer().writeInt(res.length);
                    msg.writer().write(res);
                    session.sendMessage(msg);
                } catch (Exception e) {
                    Logger.logException(DataGame.class, e, "Error sending resource: " + fileEntry.getName());
                } finally {
                    if (msg != null) {
                        msg.cleanup();
                    }
                }
            }

            // Gửi version resource cuối cùng
            Message msg = null;
            try {
                if (session.isConnected()) {
                    msg = new Message(-74);
                    msg.writer().writeByte(3);
                    msg.writer().writeInt(vsRes);
                    session.sendMessage(msg);
                }
            } catch (Exception e) {
                Logger.logException(DataGame.class, e, "Error sending version resource");
            } finally {
                if (msg != null) {
                    msg.cleanup();
                }
            }
        } finally {
            ResourceSendLimiter.gI().release();
        }
    }

    public static void sendLinkIP(MySession session) {
        try {
            Message msg = new Message(-29);
            msg.writer().writeByte(2);
            msg.writer().writeUTF(LINK_IP_PORT + ",0,0");
            msg.writer().writeByte(1);
            session.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
