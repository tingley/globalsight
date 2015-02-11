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


package com.globalsight.everest.edit.offline.download.HTMLResourcePages;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayInputStream;

import java.text.MessageFormat;

import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import com.globalsight.ling.common.Transcoder;
import com.globalsight.ling.common.TranscoderException;
import com.globalsight.ling.common.LocaleCreater;

import com.globalsight.everest.edit.offline.AmbassadorDwUpException;
import com.globalsight.everest.edit.offline.AmbassadorDwUpExceptionConstants;

public abstract class DownloadWriter
{
    private Transcoder m_transcoder = null;

    /**
     * Constructor.
     */
    public DownloadWriter()
    {
        m_transcoder = new Transcoder();
    }

    /**
     * Copies the input stream to the output stream.
     */
    protected void copyStream(InputStream p_in, OutputStream p_out)
        throws AmbassadorDwUpException
    {
        try
        {
            synchronized (p_in)
            {
                synchronized (p_out)
                {
                    byte[] buffer = new byte[256];

                    while(true)
                    {
                        int bytesRead = p_in.read(buffer);

                        if (bytesRead == -1)
                        {
                            break;
                        }
                        p_out.write(buffer, 0, bytesRead);
                    }
                }
            }
        }
        catch (IOException ex)
        {
            throw new AmbassadorDwUpException(
                AmbassadorDwUpExceptionConstants.WRITER_IO_ERROR, ex);
        }
    }

    protected String formatString(String p_template, String p_param)
    {
        String[] param = new String[2];
        param[1] = p_param;
        return MessageFormat.format(p_template, param);
    }

    protected String formatString(String p_template, String[] p_params)
    {
        return MessageFormat.format(p_template, p_params);
    }

    protected abstract StringBuffer buildPage()
        throws AmbassadorDwUpException;

    protected InputStream makeInputStream(StringBuffer p_page, String p_encoding)
        throws AmbassadorDwUpException
    {
        InputStream input = null;

        try
        {
            byte[] inBytes = m_transcoder.transcode(p_page.toString(), p_encoding);
            input = new ByteArrayInputStream(inBytes);
        }
        catch (TranscoderException ex)
        {
            throw new AmbassadorDwUpException(
                AmbassadorDwUpExceptionConstants.WRITER_UNKNOWN_ENCODING, ex);
        }

        return input;
    }

    public void write(OutputStream p_out)
        throws AmbassadorDwUpException
    {
    }

    public void write(OutputStream p_out, String p_encoding)
        throws AmbassadorDwUpException
    {
        ByteArrayInputStream input =
            (ByteArrayInputStream)makeInputStream(buildPage(), p_encoding);

        copyStream(input, p_out);

        try
        {
            input.close();
        }
        catch(IOException e)
        {
            throw new AmbassadorDwUpException(
                AmbassadorDwUpExceptionConstants.WRITER_IO_ERROR, e);
        }
    }

    protected ResourceBundle loadProperties(String p_propertyFileName,
        Locale p_locale)
        throws AmbassadorDwUpException
    {
        return ResourceBundle.getBundle(p_propertyFileName, p_locale);
    }

    protected Locale getLocale(String p_localeName)
    {
        return LocaleCreater.makeLocale(p_localeName);
    }
}
