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
package com.globalsight.restful;

import java.nio.charset.Charset;
import java.security.Key;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.IvParameterSpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.globalsight.everest.company.Company;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.permission.PermissionSet;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.tm.importer.ImportUtil;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.StringUtil;

public class RestResource
{
    private static final Logger logger = Logger.getLogger(RestResource.class);

    public final static String XML_HEAD = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n";

    private static final String CIPHER = "DES/CBC/PKCS5Padding";

    // Hold "web service" session information. This can likely be replaced later
    @SuppressWarnings("rawtypes")
    private static Hashtable s_session = new Hashtable();

    /**
     * The separator used to separate parts of the access token
     */
    protected static final String ACCESS_TOKEN_SEPARATOR = "<separator>";

    /**
     * Check if current user has the specified permission
     * 
     * @param userName
     * @param permission
     *            Permission information
     * @throws WebServiceException
     */
    protected void checkPermission(String userName, String permission)
            throws RestWebServiceException
    {
        try
        {
            User user = ServerProxy.getUserManager().getUserByName(userName);
            PermissionSet ps = Permission.getPermissionManager()
                    .getPermissionSetForUser(user.getUserId());

            if (!ps.getPermissionFor(permission))
            {
                String msg = "User '" + userName + "' does not have permission '" + permission
                        + "' to execute current request.";
                throw new RestWebServiceException(msg);
            }
        }
        catch (Exception e)
        {
            logger.error(e);
            throw new RestWebServiceException(e.getMessage());
        }
    }

    /**
     * Check if user belongs to same company.
     * 
     * @param p_userName
     *            -- user name
     * @param p_companyName
     *            -- company name
     * 
     * @throws RestWebServiceException
     */
    protected void checkUserCompanyConsistency(String p_userName, String p_companyName)
            throws RestWebServiceException
    {
        String msg = "User " + p_userName + " does not belong to company " + p_companyName;
        try
        {
            boolean flag = false;
            User user = ServerProxy.getUserManager().getUserByName(p_userName);
            if (user != null)
            {
                flag = user.getCompanyName().equals(p_companyName);
            }

            if (!flag)
            {
                throw new RestWebServiceException(msg);
            }

            // required !!!
            CompanyThreadLocal.getInstance().setValue(user.getCompanyName());
        }
        catch (Exception e)
        {
            throw new RestWebServiceException(msg);
        }
    }

    /**
     * Gets locale with specified name.
     * 
     * @param name
     *            Locale name
     * @return GlobalSightLocale Locale information with the specified name
     * 
     * @throws RestWebServiceException
     */
    protected GlobalSightLocale getLocaleByName(String name) throws RestWebServiceException
    {
        if (StringUtil.isEmpty(name))
            return null;

        try
        {
            name = ImportUtil.normalizeLocale(name.trim());
            return ImportUtil.getLocaleByName(name);
        }
        catch (Exception e)
        {
            logger.warn("getLocaleByName() : Fail to get GlobalSightLocale by locale name: " + name);
            throw new RestWebServiceException("Unable to get locale by name: " + name);
        }
    }

    protected final static String authorizationHeader(String username, String password)
    {
        String auth = username + ":" + password;
        byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(Charset.forName("US-ASCII")));
        String authHeader = "Basic " + new String(encodedAuth);

