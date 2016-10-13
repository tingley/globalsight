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

package com.globalsight.webservices.vendormanagement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.globalsight.everest.customform.CustomField;
import com.globalsight.everest.customform.CustomForm;
import com.globalsight.everest.customform.CustomFormParser;
import com.globalsight.everest.foundation.LocalePair;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.localemgr.LocaleManager;
import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.projecthandler.ProjectHandler;
import com.globalsight.everest.securitymgr.FieldSecurity;
import com.globalsight.everest.securitymgr.SecurityManager;
import com.globalsight.everest.securitymgr.VendorSecureFields;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.vendormanagement.CommunicationInfo;
import com.globalsight.everest.vendormanagement.Vendor;
import com.globalsight.everest.vendormanagement.VendorException;
import com.globalsight.everest.vendormanagement.VendorInfo;
import com.globalsight.everest.vendormanagement.VendorManagementLocal;
import com.globalsight.everest.vendormanagement.VendorRole;
import com.globalsight.everest.workflow.Activity;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.webservices.AbstractWebService;
import com.globalsight.webservices.Ambassador;
import com.globalsight.webservices.WebServiceException;

/**
 * Provides the web service implementation specific to vendor management.
 */
public class VendorManagement extends AbstractWebService
{
    // ------------------------ static variables
    private static final Logger s_logger = Logger
            .getLogger(VendorManagement.class);
    private static com.globalsight.everest.vendormanagement.VendorManagement s_vm = null;
    private static ProjectHandler s_ph = null;
    private static SecurityManager s_sm = null;
    private static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\r\n";
    private static final String SUCCESSFUL = "success";
    private static final String CONFIDENTIAL = "CONFIDENTIAL";
    private static final String NOT_CONFIDENTIAL = "NOT CONFIDENTIAL";

    // -------------------- member variables ----------------------------------
    // used by checkIfInstalled() to remember whether the web service is
    // installed
    private static boolean s_isInstalled = false;

    static
    {
        try
        {
            // if both web services and VM are installed
            // then the VM web service is considered installed
            s_isInstalled = Ambassador.isInstalled()
                    && VendorManagementLocal.isInstalled();
            if (s_isInstalled)
            {
                s_logger.info("Vendor Management Web Service is installed.");
                s_vm = ServerProxy.getVendorManagement();
                s_sm = ServerProxy.getSecurityManager();
            }
            else
            {
                s_logger.info("Vendor Management Web Service is not installed.");
            }

        }
        catch (Exception ne)
        {
            s_logger.error(
                    "Failed to create the Vendor Management web service.", ne);
        }
    }

    /**
     * Creates a VendorManagement web service object.
     */
    public VendorManagement()
    {
        s_logger.info("Creating new Vendor Management Web Service object.");

    }

    /**
     * @see VendorManagementService.login(String p_username, String p_password)
     */
    public String login(String p_username, String p_password)
            throws WebServiceException
    {
        return doLogin(p_username, p_password);
    }

    /**
     * @see VendorManagementService.helloWorld
     */
    public String helloWorld() throws WebServiceException
    {
        checkIfInstalled();
        return "Hello from the GlobalSight Vendor Management Web service.";
    }

    /**
     * @see VendorManagementService.addVendor(String)
     */
    public String addVendor(String p_accessToken, String p_xmlVendorInfo)
            throws WebServiceException
    {
        checkAccess(p_accessToken, "addVendor");
        StringBuffer responseXml = null;
        Vendor v = null;
        try
        {
            String username = this.getUsernameFromSession(p_accessToken);
            User usr = getUser(username);
            v = createVendorObject(usr, p_xmlVendorInfo, true);
            s_vm.addVendor(usr, v, null);

            responseXml = new StringBuffer(XML_HEADER);
            responseXml.append("<addVendorResult>\r\n");
            responseXml.append("\t<vendorId>")
                    .append(v.getIdAsLong().toString())
                    .append("</vendorId>\r\n");
            responseXml.append("\t<customVendorId>")
                    .append(v.getCustomVendorId())
                    .append("</customVendorId>\r\n");
            responseXml.append("\t<status>").append(SUCCESSFUL)
                    .append("</status>\r\n");
            responseXml.append("</addVendorResult>");
        }
        catch (VendorException vme)
        {
            String message = null;
            // the user couldn't be created - but the vendor was.
            if (vme.getMessageKey().equals(
                    VendorException.MSG_FAILED_TO_CREATE_USER))
            {
                message = "Vendor " + v.getCustomVendorId()
                        + " was created successfully, however "
                        + " the user associated with it could not be created."
                        + "  It is possible it already exists.";
            }
            else
            {
                message = "Failed to add vendor " + v.getCustomVendorId();
            }
            s_logger.error(message, vme);
            message = makeErrorXml("addVendor",
                    message + vme.getLocalizedMessage());
            throw new WebServiceException(message);
        }
        catch (WebServiceException wse)
        {
            throw wse;
        }
        catch (Exception e)
        {
            s_logger.error(
                    "Failed to add a vendor with the following information: "
                            + p_xmlVendorInfo, e);
            String message = "Failed to add a vendor.  "
                    + e.getLocalizedMessage();
            message = makeErrorXml("addVendor", message);
            throw new WebServiceException(message);
        }
        return responseXml.toString();
    }

