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
package com.globalsight.everest.webapp.pagehandler.administration.shutdown;

// java
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

// Envoy packages
import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.permission.PermissionSet;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.LinkHelper;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.util.GeneralException;
import com.globalsight.util.j2ee.AppServerWrapperFactory;

/**
 * Handles setting up data for shutdown.jsp. Has the ability to shut down or
 * restart GlobalSight from the UI.
 */
public class ShutdownMainHandler extends PageHandler
{
    // Request Parameters
    public static final String PARAM_HOUR = "shutdownHourDelay";
    public static final String PARAM_MIN = "shutdownMinDelay";
    public static final String PARAM_CHOICE = "choice";
    public static final String PARAM_CHOICE_SHUTDOWN = "choiceShutdown";
    public static final String PARAM_CHOICE_CANCEL = "choiceCancel";
    public static final String PARAM_MSG = "shutdownMsg";
    public static final String PARAM_CHOICE_RESTART = "choiceRestart";
    public static final String PARAM_RESTART_HOUR = "restartHourDelay";
    public static final String PARAM_RESTART_MIN = "restartMinDelay";
    public static final String PARAM_RESTART_MSG = "restartMsg";
    public static final String PARAM_RESTART = "restart";

    // Request Attributes
    public static final String ATTR_SHUTDOWN_STATE = "shutdownState";
    public static final String ATTR_SHUTDOWN_TIME = "shutdownTime";
    public static final String ATTR_RESTART_STATE = "restartState";
    public static final String ATTR_RESTART_TIME = "restartTime";

    // logging
    private static final Logger s_logger = Logger.getLogger(ShutdownMainHandler.class.getName());

    // Values to keep track of whether GlobalSight is shutting down
    private static Boolean s_isShuttingDown = Boolean.FALSE;
    private static Date s_shutdownTime = null;
    private static Timer s_shutdownTimer = null;

    // Values to keep track of whether GlobalSight is restarting
    private static Boolean s_isRestarting = Boolean.FALSE;
    private static Date s_restartTime = null;
    private static Timer s_restartTimer = null;

    // shutdown/restart properties
    private static boolean s_restartUiEnabled = true;
    private static boolean s_restartBannerEnabled = true;
    private static String s_restartMessage = null;
    private static boolean s_shutdownUiEnabled = true;
    private static boolean s_shutdownBannerEnabled = true;
    private static String s_shutdownMessage = null;

    private static final long HOURS_IN_MILLIS = 60L * 60L * 1000L;
    private static final long MINUTES_IN_MILLIS = 60L * 1000L;

    // try to load the shutdown properties and bomb out if problems
    static
    {
        try
        {
            SystemConfiguration config = SystemConfiguration.getInstance();
            s_restartUiEnabled = config
                    .getBooleanParameter(SystemConfigParamNames.RESTART_UI_ENABLED);
            s_restartBannerEnabled = config
                    .getBooleanParameter(SystemConfigParamNames.SHUTDOWN_UI_BANNER_ENABLED);
            s_shutdownUiEnabled = config
                    .getBooleanParameter(SystemConfigParamNames.SHUTDOWN_UI_ENABLED);
            s_shutdownBannerEnabled = config
                    .getBooleanParameter(SystemConfigParamNames.SHUTDOWN_UI_BANNER_ENABLED);
            s_shutdownMessage = config.getStringParameter(SystemConfigParamNames.SHUTDOWN_UI_MSG);
        }
        catch (Exception e)
        {
            String msg = "Could not load shutdown UI properties.";
            s_logger.error(msg, e);
            throw new IllegalStateException(msg);
        }
    }

    /**
     * Returns true if GlobalSight has been requested to restart through the UI.
     * 
     * @return true/false
     */
    public static boolean isRestarting()
    {
        synchronized (s_isRestarting)
        {
            return s_isRestarting.booleanValue();
        }
    }

    /**
     * Returns true if GlobalSight has been requested to shut down through the
     * UI
     * 
     * @return true/false
     */
    public static boolean isShuttingDown()
    {
        synchronized (s_isShuttingDown)
        {
            return s_isShuttingDown.booleanValue();
        }
    }

    /**
     * Returns the restart time if GlobalSight has been requested to restart
     * through the UI.
     * 
     * @return Date
     */
    public static Date getRestartTime()
    {
        return s_restartTime;
    }

    /**
     * Returns the shutdown time if GlobalSight has been requested to shut down
     * through the UI
     * 
     * @return Date
     */
    public static Date getShutdownTime()
    {
        return s_shutdownTime;
    }

    /**
     * Returns whether the restart banner can be displayed.
     * 
     * @return boolean
     */
    public static boolean restartBannerEnabled()
    {
        return s_restartBannerEnabled;
    }

