package com.example.snjcustomers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

public class LoginActivity extends Activity {
    private EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        editText = findViewById(R.id.editTextPhone);
        findViewById(R.id.buttonContinue).setOnClickListener(v -> {
            actionButtonOnClick();
        });
        editText.setOnEditorActionListener((v, actionId, event) -> {
            if(actionId== EditorInfo.IME_ACTION_NEXT)
            {
                actionButtonOnClick();
                return true;
            }
            return false;
        });
    }
    void actionButtonOnClick(){

        if (isNetworkAvailable(LoginActivity.this)) {
            String number = editText.getText().toString().trim();
            if (number.isEmpty()) {
                //editText.setError("Invalid mobile number");
                showSnackbar("Enter mobile number",R.color.red);
                editText.requestFocus();
                return;
            }
            if (number.length() < 10){
                //editText.setError("Invalid mobile number");
                showSnackbar("Invalid mobile number",R.color.red);
                editText.requestFocus();
                return;
            }
            editText.clearFocus();
            Intent i = getIntent();
            if (getIntent().getBooleanExtra("EXTRA", false)) {
                chekUser(number);
            } else {
                Intent intent = new Intent(LoginActivity.this, VerifyPhoneActivity.class);
                intent.putExtra("phonenumber", "+91" + number);
                intent.putExtra("EXTRA", i.getBooleanExtra("EXTRA", false));
                startActivity(intent);
            }
        }else{
//                Snackbar.make(findViewById(R.id.parent),,Snackbar.LENGTH_LONG).show();
            showSnackbar("Cannot connect to server. ",R.color.colorAccent);
        }
    }
    public static boolean isNetworkAvailable(Context con){
        try{
            ConnectivityManager cm=(ConnectivityManager) con.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo=cm.getActiveNetworkInfo();
            if (networkInfo!=null&& networkInfo.isConnected()){
                return true;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }
    private void chekUser(String phonenumber) {
        if (("+91"+phonenumber).equals(getSharedPreferences("USER_CREDENTIALS",MODE_PRIVATE)
                        .getString("MOBILE_NUMBER","")))
        {
            showAlertDialog("Please enter new mobile number");
            return;
        }
        findViewById(R.id.progressbar).setVisibility(View.VISIBLE);
        FirebaseDatabase.getInstance().getReference("Customers").
                orderByChild("MOBILE").startAt("+91"+phonenumber).endAt("+91"+phonenumber+"\uf8ff").
                addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                findViewById(R.id.progressbar).setVisibility(View.GONE);
                if(dataSnapshot.getChildrenCount()>=1){
                    showAlertDialog("Mobile number already exist. Please enter a new mobile number.");
                }else {
                    Intent intent = new Intent(LoginActivity.this, VerifyPhoneActivity.class);
                    intent.putExtra("phonenumber", "+91" + phonenumber);
                    intent.putExtra("EXTRA", true);
                    startActivity(intent);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    void showSnackbar(String msg, int color){
        Snackbar snackbar= Snackbar.make(findViewById(R.id.snackbar_view),msg,Snackbar.LENGTH_SHORT);
        snackbar.setBackgroundTint(getResources().getColor(color));
        snackbar.setTextColor(getResources().getColor(R.color.white));
        snackbar.show();
    }
    private void showAlertDialog(String msg) {
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setMessage(msg);
        alert.setPositiveButton("OK", (dialogInterface, i) -> {
            dialogInterface.dismiss();
        });

        AlertDialog alertDialog = alert.create();
        alertDialog.show();
    }



}
