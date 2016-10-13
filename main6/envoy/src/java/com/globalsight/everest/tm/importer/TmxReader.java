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

package com.globalsight.everest.tm.importer;

import java.io.File;
import java.util.HashSet;
import java.util.List;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.ElementHandler;
import org.dom4j.ElementPath;
import org.dom4j.io.SAXReader;

import com.globalsight.cxe.adapter.msoffice.MicrosoftWordHelperForTmx;
import com.globalsight.everest.tm.Tm;
import com.globalsight.everest.tm.util.DtdResolver;
import com.globalsight.everest.tm.util.Tmx;
import com.globalsight.everest.tm.util.trados.StylesheetRemover;
import com.globalsight.everest.tm.util.trados.TradosFmSgmlTmxToGxml;
import com.globalsight.everest.tm.util.trados.TradosFmTmxToGxml;
import com.globalsight.everest.tm.util.trados.TradosHtmlTmxToGxml;
import com.globalsight.everest.tm.util.trados.TradosQxTmxToGxml;
import com.globalsight.everest.tm.util.trados.TradosTmxToRtf;
import com.globalsight.everest.tm.util.trados.WordHtmlToTmx;
import com.globalsight.importer.IReader;
import com.globalsight.importer.ImportOptions;
import com.globalsight.util.ReaderResult;
import com.globalsight.util.ReaderResultQueue;

/**
 * Reads TMX files and produces TU objects.
 */
