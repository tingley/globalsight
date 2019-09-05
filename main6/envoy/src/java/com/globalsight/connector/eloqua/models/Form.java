package com.globalsight.connector.eloqua.models;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.globalsight.connector.eloqua.models.form.EloquaGsForm;
import com.globalsight.connector.eloqua.models.form.IdReplacer;
import com.globalsight.connector.eloqua.models.form.Select;
import com.globalsight.connector.eloqua.util.EloquaHelper;
import com.globalsight.cxe.entity.eloqua.EloquaConnector;
import com.globalsight.cxe.util.XmlUtil;
import com.globalsight.util.FileUtil;
import com.globalsight.util.StringUtil;

public class Form
{
    static private final Logger logger = Logger.getLogger(Form.class);

    private static String GS_FORM1 = "<eloquaGsForm>";
    private static String GS_FORM2 = "</eloquaGsForm>";
    private static String FORM_DEV = "<div elqid=\"{0}\" elqtype=\"UserForm\" elqformname=\"{1}\"></div>";
    private static String TERM_DEV = "<div style=\"color:red; clear:both; \"> WARN: Form has changed. The page will be updated automatically after form is saved. Please double click the div to open form.</div>";
    private static List<String> TYPES = new ArrayList<>();
    static
    {
        TYPES.add("text");
        TYPES.add("checkbox");
    }

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

    public static boolean isText(JSONObject ob) throws JSONException
    {
        return ob.has("displayType") && TYPES.contains(ob.getString("displayType"));
    }

    public static boolean isSelect(JSONObject ob) throws JSONException
    {
        return ob.has("displayType") && ("singleSelect".equals(ob.get("displayType"))
                || "multiSelect".equals(ob.get("displayType"))) && ob.has("optionListId");
    }

    public static boolean isOption(JSONObject ob) throws JSONException
    {
        return ob.has("type") && "Option".equals(ob.get("type"));
    }

    public static void updateAllForms(LandingPage m, EloquaHelper h, String sourceFolder,
            String targetFolder, String targetLocale) throws Exception
    {
        String content = m.getHtml();

        StringBuilder output = new StringBuilder();
        int start = 0;
        int index = content.indexOf(GS_FORM1, start);
        while (index > 0)
        {
            int end = content.indexOf(GS_FORM2, index) + GS_FORM2.length();
            output.append(content.substring(start, index));
            String form = content.substring(index, end);
            JSONObject ob = updateForm(form, h, sourceFolder, targetFolder, targetLocale);
            String dev = MessageFormat.format(FORM_DEV, ob.getString("id"), ob.getString("name"));
            output.append(dev);
            start = end;
            index = content.indexOf(GS_FORM1, start);
        }

        output.append(content.substring(start));
        m.setHtml(output.toString());
    }

    public static JSONObject updateForm(String content, EloquaHelper h, String sourceFolder,
            String targetFolder, String targetLocale) throws Exception
    {
        EloquaGsForm form = XmlUtil.string2Object(EloquaGsForm.class, content);
        String id = form.getId();
        File f = new File(targetFolder, id + ".form");

        JSONObject ob = null;
        boolean isNew = !f.exists();
        if (isNew)
        {
            File f2 = new File(sourceFolder, id + ".form");
            if (!f2.exists())
                return ob;

            String orgFile = FileUtil.readFile(f2, "utf-8");
            orgFile = StringUtil.replaceWithRE(orgFile, Pattern.compile("\"id\":[^\"]*\".*?\""),
                    new IdReplacer(-1));

            ob = new JSONObject(orgFile);

            ob.put("id", "");

            ob.put("processingSteps", "");
            String name = ob.getString("htmlName");
            name = getName(name, id, targetLocale);

            ob.put("htmlName", name);
        }
        else
        {
            String orgFile = FileUtil.readFile(f, "utf-8");
            // orgFile = StringUtil.replaceWithRE(orgFile,
            // Pattern.compile("\"id\":[^\"]*\".*?\""), new IdReplacer(-1));
            ob = new JSONObject(orgFile);
        }

        ob.put("html", TERM_DEV);
        // ob.remove("html");

        JSONArray els = ob.getJSONArray("elements");

        for (int j = 0; j < els.length(); j++)
        {
            JSONObject el = els.getJSONObject(j);
            updateAllElements(el, form, h, targetLocale, isNew);
        }

        if (!f.exists())
        {
            ob = h.save("form", ob.toString(1));
        }
        else
        {
            h.update(ob.getString("id"), "form", ob);
        }

        if (!StringUtil.isEmptyAndNull(ob.getString("id")))
        {
            FileUtil.writeFile(new File(targetFolder, ob.getString("id") + ".form"), ob.toString(1), "utf-8");
        }

        return ob;
    }