    /**
     * @see VendorManagementService.modifyVendor(String)
     */
    public String modifyVendor(String p_accessToken, String p_xmlVendorInfo)
            throws WebServiceException
    {
        StringBuffer responseXml = null;
        checkAccess(p_accessToken, "modifyVendor");
        Vendor v = null;
        try
        {
            String username = this.getUsernameFromSession(p_accessToken);
            User usr = getUser(username);
            v = createVendorObject(usr, p_xmlVendorInfo, false);
            s_vm.modifyVendor(usr, v, null);

            responseXml = new StringBuffer(XML_HEADER);
            responseXml.append("<modifyVendorResult>\r\n");
            responseXml.append("\t<vendorId>")
                    .append(v.getIdAsLong().toString())
                    .append("</vendorId>\r\n");
            responseXml.append("\t<customVendorId>")
                    .append(v.getCustomVendorId())
                    .append("</customVendorId>\r\n");
            responseXml.append("\t<status>").append(SUCCESSFUL)
                    .append("</status>\r\n");
            responseXml.append("</modifyVendorResult>");
        }
        catch (VendorException vme)
        {
            String message = null;
            // the user couldn't be created/modified - but the vendor was.
            if (vme.getMessageKey().equals(
                    VendorException.MSG_FAILED_TO_CREATE_USER))
            {
                message = "Vendor " + v.getCustomVendorId()
                        + " was modified successfully, however "
                        + " the user associated with it could not be created.";
            }
            else if (vme.getMessageKey().equals(
                    VendorException.MSG_FAILED_TO_MODIFY_USER))
            {
                message = "Vendor " + v.getCustomVendorId()
                        + " was modified successfully, however "
                        + "the user associated with it could not be modified.";
            }
            else
            {
                message = "Failed to modify vendor " + v.getCustomVendorId();
            }
            s_logger.error(message, vme);
            message = makeErrorXml("modifyVendor",
                    message + vme.getLocalizedMessage());
            throw new WebServiceException(message);
        }
        catch (WebServiceException wse)
        {
            throw wse;
        }
        catch (Exception e)
        {
            s_logger.error("Failed to modify vendor with info: "
                    + p_xmlVendorInfo, e);
            String message = "Failed to modify a vendor.  "
                    + e.getLocalizedMessage();
            message = makeErrorXml("modifyVendor", message);
            throw new WebServiceException(message);
        }
        return responseXml.toString();
    }

    /**
     * @see VendorManagementService.removeVendor(String)
     */
    public String removeVendor(String p_accessToken, String p_customVendorId)
            throws WebServiceException
    {
        StringBuffer responseXml = null;
        checkAccess(p_accessToken, "removeVendor");

        try
        {
            String username = this.getUsernameFromSession(p_accessToken);
            s_vm.removeVendor(getUser(username), p_customVendorId);

            responseXml = new StringBuffer(XML_HEADER);
            responseXml.append("<removeVendorResult>\r\n");
            responseXml.append("\t<customVendorId>").append(p_customVendorId)
                    .append("</customVendorId>\r\n");
            responseXml.append("\t<status>").append(SUCCESSFUL)
                    .append("</status>\r\n");
            responseXml.append("</removeVendorResult>");
        }
        catch (Exception e)
        {
            s_logger.error("Failed to remove vendor " + p_customVendorId, e);
            String message = "Failed to remove vendor " + p_customVendorId
                    + ", " + e.getLocalizedMessage();
            message = makeErrorXml("removeVendor", message);
            throw new WebServiceException(message);
        }
        return responseXml.toString();
    }

    /**
     * @see VendorManagementService.queryVendorBasicInfo()
     */
    public String queryVendorBasicInfo(String p_accessToken)
            throws WebServiceException
    {
        checkAccess(p_accessToken, "queryVendorBasicInfo");
        StringBuffer responseXml = null;
        try
        {
            User u = getUser(this.getUsernameFromSession(p_accessToken));
            List vendors = s_vm.getVendors(u);
            responseXml = new StringBuffer(XML_HEADER);
            responseXml.append("<vendorInformation>\r\n");

            for (Iterator vi = vendors.iterator(); vi.hasNext();)
            {
                VendorInfo v = (VendorInfo) vi.next();
                FieldSecurity fs = s_sm.getFieldSecurity(u, v, true);

                responseXml.append("<vendor>\r\n");
                responseXml.append("\t<vendorId>").append(v.getId())
                        .append("</vendorId>\r\n");
                responseXml
                        .append("\t<customVendorId>")
                        .append(getSecureValue(fs,
                                VendorSecureFields.CUSTOM_ID,
                                v.getCustomVendorId()))
                        .append("</customVendorId>\r\n");
                responseXml
                        .append("\t<firstName>")
                        .append(getSecureValue(fs,
                                VendorSecureFields.FIRST_NAME, v.getFirstName()))
                        .append("</firstName>\r\n");
                responseXml
                        .append("\t<lastName>")
                        .append(getSecureValue(fs,
                                VendorSecureFields.LAST_NAME, v.getLastName()))
                        .append("</lastName>\r\n");
                responseXml.append("\t<pseudonym>").append(v.getPseudonym())
                        .append("</pseudonym>\r\n");
                responseXml
                        .append("\t<status>")
                        .append(getSecureValue(fs, VendorSecureFields.STATUS,
                                v.getStatus())).append("</status>\r\n");
                responseXml.append("</vendor>\r\n");
            }
            responseXml.append("</vendorInformation>\n");
        }
        catch (Exception e)
        {
            s_logger.error(
                    "Failed to get the basic information for all vendors.", e);
            String message = "Failed to get the basic information for all vendors.  "
                    + e.getLocalizedMessage();
            message = makeErrorXml("queryVendorBasicInfo", message);
            throw new WebServiceException(message);
        }
        return responseXml.toString();
    }

