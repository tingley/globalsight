package com.globalsight.ling.util;

/* Copyright (c) 2000, Global Sight Corporation.  All rights reserved. */

import java.io.CharArrayWriter;
import java.io.PrintWriter;
import java.io.PrintStream;

/**
 * General exception that can be thrown by any component.  Every
 * instance of this exception must contain a "component id" that
 * indicates the component where this exception originated, and an
 * "exception id" that indicates what the exception is about.
 * <p>
 * This exception can optionally contain the original exception that
 * was caught immediately before this exception is thrown.
 * 
 * @version     1.0, (1/3/00 1:08:39 AM)
 * @author      Marvin Lau, mlau@globalsight.com
 */

/*
 * MODIFIED     MM/DD/YYYY
 * mlau         01/02/2000   Initial version.
 * mlau         04/14/2000   Capture and return stack trace;
 *                           Add message id as optional parameter.
 * mlau         04/17/2000   Add method to return the message id.
 */

public class GeneralException 
    extends Exception 
    implements GeneralExceptionConstants
{
	/**
	 * Default message to be used when no specific message is defined
	 * for the exception component and id in this exception.
	 */
	static final String DEFAULT_MESSAGE = "GeneralException";

	/**
	 * Default message id to be used when no message id is
	 * specified for this exception.
	 */
	static final int DEFAULT_MSG_ID = 0;

	/**
	 * The original exception that gave rise to this general exception.
	 * This field can be null, which means that there was no original
	 * exception.
	 */
	Exception m_originalException;

	/**
	 * The component that threw this exception.
	 */
	int m_componentId;

	/**
	 * This id indicates what the exception is about.
	 */
	int m_exceptionId;

	/**
	 * This is the id of the associate message.  0 indicates
	 * no specific message.
	 */
	int m_messageId;

	/**
	 * This is the stack trace of the original exception.
	 */
	String m_originalStackTrace;
/**
 * Constructs an instance using the given component and exception
 * identification.
 *
 * @param p_componentId Component where the exception originated from.
 * @param p_exceptionId Reason for the exception.
 */
public GeneralException(int p_componentId, int p_exceptionId)
{
	this(p_componentId, p_exceptionId, DEFAULT_MSG_ID);
}
/**
 * Constructs an instance using the given component, exception
 * and message identification.
 *
 * @param p_componentId Component where the exception originated from.
 * @param p_exceptionId Reason for the exception.
 * @param p_messageId Explanation of the exception.
 */
public GeneralException(int p_componentId, int p_exceptionId, int p_messageId)
{
	super();
	m_originalException = null;
	m_componentId = p_componentId;
	m_exceptionId = p_exceptionId;
	m_messageId   = p_messageId;
}
/**
 * Constructs an instance using the given component and exception
 * identification, and the original exception.
 *
 * @param p_componentId Component where the exception originated from.
 * @param p_exceptionId Reason for the exception.
 * @param p_messageId Explanation of the exception.
 * @param p_originalException Original exception that this exception identifies.
 */
public GeneralException(int p_componentId, int p_exceptionId, int p_messageId, Exception p_originalException)
{
	super();
	m_componentId = p_componentId;
	m_exceptionId = p_exceptionId;
	m_messageId   = p_messageId;
	m_originalException = p_originalException;
}
/**
 * Constructs an instance using the given component and exception
 * identification, and the original exception.
 *
 * @param p_componentId Component where the exception originated from.
 * @param p_exceptionId Reason for the exception.
 * @param p_originalException Original exception that this exception identifies.
 */
public GeneralException(int p_componentId, int p_exceptionId, Exception p_originalException)
{
	this(p_componentId, p_exceptionId, DEFAULT_MSG_ID, p_originalException);
}
/**
 * Constructs an instance using the given component and exception
 * identification.
 *
 * @param p_componentId Component where the exception originated from.
 * @param p_exceptionId Reason for the exception.
 * @param p_message Explanation of the exception.
 */
public GeneralException(int p_componentId, int p_exceptionId, String p_message)
{
	super(p_message);
	m_originalException = null;
	m_componentId = p_componentId;
	m_exceptionId = p_exceptionId;
	m_messageId   = DEFAULT_MSG_ID;
}
/**
 * Get the identification of the component where this exception
 * orginated.
 *
 * @return The identification of the component where this exception
 *         orginated.
 */
public int getComponentId()
{
	return m_componentId;
}
/**
 * Get the code that indicates what this exception is about.
 * @return The code that indicates what this exception is about.
 */
public int getExceptionId()
{
	return m_exceptionId;
}
/**
 * Get the identification of the message associated with the problem
 * that this exception is about.
 *
 * @return the identification of the message associated with the problem
 *         that this exception is about.
 */
public int getMessageId()
{
	return m_messageId;
}
/**
 * Get the original exception that gave rise to this application
 * general exception.
 * 
 * @return The original exception.
 */
public Exception getOriginalException()
{
	return m_originalException;
}
/**
 * Get the stack trace of the original exception.
 * If there is no original exception, null is returned.
 *
 * @return Stack trace of the original exception.
 */
public String getOriginalStackTrace()
{
	if (m_originalException != null)
	{
		if (m_originalStackTrace == null)
		{
			CharArrayWriter outBuffer = new CharArrayWriter();
			m_originalException.printStackTrace(new java.io.PrintWriter(outBuffer));
			m_originalStackTrace = outBuffer.toString();
		}
	}
	return m_originalStackTrace;
}
/**
 * Prints the component id and exception id, and then followed
 * by the standard stack trace.
 */
public void printStackTrace()
{
	System.err.println("Component ID: " + m_componentId
		+ "; Exception ID: " + m_exceptionId + ".");
	super.printStackTrace();
}
/**
 * Prints the component id and exception id, and then followed
 * by the standard stack trace, on the given print stream.
 */
public void printStackTrace(PrintStream ps)
{
	ps.println("Component ID: " + m_componentId
		+ "; Exception ID: " + m_exceptionId + ".");
	super.printStackTrace(ps);
}
/**
 * Prints the component id and exception id, and then followed
 * by the standard stack trace, on the given print writer.
 */
public void printStackTrace(PrintWriter pw)
{
	pw.println("Component ID: " + m_componentId
		+ "; Exception ID: " + m_exceptionId + ".");
	super.printStackTrace(pw);
}
}
