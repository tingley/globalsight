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

package com.globalsight.ling.sgml.sgmlrules;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.globalsight.ling.common.XmlEntities;
import com.globalsight.ling.sgml.GlobalSightDtd;
import com.globalsight.ling.sgml.GlobalSightDtdParser;
import com.globalsight.ling.sgml.catalog.Catalog;
import com.globalsight.ling.sgml.catalog.CatalogEntry;
import com.globalsight.ling.sgml.catalog.CatalogException;
import com.globalsight.ling.sgml.sgmldtd.DtdParserAdapter;
import com.globalsight.util.AmbFileStoragePathUtils;
import com.globalsight.util.SortUtil;

public class SgmlRulesManager
{
    static private final Logger CATEGORY = Logger
            .getLogger(SgmlRulesManager.class);

    static private XmlEntities s_codec = new XmlEntities();
    static private Catalog s_catalog;
    static private ArrayList s_rules = new ArrayList();

    static
    {
        initCatalog();
    }

    //
    // Public Methods
    //

    static synchronized private void initCatalog()
    {
        if (s_catalog == null)
        {
            s_catalog = new Catalog();

            try
            {
                // s_catalog.parseCatalog(Catalog.DEFAULT_CATALOG);
                s_catalog
                        .parseCatalog(AmbFileStoragePathUtils.CATALOG_SUB_DIRECTORY);

                // Create rules for all the PUBLIC ids in the catalog.
                Vector entries = s_catalog.getCatalogEntries();

                for (int i = 0, max = entries.size(); i < max; i++)
                {
                    CatalogEntry entry = (CatalogEntry) entries.elementAt(i);

                    if (entry.entryType() == CatalogEntry.PUBLIC)
                    {
                        SgmlRule rule = new SgmlRule();

                        rule.setFilename(entry.formalSystemIdentifier());
                        rule.setPublicId(entry.publicId());
                        rule.setSystemId(entry.originalSystemId());

                        // Load the DTD to know if we can parse it.
                        // m_exception will tell.
                        loadDtd(rule);

                        // Discard the DTD to conserve memory (this
                        // object keeps data in static members).
                        rule.setDtd(null);

                        s_rules.add(rule);
                    }
                }
            }
            catch (Exception ex)
            {
                CATEGORY.error("error initializing SGML catalog", ex);
            }
        }
    }

    static synchronized public ArrayList getDtds()
    {
        // return cloned list to protect from synchronous modifications
        ArrayList result = new ArrayList(s_rules);

        SortUtil.sort(result);

        return result;
    }

    static public String getDtdsAsXml()
    {
        StringBuffer result = new StringBuffer();

        // this call is synchronized
        ArrayList dtds = getDtds();

        result.append("<dtds>\n");

        for (int i = 0, max = dtds.size(); i < max; i++)
        {
            SgmlRule rule = (SgmlRule) dtds.get(i);

            result.append("<dtd>\n");
            result.append("<publicid>");
            result.append(s_codec.encodeStringBasic(rule.getPublicId()));
            result.append("</publicid>\n");
            result.append("<systemid>");
            result.append(s_codec.encodeStringBasic(rule.getSystemId()));
            result.append("</systemid>\n");
            result.append("<status>");
            result.append(rule.getException() == null ? "ok" : s_codec
                    .encodeStringBasic(rule.getException().toString()));
            result.append("</status>\n");
            result.append("</dtd>\n");
        }

        result.append("</dtds>\n");

        return result.toString();
    }

    /** Loads the SGML extractor properties for the DTD */
    static synchronized public SgmlRule loadSgmlRule(String p_publicId)
    {
        SgmlRule rule = getRule(p_publicId);

        if (rule != null)
        {
            loadDtd(rule);
            loadPropertyFile(rule);
        }

        return rule;
    }

    static private void loadDtd(SgmlRule p_rule)
    {
        // Don't parse DTDs without a dtd file.
        if (p_rule.getSystemId().length() == 0)
        {
            return;
        }

        try
        {
            GlobalSightDtdParser parser = new DtdParserAdapter();
            // new com.globalsight.ling.sgml.dtd.DTDParser();
            // new com.globalsight.ling.sgml.dtd2.DTDParser();

            parser.setCatalog(s_catalog);

            GlobalSightDtd dtd = parser.parseDtd(new URL(p_rule.getFilename()));

            p_rule.setDtd(dtd);
            p_rule.setException(null);
        }
        catch (Exception ex)
        {
            p_rule.setDtd(null);
            p_rule.setException(ex);
        }
    }

    /** Saves the SGML extractor properties for the DTD */
    static synchronized public void saveSgmlRule(SgmlRule p_rule)
    {
        savePropertyFile(p_rule);
    }

    static synchronized private SgmlRule getRule(String p_publicId)
    {
        for (int i = 0, max = s_rules.size(); i < max; i++)
        {
            SgmlRule rule = (SgmlRule) s_rules.get(i);

            if (rule.getPublicId().equalsIgnoreCase(p_publicId))
            {
                return rule;
            }
        }

        return null;
    }

