package com.plug.Version_8_3_0;

import java.util.HashMap;
import java.util.Map;

public class Role
{
    private String m_activityName = null;
    private String m_sourceLocale = null;
    private String m_targetLocale = null;
    private String m_cost = null;
    private String m_rate = null;
    private int m_state = User.State.CREATED;
    private String m_roleName = null;
    private String m_userId = null;

    private Map<String, String> m_uniqueMembers = new HashMap<String, String>();

    public Role()
    {
    }

    public Role(String p_roleName)
    {
        m_roleName = p_roleName;
    }

    public String getUserId()
    {
        return m_userId;
    }

    public void setUserId(String p_userId)
    {
        m_userId = p_userId;
    }

    public String getActivityName()
    {
        return m_activityName;
    }

    public void setActivityName(String p_activityName)
    {
        m_activityName = p_activityName;
    }

    public String getSourceLocale()
    {
        return m_sourceLocale;
    }

    public void setSourceLocale(String p_sourceLocale)
    {
        m_sourceLocale = p_sourceLocale;
    }

    public String getTargetLocale()
    {
        return m_targetLocale;
    }

    public void setTargetLocale(String p_targetLocale)
    {
        m_targetLocale = p_targetLocale;
    }

    public int getState()
    {
        return m_state;
    }

    public void setState(int p_state)
    {
        switch (p_state)
        {
            case User.State.CREATED:
            case User.State.ACTIVE:
            case User.State.DEACTIVE:
            case User.State.DELETED:
                m_state = p_state;
            default:
        }
    }

    public String getCost()
    {
        return m_cost;
    }

    public void setCost(String p_cost)
    {
        m_cost = p_cost;
    }

    public String getRate()
    {
        return m_rate;
    }

    public void setRate(String p_rate)
    {
        m_rate = p_rate;
    }

    public String getRoleName()
    {
        return m_roleName;
    }

    public void setRoleName(String p_roleName)
    {
        m_roleName = p_roleName;
    }

    public String getUniqueMember(String p_uid)
    {
        return m_uniqueMembers.get(p_uid);
    }

    public void setUniqueMember(String p_uid, String p_uniqueMember)
    {
        m_uniqueMembers.put(p_uid, p_uniqueMember);
    }

    public Map<String, String> getUniqueMembers()
    {
        return m_uniqueMembers;
    }

    public void removeUniqueMember(String p_uid)
    {
        m_uniqueMembers.remove(p_uid);
    }

    public void addUniqueMember(String p_uid)
    {
        m_uniqueMembers.put(p_uid, UserLdapUtil.getUserDN(p_uid));
    }

    public String toString()
    {
        return m_roleName;
    }
}
