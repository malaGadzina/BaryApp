package com.example.android.baryapp;

import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by DD on 2018-05-04.
 */

class Place implements Parcelable{
    private LatLng position;
    private String name;
    private String address;
    private String city;
    private int color;


    public Place(double ltt, double lon, String nn, String add){
        this.position = new LatLng(ltt,lon);
        this.name = nn;
        this.address = add;
        this.city = "Warszawa";
        this.color = Color.RED;
    }

    public Place(double ltt, double lon, String nn, String add, String mm, int cc){
        this.position = new LatLng(ltt,lon);
        this.name = nn;
        this.address = add;
        this.city = mm;
        this.color = cc;
    }

    protected Place(Parcel in) {
        position = in.readParcelable(LatLng.class.getClassLoader());
        name = in.readString();
        address = in.readString();
        city = in.readString();
        color = in.readInt();
    }

    public static final Creator<Place> CREATOR = new Creator<Place>() {
        @Override
        public Place createFromParcel(Parcel in) {
            return new Place(in);
        }

        @Override
        public Place[] newArray(int size) {
            return new Place[size];
        }
    };

    public LatLng getPos() {
        return position;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getCity() {
        return city;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeParcelable(position, i);
        parcel.writeString(name);
        parcel.writeString(address);
        parcel.writeString(city);
        parcel.writeInt(color);
    }
}
