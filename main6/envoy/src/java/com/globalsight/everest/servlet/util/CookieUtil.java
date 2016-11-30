/**
 * Copyright 2016 Welocalize, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.globalsight.everest.servlet.util;

import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.util.SecurityUtil;
import jodd.util.StringBand;
import jodd.util.StringUtil;
import org.apache.log4j.Logger;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

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
                return noStrip ? value : ServletUtil.stripXss(value);
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
        Cookie[] cookies;
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
            pass = SecurityUtil.AES(pass);
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

        String info = getInfo(attributeName, value);
        if (info == null)
            return;

        // Get value from cookie
        String cookieValue = getCookieValue(request, cookieName);

        StringBand newValue = new StringBand(value);
        int len;
        String[] oldValues = StringUtil.split(cookieValue, "|");
        if (oldValues != null && (len = oldValues.length) > 0)
        {
            for (int i = 0, count = 0; i < len && count < DEFAULT_ITEMS_COUNT - 1; i++)
            {
                if (StringUtil.isBlank(oldValues[i])
                        || oldValues[i].startsWith(info)
                        || oldValues[i].equals(value))
                        continue;
                newValue.append("|").append(oldValues[i]);
                count++;
            }
        }
        cookieValue = newValue.toString();
        
        session.setAttribute(attributeName, cookieValue);
        setCookie(response, cookieName, cookieValue);
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

        String info = getInfo(attributeName, value);
        if (info == null)
            return;

        String cookieValue = getCookieValue(request, cookieName);

        String[] values = StringUtil.split(cookieValue, "|");
        StringBand newValue = new StringBand();
        for (String string : values)
        {
            if (StringUtil.isBlank(string) || string.startsWith(info) || string.equals(value))
                continue;

            newValue.append(string).append("|");
        }
        cookieValue = newValue.toString();
        session.setAttribute(attributeName, cookieValue);
        setCookie(response, cookieName, cookieValue);
    }

    private static String getInfo(String attributeName, String value)
    {
        String info = null;
        if (StringUtil.isNotBlank(value))
        {
            int index = value.lastIndexOf(":");
            if (index > 0)
                info = value.substring(0, index);
        }
        return info;
    }

}
