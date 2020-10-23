package com.bissu.aguabissu;


import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bissu.aguabissu.qrcode.QRCodeGenerator;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseException;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.bissu.aguabissu.Constants.uid;


public class VerifyPhoneActivity extends AppCompatActivity {
    private String verificationId;
    private FirebaseAuth mAuth;
    private ProgressBar progressBar;
    private EditText editText;
    private String phonenumber;
    private MaterialButton resendBt;
    private MaterialButton signinBt;
    private PhoneAuthProvider.ForceResendingToken token;
    private boolean autoLogin = false;
    private String btMsg;
    private final PhoneAuthProvider.OnVerificationStateChangedCallbacks
            mCallBack = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onCodeSent(@NotNull String s, @NotNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            token = forceResendingToken;
            verificationId = s;
            countDown();
            progressBar.setVisibility(View.GONE);
            signinBt.setEnabled(true);
            signinBt.setTextColor(getResources().getColor(R.color.white));
            signinBt.setText(btMsg);
            resendBt.setVisibility(View.VISIBLE);
        }

        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
            String code = phoneAuthCredential.getSmsCode();
            progressBar.setVisibility(View.GONE);
            if (code != null) {
                editText.setText(code);
                verifyCode(code);
            } else {  // TODO: 06/04/2020 Add firebase analytics event here.
                try {
                    FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(VerifyPhoneActivity.this);
                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalytics.Param.ITEM_ID, phonenumber);
                    bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, phonenumber);
                    bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "Mobile");
                    firebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN, bundle);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                automaticLogin();
                //    showAlertDialog("Unable to send the code. Please check your mobile number.\nIf problem persists try interchanging the sim slots of your mobile.", false);

            }
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            progressBar.setVisibility(View.GONE);
            if (!isFinishing() && !signinBt.getText().toString().trim().contains("Signing you")) {
                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    if (e.getMessage() != null && e.getMessage().contains("The format of the phone number provided is incorrect")) {
                        Toast.makeText(VerifyPhoneActivity.this, "Invalid mobile number", Toast.LENGTH_LONG).show();
                        finish();
                    } else
                        Toast.makeText(VerifyPhoneActivity.this, "Invalid code", Toast.LENGTH_LONG).show();
                } else if (e.getMessage() != null && e.getMessage().contains("unusual activity")) {
                    Toast.makeText(VerifyPhoneActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(VerifyPhoneActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }

        }

        @Override
        public void onCodeAutoRetrievalTimeOut(@NonNull String s) {
            super.onCodeAutoRetrievalTimeOut(s);
            progressBar.setVisibility(View.GONE);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_phone);
        resendBt = findViewById(R.id.resendSignIn);
        signinBt = findViewById(R.id.buttonSignIn);
        mAuth = FirebaseAuth.getInstance();
        progressBar = findViewById(R.id.progressbar);
        editText = findViewById(R.id.editTextCode);
        editText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_GO) {
                actionButtonOnClick();
                return true;
            }
            return false;
        });
        phonenumber = getIntent().getStringExtra("phonenumber");
        TextView layoutSubTitle = findViewById(R.id.textView1);
        String subTitle = getString(R.string.verify_code_subtitle) + " "
                + "<font color='#000000'><b>" + getString(R.string.mobile_prefix) + " - " + phonenumber.replace(getString(R.string.mobile_prefix), "") + "</b></font>";
        Spanned spanned;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            spanned = Html.fromHtml(subTitle, Html.FROM_HTML_MODE_LEGACY);
        else spanned = Html.fromHtml(subTitle);
        layoutSubTitle.setText(spanned);
        sendVerificationCode(phonenumber);
        findViewById(R.id.buttonSignIn).setOnClickListener(v -> {
            actionButtonOnClick();
        });
    }

    private void actionButtonOnClick() {
        String code = editText.getText().toString().trim();
        editText.clearFocus();
        if (code.isEmpty()) {
            showSnackbar("Enter the code", R.color.red, false);
            editText.requestFocus();
            return;
        } else if (code.length() < 6) {
            showSnackbar("Code should be 6 digit long", R.color.red, false);
            editText.requestFocus();
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        verifyCode(code);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (getIntent().getBooleanExtra("EXTRA", false)) {
            if (uid == null)
                uid = getSharedPreferences("USER_CREDENTIALS", MODE_PRIVATE).getString("UID", null);
            btMsg = "Change Mobile";
        } else btMsg = "Sign in";
    }

    private void verifyCode(String code) {
        try {
            if (!isFinishing()) {
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
                signInWithCredential(credential);
            }
        } catch (Exception e) {
            progressBar.setVisibility(View.GONE);
            showSnackbar("Invalid code", R.color.red, false);
//           editText.setError("Invalid code...");
        }

    }

    private void sendVerificationCode(String number) {
        progressBar.setVisibility(View.VISIBLE);
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                number,
                60,
                TimeUnit.SECONDS,
                TaskExecutors.MAIN_THREAD,
                mCallBack

        );
    }

    private void signInWithCredential(PhoneAuthCredential credential) {
        signinBt.setEnabled(false);
        if (btMsg.contains("Change Mobile"))
            signinBt.setText("Checking code");
        else
            signinBt.setText("Signing you in");

        progressBar.setVisibility(View.VISIBLE);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
//                        checkUser(); // TODO: Undo my change for testing
                        changeMobile();
                        //Same in both case upto here
                    } else {
                        signinBt.setEnabled(true);
                        signinBt.setText(btMsg);
                        progressBar.setVisibility(View.GONE);
                        if (task.getException() instanceof FirebaseAuthInvalidCredentialsException)
                            Toast.makeText(VerifyPhoneActivity.this, "Invalid code", Toast.LENGTH_LONG).show();
                        else
                            Toast.makeText(VerifyPhoneActivity.this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void automaticLogin() {

        progressBar.setVisibility(View.VISIBLE);
        signinBt.setEnabled(false);
        resendBt.setEnabled(false);
        editText.getLayoutParams().height = dpToPx(35);
        editText.getLayoutParams().width = dpToPx(10);
        editText.requestLayout();
        TextView textView = findViewById(R.id.textView1);
        textView.setText("Mobile Number " + phonenumber + " Detected Automatically");
        if (btMsg.contains("Change Mobile"))
            signinBt.setText("Checking code");
        else
            signinBt.setText("Signing you in");
        editText.setEnabled(false);
        editText.setText(phonenumber);
        FirebaseAuth.getInstance().signInWithEmailAndPassword("lcxzfcko1gdewpbf0nbthpbljag2@snjchilledwater.com"
                , "Nntd7Ha4C4SkY2cEs2fMdJhZVg73").addOnSuccessListener(authResult -> {
            FirebaseDatabase.getInstance().getReference().child("Customers").orderByChild("MOBILE").
                    startAt(phonenumber).endAt(phonenumber + "\uf8ff").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists())
                        loadData(dataSnapshot);
                    else {
                        autoLogin = true;
                        if (getIntent().getBooleanExtra("EXTRA", false)) changeMobile();
                        else enterName();
//                                FirebaseAuth.getInstance().signOut();
//                                showAlertDialog("Mobile number does not exist. Please enter a valid mobile number." +
//                                    "\nIf this message looks like an error then please contact your admin for further assistance for your account retrieval.",false);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }).addOnFailureListener(e ->
                showAlertDialog("An Error occurred while signing you in the app. Please try again after sometime.", false));
    }


    private int dpToPx(int dp) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }

    private void showAlertDialog(String msg, boolean b) {
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setMessage(msg);
        alert.setPositiveButton("OK", (dialogInterface, i) -> {
            dialogInterface.dismiss();
            if (b) {
                Intent intent = new Intent(VerifyPhoneActivity.this, HomeUser.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                storeFCMToken();
                startActivity(intent);
            } else finish();
        });

        AlertDialog alertDialog = alert.create();
        alertDialog.show();
    }

    private void checkUser() {
        FirebaseDatabase.getInstance().getReference().child("Customers").orderByChild("MOBILE").
                startAt(phonenumber).endAt(phonenumber + "\uf8ff"). // FIXME: Que cojones es esto???
                addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//               Log.e(getClass().getCanonicalName(),"user check");

                if (!dataSnapshot.exists()) {    //if new mobile is used
                    if (getIntent().getBooleanExtra("EXTRA", false))
                        changeMobile();
                    else
                        enterName();
                } else {//if mobile number exist and requested to change mobile
                    if (getIntent().getBooleanExtra("EXTRA", false)) {
                        //if changed mobile number already exist
                        showAlertDialog("Mobile number already exist. Please enter a new mobile number.", true);
                        FirebaseAuth.getInstance().signOut();
                        Intent intent = new Intent(VerifyPhoneActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    } else {//if already exist
                        loadData(dataSnapshot);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(VerifyPhoneActivity.this, "An error occured", Toast.LENGTH_SHORT).show();
                FirebaseAuth.getInstance().signOut();
                finish();
            }
        });
    }

    void loadData(DataSnapshot dataSnapshot) {
        for (DataSnapshot d : dataSnapshot.getChildren()) {
            SharedPreferences sharedPreferences = getSharedPreferences("USER_CREDENTIALS", MODE_PRIVATE);
            SharedPreferences.Editor edit = sharedPreferences.edit();
            edit.putString("MOBILE_NUMBER", phonenumber);
            edit.putString("NAME", (String) d.child("NAME").getValue());
            edit.putString("UID", d.getKey());
            edit.putString("IMG_USE", "76b58f3e-a30f-4a14-b8bf-db97b488d41d");
            edit.putString("IMG_RESERVE", "479a3ebb-6ef0-4ef8-b111-3e30fa2efb");
//            Log.e("UIDLOgin",""+d.getKey());
            edit.apply();
        }
        Intent intent = new Intent(VerifyPhoneActivity.this, HomeUser.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        storeFCMToken();
        startActivity(intent);
    }

    void changeMobile() {
        SharedPreferences sharedPreferences = getSharedPreferences("USER_CREDENTIALS", MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putString("MOBILE_NUMBER", phonenumber);
        DatabaseReference databaseReference = FirebaseDatabase.
                getInstance().getReference("Customers/" + uid);
        databaseReference.child("MOBILE").setValue(phonenumber);
        new QRCodeGenerator(uid + "," + phonenumber, uid, VerifyPhoneActivity.this);
        edit.apply();
        Intent intent = new Intent(VerifyPhoneActivity.this, HomeUser.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        storeFCMToken();
        startActivity(intent);
    }

    public void resendOnClick(View view) {
        try {

            progressBar.setVisibility(View.VISIBLE);
            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                    phonenumber,
                    60,
                    TimeUnit.SECONDS,
                    TaskExecutors.MAIN_THREAD,
                    mCallBack,
                    token
            );
            signinBt.setTextColor(getResources().getColor(R.color.disable_color));
            signinBt.setText(R.string.sending_code);
            signinBt.setEnabled(false);
            resendBt.setText("00:60");
            resendBt.setTextColor(getResources().getColor(R.color.disable_color));
            resendBt.setEnabled(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void countDown() {
        new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                resendBt.setText(String.format(Locale.UK, "00:%d", millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                resendBt.setEnabled(true);
                resendBt.setTextColor(getResources().getColor(R.color.white));
                resendBt.setText(R.string.resend_code);
            }
        }.start();
    }

    void enterName() {
        findViewById(R.id.codeLayout).setVisibility(View.GONE);
        findViewById(R.id.nameLayout).setVisibility(View.VISIBLE);
        ((EditText) findViewById(R.id.username))
                .setOnEditorActionListener((v, actionId, event) -> {
                    if (actionId == EditorInfo.IME_ACTION_GO) {
                        signinWithNameClicked(findViewById(R.id.nameAction));
                        return true;
                    }
                    return false;
                });
    }

    public void signinWithNameClicked(View view) {
        EditText editText = findViewById(R.id.username);
        String name = editText.getText().toString().trim();
        try {
            if (!name.isEmpty()) {
                editText.clearFocus();
                ((MaterialButton) view).setText("Signing you in");
                ((MaterialButton) view).setEnabled(false);
                name = Constants.capitalize(name);
                final String[] uid = {!autoLogin ? FirebaseAuth.getInstance().getUid() : String.valueOf(System.currentTimeMillis())};
                HashMap<String, String> userData = new HashMap<>();
                userData.put("MOBILE", phonenumber);
                userData.put("NAME", name);
                String finalName = name;
                FirebaseDatabase.
                        getInstance().getReference("Customers").child(uid[0]).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            uid[0] = String.valueOf(System.currentTimeMillis());
                        }
                        FirebaseDatabase.
                                getInstance().getReference("Customers/" + uid[0]).setValue(userData);
                        storeNewCustomer(uid[0], finalName);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        ((MaterialButton) view).setText("Sign in");
                        ((MaterialButton) view).setEnabled(true);
                    }
                });


            } else {
                showSnackbar("Invalid name", R.color.red, false);
            }
        } catch (Exception e) {
            ((MaterialButton) view).setText("Sign in");
            ((MaterialButton) view).setEnabled(true);
        }
    }

    void storeNewCustomer(String uid, String name) {
        String data = uid + "," + phonenumber;
        new QRCodeGenerator(data, uid, VerifyPhoneActivity.this);
        SharedPreferences sharedPreferences = getSharedPreferences("USER_CREDENTIALS", MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedPreferences.edit();

        edit.putString("MOBILE_NUMBER", phonenumber);
        edit.putString("NAME", name);
        edit.putString("UID", uid);
        edit.apply();
        Intent intent = new Intent(VerifyPhoneActivity.this, HomeUser.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        storeFCMToken();
        startActivity(intent);
    }

    void showSnackbar(String msg, int color, boolean inDefinite) {
        Snackbar snackbar = Snackbar.make(findViewById(R.id.snackbar_view), msg, Snackbar.LENGTH_SHORT);
        snackbar.setBackgroundTint(getResources().getColor(color));
        snackbar.setTextColor(getResources().getColor(R.color.white));
        if (inDefinite)
            snackbar.setDuration(Snackbar.LENGTH_INDEFINITE);
        snackbar.show();
    }

    private String formatName(String name) {
        String[] split = name.split(" ");
        name = name.substring(0, 1).toUpperCase();
        name = name + split[0].substring(1);
        if (split.length > 1) {
            String temp = split[1].substring(0, 1).toUpperCase();
            temp += split[1].substring(1);
            name += " " + temp;
        }

        return name;
    }

    void storeFCMToken() {
        String uid = getSharedPreferences("USER_CREDENTIALS", MODE_PRIVATE).getString("UID", null);
        Constants.uid = uid;
        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(task -> {
            try {
                if (task.isSuccessful()) {
                    String token = task.getResult().getToken();
                    if (uid != null) {
                        FirebaseDatabase.getInstance().getReference("Customers").orderByChild("FB_TOKEN").equalTo(token).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists() && dataSnapshot.hasChildren()) {
                                    for (DataSnapshot d : dataSnapshot.getChildren()) {
                                        if (d.getKey() != null)
                                            FirebaseDatabase.getInstance()
                                                    .getReference("Customers").child(d.getKey()).child("FB_TOKEN").setValue(null);
                                    }
                                }
                                FirebaseDatabase.getInstance().getReference("Customers").child(uid).child("FB_TOKEN").setValue(token);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        FirebaseMessaging.getInstance().subscribeToTopic("customers").addOnSuccessListener(aVoid -> {
        });
        Constants.getCustomViewData(this);
        extractUserImage();
    }

    void extractUserImage() {
        if (uid != null)
            FirebaseStorage.getInstance().getReference("files/" + uid + "_pr").getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {

                    SharedPreferences sharedPreferences = getSharedPreferences("USER_CREDENTIALS", MODE_PRIVATE);
                    SharedPreferences.Editor edit = sharedPreferences.edit();
                    if (task.isSuccessful()) {
                        edit.putString("PROFILE_IMG", task.getResult().toString());
                        Log.e("UserImage", "Found->" + task.getResult().toString());
                    } else {
                        edit.putString("PROFILE_IMG", "");
                        Log.e("UserImage", "Error->" + task.getException().getMessage());
                        task.getException().printStackTrace();
                    }
                    edit.apply();
                }
            });
    }
}
