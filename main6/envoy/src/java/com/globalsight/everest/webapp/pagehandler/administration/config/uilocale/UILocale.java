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

package com.globalsight.everest.webapp.pagehandler.administration.config.uilocale;

import com.globalsight.everest.persistence.PersistentObject;

/**
 * One persistent object containing the information about ui locale
 */
public class UILocale extends PersistentObject
{
    private static final long serialVersionUID = 7618550163413340723L;
    private String shortname;
    private boolean defaultLocale;
    private String longname;

    public String getShortName()
    {
        return this.shortname;
    }

    public void setShortName(String shortname)
    {
        this.shortname = shortname;
    }
    
    public String getLongName()
    {
    	return this.longname;
    }
    
    public void setLongName(String longname)
    {
    	this.longname = longname;
    }

    public boolean isDefaultLocale()
    {
        return defaultLocale;
    }

    public void setDefaultLocale(boolean defaultLocale)
    {
        this.defaultLocale = defaultLocale;
    }
}
