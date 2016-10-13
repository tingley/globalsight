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
package com.globalsight.ling.tm2.leverage;

import com.globalsight.ling.tm2.indexer.Token;
import com.globalsight.ling.tm2.persistence.DbUtil;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.PreparedStatement;




/**
 * Print Inverted List on a console
 */
public class PrintInvertedList
{
    static private final String SELECT_INVERTED_LIST
        = "select inverted_list from segment_tm_token_t where "
        + "token = ? and locale_id = ?";

    static private final String SELECT_LOCALE_ID
        = "select id from locale where "
        + "iso_lang_code = ? and iso_country_code = ?";
    
    
    private Connection m_connection;
    private long m_localeId;
    

    public PrintInvertedList(Connection p_connection)
        throws Exception
    {
        m_connection = p_connection;
    }

    
    public static void main(String[] args)
        throws Exception
    {
        if(args.length < 2)
        {
            System.err.println("USAGE: java PrintInvertedList locale token");
            System.exit(1);
        }
        
        Connection connection = null;
        
        try
        {
            connection = DbUtil.getConnection();

            PrintInvertedList printer = new PrintInvertedList(connection);
            printer.setSourceLocale(args[0]);

            List tokenList = new ArrayList();
            for(int i = 1; i < args.length; i++)
            {
                tokenList.add(args[i]);
            }
            
            printer.print(tokenList);
        }
        catch(Exception e)
        {
            e.printStackTrace();
            throw e;
        }
        finally
        {
            // return connection 
            DbUtil.returnConnection(connection);
        }
    }


    public void print(List p_tokenList)
        throws Exception
    {
        PreparedStatement psIndex = null;
        ResultSet rs = null;
        try 
        {
            Iterator it = p_tokenList.iterator();
            psIndex = m_connection.prepareStatement(SELECT_INVERTED_LIST);
            while(it.hasNext())
            {
                String tokenString = (String)it.next();
                psIndex.setString(1, tokenString);
                psIndex.setLong(2, m_localeId);
                rs = psIndex.executeQuery();
                if(rs.next())
                {
                    byte[] invertedList = DbUtil.readBlob(rs, "inverted_list");
                    List tokens = Token.getTokenList(tokenString, invertedList);
                    printTokens(tokenString, tokens);
                }
                DbUtil.silentClose(rs);
            }
        }
        finally
        {
            DbUtil.silentClose(rs);
            DbUtil.silentClose(psIndex);
        }
    }
    

    public void setSourceLocale(String p_localeName)
        throws Exception
    {
        m_localeId = getLocaleId(p_localeName);
    }
    

    private long getLocaleId(String p_localeName)
        throws Exception
    {
        long localeId = 0;
        PreparedStatement psLocale=null;
        ResultSet rs = null;
        try
        {
            if(p_localeName.length() != 5
               || !p_localeName.substring(2, 3).equals("_"))
            {
                throw new Exception("Locale name is incorrect: " + p_localeName);
            }
            String language = p_localeName.substring(0, 2);
            String country = p_localeName.substring(3, 5);
            psLocale = m_connection.prepareStatement(SELECT_LOCALE_ID);
            psLocale.setString(1, language);
            psLocale.setString(2, country);
            rs = psLocale.executeQuery();
            if(rs.next())
            {
                localeId = rs.getLong("id");
            }
            else
            {
                throw new Exception(p_localeName + " is invalid.");
            }
        }
        finally
        {
            DbUtil.silentClose(rs);
            DbUtil.silentClose(psLocale);
        }
        return localeId;
    }


    private void printTokens(String p_tokenString, List p_tokenList)
    {
        System.out.println("Tokens for \"" + p_tokenString + "\"");
        System.out.println();
        
        Iterator it = p_tokenList.iterator();
        while(it.hasNext())
        {
            Token token = (Token)it.next();
            System.out.println(" Tuv id: " + token.getTuvId());
            System.out.println(" Tu id: " + token.getTuId());
            System.out.println(" Tm id: " + token.getTmId());
            System.out.println(" Repetition: " + token.getRepetition());
            System.out.println(" Token Count: " + token.getTotalTokenCount());
            System.out.println(" is source: "
                + (token.isSource() ? "yes" : "no"));
            System.out.println();
        }
    }
    
}
