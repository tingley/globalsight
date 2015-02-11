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

package com.globalsight.ling.sgml.sgmldtd;

import com.globalsight.ling.sgml.GlobalSightDtdParser;

import com.globalsight.ling.sgml.GlobalSightDtd;
import com.globalsight.ling.sgml.catalog.Catalog;

import com.globalsight.ling.sgml.sgmldtd.DTDParser;

import java.net.URL;

/**
 * Wraps Mark Wutka's SGML DTD Parser into a neutral interface.
 */
public class DtdParserAdapter implements GlobalSightDtdParser
{
    DTDParser parser;
    Catalog catalog;

    public DtdParserAdapter()
    {
    }

    /**
     * Parses a DTD from the given URL, returning a DTD interface.
     */
    public GlobalSightDtd parseDtd(URL p_url)
        throws Exception
    {
        parser = new DTDParser(p_url, catalog);

        return new DtdAdapter(parser.parse());
    }

    /**
     * Parses a DTD from the given URL, returning a DTD interface.
     */
    public GlobalSightDtd parseDtd(URL p_url, boolean p_trace)
        throws Exception
    {
        parser = new DTDParser(p_url, catalog, p_trace);

        return new DtdAdapter(parser.parse());
    }

    /**
     * Sets the catalog with which to resolve external DTD.
     */
    public void setCatalog(Catalog p_catalog)
    {
        catalog = p_catalog;
    }
}
