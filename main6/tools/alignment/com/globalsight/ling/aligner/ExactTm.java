/*
Copyright (c) 2000 GlobalSight Corporation. All rights reserved.
    
THIS DOCUMENT CONTAINS TRADE SECRET DATA WHICH IS THE PROPERTY OF 
GLOBALSIGHT CORPORATION. THIS DOCUMENT IS SUBMITTED TO RECIPIENT
IN CONFIDENCE. INFORMATION CONTAINED HEREIN MAY NOT BE USED, COPIED
OR DISCLOSED IN WHOLE OR IN PART EXCEPT AS PERMITTED BY WRITTEN
AGREEMENT SIGNED BY AN OFFICER OF GLOBALSIGHT CORPORATION.

THIS MATERIAL IS ALSO COPYRIGHTED AS AN UNPUBLISHED WORK UNDER
SECTIONS 104 AND 408 OF TITLE 17 OF THE UNITED STATES CODE.
UNAUTHORIZED USE, COPYING OR OTHER REPRODUCTION IS PROHIBITED
BY LAW.
*/

package com.globalsight.ling.aligner;

// Java
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.util.Iterator;
import java.util.Map;
import java.util.Hashtable;
import java.util.Set;


// GlobalSight
import com.globalsight.ling.aligner.AlignerException;
import com.globalsight.ling.common.RegEx;
import com.globalsight.ling.common.RegExException;
import com.globalsight.ling.common.RegExMatchInterface;


/*
 * Store source and target segments in EXACTTM table.
 */
class ExactTm
{
    private Connection mDbConnection = null;
    private int mPairId = 0;
    private CRC mCrc = null;


    // Constructor
    public ExactTm(CRC crc)
    {
        mCrc = crc;
    }
    
        
    // set DB connection
    public void setConnection(Connection connection)
    {
        mDbConnection = connection;
    }
    

    // set pairid from source and target locales
    public void setLocales(int srcLangid, int trgLangid)
        throws AlignerException, Exception
    {
        String sql = "SELECT PAIRID FROM LANGPAIRS WHERE SOURCELANG = ? AND TARGETLANG = ?";
        PreparedStatement stmt = mDbConnection.prepareStatement(sql);
        stmt.setInt(1, srcLangid);
        stmt.setInt(2, trgLangid);
        ResultSet rs = stmt.executeQuery();
        int pairId;
        if(!rs.next())
        {
            sql = "SELECT MAX(PAIRID) FROM LANGPAIRS";
            PreparedStatement stmt2 = mDbConnection.prepareStatement(sql);
            rs = stmt2.executeQuery();
            rs.next();
            pairId = rs.getInt(1);
            pairId++;
            stmt2.close();

            sql = "INSERT INTO LANGPAIRS VALUES (?, ?, ?, 0.25)";
            stmt2 = mDbConnection.prepareStatement(sql);            
            stmt2.setInt(1, pairId);
            stmt2.setInt(2, srcLangid);
            stmt2.setInt(3, trgLangid);
            stmt2.executeUpdate();
            stmt2.close();
        }
        else
        {
            pairId = rs.getInt(1);
        }
        stmt.close();
        mPairId = pairId;
    }
    

    // store source and target pair to EXACTTM
    public void store(String source, String target)
        throws AlignerException, Exception
    {
        // avoid to insert NULL in EXACTTM
        if(source == null || target == null 
           || source.length() == 0 || target.length() == 0)
            return;

        // Convert [%%nnn] to [%%1], [%%2]...
        String[] texts = convertSubflows(source, target);
        String srcTmText = texts[0];
        String trgTmText = texts[1];
        
        // compute CRCs

        long exactCrc = Util.getExactCrc(srcTmText, mCrc);
        long textOnlyCrc = Util.getTextOnlyCrc(srcTmText, mCrc);
        
        // store segments to EXACTTM
        if(!updateTm(srcTmText, trgTmText, mPairId))
        {
            // insert segments if update fails
            insertTm(srcTmText, trgTmText, mPairId, exactCrc, textOnlyCrc);
        }
    }
    


    // Convert [%%nnn] to [%%1], [%%2]... in source and target segments
    private String[] convertSubflows(String source, String target)
        throws AlignerException, Exception
    {
        StringBuffer buf = new StringBuffer();
        Map subflowIds = new Hashtable();
        RegExMatchInterface match = null;
        String subText = source;
        int replaceId = 1;
        
        // convert the source
        while((match = RegEx.matchSubstring(subText, "\\[%%(\\d+)\\]"))
              != null)
        {
            // cache the subflow id and its replacement
            subflowIds.put(match.group(1), new Integer(replaceId));
            replaceId++;
            
            subText = subText.substring(match.endOffset(0));
        }
        String srcTmText = source;
        
        // convert the target
        String trgTmText = target;
        Set idSet = subflowIds.keySet();
        Iterator it = idSet.iterator();
        
        while(it.hasNext())
        {
            String subflowId = (String)it.next();
            Integer replacement = (Integer)subflowIds.get(subflowId);
            trgTmText = RegEx.substituteAll(trgTmText,
                                            "\\[%%" + subflowId + "\\]",
                                            "[%%" + replacement + "]");
        }
        String[] texts = {srcTmText, trgTmText};
        return texts;

    }
    

    // update the EXACTTM record
    private boolean updateTm(String source, String target, int pairId)
        throws Exception
    {
        PreparedStatement stmt = null;

        String sql = "UPDATE EXACTTM SET TRANSTEXT = ?"
            + "WHERE SOURCETEXT = ? AND PAIRID = ?";

        stmt = mDbConnection.prepareStatement(sql);
        stmt.setString(1, target);
        stmt.setString(2, source);
        stmt.setInt(3, pairId);
        
        int count = stmt.executeUpdate();
        stmt.close();
        return (count > 0);
    }
    

    // insert the EXACTTM record
    private boolean insertTm(String source, String target, 
                             int pairId, long exactCrc, long textOnlyCrc)
        throws Exception
    {
        PreparedStatement stmt = null;

        String sql = "INSERT INTO EXACTTM VALUES (?, ?, ?, ?, ?)";
        stmt = mDbConnection.prepareStatement(sql);
        stmt.setString(1, source);
        stmt.setString(2, target);
        stmt.setInt(3, pairId);
        stmt.setLong(4, exactCrc);
        stmt.setLong(5, textOnlyCrc);

        int count = stmt.executeUpdate();
        stmt.close();
        return (count > 0);
    }

}
