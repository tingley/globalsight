package com.globalsight.connector.eloqua.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.globalsight.cxe.entity.eloqua.EloquaConnector;
//import com.google.gson.Gson;

public class EloquaHelper
{
    private Client _client;
    public final String EMAIL = "email";
    public final String PAGE = "landingPage";
    private static final Logger logger = Logger.getLogger(EloquaHelper.class);
    public static Map<String, String> USERS = new HashMap<String, String>();
    
    public EloquaHelper(EloquaConnector conn)
    {
        _client = new Client(conn.getCompany() + "\\" + conn.getUsername(),
                conn.getPassword(), conn.getUrl());
    }

    public EloquaHelper(String site, String user, String password,
            String baseUrl)
    {
        _client = new Client(site + "\\" + user, password, baseUrl);
    }
    
    public boolean doTest()
    {
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
        }
        
        return null;
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
    
    public void save(String className, JSONObject ob)
    {
        try
        {
            Response response = _client.post("/assets/" + className,
                    ob.toString());
            System.out.println(response.body);
        }
        catch (Exception e)
        {
            logger.error(e);
        }
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
                f.setHtml(ob.getString("html"));
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
        Response response = _client.post("/assets/email", email.getJson().toString());
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
        }
        
        return null;
    }
    
    public void updateLandingPage(LandingPage page)
    {
        try
        {
            _client.put("/assets/landingPage/" + page.getId(), page.getJson().toString());
        }
        catch (Exception e)
        {
            logger.error(e);
        }
    }

    public LandingPage saveLandingPage(LandingPage page)
    {
        Response response = _client.post("/assets/landingPage", page.getJson().toString());
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
        }
        
        return null;
    }
    
    public String getUser(String id)
    {
        try
        {
            Response response = _client.get("/system/user/" + id);
            JSONObject json = new JSONObject(response.body);
            return json.getString("name");
        }
        catch (Exception e)
        {
            logger.error(e);
        }
        return null;
    }
    
   

    public SearchResponse<Email> getEmails(String search, int page, int count)
    {
        SearchResponse<Email> emails = null;
        try
        {
            Response response = _client.get("/assets/emails?search=" + search
                    + "&page=" + page + "&count=" + count);
            System.out.println(response.body);
        }
        catch (Exception e)
        {
            logger.error(e);
        }
        return emails;
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
                    System.out.println(e.getId());
                }
                es.length();
            }
            catch (JSONException e)
            {
                logger.error(e);
            }
            System.out.println(response.body);
        }
        catch (Exception e)
        {
            logger.error(e);
        }
        return emails;
    }
}
