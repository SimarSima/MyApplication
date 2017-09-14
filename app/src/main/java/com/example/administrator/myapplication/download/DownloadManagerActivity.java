package com.example.administrator.myapplication.download;

import android.app.DownloadManager;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileObserver;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.administrator.myapplication.R;

public class DownloadManagerActivity extends AppCompatActivity {
    private ProgressBar pbMain;
    private TextView tvStatus;
    private TextView tvPercent;
    private long downloadId=0;
    private DownloadManager downloadManager;
    private FileObserver fileObserver;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_manager);
        pbMain= (ProgressBar) findViewById(R.id.pb_download_main);
        tvPercent= (TextView) findViewById(R.id.tv_download_percent);
        tvStatus= (TextView) findViewById(R.id.tv_download_status);
        Log.d("Simar", Environment.getExternalStorageDirectory()+"/vfm/auto_update/vfm.apk");
        fileObserver = new DownloadObserver(
                Environment.getExternalStorageDirectory()+"/vfm/auto_update");
        fileObserver.startWatching();
        download();

    }

    private  void download(){
        String apkUrl = "http://182.254.135.222/apkdownload/vfm_demo_old.apk";
        downloadManager= (DownloadManager) this.getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(apkUrl));
        request.setDestinationInExternalPublicDir("vfm/auto_update", "vfm.apk");
        request.setNotificationVisibility(View.VISIBLE);
        Log.d("Simar","Start Download");
        downloadId = downloadManager.enqueue(request);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        fileObserver.stopWatching();
    }

    public int[] checkStatus(){
        int []bytesAndStatus=new int[]{ -1, -1, 0 };
        DownloadManager.Query query = new DownloadManager.Query().setFilterById(downloadId);
        Cursor c = null;
        try {
            c = downloadManager.query(query);
            if (c != null && c.moveToFirst()) {
                bytesAndStatus[0] = c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                bytesAndStatus[1] = c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                bytesAndStatus[2] = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }
        Log.d("simar","COLUMN_BYTES_DOWNLOADED_SO_FAR: "+bytesAndStatus[0]);
        Log.d("simar","COLUMN_TOTAL_SIZE_BYTES: "+bytesAndStatus[1]);
        Log.d("simar","COLUMN_STATUS: "+bytesAndStatus[2]);
        return bytesAndStatus;
    }
    class DownloadObserver extends FileObserver {
        private static final int flags =
                FileObserver.CLOSE_WRITE
                        | FileObserver.OPEN
                        | FileObserver.MODIFY
                        | FileObserver.DELETE
                        | FileObserver.MOVED_FROM;
        public DownloadObserver(String path) {
            super(path);
        }

        @Override
        public void onEvent(int event, String path) {
            Log.d("Simar", "onEvent(" + event + ", " + path + ")");

            if (path == null) {
                return;
            }

            switch (event) {
                case FileObserver.CLOSE_WRITE:
                    // Download complete, or paused when wifi is disconnected. Possibly reported more than once in a row.
                    // Useful for noticing when a download has been paused. For completions, register a receiver for
                    // DownloadManager.ACTION_DOWNLOAD_COMPLETE.
                    break;
                case FileObserver.OPEN:
                    // Called for both read and write modes.
                    // Useful for noticing a download has been started or resumed.
                    break;
                case FileObserver.DELETE:
                case FileObserver.MOVED_FROM:
                    // These might come in handy for obvious reasons.
                    break;
                case FileObserver.MODIFY:
                    // Called very frequently while a download is ongoing (~1 per ms).
                    // This could be used to trigger a progress update, but that should probably be done less often than this.
                    checkStatus();
                    break;
            }
        }
        }
}
