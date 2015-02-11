package com.globalsight.everest.util.system.migration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.globalsight.cxe.entity.filterconfiguration.BaseFilter;
import com.globalsight.cxe.entity.filterconfiguration.BaseFilterManager;
import com.globalsight.cxe.entity.filterconfiguration.BaseFilterMapping;
import com.globalsight.cxe.entity.filterconfiguration.BaseFilterParser;
import com.globalsight.cxe.entity.filterconfiguration.Filter;
import com.globalsight.cxe.entity.filterconfiguration.FilterHelper;
import com.globalsight.cxe.entity.filterconfiguration.InternalItem;
import com.globalsight.cxe.entity.filterconfiguration.InternalText;
import com.globalsight.cxe.entity.filterconfiguration.JavaPropertiesFilter;
import com.globalsight.cxe.entity.filterconfiguration.PropertiesInternalText;
import com.globalsight.everest.company.Company;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.persistence.hibernate.HibernateUtil;

/**
 * GlobalSight 8.2 : migrate internal text in properties filter to internal text post filter
 * @author Wayne
 *
 */
public class Migrate820InternalText extends MigrateObj
{

    private static String keyname = "doPropertyInternalTextMigration";
    
    @Override
    public boolean checkIfDoMigration()
    {
        boolean doMigrationForPropertyInternalText = checkIfDoMigration(keyname);
        
        return doMigrationForPropertyInternalText;
    }

    @Override
    public void doMigration() throws Exception
    {
        Collection<Company> companies = ServerProxy.getJobHandler()
                .getAllCompanies();
        List<Long> companyIds = new ArrayList<Long>();
        for (Company c : companies)
        {
            companyIds.add(c.getId());
        }

        doMigrationForPropertiesInternalText(companyIds, keyname);
    }
    
    /**
     * Migrate java properties internal text to internal text post filter
     * 
     * @throws Exception
     */
    private void doMigrationForPropertiesInternalText(List<Long> companyIds,
            String keyname) throws Exception
    {
        org.hibernate.Transaction tran = null;
        CATEGORY.info("Start migration from Internal Text of JavaProperties Filter to Internal Text post-filter");
        try
        {
            tran = HibernateUtil.getTransaction();
            tran.begin();
            for (Long cid : companyIds)
            {
                JavaPropertiesFilter jpf = new JavaPropertiesFilter();
                ArrayList<Filter> allJpfs = jpf.getFilters(cid);
                for (Filter filter : allJpfs)
                {
                    JavaPropertiesFilter jpFilter = (JavaPropertiesFilter) filter;

                    // ignore this filter if mapping exits, internal text post
                    // filter is set
                    BaseFilterMapping mapping = BaseFilterManager
                            .getBaseFilterMapping(jpFilter.getId(),
                                    jpFilter.getFilterTableName());
                    if (mapping != null)
                    {
                        continue;
                    }

                    // get original internal text
                    PropertiesInternalText proIT = jpFilter.getInternalRegexs();
                    List<InternalText> its = new ArrayList<InternalText>();
                    if (proIT != null)
                    {
                        List<InternalItem> iis = proIT.getItems();
                        for (InternalItem internalItem : iis)
                        {
                            if (internalItem.getIsSelected())
                            {
                                InternalText it = new InternalText();
                                it.setName(internalItem.getContent());
                                it.setRE(internalItem.getIsRegex());
                                its.add(it);
                            }
                        }
                    }

                    if (its.size() > 0)
                    {
                        // add new BaseFitler for internal texts
                        BaseFilter bf = new BaseFilter();
                        String jpName = jpFilter.getFilterName();
                        String newFilterName = "ITF_" + jpName;

                        // limit the max length for filter name to 40
                        if (jpName.length() + 4 > 40)
                        {
                            String jpId = "" + jpFilter.getId();
                            int subLen = 40 - 4 - 3 - jpId.length();
                            String subJpName = jpName.substring(0, subLen);
                            newFilterName = "ITF_" + subJpName + "..." + jpId;
                        }

                        bf.setCompanyId(jpFilter.getCompanyId());
                        bf.setFilterDescription("Internal Text post-filter for Java Properties Filter : "
                                + jpName);
                        bf.setFilterName(newFilterName);

                        org.json.JSONArray jsonArray = new JSONArray();
                        for (InternalText internalText : its)
                        {
                            org.json.JSONObject jsonObj = new JSONObject();
                            jsonObj.put("aName", internalText.getName());
                            jsonObj.put("isRE",
                                    ("" + internalText.isRE()).toLowerCase());
                            jsonObj.put("enable", "true");
                            jsonArray.put(jsonObj);
                        }

                        String configxml = BaseFilterParser.toXml(jsonArray);
                        bf.setConfigXml(configxml);
                        long bfId = FilterHelper.saveFilter(bf);

                        // add mapping
                        BaseFilterManager
                                .saveBaseFilterMapping(bfId, jpFilter.getId(),
                                        jpFilter.getFilterTableName());

                        CATEGORY.info("Add Internal Text Filter : "
                                + newFilterName);
                    }
                }
            }

            updateMigrationKey(keyname);
            tran.commit();
        }
        catch (Exception e)
        {
            throw e;
        }
        CATEGORY.info("Finish migration from Internal Text of JavaProperties Filter to Internal Text post-filter");
    }
}
