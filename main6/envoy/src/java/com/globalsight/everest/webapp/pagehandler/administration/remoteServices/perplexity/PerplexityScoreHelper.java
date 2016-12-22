/**
 * Copyright 2009 Welocalize, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 * 
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 */

package com.globalsight.everest.webapp.pagehandler.administration.remoteServices.perplexity;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import com.globalsight.everest.foundation.LocalePair;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.webapp.pagehandler.administration.remoteServices.perplexity.vo.PerplexityAccount;
import com.globalsight.everest.webapp.pagehandler.administration.remoteServices.perplexity.vo.PerplexityLanguageModulePair;
import com.globalsight.everest.webapp.pagehandler.administration.remoteServices.perplexity.vo.PerplexityScores;
import com.globalsight.persistence.hibernate.HibernateUtil;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * <code>PerplexityScoreHelper</code> can help us with connect perplexity.
 * <p>
 * For GBS-4495 perplexity score on MT.
 */
public class PerplexityScoreHelper
{
    public static final String URL_POST_TOKEN = "/api/v1/api-token-auth";
    public static final String URL_GET_ACCOUNTS = "/api/v1/accounts";
    public static final String URL_GET_LANG_MODULE_PAIRS = "/api/v1/lmpairs";
    public static final String URL_POST_SCORE = "/api/v1/score/";// the last "/"
                                                                 // is required

    public static final String KEY_USER_NAME = "username";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_ACCOUNT = "account";
    public static final String KEY_SOURCE_LANGUAGE = "source_language";
    public static final String KEY_TARGET_LANGUAGE = "target_language";
    public static final String KEY_LMPAIR = "lmpair";
    public static final String KEY_SOURCE_LIST = "source_list";
    public static final String KEY_TARGET_LIST = "target_list";

    private static final Logger logger = Logger.getLogger(PerplexityScoreHelper.class);

    private static final String AUTHORIZATION = "Authorization";

    private CloseableHttpClient httpClient = null;

    /**
     * Get access token for perplexity service.
     * 
     * @param host
     * @param username
     * @param password
     * 
     * @return String
     */
    public String getToken(String host, String username, String password)
    {
        CloseableHttpClient httpclient = getHttpClient();
        HttpResponse httpResponse = null;
        try
        {
            HttpPost httppost = new HttpPost(host + URL_POST_TOKEN);

            JSONObject json = new JSONObject();
            json.put(KEY_USER_NAME, username);
            json.put(KEY_PASSWORD, password);
            StringEntity reqEntity = new StringEntity(json.toString(), "UTF-8");
            reqEntity.setContentType("application/json; charset=UTF-8");
            httppost.setEntity(reqEntity);

            httpResponse = httpclient.execute(httppost);

            if (httpResponse.getStatusLine().getStatusCode() == 200)
            {
                String entityContent = EntityUtils.toString(httpResponse.getEntity());
                JSONObject re = JSONObject.fromObject(entityContent);
                if (re.containsKey("token"))
                {
                    return "Token " + (String) re.get("token");
                }
            }
            else
            {
                printHttpResponse("getToken", host + URL_POST_TOKEN, httpResponse);
            }
        }
        catch (Exception e)
        {
            logger.error(e);
        }
        finally
        {
            consumeQuietly(httpResponse);
        }

        return null;
    }

    /**
     * Get all accounts information.
     * 
     * @param accessToken
     * @param host
     * 
     * @return List<PerplexityAccount>
     */
    public List<PerplexityAccount> getAccounts(String accessToken, String host)
    {
        List<PerplexityAccount> result = new ArrayList<PerplexityAccount>();

        try
        {
            String accountsJson = getAccountsInJSON(accessToken, host);
            if (StringUtils.isNotEmpty(accountsJson))
            {
                JSONArray arr = JSONArray.fromObject(accountsJson);
                if (arr.size() > 0)
                {
                    for (Iterator<JSONObject> it = arr.iterator(); it.hasNext();)
                    {
                        JSONObject obj = it.next();
                        PerplexityAccount account = new PerplexityAccount();
                        account.setId(obj.getLong("id"));
                        account.setName(obj.getString("name"));
                        result.add(account);
                    }
                }
            }
        }
        catch (Exception e)
        {
            logger.warn(e);
        }

        return result;
    }

