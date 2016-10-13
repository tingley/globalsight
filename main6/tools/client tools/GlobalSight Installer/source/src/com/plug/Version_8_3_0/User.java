package com.plug.Version_8_3_0;

import java.util.HashMap;
import java.util.Map;

public class User
{
    private String m_userId = null;

    private int m_state = State.CREATED;

    private String m_userName = null;

    private String m_firstName = null;

    private String m_lastName = null;

    private String m_title = null;

    private String m_companyName = null;

    private String m_password = null;

    private String m_email = null;

    private String m_ccEmail = null;

    private String m_bccEmail = null;

    private String m_address = null;

    private Map<Integer, String> m_phoneNumbers = new HashMap<Integer, String>();

    private String m_defaultLocale = null;

    private boolean isPasswordSet = false;

    private int m_type = UserType.GLOBALSIGHT;

    private boolean m_isInAllProjects = false;

    public User()
    {
    }

    public User(String p_userId)
    {
        m_userId = p_userId;
    }

    public String getUserId()
    {
        return m_userId;
    }

    public void setUserId(String p_userId)
    {
        m_userId = p_userId;
    }

    public int getState()
    {
        return m_state;
    }

    public void setState(int p_state)
    {
        m_state = p_state;
    }

    public String getUserName()
    {
        return m_userName;
    }

    public void setUserName(String p_userName)
    {
        m_userName = p_userName;
    }

    public String getFirstName()
    {
        return m_firstName;
    }

    public void setFirstName(String p_firstName)
    {
        m_firstName = p_firstName;
    }

    public String getLastName()
    {
        return m_lastName;
    }

    public void setLastName(String p_lastName)
    {
        m_lastName = p_lastName;
    }

    public String getTitle()
    {
        return m_title;
    }

    public void setTitle(String p_title)
    {
        m_title = p_title;
    }

    public String getCompanyName()
    {
        return m_companyName;
    }

    public void setCompanyName(String p_companyName)
    {
        m_companyName = p_companyName;
    }

    public String getPassword()
    {
        return m_password;
    }

    public void setPassword(String p_password)
    {
        m_password = p_password;
    }

    public String getEmail()
    {
        return m_email;
    }

    public void setEmail(String p_email)
    {
        m_email = p_email;
    }

    public String getCcEmail()
    {
        return m_ccEmail;
    }

    public void setCcEmail(String p_ccEmail)
    {
        m_ccEmail = p_ccEmail;
    }

    public String getBccEmail()
    {
        return m_bccEmail;
    }

    public void setBccEmail(String p_bccEmail)
    {
        m_bccEmail = p_bccEmail;
    }

    public String getAddress()
    {
        return m_address;
    }

    public void setAddress(String p_address)
    {
        m_address = p_address;
    }

    public String getPhoneNumber(int p_type)
    {
        return m_phoneNumbers.get(p_type);
    }

    public void setPhoneNumber(int p_type, String p_phoneNumber)
    {
        if (p_type == PhoneType.OFFICE || p_type == PhoneType.HOME
                || p_type == PhoneType.CELL || p_type == PhoneType.FAX)
        {
            if (p_phoneNumber == null)
            {
                // clear out the field
                p_phoneNumber = "";
            }
            m_phoneNumbers.put(p_type, p_phoneNumber);
        }
    }

    public String getDefaultLocale()
    {
        return m_defaultLocale;
    }

    public void setDefaultLocale(String p_defaultLocale)
    {
        m_defaultLocale = p_defaultLocale;
    }

    public boolean isPasswordSet()
    {
        return isPasswordSet;
    }

    public void setPasswordSet(boolean isPasswordSet)
    {
        this.isPasswordSet = isPasswordSet;
    }

    public int getType()
    {
        return m_type;
    }

    public void setType(int p_type)
    {
        m_type = p_type;
    }

    public boolean isInAllProjects()
    {
        return m_isInAllProjects;
    }

    public void setIsInAllProjects(boolean p_isInAllProjects)
    {
        m_isInAllProjects = p_isInAllProjects;
    }

    public interface State
    {
        static final int CREATED = 1;

        static final int ACTIVE = 2;

        static final int DEACTIVE = 3;

        static final int DELETED = 4;
    };

    public interface UserType
    {
        static final int ANONYMOUS = 0;

        static final int GLOBALSIGHT = 1;
    };

    public interface PhoneType
    {
        static final int OFFICE = 1;

        static final int HOME = 2;

        static final int CELL = 3;

        static final int FAX = 4;
    };

    public String toString()
    {
        return m_userName;
    }
}
