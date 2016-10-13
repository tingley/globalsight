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
package com.globalsight.ling.docproc.merger;

import com.globalsight.ling.docproc.DiplomatMergerException;

/**
 * <p>The interface defines the interfaces of format specific post
 * merge processor.</p>
 *
 * <p>All post merge processor in the framework must implements this
 * interface.</p> */
public interface PostMergeProcessor
{
    /**
     * Processes the merged content.
     *
     * @param p_content content as String
     * @param p_IanaEncoding encoding name as IANA registered name
     * @return processed content. If the change is unnecessary, it
     * returns null.
     */
    String process(String p_content, String p_IanaEncoding)
        throws DiplomatMergerException;
}
