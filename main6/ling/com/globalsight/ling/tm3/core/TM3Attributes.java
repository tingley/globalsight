package com.globalsight.ling.tm3.core;

import java.util.Collections;
import java.util.Map;
import java.util.HashMap;

/**
 * Utility methods to make attribute maps slightly easier to work with.
 */
public class TM3Attributes {

    private TM3Attributes() { }
    
    public static Map<TM3Attribute, Object> one(TM3Attribute attr, Object val) {
        Map<TM3Attribute, Object> map = new HashMap<TM3Attribute, Object>();
        map.put(attr, val);
        return map;
    }

    public static Map<TM3Attribute, Object> many(Entry...entries) {
        Map<TM3Attribute, Object> map = new HashMap<TM3Attribute, Object>();
        for (Entry e : entries) {
            map.put(e.attr, e.val);
        }
        return map;
    }
        
    public static class Entry {
        TM3Attribute attr;
        Object val;
        Entry(TM3Attribute attr, Object val) {
            this.attr = attr;
            this.val = val;
        }
    }
    
    public static Entry entry(TM3Attribute attr, Object val) {
        return new Entry(attr, val);
    }
    
    public static final Map<TM3Attribute, Object> NONE = Collections.emptyMap();
    
}
