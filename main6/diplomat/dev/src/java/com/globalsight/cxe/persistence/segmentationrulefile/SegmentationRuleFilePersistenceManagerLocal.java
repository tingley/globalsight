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
package com.globalsight.cxe.persistence.segmentationrulefile;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.globalsight.cxe.entity.segmentationrulefile.SegmentationRuleFile;
import com.globalsight.cxe.entity.segmentationrulefile.SegmentationRuleFileImpl;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.ling.tm2.persistence.DbUtil;
import com.globalsight.persistence.hibernate.HibernateUtil;

/**
 * Implements the service interface for performing CRUD operations for
 * SegmentationRuleFiles *
 */
public class SegmentationRuleFilePersistenceManagerLocal implements
        SegmentationRuleFilePersistenceManager
{
    static private final Logger s_logger = Logger
            .getLogger(SegmentationRuleFilePersistenceManagerLocal.class);

    /**
     * Creates a new SegmentationRuleFile object in the data store
     * 
     * @return the newly created object
     */
    public SegmentationRuleFile createSegmentationRuleFile(
            SegmentationRuleFile p_segmentationRuleFile)
            throws SegmentationRuleFileEntityException, RemoteException
    {
        try
        {
            if (p_segmentationRuleFile.getCompanyId() < 1)
            {
                p_segmentationRuleFile.setCompanyId(Long.parseLong(
                        CompanyThreadLocal.getInstance().getValue()));
            }

            HibernateUtil.save(p_segmentationRuleFile);
            return p_segmentationRuleFile;
        }
        catch (Exception e)
        {
            throw new SegmentationRuleFileEntityException(e);
        }
    }

    /**
     * Reads the SegmentationRuleFile object from the datastore
     * 
     * @return SegmentationRuleFile with the given id
     */
    public SegmentationRuleFile readSegmentationRuleFile(long p_id)
            throws SegmentationRuleFileEntityException, RemoteException
    {
        try
        {
            return (SegmentationRuleFileImpl) HibernateUtil.get(
                    SegmentationRuleFileImpl.class, p_id);
        }
        catch (Exception e)
        {
            throw new SegmentationRuleFileEntityException(e);
        }
    }

    /**
     * Deletes an Segmentation Rule File from the datastore
     */
    public void deleteSegmentationRuleFile(
            SegmentationRuleFile p_segmentationRuleFile)
            throws SegmentationRuleFileEntityException, RemoteException
    {
        try
        {
            HibernateUtil
                    .delete((SegmentationRuleFileImpl) p_segmentationRuleFile);
        }
        catch (Exception e)
        {
            throw new SegmentationRuleFileEntityException(e);
        }
    }

    /**
     * Update the SegmentationRuleFile object in the datastore
     * 
     * @return the updated SegmentationRuleFile
     */
    public SegmentationRuleFile updateSegmentationRuleFile(
            SegmentationRuleFile p_segmentationRuleFile)
            throws SegmentationRuleFileEntityException, RemoteException
    {
        try
        {
            HibernateUtil
                    .update((SegmentationRuleFileImpl) p_segmentationRuleFile);
        }
        catch (Exception e)
        {
            throw new SegmentationRuleFileEntityException(e);
        }
        return p_segmentationRuleFile;
    }

    /**
     * Get a list of all existing SegmentationRuleFile objects in the datastore;
     * make them editable.
     * 
     * @return a vector of the SegmentationRuleFile objects
     */
    public Collection getAllSegmentationRuleFiles()
            throws SegmentationRuleFileEntityException, RemoteException
    {
        try
        {
            String hql = "from SegmentationRuleFileImpl s where s.isActive = 'Y'";
            HashMap<String, Long> map = null;
            String currentId = CompanyThreadLocal.getInstance().getValue();
            if (!CompanyWrapper.SUPER_COMPANY_ID.equals(currentId))
            {
                hql += " and s.companyId = :companyId";
                map = new HashMap<String, Long>();
                map.put("companyId", Long.parseLong(currentId));
            }

            return HibernateUtil.search(hql, map);
        }
        catch (Exception e)
        {
            throw new SegmentationRuleFileEntityException(e);
        }
    }

    public SegmentationRuleFile getDefaultSegmentationRuleFile()
            throws SegmentationRuleFileEntityException, RemoteException
    {
        return readSegmentationRuleFile(1);
    }

    public SegmentationRuleFile getSegmentationRuleFileByTmpid(String p_tmpid)
            throws SegmentationRuleFileEntityException, RemoteException
    {
        String ruleid = getSegmentationRuleFileIdByTmpid(p_tmpid);
        if (ruleid == null)
            return null;
        else
            return readSegmentationRuleFile(Long.parseLong(ruleid));
    }

    /**
     * * Get a SegmentationRuleFile objects relate with special TM profile in
     * the datastore *
     * 
     * @return the id of related SegmentationRuleFile objects, null if not found
     */
    public String getSegmentationRuleFileIdByTmpid(String p_tmpid)
            throws SegmentationRuleFileEntityException
    {
        String sql = "SELECT SEGMENTATION_RULE_ID "
                + "FROM SEGMENTATION_RULE_TM_PROFILE "
                + "WHERE TM_PROFILE_ID = ?";
        String result = null;

        Connection connection = null;
        PreparedStatement stat = null;
        ResultSet rs = null;
        try
        {
            connection = DbUtil.getConnection();
            stat = connection.prepareStatement(sql);
            stat.setString(1, p_tmpid);
            rs = stat.executeQuery();
            if (rs.next())
            {
                result = String.valueOf(rs.getLong(1));
            }
        }
        catch (Exception e)
        {
            s_logger.error(e.getMessage(), e);
            throw new SegmentationRuleFileEntityException(e);
        }
        finally
        {
            DbUtil.silentClose(rs);
            DbUtil.silentClose(stat);
            DbUtil.silentReturnConnection(connection);
        }

        return result;
    }
    
    public String searchWithSql(String sql, List<?> params)
            throws SQLException {
        Connection connection = null;
        PreparedStatement stat = null;
        boolean autoCommit = true;
        ResultSet rs = null;
        String rtl = null;
        try {
            connection = DbUtil.getConnection();
            autoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            stat = connection.prepareStatement(sql);
            if (params != null) {
                for (int i = 0; i < params.size(); i++) {
                    stat.setObject(i + 1, params.get(i));
                }
            }

            rs = stat.executeQuery();

            while (rs.next()) {
                if (rs.getLong(1) == 0l)
                    return null;
                else
                    rtl = String.valueOf(rs.getLong(1));

            }

        } catch (Exception e1) {
            s_logger.error(e1.getMessage(), e1);
            connection.rollback();
        } finally {
            if (stat != null) {
                stat.close();
            }

            if (connection != null) {
                try {
                    connection.setAutoCommit(autoCommit);
                    DbUtil.returnConnection(connection);
                } catch (Exception e) {
                    s_logger.error(e.getMessage(), e);
                }
            }
        }
        return rtl;
    }

    public void createRelationshipWithTmp(String p_ruleid, String p_tmpid)
            throws SegmentationRuleFileEntityException, RemoteException
    {
        long id = Long.parseLong(p_ruleid);

        String oldRuleId = getSegmentationRuleFileIdByTmpid(p_tmpid);

        // id == -2, use default rule, delete the old relationship
        if (id != -2)
        {
            if (oldRuleId == null)
            {
                createRelationship(p_ruleid, p_tmpid);
            }
            else if (!oldRuleId.equals(p_ruleid))
            {
                updateRelationship(p_ruleid, p_tmpid);
            }
        }
        else if (oldRuleId != null)
        {
            deleteRelationshipWithTmp(oldRuleId, p_tmpid);
        }
    }

    public void deleteRelationshipWithTmp(String p_ruleid, String p_tmpid)
            throws SegmentationRuleFileEntityException, RemoteException
    {
        String sql = "DELETE FROM SEGMENTATION_RULE_TM_PROFILE " + "WHERE "
                + "SEGMENTATION_RULE_ID = ? AND TM_PROFILE_ID = ?";

        List<Object> params = new ArrayList<Object>();
        params.add(p_ruleid);
        params.add(p_tmpid);

        try
        {
            HibernateUtil.executeSql(sql, params);
        }
        catch (Exception e)
        {
            throw new SegmentationRuleFileEntityException(e);
        }
    }

    public String[] getTmpIdsBySegmentationRuleId(String p_ruleid)
            throws SegmentationRuleFileEntityException, RemoteException
    {
        StringBuilder tmpids = new StringBuilder();

        String sql = "SELECT TM_PROFILE_ID "
                + "FROM SEGMENTATION_RULE_TM_PROFILE "
                + "WHERE SEGMENTATION_RULE_ID = :ruleid";

        try
        {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("ruleid", p_ruleid);
            List<?> list = HibernateUtil.searchWithSql(sql, params);

            for (Object obj : list)
            {
                tmpids.append(obj.toString()).append(",");
            }
        }
        catch (Exception e)
        {
            throw new SegmentationRuleFileEntityException(e);
        }

        return tmpids.length() == 0 ? null : tmpids.toString().split(",");
    }

    // private method
    private void createRelationship(String p_ruleid, String p_tmpid)
            throws SegmentationRuleFileEntityException
    {
        String sql = "INSERT INTO SEGMENTATION_RULE_TM_PROFILE "
                + "(SEGMENTATION_RULE_ID, TM_PROFILE_ID) " + "VALUES (?, ?)";

        List<Object> params = new ArrayList<Object>();
        params.add(p_ruleid);
        params.add(p_tmpid);
        try
        {
            HibernateUtil.executeSql(sql, params);
        }
        catch (Exception e)
        {
            throw new SegmentationRuleFileEntityException(e);
        }
    }

    private void updateRelationship(String p_ruleid, String p_tmpid)
            throws SegmentationRuleFileEntityException
    {
        String sql = "UPDATE SEGMENTATION_RULE_TM_PROFILE SET "
                + "SEGMENTATION_RULE_ID = ? " + "WHERE TM_PROFILE_ID = ?";

        List<Object> params = new ArrayList<Object>();
        params.add(p_ruleid);
        params.add(p_tmpid);
        try
        {
            HibernateUtil.executeSql(sql, params);
        }
        catch (Exception e)
        {
            throw new SegmentationRuleFileEntityException(e);
        }
    }
}
