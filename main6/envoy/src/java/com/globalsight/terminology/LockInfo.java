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

package com.globalsight.terminology;

import com.globalsight.terminology.TermbaseException;
import com.globalsight.terminology.TermbaseExceptionMessages;
import com.globalsight.util.UTC;
import com.globalsight.terminology.util.XmlParser;

import com.globalsight.util.edit.EditUtil;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

import java.util.Date;

/**
 * Represents a lock of an entry currently being edited.
 */
public class LockInfo
    implements TermbaseExceptionMessages
{
    /**
     * Expiration duration after which a lock is considered "expired",
     * i.e. not used by the owner anymore. By default, 1 hour.
     *
     * The value is specified in milliseconds and can be set by other
     * classes in the system.
     */
    static public long s_EXPIRATION_TIME = 1*60*60*1000L;

    //
    // Members
    //

    private long m_termbase = 0;
    private long m_conceptId = 0;
    private String m_user = "";
    private String m_email = "";
    private Date m_date;
    private String m_cookie = "";

    //
    // Constructors
    //

    public LockInfo()
    {
    }

    public LockInfo(String p_xml)
        throws TermbaseException
    {
        init(p_xml);
    }

    public LockInfo(long p_termbase, long p_conceptId)
    {
        init(p_termbase, p_conceptId);
    }

    //
    // Public Methods
    //

    public long getTermbase()
    {
        return m_termbase;
    }

    public void setTermbase(long p_termbase)
    {
        m_termbase = p_termbase;
    }

    public long getConceptId()
    {
        return m_conceptId;
    }

    public void setConceptId(long p_conceptId)
    {
        m_conceptId = p_conceptId;
    }

    public String getUser()
    {
        return m_user;
    }

    public void setUser(String p_user)
    {
        m_user = p_user;
    }

    public String getEmail()
    {
        return m_email;
    }

    public void setEmail(String p_email)
    {
        m_email = p_email;
    }

    public String getCookie()
    {
        return m_cookie;
    }

    public void setCookie(String p_cookie)
    {
        m_cookie = p_cookie;
    }

    public Date getDate()
    {
        return m_date;
    }

    public void setDate(Date p_date)
    {
        m_date = p_date;
    }

    public String toString()
    {
        return asXML();
    }

    /**
     * Locks expire after a certain time, after which they're up for
     * grabs for everyone.
     */
    public boolean isExpired()
    {
        long now = System.currentTimeMillis();

        if (now - m_date.getTime() > s_EXPIRATION_TIME)
        {
            return true;
        }

        return false;
    }

    /**
     * Returns this object as an XML string for the owner of the lock,
     * including all fields.
     *
     * @return an xml string of the form
     * <lock>
     *   <termbase>1000</termbase>
     *   <conceptid>1000</conceptid>
     *   <who>user name</who>
     *   <when>date</when>
     *   <email>user's email address</email>
     *   <cookie>E96CFDC1-9909-4264-A150-986BBE8E9564</cookie>
     * </lock>
     */
    public String asXML()
    {
        StringBuffer result = new StringBuffer();

        result.append("<lock>\n");
        result.append("<termbase>");
        result.append(m_termbase);
        result.append("</termbase>\n");
        result.append("<conceptid>");
        result.append(m_conceptId);
        result.append("</conceptid>\n");
        result.append("<who>");
        result.append(EditUtil.encodeXmlEntities(m_user));
        result.append("</who>\n");
        result.append("<email>");
        result.append(EditUtil.encodeXmlEntities(m_email));
        result.append("</email>\n");
        result.append("<when>");
        result.append(UTC.valueOf(m_date));
        result.append("</when>\n");
        result.append("<cookie>");
        result.append(m_cookie);
        result.append("</cookie>\n");
        result.append("</lock>");

        return result.toString();
    }

    /**
     * Returns this object as an XML string for people other than the
     * owner, with some fields removed so the lock cannot be used to
     * unlock somebody else's entry, or to save an entry that has not
     * been locked by the caller.
     *
     * @return an xml string of the form
     * <lock>
     *   <termbase>1000</termbase>
     *   <conceptid>1000</conceptid>
     *   <who>user name</who>
     *   <when>date</when>
     *   <email>user's email address</email>
     * </lock>
     */
    public String asPublicXML()
    {
        StringBuffer result = new StringBuffer();

        result.append("<lock>\n");
        result.append("<termbase>");
        result.append(m_termbase);
        result.append("</termbase>\n");
        result.append("<conceptid>");
        result.append(m_conceptId);
        result.append("</conceptid>\n");
        result.append("<who>");
        result.append(EditUtil.encodeXmlEntities(m_user));
        result.append("</who>\n");
        result.append("<email>");
        result.append(EditUtil.encodeXmlEntities(m_email));
        result.append("</email>\n");
        result.append("<when>");
        result.append(UTC.valueOf(m_date));
        result.append("</when>\n");
        result.append("</lock>");

        return result.toString();
    }

    public boolean equals(Object p_other)
    {
        if (! (p_other instanceof LockInfo))
        {
            return false;
        }

        LockInfo other = (LockInfo)p_other;

        if (m_termbase != other.m_termbase ||
            m_conceptId != other.m_conceptId ||
            !m_user.equals(other.m_user) ||
            !m_date.equals(other.m_date) ||
            !m_cookie.equals(other.m_cookie))
        {
            return false;
        }

        return true;
    }

    //
    // Private Methods
    //

    private void init(long p_termbase, long p_conceptId)
    {
        m_termbase = p_termbase;
        m_conceptId = p_conceptId;

        // Cookie magic: the cookie is the same as the date in
        // milliseconds. Hmmmm. And we need to truncate to seconds
        // because Dates are sensitive to the milli-second range and
        // the printed representation doesn't capture that.
        //
        // This may cause problems if the same user is accessing the
        // same entry from different terminals at the same time.
        long now = (System.currentTimeMillis() / 1000) * 1000;

        m_date = new Date(now);
        m_cookie = String.valueOf(now);
    }

    private void init(String p_xml)
        throws TermbaseException
    {
        XmlParser parser = null;
        Document dom;

        try
        {
            parser = XmlParser.hire();
            dom = parser.parseXml(p_xml);
        }
        finally
        {
            XmlParser.fire(parser);
        }

        try
        {
            Element root = dom.getRootElement();
            Node lock = root.selectSingleNode("/lock");

            String tbid   = lock.valueOf("termbase");
            String cid    = lock.valueOf("conceptid");
            String who    = lock.valueOf("who");
            String email  = lock.valueOf("email");
            String when   = lock.valueOf("when");
            String cookie = lock.valueOf("cookie");

            if (tbid == null || cid == null || who == null ||
                email == null || when == null || cookie == null)
            {
                error("null field in XML", null);
            }

            setTermbase(Long.parseLong(tbid));
            setConceptId(Long.parseLong(cid));
            setUser(who);
            setEmail(email);
            setDate(UTC.parse(when));
            setCookie(cookie);
        }
        catch (TermbaseException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            // cast exception and throw
            error(e.getMessage(), e);
        }
    }

    /**
     * Throws an INVALID_LOCK exception.
     */
    private void error(String p_reason, Exception p_exception)
        throws TermbaseException
    {
        String[] args = { p_reason };

        throw new TermbaseException(MSG_INVALID_LOCK, args, p_exception);
    }
}
