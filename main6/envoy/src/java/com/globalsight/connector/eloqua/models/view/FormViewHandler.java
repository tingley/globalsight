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
package com.globalsight.connector.eloqua.models.view;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.globalsight.connector.eloqua.models.Form;
import com.globalsight.connector.eloqua.models.form.EloquaGsForm;
import com.globalsight.connector.eloqua.util.EloquaHelper;
import com.globalsight.cxe.util.XmlUtil;
import com.globalsight.util.FileUtil;

/**
 * 
 * Used to handler Form view. But we can not create a form with eloqua api.
 *
 */
public class FormViewHandler implements ViewHandler
{
    static private final Logger logger = Logger.getLogger(FormViewHandler.class);

    private EloquaHelper eh;
    private boolean uploaded;
    private String targetLocale;
    private String sourceFolder;
    private String targetFolder;
    private static Pattern P = Pattern
            .compile("<eloquaGsForm id=\"[^\"]*\" newId=\"[^\"]*\">[\\d\\D]*?</eloquaGsForm>");
    private String root;

    private List<String> values = new ArrayList<>();
    private List<EloquaGsForm> forms = new ArrayList<>();

    @Override
    public String getTyle()
    {
        return "CoreOrion.FormView.design";
    }

    @Override
    public void handContent(String content) throws JSONException
    {
        JSONObject tt = new JSONObject(content);
        if (tt.has("contentId"))
        {
            String id = tt.getString("contentId");
            Form f = eh.getForm(id);
            JSONObject ob = f.getJson();
            try
            {
                FileUtil.writeFile(new File(root, id + ".form"), ob.toString(1), "utf-8");
            }
            catch (IOException e)
            {
                logger.error(e);
            }

            if (ob.has("elements"))
            {
                JSONArray els = ob.getJSONArray("elements");

                EloquaGsForm xml = new EloquaGsForm();
                xml.setId(id);
                for (int j = 0; j < els.length(); j++)
                {
                    JSONObject el = els.getJSONObject(j);
                    Form.addAllElements(el, xml, eh);
                }
                String xml2 = XmlUtil.object2String(xml, true);
                xml2 = xml2.substring(xml2.indexOf("<", 5));
                values.add(xml2);
            }
        }
    }

    public List<String> getValues()
    {
        return values;
    }

    public void setValues(List<String> values)
    {
        this.values = values;
    }

    @Override
    public void readContentFromFile(String content)
    {
        Matcher m = P.matcher(content);
        int start = 0;
        while (m.find(start))
        {
            forms.add(XmlUtil.string2Object(EloquaGsForm.class, m.group()));
            start = m.end();
        }
    }

    @Override
    public String updateContentFromFile(String content) throws JSONException
    {
        boolean isNew = false;
        JSONObject tt = new JSONObject(content);
        if (tt.has("contentId"))
        {
            String id = tt.getString("contentId");
            File form = new File(targetFolder, id + ".form");
            if (!form.exists())
            {
                form = new File(sourceFolder, id + ".form");
                isNew = true;
            }
            
            String formContent = "";
            Form f = new Form();
            JSONObject obf = null;
            try
            {
                formContent = FileUtil.readFile(form, "utf-8");
                obf = new JSONObject(formContent);
            }
            catch (IOException e1)
            {
                logger.error(e1);
                return content;
            }

            f.setJson(obf);

            for (EloquaGsForm f1 : forms)
            {
               
                    if (f1.getId().equalsIgnoreCase(id))
                    {
                        try
                        {
                            JSONObject ob = Form.updateForm(XmlUtil.object2String(f1), eh, sourceFolder, targetFolder,
                                    targetLocale);
                           
                            if (isNew)
                            {
                                if (ob != null && ob.has("id"))
                                {
                                    String newId = ob.getString("id");
                                    tt.put("contentId", newId);
                                }
                            }
                        }
                        catch (Exception e)
                        {
                            logger.error(e);
                        }
                        break;
                    }
            }
        }

        return tt.toString();
    }

    public EloquaHelper getEh()
    {
        return eh;
    }

    public void setEh(EloquaHelper eh)
    {
        this.eh = eh;
    }

    public boolean isUploaded()
    {
        return uploaded;
    }

    public void setUploaded(boolean uploaded)
    {
        this.uploaded = uploaded;
    }

    public String getTargetLocale()
    {
        return targetLocale;
    }

    public void setTargetLocale(String targetLocale)
    {
        this.targetLocale = targetLocale;
    }

    public String getRoot()
    {
        return root;
    }

    public void setRoot(String root)
    {
        this.root = root;
    }

    public String getSourceFolder()
    {
        return sourceFolder;
    }

    public void setSourceFolder(String sourceFolder)
    {
        this.sourceFolder = sourceFolder;
    }

    public String getTargetFolder()
    {
        return targetFolder;
    }

    public void setTargetFolder(String targetFolder)
    {
        this.targetFolder = targetFolder;
    }
}
