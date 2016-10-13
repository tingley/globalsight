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
package com.globalsight.util;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.NTCredentials;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScheme;
import org.apache.commons.httpclient.auth.CredentialsNotAvailableException;
import org.apache.commons.httpclient.auth.CredentialsProvider;
import org.apache.commons.httpclient.auth.NTLMScheme;
import org.apache.commons.httpclient.auth.RFC2617Scheme;

/** This class handles prompting the user for username,password,domain
** when authenticating to a proxy or webserver. It can handle both
** NTLM authentication and basic authentication
*/
public class AuthenticationPrompter implements CredentialsProvider
{
    private boolean m_askForAuth = true;
    private String m_username = "";
    private String m_domain = "";
    private String m_password = "";


    /**
     * Creates an AuthenticationPrompter
     */
    public AuthenticationPrompter()
    {
        super();
        System.out.println("--- Creating AuthenticationPrompter");
    }

    /**
     * Returns the appropriate credentials for authentication. This method
     * may be called many times, but the username,password entered the first
     * time is re-used so the user is not prompted multiple times if the connection
     * isn't kept alive.
     * 
     * @param authscheme
     * @param host
     * @param port
     * @param proxy
     * @return Credentials
     * @exception CredentialsNotAvailableException
     */
    public Credentials getCredentials(
                                     final AuthScheme authscheme, 
                                     final String host, 
                                     int port, 
                                     boolean proxy)
    throws CredentialsNotAvailableException 
    {
        if (authscheme == null)
        {
            System.out.println("----No Proxy Authentication Required.");
            return null;
        }
        try
        {
            if (authscheme instanceof NTLMScheme)
            {
                System.out.println("----" + host + ":" + port + " requires Windows authentication");
                if (m_askForAuth)
                    askForAuthentication();
                return new NTCredentials(m_username, m_password, host, m_domain);    
            }
            else
                if (authscheme instanceof RFC2617Scheme)
            {
                System.out.println("----" + host + ":" + port + " requires authentication with the realm '" 
                                   + authscheme.getRealm() + "'");
                if (m_askForAuth)
                    askForAuthentication();
                return new UsernamePasswordCredentials(m_username, m_password);    
            }
            else
            {
                throw new CredentialsNotAvailableException("Unsupported authentication scheme: " +
                                                           authscheme.getSchemeName());
            }
        }
        catch (Exception e)
        {
            throw new CredentialsNotAvailableException(e.getMessage(), e);
        }
    }

    /**
     * Actually asks the user for the username,password,domain
     */
    private void askForAuthentication()
    {
        System.out.println("--- Asking for authentication");
        boolean requiresNTDomain = true;
        PasswordDialog p = new PasswordDialog(
                                             null,
                                             "Proxy Authentication",
                                             "The proxy is requesting you to authenticate to it.",
                                             requiresNTDomain);
        if (p.showDialog())
        {
            m_domain = p.getDomain();
            m_username = p.getUsername();
            m_password = p.getPassword();
            System.out.println("domain: " + m_domain);
            System.out.println("username: " + m_username);
        }
        else
        {
            System.out.println("User selected cancel!");
        }
        p.dispose();
    }

    /**
     * Sets whether this class should ask the user for authentication
     * 
     * @param p_ask
     */
    public void setAskForAuthentication(boolean p_ask)
    {
        m_askForAuth = p_ask;
    }
}

