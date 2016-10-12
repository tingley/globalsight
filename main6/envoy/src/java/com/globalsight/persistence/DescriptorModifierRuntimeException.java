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
package com.globalsight.persistence;

import com.globalsight.exception.WrappedRuntimeException;

/**
 * DescriptorModifierRuntimeException represents a wrapped runtime exception
 * that occurs during the loading and/or modifying of TOPLink descriptors.  In
 * practice, this exception should ONLY occur if a descriptor modifier refers
 * to an invalid entity class, or a non-existent "clob-getter" method is
 * specified.
 */
public class DescriptorModifierRuntimeException
    extends WrappedRuntimeException
{
    //
    // PRIVATE CONSTANTS
    //
    private static final String DEFAULT_MSG = 
        "Descriptor Modifier Runtime Exception";

    //
    // PUBLIC CONSTRUCTORS
    //
    /**
     * Construct an exception with a default message.
     */
    public DescriptorModifierRuntimeException()
    {
        this(null, null);
    }

    /**
     * Construct an exception with the given message.
     *
     * @param p_message the message to ge displayed to the caller.
     */
    public DescriptorModifierRuntimeException(String p_message)
    {
        this(p_message, null);
    }

    /**
     * Construct an exception with the given exception.
     *
     * @param p_exception the exception to be wrapped.
     */
    public DescriptorModifierRuntimeException(Exception p_exception)
    {
        this(null, p_exception);
    }

    /**
     * Construct an exception with the given message, and wrap the given
     * exception.
     *
     * @param p_message the message to ge displayed to the caller.
     * @param p_exception the exception to be wrapped.
     */
    public DescriptorModifierRuntimeException(String p_message,
                                              Exception p_exception)
    {
        super((p_message == null ? DEFAULT_MSG : p_message), p_exception);
    }
}

