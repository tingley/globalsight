/**
 *  Copyright 2009-2016 Welocalize, Inc. 
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
 */
package com.globalsight.everest.category;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * @author VincentYan
 *
 */
public class CategoryTypeTest
{
    @Test
    public void testCategoryTypes() {
        assertEquals(1, CategoryType.SegmentComment.getValue());
        assertEquals(2, CategoryType.ScoreCard.getValue());
        assertEquals(3, CategoryType.Quality.getValue());
        assertEquals(4, CategoryType.Market.getValue());
        assertEquals(5, CategoryType.Fluency.getValue());
        assertEquals(6, CategoryType.Adequacy.getValue());
        assertEquals(7, CategoryType.Severity.getValue());
    }
}
