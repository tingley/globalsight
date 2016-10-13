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

package com.globalsight.everest.webapp.tags;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

/**
 * All custom tags should use this when they need to report
 * an internal error.<p>
 *
 * If the exception condition originated with another exception, that can be
 * provided to the constructor.
 */
public class AmbassadorTagException extends JspException
{
    private static final long serialVersionUID = 8079215877639621384L;

    private final Exception  origExc; 
    private final String     toStringConjunction = "originally ";


    /**
     * Constructor.
     *
     * @param pageContext - the pageContext so that a handle can be
     *                      gotten to the HttpSession
     *
     * @param msg - the error message
     */
    public AmbassadorTagException(PageContext pageContext, String msg)
    {
        super(msg);
        HttpSession session = pageContext.getSession();
        session.setAttribute("AmbassadorTagError", msg);
        origExc = null;
    } 


    /**
     * Construct a AmbassadorTagException from the origExc with no description
     * of the enclosing (new) transaction.  The toStringConjunction will be
	 * used in the "toString" value to introduce the "toString" value of the
	 * original exception, where it will be surrounded by *no* spaces or
	 * punctuation.  (See the example in {@link #toString()}.)
	 */

	public AmbassadorTagException (PageContext pageContext, Exception origExc)
    {
		this(pageContext, origExc, null);
    }
    
    
    /**
     * Construct a AmbassadorTagException from the origExc and message
     * describing the enclosing (new) transaction.  The enclosingMsg should
	 * be null if it is redundant of the "getMessage" value of the origExc.
	 * The toStringConjunction will be used in the "toString" value to
	 * introduce the "toString" value of the original exception, where it will
	 * be surrounded by *no* spaces or punctuation.
	 *  (See the example in {@link #toString()}.)<p>
	 *
     * Recall that all exception messages are for debugging purposes only.
	 * (Any semantic information to be communicated to the user must be
	 * indicated in the exception name and fields.)
	 */
	public AmbassadorTagException (PageContext pageContext, Exception origExc,
						 String enclosingMsg)
    {
		super(enclosingMsg);
		if (origExc == null || toStringConjunction == null) {
			throw new NullPointerException();
		}
		this.origExc = origExc;
		HttpSession session = pageContext.getSession();
		session.setAttribute("AmbassadorTagError", getMessage());
    }


    /**
     * Return the original exception - the one which was translated into
	 * this exception.
	 */
	public Exception getOrigExc ()
    {
		return origExc;
    }
    
    
    /**
     * Return a user-readable description of the error composed from the
	 * messages of this and the enclosed exception.  May return null, in 
     * which case it is typically appropriate to use the "toString" value as a 
     * description.<p>
	 *
     * Example return value: <pre>
     *  Requested database access denied: Invalid user or password</pre>
	 */
	public String getMessage ()
    {
		String enclosingMsg = super.getMessage();
		if (origExc == null) {
			 return enclosingMsg;
		}
		String origMsg = origExc.getMessage();
		if (enclosingMsg == null && origMsg == null) {
			 return null;
		}
		if (enclosingMsg != null && origMsg == null) {
			return enclosingMsg;
		}
		if (enclosingMsg == null && origMsg != null) {
			 return origMsg;
		}
		return enclosingMsg + ": " + origMsg;
    }


    /**
     * Return a short programmer-readable description of this exception.  
     * Includes any message from the original exception.  Following the
	 * practice of "Throwable.toString" this will include the class and
	 * "getMessage" value of this exception, but it will also include the
	 * "getMessage" value of the original exception introduced with the
	 * toStringConjunction.<p>
	 *
     * For example, if DBAccessDenied is a [subclass of] TranslatedExc from
	 * a SQLException with a toStringConjunction of "via ", the result would
	 * be something like: <pre>
     *   com.acme.dbutil.DBAccessDenied: Requested database access denied\n
     *       (via java.sql.SQLException: Invalid user or password) </pre>
	 *
     * Either of the two msgs might be missing.  If both were missing, the
	 * result would be something like: <pre>
     *   com.acme.dbutil.DBAccessDenied\n
     *       (via java.sql.SQLException) </pre>
	 */
	public String toString ()
    {
		if (origExc == null) return super.toString();
		String enclosingMsg = super.getMessage();
		return getClass().getName() + 
				(enclosingMsg == null ? "" : (": " + enclosingMsg)) +
				"\n    (" + toStringConjunction + origExc + ")";
    }


    /**
     * Print this exception (its "toString" value) and its stack trace to 
     * System.err.  Add to that the original exception and its stack trace.  (Any 
     * message from the original exception is printed only once.)
     */
	public void printStackTrace ()
    {
		if (origExc == null)
			super.printStackTrace();
		else {
			System.err.println(toString());
			System.err.print(stackTraceNoMsg(origExc));
		}
		// will flush on final newline
    }


    /**
     * Print this exception (its "toString" value) and its backtrace to the 
     * given PrintStream.  Add to that the original exception and its stack 
     * trace. (Any message from the original exception is printed only once.)
	 */
	public void printStackTrace (PrintStream s)
    {
		if (origExc == null)
			super.printStackTrace(s);
		else {
			s.println(toString());
			s.print(stackTraceNoMsg(origExc));
		}
    }


    /**
     * Print this exception (its "toString" value) and its stack trace to the 
     * given PrintWriter.  Add to that the original exception and its stack
     * trace.  (Any message from the original exception is printed only once.)
	 */
	public void printStackTrace (PrintWriter s)
    {
		s.println("<pre>");  // since JSP doesn't do this; ugh
		if (origExc == null) {
			super.printStackTrace(s);
		} else {
			s.println(toString());
			s.print(stackTraceNoMsg(origExc));
			s.flush();
		} 
		s.println("</pre>");
		s.flush();
    }
    
    
    /**
     * Return the printStackTrace result of throwable stripped of any
	 * message of the throwable.
	 */
	private static String stackTraceNoMsg (Throwable throwable)
    {
		StringWriter writer = new StringWriter();
		throwable.printStackTrace(new PrintWriter(writer));  // rethrow NullPtr
		String trace = writer.toString();
		String msg = throwable.getMessage();
		if (msg == null) return trace;
		int i = trace.indexOf(msg);
		if (i < 0) return trace;
		return trace.substring(0, i) + trace.substring(i + msg.length());
    }

}
