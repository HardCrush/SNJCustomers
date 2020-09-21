package com.example.snjcustomers.RecyclerUI;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.snjcustomers.InfoActivity;
import com.example.snjcustomers.Model;
import com.example.snjcustomers.R;
import com.example.snjcustomers.ViewHolder;
import com.google.firebase.database.DataSnapshot;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class RecyclerUI {

    private final SimpleDateFormat dateFormat;
    private   FilterAdapter adapter;
    private Context context;
     private List<Model> list;
     private String key;
    private String type;
    private Calendar calendar;

    public RecyclerUI(DataSnapshot dataSnapshot, Context context, RecyclerView recyclerView
            , TextView costText, TextView totalTripText, String key, String type){
         this.context=context;
         list=new ArrayList<>();
        this.type=type;
        adapter= new FilterAdapter(list);
        this.key=key;
        dateFormat=new SimpleDateFormat("dd MMM yy hh:mm aa", Locale.UK);
        calendar=Calendar.getInstance();
        LinearLayoutManager mLayoutManager =
                new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, true);

        recyclerView.setNestedScrollingEnabled(true);
        recyclerView.setLayoutManager(mLayoutManager);
        try {
            recyclerView.scrollToPosition((int) (dataSnapshot.getChildrenCount()-1));

        }catch (Exception e){e.printStackTrace();}
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        recyclerView.setAdapter(adapter);
        totalTripText.setText(String.format(Locale.UK,"From %d %s", dataSnapshot.getChildrenCount(), type));
        setRecyclerUI(dataSnapshot,costText,totalTripText);

      }
        private void setRecyclerUI(DataSnapshot dataSnapshot, TextView costText, TextView totalTripText){
        long cost=0;
           try {
               for (DataSnapshot dataSnapshot2:dataSnapshot.getChildren())
               {
                   Model  model=dataSnapshot2.getValue(Model.class);
                   if (model != null) {
                       cost+=model.getPAID_AMOUNT();
                   }
                   list.add(model);
                   adapter.notifyDataSetChanged();
               }
           }catch (Exception e){e.printStackTrace();totalTripText.setText("Error occurred");}
            costText.setText(getFormatedAmount(cost));
        }
    private String getFormatedAmount(long amount){
        return "₹ "+NumberFormat.getNumberInstance(Locale.UK).format(amount);
    }
      class FilterAdapter extends RecyclerView.Adapter<ViewHolder.FoodViewHolder> {
          List<Model> dataList;
          FilterAdapter(List<Model> data){
              this.dataList=data;
          }
          @NonNull
          @Override
          public ViewHolder.FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
              //return new ViewHolder.FoodViewHolder( LayoutInflater.from(context).inflate(R.layout.adapter_layout,viewGroup,false));
             View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_layout, parent, false);
              return new ViewHolder.FoodViewHolder(view);
          }
          String getFormatedTime(long time){
              calendar.setTimeInMillis(time);
              return dateFormat.format(calendar.getTime());
          }
          @Override
          public void onBindViewHolder(@NonNull  ViewHolder.FoodViewHolder viewHolder, int position) {
              //set all data
              Model model=list.get(position);
                  try {
//                      viewHolder.setTime(getFormatedTime(model.getTIME()));
                      viewHolder.setTime(getFormatedTime(Long.parseLong(model.getDATE())));
                      viewHolder.setWalletStatus("Paid "+getFormatedAmount(model.getPAID_AMOUNT()));
//                      viewHolder.setOrderNo(String.valueOf(model.getTIME()));
                      viewHolder.setOrderNo(model.getDATE());

                      if(type.equals("Orders")){
                          if(model.getPAID_AMOUNT()<model.getAMOUNT()) {
                              TextView textView= viewHolder.mView.findViewById(R.id.wallet);
                              textView.setTextColor(context.getResources().getColor(R.color.red));
                          }
                          viewHolder.setAmount(getFormatedAmount(model.getAMOUNT()));

                      }else viewHolder.setAmount("Paid Via "+model.getPAID_VIA());
                      loadListener(model,viewHolder,position);
                  }catch (Exception e){
                      e.printStackTrace();
                  }

          }
          private void loadListener(Model model, final ViewHolder.FoodViewHolder viewHolder, int position){
              viewHolder.itemView.setOnClickListener(view -> {
                  Intent intent;
                  intent = new Intent(context, InfoActivity.class);
                  intent.putExtra("1",(model.getDATE()));
                  intent.putExtra("2",getFormatedTime(Long.parseLong(model.getDATE())));
                  intent.putExtra("3",String.valueOf(model.getQUANTITY()));
                  intent.putExtra("4",model.getNOTE());
                  intent.putExtra("5",String.valueOf(model.getAMOUNT()));
                  intent.putExtra("6",""+ model.getPAID_VIA());
                  intent.putExtra("7",String.valueOf( model.getPAID_AMOUNT()));
                  intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                  context.startActivity(intent);
              });
          }

          @Override
          public int getItemCount() {
              if (dataList==null)
                  return 0;
              else
              return (int) dataList.size();
          }
      }
    }