    /**
     * Get all accounts information in JSON string.
     * 
     * @param accessToken
     * @param host
     * 
     * @return JSON string.
     */
    public String getAccountsInJSON(String accessToken, String host)
    {
        CloseableHttpClient httpclient = getHttpClient();
        HttpResponse httpResponse = null;
        try
        {
            String url = host + URL_GET_ACCOUNTS;
            HttpGet httpGet = getHttpGet(url, accessToken);
            httpResponse = httpclient.execute(httpGet);

            if (httpResponse.getStatusLine().getStatusCode() == 200)
            {
                return EntityUtils.toString(httpResponse.getEntity());
            }
            else
            {
                printHttpResponse("getAccountsInJSON", url, httpResponse);
            }
        }
        catch (Exception e)
        {
            logger.error(e);
        }
        finally
        {
            consumeQuietly(httpResponse);
        }

        return null;
    }

    /**
     * Get specified account information.
     * 
     * @param accessToken
     * @param host
     * @param accountId
     * 
     * @return <PerplexityAccount> object.
     */
    public PerplexityAccount getAccount(String accessToken, String host, long accountId)
    {
        try
        {
            String accountJson = getAccountByIdInJSON(accessToken, host, accountId);
            if (StringUtils.isNotEmpty(accountJson))
            {
                JSONObject obj = JSONObject.fromObject(accountJson);
                PerplexityAccount account = new PerplexityAccount();
                account.setId(obj.getLong("id"));
                account.setName(obj.getString("name"));
                return account;
            }
        }
        catch (Exception e)
        {
            logger.warn(e);
        }

        return null;
    }

    /**
     * Get specified account information in JSON string.
     * 
     * @param accessToken
     * @param host
     * @param accountId
     * 
     * @return JSON string.
     */
    public String getAccountByIdInJSON(String accessToken, String host, long accountId)
    {
        CloseableHttpClient httpclient = getHttpClient();
        HttpResponse httpResponse = null;
        try
        {
            String url = host + URL_GET_ACCOUNTS + "/" + accountId;
            HttpGet httpGet = getHttpGet(url, accessToken);

            httpResponse = httpclient.execute(httpGet);

            if (httpResponse.getStatusLine().getStatusCode() == 200)
            {
                return EntityUtils.toString(httpResponse.getEntity());
            }
            else
            {
                printHttpResponse("getAccountByIdInJSON", url, httpResponse);
            }
        }
        catch (Exception e)
        {
            logger.error(e);
        }
        finally
        {
            consumeQuietly(httpResponse);
        }

        return null;
    }

    /**
     * Get language module pairs information.
     * 
     * @param accessToken
     * @param host
     * @param accountId
     *            -- Optional.
     * @param sourceLanguage
     *            -- Optional.
     * @param targetLanguage
     *            -- Optional.
     * 
     * @return List<PerplexityLanguageModulePair>
     */
    public List<PerplexityLanguageModulePair> getLanguageModulePairs(String accessToken,
            String host, long accountId, String sourceLanguage, String targetLanguage)
    {
        List<PerplexityLanguageModulePair> result = new ArrayList<PerplexityLanguageModulePair>();

        try
        {
            String lmpairsJson = getLanguageModulePairsInJSON(accessToken, host, accountId,
                    sourceLanguage, targetLanguage);

            if (StringUtils.isNotEmpty(lmpairsJson))
            {
                JSONArray arr = JSONArray.fromObject(lmpairsJson);
                if (arr.size() > 0)
                {
                    for (Iterator<JSONObject> it = arr.iterator(); it.hasNext();)
                    {
                        JSONObject obj = it.next();
                        PerplexityLanguageModulePair lmpair = new PerplexityLanguageModulePair();
                        lmpair.setId(obj.getLong("id"));
                        lmpair.setSourceLanguage(obj.getString("source_language"));
                        lmpair.setTargetLanguage(obj.getString("target_language"));
                        lmpair.setDomain(obj.getString("domain"));
                        lmpair.setAccountId(obj.getLong("account"));
                        result.add(lmpair);
                    }
                }
            }
        }
        catch (Exception e)
        {
            logger.warn(e);
        }

        return result;
    }

