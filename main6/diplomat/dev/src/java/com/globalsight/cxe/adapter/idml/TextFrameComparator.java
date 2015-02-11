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
package com.globalsight.cxe.adapter.idml;

import java.util.Comparator;
import java.util.List;

class TextFrameComparator implements Comparator<TextFrameObj>
{
    List<Double> pageX = null;

    public List<Double> getPageX()
    {
        return pageX;
    }

    public void setPageX(List<Double> pageX)
    {
        this.pageX = pageX;
    }

    @Override
    public int compare(TextFrameObj o1, TextFrameObj o2)
    {
        boolean isSamePage = false;
        if (pageX.size() == 1)
        {
            isSamePage = true;
        }
        else
        {
            int pageSize = pageX.size();

            for (int i = 0; i < pageSize - 1; i++)
            {
                double dbl1 = pageX.get(i);
                double dbl2 = pageX.get(i + 1);
                if (o1.pointX >= dbl1 && o1.pointX < dbl2 && o2.pointX >= dbl1
                        && o2.pointX < dbl2)
                {
                    isSamePage = true;
                    break;
                }
                if (i == (pageSize - 2) && o1.pointX >= dbl2 && o2.pointX >= dbl2)
                {
                    isSamePage = true;
                    break;
                }
            }
        }

        if (isSamePage)
        {
            // in same page, Y first, then X
            if (o1.pointY != o2.pointY)
            {
                return o1.pointY > o2.pointY ? 1 : -1;
            }
            else if (o1.pointX != o2.pointX)
            {
                return o1.pointX > o2.pointX ? 1 : -1;
            }
            else
            {
                return 0;
            }
        }
        else
        {
            // not at same page, compare X
            if (o1.pointX != o2.pointX)
            {
                return o1.pointX > o2.pointX ? 1 : -1;
            }
            else
            {
                return 0;
            }
        }
    }

}