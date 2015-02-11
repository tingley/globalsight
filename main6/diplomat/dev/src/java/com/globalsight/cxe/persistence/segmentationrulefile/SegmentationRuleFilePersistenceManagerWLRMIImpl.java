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
import java.util.Collection;

import com.globalsight.cxe.entity.segmentationrulefile.SegmentationRuleFile;
import com.globalsight.everest.util.system.RemoteServer;

public class SegmentationRuleFilePersistenceManagerWLRMIImpl 
    extends RemoteServer
    implements SegmentationRuleFilePersistenceManagerWLRemote
{
    SegmentationRuleFilePersistenceManager m_localReference;

    public SegmentationRuleFilePersistenceManagerWLRMIImpl() 
        throws java.rmi.RemoteException, SegmentationRuleFileEntityException
    {
        super(SegmentationRuleFilePersistenceManager.SERVICE_NAME);
        m_localReference = new SegmentationRuleFilePersistenceManagerLocal();
    }

    public Object getLocalReference()
    {
        return m_localReference;
    }

    public com.globalsight.cxe.entity.segmentationrulefile.SegmentationRuleFile createSegmentationRuleFile(com.globalsight.cxe.entity.segmentationrulefile.SegmentationRuleFile param1) throws com.globalsight.cxe.persistence.segmentationrulefile.SegmentationRuleFileEntityException,java.rmi.RemoteException
    {
        return m_localReference.createSegmentationRuleFile(param1);
    }

    public  void deleteSegmentationRuleFile(com.globalsight.cxe.entity.segmentationrulefile.SegmentationRuleFile param1) throws com.globalsight.cxe.persistence.segmentationrulefile.SegmentationRuleFileEntityException,java.rmi.RemoteException
    {
        m_localReference.deleteSegmentationRuleFile(param1);
    }

    public Collection getAllSegmentationRuleFiles() throws com.globalsight.cxe.persistence.segmentationrulefile.SegmentationRuleFileEntityException, java.rmi.RemoteException
    {
        return m_localReference.getAllSegmentationRuleFiles();
    }

    public  com.globalsight.cxe.entity.segmentationrulefile.SegmentationRuleFile readSegmentationRuleFile(long param1) throws com.globalsight.cxe.persistence.segmentationrulefile.SegmentationRuleFileEntityException,java.rmi.RemoteException
    {
        return m_localReference.readSegmentationRuleFile(param1);
    }

    public com.globalsight.cxe.entity.segmentationrulefile.SegmentationRuleFile updateSegmentationRuleFile(com.globalsight.cxe.entity.segmentationrulefile.SegmentationRuleFile param1) throws com.globalsight.cxe.persistence.segmentationrulefile.SegmentationRuleFileEntityException,java.rmi.RemoteException
    {
        return m_localReference.updateSegmentationRuleFile(param1);
    }

	public void createRelationshipWithTmp(String p_ruleid, String p_tmpid) throws SegmentationRuleFileEntityException, RemoteException
	{
		m_localReference.createRelationshipWithTmp(p_ruleid, p_tmpid);
	}

	public String getSegmentationRuleFileIdByTmpid(String p_tmpid) throws SegmentationRuleFileEntityException, RemoteException
	{
		return m_localReference.getSegmentationRuleFileIdByTmpid(p_tmpid);
	}

	public void deleteRelationshipWithTmp(String p_ruleid, String p_tmpid) throws SegmentationRuleFileEntityException, RemoteException
	{
		m_localReference.deleteRelationshipWithTmp(p_ruleid, p_tmpid);
	}

	public SegmentationRuleFile getDefaultSegmentationRuleFile() throws SegmentationRuleFileEntityException, RemoteException
	{
		return m_localReference.getDefaultSegmentationRuleFile();
	}

	public SegmentationRuleFile getSegmentationRuleFileByTmpid(String p_tmpid) throws SegmentationRuleFileEntityException, RemoteException
	{
		return m_localReference.getSegmentationRuleFileByTmpid(p_tmpid);
	}

	public String[] getTmpIdsBySegmentationRuleId(String p_ruleid) throws SegmentationRuleFileEntityException, RemoteException
	{
		return m_localReference.getTmpIdsBySegmentationRuleId(p_ruleid);
	}
	
	
}