    /**
     * Get language module pairs information in JSON string.
     * 
     * @param accessToken
     * @param host
     * @param accountId
     *            -- Optional.
     * @param sourceLanguage
     *            -- Optional.
     * @param targetLanguage
     *            -- Optional.
     * 
     * @return JSON string
     */
    public String getLanguageModulePairsInJSON(String accessToken, String host, long accountId,
            String sourceLanguage, String targetLanguage)
    {
        CloseableHttpClient httpclient = getHttpClient();
        HttpResponse httpResponse = null;
        try
        {
            String url = getLanguageModulePairsUrl(host, accountId, sourceLanguage, targetLanguage);
            HttpGet httpGet = getHttpGet(url, accessToken);

            httpResponse = httpclient.execute(httpGet);

            if (httpResponse.getStatusLine().getStatusCode() == 200)
            {
                return EntityUtils.toString(httpResponse.getEntity());
            }
            else
            {
                printHttpResponse("getLanguageModulePairsInJSON", url, httpResponse);
            }
        }
        catch (Exception e)
        {
            logger.error(e);
        }
        finally
        {
            consumeQuietly(httpResponse);
        }

        return null;
    }

    /**
     * Get specified language module pair information.
     * 
     * @param accessToken
     * @param host
     * @param lmpairId
     * 
     * @return <PerplexityLanguageModulePair> object.
     */
    public PerplexityLanguageModulePair getLanguageModulePairById(String accessToken, String host,
            long lmpairId)
    {
        try
        {
            String lmpairJson = getLanguageModulePairByIdInJSON(accessToken, host, lmpairId);
            if (StringUtils.isNotEmpty(lmpairJson))
            {
                JSONObject obj = JSONObject.fromObject(lmpairJson);

                PerplexityLanguageModulePair lmpair = new PerplexityLanguageModulePair();
                lmpair.setId(obj.getLong("id"));
                lmpair.setSourceLanguage(obj.getString("source_language"));
                lmpair.setTargetLanguage(obj.getString("target_language"));
                lmpair.setDomain(obj.getString("domain"));
                lmpair.setAccountId(obj.getLong("account"));

                return lmpair;
            }
        }
        catch (Exception e)
        {
            logger.warn(e);
        }

        return null;
    }

    /**
     * Get specified language module pair information in JSON string.
     * 
     * @param accessToken
     * @param host
     * @param lmpairId
     * 
     * @return JSON string
     */
    public String getLanguageModulePairByIdInJSON(String accessToken, String host, long lmpairId)
    {
        CloseableHttpClient httpclient = getHttpClient();
        HttpResponse httpResponse = null;
        try
        {
            String url = host + URL_GET_LANG_MODULE_PAIRS + "/" + lmpairId;
            HttpGet httpGet = getHttpGet(url, accessToken);

            httpResponse = httpclient.execute(httpGet);

            if (httpResponse.getStatusLine().getStatusCode() == 200)
            {
                return EntityUtils.toString(httpResponse.getEntity());
            }
            else
            {
                printHttpResponse("getLanguageModulePairByIdInJSON", url, httpResponse);
            }
        }
        catch (Exception e)
        {
            logger.error(e);
        }
        finally
        {
            consumeQuietly(httpResponse);
        }

        return null;
    }

    public List<Long> getLanguageModulePairsByPerplexity(long psId, long lpId) throws Exception
    {
        List<Long> plmIds = new ArrayList<>();

        PerplexityService ps = HibernateUtil.get(PerplexityService.class, psId);
        if (ps == null)
            return plmIds;

        LocalePair lp = ServerProxy.getLocaleManager().getLocalePairById(lpId);

        String accessToken = getToken(ps.getUrl(), ps.getUserName(), ps.getPassword());
        if (accessToken == null)
            return plmIds;

        List<PerplexityAccount> accounts = getAccounts(accessToken, ps.getUrl());

        for (PerplexityAccount pa : accounts)
        {
            List<PerplexityLanguageModulePair> plms = getLanguageModulePairs(accessToken,
                    ps.getUrl(), pa.getId(), lp.getSource().toString().replace("_", "-"),
                    lp.getTarget().toString().replace("_", "-"));
            for (PerplexityLanguageModulePair plm : plms)
            {
                plmIds.add(plm.getId());
            }
        }

        return plmIds;
    }

