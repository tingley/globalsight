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

package com.globalsight.machineTranslation.globalese;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.globalsight.connector.eloqua.util.Response;
import com.globalsight.diplomat.util.XmlUtil;
import com.globalsight.everest.webapp.pagehandler.administration.mtprofile.MTProfileConstants;
import com.google.gson.Gson;

public class GlobaleseMTUtil implements MTProfileConstants
{
    private static final Logger logger = Logger.getLogger(GlobaleseMTUtil.class);
//    private static long projectId = 0L;
    private static long fileIdx = 1;

    private static final String TARGET_REGEX = "<target[^>/]*>(.*?)</target>";
    private static final Pattern TARGET_PATTERN = Pattern.compile(TARGET_REGEX);
    
    private static final String TARGET_REGEX2 = "<target[^>/]*/>";
    private static final Pattern TARGET_PATTERN2 = Pattern.compile(TARGET_REGEX2);

    /**
     * Run a get request to test the Globalese connection
     * 
     * @param globClient
     * @return boolean
     */

    public static boolean testGlobaleseHost(Client globClient) throws Exception
    {
        int count = 0;
        boolean gotten = false;

        try
        {

            while (!gotten && count < 3)
            {
                count++;
                Response response = globClient.get("");
                if (response.statusCode == 200)
                    gotten = true;
            }

        }
        catch (Exception e)
        {
            String msg = "Invalid Globalese URL, Username or API key.";
            logger.warn(msg);
            logger.warn("Exception message is : " + e.getMessage());

            throw new Exception(msg);
        }
        return gotten;
    }

    /**
     * Run a get and post requests to translate a segment
     * 
     * @param globClient
     * @param groupId
     * @param engineId
     * @param srcLang
     *            - source langauge of the segment
     * @param tarLang
     *            - target language to be translated into
     * @param segment
     *            - segment or file that needs to be translated
     * @param waitTime
     *            - wait time after the request
     * @return String
     */
    public static String translate(Client globClient, long groupId, long engineId, String srcLang,
            String tarLang, String segment, int waitTime)
    {
        String[] segments = new String[1];
        segments[0] = segment;
        List<String> segs = batchTranslate(globClient, groupId, engineId, "", "", segments,
                waitTime);
        if (segs.size() > 0)
            return segs.get(0);

        return "";
    }

    public static GlobaleseEngine getEngine(Client globClient, long engineId)
    {
        String uri = "/engines/" + engineId;
        Response response = globClient.get(uri);

        if (response.statusCode == 200 || response.statusCode == 201)
        {
            String strResult = response.body;
            Gson gson = new Gson();
            return gson.fromJson(strResult, GlobaleseEngine.class);
        }

        return null;
    }

    private static String getLcoale(String locale)
    {
        int n = locale.indexOf("-");
        if (n > 0)
            return locale.substring(0, n);
        
        return locale;
    }
    
    public static List<String> batchTranslate(Client globClient, long groupId, long engineId,
            String srcLang, String tarLang, String[] segments, int waitTime)
    {
        GlobaleseEngine engine = getEngine(globClient, engineId);
        if (engine == null)
            return new ArrayList<>();

        for (int i = 3; i > 0; i--)
        {
            List<String> segs = doBatchTranslate(globClient, groupId, engineId, getLcoale(engine.getSource()),
                    getLcoale(engine.getTarget()), segments, waitTime);
            if (segs.size() > 0)
                return segs;
        }

        return new ArrayList<>();
    }

