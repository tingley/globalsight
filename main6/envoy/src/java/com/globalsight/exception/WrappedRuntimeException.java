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
package com.globalsight.exception;

import java.io.CharArrayWriter;
import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * WrappedRuntimeException represents an abstract runtime exception that
 * wraps other exceptions which may be thrown.
 */
public class WrappedRuntimeException
    extends RuntimeException
{
    //
    // PRIVATE MEMBER VARIABLES
    //
    private Exception m_exc;
    private transient String m_trace;

    //
    // PUBLIC CONSTRUCTORS
    //
    /**
     * Construct an exception with the given message, and wrap the given
     * exception.
     *
     * @param p_message the message to ge displayed to the caller.
     * @param p_exceptionToWrap the exception to be wrapped.
     */
    public WrappedRuntimeException(String p_message,
                                   Exception p_exceptionToWrap)
    {
        super(p_message);
        m_exc = p_exceptionToWrap;
        m_trace = null;
    }

    //
    // PUBLIC METHODS
    //
    /**
     * Return the wrapped exception, or null if none exists.
     *
     * @return the wrapped exception.
     */
    public Exception getWrappedException()
    {
        return m_exc;
    }

    /**
     * Print the wrapped exception's stack trace, plus the message, onto the
     * standard error stream.
     */
    public void printStackTrace()
    {
        printStackTrace(System.err);
    }

    /**
     * Print the wrapped exception's stack trace, plus the message, onto the
     * given print stream.
     *
     * @param p_ps the print stream to print on.
     */
    public void printStackTrace(PrintStream p_ps)
    {
        super.printStackTrace(p_ps);
        p_ps.println(detailedExceptionMessage());
    }

    /**
     * Print the wrapped exception's stack trace, plus the message, onto the
     * given print writer.
     *
     * @param p_pw the print stream to print on.
     */
    public void printStackTrace(PrintWriter p_pw)
    {
        super.printStackTrace(p_pw);
        p_pw.println(detailedExceptionMessage());
    }

    //
    // PRIVATE SUPPORT METHODS
    //
    /* Return the stack trace for the wrapped exception */
    private String wrappedExceptionStackTrace()
    {
        if (m_trace == null)
        {
            CharArrayWriter w = new CharArrayWriter();
            m_exc.printStackTrace(new PrintWriter(w));
            m_trace = w.toString();
        }
        return m_trace;
    }

    /* Return the class name without the fully scoped path. */
    private String getShortClassName()
    {
        String s = getClass().getName();
        int p = s.lastIndexOf(".");
        return (p > -1 ? s.substring(p + 1) : s);
    }

    /* Construct the detailed exception message to be displayed */
    private String detailedExceptionMessage()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("[");
        sb.append(getShortClassName());
        sb.append(" wraps [");
        sb.append(m_exc == null ? "null" : wrappedExceptionStackTrace());
        sb.append("]]");
        return sb.toString();
    }
}

