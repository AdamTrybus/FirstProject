package com.sample.notify;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.sample.notify.Adapters.RecyclerViewAdapter;
import com.sample.notify.BaseClasses.LessonInProgres;
import com.sample.notify.BaseClasses.Model;
import com.sample.notify.BaseClasses.User;
import com.sample.notify.FirebaseMessanging.MySingleton;
import com.shrikanthravi.collapsiblecalendarview.widget.CollapsibleCalendar;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class NotificationActivity extends AppCompatActivity{

    public static final String PRZEDMIOT = "PRZEDMIOT";
    public static final String UWAGI = "UWAGI";
    public static final String LATITUDE = "LATITUDE";
    public static final String LONGITUDE = "LONGITUDE";

    final private String FCM_API = "https://fcm.googleapis.com/fcm/send";
    final private String serverKey = "key=" + "AAAANx6k1Ps:APA91bEfZVfyyDKH3WInPrpxro_7u_fvbUD5S5bZbisQqYiblVT3YJsL58R2fxCTXn1yh61fE_hNbEsVr53A5HBbm-aPTFvMymzMZF1BvGrc_6KAXXxTfKpZsUry9PYCtNhe4WXMTesl ";
    final private String contentType = "application/json";
    final String TAG = "NOTIFICATION TAG";
    String NOTIFICATION_TITLE;
    String NOTIFICATION_MESSAGE;
    String TOPIC;

    private List<Model> mModelList;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private String data;
    private StringBuilder days,hours;
    CollapsibleCalendar collapsibleCalendar;
    Intent intent;
    private String poziom;
    long childNumber=0;
    FirebaseUser user;
    String[] datesInRange;
    int i=0;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.activity_notification);
        Button btnAccept = findViewById(R.id.btnAccept);
        user = FirebaseAuth.getInstance().getCurrentUser();
        intent = getIntent();
        getSupportActionBar().setElevation(0);

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_time_slot);
        days = new StringBuilder();
        hours=new StringBuilder();
        setmAdapter();
        final TextView txtNameDay = findViewById(R.id.nameOfWeek);
        final TextView txtDate = findViewById(R.id.date);
        getDatesBetween();
        data = datesInRange[i];
        txtNameDay.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_LEFT = 0;
                final int DRAWABLE_RIGHT = 2;

                if(event.getAction() == MotionEvent.ACTION_UP) {
                    if(event.getRawX() >= (txtNameDay.getRight() - txtNameDay.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        if(!results().isEmpty()) {
                            days.append(data).append("\n");
                            hours.append(results()).append("\n");
                        }
                        if(i<(datesInRange.length-1))
                            i++;
                        else
                            i=0;

                        addDate();
                        txtNameDay.setText(getNameOfDay(getDate(datesInRange[i])));
                        txtDate.setText(getMonth(getDate(datesInRange[i])));
                        return true;
                    }else if(event.getRawX() >= (txtNameDay.getLeft() - txtNameDay.getCompoundDrawables()[DRAWABLE_LEFT].getBounds().width())){
                        if(!results().isEmpty()) {
                            days.append(data).append("\n");
                            hours.append(results()).append("\n");
                        }
                        if(i==0)
                            i= datesInRange.length - 1;
                        else
                            i-=1;

                        addDate();
                        txtNameDay.setText(getNameOfDay(getDate(datesInRange[i])));
                        txtDate.setText(getMonth(getDate(datesInRange[i])));
                        return true;
                    }
                }
                return false;
            }
        });
        btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TOPIC = "/topics/userABC"; //topic has to match what the receiver subscribed to
                NOTIFICATION_TITLE = "WorkForAll";
                NOTIFICATION_MESSAGE = "Zobacz szczegóły oferty";
                //FirebaseMessaging.getInstance().unsubscribeFromTopic(SUBSCRIBE_TO);
                if(!results().isEmpty()) {
                    days.append(data).append("\n");
                    hours.append(results()).append("\n");
                }

                JSONObject notification = new JSONObject();
                JSONObject notifcationBody = new JSONObject();

                try {
                    notifcationBody.put("title", NOTIFICATION_TITLE);
                    notifcationBody.put("message", NOTIFICATION_MESSAGE);
                    notifcationBody.put("token", FirebaseInstanceId.getInstance().getToken());
                    notifcationBody.put("przedmiot",intent.getStringExtra(PRZEDMIOT));
                    notifcationBody.put("uwagi",intent.getStringExtra(UWAGI));
                    notifcationBody.put("latitude",intent.getStringExtra(LATITUDE));
                    notifcationBody.put("longitude",intent.getStringExtra(LONGITUDE));
                    notifcationBody.put("dzien",days);
                    notifcationBody.put("godzina",hours);
                    notifcationBody.put("name","zero");
                    notifcationBody.put("uczen",user.getPhoneNumber());

                    notification.put("to", TOPIC);
                    notification.put("data", notifcationBody);

                } catch (JSONException e) {
                    Log.e(TAG, "onCreate: " + e.getMessage() );
                }
                sendNotification(notification);
                FirebaseMessaging.getInstance().subscribeToTopic(TOPIC);
                Intent intent1 = new Intent(NotificationActivity.this,MainActivity.class);
                addToDatabase();
                Toast toast = Toast.makeText(getApplicationContext(),"Prosze czekac na odpowiedź korepetytora",Toast.LENGTH_SHORT);
                startActivity(intent1);
                toast.show();
            }
        });
    }

    private void sendNotification(JSONObject notification) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(FCM_API, notification,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i(TAG, "onResponse: " + response.toString());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(NotificationActivity.this, "Request error", Toast.LENGTH_LONG).show();
                        Log.i(TAG, "onErrorResponse: Didn't work");
                    }
                }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Authorization", serverKey);
                params.put("Content-Type", contentType);
                return params;
            }
        };
        MySingleton.getInstance(getApplicationContext()).addToRequestQueue(jsonObjectRequest);
    }
    private void addToDatabase(){
        DatabaseReference database =  FirebaseDatabase.getInstance().getReference().child("lessons");
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(user.getPhoneNumber());
        database.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    childNumber = dataSnapshot.getChildrenCount();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user1 = dataSnapshot.getValue(User.class);
                poziom = user1.poziom;
                LessonInProgres lesson = new LessonInProgres(user.getDisplayName(),user.getPhoneNumber(),poziom,intent.getStringExtra(PRZEDMIOT),intent.getStringExtra(UWAGI),intent.getStringExtra(LATITUDE),
                        intent.getStringExtra(LONGITUDE),days.toString(),hours.toString(),user.getPhotoUrl().toString());
                database.child(String.valueOf(childNumber + 1)).setValue(lesson);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private List<Model> getListData() {
        mModelList = new ArrayList<>();
        for (int i = 8; i <=20 ; i++) {
            mModelList.add(new Model(i+".00"));
        }
        return mModelList;
    }
    private List<Model> setmModelList(String hours){
        String[] godzinki = hours.split(",");

        mModelList = new ArrayList<>();
        for (int i = 8; i <=20 ; i++) {
            mModelList.add(new Model(i+".00"));
        }

        for (Model model : mModelList) {
            for (String string:godzinki){
                if(model.getText().equals(string))
                    model.setSelected(true);
            }
        }
        return mModelList;
    }
    private String results(){
        String text = "";
        for (Model model : mModelList) {
            if (model.isSelected()) {
                text += model.getText() + ",";
            }
        }
        return text;
    }
    private void setmAdapter(){
        int i =0;
        String[] mHour=new String[0];
        if(days.length()!=0) {
            for (String day : days.toString().split("\n")) {
                if (day.equals(data)) {
                    mHour = hours.toString().split("\n");
                    break;
                } else {
                    i++;
                }
            }
        }
        if(mHour.length != 0) {
            mAdapter = new RecyclerViewAdapter(setmModelList(mHour[i]));
        }
        else {
            mAdapter = new RecyclerViewAdapter(getListData());
        }
        mRecyclerView.setHasFixedSize(true);
        GridLayoutManager layoutManager = new GridLayoutManager(this,3);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mAdapter);
    }
    private void addDate(){
        data = datesInRange[i];
        setmAdapter();
        String[] dni = days.toString().split("\n");
        String[] godziny = hours.toString().split("\n");
        int xz=0;
        for(String s : dni){
            if (s.equals(data)){
                s = days.toString().replace(data+"\n","");
                days = new StringBuilder();
                days.append(s);
                godziny[xz] ="";
                hours = new StringBuilder();
                for (String z : godziny){
                    if (!z.equals(""))
                        hours.append(z+"\n");
                }
            }
            xz++;
        }
    }
    private void getDatesBetween() {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DATE, 1);
        Date startDate = c.getTime();
        c.add(Calendar.DATE,5);
        Date endDate=c.getTime();

        Calendar calendar = new GregorianCalendar();
        calendar.setTime(startDate);
        Calendar endCalendar = new GregorianCalendar();
        endCalendar.setTime(endDate);
        datesInRange = new String[5];
        int y=0;
        while (calendar.before(endCalendar)) {
            Date result = calendar.getTime();
            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            datesInRange[y] = dateFormat.format(result);
            calendar.add(Calendar.DATE, 1);
            y++;
        }
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

