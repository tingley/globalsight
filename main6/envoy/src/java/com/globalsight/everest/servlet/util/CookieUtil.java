/**
 * 
 */
package com.globalsight.everest.servlet.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.everest.webapp.pagehandler.projects.workflows.JobSearchConstants;
import com.globalsight.webservices.AmbassadorUtil;

import jodd.util.StringBand;
import jodd.util.StringUtil;

/**
 * Some methods used to get/set cookie value
 * 
 * @author VincentYan 09/08/2016
 * @version 1.0
 * @since 8.7.1
 */
public class CookieUtil
{
    private static final int DEFAULT_EXPIRE_DAYS = 0;
    private static final boolean DEFAULT_HTTPONLY = true;
    private static final boolean DEFAULT_SECURE = false;

    private static Logger logger = Logger.getLogger(CookieUtil.class);

    /**
     * Get value from cookie If value contains XSS attacking strings, they will
     * be removed.
     * 
     * @param request
     *            HTTP request
     * @param name
     *            Cookie name
     * @return String Cookie value
     */
    public static String getCookieValue(HttpServletRequest request, String name)
    {
        return getCookieValue(request, name, false);
    }

    /**
     * Get value from cookie If noStrip is true, the original value will be
     * return. If false, return stripped value
     * 
     * @param request
     *            HTTP request
     * @param name
     *            Cookie name
     * @param noStrip
     *            Set true to return original value, false to strip invalid
     *            characters
     * @return String Cookie value
     */
    public static String getCookieValue(HttpServletRequest request, String name, boolean noStrip)
    {
        if (request == null || request.getCookies() == null || StringUtil.isBlank(name))
            return "";

        String value = "";
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies)
        {
            if (cookie == null)
                continue;
            if (cookie.getName().equals(name))
            {
                value = cookie.getValue();
                if (StringUtil.isBlank(value))
                    return "";
                value = value.trim();
                return noStrip ? value : ServletUtil.stripXss(ServletUtil.decodeUrl(value));
            }
        }
        return value;
    }

    /**
     * Get cookie object with special cookie name
     * 
     * @param request
     *            HTTP request
     * @param name
     *            Cookie name
     * @return Cookie Cookie object
     */
    public static Cookie getCookie(HttpServletRequest request, String name)
    {
        Cookie[] cookies = null;
        if (request == null || StringUtil.isBlank(name) || (cookies = request.getCookies()) == null)
            return null;
        for (Cookie cookie : cookies)
        {
            if (cookie == null)
                continue;
            if (cookie.getName().equals(name.trim()))
                return cookie;
        }
        return null;
    }

    /**
     * Set cookie to HTTP response
     * 
     * @param response
     *            HTTP response
     * @param name
     *            Cookie name
     * @param value
     *            Cookie value
     * @return boolean Return true if setting is correctly done
     */
    public static boolean setCookie(HttpServletResponse response, String name, String value)
    {
        return setCookie(response, name, value, DEFAULT_HTTPONLY, DEFAULT_SECURE,
                DEFAULT_EXPIRE_DAYS);
    }

    /**
     * Set cookie to HTTP response
     * 
     * @param response
     *            HTTP response
     * @param name
     *            Cookie name
     * @param value
     *            Cookie value
     * @param isHttpOnly
     *            HttpOnly attribute
     * @return boolean Return true if setting is correctly done
     */
    public static boolean setCookie(HttpServletResponse response, String name, String value,
            boolean isHttpOnly)
    {
        return setCookie(response, name, value, isHttpOnly, DEFAULT_SECURE, DEFAULT_EXPIRE_DAYS);
    }

    /**
     * Set cookie to HTTP response with setting secure attribute
     * 
     * @param response
     *            HTTP response
     * @param name
     *            Cookie name
     * @param value
     *            Cookie value
     * @param isHttpOnly
     *            HttpOnly attribute
     * @return boolean Return true if setting is correctly done
     */
    public static boolean setSecureCookie(HttpServletResponse response, String name, String value,
            boolean isHttpOnly)
    {
        return setCookie(response, name, value, isHttpOnly, true, DEFAULT_EXPIRE_DAYS);
    }

    /**
     * Set cookie to HTTP response with expire days
     * 
     * @param response
     *            HTTP response
     * @param name
     *            Cookie name
     * @param value
     *            Cookie value
     * @param expireDays
     *            Expire days
     * @return boolean Return true if setting is correctly done
     */
    public static boolean setExpireCookie(HttpServletResponse response, String name, String value,
            int expireDays)
    {
        int expireTime = 0;
        if (expireDays > 0)
            expireTime = 60 * 60 * 24 * expireDays;
        return setCookie(response, name, value, DEFAULT_HTTPONLY, DEFAULT_SECURE, expireTime);
    }

    /**
     * Set cookie to HTTP response with setting secure attribute
     * 
     * @param response
     *            HTTP response
     * @param name
     *            Cookie name
     * @param value
     *            Cookie value
     * @param isHttpOnly
     *            HttpOnly attribute
     * @param isSecure
     *            Secure attribute
     * @param expireTimes
     *            Expire times (unit is second)
     * @return boolean Return true if setting is correctly done
     */
    public static boolean setCookie(HttpServletResponse response, String name, String value,
            boolean isHttpOnly, boolean isSecure, int expireTimes)
    {
        if (response == null || StringUtil.isBlank(name))
            return false;

        value = StringUtil.isBlank(value) ? "" : value.trim();
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(isHttpOnly);
        cookie.setSecure(isSecure);
        if (expireTimes > 0)
            cookie.setMaxAge(expireTimes);
        response.addCookie(cookie);

        return true;
    }

    // ==========================================================================
    // Below methods are used in GS classes for now
    // ==========================================================================

    // Adds auto login cookie.
    public static void addAutoLoginCookie(String userId, String pass, HttpServletResponse response)
    {
        if (response == null || StringUtil.isBlank(userId))
            return;

        String cookieName = "autoLogin";
        String userName = UserUtil.getUserNameById(userId);
        try
        {
            pass = AmbassadorUtil.encryptionString(pass);
        }
        catch (Exception e)
        {
            logger.error("Cannot create encryption string correctly.", e);
        }
        String cookieValue = userName + "|" + pass;

        setExpireCookie(response, cookieName, cookieValue, 14);
    }

    /*
     * Get the user's most recently used jobs and put in the session so the jobs
     * menu can show them.
     * 
     * @param p_session the http session @param p_userId the user id @param
     * p_cookieName the name of the cookie @param p_sessionConstant the session
     * attribute name to store the info
     */
    public static void loadJobIds(HttpSession session, String userId, String cookieName,
            String sessionConstant, HttpServletRequest request)
    {
        if (request == null || session == null || StringUtil.isBlank(userId)
                || StringUtil.isBlank(cookieName))
            return;

        Cookie[] cookies = request.getCookies();
        if (cookies != null && cookies.length > 0)
        {
            cookieName = cookieName + userId.hashCode();
            for (Cookie cookie : cookies)
            {
                if (cookie == null)
                    continue;
                if (cookie.getName().equals(cookieName))
                {
                    session.setAttribute(sessionConstant,
                            ServletUtil.stripXss(ServletUtil.decodeUrl(cookie.getValue())));
                    return;
                }
            }
        }
    }

    /*
     * Update the session with this most recently used job. It will become the
     * first in the list and all the rest moved down. Also check that it wasn't
     * already in the list. Don't allow more than 3 items in the list.
     */
    public static void updateMRU(HttpServletRequest request, HttpServletResponse response,
            String value,
            String cookieName, String attributeName)
    {
        if (request == null || request.getCookies() == null || response == null
                || StringUtil.isBlank(cookieName) || StringUtil.isBlank(attributeName)
                || StringUtil.isBlank(value) || ":".equals(value.trim()))
            return;
        
        HttpSession session = request.getSession(false);
        
        int DEFAULT_ITEMS_COUNT = 3;

        String taskInfo = "";
        if (JobSearchConstants.MRU_TASKS.equals(attributeName))
        {
            int index = value.lastIndexOf(":");
            if (index < 1)
                return;
            taskInfo = value.substring(0, index);
        }

        // Get value from cookie
        String cookieValue = getCookieValue(request, cookieName);

        StringBand newValue = new StringBand(value);
        int len = 0;
        String[] oldValues = StringUtil.split(cookieValue, "|");
        if (oldValues != null && (len = oldValues.length) > 0)
        {
            for (int i = 0, count = 0; i < len && count < DEFAULT_ITEMS_COUNT - 1; i++)
            {
                if (StringUtil.isBlank(oldValues[i])
                        || (JobSearchConstants.MRU_TASKS.equals(attributeName)
                                && oldValues[i].startsWith(taskInfo))
                        || oldValues[i].equals(value))
                        continue;
                newValue.append("|").append(oldValues[i]);
                count++;
            }
        }
        cookieValue = newValue.toString();
        
        session.setAttribute(attributeName, cookieValue);
        // value = URLEncoder.encode(value);
        setCookie(response, cookieName, cookieValue);
        setCookie(response, "test", "testvalue");
    }

    /**
     * remove from MRU list
     */
    public static void removeMRU(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, String value, String cookieName, String attributeName)
    {
        if (request == null || request.getCookies() == null || response == null
                || StringUtil.isBlank(cookieName) || StringUtil.isBlank(attributeName)
                || StringUtil.isBlank(value) || ":".equals(value.trim()))
            return;

        String taskInfo = "";
        if (JobSearchConstants.MRU_TASKS.equals(attributeName))
        {
            int index = value.lastIndexOf(":");
            if (index < 1)
                return;
            taskInfo = value.substring(0, index);
        }

        String cookieValue = getCookieValue(request, cookieName);

        String[] values = StringUtil.split(cookieValue, "|");
        StringBand newValue = new StringBand();
        for (String string : values)
        {
            if (StringUtil.isBlank(string) || (JobSearchConstants.MRU_TASKS.equals(attributeName)
                    && string.startsWith(taskInfo)) || string.equals(value))
                continue;

            newValue.append(string).append("|");
        }
        cookieValue = newValue.toString();
        session.setAttribute(attributeName, cookieValue);
        setCookie(response, cookieName, cookieValue);
    }

}
