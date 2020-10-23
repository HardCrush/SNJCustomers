package com.bissu.aguabissu;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import static com.bissu.aguabissu.Constants.hideNotificationBar;
import static com.bissu.aguabissu.Constants.uid;
import static com.bissu.aguabissu.Constants.userImgChanged;

public class HomeUser extends AppCompatActivity {
    NotificationAction notificationAction;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        notificationAction=new NotificationAction();
        SharedPreferences sh=getSharedPreferences("USER_CREDENTIALS",MODE_PRIVATE);
        Constants.uid=sh.getString("UID","not found");
        String  name=sh.getString("NAME","");
        FirebaseAnalytics.getInstance(this).logEvent("user_home_screen",null);
        String  img=sh.getString("PROFILE_IMG","");
        checkReceived();
        loadUserImage(name,img);
        checkUpdate();
        notificationAction. checkNewNotification(this,getSupportFragmentManager());
        findViewById(R.id.nameLayout).setOnClickListener(this::showQROnClicked);
    }

    private void checkReceived() {
        Intent  intent=getIntent();
        String title=intent.getStringExtra("TITLE");
        String desc=intent.getStringExtra("DESC");
        if (title!=null&&desc!=null){
          notificationAction. openBottomBar(notificationAction.addDataInMap(intent.getExtras()),false,getSupportFragmentManager());
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (userImgChanged)
        {   SharedPreferences sh=getSharedPreferences("USER_CREDENTIALS",MODE_PRIVATE);
            String  name=sh.getString("NAME","");
            String  img=sh.getString("PROFILE_IMG","");
            loadUserImage(name, img);
            userImgChanged=false;
        }
        if (hideNotificationBar){
            hideNotificationBar=false;
            findViewById(R.id.frame_bottom).setVisibility(View.GONE);
            findViewById(R.id.frame_fixed).setVisibility(View.GONE);
        }
    }

    private void loadUserImage(String name, String img) {
    try {
        TextView textView = findViewById(R.id.username);
        textView.setVisibility(View.VISIBLE);
        if (!name.isEmpty()) textView.setText(name.substring(0, 1));
        if (img==null||img.isEmpty()) {
            img = "https://firebasestorage.googleapis.com/v0/b/billrec.appspot.com/o/" +
                    "files%2F" + uid + "_pr?alt=media&token=" + "479a3ebb-6ef0-4ef8-b111-3e30fa2efb";
            Glide.with(this)
                    .load(img)
                    //.apply(RequestOptions.circleCropTransform())
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .apply(RequestOptions.circleCropTransform()).listener(new RequestListener<Drawable>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                    return false;
                }

                @Override
                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                    textView.setVisibility(View.GONE);
                    return false;
                }
            }).into((ImageView) findViewById(R.id.userimage));
        }
        else {
            Glide.with(this)
                    .load(img)
                    .apply(RequestOptions.circleCropTransform())
                    .listener(new RequestListener<Drawable>() {

                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            textView.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .into((ImageView) findViewById(R.id.userimage));
        }
    }catch (Exception e){e.printStackTrace();
        findViewById(R.id.nameLayout).setVisibility(View.GONE);
    }
    }
    public void checkUpdate() {
        FirebaseDatabase.getInstance().getReference("Admin").child("app").child("Update").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    if(dataSnapshot.exists()&&(long)dataSnapshot.child("APP_VERSION").getValue()>Long.parseLong(getString(R.string.version))&&
                            (boolean)dataSnapshot.child("SHOW").getValue()){
                        boolean forceUpdate=(boolean)dataSnapshot.child("FORCE_UPDATE").getValue();
                        if (forceUpdate)
                             update((String)dataSnapshot.child("APP_NAME").getValue() ,
                              (String)dataSnapshot.child("APP_SIZE").getValue());
                        else
                           update(notificationAction
                                   .addDataInMap((HashMap<String, Object>) dataSnapshot.getValue()));
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void update(HashMap<String, Object> data) {
        Object  style=  data.get("STYLE");
        if(style!=null){
            long    type=(Long)style;
            if (type == NotificationAction.BOTTOM_BAR)
              notificationAction.  openBottomBar(data, true, getSupportFragmentManager());
            else if (type == NotificationAction.INFO_BOX) {
               //notificationAction. openBottomInfo(this, data, null, null);
                notificationAction. openBottomInfoFragment(this, data, getSupportFragmentManager());
            }
            else if (type == NotificationAction.NOTIFICATION_DIALOG)
              notificationAction.  openNotificationDialog(data,this);
        }else
      notificationAction.  openBottomBar(data,false,getSupportFragmentManager());
    }

    private void update(String name, String size) {
        Intent intent=new Intent(this,UpdateActivity.class);
        intent.putExtra("APP_NAME",name);
        intent.putExtra("FORCE_UPDATE",true);
        intent.putExtra("APP_SIZE",size);
        startActivity(intent);
    }

    public void showQROnClicked(View view) {
        startActivity(new Intent(this, UserInfoActivity.class));
    }

    public void transactionOnClick(View view) {
        Intent intent=new Intent(this, StatsActivity.class);
        intent.putExtra("CLASS_NAME","Transactions");
        startActivity(intent);
    }

    public void orderOnClick(View view) {
        Intent intent=new Intent(this, StatsActivity.class);
        intent.putExtra("CLASS_NAME","Orders");
        startActivity(intent);
    }

    public void settingsOnClick(View view) {
        startActivity(new Intent(this,SettingsActivity.class));
    }

    public void BottleOnClick(View view) {
        startActivity(new Intent(this,StatsBottleActivity.class));
    }

    public void searchOnClick(View view) {
        startActivity(new Intent(this,SearchActivity.class));
    }

    public void showAppInfo(View view) {
        startActivity(new Intent(this,AppInfoActivity.class));
    }
}
