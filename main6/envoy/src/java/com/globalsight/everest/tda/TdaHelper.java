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

package com.globalsight.everest.tda;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.zip.ZipInputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import com.globalsight.everest.foundation.TDATM;
import com.globalsight.everest.tuv.Tu;
import com.globalsight.everest.tuv.Tuv;
import com.globalsight.ling.docproc.extractor.xml.GsDOMParser;
import com.globalsight.ling.tm2.TmCoreManager;
import com.globalsight.util.StringUtil;

public class TdaHelper
{
    private static Logger s_logger = Logger.getLogger(TdaHelper.class);
    private static String appKey = "C1927572";

    /*
     * Check the TDA Url and user name and password is correct or not.
     */
    public String loginCheck(String hostName, String userName, String password)
    {
        int timeoutConnection = 8000;
        HttpParams httpParameters = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParameters,
                timeoutConnection);
        DefaultHttpClient httpclient = new DefaultHttpClient(httpParameters);

        String loginUrl = new String();
        String errorInfo = new String();
        hostName = hostName.trim();
        userName = userName.trim();

        if (hostName.indexOf("http://") < 0)
        {
            loginUrl = "http://" + hostName;
        }
        else
        {
            loginUrl = hostName;
        }

        if (hostName.lastIndexOf("/") == (hostName.length() - 1))
        {
            loginUrl = loginUrl + "auth_key.json?action=login";
        }
        else
        {
            loginUrl = loginUrl + "/auth_key.json?action=login";
        }

        try
        {
            HttpPost httpost = new HttpPost(loginUrl);
            MultipartEntity reqEntity = new MultipartEntity();
            StringBody nameBody = new StringBody(userName);
            StringBody passwordBody = new StringBody(password);
            StringBody appKeyBody = new StringBody(appKey);
            reqEntity.addPart("auth_username", nameBody);
            reqEntity.addPart("auth_password", passwordBody);
            reqEntity.addPart("auth_app_key", appKeyBody);
            httpost.setEntity(reqEntity);
            HttpResponse response = httpclient.execute(httpost);
            StatusLine sl = response.getStatusLine();

            if (sl.getStatusCode() == 404)
            {
                errorInfo = "The TDA URL is not correct.";
            }
            else if (sl.getStatusCode() == 401)
            {
                errorInfo = "The username and password given are not a valid TDA login.";
            }
            else if (sl.getStatusCode() == 201)
            {
                errorInfo = "ture";
            }
            else
            {
                errorInfo = "The TDA configuration is not correct!";
            }
        }
        catch (Exception e)
        {
            s_logger.info("Can not connect TDA server:" + e.getMessage());
            errorInfo = "Can not connect TDA server.";
        }

