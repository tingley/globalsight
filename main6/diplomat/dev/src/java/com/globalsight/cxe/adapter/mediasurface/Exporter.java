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
package com.globalsight.cxe.adapter.mediasurface;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;

import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.globalsight.cxe.adapter.BaseAdapter;
import com.globalsight.cxe.message.CxeMessage;
import com.globalsight.cxe.message.CxeMessageType;
import com.globalsight.cxe.message.MessageDataReader;
import com.globalsight.cxe.util.EventFlowXmlParser;
import com.globalsight.diplomat.util.Logger;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;
import com.mediasurface.client.IHost;
import com.mediasurface.client.IItem;
import com.mediasurface.client.ILocale;
import com.mediasurface.client.ISite;
import com.mediasurface.client.IType;
import com.mediasurface.client.Mediasurface;
import com.mediasurface.datatypes.ItemKey;
import com.mediasurface.datatypes.SecurityContextHandle;

/**
 * Helper class used by the MediasurfaceAdapter for exporting
 */
public class Exporter
{
    // ////////////////////////////////////
    // Private Constants //
    // ////////////////////////////////////
    private static final char UNIX_SEPARATOR = '/';
    private static final char WIN_SEPARATOR = '\\';
    private static final String MSG_CREATE = "Created by GlobalSight";
    private static final String MSG_EDIT = "Edited by GlobalSight";

    // ////////////////////////////////////
    // Private Members //
    // ////////////////////////////////////
    private CxeMessage m_cxeMessage;
    private org.apache.log4j.Logger m_logger;
    private String m_targetLocale = null; // target locale name
    private String m_sourceLocale = null; // target locale name
    private ILocale m_targetILocale = null; // the mediasurface ILocale object
    private EventFlowXmlParser m_parser = null;
    private Mediasurface m_mediaSurface = null;
    private SecurityContextHandle m_securityContextHandle = null;
    private IItem m_item = null; // the mediasurface item
    private IItem m_targetItem = null; // the mediasurface target item
    private int m_itemKey = 0;
    private ContentServer m_contentServer = null;
    private String m_username = null;
    private String m_password = null;
    private boolean m_isFinalExport = false;

    // ////////////////////////////////////
    // Constructor //
    // ////////////////////////////////////

    /**
     * Creates an Exporter object
     * 
     * @param p_cxeMessage
     *            a CxeMessage to work from
     */
    Exporter(CxeMessage p_cxeMessage, org.apache.log4j.Logger p_logger)
            throws GeneralException
    {
        m_cxeMessage = p_cxeMessage;
        m_logger = p_logger;
        m_parser = new EventFlowXmlParser();
    }

    /**
     * private constructor for testing
     */
    private Exporter()
    {
        m_logger = org.apache.log4j.Logger.getLogger(Exporter.class);
        m_targetLocale = "fr_FR";
        m_username = "msadmin";
        m_password = "password";
        m_contentServer = new ContentServer("iiop://tempest/Mediasurface",
                "Staging Server", 8080);
    }

    // ////////////////////////////////////
    // Package Private Methods //
    // ////////////////////////////////////

    /**
     * Actually performs the write-back to mediasurface
     * 
     * @return New CxeMessage result
     */
    CxeMessage export()
    {
        CxeMessage exportStatusMsg;
        try
        {
            Logger.writeDebugFile("msta_ef.xml", m_cxeMessage.getEventFlowXml());
            parseEventFlowXml();
            connect();
            setTargetILocale();
            writeTargetItem();
            exportStatusMsg = makeExportSuccessMessage();
            BaseAdapter.preserveOriginalFileContent(
                    m_cxeMessage.getMessageData(),
                    exportStatusMsg.getParameters());
            m_cxeMessage.setDeleteMessageData(true);
        }
        catch (MediasurfaceAdapterException fsae)
        {
            exportStatusMsg = makeExportErrorMessage(fsae);
        }
        catch (Exception e)
        {
            m_logger.error("Could not write file back.", e);
            String errorArgs[] = new String[1];
            errorArgs[0] = m_logger.getName();
            MediasurfaceAdapterException fsae = new MediasurfaceAdapterException(
                    "CxeInternalEx", errorArgs, e);
            exportStatusMsg = makeExportErrorMessage(fsae);
        }

        return exportStatusMsg;
    }

    // ////////////////////////////////////
    // Private Methods //
    // ////////////////////////////////////

