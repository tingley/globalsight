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
package galign.data;

/**
 * A record holding user name, phone, office, address, institution etc.
 */
public class UserProfile
{
    //
    // Members
    //
    private String m_name;
    private String m_company;
    private String m_department;
    private String m_address1;
    private String m_address2;
    private String m_address3;
    private String m_country;
    private String m_workphone;
    private String m_homephone;
    private String m_mobilephone;
    private String m_email;
    private String m_privateemail;

    //
    // Constructor
    //
    public UserProfile()
    {
    }

    //
    // Public Methods
    //

    public String getName()
    {
        return m_name;
    }

    public String getEmail()
    {
        return m_email;
    }
}

