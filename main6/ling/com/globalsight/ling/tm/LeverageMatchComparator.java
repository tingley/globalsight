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
package com.globalsight.ling.tm;

import java.util.Comparator;
import org.apache.log4j.Logger;
import com.globalsight.ling.tm.LingManagerException;
import org.apache.log4j.Category;

public class LeverageMatchComparator

{
    // Log facility
    private static final Category CATEGORY =
        Logger.getLogger(LeverageMatchComparator.class.getName());

    /**
    LeverageMatchComparator constructor comment.
    */
    public LeverageMatchComparator()
    {
        super();
    }

    /**
    */
    /*
    public int compare(Object p_one, Object p_two)
    {
        String one = null;
        String two = null;
        
        int typeOne = ((LeverageMatch)p_one).getMatchType();
        int typeTwo = ((LeverageMatch)p_two).getMatchType();
        
        try
        {
            one = 
                ((LeverageMatch)p_one).getMatchedSourceTuv().getExactMatchFormat() +
                ((LeverageMatch)p_one).getLeveragedTargetTuv().getExactMatchFormat();

            two =
                ((LeverageMatch)p_two).getMatchedSourceTuv().getExactMatchFormat() +
                ((LeverageMatch)p_two).getLeveragedTargetTuv().getExactMatchFormat();
        }
        catch(LingManagerException e)
        {
            
            CATEGORY.error(e.getMessage(), e);
        }
        
        int sourceTargetCompare = one.compareTo(two);
        
        if (sourceTargetCompare == 0)
        {
            
            if (typeOne > typeTwo)
            {
                return 1;
            }

            if (typeOne < typeTwo)
            {
                return -1;
            }
        }

        if (sourceTargetCompare <= -1)
        {
            return -1;
        }

        if (sourceTargetCompare >= 1)
        {
            return 1;
        }

        return 0;
    }
    */
}
