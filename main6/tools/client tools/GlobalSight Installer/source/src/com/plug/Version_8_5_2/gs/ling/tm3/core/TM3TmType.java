package com.plug.Version_8_5_2.gs.ling.tm3.core;

/**
 * TM types supported TM3.
 */
public enum TM3TmType {
    /**
     * A Shared Multilingual TM can store data from any number of language 
     * pairs. Furthermore, it uses a common set of tables, rather than having
     * a dedicated private tablespace. 
     */
    MULTILINGUAL_SHARED(0),
    /**
     * A Multilingual TM can store data from any number of language 
     * pairs.  It has its own private tablespace.
     */
    MULTILINGUAL(1),
    /**
     * A Bilingual TM can only store data about a single language pair.
     * It has its own private tablespace.
     */
    BILINGUAL(2);
    
    private int id;
    TM3TmType(int id) {
        this.id = id;
    }
    
    public int getId() {
        return id;
    }
    
    public static TM3TmType valueOf(int id) {
        switch (id) {
        case 0: return MULTILINGUAL_SHARED;
        case 1: return MULTILINGUAL;
        case 2: return BILINGUAL;
        }
        return null;
    }
}
