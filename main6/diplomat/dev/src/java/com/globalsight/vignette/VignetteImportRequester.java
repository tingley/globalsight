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
package com.globalsight.vignette;

import java.net.URL;
import java.net.URLConnection;
import com.globalsight.ling.common.URLEncoder;
import java.util.Enumeration;
import java.io.OutputStreamWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Vector;
import java.util.Enumeration;

/**
 * Helper class to do an HTTP Post to the
 * VignetteImportServlet running in CAP,
 * containing the information needed to import a file
 * from Vignette.
 */
public class VignetteImportRequester
{
    private URL m_url; //the URL for the VignetteImportServlet
    private StringBuffer m_post; //the HTTP string posted to the VignetteImportServlet

    //constants for the parameter names in the request
    public static final String SRC_MID="SrcMid";
    public static final String PATH="Path";
    public static final String FILEPROFILEID="FileProfileId";
    public static final String TARGET_PROJECT_MID="TargetProjectMid";
    public static final String RETURN_STATUS="ReturnStatus";
    public static final String VERSION_FLAG="VersionFlag";
    public static final String JOB_NAME="JobName";

    private static final String EQUALS="=";
    private static final String AMP="&";

    /**
     * Creates a VignetteImportRequester set to post
     * an import request to the given URL
     * 
     * @param p_url  -- the URL of the VignetteImportServlet
     */
    public VignetteImportRequester(URL p_url)
    {
        m_url = p_url;
    }

    /**
     * Sets the data that will be posted to the
     * VignetteImportServlet
     * 
     * @param p_items -- enumeration of Item objects
     * @param p_fileProfileId -- the file profile id
     * @param p_targetProjectMid  -- target project mid
     * @param p_jobName -- job name
     * @param p_returnStatus -- return status
     * @param p_versionFlag -- version flag
     */
    public void setImportData (Enumeration p_items, String p_fileProfileId, String p_targetProjectMid,
                          String p_jobName, String p_returnStatus, String p_versionFlag)
    {
        m_post = new StringBuffer();
        while (p_items.hasMoreElements())
        {
            Item item = (Item) p_items.nextElement();
            addParameter(SRC_MID,item.getMid());
            m_post.append(AMP);
            addParameter(PATH,item.getPath());
            m_post.append(AMP);
        }
        addParameter(FILEPROFILEID,p_fileProfileId);
        m_post.append(AMP);
        addParameter(TARGET_PROJECT_MID,p_targetProjectMid);
        m_post.append(AMP);
        addParameter(JOB_NAME,p_jobName);
        m_post.append(AMP);
        addParameter(RETURN_STATUS,p_returnStatus);
        m_post.append(AMP);
        addParameter(VERSION_FLAG,p_versionFlag);
    }


    /**
     * Performs the HTTP Post to the proper URL.
     * 
     * @exception Exception
     */
    public void upload() throws Exception
    {
        URLConnection conn = m_url.openConnection();
        conn.setDoOutput(true);

        //actually do the HTTP POST here
        OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream(), "UTF8");
        wr.write(m_post.toString());
        wr.flush();
        wr.close();

        //read the output of the HTTP POST otherwise it may cause socket problems
        String l = null;
        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        while ((l = rd.readLine()) != null)
        {
            //System.out.println(l);
        }
        rd.close();
    }

    /**
     * Adds the parameter and value to the HTTP Request
     * 
     * @param p_parameter -- the parameter
     * @param p_value -- the value
     */
    private void addParameter(String p_parameter, String p_value)
    {
        p_parameter = URLEncoder.encode(p_parameter, "UTF-8");
        p_value = URLEncoder.encode(p_value, "UTF-8");
        m_post.append(p_parameter);
        m_post.append(EQUALS);
        m_post.append(p_value);
    }



    /**
     * Tests the VignetteImportRequester by posting
     * dummy data to the VignetteImportServlet
     * 
     * @param arg
     */
    public static void main(String arg[]) throws Exception
    {
        URL url = new URL("http://localhost:7001/VignetteImportServlet");
        VignetteImportRequester vir = new VignetteImportRequester(url);
        
        Vector items = new Vector();
        items.add(new Item("path1","srcmid1","status1"));
        items.add(new Item("path2","srcmid2","status2"));
        
        String fileProfileId = "1001";
        String jobName = "vignDummy";
        String targetProjectMid = "trgMid";
        String returnStatus = "returnStat";
        String versionFlag = "verFlag";
        
        vir.setImportData(items.elements(),
                          fileProfileId,
                          targetProjectMid,
                          jobName,
                          returnStatus,
                          versionFlag);

        vir.upload();
    }
}