public class TmxReader
    implements IReader
{
    private static final Logger CATEGORY =
        Logger.getLogger(
            TmxReader.class);

    //
    // Private Member Variables
    //
    private Tm m_database;
    private ImportOptions m_options;
    private int m_tmxLevel;
    private int m_entryCount;

    private TmxReaderThread m_thread = null;
    private ReaderResultQueue m_results = null;
    private ReaderResult m_result;

    // For analysis: TMX header, version, and src/trg locales of TUVs.
    private Tmx m_header;
    private String m_tmxVersion;
    private HashSet m_sourceLocales;
    private HashSet m_targetLocales;

    //
    // Constructors
    //

    public TmxReader (ImportOptions p_options, Tm p_database)
    {
        m_database = p_database;
        setImportOptions(p_options);
    }

    //
    // Interface Implementation -- IReader
    //

    public void setImportOptions(ImportOptions p_options)
    {
        m_options = p_options;

        m_tmxLevel = ImportUtil.getTmxLevel(m_options);
    }

    public boolean hasNext()
    {
        // Ensure the thread is running
        startThread();

        m_result = m_results.get();

        if (m_result != null)
        {
            return true;
        }

        // No more results, clean up
        stopThread();
        return false;
    }

    public ReaderResult next()
    {
        return m_result;
    }

    /**
     * Analyzes the import file and returns an updated ImportOptions
     * object with a status whether the file is syntactically correct,
     * the number of expected entries, and column descriptors in case
     * of CSV files.
     */
    public ImportOptions analyze()
    {
        m_entryCount = 0;

        com.globalsight.everest.tm.importer.ImportOptions options =
            (com.globalsight.everest.tm.importer.ImportOptions)m_options;

        try
        {
            if (!m_options.getStatus().equals(m_options.ANALYZED))
            {
                // Check if the file is a valid XML file.
                analyzeXml(m_options.getFileName());

                options.setSourceLocales(m_sourceLocales);
                options.setTargetLocales(m_targetLocales);

                m_options.setStatus(m_options.ANALYZED);
                m_options.setExpectedEntryCount(m_entryCount);
            }
        }
        catch (Exception ex)
        {
            m_options.setError(ex.getMessage());
        }

        return m_options;
    }

    //
    // Private Methods
    //

    private void startThread()
    {
        if (m_thread == null)
        {
            m_results = new ReaderResultQueue (ImportManager.BATCHSIZE);
            m_thread = new TmxReaderThread(m_results, m_options, m_database);
            m_thread.start();
        }
    }

    private void stopThread()
    {
        if (m_thread != null)
        {
            m_results.consumerDone();
            m_results = null;
            m_thread = null;
        }
    }


    /**
     * Reads an XML file and checks its correctness by validating
     * against the TMX DTD. If there's any error in the file, an
     * exception is thrown.
     *
     * As a side effect, this method builds a list of source and
     * target locales found in the file, including the declared source
     * locale from the header.
     */
    private void analyzeXml(String p_url)
        throws Exception
    {
        if (m_tmxLevel == ImportUtil.TMX_LEVEL_TRADOS_RTF ||
            m_tmxLevel == ImportUtil.TMX_LEVEL_TRADOS_HTML ||
            m_tmxLevel == ImportUtil.TMX_LEVEL_TRADOS_FM ||
            m_tmxLevel == ImportUtil.TMX_LEVEL_TRADOS_FM_SGML ||
            m_tmxLevel == ImportUtil.TMX_LEVEL_TRADOS_IL ||
            m_tmxLevel == ImportUtil.TMX_LEVEL_TRADOS_XPTAG)
        {
            // Convert the Trados codes to native System4 codes by
            // converting the file to RTF, saving it as HTML and
            // extracting the resulting TUVs.

            CATEGORY.info("Converting Trados TMX to native TMX: " + p_url);

            p_url = convertTradosTmx(p_url, m_tmxLevel);

            // Now we have a new file that contains native content.
            m_options.setFileName(p_url);
            m_options.setFileType(
                com.globalsight.everest.tm.importer.ImportOptions.TYPE_XML);

            m_tmxLevel = ImportUtil.TMX_LEVEL_NATIVE;
        }

        CATEGORY.debug("Analyzing document: " + p_url);

        // Reset list of locales found in the file.
        m_sourceLocales = new HashSet();
        m_targetLocales = new HashSet();

        SAXReader reader = new SAXReader();
        reader.setXMLReaderClassName("org.apache.xerces.parsers.SAXParser");

        // Read the DTD and validate.
        reader.setEntityResolver(DtdResolver.getInstance());
        reader.setValidation(true);

        // enable element complete notifications to conserve memory
        reader.addHandler("/tmx",
            new ElementHandler()
                {
                    public void onStart(ElementPath path)
                    {
                        Element element = path.getCurrent();

                        m_tmxVersion = element.attributeValue(Tmx.VERSION);
                    }

                    public void onEnd(ElementPath path)
                    {
                    }
                }
            );

        reader.addHandler("/tmx/header",
            new ElementHandler()
                {
                    public void onStart(ElementPath path)
                    {
                    }

                    public void onEnd(ElementPath path)
                    {
                        Element element = path.getCurrent();

                        m_header = new Tmx(element);
                        m_header.setTmxVersion(m_tmxVersion);

                        element.detach();
                    }
                }
            );

        reader.addHandler("/tmx/body/tu",
            new ElementHandler ()
                {
                    public void onStart(ElementPath path)
                    {
                        ++m_entryCount;
                    }

                    public void onEnd(ElementPath path)
                    {
                        Element element = path.getCurrent();

                        // Record optional source language declared on TU.
                        String srclang = element.attributeValue(Tmx.SRCLANG);
                        if (srclang != null)
                        {
                            m_sourceLocales.add(
                                ImportUtil.normalizeLocale(srclang));
                        }

                        // Find target languages
                        HashSet langs = new HashSet();
                        List tuvs = element.selectNodes("./tuv");

                        for (int i = 0, max = tuvs.size(); i < max; i++)
                        {
                            Element tuv = (Element)tuvs.get(i);

                            String lang = tuv.attributeValue(Tmx.LANG);

                            // Collect TUV locales
                            langs.add(ImportUtil.normalizeLocale(lang));
                        }

                        langs.remove(srclang);
                        m_targetLocales.addAll(langs);

                        // prune the current element to reduce memory
                        element.detach();
                    }
                }
            );

        Document document = reader.read(p_url);

        // Add declared source language from header.
        String sourceLocale =
            ImportUtil.normalizeLocale(m_header.getSourceLang());
        

        m_sourceLocales.add(sourceLocale);
    }

    /**
     * <p>Converts the RTF codes in Trados TMX files to native
     * GlobalSight codes.</p>
     *
     * <p>TMX files with RTF content are converted to RTF, saved as
     * HTML and then the resulting TUVs are extracted.</p>
     *
     * <p>TMX files with HTML or Framemaker content are converted in
     * one single step.</p>
     *
     * <p>If debugging is enabled for this class, intermediate files
     * will NOT be deleted. Else, all intermediate files will be
     * deleted and only the final result file will be kept.
     */
    public String convertTradosTmx(String p_url, int p_level)
        throws Exception
    {
        String file = p_url;
        String prevFile = file;

        try
        {
            if (m_tmxLevel == ImportUtil.TMX_LEVEL_TRADOS_HTML)
            {
                TradosHtmlTmxToGxml conv = new TradosHtmlTmxToGxml(CATEGORY);

                file = conv.convertToGxml(file);

                if (!CATEGORY.isDebugEnabled())
                {
                    new File(prevFile).delete();
                }
            }
            else if (m_tmxLevel == ImportUtil.TMX_LEVEL_TRADOS_FM)
            {
                TradosFmTmxToGxml conv = new TradosFmTmxToGxml(CATEGORY);

                file = conv.convertToGxml(file);

                if (!CATEGORY.isDebugEnabled())
                {
                    new File(prevFile).delete();
                }
            }
            else if (m_tmxLevel == ImportUtil.TMX_LEVEL_TRADOS_FM_SGML)
            {
                TradosFmSgmlTmxToGxml conv = new TradosFmSgmlTmxToGxml(CATEGORY);

                file = conv.convertToGxml(file);

                if (!CATEGORY.isDebugEnabled())
                {
                    new File(prevFile).delete();
                }
            }
            /* TODO
            else if (m_tmxLevel == ImportUtil.TMX_LEVEL_TRADOS_IL)
            {
                conv = new (CATEGORY);

                file = conv.convertToGxml(file);

                if (!CATEGORY.isDebugEnabled())
                {
                    new File(prevFile).delete();
                }
            }
            */
            else if (m_tmxLevel == ImportUtil.TMX_LEVEL_TRADOS_XPTAG)
            {
                TradosQxTmxToGxml conv = new TradosQxTmxToGxml(CATEGORY);

                file = conv.convertToGxml(file);

                if (!CATEGORY.isDebugEnabled())
                {
                    new File(prevFile).delete();
                }
            }
            else
            {
                // 1) TMX to RTF
                TradosTmxToRtf conv1 = new TradosTmxToRtf(CATEGORY);
                file = conv1.convertToRtf(file);

                if (!CATEGORY.isDebugEnabled())
                {
                    new File(prevFile).delete();
                }
                prevFile = file;

                // 2) RTF to HTML
                MicrosoftWordHelperForTmx conv2 =
                    new MicrosoftWordHelperForTmx(CATEGORY);
                file = conv2.performConversion(file);

                if (!CATEGORY.isDebugEnabled())
                {
                    new File(prevFile).delete();
                }
                prevFile = file;

                // 3) Remove a possibly very large stylesheet from the
                // HTML file
                StylesheetRemover conv3 = new StylesheetRemover(CATEGORY);
                file = conv3.removeStylesheet(file);

                if (!CATEGORY.isDebugEnabled())
                {
                    new File(prevFile).delete();
                }
                prevFile = file;

                // 4) HTML to TMX
                if (m_tmxLevel == ImportUtil.TMX_LEVEL_TRADOS_RTF)
                {
                    WordHtmlToTmx conv4 = new WordHtmlToTmx(CATEGORY);
                    file = conv4.convertHtmlToTmx(file);
                }

                if (!CATEGORY.isDebugEnabled())
                {
                    new File(prevFile).delete();
                }
            }
        }
        catch (Exception ex)
        {
            CATEGORY.error("TMX Conversion failed for file " + p_url, ex);

            throw ex;
        }

        return file;
    }
}
