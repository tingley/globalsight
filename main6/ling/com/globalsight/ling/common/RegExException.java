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
package com.globalsight.ling.common;

/**
 * Insert the type's description here.
 * Creation date: (8/11/2000 2:13:10 PM)
 * @author: Jim Hargrave
 */
public class RegExException extends Exception 
{

    /**
     * RegExException constructor comment.
     */
    public RegExException() 
    {
        super();
    }

    /**
     * RegExException constructor comment.
     * @param s java.lang.String
     */
    public RegExException(String s) 
    {
        super(s);
    }

    /**
     * Returns a String that represents the value of this object.
     * @return a string representation of the receiver
     */
    public String toString() 
    {
        // Insert code to print the receiver here.
        // This implementation forwards the message to super. You may replace or supplement this.
        return super.toString();
    }
}