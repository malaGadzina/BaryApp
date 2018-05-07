package com.example.android.baryapp;

import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Calendar;

public class ReservationActivity extends AppCompatActivity {

    Button hourBtn;
    Button plusBtn;
    Button minusBtn;
    Button rezerwacjaBtn;
    TextView liczbaLudzi;
    TextView adres;
    TextView miasto;

    boolean szybka;
    private Place miejsce;

    public  static int godzinaP=0;
    public  static int minutaP=0;
    private int liczba = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation);
        Intent i = getIntent();
        szybka = i.getBooleanExtra(MapsActivity.CZY_SZYBKA,false);
        miejsce = i.getParcelableExtra(MapsActivity.MIEJSCE_DANE);
        init();

    }

    private void init(){
        hourBtn = findViewById(R.id.hour_btn);
        plusBtn = findViewById(R.id.plus);
        minusBtn = findViewById(R.id.minus);
        rezerwacjaBtn = findViewById(R.id.rezerwuj_btn);
        liczbaLudzi = findViewById(R.id.guests);
        miasto = findViewById(R.id.city_txt);
        adres = findViewById(R.id.address_txt);

        final Calendar c = Calendar.getInstance();
        int h = c.HOUR_OF_DAY;
        int m = c.MINUTE+30;
        if (m>59){
            h++;
            m = m-60;
        }
        if(m<10)
            hourBtn.setText(h+":0"+m);
        else
            hourBtn.setText(h+":"+m);
        if (szybka)
            hourBtn.setEnabled(false);
        liczbaLudzi.setText("2");
        miasto.setText(miejsce.getCity());
        adres.setText(miejsce.getAddress());
        getActionBar().setTitle(miejsce.getName());

    }
    public void hourFun(View view) {
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getFragmentManager(),"TimePicker");
    }

    public void minusFun(View view) {
        liczba--;
        liczbaLudzi.setText(liczba);
    }

    public void plusFun(View view) {
        liczba++;
        liczbaLudzi.setText(liczba);
    }

    public void rezerwujFun(View view) {
        //TODO: rezerwacje
    }
}
