package com.example.snjcustomers;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Locale;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatRatingBar;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import static com.example.snjcustomers.Constants.uid;


public class SettingsActivity extends AppCompatActivity {
    char from;
    private long total;
    private double sum_rate;
    Button bt;
    boolean isEditVisible=false;
    private ProgressBar progressBar;
    private ImageView imageView;
    private TextView errorText;
    private AppCompatEditText editText;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        if(uid==null)
            Constants.uid=getSharedPreferences("USER_CREDENTIALS",MODE_PRIVATE).getString("UID","not found");
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        toolbar.getNavigationIcon()
                .setColorFilter(ContextCompat.getColor(this,R.color.black), PorterDuff.Mode.SRC_ATOP);
        setTitle(R.string.settings);
        bt=findViewById(R.id.action_button);
        editText=findViewById(R.id.edit_data);

        String link="https://firebasestorage.googleapis.com/v0/b/billrec.appspot.com/o/"+
                "app_qr.png?alt=media&token=76b58f3e-a30f-4a14-b8bf-db97b488d41d";
        loadImage(link);
        StorageReference stRef = FirebaseStorage.getInstance().getReferenceFromUrl(link);
        stRef.getMetadata().addOnSuccessListener(storageMetadata -> {
            String url=storageMetadata.getCustomMetadata("url");
            if (url!=null&& !url.isEmpty())
            {
                TextView textView1 =findViewById(R.id.download_app_text);
                textView1.append("\nor goto "+url);
            }
        });
        Glide.with(this).load(link)
                .diskCacheStrategy(DiskCacheStrategy.NONE).
        skipMemoryCache(true).into((ImageView) findViewById(R.id.qrimage));

        loadRating(false, 0);

