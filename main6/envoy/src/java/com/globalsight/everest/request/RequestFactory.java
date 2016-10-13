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

package com.globalsight.everest.request;

import org.apache.log4j.Logger;

// globalsight
import com.globalsight.everest.foundation.L10nProfile;
import com.globalsight.util.GeneralException;


/**
 * <p>This is the factory for creating concrete implementation classes of
 * Request.  This class uses the Factory Design Pattern.</p>
 *
 * <p>Creates the various types of requests that are valid.</p>
 */
public class RequestFactory
{
    private static Logger c_logger =
        Logger.getLogger(
            RequestFactory.class.getName());


    /**
     * <p>Creates a request according to the type specified.  If the
     * type is invalid then a null is returned.</p>
     */
    public static RequestImpl createRequest(int p_requestType,
        L10nProfile p_l10nProfile, String p_gxml,
        String p_eventFlowXml, GeneralException p_exception)
    {
        RequestImpl r = null;

        switch (p_requestType)
        {
            // both of these just set the type in the Request object
            case Request.EXTRACTED_LOCALIZATION_REQUEST:
            case Request.UNEXTRACTED_LOCALIZATION_REQUEST:
                r = new RequestImpl(p_l10nProfile, p_gxml, p_eventFlowXml,
                                    p_requestType);
                return r;
                // No break because of return
            case Request.REQUEST_WITH_CXE_ERROR:
                r = new RequestImpl(p_l10nProfile, p_gxml, p_eventFlowXml,
                    p_exception);
                return r;
                // No break because of return
            default:
                c_logger.error("Invalid request type: " + p_requestType);
                return null;
        }
    }
}
