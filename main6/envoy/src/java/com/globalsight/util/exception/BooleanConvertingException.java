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
package com.globalsight.util.exception;

/**
 * Thrown to indicate that the operation to convert a string to
 * boolean type, but that the string does not have the appropriate
 * format or the string is null.
 *
 * Only used by com.globalsight.util.gxml.GxmlElement.
 */
public class BooleanConvertingException
    extends IllegalArgumentException
{
    public static final String DEFAULT_MESSAGE=
        "Failed to convert string to boolean. Not a legal boolean value!";

    /**
     * Constructs a BooleanConvertingException with no detail message.
     */
    public BooleanConvertingException()
    {
        super(DEFAULT_MESSAGE);
    }

    /**
     * Constructs a BooleanConvertingException with the specified
     * detail message.
     *
     * @param p_message the detail message.
     */
    public BooleanConvertingException(String p_message)
    {
        super(p_message);
    }
}
