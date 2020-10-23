package com.bissu.aguabissu;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.TypedValue;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static com.bissu.aguabissu.Constants.getCustomViewData;
import static com.bissu.aguabissu.Constants.uid;

public class AppInfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_info);
        if(uid==null)
            Constants.uid=getSharedPreferences("USER_CREDENTIALS",MODE_PRIVATE).getString("UID","not found");

        checkUpdate();
        ((TextView)findViewById(R.id.version)).setText(String.format("Version %s", getString(R.string.display_version)));
        CheckBox    checkBox=findViewById(R.id.notificationCheckbox);
        SharedPreferences   sh=getSharedPreferences("USER_CREDENTIALS",MODE_PRIVATE);
        checkBox.setChecked(sh.getBoolean("ALLOW_NOTIFICATION",
                        true));
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor    editor=sh.edit();
            editor.putBoolean("ALLOW_NOTIFICATION",isChecked);
            editor.apply();
        });
        setCustomView();
    }

    public void checkUpdate() {
        FirebaseDatabase.getInstance()
        .getReference("Admin").child("app").child("Update")
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    findViewById(R.id.progressbar).setVisibility(View.GONE);
                    if((long)dataSnapshot.child("APP_VERSION").getValue()>Long.parseLong(getString(R.string.version))){
                        update((String)dataSnapshot.child("APP_NAME").getValue() ,
                                (String)dataSnapshot.child("APP_SIZE").getValue(),(boolean)dataSnapshot.child("FORCE_UPDATE").getValue());
                    }else{
                        ((TextView)findViewById(R.id.updateNote)).setText("Latest version is installed");
                    }
                }catch (Exception e){
                    ((TextView)findViewById(R.id.updateNote)).setText("Latest version is installed");
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void update(String name, String size, boolean force_update) {
        ((TextView)findViewById(R.id.updateNote)).setText("New update is available");
        findViewById(R.id.updateBt).setVisibility(View.VISIBLE);
        findViewById(R.id.updateBt).setOnClickListener(v -> {
            Intent intent=new Intent(AppInfoActivity.this,UpdateActivity.class);
            intent.putExtra("APP_NAME",name);
            intent.putExtra("FORCE_UPDATE",force_update);
            intent.putExtra("APP_SIZE",size);
            startActivity(intent);
        });

    }

    void setCustomView(){
       try {
           SharedPreferences preferences = getSharedPreferences("CUSTOM_VIEW", MODE_PRIVATE);
           HashMap<String, String> map = (HashMap<String, String>) preferences.getAll();
           if (!map.isEmpty()) {
               LinearLayout parent = findViewById(R.id.custom_views);
               int  size=map.size()-1;
               int  i=0;
               for (String  s : map.values()) {
                   ArrayList<String> list = new ArrayList<>(Arrays.asList(s.split("->")));
                   MaterialButton bt = new MaterialButton(this);
                   bt.setCornerRadius(dpToPx(15));
                   bt.setText(list.get(0));
                   bt.setAllCaps(false);
                   bt.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.disable_color)));
                   LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dpToPx(300),
                           LinearLayout.LayoutParams.WRAP_CONTENT);
//                   params.setMargins(0, dpToPx(10), 0, 0);
                   params.topMargin=dpToPx(10);
                   if (size==i)
                      params.bottomMargin=dpToPx(10);
                   i++;
                   bt.setLayoutParams(params);
                   bt.setOnClickListener(v -> {
                       Intent intent = new Intent(AppInfoActivity.this, new NotificationAction()
                               .getClassName(list.get(1), AppInfoActivity.this));
                       if (list.get(1).contains("WEB")) {
                           intent.putExtra("TITLE", list.get(0));
                           intent.putExtra("HTML", list.get(2));
                       }
                       startActivity(intent);
                   });
                   parent.addView(bt);
               }

           }
           getCustomViewData(this);
       }catch (Exception    e){e.printStackTrace();}

    }


    public void showAppInfo(View view) {
        Intent  intent=new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setData(Uri.parse("package:"+getPackageName()));
        startActivity(intent);
    }

    public void backOnClicked(View view) {
        onBackPressed();
    }


    private int dpToPx(int dp) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }


}