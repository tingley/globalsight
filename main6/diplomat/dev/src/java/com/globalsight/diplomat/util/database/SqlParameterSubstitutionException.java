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
package com.globalsight.diplomat.util.database;

/**
 * Represents an exception that occurs during parameter substitution.
 */
public class SqlParameterSubstitutionException
extends Exception
{
    //
    // PRIVATE CONSTANTS
    //
    private static final String DEFAULT_MESSAGE = "Sql Parameter Substitution Exception";

    //
    // PUBLIC CONSTRUCTORS
    //
    /**
     * Construct an instance with a default message.
     */
    public SqlParameterSubstitutionException()
    {
        this(DEFAULT_MESSAGE);
    }

    /**
     * Construct an instance with the given message.
     */
    public SqlParameterSubstitutionException(String p_message)
    {
        super(p_message);
    }
}

