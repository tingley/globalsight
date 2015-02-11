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

public class UserLanguages extends WrapSystemLanguageTable
{
    private String m_longName = "";
    private String m_abbreviation = "";
    
    public UserLanguages (int p_id, String p_exportDirectory, 
        String p_longName, String p_abbreviation)
    {
        super(p_id, p_exportDirectory);
        m_longName = p_longName;
        m_abbreviation = p_abbreviation;
    }
    
    public String getLongName() { return m_longName; }
    public String getAbbreviation() { return m_abbreviation; }
}
