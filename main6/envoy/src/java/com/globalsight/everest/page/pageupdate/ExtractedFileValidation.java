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
package com.globalsight.everest.page.pageupdate;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import com.globalsight.diplomat.util.Logger;
import com.globalsight.everest.page.PageManager;
import com.globalsight.everest.page.PagePersistenceAccessor;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.ling.docproc.DiplomatAPI;
import com.globalsight.ling.jtidy.Tidy;
import com.globalsight.util.gxml.GxmlElement;
import com.globalsight.util.gxml.GxmlFragmentReader;
import com.globalsight.util.gxml.GxmlFragmentReaderPool;
import com.globalsight.util.gxml.GxmlNames;
import com.globalsight.util.gxml.GxmlRootElement;

/**
 * <p>
 * Validates an imported and extracted file. Parses through updated gxml and
 * validates the updates.
 * </p>
 * 
 */
class ExtractedFileValidation
{
    // use a common logger category for all EditSourcePage classes
    private static final org.apache.log4j.Logger CATEGORY = org.apache.log4j.Logger
            .getLogger(ExtractedFileValidation.class);

    //
    // Members
    //

    private UpdateState m_state = null;
    private DiplomatAPI m_diplomat = null;
    private PageManager m_pageManager = null;

    //
    // package methods
    //
    // All methods are called from within the package.
    // PageUpdateAPI provides the interface.

    //
    // Constructor
    //

    ExtractedFileValidation(String p_gxml)
    {
        m_state = new UpdateState(null, p_gxml);
        init();
    }

    ExtractedFileValidation(SourcePage p_sourcePage, String p_gxml)
    {
        m_state = new UpdateState(p_sourcePage, p_gxml);
        init();
    }

    /**
     * Initializates RMI objects (only called from constructor).
     */
    private void init()
    {
        try
        {
            m_pageManager = ServerProxy.getPageManager();
        }
        catch (Exception ex)
        {
            CATEGORY.error("can't init remote objects", ex);
        }
    }

    //
    // Package Methods
    //

    /**
     * Validates GXML and HTML and returns a list of error strings.
     */
    ArrayList validateSourcePageGxml()
    {
        // 1. Verify validity of GXML
        if (!validateGxml().getValidated())
        {
            return m_state.getValidationMessages();
        }

        // Gxml Document no longer needed.
        m_state.setGxmlRoot(null);

        // 2. Verify validity of HTML, returns warning and error messages.
        validateHtml();
        return m_state.getValidationMessages();
    }

    /**
     * Validate the GXML and return the update state. The update state has a
     * method on it called "getValidated" to check if the validation was
     * confirmed or not.
     */
    UpdateState validateGxml()
    {
        GxmlFragmentReader reader = GxmlFragmentReaderPool.instance()
                .getGxmlFragmentReader();

        try
        {
            GxmlRootElement gxmlRoot = reader.parse(m_state.getGxml());

            m_state.setGxmlRoot(gxmlRoot);
            m_state.setDataFormat(gxmlRoot
                    .getAttribute(GxmlNames.GXMLROOT_DATATYPE));
            m_state.setHasGsTags(containGsTags(gxmlRoot));

            m_state.setValidated(true);
            m_state.clearValidationMessages();
        }
        catch (Throwable ex)
        {
            m_state.setValidated(false);
            m_state.addValidationMessage(ex.getMessage());
        }
        finally
        {
            GxmlFragmentReaderPool.instance().freeGxmlFragmentReader(reader);

            // State now holds the GxmlElement representation.
        }

        return m_state;
    }

    //
    // Private Methods
    //

    /**
     * Export the GXML to HTML and run jtidy(?) on it.
     */
    private boolean validateHtml()
    {
        // Non-HTML pages are OK by default.
        if (!m_state.getDataFormat().equals("html"))
        {
            return true;
        }

        try
        {
            String html = mergeGxml(m_state.getGxml());

            Logger.writeDebugFile("validationHtml.html", html);

            Tidy tidy = new Tidy();
            // getSourcePage().getExternalPageId()
            tidy.setInputStreamName("(input)");
            tidy.addMessageListener(m_state);

            InputStream in = new ByteArrayInputStream(html.getBytes());
            html = null;
            OutputStream out = null;
            tidy.parse(in, out);

            in.close();
            // out.close();

            m_state.setValidated(true);
            // m_state.clearValidationErrors();
        }
        catch (Throwable ex)
        {
            CATEGORY.error("Validation failed", ex);

            m_state.setValidated(false);
            m_state.addValidationMessage(ex.getMessage());
        }

        return m_state.getValidated();
    }

    /**
     * Calls the Diplomat merger to extract the original page from GXML.
     */
    private String mergeGxml(String p_gxml) throws Exception
    {
        DiplomatAPI api = getDiplomatApi();
        // Keep GS tags.
        return api.merge(p_gxml, true);
    }

    //
    // Helpers
    //

    /**
     * Return true if the page (Gxml) contains at least one GS tag.
     */
    private boolean containGsTags(GxmlRootElement p_root)
    {
        int gsTag[] =
        { GxmlElement.GS };
        List tagElements = p_root.getChildElements(gsTag);

        return (tagElements.size() > 0);
    }

    private DiplomatAPI getDiplomatApi()
    {
        if (m_diplomat == null)
        {
            m_diplomat = new DiplomatAPI();
        }

        m_diplomat.reset();

        return m_diplomat;
    }

    private SourcePage getSourcePage()
    {
        return m_state.getSourcePage();
    }

    private ArrayList getTargetPages() throws Exception
    {
        ArrayList result = m_state.getTargetPages();

        if (result == null)
        {
            long id = getSourcePage().getId();
            m_state.setTargetPages(new ArrayList(PagePersistenceAccessor
                    .getTargetPages(id)));

            result = m_state.getTargetPages();
        }

        return result;
    }
}
