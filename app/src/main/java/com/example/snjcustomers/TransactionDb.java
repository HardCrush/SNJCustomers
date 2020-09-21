package com.example.snjcustomers;

import android.content.Context;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Objects;

public class TransactionDb {
    private FirebaseApp loadFirebase(Context context,String ...storageName) {
        String name=String.valueOf(System.currentTimeMillis());
        FirebaseOptions.Builder options = new FirebaseOptions.Builder()
                .setProjectId("billrec-transactions")
                .setApplicationId("1:495792582566:android:b13403452166ec7f3a6967")
                .setApiKey("AIzaSyBSSWEGRjUmb5_nT3nVUFnB_bIdvW_O0mI")
                .setDatabaseUrl("https://billrec-transactions.firebaseio.com/");
                if (storageName.length==1)
                    options.setStorageBucket("billrec-transactions.appspot.com");

        FirebaseApp.initializeApp(Objects.requireNonNull(context) /* Context */, options.build(),name);
        return  FirebaseApp.getInstance(name);
    }
    public Query getReference(Context context){
        return FirebaseDatabase.
                getInstance(loadFirebase(context))
                .getReference().child("Transactions");
    }
    public StorageReference getStorageReference(Context context){
        return FirebaseStorage.
                getInstance(loadFirebase(context,""))
                .getReference();
    }
    public DatabaseReference getDatabaseReference(Context context){
        return FirebaseDatabase.
                getInstance(loadFirebase(context))
                .getReference();
    }
}
