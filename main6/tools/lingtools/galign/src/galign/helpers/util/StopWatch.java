package galign.helpers.util;

import java.util.*;

public class StopWatch {
    Calendar startCal;
    Calendar endCal;
    TimeZone tz = TimeZone.getTimeZone("CST");
    
    /** Creates a new instance of StopWatch */
    public StopWatch() {
    }

    public StopWatch(String tzoneStr) {
      tz = TimeZone.getTimeZone(tzoneStr);
    }
    
    // Start the stopwatch
    public void start() {
        startCal = Calendar.getInstance(tz);
    }
    
    // Stop the stopwatch
    public void end() {
        endCal = Calendar.getInstance(tz);
    }
    
    // Measure the elapsed time in different units
    public double elapsedSeconds() {
        return (endCal.getTimeInMillis() - startCal.getTimeInMillis())/1000.0;
    }
    
    public long elapsedMillis() {
        return endCal.getTimeInMillis() - startCal.getTimeInMillis();
    }
    
    public double elapsedMinutes() {
        return (endCal.getTimeInMillis() - startCal.getTimeInMillis())/(1000.0 * 60.0);
    }
    
    public static void main (String [] args) {
        StopWatch sw = new StopWatch();
        sw.start();  // capture start time
        
        try {
        Thread.sleep(5000);   // sleep for 5 seconds
        }catch (Exception e) {
        System.out.println(e);
            System.exit(1);
        }
        
        sw.end();  // capture end time
        
        System.out.println("Elapsed time in minutes: " + sw.elapsedMinutes());
        System.out.println("Elapsed time in seconds: " + sw.elapsedSeconds());
        System.out.println("Elapsed time in milliseconds: " + sw.elapsedMillis());
    }
}  // end of StopWatch class
