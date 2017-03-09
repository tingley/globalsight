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
package com.globalsight.everest.webapp.pagehandler.administration.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.globalsight.cxe.entity.fileextension.FileExtensionImpl;
import com.globalsight.cxe.entity.fileprofile.FileProfileImpl;
import com.globalsight.cxe.entity.filterconfiguration.Filter;
import com.globalsight.cxe.entity.filterconfiguration.FilterHelper;
import com.globalsight.cxe.entity.filterconfiguration.QAFilter;
import com.globalsight.cxe.entity.filterconfiguration.QAFilterManager;
import com.globalsight.cxe.entity.xmldtd.XmlDtdImpl;
import com.globalsight.cxe.persistence.fileprofile.FileProfilePersistenceManagerWLRemote;
import com.globalsight.everest.foundation.BasicL10nProfile;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.webapp.pagehandler.administration.config.xmldtd.XmlDtdManager;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.StringUtil;

/**
 * Imports property file file profiles.
 */
public class FileProfileImporter implements ConfigConstants
{
    private static final Logger logger = Logger.getLogger(FileProfileImporter.class);
    private String sessionId;
    private long companyId;
//    private String filterName;

    public FileProfileImporter(String sessionId, String companyId, String importToCompId)
    {
        this.sessionId = sessionId;
        if (importToCompId != null && !importToCompId.equals("-1"))
        {
            this.companyId = Long.parseLong(importToCompId);
        }
        else
        {
            this.companyId = Long.parseLong(companyId);
        }
    }

    /**
     * Analysis and imports upload file.
     */
    public void analysisAndImport(File uploadedFile)
    {
        Map<String, Map<String, String>> map = new HashMap<String, Map<String, String>>();

        try
        {
            String[] keyArr = null;
            String key = null;
            String strKey = null;
            String strValue = null;
            InputStream is;
            is = new FileInputStream(uploadedFile);
            BufferedReader bf = new BufferedReader(new InputStreamReader(is));
            Properties prop = new Properties();
            prop.load(bf);
            Enumeration enum1 = prop.propertyNames();
            while (enum1.hasMoreElements())
            {
                // The key profile
                strKey = (String) enum1.nextElement();
                key = strKey.substring(0, strKey.lastIndexOf('.'));
                keyArr = strKey.split("\\.");
                // Value in the properties file
                strValue = prop.getProperty(strKey);
                Set<String> keySet = map.keySet();
                if (keySet.contains(key))
                {
                    Map<String, String> valueMap = map.get(key);
                    Set<String> valueKey = valueMap.keySet();
                    if (!valueKey.contains(keyArr[2]))
                    {
                        valueMap.put(keyArr[2], strValue);
                    }
                }
                else
                {
                    Map<String, String> valueMap = new HashMap<String, String>();
                    valueMap.put(keyArr[2], strValue);
                    map.put(key, valueMap);
                }
            }
            // Data analysis
            analysisData(map);
        }
        catch (Exception e)
        {
            logger.error("Failed to parse the file", e);
        }
    }

    private void analysisData(Map<String, Map<String, String>> map)
    {
        if (map.isEmpty())
            return;

        Map<String, List> dataMap = new HashMap<String, List>();
        List<FileProfileImpl> fileProfileList = new ArrayList<FileProfileImpl>();
        Set<String> keySet = map.keySet();
        Iterator it = keySet.iterator();
        while (it.hasNext())
        {
            String key = (String) it.next();
            String[] keyArr = key.split("\\.");
            Map<String, String> valueMap = map.get(key);
            if (!valueMap.isEmpty())
            {
                if (keyArr[0].equalsIgnoreCase("FileProfile"))
                {
                    FileProfileImpl fileProfile = putDataIntoFP(valueMap);
                    fileProfileList.add(fileProfile);
                }
            }
        }

        if (fileProfileList.size() > 0)
            dataMap.put("FileProfile", fileProfileList);

        // Storing data
        storeDataToDatabase(dataMap);
    }

