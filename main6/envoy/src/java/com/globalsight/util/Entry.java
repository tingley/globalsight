/**
 * Copyright 2009 Welocalize, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 * 
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 */
package com.globalsight.util;

/**
 * Entry is used to store the key-value pair data. <br>
 * It typical usage is to store the <code>Entry</code> to a collection to
 * represent a group key-value pairs. e.g options in jsp page.
 * 
 */
public class Entry<K, V>
{
    private K key;

    private V value;

    private String help;

    public String getHelp()
    {
        return help;
    }

    public void setHelp(String help)
    {
        this.help = help;
    }

    public Entry(K key, V value)
    {
        this.key = key;
        this.value = value;
    }

    /**
     * Gets the key of the <code>Entry</code>.
     * 
     * @return The key.
     */
    public K getKey()
    {
        return this.key;
    }

    /**
     * Gets the value of the <code>Entry</code>.
     * 
     * @return The value.
     */
    public V getValue()
    {
        return this.value;
    }

}
