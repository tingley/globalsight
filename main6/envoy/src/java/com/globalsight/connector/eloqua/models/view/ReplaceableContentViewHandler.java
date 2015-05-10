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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.globalsight.connector.eloqua.util.EloquaHelper;

public class ReplaceableContentViewHandler implements ViewHandler
{
    private List<String> dynamicContents = new ArrayList<String>();
    private List<String> contentSections = new ArrayList<String>();
    private List<String> signatureLayouts = new ArrayList<String>();
    private List<DynamicContent> dynamics = new ArrayList<DynamicContent>();

    private EloquaHelper eh;
    private boolean uploaded;
    private String targetLocale;

    private static Pattern CONTENT_SECTION_P = Pattern
            .compile("<eloquaContentSection>([\\d\\D]*?)</eloquaContentSection>");
    private static Pattern SIGNATURE_P = Pattern
            .compile("<eloquaSignatureLayout>([\\d\\D]*?)</eloquaSignatureLayout>");
    private static Pattern DYNAMIC_CONTENT_P = Pattern
            .compile("<eloquaDynamicContent>([\\d\\D]*?)</eloquaDynamicContent>");
    private static Pattern DYNAMIC_CONTENT_DEFAULT_P = Pattern
            .compile("<eloquaDefaultDynamicContentSection>([\\d\\D]*?)</eloquaDefaultDynamicContentSection>");
    private static Pattern DYNAMIC_ROLE_P = Pattern
            .compile("<eloquaDynamicContentSection>([\\d\\D]*?)</eloquaDynamicContentSection>");

    @Override
    public String getTyle()
    {
        return "CoreOrion.ReplaceableContentView.design";
    }

    @Override
    public void handContent(String content) throws JSONException
    {
        JSONObject tt = new JSONObject(content);

        String contentId = tt.getString("contentId");
        String type = tt.getString("contentRecordType");
        if ("DynamicContent".equalsIgnoreCase(type))
        {
            dynamicContents.add(contentId);
        }
        else if ("ContentSection".equalsIgnoreCase(type))
        {
            contentSections.add(contentId);
        }
        else if ("EmailSignatureLayout".equalsIgnoreCase(type))
        {
            signatureLayouts.add(contentId);
        }
    }

    public List<String> getDynamicContents()
    {
        return dynamicContents;
    }

    public void setDynamicContents(List<String> dynamicContents)
    {
        this.dynamicContents = dynamicContents;
    }

    public List<String> getContentSections()
    {
        return contentSections;
    }

    public void setContentSections(List<String> contentSections)
    {
        this.contentSections = contentSections;
    }

    public List<String> getSignatureLayouts()
    {
        return signatureLayouts;
    }

    public void setSignatureLayouts(List<String> signatureLayouts)
    {
        this.signatureLayouts = signatureLayouts;
    }

    @Override
    public void readContentFromFile(String content)
    {
        Matcher m = CONTENT_SECTION_P.matcher(content);
        int start = 0;
        while (m.find(start))
        {
            contentSections.add(m.group(1));
            start = m.end();
        }

        m = SIGNATURE_P.matcher(content);
        start = 0;
        while (m.find(start))
        {
            signatureLayouts.add(m.group(1));
            start = m.end();
        }

        m = DYNAMIC_CONTENT_P.matcher(content);
        start = 0;
        while (m.find(start))
        {
            String dynamic = m.group(1);
            Matcher m2 = DYNAMIC_CONTENT_DEFAULT_P.matcher(dynamic);
            if (m2.find())
            {
                DynamicContent dc = new DynamicContent();
                dc.setDefaultContent(m2.group(1));
                dynamics.add(dc);

                m2 = DYNAMIC_ROLE_P.matcher(dynamic);
                int start2 = 0;
                while (m2.find(start2))
                {
                    dc.getRoles().add(m2.group(1));
                    start2 = m2.end();
                }
            }

            start = m.end();
        }
    }

