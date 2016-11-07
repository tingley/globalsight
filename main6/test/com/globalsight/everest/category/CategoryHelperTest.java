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

import static org.junit.Assert.assertEquals;
import org.junit.Test;
/**
 * @author VincentYan
 *
 */
public class CategoryHelperTest
{
    @Test
    public void testDefaultCategories() {
        assertEquals(5, CategoryHelper.SEGMENT_COMMENT.length);
        assertEquals(4, CategoryHelper.SCORECARD.length);
        assertEquals(3, CategoryHelper.QAULITY.length);
        assertEquals(3, CategoryHelper.MARKET.length);
        assertEquals(4, CategoryHelper.FLUENCY.length);
        assertEquals(4, CategoryHelper.ADEQUACY.length);
        assertEquals(6, CategoryHelper.SEVERITY.length);
        
        assertEquals("lb_conflicts_glossary_guide", CategoryHelper.SEGMENT_COMMENT[0]);
        assertEquals("lb_formatting_error", CategoryHelper.SEGMENT_COMMENT[1]);
        assertEquals("lb_mistranslated", CategoryHelper.SEGMENT_COMMENT[2]);
        assertEquals("lb_omission_of_text", CategoryHelper.SEGMENT_COMMENT[3]);
        assertEquals("lb_spelling_grammar_punctuation_error", CategoryHelper.SEGMENT_COMMENT[4]);
        
        assertEquals("lb_spelling_grammar", CategoryHelper.SCORECARD[0]);
        assertEquals("lb_good", CategoryHelper.QAULITY[0]);
        assertEquals("lb_suitable_fluent", CategoryHelper.MARKET[0]);
        assertEquals("lb_dqf_fluency_incomprehensible", CategoryHelper.FLUENCY[0]);
        assertEquals("lb_none", CategoryHelper.ADEQUACY[0]);
        assertEquals("lb_dqf_severity_critical", CategoryHelper.SEVERITY[0]);
    }
}
