/**
 *  Copyright 2016 Welocalize, Inc. 
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
package com.globalsight.ling.docproc.merger.json;

import org.apache.log4j.Logger;

import com.globalsight.ling.docproc.merger.PostMergeProcessor;
import com.globalsight.ling.docproc.DiplomatMergerException;

/**
 * This class post processes a merged JSON document.
 */
public class JsonPostMergeProcessor implements PostMergeProcessor
{
    private static Logger logger = Logger.getLogger(JsonPostMergeProcessor.class);

    @Override
    public String process(String p_content, String p_IanaEncoding) throws DiplomatMergerException
    {
        return null;
    }

}
