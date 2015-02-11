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

public class GSA_Translator
{
    private int m_userCode = 0;
    private int m_id = 0;
    private int m_pairID = 0;
    
    public GSA_Translator (int p_userCode, int p_id, int p_pairID)
    {
        m_userCode = p_userCode;
        m_id = p_id;
        m_pairID = p_pairID;
    }
    
    public int getUserCode() { return m_userCode; }
    public int getID() { return m_id; }
    public int getPairID() { return m_pairID; }  
}