        extractDetail();
        try {
            new CustomScreen(R.string.custom_user_info_screen,this,findViewById(R.id.recyclerview));
        }catch (Exception e){
            FirebaseCrashlytics.getInstance().recordException(e);
        }
    }

    void loadImage(String link){
        if (progressBar==null)
            progressBar=findViewById(R.id.progress_img);
        progressBar.setVisibility(View.VISIBLE);
        if (imageView==null)
            imageView=findViewById(R.id.qrimage);
        if (errorText==null)
            errorText=findViewById(R.id.error_text);
        Glide.with(this).load(link)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        progressBar.setVisibility(View.GONE);
                        errorText.setVisibility(View.VISIBLE);
                        if (!errorText.hasOnClickListeners())
                            errorText.setOnClickListener(v -> {
                                progressBar.setVisibility(View.VISIBLE);
                                errorText.setVisibility(View.GONE);
                                loadImage(link);
                            });
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        progressBar.setVisibility(View.GONE);
                        errorText.setVisibility(View.GONE);
                        return false;
                    }
                }).
                into(imageView);

    }





    private void extractDetail() {
        //USER INFO
        FirebaseDatabase.getInstance().getReference().child("Customers/"+Constants.uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                TextView textView=findViewById(R.id.wallet);
                try {
                    long data= (Long) dataSnapshot.child("WALLET").getValue();
                    if(data<0)
                    {data=data*-1;
                        textView.setTextColor(getResources().getColor(R.color.red));
                        textView.setText(String.format("Pending %s %s", getString(R.string.rupeesSymbol),+ data));
                    }else {
                        textView.setTextColor(getResources().getColor(R.color.green));
                        textView.setText(String.format("Available %s %s", getString(R.string.rupeesSymbol), data));
                    }
                    findViewById(R.id.walletProgress).setVisibility(View.GONE);
                }catch (Exception e){
                    e.printStackTrace();
                    textView.setTextColor(getResources().getColor(R.color.green));
                    textView.setText(String.format("%s %s", getString(R.string.rupeesSymbol), "0"));
                    findViewById(R.id.walletProgress).setVisibility(View.GONE);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //ADMIN INFO
        FirebaseDatabase.getInstance().getReference("Admin/personal_details").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String data;


                TextView textView=findViewById(R.id.mobilenumber);
                String temp=(String)dataSnapshot.child("MOBILE_NUMBER").getValue();
                textView.setText(temp);

                textView=findViewById(R.id.username);
                temp=(String)dataSnapshot.child("NAME").getValue();
                textView.setText(temp);

                textView=findViewById(R.id.shop_address);
                temp=(String)dataSnapshot.child("SHOP_ADDRESS").getValue();
                textView.setText(temp);

                textView=findViewById(R.id.email);
                temp=(String)dataSnapshot.child("PUBLIC_EMAIL").getValue();
                textView.setText(temp);

                }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    public void logoutClicked(View view) {
        Intent intent =new Intent(this,new NotificationAction().getClassName("ACTION_LOGIN",this));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }



    void loadRating(boolean b, double rate){

        if(!b || rate!=0) {
            FirebaseDatabase.getInstance().getReference("Admin/app").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    try {total=0;sum_rate=0;
                        try {if (dataSnapshot.child("TOTAL_RATES").exists())
                                 total = (long) dataSnapshot.child("TOTAL_RATES").getValue();
                            try {
                                sum_rate = (double) dataSnapshot.child("TOTAL_SUM").getValue();
                            }catch (Exception e){sum_rate=(long) dataSnapshot.child("TOTAL_SUM").getValue();}
                        }catch (Exception e){e.printStackTrace();}
                        TextView textView = findViewById(R.id.rating);
                        if (total!=0) {
                            double sum = sum_rate / total;
                            String s = String.format(Locale.UK, "%.1f", sum);
                            textView.setText(String.format(Locale.UK, "%s out of 5 from %d ratings",
                                    s, total));
                        }else textView.setText(R.string.no_ratings);
                        if(b) {
                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Admin/app");
                            databaseReference.child("TOTAL_RATES").setValue(total + 1);
                            databaseReference.child("TOTAL_SUM").setValue(sum_rate + rate);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    public void rateOnClick(View view) {
        from='r';
        isEditVisible=true;
        findViewById(R.id.edit_linear).setVisibility(View.VISIBLE);
        findViewById(R.id.card_rate).setVisibility(View.VISIBLE);
        findViewById(R.id.scroll_view).setVisibility(View.GONE);
        findViewById(R.id.one).setVisibility(View.GONE);
        findViewById(R.id.edit_data).setEnabled(true);
        setTitle(getString(R.string.rate_app));
        bt.setText("Send Rating");
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
    public void chatOnClick(View view) {
        from='c';
        isEditVisible=true;
        findViewById(R.id.edit_linear).setVisibility(View.VISIBLE);
        findViewById(R.id.card_rate).setVisibility(View.GONE);
        findViewById(R.id.scroll_view).setVisibility(View.GONE);
        findViewById(R.id.one).setVisibility(View.VISIBLE);
        findViewById(R.id.edit_data).setEnabled(true);
        bt.setText("Send Feedback");
        setTitle(getString(R.string.feedback));
    }
    void save(){
        if(from=='c'){
            String msg= Objects.requireNonNull(editText.getText()).toString().trim();
            String name=getSharedPreferences("USER_CREDENTIALS",MODE_PRIVATE).
                    getString("NAME","No name");

            if (!msg.isEmpty()) {
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Admin/chats").child(String.valueOf(System.currentTimeMillis()));
                databaseReference.child("NAME").setValue(name);
                databaseReference.child("MESSAGE").setValue(msg);
                editText.setText("");
                showMainScreen();
            }else{
              Snackbar  snackbar=  Snackbar.make(findViewById(R.id.parent),"Please enter your feedback",Snackbar.LENGTH_SHORT);
                snackbar.setBackgroundTint(getResources().getColor(R.color.colorAccent));
                snackbar.show();
            }
        }else{
            AppCompatRatingBar ratingBar=findViewById(R.id.rate);
            double rate=ratingBar.getRating();
            if (rate!=0)
            {
                loadRating(true,rate);
                showMainScreen();
                ratingBar.setRating(0);
            }
            else
            {
                Snackbar  snackbar=  Snackbar.make(findViewById(R.id.parent),"Please enter your rating",Snackbar.LENGTH_SHORT);
                snackbar.setBackgroundTint(getResources().getColor(R.color.colorAccent));
                snackbar.show();
            }
        }
    }
    public void checkUpdateOnClick(View view) {
        findViewById(R.id.progress).setVisibility(View.VISIBLE);
        TextView textView=findViewById(R.id.update);
        textView.setText(R.string.check_update);

        FirebaseDatabase.getInstance()
                .getReference("Admin").child("app").child("Update")

//        .getInstance()
//                .getReference("Admin").child("app")
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    if((long)dataSnapshot.child("APP_VERSION").getValue()>Long.parseLong(getString(R.string.version))){
                        showAlertDialog(true,(String)dataSnapshot.child("APP_NAME").getValue(),
                                (String)dataSnapshot.child("APP_SIZE").getValue(),
                                (boolean)dataSnapshot.child("FORCE_UPDATE").getValue());
                    }else{
                        showAlertDialog(false,"No new update is available","",false);
                    }
                }catch (Exception e){
                    showAlertDialog(false,"No new update is available","",false);
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void showAlertDialog(boolean b, String name,String size,boolean force) {
        findViewById(R.id.progress).setVisibility(View.GONE);
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Update");
        if(b){
            alert.setMessage("A new update is available to download.");
            alert.setPositiveButton("Download", (dialogInterface, i) -> {
                dialogInterface.dismiss();

                Intent intent=new Intent(this,UpdateActivity.class);
                intent.putExtra("APP_NAME",name);
                intent.putExtra("FORCE_UPDATE",force);
                intent.putExtra("APP_SIZE",size);
                startActivity(intent);
            });
            alert.setNegativeButton("Cancel", (dialog, which) -> {
                dialog.dismiss();
            });
        }else{
            alert.setMessage("No new update is available.");
            alert.setPositiveButton("OK", (dialogInterface, i) -> {
                dialogInterface.dismiss();

            });

        }
        alert.setOnDismissListener(dialog -> {
            TextView textView=findViewById(R.id.update);
            textView.setText(R.string.check_whether_a_new_update_is_available_to_download);
        });
        AlertDialog alertDialog = alert.create();
        alertDialog.show();
    }
    public void backOnClicked(View view) {
        if (isEditVisible)
        {   setTitle(R.string.settings);
            isEditVisible=false;
            cancelEditWindowOnClick(new View(this));
        }else{
            if (isTaskRoot())
            {
                startActivity(new Intent(this,HomeUser.class));
                finish();
            }
            else
                super.onBackPressed();
        }

    }
    @Override
    public void onBackPressed() {
        backOnClicked(new View(this));
    }

    public void cancelEditWindowOnClick(View view) {
        findViewById(R.id.scroll_view).setVisibility(View.VISIBLE);
        findViewById(R.id.edit_linear).setVisibility(View.GONE);
        editText.setEnabled(false);
    }

    public void actionButtonOnClick(View view) {
        save();

    }
    void showMainScreen(){
        setTitle(getString(R.string.settings));

        findViewById(R.id.scroll_view).setVisibility(View.VISIBLE);
        findViewById(R.id.edit_linear).setVisibility(View.GONE);
        isEditVisible=false;

    }
}
