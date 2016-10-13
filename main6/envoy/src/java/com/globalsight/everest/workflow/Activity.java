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
package com.globalsight.everest.workflow;

import com.globalsight.cxe.util.XmlUtil;
import com.globalsight.everest.persistence.PersistentObject;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.taskmanager.TaskImpl;

import net.sf.json.JSONArray;
// GlobalSight
import net.sf.json.JSONObject;

/**
 * This class only represent a wrapper object for the activity names defined in
 * Envoy database and is used for defining a workflow template (template node
 * names).
 * 
 */
public class Activity extends PersistentObject
{
    private static final long serialVersionUID = -1196302548560639848L;

    // the different types of activities
    public static final int TYPE_TRANSLATE = 1;
    public static final int TYPE_REVIEW = 2;
    public static final int TYPE_REVIEW_EDITABLE = 3;

    public static final String TYPE_TRANSLATE_STR = "TRANSLATE";
    public static final String TYPE_REVIEW_STR = "REVIEW";

    public static final String USE_TYPE_TRANS = "TRANS";
    public static final String USE_TYPE_DTP = "DTP";

    // activity description
    private String m_activityDescription;
    // specifies the type of activity
    private int m_type = TYPE_TRANSLATE;
    private String m_type_str = "TRANSLATE"; // Use by hibernate
    private String m_useType = USE_TYPE_TRANS;
    // id of the company which this activity belong to
    private long m_companyId;
    private String m_displayName;

    // for MaCfee "Trisoft (LiveContent Architect) DITA Checks"
    private boolean m_runDitaQAChecks = false;

    // For SLA report issue
    // If activity type is translate, is_Editable will be true as default.
    // If activity type is review, is_Editable will be false as default.
    private boolean m_isEditable = true;

    public boolean useActive = true;

    private boolean m_qaChecks = false;

    private boolean autoCompleteActivity = false;
    private String afterJobCreation;
    private String afterJobDispatch;
    private String afterActivityStart;
    private String completeSchedule;

    private int completeType = -1;

    public static int COMPLETE_TYPE_AFTER_JOB_CREATION = 1;
    public static int COMPLETE_TYPE_AFTER_JOB_DISPATCH = 2;
    public static int COMPLETE_TYPE_AFTER_ACTIVITY_START = 3;
    public static int COMPLETE_TYPE_SCHEDULE = 4;
    public static int COMPLETE_TYPE_DAILY = 5;
    public static int COMPLETE_TYPE_WEEKLY = 6;
    public static int COMPLETE_TYPE_MONTHLY = 7;
    public static int COMPLETE_TYPE_ONE_TIME = 8;

    // ////////////////////////////////////////////////////////////////////////////////
    // Begin: Constructor
    // ////////////////////////////////////////////////////////////////////////////////
    /**
     * Default Activity constructor used ONLY for TopLink.
     */
    public Activity()
    {
        super();
    }

    // tbd - remove
    public Activity(String p_activityName)
    {
        super();
        setName(p_activityName.length() > 30 ? p_activityName.substring(0, 30) : p_activityName);
    }

    /**
     * Activity constructor used for creating a new activity.
     * 
     * @param p_activityName
     *            - The activity name.
     * @param p_activityDescription
     *            - The description of the activity.
     */
    public Activity(String p_activityName, String p_activityDescription, int p_type,
            String p_companyId)
    {
        super();
        setName(p_activityName.length() > 30 ? p_activityName.substring(0, 30) : p_activityName);
        m_activityDescription = p_activityDescription;
        m_type = p_type;
        setCompanyId(Long.parseLong(p_companyId));
    }

    // ////////////////////////////////////////////////////////////////////////////////
    // End: Constructor
    // ////////////////////////////////////////////////////////////////////////////////

    // ////////////////////////////////////////////////////////////////////////////////
    // Begin: Local Methods
    // ////////////////////////////////////////////////////////////////////////////////
    /**
     * Get the activity name.
     * 
     * @return The activity name.
     */
    public String getActivityName()
    {
        return getName();
    }

    /**
     * Get the display name for this activity.
     * 
     * @return The display name.
     */
    public String getDisplayName()
    {
        return m_displayName;
    }

    /**
     * Set the display name for this activity.
     * 
     * @param p_displayName
     *            The display name to be set
     */
    public void setDisplayName(String p_displayName)
    {
        m_displayName = p_displayName;
    }