    /**
     * @see VendorManagementService.queryVendorDetails(String)
     */
    public String queryVendorDetails(String p_accessToken,
            String p_customVendorId) throws WebServiceException
    {
        checkAccess(p_accessToken, "queryVendorDetails");
        StringBuffer responseXml = null;

        try
        {
            String username = this.getUsernameFromSession(p_accessToken);
            User u = getUser(username);
            Vendor v = s_vm.getVendorByCustomId(u, p_customVendorId);
            if (v != null)
            {
                FieldSecurity fs = s_sm.getFieldSecurity(u, v, true);
                responseXml = new StringBuffer(XML_HEADER);
                responseXml.append("<vendor>\r\n");
                responseXml.append("\t<vendorId>").append(v.getId())
                        .append("</vendorId>\r\n");
                responseXml
                        .append("\t<customVendorId>")
                        .append(getSecureValue(fs,
                                VendorSecureFields.CUSTOM_ID,
                                v.getCustomVendorId()))
                        .append("</customVendorId>\r\n");
                responseXml
                        .append("\t<firstName>")
                        .append(getSecureValue(fs,
                                VendorSecureFields.FIRST_NAME, v.getFirstName()))
                        .append("</firstName>\r\n");
                responseXml
                        .append("\t<lastName>")
                        .append(getSecureValue(fs,
                                VendorSecureFields.LAST_NAME, v.getLastName()))
                        .append("</lastName>\r\n");
                responseXml.append("\t<pseudonym>").append(v.getPseudonym())
                        .append("</pseudonym>\r\n");
                responseXml
                        .append("\t<status>")
                        .append(getSecureValue(fs, VendorSecureFields.STATUS,
                                v.getStatus())).append("</status>\r\n");
                if (v.getTitle() != null && v.getTitle().length() > 0)
                {
                    responseXml
                            .append("\t<title>")
                            .append(getSecureValue(fs,
                                    VendorSecureFields.TITLE, v.getTitle()))
                            .append("</title>\r\n");
                }
                String isInternalVendor = v.isInternalVendor() ? "true"
                        : "false";
                responseXml
                        .append("\t<isInternalVendor>")
                        .append(getSecureValue(fs,
                                VendorSecureFields.IS_INTERNAL,
                                isInternalVendor))
                        .append("</isInternalVendor>\r\n");
                if (v.getCompanyName() != null
                        && v.getCompanyName().length() > 0)
                {
                    responseXml
                            .append("\t<companyName>")
                            .append(getSecureValue(fs,
                                    VendorSecureFields.COMPANY,
                                    v.getCompanyName()))
                            .append("</companyName>\r\n");
                }
                if (v.getAddress() != null && v.getAddress().length() > 0)
                {
                    responseXml
                            .append("\t<address>")
                            .append(getSecureValue(fs,
                                    VendorSecureFields.ADDRESS, v.getAddress()))
                            .append("</address>\r\n");
                }
                if (v.getNationalities() != null
                        && v.getNationalities().length() > 0)
                {
                    responseXml
                            .append("\t<nationality>")
                            .append(getSecureValue(fs,
                                    VendorSecureFields.CITIZENSHIP,
                                    v.getNationalities()))
                            .append("</nationality>\r\n");
                }
                if (v.getDateOfBirth() != null
                        && v.getDateOfBirth().length() > 0)
                {
                    responseXml
                            .append("\t<dob>")
                            .append(getSecureValue(fs, VendorSecureFields.DOB,
                                    v.getDateOfBirth())).append("</dob>\r\n");
                }
                if (v.getEmail() != null && v.getEmail().length() > 0)
                {
                    responseXml.append("\t<communication>\r\n");
                    responseXml
                            .append("\t\t<communicationType>EMAIL</communicationType>\r\n");
                    responseXml
                            .append("\t\t<communicationValue>")
                            .append(getSecureValue(fs,
                                    VendorSecureFields.EMAIL, v.getEmail()))
                            .append("</communicationValue>\r\n");
                    responseXml.append("\t</communication>\r\n");
                }
                responseXml.append(getPhoneNumbersXml(v, fs));

                responseXml
                        .append("\t<communicationLocale>")
                        .append(getSecureValue(fs,
                                VendorSecureFields.EMAIL_LANGUAGE,
                                v.getDefaultUILocale()))
                        .append("</communicationLocale>\r\n");

                if (v.getResume() != null && v.getResume().length() > 0)
                {
                    responseXml.append("\t<resume>");
                    responseXml.append(
                            getSecureValue(fs, VendorSecureFields.RESUME,
                                    v.getResume())).append("</resume>\r\n");
                }
                else if (v.getResumeFilename() != null
                        && v.getResumeFilename().length() > 0)
                {
                    responseXml.append("\t<resume filename=\"");
                    String resumeFilename = v.getResumeFilename();
                    responseXml.append(resumeFilename);
                    responseXml.append("\">\r\n");
                    responseXml.append(getResumeFromFile(v.getResumePath()));
                    responseXml.append("<\t</resume>");
                }
                if (v.getNotes() != null && v.getNotes().length() > 0)
                {
                    responseXml
                            .append("\t<notes>")
                            .append(getSecureValue(fs,
                                    VendorSecureFields.NOTES, v.getNotes()))
                            .append("</notes>\r\n");
                }

                responseXml.append(getRolesXml(v, fs));
                responseXml.append(getProjectsXml(v, fs));
                responseXml.append(getCustomFieldsXml(v, fs));

                boolean isAmbassadorUser = v.useInAmbassador();
                responseXml.append("\t<user isAmbassadorUser=\"").append(
                        isAmbassadorUser ? "true\">" : "false\"/>");
                responseXml.append("\r\n");
                if (isAmbassadorUser)
                {
                    if (v.getUser() != null)
                    {
                        responseXml
                                .append("\t\t<username>")
                                .append(getSecureValue(fs,
                                        VendorSecureFields.USERNAME, v
                                                .getUser().getUserName()))
                                .append("</username>");
                    }
                    responseXml.append("\t</user>\r\n");
                }

                responseXml.append("</vendor>\r\n");
            }
            else
            {
                // Vendor was not found
                s_logger.error("Failed to get the detailed information because vendor "
                        + p_customVendorId + " does not exist.");
                String message = "Failed to get the detailed information because vendor "
                        + p_customVendorId + "does not exst.";
                message = makeErrorXml("queryVendorDetails", message);
                throw new WebServiceException(message);
            }
        }
        catch (WebServiceException wse)
        {
            throw wse;
        }
        catch (Exception e)
        {
            s_logger.error("Failed to get the detailed information for vendor "
                    + p_customVendorId, e);
            String message = "Failed to get the detailed information for vendor "
                    + p_customVendorId + ", " + e.getLocalizedMessage();
            message = makeErrorXml("queryVendorDetails", message);
            throw new WebServiceException(message);
        }
        return responseXml.toString();
    }