    private static List<String> doBatchTranslate(Client globClient, long groupId, long engineId,
            String srcLang, String tarLang, String[] segments, int waitTime)
    {
        List<String> translation = new ArrayList<>();

        // Empty segment
        if (segments == null || segments.length == 0)
        {
            return translation;
        }

        // Generate a project name
        // Project Name:
        // WelProject_g<GroupId>_e<EngineId>_<sourceLang>_<targetLang>
        String projectName = "WelProject_g" + groupId + "_e" + engineId + "_" + srcLang + "_"
                + tarLang;

        long projectId = getProjectId(globClient, projectName, groupId, engineId, srcLang, tarLang);

        // Project does not exist, create a new one
        if (projectId == 0)
            projectId = createProject(globClient, projectName, groupId, engineId, srcLang, tarLang);
        
        if (projectId == 0)
            return translation;

        String fileId = "";
        // create a translation file
        fileId = createTranslationFile(globClient, srcLang, tarLang, projectId);
        if (fileId.length() == 0)
        {
            // Failed to create a file
            return translation;
        }

        // if it is a file path
        String fileContent = getXlf(srcLang, tarLang, segments);
        
        // upload a translation file - POST
        if (!uploadFileForTranslation(globClient, fileId, fileContent))
            return translation;

        // translate the file
        if (!translateFile(globClient, fileId))
            return translation;

        // download translated file
        String result = downloadTranslatedFile(globClient, fileId, waitTime);

        // Cleanup - delete files from Globalese
        deleteTranslatedFile(globClient, fileId);

        // translated file is Xliff
        if (result != null && result.length() > 0)
        {
            List<String> prefixs = new ArrayList<>();
            
            boolean hasPrefix = false;
            for (String s : segments)
            {
                if (s.startsWith("<segment "))
                {
                    prefixs.add(s.substring(0, s.indexOf(">") + 1));
                    hasPrefix = true;
                }
            }
            
            int i = 0;
            int s1 = -1;
            int s2 = -1;
            String ss1 = null;
            
            Matcher m2 = TARGET_PATTERN2.matcher(result);
            Matcher m = TARGET_PATTERN.matcher(result);
            while (true)
            {
                if (s1 < 0)
                {
                    if (m.find())
                    {
                        ss1 = m.group(1);
                        s1 = m.start();
                    }
                }
                
                if (s2 < 0)
                {
                    if (m2.find())
                    {
                        s2 = m2.start();
                    }
                }
                
                if (s1 < 0 && s2 < 0)
                    break;
                
                if (s1 < 0)
                {
                    translation.add(segments[i]);
                    s2 = -1;
                    i++;
                    continue;
                }
                    
                
                if (s2 < 0)
                {
                    if (hasPrefix)
                    {
                        ss1 = prefixs.get(i) + ss1 + "</segment>";
                    }
                    translation.add(ss1);
                    s1 = -1;
                    ss1 = null;
                    i++;
                    continue;
                }
                
                if (s1 < s2)
                {
                    if (hasPrefix)
                    {
                        ss1 = prefixs.get(i) + ss1 + "</segment>";
                    }
                    translation.add(ss1);
                    s1 = -1;
                    ss1 = null;
                    i++;
                    continue;
                }
                else
                {
                    translation.add(segments[i]);
                    s2 = -1;
                    i++;
                }
            }
        }

        return translation;
    }

    /**
     * Retrieve the project id if the project already exist
     * 
     * @param globClient
     * @param projectName
     * @param groupId
     * @param engineId
     * @param srcLang
     *            - source langauges of the segment
     * @param tarLang
     *            - language to be translated into
     * @return long
     */

    private static long getProjectId(Client globClient, String projectName, long groupId,
            long engineId, String srcLang, String tarLang)
    {
        // send a get request to get ALL the projects with given source, target
        // and group
        String relativeUrl = "/projects/?source=" + srcLang + "&target=" + tarLang + "&group="
                + groupId;

        // String jsonList = runGetRequest(globClient, relativeUrl);
        GlobaleseProject[] projectList = null;
        Gson gson = new Gson();
        for (int i = 3; i > 0; i--)
        {
            Response response = globClient.get(relativeUrl);
            if (response != null)
            {
                String jsonList = response.body;
                projectList = (GlobaleseProject[]) gson.fromJson(jsonList,
                        GlobaleseProject[].class);
                if (projectList != null)
                    break;
            }
        }

        if (projectList == null)
            return 0;

        for (int l = 0; l < projectList.length; l++)
        {
            // For each project, check the the engine id, if they match then we
            // found the project.
            if (engineId == projectList[l].getEngine()
                    && projectName.equals(projectList[l].getName()))
            {
                return projectList[l].getId();
            }
        }
        return 0;
    }

    /**
     * Create a new project and validate creation
     * 
     * @param globClient
     * @param projectName
     * @param groupId
     * @param engineId
     * @param srcLang
     *            - source langauges of the segment
     * @param tarLang
     *            - language to be translated into
     * @return void
     */

    private static long createProject(Client globClient, String projectName, long groupId,
            long engineId, String srcLang, String tarLang)
    {
        // create a new project if the current projectId = 0

        fileIdx = 1; // reset

        String jsonString = "";
        JSONObject ob = new JSONObject();
        try
        {
            ob.put("name", projectName);
            ob.put("source-language", srcLang);
            ob.put("target-language", tarLang);
            ob.put("group", groupId);
            ob.put("engine", engineId);
            jsonString = ob.toString();
        }
        catch (JSONException e)
        {
            logger.error(e);
        }

        String uri = "/projects";
        Response response = globClient.post(uri, jsonString);

        if (response.statusCode == 200 || response.statusCode == 201)
        {
            // success
            String strResult = response.body;

            // read the response JSON and save the project Id
            Gson gson = new Gson();
            GlobaleseProject project = (GlobaleseProject) gson.fromJson(strResult,
                    GlobaleseProject.class);
            return project.getId();
        }
        
        return 0;
    }

    /**
     * Create a translation file in Globalese
     * 
     * @param globClient
     * @param srcLang
     *            - source langauges of the segment
     * @param tarLang
     *            - language to be translated into
     * @return String
     */

