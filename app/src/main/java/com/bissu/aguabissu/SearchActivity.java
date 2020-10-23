package com.bissu.aguabissu;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Locale;
import java.util.Objects;

import static com.bissu.aguabissu.Constants.uid;

public class SearchActivity extends AppCompatActivity {
    private View progressBar;
    private Search adapter;
    private Runnable input_finish_checker;
    private final Handler handler = new Handler();
    private final long delay = 1000;
    private long last_text_edit = 0;
    private String searchKey = "";
    private ArrayList<Model> list;
    private View nothingFoundView;
    private Query orderDb;
    private Query transactionDb;
    ArrayList<Model> recentList;
    TextView recentTextView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controller_activity);

        nothingFoundView=findViewById(R.id.nothing_found);
        orderDb=FirebaseDatabase.getInstance().
                getReference("Orders");
        recentTextView=findViewById(R.id.recentText);
        transactionDb=new TransactionDb().getReference(this);
//                FirebaseDatabase.getInstance().
//                getReference("Transactions");
        progressBar=  findViewById(R.id.progressbar);
        progressBar.setVisibility(View.GONE);
        if(Constants.uid==null)
            Constants.uid = getSharedPreferences("USER_CREDENTIALS", MODE_PRIVATE).getString("UID", null);
         list=new ArrayList<>();
        LinearLayoutManager mLayoutManager = new LinearLayoutManager( SearchActivity.this, LinearLayoutManager.VERTICAL, false);
        RecyclerView recyclerView = findViewById(R.id.recycler_Chat);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        adapter=new Search(this,list);
        recyclerView.setAdapter(adapter);
        initializeMedSearch();
        searchBarListner();
        recentList=new ArrayList<>();
        loadRecentData();
    }

    private void loadRecentData() {
        ArrayList<String>recentTempList=new Recent().loadDataFromSharedPref(this);
        DatabaseReference   db;
      DatabaseReference tempTransaction=  new TransactionDb().getDatabaseReference(this).child("Transactions");
      DatabaseReference tempOrders=  FirebaseDatabase.getInstance().getReference("Orders");
        final int[] size = {recentTempList.size()};
        for (String id:recentTempList){
            String[]record=id.split(",");
           try {    if (record[1].contains("Tran"))
                        db=tempTransaction;
                    else    db=tempOrders;
                       db
                       .child(record[0])
                       .addListenerForSingleValueEvent(new ValueEventListener() {
                   @Override
                   public void onDataChange(@NonNull DataSnapshot d) {
                          try {
                              Model model = d.getValue(Model.class);
                              if (model!=null&&d.exists()) {
                                  Objects.requireNonNull(model).setId(d.getKey());
                                  model.setFrom(model.getQUANTITY() == 0 ? "Transactions" : "Orders");
                                  recentList.add(model);
                              }else size[0]--;
                          }catch (Exception e){e.printStackTrace();}

                       if (list.isEmpty()&& size[0] ==recentList.size())
                       {
                           Collections.reverse(recentList);
                           showRecent();
                       }
                   }

                   @Override
                   public void onCancelled(@NonNull DatabaseError error) {

                   }
               });
           }catch (Exception    e){e.printStackTrace();}
        }
    }

    private void showRecent() {
        if ( searchKey.isEmpty()) {
            list.clear();
            list.addAll(recentList);
            adapter.notifyDataSetChanged();
            if (!list.isEmpty())
                recentTextView.setVisibility(View.VISIBLE);
        }
//            list.clear();
//            list.addAll(recentList);
//            adapter.notifyDataSetChanged();
//            if (!list.isEmpty())
//                recentTextView.setVisibility(View.VISIBLE);
    }

    public void endActivity(View view) {
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
//        if (adapter!=null)
//            new Handler().postDelayed(() -> {
//                adapter.stopListening();
//                adapter=null;
//                Log.e("Runnable","->adapter stopped");
//            },1000);
    }

    private void searchBarListner(){
        EditText searchbar= findViewById(R.id.searchEdit);
        searchbar.setHint("Search by transaction / order number");
        searchbar.addTextChangedListener(new TextWatcher() {    @Override      public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {      }          @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            handler.removeCallbacks(input_finish_checker);        }
            @Override
            public void afterTextChanged(Editable editable) {
                searchKey=editable.toString();
                progressBar.setVisibility(View.VISIBLE);
                recentTextView.setVisibility(View.GONE);
                try {
                    list.clear();adapter.notifyDataSetChanged();
                    if(!searchKey.isEmpty()) {
                        nothingFoundView.setVisibility(View.GONE);
                        last_text_edit=System.currentTimeMillis();
                        handler.postDelayed(input_finish_checker,delay);
                    }else{
                        progressBar.setVisibility(View.GONE);
                        nothingFoundView.setVisibility(View.GONE);
                        showRecent();
                    }
                }catch (Exception e){e.printStackTrace();}
            }
        });
    }

    private void initializeMedSearch(){
        input_finish_checker= () -> {
            if (System.currentTimeMillis()>(last_text_edit+delay-500)){
                if (adapter!=null&&!searchKey.isEmpty())
                    firebase();
            }
        };
    }
    public void backOnClicked(View view) {
        finish();
    }
    void firebase(){
        list.clear();
        orderDb.orderByChild("KEY").startAt(uid+searchKey).endAt(uid +searchKey+ "\uf8ff").
                limitToLast(5)
            .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                progressBar.setVisibility(View.GONE);
                initializeModel(dataSnapshot);
            }  @Override      public void onCancelled(@NonNull DatabaseError databaseError) {    }
        });
        transactionDb.orderByChild("KEY").startAt(uid+searchKey).endAt(uid +searchKey+ "\uf8ff")
                .limitToLast(5)
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                progressBar.setVisibility(View.GONE);
                initializeModel(dataSnapshot);
            }  @Override      public void onCancelled(@NonNull DatabaseError databaseError) {    }
        });
    }
    void initializeModel(DataSnapshot   dataSnapshot){
        if (!searchKey.isEmpty()) {
            if (recentTextView.getVisibility()==View.VISIBLE)
            {
                list.clear();
                recentTextView.setVisibility(View.GONE);
                adapter.notifyDataSetChanged();
            }
            for (DataSnapshot d : dataSnapshot.getChildren()) {
                Model model = d.getValue(Model.class);
                Objects.requireNonNull(model).setId(d.getKey());
                model.setFrom(model.getQUANTITY() == 0 ? "Transactions" : "Orders");
                list.add(model);
            }
            if (list.size() > 0)
                adapter.notifyDataSetChanged();
            else nothingFoundView.setVisibility(View.VISIBLE);
        }
    }
}

