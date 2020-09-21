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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import static com.example.snjcustomers.Constants.getFormatedAmount;

public class StatsAdapter extends RecyclerView.Adapter<ViewHolder.FoodViewHolder> {
    private final Context context;
    private final SimpleDateFormat dateFormat;
    private final Calendar calendar;
    private final String type;
    List<Model> list;
    public StatsAdapter(List<Model> data, Context context, String type){
        this.list=data;
        this.context=context;
        this.type=type;
        dateFormat=new SimpleDateFormat("dd MMM yy hh:mm aa", Locale.UK);
        calendar= Calendar.getInstance();
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
//                      viewHolder.setTime(getFormatedTime(model.getTIME()));
            viewHolder.setTime(getFormatedTime(Long.parseLong(model.getDATE())));
            viewHolder.setWalletStatus("Paid "+getFormatedAmount(model.getPAID_AMOUNT()));
//                      viewHolder.setOrderNo(String.valueOf(model.getTIME()));
            viewHolder.setOrderNo(model.getDATE());

            if(type.equals("Orders")){
                TextView textView= viewHolder.mView.findViewById(R.id.wallet);
                if(model.getPAID_AMOUNT()<model.getAMOUNT()) {
                    textView.setTextColor(context.getResources().getColor(R.color.red));
                }else textView.setTextColor(context.getResources().getColor(R.color.green));
                viewHolder.setAmount(getFormatedAmount(model.getAMOUNT()));

            }else{
                if (model.getPAID_VIA().toLowerCase().contains("other"))
                viewHolder.setAmount("Paid via wallet");
                else
                    viewHolder.setAmount("Paid Via "+model.getPAID_VIA());
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
        if (list==null)
            return 0;
        else
            return list.size();
    }
}

