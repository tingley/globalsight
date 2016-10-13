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
package com.globalsight.terminology.audit;

import org.apache.log4j.Logger;

import com.globalsight.util.UTC;
import java.util.Date;

/**
 * The TermAuditLog class provides constants and a way to get
 * a logger that can be used to write terminology audit messages.
 */
public final class TermAuditLog
{
    /** The log4j logger name (must match what's in log4j.xml)**/
    static public final String AUDIT_LOG_NAME = "TermAuditLog";

    /** The log4j Terminology Audit Logger */
    static private final Logger s_term_audit_logger =
        Logger.getLogger(AUDIT_LOG_NAME);

    /** Unicode RECORD SEPARATOR character, unlikely to be part of the message. */
    static public final String UNISEP = "\u241E";

    /** Separator used for the log, more visually friendly **/
    static public final String SEP = "|";

    /** Escaped for use as a regexp */
    static public final String REGEXP_SEP = "\\|";

    /**
     * Logs a message to the terminology audit log (both
     * File and JMS).
     *
     * @param TermAuditEvent
     */
    static public void log(TermAuditEvent p_event)
    {
        StringBuffer msg = new StringBuffer();

        msg.append(UTC.valueOf(p_event.date)); msg.append(SEP);
        msg.append(p_event.username); msg.append(SEP);
        msg.append(p_event.termbase); msg.append(SEP);
        msg.append(p_event.item); msg.append(SEP);
        msg.append(p_event.langs); msg.append(SEP);
        msg.append(p_event.action); msg.append(SEP);
        msg.append(p_event.details);

        s_term_audit_logger.info(msg.toString());
    }


    /**
     * Returns the underlying log4j logger that can be used to log
     * terminology audit messages. NOTE that the JMS messages are
     * epected to have a certain structure.  Use log() above instead.
     **/
    static public Logger getAuditLogger()
    {
        return s_term_audit_logger;
    }
}

