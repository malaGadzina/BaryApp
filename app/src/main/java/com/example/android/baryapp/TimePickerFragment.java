package com.example.android.baryapp;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.Calendar;

import static com.example.android.baryapp.ReservationActivity.godzinaP;
import static com.example.android.baryapp.ReservationActivity.minutaP;

/**
 * Created by DD on 2018-05-01.
 */

public class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current time as the default values for the picker
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        // Create a new instance of TimePickerDialog and return it
        return new TimePickerDialog(getActivity(), this, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        // Do something with the time chosen by the user
        TextView godz = (TextView) getActivity().findViewById(R.id.hour_btn);
        if (minute<10)
            godz.setText(hourOfDay+":0"+minute);
        else
            godz.setText(hourOfDay+":"+minute);
        godzinaP = hourOfDay;
        minutaP = minute;
    }
}
