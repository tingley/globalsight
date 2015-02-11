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


package com.globalsight.log;

import java.util.HashMap;

import org.apache.log4j.Category;
import org.apache.log4j.Logger;
import org.apache.log4j.LogManager;
import org.apache.log4j.spi.LoggerFactory;
import org.apache.log4j.Level;
import org.apache.log4j.helpers.LogLog;

import java.util.Enumeration;

/**
 * Extends Logger for some separation and control of log4j logging.
 */
public class GlobalSightCategory
    extends Logger
{
    //
    // PRIVATE CONSTANTS
    //
    static private final String FATAL = "FATAL";
    static private final String ERROR = "ERROR";
    static private final String WARN  = "WARN";
    static private final String INFO  = "INFO";
    static private final String DEBUG = "DEBUG";

    // It's enough to instantiate a factory once and for all.
    static private final GlobalSightCategoryFactory FACTORY =
        new GlobalSightCategoryFactory();

    // pass in LoggingEvent
    static private final String FCQN =
        GlobalSightCategory.class.getName();
    static private HashMap c_priorities;
    static private final String LINE_CONTINUATION = "\t~\n\t";

    static
    {
        // Ensure log4j is configured before using it.
        GlobalSightPropertyConfigurator.defaultConfigure();

        c_priorities = new HashMap(5);
        c_priorities.put(FATAL, Level.FATAL);
        c_priorities.put(ERROR, Level.ERROR);
        c_priorities.put(WARN,  Level.WARN);
        c_priorities.put(INFO,  Level.INFO);
        c_priorities.put(DEBUG, Level.DEBUG);
    }

    //
    // Logger Methods
    //

    /**
     * Shorthand for <code>getLogger(p_class.getName())</code>.
     *
     * @param p_class The name of <code>p_class</code> will be used as the
     * name of the category to retrieve.  See {@link
     * Logger#getLogger(String)} for more detailed information.
     */
    static public Logger getLogger(Class p_class)
    {
        return getLogger(p_class.getName(), FACTORY);
    }

    static public Category getInstance(String p_name)
    {
        throw new UnsupportedOperationException(
            "GlobalSightCategory.getInstance() should not be used! Replace with getLogger()");
    }

    static public Category getInstance(Class p_class)
    {
        throw new UnsupportedOperationException(
            "GlobalSightCategory.getInstance() should not be used! Replace with getLogger()");
    }

    /**
     * Retrieve a category wich is named like the <code>name</code>
     * parameter. If the named category already exists, then the
     * existing instance will be returned. Otherwise, a new instance
     * is created.
     *
     * <P>This method overrides {@link Logger#getLogger} by
     * supplying its own factory type as a parameter.
     *
     * @param p_name The name of the category to retrieve.
     */
    static public Logger getLogger(String p_name)
    {
        return getLogger(p_name, FACTORY);
    }

    /**
     * Like {@link Logger#getLogger(String)} except that the type
     * of category instantiated depends on the type returned by the
     * {@link LoggerFactory#makeNewLoggerInstance} method of the
     * <code>p_factory</code> parameter.
     *
     * <p>This method is intended to be used by sub-classes.
     *
     * @param p_name The name of the category to retrieve.
     * @param p_factory A {@link LoggerFactory} implementation that
     * will actually create a new Instance.
     */
    static public Logger getLogger(String p_name,
        LoggerFactory p_factory)
    {
        return Logger.getLogger(p_name, p_factory);

        // Code for writing out log in UTF-8 encoding
        //          Enumeration enum = instance.getAllAppenders();
        //          while(enum.hasMoreElements())
        //          {
        //              Appender appender = (Appender)enum.nextElement();
        //              if(appender instanceof FileAppender)
        //              {
        //                  FileAppender fileAppender = (Appender)appender;
        //                  String fileName = fileAppender.getFile();
        //                  OutputStream os = null;
        //                  if(fileName.equals("System.out"))
        //                  {
        //                      os = System.out;
        //                  }
        //                  else if(fileName.equals("System.err"))
        //                  {
        //                      os = System.err;
        //                  }
        //                  else
        //                  {
        //                      os = new FileOutputStream(fileName);
        //                  }

        //                  Writer writer = null;
        //                  try
        //                  {
        //                      writer = new OutputStreamWriter(os,"UTF8");
        //                  }
        //                  catch(UnsupportedEncodingException e)
        //                  {
        //                      // doesn't happen
        //                  }
        //                  fileAppender.setQWForFiles(writer);
        //              }
        //          }
    }


    /**
     * Set all existing GlobalSightCategories to the given priority level.
     * @param p_priorityLevel Level to set all GlobalSightCategories to.
     */
    static public void setPriorityAllCategories(Level p_priorityLevel)
    {
        if (p_priorityLevel == null)
        {
            throw new RuntimeException("GlobalSightCatagory: " +
                "setPriorityAllCategories p_priorityLevel null!");
        }

        Enumeration enumeration =
            LogManager.getCurrentLoggers();

        while (enumeration.hasMoreElements())
	    {
            try{
	            Logger cat = (Logger)enumeration.nextElement();
	            cat.setLevel(p_priorityLevel);
	        }catch(Exception e){
	            //ignore for cast failed
	            e.printStackTrace();
	        }
        }

        GlobalSightCategory.getRoot().setLevel(p_priorityLevel);

        // now re-read log4.properties and let it override priorities
        GlobalSightPropertyConfigurator.resetConfiguration();

        LogLog.debug("setPriorityAllCategories: " +
            p_priorityLevel.toString() + " toStringAllCatagories:" +
            LINE_CONTINUATION + toStringAllCategories());
    }


    /**
     * Set all existing GlobalSightCategories to the priority level with the
     * given name.
     * @param p_priorityName the name of the desired priority level.
     */
    static public void setPriorityAllCategories(String p_priorityName)
    {
        setPriorityAllCategories((Level)c_priorities.get(
            p_priorityName.toUpperCase().trim()));
    }

    /**
     * Return line continuation string to be used in logging.
     *
     * <p>This is not a static method so some category specific
     * behavior could be used.
     */
    public String getLineContinuation()
    {
        return LINE_CONTINUATION;
    }


    static public String toStringAllCategories()
    {
        StringBuffer result = new StringBuffer(400);
        Enumeration enumeration = LogManager.getCurrentLoggers();

        while (enumeration.hasMoreElements())
        {
            Object cat = enumeration.nextElement();

            if (cat == null)
            {
                continue;
            }

            result.append(cat.toString() + "\n");
        }

        return result.toString();
    }

    static public String getAllAssignedCategoryPriorities()
    {
        StringBuffer result = new StringBuffer(400);
        Enumeration enumeration = LogManager.getCurrentLoggers();

        while (enumeration.hasMoreElements())
        {
            GlobalSightCategory cat = (GlobalSightCategory)enumeration.nextElement();

            if (cat == null || cat.getName() == null)
            {
                continue;
            }

            result.append(cat.getName() + " " +
                (cat.getLevel() != null ?
                    cat.getLevel().toString() : "null Level") +
                "\n");
        }

        return result.toString();
    }


    static public String getAllChainedCategoryPriorities()
    {
        StringBuffer result = new StringBuffer(400);
        Enumeration enumeration = LogManager.getCurrentLoggers();

        while (enumeration.hasMoreElements())
        {
            GlobalSightCategory cat = (GlobalSightCategory)enumeration.nextElement();

            if (cat == null || cat.getName() == null)
            {
                continue;
            }

            result.append(cat.getName() + " " +
                (cat.getEffectiveLevel() != null ?
                    cat.getEffectiveLevel().toString() : "null Level") +
                "\n");
        }

        return result.toString();
    }


    /**
     * @returns a string representaion of the object.
     */
    public String toString()
    {
        return super.toString() +
            " name=" + getName() +
            " assigned priority=" + (getLevel() != null ?
                getLevel().toString() : "null") +
            " chained priority=" + (getEffectiveLevel() != null ?
                getEffectiveLevel().toString() : "null");
    }

    //
    // Protected Support Methods
    //

    /**
     * This constructor created a new <code>GlobalSightCategory</code>
     * instance and
     * sets its name.
     *
     * <p>It is intended to be used by sub-classes only. You should not
     * create categories directly.
     *
     * @param p_name The name of the category.
     */
    public GlobalSightCategory(String p_name)
    {
        super(p_name);
    }


    protected void forcedLog(String p_fqcn, Level p_priorityLevel,
        Object p_message, Throwable p_t)
    {
        super.forcedLog(FCQN, p_priorityLevel, p_message, p_t);
    }
}
