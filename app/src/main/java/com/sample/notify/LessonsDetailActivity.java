package com.sample.notify;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sample.notify.Adapters.LessonTimeAdapter;
import com.sample.notify.BaseClasses.LessonInProgres;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LessonsDetailActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static final String KEY="KEY";
    private DatabaseReference database;
    private String uczen,poziom,lat,lng,przedmiot,uwagi,imageUri,day,hour;
    private String[] time,data;
    private int i=0;
    GoogleMap map;
    SupportMapFragment mapView;
    LessonTimeAdapter dataAdapter;
    RecyclerView recyclerView;
    TextView txtNameDay,txtDate;
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson_detail);
        Intent intent = getIntent();
        String key = intent.getStringExtra(KEY);
        mapView = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_detail);
        mapView.getMapAsync(this);
        database = FirebaseDatabase.getInstance().getReference().child("lessons").child(key);
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                LessonInProgres lesson = dataSnapshot.getValue(LessonInProgres.class);
                uczen = lesson.uczen;
                poziom = lesson.poziom;
                lat = lesson.latitude;
                lng = lesson.longitude;
                przedmiot = lesson.przedmiot;
                uwagi = lesson.uwagi;
                imageUri = lesson.imageUri;
                hour = lesson.hour;
                day = lesson.day;
                setViews();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        txtNameDay = findViewById(R.id.nameOfWeek);
        txtDate = findViewById(R.id.date);
        txtNameDay.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_LEFT = 0;
                final int DRAWABLE_RIGHT = 2;

                if(event.getAction() == MotionEvent.ACTION_UP) {
                    if(event.getRawX() >= (txtNameDay.getRight() - txtNameDay.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        if(i<(time.length-1)){
                            i++;
                        }else {
                            i=0;
                        }
                        setRecycler();
                        return true;
                    }else if(event.getRawX() >= (txtNameDay.getLeft() - txtNameDay.getCompoundDrawables()[DRAWABLE_LEFT].getBounds().width())){
                        if(i==0){
                            i= time.length - 1;
                        }else {
                            i-=1;
                        }
                        setRecycler();
                        return true;
                    }
                }
                return false;
            }
        });
    }
    private BroadcastReceiver buttonAccept = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String selected = intent.getStringExtra(LessonTimeAdapter.KEY_SELECTED);
            Toast.makeText(getApplicationContext(),selected,Toast.LENGTH_SHORT).show();
            AlertDialog.Builder builder = new AlertDialog.Builder(LessonsDetailActivity.this);
            builder.setTitle("Zatwierdź lekcje");
            builder.setCancelable(true);
            builder.setMessage(getResources().getString(R.string.lorem_ipsum));
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent1 = new Intent(LessonsDetailActivity.this,MainActivity.class);
                    startActivity(intent1);
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    setRecycler();
                }
            });
            builder.show();
        }
    };

    private void setViews(){
        recyclerView = findViewById(R.id.lessonDetailRecycler);
        time = hour.split("\n");
        data = day.split("\n");

        TextView przedmiotTxt;
        przedmiotTxt = findViewById(R.id.przedmiotDetail);
        przedmiotTxt.setText(przedmiot);
        showMarker();
        setRecycler();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
    }
    private void showMarker(){
        Double lat1 = Double.parseDouble(lat);
        Double lng1 = Double.parseDouble(lng);
        LatLng latLng = new LatLng(lat1, lng1);
        String adres = "";
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(lat1, lng1, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            adres = addresses.get(0).getAddressLine(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        MarkerOptions markerOptions = new MarkerOptions().position(latLng).title(adres);
        map.clear();
        map.addMarker(markerOptions);
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));
    }
    private void setRecycler(){
        dataAdapter = new LessonTimeAdapter(getApplicationContext(),time[i]);
        recyclerView.setAdapter(dataAdapter);
        GridLayoutManager layoutManager = new GridLayoutManager(this,1);
        recyclerView.setLayoutManager(layoutManager);
        txtNameDay.setText(getNameOfDay(getDate(data[i])));
        txtDate.setText(getMonth(getDate(data[i])));
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
        manager.registerReceiver(buttonAccept,new IntentFilter(LessonTimeAdapter.KEY_BUTTON_ENABLE));
    }
    private String getNameOfDay(Date date){
        String dayOfWeek = new SimpleDateFormat("EEEE", Locale.ENGLISH).format(date);
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE,1);
        if(date.getDay()==cal.getTime().getDay())
            dayOfWeek = "Jutro";
        Log.d("TAG",cal.getTime()+"");
        Log.d("TAG",date+"");
        switch (dayOfWeek) {
            case "Monday":
                dayOfWeek = "Poniedziałek";
                break;
            case "Tuesday":
                dayOfWeek = "Wtorek";
                break;
            case "Wednesday":
                dayOfWeek = "Środa";
                break;
            case "Thursday":
                dayOfWeek = "Czwartek";
                break;
            case "Friday":
                dayOfWeek = "Piątek";
                break;
            case "Saturday":
                dayOfWeek = "Sobota";
                break;
            case "Sunday":
                dayOfWeek = "Niedziela";
        }
        return dayOfWeek;
    }
    private String getMonth(Date date){
        String string = "month";
        Calendar cal = Calendar.getInstance();
        String month= new SimpleDateFormat("MM", Locale.ENGLISH).format(date);
        String day = new SimpleDateFormat("dd", Locale.ENGLISH).format(date);
        switch (Integer.parseInt(month)){
            case 1:
                string="Styczeń";
                break;
            case 2:
                string="Luty";
                break;
            case 3:
                string="Marzec";
                break;
            case 4:
                string="Kwiecień";
                break;
            case 5:
                string="Maj";
                break;
            case 6:
                string="Czerwiec";
                break;
            case 7:
                string="Lipiec";
                break;
            case 8:
                string="Sierpień";
                break;
            case 9:
                string="Wrzesień";
                break;
            case 10:
                string="Październik";
                break;
            case 11:
                string="Listopad";
                break;
            case 12:
                string="Grudzień";
                break;
        }
        return day +" "+string;
    }
    private Date getDate(String dateInString){
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        Date date=null;
        try {
            date = formatter.parse(dateInString);
            System.out.println(formatter.format(date));

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }
}