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
package com.globalsight.everest.util.server;

public interface ServerDefaults
{
    public final static String WEBLOGIC_CONTEXT =
        "weblogic.jndi.WLInitialContextFactory";

    public final static String PROVIDER_URL = "http://localhost:7001";

    public static final String PARAM_REMOTE_PROTOCOL = "remote.protocol";
    public static final String PARAM_REMOTE_PORT = "remote.port";
    public static final String PARAM_REMOTE_SSL_PORT = "remote.ssl.port";
    public static final String PARAM_REMOTE_HOST = "remote.host";
}

