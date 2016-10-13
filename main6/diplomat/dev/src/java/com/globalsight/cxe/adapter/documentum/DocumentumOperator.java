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
package com.globalsight.cxe.adapter.documentum;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;

import com.documentum.com.*;
import com.documentum.fc.client.*;
import com.documentum.fc.common.*;
import com.documentum.operations.IDfCheckinOperation;
import com.documentum.operations.IDfCheckoutOperation;
import com.documentum.operations.IDfCopyOperation;
import com.documentum.operations.IDfOperation;
import com.documentum.operations.IDfOperationError;
import com.globalsight.cxe.persistence.documentum.DocumentumUserInfoHelper;
import com.globalsight.cxe.persistence.documentum.DocumentumUserInfo;

/**
 * The <code>DocumentumOperator</code> class takes responsibility for executing all the
 * operation, including read and write operation of Documentum server.
 * 
 *
 */
public class DocumentumOperator implements DocumentumConfiguration {

    // this static String are loaded from property file.
    private static String DCTM_ATTR_STATE = "r_translation_state";
    private static String DCTM_ATTR_JOBID = "r_translation_job";
    private static String DCTM_ATTR_WFIDS = "r_translation_ids";
    public final static String DCTM_OBJECT_LANGUAGE_CODE = "language_code";

    private static String DCTM_TABLE_TRANSATTR = "cfg_translation_attr";
    private static String DCTM_ATTR_DOCTYPE = "doctype";
    private static String DCTM_ATTR_LOCATION = "location";
    private static String DCTM_ATTR_TRANSATTRS = "attributes";
    
    private static DocumentumOperator m_dtmOperator = null;
    private static IDfSessionManager m_sessionMgr = null;
    //The max size of sessionpool.
    private static final int maxPoolSize = 20;
    //The min size of sessionpool.
    private static final int minPoolSize = 10;
    //The sessionpool used to save DCTM session.
    private static Map m_sessionPool = new HashMap();
    private static IDfLoginInfo loginInfoObj = null;
    private DocumentumUserInfoHelper userInfoHelper = null;
    private static final Logger s_logger = 
        Logger.getLogger(DocumentumOperator.class.getName());
    
    //Initializate block. 
    {
        Properties dctmConfig = new Properties();
        try {
            dctmConfig.load(getClass().getResourceAsStream(DCTM_PROPERTIESFILE));
            DCTM_ATTR_STATE = dctmConfig.getProperty(DCTM_STATE_PROKEY);
            DCTM_ATTR_JOBID = dctmConfig.getProperty(DCTM_JOBID_PROKEY);
            DCTM_ATTR_WFIDS = dctmConfig.getProperty(DCTM_WORKFLOWID_PROKEY);
            DCTM_TABLE_TRANSATTR = dctmConfig.getProperty(DCTM_TABLE_PROKEY);
            DCTM_ATTR_DOCTYPE = dctmConfig.getProperty(DCTM_DOCTYPE_PROKEY);
            DCTM_ATTR_LOCATION = dctmConfig.getProperty(DCTM_LOCATION_PROKEY);
            DCTM_ATTR_TRANSATTRS = dctmConfig.getProperty(DCTM_ATTRS_PROKEY);
            s_logger.debug("Finish to load the dctm.properties for custom attributes");
        } catch (IOException ioex) {
            s_logger.error("Cannot load the dctm.properties, using the default attributes", ioex);
        }
    }
    
    /**
     * Constructor, login in Documentum server and get a shared Session Manager.
     */
    protected DocumentumOperator() {
        
        IDfClient client = null;
        IDfClientX m_clientx = null;
        
        try {
            m_clientx = new DfClientX();
            client = m_clientx.getLocalClient();
            loginInfoObj = m_clientx.getLoginInfo();
            userInfoHelper = new DocumentumUserInfoHelper();
        } catch (DfException dfEx) {
            s_logger.error("Cannot load or initialize DMCL.dll", dfEx);
        }
        //Create a session manager object
        m_sessionMgr = client.newSessionManager();
        
    }
    
    /**
     * Get a instance as a singleton.
     */
    public static DocumentumOperator getInstance() {
        //Get a instance as a singleton.
        if (m_dtmOperator == null) {
            m_dtmOperator = new DocumentumOperator();
        }
        return m_dtmOperator;
    }
    