    static public synchronized void addDTD(String p_publicId, String p_systemId)
            throws CatalogException, Exception
    {
        try
        {
            s_catalog.addPublicId(p_publicId, p_systemId);
            // s_catalog.writeCatalog(Catalog.DEFAULT_CATALOG);
            s_catalog
                    .writeCatalog(AmbFileStoragePathUtils.CATALOG_SUB_DIRECTORY);

            // if the catalog update succeeded, augment the in-memory list

            SgmlRule rule = new SgmlRule();

            rule.setFilename(s_catalog.resolvePublic(p_publicId, null));
            rule.setPublicId(p_publicId);
            rule.setSystemId(p_systemId);

            s_rules.add(rule);

            // finally parse the DTD to set the error status correctly
            loadDtd(rule);
            rule.setDtd(null);

            CATEGORY.info("Added SGML DTD `" + p_publicId + "' (system id `"
                    + p_systemId + "')");
        }
        catch (CatalogException ex)
        {
            // "PUBLIC ID ... already exists"
            CATEGORY.error(ex.getMessage(), ex);

            throw ex;
        }
        catch (Exception ex)
        {
            CATEGORY.error("cannot add public id `" + p_publicId
                    + "' and system id `" + p_systemId + "' to catalog", ex);

            throw ex;
        }
    }

    static public synchronized void removeDTD(String p_publicId)
    {
        SgmlRule rule = null;

        try
        {
            s_catalog.removePublicId(p_publicId);
            // s_catalog.writeCatalog(Catalog.DEFAULT_CATALOG);
            s_catalog
                    .writeCatalog(AmbFileStoragePathUtils.CATALOG_SUB_DIRECTORY);

            // if the catalog update succeeded, modify the in-memory list
            // and remove dtd and property files

            for (int i = 0, max = s_rules.size(); i < max; i++)
            {
                rule = (SgmlRule) s_rules.get(i);

                if (rule.getPublicId().equals(p_publicId))
                {
                    s_rules.remove(i);
                    deletePropertyFile(rule);

                    // If this public ID was the last reference to the
                    // DTD file, and if there is a file, delete it.
                    String systemId = rule.getSystemId();

                    if (systemId.length() > 0 && !hasSystemReference(systemId))
                    {
                        removeDTDFile(rule);
                    }

                    break;
                }
            }

            CATEGORY.info("Removed SGML DTD `" + p_publicId + "'");
        }
        catch (Exception ex)
        {
            CATEGORY.error("cannot remove public id `" + p_publicId
                    + "' from catalog", ex);
        }
    }

    /**
     * Returns true if the SYSTEM ID (a filename) is still used by public ids in
     * the catalog. We don't search the catalog to find out but scan the
     * in-memory list of SgmlRule objects.
     */
    static boolean hasSystemReference(String p_systemId)
    {
        for (int i = 0, max = s_rules.size(); i < max; i++)
        {
            SgmlRule rule = (SgmlRule) s_rules.get(i);

            if (rule.getSystemId().equalsIgnoreCase(p_systemId))
            {
                return true;
            }
        }

        return false;
    }

    static void removeDTDFile(SgmlRule p_rule)
    {
        // new File(Catalog.getBaseDirectory() + "/" +
        // p_rule.getSystemId()).delete();
        new File(AmbFileStoragePathUtils.getCatalogDir(), p_rule.getSystemId())
                .delete();
    }

    //
    // Property File Handling
    //

    static private void loadPropertyFile(SgmlRule p_rule)
    {
        String filename = getPropertyFilename(p_rule);

        try
        {
            InputStream is = new FileInputStream(filename);
            Properties props = new Properties();
            props.load(is);
            is.close();

            p_rule.setRules(props);
        }
        catch (IOException ex)
        {
            // file doesn't exist
            // initialize with defaults
            p_rule.initData();
        }
    }

    static private void savePropertyFile(SgmlRule p_rule)
    {
        String filename = getPropertyFilename(p_rule);

        Properties props = p_rule.getRules();

        try
        {
            OutputStream os = new FileOutputStream(filename);
            props.store(os,
                    "THIS FILE IS AUTOMATICALLY GENERATED -- DO NOT EDIT");
            os.close();
        }
        catch (Exception ex)
        {
            CATEGORY.error(
                    "cannot save properties for public id `"
                            + p_rule.getPublicId() + "'", ex);
        }
    }

    static private void deletePropertyFile(SgmlRule p_rule)
    {
        new File(getPropertyFilename(p_rule)).delete();
    }

    static private String getPropertyFilename(SgmlRule p_rule)
    {
        // return Catalog.getBaseDirectory() + "/" +
        // makeSafeName(p_rule.getPublicId() + ".properties");
        return AmbFileStoragePathUtils.getCatalogDir() + "/"
                + makeSafeName(p_rule.getPublicId() + ".properties");
    }

    static private String makeSafeName(String p_arg)
    {
        StringBuffer result = new StringBuffer(p_arg.length());

        for (int i = 0; i < p_arg.length(); i++)
        {
            char c = p_arg.charAt(i);

            if (c == '\\' || c == '/' || c == ':' || c == '|' || c == '\''
                    || c == '"' || c == '<' || c == '>')
            {
                result.append('[').append(Integer.toHexString(c)).append(']');
            }
            else
            {
                result.append(c);
            }
        }

        return result.toString();
    }
}
