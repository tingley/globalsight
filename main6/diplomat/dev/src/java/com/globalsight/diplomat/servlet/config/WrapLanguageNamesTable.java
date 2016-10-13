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

public class WrapLanguageNamesTable
{
    private int m_languageID = 0;       // the target language id
    private int m_displayID = 0;        // the current user language
    private String m_language = "";     // the target language
        
    public WrapLanguageNamesTable (int p_languageID, int p_displayID, String p_language)
    {
        m_languageID = p_languageID;
        m_displayID = p_displayID;
        m_language = p_language;
    }
    
    public int getLanguageID() { return m_languageID; }
    public int getDisplayID() { return m_displayID; }
    public String getLanguage() { return m_language; }
}