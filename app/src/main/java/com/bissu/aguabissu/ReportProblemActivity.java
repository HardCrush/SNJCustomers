package com.bissu.aguabissu;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static com.bissu.aguabissu.Constants.uid;

public class ReportProblemActivity extends AppCompatActivity {
    private TextInputEditText editText;
    ArrayList<Uri> imgList;
    ImageButton img1;
    ImageButton img2;
    ImageButton img3;
    ImageButton del1;
    ImageButton del2;
    ImageButton del3;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_problem);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        toolbar.getNavigationIcon()
                .setColorFilter(ContextCompat.getColor(this,R.color.black), PorterDuff.Mode.SRC_ATOP);
        editText=findViewById(R.id.problemEdit);
        imgList= new ArrayList<>(3);
        img1=findViewById(R.id.img1);
        img2=findViewById(R.id.img2);
        img3=findViewById(R.id.img3);
        del1=findViewById(R.id.delImg1);
        del2=findViewById(R.id.delImg2);
        del3=findViewById(R.id.delImg3);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public void sendReport(View view) {
        if (editText.getText().toString().trim().isEmpty())
            showMsg("Enter the problem you faced", R.color.red, false);
        else{String id= String.valueOf(System.currentTimeMillis());
            if (imgList.isEmpty())
            {
                sendToDB(id,editText.getText().toString().trim());
            }
            else  {
                uploadImage(id);
            }
        }
    }

    private void uploadImage(String id) {
        showMsg("Uploading img...",R.color.colorAccent,true);
        ArrayList<String>uploadedImg=new ArrayList<>();
        StorageReference storageRef= new TransactionDb().getStorageReference(this).child("Problem").child(id);
        for (Uri link:imgList) {
            storageRef.child(System.currentTimeMillis() +".png").putFile(link).addOnFailureListener(exception -> {
                // Handle unsuccessful uploads
                showMsg("Failed to upload image",R.color.red,false);
            }).addOnSuccessListener(taskSnapshot -> {
                uploadedImg.add("uploaded");
                if (uploadedImg.size()==imgList.size())
                    sendToDB(id,editText.getText().toString().trim());
            });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (uid==null||uid.isEmpty())
            Constants.uid=getSharedPreferences("USER_CREDENTIALS",MODE_PRIVATE).getString("UID","not found");
    }

    private void sendToDB(String id, String problem) {
        showMsg("Sending...",R.color.colorAccent,true);
        HashMap<String,Object>map=new HashMap<>();
        map.put("MSG",problem);
        map.put("UID", uid);
        map.put("HAVE_IMG", !imgList.isEmpty());
        new TransactionDb().getDatabaseReference(this).child("Reports").child(id).setValue(map).addOnCompleteListener(task -> {
            showMsg("Problem reported successfully", R.color.colorAccent, false);
            new Handler().postDelayed(() -> {
                if (!isFinishing())
                    finish();
            }, 1000);
        });
    }

    private void showMsg(String msg,int color, boolean isIndefinite){
        Snackbar snackbar=Snackbar.make(findViewById(R.id.parent),msg,Snackbar.LENGTH_LONG);
        if (isIndefinite)
            snackbar.setDuration(Snackbar.LENGTH_INDEFINITE);
        snackbar.setBackgroundTint(ContextCompat.getColor(this,color));
        snackbar.setTextColor(ContextCompat.getColor(this,R.color.white));
        snackbar.show();
    }

    private void showImageChooser(int code) {
        if (ActivityCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
//            Intent intent = new Intent();
//            intent.setType("image/*");
//            intent.setAction(Intent.ACTION_GET_CONTENT);
//            startActivityForResult(Intent.createChooser(intent, "Select Image"), code);
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent,code);
        }else{
            ActivityCompat.requestPermissions(this,new String[]{READ_EXTERNAL_STORAGE},code);
        }
    }
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode >= 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            if (!imgList.contains(data.getData())) {
                Uri uri = data.getData();
                setImage(uri, requestCode);
            }
        }
    }

    private void setImage(Uri uri, int requestCode) {
        ImageButton img;
        ImageButton delImg;
        if (imgList.isEmpty()) {
          img=img1;
          delImg=del1;
          imgList.add(uri);
        }else   if(requestCode==1){
            imgList.set(0,uri);
            img=img1;
            delImg=del1;
        }else   if(imgList.size()==1){
            img=img2;
            delImg=del2;
            imgList.add(uri);
        }
        else if (requestCode==2){
            img=img2;
            delImg=del2;
            imgList.set(1,uri);
        }else   if (imgList.size()==2)
        {
            img=img3;
            delImg=del3;
            imgList.add(uri);
        }
        else{
            img=img3;
            delImg=del3;
            imgList.set(2,uri);
        }
        Glide.with(getApplicationContext()).asBitmap()
                .load(uri).into(img);
        delImg.setVisibility(View.VISIBLE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0]==PackageManager.PERMISSION_GRANTED){
            showImageChooser(1);
        }else{
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this,permissions[0])){
                Snackbar    snackbar=Snackbar.make(findViewById(R.id.parent),"Require storage permission",Snackbar.LENGTH_LONG);
                snackbar.setAction("Settings", v -> {
                    Intent  intent=new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.addCategory(Intent.CATEGORY_DEFAULT);
                    intent.setData(Uri.parse("package:"+getPackageName()));
                    startActivity(intent);
                });
                snackbar.setBackgroundTint(getResources().getColor(R.color.colorAccent));
                snackbar.setTextColor(getResources().getColor(R.color.white));
                snackbar.setActionTextColor(getResources().getColor(R.color.white));
                snackbar.show();
            }
        }
    }

    public void uploadImg1Clicked(View view) {
        showImageChooser(1);
    }

    public void uploadImg2Clicked(View view) {
        showImageChooser(2);
    }

    public void uploadImg3Clicked(View view) {
        showImageChooser(3);
    }

    public void delImg3(View view) {
        delImg(img3,del3,2);
    }

    public void delImg2(View view) {
        delImg(img2,del2,1);
    }

    public void delImg1(View view) {
        delImg(img1,del1,0);
    }
    void delImg(ImageButton imgId,ImageButton delImgId,int position){
        try {
            delImgId.setVisibility(View.GONE);
            Glide.with(getApplicationContext())
                    .load(getDrawable(R.drawable.ic_baseline_add_circle_outline_24)).into(imgId);
            imgList.remove(position);
        }catch (Exception e){e.printStackTrace();}
    }

    public void backOnClicked(View view) {
        onBackPressed();
    }
}