    /**
     * Prepares the export success status message for sending to the next
     * adapter
     * 
     * @param p_exceptionMsg
     *            an error/status string
     * @param p_finalFile
     * @return CxeMessage
     */
    private CxeMessage makeExportSuccessMessage() throws Exception
    {
        CxeMessageType type = CxeMessageType
                .getCxeMessageType(CxeMessageType.CXE_EXPORT_STATUS_EVENT);
        CxeMessage newCxeMessage = new CxeMessage(type);
        newCxeMessage.setEventFlowXml(m_cxeMessage.getEventFlowXml());
        HashMap newParams = m_cxeMessage.getParameters();
        newParams.put("PreviewUrlXml", makePreviewUrlXml());
        newParams.put("ExportedTime", new Long(System.currentTimeMillis()));
        newParams.put("Exception", null);

        // copy parameters that were preset by other adapters
        // (office adapter calls this code once per batch)
        // (quark and frame call this code once per file)
        String isComp = (String) m_cxeMessage.getParameters().get(
                "IsComponentPage");
        if (isComp != null)
        {
            newParams.put("IsComponentPage", isComp); // copy to new params
        }
        String absoluteExportPath = (String) m_cxeMessage.getParameters().get(
                "AbsoluteExportPath");
        if (absoluteExportPath != null)
        {
            newParams.put("AbsoluteExportPath", absoluteExportPath); // copy to
                                                                     // new
                                                                     // params
        }

        // for all other files, we set the absolute path here
        if (isComp == null || isComp.equalsIgnoreCase("false"))
        {
            newParams.put("AbsoluteExportPath", m_targetItem.getUrl());
        }

        newCxeMessage.setParameters(newParams);
        return newCxeMessage;
    }

    /**
     * Prepares the export error message for sending to the next adapter
     * 
     * @param p_fsae
     *            a File System Adapter Exception
     * @return CxeMessage
     */
    private CxeMessage makeExportErrorMessage(
            MediasurfaceAdapterException p_fsae)
    {
        CxeMessageType type = CxeMessageType
                .getCxeMessageType(CxeMessageType.CXE_EXPORT_STATUS_EVENT);
        CxeMessage newCxeMessage = new CxeMessage(type);
        newCxeMessage.setEventFlowXml(m_cxeMessage.getEventFlowXml());
        HashMap newParams = new HashMap();
        newParams.put("Exception", p_fsae);
        newCxeMessage.setParameters(newParams);
        return newCxeMessage;
    }

    /**
     * Reads the Event Flow Xml for some needed values
     * 
     * @exception Exception
     */
    private void parseEventFlowXml() throws Exception
    {
        m_parser.parse(m_cxeMessage.getEventFlowXml());
        Element msCategory = m_parser.getCategory("Mediasurface");

        String values[] = m_parser.getCategoryDaValue(msCategory,
                "OriginalItemKey");
        m_itemKey = Integer.parseInt(values[0]);

        String url = m_parser
                .getCategoryDaValue(msCategory, "ContentServerUrl")[0];
        String name = m_parser.getCategoryDaValue(msCategory,
                "ContentServerName")[0];
        String port = m_parser.getCategoryDaValue(msCategory,
                "ContentServerPort")[0];
        m_contentServer = new ContentServer(url, name, Integer.parseInt(port));
        m_username = m_parser
                .getCategoryDaValue(msCategory, "MediasurfaceUser")[0];
        m_password = m_parser.getCategoryDaValue(msCategory,
                "MediasurfacePassword")[0];
        m_targetLocale = m_parser.getTargetLocale();
        m_sourceLocale = m_parser.getSourceLocale();

        Boolean isFinalExport = (Boolean) m_cxeMessage.getParameters().get(
                "IsFinalExport");
        m_isFinalExport = isFinalExport.booleanValue();
        m_logger.debug("m_isFinalExport=" + m_isFinalExport);
    }

