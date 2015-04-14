/**
 *  Copyright 2009 Welocalize, Inc. 
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  
 *  You may obtain a copy of the License at 
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  
 */
package com.globalsight.persistence.hibernate;

import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.service.ServiceRegistryBuilder;

import com.globalsight.everest.persistence.PersistentObject;
import com.globalsight.everest.persistence.tuv.SegmentTuTuvCacheManager;
import com.globalsight.ling.tm2.persistence.DbUtil;

/**
 * <code>HibernateUtil</code> can help us with using hibernate.
 */
public class HibernateUtil
{
    static private final Logger s_logger = Logger
            .getLogger(HibernateUtil.class);

    private static ThreadLocal<Session> sessionContext = new ThreadLocal<Session>();

    private static final SessionFactory sessionFactory;

    static
    {
        try
        {
            URL url = HibernateUtil.class.getResource("");
            String path = url.getPath();
            String path2 = path.substring(0, path.indexOf("globalsight.jar"))
                    + "lib\\classes\\c3p0-config.xml";
            path2 = path2.replace("\\", "/").replace("/", File.separator);
            System.setProperty("com.mchange.v2.c3p0.cfg.xml", path2);
        }
        catch (Exception e)
        {
            s_logger.error(
                    "Fail to set system property 'com.mchange.v2.c3p0.cfg.xml'",
                    e);
        }

        try
        {
            Configuration cfg = new Configuration().configure();
            sessionFactory = cfg
                    .buildSessionFactory(new ServiceRegistryBuilder()
                            .buildServiceRegistry());
        }
        catch (Throwable ex)
        {
            ex.printStackTrace(System.out);
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static SessionFactory getSessionFactory()
    {
        return sessionFactory;
    }

    /**
     * Closes the session in Threadlocal, it will be called by HibernateFilter
     * auto.
     */
    public static void closeSession()
    {
        Session session = sessionContext.get();
        if (session != null)
        {
            if (session.isOpen())
            {
                session.close();
            }
            else
            {
                if (s_logger.isDebugEnabled())
                {
                    s_logger.info("Hibernate session has been closed by others");
                }
            }

            sessionContext.set(null);
            s_logger.debug("Hibernate close session");
        }

        SegmentTuTuvCacheManager.clearCache();
    }

    public static void commit(Transaction tx)
    {
        if (tx != null)
        {
            tx.commit();
        }
    }

    /**
     * Excutes the hql which starts with "select count" and returns the count
     * result.
     * 
     * @param hql
     * @return the count result.
     * @throws HibernateException
     */
    public static int count(String hql) throws HibernateException
    {
        return count(hql, null);
    }

    /**
     * Execute the hql which starts with "select count" and returns the count
     * result.
     * 
     * @param hql
     * @param params
     * @return the count result.
     * @throws HibernateException
     */
    public static int count(String hql, Map<String, ?> params)
            throws HibernateException
    {
        Session session = getSession();

        Query query = session.createQuery(hql);

        if (params != null)
        {
            Iterator<String> iterator = params.keySet().iterator();
            while (iterator.hasNext())
            {
                String key = iterator.next();
                query.setParameter(key, params.get(key));
            }
        }

        if (query.uniqueResult() != null)
        {
            return ((Long) query.uniqueResult()).intValue();
        }
        else
        {
            return 0;
        }
    }

    /**
     * Execute the Sql which starts with "select count" and returns the count
     * result.
     * 
     * @param hql
     * @param params
     * @return the count result.
     * @throws HibernateException
     */
    public static int countWithSql(String sql, Map<String, ?> params)
            throws HibernateException
    {
        Session session = getSession();

        Query query = session.createSQLQuery(sql);

        if (params != null)
        {
            Iterator<String> iterator = params.keySet().iterator();
            while (iterator.hasNext())
            {
                String key = iterator.next();
                query.setParameter(key, params.get(key));
            }
        }

        return ((Number) query.uniqueResult()).intValue();

    }

    /**
     * Delete some instance. More information you can see
     * <code>delete(Object object)</code>.
     * 
     * @param objects
     *            classes to delete
     * @throws Exception
     */
    public static void delete(Collection<?> objects) throws Exception
    {
        if (objects != null && objects.size() > 0)
        {
            Session session = getSession();
            Transaction tx = getTransaction();
            try
            {
                Iterator<?> iterator = objects.iterator();
                while (iterator.hasNext())
                {
                    delete(session, iterator.next());
                }
                commit(tx);
            }
            catch (Exception e)
            {
                rollback(tx);
                e.printStackTrace();
                throw e;
            }
        }
    }

    /**
     * Remove a persistent instance from the datastore. The argument may be an
     * instance associated with the receiving <tt>Session</tt> or a transient
     * instance with an identifier associated with existing persistent state.
     * This operation cascades to associated instances if the association is
     * mapped with <tt>cascade="delete"</tt>.
     * 
     * @param object
     *            the instance to be removed
     * @throws Exception
     */
    public static void delete(Object object) throws Exception
    {
        if (object == null)
        {
            return;
        }

        Session session = getSession();
        Transaction tx = getTransaction();
        try
        {
            delete(session, object);
            commit(tx);
        }
        catch (Exception e)
        {
            rollback(tx);
            e.printStackTrace();
            throw e;
        }
    }

    private static void delete(Session session, Object ob)
            throws IllegalArgumentException, IllegalAccessException
    {
        if (ob == null || session == null)
        {
            return;
        }
        if (ob instanceof PersistentObject && isUseActive(ob))
        {
            PersistentObject pOb = (PersistentObject) ob;
            pOb = (PersistentObject) session.get(ob.getClass(),
                    pOb.getIdAsLong());
            pOb.setIsActive(false);
            session.update(pOb);
        }
        else
        {
            session.delete(ob);
        }
    }

    /**
     * Execute the hql, return the result.
     * 
     * @param hql
     *            the hql to execute.
     * @param values
     * @return
     * @throws Exception
     */
    public static void excute(String hql) throws HibernateException
    {
        if (hql == null)
        {
            throw new IllegalArgumentException(
                    "Parameter 'hql' can not be null.");
        }

        Session session = getSession();
        Transaction tx = getTransaction();

        try
        {
            session.createQuery(hql).executeUpdate();
            commit(tx);
        }
        catch (HibernateException e)
        {
            rollback(tx);
            throw e;
        }
        // finally
        // {
        // session.close();
        // }
    }

    /**
     * Execute the hql, return the result.
     * 
     * @param hql
     *            the hql to execute.
     * @param values
     * @return
     * @throws Exception
     */
    public static void excute(String hql, Map<String, ?> params)
            throws HibernateException
    {
        if (hql == null)
        {
            throw new IllegalArgumentException(
                    "Parameter 'hql' can not be null.");
        }

        Session session = getSession();
        Transaction tx = getTransaction();

        try
        {
            Query query = session.createQuery(hql);
            if (params != null)
            {
                Iterator<?> iterator = params.keySet().iterator();
                while (iterator.hasNext())
                {
                    String key = (String) iterator.next();
                    query.setParameter(key, params.get(key));
                }
            }
            query.executeUpdate();

            commit(tx);
        }
        catch (HibernateException e)
        {
            rollback(tx);
            throw e;
        }
        finally
        {
            // session.close();
        }
    }

    /**
     * Get a persistent instance, you can get more information from
     * <code>public static Object get(Class clazz, Serializable id) throws Exception</code>
     * 
     * @param clazz
     * @param id
     * @return
     * @throws Exception
     */
    public static <T> T get(Class<T> clazz, long id) throws HibernateException
    {
        return get(clazz, new Long(id));
    }

    /**
     * Get a persistent instance, you can get more information from
     * <code>public static Object get(Class clazz, Serializable id) throws Exception</code>
     * 
     * @param clazz
     * @param id
     * @return
     * @throws Exception
     */
    public static <T> T get(Class<T> clazz, long id, boolean ignoreInActiveOb)
            throws HibernateException
    {
        return get(clazz, new Long(id), ignoreInActiveOb);
    }

    /**
     * Get a persistent instance, you can get more information from
     * <code>public static Object get(Class clazz, Serializable id) throws Exception</code>
     * 
     * @param clazz
     * @param id
     * @return
     * @throws Exception
     */
    public static <T> T getEvenInActic(Class<T> clazz, long id)
            throws HibernateException
    {
        return get(clazz, new Long(id));
    }

    /**
     * Return the persistent instance of the given entity class with the given
     * identifier, or null if there is no such persistent instance. (If the
     * instance is already associated with the session, return that instance.
     * This method never returns an uninitialized instance.)
     * 
     * @param clazz
     *            a persistent class
     * @param id
     *            an identifier
     * @return a persistent instance or null
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public static <T> T get(Class<T> clazz, Serializable id,
            boolean ignoreInActiveOb) throws HibernateException
    {
        T result = null;
        if (clazz != null)
        {
            Session session = null;
            try
            {
                session = getSession();
                result = (T) session.get(clazz, id);

                if (ignoreInActiveOb && result != null && isUseActive(result))
                {
                    Method method = result.getClass().getMethod("isActive");
                    Boolean active = (Boolean) method.invoke(result,
                            (Object[]) null);
                    if (active != null && !active.booleanValue())
                    {
                        result = null;
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
                throw new HibernateException(e);
            }
        }

        return result;
    }

    public static <T> T get(Class<T> clazz, Serializable id)
            throws HibernateException
    {
        return get(clazz, id, true);
    }

    /**
     * Execute the hql, return the first result or null if nothing found.
     * 
     * @param hql
     * @return
     * @throws HibernateException
     */
    public static Object getFirst(String hql) throws HibernateException
    {
        try
        {
            List<?> result = search(hql);
            if (result == null || result.size() == 0)
            {
                return null;
            }
            return result.get(0);
        }
        catch (Exception e)
        {
            throw (HibernateException) e;
        }
    }

    /**
     * Execute the hql, return the first result or null if nothing found.
     * 
     * @param hql
     *            the hql to execute.
     * @param params
     * @return
     * @throws HibernateException
     */
    public static Object getFirst(String hql, Map<String, ?> params)
            throws HibernateException
    {
        try
        {
            List<?> result = search(hql, params);
            if (result == null || result.size() == 0)
            {
                return null;
            }
            return result.get(0);
        }
        catch (Exception e)
        {
            throw (HibernateException) e;
        }
    }

    public static Object getFirst(String hql, Object param1)
            throws HibernateException
    {
        List<?> result = search(hql, param1);
        if (result == null || result.size() == 0)
        {
            return null;
        }
        return result.get(0);
    }

    public static Object getFirst(String hql, Object param1, Object param2)
            throws HibernateException
    {
        List<?> result = search(hql, param1, param2);
        if (result == null || result.size() == 0)
        {
            return null;
        }
        return result.get(0);
    }

    public static Object getFirst(String hql, Object param1, Object param2,
            Object param3) throws HibernateException
    {
        List<?> result = search(hql, param1, param2, param3);
        if (result == null || result.size() == 0)
        {
            return null;
        }
        return result.get(0);
    }

    public static <T> T getFirstWithSql(Class<T> entityClass, String sql,
            Object param1) throws HibernateException
    {
        try
        {
            List<T> result = searchWithSql(entityClass, sql, param1);
            if (result == null || result.size() == 0)
            {
                return null;
            }
            return result.get(0);
        }
        catch (Exception e)
        {
            throw (HibernateException) e;
        }
    }

    public static Object getFirstWithSql(Class<?> entityClass, String sql,
            Object param1, Object param2) throws HibernateException
    {
        try
        {
            List<?> result = searchWithSql(entityClass, sql, param1, param2);
            if (result == null || result.size() == 0)
            {
                return null;
            }
            return result.get(0);
        }
        catch (Exception e)
        {
            throw (HibernateException) e;
        }
    }

    public static <T> T getFirstWithSql(Class<T> entityClass, String sql,
            Object param1, Object param2, Object param3)
            throws HibernateException
    {
        try
        {
            List<T> result = searchWithSql(entityClass, sql, param1, param2,
                    param3);
            if (result == null || result.size() == 0)
            {
                return null;
            }
            return result.get(0);
        }
        catch (Exception e)
        {
            throw (HibernateException) e;
        }
    }

    public static Object getFirstWithSql(String sql) throws HibernateException
    {
        try
        {
            List<?> result = searchWithSql(sql, null);
            if (result == null || result.size() == 0)
            {
                return null;
            }
            return result.get(0);
        }
        catch (Exception e)
        {
            throw (HibernateException) e;
        }
    }

    /**
     * Execute the sql, return the first result or null if nothing found.
     * 
     * @param sql
     * @param params
     * @return
     */
    public static Object getFirstWithSql(String sql, Map<String, ?> params)
            throws HibernateException
    {
        try
        {
            List<?> result = searchWithSql(sql, params);
            if (result == null || result.size() == 0)
            {
                return null;
            }
            return result.get(0);
        }
        catch (Exception e)
        {
            throw (HibernateException) e;
        }
    }

    /**
     * Execute the sql, return the first result or null if nothing found.
     * 
     * @param sql
     * @param params
     * @param clazz
     * @return
     */
    public static <T> T getFirstWithSql(String sql, Map<String, ?> params,
            Class<T> clazz) throws HibernateException
    {
        try
        {
            List<T> result = searchWithSql(sql, params, clazz);
            if (result == null || result.size() == 0)
            {
                return null;
            }
            return result.get(0);
        }
        catch (Exception e)
        {
            throw (HibernateException) e;
        }
    }

    public static Object getFirstWithSql(String sql, Object param1)
            throws HibernateException
    {
        Session session = getSession();
        SQLQuery query = session.createSQLQuery(sql);
        query.setParameter(0, param1);

        List<?> result = query.list();

        if (result == null || result.size() == 0)
        {
            return null;
        }

        return result.get(0);
    }

    public static Object getFirstWithSql(String sql, Object param1, Object param2)
            throws HibernateException
    {
        Session session = getSession();
        SQLQuery query = session.createSQLQuery(sql);
        query.setParameter(0, param1);
        query.setParameter(01, param2);

        List<?> result = query.list();

        if (result == null || result.size() == 0)
        {
            return null;
        }

        return result.get(0);
    }
    
    /**
     * Gets the real object extends by <code>cglib</code>
     * 
     * @param obj
     *            The extends object.
     * @return The real obj.
     */
    public static Object getImplementation(Object obj)
    {
        if (obj instanceof HibernateProxy)
        {
            return ((HibernateProxy) obj).getHibernateLazyInitializer()
                    .getImplementation();
        }

        return obj;
    }

    /**
     * Gets a session from ThreadLocal.
     * 
     * @return session
     */
    public static Session getSession() throws HibernateException
    {
        Session session = sessionContext.get();
        if (session == null || !session.isOpen())
        {
            session = sessionFactory.openSession();
            session.setFlushMode(FlushMode.COMMIT);
            sessionContext.set(session);
            s_logger.debug("Hibernate open session");
        }

        return session;
    }

    public static Transaction getTransaction()
    {
        Session session = getSession();
        Transaction tx = session.getTransaction();
        if (!tx.isActive())
        {
            tx.begin();
            return tx;
        }

        return null;
    }

    private static boolean isUseActive(Object ob)
            throws IllegalArgumentException, IllegalAccessException
    {
        boolean useActive = false;
        Field[] fields = ob.getClass().getFields();
        for (int i = 0; i < fields.length; i++)
        {
            Field field = fields[i];
            if (field.getName().equals("useActive"))
            {
                useActive = field.getBoolean(ob);
                break;
            }
        }

        return useActive;
    }

    /**
     * Return the persistent instance of the given entity class with the given
     * identifier, assuming that the instance exists. This method might return a
     * proxied instance that is initialized on-demand, when a non-identifier
     * method is accessed. <br>
     * <br>
     * You should not use this method to determine if an instance exists (use
     * <tt>get()</tt> instead). Use this only to retrieve an instance that you
     * assume exists, where non-existence would be an actual error.
     * 
     * @param clazz
     *            a persistent class
     * @param id
     *            a valid identifier of an existing persistent instance of the
     *            class
     * @return the persistent instance or proxy
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public static <T> T load(Class<T> clazz, Serializable id) throws Exception
    {
        T result = null;

        if (clazz != null)
        {
            Session session = null;
            try
            {
                session = getSession();
                result = (T) session.load(clazz, id);
            }
            catch (Exception e)
            {
                e.printStackTrace();
                throw e;
            }
        }

        return result;
    }

    public static void rollback(Transaction tx)
    {
        if (tx != null)
        {
            tx.rollback();
        }
    }

    /**
     * Persist some instance. More informations you can see
     * <code>save(Object object)</code>.
     * 
     * @param objects
     *            persistent classes
     * @throws Exception
     */
    public static void save(Collection<?> objects) throws Exception
    {
        if (objects != null && objects.size() > 0)
        {
            Session session = getSession();
            Transaction tx = getTransaction();
            try
            {
                Iterator<?> iterator = objects.iterator();
                while (iterator.hasNext())
                {
                    session.save(iterator.next());
                }
                commit(tx);
            }
            catch (Exception e)
            {
                rollback(tx);
                e.printStackTrace();
                throw e;
            }
        }
    }

    /**
     * Persist the given transient instance, first assigning a generated
     * identifier. (Or using the current value of the identifier property if the
     * <tt>assigned</tt> generator is used.) This operation cascades to
     * associated instances if the association is mapped with
     * <tt>cascade="save-update"</tt>.
     * 
     * @param object
     *            a transient instance of a persistent class
     * @return the generated identifier
     * @throws Exception
     */
    public static void save(Object object) throws Exception
    {
        if (object == null)
        {
            return;
        }

        Session session = getSession();
        Transaction tx = getTransaction();

        try
        {
            session.save(object);
            commit(tx);
        }
        catch (HibernateException e)
        {
            rollback(tx);
            throw e;
        }
    }

    /**
     * Save or update some instance. More informations you can see
     * <code>saveOrUpdate(Object object)</code>.
     * 
     * @param objects
     *            persistent classes
     * @throws Exception
     */
    public static void saveOrUpdate(Collection<?> objects) throws Exception
    {
        if (objects != null && objects.size() > 0)
        {
            Session session = getSession();
            Transaction tx = getTransaction();
            try
            {
                Iterator<?> iterator = objects.iterator();
                int n = 0;
                while (iterator.hasNext())
                {
                    session.saveOrUpdate(iterator.next());

                    // Release memory.
                    n++;
                    if (n % 20 == 0)
                    {
                        session.flush();
                        session.clear();
                    }
                }

                commit(tx);
            }
            catch (Exception e)
            {
                rollback(tx);
                e.printStackTrace();
                throw e;
            }
        }
    }

    /**
     * Either <tt>save()</tt> or <tt>update()</tt> the given instance, depending
     * upon the value of its identifier property. By default the instance is
     * always saved. This behaviour may be adjusted by specifying an
     * <tt>unsaved-value</tt> attribute of the identifier property mapping. This
     * operation cascades to associated instances if the association is mapped
     * with <tt>cascade="save-update"</tt>.
     * 
     * @see Session#save(java.lang.Object)
     * @see Session#update(Object object)
     * @param object
     *            a transient or detached instance containing new or updated
     *            state
     * @throws Exception
     */
    public static void saveOrUpdate(Object object) throws HibernateException
    {
        if (object == null)
        {
            return;
        }
        Session session = getSession();
        Transaction tx = getTransaction();

        try
        {
            session.saveOrUpdate(object);
            commit(tx);
        }
        catch (HibernateException e)
        {
            rollback(tx);
            throw e;
        }
    }

    /**
     * 
     * 
     * @param session
     *            the session is not null, managed by invoked method.
     * @param objects
     *            need updated data
     * @throws Exception
     */
    public static void saveOrUpdate(Session session, Collection<?> objects)
            throws Exception
    {
        if (objects != null && objects.size() > 0)
        {

            if (session == null)
            {
                throw new Exception(
                        "No Hibernate session provided, can not access database.");
            }
            Iterator<?> iterator = objects.iterator();
            int n = 0;
            while (iterator.hasNext())
            {
                session.saveOrUpdate(iterator.next());
                // Release memory.
                n++;
                if (n % 20 == 0)
                {
                    session.flush();
                    session.clear();
                }
            }
        }
    }

    /**
     * Executes the hql and returns the result.
     * 
     * @param hql
     *            the hql to execute.
     * @param values
     * @return the search result.
     * @throws Exception
     */
    public static List<?> search(String hql) throws HibernateException
    {
        try
        {
            return search(hql, null);
        }
        catch (Exception e)
        {
            throw (HibernateException) e;
        }
    }

    /**
     * Execute the hql, return the result.
     * 
     * @param hql
     *            the hql to execute.
     * @param values
     * @return
     * @throws Exception
     */
    public static List<?> search(String hql, Map<String, ?> map)
    {
        return search(hql, map, 0, 0);
    }

    /**
     * Execute the hql, return the result.
     * 
     * @param hql
     *            The hql to execute.
     * @param map
     *            Includes common parameters.
     * @param map2
     *            includes list parameters.
     * @return The search result.
     */
    public static List<?> search(String hql, Map<String, ?> map,
            Map<String, Collection<?>> map2)
    {
        List<?> result = new ArrayList<Object>();

        if (hql != null)
        {
            Session session = getSession();
            Query query = session.createQuery(hql);

            if (map != null)
            {
                Iterator<String> iterator = map.keySet().iterator();
                while (iterator.hasNext())
                {
                    String key = iterator.next();
                    query.setParameter(key, map.get(key));
                }
            }

            if (map2 != null)
            {
                Iterator<String> iterator = map2.keySet().iterator();
                while (iterator.hasNext())
                {
                    String key = iterator.next();
                    query.setParameterList(key, map2.get(key));
                }
            }

            result = query.list();
        }

        return result;
    }

    /**
     * Execute the hql, return the result.
     * 
     * @param hql
     *            the hql to execute.
     * @param values
     * @return
     * @throws Exception
     */
    public static List<?> search(String hql, Map<String, ?> map, int first,
            int max)
    {
        List<?> result = new ArrayList<Object>();

        if (hql != null)
        {
            Session session = getSession();
            Query query = session.createQuery(hql);

            if (map != null)
            {
                Iterator<String> iterator = map.keySet().iterator();
                while (iterator.hasNext())
                {
                    String key = iterator.next();
                    query.setParameter(key, map.get(key));
                }
            }

            if (first > 0)
            {
                query.setFirstResult(first);
            }

            if (max > 0)
            {
                query.setMaxResults(max);
            }

            result = query.list();
        }

        return result;
    }

    public static List<?> search(String hql, Object param1)
            throws HibernateException
    {
        List<?> result = new ArrayList<Object>();

        if (hql == null)
        {
            return result;
        }

        Session session = getSession();
        result = session.createQuery(hql).setParameter(0, param1).list();

        return result;
    }

    public static List<?> search(String hql, Object param1, Object param2)
            throws HibernateException
    {
        List<?> result = new ArrayList<Object>();

        if (hql == null)
        {
            return result;
        }

        Session session = getSession();
        result = session.createQuery(hql).setParameter(0, param1)
                .setParameter(1, param2).list();

        return result;
    }

    public static List<?> search(String hql, Object param1, Object param2,
            Object param3) throws HibernateException
    {
        List<?> result = new ArrayList<Object>();

        if (hql == null)
        {
            return result;
        }

        Session session = getSession();
        result = session.createQuery(hql).setParameter(0, param1)
                .setParameter(1, param2).setParameter(2, param3).list();

        return result;
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> searchWithSql(Class<T> entityClass, String sql)
            throws HibernateException
    {
        Session session = getSession();

        SQLQuery query = session.createSQLQuery(sql);
        query.addEntity(entityClass);
        List<T> result = query.list();

        return result;
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> searchWithSql(Class<T> entityClass, String sql,
            Object param1) throws HibernateException
    {
        Session session = getSession();

        SQLQuery query = session.createSQLQuery(sql);
        query.addEntity(entityClass);
        query.setParameter(0, param1);
        List<T> result = query.list();

        return result;
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> searchWithSql(Class<T> entityClass, String sql,
            Object param1, Object param2) throws HibernateException
    {
        Session session = getSession();

        SQLQuery query = session.createSQLQuery(sql);
        query.addEntity(entityClass);
        query.setParameter(0, param1);
        query.setParameter(1, param2);
        List<T> result = query.list();

        return result;
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> searchWithSql(Class<T> entityClass, String sql,
            Object param1, Object param2, Object param3)
            throws HibernateException
    {
        Session session = getSession();

        SQLQuery query = session.createSQLQuery(sql);
        query.addEntity(entityClass);
        query.setParameter(0, param1);
        query.setParameter(1, param2);
        query.setParameter(2, param3);
        List<T> result = query.list();

        return result;
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> searchWithSql(Class<T> entityClass, String sql,
            Object param1, Object param2, Object param3, Object param4)
            throws HibernateException
    {
        Session session = getSession();

        SQLQuery query = session.createSQLQuery(sql);
        query.addEntity(entityClass);
        query.setParameter(0, param1);
        query.setParameter(1, param2);
        query.setParameter(2, param3);
        query.setParameter(3, param4);
        List<T> result = query.list();

        return result;
    }

    /**
     * Execute sql, return the result.
     * 
     * @param sql
     * @param params
     * @return
     */
    public static List<?> searchWithSql(String sql, Map<String, ?> params)
            throws HibernateException
    {
        Session session = getSession();
        SQLQuery query = session.createSQLQuery(sql);

        if (params != null)
        {
            Iterator<String> iterator = params.keySet().iterator();
            while (iterator.hasNext())
            {
                String key = iterator.next();
                query.setParameter(key, params.get(key));
            }
        }

        List<?> result = query.list();

        return result;
    }

    /**
     * Execute the update or delete sql statement.
     * 
     * @param sql
     * @return The number of entities updated or deleted.
     * @throws HibernateException
     * @throws SQLException
     */
    public static int executeSql(String sql) throws HibernateException,
            SQLException
    {
        return executeSql(sql, null);
    }

    /**
     * Execute the update or delete sql statement.
     * 
     * @param sql
     * @param params
     * @return The number of entities updated or deleted.
     * @throws HibernateException
     * @throws SQLException
     */
    public static int executeSql(String sql, List<?> params)
            throws HibernateException, SQLException
    {
        Connection connection = null;
        PreparedStatement stat = null;
        boolean autoCommit = true;
        int n = 0;
        try
        {
            connection = DbUtil.getConnection();
            autoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            stat = connection.prepareStatement(sql);
            if (params != null)
            {
                for (int i = 0; i < params.size(); i++)
                {
                    stat.setObject(i + 1, params.get(i));
                }
            }

            n = stat.executeUpdate();
            connection.commit();
        }
        catch (Exception e1)
        {
            s_logger.error(e1.getMessage(), e1);
            connection.rollback();
        }
        finally
        {
            if (stat != null)
            {
                stat.close();
            }

            if (connection != null)
            {
                try
                {
                    connection.setAutoCommit(autoCommit);
                    DbUtil.returnConnection(connection);
                }
                catch (Exception e)
                {
                    s_logger.error(e.getMessage(), e);
                }
            }
        }

        return n;
    }

    /**
     * Execute sql, return the result.
     * 
     * @param sql
     * @param params
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> searchWithSql(String sql, Map<String, ?> params,
            Class<T> clazz) throws HibernateException
    {
        Session session = getSession();
        SQLQuery query = session.createSQLQuery(sql);
        query.addEntity(clazz);

        if (params != null)
        {
            Iterator<String> iterator = params.keySet().iterator();
            while (iterator.hasNext())
            {
                String key = iterator.next();
                query.setParameter(key, params.get(key));
            }
        }

        List<T> result = query.list();

        return result;
    }
    
    /**
     * Execute sql, return the result.
     * 
     * @param sql
     * @param params
     * @return
     */
    public static List<?> searchWithSqlWithIn(String sql, Map<String, ?> params, Map<String, List<Object>> ins)
            throws HibernateException
    {
        Session session = getSession();
        SQLQuery query = session.createSQLQuery(sql);

        if (params != null)
        {
            Iterator<String> iterator = params.keySet().iterator();
            while (iterator.hasNext())
            {
                String key = iterator.next();
                query.setParameter(key, params.get(key));
            }
        }
        
        if (ins != null)
        {
            Iterator<String> iterator = ins.keySet().iterator();
            while (iterator.hasNext())
            {
                String key = iterator.next();
                query.setParameterList(key, ins.get(key));
            }
        }

        List<?> result = query.list();

        return result;
    }

    public static void update(Collection<?> objects) throws HibernateException
    {
        if (objects == null)
        {
            return;
        }

        Session session = getSession();
        Transaction tx = getTransaction();

        try
        {
            Iterator<?> iterator = objects.iterator();
            while (iterator.hasNext())
            {
                session.update(iterator.next());
            }

            commit(tx);
        }
        catch (HibernateException e)
        {
            rollback(tx);
            throw e;
        }
    }

    /**
     * Update the persistent instance with the identifier of the given detached
     * instance. If there is a persistent instance with the same identifier, an
     * exception is thrown. This operation cascades to associated instances if
     * the association is mapped with <tt>cascade="save-update"</tt>.
     * 
     * @param object
     *            a detached instance containing updated state
     * @throws Exception
     */
    public static void update(Object object) throws HibernateException
    {
        if (object == null)
        {
            return;
        }

        Session session = getSession();
        Transaction tx = getTransaction();

        try
        {
            session.update(object);
            commit(tx);
        }
        catch (HibernateException e)
        {
            rollback(tx);
            throw e;
        }
    }
    
    /**
     * Copy the state of the given object onto the persistent object with the same
     * identifier. If there is no persistent instance currently associated with
     * the session, it will be loaded. Return the persistent instance. If the
     * given instance is unsaved, save a copy of and return it as a newly persistent
     * instance. The given instance does not become associated with the session.
     * This operation cascades to associated instances if the association is mapped
     * with <tt>cascade="merge"</tt>.<br>
     * <br>
     * The semantics of this method are defined by JSR-220.
     *
     * @param object a detached instance with state to be copied
     * @return an updated persistent instance
     */
    public static void merge(Object object) throws HibernateException
    {
        if (object == null)
        {
            return;
        }

        Session session = getSession();
        Transaction tx = getTransaction();

        try
        {
            session.merge(object);
            commit(tx);
        }
        catch (HibernateException e)
        {
            rollback(tx);
            throw e;
        }
    }
}
