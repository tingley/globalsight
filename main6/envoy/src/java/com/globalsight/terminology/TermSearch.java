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

package com.globalsight.terminology;

import java.util.ArrayList;

import org.apache.log4j.Logger;

public interface TermSearch
{
    static final Logger CATEGORY = 
        Logger.getLogger(Termbase.class);
    public static final int MAX_SORTKEY_LEN = 2000;
    public static final String MSG_SQL_ERROR = "sql_error";
    
    public void setIndexs(ArrayList indexs);
    public void setTermbaseId(long tb_id);
    public void setTerbaseName(String name);
    public void setDefinition(Definition definition);
    public String getXmlResults(String sourceLanguage, String targetLanguage,
            String p_query, int end, int begin) throws TermbaseException;
    public Hitlist getHitListResults(String sourceLanguage, String targetLanguage,
            String p_query, int end, int begin) throws TermbaseException;
}
