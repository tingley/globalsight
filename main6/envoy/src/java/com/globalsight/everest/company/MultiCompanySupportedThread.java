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
package com.globalsight.everest.company;

import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import com.globalsight.log.ActivityLog;
import com.globalsight.persistence.hibernate.HibernateUtil;

/**
 * To make the new thread get current company id currectly.
 * 
 * <pre>
 * public class TestA {
 *     public void funcOne() {
 *         Runnable runnable = new Runnable() {
 *             public void run() {
 *                 //Do something.
 *             }
 *         }
 *     
 *         //No, should not do like this.
 *         //Thread t = new Thread(runnable);
 *     
 *         //Correct!.
 *         Thread t = new MultiCompanySupportedThread(runnable);
 *     
 *         t.start();
 *     }
 * 
 *     public void funcTwo() {
 *         //No, should not do like this.
 *         //Thread t = new Thread() {
 *         //    public void run() {
 *         //        //Do something.
 *         //    }
 *         //}
 *     
 *         //Correct!.
 *         Thread t = new MultiCompanySupportedThread() {
 *             public void run() {
 *                 //Note here, must invoke super.run() first.
 *                 super.run();
 *             
 *                 //Do something.
 *             }
 *         }
 *     
 *         t.start();
 *     }
 * }
 * 
 * Another example:
 * Should not do this:
 * public class TestB extends Thraed {
 * }
 * 
 * Must like this:
 * public class TestB extends MultiCompanySupportedThread{
 *     public TestB(Object param ....) {
 *         //Note, must invoke super() first. 
 *         super();
 *         
 *         //Do something.
 *     }
 *     
 *     public void run() {
 *         //Note, must invoke super.run() first.
 *         super.run();
 *         
 *         //Do something.
 *     }
 * }
 * </pre>
 * 
 * @author Frederick.Wang 2007-1-31 10:45:27
 */
public class MultiCompanySupportedThread extends Thread
{
    protected ResourceBundle m_bundle = null;
    private String m_companyId;
    private Runnable m_runnable;  // saved for ActivityLog

    public MultiCompanySupportedThread(Runnable target)
    {
        this(null, target);
    }

    public MultiCompanySupportedThread(String name)
    {
        this(null, null, name, 0);
    }

    public MultiCompanySupportedThread(Runnable target, String name)
    {
        this(null, target, name, 0);
    }

    public MultiCompanySupportedThread(ThreadGroup group, String name)
    {
        this(group, null, name, 0);
    }

    public MultiCompanySupportedThread(ThreadGroup group, Runnable target,
            String name)
    {
        this(group, target, name, 0);
    }

    public MultiCompanySupportedThread()
    {
        this.m_companyId = CompanyThreadLocal.getInstance().getValue();
    }

    public MultiCompanySupportedThread(ThreadGroup group, Runnable target)
    {
        super(group, target);
        this.m_runnable = target;
        this.m_companyId = CompanyThreadLocal.getInstance().getValue();
    }

    public MultiCompanySupportedThread(ThreadGroup group, Runnable target,
            String name, long stackSize)
    {
        super(group, target, name, stackSize);
        this.m_runnable = target;
        this.m_companyId = CompanyThreadLocal.getInstance().getValue();
    }
    
    /**
     * Method for setting the resource bundle
     * 
     * @return
     */
    public void setResourceBundle(ResourceBundle p_bundle)
    {
        m_bundle = p_bundle;
    }
    
    /**
     * Method for getting the resource bundle
     */
    public ResourceBundle getResourceBundle()
    {
        return m_bundle;
    }
    
    /**
     * Get string from bundle, if bundle or p_key is null, return the defaultMsg
     * @param p_key
     * @param p_defaultMsg
     * @return the string from bundle, or the p_defaultMsg if p_key or {@link #getResourceBundle()} is null
     */
    public String getStringFromBundle(String p_key, String p_defaultMsg)
    {
        if (m_bundle == null || p_key == null)
        {
            return p_defaultMsg;
        }
        
        return (m_bundle.containsKey(p_key)) ? m_bundle.getString(p_key) : p_defaultMsg;
    }
    
    public String getStringFormattedFromBundle(String p_key, String p_defaultPattern, Object... arguments)
    {
        String pattern = getStringFromBundle(p_key, p_defaultPattern);
        String result = java.text.MessageFormat.format(pattern, arguments);
        return result;
    }

    public void run()
    {
        Map<Object,Object> activityArgs = new HashMap<Object,Object>();
        activityArgs.put("class", this.getClass().getName());
        activityArgs.put("runnableClass",
            m_runnable == null ? null : m_runnable.getClass().getName());
        activityArgs.put(CompanyWrapper.CURRENT_COMPANY_ID, this.m_companyId);
        ActivityLog.Start activityStart = ActivityLog.start(
            MultiCompanySupportedThread.class, "run", activityArgs);
        try
        {
            CompanyThreadLocal.getInstance().setIdValue(this.m_companyId);
            super.run();
        }
        finally
        {
            HibernateUtil.closeSession();
            activityStart.end();
        }
    }

}