    /**
     * Writes the target item back to the CMS
     * 
     * @exception Exception
     */
    private void writeTargetItem() throws Exception
    {
        // get the sourceitem
        m_item = m_mediaSurface.getItem(m_securityContextHandle, new ItemKey(
                m_itemKey));
        ArrayList sourceParents = getItemParents(m_item);

        // construct a URL and see if the target Item exists at that URL
        StringBuffer targetPath = new StringBuffer("/");
        targetPath.append(m_targetLocale).append("/");
        // skip the top parent
        for (int i = 1; i < sourceParents.size(); i++)
        {
            IItem parent = (IItem) sourceParents.get(i);
            String simpleName = parent.getSimpleName();
            targetPath.append(simpleName);
            targetPath.append("/");
        }
        targetPath.append(m_item.getSimpleName());
        m_logger.debug("target Path should be: " + targetPath.toString());
        m_targetItem = getItemByPath(targetPath.toString());
        if (m_targetItem == null)
        {
            m_logger.debug("item does not exist. Making new one.");
            makeNewTargetItem(sourceParents);
        }
        else
        {
            m_logger.debug("item exists. " + m_targetItem.getUrl());
            if (m_item.getMediaType().equals("application/x-mediasurface"))
            {
                m_logger.debug("Existing item is a ms type, filling fields.");
                m_targetItem.edit();
                fillTargetItemFields();
            }
            else
            {
                // set binary content
                long size = m_cxeMessage.getMessageData().getSize();
                m_logger.debug("Setting binary content of size " + size);
                m_targetItem.edit();
                m_targetItem.setBinaryContent(m_cxeMessage.getMessageData()
                        .getInputStream(), size);
            }
            m_targetItem.setAuditDescription(MSG_EDIT);
            m_targetItem.saveChanges();

        }
    }

    /**
     * Nicer method to lookup items from Mediasurface since it throws Exceptions
     * instead of returning null
     * 
     * @param p_url
     *            url
     * @return IItem or null
     */
    private IItem getItemByUrl(String p_url)
    {
        IItem item = null;
        try
        {
            item = m_mediaSurface.getItem(m_securityContextHandle, p_url);
        }
        catch (Exception e)
        {
        }
        return item;
    }

    /**
     * Nicer method to lookup items from Mediasurface since it throws Exceptions
     * instead of returning null
     * 
     * @param p_path
     *            path
     * @return IItem or null
     */
    private IItem getItemByPath(String p_path)
    {
        IItem item = null;
        try
        {
            IHost host = m_item.getHost();
            item = host.getItem(p_path);
        }
        catch (Exception e)
        {
        }
        return item;
    }

    /**
     * Creates a new item to be a copy of the source item.
     * 
     * @return new Target item
     * @exception Exception
     */
    private void makeNewTargetItem(ArrayList p_sourceParents) throws Exception
    {
        m_logger.debug("Making structure for new item.");
        IItem parent = makeStructureForItem(p_sourceParents);

        m_logger.debug("Now making the item itself...");
        ISite site = m_item.getSite();
        IHost host = site.getDefaultHost();
        IType type = m_item.getType();
        String name = m_item.getFullName();
        m_targetItem = m_mediaSurface.createItem(m_securityContextHandle,
                type.getKey(), name, host.getKey(), site.getKey());

        if (m_item.getMediaType().equals("application/x-mediasurface"))
        {
            fillTargetItemFields();
        }
        else
        {
            // set binary content
            long size = m_cxeMessage.getMessageData().getSize();
            m_logger.debug("Creating binary content of size " + size);
            m_targetItem.setBinaryContent(m_cxeMessage.getMessageData()
                    .getInputStream(), size);
        }
        m_targetItem.setAuditDescription(MSG_CREATE);
        m_targetItem.saveChanges();
        m_targetItem.edit();
        m_logger.debug("Setting simple name and locale");
        m_targetItem.setSimpleName(m_item.getSimpleName());
        m_targetItem.setLocale(m_targetILocale.getKey());
        m_logger.debug("Binding child to final parent.");
        m_targetItem.bindToItem(parent.getKey(), true, null);
        m_targetItem.setAuditDescription(MSG_EDIT);
        m_logger.debug("Saving!!!!");
        m_targetItem.saveChanges();

        // if this is final export, then sign off the item
        if (m_isFinalExport == true)
        {
            m_logger.info("Signing off item: " + m_targetItem.getUrl());
            try
            {
                m_targetItem.signOff("Signed-off by GlobalSight.");
            }
            catch (Exception e)
            {
                m_logger.warn("Could not sign off item '"
                        + m_targetItem.getUrl() + "' : " + e.getMessage());
            }
        }
        else
            m_logger.debug("Not signing off item because it's interim export.");
    }

    /**
     * Returns an arraylist of the item's parents as IItem objects. parents[0]
     * is the top level parent.
     * 
     * @param p_item
     *            -- an IItem
     * @return ArrayList
     * @exception Exception
     */
    private ArrayList getItemParents(IItem p_item) throws Exception
    {
        ArrayList parents = new ArrayList();
        boolean hasParent = true;
        IItem child = p_item;
        while (hasParent)
        {
            IItem parent = child.getParent();
            if (parent == null)
                hasParent = false;
            else
            {
                parents.add(parent);
                child = parent;
            }
        }
        Collections.reverse(parents);
        return parents;
    }

