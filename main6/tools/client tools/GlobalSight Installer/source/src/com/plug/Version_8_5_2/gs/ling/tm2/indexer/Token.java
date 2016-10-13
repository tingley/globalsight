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
package com.plug.Version_8_5_2.gs.ling.tm2.indexer;


import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;
import java.nio.ByteBuffer;
import java.nio.BufferUnderflowException;

import org.apache.log4j.Logger;


/**
 * Token class represents a token of an indexed segment
 */

public class Token
{
    private static final Logger c_logger =
        Logger.getLogger(
            Token.class.getName());

    private String m_token = null;
    private long m_tuvId = 0;
    private long m_tuId = 0;
    private long m_tmId = 0;
    private int m_repetitionCount = 0;
    private int m_totalTokenCount = 0;
    private boolean m_isSource = false;
    

    //constructor
    public Token(String p_token, long p_tuvId, long p_tuId, long p_tmId,
        int p_repetitionCount, int p_totalTokenCount, boolean p_isSource)
    {
        m_token = p_token;
        m_tuvId = p_tuvId;
        m_tuId = p_tuId;
        m_tmId = p_tmId;
        m_repetitionCount = p_repetitionCount;
        m_totalTokenCount = p_totalTokenCount;
        m_isSource = p_isSource;
    }
    

    public String getTokenString()
    {
        return m_token;
    }
    
    public long getTuvId()
    {
        return m_tuvId;
    }
    
    public long getTuId()
    {
        return m_tuId;
    }
    
    public void setTuId(long p_tuId)
    {
        m_tuId = p_tuId;
    }
    
    public long getTmId()
    {
        return m_tmId;
    }
    
    public int getRepetition()
    {
        return m_repetitionCount;
    }
    
    public int getTotalTokenCount()
    {
        return m_totalTokenCount;
    }
    
    public boolean isSource()
    {
        return m_isSource;
    }


    public boolean equals(Object p_other)
    {
        boolean result = false;
        
        if(p_other instanceof Token)
        {
            // if the token string and the tuv id are the same, they
            // should be the same. In some cases, we create Token
            // objects with dummy values in some attributes such as
            // repetition and isSource. It is important NOT to compare
            // the attributes other than token string and tuv id.
            Token other = (Token)p_other;
            if(m_token.equals(other.m_token) && m_tuvId == other.m_tuvId)
            {
                result = true;
            }
        }
        
        return result;
    }
    

    public int hashCode()
    {
        return (int)(m_token.hashCode() + m_tuvId);
    }
    

    // size of an index record. Three longs and three ints.
    static private int INDEX_RECORD_SIZE = 8 * 3 + 4 * 3;
    
    /**
     * converts a List of Tokens to an inverted list in byte array.
     *
     * @param p_tokens List of Token objects
     * @return inverted list
     */
    static public byte[] getInvertedList(List p_tokens)
        throws Exception
    {
        if(p_tokens == null || p_tokens.size() == 0)
        {
            return new byte[0];
        }
        
        ByteBuffer byteBuffer
            = ByteBuffer.allocate(INDEX_RECORD_SIZE * p_tokens.size());
        
        Iterator it = p_tokens.iterator();
        while(it.hasNext())
        {
            Token token = (Token)it.next();

            byteBuffer.putLong(token.getTuvId());
            byteBuffer.putLong(token.getTuId());
            byteBuffer.putLong(token.getTmId());
            byteBuffer.putInt(token.getRepetition());
            byteBuffer.putInt(token.getTotalTokenCount());
            byteBuffer.putInt(token.isSource() ? 1 : 0);
        }

        // compress the inverted list
//         ByteArrayOutputStream baos = new ByteArrayOutputStream();
//         DeflaterOutputStream deflator = new DeflaterOutputStream(baos);
//         deflator.write(sb.toString().getBytes("UTF-8"));
//         deflator.close();
//         return baos.toByteArray();
        
        return byteBuffer.array();
    }


    static final private boolean FILTER = true;
    static final private boolean NO_FILTER = false;
    

    /**
     * converts an inverted list to a List of Tokens.
     *
     * @param p_tokenString token string
     * @param p_invertedList inverted list
     * @return List of Token objects
     */
    static public List getTokenList(
        String p_tokenString, byte[] p_invertedList)
        throws Exception
    {
        return getTokenList(p_tokenString,
            p_invertedList, null, true, NO_FILTER);
    }


    /**
     * converts an inverted list to a List of Tokens. This method also
     * filters out unnecessary tokens based on the tm ids and multi
     * lingual leveraging options.
     *
     * @param p_tokenString token string
     * @param p_invertedList inverted list
     * @param p_tmIds TM ids to leverage from
     * @param p_isMultiLingLeveraging true if multi lingual leveraging is on
     * @return List of Token objects
     */
    static public List getTokenList(
        String p_tokenString, byte[] p_invertedList,
        Collection p_tmIds, boolean p_isMultiLingLeveraging)
        throws Exception
    {
        return getTokenList(p_tokenString, p_invertedList,
            p_tmIds, p_isMultiLingLeveraging, FILTER);
    }


    static private List getTokenList(
        String p_tokenString, byte[] p_invertedList,
        Collection p_tmIds, boolean p_isMultiLingLeveraging,
        boolean p_filters)
        throws Exception
    {
        if(p_invertedList == null || p_invertedList.length == 0)
        {
            return new ArrayList();
        }
        
//         // decompress the inverted list
//         ByteArrayInputStream bais = new ByteArrayInputStream(p_invertedList);
//         InflaterInputStream inflator = new InflaterInputStream(bais);
//         ByteArrayOutputStream baos = new ByteArrayOutputStream();

//         byte[] buf = new byte[4000];
//         int readLen = 0;
//         while((readLen = inflator.read(buf, 0, buf.length)) != -1)
//         {
//             baos.write(buf, 0, readLen);
//         }
//         byte[] invertedList = baos.toByteArray();
        
        byte[] invertedList = p_invertedList;
        List tokens = new ArrayList(invertedList.length / INDEX_RECORD_SIZE);
        ByteBuffer byteBuffer = ByteBuffer.wrap(invertedList);
        
        try
        {
            while(byteBuffer.hasRemaining())
            {
                long tuvId = byteBuffer.getLong();
                long tuId = byteBuffer.getLong();
                long tmId = byteBuffer.getLong();
                int repetition = byteBuffer.getInt();
                int tokenCount = byteBuffer.getInt();
                boolean isSource = byteBuffer.getInt() == 1;
            
                if(!p_filters || (p_tmIds.contains(new Long(tmId))
                       && (isSource || p_isMultiLingLeveraging)))
                {
                    Token token = new Token(
                        p_tokenString, tuvId, tuId, tmId,
                        repetition, tokenCount, isSource);

                    tokens.add(token);
                }
            }
        }
        catch(BufferUnderflowException e)
        {
            c_logger.error("Corrupted inverted list for token "
                + p_tokenString + ": size of inverted list = "
                + p_invertedList.length);
        }
        
        return tokens;
    }
    
    @Override
    public String toString() {
        return "Token('" + m_token + "', tm=" + m_tmId + ", tu=" + m_tuId + 
               ", tuv=" + m_tuvId + ", rep=" + m_repetitionCount + ", tot=" + 
               m_totalTokenCount + ")";
    }
}