    private String updateDynamicContent(String id, String content)throws JSONException
    {
        JSONObject jo = eh.get(id, "dynamicContent");
        
        DynamicContent dco = dynamics.remove(0);
        JSONObject dc = jo.getJSONObject("defaultContentSection");
        dc.put("contentHtml", dco.getDefaultContent());
        
        JSONArray js = jo.getJSONArray("rules");
        for (int i = 0; i < js.length(); i++)
        {
            JSONObject o = (JSONObject) js.get(i);
            JSONObject cs2 = o.getJSONObject("contentSection");
            cs2.put("contentHtml", dco.getRoles().get(i));
        }

        if (!isUploaded())
        {
            for (int i = 0; i < js.length(); i++)
            {
                JSONObject o = (JSONObject) js.get(i);
                JSONObject cs = o.getJSONObject("contentSection");
                JSONObject ncs = eh.save("contentSection", cs);
                JSONArray criteria = o.getJSONArray("criteria");
                for (int j = 0; j < criteria.length(); j++)
                {
                    JSONObject c = (JSONObject) criteria.get(j);
                    c.remove("id");
                }
                o.remove("id");
                o.put("contentSection", ncs);
            }
            
            jo.put("name", jo.getString("name") + "(" + targetLocale + ")");
            JSONObject jo2 = eh.save("dynamicContent", jo);
            String contentId2 = jo2.getString("id");
            content = content.replace("contentId: \"" + id + "\"",
                    "contentId: \"" + contentId2 + "\"");
        }
        else
        {
            eh.update(id, "dynamicContent", jo);
        }

        return content;
    }

    private String updateSignature(String id, String content)
            throws JSONException
    {
        JSONObject jo = eh.get(id, "email/signature/layout");

        jo.put("body", signatureLayouts.remove(0));

        if (!isUploaded())
        {
            jo.put("name", jo.getString("name") + "(" + targetLocale + ")");
            JSONObject jo2 = eh.save("email/signature/layout", jo);
            String contentId2 = jo2.getString("id");
            content = content.replace("contentId: \"" + id + "\"",
                    "contentId: \"" + contentId2 + "\"");
        }
        else
        {
            eh.update(id, "email/signature/layout", jo);
        }

        return content;
    }

    private String updateContentSection(String id, String content)
            throws JSONException
    {
        JSONObject jo = eh.get(id, "contentSection");

        jo.put("contentHtml", contentSections.remove(0));

        if (!isUploaded())
        {
            jo.put("name", jo.getString("name") + "(" + targetLocale + ")");
            JSONObject jo2 = eh.save("contentSection", jo);
            String contentId2 = jo2.getString("id");
            content = content.replace("contentId: \"" + id + "\"",
                    "contentId: \"" + contentId2 + "\"");
        }
        else
        {
            eh.update(id, "contentSection", jo);
        }

        return content;
    }

    @Override
    public String updateContentFromFile(String content) throws JSONException
    {
        JSONObject tt = new JSONObject(content);

        String contentId = tt.getString("contentId");
        String type = tt.getString("contentRecordType");
        if ("DynamicContent".equalsIgnoreCase(type))
        {
            content = updateDynamicContent(contentId, content);
        }
        else if ("ContentSection".equalsIgnoreCase(type))
        {
            content = updateContentSection(contentId, content);
        }
        else if ("EmailSignatureLayout".equalsIgnoreCase(type))
        {
            content = updateSignature(contentId, content);
        }

        return content;
    }

    public boolean isUploaded()
    {
        return uploaded;
    }

    public void setUploaded(boolean uploaded)
    {
        this.uploaded = uploaded;
    }

    public EloquaHelper getEh()
    {
        return eh;
    }

    public void setEh(EloquaHelper eh)
    {
        this.eh = eh;
    }

    public String getTargetLocale()
    {
        return targetLocale;
    }

    public void setTargetLocale(String targetLocale)
    {
        this.targetLocale = targetLocale;
    }
}
