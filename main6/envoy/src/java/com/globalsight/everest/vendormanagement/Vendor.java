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

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.globalsight.everest.costing.Rate;
import com.globalsight.everest.customform.CustomField;
import com.globalsight.everest.foundation.LocalePair;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.persistence.PersistentObject;
import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.workflow.Activity;

/**
 * This class represents a Vendor - an individual who performs work. They are
 * not a company but rather an individual associated with one company. They
 * perform localization tasks (translate, review, edit) for a fee.
 */
public class Vendor extends PersistentObject
{
    private static final long serialVersionUID = -7187773289085126639L;

    // used by TOPLink
    public final static String M_ID = "m_id";
    public final static String M_CUSTOM_ID = "m_customVendorId";
    public final static String M_PROJECTS = "m_projects";
    public final static String M_ROLES = "m_roles";
    public final static String M_USER_ID = "m_userId";
    public final static String M_COMPANY = "m_companyName";
    public final static String M_VENDOR_ROLES = "m_roles";
    public final static String M_CUSTOM_FILEDS = "m_customFields";
    public final static String M_FIRST_NAME = "m_firstName";
    public final static String M_LAST_NAME = "m_lastName";
    public final static String M_PSEUDONYM = "m_pseudonym";
    public final static String M_STATUS = "m_status";

    // the valid statuses that a vendor can have
    public final static String PENDING_STATUS = "PENDING";
    public final static String APPROVED_STATUS = "APPROVED";
    public final static String REJECTED_STATUS = "REJECTED";
    public final static String ON_HOLD_STATUS = "ON-HOLD";

    public final static String DEFAULT_UILOCALE = "en_US";

    // private data members
    private String m_status = PENDING_STATUS;
    private String m_customVendorId; // a unique id that the customer can
    // assign
    // to the vendor.
    private String m_pseudonym = null;
    private String m_firstName = null;
    private String m_lastName = null;
    private String m_title = null;
    private String m_companyName = null;
    private String m_address = null;
    private String m_country = null;
    // a String that holds one or two nationalities that a vendor may have
    // it is comma-delimited
    private String m_nationalities = null;
    // A collection of CommunicationInfo objects
    // These store the various communiations (email, phone) that
    // the vendor has. The key is the CommunicationInfo.
    // CommunicationType.
    private Hashtable m_commInfo = new Hashtable();
    private String m_dob = null; // the date of birth of the vendor
    private boolean m_isInternalVendor = false;
    private String m_notes;

    // The resume file name is set if a file was uploaded
    // and the content is saved in the m_resumeContentInBytes
    // till it has been copied. Otherwise the m_resume
    // attribute is used to store the exact text.
    // They can all be NULL if no resume is specified.
    private String m_resumeFilename = null;
    private byte[] m_resumeContentInBytes = null;
    private String m_resume = null;
    private String m_communicationLocale = DEFAULT_UILOCALE;
    // specifies whether the user is part of all current AND
    // future projects
    private boolean m_isInAllProjects = false;
    // the projects that the vendor is associated with.
    private List m_projects = new ArrayList();
    // The associated ratings
    private List m_ratings = new ArrayList();
    // the roles the vendor can perform.
    // stored in a Set because there shouldn't be any duplicates
    // this is used when adding to the role list
    private Set m_roles = new HashSet();
    // the custom fields that the vendor may have had added to them.
    // the key of the Hashtable is the "getKey()" from CustomField
    private Hashtable m_customFields = new Hashtable();

    // -------User information-------------------

    private boolean m_useInAmbassador = true;
    private User m_user = null; // if the vendor is a user in GlobalSight
    // this is the user they are
    // The m_userid and m_password aren't persisted.
    // They are held in memory until the m_user object is created and
    // the relationship persisted.
    private String m_userId = null;
    private String m_password = null;

    /**
     * Default constructor
     */
    public Vendor()
    {
    }

    public void addCommunicationInfo(int p_type, String p_value)
    {
        if (p_type == CommunicationInfo.CommunicationType.EMAIL)
        {
            setEmail(p_value);
        }
        else
        {
            setPhoneNumber(p_type, p_value);
        }
    }

