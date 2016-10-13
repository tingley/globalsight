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
package com.globalsight.ling.docproc;

public class LineIndex
{
    private int sidStart = -1;
    private int sidEnd = -1;
    private int contentStart = -1;
    private int contentEnd = -1;
    
    public LineIndex(int s, int e)
    {
        contentStart = s;
        contentEnd = e;
    }

    public int getSidStart()
    {
        return sidStart;
    }

    public void setSidStart(int sidStart)
    {
        this.sidStart = sidStart;
    }

    public int getSidEnd()
    {
        return sidEnd;
    }

    public void setSidEnd(int sidEnd)
    {
        this.sidEnd = sidEnd;
    }

    public int getContentStart()
    {
        return contentStart;
    }

    public void setContentStart(int contentStart)
    {
        this.contentStart = contentStart;
    }

    public int getContentEnd()
    {
        return contentEnd;
    }

    public void setContentEnd(int contentEnd)
    {
        this.contentEnd = contentEnd;
    }
}
