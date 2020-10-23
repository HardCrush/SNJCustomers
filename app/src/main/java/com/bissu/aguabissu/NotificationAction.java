package com.bissu.aguabissu;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Base64;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static android.content.Context.MODE_PRIVATE;
import static com.bissu.aguabissu.Constants.uid;

public class NotificationAction {
    public static int   INFO_BOX=0;
    public static int   BOTTOM_BAR=1;
    public static int   NOTIFICATION_DIALOG=2;
    public static int   WEB_DIALOG=3;
    public static int   WEB_SCREEN=4;
    public Class<?> getClassName(String name, Context    context) {
        if (name.equalsIgnoreCase("ACTION_OPEN_INFO"))
            return InfoActivity.class;
        else if (name.equalsIgnoreCase("ACTION_OPEN_UPDATE"))
            return UpdateActivity.class;
        else if (name.equalsIgnoreCase("ACTION_OPEN_SETTING"))
            return SettingsActivity.class;
        else if (name.equalsIgnoreCase("ACTION_OPEN_TRANSACTIONS")||
                name.equalsIgnoreCase("ACTION_OPEN_ORDERS"))
            return StatsActivity.class;
        else if (name.equalsIgnoreCase("ACTION_OPEN_BOTTLES"))
            return StatsBottleActivity.class;
        else if (name.equalsIgnoreCase("ACTION_OPEN_USER_INFO"))
            return UserInfoActivity.class;
        else if (name.equalsIgnoreCase("ACTION_OPEN_REPORT"))
            return ReportProblemActivity.class;
        else if (name.equalsIgnoreCase("ACTION_OPEN_WEB"))
            return LoadWebViewActivity.class;
        else if (name.equalsIgnoreCase("ACTION_CHANGE_MOBILE"))
            return LoginActivity.class;
        else if (name.equalsIgnoreCase("ACTION_LOGIN"))
        {
            delFromPreferences(context,"USER_CREDENTIALS");
            delFromPreferences(context,"RECENT");
            delFromPreferences(context,"CUSTOM_VIEW");
            delFromPreferences(context,"INFO_BOX");
            delFromPreferences(context);
            uid="";
            FirebaseMessaging.getInstance().unsubscribeFromTopic("customers");
            FirebaseAuth.getInstance().signOut();
            return LoginActivity.class;
        }else
        return HomeUser.class;
    }
    
    Intent addDataInIntent(Intent intent, Map<String, String> data){
        if (!data.isEmpty()) {
            for (Map.Entry<String, String> entry : data.entrySet()) {
                intent.putExtra(entry.getKey(), entry.getValue());
            }
        }
        return intent;
    }
    void  delNotification(HashMap<String,Object> data){
        String  user=(String)data.get("USERS");
        if (user!=null) {
            if (user.contains("," + uid))
                data.put("USERS", user.replace("," + uid, ""));
            else if (user.contains(", " + uid))
                data.put("USERS", user.replace(", " + uid, ""));
            else if (user.contains(uid + ", "))
                data.put("USERS", user.replace(uid + ", ", ""));
            else if (user.contains(uid + ","))
                data.put("USERS", user.replace(uid + ",", ""));
            else
                data.put("USERS", user.replace(uid, ""));
            String key = (String) data.get("KEY");
            if (((String) data.get("USERS")).isEmpty())
                data = null;
            if (key != null)
                FirebaseDatabase.getInstance().getReference("Notifications")
                        .child(key)
                        .setValue(data);
        }

    }
    Intent addDataInIntent(Intent intent, Bundle bundle){
        if (bundle!=null&&!bundle.isEmpty()) {
            for (Object key : bundle.keySet()) {
               try {
                   intent.putExtra((String) key, bundle.getString((String) key));
               }catch (Exception    e){e.printStackTrace();}
            }
        }
        return intent;
    }
    HashMap<String, Object> addDataInMap( Bundle bundle){
        HashMap<String,Object> map=new HashMap<>();
        if (bundle!=null&&!bundle.isEmpty()) {
            for (Object key : bundle.keySet()) {
                try {
                    map.put((String) key, bundle.getString((String) key));
                }catch (Exception    e){e.printStackTrace();}
            }
        }
        return map;
    }

    public Intent addDataInIntent(Intent intent, HashMap<String, Object> data) {
        if (!data.isEmpty()) {
            for (Map.Entry<String, Object> entry : data.entrySet()) {
              try {
                  intent.putExtra(entry.getKey(),(String) entry.getValue());
              }catch (Exception ignored){}
            }
        }
        return intent;
    }