    /**
     * Returns whether the shutdown banner can be displayed
     * 
     * @return boolean
     */
    public static boolean shutdownBannerEnabled()
    {
        return s_shutdownBannerEnabled;
    }

    /**
     * Returns whether the shutdown user interface can be displayed
     * 
     * @return boolean
     */
    public static boolean shutdownUserInterfaceEnabled()
    {
        return s_shutdownUiEnabled;
    }

    /**
     * Returns whether the restart user interface can be displayed.
     */
    public static boolean restartUserInterfaceEnabled()
    {
        return s_restartUiEnabled;
    }

    /**
     * Returns the restart UI Message
     * 
     * @param p_format
     *            -- true if the message should be formatted, false if raw
     * @return String
     */
    public static String getRestartMessage(boolean p_format)
    {
        if (p_format == false)
            return s_restartMessage;
        else
        {
            // format the message with current hours and min to go
            long diff = s_restartTime.getTime() - System.currentTimeMillis();
            int diffInMinutes = (int) (diff / MINUTES_IN_MILLIS);
            int numMinutes = diffInMinutes % 60;
            int numHours = diffInMinutes / 60;
            Object[] args =
            { new Integer(numHours), new Integer(numMinutes) };
            return MessageFormat.format(s_restartMessage, args);
        }
    }

    /**
     * Returns the shutdown UI Message
     * 
     * @param p_format
     *            -- true if the message should be formatted, false if raw
     * @return String
     */
    public static String getShutdownMessage(boolean p_format)
    {
        if (p_format == false)
            return s_shutdownMessage;
        else
        {
            // format the message with current hours and min to go
            long diff = s_shutdownTime.getTime() - System.currentTimeMillis();
            int diffInMinutes = (int) (diff / MINUTES_IN_MILLIS);
            int numMinutes = diffInMinutes % 60;
            int numHours = diffInMinutes / 60;
            Object[] args =
            { new Integer(numHours), new Integer(numMinutes) };
            return MessageFormat.format(s_shutdownMessage, args);
        }
    }

    /**
     * Sets the shutdown UI Message
     */
    public static void setShutdownMessage(String p_newMessage)
    {
        s_shutdownMessage = p_newMessage;
    }

    /**
     * Invokes the ShutdownMainHandler. Expects the request parameter
     * 'shutdownAtHour=<24-hour>' and 'shutdownAtMin=<minutes>'to initiate a
     * shutdown request. Or it expects the parameter 'cancel' to cancel a
     * shutdown request. An existing shutdonw request can be changed simply by
     * making a new one.
     *
     * @param pageDescriptor
     *            the description of the page to be produced
     * @param request
     *            the original request sent from the browser
     * @param response
     *            original response object
     * @param context
     *            the Servlet context
     */
    public void invokePageHandler(WebPageDescriptor p_pageDescriptor, HttpServletRequest p_request,
            HttpServletResponse p_response, ServletContext p_context)
            throws ServletException, IOException, EnvoyServletException
    {
        HttpSession session = p_request.getSession(false);
        PermissionSet userPerms = (PermissionSet) session.getAttribute(WebAppConstants.PERMISSIONS);

        String queryString = p_request.getQueryString();
        StringBuilder restartString = new StringBuilder();
        restartString.append(LinkHelper.ACTIVITY_NAME);
        restartString.append(LinkHelper.HTTP_REQUEST_VALUE_EQUAL);
        restartString.append(PARAM_RESTART);
        if (restartString.toString().equals(queryString))
        {
            if (!userPerms.getPermissionFor(Permission.RESTART_SYSTEM))
            {
                p_response.sendRedirect(p_request.getContextPath());
                return;
            }
            processRestart(p_request);
        }
        else
        {
            // GBS-1389: restrict direct access to shutdown page without
            // shutdown permission
            if (!userPerms.getPermissionFor(Permission.SHUTDOWN_SYSTEM))
            {
                p_response.sendRedirect(p_request.getContextPath());
                return;
            }
            processShutdown(p_request);
        }
        super.invokePageHandler(p_pageDescriptor, p_request, p_response, p_context);
    }

