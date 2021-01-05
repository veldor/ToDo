package net.veldor.todo.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Grammar {

    public static String timestampToDate(long timestamp){
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.ENGLISH);
        return formatter.format(new Date(timestamp));
    }
}
