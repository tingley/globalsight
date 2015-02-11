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

package com.globalsight.cxe.persistence.xmlrulefile;


import java.util.Collection;

import com.globalsight.everest.util.system.RemoteServer;

public class XmlRuleFilePersistenceManagerWLRMIImpl 
    extends RemoteServer
    implements XmlRuleFilePersistenceManagerWLRemote
{
    XmlRuleFilePersistenceManager m_localReference;

    public XmlRuleFilePersistenceManagerWLRMIImpl() 
        throws java.rmi.RemoteException, XmlRuleFileEntityException
    {
        super(XmlRuleFilePersistenceManager.SERVICE_NAME);
        m_localReference = new XmlRuleFilePersistenceManagerLocal();
    }

    public Object getLocalReference()
    {
        return m_localReference;
    }

    public com.globalsight.cxe.entity.xmlrulefile.XmlRuleFile createXmlRuleFile(com.globalsight.cxe.entity.xmlrulefile.XmlRuleFile param1) throws com.globalsight.cxe.persistence.xmlrulefile.XmlRuleFileEntityException,java.rmi.RemoteException
    {
        return m_localReference.createXmlRuleFile(param1);
    }

    public  void deleteXmlRuleFile(com.globalsight.cxe.entity.xmlrulefile.XmlRuleFile param1) throws com.globalsight.cxe.persistence.xmlrulefile.XmlRuleFileEntityException,java.rmi.RemoteException
    {
        m_localReference.deleteXmlRuleFile(param1);
    }

    public Collection getAllXmlRuleFiles() throws com.globalsight.cxe.persistence.xmlrulefile.XmlRuleFileEntityException, java.rmi.RemoteException
    {
        return m_localReference.getAllXmlRuleFiles();
    }

    public  com.globalsight.cxe.entity.xmlrulefile.XmlRuleFile readXmlRuleFile(long param1) throws com.globalsight.cxe.persistence.xmlrulefile.XmlRuleFileEntityException,java.rmi.RemoteException
    {
        return m_localReference.readXmlRuleFile(param1);
    }

    public com.globalsight.cxe.entity.xmlrulefile.XmlRuleFile updateXmlRuleFile(com.globalsight.cxe.entity.xmlrulefile.XmlRuleFile param1) throws com.globalsight.cxe.persistence.xmlrulefile.XmlRuleFileEntityException,java.rmi.RemoteException
    {
        return m_localReference.updateXmlRuleFile(param1);
    }
}
