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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.globalsight.connector.eloqua.util.EloquaHelper;
import com.globalsight.util.FileUtil;

public class ViewUtil
{
    private static Pattern P = Pattern.compile("<eloquaSubject>([\\d\\D]*?)</eloquaSubject>");
    
    private String subject = null;
    private String root;
    private EloquaHelper eh;

    public ViewUtil(String root, EloquaHelper eh)
    {
        super();
        this.root = root;
        this.eh = eh;
    }

    public String generateHtml() throws JSONException
    {
        StringBuffer sb = new StringBuffer("<body>").append("\n");
        
        if (subject != null){
            sb.append("<eloquaSubject>").append(subject).append("</eloquaSubject>").append("\n");
        }
        
        addTextBoxView(sb);
        addImgTitle(sb);
        addReplaceableContentView(sb, eh);
//        addFormView(sb, eh, f.getParentFile());
        sb.append("</body>");
        return sb.toString();
    }
    
    public String generateHtml(File f) throws Exception
    {
        StringBuffer sb = new StringBuffer("<body>").append("\n");
        
        if (subject != null){
            sb.append("<eloquaSubject>").append(subject).append("</eloquaSubject>").append("\n");
        }
        
        addTextBoxView(sb);
        addImgTitle(sb);
        addReplaceableContentView(sb, eh);
        addFormView(sb, eh, f.getParentFile());
        sb.append("</body>");
        FileUtil.writeFile(f, sb.toString(), "utf-8");
        return sb.toString();
    }
    
    public void addTextBoxView(StringBuffer sb) throws JSONException
    {
        TextBoxViewHandler handler = new TextBoxViewHandler();
        addView(root, handler);

        sb.append("<eloquaTxts>").append("\n");
        for (String txt : handler.getValues())
        {
            sb.append("<eloquaTxt>").append(txt).append("</eloquaTxt>")
                    .append("\n");
        }
        sb.append("</eloquaTxts>").append("\n");
    }

    public void addImgTitle(StringBuffer sb) throws JSONException
    {
        ImageViewHandler handler = new ImageViewHandler();
        addView(root, handler);

        sb.append("<eloquaImgTitles>").append("\n");
        for (String txt : handler.getValues())
        {
            sb.append("<eloquaImg>").append(txt).append("</eloquaImg>").append("\n");
        }
        sb.append("</eloquaImgTitles>").append("\n");
    }
    
    public String getSubject()
    {
        return subject;
    }

    public void setSubject(String subject)
    {
        this.subject = subject;
    }
    
    public void addFormView(StringBuffer sb, EloquaHelper eh, File f)
            throws JSONException
    {
        FormViewHandler handler = new FormViewHandler();
        handler.setEh(eh);
        handler.setRoot(f.getAbsolutePath());
        addView(root, handler);

        sb.append("<eloquaGsForms>").append("\n");
        for (String form : handler.getValues())
        {
            sb.append(form);
        }
        sb.append("</eloquaGsForms>").append("\n");
    }