        return authHeader;
    }

    protected String doLogin(String p_username, String p_password)
            throws RestWebServiceException
    {
        Map<Object, Object> activityArgs = new HashMap<Object, Object>();
        activityArgs.put("user", p_username);
        RestWebServiceLog.Start restStart = RestWebServiceLog.start(
                RestResource.class, "doLogin", activityArgs);

        String accessToken = null;
        try
        {
            boolean isValidLogin = allowAccess(p_username, p_password);
            if (isValidLogin)
            {
                try
                {
                    accessToken = generateAccessToken(p_username);
                }
                catch (Exception e)
                {
                    logger.error("Unable to create an Access Token due to the following exception"
                            + e);
                    String message = "Unable to create an encryption key for login.";
                    throw new RestWebServiceException(message);
                }
            }
            else
            {
                String message = "Unable to login user '" + p_username
                        + "' to GlobalSight.  The username or password may be incorrect.";
                throw new RestWebServiceException(message);
            }

            setUsernameInSession(accessToken, p_username);
        }
        catch (Exception e)
        {
            throw new RestWebServiceException(e.getMessage());
        }
        finally
        {
            if (restStart != null)
            {
                restStart.end();
            }
        }
        return accessToken;
    }

    private boolean allowAccess(String p_username, String p_password)
    {
        try
        {
            ServerProxy.getSecurityManager().authenticateUserByName(p_username, p_password);
            return true;
        }
        catch (Throwable e)
        {
            return false;
        }
    }

    /**
     * Generates an access token for the user.
     * 
     * @param p_userName -- user name
     * @return String -- access token
     * 
     * @exception Exception
     */
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

    /**
     * Sets the username in the session
     * 
     * @param p_accessToken
     * @param p_username
     */
    @SuppressWarnings("unchecked")
    private void setUsernameInSession(String p_accessToken, String p_username)
    {
        s_session.put(handleAccessToken(p_accessToken), p_username);
    }

    /**
     * Gets the username from the session
     * 
     * @param p_accessToken
     */
    protected static String getUserNameFromSession(String p_accessToken)
    {
        return (String) s_session.get(handleAccessToken(p_accessToken));
    }

    /**
     * Check whether the access token is okay.
     * 
     * @param p_accessToken
     *            -- access token
     * 
     * @exception WebServiceException
     */
    protected void checkAccess(String p_accessToken) throws RestWebServiceException
    {
        p_accessToken = handleAccessToken(p_accessToken);

        try
        {
            decryptToken(p_accessToken);

            this.accessCurrentCompanyId(p_accessToken);
        }
        catch (Exception e)
        {
            logger.info("Unable to decrypt the Access Token due to exception " + e.getMessage());
            String message = "The security information passed to the web service is not consistent.";
            throw new RestWebServiceException(message);
        }
    }

    private String accessCurrentCompanyId(String p_accessToken) throws RestWebServiceException
    {
        String userName = getUserNameFromSession(p_accessToken);
        String companyName = this.getUser(userName).getCompanyName();
        CompanyThreadLocal.getInstance().setValue(companyName);

        return CompanyThreadLocal.getInstance().getValue();
    }

    protected User getUser(String p_username) throws RestWebServiceException
    {
        try
        {
            return ServerProxy.getUserManager().getUserByName(p_username);
        }
        catch (Exception e)
        {
            String errMessage = "Failed to get the user associated with user name " + p_username;
            logger.error(errMessage, e);
            throw new RestWebServiceException(errMessage);
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

    @SuppressWarnings("unused")
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
     * Get file name from content-disposition header info. A sample info is
     * like:
     * "form-data; name="attachment"; filename="tm_100_InContext_Fuzzy.xml".
     * 
     * @param contentDisposition
     * @return
     */
    protected String getFileNameFromHeaderInfo(String contentDisposition)
    {
        if (StringUtil.isEmpty(contentDisposition))
            return null;

        String fileName = null;
        String[] strs = contentDisposition.split(";");
        for (String str : strs)
        {
            if (str != null && str.trim().startsWith("filename="))
            {
                str = str.trim();
                str = str.substring("filename=".length());
                if (str.startsWith("\""))
                {
                    str = str.substring(1);
                }
                if (str.endsWith("\""))
                {
                    str = str.substring(0, str.length() - 1);
                }
                fileName = str;
                break;
            }
        }

        return fileName;
    }

    protected Company getCompanyById(String p_companyID) throws RestWebServiceException
    {
        return getCompanyById(Long.parseLong(p_companyID));
    }

    protected Company getCompanyById(long p_companyID) throws RestWebServiceException
    {
        try
        {
            return ServerProxy.getJobHandler().getCompanyById(p_companyID);
        }
        catch (Exception e)
        {
            logger.warn(e);
            String msg = "Fail to get company by ID: " + p_companyID;
            throw new RestWebServiceException(msg);
        }
    }

    protected String makeErrorJson(String p_method, String p_msg)
    {
        JSONObject json = new JSONObject();
        try
        {
            json.put("method", p_method);
            json.put("message", p_msg);
        }
        catch (JSONException e)
        {
            logger.error(e);
        }
        return json.toString();
    }
}
