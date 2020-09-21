package com.example.snjcustomers;


import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.ImageViewCompat;
import androidx.fragment.app.Fragment;

public class InfoBoxSetterFragment extends Fragment {
    private final Map<String, Object> data;
    private  int layout;

    InfoBoxSetterFragment(int layout, Map<String, Object> all){
        this.layout=layout;
        this.data=all;
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(layout, container, false);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        openBottomInfo(view);
    }
    public void openBottomInfo(View view){
        try {
            Log.e("InfoBoxSetter","Setting creds\n"+data);
            View  infoBox = getView().findViewById(R.id.info_cardView);
            String title = (String) data.get("TITLE");
            String desc = (String) data.get("DESC");
            if (title == null || desc == null)
                return;
            TextView titleText = view.findViewById(R.id.title);
            TextView descText = view.findViewById(R.id.desc);
            if (titleText.getText().toString().isEmpty())
                setText(titleText, title, false);
            else if (!titleText.getText().toString().equals(title))
                setText(titleText, title, true);
            if (descText.getText().toString().isEmpty())
                setText(descText, desc, false);
            else if (!descText.getText().toString().equals(desc))
                setText(descText, desc, true);
            try {
                String textColorTitle = (String) data.get("TEXT_COLOR_TITLE");
                if (textColorTitle != null)
                    titleText.setTextColor(Color.parseColor(textColorTitle));
                else titleText.setTextColor(view.getResources().getColor(R.color.black));
                String textColorDesc = (String) data.get("TEXT_COLOR_DESC");
                if (textColorDesc != null)
                    descText.setTextColor(Color.parseColor(textColorDesc));
                else
                    descText.setTextColor(view.getResources().getColor(R.color.info_box_desc));
            } catch (Exception e) {
                e.printStackTrace();
                titleText.setTextColor(view.getResources().getColor(R.color.black));
                descText.setTextColor(view.getResources().getColor(R.color.info_box_desc));
            }
            String img = (String) data.get("IMG");
            ImageView imageView = view.findViewById(R.id.icon);
            if (img != null) {
                Glide.with(view).load(img).into(imageView);
                imageView.setVisibility(View.VISIBLE);
            } else {
                imageView.setVisibility(View.GONE);

            }
            infoBox.setVisibility(View.VISIBLE);

            String color = (String) data.get("COLOR");
            try {
                if (color != null)
                    ((MaterialCardView) infoBox).setCardBackgroundColor(Color.parseColor(color));
            } catch (Exception e) {
                e.printStackTrace();
                ((MaterialCardView) infoBox).setCardBackgroundColor(view.getResources().getColor(R.color.colorAccent));
            }
            String action = (String) data.get("ACTION");

            if (action != null)
                infoBox.setOnClickListener(v ->  new NotificationAction()
                        .startNewActivity(getActivity(), action, (HashMap<String, Object>) data));
            if (layout==R.layout.bottom_notification_card)
//
              view.findViewById(R.id.cancel).setOnClickListener(v -> infoBox.setVisibility(View.GONE));
            else{
                ImageView imageView1=view.findViewById(R.id.cancel);
                String textColorTitle = (String) data.get("TEXT_COLOR_TITLE");
                if (textColorTitle != null)
                    imageView1.setColorFilter(Color.parseColor(textColorTitle));
                else imageView1.setColorFilter(view.getResources().getColor(R.color.black));
            }
        }catch (Exception e){
            e.printStackTrace();
            FirebaseCrashlytics.getInstance().recordException(e);
        }
    }
    void setText(TextView textView, String  text,boolean isAnimate){
        if (isAnimate)
            textView.animate().alpha(0).setDuration(500).withEndAction(() -> {
                textView.setAlpha(1);
                textView.setText(text);
            });
        else  textView.setText(text);
    }

}
