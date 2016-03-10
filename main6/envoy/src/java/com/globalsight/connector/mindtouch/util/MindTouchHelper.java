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
package com.globalsight.connector.mindtouch.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.CookieOrigin;
import org.apache.http.cookie.CookieSpec;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.cookie.MalformedCookieException;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.cookie.BestMatchSpecFactory;
import org.apache.http.impl.cookie.BrowserCompatSpec;
import org.apache.http.impl.cookie.BrowserCompatSpecFactory;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.json.JSONObject;
import org.xml.sax.InputSource;

import com.globalsight.connector.mindtouch.MindTouchManager;
import com.globalsight.connector.mindtouch.vo.MindTouchPage;
import com.globalsight.connector.mindtouch.vo.MindTouchPageInfo;
import com.globalsight.cxe.entity.mindtouch.MindTouchConnector;
import com.globalsight.cxe.entity.mindtouch.MindTouchConnectorTargetServer;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.util.comparator.GlobalSightLocaleComparator;
import com.globalsight.ling.common.URLEncoder;
import com.globalsight.util.AmbFileStoragePathUtils;
import com.globalsight.util.FileUtil;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.SortUtil;
import com.globalsight.util.StringUtil;
import com.globalsight.util.edit.EditUtil;

public class MindTouchHelper
{
    static private final Logger logger = Logger
            .getLogger(MindTouchHelper.class);

    private MindTouchConnector mtc = null;
    
    private HashMap<String, MindTouchConnectorTargetServer> targetServersMap = null;

    private CloseableHttpClient httpClient = null;

    public MindTouchHelper(MindTouchConnector mtc)
    {
        this.mtc = mtc;

        setTargetServersMap();
    }

