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


package com.globalsight.diplomat.servlet.config;

public class GSA_User
{
    private int m_code = 0;
    private String m_name = "";
    private String m_password = "";
    private String m_fullName = "";
    private int m_type = 0;
    private String m_phoneNumber = "";
    private String m_faxNumber = "";
    private String m_email = "";
    private String m_address = "";
    private String m_comment = "";
    private int m_language = 0;
    private String m_homePage = "";
    
    public GSA_User (int p_code, String p_name, String p_password,
        String p_fullName, int p_type, String p_phoneNumber, 
        String p_faxNumber, String p_email, String p_address, 
        String p_comment, int p_language, String p_homePage)
    {
        m_code = p_code;
        m_name = p_name;
        m_password = p_password;
        m_fullName = p_fullName;
        m_type = p_type;
        m_phoneNumber = p_phoneNumber;
        m_faxNumber = p_faxNumber;
        m_email = p_email;
        m_address = p_address;
        m_comment = p_comment;
        m_language = p_language;
        m_homePage = p_homePage;
    }
    
    public int getCode() { return m_code; }
    public String getName() { return m_name; }
    public String getPassword() { return m_password; }
    public String getFullName() { return m_fullName; }
    public int getType() { return m_type; }
    public String getPhoneNumber() { return m_phoneNumber; }
    public String getFaxNumber() { return m_faxNumber; }
    public String getEmail() { return m_email; }
    public String getAddress() { return m_address; }
    public String getComment() { return m_comment; }
    public int getLanguage() { return m_language; }
    public String getHomePage() { return m_homePage; }  
    
    public String toString() { return m_name; }
}