    /**
     * Get the name of a Documentum object.
     */
    public String getObjectName(String userId, String objectId) {
        
        IDfSession session = getSession(userId);
        
        if (session == null || objectId == null) {
            return null;
        }
        
        IDfSysObject sysObj = null;
        String fileName = null;
        
        try {
            sysObj = (IDfSysObject)session.getObject(new DfId(objectId));
            fileName = sysObj.getObjectName();
        } catch (DfException dfEx) {
            s_logger.error("Object doesn't exist, or a server error occurs.", dfEx);
        } finally {
            releaseSession();            
        }
        
        return fileName;
    }
    
    /**
     * Get the translable attributes of a specified documentum as a xml string.
     */
    public String generateAttributesXml(String userId, String objectId) {
        
        IDfSession session = getSession(userId);

        if (session == null || objectId == null) {
            return null;
        }
        
        StringBuffer xmlStr = new StringBuffer();
        IDfDocument fileObj = null;

        try {
            fileObj = (IDfDocument)session.getObject(new DfId(objectId));
            //Get the DocType of this object.
            IDfType type =fileObj.getType();
            String docType = type.getName();
            //Get the location of this object(eg. Docbase/folder1/folder2).
            String docBase = session.getDocbaseName();
            IDfId folderId = fileObj.getFolderId(0);
            IDfFolder folderObj = (IDfFolder)session.getObject(folderId);
            String folderPath = folderObj.getFolderPath(0);
            String fileLocation = docBase + folderPath;
            
            //Query the name of all translatable attributes.
            StringBuffer queryStr = new StringBuffer();
            queryStr.append("select r_object_id, ").append(DCTM_ATTR_TRANSATTRS);
            queryStr.append(" from ").append(DCTM_TABLE_TRANSATTR);
            queryStr.append(" where ").append(DCTM_ATTR_DOCTYPE);
            queryStr.append(" = '").append(docType).append("' and ");
            queryStr.append(DCTM_ATTR_LOCATION).append(" = '").append(fileLocation).append("'");

            IDfClientX clientx = new DfClientX();
            IDfQuery query = clientx.getQuery();
            query.setDQL(queryStr.toString());
            IDfCollection result = query.execute(session, IDfQuery.DF_READ_QUERY);
            
            String latestVersion = fileObj.getVersionLabel(fileObj.getVersionLabelCount() - 1);
            xmlStr.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n");
            xmlStr.append("<documentumfile name=\"").append(fileObj.getObjectName())
                  .append("\" version=\"").append(latestVersion).append("\" objectid=\"")
                  .append(objectId).append("\">\r\n");

            //Get the value of attributes as xml string.
            boolean hasAttribute = false;
            while (result.next()) {
                hasAttribute = true;
                String attrStr = result.getAllRepeatingStrings(DCTM_ATTR_TRANSATTRS, ","); 
                String[] attrNames = attrStr.split(",");
                for (int i = 0; i < attrNames.length; i++) {
                    
                    String attrName = attrNames[i];
                    String attrValue = null;
                    try {
                        int valueCount = fileObj.getValueCount(attrName);
                        if (valueCount > 0) {
                            xmlStr.append("\t<fileattr name=\"").append(attrName).append("\" datatype=\"")
                                  .append(fileObj.getAttrDataType(attrName)).append("\" isrepeating=\"")
                                  .append(fileObj.isAttrRepeating(attrName)).append("\">\r\n");
    
                            //The dm_document doctype may include multiple attributes
                            //Just care about the attribute whose datatype is String.
                            for (int j = 0; j < valueCount; j++) {
                                attrValue = fileObj.getRepeatingString(attrName, j);
                                xmlStr.append("\t\t<value>").append(attrValue).append("</value>\r\n");
                            }
                            xmlStr.append("\t</fileattr>\r\n");
                        }
                    } catch (DfException dfEx) {
                        s_logger.error("Failed to get the value of '" + attrName + "' attribute", dfEx);
                    }
                }
            }
            if (!hasAttribute) {
                return null;
            }
            xmlStr.append("</documentumfile>");
            
        } catch (DfException dfEx) {
            s_logger.error("DCMT Server error when get the translatable attributes as xml string", dfEx);
        } finally {
            releaseSession();
        }

        return xmlStr.toString();
    }
    
