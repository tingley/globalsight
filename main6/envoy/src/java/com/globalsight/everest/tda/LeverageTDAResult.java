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
package com.globalsight.everest.tda;

public class LeverageTDAResult
{
    private long tuid;
    private String matchPercent;
    private String resultText;
    private int orderNum = 1;
    private String sourceText;

    public long getTuid()
    {
        return tuid;
    }

    public void setTuid(long id)
    {
        this.tuid = id;
    }

    public String getMatchPercent()
    {
        return matchPercent;
    }

    public void setMatchPercent(String p_percent)
    {
        matchPercent = p_percent;
    }

    public String getResultText()
    {
        return resultText;
    }

    public void setResultText(String text)
    {
        resultText = text;
    }

    public int getOrderNum()
    {
        return orderNum;
    }

    public void setOrderNum(int p_orderNum)
    {
        orderNum = p_orderNum;
    }
    
    public String getSourceText()
    {
        return sourceText;
    }

    public void setSourceText(String text)
    {
        sourceText = text;
    }
}