class Search extends RecyclerView.Adapter<ViewHolder.FoodViewHolder> {
    private final SimpleDateFormat dateFormat;
    private final Calendar calendar;
    private final Context context;
    private final ArrayList<Model> dataList;

    Search(Context context, ArrayList<Model> dataList) {
        this.context = context;
        this.dataList = dataList;
        dateFormat = new SimpleDateFormat("dd MMM yy", Locale.UK);
        calendar = Calendar.getInstance();

    }


        @NonNull
        @Override
        public ViewHolder.FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder.FoodViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.adapter_layout, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder.FoodViewHolder viewHolder, int position) {
            {   Model model=dataList.get(position);
                try {
                    viewHolder.setOrderNo(model.getId());
                    viewHolder.setAmount(getFormatedTime(Long.parseLong(model.getDATE())));
                    TextView textView= viewHolder.mView.findViewById(R.id.time);
                    textView.setTextColor(context.getResources().getColor(R.color.green));
                    if(model.getFrom().equals("Orders"))
                        viewHolder.setTime("In Orders");
                    else
                        viewHolder.setTime("In Transactions");
                    loadListener(model,viewHolder,position);
                }
                catch(Exception e){
                    e.printStackTrace();
                }
            }
        }

        private void loadListener(Model model, final ViewHolder.FoodViewHolder viewHolder, int position){
            viewHolder.itemView.setOnClickListener(view -> {
                Intent intent;
                intent = new Intent(context, InfoActivity.class);
                intent.putExtra("1",model.getId());
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

        private String getFormatedTime(long time){
            calendar.setTimeInMillis(time);
            return dateFormat.format(calendar.getTime());
        }

        @Override
        public int getItemCount() {
            return dataList.size();
        }
    }