    /**
     * Read the document content from Documentum Server.
     * 
     * @param userId - The primary key of a table, used to save the documentum account info.
     * @param objectId - DCTM object Id.
     * @param destFileName - The destination(a GlobalSight tmp file) the content will be written to.
     */
    public void readContentFromDCTM(String userId, String objectId, String destFileName) {
        
        IDfSession session = getSession(userId);

        if (session == null || objectId == null) {
            return;
        }
        

        try {
            IDfDocument docObj = (IDfDocument)session.getObject(new DfId(objectId));
            String destAbsolutePath = docObj.getFile(destFileName);
            s_logger.debug("Writing the content of this object to " + destAbsolutePath);
            s_logger.debug("Finish to read content for Documentum server, ObjId: " + objectId);
        } catch (DfException dex) {
            s_logger.error("Failed to read content from Documentum server.");
        } finally {
            releaseSession();
        }
    }

    /**
     * Write the translated content back to Documentum server when exporting a specified workflow.
     * 
     * @param userId - The primary key of a table, used to save the documentum account info.
     * @param newObjId - The destination the translated content will be written to.
     * @param contentFilePath - The source tmp file where the translated contend saved.
     * @param trgLocale - GlobalSight target locale for this workflow.
     * @param isAttrFile - False when the translated content is for the DCTM object itself.
     *                     True when the translated content is for the attributes of DCTM object. 
     * @param oldObjId - The id of the original document in DCTM server.
     * @param workflowId - The workflow id
     * @param isJobDone - Are all the workflows in this job finished.
     */
    public void writeBackToDCTM(String userId, String newObjId, String contentFilePath, String trgLocale, 
            boolean isAttrFile, String oldObjId, String workflowId, boolean isJobDone) {
        
        IDfSession session = getSession(userId);

        if (session == null || newObjId == null || oldObjId == null) {
            return;
        }

        IDfDocument newFileObj = null;
        IDfDocument oldFileObj = null;

        try {
            newFileObj = (IDfDocument)session.getObject(new DfId(newObjId));

            if (!isAttrFile) {
                //Write the file content back to DCTM server.
                writeFileContentBack(newFileObj, contentFilePath, trgLocale);        
                //Write custom attributes for original translated document file.
                oldFileObj = (IDfDocument)session.getObject(new DfId(oldObjId));
                changeFileCustomAttrs(oldFileObj, workflowId, trgLocale, isJobDone);
                doCheckout(session, oldFileObj);
                
                if (isJobDone) {
                    doCheckin(session, oldFileObj);
                }
                s_logger.debug("Finish to write file back to Documentum server");
            } else {
                //Write the translatable attributes back to DCTM server.
                writeFileAttrsBack(newFileObj, contentFilePath);
                createRelation(session, oldObjId, newObjId);
                s_logger.debug("Finish to write translatable attributes back to Documentum server");
            }
        } catch (DfException dex) {
            s_logger.error("DCTM server error when write back to DCTM server", dex);
        } finally {
            releaseSession();
        }
    }
    
    /**
     * Write the custom attributes back to DCTM Server, including job id, workflow ids.
     * 
     * @param userId - The primary key of a table, used to save the documentum account info.
     * @param objectId - DCTM document object id.
     * @param jobId - GlobalSight job id.
     * @param attrValues - GlobalSight workflow Ids.
     * @throws DfException
     */
    public void writeCustomAttrsBack(String userId, String objectId, String jobId, Collection attrValues) {
        
        IDfSession session = getSession(userId);
        
        if (session == null || objectId == null) {
            return;
        }

        IDfDocument fileObj = null;
        try {
            fileObj = (IDfDocument)session.getObject(new DfId(objectId));
            //Write job id back
            setObjectStrAttribute(fileObj, DCTM_ATTR_JOBID, 0, jobId);
            //Write job status back
            setObjectStrAttribute(fileObj, DCTM_ATTR_STATE, 0, DCTM_ATTRVALUE_IP);
            //Write workflow ids back
            Iterator iter = attrValues.iterator();
            StringBuffer wfValues = new StringBuffer();
            while (iter.hasNext()) {
                wfValues.append(iter.next().toString());
                if(iter.hasNext()) {
                    wfValues.append(",");
                }
            }
            setObjectStrAttribute(fileObj, DCTM_ATTR_WFIDS, 0, wfValues.toString());
            fileObj.save();
            s_logger.debug("write workflowids:" + wfValues.toString());
            doCheckout(session, fileObj);
            s_logger.debug("Finish to write the custom attributes back to Documentum server");
        } catch (DfException dex) {
            s_logger.error("Failed to write the custom attributes back", dex);
        } finally {
            releaseSession();
        }
    }
    
