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
package com.globalsight.everest.webapp.pagehandler.administration.mtprofile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
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

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;

import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.MultiCompanySupportedThread;
import com.globalsight.everest.projecthandler.EngineEnum;
import com.globalsight.everest.projecthandler.MachineTranslationExtentInfo;
import com.globalsight.everest.projecthandler.MachineTranslationProfile;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.AmbFileStoragePathUtils;
import com.globalsight.util.GeneralException;

public class MTProfileImportHandler extends PageHandler
{
    private static final Logger logger = Logger
            .getLogger(MTProfileImportHandler.class);
    private Map<String, Integer> filter_percentage_map = new HashMap<String, Integer>();
    private Map<String, String> filter_error_map = new HashMap<String, String>();

    public void invokePageHandler(WebPageDescriptor p_pageDescriptor,
            HttpServletRequest p_request, HttpServletResponse p_response,
            ServletContext p_context) throws ServletException, IOException,
            EnvoyServletException
    {
        HttpSession session = p_request.getSession(false);
        String sessionId = session.getId();
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(WebAppConstants.SESSION_MANAGER);
        String currentCompanyId = CompanyThreadLocal.getInstance().getValue();
        boolean isSuperAdmin = ((Boolean) session
                .getAttribute(WebAppConstants.IS_SUPER_ADMIN)).booleanValue();

        String action = p_request.getParameter(MTProfileConstants.ACTION);
        try
        {
            if (MTProfileConstants.IMPORT_ACTION.equals(action))
            {
                if (isSuperAdmin)
                {
                	importMTProfile(p_request);
                    p_request.setAttribute("currentId", currentCompanyId);
                }
            }
            else if ("startUpload".equals(action))
            {
                File uploadedFile = this.uploadFile(p_request);
                if (isSuperAdmin)
                {
                    String importToCompId = p_request.getParameter("companyId");
                    session.setAttribute("importToCompId", importToCompId);
                }
                session.setAttribute("uploading_filter", uploadedFile);
            }
            else if ("doImport".equals(action))
            {
                int count = 0;
                if (sessionMgr.getAttribute("count") != null)
                {
                    count = (Integer) sessionMgr.getAttribute("count");
                    if (count == 1)
                    {
                        count++;
                        sessionMgr.setAttribute("count", count);
                    }
                }
                else
                {
                    count++;
                    sessionMgr.setAttribute("count", count);
                }
                if (session.getAttribute("uploading_filter") != null)
                {
                    filter_percentage_map.clear();// .remove(sessionId);
                    filter_error_map.clear();// .remove(sessionId);
                    File uploadedFile = (File) session
                            .getAttribute("uploading_filter");
                    String importToCompId = (String) session
                            .getAttribute("importToCompId");

                    session.removeAttribute("importToCompId");
                    session.removeAttribute("uploading_filter");
                    DoImport imp = new DoImport(sessionId, uploadedFile,
                            currentCompanyId, importToCompId);
                    imp.start();
                }
                else
                {
                    logger.error("No uploaded user info file.");
                }
            }
            else if ("refreshProgress".equals(action))
            {
                this.refreshProgress(p_request, p_response, sessionId);
                return;
            }
        }
        catch (RemoteException re)
        {
            throw new EnvoyServletException(EnvoyServletException.EX_GENERAL,
                    re);
        }
        catch (GeneralException ge)
        {
            throw new EnvoyServletException(EnvoyServletException.EX_GENERAL,
                    ge);
        }
        super.invokePageHandler(p_pageDescriptor, p_request, p_response,
                p_context);
    }

    private void importMTProfile(HttpServletRequest p_request)
            throws EnvoyServletException, RemoteException
    {
        String hql = "select id from Company";
        List<Long> companyIdList = (List<Long>) HibernateUtil.search(hql);
        p_request.setAttribute("companyIdList", companyIdList);
    }

