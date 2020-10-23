package com.bissu.aguabissu;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.HashMap;

public class NotificationBoxBottomSheet extends BottomSheetDialogFragment {
    private final HashMap<String, Object> extraData;
    private final String icon;
    private final String title;
    private final String description;
    private final String action;
    private final String actionText;
    private Intent activityIntent;
    private final String img;

    public NotificationBoxBottomSheet(String iconDrawable, String title,
                                      String description, String action, String actionText, String img, HashMap<String, Object> extraData) {
        this.icon = iconDrawable;
        this.title = title;
        this.description = description;
        this.action = action;
        if (actionText == null || actionText.isEmpty())
            this.actionText = "Open";
        else  this.actionText=actionText;
        this.img=img;
        this.extraData=extraData;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL,R.style.CustomBottomSheetTheme);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.notification_style_bottomsheet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        try {
            TextView titleText = view.findViewById(R.id.title);
            titleText.setText(title);
            if (icon != null)
               {    setIcon(icon);
               }
            if (img!=null)
            {   view.findViewById(R.id.view).setVisibility(View.INVISIBLE);
                setTopImage(img);
                titleText.setGravity(Gravity.CENTER);
            }
            TextView descText = view.findViewById(R.id.desc);
            Spanned spanned;
          try {
              if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.N)
                  spanned= Html.fromHtml(description,Html.FROM_HTML_MODE_LEGACY);
              else    spanned=Html.fromHtml(description);
              descText.setText(spanned);
          }catch (Exception e){e.printStackTrace();descText.setText(description);}
            view.findViewById(R.id.cancel).setOnClickListener(v -> dismiss());
            Button actionBt = view.findViewById(R.id.actionButton);
            if (action!=null)
            {
                actionBt.setText(actionText);
                actionBt.setVisibility(View.VISIBLE);
                setActionButton();
                actionBt.setOnClickListener(v -> startAction());
            }else{
                actionBt.setText(R.string.ok);
                actionBt.setVisibility(View.VISIBLE);
                actionBt.setOnClickListener(v -> dismiss());
                view.findViewById(R.id.cancel).setVisibility(View.GONE);
            }
        }catch (Exception e){e.printStackTrace();
            Toast.makeText(getActivity(),"An error occurred",Toast.LENGTH_SHORT).show();
            dismiss();
        }
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if (getTag()!=null&& getTag().equals("Notification")&&getActivity()!=null)
            new NotificationAction().delFromPreferences(getActivity());
    }

    private void setTopImage(String img) {
        ImageView   imageView= requireView().findViewById(R.id.img);
        Glide.with(requireActivity()).load(img).into(imageView);
        imageView.setVisibility(View.VISIBLE);
    }

    void setIcon(Object icon){
        ImageView  imageView=getView().findViewById(R.id.icon);
        Glide.with(getView()).load(icon).into(imageView);
        imageView.setVisibility(View.VISIBLE);
    }
    void startAction(){
        startActivity(activityIntent);
        try {
            dismiss();
        }catch (Exception   e){e.printStackTrace();}
    }



    private void setActionButton() {
        NotificationAction notificationAction = new NotificationAction();
        activityIntent = new Intent(getActivity(), notificationAction.getClassName(action, getActivity()));
        if (extraData!=null&&!extraData.isEmpty())
            activityIntent=notificationAction.addDataInIntent(activityIntent,extraData);
    }


}
