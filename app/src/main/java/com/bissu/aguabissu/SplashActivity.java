package com.bissu.aguabissu;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;

import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class SplashActivity extends AppCompatActivity {
    private Intent activityIntent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        if(firebaseAuth.getUid()==null)
            activityIntent = new Intent(SplashActivity.this, LoginActivity.class);
        else if (getSharedPreferences("USER_CREDENTIALS", MODE_PRIVATE)
                    .getString("MOBILE_NUMBER", null) == null)
        {firebaseAuth.signOut();
        activityIntent = new Intent(SplashActivity.this, LoginActivity.class);
        }
        else{//if   user    logged  in
             try {
                 Intent receivedIntent = getIntent();
                 String classname = receivedIntent.getStringExtra("intent");
                 if (classname == null)
                     activityIntent = new Intent(this, HomeUser.class);
                 else {
                     if (isNewNotification(Objects.requireNonNull(receivedIntent.getExtras()))) {
                         NotificationAction action = new NotificationAction();
                         activityIntent = new Intent(this, action.getClassName(classname, this));
                         receivedIntent.removeExtra("intent");
                         activityIntent = action.addDataInIntent(activityIntent, receivedIntent.getExtras());
                     }else
                         activityIntent = new Intent(this, HomeUser.class);
                 }
             }catch(Exception   e){
                 activityIntent = new Intent(this, HomeUser.class);
             }
        }
        Log.e("Receive","->"+getIntent().toUri(0));
        Log.e("Receive","->"+getIntent().getExtras());
    }

    private boolean isNewNotification(Bundle extras) {
        SharedPreferences pref=getSharedPreferences("USER_CREDENTIALS", MODE_PRIVATE);
        String oldMessageId=pref
                .getString("message_id", null);
        String  newMesId=extras.getString("google.message_id");
        if (oldMessageId != null&&newMesId!=null){
            if (newMesId.equals(oldMessageId))
            {   Log.e("Received","Rejected");
                return false;
            }
        }
        SharedPreferences.Editor  editor=pref.edit();
        editor.putString("message_id",extras.getString("google.message_id"));
        editor.apply();
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        new Handler().postDelayed(() -> {
            if (!isFinishing()&&activityIntent!=null) {
                Bundle  bundle= ActivityOptionsCompat.makeCustomAnimation(this,android.R.anim.fade_in,android.R.anim.fade_out).toBundle();
                activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(activityIntent,bundle);
            }
        },1500);
    }


}
