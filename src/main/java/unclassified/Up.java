package unclassified;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class Up {

    public static void main(String[] args) throws ParseException {
        List<String> workers = new ArrayList<>();
        workers.add("worker03");
        workers.add("worker03");
        workers.add("worker03");
        workers.remove("worker03");

        System.out.println(workers);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy", Locale.ENGLISH);
        simpleDateFormat2.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));

        Date date = new Date();
        System.out.println(simpleDateFormat.format(date));
        System.out.println(simpleDateFormat2.format(date));

        String date2Parse = "Sat Sep 24 13:36:29 2022";
        Date parse = simpleDateFormat2.parse(date2Parse);
        System.out.println(parse);
        SimpleDateFormat simpleDateFormat3 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        simpleDateFormat3.setTimeZone(TimeZone.getTimeZone("UTC"));
        System.out.println(simpleDateFormat3.format(parse));



    }
}
