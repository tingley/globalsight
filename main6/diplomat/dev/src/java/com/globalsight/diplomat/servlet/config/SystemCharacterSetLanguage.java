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

public class SystemCharacterSetLanguage extends WrapLanguageCharacterSetTable
{
    private String m_characterSet = "";
        
    public SystemCharacterSetLanguage (int p_languageID, int p_characterSetID, String p_characterSet)    
    {
        super(p_languageID, p_characterSetID);
        m_characterSet = p_characterSet;
    }
    
    public String getCharacterSet() { return m_characterSet; }
}