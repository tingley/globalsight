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

package com.globalsight.ling.tm2.persistence.error;

import java.util.Collection;

import com.globalsight.ling.tm2.SegmentTmTuv;

/**
 * Thrown when a TM save operation found that some tuvs couldn't be saved (but
 * the rest of the save succeeded).  Lists the tuvs and problems as TuvErrors.
 */
public class BatchException extends Exception
{
    private Collection<TuvError> tuvs;
    public BatchException(Collection<TuvError> tuvs) {
        this.tuvs = tuvs;
    }
    public Collection<TuvError> getTuvs() {
        return tuvs;
    }

    /**
     * An error report for a tuv.  The messageKey will be used to look up a
     * message pattern in a resource bundle, which will passed to
     * MessageFormat.format along with the messageArguments.
     */
    public static class TuvError {
        private final String segment, messageKey, defaultMessagePattern;
        private final Object[] messageArguments;
        public TuvError(SegmentTmTuv tuv, String messageKey,
                String defaultMessagePattern, Object... messageArguments) {
            this.segment = tuv.getOrgSegment();
            this.messageKey = messageKey;
            this.defaultMessagePattern = defaultMessagePattern;
            this.messageArguments = messageArguments;
        }
        public String getSegment() {
            return segment;
        }
        public String getMessageKey() {
            return messageKey;
        }
        public String getDefaultMessagePattern() {
            return defaultMessagePattern;
        }
        public Object[] getMessageArguments() {
            return messageArguments;
        }
    }
}