    /**
     * Copy a DCTM document object.
     * 
     * @param userId - The primary key of a table, used to save the documentum account info.
     * @param oldFileObjId - The source object id.
     * @param destFolderPath - The location the source object is copied to.
     * @return String - The new oject Id.
     */
    public String doCopy(String userId, String oldFileObjId, String destFolderPath) {
        
        IDfSession session = getSession(userId);

        if (session == null) {
            return null;
        }

        IDfDocument fileObj = null;
        IDfId folderId = null;
        String newFileObjId = null;

        try {
            fileObj = (IDfDocument)session.getObject(new DfId(oldFileObjId));
            if (destFolderPath == null || destFolderPath.length() == 0) {
                folderId = fileObj.getFolderId(0);
            } else {
                IDfFolder destFolder = session.getFolderByPath(destFolderPath);
                folderId = destFolder.getObjectId();
            }

            IDfClientX clientx = new DfClientX();
            IDfCopyOperation operation = clientx.getCopyOperation();
            operation.setDestinationFolderId(folderId);
            operation.setDeepFolders(true);
    
            if(fileObj.isVirtualDocument()) {
                IDfVirtualDocument vDoc = fileObj.asVirtualDocument(DCTM_VERSION_CURRENT, false);
                operation.add(vDoc);
            } else {
                operation.add(fileObj);
            }
            executeOperation(operation);
            
            IDfList objList = operation.getNewObjects();
            IDfDocument newFileObj = (IDfDocument)objList.get(0);
            //Clean new custom attributes
            setObjectStrAttribute(newFileObj, DCTM_ATTR_JOBID, 0, "");
            setObjectStrAttribute(newFileObj, DCTM_ATTR_STATE, 0, "");
            setObjectStrAttribute(newFileObj, DCTM_ATTR_WFIDS, 0, "");
            newFileObj.save();
            newFileObjId = newFileObj.getObjectId().getId();
            s_logger.debug("Finish to copy a doucmentum object.");
        } catch (DfException dex) {
            s_logger.error("DCTM server error when copy the document object", dex);
        } finally {
            releaseSession();
        }
        
        return newFileObjId;
    }
    
    /**
     * Clean all the custom attributes(jobId, translate state, workIds).
     */
    public void cleanCustomAttrs(String userId, String objectId) {
        
        IDfSession session = getSession(userId);

        if (session == null) {
            return;
        }
        
        try {
            IDfDocument fileObj = (IDfDocument)session.getObject(new DfId(objectId));
            setObjectStrAttribute(fileObj, DCTM_ATTR_JOBID, 0, "");
            setObjectStrAttribute(fileObj, DCTM_ATTR_STATE, 0, "");
            setObjectStrAttribute(fileObj, DCTM_ATTR_WFIDS, 0, "");
            fileObj.save();
            doCheckin(session, fileObj);
            s_logger.debug("Finish to clean the custom attributes.");
        } catch (DfException dex) {
            s_logger.error("DCTM server error when clear the custom attributes.", dex);
        } finally {
            releaseSession();
        }
    }
    
    /**
     * Write the content of document back to DCTM server.
     * 
     * @param fileObj - DCTM document object, written the content in.
     * @param contentFilePath - The translated content.
     * @param trgLocale - GlobalSight target locale.
     * @throws DfException
     */
    private void writeFileContentBack(IDfDocument fileObj, String contentFilePath, String trgLocale) 
        throws DfException {

        String oriName = fileObj.getObjectName();
        int index = oriName.lastIndexOf(".");
        String newName = oriName;
        if (index == -1) {
            newName = oriName + "_" + trgLocale; 
        } else {
            newName = oriName.substring(0, index) + "_" + trgLocale 
            + oriName.substring(index, oriName.length());    
        }
        s_logger.debug("new Nmae:" + newName);        
        fileObj.setString(DCTM_OBJECT_LANGUAGE_CODE, trgLocale);
        s_logger.debug(DCTM_OBJECT_LANGUAGE_CODE + ":" + trgLocale);
        fileObj.setObjectName(newName);
        fileObj.setFile(contentFilePath);
        fileObj.save();
        s_logger.debug("Write file content back to DCTM server");
    }
    
