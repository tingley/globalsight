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
package com.globalsight.diplomat.util.database;

import java.util.Vector;

/**
 * RecordProfile maintains information about how records are read from and
 * written to a database.  Records are read from the database via the
 * acquisition SQL string, and written back via one of the preview/final
 * SQL statements.  Connections to the database are made through the various
 * connection IDs.
 * <p>
 * The record profile also maintains a collection of column profiles, each of
 * which provides information about the structure of a single field in a row
 * of the result set that is obtained by executing the acquisition SQL.
 */
public class RecordProfile
{
    //
    // PRIVATE MEMBER VARIABLES
    //
    private long m_id;
    private String m_name;
    private String m_acqSql;
    private String m_acqCtxSql;
    private long m_acqConnectId;
    private String m_prvInsSql;
    private String m_prvUpdSql;
    private long m_prvConnectId;
    private String m_finInsSql;
    private String m_finUpdSql;
    private long m_finConnectId;
    private long m_prvUrlId;
    private boolean m_manualMode;
    private Vector m_colProfiles;
    private String m_codeset;
    private String m_l10nprofile_id;

    //
    // Public Constructors
    //

    /**
     * Create a new, partially initialized instance of a record profile.  The
     * result should not be considered a "real" record profile, and its
     * attributes shoule be set to real values via the appropriate setters.
     */
    public RecordProfile()
    {
        super();
        m_id = -1;
        m_name = "";
        m_acqSql = "";
        m_acqCtxSql = "";
        m_acqConnectId = -1;
        m_prvInsSql = "";
        m_prvUpdSql = "";
        m_prvConnectId = -1;
        m_finInsSql = "";
        m_finUpdSql = "";
        m_finConnectId = -1;
        m_prvUrlId = -1;
        m_manualMode = false;
        m_colProfiles = new Vector();
        // always use UTF-8 as a default
        m_codeset = "UTF-8";
        m_l10nprofile_id = "";
    }

    //
    // PUBLIC ACCESSORS
    //

    /**
     * Return the id for this record profile.
     *
     * @return the current id.
     */
    public long getId()
    {
        return m_id;
    }

    /**
     * Set the id for this record profile.
     *
     * @param p_id the new id.
     */
    void setId(long p_id)
    {
        m_id = p_id;
    }

    /**
     * Return the name of this record profile.
     *
     * @return the current name.
     */
    public String getName()
    {
        return m_name;
    }

    /**
     * Set the name for this record profile.
     *
     * @param p_name the new name.
     */
    void setName(String p_name)
    {
        m_name = p_name;
    }

    /**
     * Return the acquisition SQL string.
     *
     * @return the current acquisition SQL.
     */
    public String getAcquisitionSql()
    {
        return m_acqSql;
    }

    /**
     * Set the acquisition SQL.
     *
     * @param p_sql the new value.
     */
    void setAcquisitionSql(String p_sql)
    {
        m_acqSql = p_sql;
    }

    /**
     * Return the acquisition context SQL string.
     *
     * @return the current acquisition context SQL.
     */
    public String getAcquisitionContextSql()
    {
        return m_acqCtxSql;
    }

    /**
     * Set the acquisition contextSQL.
     *
     * @param p_sql the new value.
     */
    void setAcquisitionContextSql(String p_sql)
    {
        m_acqCtxSql = p_sql;
    }

    /**
     * Return the id of the acquisition connection.
     *
     * @return the current acquisition id.
     */
    public long getAcquisitionConnectionId()
    {
        return m_acqConnectId;
    }

    /**
     * Set the id of the acquisition connection.
     *
     * @param p_id the new id.
     */
    void setAcquisitionConnectionId(long p_id)
    {
        m_acqConnectId = p_id;
    }

    /**
     * Return the preview insert SQL string.
     *
     * @return the current SQL.
     */
    public String getPreviewInsertSql()
    {
        return m_prvInsSql;
    }

    /**
     * Set the preview insert SQL string.
     *
     * @param p_sql the new value.
     */
    void setPreviewInsertSql(String p_sql)
    {
        m_prvInsSql = p_sql;
    }

    /**
     * Return the preview update SQL string.
     *
     * @return the current SQL.
     */
    public String getPreviewUpdateSql()
    {
        return m_prvUpdSql;
    }

    /**
     * Set the preview update SQL string.
     *
     * @param p_sql the new value.
     */
    void setPreviewUpdateSql(String p_sql)
    {
        m_prvUpdSql = p_sql;
    }

    /**
     * Return the id of the preview connection.
     *
     * @return the current preview id.
     */
    public long getPreviewConnectionId()
    {
        return m_prvConnectId;
    }

