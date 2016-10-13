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
package com.globalsight.ling.tm.fuzzy;

/**
Manages creation and destruction of Token objects.
*/
public class TokenPool
{

    // Free pool capacity.
    private static final int FREE_POOL_SIZE = 1000;

    // Pool owned by class.
    private static final Token[] freeStack = new Token[FREE_POOL_SIZE];
    private static int countFree;

    /**
    */
    public static synchronized void freeInstance(Token p_token)
    {
        if (countFree < FREE_POOL_SIZE)
        {
            freeStack[countFree++] = p_token;
        }
    }

    /**
    */
    public static synchronized Token getInstance(String p_token)
    {

        // Check if the pool is empty.
        Token result;
        if (countFree == 0)
        {
            // Create a new object if so.
            result = new Token();
        }
        else
        {

            // Remove object from end of free pool.
            result = freeStack[--countFree];
        }

        // Initialize the object to the specified state.
        result.setToken(p_token);

        return result;
    }
}
