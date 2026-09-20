using System;
using System.Collections.Generic;
using System.IO;
using System.Threading;
using UnityEngine;


public class FastResourceDownloader
{
    private static FastResourceDownloader instance;
    public static FastResourceDownloader Instance
    {
        get
        {
            if (instance == null)
            {
                instance = new FastResourceDownloader();
            }
            return instance;
        }
    }

    private Queue<ResourceFile> fileQueue = new Queue<ResourceFile>();
    private object queueLock = new object();
    
    private int totalFiles = 0;
    private int receivedFiles = 0;
    private int writtenFiles = 0;
    
    private static string persistentDataPath = null;
    
    private List<Thread> writeThreads = new List<Thread>();
    private const int MAX_PARALLEL_WRITES = 50;
    
    private Action onComplete;
    private bool isDownloading = false;
    private bool pendingComplete = false;
    private int pendingResVersion = 0;
    private int activeWriteCount = 0;

    private class ResourceFile
    {
        public string filename;
        public sbyte[] data;
        
        public ResourceFile(string filename, sbyte[] data)
        {
            this.filename = filename;
            this.data = data;
        }
    }


    public void StartDownload(int totalFiles, Action onCompleteCallback)
    {

        if (persistentDataPath == null)
        {
            persistentDataPath = Rms.GetiPhoneDocumentsPath();
        }
        
        lock (queueLock)
        {
            this.totalFiles = totalFiles;
            this.receivedFiles = 0;
            this.writtenFiles = 0;
            this.onComplete = onCompleteCallback;
            this.isDownloading = true;
            this.pendingComplete = false;
            this.activeWriteCount = 0;
            fileQueue.Clear();
            writeThreads.Clear();
            Res.outz("[FAST_DOWNLOAD] Bắt đầu download " + totalFiles + " files");
        }
    }





    public void AddFile(string filename, sbyte[] data)
    {
        if (!isDownloading) return;

        lock (queueLock)
        {
            receivedFiles++;
            fileQueue.Enqueue(new ResourceFile(filename, data));
            
            if (totalFiles > 0)
            {
                ServerListScreen.demPercent = receivedFiles;
                ServerListScreen.percent = receivedFiles * 100 / totalFiles;
            }
        }
        

        StartWritingFiles();
    }
    



    private void StartWritingFiles()
    {

        lock (queueLock)
        {
            for (int i = writeThreads.Count - 1; i >= 0; i--)
            {
                if (!writeThreads[i].IsAlive)
                {
                    writeThreads.RemoveAt(i);
                    activeWriteCount--;
                }
            }
        }
        

        while (true)
        {
            ResourceFile file = null;
            int currentActive = 0;
            
            lock (queueLock)
            {
                currentActive = activeWriteCount;
                if (currentActive >= MAX_PARALLEL_WRITES || fileQueue.Count == 0)
                {
                    break;
                }
                file = fileQueue.Dequeue();
            }
            
            if (file != null)
            {
                ResourceFile fileToWrite = file;
                Thread writeThread = new Thread(() =>
                {
                    try
                    {
                        WriteFileFast(fileToWrite.filename, fileToWrite.data);
                        lock (queueLock)
                        {
                            writtenFiles++;
                        }
                    }
                    catch (Exception ex)
                    {
                        Debug.LogError("[FAST_DOWNLOAD] Lỗi ghi file " + fileToWrite.filename + ": " + ex.Message);
                    }
                    finally
                    {
                        lock (queueLock)
                        {
                            activeWriteCount--;
                        }
                    }
                });
                
                writeThread.IsBackground = true;
                writeThread.Priority = System.Threading.ThreadPriority.BelowNormal;
                
                lock (queueLock)
                {
                    writeThreads.Add(writeThread);
                    activeWriteCount++;
                }
                
                writeThread.Start();
            }
            else
            {
                break;
            }
        }
    }





    public void ProcessFileQueue()
    {

        bool shouldComplete = false;
        int resVersion = 0;
        
        lock (queueLock)
        {
            if (pendingComplete && !isDownloading)
            {
                shouldComplete = true;
                resVersion = pendingResVersion;
                pendingComplete = false;
            }
        }
        
        if (shouldComplete)
        {

            OnDownloadComplete(resVersion);
            return;
        }
        
        if (!isDownloading) return;
        

        StartWritingFiles();
    }




    private void WriteFileFast(string filename, sbyte[] data)
    {
        try
        {
            if (persistentDataPath == null)
            {
                throw new Exception("persistentDataPath chưa được khởi tạo");
            }
            
            string filePath = persistentDataPath + "/" + filename;
            
            string directory = Path.GetDirectoryName(filePath);
            if (directory != null && !Directory.Exists(directory))
            {
                Directory.CreateDirectory(directory);
            }
            



            byte[] byteData = ArrayCast.cast(data);
            using (FileStream fs = new FileStream(filePath, FileMode.Create, FileAccess.Write, FileShare.None, 131072, FileOptions.WriteThrough))
            {
                fs.Write(byteData, 0, byteData.Length);

            }
            

        }
        catch (Exception ex)
        {
            Debug.LogError("[FAST_DOWNLOAD] Lỗi ghi file " + filename + ": " + ex.Message);
            throw;
        }
    }




    public void CompleteDownload(int resVersion)
    {
        if (!isDownloading) return;

        Res.outz("[FAST_DOWNLOAD] Hoàn thành nhận " + receivedFiles + "/" + totalFiles + " files, đang ghi file...");

        StartWritingFiles();
        
        Thread waitThread = new Thread(() =>
        {
            while (true)
            {
                bool hasMore = false;
                lock (queueLock)
                {
                    if (fileQueue.Count > 0)
                    {
                        hasMore = true;
                        StartWritingFiles();
                    }
                }
                
                if (!hasMore)
                {
                    break;
                }
                Thread.Sleep(10);
            }
            
            List<Thread> threadsToWait = new List<Thread>();
            lock (queueLock)
            {
                threadsToWait.AddRange(writeThreads);
            }
            
            foreach (Thread thread in threadsToWait)
            {
                if (thread.IsAlive)
                {
                    thread.Join(5000);
                }
            } 
            lock (queueLock)
            {
                isDownloading = false;
            }
            

            lock (queueLock)
            {
                isDownloading = false;
                pendingComplete = true;
                pendingResVersion = resVersion;
            }
        });
        
        waitThread.IsBackground = true;
        waitThread.Start();
    }




    private void OnDownloadComplete(int resVersion)
    {
        try
        {
            Rms.saveRMSInt("musicSize", ModFunc.musicCount);
            ModFunc.InitMusic();
            Controller.isLoadingData = false;
            Rms.saveRMSString("ResVersion", resVersion.ToString());
            Service.gI().getResource(3, null);
            GameCanvas.endDlg();
            SmallImage.loadBigRMS();
            mSystem.gcc();
            ServerListScreen.bigOk = true;
            ServerListScreen.loadScreen = true;
            GameScr.gI().loadGameScr();
            
            if (GameCanvas.currentScreen != GameCanvas.loginScr)
            {
                GameCanvas.serverScreen.switchToMe();
            }

            Res.outz("[FAST_DOWNLOAD] Download hoàn tất! Đã tải " + receivedFiles + " files");
            
            if (onComplete != null)
            {
                onComplete();
            }
        }
        catch (Exception ex)
        {
            Debug.LogError("[FAST_DOWNLOAD] Lỗi khi hoàn thành download: " + ex.Message);
        }
    }




    public bool IsDownloading()
    {
        lock (queueLock)
        {
            return isDownloading;
        }
    }
}

