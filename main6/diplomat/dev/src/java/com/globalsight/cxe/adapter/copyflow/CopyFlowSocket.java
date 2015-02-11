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

package com.globalsight.cxe.adapter.copyflow;


import java.io.*;
import java.net.*;

import org.apache.log4j.Logger;

/**
 * Encapsulates the protocol exchanged with a CopyFlow Gold Server on
 * a Macintosh or Windows server. This class is reusable, so connect()
 * - close() - connect() - close() can be called multiple times.
 */
public class CopyFlowSocket
{
    static private String s_winEncoding = "ISO8859_1";
    static private String s_macEncoding = "MacRoman";

    private boolean m_isMacServer;
    private Socket m_socket = null;
    private OutputStream m_out;
    private InputStream m_in;

    private int m_job = -1;
    private byte[] m_buffer = new byte[1024];

    private Logger m_logger = null;

    public CopyFlowSocket(Logger p_logger,  boolean p_isMacServer)
    {
        m_isMacServer = p_isMacServer;
        m_logger = p_logger;
    }

    public void connect(String p_host, int p_port)
        throws IOException, UnknownHostException
    {
        m_socket = new Socket(p_host, p_port);

        if (m_logger != null && m_logger.isDebugEnabled())
        {
            m_logger.debug("<-> connected to " + p_host + ":" + p_port);
        }

        m_out = m_socket.getOutputStream();
        m_in = m_socket.getInputStream();

        String line = readString();

        openJob();
    }

    public void close()
    {
        if (m_socket != null)
        {
            try
            {
                try
                {
                    closeJob();
                }
                catch (Throwable ex)
                {
                    // too bad, need to release resource nonetheless.
                }

                m_in.close();
                m_out.close();
                m_socket.close();
            }
            catch (Throwable ex1)
            {
            }
        }

        m_in = null;
        m_out = null;
        m_socket = null;
    }

    public void openDocument(String directory, String file)
        throws IOException
    {
        String line = "[OpenDocument(" + m_job + "," + directory + "," +
            file + ",QXD)]";

        send(line);

        checkError();
    }

    public void saveDocument(String directory, String file)
        throws IOException
    {
        String line = "[SaveDocument(" + m_job + "," + directory + "," +
            file + ")]";

        send(line);

        checkError();
    }

    public void closeDocument()
        throws IOException
    {
        String line = "[CloseDocument(" + m_job + ")]";

        send(line);

        checkError();
    }

    public void autoNameBoxes(String prefix, String file)
        throws IOException
    {
        // Include art boxes and anchored boxes.
        String line = "[AutoNameBoxes(" + m_job + "," + prefix + "," +
            file + ",true,true)]";

        send(line);

        checkError();
    }

    public void exportStory(String directory, String file)
        throws IOException
    {
        String line = "[ExportStory(" + m_job + "," + directory + "," +
            file + ",XTG)]";

        send(line);

        checkError();
    }

    public void importStory(String directory, String file)
        throws IOException
    {
        String line = "[ImportStory(" + m_job + "," + directory + "," +
            file + ")]";

        send(line);

        checkError();
    }

    //
    // Private Methods
    //

    private void openJob()
        throws IOException
    {
        String line = "[OpenJob]";

        send(line);

        int rc = readCode();

        // line should contain "job_id, platform".
        m_job = rc;

        if (m_logger != null && m_logger.isDebugEnabled())
        {
            m_logger.debug("=== job id = " + m_job);
        }

        if (m_job < 0)
        {
            throw new IOException("CopyFlow is overloaded.");
        }
    }

    private void closeJob()
        throws IOException
    {
        String line = "[CloseJob(" + m_job + ")]";

        m_job = -1;

        send(line);

        checkError();
    }

    private void send(String s)
        throws IOException
    {
        if (m_logger != null && m_logger.isDebugEnabled())
        {
            m_logger.debug("--> " + toDebugString(s));
        }

        byte[] bytes = s.getBytes(getEncoding());
        int len = bytes.length;

        // Write binary length of the message (could be byte-order sensitive).
        m_buffer[0] = (byte)(len / 256); // hi-byte
        m_buffer[1] = (byte)(len % 256); // lo-byte

        for (int i = 0; i < len; i++)
        {
            m_buffer[i + 2] = bytes[i];
        }

        // Must write message in a single write.
        m_out.write(m_buffer, 0, len + 2);
        m_out.flush();
    }

    private int readCode()
        throws IOException
    {
        m_in.read(m_buffer, 0, 2);

        // read length of message (may be byte-order dependent)
        int len = 256 * m_buffer[0] + m_buffer[1];

        m_in.read(m_buffer, 0, len);

        String response = new String(m_buffer, 0, len, getEncoding());

        if (m_logger != null && m_logger.isDebugEnabled())
        {
            m_logger.debug("<-- " + toDebugString(response));
        }

        return Integer.parseInt(response);
    }

    private String readString()
        throws IOException
    {
        m_in.read(m_buffer, 0, 2);

        // read length of message (may be byte-order dependent)
        int len = 256 * m_buffer[0] + m_buffer[1];

        m_in.read(m_buffer, 0, len);

        String response = new String(m_buffer, 0, len, getEncoding());

        if (m_logger != null && m_logger.isDebugEnabled())
        {
            m_logger.debug("<-- " + toDebugString(response));
        }

        return response;
    }

    private int getErrorCode()
        throws IOException
    {
        String line = "ErrorCode";

        send(line);

        return readCode();
    }

    private String getMessage()
        throws IOException
    {
        String line = "ErrorMessage";

        send(line);

        return readString();
    }

    private void checkError()
        throws IOException
    {
        int rc = readCode();

        if (rc != 0)
        {
            String message = getMessage();

            throw new IOException(message);
        }
    }

    private String getEncoding()
    {
        if (m_isMacServer)
        {
            return s_macEncoding;
        }

        return s_winEncoding;
    }


    static private final char[] ZERO_ARRAY = {'0', '0', '0', '0'};
    static private final int HEX_DIGIT = 4;

    /**
     * Converts a Unicode string to a debug string with all non
     * ASCII characters encoded by Javascript \&zwnj;u escapes.
     */
    public static String toDebugString(String s)
    {
        StringBuffer ret = new StringBuffer(s.length());

        for (int i = 0; i < s.length(); ++i)
        {
            char ch = s.charAt(i);
            if (ch < 32 || ch > 127)
            {
                String hex = Integer.toHexString(ch);

                ret.append("\\u");

                int len = hex.length();
                if (len < HEX_DIGIT)
                {
                    ret.append(ZERO_ARRAY, 0, HEX_DIGIT - len);
                }

                ret.append(hex);
            }
            else
            {
                ret.append(ch);
            }
        }

        return ret.toString();
    }

    //
    // Test code.
    //

    static public void main(String[] argv)
        throws Exception
    {
        CopyFlowSocket s = new CopyFlowSocket(null, true);

        s.connect("10.1.1.115", 1024);

        try
        {
            s.openDocument("Macintosh HD:quark:docs:", "LPR3106IT-19-00_780g");
            s.closeDocument();
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
        finally
        {
            s.close();
        }
    }
}
