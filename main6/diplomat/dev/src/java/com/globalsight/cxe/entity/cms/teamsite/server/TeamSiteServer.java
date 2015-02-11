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
package com.globalsight.cxe.entity.cms.teamsite.server;

import java.util.Vector;

public interface TeamSiteServer
{
    /**
     * Get the id of this TeamSiteServer
     * 
     * @return The Teamsite Server Id
     */
    public long getId();

    /**
     * Get name of the company this TeamSiteServer belong to.
     * 
     * @return The company name.
     */
    public long getCompanyId();

    /**
     * Get name of the company this TeamSiteServer belong to.
     * 
     * @return The company name.
     */
    public void setCompanyId(long p_companyId);

    /**
     * Get the ids of backing stores associated with this server.
     * 
     * @return The vector containing BackingStoreIds as BigDecimal
     */
    public Vector getBackingStoreIds();

    /**
     * Get the Name of the Teamsite Server.
     * 
     * @return Name of the Teamsite Server
     */
    public String getName();

    /**
     * Get the Description of the Teamsite Server.
     * 
     * @return Description of the Teamsite Server
     */
    public String getDescription();

    /**
     * Get the Operating System of the Teamsite Server.
     * 
     * @return Operating System of the Teamsite Server
     */
    public String getOS();

    /**
     * Get the Export Port of the Teamsite Server.
     * 
     * @return Export Port of the Teamsite Server
     */
    public int getExportPort();

    /**
     * Get the Import Port of the Teamsite Server.
     * 
     * @return Import Port of the Teamsite Server
     */
    public int getImportPort();

    /**
     * Get the Proxy Port of the Teamsite Server.
     * 
     * @return Proxy Port of the Teamsite Server
     */
    public int getProxyPort();

    /**
     * Get the Home of the Teamsite Server.
     * 
     * @return Home of the Teamsite Server
     */
    public String getHome();

    /**
     * Get the User of the Teamsite Server.
     * 
     * @return User of the Teamsite Server
     */
    public String getUser();

    public String getUserPass();

    public void setUserPass(String p_userPass);

    /**
     * Get the Type of the Teamsite Server.
     * 
     * @return Type of the Teamsite Server
     */
    public String getType();

    /**
     * Get the Mount of the Teamsite Server.
     * 
     * @return Mount of the Teamsite Server
     */
    public String getMount();

    /**
     * Get the Locale specifc reimport setting of the Teamsite Server.
     * 
     * @return Locale specifc reimport setting of the Teamsite Server
     */
    public boolean getLocaleSpecificReimportSetting();

    /**
     * Set the Name of the Teamsite Server.
     */
    public void setName(String p_name);

    /**
     * Set the Description of the Teamsite Server.
     */
    public void setDescription(String p_description);

    /**
     * Set the Operating System of the Teamsite Server.
     */
    public void setOS(String p_operatingSystem);

    /**
     * Set the Export Port of the Teamsite Server.
     */
    public void setExportPort(int p_exportPort);

    /**
     * Set the Import Port of the Teamsite Server.
     */
    public void setImportPort(int p_importPort);

    /**
     * Set the Proxy Port of the Teamsite Server.
     */
    public void setProxyPort(int p_proxyPort);

    /**
     * Set the Home of the Teamsite Server.
     */
    public void setHome(String p_home);

    /**
     * Set the User of the Teamsite Server.
     */
    public void setUser(String p_user);

    /**
     * Set the Type of the Teamsite Server.
     */
    public void setType(String p_type);

    /**
     * Set the Mount of the Teamsite Server.
     */
    public void setMount(String p_mount);

    /**
     * Set the Locale specifc reimport setting of the Teamsite Server.
     */
    public void setLocaleSpecificReimportSetting(
            boolean p_allowLocaleSpecificReimport);

    /**
     * Set the backing stores for the TeamsiteServer
     */
    public void setBackingStoreIds(Vector p_backingStoreIds);

}