    /**
     * Form an URL with optional query parameters.
     * 
     * @param host
     *            -- Required.
     * @param accountId
     *            -- Query parameter. Optional.
     * @param sourceLanguage
     *            -- Query parameter. Optional.
     * @param targetLanguage
     *            -- Query parameter. Optional.
     * 
     * @return An URL like:
     *         http://10.100.1.13/api/v1/lmpairs?account=1&source_language
     *         =en-US&target_language=zh-CN
     * 
     * @throws UnsupportedEncodingException
     * 
     */
    private String getLanguageModulePairsUrl(String host, long accountId, String sourceLanguage,
            String targetLanguage) throws UnsupportedEncodingException
    {
        StringBuffer url = new StringBuffer();
        url.append(host).append(URL_GET_LANG_MODULE_PAIRS);

        StringBuffer queryParams = new StringBuffer();
        if (accountId > 0)
        {
            queryParams.append(KEY_ACCOUNT).append("=").append(accountId);
        }
        if (StringUtils.isNotEmpty(sourceLanguage))
        {
            if (queryParams.length() > 0)
            {
                queryParams.append("&");
            }
            queryParams.append(KEY_SOURCE_LANGUAGE).append("=");
            queryParams.append(java.net.URLEncoder.encode(sourceLanguage, "UTF-8"));
        }
        if (StringUtils.isNotEmpty(targetLanguage))
        {
            if (queryParams.length() > 0)
            {
                queryParams.append("&");
            }
            queryParams.append(KEY_TARGET_LANGUAGE).append("=");
            queryParams.append(java.net.URLEncoder.encode(targetLanguage, "UTF-8"));
        }

        if (queryParams.length() > 0)
        {
            url.append("?").append(queryParams);
        }

        return url.toString();
    }

    /**
     * Get perplexity scores.
     * 
     * @param accessToken
     * @param host
     * @param lmpairId
     * @param sourceList
     *            -- source list
     * @param targetList
     *            -- target list
     * 
     * @return <PerplexityScores> object.
     */
    public PerplexityScores getPerplexityScore(String accessToken, String host, long lmpairId,
            List<String> sourceList, List<String> targetList)
    {
        try
        {
            String scoresJson = getPerplexityScoreInJSON(accessToken, host, lmpairId, sourceList,
                    targetList);
            if (StringUtils.isNotEmpty(scoresJson))
            {
                PerplexityScores scores = new PerplexityScores();

                JSONObject obj = JSONObject.fromObject(scoresJson);

                JSONArray sources = obj.getJSONArray("source");
                for (Iterator it1 = sources.iterator(); it1.hasNext();)
                {
                    Double srcScore = (Double) it1.next();
                    scores.addSource(srcScore);
                }

                JSONArray targets = obj.getJSONArray("target");
                for (Iterator it2 = targets.iterator(); it2.hasNext();)
                {
                    Double trgScore = (Double) it2.next();
                    scores.addTarget(trgScore);
                }

                return scores;
            }
        }
        catch (Exception e)
        {
            logger.warn(e);
        }

        return null;
    }

    /**
     * Get perplexity scores in JSON string.
     * 
     * @param accessToken
     * @param host
     * @param lmpairId
     * @param sourceList
     *            -- source list
     * @param targetList
     *            -- target list
     * 
     * @return JSON string.
     */
    public String getPerplexityScoreInJSON(String accessToken, String host, long lmpairId,
            List<String> sourceList, List<String> targetList)
    {
        CloseableHttpClient httpclient = getHttpClient();
        HttpResponse httpResponse = null;
        try
        {
            String url = host + URL_POST_SCORE;
            HttpPost httppost = getHttpPost(url, accessToken);

            JSONObject bodyInJson = turnToJson(lmpairId, sourceList, targetList);
            StringEntity reqEntity = new StringEntity(bodyInJson.toString(), "UTF-8");
            reqEntity.setContentType("application/json; charset=UTF-8");
            httppost.setEntity(reqEntity);

            httpResponse = httpclient.execute(httppost);

            if (httpResponse.getStatusLine().getStatusCode() == 200)
            {
                return EntityUtils.toString(httpResponse.getEntity());
            }
            else
            {
                printHttpResponse("getPerplexityScoreInJSON", url, httpResponse);
            }
        }
        catch (Exception e)
        {
            logger.error(e);
        }
        finally
        {
            consumeQuietly(httpResponse);
        }

        return null;
    }

