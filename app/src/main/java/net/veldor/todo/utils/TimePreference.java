package net.veldor.todo.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.preference.DialogPreference;

import java.util.Locale;

public class TimePreference extends DialogPreference {

    public TimePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onSetInitialValue(@Nullable Object defaultValue) {
        super.onSetInitialValue(defaultValue);
        Log.d("surprise", "TimePreference onSetInitialValue 20: setting initial value");
        setSummary(minutesFromMidnightToHourlyTime(getPersistedMinutesFromMidnight()));
    }


    // Save preference
    public void persistMinutesFromMidnight(int minutesFromMidnight) {
        super.persistInt(minutesFromMidnight);
        notifyChanged();
    }

    String minutesFromMidnightToHourlyTime(int minutesAfterMidnight) {
        int hours = minutesAfterMidnight / 60;
        int minutes = minutesAfterMidnight % 60;
        return String.format(Locale.ENGLISH, "%02d:%02d", hours, minutes);
    }

    public int getPersistedMinutesFromMidnight(){
        return super.getPersistedInt(getDefaultTimeValue());
    }

    private int getDefaultTimeValue() {
        String key = getKey();
        if(key.equals(Preferences.KEY_START_ACTIVE_TIME)){
            return Preferences.getInstance().getActiveStartTime();
        }
        else if(key.equals(Preferences.KEY_FINISH_ACTIVE_TIME)){
            return Preferences.getInstance().getActiveFinishTime();
        }
        return 0;
    }
}
