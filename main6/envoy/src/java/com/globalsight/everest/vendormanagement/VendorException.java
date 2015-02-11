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
 
package com.globalsight.everest.vendormanagement;


// globalsight
import com.globalsight.util.GeneralException;


public class VendorException 
    extends GeneralException
{
    //message keys - for Exception
    
    public final static String MSG_NOT_INSTALLED = "VendorManagementNotInstalled";
    // Args: 1 = user id
    public final static String MSG_NOT_AUTHORIZED = 
        "FailedActionUserNotAuthorized";
    public final static String MSG_FAILED_TO_GET_USERMANAGER = 
        "FailedToFindUserManager";
    public final static String MSG_FAILED_TO_GET_ALL_VENDORS = 
        "FailedToGetAllVendors";
    // Args: 1 = vendor id
    public final static String MSG_FAILED_TO_GET_VENDOR = 
        "FailedToGetVendor";
    // Args: 1 = vendor custom id
    public final static String MSG_FAILED_TO_GET_VENDOR_BY_CUSTOM =
        "FailedToGetVendorByCustomId";
    //Args: 1 = user id
    public final static String MSG_FAILED_TO_GET_VENDOR_BY_USER_ID =
            "FailedToGetVendorByUserId";
    // Args: 1 = vendor id
    public final static String MSG_FAILED_TO_REMOVE_VENDOR = 
        "FailedToRemoveVendor";
    // Args: 1 = user id
    //       2 = custom vendor i
    public final static String MSG_FAILED_TO_DEACTIVATE_USER = 
        "FailedToDeactivateUser";
    // Args: 1 = user id
    //       2 = custom vendor id
    public final static String MSG_FAILED_TO_ACTIVATE_USER = 
        "FailedToActivateUser";
    // Args: 1 = user id
    //       2 = vendor custom id
    public final static String MSG_FAILED_TO_REMOVE_USER = 
        "FailedToRemoveUser";
    // Args: 1 = vendor full name (first and last name)
    public final static String MSG_FAILED_TO_ADD_VENDOR =
        "FailedToAddVendor";
    // Args: 1 - first name
    //       2 - last name
    public final static String MSG_FAILED_TO_ADD_VENDOR_SECURITY =
        "FailedToAddVendorSecurity";
    // Args: 1 - project name
    public final static String MSG_FAILED_TO_ADD_VENDORS_TO_PROJECT =
        "FailedToAddVendorsToProject";
    //Args: 1 - project name
    public final static String MSG_FAILED_TO_REMOVE_VENDORS_FROM_PROJECT =
        "FailedToRemoveVendorsFromProject";
    // Args: 1 = vendor id
    public final static String MSG_FAILED_TO_MODIFY_VENDOR = 
        "FailedToModifyVendor";
    // Args: 1 - vendor id
    public final static String MSG_FAILED_TO_MODIFY_VENDOR_SECURITY =
        "FailedToModifyVendorSecurity";
    public final static String MSG_FAILED_TO_GET_VENDOR_COMPANY_NAMES = 
        "FailedToGetVendorCompanyNames";
    public final static String MSG_FAILED_TO_GET_VENDOR_PSEUDONYMS =
        "FailedToGetVendorPseudonyms";
    public final static String MSG_FAILED_TO_GET_VENDOR_CUSTOM_IDS =
        "FailedToGetVendorCustomIds";
    public final static String MSG_FAILED_TO_GET_VENDOR_USER_IDS = 
        "FailedToGetVendorUserIds";
    // Args: 1 = user id
    public final static String MSG_FAILED_TO_MODIFY_VENDOR_WITH_USERINFO = 
        "FailedToModifyVendorWithUserInfo";
    // Args: 1 = user id
    //       2 = custom vendor id
    public final static String MSG_FAILED_TO_CREATE_USER =
        "FailedToAddUser";
    //Args: 1 = user id
    //      2 = custom vendor id
    public final static String MSG_FAILED_TO_MODIFY_USER = 
        "FailedToModifyUser";
    // Args: 1= user id
    //       2 = custom vendor id
    public final static String MSG_FAILED_TO_ASSOCIATE_USER = 
        "FailedToAssociateUser";
    // Args:1 = user id
    //
    public final static String MSG_FAILED_TO_DEASSOCIATE_USER_VENDOR =
        "FailedToDeassociateUserAndVendor";
    // Args: 1 = custom vendor id
    public final static String MSG_FAILED_TO_SAVE_RESUME =
        "FailedToSaveResumeFile";
    // Args: 1= custom vendor id
    public final static String MSG_REQUIRED_FIELDS_MISSING = 
        "RequiredFieldsAreMissing";
    // Args: 1 = id of user making the request to add, modify, remove, query a vendor
    //       2 = vendor custom id
    public final static String MSG_NO_PERMISSION_FOR_USER = 
        "UserDoesNotHavePermission";

    // -------------custom form exceptions -------------------------
    public final static String MSG_FAILED_TO_REMOVE_CUSTOM_FORM = 
        "FailedToRemoveCustomForm";
    public final static String MSG_FAILED_TO_RETRIEVE_CUSTOM_FORM = 
        "FailedToGetCustomForm";
    public final static String MSG_FAILED_TO_UPDATE_CUSTOM_FORM = 
        "FailedToUpdateCustomForm";

    // -------------Rating exceptions -------------------------
    public final static String MSG_FAILED_TO_ADD_RATING = 
        "FailedToAddRating";
    public final static String MSG_FAILED_TO_REMOVE_RATING =
        "FailedToRemoveRating";
    public final static String MSG_FAILED_TO_UPDATE_RATING =
        "FailedToUpdateRating";
    public final static String MSG_FAILED_TO_GET_RATING_BY_ID =
        "FailedToGetRatingById";
    public final static String MSG_FAILED_TO_GET_RATINGS_BY_TASK_IDS =
        "FailedToGetRatingsByTaskIds";

    // message file name
    private static final String PROPERTY_FILE_NAME = "VendorException";

    /**
     * @see GeneralException
     */
    public VendorException(String p_messageKey, String[] p_messageArguments, 
                           Exception p_originalException)
    {
        super(p_messageKey, p_messageArguments, p_originalException, PROPERTY_FILE_NAME);
    }
};
