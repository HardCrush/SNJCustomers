package com.example.snjcustomers;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.Objects;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

public class InfoActivity extends AppCompatActivity {
    private ProgressBar progressBar;
    private TextView errorText;
    private ImageView imageView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
            toolbar.getNavigationIcon()
                    .setColorFilter(ContextCompat.getColor(this, R.color.black), PorterDuff.Mode.SRC_ATOP);

            receive();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        if (isTaskRoot())
        {
            startActivity(new Intent(this,HomeUser.class));
            finish();
        }
        else
            super.onBackPressed();
    }
    void receive(){
        Intent i=getIntent();
//        TextView textView=findViewById(R.id.title);
//        textView.setText(i.getStringExtra("1"));
        setTitle(i.getStringExtra("1"));
        String qr;
        if(Objects.equals(i.getStringExtra("6"), "null"))
        {    qr = "re";
            setScreenForBottleData();
        } else if(Objects.equals(i.getStringExtra("3"), "0"))
        {
            qr="tr";
            new Recent().addNewRecent(i.getStringExtra("1"),"Transactions",this);
        }
        else {
            qr="or";
            new Recent().addNewRecent(i.getStringExtra("1"),"Orders",this);
        }

        loadImage("https://firebasestorage.googleapis.com/v0/b/billrec.appspot.com/o/files%2F"+
                i.getStringExtra("1")+"_"+qr+"?alt=media&token=76b58f3e-a30f-4a14-b8bf-db97b488d41d");

        setData(i.getStringExtra("1"),i.getStringExtra("2"),i.getStringExtra("3"),
                i.getStringExtra("4"),i.getStringExtra("5"),i.getStringExtra("6")
                ,i.getStringExtra("7"));
    }

 private void setData(String o, String t,
       String q, String w, String a,
        String via,String pamt) {
        TextView textView;
        if(Objects.equals(q, "0")){
            TextView textView1=findViewById(R.id.oneTitle);
            textView1.setText(R.string.trans_no);
        }else if (!Objects.equals(via,"null")){
            textView=findViewById(R.id.quantity);
            findViewById(R.id.qntLinear).setVisibility(View.VISIBLE);
            textView.setText(q);
        }else{textView=findViewById(R.id.quantity); textView.setText(q);}
        textView=findViewById(R.id.orderno);
        textView.setText(o);
        if(!Objects.equals(a, "0") && !Objects.equals(via,"null")) {
            textView = findViewById(R.id.amount);
            findViewById(R.id.amtLinear).setVisibility(View.VISIBLE);
            textView.setText(String.format("₹ %s", a));
        }
        if(w!=null) {
            textView = findViewById(R.id.note);
            findViewById(R.id.noteLinear).setVisibility(View.VISIBLE);
            textView.setText(w);
        }

        textView=findViewById(R.id.date);
        textView.setText(t);
        textView=findViewById(R.id.paidBy);
        textView.setText(via);
        textView=findViewById(R.id.paidAmt);
        textView.setText(String.format("₹ %s", pamt));

    }

    private void setScreenForBottleData() {
        TextView textView1=findViewById(R.id.oneTitle);
        textView1.setText(R.string.bottles_id);
        TextView textView2=findViewById(R.id.quant_text);
        textView2.setText("Total Returned Bottles");
        findViewById(R.id.amtLinear).setVisibility(View.GONE);
        findViewById(R.id.paid_amount_linear).setVisibility(View.GONE);
        findViewById(R.id.paidBy_Linar).setVisibility(View.GONE);
        findViewById(R.id.qntLinear).setVisibility(View.VISIBLE);

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


   

    public void backOnClicked(View view) {
        onBackPressed();
    }


}