    // ============================= private methods
    // ==================================

    /**
     * This checks whether the web service is installed. The websvc.installKey
     * system parameter value is checked against the expected value. If not
     * installed, then an exception is thrown
     * 
     * @exception WebServiceException
     */
    protected void checkIfInstalled() throws WebServiceException
    {
        if (!s_isInstalled)
            throw new WebServiceException("VM Web service is not installed.");
    }

    /**
     * Parses through the XML sent in and creates a vendor or modified an
     * existing vendor from the vendor information specified. It also only
     * allows updates to the fields that are SHARED with the user requesting the
     * modification or add.
     * 
     * @param p_xmlVendorInfo
     *            An XML string describing the vendor.
     * @param p_createNewVendor
     *            Specifies if the vendodr is new or an existing one to be
     *            modified. 'true' means create a new one, 'false' means modify
     *            an existing one.
     * 
     * @return The newly created or modified vendor is returned.
     * 
     */
    private Vendor createVendorObject(User p_userRequesting,
            String p_xmlVendorInfo, boolean p_createNewVendor)
            throws WebServiceException
    {
        Vendor v = null;
        VendorXmlParser parser = new VendorXmlParser(p_xmlVendorInfo);
        try
        {
            parser.parse();
        }
        catch (Exception e)
        {
            String errMessage = "An error occured with parsing the vendor XML.";
            s_logger.error(errMessage, e);
            throw new WebServiceException(errMessage + e.getLocalizedMessage());
        }

        String customVendorId = parser.getCustomVendorId();
        if (p_createNewVendor)
        {
            v = new Vendor();
            v.setCustomVendorId(customVendorId);
        }
        else
        {
            try
            {
                v = s_vm.getVendorByCustomId(p_userRequesting, customVendorId);
            }
            catch (Exception e)
            {
                String errMessage = "Failed to find vendor with custom id "
                        + customVendorId + " for modification.";
                s_logger.error(errMessage, e);
                throw new WebServiceException(errMessage);
            }
            // couldn't find the vendor so throw an error - they must
            // specifically
            // say ADD if the vendor doesn't exist yet.
            if (v == null)
            {
                String errMessage = "Failed to find vendor with custom id "
                        + customVendorId + " for modification.";
                s_logger.error(errMessage);
                throw new WebServiceException(errMessage);
            }
        }

        FieldSecurity fs = null;

        try
        {
            fs = ServerProxy.getSecurityManager().getFieldSecurity(
                    p_userRequesting, v, true);
        }
        catch (Exception e)
        {
            String errMessage = "Failed to add/modify the vendor "
                    + v.getCustomVendorId()
                    + " - couldn't retrieve the field security for the vendor";
            s_logger.error(errMessage, e);
            throw new WebServiceException(errMessage);
        }

        if (canFieldBeUpdated(fs, VendorSecureFields.FIRST_NAME))
        {
            v.setFirstName(parser.getFirstName());
        }
        if (canFieldBeUpdated(fs, VendorSecureFields.LAST_NAME))
        {
            v.setLastName(parser.getLastName());
        }
        // the alias is not secure
        v.setPseudonym(parser.getPseudonym());

        if (canFieldBeUpdated(fs, VendorSecureFields.TITLE))
        {
            v.setTitle(parser.getTitle());
        }
        if (canFieldBeUpdated(fs, VendorSecureFields.COMPANY))
        {
            v.setCompanyName(parser.getCompanyName());
        }
        if (canFieldBeUpdated(fs, VendorSecureFields.ADDRESS))
        {
            v.setAddress(parser.getAddress());
        }
        if (canFieldBeUpdated(fs, VendorSecureFields.COUNTRY))
        {
            v.setCountry(parser.getCountry());
        }
        if (canFieldBeUpdated(fs, VendorSecureFields.CITIZENSHIP))
        {
            v.setNationalities(parser.getNationality());
        }
        if (canFieldBeUpdated(fs, VendorSecureFields.DOB))
        {
            v.setDateOfBirth(parser.getDateOfBirth());
        }
        if (canFieldBeUpdated(fs, VendorSecureFields.IS_INTERNAL))
        {
            String isInternal = parser.isInternalVendor();
            if (isInternal != null && isInternal.toLowerCase().equals("true"))
            {
                v.isInternalVendor(true);
            }
        }
        if (canFieldBeUpdated(fs, VendorSecureFields.NOTES))
        {
            v.setNotes(parser.getNotes());
        }
        if (canFieldBeUpdated(fs, VendorSecureFields.STATUS))
        {
            v.setStatus(parser.getStatus());
        }
        if (canFieldBeUpdated(fs, VendorSecureFields.EMAIL_LANGUAGE))
        {
            v.setDefaultUILocale(parser.getCommunicationLocale());
        }
        if (canFieldBeUpdated(fs, VendorSecureFields.RESUME))
        {
            String resume = parser.getResume();
            String resumeFilename = parser.getResumeFilename();
            if (resumeFilename != null && resumeFilename.length() > 0)
            {
                v.setResume(resumeFilename, resume.getBytes());
            }
            else
            {
                // This could be setting it to NULL or setting it to valid text.
                v.setResume(resume);
            }
        }

        // set communication information (email and phone numbers)
        Hashtable cis = parser.getCommunicationInfo();
        for (Enumeration keys = cis.keys(); keys.hasMoreElements();)
        {
            String type = (String) keys.nextElement();
            String value = (String) cis.get(type);
            if ("EMAIL".equals(type)
                    && canFieldBeUpdated(fs, VendorSecureFields.EMAIL))
            {
                v.setEmail(value);
            }
            else if (!"EMAIL".equals(type))
            {
                int typeAsInt = CommunicationInfo.typeAsInt(type);
                String fieldName;
                switch (typeAsInt)
                {
                    case CommunicationInfo.CommunicationType.WORK:
                        fieldName = VendorSecureFields.WORK_PHONE;
                        break;
                    case CommunicationInfo.CommunicationType.HOME:
                        fieldName = VendorSecureFields.HOME_PHONE;
                        break;
                    case CommunicationInfo.CommunicationType.CELL:
                        fieldName = VendorSecureFields.CELL_PHONE;
                        break;
                    case CommunicationInfo.CommunicationType.FAX:
                        fieldName = VendorSecureFields.FAX;
                        break;
                    case CommunicationInfo.CommunicationType.OTHER:
                    default:
                        fieldName = ""; // no field name - won't be able to add
                        break;
                }
                if (canFieldBeUpdated(fs, fieldName))
                {
                    v.addCommunicationInfo(typeAsInt, value);
                }
            }
        }

        if (canFieldBeUpdated(fs, VendorSecureFields.PROJECTS))
        {
            String inAllProjects = parser.getAllProjectsFlag();
            setProjects(v, inAllProjects.equals("true") ? true : false,
                    parser.getProjects());
        }
        if (canFieldBeUpdated(fs, VendorSecureFields.ROLES))
        {
            setRoles(v, parser.getRoles());
        }
        if (canFieldBeUpdated(fs, VendorSecureFields.CUSTOM_FIELDS))
        {
            setCustomFields(v, parser.getCustomFields());
        }

        // user info
        String userId = new String();
        String password = new String();
        if (canFieldBeUpdated(fs, VendorSecureFields.USERNAME))
        {
            userId = parser.getUserId();
        }
        else
        {
            if (v.getUser() != null)
            {
                userId = v.getUser().getUserId();
                password = v.getUser().getPassword();
            }
        }
        if (canFieldBeUpdated(fs, VendorSecureFields.PASSWORD))
        {
            password = parser.getPassword();
        }
        String isAmbassadorUser = v.useInAmbassador() ? "true" : "false";
        String isExistingUser = v.getUser() != null ? "true" : "false";
        if (canFieldBeUpdated(fs, VendorSecureFields.AMBASSADOR_ACCESS))
        {
            isExistingUser = parser.getExistingAmbassadorUser();
            isAmbassadorUser = parser.getAmbassadorUser();

            // if an existing user then find the user and set in the vendor
            // object.
            if (isExistingUser.toLowerCase().equals("true"))
            {
                v.useInAmbassador(true);
                User u = null;
                try
                {
                    u = ServerProxy.getUserManager().getUser(userId);
                }
                catch (Exception e)
                {
                    s_logger.error(
                            "Couldn't find the existing user the vendor should be assigned to.",
                            e);
                    u = null;
                }
                if (u != null)
                {
                    v.setUser(u);
                    v.setUserId(userId);
                }
                else
                {
                    v.setUserId(userId);
                    v.setPassword(parser.getPassword());
                }
            }
            // else if just set to be a new user
            else if (isAmbassadorUser.toLowerCase().equals("true"))
            {
                v.useInAmbassador(true);
                v.setUserId(userId);
                v.setPassword(password);
            }
            else
            // not set to be used in ambassador
            {
                v.useInAmbassador(false);
            }
        }

        s_logger.info("Created vendor " + v.toDebugString());
        return v;
    }