    private static String createTranslationFile(Client globClient, String srcLang, String tarLang, long projectId)
    {

        // File Name: Xliff<projectId>_<sourceLang>_<targetLang>_<fileIdx>
        String fileName = "gg.xlf";
        String fileId = "";
        String jsonString = "";
        JSONObject ob = new JSONObject();
        try
        {
            ob.put("project", projectId);
            ob.put("name", fileName);
            ob.put("source-language", srcLang);
            ob.put("target-language", tarLang);
            jsonString = ob.toString();
        }
        catch (JSONException e)
        {
            logger.error(e);
        }

        String uri = "/translation-files";
        Response response = globClient.post(uri, jsonString);
        int statusCode = response.statusCode;

        if (statusCode == 200 || statusCode == 201)
        {
            String strResult = response.body;

            // read the response JSON and save the project Id
            Gson gson = new Gson();
            GlobaleseFile file = (GlobaleseFile) gson.fromJson(strResult, GlobaleseFile.class);
            fileId = file.getId();

            fileIdx++;
        }
        else
        {
            // FAILED
        }
        return fileId;
    }

    /**
     * Upload the file content to be translated into Globalese project
     * 
     * @param globClient
     * @param fileId
     * @param fileContent
     * @return boolean
     */

    private static boolean uploadFileForTranslation(Client globClient, String fileId,
            String fileContent)
    {
        String uri = "/translation-files/" + fileId;
        Response response = globClient.post(uri, fileContent);

        if (response.statusCode == 200 || response.statusCode == 201)
            return true;
        return false;
    }

    /**
     * Send a request to Globalese to MT the file with given id
     * 
     * @param globClient
     * @param fileId
     * 
     * @return boolean
     */

    private static boolean translateFile(Client globClient, String fileId)
    {
        String uri = "/translation-files/" + fileId + "/translate";
        Response response = globClient.post(uri, "");

        if (response.statusCode == 200 || response.statusCode == 201)
            return true;
        return false;
    }

    /**
     * Download translated file if the status is translated
     * 
     * @param globClient
     * @param fileId
     * @param waitTime
     * @return String
     */

    private static String downloadTranslatedFile(Client globClient, String fileId, int waitTime)
    {

        // Get a translated file - GET
        String uri = "/translation-files/" + fileId + "/download?state=translated";

        String strResult = "";
        int i = 1;

        try
        {
            while (i <= 20)
            {

                // wait for waitTime seconds
                Thread.sleep(waitTime * 1000); // if waitTime = 30 then wait 30
                                               // seconds

                Response response = globClient.get(uri);

                int statusCode = response.statusCode;
                if (statusCode == 200)
                {
                    strResult = response.body;
                    return strResult;
                }
                else
                {
                    i++;
                }
            }
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt(); // set interrupt flag
            String msg = "Failed to download translated file.";
            logger.warn(msg);
            logger.warn("Exception message is : " + e.getMessage());
        }

        return strResult;
    }

    /**
     * Delete the file from the Globalese project - cleanup
     * 
     * @param globClient
     * @param fileId
     * @return void
     */

    private static void deleteTranslatedFile(Client globClient, String fileId)
    {
        String uri = "/translation-files/" + fileId;
        globClient.delete(uri);
    }

    /**
     * Create an Xliff for translation
     * 
     * @param srcLang
     * @param tarLang
     * @param segment
     * 
     * @return String
     */
    private static String getXlf(String srcLang, String tarLang, String[] segments)
    {
        StringBuffer sb = new StringBuffer();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n");
        sb.append("<xliff xmlns=\"urn:oasis:names:tc:xliff:document:1.2\" version=\"1.2\">\r\n");
        sb.append("<file original=\"globalsight\" source-language=\"").append(srcLang)
                .append("\" target-language=\"").append(tarLang)
                .append("\" datatype=\"x-undefined\">\r\n");
        sb.append("<header>\r\n");
        sb.append("</header>\r\n");
        sb.append("<body>\r\n");
        for (String segment : segments)
        {
            sb.append("  <trans-unit id=\"37323\">\r\n");
            sb.append("  <source>").append(XmlUtil.escapeString(segment)).append("</source>\r\n");
            sb.append("  <target state=\"new\"></target>\r\n");
            sb.append("  </trans-unit>\r\n");
        }
        sb.append("</body>\r\n");
        sb.append("</file>\r\n");
        sb.append("</xliff>");

        return sb.toString();
    }

    /**
     * Deletes the project with the specified id
     * 
     * @param globClient
     * @return void
     */
    public static void deleteProject(Client globClient, long projectId)
    {
        if (projectId == 0)
            return;
        String uri = "/projects/" + projectId;
        globClient.delete(uri);
    }

    /**
     * Load UTF-8 file content into a string
     * 
     * @param file
     * @return String
     */
    private static String fileString(File file)
    {

        String content = "";
        try
        {
            content = new String(Files.readAllBytes(file.toPath()), Charset.forName("UTF-8"));
        }
        catch (IOException e)
        {
            String msg = "Failed to open the UTF8 file: " + file.getAbsolutePath();
            logger.warn(msg);
            logger.warn("Exception message is : " + e.getMessage());
        }
        return content;
    }
}
