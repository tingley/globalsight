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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;

import com.globalsight.everest.persistence.PersistentObject;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.edit.EditUtil;

public class ProjectTmTuTProp extends PersistentObject
{
    private static final long serialVersionUID = -2057388445202548426L;

    static private final Logger s_logger = Logger.getLogger(ProjectTmTuTProp.class);

    public static final String TYPE_ATT_PREFIX = "Att::";

    private ProjectTmTuT tu;

    private String propType;
    private String propValue;

    public ProjectTmTuT getTu()
    {
        return tu;
    }

    public void setTu(ProjectTmTuT tu)
    {
        this.tu = tu;
    }

    public String getPropType()
    {
        return propType;
    }

    public void setPropType(String propType)
    {
        this.propType = propType;
    }

    public String getPropValue()
    {
        return propValue;
    }

    public void setPropValue(String propValue)
    {
        this.propValue = propValue;
    }

    @Override
    public int hashCode()
    {
        long id = getId();
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (id ^ (id >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        long id = getId();

        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final ProjectTmTuTProp other = (ProjectTmTuTProp) obj;
        if (id != other.getId())
            return false;
        if (id == -1)
        {
            String pType = getPropType();
            if (pType == null)
            {
                return false;
            }
            
            return pType.equals(other.getPropType());
        }
        return true;
    }
    
    @Override
    public String toString()
    {
        return propType + " : " + propValue;
    }

    public boolean isSameType(String p_type)
    {
        if (p_type != null && p_type.equals(propType))
        {
            return true;
        }

        return false;
    }

    public boolean isAttribute()
    {
        if (propType != null && propType.startsWith(TYPE_ATT_PREFIX))
        {
            return true;
        }

        return false;
    }

    public String getAttributeName()
    {
        if (isAttribute())
        {
            return propType.substring(TYPE_ATT_PREFIX.length());
        }

        return null;
    }

    public static String getPropValue(Set<ProjectTmTuTProp> props, String p_type)
    {
        if (props != null)
        {
            for (ProjectTmTuTProp p : props)
            {
                if (p.isSameType(p_type))
                {
                    return p.getPropValue();
                }
            }
        }

        return null;
    }

    public static String getAttributeValue(Collection<ProjectTmTuTProp> props, String aName)
    {
        if (props != null)
        {
            for (ProjectTmTuTProp p : props)
            {
                if (p.isAttribute() && p.getAttributeName().equals(aName))
                {
                    return p.getPropValue();
                }
            }
        }

        return null;
    }

    public static ProjectTmTuTProp newProjectTmTuTProp(String typePrefix, String aName, String v)
    {
        ProjectTmTuTProp p = new ProjectTmTuTProp();
        p.setPropType(typePrefix + aName);
        p.setPropValue(v);

        return p;
    }

    public static List<ProjectTmTuTProp> getTuProps(long tuId)
    {
        List<ProjectTmTuTProp> result = new ArrayList<ProjectTmTuTProp>();

        String sql = "select * from project_tm_tu_t_prop where tu_id = " + tuId;

        return HibernateUtil.searchWithSql(ProjectTmTuTProp.class, sql);
    }

    public String convertToTmx()
    {
        StringBuffer result = new StringBuffer();

        result.append("<prop type=\"");
        result.append(EditUtil.encodeXmlEntities(propType));
        result.append("\">");
        result.append(EditUtil.encodeXmlEntities(propValue));
        result.append("</prop>\r\n");

        return result.toString();
    }

    public static void removeTuProps(long tuId) throws HibernateException, SQLException
    {
        String sql = "DELETE from project_tm_tu_t_prop where tu_id = " + tuId;

        HibernateUtil.executeSql(sql);
    }

    public static void removeTuProps(Connection connection, long tuId) throws SQLException
    {
        Statement sta = null;
        try
        {
            String sql = "DELETE from project_tm_tu_t_prop where tu_id = " + tuId;

            sta = connection.createStatement();
            sta.executeUpdate(sql);
        }
        finally
        {
            if (sta != null)
                sta.close();
        }
    }

    public static void saveTuProp(Connection connection, ProjectTmTuTProp prop, long tuId)
            throws SQLException
    {
        PreparedStatement psta = null;
        try
        {
            String sql = "INSERT INTO `project_tm_tu_t_prop` (`TU_ID`, `PROP_TYPE`, `PROP_VALUE`) VALUES (?, ?, ?)";

            psta = connection.prepareStatement(sql);
            psta.setLong(1, tuId);
            psta.setString(2, prop.getPropType());
            psta.setString(3, prop.getPropValue());
            psta.executeUpdate();
        }
        finally
        {
            if (psta != null)
                psta.close();
        }
    }
    
    public static void updateTuProp(Connection connection, ProjectTmTuTProp prop, long tuId)
            throws SQLException
    {
        PreparedStatement psta = null;
        try
        {
            String sql = "UPDATE project_tm_tu_t_prop SET PROP_VALUE = ? WHERE TU_ID= ? AND PROP_TYPE=?";

            psta = connection.prepareStatement(sql);
            psta.setString(1, prop.getPropValue());
            psta.setLong(2, tuId);
            psta.setString(3, prop.getPropType());
            psta.executeUpdate();
        }
        finally
        {
            if (psta != null)
                psta.close();
        }
    }
    
    public static void removeTuProps(Connection connection, long tuId, String propType) throws SQLException
    {
        PreparedStatement psta = null;
        try
        {
            String sql = "DELETE from project_tm_tu_t_prop where tu_id = ? and PROP_TYPE = ?";

            psta = connection.prepareStatement(sql);
            psta.setLong(1, tuId);
            psta.setString(2, propType);
            psta.executeQuery();
        }
        finally
        {
            if (psta != null)
                psta.close();
        }
    }
    
    public static boolean doesPropsExistTu(Connection connection, long tuId, String propType) throws SQLException
    {
        PreparedStatement psta = null;
        try
        {
            String sql = "select * from project_tm_tu_t_prop where tu_id = ? and PROP_TYPE = ?";

            psta = connection.prepareStatement(sql);
            psta.setLong(1, tuId);
            psta.setString(2, propType);
            ResultSet rs = psta.executeQuery();
            
            return rs == null ? false : rs.next(); 
        }
        finally
        {
            if (psta != null)
                psta.close();
        }
    }
}
