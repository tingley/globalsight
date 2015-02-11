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

public class WrapExtensionTable
{
    protected long m_id = 0;
    protected String m_extension = "";
        
    public WrapExtensionTable (long p_id, String p_extension)
    {
        m_id = p_id;
        m_extension = p_extension;
    }
    
    public long getID() { return m_id; }
    public String getExtension() { return m_extension; }
    public void setID(long p_id) { m_id = p_id; }
    public void setExtension(String p_extension) { m_extension = p_extension; }
}