    /**
     * Processes restart request.
     */
    private void processRestart(HttpServletRequest p_request)
    {
        synchronized (s_isRestarting)
        {
            try
            {
                String param = (String) p_request.getParameter(PARAM_CHOICE);
                if (PARAM_CHOICE_RESTART.equals(param))
                {
                    param = (String) p_request.getParameter(PARAM_RESTART_HOUR);
                    long hourDelayInMSecs = Long.parseLong(param) * HOURS_IN_MILLIS;
                    param = (String) p_request.getParameter(PARAM_RESTART_MIN);
                    long minuteDelayInMSecs = Long.parseLong(param) * MINUTES_IN_MILLIS;
                    long delay = System.currentTimeMillis() + hourDelayInMSecs + minuteDelayInMSecs;
                    s_restartTime = new Date(delay);
                    s_logger.info("Requesting GlobalSight restart at " + s_restartTime);
                    if (s_restartTimer != null)
                    {
                        s_restartTimer.cancel();
                        s_restartTimer = null;
                        s_isRestarting = Boolean.FALSE;
                    }
                    if (s_shutdownTimer != null)
                    {
                        s_shutdownTimer.cancel();
                        s_shutdownTime = null;
                        s_shutdownTimer = null;
                        s_isShuttingDown = Boolean.FALSE;
                    }

                    s_restartTimer = new Timer(false);
                    s_restartTimer.schedule(new Restarter(), s_restartTime);
                    s_isRestarting = Boolean.TRUE;
                    s_restartMessage = (String) p_request.getParameter(PARAM_RESTART_MSG);
                }
                else if (PARAM_CHOICE_CANCEL.equals(param))
                {
                    s_restartTimer.cancel();
                    s_logger.info("Cancelling restart request.");
                    s_restartTime = null;
                    s_restartTimer = null;
                    s_isRestarting = Boolean.FALSE;
                }

                p_request.setAttribute(ATTR_RESTART_STATE, s_isRestarting);
                p_request.setAttribute(ATTR_RESTART_TIME, s_restartTime);
            }
            catch (Exception e)
            {
                s_logger.error("Failed to process restart request.", e);
                throw new EnvoyServletException(GeneralException.EX_GENERAL, e);
            }
        }
    }

    /**
     * Processes shut down request.
     */
    private void processShutdown(HttpServletRequest p_request)
    {
        synchronized (s_isShuttingDown)
        {
            try
            {
                String param = (String) p_request.getParameter(PARAM_CHOICE);
                if (PARAM_CHOICE_SHUTDOWN.equals(param))
                {
                    // if we want to shutdown or change shutdown request
                    param = (String) p_request.getParameter(PARAM_HOUR);
                    long hourDelayInMSecs = Long.parseLong(param) * HOURS_IN_MILLIS;
                    param = (String) p_request.getParameter(PARAM_MIN);
                    long minuteDelayInMSecs = Long.parseLong(param) * MINUTES_IN_MILLIS;
                    long delay = System.currentTimeMillis() + hourDelayInMSecs + minuteDelayInMSecs;
                    s_shutdownTime = new Date(delay);
                    s_logger.info("Requesting GlobalSight shutdown at " + s_shutdownTime);
                    if (s_shutdownTimer != null)
                    {
                        s_shutdownTimer.cancel();
                        s_shutdownTimer = null;
                        s_isShuttingDown = Boolean.FALSE;
                    }
                    if (s_restartTimer != null)
                    {
                        s_restartTimer.cancel();
                        s_restartTime = null;
                        s_restartTimer = null;
                        s_isRestarting = Boolean.FALSE;
                    }

                    s_shutdownTimer = new Timer(false);
                    s_shutdownTimer.schedule(new AmbassadorStopper(), s_shutdownTime);
                    s_isShuttingDown = Boolean.TRUE;
                    s_shutdownMessage = (String) p_request.getParameter(PARAM_MSG);
                }
                else if (PARAM_CHOICE_CANCEL.equals(param))
                {
                    // check if we want to cancel
                    s_shutdownTimer.cancel();
                    s_logger.info("Cancelling shutdown request.");
                    s_shutdownTime = null;
                    s_shutdownTimer = null;
                    s_isShuttingDown = Boolean.FALSE;
                }

                p_request.setAttribute(ATTR_SHUTDOWN_STATE, s_isShuttingDown);
                p_request.setAttribute(ATTR_SHUTDOWN_TIME, s_shutdownTime);
            }
            catch (Exception e)
            {
                s_logger.error("Failed to process shutdown request parameters.", e);
                throw new EnvoyServletException(GeneralException.EX_GENERAL, e);
            }
        }
    }

    private class Restarter extends TimerTask
    {
        public void run()
        {
            s_logger.info("Restarting GlobalSight now.");
            AppServerWrapperFactory.getAppServerWrapper().restart();
        }
    }

    /**
     ** This class is a timer task used to shutdown the application server or
     * undeploy the GlobalSight application (as appropriate)
     **/
    private class AmbassadorStopper extends TimerTask
    {
        public void run()
        {
            // now shut down the application server
            s_logger.info("Shutting down GlobalSight now.");
            AppServerWrapperFactory.getAppServerWrapper().shutdown();
        }
    }
}