    private JSONObject turnToJson(long lmpairId, List<String> sourceList, List<String> targetList)
    {
        JSONObject bodyData = new JSONObject();
        bodyData.put(KEY_LMPAIR, lmpairId);

        JSONArray sources = new JSONArray();
        for (String source : sourceList)
        {
            sources.add(source);
        }
        bodyData.put(KEY_SOURCE_LIST, sources);

        JSONArray targets = new JSONArray();
        for (String target : targetList)
        {
            targets.add(target);
        }
        bodyData.put(KEY_TARGET_LIST, targets);
        return bodyData;
    }

    private CloseableHttpClient getHttpClient()
    {
        if (httpClient == null)
        {
            httpClient = HttpClients.custom().build();
        }

        return httpClient;
    }

    private String printHttpResponse(String methodName, String url, HttpResponse httpResponse)
            throws ParseException, IOException
    {
        logger.warn("Method name is '" + methodName + "', url is '" + url + "'.");

        int statusCode = httpResponse.getStatusLine().getStatusCode();
        logger.warn("Status code: " + statusCode);

        String res = httpResponse.getStatusLine().toString();
        logger.warn("Status line: " + res);

        String entityContent = EntityUtils.toString(httpResponse.getEntity());
        logger.warn("entityContent: " + entityContent);

        return entityContent;
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

    private HttpPost getHttpPost(String url, String accessToken)
    {
        HttpPost httppost = new HttpPost(url);
        httppost.setHeader(AUTHORIZATION, accessToken);
        return httppost;
    }

    private HttpGet getHttpGet(String url, String accessToken)
    {
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader(AUTHORIZATION, accessToken);
        return httpGet;
    }

    public void shutdownHttpClient()
    {
        if (httpClient == null)
            return;

        try
        {
            httpClient.close();
            httpClient = null;
        }
        catch (IOException e)
        {
            logger.error("Perplexity scorer: fail to close HttpClient.", e);
        }
    }

    public static void main(String[] args) throws Exception
    {
        String host = "https://cscore.welocalize.com";
        String username = "globalsight";
        String password = "3LE$UxI506PxsvXU£oeG5e";

        PerplexityScoreHelper helper = new PerplexityScoreHelper();
        try
        {
            String accessToken = helper.getToken(host, username, password);
            System.out.println("accessToken: " + accessToken);

            String accountsJson = helper.getAccountsInJSON(accessToken, host);
            System.out.println("accounts json: " + accountsJson);
            List<PerplexityAccount> accounts = helper.getAccounts(accessToken, host);

            long accountId = 1L;
            String accountJson = helper.getAccountByIdInJSON(accessToken, host, accountId);
            System.out.println("account json: " + accountJson);
            PerplexityAccount account = helper.getAccount(accessToken, host, accountId);

            // optional query parameters
            String sourceLanguage = "en-US";
            String targetLanguage = "zh-CN";
            String lmpairsJson = helper.getLanguageModulePairsInJSON(accessToken, host, accountId,
                    sourceLanguage, targetLanguage);
            System.out.println("lmpairs json: " + lmpairsJson);
            List<PerplexityLanguageModulePair> lmpairs = helper.getLanguageModulePairs(accessToken,
                    host, accountId, sourceLanguage, targetLanguage);

            long lmpairId = 29L;
            String lmpairJson = helper.getLanguageModulePairByIdInJSON(accessToken, host, lmpairId);
            System.out.println("lmpair json: " + lmpairJson);
            PerplexityLanguageModulePair lmpair = helper.getLanguageModulePairById(accessToken,
                    host, lmpairId);

            // long lmpairId = 1L;
            List<String> sourceList = new ArrayList<String>();
            sourceList.add("This is a test");
            sourceList.add("This is another test");
            List<String> targetList = new ArrayList<String>();
            targetList.add("This is a test");
            targetList.add("This is another test");
            String scoresJson = helper.getPerplexityScoreInJSON(accessToken, host, lmpairId,
                    sourceList, targetList);
            System.out.println("scores json: " + scoresJson);
            PerplexityScores scores = helper.getPerplexityScore(accessToken, host, lmpairId,
                    sourceList, targetList);
        }
        finally
        {
            // NOTE that HttpClient must be closed after an unit running is
            // finished.
            helper.shutdownHttpClient();
        }
    }
}
