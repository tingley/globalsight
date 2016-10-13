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

package com.globalsight.util;

import org.apache.log4j.Logger;

import com.globalsight.util.resourcebundle.SystemResourceBundle;
import com.globalsight.util.resourcebundle.ResourceBundleConstants;
import java.io.CharArrayWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import com.sun.org.apache.xerces.internal.dom.DocumentImpl;
import org.apache.xerces.parsers.SAXParser;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

/**
 * <p>
 * General exception that can be thrown by any component. See
 * envoy/doc/Design/ErrorMessage.doc for detailed description.
 * </p>
 * 
 * @version 1.0, (1/3/00 1:08:39 AM)
 * @author Marvin Lau, mlau@globalsight.com
 * @see "envoy/doc/Design/ErrorMessage.doc"
 */

public class GeneralException extends RuntimeException implements
        GeneralExceptionConstants
{
    /**
     * 
     */
    private static final long serialVersionUID = -2067357005253595605L;

    private Logger m_logger = null;

    /**
     * The component that threw this exception.
     * 
     * @deprecated
     */
    private int m_componentId;

    /**
     * ID indicating what the exception is about.
     * 
     * @deprecated
     */
    private int m_exceptionId;

    /**
     * ID of the associate message. DEFAULT_MSG_ID indicates no specific
     * message.
     * 
     * @deprecated
     */
    private int m_messageId = DEFAULT_MSG_ID;

    /**
     * The error message to be displayed.
     * 
     * @deprecated
     */
    private String m_errorMessage = null;

    /**
     * The stack trace of the original exception.
     * 
     * @deprecated
     */
    private String m_originalStackTrace;

    /**
     * The message arguments for the compound messages.
     */
    private String[] m_messageArguments = null;

    /**
     * The original exception that gave rise to this general exception. This
     * field can be null, which means that there was no original exception.
     */
    private Exception m_originalException = null;

    // Use String member which is Serializable to transfer via JMS
    private String m_originalMessage = null;

    /**
     * Message key in the error message property file.
     */
    private String m_messageKey = null;

    /**
     * Property file name. This is used when designating the property file other
     * than the default one.
     */
    private String m_propertyFileName = null;

    private static final String GENERAL_EXCEPTION_QNAME = "com.globalsight.util.GeneralException";

    private static final String RESOURCE_PACKAGE_NAME = "com.globalsight.resources.messages.";

    // element name of serialized xml
    private static final String GENERAL_EXCEPTION = "GeneralException";
    private static final String JAVA_EXCEPTION = "JavaException";
    private static final String ORIGINALMESSAGE = "originalMessage";
    private static final String KEY = "key";
    private static final String ARGS = "args";
    private static final String ARG = "arg";
    private static final String MESSAGEFILE = "messageFile";
    private static final String STACKTRACE = "stackTrace";
    private static final String MESSAGE = "message";

    // Message resource cache
    private static Hashtable m_resourceCache = new Hashtable();

    // stack trace storage. Used only in the recreated object by
    // deserialize()
    private String m_stackTrace = null;

    // message storage. Used only in the recreated object by
    // deserialize()
    private String m_message = null;

    /**
     * <p>
     * It seems that TOPlink cannot import classes that use GeneralException
     * directly or indirectly without this default constructor. Will investigate
     * a way to avoid having this.
     * </p>
     */
    public GeneralException()
    {
    }

    /**
     * <p>
     * Constructs an instance using the given component id and exception
     * identification.
     * </p>
     * 
     * @param p_componentId
     *            Component where the exception originated from.
     * @param p_exceptionId
     *            Reason for the exception.
     * 
     * @deprecated Component ID and Exception ID are not used anymore.
     */
    public GeneralException(int p_componentId, int p_exceptionId)
    {
        this(p_componentId, p_exceptionId, DEFAULT_MSG_ID, null);
    }

    /**
     * <p>
     * Constructs an instance using the given component id and exception
     * identification, and the original exception.
     * </p>
     * 
     * @param p_componentId
     *            Component where the exception originated from.
     * @param p_exceptionId
     *            Reason for the exception.
     * @param p_originalException
     *            Original exception that this exception identifies.
     * 
     * @deprecated Component ID and Exception ID are not used anymore.
     */
    public GeneralException(int p_componentId, int p_exceptionId,
            Exception p_originalException)
    {
        this(p_componentId, p_exceptionId, DEFAULT_MSG_ID, p_originalException);
    }

    /**
     * <p>
     * Constructs an instance using the given component id and exception
     * identification.
     * </p>
     * 
     * @param p_componentId
     *            Component where the exception originated from.
     * @param p_exceptionId
     *            Reason for the exception.
     * @param p_message
     *            Explanation of the exception.
     * 
     * @deprecated Component ID and Exception ID are not used anymore.
     */
    public GeneralException(int p_componentId, int p_exceptionId,
            String p_message)
    {
        super(p_message);
        m_errorMessage = p_message;
        m_originalException = null;
        m_componentId = p_componentId;
        m_exceptionId = p_exceptionId;
        m_messageId = DEFAULT_MSG_ID;
    }

    /**
     * <p>
     * Constructs an instance using the given component id and exception
     * identification.
     * </p>
     * 
     * @param p_componentId
     *            Component where the exception originated from.
     * @param p_exceptionId
     *            Reason for the exception.
     * @param p_message
     *            Explanation of the exception.
     * @param p_originalException
     *            Original exception that this exception identifies.
     * 
     * @deprecated Component ID and Exception ID are not used anymore.
     */
    public GeneralException(int p_componentId, int p_exceptionId,
            String p_message, Exception p_originalException)
    {
        super(p_message);
        m_errorMessage = p_message;
        m_originalException = p_originalException;
        m_componentId = p_componentId;
        m_exceptionId = p_exceptionId;
        m_messageId = DEFAULT_MSG_ID;
    }

    /**
     * <p>
     * Constructs an instance using the given component id, exception id and
     * message identification.
     * </p>
     * 
     * @param p_componentId
     *            Component where the exception originated from.
     * @param p_exceptionId
     *            Reason for the exception.
     * @param p_messageId
     *            Explanation of the exception.
     * 
     * @deprecated Component ID and Exception ID are not used anymore.
     */
    public GeneralException(int p_componentId, int p_exceptionId,
            int p_messageId)
    {
        this(p_componentId, p_exceptionId, p_messageId, null);
    }

    /**
     * <p>
     * Constructs an instance using the given component id and exception
     * identification, message identification, and the original exception.
     * </p>
     * 
     * @param p_componentId
     *            Component where the exception originated from.
     * @param p_exceptionId
     *            Reason for the exception.
     * @param p_messageId
     *            Explanation of the exception.
     * @param p_originalException
     *            Original exception that this exception identifies.
     * 
     * @deprecated Component ID and Exception ID are not used anymore.
     */
    public GeneralException(int p_componentId, int p_exceptionId,
            int p_messageId, Exception p_originalException)
    {
        this(p_componentId, p_exceptionId, p_messageId, null,
                p_originalException);
    }

    /**
     * <p>
     * Constructs an instance using the given component, exception
     * identification, message identification, and the original exception.
     * </p>
     * 
     * <p>
     * NOTE: THIS CONSTRUCTOR IS USED FOR COMPOUND ERROR MESSAGES.
     * </p>
     * 
     * @param p_componentId
     *            Component where the exception originated from.
     * @param p_exceptionId
     *            Reason for the exception.
     * @param p_messageId
     *            Explanation of the exception.
     * @param p_messageArguments
     *            The message arguments in order.
     * @param p_originalException
     *            Original exception that this exception identifies.
     * 
     * @deprecated Component ID and Exception ID are not used anymore.
     */
    public GeneralException(int p_componentId, int p_exceptionId,
            int p_messageId, String[] p_messageArguments,
            Exception p_originalException)
    {
        super();

        m_componentId = p_componentId;
        m_exceptionId = p_exceptionId;
        m_messageId = p_messageId;
        m_messageArguments = p_messageArguments;
        m_originalException = p_originalException;
        if (p_originalException != null)
        {
            m_originalMessage = p_originalException.getMessage();
        }

        if (m_messageId != DEFAULT_MSG_ID)
        {
            ResourceBundle resourceBundle = SystemResourceBundle.getInstance()
                    .getResourceBundle(
                            ResourceBundleConstants.EXCEPTION_RESOURCE_NAME,
                            Locale.getDefault());

            if (resourceBundle == null)
            {
                m_errorMessage = "ResourceBundle not found: "
                        + ResourceBundleConstants.EXCEPTION_RESOURCE_NAME
                        + " "
                        + Locale.getDefault().toString()
                        + " "
                        + Integer.toString(m_messageId)
                        + " "
                        + (m_messageArguments != null ? m_messageArguments
                                .toString() : "null");
            }
            else
            {
                m_errorMessage = resourceBundle.getString(Integer
                        .toString(m_messageId));
                if (m_messageArguments != null)
                {
                    m_errorMessage = MessageFormat.format(m_errorMessage,
                            m_messageArguments);
                }
            }
        }
        else if (p_componentId != 0 && p_exceptionId != 0)
        {
            switch (p_componentId)
            {
                case COMP_FOUNDATION:
                    m_errorMessage = FOUNDATION;
                    break;
                case COMP_JOBS:
                    m_errorMessage = JOBS;
                    break;
                case COMP_LING:
                    m_errorMessage = LING;
                    break;
                case COMP_LOCALEMANAGER:
                    m_errorMessage = LOCALEMANAGER;
                    break;
                case COMP_PERSISTENCE:
                    m_errorMessage = PERSISTENCE;
                    break;
                case COMP_REQUEST:
                    m_errorMessage = REQUEST;
                    break;
                case COMP_SERVLET:
                    m_errorMessage = SERVLET;
                    break;
                case COMP_USERMANAGER:
                    m_errorMessage = USERMANAGER;
                    break;
                case COMP_ENVOYSYSTEM:
                    m_errorMessage = ENVOYSYSTEM;
                    break;
                case COMP_WEBAPP:
                    m_errorMessage = WEBAPP;
                    break;
                case COMP_WORKFLOW:
                    m_errorMessage = WORKFLOW;
                    break;
                case COMP_GENERAL:
                    m_errorMessage = GENERAL;
                    break;
                case COMP_SYSUTIL:
                    m_errorMessage = SYSUTIL;
                    break;
                case COMP_PROJECT:
                    m_errorMessage = PROJECT;
                    break;
                case COMP_SECURITYMANAGER:
                    m_errorMessage = SECURITYMANAGER;
                    break;
                case COMP_GXML:
                    m_errorMessage = GXML;
                    break;
                case COMP_WORKFLOWMANAGER:
                    m_errorMessage = WORKFLOWMANAGER;
                    break;
                case COMP_PAGEIMPORTER:
                    m_errorMessage = PAGEIMPORTER;
                    break;
                case COMP_ONLINEEDITOR:
                    m_errorMessage = ONLINEEDITOR;
                    break;
                case COMP_OFFLINEEDITMANAGER:
                    m_errorMessage = OFFLINEEDITMANAGER;
                    break;
                case COMP_SEGMENTER:
                    m_errorMessage = SEGMENTER;
                    break;
                case COMP_MERGER:
                    m_errorMessage = MERGER;
                    break;
                case COMP_EXTRACTOR:
                    m_errorMessage = EXTRACTOR;
                    break;
                case COMP_WORDCOUNTER:
                    m_errorMessage = WORDCOUNTER;
                    break;
                default:
                    m_errorMessage = "unknown component";
                    break;
            }

            m_errorMessage += ": ";

            switch (p_exceptionId)
            {
                case EX_GENERAL:
                    m_errorMessage += "general exception";
                    break;
                case EX_JMS:
                    m_errorMessage += "JMS exception";
                    break;
                case EX_MISSING_RESOURCE_EXCEPTION:
                    m_errorMessage += "missing resource exception";
                    break;
                case EX_NAMING:
                    m_errorMessage += "naming exception";
                    break;
                case EX_PROPERTIES:
                    m_errorMessage += "property access exception";
                    break;
                case EX_REMOTE:
                    m_errorMessage += "remote or network exception";
                    break;
                case EX_SQL:
                    m_errorMessage += "SQL exception";
                    break;
                case EX_GLOSSARY:
                    m_errorMessage += "glossary exception";
                    break;
                case EX_COMMENT_REFERENCE:
                    m_errorMessage += "comment reference exception";
                    break;
                case EX_NATIVE_FILE:
                    m_errorMessage += "native file exception";
                    break;
                case MSG_FAILED_TO_IMPORT_MIF:
                    m_errorMessage += "improper mif version exception";
                    break;
                default:
                    m_errorMessage += "unknown exception";
                    break;
            }
        }
        else
        {
            m_errorMessage = DEFAULT_MESSAGE;
        }
    }

    /**
     * @see GeneralException#GeneralException(int, int, int, String)
     * @param p_message
     *            error message.
     * 
     * @deprecated It doesn't take a raw message any more
     */
    public GeneralException(String p_message)
    {
        this(GeneralExceptionConstants.EX_GENERAL,
                GeneralExceptionConstants.COMP_FOUNDATION, p_message);
    }

    /**
     * @see GeneralException#GeneralException(int, int, int, String)
     * @param p_message
     *            error message.
     * @param p_message
     *            error message.
     * 
     * @deprecated It doesn't take a raw message any more
     */
    public GeneralException(String p_message, Exception p_originalException)
    {
        this(GeneralExceptionConstants.EX_GENERAL,
                GeneralExceptionConstants.COMP_FOUNDATION, p_message,
                p_originalException);
    }

    public String getOriginalMessage()
    {
        return m_originalMessage;
    }

    public Logger getLogger()
    {
        if (m_logger == null)
        {
            m_logger = Logger.getLogger(GeneralException.class);
        }
        return m_logger;
    }

    public void setLogger(Logger logger)
    {
        m_logger = logger;
    }

    /**
     * <p>
     * Get the identification of the component where this exception orginated.
     * </p>
     * 
     * @return The identification of the component where this exception
     *         orginated.
     * 
     * @deprecated Component ID is not used anymore.
     */
    public int getComponentId()
    {
        return m_componentId;
    }

    /**
     * <p>
     * Get the name of the component where this exception originated.
     * </p>
     * 
     * @return The name of the component where this exception originated.
     * 
     * @deprecated Component ID is not used anymore.
     */
    public String getComponentName()
    {
        switch (m_componentId)
        {
            case COMP_FOUNDATION:
                return FOUNDATION;
            case COMP_JOBS:
                return JOBS;
            case COMP_LING:
                return LING;
            case COMP_LOCALEMANAGER:
                return LOCALEMANAGER;
            case COMP_PERSISTENCE:
                return PERSISTENCE;
            case COMP_REQUEST:
                return REQUEST;
            case COMP_SERVLET:
                return SERVLET;
            case COMP_USERMANAGER:
                return USERMANAGER;
            case COMP_ENVOYSYSTEM:
                return ENVOYSYSTEM;
            case COMP_WEBAPP:
                return WEBAPP;
            case COMP_WORKFLOW:
                return WORKFLOW;
            case COMP_GENERAL:
                return GENERAL;
            case COMP_SYSUTIL:
                return SYSUTIL;
            case COMP_PROJECT:
                return PROJECT;
            case COMP_SECURITYMANAGER:
                return SECURITYMANAGER;
            case COMP_GXML:
                return GXML;
            case COMP_WORKFLOWMANAGER:
                return WORKFLOWMANAGER;
            case COMP_PAGEIMPORTER:
                return PAGEIMPORTER;
            case COMP_ONLINEEDITOR:
                return ONLINEEDITOR;
            case COMP_OFFLINEEDITMANAGER:
                return OFFLINEEDITMANAGER;
        }

        return GENERAL;
    }

    /**
     * <p>
     * Get the code that indicates what this exception is about.
     * </p>
     * 
     * @return The code that indicates what this exception is about.
     * 
     * @deprecated Exception ID is not used anymore.
     */
    public int getExceptionId()
    {
        return m_exceptionId;
    }

    /**
     * <p>
     * Get the identification of the message associated with the problem that
     * this exception is about.
     * </p>
     * 
     * @return The identification of the message associated with the problem
     *         that this exception is about.
     * 
     * @deprecated Message ID changed to String
     */
    public int getMessageId()
    {
        return m_messageId;
    }

    /**
     * <p>
     * Get the original exception that gave rise to this application general
     * exception.
     * </p>
     * 
     * @return The original exception.
     * 
     * @deprecated Necessary?
     */
    public Exception getOriginalException()
    {
        return m_originalException;
    }

    /**
     * <p>
     * Get the stack trace of the original exception. If there is no original
     * exception, null is returned.
     * </p>
     * 
     * @return Stack trace of the original exception.
     * 
     * @deprecated It has been replaced by {@link #getStackTrace getStackTrace}.
     */
    public String getOriginalStackTrace()
    {
        if (m_originalException != null)
        {
            if (m_originalStackTrace == null)
            {
                CharArrayWriter outBuffer = new CharArrayWriter();
                m_originalException.printStackTrace(new PrintWriter(outBuffer));
                m_originalStackTrace = outBuffer.toString();
            }
        }

        return m_originalStackTrace;
    }

    /**
     * Get the stack trace the Throwable.
     * 
     * @param p_throwable
     *            a Throwable
     * @return Stack trace of p_throwable
     */
    public static String getStackTraceString(Throwable p_throwable)
    {
        CharArrayWriter outBuffer = new CharArrayWriter();
        p_throwable.printStackTrace(new java.io.PrintWriter(outBuffer));
        return outBuffer.toString();
    }

    // ////////////////////// New Methods //////////////////////////////

    /**
     * <p>
     * Constructs an instance using the given Exception object. The primary use
     * of this constructor is to wrap the other exception in GeneralException.
     * </p>
     * 
     * @param p_originalException
     *            Original exception that caused the error
     */
    public GeneralException(Exception p_originalException)
    {
        m_originalException = p_originalException;
    }

    /**
     * <p>
     * Constructs an instance using the given key in the error message property
     * file, the arguments to the messages and the Exception object.
     * </p>
     * 
     * @param p_messageKey
     *            key in properties file
     * @param p_messageArguments
     *            Arguments to the message. It can be null.
     * @param p_originalException
     *            Original exception that caused the error. It can be null.
     */
    public GeneralException(String p_messageKey, String[] p_messageArguments,
            Exception p_originalException)
    {
        m_messageKey = p_messageKey;
        m_messageArguments = p_messageArguments;
        m_originalException = p_originalException;
    }

    /**
     * <p>
     * Constructs an instance using the given key in the error message property
     * file, the arguments to the message, the Exception object and the property
     * file name. This is used in the sub-classes to explicitly designate the
     * property file for the message in the object.
     * </p>
     * 
     * @param p_messageKey
     *            key in properties file
     * @param p_messageArguments
     *            Arguments to the message. It can be null.
     * @param p_originalException
     *            Original exception that caused the error. It can be null.
     * @param p_propertyFileName
     *            Property file base name. If the property file is
     *            LingMessage.properties, the parameter should be "LingMessage".
     */
    protected GeneralException(String p_messageKey,
            String[] p_messageArguments, Exception p_originalException,
            String p_propertyFileName)
    {
        if (p_originalException != null)
        {
            m_originalMessage = p_originalException.getMessage();
        }
        m_messageKey = p_messageKey;
        m_messageArguments = p_messageArguments;
        m_propertyFileName = p_propertyFileName;
    }

    /**
     * <p>
     * Returns a message in the specified locale. If the exception has the
     * nested exception objects (original exception) in it, the messages of
     * these original exceptions will also be returned.
     * </p>
     * 
     * @param p_uiLocale
     *            UI locale
     * @return Error message
     */
    public String getMessage(Locale p_uiLocale)
    {
        // Old stuff. Should be removed once old codes are removed.
        if (m_messageId != DEFAULT_MSG_ID)
        {
            ResourceBundle resourceBundle = SystemResourceBundle.getInstance()
                    .getResourceBundle(
                            ResourceBundleConstants.EXCEPTION_RESOURCE_NAME,
                            p_uiLocale);

            if (resourceBundle == null)
            {
                return "ResourceBundle not found: "
                        + ResourceBundleConstants.EXCEPTION_RESOURCE_NAME
                        + " "
                        + p_uiLocale.toString()
                        + " "
                        + Integer.toString(m_messageId)
                        + " "
                        + (m_messageArguments != null ? m_messageArguments
                                .toString() : "null");
            }

            String value = resourceBundle.getString(Integer
                    .toString(m_messageId));

            if (m_messageArguments != null)
            {
                value = MessageFormat.format(value, m_messageArguments);
            }

            return value;
        }

        // get the message of this instance
        String message = getOwnMessage(p_uiLocale);

        // Call getMessage recursively
        if (m_originalException instanceof GeneralException)
        {
            if (message == null)
            {
                message = new String();
            }
            else
            {
                message += "\n";
            }

            message += ((GeneralException) m_originalException)
                    .getMessage(p_uiLocale);
        }
        else if (m_originalException != null)
        {
            if (message == null)
            {
                message = new String();
            }
            else
            {
                message += "\n";
            }

            message += m_originalException.toString();
        }

        return message;
    }

    /**
     * Returns message key
     * 
     * @return String message key
     */
    public String getMessageKey()
    {
        return m_messageKey;
    }

    /**
     * Get the message arguments.
     * 
     * @return The message arguments.
     */
    public String[] getMessageArguments()
    {
        return m_messageArguments;
    }

    /**
     * <p>
     * Returns a stack trace of the exception. If the exception has the nested
     * exception objects (original exception) in it, all the exceptions' stack
     * trace are returned. The locale of the trace is the default locale of the
     * system.
     * </p>
     * 
     * @return Stack trace
     */
    public String getStackTraceString()
    {
        StringBuffer res = new StringBuffer(getStackTraceString(this));

        // Call getStackTrace recursively
        if (m_originalException instanceof GeneralException)
        {
            res.append('\n');
            res.append(((GeneralException) m_originalException)
                    .getStackTraceString());
        }
        else if (m_originalException != null)
        {
            res.append('\n');
            res.append(getStackTraceString(m_originalException));
        }

        return res.toString();
    }

    /**
     * <p>
     * Returns a serialized form of the exception as a String. It has nothing to
     * do with Java serialization. The string can be used to send the exception
     * via HTTP between CXE and CAP. As the string is not URL encoded, it's a
     * programmer's responsibility to encode it appropriately.
     * </p>
     * 
     * <p>
     * The current implementation is to create a XML document as a serialized
     * form. The following is the DTD.
     * </p>
     * 
     * <p>
     * <!ELEMENT GeneralException (key?, args?, messageFile, stackTrace,
     * (GeneralException | JavaException)?) > <!ELEMENT key (#PCDATA) >
     * <!ELEMENT args (arg+) > <!ELEMENT arg (#PCDATA) > <!ELEMENT messageFile
     * (#PCDATA) > <!ELEMENT stackTrace (#PCDATA) > <!ELEMENT JavaException
     * (message, stackTrace) > <!ELEMENT message (#PCDATA) >
     * 
     * <!-- for javadoc -->
     * 
     * <pre>
     * &lt;!ELEMENT GeneralException (key?, args?, messageFile, stackTrace, (GeneralException | JavaException)?) &gt;
     * &lt;!ELEMENT key (#PCDATA) &gt;
     * &lt;!ELEMENT args (arg+) &gt;
     * &lt;!ELEMENT arg (#PCDATA) &gt;
     * &lt;!ELEMENT messageFile (#PCDATA) &gt;
     * &lt;!ELEMENT stackTrace (#PCDATA) &gt;
     * &lt;!ELEMENT JavaException (message, stackTrace) &gt;
     * &lt;!ELEMENT message (#PCDATA) &gt;
     * </pre>
     * 
     * @return Serialized exception
     */
    public final String serialize() throws GeneralException
    {
        String xml = null;
        Node root = null;

        try
        {
            // generate DOM
            Document doc = new DocumentImpl();
            root = writeGeneralExceptionElement(doc);
            doc.appendChild(root);

            // Serialize DOM
            OutputFormat format = new OutputFormat(doc);
            format.setPreserveSpace(true); // Preserve space
            format.setIndenting(true); // Do indent
            // format.setLineWidth(0); // Do not do line wrap

            StringWriter stringOut = new StringWriter();
            XMLSerializer serial = new XMLSerializer(stringOut, format);
            // ?? serial.asDOMSerializer();
            serial.serialize(doc.getDocumentElement());
            xml = stringOut.toString();
        }
        catch (Exception e)
        {
            String message = (root != null ? root.toString() : "null root")
                    + e.getMessage();
            getLogger().error(message, e);
            return message;
        }

        return xml;
    }

    /**
     * <p>
     * Recreates a GeneralException (and derived) object from a serialized
     * exception that is created by serialize(). It is used to restore the
     * object sent via HTTP request. Note that the returned object is not the
     * same type as the original. The sole purpose of serializing/deserializing
     * the exceptions is to retrieve the localized error messages properly.
     * </p>
     * 
     * <p>
     * deserialize creates a GeneralException object, not the original object.
     * It even creates GeneralException for a nested exception that is NOT a
     * subclass of GeneralException. The purpose of the recreation is just to
     * get appropriate localized error messages. The type of the exception
     * doesn't really matter.
     * </p>
     * 
     * @param p_serializedException
     *            Serialized exception
     * @return Re-created GeneralException.
     */
    public final static GeneralException deserialize(
            String p_serializedException) throws GeneralException
    {
        // TODO
        // The parser should be a validating parser

        SAXParser parser = new SAXParser();
        GeneralExceptionHandler handler = new GeneralExceptionHandler();
        parser.setContentHandler(handler);

        try
        {
            parser.parse(new InputSource(
                    new StringReader(p_serializedException)));
        }
        catch (Exception e)
        {
            GeneralException ge = new GeneralException(p_serializedException, e);
            ge.getLogger().error(ge.getMessage(), ge);
            return ge;
        }

        return handler.getObject();
    }

    /**
     * Overrides java.lang.Throwable#getMessage(). This method returns only its
     * own message (not including the nested exception's) in system's locale.
     * The main purpose of it is to serve for stack trace. See
     * java.lang.Throwable#toString() and java.lang.Throwable#printStackTrace()
     * for the structure of stack trace.
     */
    public String getMessage()
    {
        if (m_originalMessage != null)
        {
            return m_originalMessage;
        }
        // Old stuff. Should be removed once the old code is cleaned up.
        if (m_errorMessage != null)
        {
            return m_errorMessage;
        }

        return getOwnMessage(Locale.getDefault());
    }

    /**
     * @see Exception#getLocalizedMessage
     */
    public String getLocalizedMessage()
    {
        return getMessage();
    }

    /**
     * Return a simple string representation of the object. Note that
     * super.toString() already calls getLocalizedMessage(), so there is really
     * no need to overload this function.
     */
    public String toString()
    {
        return super.toString() /* + " : " + getMessage() */;
    }

    /**
     * <p>
     * Returns the first instance of a nested Exception of the specified class.
     * Returns null if not found.
     * </p>
     * 
     * @param p_class
     *            Class of nested exception to find
     * @return the first instance of a nested Exception of the specified class.
     *         Returns null if not found.
     */
    public Exception containsNestedException(Class p_class)
    {
        if (m_originalException == null)
        {
            return null;
        }

        if (m_originalException.getClass().equals(p_class))
        {
            return m_originalException;
        }

        if (GeneralException.class.isAssignableFrom(m_originalException
                .getClass()))
        {
            return ((GeneralException) m_originalException)
                    .containsNestedException(p_class);
        }

        return null;
    }

    /**
     * <p>
     * Returns the first instance of a nested Exception that isAssignableFrom
     * the specified class. Returns null if not found.
     * </p>
     * 
     * @param p_class
     *            Class that a nested exception isAssignableFrom.
     * @return the first instance of a nested Exception that isAssignableFrom
     *         the specified class. Returns null if not found.
     */
    public Exception containsNestedExceptionIsAssignableFrom(Class p_class)
    {
        if (m_originalException == null)
        {
            return null;
        }

        if (m_originalException.getClass().isAssignableFrom(p_class))
        {
            return m_originalException;
        }

        if (m_originalException.getClass().isAssignableFrom(
                GeneralException.class))
        {
            return ((GeneralException) m_originalException)
                    .containsNestedExceptionIsAssignableFrom(p_class);
        }

        return null;
    }

    /**
     * Overrides java.lang.Throwable#printStackTrace().
     */
    public void printStackTrace()
    {
        getLogger().error(getMessage(), this);
    }

    /**
     * Overrides java.lang.Throwable#printStackTrace(PrintStream).
     */
    public void printStackTrace(PrintStream s)
    {
        s.println(getStackTraceString());
    }

    /**
     * Overrides java.lang.Throwable#printStackTrace(PrintWriter).
     */
    public void printStackTrace(PrintWriter s)
    {
        s.println(getStackTraceString());
    }

    // The idea here is that if the object is a deserialized one,
    // m_stackTrace holds the original stack trace so we return
    // it. Otherwise we get the stack trace from the parent.
    private void localPrintStackTrace(PrintWriter s)
    {
        if (m_stackTrace != null)
        {
            s.print(m_stackTrace);
        }
        else
        {
            super.printStackTrace(s);
        }
    }

    // get the fully qualified property file name
    public String getPropertyFileName()
    {
        String className = null;
        if (m_propertyFileName == null)
        {
            className = getClass().getName();
            className = className.substring(className.lastIndexOf('.') + 1);
        }
        else
        {
            className = m_propertyFileName;
        }

        return className;
    }

    // get a resource and cash it
    private ResourceBundle getResource(String p_propertyFileName,
            Locale p_uiLocale)
    {
        String key = p_propertyFileName + "_" + p_uiLocale.getDisplayName();
        ResourceBundle resource = null;

        if ((resource = (ResourceBundle) m_resourceCache.get(key)) == null)
        {
            try
            {
                resource = PropertyResourceBundle.getBundle(
                        RESOURCE_PACKAGE_NAME + p_propertyFileName, p_uiLocale);
            }
            catch (MissingResourceException e)
            {
                getLogger().error(
                        e.getMessage() + " " + RESOURCE_PACKAGE_NAME + " "
                                + p_propertyFileName + " "
                                + p_uiLocale.toString(), e);
                return null;
            }

            // cache the resource
            m_resourceCache.put(key, resource);
        }

        return resource;
    }

    // write GE_instance element
    private Node writeGeneralExceptionElement(Document doc) throws Exception
    {
        // <GeneralException>
        Element root = doc.createElement(GENERAL_EXCEPTION);

        // <originalMessage>
        if (m_originalMessage != null)
        {
            Element originalMessage = doc.createElement(ORIGINALMESSAGE);
            originalMessage.appendChild(doc.createTextNode(m_originalMessage));
            root.appendChild(originalMessage);
        }

        // <key>
        if (m_messageKey != null)
        {
            Element key = doc.createElement(KEY);
            key.appendChild(doc.createTextNode(m_messageKey));
            root.appendChild(key);
        }

        // <args>
        if (m_messageArguments != null)
        {
            Element args = doc.createElement(ARGS);
            for (int i = 0; i < m_messageArguments.length; i++)
            {
                Element arg = doc.createElement(ARG);
                arg.appendChild(doc.createTextNode(m_messageArguments[i]));
                args.appendChild(arg);
            }

            root.appendChild(args);
        }

        // <messageFile>
        Element messageFile = doc.createElement(MESSAGEFILE);
        messageFile.appendChild(doc.createTextNode(getPropertyFileName()));
        root.appendChild(messageFile);

        // <stackTrace>
        Element stackTrace = doc.createElement(STACKTRACE);
        String stack = getStackTraceString(this);
        stackTrace.appendChild(doc.createTextNode(stack));
        root.appendChild(stackTrace);

        // Call writeGeneralExceptionElement recursively
        if (m_originalException instanceof GeneralException)
        {
            Node node = ((GeneralException) m_originalException)
                    .writeGeneralExceptionElement(doc);
            root.appendChild(node);
        }
        else if (m_originalException != null)
        {
            // <JavaException>
            Element instance = doc.createElement(JAVA_EXCEPTION);

            // <message>
            Element message = doc.createElement(MESSAGE);
            String java_exception = m_originalException.toString();
            message.appendChild(doc.createTextNode(java_exception));
            instance.appendChild(message);

            // <stackTrace>
            stackTrace = doc.createElement(STACKTRACE);
            String trace_string = getStackTraceString(m_originalException);
            stackTrace.appendChild(doc.createTextNode(trace_string));
            instance.appendChild(stackTrace);

            root.appendChild(instance);
        }

        return root;
    }

    public String getTopLevelMessage()
    {
        return getTopLevelMessage(Locale.getDefault());
    }

    public String getTopLevelMessage(Locale p_uiLocale)
    {
        // Old stuff. Should be removed once old codes are removed.
        if (m_messageId != DEFAULT_MSG_ID)
        {
            ResourceBundle resourceBundle = SystemResourceBundle.getInstance()
                    .getResourceBundle(
                            ResourceBundleConstants.EXCEPTION_RESOURCE_NAME,
                            p_uiLocale);

            if (resourceBundle == null)
            {
                return "ResourceBundle not found: "
                        + ResourceBundleConstants.EXCEPTION_RESOURCE_NAME
                        + " "
                        + p_uiLocale.toString()
                        + " "
                        + Integer.toString(m_messageId)
                        + " "
                        + (m_messageArguments != null ? m_messageArguments
                                .toString() : "null");
            }

            String value = resourceBundle.getString(Integer
                    .toString(m_messageId));

            if (m_messageArguments != null)
            {
                value = MessageFormat.format(value, m_messageArguments);
            }
            return value;
        }

        // new exception code
        // get the message of this instance
        String message = getOwnMessage(p_uiLocale);

        // if the default message string remove it - want to find a specific one
        if (message.equals(DEFAULT_MSG_STRING))
        {
            message = "";
        }

        // if there wasn't an error message - look for one in nested
        // exceptions
        if (message.length() == 0)
        {
            Exception nextException = getOriginalException();
            if (nextException != null)
            {
                if (nextException instanceof GeneralException)
                {
                    // Call getTopLevelMessage recursively until
                    // a message is specified or have gone through
                    // all the exceptions
                    message = ((GeneralException) nextException)
                            .getTopLevelMessage(p_uiLocale);
                }
                else
                {
                    // Encountered non GeneralException, which means the
                    // end of the Exception link. Get the message.
                    message = nextException.getMessage();
                }
            }
        }

        return message;
    }

    // get the message of its own (without nested one)
    public String getOwnMessage(Locale p_uiLocale)
    {
        String message = DEFAULT_MSG_STRING;

        if (m_messageKey != null)
        {
            // get the fully qualified property file name
            String propertyFileName = getPropertyFileName();

            // get the resource
            ResourceBundle resourceBundle = getResource(propertyFileName,
                    p_uiLocale);

            if (resourceBundle == null)
            {
                return "ResourceBundle not found: "
                        + propertyFileName
                        + " "
                        + p_uiLocale.toString()
                        + " "
                        + m_messageKey.toString()
                        + " "
                        + (m_messageArguments != null ? m_messageArguments
                                .toString() : "null");
            }

            String value;

            try
            {
                value = resourceBundle.getString(m_messageKey);

                // format message with arguments
                if (m_messageArguments != null)
                {
                    value = MessageFormat.format(value, m_messageArguments);
                }
            }
            catch (MissingResourceException ex)
            {
                value = "Key " + m_messageKey + " in resource file "
                        + propertyFileName + " is missing.";
            }

            message = value;
        }
        // m_message != null only if the object is recreated by
        // deserialize() AND the original exception is not
        // a GeneralException nor a subclass of it.
        else if (m_message != null)
        {
            message = m_message;
        }
        // message is set to DEFAULT_MSG_STRING
        else
        {
            // try to get the message of the original exception
            if (m_originalException != null)
            {
                String origMessage = m_originalException.getMessage();
                if (origMessage != null && origMessage.length() > 0)
                {
                    message = origMessage;
                }
            }
            else if (m_originalMessage != null)
            {
                message = m_originalMessage;
            }
            else
            {
                // the last method to get the error message.
                message = m_errorMessage;
            }
        }

        return message;
    }

    // get the stack trace of its own (without nested one)
    private static String getStackTraceString(Exception e)
    {
        CharArrayWriter outBuffer = new CharArrayWriter();
        PrintWriter writer = new PrintWriter(outBuffer);

        if (e instanceof GeneralException)
        {
            ((GeneralException) e).localPrintStackTrace(writer);
        }
        else
        {
            e.printStackTrace(writer);
        }

        return outBuffer.toString();
    }

    /**
     * XML Content handler for deserialized GeneralExceptionHandler
     */
    private static class GeneralExceptionHandler extends DefaultHandler
    {
        private GeneralException m_root = null;
        private GeneralException m_current = null;
        private String text = null;
        private List argList = null;

        /** Start element. */
        public void startElement(String uri, String local, String raw,
                Attributes attrs)
        {
            if (raw.equals(GENERAL_EXCEPTION) || raw.equals(JAVA_EXCEPTION))
            {
                // create a root or nested exception
                GeneralException ge = new GeneralException((Exception) null);
                if (m_root == null)
                {
                    m_root = ge;
                }
                if (m_current != null)
                {
                    m_current.m_originalException = ge;
                }
                m_current = ge;
            }

            // create argument holder
            else if (raw.equals(ARGS))
            {
                argList = new ArrayList();
            }
        }

        /** End element. */
        public void endElement(String uri, String local, String raw)
        {
            if (raw.equals(ORIGINALMESSAGE))
            {
                m_current.m_originalMessage = text;
            }
            else if (raw.equals(KEY))
            {
                m_current.m_messageKey = text;
            }
            else if (raw.equals(MESSAGEFILE))
            {
                m_current.m_propertyFileName = text;
            }
            else if (raw.equals(STACKTRACE))
            {
                m_current.m_stackTrace = text;
            }
            else if (raw.equals(MESSAGE))
            {
                m_current.m_message = text;
            }
            else if (raw.equals(ARGS))
            {
                // see the cool trick in Javadoc on
                // Collenction#toArray(Object[])
                m_current.m_messageArguments = (String[]) argList
                        .toArray(new String[0]);
            }
            else if (raw.equals(ARG))
            {
                argList.add(text);
            }

            // clear out `text'
            text = null;

        }

        /** Characters. */
        public void characters(char ch[], int start, int length)
        {
            String tmpText = new String(ch, start, length);
            if (text == null)
                text = tmpText;
            else
                text += tmpText;
        }

        public GeneralException getObject()
        {
            return m_root;
        }
    }
}