    private void setCustomFields(Vendor p_v, NodeList p_sections)
    {
        // clear out the custom fields before resetting them
        p_v.setCustomFields(new Hashtable());

        if (p_sections != null)
        {
            for (int i = 0; i < p_sections.getLength(); i++)
            {
                Element elem = (Element) p_sections.item(i);
                // get the name of the section
                String section = VendorXmlParser.getValue(elem,
                        VendorXmlParser.CUSTOM_NAME);

                CustomForm cf = null;
                try
                {
                    cf = s_vm.getCustomForm();
                }
                catch (Exception e)
                { /* just catch exception will leave as null */
                }
                Hashtable validFields = null;
                if (cf != null)
                {
                    validFields = CustomFormParser.getCustomFields(cf);
                }
                else
                {
                    // just work with an empty hashtable. none of the fields
                    // will
                    // be valid since there isn't a custom form but each invalid
                    // field
                    // will be logged out and written to the vendor's notes
                    // field
                    validFields = new Hashtable();
                }

                // get fields in the section
                NodeList fields = elem
                        .getElementsByTagName(VendorXmlParser.CUSTOM_FIELD);
                for (int fi = 0; fi < fields.getLength(); fi++)
                {
                    Element fElem = (Element) fields.item(fi);
                    // get the field name and value
                    String fieldName = VendorXmlParser.getValue(fElem,
                            VendorXmlParser.CUSTOM_NAME);
                    String value = VendorXmlParser.getValue(fElem,
                            VendorXmlParser.CUSTOM_FIELD_VALUE);
                    if (value != null)
                    {
                        value.toLowerCase();
                    }
                    else
                    {
                        value = "";
                    }

                    // verify the section and field name is a valid name
                    String key = section;
                    if (fieldName != null)
                    {
                        key += ".";
                        key += fieldName;
                    }

                    if (validFields.containsKey(key))
                    {
                        // get the field type
                        String type = (String) validFields.get(key);
                        // if a checkbox change the value to be the exact as the
                        // fieldName
                        if (CustomFormParser.FIELD_TYPE_CHECKBOX.equals(type))
                        {
                            // assumes the type is checkbox if the value is
                            // true/false
                            if (value.equals("true"))
                            {
                                value = fieldName;
                            }
                            else
                            {
                                // if the value equals "false" then no need to
                                // store the value
                                continue;
                            }
                        }
                        try
                        {
                            p_v.addOrUpdateCustomField(key, value);
                        }
                        catch (Exception e)
                        {
                            // if an error add custom fields to the notes area
                            // and keep going
                            p_v.appendNotes("\n\rCustom field "
                                    + key
                                    + " could not be added or updated with the value "
                                    + value + " to the vendor.\n\r");
                        }
                    }
                    else
                    {
                        s_logger.error("Custom field "
                                + key
                                + " is not a valid custom field.  It wasn't added to the vendor.");
                        p_v.appendNotes("\n\rCustom field "
                                + key
                                + " could not be added or updated with the value.  It isn't a valid field or section.");
                    }
                } // for
            } // for
        } // if sections exist
    }