    /**
     * Add or update a custom field in the list. It can only be updated if the
     * key hasn't changed.
     */
    public void addOrUpdateCustomField(String p_key, String p_value)
    {
        CustomField cf = (CustomField) m_customFields.get(p_key);
        if (cf != null)
        {
            // if set to NULL or empty string then remove
            if (p_value == null || p_value.length() == 0)
            {
                m_customFields.remove(p_key);
            }
            else
            {
                cf.setValue(p_value);
                m_customFields.put(p_key, cf);
            }
        }
        else
        {
            // only add if the value is worth while
            if (p_value != null && p_value.length() > 0)
            {
                cf = new CustomField(p_key, p_value);
                m_customFields.put(p_key, cf);
            }
        }
    }

    /**
     * Associate a rating to this vendor by adding it to the list of existing
     * ratings.
     */
    void addRating(Rating p_rating)
    {
        p_rating.setVendor(this);
        m_ratings.add(p_rating);
    }

    /**
     * Create a vendor role with the activity and locale pair specified and add
     * to the vendor.
     */
    public void addRole(Activity p_act, LocalePair p_lp)
    {
        if (p_act != null && p_lp != null)
        {
            VendorRole vr = new VendorRole(p_act, p_lp);
            addRole(vr);
        }
    }

    /**
     * Create a vendor role with the activity, locale pair and rate specified
     * and add to the vendor.
     */
    public void addRole(Activity p_act, LocalePair p_lp, Rate p_rate)
    {
        // check if NULL, the rate can be null if removing it from the role.
        if (p_act != null && p_lp != null)
        {
            VendorRole vr = new VendorRole(p_act, p_lp, p_rate);
            addRole(vr);
        }
    }

    /**
     * Add the role to the user.
     */
    public void addRole(VendorRole p_role)
    {
        if (p_role != null)
        {
            p_role.setVendor(this);
            m_roles.add(p_role);
        }
    }

    /**
     * Add the vendor to a new project.
     */
    public void addToProject(Project p_project)
    {
        if (p_project != null)
        {
            m_projects.add(p_project);
        }
    }

    /**
     * Append more notes to the notes that are associated with this vendor.
     */
    public void appendNotes(String p_notes)
    {
        if (m_notes == null)
        {
            m_notes = p_notes;
        }
        else
        {
            m_notes += p_notes;
        }
    }

    /**
     * Checks if the vendor passed and this vendor are equal.
     */
    public boolean equals(Object p_obj)
    {
        Vendor v = (Vendor) p_obj;
        if (v.getCustomVendorId().equals(this.getCustomVendorId()))
        {
            return true;
        }

        return false;
    }

    public String getAddress()
    {
        return m_address;
    }

    /**
     * Gets the average of all the ratinngs
     */
    public double getAverageRating()
    {
        if (m_ratings.size() == 0)
        {
            return 0;
        }

        Iterator iter = m_ratings.iterator();
        float total = 0;
        while (iter.hasNext())
        {
            Rating rating = (Rating) iter.next();
            int value = rating.getValue();
            total += value;
        }
        float average = total / m_ratings.size();

        return Math.round(average * 100) / 100d;
    }

    public Set getCommInfo()
    {
        Set commInfo = new HashSet();
        if (m_commInfo == null)
        {
            m_commInfo = new Hashtable();
        }
        commInfo.addAll(m_commInfo.values());
        return commInfo;
    }

    public String getCommunicationLocale()
    {
        return m_communicationLocale;
    }

    /**
     * Return the value of the specific communication.
     * 
     * @param p_type
     *            The type of communication. See
     *            CommunicationInfo.CommunicationType for the valid types.
     */
    private String getCommunicationValue(int p_type)
    {
        String value = null;
        CommunicationInfo ci = (CommunicationInfo) m_commInfo.get(new Integer(
                p_type));
        if (ci != null)
        {
            value = ci.getValue();
        }
        return value;
    }

    public String getCompanyName()
    {
        return m_companyName;
    }

    public String getCountry()
    {
        return m_country;
    }

    /**
     * Return the custom fields.
     */
    public Hashtable getCustomFields()
    {
        return m_customFields;
    }

