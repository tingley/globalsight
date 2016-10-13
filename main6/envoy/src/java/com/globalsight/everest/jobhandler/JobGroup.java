package com.globalsight.everest.jobhandler;

import java.util.Date;

import org.apache.log4j.Logger;

import com.globalsight.everest.foundation.User;
import com.globalsight.everest.persistence.PersistentObject;
import com.globalsight.everest.projecthandler.ProjectImpl;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserHandlerHelper;
import com.globalsight.util.GlobalSightLocale;

public class JobGroup extends PersistentObject
{
	static private final Logger c_logger = Logger.getLogger(JobGroup.class);
	/**
	 * TODO
	 */
	private static final long serialVersionUID = 1L;
	private long m_id;
	private String m_name;
	private long m_companyId;
	private ProjectImpl m_project;
	private GlobalSightLocale m_sourceLocale;
	private User m_createUser;
	private String m_createUserId;
	private Date m_createDate;
	private String m_stringColum1;
	private String m_stringColum2;
	private long m_longColum1;
	private long m_longColum2;
	private Date m_dateColum1;
	private Date m_dateColum2;

	public long getId()
	{
		return m_id;
	}

	public void setId(long p_id)
	{
		this.m_id = p_id;
	}

	public String getName()
	{
		return m_name;
	}

	public void setName(String p_name)
	{
		this.m_name = p_name;
	}

	public ProjectImpl getProject()
	{
		return m_project;
	}

	public void setProject(ProjectImpl p_project)
	{
		this.m_project = p_project;
	}

	public GlobalSightLocale getSourceLocale()
	{
		return m_sourceLocale;
	}

	public void setSourceLocale(GlobalSightLocale p_sourceLocale)
	{
		this.m_sourceLocale = p_sourceLocale;
	}

	public User getCreateUser()
	{
		if (m_createUser == null && m_createUserId != null)
		{
			try
			{
				m_createUser = UserHandlerHelper.getUser(m_createUserId);
			}
			catch (Exception e)
			{
				c_logger.error(e.getMessage(), e);
			}
		}
		return m_createUser;
	}
	
    public String getCreateUserId()
    {
        return m_createUserId;
    }

    public void setCreateUserId(String userId)
    {
        m_createUserId = userId;
    }
    
	public long getCompanyId()
	{
		return m_companyId;
	}

	public void setCompanyId(long p_companyId)
	{
		this.m_companyId = p_companyId;
	}

	public Date getCreateDate()
	{
		return m_createDate;
	}

	public void setCreateDate(Date p_createDate)
	{
		this.m_createDate = p_createDate;
	}

	public String getStringColum1()
	{
		return m_stringColum1;
	}

	public void setStringColum1(String p_stringColum1)
	{
		this.m_stringColum1 = p_stringColum1;
	}

	public String getStringColum2()
	{
		return m_stringColum2;
	}

	public void setStringColum2(String p_stringColum2)
	{
		this.m_stringColum2 = p_stringColum2;
	}

	public long getLongColum1()
	{
		return m_longColum1;
	}

	public void setLongColum1(long p_longColum1)
	{
		this.m_longColum1 = p_longColum1;
	}

	public long getLongColum2()
	{
		return m_longColum2;
	}

	public void setLongColum2(long p_longColum2)
	{
		this.m_longColum2 = p_longColum2;
	}

	public Date getDateColum1()
	{
		return m_dateColum1;
	}

	public void setDateColum1(Date p_dateColum1)
	{
		this.m_dateColum1 = p_dateColum1;
	}

	public Date getDateColum2()
	{
		return m_dateColum2;
	}

	public void setDateColum2(Date p_dateColum2)
	{
		this.m_dateColum2 = p_dateColum2;
	}
}