package com.globalsight.ling.tm3.core;

/**
 * Top-level throwable for TM3 errors.  In general, these are wrappers for 
 * org.hibernate.HibernateException or java.sql.SQLException.
 *
 */
public class TM3Exception extends RuntimeException {
    public TM3Exception(String s) {
        super(s);
    }
    
    public TM3Exception(String string, Throwable root) {
        super(string, root);
    }
               
    public TM3Exception(Throwable root) { 
        super(root);
    }
}