    private void setRoles(Vendor p_vendor, NodeList p_roles)
    {
        // go through each of them and add to the new list
        Set newRoles = new HashSet();
        for (int i = 0; i < p_roles.getLength(); i++)
        {
            Element elem = (Element) p_roles.item(i);
            String sLocaleName = VendorXmlParser.getValue(elem,
                    VendorXmlParser.SOURCE_LOCALE);
            String tLocaleName = VendorXmlParser.getValue(elem,
                    VendorXmlParser.TARGET_LOCALE);
            String activityName = VendorXmlParser.getValue(elem,
                    VendorXmlParser.ACTIVITY);

            try
            {
                Activity a = ServerProxy.getJobHandler().getActivity(
                        activityName);
                LocaleManager lm = ServerProxy.getLocaleManager();
                GlobalSightLocale sLocale = lm.getLocaleByString(sLocaleName);
                GlobalSightLocale tLocale = lm.getLocaleByString(tLocaleName);
                LocalePair lp = lm.getLocalePairBySourceTargetIds(
                        sLocale.getId(), tLocale.getId());

                // now go through the old ones and see if any have the same
                // activity and locale pair. If so copy the old one over (retain
                // rates and ID).
                Set oldRoles = p_vendor.getRoles();
                boolean found = false;
                for (Iterator j = oldRoles.iterator(); !found && j.hasNext();)
                {
                    VendorRole oldVr = (VendorRole) j.next();
                    if (oldVr.getActivity().equals(a)
                            && oldVr.getLocalePair().equals(lp))
                    {
                        newRoles.add(oldVr);
                        found = true;
                    }
                }
                if (!found)
                {
                    VendorRole vr = new VendorRole(a, lp);
                    newRoles.add(vr);
                }
            }
            catch (Exception e)
            {
                // if an error add the role to the notes area and keep going
                p_vendor.appendNotes("\n\rRole with source locale "
                        + sLocaleName + " and target locale " + tLocaleName
                        + " and activity " + activityName
                        + " failed to be added to the vendor.\n\r");
            }
        }
        p_vendor.setRoles(newRoles);
    }

