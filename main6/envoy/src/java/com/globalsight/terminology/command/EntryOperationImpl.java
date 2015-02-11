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

package com.globalsight.terminology.command;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.globalsight.everest.util.comparator.TbLanguageComparator;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.terminology.ConceptHelper;
import com.globalsight.terminology.Definition;
import com.globalsight.terminology.Entry;
import com.globalsight.terminology.EntryUtils;
import com.globalsight.terminology.LockInfo;
import com.globalsight.terminology.TermbaseException;
import com.globalsight.terminology.ValidationInfo;
import com.globalsight.terminology.Validator;
import com.globalsight.terminology.java.TbConcept;
import com.globalsight.terminology.java.TbLanguage;
import com.globalsight.terminology.java.TbTerm;
import com.globalsight.terminology.util.SqlUtil;
import com.globalsight.util.SessionInfo;
import com.globalsight.util.SortUtil;
import com.globalsight.util.StringUtil;
import com.globalsight.util.edit.EditUtil;

public class EntryOperationImpl implements EntryOperation
{
    /*
     * If the termbase has huge data, use hibernate cascade query will get low
     * efficiency, so here use jdbc just for export.
     */
    public String getEntryForExport(long tbid, long p_entryId, long p_termId,
            String p_srcLang, String p_trgLang, SessionInfo p_session)
            throws TermbaseException
    {

        StringBuffer result = new StringBuffer();

        Connection conn = null;
        Statement stmt = null;
        ResultSet rset = null;

        try
        {
            conn = SqlUtil.hireConnection();
            conn.setAutoCommit(false);

            // Retrieve concept level data
            stmt = conn.createStatement();

            rset = stmt.executeQuery("select XML from TB_CONCEPT "
                    + "where Cid=" + p_entryId);

            if (!rset.next())
            {
                conn.commit();

                if (CATEGORY.isDebugEnabled())
                {
                    CATEGORY.debug("Get entry " + p_entryId
                            + ": entry does not exist.");
                }

                return "";
            }

            result.append("<conceptGrp>");
            result.append(SqlUtil.readClob(rset, "XML"));

            // Retrieve languages and terms
            rset = stmt
                    .executeQuery("select l.LID, l.NAME, l.LOCALE, l.XML L_XML,"
                            + "  t.TID, t.TERM, t.XML T_XML "
                            + "from TB_LANGUAGE l, TB_TERM t "
                            + "where "
                            + "l.Cid  ="
                            + p_entryId
                            + " and l.TBID="
                            + tbid
                            + " and t.Cid ="
                            + p_entryId
                            + " and t.TBID="
                            + tbid + getSqlInLanguageString(p_srcLang)
                            + " and t.Lid = l.Lid " + "order by l.lid");

            long previousLid = 0;
            int langNum =0;
            while (rset.next())
            {
                long lid = rset.getLong("LID");
                long tid = rset.getLong("TID");
                String langName = rset.getString("NAME");
                String langLocale = rset.getString("LOCALE");
                String term = rset.getString("TERM");
                String txml = SqlUtil.readClob(rset, "T_XML");

                // start a new languageGrp for a new language
                if (lid != previousLid)
                {
                	langNum++;
                    if (previousLid != 0)
                    {
                        result.append("</languageGrp>");
                    }

                    result.append("<languageGrp>");
                    result.append("<language name=\"");
                    result.append(EditUtil.encodeXmlEntities(langName));
                    result.append("\" locale=\"");
                    result.append(EditUtil.encodeXmlEntities(langLocale));
                    result.append("\"");

                    if (langName.equals(p_srcLang))
                    {
                        result.append(" source-lang=\"true\"");
                    }
                    else if (langName.equals(p_trgLang))
                    {
                        result.append(" target-lang=\"true\"");
                    }

                    result.append("/>");

                    String lxml = SqlUtil.readClob(rset, "L_XML");

                    result.append(lxml);
                }

                result.append("<termGrp>");
                result.append("<term");

                if (tid == p_termId)
                {
                    result.append(" search-term=\"true\"");
                }

                result.append(">");
                result.append(EditUtil.encodeXmlEntities(term));
                result.append("</term>");
                result.append(txml);
                result.append("</termGrp>");

                previousLid = lid;
            }
            if(langNum <2)
            	return "";

            // there could be entries with no languages at all??
            if (previousLid != 0)
            {
                result.append("</languageGrp>");
            }

            result.append("</conceptGrp>");

            if (CATEGORY.isDebugEnabled())
            {
                CATEGORY.debug("Get entry " + p_entryId + ": "
                        + result.toString());
            }

            return result.toString();
        }
        catch (Exception e)
        {
            try
            {
                conn.rollback();
            }
            catch (Exception ex)
            {
            }
        }
        finally
        {
            try
            {
                if (rset != null)
                    rset.close();
                if (stmt != null)
                    stmt.close();
            }
            catch (Throwable t)
            {
            }

            SqlUtil.fireConnection(conn);
        }

        return "";
    }

