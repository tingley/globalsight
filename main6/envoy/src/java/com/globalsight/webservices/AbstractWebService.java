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

package com.globalsight.webservices;

import java.security.Key;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.IvParameterSpec;

import org.apache.log4j.Logger;

import com.globalsight.diplomat.util.database.ConnectionPool;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.permission.PermissionSet;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.webapp.pagehandler.login.LoginAttemptController;

/**
 * Abstract base class for web service classes to extend.
 */
public abstract class AbstractWebService
{
    private static final Logger s_logger = Logger.getLogger("WebService");

    private static final String CIPHER = "DES/CBC/PKCS5Padding";

    // holds "web service" session information. This can likely be replaced
    // later
    // with Axis session (which involves use of MessageContext, Handlers, etc.)
    private static Hashtable s_session = new Hashtable();

    /**
     * The separator used to separate parts of the access token
     */
    protected static final String ACCESS_TOKEN_SEPARATOR = "<separator>";

    // ///////////////////////////////////////
    // / Protected Methods for Subclasses ////
    // ///////////////////////////////////////

    /**
     * Login takes in a username and password, and returns an access token that
     * must then be used to make subsequent web service calls as the logged in
     * user.
     */
    protected String doLogin(String p_username, String p_password)
            throws WebServiceException
    {
        checkIfInstalled();

        Map<Object, Object> activityArgs = new HashMap<Object, Object>();
        activityArgs.put("userIP", LoginAttemptController.getIpAddressAxis());
        activityArgs.put("user", p_username);
        WebServicesLog.Start activityStart = WebServicesLog.start(
                AbstractWebService.class, "doLogin", activityArgs);

        String accessToken = null;
        try
        {

            boolean isValidLogin = allowAccess(p_username, p_password);
            // GBS-3991, invalid login if the IP is blocked
            if (LoginAttemptController.isIpBlocked())
            {
                isValidLogin = false;
            }
            if (isValidLogin)
            {
                try
                {
                    accessToken = generateAccessToken(p_username);
                }
                catch (Exception e)
                {
                    s_logger.error("Unable to create an Access Token due to the following exception"
                            + e);
                    String message = "Unable to create an encryption key for login";
                    message = makeErrorXml("login", message);
                    throw new WebServiceException(message);
                }
            }
            else
            {
                // GBS-3991, record failed login attempt
                LoginAttemptController.recordFailedLoginAttempt();
                String message = "Unable to login user "
                        + p_username
                        + " to GlobalSight.  The username or password may be incorrect.";
                message = makeErrorXml("login", message);
                throw new WebServiceException(message);
            }

            // set the username in the session
            setUsernameInSession(accessToken, p_username);
            // GBS-3991 clean up the failed login attempt associated with the IP
            // address while login successfully
            LoginAttemptController.cleanFailedLoginAttempt();
        }
        catch (Exception e)
        {
            throw new WebServiceException(e.getMessage());
        }
        finally
        {
            if (activityStart != null)
            {
                activityStart.end();
            }
        }
        return accessToken;
    }

    /**
     * Sets the username in the session
     * 
     * @param p_accessToken
     * @param p_username
     */
    protected void setUsernameInSession(String p_accessToken, String p_username)
    {
        s_session.put(handleAccessToken(p_accessToken), p_username);
    }

    /**
     * Gets the username from the session
     * 
     * @param p_accessToken
     */
    public static String getUsernameFromSession(String p_accessToken)
    {
        return (String) s_session.get(handleAccessToken(p_accessToken));
    }

    /**
     * Returns the connection back to the connection pool.
     * 
     * @param p_connection
     *            connection to return
     */
    protected void returnConnection(Connection p_connection)
    {
        try
        {
            ConnectionPool.returnConnection(p_connection);
        }
        catch (Exception cpe)
        {
        }
    }

    /**
     * Generates an access token for the user.
     * 
     * @param p_userName
     *            user name
     * @return String -- access token
     * @exception Exception
     */
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

