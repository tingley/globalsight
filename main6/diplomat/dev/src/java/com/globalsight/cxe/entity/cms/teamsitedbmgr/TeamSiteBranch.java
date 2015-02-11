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
package com.globalsight.cxe.entity.cms.teamsitedbmgr;

import com.globalsight.everest.persistence.PersistentObject;

/**
 * This class represents TeamSite Branches defined in Envoy database
 *
 * @version     1.0
*/


public class TeamSiteBranch
    extends PersistentObject
{
    // branch source.
    private String m_branchSource = null;
    // branch language
    private int m_branchLanguage = 0;
    // branch target
    private String m_branchTarget = null;
    // teamsite server
    private int m_server = 0;
    // teamsite store
    private int m_store  = 0;


    //////////////////////////////////////////////////////////////////////////////////
    //  Begin:  Constructor
    //////////////////////////////////////////////////////////////////////////////////
    /**
    * Default TeamSiteBranch constructor used ONLY for TopLink.
    */
    public TeamSiteBranch()
    {
    }


    /**
    * TeamSiteBranch constructor used for creating a new branch.
    * @param p_branchSource - The source branch.
    * @param p_branchLanguage - The target language.
    * @param p_branchTarget - The target branch.
    */
    public TeamSiteBranch(String p_branchSource, int p_branchLanguage,
      String p_branchTarget, int p_server, int p_store)
    {
        m_branchSource = p_branchSource;
        m_branchLanguage = p_branchLanguage;
        m_branchTarget = p_branchTarget;
        m_server = p_server;
        m_store = p_store;

    }
    //////////////////////////////////////////////////////////////////////////////////
    //  End:  Constructor
    //////////////////////////////////////////////////////////////////////////////////


    //////////////////////////////////////////////////////////////////////////////////
    //  Begin:  Local Methods
    //////////////////////////////////////////////////////////////////////////////////
    /**
    * Get the branch source.
    * @return The branch source.
    */
    public String getBranchSource()
    {
        return m_branchSource;
    }


    /**
    * Get the branch target.
    * @return The branch target.
    */
    public String getBranchTarget()
    {
        return m_branchTarget;
    }

    /**
    * Returns a string representation of the object (based on the object name).
    */
    public String toString()
    {
        return m_branchSource;
    }

    /**
    * Get the TeamSite Server Id.
    * @return The server id.
    */
    public int getTeamSiteServerId()
    {
        return m_server;
    }

    /**
    * Get the TeamSite Store.
    * @return The store id.
    */
    public int getTeamSiteStoreId()
    {
        return m_store;
    }


	public int getBranchLanguage()
	{
		return m_branchLanguage;
	}


	public void setBranchLanguage(int language)
	{
		m_branchLanguage = language;
	}



	public void setBranchSource(String source)
	{
		m_branchSource = source;
	}

	public void setBranchTarget(String target)
	{
		m_branchTarget = target;
	}


	public int getServer()
	{
		return m_server;
	}


	public void setServer(int m_server)
	{
		this.m_server = m_server;
	}


	public int getStore()
	{
		return m_store;
	}


	public void setStore(int m_store)
	{
		this.m_store = m_store;
	}


  }