    public void addReplaceableContentView(StringBuffer sb, EloquaHelper eh)
            throws JSONException
    {
        ReplaceableContentViewHandler handler = new ReplaceableContentViewHandler();
        addView(root, handler);

        sb.append("<eloquaContentSections>").append("\n");
        for (String id : handler.getContentSections())
        {
            JSONObject jo = eh.get(id, "contentSection");
            sb.append("<eloquaContentSection>");
            sb.append(jo.getString("contentHtml"));
            sb.append("</eloquaContentSection>").append("\n");
        }
        sb.append("</eloquaContentSections>").append("\n");

        sb.append("<eloquaDynamicContents>").append("\n");
        for (String id : handler.getDynamicContents())
        {
            sb.append("<eloquaDynamicContent>").append("\n");
            JSONObject jo = eh.get(id, "dynamicContent");
            JSONObject dc = jo.getJSONObject("defaultContentSection");
            sb.append("<eloquaDefaultDynamicContentSection>");
            sb.append(dc.getString("contentHtml"));
            sb.append("</eloquaDefaultDynamicContentSection>").append("\n");

            JSONArray js = jo.getJSONArray("rules");
            for (int i = 0; i < js.length(); i++)
            {
                JSONObject o = (JSONObject) js.get(i);
                JSONObject cs2 = o.getJSONObject("contentSection");
                sb.append("<eloquaDynamicContentSection>");
                sb.append(cs2.getString("contentHtml"));
                sb.append("</eloquaDynamicContentSection>").append("\n");
            }
            sb.append("</eloquaDynamicContent>").append("\n");
        }
        sb.append("</eloquaDynamicContents>").append("\n");

        sb.append("<eloquaSignatureLayouts>").append("\n");
        for (String id : handler.getSignatureLayouts())
        {
            sb.append("<eloquaSignatureLayout>");
            JSONObject jo = eh.get(id, "email/signature/layout");
            sb.append(jo.getString("body"));
            sb.append("</eloquaSignatureLayout>").append("\n");
        }
        sb.append("</eloquaSignatureLayouts>").append("\n");
    }

    public String updateFromFile(String fileContent, boolean uploaded, String targetLocale, String sourceFolder, String targetFolder)
            throws JSONException
    {
        ReplaceableContentViewHandler replaceableHandler = new ReplaceableContentViewHandler();
        replaceableHandler.setUploaded(uploaded);
        replaceableHandler.setEh(eh);
        replaceableHandler.setTargetLocale(targetLocale);
        
//        FormViewHandler formHandler = new FormViewHandler();
//        formHandler.setUploaded(uploaded);
//        formHandler.setEh(eh);
//        formHandler.setSourceFolder(sourceFolder);
//        formHandler.setTargetFolder(targetFolder);
//        formHandler.setTargetLocale(targetLocale);

        root = updateFromFile(fileContent, new TextBoxViewHandler());
        root = updateFromFile(fileContent, new ImageViewHandler());
        root = updateFromFile(fileContent, replaceableHandler);
        //root = updateFromFile(fileContent, formHandler);

        return root;
    }

    private String updateFromFile(String fileContent, ViewHandler handler)
            throws JSONException
    {
        handler.readContentFromFile(fileContent);
        String type = handler.getTyle() + "({";

        int l = type.length();
        int n = root.indexOf(type);

        StringBuffer sb = new StringBuffer();
        int start = 0;

        while (n > 0)
        {
            int f = 1;

            int j = 1;
            while (f > 0)
            {
                char c = root.charAt(n + l + j);
                if (c == ')')
                {
                    f--;
                }
                else if (c == '(')
                {
                    f++;
                }

                j++;
            }

            String text = root.substring(n + l - 1, n + l + j - 1);

            sb.append(root.substring(start, n + l - 1));
            sb.append(handler.updateContentFromFile(text));

            start = n + l + j - 1;

            n = root.indexOf(type, n + 1);
        }

        sb.append(root.substring(start));

        return sb.toString();
    }

    public void addView(String root, ViewHandler handler) throws JSONException
    {
        String type = handler.getTyle() + "({";
        int l = type.length();
        int n = root.indexOf(type);
        while (n > 0)
        {
            int f = 1;

            int j = 1;
            while (f > 0)
            {
                char c = root.charAt(n + l + j);
                if (c == ')')
                {
                    f--;
                }
                else if (c == '(')
                {
                    f++;
                }

                j++;
            }

            String text = root.substring(n + l - 1, n + l + j - 1);
            handler.handContent(text);

            n = root.indexOf(type, n + 1);
        }
    }
    
    public String getEmailSubject(String fileContent)
    {
        Matcher m = P.matcher(fileContent);
        if (m.find())
        {
            return m.group(1);
        }
        
        return null;
    }
}
