package com.example.snjcustomers;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static com.example.snjcustomers.Constants.uid;

public class ControllerActivity extends AppCompatActivity {
    private View progressBar;
    private String type;
    private Search adapter;
    private Runnable input_finish_checker;
    private Handler handler=new Handler();
    private long delay=1000;
    private long last_text_edit=0;
    private Query fireDb;
    private ArrayList<Model>list=new ArrayList<>();
    private View nothingFoundView;
    private String searchKey="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controller_activity);
        nothingFoundView=findViewById(R.id.nothing_found);
        Intent intent=getIntent();
        type=intent.getStringExtra("CLASS_NAME");
        progressBar=  findViewById(R.id.progressbar);
        progressBar.setVisibility(View.GONE);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager( this, LinearLayoutManager.VERTICAL, false);
        RecyclerView recyclerView = findViewById(R.id.recycler_Chat);;
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        adapter=new Search(this,list);
        recyclerView.setAdapter(adapter);
        if (type.contains("Trans"))
            fireDb=new TransactionDb().getReference(this);
        else
            fireDb=FirebaseDatabase.getInstance().getReference(type);
        if(Constants.uid==null)
            Constants.uid = getSharedPreferences("USER_CREDENTIALS", MODE_PRIVATE).getString("UID", null);
        initializeMedSearch();
        searchBarListner();
    }



    public void searchuttonOnclick(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            animation(true,view);
        }else{
            findViewById(R.id.searchBar).setVisibility(View.VISIBLE);
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    void animation(boolean show, View view) {
        final RelativeLayout cardView = findViewById(R.id.searchBar);

        int height = cardView.getHeight();
        int width = cardView.getWidth();
        int endRadius = (int) Math.hypot(width, height);
        int cx = (int) (view.getX());// + (cardView.getWidth()));
        int cy = (int) (view.getY());// + cardView.getHeight();

        if (show) {
            Animator revealAnimator = ViewAnimationUtils.createCircularReveal(cardView, cx, cy, cy, endRadius);
            revealAnimator.setDuration(400);
            revealAnimator.start();
            cardView.setVisibility(View.VISIBLE);
            // showZoomIn();
        } else {
            Animator anim =
                    ViewAnimationUtils.createCircularReveal(cardView, cx, cy, cx, 0);

            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    cardView.setVisibility(View.INVISIBLE);

                }
            });
            anim.setDuration(400);
            anim.start();
        }

    }

    public void endActivity(View view) {
        finish();
    }



    @Override
    public void onBackPressed() {
            super.onBackPressed();
    }

    private void searchBarListner(){
        EditText searchbar= findViewById(R.id.searchEdit);
        searchbar.setHint("Search by "+type.substring(0,type.length()-1).toLowerCase()+" number");
        searchbar.addTextChangedListener(new TextWatcher() {    @Override      public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {      }          @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            handler.removeCallbacks(input_finish_checker);
        }
            @Override
            public void afterTextChanged(Editable editable) {
                configureSearch();
                searchKey=editable.toString();
                try {
                    if(!searchKey.isEmpty()) {
                            last_text_edit=System.currentTimeMillis();
                            handler.postDelayed(input_finish_checker,delay);
                    }else{
                        progressBar.setVisibility(View.GONE);
                        nothingFoundView.setVisibility(View.GONE);
                        list.clear();
                        adapter.notifyDataSetChanged();
                    }
                }catch (Exception e){e.printStackTrace();}
            }
        });
    }

    private void configureSearch() {
        list.clear();
        adapter.notifyDataSetChanged();
        progressBar.setVisibility(View.VISIBLE);
        nothingFoundView.setVisibility(View.GONE);
    }


    void firebaseQuery(){
        configureSearch();
        fireDb.orderByChild("KEY")
                .startAt(uid+searchKey).endAt(uid +searchKey+ "\uf8ff")
                .limitToFirst(7)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        configureSearch();
                        progressBar.setVisibility(View.GONE);
                        initializeModel(dataSnapshot);
                    }  @Override      public void onCancelled(@NonNull DatabaseError databaseError) {    }
                });
    }
    void initializeModel(DataSnapshot   dataSnapshot){
        if (!searchKey.isEmpty()) {
            for (DataSnapshot d : dataSnapshot.getChildren()) {
                if (d.getKey().startsWith(searchKey)) {
                    Model model = d.getValue(Model.class);
                    Objects.requireNonNull(model).setId(d.getKey());
                    model.setFrom(model.getQUANTITY() == 0 ? "Transactions" : "Orders");
                    list.add(model);
                }
            }
            if (list.size() > 0)
                adapter.notifyDataSetChanged();
            else nothingFoundView.setVisibility(View.VISIBLE);
        }
    }

    private void initializeMedSearch(){
        input_finish_checker= () -> {
            if (System.currentTimeMillis()>(last_text_edit+delay-500)){
                if (adapter!=null&&!searchKey.isEmpty())
                    firebaseQuery();
            }
        };
    }

    public void backOnClicked(View view) {
        finish();
    }
    class Search extends RecyclerView.Adapter<ViewHolder.FoodViewHolder>{
        private final SimpleDateFormat dateFormat;
        private final Calendar calendar;
        private Context context;
        private ArrayList<Model> dataList;

        Search(Context context, ArrayList<Model> dataList) {
            this.context = context;
            this.dataList = dataList;
            dateFormat=new SimpleDateFormat("dd MMM yy", Locale.UK);
            calendar= Calendar.getInstance();

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
//                    viewHolder.setTime(getFormatedTime(model.getTIME()));
                    viewHolder.setTime(getFormatedTime(Long.parseLong(model.getDATE())));
                    viewHolder.setWalletStatus("Paid "+context.getString(R.string.rupeesSymbol)+" "+ model.getPAID_AMOUNT());
                    viewHolder.setOrderNo(model.getId());

                    if(type.equals("Orders")){
                        TextView textView= viewHolder.mView.findViewById(R.id.wallet);
                        if(model.getPAID_AMOUNT()<model.getAMOUNT()) {
                            textView.setTextColor(context.getResources().getColor(R.color.red));
                        }else
                            textView.setTextColor(context.getResources().getColor(R.color.green));
                        viewHolder.setAmount(context.getString(R.string.rupeesSymbol)+" "+model.getAMOUNT());

                    }else viewHolder.setAmount("Paid Via "+model.getPAID_VIA());

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
//                intent.putExtra("2",getFormatedTime(model.getTIME()));
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

}
