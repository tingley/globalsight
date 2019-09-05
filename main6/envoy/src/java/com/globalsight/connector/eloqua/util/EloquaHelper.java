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
package com.globalsight.connector.eloqua.util;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.globalsight.connector.eloqua.models.Alls;
import com.globalsight.connector.eloqua.models.EloquaObject;
import com.globalsight.connector.eloqua.models.Email;
import com.globalsight.connector.eloqua.models.Form;
import com.globalsight.connector.eloqua.models.LandingPage;
import com.globalsight.connector.eloqua.models.SearchResponse;
import com.globalsight.connector.eloqua.util.config.AutoMapping;
import com.globalsight.connector.eloqua.util.config.AutoMappingObj;
import com.globalsight.connector.eloqua.util.config.EloquaConnectorMapping;
import com.globalsight.connector.eloqua.util.config.Mapping;
import com.globalsight.connector.eloqua.util.config.MappingObj;
import com.globalsight.cxe.entity.eloqua.EloquaConnector;
import com.globalsight.cxe.util.XmlUtil;
import com.globalsight.everest.localemgr.LocaleManagerWLRemote;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.StringUtil;
//import com.google.gson.Gson;

public class EloquaHelper
{
    private Client _client;
    public final String EMAIL = "email";
    public final String PAGE = "landingPage";
    private final String DYNAMIC_CONTENT = "dynamicContent";
    private static final Logger logger = Logger.getLogger(EloquaHelper.class);
    public static Map<String, String> USERS = new HashMap<String, String>();
    
    private static final String CONFIG_FILE = "/properties/EloquaConnectorMapping.xml";
    private static EloquaConnectorMapping CONFIG = new EloquaConnectorMapping();
    private static long CONFIG_LAST = 0;
    
//    private static Pattern DYNAMIC_PATTERN = Pattern.compile("(><span )(elqid=\"[^\"]*\" elqtype=\"DynamicContent\")([^>]*>[\\d\\D]*?</span><)");
    private static Pattern DYNAMIC_PATTERN = Pattern.compile("(<span )elqid=\"[^\"]*\" elqtype=\"DynamicContent\"([^>]*>)([\\d\\D]*?)</span>");
//    private static Pattern DYNAMIC_PATTERN = Pattern.compile("(><span elqid=\")([^\"]*)(\" elqtype=\"DynamicContent\"[^>]*>)([\\d\\D]*?)(</span><)");
    private EloquaConnector conn;
    // api document:http://docs.oracle.com/cloud/latest/marketingcs_gs/OMCAB/index.html#Developers/RESTAPI/REST-API.htm%3FTocPath%3D%2520REST%2520API%7C_____0
   
    static
    {
        USERS.put("--", "--");
    }
    
    public EloquaHelper(EloquaConnector conn)
    {
        this.conn = conn;
        _client = new Client(conn.getCompany() + "\\" + conn.getUsername(),
                conn.getPassword(), conn.getUrl());
    }

    public EloquaHelper(String site, String user, String password,
            String baseUrl)
    {
        _client = new Client(site + "\\" + user, password, baseUrl);
    }
    
    public String login()
    {
        Response response = _client.get("/id");
        String s = response.body;
        if (s != null && s.startsWith("{"))
        {
            try
            {
                JSONObject o = new JSONObject(s);
                String url = o.getJSONObject("urls").getJSONObject("apis").getJSONObject("rest").getString("standard");
                url = StringUtil.replace(url, "{version}", "2.0");
                
                return url;
            }
            catch (JSONException e)
            {
                return null;
            }
            
        }
        
        return null;
    }
    
    public boolean doTest()
    {
        if (conn.getUrl().startsWith("https://login.eloqua.com"))
        {
            String s = login();
            if (s == null)
                return false;
            
            conn.setUrl(s);
            return true;
        }
        
        try
        {
            Response response = _client.get("/assets/emails?page=1&count=1");
            if (response.e != null)
            {
//                logger.error(response.e);
                return false;
            }
        }
        catch (Exception e)
        {
//            logger.error(e);
            return false;
        }
        
        return true;
    }

