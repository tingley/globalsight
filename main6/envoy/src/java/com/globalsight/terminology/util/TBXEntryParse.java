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
import com.globalsight.terminology.EntryUtils;
import com.globalsight.terminology.Termbase;
import com.globalsight.terminology.TermbaseException;
import com.globalsight.terminology.java.TbConcept;
import com.globalsight.terminology.java.TbLanguage;
import com.globalsight.terminology.java.TbTerm;
import com.globalsight.util.SessionInfo;
import com.globalsight.util.edit.EditUtil;

public class TBXEntryParse implements IEntryParse
{
    @Override
    public String getLanNodeName()
    {
        return "langSet";
    }

    @Override
    public String[] getTermNodeNames()
    {
        String[] termNodeNames = {"ntig/termGrp", "tig"};
        return termNodeNames;
    }

    @Override
    public TbConcept getConceptFromXml(Element root, SessionInfo p_session)
    {
        String domain = "*unknown*";
        String project = "*unknown*";
        String status = "proposed";
        StringBuffer xml = new StringBuffer();

        // Prepare concept nodes for storage
        for (Iterator it = root.elementIterator(); it.hasNext();)
        {
            Element elmt = (Element) it.next();

            if (!elmt.getName().equals("langSet"))
            {
                String tmp = elmt.asXML();
                tmp = tmp.replace("\\", "\\\\");
                xml.append(tmp);
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
        String langLocale = langGrp.attribute("lang").getText();
        String langName = EntryUtils.getLanguageName(langLocale);
        StringBuffer xml = new StringBuffer();

        // Prepare language nodes for storage
        for (Iterator it = langGrp.elementIterator(); it.hasNext();)
        {
            Element elmt = (Element) it.next();

            if (!elmt.getName().equals("ntig") && !elmt.getName().equals("tig"))
            {
                String tmp = elmt.asXML();
                tmp = tmp.replace("\\", "\\\\");
                xml.append(tmp);
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
        String term;
        String termType = "*unknown*";
        String termStatus = "*unknown*";
        String sortKey;
        StringBuffer xml = new StringBuffer();

        // Extract term and compute binary sortkey
        term = termGrp.valueOf("term");
        sortKey = SqlUtil.toHex(Sortkey.getSortkey(term, tlan.getLocal()),
                Termbase.MAX_SORTKEY_LEN);

        // Limit size of data
        term = EditUtil.truncateUTF8Len(term, Termbase.MAX_TERM_LEN);

        for (Iterator it2 = termGrp.elementIterator(); it2.hasNext();)
        {
            Element elmt = (Element) it2.next();
            String name = elmt.getName();

            if (!name.equals("term"))
            {
                String tmp = elmt.asXML();
                tmp = tmp.replace("\\", "\\\\");
                xml.append(tmp);
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
        tbterm.setXml(xml.toString());
        tbterm.setTbConcept(tlan.getConcept());
        tbterm.setTbid(tlan.getConcept().getTermbase().getId());

        return tbterm;
    }

    /**
     * Get TBX files entry id
     * 
     * @param entry
     * @return
     * @throws TermbaseException
     */
    public String getConceptId(Entry entry) throws TermbaseException
    {
        Document dom = entry.getDom();
        Element root = dom.getRootElement();

        if (root.attribute("id") == null
                || root.attribute("id").getText().length() == 0)
        {
            return null;
        }
        else
        {
            return root.attribute("id").getText();
        }
    }
}
