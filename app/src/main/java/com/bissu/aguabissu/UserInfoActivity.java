package com.bissu.aguabissu;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.InputType;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bissu.aguabissu.qrcode.QRCodeGenerator;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static com.bissu.aguabissu.Constants.uid;
import static com.bissu.aguabissu.Constants.userImgChanged;

public class UserInfoActivity extends AppCompatActivity {
    char btAction;
    RelativeLayout dataLinear;
    ScrollView scrollView;
    Button button;
    HashMap<String,String> orginalData;
    StringBuilder   msg=new StringBuilder("Enter ");
    TextInputEditText editText;
    TextInputLayout editLayout;
    private ProgressBar progress_data;
    private boolean editWindowVisible=false;
    private String buttonText;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);
        if(uid==null)
            Constants.uid=getSharedPreferences("USER_CREDENTIALS",MODE_PRIVATE).getString("UID","not found");
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        toolbar.getNavigationIcon()
                .setColorFilter(ContextCompat.getColor(this,R.color.black), PorterDuff.Mode.SRC_ATOP);

        setTitle("");
        button=findViewById(R.id.action_button);
        scrollView=findViewById(R.id.scroll_view);
        editLayout=findViewById(R.id.one);
        dataLinear=findViewById(R.id.edit_linear);
        editText=findViewById(R.id.edit_data);
        orginalData=new HashMap<>();
        editText.setOnEditorActionListener((v, actionId, event) -> {
            if(actionId== EditorInfo.IME_ACTION_GO)
            {
                actionButtonOnClick(null);
                return true;
            }
            return false;
        });
        progress_data=findViewById(R.id.progress_data);
        ImageView imageView=findViewById(R.id.qrimage);
        String mobile=getSharedPreferences("USER_CREDENTIALS",MODE_PRIVATE).getString("MOBILE_NUMBER","");
        String img=getSharedPreferences("USER_CREDENTIALS",MODE_PRIVATE).getString("PROFILE_IMG","");

        Glide.with(getApplicationContext()).asBitmap().
                load(new QRCodeGenerator().generateQR(
                        uid+","+mobile,uid,UserInfoActivity.this,false
                )).into(imageView);

        loadUserImage(img);

        try {
          extractDetail();
      }catch (Exception e){e.printStackTrace();}

    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void loadUserImage(String img) {
        if (img==null||img.isEmpty()) {
            img = "https://firebasestorage.googleapis.com/v0/b/billrec.appspot.com/o/" +
                    "files%2F" + uid + "_pr?alt=media&token=" + "479a3ebb-6ef0-4ef8-b111-3e30fa2efb";
            Glide.with(this)
                    .load(img)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .apply(RequestOptions.circleCropTransform())
                    .listener(new RequestListener<Drawable>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                    return false;
                }

                @Override
                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                    return false;
                }
            })
                    .into((ImageView) findViewById(R.id.profile_img));
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
                            return false;
                        }
                    })
                    .into((ImageView) findViewById(R.id.profile_img));
        }
    }

    public void changeMobileOnClick(View view) {
        Intent intent= new Intent(this,LoginActivity.class);
        intent.putExtra("EXTRA",true);
        startActivity(intent);
    }

    public void nameOnClick(View view) {
        buttonText=getString(R.string.username_shop_name);
        button.setText(String.format("Change %s",buttonText));
        dataLinear.setVisibility(View.VISIBLE);
        scrollView.setVisibility(View.GONE);
        editWindowVisible=true;editText.setText("");
        setTitle(getString(R.string.username_shop_name));
        editLayout.setHint(getString(R.string.username_shop_name));
        btAction='n';
        editText.setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME|InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        editText.setEnabled(true);
        setMsg(true,"name / shop name");

    }
    void setMsg(boolean usePrefix,String  msg){
        this.msg.delete(0,this.msg.length());
        if (usePrefix)
          this.msg.append("Enter your ");
        this.msg.append(msg);
    }
    public void backOnClicked(View view) {
        if (editWindowVisible)
            cancelEditWindowOnClick(view);
        else
       onBackPressed();
    }

    @Override
    public void onBackPressed() {
        if (editWindowVisible)
            cancelEditWindowOnClick(new View(this));
        else {
            if (isTaskRoot())
            {
                startActivity(new Intent(this,HomeUser.class));
                finish();
            }
            else
                super.onBackPressed();
        }
    }

    public void addressOnClick(View view) {
        editWindowVisible=true;
        buttonText=getString(R.string.address);
        button.setText(String.format("Change %s",buttonText));
        dataLinear.setVisibility(View.VISIBLE);
        scrollView.setVisibility(View.GONE);
        setTitle(getString(R.string.address));
        editLayout.setHint(getString(R.string.address));
        btAction='a';editText.setText("");
        editText.setInputType(InputType.TYPE_CLASS_TEXT|  InputType.TYPE_TEXT_FLAG_MULTI_LINE|InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        editText.setEnabled(true);
        setMsg(true,getString(R.string.address).toLowerCase());
    }

    public void enterEmail(View view) {
        editWindowVisible=true;
        buttonText=getString(R.string.email_address);
        button.setText(String.format("Change %s",buttonText));
        dataLinear.setVisibility(View.VISIBLE);
        scrollView.setVisibility(View.GONE);
        editText.setText("");
        setTitle(getString(R.string.email_address));
        editText.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        editLayout.setHint(getString(R.string.email_address));
        btAction='e';
        editText.setEnabled(true);
        setMsg(true,getString(R.string.email_address).toLowerCase());
    }
    public void actionButtonOnClick(View view) {
        progress_data.setVisibility(View.VISIBLE);
        button.setText("");
       try {
           if (editText!=null) {
               String  data=editText.getText().toString();
               if (!data.isEmpty()){
                   editText.getRootView().clearFocus();
                   switch (btAction) {
                       case 'm':
                           save(view,data, "MOBILE_NUMBER", editText, (TextView) findViewById(R.id.username));
                           break;
                       case 'n':
                           save(view, Constants.capitalize(data), "NAME", editText, (TextView) findViewById(R.id.username));
                           break;
                       case 'a':
                           save(view, data, "USER_ADDRESS", editText, (TextView) findViewById(R.id.shop_address));
                           break;
                      case 'e':
                           if (Patterns.EMAIL_ADDRESS.matcher(data).matches())
                               save(view, data, "USER_EMAIL", editText, (TextView) findViewById(R.id.email));
                           else  {
                               setMsg(false,"Enter a valid email address");
                               showMsg(R.color.red,false);
                               button.setText(String.format("Change %s", buttonText));
                               showMsg(R.color.red, false);
                               progress_data.setVisibility(View.GONE);
                           }
                           break;
                   }
               }else   {
                   button.setText(String.format("Change %s", buttonText));
                   showMsg(R.color.red, false);
                   progress_data.setVisibility(View.GONE);
               }
           }else{
               setMsg(false,"Error occurred");
               showMsg(R.color.red, false);
               progress_data.setVisibility(View.GONE);
               FirebaseCrashlytics.getInstance().log("SNJCustomer  Line 285 :UserInfo   Error  occurred");
           }
       }catch (Exception    e){e.printStackTrace();}
    }
    private void showMsg(int color, boolean isIndefinite){
       Snackbar snackbar=Snackbar.make(findViewById(R.id.parent),msg,Snackbar.LENGTH_LONG);
       if (isIndefinite)
           snackbar.setDuration(Snackbar.LENGTH_INDEFINITE);
       snackbar.setBackgroundTint(ContextCompat.getColor(this,color));
       snackbar.setTextColor(ContextCompat.getColor(this,R.color.white));
       snackbar.show();

    }
    private void save(View view, String data, String type, TextInputEditText editText, TextView textid) {
        //            setMsg(false,"Saving");
        //            showMsg(R.color.colorAccent,true);
        if (type.equals("NAME"))
            userImgChanged=true;
        FirebaseDatabase.getInstance().getReference("Customers/"+uid).child(type).setValue(data).addOnSuccessListener(aVoid -> {
           if(editText==null){
               Toast.makeText(UserInfoActivity.this,"Image changed",Toast.LENGTH_SHORT).show();
            textid.setText(R.string.change_profile_image);
           }else {
               editText.setText(data);
               progress_data.setVisibility(View.GONE);
               button.setText(type + " saved");
               editText.setText("");
               textid.setText(data);
               setMsg(false,buttonText.substring(0,1).toUpperCase()+
                       buttonText.substring(1).toLowerCase()+ " saved");
               showMsg(R.color.colorAccent, false);
               dataLinear.setVisibility(View.GONE);
               scrollView.setVisibility(View.VISIBLE);
               editWindowVisible = false;
               editText.setEnabled(false);
               setTitle("");

           }
        }).addOnFailureListener(e -> {
            editText.setText(orginalData.get(type));
            Toast.makeText(this,"Error: Unable to process request",Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            setTitle("");
            dataLinear.setVisibility(View.GONE);
            scrollView.setVisibility(View.VISIBLE);
            editText.setEnabled(false);
        });


    }
    private void extractDetail() {
        TextView textView=findViewById(R.id.mobilenumber);
        SharedPreferences sh= getSharedPreferences("USER_CREDENTIALS",MODE_PRIVATE);
        String temp= sh.getString("MOBILE_NUMBER","");
        orginalData.put("MOBILE_NUMBER",temp);
        textView.setText(temp);
        textView=findViewById(R.id.username);
        temp=sh.getString("NAME", getString(R.string.enter_name));
        orginalData.put("NAME",temp);
        textView.setText(temp);
        textView=findViewById(R.id.shop_address);
        temp=sh.getString("USER_ADDRESS", getString(R.string.enter_address));
        orginalData.put("USER_ADDRESS",temp);
        textView.setText(temp);

        FirebaseDatabase.getInstance().getReference().child("Customers/"+uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long remainBottle=0;
                try {
                   remainBottle=(long) dataSnapshot.child("WATER_BOTTLE_PENDING").getValue();

                }catch (Exception e){e.printStackTrace();}

                long finalRemainBottle = remainBottle;
                FirebaseDatabase.getInstance().getReference().child("Orders").orderByChild("UID").equalTo(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        long tot=0;
                     try {
                         for(DataSnapshot d:dataSnapshot.getChildren()) {
                             tot += (long) d.child("QUANTITY").getValue();
                         }
                        TextView textView=findViewById(R.id.total_bottle);
                        textView.setText(String.format(Locale.UK,"%d bottles taken ", tot));
                        textView = findViewById(R.id.total_remaining);
                        textView.setText(String.format(Locale.UK, ", remaining %d bottles", finalRemainBottle));
                    }catch (Exception e){e.printStackTrace();}
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                TextView textView1=findViewById(R.id.email);
                String email=(String) dataSnapshot.child("USER_EMAIL").getValue();
                if (email!=null)
                    textView1.setText(email);
                else textView1.setText("Enter email id");
                TextView  textView=findViewById(R.id.wallet);
                try {
                  long data= (Long) dataSnapshot.child("WALLET").getValue();
                   if(data<0)
                   {data=data*-1;
                    textView.setTextColor(getResources().getColor(R.color.red));
                    textView.setText(String.format("Pending %s %s", getString(R.string.rupeesSymbol),+ data));
                   }else if(data==0){
                       textView.setTextColor(getResources().getColor(R.color.green));
                       textView.setText(String.format("%s %s", getString(R.string.rupeesSymbol), data));
                   }
                    else{
                           textView.setTextColor(getResources().getColor(R.color.green));
                           textView.setText(String.format("Available %s %s", getString(R.string.rupeesSymbol), data));
                       }

                 findViewById(R.id.walletProgress).setVisibility(View.GONE);
               }catch (Exception e){
                    textView.setTextColor(getResources().getColor(R.color.green));
                   textView.setText(String.format("%s %s", getString(R.string.rupeesSymbol), "0"));
                   findViewById(R.id.walletProgress).setVisibility(View.GONE);
               }
                String data;
                SharedPreferences.Editor edit=sh.edit();
                  data= (String) dataSnapshot.child("NAME").getValue();
                if(data!=null&& !data.isEmpty() && !Objects.equals(orginalData.get("Name"), data)) {
                    edit.putString("NAME", data);
                    orginalData.put("NAME",data);
                    textView=findViewById(R.id.username);
                    textView.setText(data);
                }
                data= (String) dataSnapshot.child("MOBILE").getValue();
                if(data!=null&& !data.isEmpty() &&!Objects.equals(orginalData.get("MOBILE_NUMBER"), data)) {
                    edit.putString("MOBILE_NUMBER", data);
                    orginalData.put("MOBILE_NUMBER",data);
                    textView=findViewById(R.id.mobilenumber);
                    textView.setText(data);
                }
                data= (String) dataSnapshot.child("USER_ADDRESS").getValue();
                if(data!=null&& !data.isEmpty() &&!Objects.equals(orginalData.get("USER_ADDRESS"), data)) {
                    edit.putString("USER_ADDRESS", data);
                    orginalData.put("USER_ADDRESS",data);
                    textView=findViewById(R.id.shop_address);
                    textView.setText(data);
                }
                edit.apply();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


    public void cancelEditWindowOnClick(View view) {
        editWindowVisible=false;
        dataLinear.setVisibility(View.GONE);
        scrollView.setVisibility(View.VISIBLE);
        setTitle("");
        editText.setEnabled(false);
    }

    public void changeImageClicked(View view) {
        showImageChooser();
    }

    private void showImageChooser() {
        if (ActivityCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
          try {
              Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
              startActivityForResult(intent, 102);
          }catch (ActivityNotFoundException e){
              Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Image"), 102);
          }catch (Exception e){
              setMsg(false,"Error occurred");
              showMsg(R.color.red,false);
          }
        }else{
            ActivityCompat.requestPermissions(this,new String[]{READ_EXTERNAL_STORAGE},102);
        }
    }
    private void uploadImageToFirebaseStorage(Uri link,ImageView    imageView) {
        TextView textView=findViewById(R.id.change_img);
        textView.setText(R.string.changing_img);
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference("files/"+uid+"_pr");
        storageRef.putFile(link).addOnFailureListener(exception -> {
            // Handle unsuccessful uploads
            Toast.makeText(UserInfoActivity.this,"Failed to upload profile image",Toast.LENGTH_SHORT).show();
        }).addOnSuccessListener(taskSnapshot -> {
            userImgChanged=true;
            taskSnapshot.getStorage().getDownloadUrl().addOnCompleteListener(task -> {
                SharedPreferences sharedPreferences = getSharedPreferences("USER_CREDENTIALS", MODE_PRIVATE);
                SharedPreferences.Editor edit = sharedPreferences.edit();
                if (task.isSuccessful())
                {    edit.putString("PROFILE_IMG", task.getResult().toString());
                    imageChangeAfterSteps(task.getResult().toString(),imageView,textView);
                }
                else edit.putString("PROFILE_IMG", "");
                edit.apply();
            });
        });
    }
    void imageChangeAfterSteps(String url, ImageView imageView, TextView textView){
        Glide.with(this)
                .load(url)
                .apply(RequestOptions.circleCropTransform())
                .into(imageView);
        textView.setText("Profile image changed");
        textView.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.success_24),null,null,null);
        new Handler().postDelayed(() -> {
            if (!isFinishing())
                textView.animate().alpha(0).setDuration(500).withEndAction(() -> {
                    textView.setAlpha(1);
                    textView.setCompoundDrawablesWithIntrinsicBounds(null,null,null,null);
                    textView.setText("Select new profile image");
                });
        },3000);
    }
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 102 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uriProfileImage = data.getData();
            ImageView imageView=findViewById(R.id.profile_img);
            Glide.with(this).load(uriProfileImage)
                    .apply(RequestOptions.circleCropTransform()).diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true).into(imageView);
            uploadImageToFirebaseStorage(uriProfileImage,imageView);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0]==PackageManager.PERMISSION_GRANTED){
            showImageChooser();
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
    public void logoutClicked(View view) {
        Intent intent =new Intent(this,new NotificationAction().getClassName("ACTION_LOGIN",this));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }


}