    public Set getCustomFieldsAsSet()
    {
        Set customFields = new HashSet();
        customFields.addAll(m_customFields.values());
        return customFields;
    }

    /**
     * Get the unique identifier for the vendor that the customer can defined.
     * If they don't define one then it is generated automatically.
     */
    public String getCustomVendorId()
    {
        return m_customVendorId;
    }

    public String getDateOfBirth()
    {
        return m_dob;
    }

    /**
     * Return the locale used for ui and communication (email)
     */
    public String getDefaultUILocale()
    {
        return (m_communicationLocale == null || m_communicationLocale.length() == 0) ? DEFAULT_UILOCALE
                : m_communicationLocale;
    }

    public String getDob()
    {
        return m_dob;
    }

    /**
     * Returns the vendor's email address or NULL if one isn't specified.
     */
    public String getEmail()
    {
        return getCommunicationValue(CommunicationInfo.CommunicationType.EMAIL);
    }

    public String getFirstName()
    {
        return m_firstName;
    }

    /**
     * Get the full name of the vendor (First name+ Last name)
     */
    public String getFullName()
    {
        StringBuffer sb = new StringBuffer();

        if (m_firstName != null)
        {
            sb.append(m_firstName);
            sb.append(" ");
        }

        if (m_lastName != null)
        {
            sb.append(m_lastName);
        }

        return sb.toString();
    }

    public boolean getIsInAllProjects()
    {
        return m_isInAllProjects;
    }

    public boolean getIsInternalVendor()
    {
        return m_isInternalVendor;
    }

    public String getLastName()
    {
        return m_lastName;
    }

    /**
     * Return NULL, one or two nationalities that a vendor is associated with.
     * If two nationalities then it is comma-delimited
     */
    public String getNationalities()
    {
        return m_nationalities;
    }

    /**
     * Get any notes that are associated with the vendor and their abilities.
     */
    public String getNotes()
    {
        if (m_notes == null)
        {
            m_notes = new String();
        }
        return m_notes;
    }

    public String getPassword()
    {
        return m_password;
    }

    /**
     * Returns the vendor's phone number for the particular type or NULL if one
     * wasn't specified.
     */
    public String getPhoneNumber(int p_type)
    {
        return getCommunicationValue(p_type);
    }

    /**
     * Return the list of projects. The list could be empty if the vendor isn't
     * associated with any.
     */
    public List getProjects()
    {
        return m_projects;
    }

    /**
     * Get the pseudonym/alias of the vendor.
     */
    public String getPseudonym()
    {
        return m_pseudonym;
    }

    /**
     * Get a list of ratings associated with this vendor.
     */
    public List getRatings()
    {
        return m_ratings;
    }

    /**
     * Return the contents of the resume or NULL if there isn't any.
     */
    public String getResume()
    {
        return m_resume;
    }

    /**
     * Returns the bytes of the resume file. This is only populated when the
     * vendor is first created and is used to copy the file to the temporary
     * directory. Could return NULL if no resume file is specified.
     */
    byte[] getResumeContentInBytes()
    {
        return m_resumeContentInBytes;
    }

    /**
     * Return the name of the resume if it is a file. Will return NULL if the
     * resume wasn't uploaded as a file.
     */
    public String getResumeFilename()
    {
        return m_resumeFilename;
    }

    /**
     * Returns the relative path to the resume filename, plus the file name
     * itself. This is where the file is being stored on the GlobalSight file
     * system.
     */
    public String getResumePath()
    {
        String path = null;
        if (getResumeFilename() != null)
        {
            path = VendorManagement.VENDOR_RESUME_STORAGE + File.separator
                    + this.getCustomVendorId() + File.separator
                    + this.getResumeFilename();
        }
        return path;
    }

    /**
     * Gets the list of roles that the vendor can perform.
     */
    public Set getRoles()
    {
        return m_roles;
    }

    /**
     * Gets the status of the vendor (APPROVED, PENDING, ON-HOLD, REJECTED
     */
    public String getStatus()
    {
        return m_status;
    }

    public String getTitle()
    {
        return m_title;
    }

    public boolean getUseInAmbassador()
    {
        return m_useInAmbassador;
    }

