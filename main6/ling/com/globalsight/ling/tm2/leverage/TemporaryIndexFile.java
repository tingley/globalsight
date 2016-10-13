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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.globalsight.ling.tm2.indexer.Token;
import com.globalsight.util.AmbFileStoragePathUtils;


/**
 * Map a collection of index to a memory mapped file.
 */

class TemporaryIndexFile
{
    private static final Logger c_logger =
        Logger.getLogger(
            TemporaryIndexFile.class.getName());

//    private final static String TM_INDEX_FILE_DIR
//        = "GlobalSight/TmIndexFiles";

    // one record consists of
    //   long m_tuvId
    //   long m_tuId
    //   int m_repetitionCount
    //   int m_totalTokenCount
    // of Token object
    private static final int RECORD_SIZE = 8 + 8 + 4 + 4;
    
//    private static final File MMF_DIR = getTempFileDir();
    
    private String m_tokenString;
    private File m_file;
    private int m_fileSize;
    private int m_readBytes;
    
    private FileInputStream m_fis = null;
    private DataInputStream m_dis = null;
    
//    private static File getTempFileDir()
//    {
//        File tmIndexFilesDir;
//        
//        try
//        {
//            SystemConfiguration sc = SystemConfiguration.getInstance();
//            String fileStorageDir
//                = sc.getStringParameter(SystemConfiguration.FILE_STORAGE_DIR);
//
//            tmIndexFilesDir = new File(fileStorageDir, TM_INDEX_FILE_DIR);
//            tmIndexFilesDir.mkdirs();
//        }
//        catch(Exception e)
//        {
//            throw new RuntimeException(e);
//        }
//
//        return tmIndexFilesDir;
//    }
    
        
    // constructor
    public TemporaryIndexFile(String p_tokenString, List p_tokenList)
        throws Exception
    {
        m_tokenString = p_tokenString;
//        m_file = File.createTempFile(filePrefix(p_tokenString), ".mmf", MMF_DIR);
        m_file = File.createTempFile(filePrefix(p_tokenString), ".mmf", 
                AmbFileStoragePathUtils.getTmIndexFileDir());
        try
        {
            createTempIndexFile(p_tokenList);
        }
        catch(Exception e)
        {
            m_file.delete();
            throw e;
        }
    }
    

    public void open()
        throws Exception
    {
        close();
        
        m_fis = new FileInputStream(m_file);
        BufferedInputStream bis = new BufferedInputStream(m_fis);
        m_dis = new DataInputStream(bis);
        m_readBytes = 0;
    }
    

    public boolean hasNext()
        throws Exception
    {
        return m_readBytes < m_fileSize;
    }


    public Token nextToken()
        throws Exception
    {
        Token token = new Token(m_tokenString, m_dis.readLong(),
            m_dis.readLong(), 0, m_dis.readInt(),
            m_dis.readInt(), true);
        
        m_readBytes += RECORD_SIZE;
        
        return token;
    }
    

    public void close()
        throws Exception
    {
        if(m_fis != null)
        {
            m_fis.close();
        }
        
        m_fis = null;
        m_dis = null;
    }
    

    private String filePrefix(String p_tokenString)
    {
        String prefix = "GSTM2Idx-";
        
        for(int i = 0; i < p_tokenString.length(); i++)
        {
            char c = p_tokenString.charAt(i);
            
            // pass through only ascii number and lower case a-z
            // The prefix might look "idx-the-" or "idx-[45f6]h[7c]-"
            if((c >= 0x30 && c <= 0x39) || (c >= 0x61 && c <= 0x7a))
            {
                prefix += c;
            }
            else
            {
                prefix += "[" + Integer.toHexString(c) + "]";
            }
        }
        
        return prefix + "-";
    }
    

    private void createTempIndexFile(List p_tokenList)
        throws Exception
    {
        FileOutputStream fos = new FileOutputStream(m_file);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        DataOutputStream dos = new DataOutputStream(bos);
        
        // write the contents of Token list into a file. A Token in
        // the file is a fixed 24 bytes (2 x long, 2 x int) record.
        Iterator it = p_tokenList.iterator();
        while(it.hasNext())
        {
            Token token = (Token)it.next();
            dos.writeLong(token.getTuvId());
            dos.writeLong(token.getTuId());
            dos.writeInt(token.getRepetition());
            dos.writeInt(token.getTotalTokenCount());
        }

        dos.flush();
        m_fileSize = dos.size();
        fos.close();
    }


    public void deleteTempFile()
    {
        m_file.delete();
    }
    
}