    /**
     * Update the custom attributes back to DCTM server when export a documentum workflow, including
     * workflow ids, Job state.
     * 
     * @param oldFileObj - DCTM document object.
     * @param workflowId - Workflow ids.
     * @param trgLocale - GlobalSight target locale.
     * @param isJobDone - Are all the workflows finished.
     */
    private void changeFileCustomAttrs(IDfDocument oldFileObj, String workflowId, String trgLocale, boolean isJobDone) {
        
        //remove workflow id from list.
        String workflowIds = getObjectStrAttrValue(oldFileObj, DCTM_ATTR_WFIDS);
        workflowIds = workflowIds.replaceAll(workflowId, trgLocale);
        setObjectStrAttribute(oldFileObj, DCTM_ATTR_WFIDS, 0, workflowIds);
        try {
        oldFileObj.save();
        //if this documentu job finished, set translate_status = Localized.
        if (isJobDone) {
            setObjectStrAttribute(oldFileObj, DCTM_ATTR_STATE, 0, DCTM_ATTRVALUE_LOC);
            oldFileObj.save();
        }
        } catch (DfException e) {
            s_logger.error("IDFDocumentum save exception, can not save", e);
        }
        s_logger.debug("Update the custom attriubtes when export a documentum workflow");
    }
    
    /**
     * Write the translatable attribute values back to DCTM server.
     * 
     * @param fileObj - DCTM document object.
     * @param attrFilePath - The tmp file, the content of attribute values.
     * @throws DfException
     */
    private void writeFileAttrsBack(IDfDocument fileObj, String attrFilePath) throws DfException {
        
        Map fileAttrMap = null;
        try {
            InputStream fis = new FileInputStream(attrFilePath);
            fileAttrMap = FileAttrXmlParser.parseFileAttrXml(fis);
            s_logger.debug("Parse a xml file to get the translatable attribute values");
        } catch (FileNotFoundException fexc) {
            s_logger.error("The file " + attrFilePath + " not found", fexc);
        } catch (IOException exc) {
            s_logger.error("System failed because of IOException", exc);
        }

        Set keys = fileAttrMap.keySet();
        Iterator iter = keys.iterator();
        String key = null;
        
        s_logger.debug("Write the translatable attribute values back");
        while (iter.hasNext()) {
            key = iter.next().toString();
            FileAttr fileAttr = (FileAttr)fileAttrMap.get(key);
            modifyObjectAttribute(fileObj, fileAttr.getName(), fileAttr.getDatatype(), fileAttr.getValues(), false);
        }
        fileObj.save();
    }
    
    /**
     * Create a relation between source document and the translated document.
     * 
     * @param session - The DCTM session.
     * @param oldId - The source document.
     * @param newId - The translated document.
     * @throws DfException
     */
    private void createRelation(IDfSession session, String oldId, String newId) throws DfException {
        
        IDfClientX clientx = new DfClientX();
        IDfQuery query = clientx.getQuery();
        query.setDQL(DCTM_RELATION_SQL);
        IDfCollection result = query.execute(session, IDfQuery.DF_READ_QUERY);
        
        //If the relation_type not existing, create it.
        if (!result.next()) {
            IDfRelationType relationType = (IDfRelationType)session.newObject(DCTM_RELATION_TYPE);
            relationType.setSecurityType(IDfRelationType.PARENT);
            relationType.setPermanentLink(false);
            relationType.setRelationName(DCTM_RELATION_NAME);
            relationType.setParentType(DCTM_DOCUMENT_TYPE);
            relationType.setChildType(DCTM_DOCUMENT_TYPE);
            relationType.setDescription(DCTM_RELATION_DESC);
            relationType.save();
        }
        
        //Create the relationship
        IDfRelation relation = (IDfRelation)session.newObject(DCTM_RELATION_OBJECT);
        relation.setRelationName(DCTM_RELATION_NAME);
        relation.setParentId(new DfId(oldId));
        relation.setChildId(new DfId(newId));
        relation.setChildLabel(DCTM_VERSION_CURRENT);
        
        SimpleDateFormat dateFormator = new SimpleDateFormat(JAVA_DATE_FORMAT);
        String strNow = dateFormator.format(new Date());
        IDfTime effDate = clientx.getTime(strNow, DCTM_DATE_FORMAT);

        relation.setEffectiveDate(effDate);
        relation.setDescription(DCTM_RELATION_DESC);
        relation.save();
    }

