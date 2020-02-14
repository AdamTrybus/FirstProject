package com.sample.notify;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
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
        collapsibleCalendar = findViewById(R.id.collapsibleCalendarView);
        Calendar today=new GregorianCalendar();
        data = today.get(Calendar.DAY_OF_MONTH)+"/"+today.get(Calendar.MONTH)+"/"+today.get(Calendar.YEAR);
        collapsibleCalendar.setCalendarListener(new CollapsibleCalendar.CalendarListener() {
            @Override
            public void onDaySelect() {
                if(!results().isEmpty()) {
                    days.append(data).append("\n");
                    hours.append(results()).append("\n");
                }
                data = collapsibleCalendar.getSelectedDay().getDay() + "/" + collapsibleCalendar.getSelectedDay().getMonth() + "/" + collapsibleCalendar.getSelectedDay().getYear();
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

            @Override
            public void onItemClick(View v) {

            }

            @Override
            public void onDataUpdate() {

            }

            @Override
            public void onMonthChange() {

            }

            @Override
            public void onWeekChange(int position) {

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
}

