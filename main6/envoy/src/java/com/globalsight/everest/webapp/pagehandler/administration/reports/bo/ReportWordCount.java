/**
 *  Copyright 2012 Welocalize, Inc. 
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
package com.globalsight.everest.webapp.pagehandler.administration.reports.bo;

import com.globalsight.util.GlobalSightLocale;

/**
 * This Business Object is used for storing the word count,
 * storing the word count by month and target locale.
 * 
 * @Date Sep 7, 2012
 */
public class ReportWordCount implements Cloneable 
{
    private int month;
    private int year;
    private GlobalSightLocale targetLocale;
    private String companyId;

    // "Trados" values
    private long trados100WordCount = 0;
    private long tradosInContextMatchWordCount = 0;
    private long tradosContextMatchWordCount = 0;
    private long trados95to99WordCount = 0;
    private long trados85to94WordCount = 0;
    private long trados75to84WordCount = 0;
    private long trados50to74WordCount = 0;
    private long tradosNoMatchWordCount = 0;
    private long tradosRepsWordCount = 0;
    private long tradosTotalWordCount = 0;

    public int getMonth()
    {
        return month;
    }

    public int getYear()
    {
        return year;
    }

    public void setYearMonth(int year, int month)
    {
        this.year = year;
        this.month = month;
    }
    
    public String getYearMonth()
    {
        return year + "" + month;
    }

    public GlobalSightLocale getTargetLocale()
    {
        return targetLocale;
    }

    public void setTargetLocale(GlobalSightLocale targetLocale)
    {
        this.targetLocale = targetLocale;
    }

    public String getCompanyId()
    {
        return companyId;
    }

    public void setCompanyId(String companyId)
    {
        this.companyId = companyId;
    }

    public long getTrados100WordCount()
    {
        return trados100WordCount;
    }

    public void setTrados100WordCount(long trados100WordCount)
    {
        this.trados100WordCount = trados100WordCount;
    }
    
    public void addTrados100WordCount(long trados100WordCount)
    {
        this.trados100WordCount += trados100WordCount;
    }

    public long getTradosInContextMatchWordCount()
    {
        return tradosInContextMatchWordCount;
    }

    public void setTradosInContextMatchWordCount(
            long tradosInContextMatchWordCount)
    {
        this.tradosInContextMatchWordCount = tradosInContextMatchWordCount;
    }
    
    public void addTradosInContextMatchWordCount(
            long tradosInContextMatchWordCount)
    {
        this.tradosInContextMatchWordCount += tradosInContextMatchWordCount;
    }

    public long getTradosContextMatchWordCount()
    {
        return tradosContextMatchWordCount;
    }

    public void setTradosContextMatchWordCount(long tradosContextMatchWordCount)
    {
        this.tradosContextMatchWordCount = tradosContextMatchWordCount;
    }
    
    public void addTradosContextMatchWordCount(long tradosContextMatchWordCount)
    {
        this.tradosContextMatchWordCount += tradosContextMatchWordCount;
    }

    public long getTrados95to99WordCount()
    {
        return trados95to99WordCount;
    }

    public void setTrados95to99WordCount(long trados95to99WordCount)
    {
        this.trados95to99WordCount = trados95to99WordCount;
    }
    
    public void addTrados95to99WordCount(long trados95to99WordCount)
    {
        this.trados95to99WordCount += trados95to99WordCount;
    }

    public long getTrados85to94WordCount()
    {
        return trados85to94WordCount;
    }

    public void setTrados85to94WordCount(long trados85to94WordCount)
    {
        this.trados85to94WordCount = trados85to94WordCount;
    }
    
    public void addTrados85to94WordCount(long trados85to94WordCount)
    {
        this.trados85to94WordCount += trados85to94WordCount;
    }

    public long getTrados75to84WordCount()
    {
        return trados75to84WordCount;
    }

    public void setTrados75to84WordCount(long trados75to84WordCount)
    {
        this.trados75to84WordCount = trados75to84WordCount;
    }
    
    public void addTrados75to84WordCount(long trados75to84WordCount)
    {
        this.trados75to84WordCount += trados75to84WordCount;
    }

    public long getTrados50to74WordCount()
    {
        return trados50to74WordCount;
    }

    public void setTrados50to74WordCount(long trados50to74WordCount)
    {
        this.trados50to74WordCount = trados50to74WordCount;
    }
    
    public void addTrados50to74WordCount(long trados50to74WordCount)
    {
        this.trados50to74WordCount += trados50to74WordCount;
    }

    public long getTradosNoMatchWordCount()
    {
        return tradosNoMatchWordCount;
    }

    public void setTradosNoMatchWordCount(long tradosNoMatchWordCount)
    {
        this.tradosNoMatchWordCount = tradosNoMatchWordCount;
    }
    
    public void addTradosNoMatchWordCount(long tradosNoMatchWordCount)
    {
        this.tradosNoMatchWordCount += tradosNoMatchWordCount;
    }

    public long getTradosRepsWordCount()
    {
        return tradosRepsWordCount;
    }

    public void setTradosRepsWordCount(long tradosRepsWordCount)
    {
        this.tradosRepsWordCount = tradosRepsWordCount;
    }
    
    public void addTradosRepsWordCount(long tradosRepsWordCount)
    {
        this.tradosRepsWordCount += tradosRepsWordCount;
    }

    public long getTradosTotalWordCount()
    {
        tradosTotalWordCount = trados100WordCount
                    + tradosInContextMatchWordCount
                    + tradosContextMatchWordCount 
                    + trados95to99WordCount
                    + trados85to94WordCount 
                    + trados75to84WordCount
                    + trados50to74WordCount 
                    + tradosNoMatchWordCount
                    + tradosRepsWordCount;
        
        return tradosTotalWordCount;
    }

    public void setTradosTotalWordCount(long tradosTotalWordCount)
    {
        this.tradosTotalWordCount = tradosTotalWordCount;
    }
    
    public void addTradosWordCount(ReportWordCount p_reportWordCount)
    {
        addTrados100WordCount(p_reportWordCount.getTrados100WordCount());
        addTradosInContextMatchWordCount(p_reportWordCount.getTradosInContextMatchWordCount());
        addTradosContextMatchWordCount(p_reportWordCount.getTradosContextMatchWordCount());
        addTrados95to99WordCount(p_reportWordCount.getTrados95to99WordCount());
        addTrados85to94WordCount(p_reportWordCount.getTrados85to94WordCount());
        addTrados75to84WordCount(p_reportWordCount.getTrados75to84WordCount());
        addTrados50to74WordCount(p_reportWordCount.getTrados50to74WordCount());
        addTradosNoMatchWordCount(p_reportWordCount.getTradosNoMatchWordCount());
        addTradosRepsWordCount(p_reportWordCount.getTradosRepsWordCount());
    }

    public ReportWordCount clone() 
    {
		try 
		{
			return (ReportWordCount) super.clone();
		} 
		catch (CloneNotSupportedException e) 
		{
			e.printStackTrace();
		}
		
		return new ReportWordCount();
	}
    
    
}
