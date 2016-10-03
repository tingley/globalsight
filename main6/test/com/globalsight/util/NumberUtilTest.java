/**
 * Copyright 2011 Welocalize, Inc.
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

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author Vincent
 *
 */
public class NumberUtilTest
{
    @Test
    public void testConvertToInt()
    {
        assertEquals(100, NumberUtil.convertToInt("100"));
        assertEquals(-1, NumberUtil.convertToInt("123test"));
        assertEquals(100, NumberUtil.convertToInt("123test", 100));
    }

    @Test
    public void testIsInteger()
    {
        assertEquals(true, NumberUtil.isInteger("123"));

        assertEquals(false, NumberUtil.isInteger("123 "));

        assertEquals(false, NumberUtil.isInteger("123.0"));

        assertEquals(false, NumberUtil.isInteger("null"));
    }

    @Test
    public void testIsLong()
    {
        assertEquals(true, NumberUtil.isLong("123456"));

        assertEquals(false, NumberUtil.isLong("123456 "));

        assertEquals(false, NumberUtil.isLong("123456.0"));

        assertEquals(false, NumberUtil.isLong("null"));
    }
}
