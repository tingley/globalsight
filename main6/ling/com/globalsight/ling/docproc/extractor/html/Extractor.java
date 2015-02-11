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
package com.globalsight.ling.docproc.extractor.html;

import java.io.Reader;

import com.globalsight.cxe.entity.filterconfiguration.Filter;
import com.globalsight.cxe.entity.filterconfiguration.FilterConstants;
import com.globalsight.cxe.entity.filterconfiguration.FilterHelper;
import com.globalsight.cxe.entity.filterconfiguration.HtmlFilter;
import com.globalsight.cxe.entity.filterconfiguration.MSOfficeDocFilter;
import com.globalsight.cxe.entity.filterconfiguration.MSOfficeExcelFilter;
import com.globalsight.cxe.entity.filterconfiguration.MSOfficePPTFilter;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.ling.docproc.AbstractExtractor;
import com.globalsight.ling.docproc.ExtractorException;
import com.globalsight.ling.docproc.ExtractorExceptionConstants;
import com.globalsight.ling.docproc.ExtractorRegistry;


public class Extractor
    extends AbstractExtractor implements IHTMLConstants, IHtmlExtractor
{
    //
    // Private Variables
    //
    private IHtmlExtractor extend = new HtmlExtractorImpl();
    private Parser parser;
    private long msOfficeDocFilterId = -1;
    protected ExtractionRules m_rules = null;
    protected int m_xspLanguage = EC_UNKNOWNSCRIPT;
    private String filterTableName;
    
    private static Boolean IGNORE_INVALID_HTML_TAGS = null;    
    //
    // Constructors
    //

    public Extractor()
    {
        super();

        m_rules = new ExtractionRules();
    }

    public void setRules(String fileProfileId, long filterId)
    {
        m_rules = new ExtractionRules(fileProfileId, filterId);
    }
    
    public void useDefaultRules()
    {
        if (m_rules == null)
            m_rules = new ExtractionRules();
        m_rules.setConfigurationValues();
    }
    
    //
    // Public Methods
    //
    public Parser getParser()
    {
        return parser;
    }

    //
    // Will be overwritten in classes derived from HTML extractor (CF,
    // JHTML).
    //
    public void setFormat()
    {
        setMainFormat(ExtractorRegistry.FORMAT_HTML);
    }

    protected void setXspLanguage(int p_xspLanguage)
    {
        m_xspLanguage = p_xspLanguage;
    }

    //
    // Interface Implementation -- ExtractorInterface
    //

    public boolean isIgnoreInvalidHtmlTags()
    {
        if (IGNORE_INVALID_HTML_TAGS == null)
        {
            SystemConfiguration tagsProperties = SystemConfiguration
                .getInstance("/properties/Tags.properties");
            String ignore = tagsProperties
                    .getStringParameter("IgnoreInvalidHtmlTags");
            if (ignore != null)
            {
                IGNORE_INVALID_HTML_TAGS = "true".equalsIgnoreCase(ignore.trim());
            }
            else
            {
                IGNORE_INVALID_HTML_TAGS = true;
            }
        }
        
        return IGNORE_INVALID_HTML_TAGS.booleanValue();
    }
    
    public void extract()
        throws ExtractorException
    {
        // Set the main format depending on which (derived) class
        // we're called in.
        setFormat();

        ExtractionHandler extractor =
            new ExtractionHandler (getInput(), getOutput(), this, m_rules,
                m_xspLanguage);
        
        long filterId = getFilterId();
        String jsFunctionText = null;
        boolean isIgnoreInvalidHtmlTags = isIgnoreInvalidHtmlTags();
        if(FilterConstants.HTML_TABLENAME.equals(filterTableName) && filterId > 0)
        {
            HtmlFilter htmlFilte = FilterHelper.getHtmlFilter(filterId);
            jsFunctionText = htmlFilte.getJsFunctionText();
            isIgnoreInvalidHtmlTags = htmlFilte.isIgnoreInvalideHtmlTags();
            extractor.setHtmlInternalTags(htmlFilte.getInternalTags());
            //always not extract the charset
            m_rules.setExtractCharset(false);
        }
        if ( (FilterConstants.MSOFFICEDOC_TABLENAME.equals(filterTableName)
        	|| FilterConstants.MSOFFICEPPT_TABLENAME.equals(filterTableName)
        	|| FilterConstants.MSOFFICEEXCEL_TABLENAME.equals(filterTableName)) && filterId > 0)
        {
        	try {
        		HtmlFilter htmlFilter = null;

        		Filter officeFilter = FilterHelper.getFilter(filterTableName, filterId);
        		if (officeFilter instanceof MSOfficeDocFilter) {
        			officeFilter = (MSOfficeDocFilter) officeFilter;
        			htmlFilter = FilterHelper.getHtmlFilter(
        					((MSOfficeDocFilter) officeFilter).getSecondFilterId());
        		} else if (officeFilter instanceof MSOfficeExcelFilter) {
        			officeFilter = (MSOfficeExcelFilter) officeFilter;
        			htmlFilter = FilterHelper.getHtmlFilter(
        					((MSOfficeExcelFilter) officeFilter).getSecondFilterId());
        		} else if (officeFilter instanceof MSOfficePPTFilter) {
        			officeFilter = (MSOfficePPTFilter) officeFilter;
        			htmlFilter = FilterHelper.getHtmlFilter(
        					((MSOfficePPTFilter) officeFilter).getSecondFilterId());
        		}
        		
        		if (htmlFilter != null) {
    	            jsFunctionText = htmlFilter.getJsFunctionText();
    	            isIgnoreInvalidHtmlTags = htmlFilter.isIgnoreInvalideHtmlTags();
    	            extractor.setHtmlInternalTags(htmlFilter.getInternalTags());        			
        		}
        		else
        		{
        		    isIgnoreInvalidHtmlTags = true;
        		}
	            //always not extract the charset
	            m_rules.setExtractCharset(false);
			} catch (Exception e) {

			}
        }
        extractor.setIgnoreInvalidHtmlTags(isIgnoreInvalidHtmlTags);
        extractor.setJsFunctionText(jsFunctionText);
        
        Reader inputReader = readInput();
        parser = new Parser(inputReader);
        parser.setHandler(extractor);
        parser.setIgnoreInvalidHtmlTags(isIgnoreInvalidHtmlTags);
        parser.setJsFunctionText(jsFunctionText);
        try
        {
            parser.parse();
        }
        catch (ParseException e)
        {
            e.printStackTrace();
            throw new ExtractorException (
                ExtractorExceptionConstants.HTML_PARSE_ERROR, e.getMessage());
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            throw new ExtractorException (
                ExtractorExceptionConstants.INTERNAL_ERROR, e.toString());
        }

        String strError = extractor.checkError();
        if (strError != null)
        {
            throw new ExtractorException (
                ExtractorExceptionConstants.HTML_PARSE_ERROR, strError);
        }
    }

    public void loadRules()
        throws ExtractorException
    {
        String str_rules = getInput().getRules();
        m_rules.loadRules(str_rules);
        m_rules.loadRules(getDynamicRules());
    }

    public String getFileProfileId()
    {
        return extend.getFileProfileId();
    }

    public long getFilterId()
    {
        return extend.getFilterId();
    }

    public void setFileProfileId(String fileProfileId)
    {
        extend.setFileProfileId(fileProfileId);
    }

    public void setFilterId(long filterId)
    {
        extend.setFilterId(filterId);
    }
    
    public void setJsFunctionText(String jsFunctionText)
    {
        extend.setJsFunctionText(jsFunctionText);
    }
    
    public String getJsFunctionText()
    {
        return extend.getJsFunctionText();
    }
    
    public long getMSOfficeDocFilterId()
    {
        return msOfficeDocFilterId;
    }
    
    public void setMSOfficeDocFilterId(long filterId)
    {
        this.msOfficeDocFilterId = filterId;
    }

    public void setFilterTableName(String filterTableName)
    {
        this.filterTableName = filterTableName;
    }
}
