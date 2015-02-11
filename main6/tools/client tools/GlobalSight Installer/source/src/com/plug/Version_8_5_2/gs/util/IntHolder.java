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

package com.plug.Version_8_5_2.gs.util;

/**
 * A wrapper object for an int. In contrast to Integer, this object is
 * mutable and the int value can be modified.
 */
public class IntHolder
{
    /**
     * The int value. A public field for lazy programmers.
     */
    public int value;

    public IntHolder()
    {
        value = 0;
    }

    public IntHolder(int p_value)
    {
        value = p_value;
    }

    public int getValue()
    {
        return value;
    }

    public void setValue(int p_value)
    {
        value = p_value;
    }

    /**
     * The post-increment ++ operator: increments the value by one,
     * and returns the old value.
     */
    public int inc()
    {
        return value++;
    }

    /**
     * The post-decrement -- operator: decrements the value by one,
     * and returns the old value.
     */
    public int dec()
    {
        return value--;
    }
}
