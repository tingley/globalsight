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

package com.globalsight.terminology.util;

import java.sql.Timestamp;
import java.util.Iterator;

import org.dom4j.Document;
import org.dom4j.Element;

import com.globalsight.terminology.Entry;
import com.globalsight.terminology.Termbase;
import com.globalsight.terminology.TermbaseException;
import com.globalsight.terminology.java.TbConcept;
import com.globalsight.terminology.java.TbLanguage;
import com.globalsight.terminology.java.TbTerm;
import com.globalsight.util.SessionInfo;
import com.globalsight.util.edit.EditUtil;

public class GSEntryParse implements IEntryParse
{
    @Override
    public String getLanNodeName()
    {
        return "/conceptGrp/languageGrp";
    }

    @Override
    public String[] getTermNodeNames()
    {
        String[] termNodeNames = {"termGrp"};
        return termNodeNames;
    }

    @Override
    public TbConcept getConceptFromXml(Element root, SessionInfo p_session)
    {
        String value;
        String domain = "*unknown*";
        String project = "*unknown*";
        String status = "proposed";
        StringBuffer xml = new StringBuffer();

        // Extract values of indexed concept attributes.
        // TODO: this has to use the TbDefinition.
        if ((value = root
                .valueOf("/conceptGrp/descripGrp/descrip[@type='domain']")) != null
                && value.length() > 0)
        {
            domain = value;
        }
        else if ((value = root.valueOf("/conceptGrp/descrip[@type='domain']")) != null
                && value.length() > 0)
        {
            domain = value;
        }

        if ((value = root
                .valueOf("/conceptGrp/descripGrp/descrip[@type='project']")) != null
                && value.length() > 0)
        {
            project = value;
        }
        else if ((value = root.valueOf("/conceptGrp/descrip[@type='project']")) != null
                && value.length() > 0)
        {
            project = value;
        }

        if ((value = root
                .valueOf("/conceptGrp/descripGrp/descrip[@type='status']")) != null
                && value.length() > 0)
        {
            status = value;
        }
        else if ((value = root.valueOf("/conceptGrp/descrip[@type='status']")) != null
                && value.length() > 0)
        {
            status = value;
        }

        // Prepare concept nodes for storage
        for (Iterator it = root.elementIterator(); it.hasNext();)
        {
            Element elmt = (Element) it.next();

            if (!elmt.getName().equals("languageGrp"))
            {
                xml.append(elmt.asXML());
            }
        }

        TbConcept tc = new TbConcept();
        tc.setDomain(domain);
        tc.setStatus(status);
        tc.setProject(project);
        tc.setXml(xml.toString());
        tc.setCreationBy(p_session.getUserName());
        Timestamp ts = new Timestamp(p_session.getTimestamp().getTime());
        tc.setCreationDate(ts);

        return tc;
    }

    @Override
    public TbLanguage getTbLanguaeFromXml(TbConcept tc, Element langGrp,
            SessionInfo p_session)
    {
        String langName = langGrp.valueOf("language/@name");
        String langLocale = langGrp.valueOf("language/@locale");
        StringBuffer xml = new StringBuffer();

        // Prepare language nodes for storage
        for (Iterator it = langGrp.elementIterator(); it.hasNext();)
        {
            Element elmt = (Element) it.next();

            if (!elmt.getName().equals("language")
                    && !elmt.getName().equals("termGrp"))
            {
                xml.append(elmt.asXML());
            }
        }

        String lxml = xml.toString();

        TbLanguage tlan = new TbLanguage();
        tlan.setTbid(tc.getTermbase().getId());
        tlan.setConcept(tc);
        tlan.setLocal(langLocale);
        tlan.setName(langName);
        tlan.setXml(lxml);

        return tlan;
    }

    @Override
    public TbTerm getTbTermFromXml(TbLanguage tlan, Element termGrp,
            SessionInfo p_session)
    {
        String value;
        String term;
        String termType = "*unknown*";
        String termStatus = "*unknown*";
        String sortKey;
        StringBuffer xml1 = new StringBuffer();

        // Extract term and compute binary sortkey
        term = termGrp.valueOf("term");
        sortKey = SqlUtil.toHex(Sortkey.getSortkey(term, tlan.getLocal()),
                Termbase.MAX_SORTKEY_LEN);

        // Limit size of data
        term = EditUtil.truncateUTF8Len(term, Termbase.MAX_TERM_LEN);

        // Extract term and values of indexed term attributes
        if ((value = termGrp.valueOf(".//descrip[@type='type']")) != null
                && value.length() > 0)
        {
            termType = value;
        }

        if ((value = termGrp.valueOf(".//descrip[@type='status']")) != null
                && value.length() > 0)
        {
            termStatus = value;
        }

        for (Iterator it = termGrp.elementIterator(); it.hasNext();)
        {
            Element elmt = (Element) it.next();
            String name = elmt.getName();

            if (!name.equals("term"))
            {
                xml1.append(elmt.asXML());
            }
        }

        Timestamp ts = new Timestamp(p_session.getTimestamp().getTime());
        TbTerm tbterm = new TbTerm();
        tbterm.setCreationBy(p_session.getUserName());
        tbterm.setCreationDate(ts);
        tbterm.setLanguage(tlan.getName());
        tbterm.setModifyBy(p_session.getUserName());
        tbterm.setModifyDate(ts);
        tbterm.setSortKey(sortKey);
        tbterm.setStatus(termStatus);
        tbterm.setTbLanguage(tlan);
        tbterm.setTermContent(term);
        tbterm.setType(termType);
        tbterm.setXml(xml1.toString());
        tbterm.setTbConcept(tlan.getConcept());
        tbterm.setTbid(tlan.getConcept().getTermbase().getId());

        return tbterm;
    }

    /**
     * Gets the entry's concept ID.
     * 
     * @return 0 if the entry has no ID yet, else a positive number.
     */
    public String getConceptId(Entry p_entry) throws TermbaseException
    {
        Document dom = p_entry.getDom();
        Element root = dom.getRootElement();
        Element concept = root.element("concept");

        if (concept == null || concept.getText().length() == 0)
        {
            return null;
        }
        else
        {
            return concept.getText();
        }
    }

}