    /**
     * Upload the properties file to FilterConfigurations/import folder
     * 
     * @param request
     */
    private File uploadFile(HttpServletRequest request)
    {
        File f = null;
        try
        {
            String tmpDir = AmbFileStoragePathUtils.getFileStorageDirPath()
                    + File.separator + "GlobalSight" + File.separator
                    + "MachineTranslationProfiles" + File.separator + "import";
            boolean isMultiPart = ServletFileUpload.isMultipartContent(request);
            if (isMultiPart)
            {
                DiskFileItemFactory factory = new DiskFileItemFactory();
                factory.setSizeThreshold(1024000);
                ServletFileUpload upload = new ServletFileUpload(factory);
                List<?> items = upload.parseRequest(request);
                for (int i = 0; i < items.size(); i++)
                {
                    FileItem item = (FileItem) items.get(i);
                    if (!item.isFormField())
                    {
                        String filePath = item.getName();
                        if (filePath.contains(":"))
                        {
                            filePath = filePath
                                    .substring(filePath.indexOf(":") + 1);
                        }
                        String originalFilePath = filePath.replace("\\",
                                File.separator).replace("/", File.separator);
                        String fileName = tmpDir + File.separator
                                + originalFilePath;
                        f = new File(fileName);
                        f.getParentFile().mkdirs();
                        item.write(f);
                    }
                }
            }
            return f;
        }
        catch (Exception e)
        {
            logger.error("File upload failed.", e);
            return null;
        }
    }

    /**
     * Import the user info into system
     * 
     * @param request
     * @param response
     * @param sessionId
     */
    private void refreshProgress(HttpServletRequest request,
            HttpServletResponse response, String sessionId)
    {
        HttpSession session = request.getSession(false);
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(WebAppConstants.SESSION_MANAGER);
        int count = 0;
        if (sessionMgr.getAttribute("count") != null)
        {
            count = (Integer) sessionMgr.getAttribute("count");
        }
        else
        {
            count++;
            sessionMgr.setAttribute("count", count);
        }
        try
        {
            int percentage;
            if (filter_percentage_map.get(sessionId) == null)
            {
                percentage = 0;
            }
            else
            {
                if (count == 1)
                {
                    percentage = 0;
                }
                else
                {
                    percentage = filter_percentage_map.get(sessionId);
                }
            }

            String msg;
            if (filter_error_map.get(sessionId) == null)
            {
                msg = "";
            }
            else
            {
                if (count == 1)
                {
                    msg = "";
                }
                else
                {
                    msg = filter_error_map.get(sessionId);
                }
            }
            count++;
            sessionMgr.setAttribute("count", count);

            response.setContentType("text/html;charset=UTF-8");
            PrintWriter writer = response.getWriter();
            writer.write(String.valueOf(percentage + "&" + msg));
            writer.close();
            if (percentage == 100)
            {
                sessionMgr.removeElement("count");
            }
        }
        catch (Exception e)
        {
            logger.error("Refresh failed.", e);
        }

    }

    private class DoImport extends MultiCompanySupportedThread
    {
        private Map<Long, MachineTranslationProfile> mtpMap = new HashMap<Long, MachineTranslationProfile>();
        private Map<Long, Long> mtpExtentInfoMap = new HashMap<Long, Long>();
        private File uploadedFile;
        private String currentCompanyId;
        private String sessionId;
        private String importToCompId;
        private EngineEnum[] engines = null;

        public DoImport(String sessionId, File uploadedFile,
                String currentCompanyId, String importToCompId)
        {
            this.sessionId = sessionId;
            this.uploadedFile = uploadedFile;
            this.currentCompanyId = currentCompanyId;
            this.importToCompId = importToCompId;
        }

        public void run()
        {
            CompanyThreadLocal.getInstance().setIdValue(this.currentCompanyId);
            engines = EngineEnum.values();
            this.analysisAndImport(uploadedFile);
        }