    public JSONObject get(String id, String className)
    {
        Response response = _client.get("/assets/" + className + "/" + id);
        try
        {
            return new JSONObject(response.body);
        }
        catch (JSONException e)
        {
            logger.error(e);
            logger.error(response.body);
        }
        
        return null;
    }
    
    public JSONObject getAll(String className)
    {
        Response response = _client.get("/assets/" + className + "s");
        try
        {
            return new JSONObject(response.body);
        }
        catch (JSONException e)
        {
            logger.error(e);
            logger.error(response.body);
        }
        
        return null;
    }
    
    public JSONObject getAllHeaders()
    {
        return getObjectAll("email/header");
    }
    
    public JSONObject getAllFooters()
    {
        return getObjectAll("email/footer");
    } 
    
    public JSONObject getObjectAll(String className)
    {
        int i = 1;
        JSONObject ob = getAll(className, i++, 1000);
        try
        {
            JSONArray els = ob.getJSONArray("elements");
            while(true) 
            {
                JSONObject ob2 = getAll(className, i++, 1000);
                if (ob2 == null)
                    break;
                
                JSONArray els2 = ob2.getJSONArray("elements");
                if (els2.length() == 0)
                    break;
                
                for (int j = 0; j < els2.length(); j++)
                {
                    els.put(els2.get(j));
                }
            }
        }
        catch (JSONException e)
        {
            logger.error(e);
        }
        return ob;
    }
    
    public JSONObject getAll(String className, int page, int count)
    {
        Response response = _client.get("/assets/" + className + "s?page=" + page + "&count=" + count);
        try
        {
            return new JSONObject(response.body);
        }
        catch (JSONException e)
        {
            logger.error(e);
            logger.error(response.body);
        }
        
        return null;
    }

    
    public JSONObject getData()
    {
        Response response = _client.get("/data/form");
        try
        {
            return new JSONObject(response.body);
        }
        catch (JSONException e)
        {
            logger.error(e);
            logger.error(response.body);
        }
        
        return null;
    }
    
    public void update(String id, String className, JSONObject ob)
    {

        try
        {
            _client.put("/assets/" + className + "/" + id, ob.toString());
        }
        catch (Exception e)
        {
            logger.error(e);
        }
    }
    
    public JSONObject save(String className, JSONObject ob)
    {
        Response response = _client.post("/assets/" + className,
                ob.toString());
        try
        {
            return new JSONObject(response.body);
        }
        catch (Exception e)
        {
            logger.error(e);
            logger.error(response.body);
        }
        
        return null;
    }
    
    public JSONObject save(String className, String ob)
    {
        Response response = _client.post("/assets/" + className,
                ob);
        try
        {
            return new JSONObject(response.body);
        }
        catch (Exception e)
        {
            logger.error(e);
            logger.error(response.body);
        }
        
        return null;
    }
    
    public JSONObject delete(String className, String id)
    {
        _client.delete("/assets/" + className + "/" + id);
        
        return null;
    }
    
    public Form getForm(String id)
    {
        JSONObject ob = get(id, "form");
        if (ob != null)
        {
            Form f = new Form();
            f.setJson(ob);
            try
            {
                f.setId(ob.getString("id"));
                if (ob.has("html"))
                {
                    f.setHtml(ob.getString("html"));
                }
                
                return f;
            }
            catch (JSONException e)
            {
                logger.error(e);
                return null;
            }
        }
        
        return null;
    }
    
    public void updateForm(Form f)
    {
        JSONObject ob = f.getJson();
        update(f.getId(), "form", ob);
    }

    public Email getEmail(String id)
    {
        JSONObject ob = get(id, EMAIL);
        if (ob == null)
            return null;
        
        Email email = new Email();
        email.setJson(ob, false);
        return email;
    }
    
    public LandingPage getLandingPage(String id)
    {
        JSONObject ob = get(id, PAGE);
        if (ob == null)
            return null;
        
        LandingPage p = new LandingPage();
        p.setJson(ob, false);
//        p.updateForm(ob, new EloquaHelper(conn));
        return p;
    }
    