    public static String addAllForms(JSONObject json, String html, EloquaHelper h, String root)
    {
        try
        {
            JSONArray forms = json.getJSONArray("forms");
            for (int i = 0; i < forms.length(); i++)
            {
                StringBuffer sb = new StringBuffer();
                JSONObject form = (JSONObject) forms.get(i);
                String id = form.getString("id");
                Form f = h.getForm(id);
                JSONObject ob = f.getJson();

                FileUtil.writeFile(new File(root + id + ".form"), ob.toString(1), "utf-8");

                if (ob.has("elements"))
                {
                    JSONArray els = ob.getJSONArray("elements");

                    EloquaGsForm xml = new EloquaGsForm();
                    xml.setId(id);
                    for (int j = 0; j < els.length(); j++)
                    {
                        JSONObject el = els.getJSONObject(j);
                        Form.addAllElements(el, xml, h);
                    }
                    String xml2 = XmlUtil.object2String(xml, true);
                    xml2 = xml2.substring(xml2.indexOf("<", 5));
                    System.out.println(xml2);
                    sb.append(xml2);
                }

                int index = html.indexOf("<div elqid=\"" + id + "\" elqtype=\"UserForm\" ");
                if (index > 0)
                {
                    int index2 = html.indexOf("<!-- end form -->", index);
                    if (index2 > 0)
                    {
                        html = html.substring(0, index) + sb.toString() + html.substring(index2);
                    }
                }
            }
        }
        catch (Exception e)
        {
            logger.error(e);
        }

        return html;
    }

    private static String getName(String name, String id, String targetLocale)
    {
        name = name + id + new Date().getTime() + "(" + targetLocale + ")";
        if (name.length() > 50)
        {
            name = name.substring(0, 20) + id + new Date().getTime() + "(" + targetLocale + ")";
        }

        return name;
    }

    private static void updateAllElements(JSONObject ob, EloquaGsForm sb, EloquaHelper h,
            String targetLocale, boolean isNew) throws JSONException
    {
        if (ob.has("fields"))
        {
            JSONArray fs = ob.getJSONArray("fields");
            for (int i = 0; i < fs.length(); i++)
            {
                JSONObject el = fs.getJSONObject(i);
                updateAllElements(el, sb, h, targetLocale, isNew);
            }
        }
        else if (ob.has("dataType"))
        {
            String type = ob.getString("dataType");
            if ("text".equals(type))
            {
                ob.put("name", sb.getDiv().remove(0));
                if (ob.has("defaultValue"))
                {
                    ob.put("defaultValue", sb.getDiv().remove(0));
                }

                if (isSelect(ob))
                {
                    String id = ob.getString("optionListId");
                    JSONObject optionlist = h.get(id, "optionList");
                    if (optionlist.has("elements"))
                    {
                        Select select = sb.getSelect().remove(0);
                        JSONArray fs = optionlist.getJSONArray("elements");
                        for (int i = 0; i < fs.length(); i++)
                        {
                            JSONObject el = fs.getJSONObject(i);
                            if (isOption(el))
                            {
                                el.put("displayName", select.getOption().remove(0));
                            }
                        }
                    }

                    String name = optionlist.getString("name");
                    name = getName(name, id, targetLocale);

                    optionlist.put("name", name);

                    if (isNew)
                    {
                        JSONObject el2 = h.save("optionList", optionlist);
                        ob.put("optionListId", el2.getString("id"));
                    }
                    else
                    {
                        h.update(optionlist.getString("id"), "optionList", optionlist);
                    }
                }
            }
        }
    }