    private FileProfileImpl putDataIntoFP(Map<String, String> valueMap)
    {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        dateFormat.setLenient(false);
        FileProfileImpl fileProfile = new FileProfileImpl();
        String filterName2 = null;
        String filterTableName = null;
        try
        {
            String keyField = null;
            String valueField = null;
            Set<String> valueKey = valueMap.keySet();
            Iterator itor = valueKey.iterator();
            while (itor.hasNext())
            {
                keyField = (String) itor.next();
                valueField = valueMap.get(keyField);
                if ("ID".equalsIgnoreCase(keyField))
                {
                    fileProfile.setId(Long.parseLong(valueField));
                }
                else if ("NAME".equalsIgnoreCase(keyField))
                {
                    fileProfile.setName(valueField);
                }
                else if ("DESCRIPTION".equalsIgnoreCase(keyField))
                {
                    fileProfile.setDescription(valueField);
                }
                else if ("KNOWN_FORMAT_TYPE_ID".equalsIgnoreCase(keyField))
                {
                    fileProfile.setKnownFormatTypeId(Integer.parseInt(valueField));
                }
                else if ("CODE_SET".equalsIgnoreCase(keyField))
                {
                    fileProfile.setCodeSet(valueField);
                }
                else if ("XML_DTD_NAME".equalsIgnoreCase(keyField))
                {
                    XmlDtdImpl xmlDtd = null;
                    if (StringUtil.isNotEmptyAndNull(valueField))
                    {
                        List<XmlDtdImpl> xmlDtdList = XmlDtdManager
                                .getAllXmlDtdByCompanyId(companyId);
                       
                        for (XmlDtdImpl dtd : xmlDtdList)
                        {
                            String dtdName = dtd.getName();
                            if (dtdName.equals(valueField)
                                    || dtdName.startsWith(valueField + "_import_"))
                            {
                                xmlDtd = dtd;
                                break;
                            }
                        }
                    }
                    fileProfile.setXmlDtd(xmlDtd);
                }
                else if ("L10N_PROFILE_NAME".equalsIgnoreCase(keyField))
                {
                    List<BasicL10nProfile> lpList = ServerProxy.getProjectHandler()
                            .getAllL10nProfileByCompanyId(companyId);
                    long locProfileId = -1;
                    for (BasicL10nProfile lp : lpList)
                    {
                        String lpName = lp.getName();
                        if (lpName.equals(valueField) || lpName.startsWith(valueField + "_import_"))
                        {
                            locProfileId = lp.getId();
                            break;
                        }
                    }
                    fileProfile.setL10nProfileId(locProfileId);
                }
                else if ("DEFAULT_EXPORT_STF".equalsIgnoreCase(keyField))
                {
                    fileProfile.setByDefaultExportStf(Boolean.parseBoolean(valueField));
                }
                else if ("TIMESTAMP".equalsIgnoreCase(keyField))
                {
                    Date timeDate = dateFormat.parse(valueField);
                    Timestamp dateTime = new Timestamp(timeDate.getTime());
                    fileProfile.setTimestamp(dateTime);
                }
                else if ("IS_ACTIVE".equalsIgnoreCase(keyField))
                {
                    fileProfile.setIsActive(Boolean.parseBoolean(valueField));
                }
                else if ("FILTER_NAME".equalsIgnoreCase(keyField))
                {
                    filterName2 = valueField;
                }
                else if ("FILTER_TABLE_NAME".equalsIgnoreCase(keyField))
                {
                    fileProfile.setFilterTableName(valueField);
                    filterTableName = valueField;
                }
                else if ("QA_FILTER_NAME".equalsIgnoreCase(keyField))
                {
                    if (StringUtil.isNotEmptyAndNull(valueField))
                    {
                        List<Filter> qaFilterList = QAFilterManager.getAllQAFilters(companyId);
                        if (qaFilterList.size() == 0)
                            fileProfile.setQaFilter(null);
                        for (Filter qaFilter : qaFilterList)
                        {
                            String filterName = qaFilter.getFilterName();
                            if (filterName.equals(valueField)
                                    || filterName.startsWith(valueField + "_import_"))
                            {
                                fileProfile.setQaFilter((QAFilter) qaFilter);
                                break;
                            }
                        }
                    }
                    else
                    {
                        fileProfile.setQaFilter(null);
                    }
                }
                else if ("COMPANYID".equalsIgnoreCase(keyField))
                {
                    fileProfile.setCompanyId(companyId);
                }
                else if ("SCRIPT_ON_IMPORT".equalsIgnoreCase(keyField))
                {
                    fileProfile.setScriptOnImport(valueField);
                }
                else if ("SCRIPT_ON_EXPORT".equalsIgnoreCase(keyField))
                {
                    fileProfile.setScriptOnExport(valueField);
                }
                else if ("NEW_ID".equalsIgnoreCase(keyField))
                {
                    if (StringUtil.isNotEmptyAndNull(valueField))
                    {
                        fileProfile.setNewId(Long.parseLong(valueField));
                    }
                }
                else if ("TERMINOLOGY_APPROVAL".equalsIgnoreCase(keyField))
                {
                    fileProfile.setTerminologyApproval(Integer.parseInt(valueField));
                }
                else if ("XLF_SOURCE_AS_UNTRANSLATED_TARGET".equalsIgnoreCase(keyField))
                {
                    fileProfile.setXlfSourceAsUnTranslatedTarget(Integer.parseInt(valueField));
                }
                else if ("REFERENCE_FP".equalsIgnoreCase(keyField))
                {
                    fileProfile.setReferenceFP(Long.parseLong(valueField));
                }
                else if ("BOM_TYPE".equalsIgnoreCase(keyField))
                {
                    fileProfile.setBOMType(Integer.parseInt(valueField));
                }
                else if ("EOL_ENCODING".equalsIgnoreCase(keyField))
                {
                    fileProfile.setEolEncoding(Integer.parseInt(valueField));
                }
                else if ("EXTENSION_NAMES".equalsIgnoreCase(keyField))
                {
                    Vector extensionIds = new Vector();
                    String[] extensionNameList = valueField.split(",");
                    for (String extensionName : extensionNameList)
                    {
                        FileExtensionImpl fileExtension = ServerProxy.getFileProfilePersistenceManager()
                                .getFileExtensionByNameAndCompanyId(extensionName, companyId);
                        if(fileExtension!=null){
                            extensionIds.add(fileExtension.getId());
                        }
                    }
                    fileProfile.setFileExtensionIds(extensionIds);
                }
            }
            
            Filter origFilter = FilterHelper.getFilter(filterTableName, filterName2, companyId);
            if (origFilter != null)
            {
                List<Filter> filters = FilterHelper.getFiltersByTableName(filterTableName,
                        companyId);
                if (filters.size() == 0)
                    fileProfile.setFilterId(-1);
                for (Filter filter : filters)
                {
                    String filterName = filter.getFilterName();
                    if (filterName.equals(origFilter.getFilterName())
                            || filterName.startsWith(origFilter.getFilterName() + "_import_"))
                    {
                        fileProfile.setFilterId(filter.getId());
                        break;
                    }
                }
            }
            else
            {
                fileProfile.setFilterId(-1);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return fileProfile;
    }

    private void storeDataToDatabase(Map<String, List> dataMap)
    {
        if (dataMap.isEmpty())
            return;

        try
        {
            if (dataMap.containsKey("FileProfile"))
            {
                storeFileProfileData(dataMap);
            }
            addMessage("<b> Done importing File Profiles.</b>");
        }
        catch (Exception e)
        {
            logger.error("Failed to import File Profiles.", e);
            addToError(e.getMessage());
        }
    }

    private void storeFileProfileData(Map<String, List> dataMap)
    {
        List<FileProfileImpl> fileProfileList = dataMap.get("FileProfile");
        FileProfileImpl originalFileProfile = null;
        try
        {
            for (int i = 0; i < fileProfileList.size(); i++)
            {
                originalFileProfile = fileProfileList.get(i);
                long companyId = originalFileProfile.getCompanyId();

                // checks localization file exist
                long profileId = originalFileProfile.getL10nProfileId();
                if (profileId != -1)
                {
                    String oldName = originalFileProfile.getName();
                    String newName = getFileProfileNewName(oldName, companyId);
                    FileProfileImpl newFileProfile = createNewFileProfile(newName,
                            originalFileProfile);
                    HibernateUtil.save(newFileProfile);
                    if (oldName.equals(newName))
                    {
                        addMessage("<b>" + newName + "</b> imported successfully.");
                    }
                    else
                    {
                        addMessage(" File Profile name <b>" + oldName + "</b> already exists. <b>"
                                + newName + "</b> imported successfully.");
                    }
                }
                else
                {
                    String msg = "Failed uploading File Profile data! Missing some required information.";
                    logger.warn(msg);
                    addToError(msg);
                }
            }
        }
        catch (Exception e)
        {
            String msg = "Upload File Profile data failed !";
            logger.warn(msg);
            addToError(msg);
        }
    }

    /**
     * Creates a new file profile.
     */
    private FileProfileImpl createNewFileProfile(String newName, FileProfileImpl originalFileProfile)
    {
        FileProfileImpl fileProfile = new FileProfileImpl();
        createBasicFileProfile(fileProfile, newName, originalFileProfile);
        // If the known format is XLZ, it need to process more
        if (originalFileProfile.getKnownFormatTypeId() == 48)
            processXLZFormat(fileProfile, newName, originalFileProfile);

        return fileProfile;
    }

    /**
     * Creates a file profile contains basic info.
     */
    private void createBasicFileProfile(FileProfileImpl fileProfile, String newName,
            FileProfileImpl originalFileProfile)
    {
        try
        {
            fileProfile.setName(newName);
            fileProfile.setDescription(originalFileProfile.getDescription());
            fileProfile.setKnownFormatTypeId(originalFileProfile.getKnownFormatTypeId());
            fileProfile.setCodeSet(originalFileProfile.getCodeSet());
            fileProfile.setFilterTableName(originalFileProfile.getFilterTableName());
            fileProfile.setL10nProfileId(originalFileProfile.getL10nProfileId());
            fileProfile.setByDefaultExportStf(originalFileProfile.byDefaultExportStf());
            fileProfile.setIsActive(originalFileProfile.getIsActive());
            fileProfile.setCompanyId(originalFileProfile.getCompanyId());
            fileProfile.setScriptOnImport(originalFileProfile.getScriptOnImport());
            fileProfile.setScriptOnExport(originalFileProfile.getScriptOnExport());
            fileProfile.setBOMType(originalFileProfile.getBOMType());
            fileProfile.setTerminologyApproval(originalFileProfile.getTerminologyApproval());
            fileProfile.setXlfSourceAsUnTranslatedTarget(originalFileProfile
                    .getXlfSourceAsUnTranslatedTarget());
            fileProfile.setEolEncoding(originalFileProfile.getEolEncoding());
            fileProfile.setFileExtensionIds(originalFileProfile.getFileExtensionIds());

            // saves xmlDtd
            fileProfile.setXmlDtd(originalFileProfile.getXmlDtd());

            // saves filter
            fileProfile.setFilterId(originalFileProfile.getFilterId());

            // saves qa filter
            fileProfile.setQaFilter(originalFileProfile.getQaFilter());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void processXLZFormat(FileProfileImpl fileProfile, String newName,
            FileProfileImpl originalFileProfile)
    {
        FileProfileImpl xlzRefFp = null;
        try
        {
            FileProfilePersistenceManagerWLRemote fpManager = ServerProxy
                    .getFileProfilePersistenceManager();

            // XLZ file profile
            xlzRefFp = new FileProfileImpl();
            createBasicFileProfile(xlzRefFp, newName, originalFileProfile);

            String fpName = xlzRefFp.getName();
            xlzRefFp.setName(fpName + "_RFP");
            xlzRefFp.setIsActive(false);
            xlzRefFp.setKnownFormatTypeId(39);

            // Set file extensions
            ArrayList exts = new ArrayList(fpManager.getAllFileExtensions());
            Vector tmpExts = new Vector();
            FileExtensionImpl ext = null;
            for (int i = 0; i < exts.size(); i++)
            {
                ext = (FileExtensionImpl) exts.get(i);
                if ("xlf".equalsIgnoreCase(ext.getName())
                        || "xliff".equalsIgnoreCase(ext.getName()))
                {
                    tmpExts.add(ext.getIdAsLong());
                }
            }
            xlzRefFp.setFileExtensionIds(tmpExts);
            fpManager.createFileProfile(xlzRefFp);
            fileProfile.setReferenceFP(xlzRefFp.getId());
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
        }
    }

    private String getFileProfileNewName(String oldName, long companyId)
    {
        String hql = "select fp.name from FileProfileImpl fp where fp.companyId=:companyId and fp.isActive='Y'";
        Map map = new HashMap();
        map.put("companyId", companyId);
        List itList = HibernateUtil.search(hql, map);

        if (itList.contains(oldName))
        {
            for (int num = 1;; num++)
            {
                String returnStr = null;
                if (oldName.contains("_import_"))
                {
                    returnStr = oldName.substring(0, oldName.lastIndexOf('_')) + "_" + num;
                }
                else
                {
                    returnStr = oldName + "_import_" + num;
                }
                if (!itList.contains(returnStr))
                {
                    return returnStr;
                }
            }
        }
        else
        {
            return oldName;
        }
    }

    private void addToError(String msg)
    {
        String former = config_error_map.get(sessionId) == null ? "" : config_error_map
                .get(sessionId);
        config_error_map.put(sessionId, former + "<p>" + msg);
    }

    private void addMessage(String msg)
    {
        String former = config_error_map.get(sessionId) == null ? "" : config_error_map
                .get(sessionId);
        config_error_map.put(sessionId, former + "<p>" + msg);
    }
}
