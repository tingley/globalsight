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

/**
 * Message sent to listeners for validation errors/warnings and info.
 * @see Tidy#setMessageListener(TidyMessageListener)
 * @author Fabrizio Giustina
 * @version $Revision: 1.1 $ ($Author: yorkjin $)
 */
public final class TidyMessage
{

    /**
     * Line in the source file (can be 0 if the message is not related
     * to a particular line, such as a summary message).
     */
    private int line;

    /**
     * Column in the source file (can be 0 if the message is not
     * related to a particular column, such as a summary message).
     */
    private int column;

    /**
     * Level for this message. Can be TidyMessage.Level.SUMMARY |
     * TidyMessage.Level.INFO | TidyMessage.Level.WARNING |
     * TidyMessage.Level.ERROR.
     */
    private Level level;

    /**
     * Formatted text for this message.
     */
    private String message;

    /**
     * Tidy internal error code.
     */
    private int errorCode;

    /**
     * Instantiates a new message.
     * @param errorCode Tidy internal error code.
     * @param line Line number in the source file
     * @param column Column number in the source file
     * @param level severity
     * @param message message text
     */
    public TidyMessage(int errorCode, int line, int column,
        Level level, String message)
    {
        this.errorCode = errorCode;
        this.line = line;
        this.column = column;
        this.level = level;
        this.message = message;
    }

    /**
     * Getter for <code>errorCode</code>.
     * @return Returns the errorCode.
     */
    public int getErrorCode()
    {
        return this.errorCode;
    }

    /**
     * Getter for <code>column</code>.
     * @return Returns the column.
     */
    public int getColumn()
    {
        return this.column;
    }

    /**
     * Getter for <code>level</code>.
     * @return Returns the level.
     */
    public Level getLevel()
    {
        return this.level;
    }

    /**
     * Getter for <code>line</code>.
     * @return Returns the line.
     */
    public int getLine()
    {
        return this.line;
    }

    /**
     * Getter for <code>message</code>.
     * @return Returns the message.
     */
    public String getMessage()
    {
        return this.message;
    }

    /**
     * Message severity enumeration.
     * @author fgiust
     * @version $Revision: 1.1 $ ($Author: yorkjin $)
     */
    public static final class Level implements Comparable
    {
        /**
         * level = summary (0).
         */
        public static final Level SUMMARY = new Level(0);

        /**
         * level = info (1).
         */
        public static final Level INFO = new Level(1);

        /**
         * level = warning (2).
         */
        public static final Level WARNING = new Level(2);

        /**
         * level = error (3).
         */
        public static final Level ERROR = new Level(3);

        /**
         * short value for this level.
         */
        private short code;

        /**
         * Instantiates a new message with the given code.
         * @param code int value for this level
         */
        private Level(int code)
        {
            this.code = (short) code;
        }

        /**
         * Returns the int value for this level.
         * @return int value for this level
         */
        public short getCode()
        {
            return this.code;
        }

        /**
         * Returns the Level instance corresponding to the given int value.
         * @param code int value for the level
         * @return Level instance
         */
        public static Level fromCode(int code)
        {
            switch (code)
            {
                case 0 :
                    return SUMMARY;
                case 1 :
                    return INFO;
                case 2 :
                    return WARNING;
                case 3 :
                    return ERROR;

                default :
                    return null;
            }
        }

        /**
         * @see java.lang.Comparable#compareTo(Object)
         */
        public int compareTo(Object object)
        {
            return this.code - ((Level) object).code;
        }

        /**
         * @see java.lang.Object#equals(Object)
         */
        public boolean equals(Object object)
        {
            if (!(object instanceof Level))
            {
                return false;
            }
            return this.code == ((Level) object).code;
        }

        /**
         * @see java.lang.Object#toString()
         */
        public String toString()
        {
            switch (code)
            {
                case 0 :
                    return "SUMMARY";
                case 1 :
                    return "INFO";
                case 2 :
                    return "WARNING";
                case 3 :
                    return "ERROR";

                default :
                    // should not happen
                    return "?";
            }
        }

        /**
         * @see java.lang.Object#hashCode()
         */
        public int hashCode()
        {
            // new instances should not be created
            return super.hashCode();
        }
    }
}