    /**
     * Get name of the company this activity belong to.
     * 
     * @return The company name.
     */
    public long getCompanyId()
    {
        return this.m_companyId;
    }

    /**
     * Get name of the company this activity belong to.
     * 
     * @return The company name.
     */
    public void setCompanyId(long p_companyId)
    {
        this.m_companyId = p_companyId;
    }

    /**
     * @deprecated Use getDescription().
     */
    public String getActivityDescription()
    {
        return getDescription();
    }

    /**
     * Get the activity description.
     * 
     * @return The activity description.
     */
    public String getDescription()
    {
        return m_activityDescription;
    }

    /**
     * Update the activity's description;
     */
    public void setDescription(String p_newDescription)
    {
        m_activityDescription = p_newDescription;
    }

    /**
     * Returns 'true' if the activity is of the type specified. 'false' if the
     * activity is not of the type.
     */
    public boolean isType(int p_type)
    {
        return m_type == p_type;
    }

    /**
     * Return the activity's type
     */
    public int getType()
    {
        return m_type;
    }

    /**
     * Set the activity's type
     */
    public void setType(int p_type)
    {
        if (isValidType(p_type))
        {
            m_type = p_type;
            m_type_str = typeAsString(p_type); // For hibernate
        }
    }

    public void setIsEditable(boolean isEditable)
    {
        m_isEditable = isEditable;
    }

    public boolean getIsEditable()
    {
        return m_isEditable;
    }

    public void setQaChecks(boolean p_qaChecks)
    {
        m_qaChecks = p_qaChecks;
    }

    public boolean getQaChecks()
    {
        return m_qaChecks;
    }

    public boolean getAutoCompleteActivity()
    {
        return autoCompleteActivity;
    }

    public void setAutoCompleteActivity(boolean autoCompleteActivity)
    {
        this.autoCompleteActivity = autoCompleteActivity;
    }

    public String getAfterJobCreation()
    {
        return afterJobCreation;
    }

    public void setAfterJobCreation(String afterJobCreation)
    {
        this.afterJobCreation = afterJobCreation;
    }

    public String getAfterJobDispatch()
    {
        return afterJobDispatch;
    }

    public void setAfterJobDispatch(String afterJobDispatch)
    {
        this.afterJobDispatch = afterJobDispatch;
    }

    public String getAfterActivityStart()
    {
        return afterActivityStart;
    }

    public void setAfterActivityStart(String afterActivityStart)
    {
        this.afterActivityStart = afterActivityStart;
    }

    // ////////////////////////////////////////////////////////////////////////////////
    // End: Local Methods
    // ////////////////////////////////////////////////////////////////////////////////
    /**
     * Returns a string representation of the object (based on the object name).
     */
    public String toString()
    {
        return getName();
    }

    /**
     * Returns 'true' if the ids are the same - this denotes the same activity
     * object. If they aren't the same then it returns 'false'.
     */
    public boolean equals(Object p_activity)
    {
        if (p_activity instanceof Activity)
        {
            return (getId() == ((Activity) p_activity).getId());
        }
        return false;
    }

    /**
     * Return a string representation of the object appropriate for debugging.
     * 
     * @return a string representation of the object appropriate for debugging.
     */
    public String toDebugString()
    {
        StringBuilder buff = new StringBuilder();
        buff.append(super.toString());
        buff.append(", m_activityDescription=");
        buff.append(m_activityDescription != null ? m_activityDescription : "null");
        buff.append(", m_type=");
        buff.append(typeAsString(m_type));
        buff.append(", m_userType=");
        buff.append(getUseType());
        return buff.toString();
    }

    /**
     * Checks if the type is a valid type. Returns 'true' if it is, 'false' if
     * not.
     */
    static public boolean isValidType(int p_type)
    {
        if (p_type != TYPE_TRANSLATE && p_type != TYPE_REVIEW)
        {
            return false;
        }

        return true;
    }

    public static int typeAsInt(String p_type)
    {
        int type = TYPE_TRANSLATE;
        if (TYPE_REVIEW_STR.equalsIgnoreCase(p_type))
        {
            type = TYPE_REVIEW;
        }

        return type;
    }

    public static String typeAsString(int p_type)
    {
        String typeString = TYPE_TRANSLATE_STR;
        if (p_type == TYPE_REVIEW)
        {
            typeString = TYPE_REVIEW_STR;
        }

        return typeString;
    }