    /**
     * Check out a DCTM document object.
     */
    private void doCheckout(IDfSession session, IDfDocument fileObj) throws DfException {

        if (fileObj == null || fileObj.isCheckedOut()) {
            return;
        }

        IDfClientX clientx = new DfClientX();
        IDfCheckoutOperation operation = clientx.getCheckoutOperation();

        if(fileObj.isVirtualDocument() ) {
            IDfVirtualDocument vDoc = fileObj.asVirtualDocument(DCTM_VERSION_CURRENT, false);
            operation.add(vDoc);
        } else {
            operation.add(fileObj);
        }
        
        executeOperation(operation);
        s_logger.debug("Check out a DCTM document object");
    }
    
    /**
     * Check in a the DCTM document object.
     */
    private void doCheckin(IDfSession session, IDfDocument fileObj) throws DfException {
        
        if (fileObj == null || !fileObj.isCheckedOut()) {
            return;
        }

        IDfClientX clientx = new DfClientX();
        IDfCheckinOperation operation = clientx.getCheckinOperation();
        if( fileObj.isVirtualDocument() ) {
            IDfVirtualDocument vDoc = fileObj.asVirtualDocument(DCTM_VERSION_CURRENT, false );
            operation.add(vDoc);
        } else {
            operation.add(fileObj);
        }
        
        operation.setCheckinVersion(IDfCheckinOperation.SAME_VERSION);
        executeOperation(operation);
        s_logger.debug("Check in a DCTM document object");
    }
    
    /**
     * Execute the checkin, checkout, copy operation.
     */
    private void executeOperation(IDfOperation operation) throws DfException {
        
        operation.setOperationMonitor(new Progress());
        boolean executeFlag = operation.execute();
        
        if (executeFlag == false) {
            s_logger.error("Failed to execute such a operation");
            IDfList errorList = operation.getErrors();
            String message = null;
            IDfOperationError error = null;
            for(int i = 0; i < errorList.getCount() ; i++) {
                error = (IDfOperationError) errorList.get(i);
                message += error.getMessage();
            }
            s_logger.error("Errors: " + message);
            throw new DfException("Failed to write back to Documentum.");
        }
    }

    /**
     * Get a string attribute value from a DCTM object.
     * 
     * Note: If multi-value attribute, return the first value.
     */
    private String getObjectStrAttrValue(IDfDocument fileObj, String attrName) {
        
        String attrValue = null;
        if (fileObj == null) {
            return attrValue;
        }
        
        try {
            if (!fileObj.hasAttr(attrName)) {
                throw new DfException("The attribute " + attrName + " does not exist");
            }
            if (fileObj.getAttrDataType(attrName) != IDfAttr.DM_STRING) {
                throw new DfException("This method just support to set String attribute type");
            }
            attrValue = fileObj.getRepeatingString(attrName, 0);

        } catch (DfException dfEx) {
            s_logger.error(dfEx.getMessage(), dfEx);
        }

        return attrValue;
    }
    
    /**
     * Sets the string value of a repeating attribute.
     * 
     * Note: Use this method on a single-valued attribute as long as the valueIndex is 0.
     * 
     * @param fileObj - DCTM document object.
     * @param attrName - The attribute name.
     * @param valueIndex - The index position where set a string value.
     * @param attrValue - The string value.
     * @param autoSave - Save the value automatically or not.
     *                   false, invoke IDfSysObject.save() mannually.
     */
    private void setObjectStrAttribute(IDfDocument fileObj, String attrName, 
            int valueIndex, String attrValue) {
        
        if (fileObj == null) {
            return;
        }
        
        try {
            if (!fileObj.hasAttr(attrName)) {
                throw new DfException("The attribute " + attrName + " does not exist");
            }
            if (fileObj.getAttrDataType(attrName) != IDfAttr.DM_STRING) {
                throw new DfException("This method just support to set String attribute type");
            }
            if (valueIndex < 0 || valueIndex >= fileObj.getValueCount(attrName)) {
                throw new DfException("The valueIndex is out of bound when setting attribute value");
            }
            //Use this method, no need to care whether the attribute is repeating or not.
            fileObj.setRepeatingString(attrName, valueIndex, attrValue);            
        } catch (DfException dfEx) {
            s_logger.error(dfEx.getMessage(), dfEx);
        }
    }
    
