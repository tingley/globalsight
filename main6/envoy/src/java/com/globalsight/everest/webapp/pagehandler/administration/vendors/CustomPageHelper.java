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

package com.globalsight.everest.webapp.pagehandler.administration.vendors;

import org.apache.log4j.Logger;

// globalsight
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.localemgr.LocaleManager;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.resourcebundle.SystemResourceBundle;
import com.globalsight.everest.customform.CustomForm;
import com.globalsight.everest.customform.CustomFormParser;
import com.globalsight.everest.customform.CustomField;

// DOM
import org.apache.xerces.parsers.DOMParser;
import org.xml.sax.InputSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

// Core Java classes
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.StringTokenizer;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;


/**
 *  Helper class for parsing XML for the custom form
 */
public class CustomPageHelper implements VendorConstants
{
    private static final Logger CATEGORY =
        Logger.getLogger(
            CustomPageHelper.class);
    
    /**
     * Get the custom form. 
     */
    public static CustomForm getCustomForm()
    {
        try
        {
            return ServerProxy.getVendorManagement().getCustomForm();
        }
        catch (Exception e)
        {
            return null;
        }
    }

    /**
     * Save the custom form in the database, and write out a property
     * file under properties/vendormanagement.
     *
     */
    public static void saveCustomForm(HttpServletRequest request)
        throws EnvoyServletException
    {
        // Save the xml representing the form
        String xml = (String)request.getParameter("xml");
        String pageTitle = (String)request.getParameter("pageTitle");
        try
        {
            CustomForm form =  getCustomForm();
            if (form == null)
            {
                form = new CustomForm();
            }
            String removedFields = (String)request.getParameter("removedFields");
            StringTokenizer tok = new StringTokenizer(removedFields, ",,");
            ArrayList list = new ArrayList();
            while (tok.hasMoreTokens())
            {
                list.add(tok.nextElement());
            }
          
            form.update(pageTitle, getLocale(request), getUserId(request), xml);
            ServerProxy.getVendorManagement().updateCustomForm(form,
                                                               list);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }

        // write out property file
        String properties = (String)request.getParameter("properties");
        String DOCROOT = "/";

        try
        {
            SystemConfiguration sc = SystemConfiguration.getInstance();

            String root = sc.getStringParameter(
                SystemConfiguration.WEB_SERVER_DOC_ROOT);

            if (!(root.endsWith("/") || root.endsWith("\\")))
            {
                root = root + "/";
            }

            DOCROOT = root + "../lib/classes/";
            File dir = new File(DOCROOT);
            dir.mkdirs();
            File outFile = new File (dir, VendorConstants.CUSTOM_FORM_PROPERTIES +
                                     ".properties");

            FileWriter fr = new FileWriter(outFile);
            fr.write(properties, 0, properties.length());
            fr.close();
        }
        catch (Exception e)
        {
            CATEGORY.error("Error writing custom form property file.");
        }
    }

    /**
     * Get the XML, parse it and return handle to the DOM
     */
    public static Document getDocument(CustomForm form)
    {
        return CustomFormParser.getDocument(form);
    }
    
    public static ArrayList getCustomFieldNames()
    {
        Hashtable fields = 
            CustomFormParser.getCustomFields(getCustomForm());
        Set fieldValues = fields.keySet();
        return new ArrayList(fieldValues);
    }