    /**
     * Set the id of the preview connection.
     *
     * @param p_id the new id.
     */
    void setPreviewConnectionId(long p_id)
    {
        m_prvConnectId = p_id;
    }

    /**
     * Return the final insert SQL string.
     *
     * @return the current SQL.
     */
    public String getFinalInsertSql()
    {
        return m_finInsSql;
    }

    /**
     * Set the final insert SQL string.
     *
     * @param p_sql the new value.
     */
    void setFinalInsertSql(String p_sql)
    {
        m_finInsSql = p_sql;
    }

    /**
     * Return the final update SQL string.
     *
     * @return the current SQL.
     */
    public String getFinalUpdateSql()
    {
        return m_finUpdSql;
    }

    /**
     * Set the final update SQL string.
     *
     * @param p_sql the new value.
     */
    void setFinalUpdateSql(String p_sql)
    {
        m_finUpdSql = p_sql;
    }

    /**
     * Return the id of the final connection.
     *
     * @return the current final id.
     */
    public long getFinalConnectionId()
    {
        return m_finConnectId;
    }

    /**
     * Set the id of the final connection.
     *
     * @param p_id the new id.
     */
    void setFinalConnectionId(long p_id)
    {
        m_finConnectId = p_id;
    }

    /**
     * Return the id of the preview url.
     *
     * @return the current preview url id.
     */
    public long getPreviewUrlId()
    {
        return m_prvUrlId;
    }

    /**
     * Set the id of the preview url.
     *
     * @param p_id the new id.
     */
    void setPreviewUrlId(long p_id)
    {
        m_prvUrlId = p_id;
    }

    /**
     * Return the manual mode flag.
     *
     * @return the value of the flag.
     */
    public boolean isManualMode()
    {
        return m_manualMode;
    }

    /**
     * Set the manual mode flag to the given value..
     *
     * @param p_mode the new mode.
     */
    void setManualMode(boolean p_mode)
    {
        m_manualMode = p_mode;
    }

    /**
     * Return the vector of column profiles associated with this record profile.
     *
     * @return the column profiles.
     */
    public Vector getColumnProfiles()
    {
        return m_colProfiles;
    }

    /**
     * Set the vector of column profiles for this record profile.
     *
     * @param p_profiles the new vector of profiles.
     */
    void setColumnProfiles(Vector p_profiles)
    {
        m_colProfiles = p_profiles;
    }

    /**
     * Return the code set as an IANA string
     *
     * @return codeset
     */
    public String getCodeSet()
    {
        return m_codeset;
    }

    /**
     * Set the codeset (should be IANA string, not Java codeset)
     *
     * @param p_codeset IANA codeset
     */
    void setCodeSet(String p_codeset)
    {
        m_codeset = p_codeset;
    }

    /**
     * Return the l10n profile id
     *
     * @return l10nprofile_id
     */
    public String getL10nProfileId()
    {
        return m_l10nprofile_id;
    }

    /**
     * Set the codeset (should be IANA string, not Java codeset)
     *
     * @param p_l10nprofile_id -- the l10nprofile ID
     */
    void setL10nProfileId(String p_l10nprofile_id)
    {
        m_codeset = p_l10nprofile_id;
    }

    /**
     * Return a string representation of the record profile.
     *
     * @return a description of the receiver.
     */
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("RecordProfile [id=");
        sb.append(getId());
        sb.append(", ");
        sb.append(getColumnProfiles().size());
        sb.append(" ColumnProfiles]");
        return sb.toString();
    }
    /**
     * Return a detailed string representation of the record profile.
     *
     * @return a description of the receiver.
     */
    public String detailString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("RecordProfile [id=");
        sb.append(getId());
        sb.append(", Name=");
        sb.append(getName());
        sb.append(", AcqSql=");
        sb.append(getAcquisitionSql());
        sb.append(", CtxSql=");
        sb.append(getAcquisitionContextSql());
        sb.append(", PInsSql=");
        sb.append(getPreviewInsertSql());
        sb.append(", PUpdSql=");
        sb.append(getPreviewUpdateSql());
        sb.append(", FInsSql=");
        sb.append(getFinalInsertSql());
        sb.append(", FUpdSql=");
        sb.append(getFinalUpdateSql());
        sb.append(", Manual=");
        sb.append(isManualMode());
        sb.append(", Columns=[");
        Vector v = getColumnProfiles();
        for (int i = 0 ; i < v.size() ; i++)
        {
            sb.append("[");
            sb.append(((ColumnProfile)v.elementAt(i)).detailString());
            sb.append("]");
            if (i < v.size() - 1)
            {
                sb.append(", ");
            }
        }
        sb.append("]]");
        return sb.toString();
    }
}

