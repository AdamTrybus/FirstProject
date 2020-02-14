package com.sample.notify;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.GoogleMap;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.sample.notify.Adapters.LessonsAdapter;
import com.sample.notify.BaseClasses.LessonInProgres;

public class LessonsActivity extends AppCompatActivity {


    private LessonsAdapter itemAdapter;
    private RecyclerView recyclerView;
    private String[] key,przedmiot,lat,lng;
    private int i;
    private DatabaseReference database;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lessons);
        user = FirebaseAuth.getInstance().getCurrentUser();
        recyclerView = findViewById(R.id.recycler_lessons);
        database = FirebaseDatabase.getInstance().getReference().child("lessons");
        readData();
    }

    private void readData() {
        Query searchQuery = database.orderByChild("uczen").equalTo("Adam Trybus");
        searchQuery.addValueEventListener(queryValueEventListener);
    }

    ValueEventListener queryValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            i = (int) dataSnapshot.getChildrenCount();
            lat = new String[i];
            lng = new String[i];
            przedmiot = new String[i];
            key = new String[i];
            i=0;
            for (DataSnapshot singleSnapshot: dataSnapshot.getChildren()){
                LessonInProgres lesson = singleSnapshot.getValue(LessonInProgres.class);
                lat[i]=lesson.latitude;
                lng[i]=lesson.longitude;
                przedmiot[i]=lesson.przedmiot;
                key[i]=singleSnapshot.getKey();
                i++;
            }
            showList();
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };
    private void showList(){
        itemAdapter = new LessonsAdapter(getApplicationContext(),key,przedmiot,lat,lng);
        recyclerView.setAdapter(itemAdapter);
        GridLayoutManager layoutManager = new GridLayoutManager(this,1);
        recyclerView.setLayoutManager(layoutManager);
    }
}



