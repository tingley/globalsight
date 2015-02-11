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

package com.globalsight.everest.webapp.pagehandler.administration.config.remoteip;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.apache.log4j.Logger;

import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.Assert;

/**
 * A manager who manage remote ip filter for webservice. The user can validate
 * ip address, gets all remote ip filters or check the the ip address has the
 * operate permission while a webservice request come.
 * 
 * <p>
 * The remote ip filter supports setting ip with wild card(*). The principle is
 * converting a ip address, which has wild card, to 255 no-wild-word ip
 * addresses. For example, there is a remote ip filter such that the ip is
 * setted to 192.168.1.*, then the ip will translated to 192.168.1.1,
 * 192.168.1.2, 192.168.1.3 ... 192.168.1.255. So an webservice request with
 * host ip 192.168.1.67(jsut for example) will be allowed.
 * 
 * <p>
 * The remote ip information stored in <tt>REMOTE_IP</tt> table, and the
 * persistent object is <tt>RemoteIp</tt>.
 */
public class RemoteIpManager
{
    private static final Logger s_logger = Logger
            .getLogger("RemoteIpManager");
    private static final String WILDCARD = "*";
    private static final int MAX_IP_NUMBER = 255;

    /**
     * Gets all remote ip filters stored in database. Will return an empty list
     * if no any remote ip filters.
     * 
     * @return A <tt>List</tt>, contains all remote ip filters stored in
     *         database.
     */
    @SuppressWarnings("unchecked")
    public static List<RemoteIp> getRemoteIps()
    {
        return (List<RemoteIp>) HibernateUtil.search("from RemoteIp");
    }

    /**
     * Checks whether the ip is exist in database.
     * 
     * <p>
     * The ip address will be repaired before checking. You can get more
     * information about how to repare ip address from ({@link RemoteIp#repairIp(String)}).
     * 
     * <p>
     * Note that only ip expression will be checked. For example, one ip address
     * (192.168.1.*) has been stored into database, and it seems that the ip
     * address (192.168.1.67) is included. But the return value will be
     * <tt>false</tt> if call <tt>isExist("192.168.1.67")</tt>.
     * 
     * <p>
     * An IllegalArgumentException will be throw if the ip is null or the length
     * of the ip is zero.
     * 
     * @param ip
     *            the ip address to be checked.
     * @return <tt>true</tt> if the ip address is exist in database.
     * @see RemoteIp#repairIp(String)
     */
    public static boolean isExist(String ip, Long id)
    {
        Assert.assertNotEmpty(ip, "IP address");

        String hql = "from RemoteIp r where r.ip = :ip";
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("ip", RemoteIp.repairIp(ip));
        if (id != null && id > 0)
        {
            hql += " and r.id != :id";
            map.put("id", id);
        }

        return HibernateUtil.search(hql, map).size() > 0;
    }

    /**
     * Gets all ip addresses that allowed to call webservice methods. All ip
     * address expressions with wild card will be converted to real ip
     * addresses.
     * 
     * <p>
     * The invalid ip addresses expression will be ignore.
     * 
     * @return A HashSet containing all ip addresses that allowed to call
     *         webservice methods.
     * 
     * @see #getRemoteIps()
     * @see #convert(String)
     * @see RemoteIp
     */
    private static HashSet<String> getRealIpAddresses()
    {
        HashSet<String> realIPAddresses = new HashSet<String>();
        for (RemoteIp ip : getRemoteIps())
        {
            try
            {
                realIPAddresses.addAll(convert(ip.getIp()));
            }
            catch (UnknownHostException e)
            {
                s_logger.error(e.getMessage(), e);
            }
        }

        return realIPAddresses;
    }

    /**
     * Checks whether the ip address is allowed to call webservice methods.
     * 
     * @param ip
     *            The ip address to be checked.
     * @return <tt>true</tt> if the ip address is allowed to call webservice
     *         methods.
     * @see #getRealIpAddresses()
     */
    public static boolean allowed(String ip)
    {
        return getRealIpAddresses().contains(ip);
    }

    /**
     * Converts one ip address expression. At first the ip address expression
     * will be repaired, then it will be converted to real ip addresses.
     * 
     * <p>
     * If the expression include wild card(*), it will be converted to 255 real
     * ip addressed by replacing wild card(*) to 1,2,3 ... 255.
     * 
     * <P>
     * It will be not changed if the expression not include wild card(*).
     * 
     * @param ip
     *            The ip address expression to be converted.
     * @return A HashSet&lt;String&gt; containing all real ip addresses.
     * @throws UnknownHostException
     *             if the ip address expression is invalid.
     * @throws IllegalArgumentException
     *             if the ip addres expression is <tt>null</tt>.
     * 
     * @see RemoteIp#repairIp(String)
     * @see InetAddress#getByName(String)
     */
    private static HashSet<String> convert(String ip)
            throws UnknownHostException
    {
        Assert.assertNotNull(ip, "IP address");

        ip = RemoteIp.repairIp(ip);

        HashSet<String> ips = new HashSet<String>();
        if (ip.indexOf(WILDCARD) >= 0)
        {
            String subIp = ip.substring(0, ip.indexOf(WILDCARD));

            for (int i = 1; i < MAX_IP_NUMBER; i++)
            {
                InetAddress ipAddr = InetAddress.getByName(subIp + i);
                ips.add(ipAddr.getHostAddress());
            }
        }
        else
        {
            InetAddress ipAddr = InetAddress.getByName(ip);
            ips.add(ipAddr.getHostAddress());
        }
        return ips;
    }

    /**
     * Return <tt>true</tt> if the ip address is valid.
     * 
     * @param ip
     *            the ip address to be checked.
     * @return <tt>true</tt> if the ip address is valid.
     * 
     * @see InetAddress#getByName(String)
     */
    public static boolean isValidIp(String ip)
    {
        if (ip == null)
            return false;
        
        if (ip.endsWith("."))
            return false;
        
        String[] parts = ip.split("\\.");
        if (parts.length != 4)
            return false;

        for (int i = 0; i < parts.length - 1; i++)
        {
            if (!validatePart(parts[i]))
                return false;
        }

        String endPart = parts[parts.length - 1];
        if (!"*".equals(endPart) && !validatePart(endPart))
            return false;

        return true;
    }

    private static boolean validatePart(String partString)
    {
        try
        {
            int part = Integer.parseInt(partString);
            if (part < 0 || part > 255)
            {
                return false;
            }
        }
        catch (NumberFormatException e)
        {
            return false;
        }

        return true;
    }
}
