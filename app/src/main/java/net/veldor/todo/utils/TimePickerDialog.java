package net.veldor.todo.utils;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TimePicker;

import androidx.annotation.RequiresApi;
import androidx.preference.PreferenceDialogFragmentCompat;

import net.veldor.todo.App;

public class TimePickerDialog extends PreferenceDialogFragmentCompat {

    private final String mKey;
    private TimePicker mTimePicker;

    public TimePickerDialog(String key){
        mKey = key;
        Bundle bundle = new Bundle(1);
        bundle.putString(ARG_KEY, key);
        setArguments(bundle);
    }

    @Override
    protected View onCreateDialogView(Context context) {
        mTimePicker = new TimePicker(App.getInstance());
        return mTimePicker;
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        mTimePicker.setIs24HourView(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(mKey.equals(Preferences.KEY_START_ACTIVE_TIME)){
                int time = Preferences.getInstance().getActiveStartTime();
                mTimePicker.setHour(time / 60);
                mTimePicker.setMinute(time % 60);
            }
            else if(mKey.equals(Preferences.KEY_FINISH_ACTIVE_TIME)){
                int time = Preferences.getInstance().getActiveFinishTime();
                mTimePicker.setHour(time / 60);
                mTimePicker.setMinute(time % 60);
            }
            else{
                mTimePicker.setHour(9);
                mTimePicker.setMinute(0);
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onDialogClosed(boolean positiveResult) {
        if(positiveResult){
            int minutesAfterMidnight = (mTimePicker.getHour() * 60) + mTimePicker.getMinute();
            ((TimePreference)getPreference()).persistMinutesFromMidnight(minutesAfterMidnight);
            getPreference().setSummary(((TimePreference)getPreference()).minutesFromMidnightToHourlyTime(minutesAfterMidnight));
        }
    }

}
