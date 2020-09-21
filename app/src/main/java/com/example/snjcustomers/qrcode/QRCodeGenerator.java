package com.example.snjcustomers.qrcode;

import android.app.Activity;
import android.graphics.Bitmap;
import android.util.Base64;
import android.util.Log;

import com.example.snjcustomers.VerifyPhoneActivity;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.io.ByteArrayOutputStream;

public class QRCodeGenerator {

    private static final String TAG = "QRGenerator";
    public QRCodeGenerator(){

    }
    public QRCodeGenerator(String data, String uid, VerifyPhoneActivity activity){
       generateQR(data,uid,activity,true);
   }
    public Bitmap generateQR(String data, String uid, Activity activity, boolean isSave) {
        Bitmap bitmap = QRCodeHelper
                .newInstance(activity)
                .setContent(data)
                .setErrorCorrectionLevel(ErrorCorrectionLevel.Q)
                .setMargin(2)
                .getQRCOde();
        if(isSave)
        {   getImageData(bitmap,uid);
            return null;
        }else
    return bitmap;
    }
       


    private void getImageData(Bitmap bmp, String uid) {

        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, bao); // bmp is bitmap from user image file
        bmp.recycle();
        byte[] byteArray = bao.toByteArray();
        String imageB64 = Base64.encodeToString(byteArray, Base64.URL_SAFE);
        Base64.decode(imageB64,Base64.URL_SAFE);

        //  store & retrieve this string which is URL safe(can be used to store in FBDB) to firebase
        Log.d(TAG,imageB64);
        Log.d(TAG,"Uploading started for storage");
        uploadImage(byteArray,uid);
    }
    private void uploadImage(byte[] byteArray, String uid){
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference("files/"+uid+"_qr");
     storageRef.putBytes(byteArray).addOnFailureListener(exception -> {
         // Handle unsuccessful uploads
         Log.d(TAG,"Image uploaded fail");
     }).addOnSuccessListener(taskSnapshot -> {
         Log.d(TAG,"Image uploaded");


     });

    }

}
