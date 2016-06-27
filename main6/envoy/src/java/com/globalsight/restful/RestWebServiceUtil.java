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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.jboss.resteasy.util.Base64;

public class RestWebServiceUtil
{
    private static final Logger logger = Logger.getLogger(RestWebServiceUtil.class);

    /**
     * Get a random string
     * 
     * @return String
     */
    public static synchronized String getRandomFeed()
    {
        try
        {
            Thread.sleep(1000);
        }
        catch (InterruptedException e)
        {
        }

        String randomStr = String.valueOf((new Random()).nextInt(999999999));
        while (randomStr.length() < 9)
        {
            randomStr = "1" + randomStr;
        }
        return randomStr;
    }

    public static String getUserNameFromBasicAuthorizationHeader(List<String> authorization)
            throws RestWebServiceException
    {
        return getUserNamePwdFromRequest(authorization).get(0);
    }

    /**
     * Get the userName and password from http header "Authorization".
     * 
     * @param authorization
     *            List<String> authorization from header
     * @return List<String> which contains "userName" and "password".
     * 
     * @throws RestWebServiceException
     */
    private static List<String> getUserNamePwdFromRequest(List<String> authorization)
            throws RestWebServiceException
    {
        List<String> userNameAndPwd = new ArrayList<String>();

        // If no authorization information present; block access
        if (authorization == null || authorization.isEmpty())
        {
            throw new RestWebServiceException("Empty authorization");
        }

        try
        {
            // Get encoded username and password
            final String encodedUserPassword = authorization.get(0).replaceFirst("Basic ", "");

            // Decode username and password
            String usernameAndPassword = new String(Base64.decode(encodedUserPassword));

            // Split username and password tokens
            final StringTokenizer tokenizer = new StringTokenizer(usernameAndPassword, ":");
            final String username = tokenizer.nextToken();
            final String password = tokenizer.nextToken();

            userNameAndPwd.add(username);
            userNameAndPwd.add(password);

            return userNameAndPwd;
        }
        catch (Exception e)
        {
            logger.warn(e);
            throw new RestWebServiceException(
                    "Fail to get userName/password from authorization header", e);
        }
    }

    public static void writeFile(File file, byte[] p_bytes) throws RestWebServiceException
    {
        file.getParentFile().mkdirs();

        FileOutputStream fos = null;
        try
        {
            // append "true"
            fos = new FileOutputStream(file, true);
            fos.write(p_bytes);
        }
        catch (Exception e)
        {
            logger.error(e);
            throw new RestWebServiceException(e.getMessage());
        }
        finally
        {
            try
            {
                fos.close();
            }
            catch (IOException e)
            {
                String msg = "Fail to close FileOutPutSteam";
                logger.error(msg, e);
                throw new RestWebServiceException(msg + ": " + e.getMessage());
            }
        }
    }
}
