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
package com.globalsight.everest.vendormanagement;

import com.globalsight.everest.persistence.PersistentObject;

/**
 * The class that holds the information about one particular communication for a
 * user/vendor. This includes email, various phone numbers, fax numbers, etc.
 */
public class CommunicationInfo extends PersistentObject
{
    private static final long serialVersionUID = 4417178869407380998L;

    // constants for types of communications the user can have
    public interface CommunicationType
    {
        static final int WORK = 1;
        static final int HOME = 2;
        static final int CELL = 3;
        static final int FAX = 4;
        static final int EMAIL = 5;
        static final int OTHER = 10;
    };

    // used by toplink
    public static final String KEY_METHOD = "getTypeAsInteger";

    private int m_type; // used by program
    private char type_char; // used by Hibernate
    private String m_value;
    private Vendor m_vendor; // backpointer to the vendor

    /**
     * Default constructor
     */
    public CommunicationInfo()
    {

    }

    /**
     * Create the communication info with one of the valid types specified in
     * the CommunicationType interface.
     */
    public CommunicationInfo(int p_type, String p_value, Vendor p_vendor)
    {
        if (isValidType(p_type))
        {
            m_type = p_type;
        }
        else
        {
            m_type = CommunicationType.OTHER;
        }
        switch (m_type)
        {
            case CommunicationType.WORK:
                type_char = 'W';
                break;
            case CommunicationType.HOME:
                type_char = 'H';
                break;
            case CommunicationType.CELL:
                type_char = 'C';
                break;
            case CommunicationType.FAX:
                type_char = 'F';
                break;
            case CommunicationType.EMAIL:
                type_char = 'E';
                break;
            case CommunicationType.OTHER:
                type_char = 'O';

        }
        m_value = p_value;
        m_vendor = p_vendor;
    }

    public int getType()
    {
        return m_type;
    }

    /**
     * This is needed by TOPLink to put into hash table. Can't use the primitive
     * int.
     */
    public Integer getTypeAsInteger()
    {
        return new Integer(m_type);
    }

    public String getValue()
    {
        return m_value;
    }

    public void setValue(String p_value)
    {
        m_value = p_value;
    }

    /**
     * Returns 'true' if the specified type is a valid type. Returns 'false' if
     * the type is not valid.
     */
    public static boolean isValidType(int p_type)
    {
        boolean isValid = false;
        switch (p_type)
        {
            case CommunicationType.WORK:
            case CommunicationType.HOME:
            case CommunicationType.CELL:
            case CommunicationType.FAX:
            case CommunicationType.EMAIL:
            case CommunicationType.OTHER:
                isValid = true;
                break;
            default:
                // leave as false
        }
        return isValid;
    }

    public static String typeAsString(int p_type)
    {
        String type = null;
        switch (p_type)
        {
            case CommunicationType.WORK:
                type = "WORK";
                break;
            case CommunicationType.HOME:
                type = "HOME";
                break;
            case CommunicationType.CELL:
                type = "CELL";
                break;
            case CommunicationType.FAX:
                type = "FAX";
                break;
            case CommunicationType.EMAIL:
                type = "EMAIL";
                break;
            case CommunicationType.OTHER:
                type = "OTHER";
                break;
            default:
                // leave as null because not valid
        }
        return type;
    }

    public static int typeAsInt(String p_type)
    {
        int type = -1;
        if (p_type.equals("WORK"))
        {
            type = CommunicationType.WORK;
        }
        else if (p_type.equals("EMAIL"))
        {
            type = CommunicationType.EMAIL;
        }
        else if (p_type.equals("HOME"))
        {
            type = CommunicationType.HOME;
        }
        else if (p_type.equals("FAX"))
        {
            type = CommunicationType.FAX;
        }
        else if (p_type.equals("CELL"))
        {
            type = CommunicationType.CELL;
        }
        else if (p_type.equals("OTHER"))
        {
            type = CommunicationType.OTHER;
        }
        return type;
    }

    public char getType_char()
    {
        return type_char;
    }

    public void setType_char(char type_char)
    {
        this.type_char = type_char;
        switch (type_char)
        {
            case 'W':
                m_type = CommunicationType.WORK;
                break;
            case 'H':
                m_type = CommunicationType.HOME;
                break;
            case 'C':
                m_type = CommunicationType.CELL;
                break;
            case 'F':
                m_type = CommunicationType.FAX;
                break;
            case 'E':
                m_type = CommunicationType.EMAIL;
                break;
            case 'O':
                m_type = CommunicationType.OTHER;
        }
    }

    public Vendor getVendor()
    {
        return m_vendor;
    }

    public void setVendor(Vendor m_vendor)
    {
        this.m_vendor = m_vendor;
    }

    @Override
    public int hashCode()
    {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object paramObject)
    {
        return super.equals(paramObject);
    }
}
