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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.globalsight.cxe.entity.segmentationrulefile.SegmentationRuleFile;
import com.globalsight.cxe.entity.segmentationrulefile.SegmentationRuleFileImpl;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.persistence.hibernate.HibernateUtil;

/**
 * Implements the service interface for performing CRUD operations for
 * SegmentationRuleFiles *
 */
public class SegmentationRuleFilePersistenceManagerLocal implements
        SegmentationRuleFilePersistenceManager
{

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
            if (p_segmentationRuleFile.getCompanyId() == null)
                p_segmentationRuleFile.setCompanyId(CompanyThreadLocal
                        .getInstance().getValue());
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
            HashMap<String, Object> map = null;
            String currentId = CompanyThreadLocal.getInstance().getValue();
            if (!CompanyWrapper.SUPER_COMPANY_ID.equals(currentId))
            {
                hql += " and s.companyId = :companyId";
                map = new HashMap<String, Object>();
                map.put("companyId", currentId);
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
     * @return the id of related SegmentationRuleFile objects, * null if not
     *         found
     */
    public String getSegmentationRuleFileIdByTmpid(String p_tmpid)
            throws SegmentationRuleFileEntityException, RemoteException
    {
        String sql = "SELECT SEGMENTATION_RULE_ID "
                + "FROM SEGMENTATION_RULE_TM_PROFILE "
                + "WHERE TM_PROFILE_ID = :tmpid";
        try
        {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("tmpid", p_tmpid);
            Object obj = HibernateUtil.getFirstWithSql(sql, params);
            return obj == null ? null : obj.toString();
        }
        catch (Exception e)
        {
            throw new SegmentationRuleFileEntityException(e);
        }
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
                + "(SEGMENTATION_RULE_ID, TM_PROFILE_ID) "
                + "VALUES (?, ?)";
        
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
                + "SEGMENTATION_RULE_ID = ? "
                + "WHERE TM_PROFILE_ID = ?";
        
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
