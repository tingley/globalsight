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
package com.globalsight.everest.securitymgr;

import com.globalsight.util.GeneralException;
import com.globalsight.util.GeneralExceptionConstants;

/**
 * An exception handling object for user manager component.
 * <p>
 * @version     1.0
 */
public class SecurityManagerException extends GeneralException
        implements GeneralExceptionConstants
{

    ///////////////////////////////////////////////////////////////////
    ////////////  Component Specific Error Message keys  //////////////
    ///////////////////////////////////////////////////////////////////
    public final static String MSG_FAILED_TO_INIT_SERVER =
            "failedToInitServer";

    public final static String MSG_FAILED_TO_AUTHENTICATE =
            "failedToAuthenticate";

    public final static String MSG_FAILED_CONCURRENT_LOGIN =
            "failedConcurrentLogin";

    public final static String MSG_FAILED_TO_GET_PERMS_BY_USER =
            "failedToGetPermissionsByUser";

    public final static String MSG_FAILED_TO_GET_PERMS_BY_GRP =
            "failedToGetPermissionsByGroup";

    // -------------- exceptions for field security ---------------------


    // -------------- vendor field security --------------------------
    // 
    // Args: 1 - user id (user requesting action)
    //       2 - vendor id (vendor the field security is on)
    public final static String MSG_FAILED_VENDOR_PROJECTS_VERIFY = 
        "failedToVerifyVendorProjects";

    // Args: 1 - vendor id whose field security is being requested
    public final static String MSG_FAILED_TO_GET_VENDOR_FIELD_SECURITY =
        "failedToGetVendorFieldSecurity";

    // Args: 1 = vendor id whose field security is being saved
    public final static String MSG_FAILED_TO_SAVE_VENDOR_FIELD_SECURITY =
        "failedToSaveVendorFieldSecurity";

    // -------------- user field security ----------------------------

    // Args: 1 - user id (requesting action)
    //       2 - user id (field security is on)
    public final static String MSG_FAILED_USER_PROJECTS_VERIFY =
        "failedToVerifyUserProjects";

    // Args: 1 - user id whose field security is being requested
    public final static String MSG_FAILED_TO_GET_USER_FIELD_SECURITY =
        "failedToGetUserFieldSecurity";

    // Args: 1 = user id whose field security is being saved
    public final static String MSG_FAILED_TO_SAVE_USER_FIELD_SECURITY =
        "failedToSaveUserFieldSecurity";
    // Args: 1 = the object's toString result - description of object
    public final static String MSG_FAILED_TO_REMOVE_FIELD_SECURITY =
        "failedToRemoveFieldSecurity";


    /**
     * @see GeneralException#GeneralException(Exception)
     * This constructor is used when a subclass of GeneralException is wrapped.
     * In this case the wrapped exception already has the message related
     * information (unless a new message or arguments are needed).
     *
     * @param p_originalException Original exception that caused the error
     */
    public SecurityManagerException(Exception p_originalException)
    {
        super(p_originalException);
    }

    /**
     * @see GeneralException#GeneralException(String, String[], Exception, String)
     *
     * @param p_messageKey key in properties file
     * @param p_messageArguments Arguments to the message. It can be null.
     * @param p_originalException Original exception that caused the error. It can be null.
     */
    public SecurityManagerException(String p_messageKey,
                            String[] p_messageArguments,
                            Exception p_originalException)
    {
        super(p_messageKey, p_messageArguments, p_originalException);
    }
}