    /**
     * Returns 'NULL' or the user that this vendor is in GlobalSight.
     */
    public User getUser()
    {
        return m_user;
    }

    public String getUserId()
    {
        return m_userId;
    }

    /**
     * @see User.isInAllProjects()
     */
    public boolean isInAllProjects()
    {
        return m_isInAllProjects;
    }

    /**
     * @see User.isInAllProjects(boolean)
     */
    public void isInAllProjects(boolean p_inAllProjects)
    {
        m_isInAllProjects = p_inAllProjects;
    }

    /**
     * Returns 'true' if the vendor is internal and 'false' if the vendor is not
     * internal.
     */
    public boolean isInternalVendor()
    {
        return m_isInternalVendor;
    }

    /**
     * Sets whether the vendor is an internal vendor 'true' or not 'false'.
     */
    public void isInternalVendor(boolean p_isInternal)
    {
        m_isInternalVendor = p_isInternal;
    }

    /**
     * Remove a custom field from the list. If it doesn't exist then it just
     * ignores the request.
     */
    public void removeCustomField(String p_key)
    {
        m_customFields.remove(p_key);
    }

    /**
     * Remove the specified project from the list of projects the vendor is
     * associated with.
     */
    public void removeFromProject(Project p_project)
    {
        if (p_project != null)
        {
            m_projects.remove(p_project);
        }
    }

    /**
     * Remove the rating associated to this vendor.
     */
    public void removeRating(Rating p_rating)
    {
        if (p_rating != null)
        {
            // clear out the back pointer and remove.
            p_rating.setVendor(null);
            m_ratings.remove(p_rating);
        }
    }

    /**
     * Remove the role from this vendor.
     */
    public void removeRole(VendorRole p_role)
    {
        if (p_role != null)
        {
            // clear out the back pointer and remove.
            p_role.setVendor(null);
            m_roles.remove(p_role);
        }
    }

    public void seCommunicationLocale(String locale)
    {
        m_communicationLocale = locale;
    }

    public void setAddress(String p_address)
    {
        m_address = p_address;
    }

    public void setCommInfo(Set commInfo)
    {
        m_commInfo.clear();
        for (Iterator it = commInfo.iterator(); it.hasNext();)
        {
            CommunicationInfo info = (CommunicationInfo) it.next();
            m_commInfo.put(info.getTypeAsInteger(), info);
        }
    }

    public void setCompanyName(String p_companyName)
    {
        m_companyName = p_companyName;
    }

    public void setCountry(String p_country)
    {
        m_country = p_country;
    }

    /**
     * Set all the custom fields to the hashtable being passed in.
     */
    public void setCustomFields(Hashtable p_custom)
    {
        m_customFields = p_custom;
    }

    public void setCustomFieldsAsSet(Set customFields)
    {
        m_customFields.clear();
        for (Iterator it = customFields.iterator(); it.hasNext();)
        {
            CustomField cf = (CustomField) it.next();
            m_customFields.put(cf.getKey(), cf);
        }
    }

    /**
     * Set the unique identifier for the vendor that the customer can define. If
     * they don't define one then it is generated automatically.
     */
    public void setCustomVendorId(String p_vendorId)
    {
        m_customVendorId = p_vendorId;
    }

    /**
     * Set the date of birth of the vendor. No validation is done to check if
     * the string is truly a date.
     */
    public void setDateOfBirth(String p_dob)
    {
        m_dob = p_dob;
    }

    /**
     * Set the locale used for communication to this vendor.
     */
    public void setDefaultUILocale(String p_locale)
    {
        if (p_locale != null)
        {
            m_communicationLocale = p_locale;
        }
    }

    public void setDob(String m_dob)
    {
        this.m_dob = m_dob;
    }