    /**
     * Decrypts the access token. Throws an exception if it won't decrypt.
     * 
     * @param p_accessToken
     *            access token
     * @exception Exception
     */
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
        byte output[] = c.doFinal(encrypted);
    }

    /**
     * Checks if the web service is installed, and checks whether the access
     * token is ok.
     * 
     * @param p_accessToken
     *            access token
     * @param p_webMethodName
     *            webservice method that was called (used for error reporting)
     * @exception WebServiceException
     */
    protected void checkAccess(String p_accessToken, String p_webMethodName)
            throws WebServiceException
    {
        checkIfInstalled();
        p_accessToken = handleAccessToken(p_accessToken);

        try
        {
            decryptToken(p_accessToken);

            this.accessCurrentCompanyId(p_accessToken);
        }
        catch (Exception e)
        {
            s_logger.info("Unable to decrypt the Access Token due to exception "
                    + e.getMessage());
            String message = "The security information passed to the web service is not consistent.";
            message = makeResponseXml(p_webMethodName, false, message)
                    .toString();
            throw new WebServiceException(message);
        }
        if (s_logger.isDebugEnabled())
        {
            s_logger.debug("Invoking web service method \"" + p_webMethodName
                    + "\" for user " + getUsernameFromSession(p_accessToken));
        }
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

    /**
     * Returns true if the user can access the web service
     * 
     * @param p_username
     * @param p_password
     * @return
     */
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
        // If the "p_message" has been already wrapped, return it directly.
        if (p_message != null && p_message.indexOf("<errorXml>") > -1)
            return p_message;

        StringBuffer xml = new StringBuffer(
                "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\r\n");
        xml.append("<errorXml>\r\n");
        xml.append("\t<method>").append(p_method).append("</method>\r\n");
        xml.append("\t<error>").append(p_message).append("</error>\r\n");
        xml.append("</errorXml>\r\n");
        return xml.toString();
    }

    protected StringBuilder makeResponseXml(String p_method, boolean isOK)
    {
        return makeResponseXml(p_method, isOK, "");
    }

    protected StringBuilder makeResponseXml(String p_method, boolean isOK,
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

    /**
     * Gets the user associated with the specified user name (user id).
     */
    protected User getUser(String p_username) throws WebServiceException
    {
        try
        {
            return ServerProxy.getUserManager().getUserByName(p_username);
        }
        catch (Exception e)
        {
            String errMessage = "Failed to get the user associated with user name "
                    + p_username;
            s_logger.error(errMessage, e);
            throw new WebServiceException(errMessage);
        }
    }

    /**
     * Each web service should override this method. It throws an exception if
     * the web service is not installed, or that cannot be determined.
     */
    protected abstract void checkIfInstalled() throws WebServiceException;

    /**
     * Get the id of the company which this specific user belong to. And at the
     * same time, set current company id into CompanyThreadLocal.
     * 
     * @throws WebServiceException
     */
    protected String accessCurrentCompanyId(String p_accessToken)
            throws WebServiceException
    {
        String userName = getUsernameFromSession(p_accessToken);
        String companyName = this.getUser(userName).getCompanyName();
        CompanyThreadLocal.getInstance().setValue(companyName);

        return CompanyThreadLocal.getInstance().getValue();
    }

    /**
     * Check if current user has the specified permission
     * 
     * @param accessToken
     * @param permission
     *            Permission information
     * @throws WebServiceException
     */
    protected void checkPermission(String accessToken, String permission)
            throws WebServiceException
    {
        try
        {
            User user = ServerProxy.getUserManager().getUserByName(
                    getUsernameFromSession(accessToken));
            PermissionSet ps = Permission.getPermissionManager()
                    .getPermissionSetForUser(user.getUserId());

            if (!ps.getPermissionFor(permission))
            {
                String msg = "User " + user.getUserName()
                        + " does not have enough permission";
                throw new WebServiceException(msg);
            }
        }
        catch (Exception e)
        {
            s_logger.error(e.getMessage(), e);
            throw new WebServiceException(e.getMessage());
        }
    }

    /**
     * Check if current user has the specified permission
     * 
     * @param accessToken
     * @param permission
     *            Permission information
     * @throws WebServiceException
     */
    protected String checkPermissionReturnStr(String accessToken,
            String permission) throws WebServiceException
    {
        try
        {
            User user = ServerProxy.getUserManager().getUserByName(
                    getUsernameFromSession(accessToken));
            PermissionSet ps = Permission.getPermissionManager()
                    .getPermissionSetForUser(user.getUserId());

            if (!ps.getPermissionFor(permission))
            {
                String msg = "User " + user.getUserName()
                        + " does not have enough permission";
                return msg;
            }
        }
        catch (Exception e)
        {
            s_logger.error(e.getMessage(), e);
            throw new WebServiceException(e.getMessage());
        }
        return null;
    }
}
