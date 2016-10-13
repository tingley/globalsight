package com.globalsight.persistence.hibernate;

import org.hibernate.Session;
import org.hibernate.HibernateException;

import static org.junit.Assert.*;
import org.junit.*;

/* This is a simple test that verifies we have access to the database.  If this
 * test causes an error, you probably have not added the generated properties
 * from a GlobalSight install to your classpath.  If testing from ant, set the
 * gs.home property.
 */
public class HibernateTest {

    @Test
    public void testHibernateBasics() throws HibernateException {
        HibernateUtil.getSession();
    }
}