    private void setProjects(Vendor p_vendor, boolean inAllProjects,
            List p_projects) throws WebServiceException
    {
        try
        {
            // projects
            p_vendor.setProjects(null); // clear out all the projects
            if (inAllProjects)
            {
                p_vendor.isInAllProjects(true);
                // so add all projects
                Collection ps = getProjectHandler().getAllProjects();
                for (Iterator psi = ps.iterator(); psi.hasNext();)
                {
                    Project p = (Project) psi.next();
                    p_vendor.addToProject(p);
                }
            }
            else
            {
                p_vendor.isInAllProjects(false);
                // so not in all projects - find the ones that are needed
                // if any projects were specified
                if (p_projects != null)
                {
                    for (Iterator pi = p_projects.iterator(); pi.hasNext();)
                    {
                        Long projectId = (Long) pi.next();
                        Project p = getProjectHandler().getProjectById(
                                projectId.intValue());
                        p_vendor.addToProject(p);
                    }
                }
            }

        }
        catch (Exception e)
        {
            String errMessage = "Failed to retrieve the projects that vendor "
                    + p_vendor.getCustomVendorId()
                    + " should be associated with.";
            s_logger.error(errMessage, e);
            throw new WebServiceException(errMessage);
        }
    }

    private boolean canFieldBeUpdated(FieldSecurity p_fs, String p_fieldName)
    {
        boolean canUpdate = false;
        if (p_fs != null && p_fieldName != null)
        {
            String access = p_fs.get(p_fieldName);
            if (FieldSecurity.SHARED.equals(access))
            {
                canUpdate = true;
            }
        }
        return canUpdate;
    }

    private byte[] getResumeFromFile(String p_filename)
            throws WebServiceException
    {
        try
        {
            return ServerProxy.getNativeFileManager().getBytes(p_filename);
        }
        catch (Exception e)
        {
            String errMessage = "Failed to get the vendor's resume "
                    + p_filename + " from the file system.";
            s_logger.error(errMessage, e);
            throw new WebServiceException(errMessage);
        }
    }

    private String getRolesXml(Vendor p_v, FieldSecurity p_fs)
    {
        StringBuffer sb = new StringBuffer();

        // if the roles are confidential
        if (getSecureValue(p_fs, VendorSecureFields.ROLES, NOT_CONFIDENTIAL)
                .equals(CONFIDENTIAL))
        {
            sb.append("\t<role>");
            sb.append(CONFIDENTIAL);
            sb.append("</role>\r\n");
        }
        else
        {
            Set roles = p_v.getRoles();
            for (Iterator ri = roles.iterator(); ri.hasNext();)
            {
                VendorRole vr = (VendorRole) ri.next();
                LocalePair lp = vr.getLocalePair();
                sb.append("\t<role>\r\n");
                sb.append("\t\t<sourceLocale>")
                        .append(lp.getSource().toString())
                        .append("</sourceLocale>\r\n");
                sb.append("\t\t<targetLocale>")
                        .append(lp.getTarget().toString())
                        .append("</targetLocale>\r\n");
                sb.append("\t\t<activityType>")
                        .append(vr.getActivity().getName())
                        .append("</activityType>\r\n");
                sb.append("\t</role>\r\n");
            }
        }

        return sb.toString();
    }

    /**
     * Get the XML to return for the projects of a vendor.
     */
    private String getProjectsXml(Vendor p_v, FieldSecurity p_fs)
    {
        StringBuffer sb = new StringBuffer();
        // if the projects are confidential
        if (getSecureValue(p_fs, VendorSecureFields.PROJECTS, NOT_CONFIDENTIAL)
                .equals(CONFIDENTIAL))
        {
            sb.append("\t<projects>");
            sb.append(CONFIDENTIAL);
            sb.append("</projects>\r\n");
        }
        else
        {
            boolean inAllProjects = p_v.isInAllProjects();
            sb.append("\t<projects all=\"").append(
                    inAllProjects ? "true\"/>" : "false\">");
            sb.append("\r\n");
            if (!inAllProjects)
            {
                List projects = p_v.getProjects();
                for (Iterator pi = projects.iterator(); pi.hasNext();)
                {
                    Project p = (Project) pi.next();
                    sb.append("\t\t<projectId>").append(p.getId())
                            .append("</projectId>\r\n");
                }
                sb.append("\t</projects>\r\n");
            }
        }
        return sb.toString();
    }

