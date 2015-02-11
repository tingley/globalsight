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
package com.globalsight.everest.foundation;

//import java.security.acl.Group;
import java.util.Vector;
import java.io.Serializable;

public class GroupImpl implements Serializable, Group
    //java.security.acl.Group
{
    private String m_groupName=null;
    private String m_groupDescription=null;
    private String[] m_userIds=null;
    private String[] m_permissionNames=null;

    private String m_groupModuleType = null;

    public GroupImpl()
    {
    }

    public String getGroupName()
    {
        return m_groupName;
    }

    public void setGroupName(String p_groupName)
    {
        m_groupName=p_groupName;
        // set the group module type according to the name

        if (m_groupName.equals(GROUP_VENDOR_ADMIN) ||
            m_groupName.equals(GROUP_VENDOR_MANAGER) ||
            m_groupName.equals(GROUP_VENDOR_VIEWER))
        {
            m_groupModuleType = GROUP_MODULE_VENDOR_MANAGER;
        }
        else
        {
            m_groupModuleType = GROUP_MODULE_GLOBALSIGHT;
        }

    }

    // retrieves the module that the group is part of
    // see GROUP_MODULE_xxxx types
    public String getGroupModuleType()
    {
        return m_groupModuleType;
    }  
       
    public String getGroupDescription()
    {
        return m_groupDescription;
    }
    public void setGroupDescription(String p_groupDescription)
    {
        m_groupDescription=p_groupDescription;
    }

    public String[] getPermissionNames()
    {
        return m_permissionNames;
    }

    public void setPermissionNames(String[] p_permissionNames)
    {
        m_permissionNames=p_permissionNames;
    }

    public String[] getUserIds()
    {
        return m_userIds;
    }

    public void setUserIds(String[] p_userIds)
    {
        m_userIds=p_userIds;
    }

    private String permissionsString()
    {
        String names="";
        if( m_permissionNames!=null)
        {
            for(int i=0; i<m_permissionNames.length; i++)
                names+=m_permissionNames[i]+", ";
        }

        return names;
    }

    private String getUsersString()
    {
        String names="";
        if( m_userIds!=null)
        {
            for(int i=0; i<m_userIds.length; i++)
                names+=m_userIds[i]+", ";
        }

        return names;
    }

    /**
     * Override the toString() method for debugging use
     */
    public String toString()
    {
        String grp_str="";

        grp_str="Name="+ getGroupName()+"; "
                +"Description="+ getGroupDescription()+";\n "
                +"Permissions="+ permissionsString()+"; \n"
                +"Members="+ getUsersString()+"; \n";

        return grp_str;
    }

}