    /**
     * Change the string value of a attribute.
     * 
     * Note: If single-valued attribute, just set the first value of attrValues to DCTM object.
     *       Before change operation, It will remove all the values for muti-value attribute.
     * 
     * @param attrName - DCTM attribute name.
     * @param dataType - DCTM attribute type.
     * @param attrValues - A set of attribute values.
     * @param autoSave - Save the value automatically or not.
     *                   false, invoke IDfSysObject.save() mannually.
     */
    private void modifyObjectAttribute(IDfDocument fileObj, String attrName, int dataType, 
            Collection attrValues, boolean autoSave) {
        
        try {
            if (!fileObj.hasAttr(attrName)) {
                throw new DfException("The attribute " + attrName + " does not exist");
            }
            if (fileObj.getAttrDataType(attrName) != dataType) {
                throw new DfException("This method just support to set String attribute type");
            }

            //Use this method, no need to care whether the attribute is repeating or not.
            Iterator iter = attrValues.iterator();
            if (fileObj.isAttrRepeating(attrName)) {
                fileObj.removeAll(attrName);
                while (iter.hasNext()) {
                    fileObj.appendString(attrName, iter.next().toString());
                }
            } else {
                if (iter.hasNext()) {
                    fileObj.setString(attrName, iter.next().toString());
                }
            }

            if (autoSave) {
                fileObj.save();
            }
        } catch (DfException dfEx) {
            s_logger.error(dfEx.getMessage(), dfEx);
        }
    }
    
    /**
     * Get a session from the shared session manager or the session pool.
     */
    private synchronized IDfSession getSession(String userId) {

        IDfSession session= (IDfSession)m_sessionPool.get(userId);
        
        if (session == null) {
    
            try {
                //Account account = ldldldl;
                DocumentumUserInfo userInfo = null;
                userInfo = userInfoHelper.findDocumentumUserInfo(userId);
                String docBase = userInfo.getDocumentumDocbase();
                String userName = userInfo.getDocumentumUserId();
                String password = userInfo.getDocumentumPassword();
                /*String docBase = getDCTMDocbase(userId);
                String userName = getDCTMUserName(userId);
                String password = getDCTMPassword(userId);*/

                //Create an IDfLoginInfo object named loginInfoObj
                loginInfoObj.setUser(userName);
                loginInfoObj.setPassword(password);
                loginInfoObj.setDomain(null);
    
                //Bind the session manager to the login info
                m_sessionMgr.clearIdentity(docBase);
                m_sessionMgr.setIdentity(docBase, loginInfoObj);
                session = m_sessionMgr.getSession(docBase);
                m_sessionPool.put(userId, session);
            } catch (DfAuthenticationException dfaEx) {
                s_logger.error("Failed to login Documentum server because of incorrect user info", dfaEx);
            } catch (DfServiceException dfsEx) {
                s_logger.error("Failed to get user session", dfsEx);
            }
        }
        
        return session;
    }

    /**
     * Release the session back to session manager from session pool.
     */
    private void releaseSession() {

        if (m_sessionPool.size() >= maxPoolSize) {
            
            synchronized(this) {

                Set keys = m_sessionPool.keySet();
                Iterator keyIter = keys.iterator();
                String key = null;
                IDfSession session = null;
                int poolSize = minPoolSize;
                
                while (keyIter.hasNext() && poolSize-- > 0) {
                    key = keyIter.next().toString();
                    session = (IDfSession)m_sessionPool.get(key);
                    m_sessionPool.remove(key);
                    if (session != null) {
                            m_sessionMgr.release(session);
                    }
                }
            }
        }
    }
    
    public String saveDCTMAccount(String dctmDocbase, String dctmUserName, String dctmPassword) throws Exception {
        
        DocumentumUserInfo userInfo = new DocumentumUserInfo();
        userInfo.setDocumentumUserId(dctmUserName);
        userInfo.setDocumentumPassword(dctmPassword);
        userInfo.setDocumentumDocbase(dctmDocbase);
        userInfo = userInfoHelper.createDocumentumUserInfo(userInfo);
        String userId = String.valueOf(userInfo.getId());
        IDfSession session = (IDfSession)getSession(userId);
        if (session == null) {
            userInfoHelper.removeDocumentumUserInfo(userInfo);
            throw new Exception ("This account can not connect to Documentum server");
        }
        return userId;  
    }
    
}
