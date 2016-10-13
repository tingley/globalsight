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
package com.globalsight.ling.tm2;

/**
 * PageTmTu represents a Tu in Page Tm table. And also it is used to represent
 * job data tu (transltaion_unit). The structure of the two are almost the same.
 */

public class PageTmTu extends AbstractTmTu
{
    /**
     * Default constructor.
     */
    public PageTmTu()
    {
        super();
    }

    /**
     * Constructor.
     * 
     * @param p_id
     *            id
     * @param p_tmId
     *            tm id
     * @param p_format
     *            format name
     * @param p_type
     *            type name
     * @param p_translatable
     *            set this Tu translatable if this param is true
     */
    public PageTmTu(long p_id, long p_tmId, String p_format, String p_type,
            boolean p_translatable)
    {
        super(p_id, p_tmId, p_format, p_type, p_translatable);
    }

    @Override
    public boolean isFromWorldServer()
    {
        return false;
    }

    @Override
    public void setFromWorldServer(boolean fromWorldServer)
    {
        
    }
}
