package edu.kfirawad.cyber2021finalprojectbeta;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;
import java.util.Locale;

import edu.kfirawad.cyber2021finalprojectbeta.dialog.TimePickerFragment;

public class RideCreateActivity extends AppCompatActivity implements TimePickerDialog.OnTimeSetListener {
    private EditText etName, etDestination;
    private TextView tvStartTime;
    private int startHour, startMinute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride_create);

        etName = findViewById(R.id.etName);
        etDestination = findViewById(R.id.etDestination);
        tvStartTime = findViewById(R.id.tvStartTime);

        final Calendar c = Calendar.getInstance();
        startHour = c.get(Calendar.HOUR_OF_DAY);
        startMinute = c.get(Calendar.MINUTE);
        updateStartTime();
    }

    private void updateStartTime() {
        tvStartTime.setText(String.format(Locale.ROOT, "%02d:%02d", startHour, startMinute));
    }

    public void onClick_btnChangeTime(View view) {
        TimePickerFragment tpFrag = new TimePickerFragment();
        tpFrag.show(getSupportFragmentManager(), "startTimePicker");
    }

    public void onClick_btnCreate(View view) {
        // TODO
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        startHour = hourOfDay;
        startMinute = minute;
        updateStartTime();
    }
}