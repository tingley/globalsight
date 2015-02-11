package com.globalsight.ling.tm3.core;

import java.util.Date;

/**
 * Representation of a single event affecting one or more entries in a TM.
 * Things that might generate events include:
 * <ul>
 * <li>TMX Import</li> 
 * <li>Saving a set of translations</li>
 * <li>Editing an individual TM entry</li>
 * </ul>
 * 
 * Events are accessed through a TM3EventLog, typically by calling 
 * <code>getEventLog()</code> on the appropriate object.
 * 
 * <p>
 * 
 * Events, once created, are read-only.  If a TU or TUV is deleted, its
 * event history is also deleted.  However, the underlying events that 
 * comprised the history (for example, the record of the TMX import) remains
 * and is still accessible through the TM's event log.  Removing the TM
 * will destroy its history entirely.
 * 
 * <p>
 * 
 * User names are stored as strings rather than live identifiers, to avoid
 * data integrity problems if the user is subsequently deleted.
 */
public class TM3Event {

    private Long id;
    private int type;
    private String username;
    private Date timestamp;
    private String arg;
    private BaseTm<?> tm;
      
    TM3Event() { }
  
    TM3Event(BaseTm<?> tm, int type, String username, String arg, Date timestamp) {
        this.tm = tm;
        this.type = type;
        this.username = username;
        this.arg = arg;
        this.timestamp = timestamp;
    }
    
    public Long getId() {
        return id;
    }
    
    void setId(Long id) {
        this.id = id;
    }
    
    /**
     * TODO: This needs to be an enum type.
     */
    public int getType() {
        return type;
    }
    
    public String getUsername() {
        return username;
    }
    
    public Date getTimestamp() {
        return timestamp;
    }
    
    void setTm(BaseTm<?> tm) {
        this.tm = tm;
    }

    BaseTm<?> getTm() {
        return tm;
    }
    
    /**
     * Generic "argument" field for the event.  For example, for a TMX import,
     * this may be the name of the TMX file imported.  
     * @return
     */
    public String getArgument() {
        return arg;
    }
    
    void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
    
    void setUsername(String username) { 
        this.username = username;
    }
    
    void setType(int type) {
        this.type = type;
    }
    
    void setArgument(String arg) {
        this.arg = arg;
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof TM3Event)) {
            return false;
        }
        TM3Event e = (TM3Event)o;
        if (getId() != null && e.getId() != null) {
            return getId().equals(e.getId());
        }
        else if (getId() == null && e.getId() == null) {
            return System.identityHashCode(this) == System.identityHashCode(e);
        }
        return false;
    }
}
