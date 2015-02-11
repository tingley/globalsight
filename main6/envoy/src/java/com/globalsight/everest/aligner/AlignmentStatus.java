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

package com.globalsight.everest.aligner;

import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;

public class AlignmentStatus
    implements Serializable
{
    //
    // Members
    //

    private String m_name = "";
    private String m_description = "";
    private String m_url = "";
    private int m_totalUnit = 0;
    private int m_errorUnit = 0;
    
    // List of error strings.
    private ArrayList m_errorMessages;

    //
    // Constructor
    //
    public AlignmentStatus(String p_name, String p_description, String p_url)
    {
        m_name = p_name;
        m_description = p_description;
        m_url = p_url;
    }

    public String getPackageName()
    {
        return m_name;
    }

    public String getPackageDescription()
    {
        return m_description;
    }

    public String getPackageUrl()
    {
        return m_url;
    }

    public void addErrorMessage(String p_errorMessage)
    {
        if (m_errorMessages == null)
        {
            m_errorMessages = new ArrayList();
        }

        m_errorMessages.add(p_errorMessage);
    }

    public void addAllErrorMessages(List p_errorMessages)
    {
        if (m_errorMessages == null)
        {
            m_errorMessages = new ArrayList();
        }

        m_errorMessages.addAll(p_errorMessages);
    }

    public boolean isError()
    {
        return m_errorUnit == m_totalUnit;
    }

    public boolean isWarning()
    {
        return m_errorUnit > 0 && m_errorUnit < m_totalUnit;
    }
    
    public ArrayList getErrorMessages()
    {
        return m_errorMessages;
    }

    public void setTotalUnitNumber(int p_totalUnitNumber)
    {
        m_totalUnit = p_totalUnitNumber;
    }
    
    public int getTotalUnitNumber()
    {
        return m_totalUnit;
    }
    
    public void setErrorUnitNumber(int p_errorUnitNumber)
    {
        m_errorUnit = p_errorUnitNumber;
    }
    
    public int getErrorUnitNumber()
    {
        return m_errorUnit;
    }
    

    public String toString()
    {
        return "[AlignerPackage " + m_name + ", " + m_url +
            ", error messages=" + m_errorMessages + "]";
    }

    public static ArrayList getTestPackages()
    {
        ArrayList result = new ArrayList();

        result.add(new AlignmentStatus(
            "test1", "created on Thu Aug 26 22:39:41 2004", "test1.alp"));
        result.add(new AlignmentStatus(
            "test2", "created on Thu Aug 26 22:39:41 2004", "test2.alp"));

        AlignmentStatus temp = new AlignmentStatus(
            "test 3", "created on Thu Aug 26 22:39:41 2004", "test_3.alp");
        temp.addErrorMessage("file 1 could not be extracted");
        result.add(temp);

        return result;
    }

    /**
     * Returns 'true' if the name is the same
     */
    public boolean equals(Object p_status)
    {
	if (p_status instanceof AlignmentStatus)
        {
	    return (m_name.equals(((AlignmentStatus)p_status).m_name));
	}
	return false;
    }

    /**
     * Returns the hash code of its name
     */
    public int hashCode()
    {
        return m_name.hashCode();
    }

}
