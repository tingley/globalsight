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
 
package com.globalsight.persistence.pageimport;

import java.sql.Connection;
import java.sql.PreparedStatement;

import com.globalsight.everest.persistence.PersistenceException;
import com.globalsight.everest.request.RequestImpl;
import com.globalsight.persistence.PersistenceCommand;

/**
 * @deprecated
 */
public class UpdateRequestExceptionCommand extends PersistenceCommand
{
//    private String m_updateRequestException = "update request set exception_xml = empty_clob(), type = ?  where id = ? ";
    private String m_updateRequestException = "update request set exception_xml = ?, type = ?  where id = ? ";
    
//    private String m_selectForUpdate = "select exception_xml from request where id = ? for update";
    private PreparedStatement m_ps;
    private PreparedStatement m_ps1;
    private long m_requestId;
    private String m_requestType;
    private String m_exceptionXml;

    public UpdateRequestExceptionCommand(RequestImpl p_request)
    {
        m_requestId = p_request.getId();
        m_requestType = getTypeAsString(p_request.getType());
        m_exceptionXml = p_request.getExceptionAsString();
    }

    /**
     * Constructor when the type, id and exception are passed in
     * rather than the entire Request object.
     */
    public UpdateRequestExceptionCommand(long p_requestId, int p_requestType, 
                                         String p_exceptionMessageXml)
    {
        m_requestId = p_requestId;
        m_requestType = getTypeAsString(p_requestType);
        m_exceptionXml = p_exceptionMessageXml;
    }
    
    public void persistObjects(Connection p_connection)
        throws PersistenceException
    {
        try
        {
            createPreparedStatement(p_connection);
            setData();
            batchStatements();
        }
        catch (Exception e)
        {
            throw new PersistenceException(e);
        }
        finally
        {
            try
            {
                if (m_ps != null)  m_ps.close();
                if (m_ps1 != null) m_ps1.close();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    public void createPreparedStatement(Connection p_connection) 
        throws Exception 
    {
        m_ps = p_connection.prepareStatement(m_updateRequestException);
//        m_ps1 = p_connection.prepareStatement(m_selectForUpdate);
    }

    public void setData() throws Exception 
    {
    	m_ps.setString(1, m_exceptionXml);
        m_ps.setString(2, m_requestType); 
        m_ps.setLong(3, m_requestId);
    }

    public void batchStatements() throws Exception 
    {
        m_ps.executeUpdate();
//        Clob clob = null;
//        CLOB oclob;
//        Writer writer = null;
//        ResultSet rs = null;
//        try
//        {
//            m_ps1.setLong(1,m_requestId);
//            rs = m_ps1.executeQuery();
//            if (rs.next())
//            {
//                clob = rs.getClob(1);
//            }
//            oclob = (CLOB)clob;
//            writer = oclob.getCharacterOutputStream();
//            StringReader sr = new StringReader(m_exceptionXml);
//            char[] buffer = new char[oclob.getChunkSize()];
//            int charsRead = 0;
//            while ((charsRead = sr.read(buffer)) != EOF)
//            {
//                writer.write(buffer, 0, charsRead);
//            }
//        }
//	finally
//        {
//	    close(writer);
//	    close(rs);
//        }
      }

    /**
     * Return the request type as a string.
     */
    private String getTypeAsString(int p_requestType)
    {
        String requestType = "";
        if (p_requestType == RequestImpl.REQUEST_WITH_IMPORT_ERROR)
        {
            requestType = "REQUEST_WITH_IMPORT_ERROR";
        }
        else if (p_requestType == RequestImpl.REQUEST_WITH_CXE_ERROR)
        {
            requestType = "REQUEST_WITH_CXE_ERROR";
        }
        return requestType;
    }
}
