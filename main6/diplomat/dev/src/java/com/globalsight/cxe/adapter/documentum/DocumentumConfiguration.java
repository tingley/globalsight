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

public interface DocumentumConfiguration {
    
    public final static String DCTM_VERSION_CURRENT = "CURRENT";
    public final static String DCTM_ATTRVALUE_IP = "In Progress";
    public final static String DCTM_ATTRVALUE_LOC = "Localized";
    
    public final static String DCTM_DOCUMENT_TYPE = "dm_document";
    public final static String DCTM_RELATION_TYPE = "dm_relation_type";
    public final static String DCTM_RELATION_OBJECT = "dm_relation";
    public final static String DCTM_RELATION_NAME = "globalsight_relation";
    public final static String DCTM_RELATION_SQL = "select r_object_id from dm_relation_type where relation_name = '" 
        + DCTM_RELATION_NAME + "'";
    public final static String DCTM_DATE_FORMAT = "mm/dd/yyyy hh:mi:ss";
    public final static String JAVA_DATE_FORMAT = "MM/dd/yyyy HH:mm:ss";
    public final static String DCTM_RELATION_DESC = "Created by GlobalSight";
    
    public final static String DCTM_PROPERTIESFILE = "/properties/DocumentumAdapter.properties";
    public final static String DCTM_CATEGORY = "Documentum";
    public final static String DCTM_OBJECTID = "ObjectId";
    public final static String DCTM_NEWOBJECTID = "NewObjectId";
    public final static String DCTM_WORKFLOWID = "WorkflowId";
    public final static String DCTM_ISJOBDONE = "IsJobDone";
    public final static String DCTM_ISATTRFILE = "IsAttrFile";
    public final static String DCTM_USERID = "UserId";
    public final static String DCTM_FILEATTRXML = "DctmFileAttrXml";
    public final static String DCTM_SELWFI_SQL = "select iflow_instance_id from workflow where job_id = ?";
    
    public final static String DCTM_STATE_PROKEY = "field_translation_state";
    public final static String DCTM_JOBID_PROKEY = "field_translation_jobid";
    public final static String DCTM_WORKFLOWID_PROKEY = "field_translation_ids";
    public final static String DCTM_TABLE_PROKEY = "table_translation_attr";
    public final static String DCTM_DOCTYPE_PROKEY = "field_doctype";
    public final static String DCTM_LOCATION_PROKEY = "field_location";
    public final static String DCTM_ATTRS_PROKEY = "field_translation_attrs";
}