    /**
     * <p>
     * Retrieves an entry for export as xml string conforming to the Entry
     * Structure Schema, with source/target language group and source term
     * specially marked for easy formatting in the viewer.
     * </p>
     * 
     * @return the empty string if the entry does not exist, else an XML string
     *         with root "conceptGrp".
     */
    public String getTbxEntryForExport(long tbid, long p_entryId,
            long p_termId, String p_srcLang, String p_trgLang,
            SessionInfo p_session) throws TermbaseException
    {
        StringBuffer result = new StringBuffer();

        Connection conn = null;
        Statement stmt = null;
        ResultSet rset = null;

        try
        {
            conn = SqlUtil.hireConnection();
            conn.setAutoCommit(false);

            // Retrieve concept level data
            stmt = conn.createStatement();
            result.append("<termEntry id=\"").append(p_entryId).append("\">");

            // Retrieve languages and terms
            rset = stmt
                    .executeQuery("select l.LID, l.NAME, l.LOCALE, l.XML L_XML, "
                            + "  t.TID, t.TERM, t.XML T_XML "
                            + "from TB_LANGUAGE l, TB_TERM t "
                            + "where "
                            + " l.TBID="
                            + tbid
                            + " and l.Cid  ="
                            + p_entryId
                            + " and t.TBID="
                            + tbid
                            + " and t.Cid ="
                            + p_entryId
                            + getSqlInLanguageString(p_srcLang)
                            + " and t.Lid = l.Lid "
                            + "order by l.lid");

            long previousLid = 0;

            int langNum= 0;
            while (rset.next())
            {
                long lid = rset.getLong("LID");
                String langLocale = rset.getString("LOCALE");
                String term = rset.getString("TERM");

                // start a new languageGrp for a new language
                if (lid != previousLid)
                {
                	langNum++;
                    if (previousLid != 0)
                    {
                        result.append("</langSet>");
                    }

                    result.append("<langSet").append(" xml:lang=\"")
                            .append(langLocale).append("\">");
                }

                result.append("<ntig>").append("<termGrp>").append("<term>");

                result.append(EditUtil.encodeXmlEntities(term));
                result.append("</term>");
                result.append("</termGrp>").append("</ntig>");

                previousLid = lid;
            }
            if(langNum <2)
            	return "";
            // there could be entries with no languages at all??
            if (previousLid != 0)
            {
                result.append("</langSet>");
            }

            result.append("</termEntry>");

            conn.commit();

            if (CATEGORY.isDebugEnabled())
            {
                CATEGORY.debug("Get entry " + p_entryId + ": "
                        + result.toString());
            }

            return result.toString();
        }
        catch (Exception e)
        {
            try
            {
                conn.rollback();
            }
            catch (Exception ex)
            { /* ignore */
            }
            throw new TermbaseException(e);
        }
        finally
        {
            try
            {
                if (rset != null)
                    rset.close();
                if (stmt != null)
                    stmt.close();
            }
            catch (Throwable t)
            { /* ignore */
            }

            SqlUtil.fireConnection(conn);
        }
    }

    private String getSqlInLanguageString(String p_Langs)
    {
    	 String sql = "";
         if(StringUtil.isNotEmpty(p_Langs))
         {
         	String[] langs = p_Langs.split(",");
         	for(String lang: langs)
         	{
         		sql = sql + "'" + lang + "',";
         	}
         	
         	sql = " and t.lang_name in ( " + sql.substring(0,sql.length() - 1) + ") ";
         }
         
         return sql;
    }
    
