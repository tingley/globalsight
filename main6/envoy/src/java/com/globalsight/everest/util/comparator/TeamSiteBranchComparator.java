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
package com.globalsight.everest.util.comparator;

import java.util.Locale;

import com.globalsight.cxe.entity.cms.teamsite.server.TeamSiteServer;
import com.globalsight.cxe.entity.cms.teamsite.store.BackingStore;
import com.globalsight.cxe.entity.cms.teamsitedbmgr.TeamSiteBranch;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.util.GlobalSightLocale;

/**
 * This class can be used to compare TeamSiteBranch objects
 */
public class TeamSiteBranchComparator extends StringComparator
{
    // types of comparison
    public static final int BRANCH = 0;
    public static final int TARG_LOCALE = 1;
    public static final int TARG_BRANCH = 2;
    public static final int SERVER = 3;
    public static final int STORE = 4;
    public static final int COMPANY = 5;

    public TeamSiteBranchComparator(int p_type, Locale p_locale)
    {
        super(p_type, p_locale);
    }

    public TeamSiteBranchComparator(Locale p_locale)
    {
        super(p_locale);
    }

    /**
     * Performs a comparison of two Tm objects.
     */
    public int compare(java.lang.Object p_A, java.lang.Object p_B)
    {
        TeamSiteBranch a = (TeamSiteBranch) p_A;
        TeamSiteBranch b = (TeamSiteBranch) p_B;

        String aValue;
        String bValue;
        int rv = 0;

        try
        {

            switch (m_type)
            {
                default:
                case BRANCH:
                    aValue = a.toString();
                    bValue = b.toString();
                    rv = this.compareStrings(aValue, bValue);
                    break;
                case TARG_LOCALE:
                    GlobalSightLocale targ_locale_a = ServerProxy
                            .getLocaleManager().getLocaleById(
                                    a.getBranchLanguage());
                    GlobalSightLocale targ_locale_b = ServerProxy
                            .getLocaleManager().getLocaleById(
                                    b.getBranchLanguage());
                    aValue = targ_locale_a.getDisplayLanguage() + " / "
                            + targ_locale_a.getDisplayCountry();
                    bValue = targ_locale_b.getDisplayLanguage() + " / "
                            + targ_locale_b.getDisplayCountry();
                    rv = this.compareStrings(aValue, bValue);
                    break;
                case TARG_BRANCH:
                    aValue = a.getBranchTarget();
                    bValue = b.getBranchTarget();
                    rv = this.compareStrings(aValue, bValue);
                    break;
                case SERVER:
                    TeamSiteServer ts_a = ServerProxy
                            .getTeamSiteServerPersistenceManager()
                            .readTeamSiteServer(a.getTeamSiteServerId());
                    TeamSiteServer ts_b = ServerProxy
                            .getTeamSiteServerPersistenceManager()
                            .readTeamSiteServer(b.getTeamSiteServerId());
                    aValue = ts_a.getName();
                    bValue = ts_b.getName();
                    rv = this.compareStrings(aValue, bValue);
                    break;
                case STORE:
                    BackingStore store_a = ServerProxy
                            .getTeamSiteServerPersistenceManager()
                            .readBackingStore(a.getTeamSiteStoreId());
                    BackingStore store_b = ServerProxy
                            .getTeamSiteServerPersistenceManager()
                            .readBackingStore(b.getTeamSiteStoreId());
                    aValue = store_a.getName();
                    bValue = store_b.getName();
                    rv = this.compareStrings(aValue, bValue);
                    break;
                case COMPANY:
                    ts_a = ServerProxy.getTeamSiteServerPersistenceManager()
                            .readTeamSiteServer(a.getTeamSiteServerId());
                    ts_b = ServerProxy.getTeamSiteServerPersistenceManager()
                            .readTeamSiteServer(b.getTeamSiteServerId());
                    aValue = String.valueOf(ts_a.getCompanyId());
                    bValue = String.valueOf(ts_b.getCompanyId());
                    rv = this.compareStrings(aValue, bValue);
                    break;
            }
        }
        catch (Exception e)
        {
        }
        return rv;
    }
}
