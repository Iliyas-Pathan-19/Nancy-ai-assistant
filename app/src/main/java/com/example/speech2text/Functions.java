package com.example.speech2text;

import java.util.Calendar;

public class Functions {
    public static String wishMe() {
        String s = "";
        Calendar c = Calendar.getInstance();
        int time = c.get(Calendar.HOUR_OF_DAY);
        if (time >= 6 && time < 12) {
            s = "Good Morning Boss";
        } else if (time >= 12 && time < 16) {
            s = "Good Afternoon Boss";
        } else if (time >= 16 && time < 22) {
            s = "Good Evening Boss";
        } else {
            s = "Good Night Boss";
        }
        return s;
    }
}
