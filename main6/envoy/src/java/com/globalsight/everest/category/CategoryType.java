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
package com.globalsight.everest.category;


/**
 * @author VincentYan
 *
 */
public enum CategoryType
{
    SegmentComment(1), ScoreCard(2), Quality(3), Market(4), Fluency(5), Adequacy(6), Severity(7);
    private int index;
    
    private CategoryType(int index) {
        this.index = index;
    }

    @Override
    public String toString()
    {
        return String.valueOf(this.index);
    }

    public int getValue()
    {
        return this.index;
    }
}
