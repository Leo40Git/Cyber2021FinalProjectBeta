package edu.kfirawad.cyber2021finalprojectbeta.dialog;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

import java.util.Calendar;

public class TimePickerFragment extends DialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        FragmentActivity activity = requireActivity();
        if (!(activity instanceof TimePickerDialog.OnTimeSetListener))
            throw new IllegalStateException("Creating activity must implement TimePickerDialog.OnTimeSetListener!");

        // Use the current time as the default values for the picker
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        // Create a new instance of TimePickerDialog and return it
        return new TimePickerDialog(activity, (TimePickerDialog.OnTimeSetListener) activity, hour, minute,
                DateFormat.is24HourFormat(activity));

    }
}
