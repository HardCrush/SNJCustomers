package com.bissu.aguabissu;


import android.os.Build;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;


public class ViewHolder {







    public static class FoodViewHolder extends RecyclerView.ViewHolder{
        public View mView;

        public FoodViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }





        void setUserName(String Name){
            try {
                TextView textView=mView.findViewById(R.id.username);
                textView.setText(Name);
            }catch (Exception ignored){}

        }




        public void setMobileNumber(String MobileNumber){
            try {
                TextView textView=mView.findViewById(R.id.mobilenumber);
                textView.setText(MobileNumber);
            }catch (Exception ignored){}

        }



        public void setOrderNo(String order_no) {
            try {
                TextView textView=mView.findViewById(R.id.orderno);
                textView.setText(order_no);
            }catch (Exception e){e.printStackTrace();}
        }

        public void setAmount(String amount) {
            try {
                TextView textView=mView.findViewById(R.id.amount);
                textView.setText(amount);
            }catch (Exception e){e.printStackTrace();}
        }

      public   void setTime(String time) {
            try {
                TextView textView=mView.findViewById(R.id.time);
                textView.setText(time);
            }catch (Exception e){e.printStackTrace();}
        }

        public    void setWalletStatus(String wallet_status) {
            try {
                TextView textView=mView.findViewById(R.id.wallet);
                textView.setText(wallet_status);
            }catch (Exception e){e.printStackTrace();}
        }
        public void setBottleImage() {
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M) {
                Glide.with(itemView).load(R.drawable.bottle_drawable)
                        .apply(RequestOptions.circleCropTransform()).
                        into((ImageView) mView.findViewById(R.id.img1));
            }else{
                Glide.with(itemView).load(R.drawable.bottlefinal)
                        .apply(RequestOptions.circleCropTransform()).
                        into((ImageView) mView.findViewById(R.id.img1));
            }
        }
    }



    }




