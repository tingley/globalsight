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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.axis.MessageContext;
import org.apache.axis.transport.http.HTTPConstants;
import org.apache.log4j.Logger;

import com.globalsight.cxe.entity.systemActivity.LoginAttemptConfig;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.StringUtil;

/**
 * Used for checking and controlling failed login attempts from unexpected IP
 * addresses.
 */
public class LoginAttemptController
{
    private static final Logger logger = Logger
            .getLogger(LoginAttemptController.class);

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
        
        for (LoginAttempt attempt : getAllAttempts())
        {
            if (ip.equals(attempt.getIp()))
            {
                try
                {
                    HibernateUtil.delete(attempt);
                }
                catch (Exception e)
                {
                    logger.error(e);
                }
                return;
            }
        }
    }
    
    /**
     * Gets all attempts saved in database.
     */
    @SuppressWarnings("unchecked")
    private static List<LoginAttempt> getAllAttempts()
    {
        return (List<LoginAttempt>) HibernateUtil.search("from LoginAttempt");
    }
    
    /**
     * Gets login attempt with ip.
     */
    private static LoginAttempt getLoginAttempt(String ip)
    {
        return (LoginAttempt) HibernateUtil.getFirst("from LoginAttempt where ip = ?", ip);
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
        if (!isIpBlockingEnabled())
            return;
        
        if (ip == null || isExemptedIp(ip))
        {
            return;
        }
        LoginAttempt attempt = getLoginAttempt(ip);
        if (attempt == null)
        {
            attempt = new LoginAttempt(ip);
        }

        attempt.setCount(attempt.getCount() + 1);

        int loginAttempts = getFailedLoginAttemptAllowed();
        long blockTime = getBlockTime();
        if (attempt.getCount() == loginAttempts)
        {
            attempt.setBlockTime(new Date());
            logger.info("IP "
                    + ip
                    + " has reached failed login attempts allowed consecutively("
                    + loginAttempts
                    + "). Will be blocked for "
                    + (blockTime == 0 ? "an indefinite period." : blockTime
                            + " minutes."));

        }
        
        HibernateUtil.saveOrUpdate(attempt);
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
     * Gets all blocked IP.
     * @return
     */
    public static List<String> getBlockedIpList()
    {
        List<String> blockedIps = new ArrayList<String>();
        List<String> ips = new ArrayList<String>();
        for (LoginAttempt ip : getAllAttempts())
        {
            if (isIpBlocked(ip.getIp()))
            {
                blockedIps.add(ip.getIp());
            }
        }
        
        return blockedIps;
    }
    
    /**
     * Gets all exempt IP.
     * @return
     */
    public static List<String> getExemptIpList()
    {
        return getConfigFromDb().getExemptIpAsList();
    }

    /**
     * Checks if the IP address is being blocked or not.
     */
    public static boolean isIpBlocked(String ip)
    {
        if (!isIpBlockingEnabled())
            return false;
        
        if (ip == null || isExemptedIp(ip))
        {
            return false;
        }
        
        LoginAttempt attempt = getLoginAttempt(ip);
        if (attempt != null)
        {
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
                    try
                    {
                        HibernateUtil.delete(attempt);
                    }
                    catch (Exception e)
                    {
                        logger.error(e);
                    }
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
        return getConfigFromDb().getMaxTime();
    }

    /**
     * Gets the block time (minutes).
     */
    private static long getBlockTime()
    {
        return getConfigFromDb().getBlockTime();
    }

    /**
     * Gets the exempted IP list.
     */
    private static List<String> getExemptedIpList()
    {
        return getConfigFromDb().getExemptIpAsList();
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
        long blockTime = getBlockTime();
        if (blockTime == 0)
            return true;
        
        Date date = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(attempt.getBlockTime());
        c.add(Calendar.MINUTE, (int) blockTime);
        return date.before(c.getTime());
    }
    
    /**
     * Checks if Fail Login Attempts IP Blocking is enabled or not.
     */
    private static boolean isIpBlockingEnabled()
    {
        return getConfigFromDb().isEnable();
    }
    
    /**
     * Gets LoginAttemptConfig from database.
     */
    public static LoginAttemptConfig getConfigFromDb()
    {
        LoginAttemptConfig config = (LoginAttemptConfig) HibernateUtil.getFirst("from LoginAttemptConfig");
        if (config == null)
        {
            config = new LoginAttemptConfig();
            try
            {
                HibernateUtil.save(config);
            }
            catch (Exception e)
            {
                logger.error(e);
            }
        }
        
        return config;
    }
}