        private void analysisAndImport(File uploadedFile)
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
                BufferedReader bf = new BufferedReader(
                        new InputStreamReader(is));
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
                throws ParseException
        {
            if (map.isEmpty())
                return;

            Map<String, List> dataMap = new HashMap<String, List>();
            List<MachineTranslationProfile> mtpList = new ArrayList<MachineTranslationProfile>();
            List<MachineTranslationExtentInfo> extenInfoList = new ArrayList<MachineTranslationExtentInfo>();
            Set<String> keySet = map.keySet();
            Iterator it = keySet.iterator();
            while (it.hasNext())
            {
                String key = (String) it.next();
                String[] keyArr = key.split("\\.");
                Map<String, String> valueMap = map.get(key);
                if (!valueMap.isEmpty())
                {
                    if (keyArr[0].equalsIgnoreCase("MachineTranslationProfile"))
                    {
                        MachineTranslationProfile mtp = putDataIntoMTP(valueMap);
                        if (mtp.getMtEngine() == null
                                || "".equals(mtp.getMtEngine()))
                        {
                            addToError("<b >Profile name is "
                                    + mtp.getMtProfileName()
                                    + " imported failed because mt engine was modified type not supported!</b>");
                        }
                        if (mtp.getMtEngine() != null
                                && !"".equals(mtp.getMtEngine()))
                        {
                            mtpList.add(mtp);
                        }
                    }
                    if (keyArr[0]
                            .equalsIgnoreCase("MachineTranslationExtentInfo"))
                    {
                        MachineTranslationExtentInfo globalSightLocale = putDataIntoMTPExtenInfo(valueMap);
                        extenInfoList.add(globalSightLocale);
                    }
                }
            }

            if (mtpList.size() > 0)
                dataMap.put("MachineTranslationProfile", mtpList);

            if (extenInfoList.size() > 0)
                dataMap.put("MachineTranslationExtentInfo", extenInfoList);

            // Storing data
            storeDataToDatabase(dataMap);
        }

        private void storeDataToDatabase(Map<String, List> dataMap)
        {
            if (dataMap.isEmpty())
                return;
            int i = 0;
            int size = dataMap.keySet().size();

            try
            {
                if (dataMap.containsKey("MachineTranslationProfile"))
                {
                    i++;
                    storeMTPData(dataMap);
                    this.cachePercentage(i, size);
                    Thread.sleep(100);
                }

                if (dataMap.containsKey("MachineTranslationExtentInfo"))
                {
                    i++;
                    storeMTPExtentInfoData(dataMap);
                    this.cachePercentage(i, size);
                    Thread.sleep(100);
                }

                addMessage("<b>Imported successfully !</b>");
            }
            catch (Exception e)
            {
                // TODO Auto-generated catch block
                logger.error("Failed to import Machine Translation Profiles.",
                        e);
                addToError(e.getMessage());
            }
        }

        private void storeMTPData(Map<String, List> dataMap)
        {
            List<MachineTranslationProfile> mtpList = dataMap
                    .get("MachineTranslationProfile");
            MachineTranslationProfile mtp = null;
            try
            {
                for (int i = 0; i < mtpList.size(); i++)
                {
                    mtp = mtpList.get(i);
                    long oldId = mtp.getId();
                    String newName = getkMTPNewName(mtp.getMtProfileName(),
                            mtp.getCompanyid());
                    mtp.setMtProfileName(newName);
                    HibernateUtil.save(mtp);
                    long newId = selectNewId(newName, mtp.getCompanyid());
                    mtp = MTProfileHandlerHelper.getMTProfileById(String
                            .valueOf(newId));
                    mtpMap.put(oldId, mtp);
                }
            }
            catch (Exception e)
            {
                // TODO Auto-generated catch block
                String msg = "Upload MachineTranslationProfile data failed !";
                logger.warn(msg);
                addToError(msg);
            }
        }

