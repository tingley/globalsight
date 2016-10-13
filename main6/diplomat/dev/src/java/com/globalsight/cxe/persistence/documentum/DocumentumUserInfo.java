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
package com.globalsight.cxe.persistence.documentum;

import com.globalsight.everest.persistence.PersistentObject;

public class DocumentumUserInfo 
extends PersistentObject
{
    private static final long serialVersionUID = -3759956732481906960L;
    
    //
    // PUBLIC CONSTANTS FOR USE BY TOPLINK
    //
    public static final String M_USERID = "m_documentumUserId";
    public static final String M_PASSWORD = "m_documentumPassword";
    public static final String M_DOCBASE = "m_documentumDocbase";
    
    //
    // PRIVATE MEMBER VARIABLES
    //
    private String m_documentumUserId = null;
    private String m_documentumPassword = null;
    private String m_documentumDocbase = null;
    
    /**
     *  Default constructor used by TopLink only
     */
    public DocumentumUserInfo()
    {
        this( null, null);
    }


    /**
     * Constructor that supplies all attributes for user parameter object.
     *
     * @param p_documentumUserId The user id used for CMS
     * @param p_documentumPasswordId The password used for CMS
     */
    public DocumentumUserInfo( 
                       String p_documentumUserId, 
                       String p_documentumPassword)
    {
        super();

        m_documentumUserId = p_documentumUserId;
        m_documentumPassword = p_documentumPassword;
    }


    public String getDocumentumPassword() {
        return m_documentumPassword;
    }


    public void setDocumentumPassword(String password) {
        m_documentumPassword = password;
    }


    public String getDocumentumUserId() {
        return m_documentumUserId;
    }


    public void setDocumentumUserId(String userId) {
        m_documentumUserId = userId;
    }


    public String getDocumentumDocbase() {
        return m_documentumDocbase;
    }


    public void setDocumentumDocbase(String docbase) {
        m_documentumDocbase = docbase;
    }

    public String toString() {
        
        StringBuffer sb = new StringBuffer();

        sb.append(super.toString());
        sb.append("{m_documentumUserId=");
        sb.append(m_documentumUserId);
        sb.append(", m_documentumPassword=");
        sb.append(m_documentumPassword);
        sb.append(", m_documentumDocbase=");
        sb.append(m_documentumDocbase);
        sb.append("}");

        return sb.toString();
    }
    
}