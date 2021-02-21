package net.veldor.todo.utils;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Grammar {

    public static String timestampToDate(long timestamp){
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.ENGLISH);
        return formatter.format(new Date(timestamp));
    }

    public static String filterToSting(boolean[] filter) {
        StringBuilder sb = new StringBuilder();
        for (boolean v :filter) {
            sb.append(v ? 1 : 0);
        }
        return sb.toString();
    }
}
