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
package com.util;

import java.io.File;

/**
 * A util class, provide some methods to validate parameters. <br>
 * For example, you can use Assert.assertNotNull(book, "book") to make sure that
 * book is not null, a <code>IllegalArgumentException</code> about "book can
 * not be null" will be throw out in the example if book is null.
 * 
 */
public class Assert
{
    /**
     * Makes sure that the object is not null, others a
     * <code>IllegalArgumentException</code> with error message (<code>name</code> + "
     * can not be null") will be throw out.
     * 
     * @param ob
     *            The object to check.
     * @param name
     *            Used to create error message. The error message is
     *            <code>name</code> + " can not be null"
     */
    public static void assertNotNull(Object ob, String name)
    {
        if (ob == null)
        {
            throw new IllegalArgumentException(name + " can not be null");
        }
    }

    /**
     * Makes sure that a expression is true, others a
     * <code>IllegalArgumentException</code> with error message (<code>msg</code>)
     * will be throw out.
     * 
     * @param isTrue
     *            The result of expression.
     * @param msg
     *            The error message if assert failed.
     */
    public static void assertTrue(boolean isTrue, String msg)
    {
        if (!isTrue)
        {
            throw new IllegalArgumentException(msg);
        }
    }

    /**
     * Makes sure that a file is exist, others a
     * <code>IllegalArgumentException</code> with error message ("File (" +
     * <code>path</code> + ") is not exist") will be throw out.
     * 
     * @param path
     *            The path of file, can't be null.
     */
    public static void assertFileExist(String path)
    {
        assertNotNull(path, "File path");
        
        if (!new File(path).exists())
        {
            throw new IllegalArgumentException("File (" + path
                    + ") does not exist");
        }
    }

    /**
     * Makes sure that a file is exist, others a
     * <code>IllegalArgumentException</code> with error message ("File (" +
     * <code>path</code> + ") is not exist") will be throw out.
     * 
     * @param file
     *            The file to check, can't be null.
     */
    public static void assertFileExist(File file)
    {
        assertNotNull(file, "File");
        
        if (!file.exists())
        {
            throw new IllegalArgumentException("File (" + file.getPath()
                    + ") does not exist");
        }
    }
}