    public HashMap<String, Object> addDataInMap(HashMap<String,Object> data) {
        HashMap<String,Object> map=new HashMap<>();
        if (data!=null&&!data.isEmpty()) {
            for (Object key : data.keySet()) {
                try {
                    map.put((String) key, data.get(key));
                }catch (Exception e){e.printStackTrace();}
            }
        }
        return map;
    }

    public void addHashMapToPref(Activity context,String name, HashMap<String, Object> data) {
        SharedPreferences  sharedPreferences=context.getSharedPreferences(name,MODE_PRIVATE);
        SharedPreferences.Editor   editor=sharedPreferences.edit();
        for (Map.Entry<String,Object>entry:data.entrySet()){
           try {Object value=entry.getValue();
               if (value instanceof Long)
                   editor.putLong(entry.getKey(), (Long) value);
               else if (value instanceof Boolean)
                   editor.putBoolean(entry.getKey(), (Boolean) value);
               else
                   editor.putString(entry.getKey(), (String) value);
           }catch (Exception e){e.printStackTrace();}
        }
        editor.apply();

    }
    void delFromPreferences(Context context,String ...name){
        try {
        SharedPreferences  sharedPreferences=context.getSharedPreferences(name.length>0?name[0]:
                "NOTIFICATIONS",MODE_PRIVATE);
        if (sharedPreferences!=null) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();
        }
      }catch (Exception ignored){}
    }
    public void openBottomBar(HashMap<String, Object> data, boolean delAction, FragmentManager fragmentManager) {
        String icon= (String) data.get("ICON");
        String action=(String) data.get("ACTION");
        String  title=(String) data.get("TITLE");
        String  desc=(String) data.get("DESC");
        String  img=(String) data.get("IMG");
        String  actionText= (String) data.get("ACTION_TEXT");
        NotificationBoxBottomSheet  sheet=new NotificationBoxBottomSheet(
                icon,title,desc,action,actionText,img,data);
        if (delAction)
            sheet.showNow(fragmentManager,"Notification");
        else  sheet.showNow(fragmentManager,"Home");
    }
    public void openNotificationDialog(HashMap<String,Object>data,Activity  context){
        try {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
            View customLayout =context. getLayoutInflater().inflate(R.layout.notification_dialog, null);
            alertDialog.setView(customLayout);
            AlertDialog alert = alertDialog.create();
            Objects.requireNonNull(alert.getWindow())
                    .setBackgroundDrawableResource(android.R.color.transparent);
            alert.setCanceledOnTouchOutside(true);
            TextView actionText = customLayout.findViewById(R.id.actionButton);
            TextView titleText = customLayout.findViewById(R.id.title);
            TextView descText = customLayout.findViewById(R.id.desc);
            String title = (String) data.get("TITLE");
            if (title != null)
                titleText.setText(title);
            String desc = (String) data.get("DESC");
            if (desc != null)
                descText.setText(desc);
            String actionTitle = (String) data.get("ACTION_TEXT");
            if (actionTitle != null)
                actionText.setText(actionTitle);
            else actionText.setText("OK");

            actionText.setOnClickListener(v -> {
                String action = (String) data.get("ACTION");
                if (action != null) {
                    Intent intent = new Intent(context,
                           getClassName(action, context));
                    intent = addDataInIntent(intent, data);
                   context. startActivity(intent);
                }
                alert.dismiss();
            });
            alert.show();
            alert.setOnDismissListener(dialog -> {
                delFromPreferences(context);
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public  void showWebNotificationDialog(HashMap<String,Object> data,String html, int width, int height,int h_w,
                                            int h_h,String action,Activity context){

        View dialogView = View.inflate(context, R.layout.dialog_notification, null);
        final Dialog dialog = new Dialog(context, R.style.Dialog1);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.width = dpToPx(width,context);//WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = dpToPx(height,context);// WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.CENTER;
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
        Objects.requireNonNull(dialog.getWindow()).setAttributes(lp);
        dialog.setCanceledOnTouchOutside(true);
        WebView webView = dialogView.findViewById(R.id.webview);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getLayoutParams().height = dpToPx(h_h,context);
        webView.getLayoutParams().width = dpToPx(h_w,context);
        String encode = Base64.encodeToString(html.getBytes(), Base64.NO_PADDING);
        webView.loadData(encode, "text/html", "base64");
        dialog.setContentView(dialogView);
        dialog.setOnDismissListener(dialogInterface -> {
            if (action != null) {
                String[] allActions = action.split(",");
                for (String act :
                        allActions)
                    advanceActions(dialog, act, data,context);

            } else advanceActions(dialog, "", data,context);
        });
        dialog.setOnDismissListener(dialog1 -> delFromPreferences(context));
        dialog.findViewById(R.id.cancel).setOnClickListener(view -> dialog.dismiss());
        dialog.setOnKeyListener((dialogInterface, i, keyEvent) -> {
            if (i == KeyEvent.KEYCODE_BACK) {
                dialog.dismiss();
                return true;
            }
            return false;
        });
        dialog.show();

    }
    private int dpToPx(int dp,Context context) {
        Resources r =context.getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }
    public void  advanceActions(Dialog dialog, String action, HashMap<String, Object> data,Activity context){
        try {
            switch (action){
                case "ACTION_EXIT":
                    context. finishAffinity();
                    break;
                case "ACTION_OPEN_ORDERS":
                case "ACTION_OPEN_TRANSACTIONS":
                case "ACTION_OPEN_BOTTLES":
                case "ACTION_OPEN_USER_INFO":
                case "ACTION_OPEN_SETTING":
                case "ACTION_LOGIN":
                    startNewActivity(context,action,null); break;
                case "ACTION_DELETE_NOTIFICATION":
                    delFromPreferences(context);
                    break;
                case "ACTION_CLOSE":
                default:context.finish();
            }
        }catch (Exception e){e.printStackTrace();}
    }
    void startNewActivity(Context context, String action, HashMap<String, Object> data ){
        Intent intent=new Intent(context,getClassName(action,context));
        if (data!=null)
            intent = addDataInIntent(intent, data);
        context.startActivity(intent);
    }
    void checkNewNotification(Activity  activity,FragmentManager  manager){
        checkInfoBoxNotification(activity,manager);
        SharedPreferences   preferences=activity.getSharedPreferences("NOTIFICATIONS",MODE_PRIVATE);
        if (preferences==null|| preferences.getAll().isEmpty()) {
            FirebaseDatabase.getInstance().getReference("Notifications").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    try {
                        HashMap<String, HashMap<String, Object>> map = (HashMap<String, HashMap<String, Object>>)
                                dataSnapshot.getValue();
                        if (map != null) {
                            HashMap<String, Object> data = searchInValues(map);
                            if (data != null) {
                                addHashMapToPref(activity,"NOTIFICATIONS",data);
                                delNotification(data);
                                configureNotification(data,activity,manager);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
        }else{
            configureNotification((HashMap<String, Object>) preferences.getAll(),activity,manager);
        }
    }

    private void checkInfoBoxNotification(Activity activity, FragmentManager manager) {
       try {
           SharedPreferences preferences = activity.getSharedPreferences("INFO_BOX", MODE_PRIVATE);
           HashMap<String, Object> prefData = (HashMap<String, Object>) preferences.getAll();
           if (!prefData.isEmpty()){
               //setBottomInfoBox(activity, prefData);
               setBottomInfoBoxFragment(activity, prefData,manager);
           }
           new TransactionDb().getDatabaseReference(activity).child("Notifications").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
               @Override
               public void onDataChange(@NonNull DataSnapshot snapshot) {
                  try {
                      HashMap<String, Object> dbData = (HashMap<String, Object>) snapshot.getValue();
                      if (!snapshot.exists() || dbData == null) {
                          if (!prefData.isEmpty())
                          {
                              delFromPreferences(activity, "INFO_BOX");
                              //setBottomInfoBox(activity, null);
                              setBottomInfoBoxFragment(activity, null,null);
                          }
                      } else {
                          if (!dbData.equals(prefData)) {
                              addHashMapToPref(activity, "INFO_BOX", dbData);
                              //setBottomInfoBox(activity, dbData);
                              setBottomInfoBoxFragment(activity, dbData,manager);
                          }
                      }
                  }catch (Exception e){e.printStackTrace();}
               }

               @Override
               public void onCancelled(@NonNull DatabaseError error) {

               }
           });
       }catch (Exception e){e.printStackTrace();}
    }
    public void openBottomInfoFragment(Activity activity, Map<String, Object> all, FragmentManager manager){
        try{
        InfoBoxSetterFragment fragment;
        int id=R.id.frame_bottom;
        activity.findViewById(R.id.frame_bottom).setVisibility(View.GONE);
            activity.findViewById(R.id.frame_fixed).setVisibility(View.GONE);
        if (all.get("STYLE2")!=null){
            if ((long)all.get("STYLE2")==BOTTOM_BAR)
                fragment = new InfoBoxSetterFragment(R.layout.bottom_notification_card, all);
            else
            {
                fragment = new InfoBoxSetterFragment(R.layout.info_card_small, all);
                id=R.id.frame_fixed;
            }
        }else fragment = new InfoBoxSetterFragment(R.layout.bottom_notification_card, all);
        activity.findViewById(id).setVisibility(View.VISIBLE);
        FragmentTransaction fragmentTransaction2;
        fragmentTransaction2= manager.beginTransaction();

            fragmentTransaction2.replace(id, Objects.requireNonNull(fragment));
            fragmentTransaction2.commit();
        }catch (Exception e){FirebaseCrashlytics.getInstance().recordException(e);e.printStackTrace();}
    }
//    public void openBottomInfo(Activity activity, Map<String, Object> all,View infoBox,View tempView){
//      try {
//          if (infoBox == null)
//              infoBox = activity.findViewById(R.id.info_box);
//          if (tempView == null)
//              tempView = activity.findViewById(R.id.temp_info_card);
//          String title = (String) all.get("TITLE");
//          String desc = (String) all.get("DESC");
//          if (title == null || desc == null)
//              return;
//          TextView titleText = activity.findViewById(R.id.title);
//          TextView descText = activity.findViewById(R.id.desc);
//          TextView tempTitleText = activity.findViewById(R.id.title_temp);
//          TextView tempDescText = activity.findViewById(R.id.desc_temp);
//          if (titleText.getText().toString().isEmpty())
//              setText(titleText, tempTitleText, title, false);
//          else if (!titleText.getText().toString().equals(title))
//              setText(titleText, tempTitleText, title, true);
//          if (descText.getText().toString().isEmpty())
//              setText(descText, tempDescText, desc, false);
//          else if (!descText.getText().toString().equals(desc))
//              setText(descText, tempDescText, desc, true);
//          try {
//              String textColorTitle = (String) all.get("TEXT_COLOR_TITLE");
//              if (textColorTitle != null)
//                  titleText.setTextColor(Color.parseColor(textColorTitle));
//              else titleText.setTextColor(activity.getResources().getColor(R.color.black));
//              String textColorDesc = (String) all.get("TEXT_COLOR_DESC");
//              if (textColorDesc != null)
//                  descText.setTextColor(Color.parseColor(textColorDesc));
//              else
//                  descText.setTextColor(activity.getResources().getColor(R.color.info_box_desc));
//          } catch (Exception e) {
//              e.printStackTrace();
//              titleText.setTextColor(activity.getResources().getColor(R.color.black));
//              descText.setTextColor(activity.getResources().getColor(R.color.info_box_desc));
//          }
//          String img = (String) all.get("IMG");
//          ImageView imageView = activity.findViewById(R.id.icon);
//          if (img != null) {
//              Glide.with(activity).load(img).into(imageView);
//              imageView.setVisibility(View.VISIBLE);
//              activity.findViewById(R.id.icon_temp).setVisibility(View.INVISIBLE);
//          } else {
//              imageView.setVisibility(View.GONE);
//              activity.findViewById(R.id.icon_temp).setVisibility(View.GONE);
//          }
//          infoBox.setVisibility(View.VISIBLE);
//          tempView.setVisibility(View.INVISIBLE);
//
//          String color = (String) all.get("COLOR");
//          try {
//              if (color != null)
//                  ((MaterialCardView) infoBox).setCardBackgroundColor(Color.parseColor(color));
//          } catch (Exception e) {
//              e.printStackTrace();
//              ((MaterialCardView) infoBox).setCardBackgroundColor(activity.getResources().getColor(R.color.colorAccent));
//          }
//          String action = (String) all.get("ACTION");
//          if (action != null)
//              infoBox.setOnClickListener(v -> startNewActivity(activity, action, (HashMap<String, Object>) all));
//          View finalInfoBox = infoBox;
//          activity.findViewById(R.id.cancel).setOnClickListener(v -> finalInfoBox.setVisibility(View.GONE));
//      }catch (Exception e){
//          e.printStackTrace();
//          FirebaseCrashlytics.getInstance().recordException(e);
//      }
//      }
    private void setBottomInfoBoxFragment(Activity activity, Map<String, Object> all,FragmentManager manager) {
        try {
            View infoBox;
            if (all != null) {
                if (all.get("STYLE2")==null||(long)all.get("STYLE2")==BOTTOM_BAR)
                    infoBox = activity.findViewById(R.id.frame_bottom);
                else infoBox = activity.findViewById(R.id.frame_fixed);
                long from = (long) all.get("FROM");
                long to = (long) all.get("TO");
                long currentTime = System.currentTimeMillis();
                if (currentTime >= from && currentTime <= to) {
                    openBottomInfoFragment(activity,all,manager);
                }
                else {

                    if (currentTime > to)
                    {   delFromPreferences(activity,"INFO_BOX");
                        new TransactionDb().getDatabaseReference(activity).child("Notifications").child(uid).setValue(null);
                    }
                    infoBox.setVisibility(View.GONE);
                }
            } else {
                //infoBox.setVisibility(View.GONE);
                activity.findViewById(R.id.frame_bottom).setVisibility(View.GONE);
                activity.findViewById(R.id.frame_fixed).setVisibility(View.GONE);
            }
        }catch (Exception e){e.printStackTrace();}
    }

//    private void setBottomInfoBox(Activity activity, Map<String, Object> all) {
//       try {
//           View infoBox = activity.findViewById(R.id.info_box);
//           View tempView = activity.findViewById(R.id.temp_info_card);
//           if (all != null) {
//               long from = (long) all.get("FROM");
//               long to = (long) all.get("TO");
//               long currentTime = System.currentTimeMillis();
//               if (currentTime >= from && currentTime <= to) {
//                   openBottomInfo(activity,all,infoBox,tempView);
//               }
//                else {
//                   delFromPreferences(activity,"INFO_BOX");
//                   new TransactionDb().getDatabaseReference(activity).child("Notifications").child(uid).setValue(null);
//                   infoBox.setVisibility(View.GONE);
//               }
//           } else {
//               infoBox.setVisibility(View.GONE);
//           }
//       }catch (Exception e){e.printStackTrace();}
//    }
    void setText(TextView textView,TextView tempTextView, String  text,boolean isAnimate){
        if (tempTextView!=null)
            tempTextView.setText(text);
        if (isAnimate)
        textView.animate().alpha(0).setDuration(500).withEndAction(() -> {
            textView.setAlpha(1);
            textView.setText(text);
        });
        else  textView.setText(text);
    }
    void configureNotification(HashMap<String, Object> data,Activity activity,FragmentManager  manager){
        long type = (long) data.get("STYLE");
        try {
            if (type == NotificationAction.BOTTOM_BAR)
                openBottomBar(data, true, manager);
            else if (type == NotificationAction.INFO_BOX) {
                //openBottomInfo(activity, data, null, null);
                openBottomInfoFragment(activity, data, manager);
                delFromPreferences(activity);
            }
            else if (type == NotificationAction.NOTIFICATION_DIALOG)
                openNotificationDialog(data,activity);
            else if (type == NotificationAction.WEB_DIALOG)
                showWebNotificationDialog(data,
                        Objects.requireNonNull((String) data.get("HTML")),
                        (int) ((long) data.get("WIDTH")),
                        (int) ((long) data.get("HEIGHT")),
                        (int) ((long) data.get("HTML_WIDTH")),
                        (int) ((long) data.get("HTML_HEIGHT")),
                        (String) data.get("ACTION"),activity);
            else if (type == NotificationAction.WEB_SCREEN) {
                openInWebview((String) data.get("HTML"), (String) data.get("ACTION"),
                        (String) data.get("TITLE"),activity);
            }
        }catch (Exception  e){e.printStackTrace();
            FirebaseCrashlytics.getInstance().recordException(e);
        }
    }


    private HashMap<String, Object> searchInValues(HashMap<String, HashMap<String, Object>> map) {
        for (String key:map.keySet()) {
            HashMap<String,Object> tempMap=map.get(key);
            if(((String)tempMap.get("USERS")).contains(uid)){
                tempMap.put("KEY",key);
                return tempMap;
            }

        }
        return null;
    }

    private void openInWebview(String html, String action, String title,Activity activity) {
        delFromPreferences(activity);
        Intent intent=new Intent(activity,LoadWebViewActivity.class);
        intent.putExtra("HTML",html);
        intent.putExtra("ACTION",action);
        intent.putExtra("TITLE",title);
       activity. startActivity(intent);
    }

}
