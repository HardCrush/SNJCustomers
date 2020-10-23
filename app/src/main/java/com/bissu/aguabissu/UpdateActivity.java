package com.bissu.aguabissu;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Locale;

public class UpdateActivity extends AppCompatActivity {
    private DownloadManager downloadManager;
    private boolean force;
    private String name;
    private long downloadid;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);
        downloadManager =(DownloadManager)  getSystemService(Context.DOWNLOAD_SERVICE);
        receive();

    }

    private void receive() {
        Intent intent=getIntent();
        Log.e("Update","Received->"+intent.toUri(0));
        force=intent.getBooleanExtra("FORCE_UPDATE",false);
        String size=intent.getStringExtra("APP_SIZE");
        TextView textView = findViewById(R.id.download_size);
        textView.setText(size);
        textView = findViewById(R.id.download_version);
        name=getIntent().getStringExtra("APP_NAME");
        textView.setText(name);
    }

    @Override
    public void onBackPressed() {
        if(force){
            finishAffinity();
        }else
        super.onBackPressed();
    }

    private void downloadFile() {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReferenceFromUrl
                ("gs://billrec.appspot.com/apps/"+name);
        //StorageReference  islandRef = storageRef.child("Mcafe_v_5.apk");
        findViewById(R.id.download_path).setVisibility(View.VISIBLE);
        TextView textView = findViewById(R.id.download_version);
        textView.setText(String.format("Getting download link of\n%s", name));
        storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
            download(uri,getString(R.string.app_name),textView);
        }).addOnFailureListener(Throwable::printStackTrace);
    }
    public void download(Uri url, String name,TextView textView) {
        Button button=findViewById(R.id.download_bt);
        button.setText("Downloading");
        button.setClickable(false);
        findViewById(R.id.progressbar).setVisibility(View.VISIBLE);
        textView.setText(String.format(Locale.UK, "Downloading %s", name));
       try {
           DownloadManager.Request request = new DownloadManager.Request(url);
           this.registerReceiver(attachmentDownloadCompleteReceive, new IntentFilter(
                   DownloadManager.ACTION_DOWNLOAD_COMPLETE));
           //Restrict the types of networks over whch this download may proceed.
           request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
           //Set whether this download may proceed over a roaming connection.
           request.setAllowedOverRoaming(false);
           request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
           //Set the title of this download, to be displayed in notifications (if enabled).
           request.setTitle(name);
           //Set a description of this download, to be displayed in notifications (if enabled)
           request.setDescription("Downloading");
           request.setMimeType("application/vnd.android.package-archive");
           //Set the local destination for the downloaded file to a path within the application's external files directory
//           request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,"/"+name+"/"+name+".apk");

           String destination =getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                   .toString() + "/" + name+".apk";
           Uri uri1 = Uri.parse("file://" + destination);
           File file=new File(destination);
           if (file.exists())
               file.delete();
           request.setDestinationUri(uri1);
           //Enqueue a new download and same the referenceId
         downloadid=  downloadManager.enqueue(request);
           }catch (Exception e){e.printStackTrace();}
       }
    private void requestPermission(Activity context)  {
        boolean hasPermission = (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        if (!hasPermission) {
            ActivityCompat.requestPermissions(context,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    101);
        }else{
            downloadFile();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions, @NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 101) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                downloadFile();

            }
        }
    }
    BroadcastReceiver attachmentDownloadCompleteReceive = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                TextView textView=findViewById(R.id.path_text);
                textView.setText("Downloaded At");
                textView = findViewById(R.id.download_version);
                textView.setText(String.format(Locale.UK, "Finish Downloading %s", name));
                findViewById(R.id.download_size).setVisibility(View.GONE);
                ProgressBar progressBar=findViewById(R.id.progressbar);
                Button button=findViewById(R.id.download_bt);
                button.setText("Downloaded");
                progressBar.setIndeterminate(false);
                progressBar.setProgress(100);
               try {
                   String destination =getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                           .toString() + "/" + getString(R.string.app_name)+".apk";
                   Uri uri1 = Uri.parse("file://" + destination);
                   if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                       Uri contentUri = FileProvider.getUriForFile(UpdateActivity.this,
                               BuildConfig.APPLICATION_ID + ".provider", new File(destination));
                       Intent install = new Intent(Intent.ACTION_VIEW);
                       install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                       install.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                       install.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
                       install.setData(contentUri);
                       startActivity(install);
                   } else {
                       Intent install = new Intent(Intent.ACTION_VIEW);
                       install.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                       install.setDataAndType(uri1, downloadManager.getMimeTypeForDownloadedFile(downloadid));
                        startActivity(install);
                   }
                   unregisterReceiver(attachmentDownloadCompleteReceive);
               }catch (Exception e){e.printStackTrace();}
            }
        }
    };



    public void backOnClicked(View view) {
        onBackPressed();
    }

    public void downloadStartOnClick(View view) {
        requestPermission(this);
    }

}