    private String getEntry(long p_entryId, long p_termId, String p_srcLang,
            String p_trgLang, SessionInfo p_session, boolean isForBrowser)
            throws TermbaseException
    {
        StringBuffer result = new StringBuffer();

        try
        {
            TbConcept tConcept = HibernateUtil.get(TbConcept.class, p_entryId);
            result.append("<conceptGrp>");
            String conceptXml = ConceptHelper.fixConceptXml(tConcept.getXml(),
                    p_entryId);
            result.append(fixConceptXml(conceptXml));

            Iterator ite = tConcept.getLanguages().iterator();

            // fix for the sort order for the entries, related to GBS-1693
            String sourceLan = p_session.getSourceLan();
            String targetLan = p_session.getTargetLan();
            TbLanguage source = null;
            TbLanguage target = null;
            ArrayList<TbLanguage> otherLanguages = new ArrayList<TbLanguage>();
            while (ite.hasNext())
            {
                TbLanguage tl = (TbLanguage) ite.next();
                String langName = tl.getName();
                String langLocale = tl.getLocal();

                if (langName.equals(sourceLan))
                {
                    source = tl;
                }
                else if (langName.equals(targetLan))
                {
                    target = tl;
                }
                else
                {
                    otherLanguages.add(tl);
                }
            }
            SortUtil.sort(otherLanguages,
                    new TbLanguageComparator(Locale.getDefault()));

            ArrayList<TbLanguage> languages = new ArrayList<TbLanguage>();
            if (source != null)
            {
                languages.add(source);
            }
            if (target != null)
            {
                languages.add(target);
            }
            languages.addAll(otherLanguages);

            ite = languages.iterator();
            while (ite.hasNext())
            {
                TbLanguage tl = (TbLanguage) ite.next();
                String langName = tl.getName();
                String langLocale = tl.getLocal();

                result.append("<languageGrp>");
                result.append("<language name=\"");
                result.append(EditUtil.encodeXmlEntities(langName));
                result.append("\" locale=\"");
                result.append(EditUtil.encodeXmlEntities(langLocale));
                result.append("\"");

                if (langName.equals(p_srcLang))
                {
                    result.append(" source-lang=\"true\"");
                }
                else if (langName.equals(p_trgLang))
                {
                    result.append(" target-lang=\"true\"");
                }

                result.append("/>");
                String lxml = tl.getXml();
                result.append(lxml);

                Iterator terms = tl.getTerms().iterator();
                while (terms.hasNext())
                {
                    result.append("<termGrp>");

                    TbTerm term = (TbTerm) terms.next();
                    long tid = term.getId();
                    String termContent = term.getTermContent();
                    result.append("<term");

                    if (tid == p_termId)
                    {
                        result.append(" search-term=\"true\"");
                    }

                    result.append(" termId=\"" + term.getId() + "\">");

                    if (isForBrowser)
                    {
                        result.append(EditUtil.encodeTohtml(EditUtil
                                .encodeTohtml(termContent)));
                    }
                    else
                    {
                        result.append(EditUtil.encodeXmlEntities(termContent));
                    }

                    result.append("</term>");
                    result.append(term.getXml());
                    result.append("</termGrp>");
                }

                result.append("</languageGrp>");
            }

            result.append("</conceptGrp>");
        }
        catch (Exception e)
        {

            CATEGORY.error(e.getMessage(), e);
        }

        return result.toString();
    }

    @Override
    public String getEntry(long p_entryId, long p_termId, String p_srcLang,
            String p_trgLang, SessionInfo p_session) throws TermbaseException
    {
        return getEntry(p_entryId, p_termId, p_srcLang, p_trgLang, p_session,
                false);
    }

    @Override
    public String getEntry(long p_entryId, String fileType,
            SessionInfo p_session) throws TermbaseException
    {
        if (fileType != null
                && fileType.equalsIgnoreCase(WebAppConstants.TERMBASE_TBX))
        {
            return getTbxEntry(p_entryId, p_session);

        }
        else
        {
            return getEntry(p_entryId, 0, "", "", p_session);
        }
    }

    @Override
    public String getEntryForBrowser(long id, SessionInfo p_session)
            throws TermbaseException
    {
        return getEntry(id, 0, "", "", p_session, true);
    }

    private String getTbxEntry(long p_entryId, SessionInfo p_session)
            throws TermbaseException
    {
        StringBuffer result = new StringBuffer();
        result.append("<termEntry id=\"").append(p_entryId).append("\">");

        TbConcept tConcept = HibernateUtil.get(TbConcept.class, p_entryId);
        Set set = tConcept.getLanguages();
        Iterator ite = set.iterator();

        while (ite.hasNext())
        {
            TbLanguage tl = (TbLanguage) ite.next();
            String langLocale = tl.getLocal();
            result.append("<langSet").append(" xml:lang=\"");
            result.append(langLocale).append("\">");

            Iterator terms = tl.getTerms().iterator();

            while (terms.hasNext())
            {
                TbTerm term = (TbTerm) terms.next();
                result.append("<ntig>").append("<termGrp>").append("<term>");
                result.append(EditUtil.encodeXmlEntities(EditUtil
                        .encodeXmlEntities(term.getTermContent())));
                result.append("</term>");
                result.append("</termGrp>").append("</ntig>");
            }

            result.append("</langSet>");
        }

        result.append("</termEntry>");

        return result.toString();
    }

    @Override
    public long addEntry(long tb_id, String p_entry, Definition m_definition,
            SessionInfo p_session) throws TermbaseException
    {
        ConceptHelper cp = new ConceptHelper();
        Entry entry = new Entry(p_entry);

        // Minimize entry and check for presence of required fields.
        EntryUtils.normalizeEntry(entry, m_definition);

        // Save entry to database
        long cid = cp.addEntry(tb_id, entry, p_session);

        return cid;
    }

