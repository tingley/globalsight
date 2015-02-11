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

import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import com.globalsight.cxe.entity.cms.teamsite.store.BackingStoreImpl;
import com.globalsight.everest.persistence.PersistentObject;

/**
 * This class holds TeamSite server information
 */
public class TeamSiteServerImpl extends PersistentObject implements
        TeamSiteServer
{
    /**
     * Used for TOPLink queries based on a TeamSite Server Name.
     */
    public static final String NAME = "m_name";

    // id of the company which this TeamSiteServer belong to
    private long m_companyId;

    private String m_name;

    private String m_description;

    private int m_exportPort;

    private int m_importPort;

    private int m_proxyPort;

    private String m_operatingSystem;

    private String m_home;

    private String m_user;

    private String m_userPass;

    private String m_type;

    private String m_mount;

    private boolean m_allowLocaleSpecificReimport;

    private Vector m_backingStoreIds;

    private Vector m_backingStores;

    /**
     * Default constructor to be used by TopLink only. This is here solely
     * because the persistence mechanism that persists instances of this class
     * is using TopLink, and TopLink requires a public default constructor for
     * all the classes that it handles persistence for.
     */
    public TeamSiteServerImpl()
    {
        m_companyId = -1;
        m_name = null;
        m_description = null;
        m_exportPort = 0;
        m_importPort = 0;
        m_proxyPort = 0;
        m_operatingSystem = null;
        m_home = null;
        m_user = null;
        m_userPass = null;
        m_type = null;
        m_mount = null;
        m_allowLocaleSpecificReimport = false;
        m_backingStoreIds = new Vector();
    }

    /**
     * Construct a TeamSiteServerImpl
     */
    public TeamSiteServerImpl(TeamSiteServer p_tso)
    {
        m_companyId = p_tso.getCompanyId();
        m_name = p_tso.getName();
        m_description = p_tso.getDescription();
        m_exportPort = p_tso.getExportPort();
        m_importPort = p_tso.getImportPort();
        m_proxyPort = p_tso.getProxyPort();
        m_operatingSystem = p_tso.getOS();
        m_home = p_tso.getHome();
        m_user = p_tso.getUser();
        m_userPass = p_tso.getUserPass();
        m_type = p_tso.getType();
        m_mount = p_tso.getMount();
        m_allowLocaleSpecificReimport = p_tso
                .getLocaleSpecificReimportSetting();
        if (p_tso.getBackingStoreIds() == null)
        {
            m_backingStoreIds = new Vector();
        }
        else
        {
            m_backingStoreIds = p_tso.getBackingStoreIds();
        }
    }

    /**
     * Get name of the company this TeamSiteServer belong to.
     * 
     * @return The company name.
     */
    public long getCompanyId()
    {
        return this.m_companyId;
    }

    /**
     * Get name of the company this TeamSiteServer belong to.
     * 
     * @return The company name.
     */
    public void setCompanyId(long p_companyId)
    {
        this.m_companyId = p_companyId;
    }

    /**
     * Get the Name of the TeamSite Server.
     * 
     * @return Name of the TeamSite Server
     */
    public String getName()
    {
        return m_name;
    }

    /**
     * Get the Description of the TeamSite Server.
     * 
     * @return Description of the TeamSite Server
     */
    public String getDescription()
    {
        return m_description;
    }

    /**
     * Get the Operating System of the TeamSite Server.
     * 
     * @return Operating System of the TeamSite Server
     */
    public String getOS()
    {
        return m_operatingSystem;
    }

    /**
     * Get the Export Port of the TeamSite Server.
     * 
     * @return Export Port of the TeamSite Server
     */
    public int getExportPort()
    {
        return m_exportPort;
    }

    /**
     * Get the Import Port of the TeamSite Server.
     * 
     * @return Import Port of the TeamSite Server
     */
    public int getImportPort()
    {
        return m_importPort;
    }

    /**
     * Get the Proxy Port of the TeamSite Server.
     * 
     * @return Proxy Port of the TeamSite Server
     */
    public int getProxyPort()
    {
        return m_proxyPort;
    }

    /**
     * Get the Home of the TeamSite Server.
     * 
     * @return Home of the TeamSite Server
     */
    public String getHome()
    {
        return m_home;
    }

    /**
     * Get the User of the TeamSite Server.
     * 
     * @return User of the TeamSite Server
     */
    public String getUser()
    {
        return m_user;
    }

    /**
     * Get the Type of the TeamSite Server.
     * 
     * @return Type of the TeamSite Server
     */
    public String getType()
    {
        return m_type;
    }

    /**
     * Get the Mount of the TeamSite Server.
     * 
     * @return Mount of the TeamSite Server
     */
    public String getMount()
    {
        return m_mount;
    }

    /**
     * Get the Locale specifc reimport setting of the TeamSite Server.
     * 
     * @return Locale specifc reimport setting of the TeamSite Server
     */
    public boolean getLocaleSpecificReimportSetting()
    {
        return m_allowLocaleSpecificReimport;
    }

    /**
     * Get the Backing Store Ids for this TeamSiteServer
     * 
     * @return Vector containing the backing store ids.
     */
    public Vector getBackingStoreIds()
    {
        return m_backingStoreIds;
    }

    /**
     * Sets the Teamsite Sserver associated list of Backing Store ids.
     * 
     * @param p_backingStores
     *            An Vector of extension Ids for this file The Vector must
     *            contain objects of type Number (probably BigDecimal since
     *            TOPLink uses this method to populate from the database). This
     *            method will convert them to Longs.
     */
    public void setBackingStoreIds(Vector p_backingStoreIds)
    {
        if (p_backingStoreIds == null)
            m_backingStoreIds = new Vector();
        else
        {
            // clear out the current ones if there are any
            // can't use method "clear" because it isn't Java 1.1 compliant
            // for the applet
            if (m_backingStoreIds.size() > 0)
            {
                m_backingStoreIds = new Vector();
            }
            // convert whatever type Number it is to a Long
            int size = p_backingStoreIds.size();
            for (int i = 0; i < size; i++)
            {
                Long backingStore = new Long(
                        ((Number) p_backingStoreIds.elementAt(i)).longValue());
                m_backingStoreIds.addElement(backingStore);
            }
        }
    }

    /**
     * Set the Name of the TeamSite Server.
     */
    public void setName(String p_name)
    {
        m_name = p_name;
    }

    /**
     * Set the Description of the TeamSite Server.
     */
    public void setDescription(String p_description)
    {
        m_description = p_description;
    }

    /**
     * Set the Operating System of the TeamSite Server.
     */
    public void setOS(String p_operatingSystem)
    {
        m_operatingSystem = p_operatingSystem;
    }

    /**
     * Set the Export Port of the TeamSite Server.
     */
    public void setExportPort(int p_exportPort)
    {
        m_exportPort = p_exportPort;
    }

    /**
     * Set the Import Port of the TeamSite Server.
     */
    public void setImportPort(int p_importPort)
    {
        m_importPort = p_importPort;
    }

    /**
     * Set the Proxy Port of the TeamSite Server.
     */
    public void setProxyPort(int p_proxyPort)
    {
        m_proxyPort = p_proxyPort;
    }

    /**
     * Set the Home of the TeamSite Server.
     */
    public void setHome(String p_home)
    {
        m_home = p_home;
    }

    /**
     * Set the User of the TeamSite Server.
     */
    public void setUser(String p_user)
    {
        m_user = p_user;
    }

    public void setUserPass(String p_userPass)
    {
        m_userPass = p_userPass;
    }

    public String getUserPass()
    {
        return m_userPass;
    }

    /**
     * Set the Type of the TeamSite Server.
     */
    public void setType(String p_type)
    {
        m_type = p_type;
    }

    /**
     * Set the Mount of the TeamSite Server.
     */
    public void setMount(String p_mount)
    {
        m_mount = p_mount;
    }

    /**
     * Set the Locale specifc reimport setting of the TeamSite Server.
     */
    public void setLocaleSpecificReimportSetting(
            boolean p_allowLocaleSpecificReimport)
    {
        m_allowLocaleSpecificReimport = p_allowLocaleSpecificReimport;
    }

    /**
     * Sets the TeamsiteServer's associated list of backing Store ids
     * 
     * @param p_backingStoreIds
     *            A Vector of backing store Ids for this TeamSite Server The
     *            Vector must contain objects of type Number (probably
     *            BigDecimal since TOPLink uses this method to populate from the
     *            database). This method will convert them to Longs.
     */
    public void setLocaleSpecificReimportSetting(Vector p_backingStoreIds)
    {
        if (p_backingStoreIds == null)
        {
            m_backingStoreIds = new Vector();
        }
        else
        {
            m_backingStoreIds = p_backingStoreIds;
        }
        // convert whatever type Number it is to a Long
        int size = p_backingStoreIds.size();
        for (int i = 0; i < size; i++)
        {
            Long backingStoreId = new Long(
                    ((Number) p_backingStoreIds.elementAt(i)).longValue());
            m_backingStoreIds.addElement(backingStoreId);
        }
    }

    /** Returns a string representation of the object */
    public String toString()
    {
        return toDebugString();
        // return m_name;
    }

    /**
     * Return a string representation of the object for debugging purposes.
     * 
     * @return a string representation of the object for debugging purposes.
     */
    public String toDebugString()
    {
        return super.toString()
                + " m_companyId"
                + m_companyId
                + "m_name="
                + m_name
                + "m_description = "
                + m_description
                + "m_exportPort = "
                + m_exportPort
                + "m_importPort = "
                + m_importPort
                + "m_proxyPort = "
                + m_proxyPort
                + "m_operatingSystem = "
                + m_operatingSystem
                + "m_home = "
                + m_home
                + "m_user = "
                + m_user
                + "m_type = "
                + m_type
                + "m_mount = "
                + m_mount
                + "m_allowLocaleSpecificReimport = "
                + m_allowLocaleSpecificReimport
                + " m_backingStoreIds="
                + (m_backingStoreIds == null ? "null" : m_backingStoreIds
                        .toString());
    }

    // Below method for hibernate
    public String getOperatingSystem()
    {
        return m_operatingSystem;
    }

    public void setOperatingSystem(String system)
    {
        m_operatingSystem = system;
    }

    public boolean isAllowLocaleSpecificReimport()
    {
        return m_allowLocaleSpecificReimport;
    }

    public void setAllowLocaleSpecificReimport(boolean localeSpecificReimport)
    {
        m_allowLocaleSpecificReimport = localeSpecificReimport;
    }

    public Set getBackingStores()
    {
        HashSet stores = new HashSet();
        if (m_backingStores != null)
        {
            stores = new HashSet(m_backingStores);
        }
        return stores;
    }

    public void setBackingStores(Vector stores)
    {
        if (stores != null)
        {
            int size = stores.size();
            for (int i = 0; i < size; i++)
            {
                BackingStoreImpl backingStore = (BackingStoreImpl) stores
                        .elementAt(i);
                Long backingStoreId = backingStore.getIdAsLong();
                m_backingStoreIds.addElement(backingStoreId);
            }
        }
        m_backingStores = stores;
    }

    public void setBackingStores(Set stores)
    {
        setBackingStores(new Vector(stores));
    }
}