    public List<Email> getEmails()
    {
        List<Email> all = new ArrayList<Email>();
        
        JSONObject ob = getAll(EMAIL);
        if (ob == null)
            return all;
        
        try
        {
            JSONArray es = ob.getJSONArray("elements");
            for (int i = 0; i < es.length(); i++)
            {
                Email e = new Email();
                e.setJson((JSONObject) es.get(i), true);
                setCreateBy(e);
                setCreateAt(e);
                all.add(e);
            }
        }
        catch (JSONException e)
        {
            logger.error(e);
        }
        
        return all;
    }
    
    public Alls getEmails(int page, int pageSize)
    {
        JSONObject ob = getAll(EMAIL, page, pageSize);
        if (ob == null)
            return null;
        
        Alls all = new Alls();
        all.setPage(page);
        all.setPageSize(pageSize);
        try
        {
            all.setTotal(ob.getInt("total"));
            JSONArray es = ob.getJSONArray("elements");
            for (int i = 0; i < es.length(); i++)
            {
                Email e = new Email();
                e.setJson((JSONObject) es.get(i), true);
                setCreateBy(e);
                setCreateAt(e);
                all.addElements(e);
            }
        }
        catch (JSONException e)
        {
            logger.error(e);
        }
        
        return all;
    }
    
    private void setCreateBy(EloquaObject ob)
    {
        String id = ob.getString("createdBy");
        String createBy = USERS.get(id);
        if (createBy == null)
        {
            createBy = getUser(id);
            
            if (createBy == null)
                createBy = "--";
            
            USERS.put(id, createBy);
        }
        
        ob.setCreateBy(createBy);
    }
    
    private void setCreateAt(EloquaObject ob)
    {
        String createAt = "--";
        if (ob.getJson().has("createdAt"))
        {
            String d = ob.getString("createdAt");
            DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
            long t = Long.parseLong(d);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(t * 1000);
            
            createAt = dateFormat.format(calendar.getTime());
        }
        
        ob.setCreatedAt(createAt);
    }
    
    public List<LandingPage> getPages()
    {
        List<LandingPage> all = new ArrayList<LandingPage>();
        JSONObject ob = getAll(PAGE);
        if (ob == null)
            return all;
        
        try
        {
            JSONArray es = ob.getJSONArray("elements");
            for (int i = 0; i < es.length(); i++)
            {
                LandingPage e = new LandingPage();
                e.setJson((JSONObject) es.get(i), true);
                setCreateBy(e);
                setCreateAt(e);
                all.add(e);
            }
        }
        catch (JSONException e)
        {
            logger.error(e);
        }
        
        return all;
    }
    
    
    public Alls getPages(int page, int pageSize)
    {
        JSONObject ob = getAll(PAGE, page, pageSize);
        if (ob == null)
            return null;
        
        Alls all = new Alls();
        all.setPage(page);
        all.setPageSize(pageSize);
        try
        {
            all.setTotal(ob.getInt("total"));
            JSONArray es = ob.getJSONArray("elements");
            for (int i = 0; i < es.length(); i++)
            {
                LandingPage e = new LandingPage();
                e.setJson((JSONObject) es.get(i), true);
                setCreateBy(e);
                setCreateAt(e);
                all.addElements(e);
            }
        }
        catch (JSONException e)
        {
            logger.error(e);
        }
        
        return all;
    }

    public void updateEmail(Email email)
    {
        try
        {
            _client.put("/assets/email/" + email.getId(), email.getJson().toString());
        }
        catch (Exception e)
        {
            logger.error(e);
        }
    }

    public Email saveEmail(Email email)
    {
        Response response = _client.post("assets/email", email.getJson().toString());
        try
        {
            Email e = new Email();
            JSONObject json = new JSONObject(response.body);
            e.setJson(json, false);
            return e;
        }
        catch (Exception e)
        {
            logger.error(e);
            logger.error(response.body);
        }
        
        return null;
    }
    
    public void updateLandingPage(LandingPage page)
    {
        JSONObject js = page.getJson();
        try
        {
            js.put("relativePath", "");
        }
        catch (JSONException e1)
        {
            logger.error(e1);
        }
        
        try
        {
            _client.put("/assets/landingPage/" + page.getId(), js.toString());
        }
        catch (Exception e)
        {
            logger.error(e);
        }
    }
    
