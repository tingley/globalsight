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

public class Page
{
    public static final int MAX_ROW = 15;
    
    private int index = -1;
    private int pageNumber = 0;

    public void init()
    {
        index = 0;
        pageNumber = 0;
    }
    
    public int getIndex()
    {
        return index;
    }
    
    public int getPageNumber()
    {
        return pageNumber;
    }

    public void setIndex(int index)
    {
        this.index = index;
    }

    public int up()
    {
        pageNumber--;
        return pageNumber;
    }
    
    public int down()
    {
        pageNumber++;        
        return pageNumber;
    }


    public int next()
    {
        index++;
        pageNumber = 0;
        return index;
    }

    public int previous()
    {
        index--;
        pageNumber = 0;
        return index;
    }
}