    /**
     * Return the XML string for all the vendors phone numbers.
     */
    private String getPhoneNumbersXml(Vendor p_v, FieldSecurity p_fs)
    {
        StringBuffer sb = new StringBuffer();
        if (p_v.getPhoneNumber(CommunicationInfo.CommunicationType.HOME) != null)
        {
            String homeNumber = p_v
                    .getPhoneNumber(CommunicationInfo.CommunicationType.HOME);
            if (homeNumber != null && homeNumber.length() > 0)
            {
                sb.append("\t<communication>\r\n");
                sb.append("\t\t<communicationType>HOME</communicationType>\r\n");
                sb.append("\t\t<communicationValue>")
                        .append(getSecureValue(p_fs,
                                VendorSecureFields.HOME_PHONE, homeNumber))
                        .append("</communicationValue>\r\n");
                sb.append("\t</communication>\r\n");
            }
        }
        if (p_v.getPhoneNumber(CommunicationInfo.CommunicationType.WORK) != null)
        {
            String workNumber = p_v
                    .getPhoneNumber(CommunicationInfo.CommunicationType.WORK);
            if (workNumber != null && workNumber.length() > 0)
            {
                sb.append("\t<communication>\r\n");
                sb.append("\t\t<communicationType>WORK</communicationType>\r\n");
                sb.append("\t\t<communicationValue>")
                        .append(getSecureValue(p_fs,
                                VendorSecureFields.WORK_PHONE, workNumber))
                        .append("</communicationValue>\r\n");
                sb.append("\t</communication>\r\n");
            }
        }
        if (p_v.getPhoneNumber(CommunicationInfo.CommunicationType.CELL) != null)
        {
            String cellNumber = p_v
                    .getPhoneNumber(CommunicationInfo.CommunicationType.CELL);
            if (cellNumber != null && cellNumber.length() > 0)
            {
                sb.append("\t<communication>\r\n");
                sb.append("\t\t<communicationType>CELL</communicationType>\r\n");
                sb.append("\t\t<communicationValue>")
                        .append(getSecureValue(p_fs,
                                VendorSecureFields.CELL_PHONE, cellNumber))
                        .append("</communicationValue>\r\n");
                sb.append("\t</communication>\r\n");
            }
        }
        if (p_v.getPhoneNumber(CommunicationInfo.CommunicationType.FAX) != null)
        {
            String faxNumber = p_v
                    .getPhoneNumber(CommunicationInfo.CommunicationType.FAX);
            if (faxNumber != null && faxNumber.length() > 0)
            {
                sb.append("\t<communication>\r\n");
                sb.append("\t\t<communicationType>FAX</communicationType>\r\n");
                sb.append("\t\t<communicationValue>")
                        .append(getSecureValue(p_fs, VendorSecureFields.FAX,
                                faxNumber)).append("</communicationValue>\r\n");
                sb.append("\t</communication>\r\n");
            }
        }
        return sb.toString();
    }

    /**
     * Return the XML for a Vendor's custom fields.
     */
    private String getCustomFieldsXml(Vendor p_v, FieldSecurity p_fs)
    {
        StringBuffer sb = new StringBuffer();

        // if there are roles and they are confidential
        if (getSecureValue(p_fs, VendorSecureFields.CUSTOM_FIELDS,
                NOT_CONFIDENTIAL).equals(CONFIDENTIAL))
        {
            sb.append("\t<customFields>");
            sb.append(CONFIDENTIAL);
            sb.append("</customFields>\r\n");
        }
        else
        {
            // custom fields
            Hashtable cfs = p_v.getCustomFields();
            if (cfs != null && cfs.size() > 0)
            {
                // sort the custom fields by the section they are in
                Hashtable bySections = new Hashtable();
                Set keys = cfs.keySet();
                for (Iterator ki = keys.iterator(); ki.hasNext();)
                {
                    String key = (String) ki.next();
                    int index = key.indexOf('.');
                    // if there is no "." then the index is the full length
                    if (index == -1)
                    {
                        index = key.length();
                    }
                    String section = key.substring(0, index);
                    List fieldList = null;
                    if (bySections.containsKey(section))
                    {
                        fieldList = (List) bySections.get(section);
                    }
                    else
                    {
                        fieldList = new ArrayList();
                    }
                    fieldList.add(cfs.get(key));
                    bySections.put(section, fieldList);
                }

                sb.append("\t<customFields>\r\n");

                Set keys2 = bySections.keySet();
                for (Iterator ki2 = keys2.iterator(); ki2.hasNext();)
                {
                    String sectionKey = (String) ki2.next();
                    sb.append("\t\t<section>\r\n");
                    sb.append("\t\t<name>");
                    sb.append(sectionKey);
                    sb.append("</name>\r\n");
                    List l = (List) bySections.get(sectionKey);
                    for (Iterator fi = l.iterator(); fi.hasNext();)
                    {
                        CustomField cf = (CustomField) fi.next();
                        sb.append("\t\t<field>\r\n");
                        if (cf.getFieldName() != null)
                        {
                            sb.append("\t\t\t<name>");
                            sb.append(cf.getFieldName());
                            sb.append("</name>\r\n");
                        }
                        sb.append("\t\t\t<value>");
                        // if they are the same then assume it is a checkbox
                        // tbd - probably should check the type to ensure a
                        // checkbox
                        String value = cf.getValue();
                        if (value == null)
                        {
                            value = "";
                        }
                        else if (value != null
                                && value.equals(cf.getFieldName()))
                        {
                            value = "true";
                        }
                        sb.append(value);
                        sb.append("</value>\r\n");
                        sb.append("\t\t</field>\r\n");
                    }
                    sb.append("\t\t</section>\r\n");
                }
                sb.append("\t</customFields>\r\n");
            }
        }
        return sb.toString();
    }

    private ProjectHandler getProjectHandler() throws WebServiceException
    {
        try
        {
            if (s_ph == null)
            {
                s_ph = ServerProxy.getProjectHandler();
            }
        }
        catch (Exception e)
        {
            String errMessage = "Couldn't find the project handler component.";
            s_logger.error(errMessage, e);
            throw new WebServiceException(errMessage);
        }
        return s_ph;
    }

    /**
     * Return the value for the specified field according to the field security.
     */
    private String getSecureValue(FieldSecurity p_fs, String p_fieldName,
            String p_fieldValue)
    {
        String access = p_fs.get(p_fieldName);
        if (FieldSecurity.HIDDEN.equals(access))
        {
            return "CONFIDENTIAL";
        }
        else
        {
            return p_fieldValue;
        }
    }
}