    private String newDynamicContent(String oldId, String newId, String targetLocale, String newContent) throws JSONException
    {
        JSONObject old = get(oldId, DYNAMIC_CONTENT);
        String name = old.getString("name");
        String newName = name + "(" + targetLocale + ")";
        old.put("name", newName);
        JSONObject defaultContent = old.getJSONObject("defaultContentSection");
        defaultContent.put("contentHtml", newContent);
        JSONObject newOb = save("dynamicContent", old);
        if (newOb != null)
        {
            return newOb.getString("id");
        }
        
        return oldId;
    }
    
    public String updateDynamicContent(String content, String targetLocale)
    {
        return StringUtil.replaceWithRE(content, "<span elqid=\"[^\"]*\" elqtype=\"[^\"]*\"", "<span");
    }

    public LandingPage saveLandingPage(LandingPage page)
    {
        JSONObject js = page.getJson();
        try
        {
            js.put("relativePath", "");
        }
        catch (JSONException e1)
        {
            logger.error(e1);
        }
        
        Response response = _client.post("/assets/landingPage", js.toString());
        try
        {
            LandingPage p = new LandingPage();
            JSONObject json = new JSONObject(response.body);
            p.setJson(json, false);
            return p;
        }
        catch (Exception e)
        {
            logger.error(e);
            logger.error(response.body);
        }
        
        return null;
    }
    
    public String getUser(String id)
    {
        Response response = _client.get("/system/user/" + id);
        try
        {
            JSONObject json = new JSONObject(response.body);
            return json.getString("name");
        }
        catch (Exception e)
        {
            logger.error(e);
            logger.error(response.body);
        }
        return null;
    }
    
    public SearchResponse<Email> getSortEmails(int page, int count)
    {
        SearchResponse<Email> emails = null;
        try
        {
            Response response = _client.get("/assets/emails?sort=id&dir=desc");
            JSONObject ob = new JSONObject(response.body);
            
            Alls all = new Alls();

            try
            {
                all.setTotal(ob.getInt("total"));
                JSONArray es = ob.getJSONArray("elements");
                for (int i = 0; i < es.length(); i++)
                {
                    Email e = new Email();
                    e.setJson((JSONObject) es.get(i), true);
                    setCreateBy(e);
                    all.addElements(e);
                }
                es.length();
            }
            catch (JSONException e)
            {
                logger.error(e);
            }
        }
        catch (Exception e)
        {
            logger.error(e);
        }
        return emails;
    }
    
    public static void updateEmailHeaderFooter(Email sourceEmail, Email targetEmail,
            String sourceLocale, String targetLocale)
    {
        updateEmailHeaderFooter("emailHeaderId", sourceEmail, targetEmail, sourceLocale,
                targetLocale);
        updateEmailHeaderFooter("emailFooterId", sourceEmail, targetEmail, sourceLocale,
                targetLocale);
    }