    /**
     * Set the email address for the vendor.
     * 
     * @param p_emailAddress
     *            The email address to set for the vendor. If set to NULL then
     *            remove the existing one from the vendor (clear out the email
     *            address).
     */
    public void setEmail(String p_emailAddress)
    {
        Integer emailType = new Integer(
                CommunicationInfo.CommunicationType.EMAIL);
        CommunicationInfo ci = (CommunicationInfo) m_commInfo.get(emailType);

        // if set to NULL and one exists then remove it
        if (p_emailAddress == null && ci != null)
        {
            m_commInfo.remove(emailType);
        }
        else if (p_emailAddress != null)
        {
            if (ci == null)
            {
                ci = new CommunicationInfo(
                        CommunicationInfo.CommunicationType.EMAIL,
                        p_emailAddress, this);
            }
            else
            // replace the existing one
            {
                ci.setValue(p_emailAddress);
            }
            m_commInfo.put(emailType, ci);
        }
    }

    public void setFirstName(String p_firstName)
    {
        m_firstName = p_firstName;
    }

    public void setIsInAllProjects(boolean inAllProjects)
    {
        m_isInAllProjects = inAllProjects;
    }

    public void setIsInternalVendor(boolean internalVendor)
    {
        m_isInternalVendor = internalVendor;
    }

    public void setLastName(String p_lastName)
    {
        m_lastName = p_lastName;
    }

    public void setNationalities(String p_nationalities)
    {
        m_nationalities = p_nationalities;
    }

    /**
     * Set the notes that should be associated with the vendor.
     */
    public void setNotes(String p_notes)
    {
        m_notes = p_notes;
    }

    // ======================= package methods===============================

    public void setPassword(String p_password)
    {
        m_password = p_password;
    }

    /**
     * Set the particular phone number
     * 
     * @param p_type
     *            The type of phone number. See
     *            CommunicationInfo.CommunicationType for the valid types.
     * @param p_phoneNumber
     *            The actual phone number. If this is NULL then remove the
     *            existing phone number for the vendor (clear it out).
     */
    public void setPhoneNumber(int p_type, String p_phoneNumber)
    {
        switch (p_type)
        {
        case CommunicationInfo.CommunicationType.WORK:
        case CommunicationInfo.CommunicationType.HOME:
        case CommunicationInfo.CommunicationType.CELL:
        case CommunicationInfo.CommunicationType.FAX:
        case CommunicationInfo.CommunicationType.OTHER:

            CommunicationInfo ci = (CommunicationInfo) m_commInfo
                    .get(new Integer(p_type));
            // if phone number set to NULL remove the existing one
            if (p_phoneNumber == null && ci != null)
            {
                // clear out the field
                m_commInfo.remove(new Integer(p_type));
            }
            else if (p_phoneNumber != null)
            {
                if (ci == null)
                {
                    ci = new CommunicationInfo(p_type, p_phoneNumber, this);
                }
                else
                {
                    ci.setValue(p_phoneNumber);
                }
                m_commInfo.put(new Integer(p_type), ci);
            }
        }
    }

    /**
     * Sets the list of projects that the vendor is associated with.
     */
    public void setProjects(List p_projects)
    {
        if (p_projects == null)
        {
            m_projects.clear();
        }
        else
        {
            m_projects = p_projects;
        }
    }

    /**
     * Set the pseudonym/alias of the vendor.
     */
    public void setPseudonym(String p_pseudonym)
    {
        m_pseudonym = p_pseudonym;
    }

    public void setRatings(List ratings)
    {
        m_ratings = ratings;
    }

    /**
     * Sets and gets the CV/Resume text of the vendor explaining their skills
     * and experiences.
     */
    public void setResume(String p_resume)
    {
        m_resumeFilename = null; // null out in case setting something
        m_resumeContentInBytes = null; // new
        m_resume = p_resume;
    }

    /**
     * Sets a resume that is an uploaded file.
     */
    public void setResume(String p_fileName, byte[] fileContent)
    {
        m_resume = null;

        // String resumeFilename = null;
        int index = p_fileName.lastIndexOf(File.separator);
        // strip off any directory and just store the base filename
        if (index > 0)
        {
            m_resumeFilename = p_fileName.substring(index + 1);
        }
        else
        {
            m_resumeFilename = p_fileName;
        }
        m_resumeContentInBytes = fileContent;
    }

    public void setResumeFilename(String filename)
    {
        m_resumeFilename = filename;
    }

    /**
     * Sets the list of roles that the vendor can perform.
     */
    public void setRoles(Set p_roles)
    {
        m_roles.clear();

        if (p_roles != null)
        {
            m_roles = p_roles;
        }
    }

