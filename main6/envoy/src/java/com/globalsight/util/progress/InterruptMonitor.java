package com.globalsight.util.progress;

/**
 * A class that long-running jobs can use to see if they have been
 * canceled.  Long-running jobs should poll hasInterrupt() periodically
 * and abort themselves if it is set.
 */
public class InterruptMonitor {

    private volatile boolean flag = false;
    
    public void interrupt() {
        this.flag = true;
    }
    
    public void reset() {
        this.flag = false;
    }
    
    public boolean hasInterrupt() {
        return flag;
    }
}