    /**
     * Test if it can connect to MindTouch server successfully.
     * 
     * @return error message if failed; return null if successfully.
     */
    public String doTest()
    {
        CloseableHttpClient httpClient = getHttpClient();
        HttpResponse httpResponse = null;
        try
        {
            String url = mtc.getUrl() + "/@api/deki/pages/home/info";
            HttpGet httpget = getHttpGet(url);

            httpResponse = httpClient.execute(httpget);

            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode == 200)
            {
                return null;
            }
            else
            {
                return httpResponse.getStatusLine().toString();
            }
        }
        catch (Exception e)
        {
            logger.warn("Fail to test MindTouch connector: " + e.getMessage());
            return "Failed to connect to MindTouch server";
        }
        finally
        {
            consumeQuietly(httpResponse);
        }
    }

    public void deletePage(long pageId) throws Exception
    {
        CloseableHttpClient httpClient = getHttpClient();
        HttpResponse httpResponse = null;
        try
        {
            String url = mtc.getUrl() + "/@api/deki/pages/" + pageId;
            HttpDelete httpDelete = getHttpDelete(url);

            httpResponse = httpClient.execute(httpDelete);

            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode != 200)
            {
                logger.error("Fail to delete page: " + pageId + ", returning info is: "
                        + EntityUtils.toString(httpResponse.getEntity()));
            }
        }
        catch (Exception e)
        {
            logger.error("Fail to delete page: " + pageId, e);
        }
        finally
        {
            consumeQuietly(httpResponse);
        }
    }

    /**
     * Get the tree in XML format for specified pageId. For root page, the
     * parameter can be "home".
     */
    public String getTreeXml(String pageId)
    {
        CloseableHttpClient httpClient = getHttpClient();
        HttpResponse httpResponse = null;
        try
        {
            String url = mtc.getUrl() + "/@api/deki/pages/" + pageId + "/tree";
            HttpGet httpget = getHttpGet(url);

            httpResponse = httpClient.execute(httpget);

            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode == 200)
            {
                return EntityUtils.toString(httpResponse.getEntity());
            }
            else
            {
                logger.warn("Fail to get sitemap tree: "
                        + httpResponse.getStatusLine().toString());
                return null;
            }
        }
        catch (Exception e)
        {
            logger.error("Fail to get sitemap tree: " + e.getMessage());
            return null;
        }
        finally
        {
            consumeQuietly(httpResponse);
        }
    }

    /**
     * Parse tree xml from "getTreeXml(pageId)" method to form a tree.
     * 
     * @param treeXml
     *            -- the tree information in XML format.
     * @return MindTouchPage
     */
    @SuppressWarnings("rawtypes")
    public MindTouchPage parseTreeXml(String treeXml)
            throws Exception
    {
        MindTouchPage rootMtp = null;
        Document doc = getDocument(treeXml);

        String id = null;
        String href = null;
        List pageNodes = doc.selectNodes("//page");
        List<MindTouchPage> allPages = new ArrayList<MindTouchPage>();
        Iterator it = pageNodes.iterator();
        while (it.hasNext())
        {
            MindTouchPage mtp = new MindTouchPage();
            Element pageNode = (Element) it.next();
            // page id
            id = pageNode.attributeValue("id");
            mtp.setId(Long.parseLong(id));
            // href
            href = pageNode.attributeValue("href");
            mtp.setHref(href);
            // parent page id
            String parentName = null;
            if (pageNode.getParent() != null)
            {
                parentName = pageNode.getParent().getName();
            }
            if ("subpages".equals(parentName))
            {
                String parentId = pageNode.getParent().getParent()
                        .attributeValue("id");
                mtp.setParentId(Long.parseLong(parentId));
            }
            else if ("pages".equals(parentName))
            {
                rootMtp = mtp;
            }

            Iterator subNodeIt = pageNode.nodeIterator();
            while (subNodeIt.hasNext())
            {
                Element node = (Element) subNodeIt.next();
                String name = node.getName();
                String text = node.getText();
                if ("uri.ui".equals(name))
                {
                    mtp.setUriUi(text);
                }
                else if ("title".equals(name))
                {
                	// title cannot have "<" and ">"
                	text = text.replace("<", "&lt;").replace(">", "&gt;");
                	// as json does not allow "\" and "/", remove them for displaying.
                	text = text.replace("\\", "").replace("/", "");
                	text = text.replace("%22", "\"");
                	text = text.replace("%3F", "?");
                	text = text.replace("%23", "#");
                	text = text.replace("%3D", "=");
                	text = text.replace("%26", "&");
                	text = text.replace("%25", "%");
                    mtp.setTitle(text);
                }
                else if ("path".equals(name))
                {
                    mtp.setPath(text);
                }
                else if ("date.created".equals(name))
                {
                    mtp.setDateCreated(text);
                }
            }
            allPages.add(mtp);
        }

        HashMap<Long, MindTouchPage> map = new HashMap<Long, MindTouchPage>();
        for (MindTouchPage mtp : allPages)
        {
            map.put(mtp.getId(), mtp);
        }

        for (MindTouchPage mtp : allPages)
        {
            long parentId = mtp.getParentId();
            MindTouchPage parent = map.get(parentId);
            if (parent != null)
            {
                parent.addSubPage(mtp);
            }
        }

        return rootMtp;
    }

    /**
     * Get page contents with "contents" API.
     * 
     * @param pageId
     */
    public String getPageContents(String pageId)
    {
        CloseableHttpClient httpClient = getHttpClient();
        HttpResponse httpResponse = null;
        try
        {
            String url = mtc.getUrl() + "/@api/deki/pages/" + pageId + "/contents?mode=edit";
            HttpGet httpget = getHttpGet(url);

            httpResponse = httpClient.execute(httpget);

            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode == 200)
            {
                return EntityUtils.toString(httpResponse.getEntity());
            }
            else
            {
                logger.warn("Fail to get page content for pageId " + pageId
                        + " : " + httpResponse.getStatusLine().toString());
            }
        }
        catch (Exception e)
        {
            logger.error("Fail to get page content for pageId: " + pageId, e);
        }
        finally
        {
            consumeQuietly(httpResponse);
        }

        return null;
    }

    /**
     * Get page tags with "tags" API.
     * 
     * @param pageId
     * @return String
     */
    public String getPageTags(long pageId)
    {
        CloseableHttpClient httpClient = getHttpClient();
        HttpResponse httpResponse = null;
        try
        {
            String url = mtc.getUrl() + "/@api/deki/pages/" + pageId + "/tags";
            HttpGet httpget = getHttpGet(url);

            httpResponse = httpClient.execute(httpget);

            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode == 200)
            {
                return EntityUtils.toString(httpResponse.getEntity());
            }
            else
            {
                logger.warn("Fail to get page tags for pageId " + pageId
                        + " : " + httpResponse.getStatusLine().toString());
            }
        }
        catch (Exception e)
        {
            logger.error("Fail to get page tags for pageId: " + pageId, e);
        }
        finally
        {
            consumeQuietly(httpResponse);
        }

        return null;
    }

    /**
     * Get page properties with "properties" API.
     * 
     * @param pageId
     * @return String
     */
    public String getPageProperties(long pageId)
    {
        CloseableHttpClient httpClient = getHttpClient();
        HttpResponse httpResponse = null;
        try
        {
            String url = mtc.getUrl() + "/@api/deki/pages/" + pageId + "/properties";
            HttpGet httpget = getHttpGet(url);

            httpResponse = httpClient.execute(httpget);

            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode == 200)
            {
                return EntityUtils.toString(httpResponse.getEntity());
            }
            else
            {
                logger.warn("Fail to get page properties for pageId " + pageId
                        + " : " + httpResponse.getStatusLine().toString());
            }
        }
        catch (Exception e)
        {
            logger.error("Fail to get page properties for pageId: " + pageId, e);
        }
        finally
        {
            consumeQuietly(httpResponse);
        }

        return null;
    }
    
    public String getPageProperties(String url, String pagePath)
    {
        CloseableHttpClient httpClient = getHttpClient();
        HttpResponse httpResponse = null;
        try
        {
            url += "/@api/deki/pages/=" + pagePath + "/properties";
            HttpGet httpget = getHttpGet(url);

            httpResponse = httpClient.execute(httpget);

            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode == 200)
            {
                return EntityUtils.toString(httpResponse.getEntity());
            }
            else
            {
                logger.warn("Fail to get page properties for pagePath " + pagePath
                        + " : " + httpResponse.getStatusLine().toString());
            }
        }
        catch (Exception e)
        {
            logger.error("Fail to get page properties for pagePath: " + pagePath, e);
        }
        finally
        {
            consumeQuietly(httpResponse);
        }

        return null;
    }
    
    public String getPageFiles(String pageId)
    {
        CloseableHttpClient httpClient = getHttpClient();
        String url = mtc.getUrl() + "/@api/deki/pages/" + pageId + "/files";

        int count = 0;
        String pageFilesXml = null;
        while (pageFilesXml == null && count < 3)
        {
            count++;
            if (count > 1)
            {
                logger.info("Retry to getPageFiles for url: " + url);
            }

            HttpResponse httpResponse = null;
            try
            {
                HttpGet httpget = getHttpGet(url);
                httpResponse = httpClient.execute(httpget);
                int statusCode = httpResponse.getStatusLine().getStatusCode();
                if (statusCode == 200)
                {
                    pageFilesXml = EntityUtils.toString(httpResponse.getEntity());
                }
                else
                {
                    logger.warn("Fail to get page files for pageId " + pageId
                            + " : " + httpResponse.getStatusLine().toString());
                }
            }
            catch (Exception e)
            {
                logger.error("Fail to get page files for pageId: " + pageId, e);
            }
            finally
            {
                consumeQuietly(httpResponse);
            }
        }

        return pageFilesXml;
    }
    
    public String getPageFile(String url)
    {
        logger.info("getPageFile url: " + url);
    	CloseableHttpClient httpClient = getHttpClient();
    	HttpResponse httpResponse = null;
        try
        {
            HttpGet httpget = getHttpGet(url);
            httpResponse = httpClient.execute(httpget);

            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode == 200)
            {
            	String fileName = url.substring(url.lastIndexOf("/") + 1);
            	File docFolder = AmbFileStoragePathUtils.getCxeDocDir(mtc.getCompanyId());
                String filePath = docFolder + File.separator + "MindTouchConnectorFiles" + File.separator + fileName;
            	File storeFile = new File(filePath);
            	storeFile.getParentFile().mkdirs();
            	if(!storeFile.exists())
            	{
            		storeFile.createNewFile();
            	} 
            	FileOutputStream output = new FileOutputStream(storeFile);
                InputStream instream = httpResponse.getEntity().getContent();
                byte b[] = new byte[1024];
                int j = 0;
                while((j = instream.read(b))!=-1)
                {
                	output.write(b,0,j);
                }
                output.flush();
                output.close();
                instream.close();
                
                return filePath;
            }
            else
            {
                logger.warn("Fail to get page file for url " + url
                        + " : " + httpResponse.getStatusLine().toString());
            }
        }
        catch (Exception e)
        {
            logger.error("Fail to get page file for url: " + url, e);
        }
        finally
        {
            consumeQuietly(httpResponse);
        }

        return null;
    }

    @SuppressWarnings("rawtypes")
	public String handleFiles(String pageId, String content,
			String targetLocale, String sourceLocale, MindTouchPageInfo pageInfo)
			throws DocumentException
    {
    	String filesXml = getPageFiles(pageId);
        if (StringUtil.isEmpty(filesXml))
        {
            return content;
        }

        HashMap<String, String> fileMap = new HashMap<String, String>();
    	Document doc = getDocument(filesXml);
    	List propertyNodes = doc.selectNodes("//contents ");
    	Iterator it = propertyNodes.iterator();
    	String sourceFileUrl = null;
    	while(it.hasNext())
    	{
    		Element propertyNode = (Element) it.next();
    		sourceFileUrl = propertyNode.attributeValue("href");
    		String filePath = getPageFile(sourceFileUrl);
    		if (filePath != null)
    		{
                fileMap.put(sourceFileUrl, filePath);
    		}
    	}

    	if(fileMap.size() > 0)
    	{
    		for(String tempSourceFileUrl: fileMap.keySet())
    		{
				String fileXml = putPageFile(fileMap.get(tempSourceFileUrl),
						targetLocale, sourceLocale, pageInfo);
				if (StringUtil.isNotEmpty(fileXml))
				{
	    			doc = getDocument(fileXml);
	    			propertyNodes = doc.selectNodes("//contents ");
	    			it = propertyNodes.iterator();
	    			while(it.hasNext())
	    			{
	    				Element propertyNode = (Element) it.next();
	    				String targetFileUrl = propertyNode.attributeValue("href");
	    				fileMap.put(tempSourceFileUrl, targetFileUrl);
	    			}
				}
    		}

    		for(String tempSourceFileUrl: fileMap.keySet())
    		{
				content = StringUtil.replace(content, tempSourceFileUrl,
						fileMap.get(tempSourceFileUrl));
    		}
    	}

        return content;
    }

    /**
     * Get page info with "info" API.
     * 
     * @param pageId
     * @return String
     */
    public String getPageInfo(String url, long pageId)
    {
        url += "/@api/deki/pages/" + pageId + "/info";
        return getPageInfo2(url);
    }

    /**
     * Get page info with "info" API.
     * 
     * @param path
     * @return String
     */
    public String getPageInfo(String url, String path)
    {
        url += "/@api/deki/pages/=" + path + "/info";
        return getPageInfo2(url);
    }

    private String getPageInfo2(String url)
    {
        CloseableHttpClient httpClient = getHttpClient();
        HttpResponse httpResponse = null;
        try
        {
            HttpGet httpget = getHttpGet(url);
            httpResponse = httpClient.execute(httpget);
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode == 200)
            {
                return EntityUtils.toString(httpResponse.getEntity());
            }
        }
        catch (Exception e)
        {
            logger.warn("Fail to get page info for page: " + url, e);
        }
        finally
        {
            consumeQuietly(httpResponse);
        }

        return null;
    }

    /**
     * Parse page info xml from "getPageInfo()" method.
     * 
     * @param pageInfoXml
     * @return MindTouchPage
     * @throws DocumentException
     */
    @SuppressWarnings("rawtypes")
    public MindTouchPage parsePageInfoXml(String pageInfoXml)
            throws DocumentException
    {
        MindTouchPage mtp = new MindTouchPage();
        Document doc = getDocument(pageInfoXml);

        String id = null;
        String href = null;
        List pageNodes = doc.selectNodes("//page");
        if (pageNodes != null && pageNodes.size() > 0)
        {
            Element pageNode = (Element) pageNodes.get(0);
            // page id
            id = pageNode.attributeValue("id");
            mtp.setId(Long.parseLong(id));
            // href
            href = pageNode.attributeValue("href");
            mtp.setHref(href);

            String name = null;
            String text = null;
            Iterator subNodeIt = pageNode.nodeIterator();
            while (subNodeIt.hasNext())
            {
                Element node = (Element) subNodeIt.next();
                name = node.getName();
                text = node.getText();
                if ("uri.ui".equals(name))
                {
                    mtp.setUriUi(text);
                }
                else if ("title".equals(name))
                {
                    mtp.setTitle(text);
                }
                else if ("path".equals(name))
                {
                    mtp.setPath(text);
                }
                else if ("date.created".equals(name))
                {
                    mtp.setDateCreated(text);
                }
            }
        }

        return mtp;
    }

    /**
     * Send the translated contents back to MindTouch server via pages "post"
     * API. If the path specified page has already exists, it will be
     * updated;Otherwise, create a new page.
     * 
     * @param contentsTrgFile
     * @param pageInfo
     * @param targetLocale
     * @throws Exception
     */
	public void postPageContents(File contentsTrgFile,
			MindTouchPageInfo pageInfo, String sourceLocale, String targetLocale)
			throws Exception
    {
    	if(!isTargetServerExist(targetLocale) && !mtc.getIsPostToSourceServer())
    	{
    		return;
    	}
    	
        CloseableHttpClient httpClient = getHttpClient();
        String path = null;
        try
        {
            // to be safe, it must use "text/plain" content type instead of
            // "text/xml" or "application/xml".
            String content = FileUtil.readFile(contentsTrgFile, "UTF-8");
            content = StringUtil.replace(content, "&nbsp;", "&#160;");
            String title = getTitleFromTranslatedContentXml(content);
            content = fixTitleValueInContentXml(content);
            // Only when target server exists, do this...
            if (isTargetServerExist(targetLocale))
            {
				content = handleFiles(pageInfo.getPageId(), content,
						targetLocale, sourceLocale, pageInfo);
            }

            int times = 0;
            while (times < 2)
            {
        		times++;
        		HttpResponse response = null;
        		try
        		{
                    String tmpContent = content;
                    tmpContent = EditUtil.decodeXmlEntities(tmpContent);
                    // empty body
                    if (tmpContent.indexOf("<body/>") > -1)
                    {
                        tmpContent = "";
                    }
                    // normal case
                    else
                    {
                        tmpContent = tmpContent.substring(tmpContent.indexOf("<body>") + 6);
                        tmpContent = tmpContent.substring(0, tmpContent.indexOf("</body>"));
                    }
                    StringEntity reqEntity = new StringEntity(tmpContent, "UTF-8");
                    reqEntity.setContentType("text/plain; charset=UTF-8");

                    path = getNewPath(pageInfo, sourceLocale, targetLocale);
                    String strUrl = getPutServerUrl(targetLocale) + "/@api/deki/pages/=" + path
                            + "/contents?edittime=now&abort=never";
                    if (title != null)
                    {
                        strUrl += "&title=" + title;
                    }
                    URL url = new URL(strUrl);
                    URI uri = new URI(url.getProtocol(), url.getHost(), url.getPath(),
                            url.getQuery(), null);
                    HttpPost httppost = getHttpPost(uri, targetLocale);
                    httppost.setEntity(reqEntity);
                    response = httpClient.execute(httppost);

                    String entityContent = null;
                    if (response.getEntity() != null)
                    {
                        entityContent = EntityUtils.toString(response.getEntity());
                    }
                    if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
                    {
                        break;
                    }
                    else
                    {
                        String msg = "";
                        if (times == 1)
                        {
                            msg = "First try ";
                        }
                        else
                        {
                            msg = "Second try ";
                        }
                        msg += "fails to post contents back to MindTouch server for page '" + path
                                + "' : " + entityContent;
                        logger.error(msg);
                    }
        		}
        		catch (Exception e)
        		{
                    logger.error("Fail to post contents back to MindTouch server for " + times
                            + "times for page '" + path + "'.", e);
        		}
        		finally
        		{
        		    consumeQuietly(response);
        		}
            }
        }
        catch (Exception e)
        {
            logger.error("Fail to post contents back to MindTouch server for page '" + path + "'.",
                    e);
        }
    }

    /**
     * Put translated tags to MindTouch server page.
     * 
     * If tags are the same, MindTouch server will ignore them, tag IDs keep
     * unchanged. Otherwise, MindTouch will delete old ones and add new to
     * create new tags(with new tag IDs).
     * 
     * @param tagsTrgFile
     * @param pageInfo
     * @param targetLocale
     */
    public void putPageTags(File tagsTrgFile, MindTouchPageInfo pageInfo,
            String sourceLocale, String targetLocale)
    {
    	if(!isTargetServerExist(targetLocale) && !mtc.getIsPostToSourceServer())
    	{
    		return;
    	}
    	
        CloseableHttpClient httpClient = getHttpClient();
        HttpResponse response = null;
        String path = null;
        String url = null;
        try
        {
            url = getPutServerUrl(targetLocale);
        	path = getNewPath(pageInfo, sourceLocale, targetLocale);
			// To add tags to page, the page must exist. Should ensure the page
			// has been created before this. The loop waiting should not happen
			// actually.
            int count = 0;
            while (count < 5 && getPageInfo(url, path) == null)
            {
                count++;
                Thread.sleep(1000);
            }

            url += "/@api/deki/pages/=" + path + "/tags";
            HttpPut httpput = getHttpPut(url, targetLocale);

            String content = FileUtil.readFile(tagsTrgFile, "UTF-8");
            content = getTagTitlesXml(content);
            StringEntity reqEntity = new StringEntity(content, "UTF-8");
            reqEntity.setContentType("application/xml; charset=UTF-8");
            httpput.setEntity(reqEntity);

            response = httpClient.execute(httpput);

            String entityContent = null;
            if (response.getEntity() != null) {
                entityContent = EntityUtils.toString(response.getEntity());
            }
            if (HttpStatus.SC_OK != response.getStatusLine().getStatusCode())
            {
				logger.error("Fail to put tags back to MindTouch server for page '"
						+ path + "' : " + entityContent);
            }
        }
        catch (Exception e)
        {
            logger.error(
                    "Fail to put tags back to MindTouch server for page '"
                            + path + "'.", e);
        }
        finally
        {
            consumeQuietly(response);
        }
    }

	public String putPageFile(String filePath, String targetLocale,
			String sourceLocale, MindTouchPageInfo pageInfo)
    {
		if (!isTargetServerExist(targetLocale)
				&& !mtc.getIsPostToSourceServer())
    	{
    		return null;
    	}

    	CloseableHttpClient httpClient = getHttpClient();
        String entityContent = null;
        int count = 0;
        String path = null;
        File picFile = null;
        while (entityContent == null && count < 3)
        {
            count++;
            HttpResponse response = null;
            try
            {
                String fileName = filePath.substring(filePath.lastIndexOf(File.separator) + 1);
                String tempFileName = URLEncoder.encode(fileName);
                tempFileName = URLEncoder.encode(tempFileName);
                path = getNewPath(pageInfo, sourceLocale, targetLocale);
                String url = getPutServerUrl(targetLocale) + "/@api/deki/pages/="
                        + path + "/files/=" + tempFileName;
                if (count > 1)
                {
                    logger.info("Retry to putPageFile for url: " + url);
                }
                HttpPut httpput = getHttpPut(url, targetLocale);
                picFile = new File(filePath);
                FileEntity reqEntity = new FileEntity(picFile);
                httpput.setEntity(reqEntity);
                
                response = httpClient.execute(httpput);

                if (response.getEntity() != null) {
                    entityContent = EntityUtils.toString(response.getEntity());
                }
                if (HttpStatus.SC_OK != response.getStatusLine().getStatusCode())
                {
                    logger.error("Fail to put file back to MindTouch server for file '"
                            + filePath + "' : " + entityContent);
                }
            }
            catch (Exception e)
            {
                logger.error(
                        "Fail to put file back to MindTouch server for file '"
                                + filePath + "'.", e);
            }
            finally
            {
                consumeQuietly(response);
            }
        }
        if (picFile != null && picFile.exists())
        {
            picFile.delete();
        }

        return entityContent;
    }
    
    public static byte[] File2byte(String filePath)  
    {  
        byte[] buffer = null;  
        try  
        {  
            File file = new File(filePath);  
            FileInputStream fis = new FileInputStream(file);  
            ByteArrayOutputStream bos = new ByteArrayOutputStream();  
            byte[] b = new byte[1024];  
            int n;  
            while ((n = fis.read(b)) != -1)  
            {  
                bos.write(b, 0, n);  
            }  
            fis.close();  
            bos.close();  
            buffer = bos.toByteArray();  
        }  
        catch (FileNotFoundException e)  
        {  
            e.printStackTrace();  
        }  
        catch (IOException e)  
        {  
            e.printStackTrace();  
        }  
        return buffer;  
    } 
    
    /**
     * Put translated properties to MindTouch server page.
     * 
     * @param tagsTrgFile
     * @param pageInfo
     * @param targetLocale
     */
	public void putPageProperties(File propertiesTrgFile,
			MindTouchPageInfo pageInfo, String sourceLocale, String targetLocale)
    {
    	if(!isTargetServerExist(targetLocale) && !mtc.getIsPostToSourceServer())
    	{
    		return;
    	}
    	
        CloseableHttpClient httpClient = getHttpClient();
        HttpResponse response = null;
        String path = null;
        String url = null;
        try
        {
            url = getPutServerUrl(targetLocale);
            path = getNewPath(pageInfo, sourceLocale, targetLocale);
			// To add properties to page, the page must exist. Should ensure the
			// page has been created before this. The loop waiting should not
			// happen actually.
            int count = 0;
            while (count < 5 && getPageInfo(url, path) == null)
            {
                count++;
                Thread.sleep(1000);
            }

            // Use Etag from target server if exists.
            HashMap<String, String> etagMap =
            		getePropertiesEtagMap(getPageProperties(url, path));
            if (etagMap.size() == 0)
            {
				etagMap = getePropertiesEtagMap(
						getPageProperties(mtc.getUrl(), path));
            }

            url += "/@api/deki/pages/=" + path + "/properties";
            HttpPut httpput = getHttpPut(url, targetLocale);

            String content = FileUtil.readFile(propertiesTrgFile, "UTF-8");
            content = getPropertiesContentsXml(content, etagMap);
            StringEntity reqEntity = new StringEntity(content, "UTF-8");
            reqEntity.setContentType("application/xml; charset=UTF-8");
            httpput.setEntity(reqEntity);

            response = httpClient.execute(httpput);

            String entityContent = null;
            if (response.getEntity() != null)
            {
                entityContent = EntityUtils.toString(response.getEntity());
            }
            if (HttpStatus.SC_OK != response.getStatusLine().getStatusCode())
            {
				logger.error("Fail to put properties back to MindTouch server for page '"
						+ path + "' : " + entityContent);
            }
        }
        catch (Exception e)
        {
            logger.error(
                    "Fail to put properties back to MindTouch server for page '" + path + "'.", e);
        }
        finally
        {
            consumeQuietly(response);
        }
    }

    public boolean isTargetServerExist(String targetLocale)
    {
		if(targetServersMap.get(targetLocale) != null)
		{
			return true;
		}

    	return false;
    }

    private String getPutServerUrl(String targetLocale)
    {
		if(targetServersMap.get(targetLocale) != null)
		{
			return targetServersMap.get(targetLocale).getUrl();
		}
		else if (mtc.getIsPostToSourceServer())
    	{
    		return mtc.getUrl();
    	}

    	return null;
    }

    private String getPutServerUsername(String targetLocale)
    {
		if(targetServersMap.get(targetLocale) != null)
		{
			return targetServersMap.get(targetLocale).getUsername();
		}
		else if(mtc.getIsPostToSourceServer())
    	{
    		return mtc.getUsername();
    	}

    	return null;
    }

    private String getPutServerPassword(String targetLocale)
    {
		if(targetServersMap.get(targetLocale) != null)
		{
			return targetServersMap.get(targetLocale).getPassword();
		}
		else if (mtc.getIsPostToSourceServer())
    	{
    		return mtc.getPassword();
    	}

    	return null;
    }

    private void setTargetServersMap()
    {
    	HashMap<String, MindTouchConnectorTargetServer> tempMap = 
    			new HashMap<String, MindTouchConnectorTargetServer>();
    	List<MindTouchConnectorTargetServer> targetServers = 
				MindTouchManager.getAllTargetServers(mtc.getId());
		for(MindTouchConnectorTargetServer ts: targetServers)
		{
			tempMap.put(ts.getTargetLocale(), ts);
		}
		
		targetServersMap = tempMap;
    }

    /**
     * If the original page path is like "en-us/Developer_Resources", the target
     * path should be like "zh-cn/Developer_Resources"; If the original page
     * path has no source locale information, it will add target locale as
     * suffix.
     * 
     * @param pageInfo
     * @param sourceLocale -- sample "en_US"
     * @param targetLocale -- sample "zh_CN"
     * @return String
     * 
     * @throws UnsupportedEncodingException 
     */
    private String getNewPath(MindTouchPageInfo pageInfo, String sourceLocale,
            String targetLocale) throws UnsupportedEncodingException
    {
        String path = pageInfo.getPath();
        path = java.net.URLDecoder.decode(path, "UTF-8");
        // If no target server and post to source server, need re-organize path
    	if (!isTargetServerExist(targetLocale) && mtc.getIsPostToSourceServer())
    	{
        	sourceLocale = sourceLocale.replace("_", "-").toLowerCase();
            String sourceLang = null;
            if (sourceLocale.indexOf("-") > 0)
            {
            	sourceLang = sourceLocale.substring(0, sourceLocale.indexOf("-"));
            }
            targetLocale = targetLocale.replace("_", "-").toLowerCase();

            // this must be root page
            if (StringUtil.isEmpty(path))
            {
                path = pageInfo.getTitle() + "(" + targetLocale + ")";
            }
            // any non-root pages
            else
            {
                path = path.replace("\\", "/");// to be safe
                int index = path.indexOf(sourceLocale);
                if (index > -1)
                {
                    String part1 = path.substring(0, index);
                    String part2 = path.substring(index + sourceLocale.length());
                    path = part1 + targetLocale + part2;
                }
                else
                {
                	path = getNewPathByLangOnly(path, sourceLang, targetLocale);
                }
            }
    	}

        path = URLEncoder.encode(path);
        path = URLEncoder.encode(path);
        return path;
    }

	private String getNewPathByLangOnly(String sourcePath, String sourceLang,
			String targetLocale)
    {
		if (sourceLang == null)
			return sourcePath + "(" + targetLocale + ")";

    	boolean isSrcFound = false;
    	StringBuilder newPath = new StringBuilder();
    	String[] paths = sourcePath.split("/");
    	String path = null;
		for (int i = 0; i < paths.length; i++)
    	{
    		path = paths[i];
    		if (path.startsWith(sourceLang + "-") && !isSrcFound)
    		{
    			isSrcFound = true;
    			newPath.append("/").append(targetLocale);
    		}
    		// replace first section in path into target locale, the first section must be like "xx-xx" style.
    		else if (i == 0 && path.split("-").length == 2 && path.indexOf("-") > 0)
    		{
    			isSrcFound = true;
    			newPath.append("/").append(targetLocale);
    		}
    		else
    		{
    			newPath.append("/").append(path);
    		}
    	}

    	if (!isSrcFound)
    	{
    		newPath.append("(").append(targetLocale).append(")");
    	}

    	String result = newPath.toString();
    	if (result.startsWith("/"))
    	{
    		result = result.substring(1);
    	}

    	return result;
    }

    /**
     * Return an XML like
     * "<tags><tag value=\"title1\"/><tag value=\"title2\"/></tags>".
     * 
     * @param tagsXml
     * @return String
     * @throws DocumentException
     */
    @SuppressWarnings("rawtypes")
    private String getTagTitlesXml(String tagsXml) throws DocumentException
    {
        StringBuffer titles = new StringBuffer();
        titles.append("<tags>");

        Document doc = getDocument(tagsXml);
        List titleNodes = doc.selectNodes("//title");
        Iterator it = titleNodes.iterator();
        String title = null;
        while (it.hasNext())
        {
            Element titleNode = (Element) it.next();
            title = titleNode.getTextTrim();
            if (title != null && title.length() > 0)
            {
                title = StringUtil.replace(title, "&", "&amp;");
                title = StringUtil.replace(title, "<", "&lt;");
                title = StringUtil.replace(title, ">", "&gt;");
                titles.append("<tag value=\"").append(title).append("\"/>");
            }
        }
        titles.append("</tags>");

        return titles.toString();
    }
    
    /**
     * Return an XML like
     * "<properties><property name="name1"><contents type="text/plain">yes</contents></property><property name="name2"/></properties>"
     * 
     * @param propertiesXml
     * @return String
     * @throws DocumentException
     */
    @SuppressWarnings("rawtypes")
	private String getPropertiesContentsXml(String propertiesXml,
			HashMap<String, String> etagMap) throws DocumentException
    {
        StringBuffer titles = new StringBuffer();
        titles.append("<properties>");

        Document doc = getDocument(propertiesXml);
        List propertyNodes = doc.selectNodes("//property");
        Iterator it = propertyNodes.iterator();
        String name = null;
        String content = null;
        while (it.hasNext())
        {
            Element propertyNode = (Element) it.next();
            name = propertyNode.attributeValue("name");
            Element contentNode = (Element) propertyNode.selectSingleNode("contents");
            content = contentNode.getTextTrim();
            if (content != null && content.length() > 0)
            {
                content = StringUtil.replace(content, "&", "&amp;");
                content = StringUtil.replace(content, "<", "&lt;");
                content = StringUtil.replace(content, ">", "&gt;");
                titles.append("<property name=\"").append(name).append("\" etag=\"").append(etagMap.get(name)).append("\"><contents type=\"text/plain; charset=UTF-8\">").append(content).append("</contents></property>");
            }
        }
        titles.append("</properties>");

        return titles.toString();
    }
    
    @SuppressWarnings("rawtypes")
	private HashMap<String, String> getePropertiesEtagMap(String propertiesXml)
    {
    	HashMap<String, String> etagMap = new HashMap<String, String>();
    	try
    	{
        	if (propertiesXml != null)
        	{
            	Document doc = getDocument(propertiesXml);
            	List propertyNodes = doc.selectNodes("//property");
            	Iterator it = propertyNodes.iterator();
            	String name = null;
                String etag = null;
            	while(it.hasNext())
            	{
            		Element propertyNode = (Element) it.next();
                    name = propertyNode.attributeValue("name");
                    etag =  propertyNode.attributeValue("etag");
                    etagMap.put(name, etag);
            	}
        	}
    	}
    	catch (DocumentException e)
    	{
    		logger.warn(e);
    	}
    	return etagMap;
    }

    /**
	 * As the "title" need to be translated, get the translated title from
	 * target file.
	 * 
	 * @param contentXml
	 * @return title
	 */
    private String getTitleFromTranslatedContentXml(String contentXml)
    {
    	try
    	{
    		contentXml = fixTitleValueInContentXml(contentXml);
    		int index = contentXml.indexOf("<body>");
    		if (index == -1)
    		{
    		    index = contentXml.indexOf("<body");
    		}
    		String content = contentXml.substring(0, index);
    		content = content.replace("&nbsp;", " ");
    		content += "</content>";
    		Element root = getDocument(content).getRootElement();
    		String title = root.attributeValue("title");
    		if (title.trim().length() > 0)
    		{
				// Encode the whole title is the right behavior, but as
				// MindTouch does not decode title, we have to only encode # = &
				// title = URLEncoder.encode(title);
    			title = title.replace("#", "%23");
    			title = title.replace("=", "%3D");
    			title = title.replace("&", "%26");
    			return new String(title.trim().getBytes("UTF-8"), "UTF-8");
    		}
    	}
    	catch (Exception e)
    	{
			logger.error("Fail to get title from translated contents xml: "
					+ contentXml, e);
    		return null;
    	}
    	return null;
    }

    /**
	 * If there are "<", ">" in value of "title" attribute, it will fail to
	 * create job because of bad XML, need fix them.
	 * 
	 * @param contentXml
	 * @return Fixed contentXml
	 */
    public static String fixTitleValueInContentXml(String contentXml)
    {
		StringBuilder xml = new StringBuilder();
    	try
    	{
    		int index = contentXml.indexOf(" title=");
    		String a = contentXml.substring(0, index + " title=\"".length());
    		xml.append(a);
    		String b = contentXml.substring(index + " title=\"".length());
    		index = b.indexOf("=");
    		if (index > -1)
    		{
    			a = b.substring(0, index);
    			b = b.substring(index);
    			index = a.lastIndexOf(" ");
    			String title = a.substring(0, index - 1);
				title = title.replace("\"", "&quot;").replace("<", "&lt;")
						.replace(">", "&gt;");
    			xml.append(title);
    			xml.append(a.substring(index - 1));
    			xml.append(b);
    		}
    		else
    		{
    			xml.append(b);
    		}

    		return new String(xml.toString().trim().getBytes("UTF-8"), "UTF-8");
    	}
    	catch (Exception e)
    	{
			logger.error("Fail to fix title in contents xml: " + contentXml, e);
			return contentXml;
    	}
    }

    /**
     * An object file content is like:
     * {"title":"Get Involved","PageId":"1845","MindTouchConnectorId":"7","path":"en-us/Developer_Resources/Community/Get_Involved"}
     * 
     * @return MindTouchPageInfo
     * 
     */
    public static MindTouchPageInfo parseObjFile(File objFile)
    {
        MindTouchPageInfo info = new MindTouchPageInfo();
        if (objFile.exists() && objFile.isFile())
        {
            try
            {
                String json = FileUtil.readFile(objFile, "UTF-8");
                JSONObject jsonObj = new JSONObject(json);

                String mindTouchConnectorId = String.valueOf(jsonObj.get("mindTouchConnectorId"));
                info.setMindTouchConnectorId(mindTouchConnectorId);

                String pageId = String.valueOf(jsonObj.get("pageId"));
                info.setPageId(pageId);

                String path = (String) jsonObj.get("path");
                info.setPath(path);

                String title = (String) jsonObj.get("title");
                info.setTitle(title);
            }
            catch (Exception e)
            {
                logger.warn(e.getMessage());
            }
        }

        return info;
    }

    public static Document getDocument(String xml) throws DocumentException
    {
        SAXReader reader = new SAXReader();
        return reader.read(new InputSource(new StringReader(xml)));
    }

    private HttpPost getHttpPost(URI uri, String targetLocale)
    {
        HttpPost httppost = new HttpPost(uri);
		httppost.setHeader(
				HttpHeaders.AUTHORIZATION,
				authorizationHeader(getPutServerUsername(targetLocale),
						getPutServerPassword(targetLocale)));
        return httppost;
    }

    private HttpGet getHttpGet(String url)
    {
        HttpGet httpget = new HttpGet(url);
        httpget.setHeader(HttpHeaders.AUTHORIZATION,
                authorizationHeader(mtc.getUsername(), mtc.getPassword()));
        return httpget;
    }

    private HttpPut getHttpPut(String url, String targetLocale)
    {
        HttpPut httpput = new HttpPut(url);
		httpput.setHeader(
				HttpHeaders.AUTHORIZATION,
				authorizationHeader(getPutServerUsername(targetLocale),
						getPutServerPassword(targetLocale)));
        return httpput;
    }

    private HttpDelete getHttpDelete(String url)
    {
        HttpDelete httpdelete = new HttpDelete(url);
        httpdelete.setHeader(HttpHeaders.AUTHORIZATION,
                authorizationHeader(mtc.getUsername(), mtc.getPassword()));
        return httpdelete;
    }

    CookieSpecProvider easySpecProvider = new CookieSpecProvider()
    {
        public CookieSpec create(HttpContext context)
        {
        	return new BrowserCompatSpec()
        	{
        		@Override
				public void validate(Cookie cookie, CookieOrigin origin)
						throws MalformedCookieException {
					// Oh, I am easy
				}
            };
        }
    };

	Registry<CookieSpecProvider> reg = RegistryBuilder
			.<CookieSpecProvider> create()
			.register(CookieSpecs.BEST_MATCH, new BestMatchSpecFactory())
			.register(CookieSpecs.BROWSER_COMPATIBILITY, new BrowserCompatSpecFactory())
			.register("mySpec", easySpecProvider)
			.build();

	RequestConfig requestConfig = RequestConfig.custom()
			.setCookieSpec("mySpec").setConnectTimeout(5000)
			.setSocketTimeout(20000).build();

	private CloseableHttpClient getHttpClient()
    {
		if (httpClient == null)
		{
			httpClient = HttpClients.custom().setDefaultCookieSpecRegistry(reg)
					.setDefaultRequestConfig(requestConfig).build();
		}

        return httpClient;
    }

	private void consumeQuietly(HttpResponse httpResponse)
	{
        if (httpResponse != null)
        {
            try
            {
                EntityUtils.consumeQuietly(httpResponse.getEntity());                
            }
            catch (Exception ignore)
            {
                
            }
        }
	}

	public void shutdownHttpClient()
    {
        if (httpClient == null)
            return;

        try
        {
			httpClient.close();
		}
        catch (IOException e)
        {
        	logger.error("Fail to close httpclient", e);
		}
    }

    private final String authorizationHeader(String username, String password)
    {
        String auth = username + ":" + password;
        byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(Charset
                .forName("US-ASCII")));
        String authHeader = "Basic " + new String(encodedAuth);

        return authHeader;
    }

    @SuppressWarnings({ "unchecked" })
	public static Vector<GlobalSightLocale> getAllTargetLocales()
    {
    	Vector<GlobalSightLocale> targetLocales = new Vector<GlobalSightLocale>();
    	try 
		{
			targetLocales = ServerProxy.getLocaleManager()
					.getAllTargetLocales();
			SortUtil.sort(targetLocales, new GlobalSightLocaleComparator(
					GlobalSightLocaleComparator.DISPLAYNAME, Locale.US));
		}
		catch (Exception e)
		{
			throw new EnvoyServletException(EnvoyServletException.EX_GENERAL, e);
		}

    	return targetLocales;
    }
}
