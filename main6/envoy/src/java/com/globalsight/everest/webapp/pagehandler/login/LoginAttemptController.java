/**
 * Copyright 2009 Welocalize, Inc.
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
 * 
 */
package com.globalsight.everest.webapp.pagehandler.login;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.axis.MessageContext;
import org.apache.axis.transport.http.HTTPConstants;
import org.apache.log4j.Logger;

import com.globalsight.util.StringUtil;

/**
 * Used for checking and controlling failed login attempts from unexpected IP
 * addresses.
 */
public class LoginAttemptController
{
    private static final Logger logger = Logger
            .getLogger(LoginAttemptController.class);

    public static Map<String, LoginAttempt> LOGIN_ATTEMPTS = new HashMap<String, LoginAttempt>();

    /**
     * Cleans up failed login attempt associated with the IP address while login
     * successfully (for web service call).
     */
    public static void cleanFailedLoginAttempt()
    {
        cleanFailedLoginAttempt(getIpAddressAxis());
    }

    /**
     * Cleans up failed login attempt associated with the IP address while login
     * successfully.
     */
    public static void cleanFailedLoginAttempt(HttpServletRequest p_request)
    {
        cleanFailedLoginAttempt(getIpAddress(p_request));
    }

    /**
     * Cleans up failed login attempt associated with the IP address while login
     * successfully.
     */
    public static void cleanFailedLoginAttempt(String ip)
    {
        if (ip == null || isExemptedIp(ip))
        {
            return;
        }
        LOGIN_ATTEMPTS.remove(ip);
    }

    /**
     * Records failed login attempt (for web service call).
     */
    public static void recordFailedLoginAttempt()
    {
        recordFailedLoginAttempt(getIpAddressAxis());
    }

    /**
     * Records failed login attempt.
     */
    public static void recordFailedLoginAttempt(HttpServletRequest p_request)
    {
        recordFailedLoginAttempt(getIpAddress(p_request));
    }

    /**
     * Records failed login attempt.
     */
    public static void recordFailedLoginAttempt(String ip)
    {
        if (ip == null || isExemptedIp(ip))
        {
            return;
        }
        LoginAttempt attempt = new LoginAttempt(ip);
        if (LOGIN_ATTEMPTS.get(ip) != null)
        {
            attempt = LOGIN_ATTEMPTS.get(ip);
        }
        attempt.setCount(attempt.getCount() + 1);
        LOGIN_ATTEMPTS.put(ip, attempt);

        int loginAttempts = getFailedLoginAttemptAllowed();
        long blockTime = getBlockTime();
        if (attempt.getCount() == loginAttempts)
        {
            attempt.setBlockTime(LocalDateTime.now());
            logger.info("IP "
                    + ip
                    + " has reached failed login attempts allowed consecutively("
                    + loginAttempts + "). Will be blocked for " + blockTime
                    + " minutes.");
        }
    }

    /**
     * Gets the client's IP address.
     */
    public static String getIpAddress(HttpServletRequest p_request)
    {
        String ip = p_request.getHeader("x-forwarded-for");
        if (StringUtil.isEmpty(ip) || "unknown".equalsIgnoreCase(ip))
        {
            ip = p_request.getHeader("Proxy-Client-IP");
        }
        if (StringUtil.isEmpty(ip) || "unknown".equalsIgnoreCase(ip))
        {
            ip = p_request.getHeader("WL-Proxy-Client-IP");
        }
        if (StringUtil.isEmpty(ip) || "unknown".equalsIgnoreCase(ip))
        {
            ip = p_request.getHeader("HTTP_CLIENT_IP");
        }
        if (StringUtil.isEmpty(ip) || "unknown".equalsIgnoreCase(ip))
        {
            ip = p_request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (StringUtil.isEmpty(ip) || "unknown".equalsIgnoreCase(ip))
        {
            ip = p_request.getRemoteAddr();
        }
        return ip;
    }

    /**
     * Gets the client's IP address from axis web service call.
     */
    public static String getIpAddressAxis()
    {
        String ip = null;
        MessageContext mc = null;
        HttpServletRequest request = null;

        try
        {
            mc = MessageContext.getCurrentContext();
            if (mc != null)
            {
                request = (HttpServletRequest) mc
                        .getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);
                if (request != null)
                {
                    ip = request.getRemoteAddr();
                }
            }
        }
        catch (Exception e)
        {
            logger.warn("Failed to get the client's IP address.");
        }

        return ip;
    }

    /**
     * Checks if the IP address is being blocked or not (for web service call).
     */
    public static boolean isIpBlocked()
    {
        return isIpBlocked(getIpAddressAxis());
    }

    /**
     * Checks if the IP address is being blocked or not.
     */
    public static boolean isIpBlocked(HttpServletRequest p_request)
    {
        return isIpBlocked(getIpAddress(p_request));
    }

    /**
     * Checks if the IP address is being blocked or not.
     */
    public static boolean isIpBlocked(String ip)
    {
        if (ip == null || isExemptedIp(ip))
        {
            return false;
        }
        if (LOGIN_ATTEMPTS.get(ip) != null)
        {
            LoginAttempt attempt = LOGIN_ATTEMPTS.get(ip);
            int loginAttempts = getFailedLoginAttemptAllowed();
            if (attempt.getCount() >= loginAttempts)
            {
                if (isInBlockTime(attempt))
                {
                    logger.info("IP " + ip + " is being blocked.");
                    return true;
                }
                else
                {
                    // time exceeds the block time, then remove this IP from
                    // blocked list
                    LOGIN_ATTEMPTS.remove(ip);
                }
            }
        }
        return false;
    }

    /**
     * Gets maximum failed login attempt allowed consecutively.
     */
    private static int getFailedLoginAttemptAllowed()
    {
        return 10;
    }

    /**
     * Gets the block time (minutes).
     */
    private static long getBlockTime()
    {
        return 60;
    }

    /**
     * Gets the exempted IP list.
     */
    private static List<String> getExemptedIpList()
    {
        return new ArrayList<String>();
    }

    /**
     * Checks if the IP address is in the exempted IP list or not.
     */
    private static boolean isExemptedIp(String ip)
    {
        List<String> exemptedIps = getExemptedIpList();
        return exemptedIps.contains(ip);
    }

    /**
     * Checks if the IP is in block time.
     */
    private static boolean isInBlockTime(LoginAttempt attempt)
    {
        return LocalDateTime.now().minusMinutes(getBlockTime())
                .isBefore(attempt.getBlockTime());
    }
}
