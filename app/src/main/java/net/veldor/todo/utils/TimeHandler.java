package net.veldor.todo.utils;

import android.util.Log;

import net.veldor.todo.App;
import net.veldor.todo.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class TimeHandler {
    private static final String[] months = "января, февраля, марта, апреля, мая, июня, июля, августа, сенрября, октября, ноября, декабря".split(", ");
    public static boolean isWorkingTime(){
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        Log.d("surprise", "TimeHandler isWorkingTime 8: now is " + hour);
        return hour > 8 && hour < 16;
    }

    public static String formatTime(long timestamp) {
        Date date= new Date(timestamp * 1000);
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int month = cal.get(Calendar.MONTH);
        SimpleDateFormat formatter = new SimpleDateFormat(App.getInstance().getString(R.string.date_pattern), Locale.ENGLISH);
        return cal.get(Calendar.DATE) + " " + months[month] +  formatter.format(date);
    }
}
