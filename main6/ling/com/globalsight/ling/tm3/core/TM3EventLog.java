package com.globalsight.ling.tm3.core;

/**
 * Provides access to a (potentially large) set of TM3Events.  An event log 
 * can provide access to events on a TM, a TU, or a TUV.
 * 
 * <p>
 * TODO: This probably needs some sort of paging interface.
 */
public class TM3EventLog {

    private Iterable<TM3Event> events;
    
    TM3EventLog(Iterable<TM3Event> events) {
        this.events = events;
    }
    
    /**
     * Get an Iterable that will return all the events in this log.
     * @return
     */
    public Iterable<TM3Event> getEvents() throws TM3Exception {
        return events;
    }

}
