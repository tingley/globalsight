package com.globalsight.everest.webapp.pagehandler.projects.jobvo;

import com.globalsight.everest.foundation.User;
import com.globalsight.everest.permission.Permission;

public abstract class JobVoStateSearcher extends JobVoSearcher {

	abstract String getStateSql();
	
	@Override
	protected String getSpecialFrom() {
		if (!userPerms.getPermissionFor(Permission.JOB_SCOPE_ALL))
		{
			StringBuffer sql = new StringBuffer();
			if (isWorkflowManger())
			{
				sql.append(", L10N_PROFILE_WFTEMPLATE_INFO lw, WORKFLOW_TEMPLATE wt  LEFT OUTER JOIN WF_TEMPLATE_WF_MANAGER wtm ON wt.id = wtm.WORKFLOW_TEMPLATE_ID ");
			}
			
			if (isScopeMyProject())
			{
				sql.append(", PROJECT_USER pu ");
			}
			
			return sql.toString();
		}
		return "";
	}

	@SuppressWarnings("unchecked")
	@Override
	protected String getSpecialWhere() {
		StringBuffer sql = new StringBuffer();
		
        if (!userPerms.getPermissionFor(Permission.JOB_SCOPE_ALL))
        {
        	if (isWorkflowManger())
        	{
        		sql.append(" AND lw.L10N_PROFILE_ID = l.id AND lw.WF_TEMPLATE_ID = wt.id ");
        	}
        	
        	if (isScopeMyProject())
			{
				sql.append(" AND pu.PROJECT_ID = p.PROJECT_SEQ ");
			}
        }
        
        if (!userPerms.getPermissionFor(Permission.JOB_SCOPE_ALL))
        {
        	StringBuffer sql2 = new StringBuffer();
        	
        	if (isProjectManger())
        	{
        		if (sql2.length() > 0)
        		{
        			sql2.append(" or ");
        		}
        		
        		sql2.append(" p.MANAGER_USER_ID = :userId ");
        	}
        	
        	if (isWorkflowManger())
        	{
        		if (sql2.length() > 0)
        		{
        			sql2.append(" or ");
        		}
        		
        		sql2.append(" wtm.WORKFLOW_MANAGER_ID = :userId ");
        	}
        	
        	if (isScopeMyProject())
        	{
        		if (sql2.length() > 0)
        		{
        			sql2.append(" or ");
        		}
        		
        		sql2.append(" pu.USER_ID = :userId ");
        	}
        	
    		User user = (User) sessionMgr.getAttribute(USER);
    		map.put("userId", user.getUserId());
    		
    		sql.append(" AND ( ").append(sql2).append(") ");
        }
        
        sql.append(getStateSql());
        
		return sql.toString();
	}
	
	private boolean isProjectManger()
	{
		return userPerms.getPermissionFor(Permission.PROJECTS_MANAGE);
	}
	
	private boolean isWorkflowManger()
	{
		return userPerms
                .getPermissionFor(Permission.PROJECTS_MANAGE_WORKFLOWS);
	}
	
	private boolean isScopeMyProject()
	{
		return userPerms.getPermissionFor(Permission.JOB_SCOPE_MYPROJECTS);
	}
}
