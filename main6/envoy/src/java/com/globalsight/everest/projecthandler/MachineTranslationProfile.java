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
package com.globalsight.everest.projecthandler;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * MachineTranslationProfile generated by hbm2java
 */
public class MachineTranslationProfile implements java.io.Serializable
{
    private static final long serialVersionUID = 3321290565544942964L;

    private long id;
    private Timestamp timestamp;
    private String mtProfileName;
    private String mtEngine;
    private String description;
    private String url;
    private Integer port;
    private String username;
    private String password;
    private String category;
    private String accountinfo;
    private Long companyid;
    private Set<MachineTranslationExtentInfo> exInfo = new HashSet<MachineTranslationExtentInfo>();
    private String jsonInfo;
    private long mtThreshold;
    private boolean includeMTIdentifiers = false;
    private String mtIdentifierLeading;
    private String mtIdentifierTrailing;
    private boolean logDebugInfo;
    private boolean ignoreTMMatch;
    private long msMaxLength;
    private String msTransType;

    public String getMsTransType()
    {
        return msTransType;
    }

    public void setMsTransType(String msTransType)
    {
        this.msTransType = msTransType;
    }

    public long getMsMaxLength()
    {
        return msMaxLength;
    }

    public void setMsMaxLength(long msMaxLength)
    {
        this.msMaxLength = msMaxLength;
    }

    public boolean isIgnoreTMMatch()
    {
        return ignoreTMMatch;
    }

    public void setIgnoreTMMatch(boolean ignoreTMMatch)
    {
        this.ignoreTMMatch = ignoreTMMatch;
    }

    public boolean isLogDebugInfo()
    {
        return logDebugInfo;
    }

    public void setLogDebugInfo(boolean logDebugInfo)
    {
        this.logDebugInfo = logDebugInfo;
    }

    public String getJsonInfo()
    {
        return jsonInfo;
    }

    public void setJsonInfo(String jsonInfo)
    {
        this.jsonInfo = jsonInfo;
    }

    public boolean isIncludeMTIdentifiers()
    {
        return includeMTIdentifiers;
    }

    public void setIncludeMTIdentifiers(boolean includeMTIdentifiers)
    {
        this.includeMTIdentifiers = includeMTIdentifiers;
    }

    public boolean isActive()
    {
        return active;
    }

    public void setActive(boolean active)
    {
        this.active = active;
    }

    private boolean active;

    public long getMtThreshold()
    {
        return mtThreshold;
    }

    public void setMtThreshold(long mtThreshold)
    {
        this.mtThreshold = mtThreshold;
    }

    public String getExInfoVal()
    {
        if (this.getExInfo() == null || this.getExInfo().size() == 0)
        {
            return "";
        }
        Iterator i = this.getExInfo().iterator();
        StringBuffer node = new StringBuffer();
        while (i.hasNext())
        {
            MachineTranslationExtentInfo mInfo = (MachineTranslationExtentInfo) i
                    .next();
            node.append(mInfo.getSelfInfo()).append(",");
        }
        return node.toString();
    }

    private HashMap paramHM;

    public HashMap getParamHM()
    {
        paramHM = new HashMap();
        EngineEnum ee = EngineEnum.getEngine(getMtEngine());
        String[] s = ee.getInfo();
        String[] me = this.toArray();
        for (int i = 0; i < s.length; i++)
        {
            paramHM.put(s[i], me[i]);
        }
        return paramHM;
    }

    public String[] toArray()
    {
        return new String[]
        { String.valueOf(getId()), getUrl(), getPort().toString(),
                getUsername(), getPassword(), getCategory(), getAccountinfo() };
    }

    public MachineTranslationProfile()
    {
        this.mtProfileName = "";
        this.mtEngine = "MS_Translator";
        this.description = "";
        this.url = "";
        this.username = "";
        this.category = "";
        this.accountinfo = "";
        this.mtThreshold = 100;
    }

    public MachineTranslationProfile(long id)
    {
        this.id = id;
    }

    public MachineTranslationProfile(long id, String mtProfileName,
            String mtEngin, String description, long mtThreshold,
            String url, Integer port, String username, String password,
            String category, String accountinfo, Long companyid)
    {
        this.id = id;
        this.mtProfileName = mtProfileName;
        this.mtEngine = mtEngin;
        this.description = description;
        this.mtThreshold = mtThreshold;
        this.url = url;
        this.port = port;
        this.username = username;
        this.password = password;
        this.category = category;
        this.accountinfo = accountinfo;
        this.companyid = companyid;
    }

    public long getId()
    {
        return this.id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public Timestamp getTimestamp()
    {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp)
    {
        this.timestamp = timestamp;
    }

    public String getMtProfileName()
    {
        return this.mtProfileName;
    }

    public void setMtProfileName(String mtProfileName)
    {
        this.mtProfileName = mtProfileName;
    }

    public String getMtEngine()
    {
        return this.mtEngine;
    }

    public void setMtEngine(String mtEngine)
    {
        this.mtEngine = mtEngine;
    }

    public String getDescription()
    {
        return this.description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getUrl()
    {
        return this.url;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    public Integer getPort()
    {
        if (null == port)
        {
            port = new Integer(80);
        }
        return this.port;
    }

    public void setPort(Integer port)
    {
        this.port = port;
    }

    public String getUsername()
    {
        return this.username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getPassword()
    {
        return this.password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public String getCategory()
    {
        return this.category;
    }

    public void setCategory(String category)
    {
        this.category = category;
    }

    public String getAccountinfo()
    {
        return this.accountinfo;
    }

    public void setAccountinfo(String accountinfo)
    {
        this.accountinfo = accountinfo;
    }

    public String getMtIdentifierLeading()
    {
        return this.mtIdentifierLeading;
    }

    public void setMtIdentifierLeading(String mtIdentifierLeading)
    {
        this.mtIdentifierLeading = mtIdentifierLeading;
    }

    public String getMtIdentifierTrailing()
    {
        return this.mtIdentifierTrailing;
    }

    public void setMtIdentifierTrailing(String mtIdentifierTrailing)
    {
        this.mtIdentifierTrailing = mtIdentifierTrailing;
    }

    public Long getCompanyid()
    {
        return this.companyid;
    }

    public void setCompanyid(Long companyid)
    {
        this.companyid = companyid;
    }

    public Set<MachineTranslationExtentInfo> getExInfo()
    {
        return exInfo;
    }

    public void setExInfo(Set<MachineTranslationExtentInfo> exInfo)
    {
        this.exInfo = exInfo;
    }

    /**
     * For MS Translator and "sr" locale only
     * 
     * @param p_locale available values "sr_RS" or "sr_YU".
     * 
     * @return "sr-Latn" or "sr-Cyrl".
     */
	public String getPreferedLangForSr(String p_locale)
	{
		if (!"MS_Translator".equalsIgnoreCase(this.mtEngine))
			return null;

		if (p_locale == null || !p_locale.toLowerCase().startsWith("sr_"))
			return null;

		// default
		if (StringUtils.isEmpty(jsonInfo))
			return "rs-Latn";

		try
		{
			JSONArray arr = new JSONArray(jsonInfo);
			for (int i = 0; i < arr.length(); i++)
			{
				JSONObject obj = (JSONObject) arr.get(i);
				String key = (String) obj.keys().next();
				if (p_locale.equalsIgnoreCase(key))
				{
					return obj.getString(key);
				}
			}
		}
		catch (JSONException e)
		{
			return "rs-Latn";
		}

		return "rs-Latn";
	}
}
