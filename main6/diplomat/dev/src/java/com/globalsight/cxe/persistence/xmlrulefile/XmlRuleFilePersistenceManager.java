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

import com.globalsight.cxe.entity.xmlrulefile.XmlRuleFile;

import java.util.Collection;

import java.rmi.RemoteException;

/** A service interface for performing CRUD operations for XmlRuleFiles **/
public interface XmlRuleFilePersistenceManager
{
    public static final String SERVICE_NAME = "XmlRuleFilePersistenceManager";

    /**
    ** Creates a new XmlRuleFile object in the data store
    ** @return the created object
    **/
    public XmlRuleFile createXmlRuleFile(XmlRuleFile p_xmlRuleFile)
    throws XmlRuleFileEntityException, RemoteException;

    /**
    ** Reads the XmlRuleFile object from the datastore
    ** @return the XmlRuleFile
    **/
    public XmlRuleFile readXmlRuleFile(long p_id)
    throws XmlRuleFileEntityException, RemoteException;

    /**
    ** Deletes an XML Rule File from the datastore
    **/
    public void deleteXmlRuleFile(XmlRuleFile p_xmlRuleFile)
    throws XmlRuleFileEntityException, RemoteException;


    /**
    ** Update the XMLRuleFile object in the datastore
    ** @return the updated object
    **/
    public XmlRuleFile updateXmlRuleFile(XmlRuleFile p_xmlRuleFile)
    throws XmlRuleFileEntityException, RemoteException;

    /**
    ** Get a list of all existing XMLRuleFile objects in the datastore
    ** @return a vector of the XmlRuleFile objects
    **/
    public Collection getAllXmlRuleFiles()
    throws XmlRuleFileEntityException, RemoteException;

}

