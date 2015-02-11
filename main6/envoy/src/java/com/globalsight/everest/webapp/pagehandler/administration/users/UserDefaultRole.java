package com.globalsight.everest.webapp.pagehandler.administration.users;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.globalsight.everest.persistence.PersistentObject;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.util.GlobalSightLocale;

public class UserDefaultRole extends PersistentObject
{
    private static final long serialVersionUID = -5219287185562542048L;

	public static final String ADD = "A";
	public static final String EDIT = "E";

	private long sourceLocaleId = 0l;
	private long targetLocaleId = 0l;
	private GlobalSightLocale sourceLocaleObject = null;
	private GlobalSightLocale targetLocaleObject = null;
	private String userId = "";
	private Set<UserDefaultActivity> activities = null;
	private String status = "";
	
    public long getSourceLocaleId()
    {
        return sourceLocaleId;
    }

    public void setSourceLocaleId(long sourceLocaleId)
    {
        this.sourceLocaleId = sourceLocaleId;
    }

    public long getTargetLocaleId()
    {
        return targetLocaleId;
    }

    public void setTargetLocaleId(long targetLocaleId)
    {
        this.targetLocaleId = targetLocaleId;
    }

    public GlobalSightLocale getSourceLocaleObject()
    {
        try
        {
            if (sourceLocaleObject == null)
                this.sourceLocaleObject = ServerProxy.getLocaleManager()
                        .getLocaleById(sourceLocaleId);
        }
        catch (Exception e)
        {
            this.sourceLocaleObject = new GlobalSightLocale();
        }
        return this.sourceLocaleObject;
    }

    public void setSourceLocaleObject(GlobalSightLocale sourceLocaleObject)
    {
        this.sourceLocaleObject = sourceLocaleObject;
    }

    public GlobalSightLocale getTargetLocaleObject()
    {
        try
        {
            if (targetLocaleObject == null)
                this.targetLocaleObject = ServerProxy.getLocaleManager()
                        .getLocaleById(targetLocaleId);
        }
        catch (Exception e)
        {
            this.targetLocaleObject = new GlobalSightLocale();
        }
        return targetLocaleObject;
    }

    public void setTargetLocaleObject(GlobalSightLocale targetLocaleObject)
    {
        this.targetLocaleObject = targetLocaleObject;
    }

    public Set<UserDefaultActivity> getActivities()
    {
        return activities;
    }

    public void setActivities(Set<UserDefaultActivity> activities)
    {
        this.activities = activities;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getUserId()
    {
        return userId;
    }

    public void setUserId(String userId)
    {
        this.userId = userId;
    }

    public void removeActivity(UserDefaultActivity act)
    {
        this.getActivities().remove(act);
    }

    public UserDefaultRole clone()
    {
        UserDefaultRole role = new UserDefaultRole();
        role.setUserId(this.userId);
        role.setSourceLocaleId(this.sourceLocaleId);
        role.setTargetLocaleId(this.targetLocaleId);
        role.setSourceLocaleObject(this.sourceLocaleObject);
        role.setTargetLocaleObject(this.targetLocaleObject);
        role.setStatus(UserDefaultRole.ADD);

        Set<UserDefaultActivity> activities = new HashSet<UserDefaultActivity>();
        for (Iterator<UserDefaultActivity> it = this.activities.iterator(); it
                .hasNext();)
        {
            UserDefaultActivity act = (UserDefaultActivity) it.next();
            UserDefaultActivity ac = new UserDefaultActivity();
            // In "user_default_activities" table, it does not care
            // company id. So if an activity does not exist in
            // system, it does not matter, no need to check here.
            ac.setActivityName(act.getActivityName());
            ac.setDefaultRole(role);
            activities.add(ac);
        }
        role.setActivities(activities);

        return role;
    }
}
