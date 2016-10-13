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
package com.globalsight.ling.common;

import java.util.Properties;

/**
 * Default handler for parsing standalone Diplomat strings
 */
public interface DiplomatBasicHandler
{
    public void handleEndTag(String p_name, String p_originalTag)
        throws DiplomatBasicParserException;

    public void handleStartTag(String p_name,
      Properties p_atributes, String p_originalString)
        throws DiplomatBasicParserException;

    public void handleText(String p_text)
        throws DiplomatBasicParserException;

    public void handleStart()
        throws DiplomatBasicParserException;

    public void handleStop()
        throws DiplomatBasicParserException;
}
