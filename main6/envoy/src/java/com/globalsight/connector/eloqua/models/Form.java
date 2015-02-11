package com.globalsight.connector.eloqua.models;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.globalsight.cxe.entity.eloqua.EloquaConnector;
import com.globalsight.util.FileUtil;

public class Form
{
    static private final Logger logger = Logger.getLogger(Form.class);

    private String id;
    private String html;
    private JSONObject json;

    // Save connection information for export.
    private EloquaConnector connect;

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getHtml()
    {
        return html;
    }

    public void setHtml(String html)
    {
        this.html = html;
    }

    public JSONObject getJson()
    {
        return json;
    }

    public void setJson(JSONObject json)
    {
        this.json = json;
    }

    public EloquaConnector getConnect()
    {
        return connect;
    }

    public void setConnect(EloquaConnector connect)
    {
        this.connect = connect;
    }

//    public void saveToFile(File f)
//    {
//        String content = getHtmlContent().getHtmlBody();
//        StringBuffer sb = new StringBuffer();
//        sb.append("<html><head>");
//        sb.append("<titel>").append(getName()).append("</titel>");
//        sb.append("<titel>").append(getSubject()).append("</titel>");
//        sb.append("</head>");
//        sb.append(content);
//        sb.append("</html>");
//
//        try
//        {
//            FileUtil.writeFile(f, sb.toString(), "utf-8");
//        }
//        catch (IOException e)
//        {
//            logger.error(e);
//        }
//    }
//    
//    /**
//     * Update the translated name, subject and html body
//     * @param f
//     */
//    public void updateFromFile(File f)
//    {
//        String content;
//        try
//        {
//            content = FileUtil.readFile(f, "utf-8");
//            int i1 = content.indexOf("<titel>") + "<titel>".length();
//            int i2 = content.indexOf("</titel>", i1);
//            this.name = content.substring(i1, i2);
//            
//            i1 = content.indexOf("<titel>", i2 + 5) + "<titel>".length();
//            i2 = content.indexOf("</titel>", i1);
//            this.subject = content.substring(i1, i2);
//            
//            i1 = i2 + "</titel></head>".length();
//            i2 = content.length() - "</html>".length();
//            String content2 = content.substring(i1, i2);
//            getHtmlContent().setHtmlBody(content2);
//        }
//        catch (IOException e)
//        {
//            logger.error(e);
//        }        
//    }
//    
//
//    public void saveJsonToFile(File f)
//    {
//        String content = getHtmlContent().getHtmlBody();
//        String txt = getPlainText();
//
//        getHtmlContent().setHtmlBody("");
//        setPlainText("");
//        Gson gson = new Gson();
//        String ob = gson.toJson(this);
//        getHtmlContent().setHtmlBody(content);
//        setPlainText(txt);
//
//        try
//        {
//            FileUtil.writeFile(f, ob, "utf-8");
//        }
//        catch (IOException e)
//        {
//            logger.error(e);
//        }
//    }
//
//    public static Email loadFromFile(File f)
//    {
//        try
//        {
//            Email email = new Email();
//            
//            String content = FileUtil.readFile(f, "utf-8");
//            Gson gson = new Gson();
//            email = gson.fromJson(content, Email.class);
//            return email;
//        }
//        catch (IOException e)
//        {
//            logger.error(e);
//        }
//        
//        return null;
//    }
}
