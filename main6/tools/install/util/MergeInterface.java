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

package util;

import java.io.IOException;

public interface MergeInterface
{
    /**
     * Values are equal.
     */
    static public final int EQUAL = 0;
    /**
     * Values are different, but do nothing.
     */
    static public final int IGNORE = -1;
    /**
     * Values are different, and update the new value.
     */
    static public final int UPDATE = 1;

    /**
     * Compares oldValue to newValue.
     *
     * @param fileName name of the file.
     * @param key key name.
     * @param comment key comment.
     * @param oldValue value from old property file.
     * @param newValue value from new property file.
     *
     * @return EQUAL if the values are equal,
     * IGNORE if the values are different but keep the new value,
     * UPDATE if the values are different and update the new value.
     *
     * @throws IOException
     */
    public int checkToUpdateValue(String fileName, String key, String comment, String oldValue, String newValue)
    throws IOException;
        
    /**
     * Handles the case where a key is in the old property file but not in the new one.
     *
     * @param fileName name of the file.
     * @param key key name.
     * @param comment key comment.
     * @param value key value.
     *
     * @return IGNORE to do nothing,
     * UPDATE to add the key, comment, and value to the new property file.
     *
     * @throws IOException
     */
    public int checkToAddKey(String fileName, String key, String comment, String value)
    throws IOException;
        
    /**
     * Handles the case where the file is not a property file.
     *
     * @param fileName name of the file.
     *
     * @return IGNORE to do nothing,
     * UPDATE to copy the old file to replace the new file.
     *
     * @throws IOException
     */
    public int checkToCopyFile(String fileName)
    throws IOException;
        
    /**
     * Handles the case where the old and new files are the same.
     *
     * @param fileName name of the file.
     *
     * @throws IOException
     */
    public void noDifference(String fileName)
    throws IOException;
}