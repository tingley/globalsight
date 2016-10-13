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
package com.util.ldap;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

public class DirContextPool
{
    private static final Object LDAP_CONTEXT_FACTORY = "com.sun.jndi.ldap.LdapCtxFactory";

    private static final Object LDAP_PROVIDER_URL_PREFIX = "ldap://";

    private static final Object LDAP_CONNECTION_POOL = "com.sun.jndi.ldap.connect.pool";

    private static final Object LDAP_CONNECTION_TIMEOUT = "com.sun.jndi.ldap.connect.timeout";

    private static final Object LDAP_CONNECTION_MINSIZE = "com.sun.jndi.ldap.connect.pool.initsize";

    private static final Object LDAP_CONNECTION_MAXSIZE = "com.sun.jndi.ldap.connect.maxsize";

    private Hashtable env = null;

    // the default max connections in openldap is 64, we set a max connection
    // here to do the gc to release the unused connections to avoid connection
    // table full exception.
    private static final int MAX_CONNECTIONS = 20;

    private static int createdConns = 0;

    public DirContextPool(int minconNum, int maxconNum, String ldapHost,
            String ldapPort) throws NamingException
    {
        env = new Hashtable();
        env.put(Context.INITIAL_CONTEXT_FACTORY, LDAP_CONTEXT_FACTORY);
        env.put(Context.PROVIDER_URL, LDAP_PROVIDER_URL_PREFIX + ldapHost + ":"
                + ldapPort);
        env.put(LDAP_CONNECTION_POOL, "true");
        env.put(LDAP_CONNECTION_TIMEOUT, "1500");
        env.put(LDAP_CONNECTION_MAXSIZE, String.valueOf(maxconNum));
        env.put(LDAP_CONNECTION_MINSIZE, String.valueOf(minconNum));
    }

    /**
     * Creates the DirContext.
     * 
     * @return DirContext
     * @throws Exception
     */
    private DirContext createDirContext() throws NamingException
    {
        createdConns++;
        return new InitialDirContext(env);
    }

    /**
     * Adds the used DirContext in to <i>usedDirContext</i> list and get rid of
     * it from <i>freeDirContext</i> list.
     * 
     * @return DirContext
     * @throws Exception
     */
    public DirContext getDirContext() throws NamingException
    {
        return createDirContext();
    }

    public void closeDirContext(DirContext dct) throws NamingException
    {
        if (dct != null)
        {
            dct.close();
        }
        releaseConnections();
    }

    /**
     * Releases the unused connections using system gc.
     */
    private void releaseConnections()
    {
        if (createdConns >= MAX_CONNECTIONS)
        {
            createdConns = 0;
            // did not find an effective method to release the connections.
            // we use system gc for a walkaround.
            Runtime.getRuntime().gc();
            Runtime.getRuntime().runFinalization();
        }
    }
}
