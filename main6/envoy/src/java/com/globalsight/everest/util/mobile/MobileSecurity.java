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
package com.globalsight.everest.util.mobile;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.servlet.http.HttpServletRequest;

import java.security.Key;
import java.util.Date;
import java.util.Hashtable;

import javax.crypto.KeyGenerator;

import org.apache.log4j.Logger;

import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.servlet.util.ServerProxy;

public class MobileSecurity
{
    private static final Logger CATEGORY = Logger
            .getLogger(MobileSecurity.class);

    private static final String CIPHER = "DES/CBC/PKCS5Padding";
    private static final String ACCESS_TOKEN_SEPARATOR = "<separator>";

    @SuppressWarnings("rawtypes")
    private static Hashtable s_session = new Hashtable();

    // AccessToken : Time
    private static Hashtable<String, Long> time_session = new Hashtable<String, Long>();

    public String getIpAddr(HttpServletRequest request)
    {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip))
        {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip))
        {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip))
        {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    protected String doLogin(String p_username, String p_password)
            throws MobileServiceException
    {
        CATEGORY.debug("Logging in user " + p_username);
        boolean isValidLogin = allowAccess(p_username, p_password);
        String accessToken = null;
        if (isValidLogin)
        {
            try
            {
                accessToken = generateAccessToken(p_username);
                // If use browser url to test and there is "+" in token, it will
                // be replaced by white space, and it will result in problem, so
                // generate a token without "+" to ensure all is fine.
                while (accessToken.indexOf("+") > -1)
                {
                    accessToken = generateAccessToken(p_username);
                }
            }
            catch (Exception e)
            {
                String message = "Unable to create an encryption key for login";
                throw new MobileServiceException(message, e);
            }
        }
        else
        {
            String message = "Unable to login user "
                    + p_username
                    + " to GlobalSight. The username or password may be incorrect.";
            throw new MobileServiceException(message);
        }

        // set the user-name in the session
        setUsernameInSession(accessToken, p_username);
        // record the login time.
        updateAccessTime(accessToken);

        return accessToken;
    }

    protected boolean allowAccess(String p_username, String p_password)
    {
        try
        {
            ServerProxy.getSecurityManager().authenticateUserByName(p_username,
                    p_password);
            return true;
        }
        catch (Throwable e)
        {
            return false;
        }
    }

    protected String makeErrorXml(String p_method, String p_message)
    {
        StringBuffer xml = new StringBuffer(
                "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\r\n");
        xml.append("<errorXml>\r\n");
        xml.append("\t<method>").append(p_method).append("</method>\r\n");
        xml.append("\t<error>").append(p_message).append("</error>\r\n");
        xml.append("</errorXml>\r\n");
        return xml.toString();
    }

    @SuppressWarnings("unchecked")
    protected void setUsernameInSession(String p_accessToken, String p_username)
    {
        s_session.put(handleAccessToken(p_accessToken), p_username);
    }

    protected String getUsernameFromSession(String p_accessToken)
    {
        return (String) s_session.get(handleAccessToken(p_accessToken));
    }

    protected void removeUsernameFromSession(String p_accessToken)
    {
        if (p_accessToken == null) return;
        String token = handleAccessToken(p_accessToken);
        s_session.remove(token);

        if (token.indexOf(ACCESS_TOKEN_SEPARATOR) != -1)
        {
            String[] parts = token.split(ACCESS_TOKEN_SEPARATOR);
//          String userToken = parts[0];
            String userVector = parts[1];
            s_session.remove(userVector);            
        }

        time_session.remove(token);
    }

    protected boolean checkAccess(String p_accessToken)
    {
        p_accessToken = handleAccessToken(p_accessToken);

        try
        {
            decryptToken(p_accessToken);

            accessCurrentCompanyId(p_accessToken);
        }
        catch (Exception e)
        {
            CATEGORY.warn("Error when check access token : '" + p_accessToken
                    + "' :: " + e.getMessage());
            return false;
        }

        return true;
    }

    protected String accessCurrentCompanyId(String p_accessToken)
            throws MobileServiceException
    {
        String userName = getUsernameFromSession(p_accessToken);
        String companyName = getUser(userName).getCompanyName();
        CompanyThreadLocal.getInstance().setValue(companyName);

        return CompanyThreadLocal.getInstance().getValue();
    }

    protected User getUser(String p_username) throws MobileServiceException
    {
        try
        {
            return ServerProxy.getUserManager().getUserByName(p_username);
        }
        catch (Exception e)
        {
            String errMessage = "Failed to get the user associated with user name "
                    + p_username;
            CATEGORY.error(errMessage, e);
            throw new MobileServiceException(errMessage);
        }
    }

    protected StringBuilder makeResponseJson(String p_method, boolean isOK,
            String p_message)
    {
        StringBuilder xml = new StringBuilder(
                "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\r\n");
        xml.append("<status>").append((isOK ? "OK" : "Failed"))
                .append("</status>\r\n");
        if (!isOK)
        {
            xml.append("<error>").append(p_message).append("</error>\r\n");
        }

        return xml;
    }

    protected void updateAccessTime(String p_accessToken)
    {
        if (p_accessToken != null)
        {
            String token = handleAccessToken(p_accessToken);
            Date now = new Date();
            time_session.put(token, now.getTime());
        }
    }
    
    protected long getLastAccessTime(String p_accessToken)
    {
        long result = 0;
        if (p_accessToken != null)
        {
            String token = handleAccessToken(p_accessToken);
            result = time_session.get(token);
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    private String generateAccessToken(String p_userName) throws Exception
    {
        KeyGenerator kg = KeyGenerator.getInstance("DES");
        Cipher c = Cipher.getInstance(CIPHER);
        Key key = kg.generateKey();

        c.init(Cipher.ENCRYPT_MODE, key);
        byte input[] = p_userName.getBytes();
        byte encrypted[] = c.doFinal(input);
        byte iv[] = c.getIV();
        String userToken = new sun.misc.BASE64Encoder().encode(encrypted);
        String userVector = new sun.misc.BASE64Encoder().encode(iv);

        // combine the userToken and userVector together and return that
        // as the access token
        String accessToken = userToken + ACCESS_TOKEN_SEPARATOR + userVector;
        s_session.put(userVector, key);
        return accessToken;
    }

    private static String handleAccessToken(String accessToken)
    {
        String separator = "+_+";
        int index = accessToken.indexOf(separator);
        if (index != -1)
        {
            accessToken = accessToken.substring(0, index);
        }

        return accessToken;
    }

    private void decryptToken(String p_accessToken) throws Exception
    {
        Cipher c = Cipher.getInstance(CIPHER);
        String[] parts = p_accessToken.split(ACCESS_TOKEN_SEPARATOR);
        String userToken = parts[0];
        String userVector = parts[1];
        Key key = (Key) s_session.get(userVector);
        byte iv[] = new sun.misc.BASE64Decoder().decodeBuffer(userVector);
        IvParameterSpec dps = new IvParameterSpec(iv);
        c.init(Cipher.DECRYPT_MODE, key, dps);
        byte encrypted[] = new sun.misc.BASE64Decoder().decodeBuffer(userToken);
        c.doFinal(encrypted);
    }

}
