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

public class WrapFileProfileExtensionTable
{
    private long m_fileProfileID = 0;
    private long m_extensionID = 0;
        
    public WrapFileProfileExtensionTable (long p_fileProfileID, long p_extensionID)
    {
        m_fileProfileID = p_fileProfileID;
        m_extensionID = p_extensionID;
    }
    
    public long getFileProfileID() { return m_fileProfileID; }
    public long getExtensionID() { return m_extensionID; }
    public void setFileProfileID(long p_fileProfileID) { m_fileProfileID = p_fileProfileID; }
    public void setExtensionID(long p_extensionID) { m_extensionID = p_extensionID; }
}