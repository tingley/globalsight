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
package com.globalsight.ling.jtidy;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;


/**
 * Output implementation using java writers.
 * @author Fabrizio Giustina
 * @version $Revision: 1.1 $ ($Author: yorkjin $)
 */
public class OutJavaImpl implements Out
{

    /**
     * Java input stream writer.
     */
    private Writer writer;

    /**
     * Newline string.
     */
    private char[] newline;

    /**
     * Constructor.
     * @param configuration actual configuration instance (needed for newline configuration)
     * @param encoding encoding name
     * @param out output stream
     * @throws UnsupportedEncodingException if the undelining OutputStreamWriter doesn't support the rquested encoding.
     */
    public OutJavaImpl(Configuration configuration, String encoding, OutputStream out)
        throws UnsupportedEncodingException
    {
        this.writer = new OutputStreamWriter(out, encoding);
        this.newline = configuration.newline;
    }

    /**
     * @see com.globalsight.ling.jtidy.Out#outc(int)
     */
    public void outc(int c)
    {
        try
        {
            writer.write(c);
        }
        catch (IOException e)
        {
            // @todo throws exception
            System.err.println("OutJavaImpl.outc: " + e.getMessage());
        }
    }

    /**
     * @see com.globalsight.ling.jtidy.Out#outc(byte)
     */
    public void outc(byte c)
    {
        try
        {
            writer.write(c);
        }
        catch (IOException e)
        {
            // @todo throws exception
            System.err.println("OutJavaImpl.outc: " + e.getMessage());
        }
    }

    /**
     * @see com.globalsight.ling.jtidy.Out#newline()
     */
    public void newline()
    {
        try
        {
            writer.write(this.newline);
        }
        catch (IOException e)
        {
            // @todo throws exception
            System.err.println("OutJavaImpl.newline: " + e.getMessage());
        }
    }

    /**
     * @see com.globalsight.ling.jtidy.Out#close()
     */
    public void close()
    {
        try
        {
            writer.close();
        }
        catch (IOException e)
        {
            System.err.println("OutJavaImpl.close: " + e.getMessage());
        }
    }

}