    @Override
    public void deleteEntry(long p_entryId, SessionInfo p_session)
            throws TermbaseException
    {
        try
        {
            TbConcept tc = HibernateUtil.get(TbConcept.class, p_entryId);
            HibernateUtil.delete(tc);
        }
        catch (Exception ex)
        {
            CATEGORY.warn("Entry " + p_entryId + " could not be deleted.", ex);
            throw new TermbaseException(ex);
        }

        CATEGORY.info("Entry " + p_entryId + " deleted.");
    }

    @Override
    public String lockEntry(long tb_id, long p_entryId, boolean p_steal,
            HashMap m_entryLocks, SessionInfo p_session)
            throws TermbaseException
    {
        String result = "";
        LockInfo myLock = makeLock(tb_id, p_entryId, p_session);
        Long key = new Long(p_entryId);
        boolean b_acquired = false;

        synchronized (m_entryLocks)
        {
            LockInfo lock = (LockInfo) m_entryLocks.get(key);

            if (lock != null)
            {
                // locked, try to steal lock
                if (lock.isExpired() || p_steal
                        && canStealLock(lock, p_session))
                {
                    m_entryLocks.put(key, myLock);
                    b_acquired = true;
                }
            }
            else
            {
                // not locked, acquire lock
                m_entryLocks.put(key, myLock);
                b_acquired = true;
            }
        }

        if (b_acquired)
        {
            if (CATEGORY.isDebugEnabled())
            {
                CATEGORY.debug("Locked entry " + p_entryId + " for user "
                        + p_session.getUserDisplayName());
            }

            result = myLock.asXML();
        }

        return result;
    }

    @Override
    public void unlockEntry(long p_entryId, String p_cookie,
            HashMap m_entryLocks, SessionInfo p_session)
            throws TermbaseException
    {
        LockInfo lock;
        Long key = new Long(p_entryId);

        synchronized (m_entryLocks)
        {
            lock = (LockInfo) m_entryLocks.get(key);
        }

        if (lock != null)
        {
            LockInfo myLock = new LockInfo(p_cookie);

            if (myLock.equals(lock))
            {
                synchronized (m_entryLocks)
                {
                    m_entryLocks.remove(key);
                }

                if (CATEGORY.isDebugEnabled())
                {
                    CATEGORY.debug("Unlocked entry " + p_entryId);
                }
            }
            else
            {
                throw new TermbaseException(MSG_YOU_DONT_OWN_LOCK, null, null);
            }
        }
        else
        {
            throw new TermbaseException(MSG_ENTRY_NOT_LOCKED, null, null);
        }

    }

    @Override
    public void updateEntry(long p_entryId, Entry p_newEntry,
            SessionInfo p_session) throws TermbaseException
    {
        ConceptHelper cp = new ConceptHelper();
        try
        {
            TbConcept tc = cp.updateConcept(p_entryId, p_newEntry, p_session);
            // produce language-level statements
            HibernateUtil.saveOrUpdate(tc);
        }
        catch (Exception e)
        {
            throw new TermbaseException(e);
        }

    }

    @Override
    public String validateEntry(Definition m_definition, long tb_id,
            String p_entry, SessionInfo p_session) throws TermbaseException
    {
        ValidationInfo result;
        Entry entry = new Entry(p_entry);
        // Minimize entry.
        EntryUtils.pruneEntry(entry);
        result = Validator.validate(entry, m_definition, tb_id);

        return result.asXML();
    }

    /**
     * Creates and initializes a lock object.
     */
    private LockInfo makeLock(long tb_id, long p_entryId, SessionInfo p_session)
    {
        LockInfo result = new LockInfo(tb_id, p_entryId);

        result.setUser(p_session.getUserDisplayName());
        result.setEmail("");

        // Timestamp and cookie set in LockInfo.init();

        return result;
    }

    /**
     * Hook to override who can steal locks. Currently, every user can overide
     * everybody else's lock.
     * 
     * Later on we may decide that only administrator's can overide user's
     * locks.
     */
    private boolean canStealLock(LockInfo p_lock, SessionInfo p_session)
    {
        return true;
    }

    private String fixConceptXml(String xml)
    {
        Pattern p = Pattern
                .compile("(<transac type[^>]*>)([\\s\\S]*?)(</transac>)");
        Matcher m = p.matcher(xml);
        while (m.find())
        {
            String transac = m.group(2);
            if (StringUtil.isEmpty(transac))
            {
                continue;
            }
            String newTransac = UserUtil.getUserNameById(transac);
            String newString = m.group(1) + newTransac + m.group(3);
            xml = xml.replace(m.group(), newString);
        }

        return xml;
    }
}