    /**
     * Looks up the Mediasurface ILocale that corresponds to the target locale
     * and sets m_targetILocale
     */
    private void setTargetILocale() throws Exception
    {
        ILocale locales[] = m_mediaSurface.getLocales(m_securityContextHandle);
        Locale targetLocale = GlobalSightLocale
                .makeLocaleFromString(m_targetLocale);
        m_targetILocale = MediasurfaceConnection.getMediasurfaceLocale(
                targetLocale, m_mediaSurface, m_securityContextHandle);
    }

    private void fillTargetItemFields() throws Exception
    {
        // now iterate through the items in the MediasurfaceXML
        String msXml = MessageDataReader.readString(m_cxeMessage
                .getMessageData());
        StringReader sr = new StringReader(msXml);
        InputSource is = new InputSource(sr);
        DOMParser parser = new DOMParser();
        parser.setFeature("http://xml.org/sax/features/validation", false);
        parser.parse(is);
        Document document = parser.getDocument();
        Element rootElement = document.getDocumentElement();
        NodeList nl = rootElement.getElementsByTagName("Field");
        for (int i = 0; i < nl.getLength(); i++)
        {
            Element e = (Element) nl.item(i);
            String fieldName = e.getAttribute("name");
            String value = e.getFirstChild().getNodeValue();
            m_logger.debug("Setting field: " + fieldName + "\r\nValue=" + value);
            if (fieldName.toLowerCase().indexOf("date") > -1)
            {
                // for now just re-use the item's date instead of trying to
                // parse
                // the date from the XML
                m_targetItem.setDateFieldValue(fieldName,
                        m_item.getDateFieldValue(fieldName));
            }
            else
            {
                m_targetItem.setFieldValue(fieldName, value);
            }
        }
    }

    private void connect() throws Exception
    {
        m_mediaSurface = MediasurfaceConnection.getConnection(m_contentServer);
        m_securityContextHandle = m_mediaSurface.login(m_username, m_password);
        m_mediaSurface.setAdminMode(m_securityContextHandle, true);
    }

    /**
     * Creates the top level target item and binds it to the host, but not as
     * the root.
     * 
     * @param p_sourceTopLevelItem
     *            the source language top level item
     */
    private IItem makeTargetTopLevelItem(IItem p_sourceTopLevelItem)
            throws Exception
    {
        IItem targetTopLevelItem = m_mediaSurface.createItem(
                m_securityContextHandle, p_sourceTopLevelItem.getType()
                        .getKey(), m_targetLocale, p_sourceTopLevelItem
                        .getHost().getKey(), p_sourceTopLevelItem.getSite()
                        .getKey());

        m_logger.debug("Copying fields from source top level.");
        copyItem(p_sourceTopLevelItem, targetTopLevelItem);
        m_logger.debug("Created target top level item, binding to host.");
        targetTopLevelItem.bindToHost(p_sourceTopLevelItem.getHost(), false);
        m_logger.debug("Bound to host.");
        m_logger.debug("Saving changes.");
        targetTopLevelItem.setAuditDescription(MSG_CREATE);
        targetTopLevelItem.saveChanges();
        targetTopLevelItem.edit();
        m_logger.debug("Setting simple name and locale for: " + m_targetLocale);

        try
        {
            targetTopLevelItem.setSimpleName(m_targetLocale);
        }
        catch (Exception e)
        {
            m_logger.debug("Could not create object with simple name. "
                    + e.getMessage());
        }
        targetTopLevelItem.setLocale(m_targetILocale.getKey());
        targetTopLevelItem.setAuditDescription(MSG_EDIT);
        targetTopLevelItem.saveChanges();
        if (m_logger.isDebugEnabled())
        {
            m_logger.debug("Created item: " + targetTopLevelItem.getUrl());            
        }
        return targetTopLevelItem;
    }

    /**
     * Copy fields and metadata from source to target item. This assumes the
     * target item is already being edited.
     * 
     * @param p_source
     *            source item
     * @param p_target
     *            target item
     */
    private void copyItem(IItem p_source, IItem p_target) throws Exception
    {
        String[] fieldNames = p_source.getFieldNames();
        for (int i = 0; i < fieldNames.length; i++)
        {
            String fieldName = fieldNames[i];
            m_logger.debug("Copying field: " + fieldName);
            if (fieldName.toLowerCase().indexOf("date") > -1)
            {
                p_target.setDateFieldValue(fieldName,
                        p_source.getDateFieldValue(fieldName));
            }
            else
            {
                String value = p_source.getFieldValue(fieldName);
                if (value == null)
                    value = "";
                p_target.setFieldValue(fieldName, value);
            }
        }
        String comment = p_source.getComment();
        if (comment == null)
            comment = "";
        p_target.setComment(comment);
    }

