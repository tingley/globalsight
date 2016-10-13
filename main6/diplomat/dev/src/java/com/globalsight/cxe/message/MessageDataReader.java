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
package com.globalsight.cxe.message;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedInputStream;
import java.io.OutputStream;
import com.globalsight.cxe.message.MessageData;

/**
 * Provides some utilities for reading MessageData objects
 */
public class MessageDataReader
{
    /**
     * Reads the content of the MessageData as a UTF8 String.
     * @exception IOException
     * @return String
     */
    static public String readString(MessageData p_messageData) throws IOException
    {
        int len = (int) p_messageData.getSize();
        StringBuffer content = new StringBuffer(len);
        char buf[] = new char[32768];
        BufferedInputStream bis = new BufferedInputStream(p_messageData.getInputStream());
        InputStreamReader reader = new InputStreamReader(bis, "UTF8");
        int n = -1;
        do
        {
            n = reader.read(buf,0,buf.length);
            if (n > 0)
                content.append(buf,0,n);
        }
        while (n>0);
        reader.close();
        return content.toString();
    }

    /**
     * Reads the content of the MessageData as a byte array
     * @exception IOException
     * @return byte[]
     */
    static public byte[] readBytes(MessageData p_messageData) throws IOException
    {
        int len = (int) p_messageData.getSize();
        byte buffer[] = new byte[len];
        BufferedInputStream bis = new BufferedInputStream(p_messageData.getInputStream());
        bis.read(buffer,0,len);
        bis.close();
        
        return buffer;
    }
}