    public static void addAllElements(JSONObject ob, EloquaGsForm sb, EloquaHelper h)
            throws JSONException
    {
        if (ob.has("fields"))
        {
            JSONArray fs = ob.getJSONArray("fields");
            for (int i = 0; i < fs.length(); i++)
            {
                JSONObject el = fs.getJSONObject(i);
                addAllElements(el, sb, h);
            }
        }
        else if (ob.has("dataType"))
        {
            String type = ob.getString("dataType");
            if ("text".equals(type))
            {
                sb.getDiv().add(ob.getString("name"));
                if (ob.has("defaultValue"))
                {
                    sb.getDiv().add(ob.getString("defaultValue"));
                }
                // sb.append("<div>").append(ob.getString("name")).append("</div>").append("\n");

                if (isSelect(ob))
                {
                    JSONObject optionlist = h.get(ob.getString("optionListId"), "optionList");
                    if (optionlist.has("elements"))
                    {
                        Select select = new Select();
                        sb.getSelect().add(select);
                        JSONArray fs = optionlist.getJSONArray("elements");
                        for (int i = 0; i < fs.length(); i++)
                        {
                            JSONObject el = fs.getJSONObject(i);
                            if (isOption(el))
                            {
                                select.getOption().add(el.getString("displayName"));
                            }
                        }
                    }
                }
            }
        }
    }

    // public void saveToFile(File f)
    // {
    // String content = getHtmlContent().getHtmlBody();
    // StringBuffer sb = new StringBuffer();
    // sb.append("<html><head>");
    // sb.append("<titel>").append(getName()).append("</titel>");
    // sb.append("<titel>").append(getSubject()).append("</titel>");
    // sb.append("</head>");
    // sb.append(content);
    // sb.append("</html>");
    //
    // try
    // {
    // FileUtil.writeFile(f, sb.toString(), "utf-8");
    // }
    // catch (IOException e)
    // {
    // logger.error(e);
    // }
    // }
    //
    // /**
    // * Update the translated name, subject and html body
    // * @param f
    // */
    // public void updateFromFile(File f)
    // {
    // String content;
    // try
    // {
    // content = FileUtil.readFile(f, "utf-8");
    // int i1 = content.indexOf("<titel>") + "<titel>".length();
    // int i2 = content.indexOf("</titel>", i1);
    // this.name = content.substring(i1, i2);
    //
    // i1 = content.indexOf("<titel>", i2 + 5) + "<titel>".length();
    // i2 = content.indexOf("</titel>", i1);
    // this.subject = content.substring(i1, i2);
    //
    // i1 = i2 + "</titel></head>".length();
    // i2 = content.length() - "</html>".length();
    // String content2 = content.substring(i1, i2);
    // getHtmlContent().setHtmlBody(content2);
    // }
    // catch (IOException e)
    // {
    // logger.error(e);
    // }
    // }
    //
    //
    // public void saveJsonToFile(File f)
    // {
    // String content = getHtmlContent().getHtmlBody();
    // String txt = getPlainText();
    //
    // getHtmlContent().setHtmlBody("");
    // setPlainText("");
    // Gson gson = new Gson();
    // String ob = gson.toJson(this);
    // getHtmlContent().setHtmlBody(content);
    // setPlainText(txt);
    //
    // try
    // {
    // FileUtil.writeFile(f, ob, "utf-8");
    // }
    // catch (IOException e)
    // {
    // logger.error(e);
    // }
    // }
    //
    // public static Email loadFromFile(File f)
    // {
    // try
    // {
    // Email email = new Email();
    //
    // String content = FileUtil.readFile(f, "utf-8");
    // Gson gson = new Gson();
    // email = gson.fromJson(content, Email.class);
    // return email;
    // }
    // catch (IOException e)
    // {
    // logger.error(e);
    // }
    //
    // return null;
    // }
}
