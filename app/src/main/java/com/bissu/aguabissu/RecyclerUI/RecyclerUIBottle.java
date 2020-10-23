package com.bissu.aguabissu.RecyclerUI;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bissu.aguabissu.InfoActivity;
import com.bissu.aguabissu.Model;
import com.bissu.aguabissu.R;
import com.bissu.aguabissu.ViewHolder;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class RecyclerUIBottle {

    private final SimpleDateFormat dateFormat;
    private final FilterAdapter adapter;
    private final Context context;
    private final List<Model> list;
    private final String type;
    private final Calendar calendar;


    public RecyclerUIBottle(List<Model> list, Context context, RecyclerView recyclerView,
                            TextView costText, TextView totalBottleText,
                            String key, String type, long bottle, long returnbottle) {
        this.context = context;
        this.type = type;
        adapter = new FilterAdapter(list);
        dateFormat = new SimpleDateFormat("dd MMM yy hh:mm aa", Locale.UK);
        calendar= Calendar.getInstance();
        LinearLayoutManager mLayoutManager =
                new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, true);

        recyclerView.setNestedScrollingEnabled(true);
        recyclerView.setLayoutManager(mLayoutManager);
        this.list=list;
        try {
            recyclerView.scrollToPosition((list.size()-1));
        }catch (Exception e){e.printStackTrace();}
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
        totalBottleText.setText(String.format(Locale.UK,"Taken %d %s \nReturned %d", bottle, type,returnbottle));
        costText.setText(getFormatedAmount(bottle));
    }

    private String getFormatedAmount(long amount){
        return ""+ NumberFormat.getNumberInstance(Locale.UK).format(amount);
    }
      class FilterAdapter extends RecyclerView.Adapter<ViewHolder.FoodViewHolder> {
          List<Model> dataList;
          FilterAdapter(List<Model> data){
              this.dataList=data;
          }
          @NonNull
          @Override
          public ViewHolder.FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
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
                        viewHolder.setBottleImage();
//                      Glide.with(context).load(context.getDrawable(R.drawable.water_bottle_big)).apply(RequestOptions.circleCropTransform()).
//                          into((ImageView) viewHolder.itemView.findViewById(R.id.img1));
                        viewHolder.setOrderNo(String.valueOf(model.getTIME()));
                      viewHolder.setTime(getFormatedTime(model.getTIME()));
                      TextView textView= viewHolder.mView.findViewById(R.id.amount);
                          if(model.getPAID_VIA()==null) {
                              textView.setTextColor(context.getResources().getColor(R.color.green));
                              viewHolder.setAmount("Returned "+model.getQUANTITY()+" bottles");
                          }else{
                              textView.setTextColor(context.getResources().getColor(R.color.red));
                              viewHolder.setAmount("Taken "+model.getQUANTITY()+" bottles");
                          }
                      loadListener(model,viewHolder);
                  }catch (Exception e){
                      e.printStackTrace();
                  }

          }

          private void loadListener(Model model, final ViewHolder.FoodViewHolder viewHolder){
              viewHolder.itemView.setOnClickListener(view -> {
                  Intent intent;
                  intent = new Intent(context, InfoActivity.class);
                  intent.putExtra("1", String.valueOf(model.getTIME()));
                  intent.putExtra("2",getFormatedTime(model.getTIME()));
                  intent.putExtra("3", String.valueOf(model.getQUANTITY()));
                  intent.putExtra("4",model.getNOTE());
                  intent.putExtra("5", String.valueOf(model.getAMOUNT()));
                  intent.putExtra("6",""+ model.getPAID_VIA());
                  intent.putExtra("7", String.valueOf( model.getPAID_AMOUNT()));
                  intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                  context.startActivity(intent);
              });
          }

          @Override
          public int getItemCount() {
              if (dataList==null)
                  return 0;
              else
              return dataList.size();
          }
      }
    }