    public static void updateEmailHeaderFooter(String name, Email sourceEmail, Email targetEmail,
            String sourceLocale, String targetLocale)
    {
        JSONObject src = sourceEmail.getJson();
        JSONObject trg = targetEmail.getJson();
        EloquaConnectorMapping config = getConfig();

        if (src.has(name))
        {
            try
            {
                String footerId = (String) src.get(name);
                List<Mapping> mappings;

                boolean isFooter = name.equals("emailFooterId");
                if (isFooter)
                {
                    mappings = config.getFooters();
                }
                else
                {
                    mappings = config.getHeaders();
                }

                boolean found = false;
                for (Mapping f : mappings)
                {
                    if (f.getSourceId() == Integer.parseInt(footerId))
                    {
                        List<MappingObj> tOjbs = f.getTarget();
                        for (MappingObj t : tOjbs)
                        {
                            if (t.getLanguage().equalsIgnoreCase(targetLocale))
                            {
                                trg.put(name, t.getId());
                                found = true;
                                break;
                            }
                        }
                        break;
                    }
                }
                
                //auto mapping
                if (!found)
                {
                    AutoMapping autoMapping = config.getAutoMapping();
                    if (autoMapping.getOpen())
                    {
                        EloquaConnector conn = sourceEmail.getConnect();
                        EloquaHelper h = new EloquaHelper(conn);
                        
                        JSONObject srcObj = isFooter ? h.get(footerId, "email/footer") : h.get(footerId, "email/header");
                        if (srcObj == null)
                            return;
                        
                        String srcName = srcObj.getString("name").toLowerCase();
                        
                        LocaleManagerWLRemote localeMgr = ServerProxy.getLocaleManager();
                        try
                        {
                            // find the prefix of the name if the name if "prefix+locale" format
                            String prefix = null;
                            
                            // if the name is end with source locale
                            if (srcName.endsWith(sourceLocale.toLowerCase()))
                            {
                                prefix = srcName.substring(0, srcName.length() - sourceLocale.length());
                            }
                            
                            // if the name is end with eloqua locale
                            if (prefix == null)
                            {
                                List<AutoMappingObj> objs = autoMapping.getLanguageMapping();
                                for (AutoMappingObj ob :objs)
                                {
                                    if (srcName.endsWith(ob.getEloquaLanguage().toLowerCase()))
                                    {
                                        prefix = srcName.substring(0, srcName.length() - ob.getEloquaLanguage().length());
                                        break;
                                    }
                                }
                            }
                            
                            // if the name is end with any gs locale
                            if (prefix == null)
                            {
                                Vector<GlobalSightLocale> locales = localeMgr.getAvailableLocales();
                                for (GlobalSightLocale local : locales)
                                {
                                    String localName = local.toString().toLowerCase();
                                    if (srcName.endsWith(localName))
                                    {
                                        prefix = srcName.substring(0, srcName.length() - localName.length());
                                        break;
                                    }
                                }
                            }
                            
                            if (prefix != null)
                            {
                                JSONObject targetObj = null;
                                
                                // search all footers or headers
                                JSONObject obs = isFooter ? h.getAllFooters() : h.getAllHeaders();
                                JSONArray els = obs.getJSONArray("elements");
                                for (int i = 0; i < els.length(); i++)
                                {
                                    JSONObject obj = els.getJSONObject(i);
                                    String objName = obj.getString("name").toLowerCase();
                                    
                                    // start with prefix
                                    if (objName.startsWith(prefix))
                                    {
                                        // if end with the gs target locale.
                                        String suffix = objName.substring(prefix.length());
                                        if (suffix.equalsIgnoreCase(targetLocale))
                                        {
                                            targetObj = obj;
                                        }
                                        else
                                        {
                                            // if end with the eloqua target local.
                                            List<AutoMappingObj> objs = autoMapping.getLanguageMapping();
                                            for (AutoMappingObj ob :objs)
                                            {
                                                if (ob.getGlobalSightLanguage().equalsIgnoreCase(targetLocale) && ob.getEloquaLanguage().equalsIgnoreCase(suffix))
                                                {
                                                    targetObj = obj;
                                                    break;
                                                }
                                            }
                                            
                                            if (targetObj != null)
                                                break;
                                        }
                                    }
                                }
                                
                                // if found. update the id.
                                if (targetObj != null)
                                {
                                    trg.put(name, targetObj.getString("id"));
                                }
                            }
                        }
                        catch (Exception e)
                        {
                            logger.error(e);
                        }
                    }
                }
            }
            catch (JSONException e)
            {
                logger.error(e);
            }
        }
    }
    
    public static EloquaConnectorMapping getConfig()
    {
        File f = getConfigFile();
        if (f == null)
            return CONFIG;
        
        long t = f.lastModified();
        if (t != CONFIG_LAST)
        {
            CONFIG = XmlUtil.load(EloquaConnectorMapping.class, f.getAbsolutePath());
            CONFIG_LAST = t;
        }
        
        return CONFIG;
    }
    
    private static File getConfigFile()
    {
       
        URL url = EloquaHelper.class.getResource(CONFIG_FILE);

        if (url == null)
        {
            logger.error("File " + CONFIG_FILE
                    + " not found");
            return null;
        }

        try
        {
            return new File(url.toURI().getPath());
        }
        catch (URISyntaxException e)
        {
            logger.error(e);
            return null;
        }
    }
}
