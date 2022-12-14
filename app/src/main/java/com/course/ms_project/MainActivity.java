package com.course.ms_project;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Queue;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class MainActivity extends AppCompatActivity implements CalendarAdepter.OnListItemSelectedInterface{
    TextView monthYearText;
    ItemAdapter itemAdapter;
    RecyclerView recyclerView1;
    RecyclerView recyclerView2;
    CardView cardView;
    TextView infoText;
    ImageButton insert_btn;
    String foodDate;
    TextView allcal;
    Double lati;
    Double longi;

    ArrayList f_cal_day=new ArrayList<>();
    ArrayList<Item> items = new ArrayList<Item>();
    DatabaseReference Food_db=FirebaseDatabase.getInstance().getReference();

    @SuppressLint("MissingInflatedId")
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //?????????
        allcal=findViewById(R.id.all_cal);
        monthYearText = findViewById(R.id.monthYearText);
        ImageButton preBtn = findViewById(R.id.pre_btn);
        ImageButton nextBtn = findViewById(R.id.next_btn);
        recyclerView1 = findViewById(R.id.recyclerView1);
        recyclerView2 = findViewById(R.id.recyclerView2);
        cardView = findViewById(R.id.cardView);
        infoText = findViewById(R.id.info_text);
        insert_btn = findViewById(R.id.insert_btn);

        itemAdapter = new ItemAdapter();
        recyclerView2.setAdapter(itemAdapter);


        //?????? ??????
        CalrendarUtil.selectedDate = LocalDate.now();

        //?????? ??????
        setMonthview();

        //?????? ??? ?????? ?????????
        preBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //-1??? ?????? ????????????.
                cardView.setVisibility(View.GONE);
                infoText.setVisibility(View.VISIBLE);
                CalrendarUtil.selectedDate = CalrendarUtil.selectedDate.minusMonths(1);
                setMonthview();
            }
        });

        //?????? ??? ?????? ?????????
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //+1??? ?????? ????????????
                cardView.setVisibility(View.GONE);
                infoText.setVisibility(View.VISIBLE);
                CalrendarUtil.selectedDate = CalrendarUtil.selectedDate.plusMonths(1);
                setMonthview();
            }
        });

        insert_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,CreatingDiet.class);
                intent.putExtra("food_date",foodDate);
                startActivity(intent);
            }
        });
    }//onCreate

    //?????? ?????? ??????
    @RequiresApi(api = Build.VERSION_CODES.O)
    private String yearMonthFromDate(LocalDate date){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy??? MM???");

        return date.format(formatter);
    }

    //?????? ??????
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setMonthview(){
        //?????? ???????????? ??????
        monthYearText.setText(yearMonthFromDate(CalrendarUtil.selectedDate));

        //?????? ??? ?????? ????????????
        ArrayList<LocalDate> daylist = daysInMonthArray(CalrendarUtil.selectedDate);

        //????????? ????????? ??????
        CalendarAdepter adapter = new CalendarAdepter(daylist, this);

        //???????????? ??????
        RecyclerView.LayoutManager manager = new GridLayoutManager(getApplicationContext(), 7);

        //???????????? ??????
        recyclerView1.setLayoutManager(manager);

        //????????? ??????
        recyclerView1.setAdapter(adapter);
    }

    //?????? ??????
    @RequiresApi(api = Build.VERSION_CODES.O)
    private ArrayList<LocalDate> daysInMonthArray(LocalDate date){

        ArrayList<LocalDate> dayList = new ArrayList<>();

        YearMonth yearMonth = YearMonth.from(date);

        //?????? ?????? ????????? ?????? ????????????
        int lastDay = yearMonth.lengthOfMonth();

        //?????? ?????? ??? ?????? ??? ????????????
        LocalDate firstDay = CalrendarUtil.selectedDate.withDayOfMonth(1);

        //??? ?????? ??? ?????? ????????????
        int dayOfWeek = firstDay.getDayOfWeek().getValue();

        //?????? ??????
        for (int i = 1; i < 44; i++){
            if (i <= dayOfWeek || i > lastDay + dayOfWeek){
                dayList.add(null);
            }
            else{
                dayList.add(LocalDate.of(CalrendarUtil.selectedDate.getYear(), CalrendarUtil.selectedDate.getMonth(), i - dayOfWeek));
            }
        }
        return dayList;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onItemSelected(View v, int position, LocalDate day) {
        itemAdapter.removeAllItem();

        ArrayList user_list=new ArrayList<>();
        ArrayList f_list=new ArrayList<>();
        ArrayList<Integer> c_list=new ArrayList<>();

        foodDate = day.toString();



        cardView.setVisibility(View.VISIBLE);
        infoText.setVisibility(View.GONE);
        //itemAdapter.removeAllItem();


        Food_db.child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getChildrenCount()==0){
                    itemAdapter.notifyDataSetChanged();
                    recyclerView2.startLayoutAnimation();
                }
                else{

                    itemAdapter.notifyDataSetChanged();
                    recyclerView2.startLayoutAnimation();

                    for( int i=1; i < snapshot.getChildrenCount()+1; i++){
                        if(snapshot.child(Integer.toString(i)).child("f_date").getValue().equals(foodDate)){
                            f_list.add(snapshot.child(Integer.toString(i)).getValue());

                            Integer cal= Integer.parseInt((String) snapshot.child(Integer.toString(i)).child("f_calorie").getValue());
                            Integer count=Integer.parseInt((String) snapshot.child(Integer.toString(i)).child("f_count").getValue());

                            Integer to_all_cal= Integer.valueOf(cal*count);
                            String today_cal= String.valueOf(to_all_cal);
                            c_list.add(to_all_cal);
                            add_item(user_list , (String) snapshot.child(Integer.toString(i)).child("f_name").getValue(), today_cal,i);
                        }
                        else{
                            System.out.println(" ");
                        }
                    }
                    System.out.println(c_list+"ffffffffffffff");
                    int sum=0;
                    for(int num : c_list){
                        sum+=num;
                    }
                    allcal.setText(String.valueOf(sum)+" kcal");

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.out.println(" ");
            }
        });

    }

    public void add_item(ArrayList userList,String foodName,String foodcalorie,Integer j){
        Item item=new Item();

       if(userList==null){
           item.setTitle(foodName);
           item.setCalorie(foodcalorie);
           item.setDescription(j.toString());

           itemAdapter.addItem(item);
           userList.add(j);
       }
       else {
           if(userList.contains(j)==false){

               item.setTitle(foodName);
               item.setCalorie(foodcalorie);
               item.setDescription(j.toString());

               itemAdapter.addItem(item);
               userList.add(j);
           }
       }
    }
}//MainActivity