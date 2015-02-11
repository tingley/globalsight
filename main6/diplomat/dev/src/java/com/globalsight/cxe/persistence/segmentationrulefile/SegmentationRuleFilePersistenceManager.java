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

import com.globalsight.cxe.entity.segmentationrulefile.SegmentationRuleFile;

import java.util.Collection;

import java.rmi.RemoteException;

/** A service interface for performing CRUD operations for SegmentationRuleFiles **/
public interface SegmentationRuleFilePersistenceManager
{
    public static final String SERVICE_NAME = "SegmentationRuleFilePersistenceManager";

    /**
    ** Creates a new SegmentationRuleFile object in the data store
    ** @return the created object
    **/
    public SegmentationRuleFile createSegmentationRuleFile(SegmentationRuleFile p_segmentationRuleFile)
    throws SegmentationRuleFileEntityException, RemoteException;

    /**
    ** Reads the SegmentationRuleFile object from the datastore
    ** @return the SegmentationRuleFile
    **/
    public SegmentationRuleFile readSegmentationRuleFile(long p_id)
    throws SegmentationRuleFileEntityException, RemoteException;

    /**
    ** Deletes an Segmentation Rule File from the datastore
    **/
    public void deleteSegmentationRuleFile(SegmentationRuleFile p_segmentationRuleFile)
    throws SegmentationRuleFileEntityException, RemoteException;


    /**
    ** Update the SegmentationRuleFile object in the datastore
    ** @return the updated object
    **/
    public SegmentationRuleFile updateSegmentationRuleFile(SegmentationRuleFile p_segmentationRuleFile)
    throws SegmentationRuleFileEntityException, RemoteException;

    /**
    ** Get a list of all existing SegmentationRuleFile objects in the datastore
    ** @return a vector of the SegmentationRuleFile objects
    **/
    public Collection getAllSegmentationRuleFiles()
    throws SegmentationRuleFileEntityException, RemoteException;
    
    /**
     ** Get the default SegmentationRuleFile objects in GlobalSight
     ** @return the default SegmentationRuleFile objects, 
     **  save in DB by id is 1; 
     **/
    public SegmentationRuleFile getDefaultSegmentationRuleFile()
    throws SegmentationRuleFileEntityException, RemoteException;
    
    /**
     ** Get a SegmentationRuleFile objects relate with special TM Profile in the datastore
     ** @return the related SegmentationRuleFile objects,
     **  null if not found
     **/
    public SegmentationRuleFile getSegmentationRuleFileByTmpid(String p_tmpid)
    throws SegmentationRuleFileEntityException, RemoteException;
    
    /**
     ** Get a SegmentationRuleFile objects relate with special TM Profile in the datastore
     ** @return the id of related SegmentationRuleFile objects,
     **  null if not found
     **/
     public String getSegmentationRuleFileIdByTmpid(String p_tmpid)
     throws SegmentationRuleFileEntityException, RemoteException;
     
     /**
      ** Get array of tm profiles which relate with this SegmentationRuleFile 
      ** @return the ids of tm profile,
      **  null if not found
      **/
      public String[] getTmpIdsBySegmentationRuleId(String p_ruleid)
      throws SegmentationRuleFileEntityException, RemoteException;
     
     /**
      ** Create a relationshop between SegmentationRuleFile objects with TM Profile in the datastore
      ** Up to now, GlobalSight support one tm_profile relate with one rules, 
      ** and one rules can be related to any tm_profiles.
      ** This method will find the last relationship first, delete it if found.
      ** And then insert new relationship into DB
      **/
      public void createRelationshipWithTmp(String p_ruleid, String p_tmpid)
      throws SegmentationRuleFileEntityException, RemoteException;
      
      /**
       ** Delete a relationshop between SegmentationRuleFile objects with TM Profile in the datastore
       **/
       public void deleteRelationshipWithTmp(String p_ruleid, String p_tmpid)
       throws SegmentationRuleFileEntityException, RemoteException;

}

