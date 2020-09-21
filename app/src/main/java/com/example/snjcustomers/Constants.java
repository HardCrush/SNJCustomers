package com.example.snjcustomers;

import android.app.Activity;
import android.content.SharedPreferences;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.NumberFormat;
import java.util.HashSet;
import java.util.Locale;

import androidx.annotation.NonNull;

import static android.content.Context.MODE_PRIVATE;

public class Constants {
    public static String uid;
    public  static boolean userImgChanged =false;
    public  static boolean hideNotificationBar =false;
    public static String capitalize(String searchKey) {
        String[] temp =searchKey.split(" ");
        StringBuilder searchKeyBuilder = new StringBuilder();
        for(String  key:temp)
            searchKeyBuilder.append(key.substring(0, 1).toUpperCase()).append(key.substring(1).toLowerCase()).append(" ");
        searchKey = searchKeyBuilder.toString();
        return searchKey.trim();
    }
    static  public String getFormatedAmount(long amount){
        return "₹ "+NumberFormat.getNumberInstance(Locale.UK).format(amount);
    }
    static public void getCustomViewData(Activity  activity){
        new TransactionDb().getDatabaseReference(activity).child("CustomViews")
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                SharedPreferences   preferences=activity.getSharedPreferences("CUSTOM_VIEW",MODE_PRIVATE);
                SharedPreferences.Editor    editor=preferences.edit();
                editor.clear();
                for (DataSnapshot   d:snapshot.getChildren())
                {
                    String  data;
                    data=d.child("NAME").getValue()+"->"+d.child("ACTION").getValue()+"->"+d.child("DATA").getValue();
                    editor.putString(d.getKey(),data);
                }
                editor.apply();
                FirebaseDatabase.getInstance().getReference("Customers").child(Constants.uid).child("CustomViews")
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    SharedPreferences preferences = activity.getSharedPreferences("CUSTOM_VIEW", MODE_PRIVATE);
                                    SharedPreferences.Editor editor = preferences.edit();
                                    for (DataSnapshot d : snapshot.getChildren()) {
                                        String data;
                                        data = d.child("NAME").getValue() + "->" + d.child("ACTION").getValue() + "->" + d.child("DATA").getValue();
                                        editor.putString(d.getKey(), data);
                                    }
                                    editor.apply();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}
