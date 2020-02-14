package com.sample.notify.BaseClasses;

import android.net.Uri;

public class LessonInProgres {

    public String hour,day,longitude,latitude,uwagi,przedmiot,uczen,poziom,phone;
    public String imageUri;

    LessonInProgres(){}

    public LessonInProgres(String uczen, String phone, String poziom, String przedmiot, String uwagi, String latitude, String longitude, String day, String hour,String imageUri) {
        this.uczen = uczen;
        this.poziom = poziom;
        this.przedmiot = przedmiot;
        this.uwagi = uwagi;
        this.latitude = latitude;
        this.longitude = longitude;
        this.day = day;
        this.hour = hour;
        this.phone = phone;
        this.imageUri = imageUri;
    }
}