    public String getUseType()
    {
        return m_useType;
    }

    public void setUseType(String p_useType)
    {
        this.m_useType = p_useType;
    }

    public String getType_str()
    {
        return m_type_str;
    }

    public void setType_str(String p_type)
    {
        if (p_type != null)
        {
            m_type = typeAsInt(p_type);
            m_type_str = p_type;
        }
    }

    public static boolean isTranslateActivity(int type)
    {
        return type == Task.TYPE_TRANSLATE;
    }

    public int getActivityType()
    {
        // default
        int type = TaskImpl.TYPE_TRANSLATE;
        try
        {
            type = getType();

            // for sla report issue
            if ((type == Activity.TYPE_REVIEW) && getIsEditable())
            {
                type = TaskImpl.TYPE_REVIEW_EDITABLE;
            }
        }
        catch (Exception e)
        {
            // do nothing just return the default
        }
        return type;
    }

    public boolean getRunDitaQAChecks()
    {
        return m_runDitaQAChecks;
    }

    public void setRunDitaQAChecks(boolean p_runDitaQAChecks)
    {
        this.m_runDitaQAChecks = p_runDitaQAChecks;
    }

    public int getCompleteType()
    {
        // have not upgrade "complete_type" column
        if (completeType == -1)
        {
            // Not auto complete activity, set to 0 to indicate it has been upgraded.
            if (!getAutoCompleteActivity())
            {
                completeType = 0;
            }
            else if (afterJobCreation != "" && afterJobCreation != null)
            {
                completeType = COMPLETE_TYPE_AFTER_JOB_CREATION;
            }
            else if (afterJobDispatch != "" && afterJobDispatch != null)
            {
                completeType = COMPLETE_TYPE_AFTER_JOB_DISPATCH;
            }
            else if (afterActivityStart != "" && afterActivityStart != null)
            {
                completeType = COMPLETE_TYPE_AFTER_ACTIVITY_START;
            }
            else if (completeSchedule != null && completeSchedule != "")
            {
                completeType = COMPLETE_TYPE_SCHEDULE;
            }
        }
        return completeType;
    }

    public void setCompleteType(int completeType)
    {
        this.completeType = completeType;
    }

    public void setCompleteTime(String value, String prefixName, JSONObject js)
    {
        if (value == null || value.length() == 0)
            return;

        String[] strArr = value.split("-");

        js.put(prefixName + "D", strArr[0]);
        js.put(prefixName + "H", strArr[1]);
        js.put(prefixName + "M", strArr[2]);
    }

    private void addEmptyList(JSONObject json, String name)
    {
        if (json.get(name) == null)
        {
            json.put(name, new JSONArray());
        }
    }
    
    private void addEmpltyValue(JSONObject json, String name, Object value)
    {
        if (json.get(name) == null)
        {
            json.put(name, value);
        }
    }
    
    private void setCompleteSchedule(JSONObject js)
    {
        addEmptyList(js, "weeklyWeeks");
        addEmpltyValue(js, "dailyRecur", 1);
        addEmpltyValue(js, "weeklyRecur", 1);
        addEmpltyValue(js, "startH", 0);
        addEmpltyValue(js, "startM", 0);
        
        if (this.getCompleteType() != COMPLETE_TYPE_SCHEDULE)
            return;
        
        JSONObject json = (JSONObject) XmlUtil.xml2Json(getCompleteSchedule());
        
        Object ob = json.get("weeklyWeeks");
        if (!(ob instanceof JSONArray))
        {
            JSONArray weeks = new JSONArray();
            weeks.add(ob);
            json.put("weeklyWeeks", weeks);
        }
        js.putAll(json);
    }

    public String toJson()
    {
        JSONObject js = new JSONObject();
        js.put("name", getDisplayName());
        js.put("desc", getDescription());
        js.put("type", getActivityType());
        js.put("autoComplete", getAutoCompleteActivity());
        js.put("completeType", getCompleteType());

        setCompleteTime(afterJobCreation, "afterJobCreation", js);
        setCompleteTime(afterJobDispatch, "afterJobDispatch", js);
        setCompleteTime(afterActivityStart, "afterActivityStart", js);

        setCompleteSchedule(js);

        return js.toString();
    }

    public String getCompleteSchedule()
    {
        return completeSchedule;
    }

    public void setCompleteSchedule(String completeSchedule)
    {
        this.completeSchedule = completeSchedule;
    }
}
