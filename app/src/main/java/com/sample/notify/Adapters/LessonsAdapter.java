package com.sample.notify.Adapters;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.sample.notify.LessonsDetailActivity;
import com.sample.notify.R;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class LessonsAdapter extends RecyclerView.Adapter<LessonsAdapter.MyLessonViewHolder> {
    private LayoutInflater mInflater;
    private String[] key,przedmiot, lat, lng;
    public MapView map;
    public TextView Txtprzedmiot,TxtSzczegoly;
    private Context c1;

    public LessonsAdapter(Context c, String[] key, String[] przedmiot, String[] lat, String[] lng) {
        mInflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.przedmiot = przedmiot;
        this.lat = lat;
        this.lng = lng;
        this.key = key;
        c1 = c;
    }

    @NonNull
    @Override
    public MyLessonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        CardView cv = (CardView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.lessons_details, parent, false);
        return new MyLessonViewHolder(cv);
    }

    @Override
    public void onBindViewHolder(@NonNull MyLessonViewHolder holder, int i) {
        Txtprzedmiot.setText(przedmiot[i]);

        //holder.cardView
          TxtSzczegoly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(c1, LessonsDetailActivity.class);
                intent.putExtra("KEY",key[i]);
                c1.startActivity(intent);
            }
        });

        map.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                Double lat1 = Double.parseDouble(lat[i]);
                Double lng1 = Double.parseDouble(lng[i]);
                LatLng latLng = new LatLng(lat1, lng1);
                String adres = "";
                Geocoder geocoder = new Geocoder(c1, Locale.getDefault());
                try {
                    List<Address> addresses = geocoder.getFromLocation(lat1, lng1, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                    adres = addresses.get(0).getAddressLine(0);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                MarkerOptions markerOptions = new MarkerOptions().position(latLng).title(adres);
                googleMap.clear();
                googleMap.addMarker(markerOptions);
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,13));
                //animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13));
            }
        });
    }

    @Override
    public int getItemCount() {
        return przedmiot.length;
    }

    public class MyLessonViewHolder extends RecyclerView.ViewHolder implements OnMapReadyCallback {

        public CardView cardView;
        public GoogleMap googleMap;

        public MyLessonViewHolder(@NonNull CardView itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_lessons);
            map = itemView.findViewById(R.id.map_fragment);
            Txtprzedmiot = itemView.findViewById(R.id.txtVPrzedmiot);
            TxtSzczegoly = itemView.findViewById(R.id.szczegoly);

            map.onCreate(null);
            map.onResume();
            map.getMapAsync(this);
        }

        @Override
        public void onMapReady(GoogleMap googleMap) {
            MapsInitializer.initialize(c1);
            this.googleMap = googleMap;
        }
    }
}

