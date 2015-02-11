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
package com.globalsight.everest.foundation;

import java.util.Locale;

/**
 * User is an interface defining the data properties for a User.
 */
public interface User
{
    // user id used when a method needs to know what user requested
    // the action, but the user is the system.
    // i.e. cancelling a job can be done by a user or the system
    // when done by the system this user id is passed in
    public static final String SYSTEM_USER_ID = "1";

    // the anonymous vendor viewer user id
    // this is needed to check against with duplicate logins.
    // the anonymous users can have duplicate logins even if the
    // duplicate login attribute is set to false
    public static final String ANONYMOUS_VV_USER_ID = "anonymous_vendor_viewer";

    // constants for types of phone numbers the user can have
    public interface PhoneType
    {
        static final int OFFICE = 1;

        static final int HOME = 2;

        static final int CELL = 3;

        static final int FAX = 4;
    };

    // constants for states a user can be in
    public interface State
    {
        static final int CREATED = 1;

        static final int ACTIVE = 2;

        static final int DEACTIVE = 3;

        static final int DELETED = 4;
    };

    // Type of user - right now just using for anonymous user vs.
    // normal GlobalSight user
    // could store more info like 'VENDOR', 'EMPLOYEE', etc... later
    public interface UserType
    {
        static final int ANONYMOUS = 0;

        static final int GLOBALSIGHT = 1;
    };

    /** Gets the user's name for display in a locale sensitive manner. */
    public String getDisplayName(Locale p_locale);

    public String getUserId();

    public void setUserId(String p_userId);

    /*
     * Set and get the state of the user the user states are listed above in
     * constants.
     */
    public int getState();

    public void setState(int p_userState);

    /**
     * Returns 'true' if the user is in the ACTIVE state, 'false' otherwise.
     */
    public boolean isActive();

    public String getUserName();

    public void setUserName(String p_userName);

    public String getFirstName();

    public void setFirstName(String p_firstName);

    public String getLastName();

    public void setLastName(String p_lastName);

    public String getPassword();

    public void setPassword(String p_password);
    
    public String getWssePassword();

    public void setWssePassword(String p_wssePassword);

    public boolean isPasswordSet();

    public void setPasswordSet(boolean passwordSet);

    public String getEmail();

    public void setEmail(String p_email);

    public String getCCEmail();

    public void setCCEmail(String p_ccEmail);

    public String getBCCEmail();

    public void setBCCEmail(String p_bccEmail);

    public String getAddress();

    public void setAddress(String p_address);

//    /**
//     * the types of phone numbers are listed above in the CONSTANTS can only
//     * hold one of each type. Each set will overwrite a current phone number of
//     * the same type.
//     */
//    public String getPhoneNumber(int p_type);
//
//    public void setPhoneNumber(int p_type, String p_phoneNumber);

    public String getOfficePhoneNumber();

	public void setOfficePhoneNumber(String officePhoneNumber);

	public String getHomePhoneNumber();

	public void setHomePhoneNumber(String homePhoneNumber);

	public String getCellPhoneNumber();

	public void setCellPhoneNumber(String cellPhoneNumber);

	public String getFaxPhoneNumber();

	public void setFaxPhoneNumber(String faxPhoneNumber);
	
    public String getDefaultUILocale();

    public void setDefaultUILocale(String p_locale);

    public String getTitle();

    public void setTitle(String p_title);

    public String getCompanyName();

    public void setCompanyName(String p_companyName);

    /**
     * Sets the value to specify if the user should be added to all projects
     * (current and future) or not. 'true' means they should, 'false' means they
     * shouldn'
     * 
     * @param p_inAllProjects
     *            'true' the user should be part of all current and future
     *            projects. 'false' the user shouldn't be part of all future
     *            projects.
     */
    public void isInAllProjects(boolean p_inAllProjects);

    /**
     * Get the value to whether the user is part of all projects and should be
     * added to all new project that is created.
     * 
     * @return 'true' - the user is part of all current and future projects.
     *         'false - the user is not to be added to all future projects.
     */
    public boolean isInAllProjects();

    /**
     * Validates the User object: the UserId can not be empty.
     */
    public boolean isUserValid();

    /**
     * Return the type of user this is.
     */
    public int getType();

    /**
     * Set the type of user this is. Should only be used when reading from
     * storage (LDAP) and shouldn't be changed back and forth.
     */
    public void setType(int p_type);

    /*
     * Get the data with the format "username (firstname lastname)" for email
     */
    public String getSpecialNameForEmail();
    
    public String getProjectNames();
    public void setProjectNames(String projectN);
    public String getPermissiongNames();
    public void setPermissiongNames(String permisssingN);
}