    /**
     * Sets the status of the vendor (APPROVED, PENDING, ON-HOLD, REJECTED
     */
    public void setStatus(String p_status)
    {
        // only change the status if it is a valid one
        if (p_status != null
                && (p_status.equals(APPROVED_STATUS)
                        || p_status.equals(PENDING_STATUS)
                        || p_status.equals(REJECTED_STATUS) || p_status
                        .equals(ON_HOLD_STATUS)))
        {
            m_status = p_status;
        }
        // if set to NULL then set to the default status
        else if (p_status == null)
        {
            m_status = PENDING_STATUS;
        }

    }

    public void setTitle(String p_title)
    {
        m_title = p_title;
    }

    public void setUseInAmbassador(boolean inAmbassador)
    {
        m_useInAmbassador = inAmbassador;
    }

    /**
     * Set the GlobalSight user that this vendor is associated with.
     */
    public void setUser(User p_user)
    {
        m_user = p_user;
    }

    public void setUserId(String p_userId)
    {
        m_userId = p_userId;
    }

    /**
     * This method is used only for debugging.
     */
    public String toDebugString()
    {

        StringBuffer dString = new StringBuffer();
        dString.append(super.toString());
        dString.append("m_customVendorId=");
        dString.append(m_customVendorId != null ? m_customVendorId : "null");
        dString.append(", m_status=");
        dString.append(m_status);
        dString.append(", m_isInternalVendor=");
        dString.append(m_isInternalVendor ? "true" : "false");
        dString.append(", m_companyName=");
        dString.append(m_companyName != null ? m_companyName : "null");
        dString.append(", m_firstName=");
        dString.append(m_firstName != null ? m_firstName : "null");
        dString.append(", m_lastName=");
        dString.append(m_lastName != null ? m_lastName : "null");
        dString.append(", m_password=");
        dString.append(m_password != null ? m_password : "null");
        dString.append(", m_address=");
        dString.append(m_address != null ? m_address : "null");
        dString.append(", m_commInfo=");
        dString.append(m_commInfo.toString());
        dString.append(", m_communicationLocale=");
        dString.append(getDefaultUILocale());
        dString.append(", m_resume=");
        dString.append(m_resume == null ? "null" : getResume());
        dString.append(", m_resumeFilename=");
        dString.append(m_resumeFilename == null ? "null" : getResumeFilename());
        dString.append(", m_notes=");
        dString.append(m_notes);
        dString.append(", m_isInAllProjects=");
        dString.append(m_isInAllProjects ? "true" : "false");
        dString.append(", m_projects=");
        dString.append(m_projects.toString());
        dString.append(", m_roles=");
        dString.append(m_roles.toString());
        dString.append(", m_useInAmbassador=");
        dString.append(m_useInAmbassador ? " true" : "false");
        if (m_useInAmbassador)
        {
            dString.append(", m_userId=");
            dString.append(m_userId != null ? m_userId : "null");
            dString.append(", m_password=");
            dString.append(m_password != null ? m_password : "null");
        }

        dString.append("\n");
        return dString.toString();
    }

    public String toString()
    {
        StringBuffer dString = new StringBuffer();
        dString.append(super.toString());
        dString.append("m_customVendorId=");
        dString.append(m_customVendorId != null ? m_customVendorId : "null");
        dString.append(", m_status=");
        dString.append(m_status);
        dString.append(", m_firstName=");
        dString.append(m_firstName != null ? m_firstName : "null");
        dString.append(", m_lastName=");
        dString.append(m_lastName != null ? m_lastName : "null");
        return dString.toString();
    }

    /**
     * Gets whether the vendor should be used in GlobalSight as a user.
     */
    public boolean useInAmbassador()
    {
        return m_useInAmbassador;
    }

    /**
     * Sets whether the vendor should be used in GlobalSight. If set to 'true'
     * then once the vendor is approved they are created as a user in GlobalSight
     * and can have work assigned to them.
     */
    public void useInAmbassador(boolean p_useInAmbassador)
    {
        m_useInAmbassador = p_useInAmbassador;
    }
}
