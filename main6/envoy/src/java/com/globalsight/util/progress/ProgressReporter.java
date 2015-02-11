package com.globalsight.util.progress;


/**
 * Interface to allow a long-running job to provide status messages, 
 * for example to provide data that would then be exported via a 
 * ProcessMonitor.
 */
public interface ProgressReporter {

    /**
     * Set the current progress message key.  It is generally assumed that
     * this is a key into a localizable message bundle.
     * @param statusMessage progress message key
     * @param defualtMessage message to display if the key is not found
     */
    public void setMessageKey(String messageKey, String defaultMessage);
    
    /**
     * Set the current completion percentage.  
     * @param percentage completion percentage
     */
    public void setPercentage(int percentage);
}