        private void storeMTPExtentInfoData(Map<String, List> dataMap)
        {
            MachineTranslationExtentInfo extenInfo = null;
            List<MachineTranslationExtentInfo> extenInfoList = dataMap
                    .get("MachineTranslationExtentInfo");
            try
            {
                for (int i = 0; i < extenInfoList.size(); i++)
                {
                    extenInfo = extenInfoList.get(i);
                    long id = extenInfo.getId();
                    if (mtpExtentInfoMap.containsKey(id))
                    {
                        long value = mtpExtentInfoMap.get(id);
                        if (mtpMap.containsKey(value))
                        {
                            MachineTranslationProfile mtp = mtpMap.get(value);
                            extenInfo.setMtProfile(mtp);
                        }
                    }
                    HibernateUtil.save(extenInfo);
                }
            }
            catch (Exception e)
            {
                String msg = "Upload MachineTranslationExtentInfo data failed !";
                logger.warn(msg);
                addToError(msg);
            }
        }

        private MachineTranslationProfile putDataIntoMTP(
                Map<String, String> valueMap) throws ParseException
        {
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
					Locale.ENGLISH);
            dateFormat.setLenient(false);
            MachineTranslationProfile mtp = new MachineTranslationProfile();
            mtp.setMtEngine("");
            String keyField = null;
            String valueField = null;
            Set<String> valueKey = valueMap.keySet();
            Iterator itor = valueKey.iterator();
            while (itor.hasNext())
            {
                keyField = (String) itor.next();
                valueField = valueMap.get(keyField);
                if (keyField.equalsIgnoreCase("ID"))
                {
                    mtp.setId(Long.parseLong(valueField));
                }
                else if (keyField.equalsIgnoreCase("MT_PROFILE_NAME"))
                {
                    mtp.setMtProfileName(valueField);
                }
                else if (keyField.equalsIgnoreCase("MT_ENGINE"))
                {
                    for (int i = 0; i < engines.length; i++)
                    {
                        String _engine = engines[i].name();
                        if (_engine.equalsIgnoreCase(valueField))
                        {
                            mtp.setMtEngine(valueField);
                        }
                    }
                }
                else if (keyField.equalsIgnoreCase("DESCRIPTION"))
                {
                    mtp.setDescription(valueField);
                }
                else if (keyField.equalsIgnoreCase("MT_CONFIDENCE_SCORE"))
                {
                    mtp.setMtConfidenceScore(Long.parseLong(valueField));
                }
                else if (keyField.equalsIgnoreCase("URL"))
                {
                    mtp.setUrl(valueField);
                }
                else if (keyField.equalsIgnoreCase("PORT"))
                {
                    mtp.setPort(Integer.parseInt(valueField));
                }
                else if (keyField.equalsIgnoreCase("USERNAME"))
                {
                    mtp.setUsername(valueField);
                }
                else if (keyField.equalsIgnoreCase("PASSWORD"))
                {
                    mtp.setPassword(valueField);
                }
                else if (keyField.equalsIgnoreCase("CATEGORY"))
                {
                    mtp.setCategory(valueField);
                }
                else if (keyField.equalsIgnoreCase("ACCOUNTINFO"))
                {
                    mtp.setAccountinfo(valueField);
                }
                else if (keyField.equalsIgnoreCase("COMPANY_ID"))
                {
                    if (importToCompId != null && !importToCompId.equals("-1"))
                    {
                        mtp.setCompanyid(Long.parseLong(importToCompId));
                    }
                    else
                    {
                        mtp.setCompanyid(Long.parseLong(currentCompanyId));
                    }
                }
                else if (keyField.equalsIgnoreCase("TIMESTAMP"))
                {
                    Date timeDate = dateFormat.parse(valueField);
                    Timestamp dateTime = new Timestamp(timeDate.getTime());
                    mtp.setTimestamp(dateTime);
                }
                else if (keyField.equalsIgnoreCase("SHOW_IN_EDITOR"))
                {
                    mtp.setShowInEditor(Boolean.parseBoolean(valueField));
                }
                else if (keyField.equalsIgnoreCase("INCLUDE_MT_IDENTIFIERS"))
                {
                    mtp.setIncludeMTIdentifiers(Boolean
                            .parseBoolean(valueField));
                }
                else if (keyField.equalsIgnoreCase("MT_IDENTIFIER_LEADING"))
                {
                    mtp.setMtIdentifierLeading(valueField);
                }
                else if (keyField.equalsIgnoreCase("MT_IDENTIFIER_TRAILING"))
                {
                    mtp.setMtIdentifierTrailing(valueField);
                }
                else if (keyField.equalsIgnoreCase("IS_ACTIVE"))
                {
                    mtp.setActive(Boolean.parseBoolean(valueField));
                }
                else if (keyField.equalsIgnoreCase("EXTENT_JSON_INFO"))
                {
                    if (valueField == null || "".equals(valueField))
                    {
                        mtp.setJsonInfo(null);
                    }
                    else
                    {
                        mtp.setJsonInfo(valueField);
                    }
                }
            }
            return mtp;
        }