        return errorInfo;
    }

    /*
     * Check the TDA Url and user name and password is correct or not.
     */
    public void loginCheck(TDATM tda)
    {
        loginCheck(tda.getHostName(), tda.getUserName(), tda.getPassword());
    }

    /*
     * Leverage segment from TDA TM
     */
    public void leverageTDA(TDATM tda, File needLeverageXliffFile,
            String storePath, String fileName, String sourceLocal,
            String targetLocal)
    {
        int timeoutConnection = 15000;

        HttpParams httpParameters = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParameters,
                timeoutConnection);

        DefaultHttpClient httpclient = new DefaultHttpClient(httpParameters);
        String loginUrl = new String();

        if (tda.getHostName().indexOf("http://") < 0)
        {
            loginUrl = "http://" + tda.getHostName();
        }
        else
        {
            loginUrl = tda.getHostName();
        }

        if (tda.getHostName().lastIndexOf("/") < (tda.getHostName().length() - 1))
        {
            loginUrl = loginUrl + "/";
        }

        try
        {
            // Judge if the TDA server has the source and target language
            HttpGet lanGet = new HttpGet(loginUrl + "lang/"
                    + sourceLocal.toLowerCase() + ".json?auth_username="
                    + tda.getUserName() + "&auth_password=" + tda.getPassword()
                    + "&auth_app_key=" + appKey);
            HttpResponse lanRes = httpclient.execute(lanGet);
            StatusLine stl = lanRes.getStatusLine();

            if (stl.getStatusCode() != 200)
            {
                loggerTDAInfo(stl, sourceLocal.toString());

                return;
            }
            lanGet.abort();
            HttpGet lanGet2 = new HttpGet(loginUrl + "lang/"
                    + targetLocal.toLowerCase() + ".json?auth_username="
                    + tda.getUserName() + "&auth_password=" + tda.getPassword()
                    + "&auth_app_key=" + appKey);
            HttpResponse lanRes2 = httpclient.execute(lanGet2);
            stl = lanRes2.getStatusLine();

            if (stl.getStatusCode() != 200)
            {
                loggerTDAInfo(stl, targetLocal.toString());

                return;
            }
            lanGet2.abort();

            HttpPost httpPost = new HttpPost(loginUrl
                    + "leverage.json?action=create");
            FileBody fileBody = new FileBody(needLeverageXliffFile);
            StringBody nameBody = new StringBody(tda.getUserName());
            StringBody passwordBody = new StringBody(tda.getPassword());
            StringBody appKeyBody = new StringBody(appKey);
            StringBody srcBody = new StringBody(sourceLocal.toLowerCase());
            StringBody trBody = new StringBody(targetLocal.toLowerCase());
            StringBody confirmBody = new StringBody("true");
            MultipartEntity reqEntity = new MultipartEntity();

            reqEntity.addPart("file", fileBody);
            reqEntity.addPart("auth_username", nameBody);
            reqEntity.addPart("auth_password", passwordBody);
            reqEntity.addPart("auth_app_key", appKeyBody);
            reqEntity.addPart("source_lang", srcBody);
            reqEntity.addPart("target_lang", trBody);
            reqEntity.addPart("confirm", confirmBody);

            httpPost.setEntity(reqEntity);

            HttpResponse response = httpclient.execute(httpPost);
            HttpEntity entity = response.getEntity();
            StatusLine sl = response.getStatusLine();

            if (sl.getStatusCode() != 201)
            {
                loggerTDAInfo(stl, null);

                return;
            }

            JSONObject jso = new JSONObject(EntityUtils.toString(entity));
            JSONArray lev = jso.getJSONArray("leverage");

            httpPost.abort();

            if (lev.length() > 0)
            {
                JSONObject obj = lev.getJSONObject(0);
                String states = obj.getString("state");

                // waiting the "not ready" state becoming "ready" state
                Thread.sleep(3 * 1000);
                int i = 0;
                if (!states.equals("ready"))
                {
                    boolean flag = true;

                    while (flag)
                    {
                        if (i > 40)
                        {
                            s_logger.info("Get TDA job status overtime. TDA job id:"
                                    + obj.getInt("id"));
                            s_logger.info("TDA leveraging waited time:"
                                    + (40 * 3) + " seconds!");
                            return;
                        }

                        i++;
                        HttpGet httpget = new HttpGet(loginUrl + "leverage/"
                                + obj.getInt("id") + ".json?auth_username="
                                + tda.getUserName() + "&auth_password="
                                + tda.getPassword() + "&auth_app_key=" + appKey);

                        response = httpclient.execute(httpget);
                        StatusLine status = response.getStatusLine();

                        if (status.getStatusCode() != 200)
                        {
                            s_logger.info("Get TDA job status error, please confirm the TDA url is correct or not! TDA job id:"
                                    + obj.getInt("id"));
                            return;
                        }

                        entity = response.getEntity();
                        JSONObject getObj = new JSONObject(
                                EntityUtils.toString(entity));

                        if (getObj.getJSONObject("leverage").getString("state")
                                .equals("ready"))
                        {
                            s_logger.info("TDA leveraging waited time:"
                                    + (i * 3) + " seconds!");
                            flag = false;
                        }
                        else
                        {
                            Thread.sleep(3 * 1000);
                        }

                        httpget.abort();
                    }
                }

                HttpPost httpPost2 = new HttpPost(loginUrl + "leverage/"
                        + obj.getInt("id")
                        + ".json?action=approve&auth_username="
                        + tda.getUserName() + "&auth_password="
                        + tda.getPassword() + "&auth_app_key=" + appKey);

                response = httpclient.execute(httpPost2);
                entity = response.getEntity();
                httpPost2.abort();

                HttpGet httpGet = new HttpGet(loginUrl + "leverage/"
                        + obj.getString("id")
                        + "/result.xlf.zip?auth_username=" + tda.getUserName()
                        + "&auth_password=" + tda.getPassword()
                        + "&auth_app_key=" + appKey);
                HttpResponse response2 = httpclient.execute(httpGet);
                entity = response2.getEntity();

                ZipInputStream fs = new ZipInputStream(entity.getContent());

                int BUFFER = 2048;

                byte data[] = new byte[BUFFER];
                int count;

                while (fs.getNextEntry() != null)
                {
                    FileOutputStream fos = new FileOutputStream(storePath
                            + File.separator + fileName);
                    BufferedOutputStream dest = new BufferedOutputStream(fos,
                            BUFFER);

                    while ((count = fs.read(data, 0, BUFFER)) != -1)
                    {
                        dest.write(data, 0, count);
                    }

                    dest.flush();
                    dest.close();
                }

                httpGet.abort();

                s_logger.info("Leverage TDA TM success, TDA id:"
                        + obj.getString("id"));
            }
        }
        catch (Exception e)
        {
            s_logger.error("TDA leverage process error:" + e.getMessage());
        }
    }

    /*
     * Write log in TDA leverage process.
     */
    private void loggerTDAInfo(StatusLine status, String local)
    {
        if (status.getStatusCode() == 400)
        {
            if (status.getReasonPhrase() != null)
            {
                String reason = status.getReasonPhrase().trim();

                if (reason.equals("bad_request"))
                {
                    s_logger.info("TDA connect error, please confirm the TDA url is correct or not!");
                }
                else if (reason.equals("invalid_params"))
                {
                    s_logger.info("TDA request parameters were missing, of the wrong types, or otherwise incorrect!");
                }
                else if (reason.equals("no_such_resource"))
                {
                    s_logger.info("TDA server does not has " + local
                            + " in its local list!");
                }
            }
        }
        else if (status.getStatusCode() == 401)
        {
            s_logger.info("The TDA request requires authentication but no credentials were supplied, please check the user name and password!");
        }
        else if (status.getStatusCode() == 403)
        {
            s_logger.info("This TDA user is not allowed to make this request!");
        }
        else if (status.getStatusCode() == 404)
        {
            s_logger.info("The TDA resource named in the URL does not exist or is invisible to this user!");
        }
        else if (status.getStatusCode() == 500)
        {
            s_logger.info("TDA system error. TDA will investigate the problem!");
        }
    }

    /*
     * Extract the leverage TDA file, and sort the leverage results order by
     * match percent
     */
    public ArrayList extract(FileInputStream file, long tmProfileThreshold)
    {
        try
        {
            GsDOMParser parser = new GsDOMParser();
            InputStreamReader inputStream = new InputStreamReader(file, "UTF-8");
            // parse and create DOM tree
            Document document = parser.parse(new InputSource(inputStream));
            // traverse the DOM tree
            ArrayList matchList = new ArrayList();

            domNodeVisitor(document, matchList, tmProfileThreshold);

            ArrayList sortedList = new ArrayList();

            if (matchList != null && matchList.size() > 1)
            {
                sortedList = groupTuListAndSort(matchList);
            }
            else
            {
                sortedList = matchList;
            }

            return sortedList;
        }
        catch (Exception e)
        {
            s_logger.error("TDA extract xliff file error:" + e.getMessage());
        }

        return null;
    }

    private void domNodeVisitor(Node p_node, ArrayList matchList,
            long tmProfileThreshold)
    {
        HashMap TDAResults = new HashMap();
        LeverageTDAResult tdaResult = null;

        if (matchList.size() > 0)
        {
            tdaResult = (LeverageTDAResult) matchList.get(matchList.size() - 1);
        }

        while (true)
        {
            if (p_node == null)
            {
                return;
            }

            switch (p_node.getNodeType())
            {
                case Node.DOCUMENT_NODE:
                    domNodeVisitor(p_node.getFirstChild(), matchList,
                            tmProfileThreshold);
                    return;
                case Node.ELEMENT_NODE:
                    String nodeName = p_node.getNodeName().toLowerCase();

                    if (nodeName.equals("alt-trans"))
                    {
                        String tuid = "-1";

                        NamedNodeMap parentAttrs = p_node.getParentNode()
                                .getAttributes();

                        for (int i = 0; i < parentAttrs.getLength(); ++i)
                        {
                            Node att = parentAttrs.item(i);
                            String attname = att.getNodeName();
                            String value = att.getNodeValue();

                            if (attname.equals("id"))
                            {
                                tuid = value;
                            }
                        }

                        NamedNodeMap attrs = p_node.getAttributes();
                        boolean fromTDA = false;
                        String percentValue = "";

                        for (int i = 0; i < attrs.getLength(); ++i)
                        {
                            Node att = attrs.item(i);
                            String attname = att.getNodeName();
                            String value = att.getNodeValue();

                            if (attname.equals("tda:provider"))
                            {
                                fromTDA = true;
                            }

                            if (attname.equals("match-quality"))
                            {
                                percentValue = value;
                            }

                        }

                        if (fromTDA)
                        {
                            if (PecentToInt(percentValue) > tmProfileThreshold
                                    || PecentToInt(percentValue) == tmProfileThreshold)
                            {
                                tdaResult = new LeverageTDAResult();
                                tdaResult.setTuid(Long.parseLong(tuid));
                                tdaResult.setMatchPercent(percentValue);
                                matchList.add(tdaResult);
                            }
                            else
                            {
                                p_node = p_node.getNextSibling();
                                break;
                            }
                        }
                    }

                    domNodeVisitor(p_node.getFirstChild(), matchList,
                            tmProfileThreshold);
                    p_node = p_node.getNextSibling();
                    break;
                case Node.TEXT_NODE:
                    nodeName = p_node.getNodeName().toLowerCase();

                    if (p_node.getParentNode() != null
                            && p_node.getParentNode().getParentNode() != null)
                    {
                        String parentNodeName = p_node.getParentNode()
                                .getNodeName().toLowerCase();
                        String grandNodeName = p_node.getParentNode()
                                .getParentNode().getNodeName().toLowerCase();

                        if (grandNodeName.equals("alt-trans")
                                && parentNodeName.equals("target"))
                        {
                            if (tdaResult != null)
                            {
                                tdaResult.setResultText(p_node.getNodeValue());
                            }
                        }
                        else if (grandNodeName.equals("alt-trans")
                                && parentNodeName.equals("source"))
                        {
                            if (tdaResult != null)
                            {
                                tdaResult.setSourceText(p_node.getNodeValue());
                            }
                        }
                    }

                    p_node = p_node.getNextSibling();
                    break;
            }
        }
    }

    /*
     * Remove the "%" from string and parse it into Interger.
     */
    public static int PecentToInt(String percentValue)
    {
        percentValue = percentValue.replaceAll("%", "");
        int newValue = 0;

        try
        {
            double temp = Double.parseDouble(percentValue);
            newValue = Integer.parseInt(new java.text.DecimalFormat("0")
                    .format(temp));
        }
        catch (Exception e)
        {
        }

        return newValue;
    }

    /*
     * Remove the "%" from string and parse it into Double.
     */
    public static double PecentToDouble(String percentValue)
    {
        double newValue = 0;

        try
        {
			if (StringUtil.isNotEmpty(percentValue))
			{
				percentValue = percentValue.replaceAll("%", "");
				newValue = Double.parseDouble(percentValue);
			}
        }
        catch (Exception e)
        {
        }

        return newValue;
    }

    /*
     * Group the TDA leverage results by TU id, and order every group elements
     * by match percent
     */
    private ArrayList groupTuListAndSort(ArrayList matchList)
    {
        ArrayList sortedArray = new ArrayList();
        ArrayList newArray = new ArrayList();

        newArray.add(matchList.get(0));

        for (int i = 0; i < matchList.size() - 1; i++)
        {
            LeverageTDAResult tdaResult1 = (LeverageTDAResult) matchList.get(i);
            LeverageTDAResult tdaResult2 = (LeverageTDAResult) matchList
                    .get(i + 1);

            if (tdaResult1.getTuid() == tdaResult2.getTuid())
            {
                newArray.add(tdaResult2);
            }
            else
            {
                sortMatchList(newArray);
                sortedArray.addAll(newArray);
                newArray = new ArrayList();
                newArray.add(matchList.get(i + 1));
            }

            if (i == matchList.size() - 2)
            {
                sortMatchList(newArray);
                sortedArray.addAll(newArray);
            }
        }

        return sortedArray;
    }

    /*
     * Sort array by match percent and remove the segment whose match percent
     * and match content is all same
     */
    private void sortMatchList(ArrayList matchList)
    {
        for (int i = 0; i < matchList.size() - 1; i++)
        {
            LeverageTDAResult tdaResult1 = (LeverageTDAResult) matchList.get(i);

            for (int j = i + 1; j < matchList.size(); j++)
            {
                LeverageTDAResult tdaResult2 = (LeverageTDAResult) matchList
                        .get(j);

                if (PecentToInt(tdaResult1.getMatchPercent()) < PecentToInt(tdaResult2
                        .getMatchPercent()))
                {
                    LeverageTDAResult temp = tdaResult1;
                    tdaResult1 = tdaResult2;
                    tdaResult2 = temp;
                }

                tdaResult1
                        .setOrderNum(TmCoreManager.LM_ORDER_NUM_START_TDA + i);
                tdaResult2
                        .setOrderNum(TmCoreManager.LM_ORDER_NUM_START_TDA + j);
                matchList.set(i, tdaResult1);
                matchList.set(j, tdaResult2);
            }
        }

        // remember the segment whose match percent and match content is all
        // same
        ArrayList<LeverageTDAResult> sameArray = new ArrayList<LeverageTDAResult>();

        for (int i = 0; i < matchList.size() - 1; i++)
        {
            LeverageTDAResult tdaResult1 = (LeverageTDAResult) matchList.get(i);

            for (int j = i + 1; j < matchList.size(); j++)
            {
                LeverageTDAResult tdaResult2 = (LeverageTDAResult) matchList
                        .get(j);

                if (PecentToInt(tdaResult1.getMatchPercent()) == PecentToInt(tdaResult2
                        .getMatchPercent())
                        && tdaResult1.getResultText().equals(
                                tdaResult2.getResultText()))
                {
                    if (!sameArray.contains(tdaResult2))
                    {
                        sameArray.add(tdaResult2);
                    }
                }
            }
        }

        matchList.removeAll(sameArray);
    }

    /*
     * Write TDA leverage results into file.
     */
    public void WriteTDAXliffFile(OutputStreamWriter m_outputStream,
            String sLocale, String tLocale, HashMap needHitMTTuTuvMap)
            throws IOException
    {
        writeXlfHeader(m_outputStream);
        writeTDADocumentHeader(m_outputStream, sLocale, tLocale);
        writeTranslationUnit(m_outputStream, needHitMTTuTuvMap);
        writeXlfEnd(m_outputStream);
    }

    private void writeXlfHeader(OutputStreamWriter m_outputStream)
            throws IOException
    {
        String m_strEOL = "\r\n";
        m_outputStream.write("<?xml version=\"1.0\"?>");
        m_outputStream.write(m_strEOL);
        m_outputStream.write("<xliff version=\"1.2\">");
        m_outputStream.write(m_strEOL);
    }

    private void writeXlfEnd(OutputStreamWriter m_outputStream)
            throws IOException
    {
        String m_strEOL = "\r\n";
        m_outputStream.write("</body>");
        m_outputStream.write(m_strEOL);
        m_outputStream.write("</file>");
        m_outputStream.write(m_strEOL);
        m_outputStream.write("</xliff>");
    }

    /*
     * Writes the document header for TDA xliff file
     * 
     * @added by walter
     */
    private void writeTDADocumentHeader(OutputStreamWriter m_outputStream,
            String sLocale, String tLocale) throws IOException
    {
        String m_strEOL = "\r\n";
        String m_space = "  ";
        m_outputStream.write("<file ");
        m_outputStream.write("original=" + str2DoubleQuotation("None"));
        m_outputStream.write(m_space);
        m_outputStream.write("source-language="
                + str2DoubleQuotation(sLocale.replace("_", "-")));
        m_outputStream.write(m_space);
        m_outputStream.write("target-language="
                + str2DoubleQuotation(tLocale.replace("_", "-")));
        m_outputStream.write(m_space);
        m_outputStream.write(">");
        m_outputStream.write(m_strEOL);
        m_outputStream.write("<body>");
        m_outputStream.write(m_strEOL);
    }

    private void writeTranslationUnit(OutputStreamWriter m_outputStream,
            HashMap needHitMTTuTuvMap) throws IOException
    {
        // put all tus into array.
        Object[] key_tus = needHitMTTuTuvMap.keySet().toArray();
        Tu[] tusInArray = new Tu[key_tus.length];

        for (int key = 0; key < key_tus.length; key++)
        {
            tusInArray[key] = (Tu) key_tus[key];
        }

        // put all tuvs into array
        Object[] value_tuvs = needHitMTTuTuvMap.values().toArray();
        Tuv[] tuvsInArray = new Tuv[value_tuvs.length];
        for (int value = 0; value < value_tuvs.length; value++)
        {
            tuvsInArray[value] = (Tuv) value_tuvs[value];
        }

        for (int i = 0; i < tuvsInArray.length; i++)
        {
            Tu tu = (Tu) key_tus[i];
            Tuv tuv = (Tuv) value_tuvs[i];
            String m_strEOL = "\r\n";
            String m_space = "  ";
            m_outputStream.write("<trans-unit id=\"" + tu.getId() + "\"");
            m_outputStream.write(">");
            m_outputStream.write(m_strEOL);
            m_outputStream.write("<source>");
            m_outputStream.write(tuv.getGxmlExcludeTopTags());
            m_outputStream.write("</source>");
            m_outputStream.write(m_strEOL);
            m_outputStream.write("<target></target>");
            m_outputStream.write(m_strEOL);
            m_outputStream.write("</trans-unit>");
            m_outputStream.write(m_strEOL);
        }
    }

    /*
     * parse string to "string"
     */
    private String str2DoubleQuotation(String str)
    {
        String result = null;
        result = new StringBuffer().append("\"").append(str).append("\"")
                .toString();
        return result;
    }
}
