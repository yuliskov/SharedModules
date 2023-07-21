package com.liskovsoft.sharedutils.helpers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DateHelper {
    /**
     * Input example: "2022-09-11T23:39:38+00:00"<br/>
     * https://stackoverflow.com/questions/2597083/illegal-pattern-character-t-when-parsing-a-date-string-to-java-util-date<br/>
     * https://stackoverflow.com/questions/7681782/simpledateformat-unparseable-date-exception
     */
    public static long toUnixTimeMs(String timestamp) {
        if (timestamp == null || timestamp.isEmpty()) {
            return 0;
        }

        String longPattern = "yyyy-MM-dd'T'HH:mm:ss" + (timestamp.contains("+") ? "X" : "");
        String shortPattern = "yyyy-MM-dd";
        SimpleDateFormat format = new SimpleDateFormat(timestamp.contains("T") ? longPattern : shortPattern, Locale.US);
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date = null;
        try {
            date = format.parse(timestamp);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date != null ? date.getTime() : 0;
    }
}