    // returns final parent
    private IItem makeStructureForItem(ArrayList p_sourceParents)
            throws Exception
    {
        // see if the locale specific item (HomePage, Folder, etc.) exists
        // and create it if not
        String targetTopPath = "/" + m_targetLocale + "/";
        m_logger.debug("target top path is " + targetTopPath);
        // see if top item exists, if not create and bind to host
        IItem topItem = getItemByPath(targetTopPath);
        if (topItem == null)
        {
            m_logger.debug("top item does not exist. Creating it.");
            topItem = makeTargetTopLevelItem((IItem) p_sourceParents.get(0));
        }
        else
        {
            m_logger.debug("top item: " + topItem.getSimpleName() + " exists.");
        }

        StringBuffer path = new StringBuffer(topItem.getPath());

        // now go and create all the items if they don't already exist
        // skipping toplevel parent
        IItem parent = topItem;
        for (int i = 1; i < p_sourceParents.size(); i++)
        {
            IItem child = (IItem) p_sourceParents.get(i);
            path.append(child.getSimpleName()).append("/");
            IItem targetChild = getItemByPath(path.toString());
            if (targetChild == null)
                targetChild = makeMirror(child, parent);
            parent = targetChild;
        }

        return parent;
    }

    // make a copy of a under parent
    private IItem makeMirror(IItem a, IItem parent) throws Exception
    {
        if (m_logger.isDebugEnabled())
        {
            m_logger.debug("child URL is: " + a.getUrl());
            m_logger.debug("child shortname is: " + a.getSimpleName());
            m_logger.debug("child fullname is: " + a.getFullName());            
        }
        ISite site = a.getSite();
        IHost host = site.getDefaultHost();
        IType type = a.getType();
        IItem newChild = m_mediaSurface.createItem(m_securityContextHandle,
                type.getKey(), a.getFullName(), host.getKey(), site.getKey());
        copyItem(a, newChild); // copy all fields over from original
        newChild.bindToItem(parent.getKey(), true, null);
        newChild.setAuditDescription(MSG_CREATE);
        newChild.saveChanges();
        newChild.edit();
        newChild.setSimpleName(a.getSimpleName());
        newChild.setLocale(m_targetILocale.getKey());
        newChild.setAuditDescription(MSG_EDIT);
        newChild.saveChanges();
        return newChild;
    }

    public static void main(String args[]) throws Exception
    {
        if (args.length < 1)
        {
            System.out.println("USAGE: Exporter <url>");
            return;
        }

        String url = args[0];
        Exporter me = new Exporter();
        me.connect();
        me.setTargetILocale();
        IItem item = me.getItemByUrl(url);
        if (item == null)
        {
            System.out.println("Source item " + url + " does not exist.");
            return;
        }
        me.m_itemKey = item.getKey().getIntKey();
        me.writeTargetItem();
    }

    /**
     * Makes a preview URL XML that can be used to preview this item from
     * MediaSurface. Many views can be shown this way.
     * 
     * @return String of XML
     */
    private String makePreviewUrlXml()
    {
        try
        {
            StringBuffer xml = new StringBuffer(
                    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n");
            xml.append("<previewUrlXml>\r\n");
            xml.append("<locale name=\"").append(m_sourceLocale)
                    .append("\">\r\n");
            xml.append("<sourceUrls>\r\n");
            xml.append("<url type=\"get\">\r\n");
            xml.append("<label>Standard View</label>\r\n");
            xml.append("<href>");
            xml.append(m_item.getUrl());
            xml.append("</href>\r\n");
            xml.append("</url>\r\n");
            xml.append("</sourceUrls>\r\n");
            xml.append("</locale>\r\n");

            xml.append("<locale name=\"").append(m_targetLocale)
                    .append("\">\r\n");
            xml.append("<targetUrls>\r\n");
            xml.append("<url type=\"get\">\r\n");
            xml.append("<label>Standard View</label>\r\n");
            xml.append("<href>");
            xml.append(m_targetItem.getUrl());
            xml.append("</href>\r\n");
            xml.append("</url>\r\n");
            xml.append("</targetUrls>\r\n");
            xml.append("</locale>\r\n");
            xml.append("</previewUrlXml>\r\n");
            return xml.toString();
        }
        catch (Exception e)
        {
            m_logger.error("Problem creating preview url xml.", e);
            return "";
        }
    }
}
