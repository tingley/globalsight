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

package com.globalsight.everest.unittest.util;

import com.globalsight.everest.util.system.ServerObject;

public class ServerUtil
{
    /**
     * Initiate server instance for the specified class.
     * 
     * To ensure "ServerProxy.getXXXManger()" returns non-null, unit tests
     * should run this first.  You can use a @Before method to run it before
     * all of your tests.  Example:
     * <pre>
     * @Before
     * public void setUp() throws Exception
     * {
     *     ServerUtil.initServerInstance(TuvManagerWLImpl.class);
     * }
     * </pre>
     * You may have to use trial-and-error to figure out what server classes
     * need to be initialized.
     * 
     * @param serverClass
     *            -- from "server.classes" of "envoy_generated.properties",e.g.
     *            com.globalsight.everest.tuv.TuvManagerWLImpl.class
     */
    public static void initServerInstance(
            Class<? extends ServerObject> serverClass)
            throws InstantiationException, IllegalAccessException
    {
        serverClass.newInstance().init();
    }
}
