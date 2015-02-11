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
package com.globalsight.everest.persistence;

/**
 * PersistenceRuntimeException is an extension of RuntimeException, created
 * solely for semantic purposes.
 * <p>
 * This exception can be thrown whenever it is not appropriate for a checked
 * exception to be thrown.  For example, PersistentObject throws this 
 * exception when makeNew() fails.
 */
public class PersistenceRuntimeException 
    extends RuntimeException
{
    //
    // PRIVATE CONSTANTS
    //
    private static final String DEFAULT_MESSAGE = 
        "A persistence runtime exception has occurred.";

    //
    // PUBLIC CONSTRUCTORS
    //
    /**
     * Default constructor: create an exception with a default message.
     */
    public PersistenceRuntimeException()
    {
        this(DEFAULT_MESSAGE);
    }

    /**
     * Create an exception with the given message.
     */
    public PersistenceRuntimeException(String p_message)
    {
        super(p_message);
    }
}


