    /**
     * Return the page title.
     */
    public static String getPageTitle()
        throws EnvoyServletException
    {
        try {
            CustomForm form = ServerProxy.getVendorManagement().getCustomForm();
            if (form != null)
            {
                return form.getPageName();
            }
            return null;
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    public static String getPageContent(HttpSession session,
                                        Hashtable customFields, String access)
    {
        return getPageContent(session, customFields, access, false);
    }

    /**
     * Sample XML:
     *      <section>
     *          <name>section name</name>
     *          <field>
     *              <label>foo</label>
     *              <type>Checkbox</type>
     *          </field>
     *          <field>
     *              <label>bar</label>
     *              <type>Text</type>
     *          </field>
     *      </section>
     *
     * Return the html for displaying this XML has an input form.
     */
    public static String getPageContent(HttpSession session,
                                        Hashtable customFields,
                                        String access,
                                        boolean viewOnly)
    {
        ResourceBundle bundle = PageHandler.getBundle(session);
        String confidential = bundle.getString("lb_confidential");

        StringBuffer html = new StringBuffer();
        if ("hidden".equals(access) && !viewOnly)
        {
            // User is not allowed to see custom field values
            html.append("<span class='confidential'>[" + confidential + "]</span>");
        }
        else
        {
            ResourceBundle custombundle = getVendorBundle(session);
            Document doc = CustomFormParser.getDocument(getCustomForm());
            if (doc == null)
                return null;
            getSections(custombundle, doc, html, customFields, access,
                       confidential, viewOnly);
        }
        return html.toString();
    }

    /**
     * Return the html for the section table in the custom page designer
     */
    public static String getSectionsForDesigner(Document doc, ResourceBundle bundle)
    {
        StringBuffer html = new StringBuffer();
        // table heading
        html.append("<table id='sectionTable' border='0' cellspacing='0' cellpadding='5' class='list' width ='60%'>\n");
        html.append("<tbody id='sectionTableBody'>\n");
        html.append("<tr class='tableHeadingBasic'>\n");
        html.append("<td width='2%'>&nbsp;</td>");
        html.append("<td>" + bundle.getString("lb_name") +  "</td>\n");
        html.append("</tr>\n");
        html.append("<tr><td colspan='2' style='standardText'>");
        html.append("<div id='emptySectionTable' class='standardText' style='display:none'>");
        html.append("&nbsp;" + bundle.getString("jsmsg_custom_page_no_sections") + "</div></tr>");

        // table data
        NodeList sections = 
            doc.getElementsByTagName(CustomFormParser.SECTION);
        for (int i=0; i < sections.getLength(); i++)
        {
            Node section = sections.item(i);
            NodeList children = section.getChildNodes();
            for (int j=0; j < children.getLength(); j++)
            {
                Node item = children.item(j);
                if (item.getNodeName().equals(CustomFormParser.SECTION_NAME))
                {
                    String sectionLineColor = (i%2 == 0) ? "#FFFFFF" : "#EEEEEE";
                    html.append("<tr id=" + i + " bgcolor=" + sectionLineColor + ">\n"); 
                    html.append("<td>");
                    html.append("<input type='radio' name='radioBtn' value='");
                    html.append(i + "' onclick='javascript:enableFields()'");
                    if (i == 0)
                        html.append(" checked ");
                    html.append(">");
                    html.append("</td>\n");
                    html.append("<td id=section" + i + ">");
                    html.append(item.getFirstChild().getNodeValue());
                    html.append("</td></tr>\n");
                }
            }
        }
        html.append("</tbody>\n</table>\n");
        return html.toString();
    }

    /**
     * Return the html for the field table in the custom page designer
     */
    public static String getFieldsForDesigner(Document doc, ResourceBundle bundle)
    {
        StringBuffer html = new StringBuffer();

        // Get sections because each section has a fields table
        NodeList sections = doc.getElementsByTagName(CustomFormParser.SECTION);
        for (int i=0; i < sections.getLength(); i++)
        {
            // table heading
            html.append("<table id='table" + i + "' border='0' cellspacing='0' cellpadding='5' class='list' ");
            if (i == 0)
                html.append("style='display:block'>\n");
            else
                html.append("style='display:none'>\n");
            html.append("<tbody id='tbody" + i +"' >");
            html.append("<tr class='tableHeadingBasic'>\n");
            html.append("<td width='2%'>&nbsp;</td><td>");
            html.append(bundle.getString("lb_name"));
            html.append("</td><td>" + bundle.getString("lb_type") + "</td></tr>");
            Node section = sections.item(i);
            NodeList fields = section.getChildNodes();
            for (int j=0; j < fields.getLength(); j++)
            {
                Node field = fields.item(j);
                if (field.getNodeName().equals(CustomFormParser.SECTION_NAME)) continue;

                String id = i + "_" + j;
                String color = (j%2 == 0) ? "#EEEEEE" : "#FFFFFF";
                html.append("<tr id=tr" + id + " bgcolor=");
                html.append(color + " class='standardText'>\n");
                html.append("<td>");
                html.append("<input type='radio' name='radioBtn2' value='" + id +
                            "' onclick='javascript:updateFieldInput()'>");
                html.append("</td>\n");
                html.append("<td id=label" + id + " >");
                html.append(field.getFirstChild().getFirstChild().getNodeValue());
                html.append("</td>\n");
                html.append("<td id=type" + id + " >");
                html.append(field.getLastChild().getFirstChild().getNodeValue());
                html.append("</td>\n");
            }
            html.append("</tr>\n");
            html.append("</tbody></table>\n");
        }
        return html.toString();
    }

    /**
     * Remove custom form 
     * <p>
     * @exception EnvoyServletException
     */
    public static void removeForm()
        throws EnvoyServletException
    {
        try
        {
            ServerProxy.getVendorManagement().removeCustomForm();
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }


    }
   
    private static ResourceBundle getVendorBundle(HttpSession session)
    {
        ResourceBundle rb;
        SystemResourceBundle srb = SystemResourceBundle.getInstance();

        try {
            // if session has a valid locale, use it
            if (session!= null && session.getAttribute(WebAppConstants.UILOCALE) != null)
            {
                    rb = srb.getResourceBundle(
                        VendorConstants.CUSTOM_FORM_PROPERTIES,
                        (Locale)session.getAttribute(WebAppConstants.UILOCALE));
            }
            // otherwise, use default locale
            else
            {
                rb = srb.getResourceBundle(
                    "vendormanagement/" + VendorConstants.CUSTOM_FORM_PROPERTIES,
                    Locale.getDefault());
            }
        }
        catch (java.util.MissingResourceException e)
        {
            rb = null;
        }
        return rb;
    }

    private static String getKey(ResourceBundle bundle, String key, String defaultValue)
    {
        if (bundle != null)
        {
            try
            {
                return bundle.getString(key.replace(' ', '_'));
            }
            catch (java.util.MissingResourceException e)
            {
                return defaultValue;
            }
        }
        return defaultValue;
    }


    /**
     * Return the html for the editable custom page.
     */
    private static void getSections(ResourceBundle bundle, Document doc,
                                    StringBuffer html, Hashtable customFields,
                                    String access, String confidential, boolean viewOnly)
    {
        String sectionName = null;

        NodeList sections = 
            doc.getElementsByTagName(CustomFormParser.SECTION);
        for (int i=0; i < sections.getLength(); i++)
        {
            Node section = sections.item(i);
            NodeList children = section.getChildNodes();
            for (int j=0; j < children.getLength(); j++)
            {
                Node item = children.item(j);
                if (item.getNodeName().equals(CustomFormParser.SECTION_NAME))
                {
                    if (viewOnly)
                    {
                        
                        html.append("<table cellspacing='0' cellpadding='1' border='0' class='detailText' width='100%'>\n<tr><td bgcolor='D6CFB2'>");
                        html.append("<table cellpadding=4 cellspacing=0 border=0 class=detailText bgcolor='WHITE' width='100%'>\n");
                        html.append("<tr valign='TOP'>\n");
                        html.append("<td style='background:D6CFB2; font-weight:bold; font-size:larger' colspan=2>\n");
                        
                    }
                    else
                    {
                        html.append("<tr><td style='background:D6CFB2; font-weight:bold; font-size:larger' colspan=3>");
                    }
                    sectionName = item.getFirstChild().getNodeValue();
                    html.append(getKey(bundle, sectionName, sectionName));
                    html.append("</td></tr>");
                    if (viewOnly && "hidden".equals(access))
                    {
                        html.append("<tr><td class='standardTextBold'><span class='confidential'>[" + confidential + "]</span></td></tr>");
                    }
                }
                else if (item.getNodeName().equals(CustomFormParser.FIELD))
                {
                    if ("hidden".equals(access) && viewOnly)
                    {
                        continue;
                    }
                    getField(bundle, sectionName, item, html, customFields, access,
                             viewOnly);
                }
            }
            if (viewOnly)
            {
                html.append("</table></td></tr></table><p>");
            }
        }
    }


    /**
     * Generate the html for the field row.
     *
     * @param bundle - for retrieving localized label
     * @param sectionName - the key for a label is sectionName.label
     * @param field - the DOM node containing field info
     * @param html - the html generated so far
     */
    private static void getField(ResourceBundle bundle, String sectionName,
                                 Node field, StringBuffer html,
                                 Hashtable customFields, String access,
                                 boolean viewOnly)
    {
        String label = null;
        String name = null;
        String radioGroup = null;

        NodeList children = field.getChildNodes();
        html.append("<tr>");
        for (int i=0; i < children.getLength(); i++)
        {
            Node item = children.item(i);
            if (item.getNodeName().equals(CustomFormParser.FIELD_LABEL))
            {
                if (viewOnly)
                    html.append("<td class='standardText' width='25%'>");
                else
                    html.append("<td class='standardText'>");
                name = sectionName + "." + item.getFirstChild().getNodeValue();
                radioGroup = sectionName;
                label = getKey(bundle, name, item.getFirstChild().getNodeValue());
                html.append(label + ":");
            }
            else if (item.getNodeName().equals(CustomFormParser.FIELD_TYPE))
            {
                if (viewOnly)
                    html.append("<td class='standardTextBold'>");
                else
                    html.append("<td class='standardText'>");
                String value = "";
                CustomField customField = (CustomField)customFields.get(name);
                if (customField != null)
                {
                    value = customField.getValue();
                    if (value == null) value = "";
                }
                if (item.getFirstChild().getNodeValue().equals(
                                                CustomFormParser.FIELD_TYPE_TEXT))
                {
                    if ("shared".equals(access))
                    {
                        if (viewOnly)
                        {
                            html.append(value);
                        }
                        else
                        {
                            String encoded = encodeFieldName(name);
                            html.append("<input type='text' name=\"" + encoded +
                                         "\" value='" + value + "'>");
                        }
                    }
                    else
                    {
                        html.append(value);
                    }
                }
                else if (item.getFirstChild().getNodeValue().equals(
                                                CustomFormParser.FIELD_TYPE_CHECKBOX))
                {
                    if ("shared".equals(access))
                    {
                        if (viewOnly)
                        {
                            if (!value.equals(""))
                            {
                                html.append(" X ");
                            }
                        }
                        else
                        {
                            String encoded = encodeFieldName(name);
                            html.append("<input type='checkbox' name=\"" + encoded +
                                         "\" value='" + label + "'");
                            if (!value.equals(""))
                                html.append(" checked ");
                            else
                                html.append(">");
                        }
                    }
                    else
                    {
                        if (!value.equals(""))
                        {
                            html.append("X");
                        }
                    }
                }
                else
                {
                    // Radio button.  Look up the section name in the customFields
                    // hashtable to see if it's set to this label name.
                    String radioValue = "";
                    CustomField secfield = (CustomField)customFields.get(sectionName);
                    if (secfield != null)
                         radioValue = secfield.getValue();
                    // Radio button
                    if ("shared".equals(access))
                    {
                        if (viewOnly)
                        {
                            if (label.equals(radioValue))
                            {
                                html.append(" X ");
                            }
                        }
                        else
                        {
                            String encoded = encodeFieldName(radioGroup);
                            html.append("<input type='radio' name=\"" + encoded +
                                         "\" value='" + label + "'");
                            if (label.equals(radioValue))
                                html.append(" checked ");
                            else
                                html.append(">");
                        }
                    }
                    else
                    {
                        if (label.equals(radioValue))
                        {
                            html.append("X");
                        }
                    }
                }
            }
            html.append("</td>");
        }
        html.append("</tr>");
    }

    private static String getUserId(HttpServletRequest request)
    {
        HttpSession session = request.getSession(false);
        SessionManager sessionMgr =
            (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
        User user = (User)sessionMgr.getAttribute(WebAppConstants.USER);
        return user.getUserId();
    }

    private static GlobalSightLocale getLocale(HttpServletRequest request)
        throws EnvoyServletException
    {
        try
        {
            String locale = (String)request.getParameter("uiLocale");
            return ServerProxy.getLocaleManager().getLocaleByString(locale);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    private static String encodeFieldName(String field)
    {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < field.length(); i++)
        {
            if (field.charAt(i) == '"')
                buf.append("&quot;");
            buf.append(field.charAt(i));
        }
        return buf.toString();
    }
}