        private MachineTranslationExtentInfo putDataIntoMTPExtenInfo(
                Map<String, String> valueMap)
        {
            MachineTranslationExtentInfo extenInfo = new MachineTranslationExtentInfo();
            Long mtProfileId = null;
            String key = null;
            String value = null;
            Set<String> valueKey = valueMap.keySet();
            Iterator itor = valueKey.iterator();
            while (itor.hasNext())
            {
                key = (String) itor.next();
                value = valueMap.get(key);
                if (key.equalsIgnoreCase("ID"))
                {
                    extenInfo.setId(Long.parseLong(value));
                }
                else if (key.equalsIgnoreCase("MT_PROFILE_ID"))
                {
                    mtProfileId = Long.parseLong(value);
                }
                else if (key.equalsIgnoreCase("LANGUAGE_PAIR_CODE"))
                {
                    extenInfo.setLanguagePairCode(Long.parseLong(value));
                }
                else if (key.equalsIgnoreCase("LANGUAGE_PAIR_NAME"))
                {
                    extenInfo.setLanguagePairName(value);
                }
                else if (key.equalsIgnoreCase("DOMAIN_CODE"))
                {
                    extenInfo.setDomainCode(value);
                }
            }
            mtpExtentInfoMap.put(extenInfo.getId(), mtProfileId);
            return extenInfo;
        }

        private String getkMTPNewName(String filterName, Long companyId)
        {
            String hql = "select mtp.mtProfileName from MachineTranslationProfile "
                    + "  mtp where mtp.companyid=:companyid";
            Map map = new HashMap();
            map.put("companyid", companyId);
            List itList = HibernateUtil.search(hql, map);

            if (itList.contains(filterName))
            {
                for (int num = 1;; num++)
                {
                    String returnStr = null;
                    if (filterName.contains("_import_"))
                    {
                        returnStr = filterName.substring(0,
                                filterName.lastIndexOf('_'))
                                + "_" + num;
                    }
                    else
                    {
                        returnStr = filterName + "_import_" + num;
                    }
                    if (!itList.contains(returnStr))
                    {
                        return returnStr;
                    }
                }
            }
            else
            {
                return filterName;
            }
        }

        private Long selectNewId(String mtProfileName, Long companyId)
        {
            Map map = new HashMap();

            String hql = "select mtp.id from MachineTranslationProfile "
                    + "  mtp where mtp.companyid=:companyid and  mtp.mtProfileName=:mtProfileName ";

            map.put("companyid", companyId);
            map.put("mtProfileName", mtProfileName);

            Long id = (Long) HibernateUtil.getFirst(hql, map);

            return id;
        }

        private void cachePercentage(double per, int size)
        {
            int percentage = (int) (per * 100 / size);
            filter_percentage_map.put(sessionId, percentage);
        }

        private void addToError(String msg)
        {
            String former = filter_error_map.get(sessionId) == null ? ""
                    : filter_error_map.get(sessionId);
            filter_error_map.put(sessionId, former + "<p style='color:red'>"
                    + msg);
        }

        private void addMessage(String msg)
        {
            String former = filter_error_map.get(sessionId) == null ? ""
                    : filter_error_map.get(sessionId);
            filter_error_map.put(sessionId, former + "<p style='color:blue'>"
                    + msg);
        }
    }
}