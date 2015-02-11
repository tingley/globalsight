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
package com.globalsight.diplomat.util.previewUrlXml;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;

import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.globalsight.diplomat.util.Logger;

/**
 * PreviewUrlXmlParser
 * <p>
 * This class can parse a PreviewUrlXml document and store its contents.
 */
public class PreviewUrlXmlParser
{
    // private members
    private String m_previewUrlXml; // the preview Url XML string
    private UrlElement[] m_srcUrls;
    private UrlElement[] m_tgtUrls;
    private String m_srcLocale;
    private String m_tgtLocale;
    private String m_srcUrlListEncoding = "UTF-8"; // the IANA character
                                                   // encoding
    private String m_tgtUrlListEncoding = "UTF-8"; // the IANA character
                                                   // encoding

    /**
     * Return a PreviewUrlXmlParser object that knows how to parse the given
     * previewUrlXml for the given srcLocale and tgtLocale
     * 
     * @param a
     *            string containing the Preview Url Xml
     * @param the
     *            source locale
     * @param the
     *            target locale
     */
    public PreviewUrlXmlParser(String p_previewUrlXml, String p_srcLocale,
            String p_tgtLocale)
    {
        m_previewUrlXml = p_previewUrlXml;
        m_srcLocale = p_srcLocale;
        m_tgtLocale = p_tgtLocale;
        m_srcUrls = null;
        m_tgtUrls = null;
    }

    /**
     * Return a PreviewUrlXmlParser object that knows how to parse the given
     * previewUrlXml for the given srcLocale and tgtLocale
     * 
     * @param a
     *            URL to a file of PreviewUrlXml
     * @param the
     *            source locale
     * @param the
     *            target locale
     * @throws PreviewUrlXmlException
     * @throws IOException
     */
    public PreviewUrlXmlParser(URL p_previewUrlXml, String p_srcLocale,
            String p_tgtLocale) throws PreviewUrlXmlException, IOException
    {
        m_srcLocale = p_srcLocale;
        m_tgtLocale = p_tgtLocale;
        m_srcUrls = null;
        m_tgtUrls = null;

        if (p_previewUrlXml == null)
            throw new PreviewUrlXmlException("URL to PreviewUrlXml is null.");

        // open the URL and read the contents
        InputStream is = p_previewUrlXml.openStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        StringBuffer sb = new StringBuffer("");
        String s;
        while ((s = br.readLine()) != null)
            sb.append(s + "\n");
        m_previewUrlXml = sb.toString();
    }

    /**
     * Parses the PreviewUrlXml and fills the internal structures with its
     * contents.
     * 
     * @throws PreviewUrlXmlException
     *             if expected elements are not found.
     */
    public void parse() throws PreviewUrlXmlException
    {
        Element srcLocaleElement = null;
        Element tgtLocaleElement = null;

        try
        {
            StringReader sr = new StringReader(m_previewUrlXml);
            InputSource is = new InputSource(sr);
            DOMParser parser = new DOMParser();
            parser.setFeature("http://xml.org/sax/features/validation", false); // don't
                                                                                // validate
            parser.parse(is);
            Element elem = parser.getDocument().getDocumentElement();

            // System.out.println("parsed.");
            // get all the locale elements
            NodeList nl = elem.getElementsByTagName("locale");
            int n;

            // first find the elements corresponding to the src and tgt locales
            for (n = 0; n < nl.getLength()
                    && (srcLocaleElement == null || tgtLocaleElement == null); n++)
            {
                Element localeElement = (Element) nl.item(n);
                String localeName = localeElement.getAttribute("name");
                // System.out.println("On locale id " + localeName);
                if (m_srcLocale.equals(localeName))
                {
                    srcLocaleElement = localeElement;
                    // System.out.println("Found srcLocaleElement");
                }
                if (m_tgtLocale.equals(localeName))
                {
                    tgtLocaleElement = localeElement;
                    // System.out.println("Found tgtLocaleElement");
                }
            }
        }
        catch (Exception e)
        {
            throw new PreviewUrlXmlException(e.getMessage());
        }

        parseUrls(srcLocaleElement, "sourceUrls");
        parseUrls(tgtLocaleElement, "targetUrls");
    }

    /**
     * Returns an array of UrlElement objects corresponding to the source URLs
     * in the XML.
     * 
     * @return array of UrlElement
     */
    public UrlElement[] getSourceUrls()
    {
        return m_srcUrls;

    }

    /**
     * Returns an array of UrlElement objects corresponding to the target URLs
     * in the XML.
     * 
     * @return array of UrlElement
     */
    public UrlElement[] getTargetUrls()
    {
        return m_tgtUrls;
    }

    /** Returns the source URL list IANA encoding */
    public String getSourceUrlListEncoding()
    {
        return m_srcUrlListEncoding;
    }

    /** Returns the target URL list IANA encoding */
    public String getTargetUrlListEncoding()
    {
        return m_tgtUrlListEncoding;
    }

    /**
     * Takes the named locale element and a URL sub element choice
     * ("sourceUrls", or "targetUrls") and fills the appropriate internal
     * structure
     * 
     * @param the
     *            locale element
     * @param "sourceUrls" or "targetUrls"
     * @throws PreviewUrlXmlException
     */
    private void parseUrls(Element p_localeElement, String p_choice)
            throws PreviewUrlXmlException
    {
        String theLocale = null;
        String description = null;
        UrlElement[] theUrlArray = null;

        if (p_choice.equals("sourceUrls"))
        {
            theLocale = m_srcLocale;
            description = "source";
        }
        else
        {
            theLocale = m_tgtLocale;
            description = "target";
        }

        try
        {
            NodeList nl = null;
            int n = 0;
            Element argNode = null;
            Element parameterNode = null;
            Element valueNode = null;
            String parameter = null;
            String value = null;
            String par_subSource = null;
            String val_subSource = null;
            String type = null;
            String href = null;
            String label = null;

            nl = p_localeElement.getElementsByTagName(p_choice);
            Element urlElementParent = (Element) nl.item(0);

            // record the IANA character encoding
            String encoding = urlElementParent.getAttribute("encoding");
            theLogger.println(Logger.DEBUG_D,
                    "PreviewUrlXmlParser: read encoding " + encoding + " for "
                            + p_choice);
            if (encoding == null || encoding.equals(""))
                encoding = "UTF-8";

            if (description.equals("source"))
                m_srcUrlListEncoding = encoding;
            else
                m_tgtUrlListEncoding = encoding;

            nl = urlElementParent.getElementsByTagName("url");

            int urlLength = nl.getLength();
            if (description.equals("source"))
            {
                m_srcUrls = new UrlElement[urlLength];
                theUrlArray = m_srcUrls;
            }
            else
            {
                m_tgtUrls = new UrlElement[urlLength];
                theUrlArray = m_tgtUrls;
            }

            for (n = 0; n < urlLength; n++)
            {
                Element urlElement = (Element) nl.item(n);
                type = urlElement.getAttribute("type");
                // System.out.println("URL type is " + type);
                href = urlElement.getElementsByTagName("href").item(0)
                        .getFirstChild().getNodeValue();
                // System.out.println("\tHREF = " + href);
                label = urlElement.getElementsByTagName("label").item(0)
                        .getFirstChild().getNodeValue();

                // now print out args
                NodeList urlArgs = urlElement.getElementsByTagName("arg");
                int argLength = urlArgs.getLength();
                ArgElement[] theArgs = new ArgElement[argLength];
                for (int j = 0; j < argLength; j++)
                {
                    argNode = (Element) urlArgs.item(j);
                    parameterNode = null;
                    valueNode = null;
                    parameter = null;
                    value = null;
                    par_subSource = null;
                    val_subSource = null;

                    parameterNode = (Element) argNode.getElementsByTagName(
                            "parameter").item(0);
                    parameter = parameterNode.getFirstChild().getNodeValue();
                    par_subSource = parameterNode
                            .getAttribute("substitution_source");
                    if (par_subSource == null || par_subSource.equals(""))
                        par_subSource = "none";

                    NodeList valueList = argNode.getElementsByTagName("value");
                    if (valueList.getLength() > 0)
                    {
                        valueNode = (Element) valueList.item(0);
                        value = valueNode.getFirstChild().getNodeValue();
                        val_subSource = valueNode
                                .getAttribute("substitution_source");
                        if (val_subSource == null || val_subSource.equals(""))
                            val_subSource = "none";
                    }
                    else
                    {
                        value = null;
                        val_subSource = null;
                    }

                    theArgs[j] = new ArgElement(parameter, par_subSource,
                            value, val_subSource);
                    /*
                     * System.out.println(theArgs[j].getParameter() +"="+
                     * theArgs[j].getValue() +" ("+
                     * theArgs[j].isParameterSubstitutable() +"," +
                     * theArgs[j].isValueSubstitutable() +") (" +
                     * theArgs[j].getParameterSubstitutionSource() +"," +
                     * theArgs[j].getValueSubstitutionSource() + ")");
                     */
                }

                // now create the UrlElement object and stick in the array
                theUrlArray[n] = new UrlElement(href, type, label, theArgs);
            }
        }
        catch (Exception e)
        {
            Logger.getLogger().println(
                    Logger.ERROR,
                    "There are no " + description + " URLs for locale "
                            + theLocale);
            throw new PreviewUrlXmlException(e.getMessage());
        }
    }

    private Logger theLogger = Logger.getLogger();

    public static void main(String[] args) throws Exception
    {
        if (args.length < 3)
        {
            System.out
                    .println("USAGE: PreviewUrlXmlParser <url> <srcLocale> <tgtLocale>");
            System.exit(0);
        }

        String url = args[0];
        String srcLocale = args[1];
        String tgtLocale = args[2];

        PreviewUrlXmlParser p = new PreviewUrlXmlParser(new URL(url),
                srcLocale, tgtLocale);
        p.parse();

        UrlElement[] srcUrls = p.getSourceUrls();
        UrlElement[] tgtUrls = p.getTargetUrls();

        System.out.println("Source URLs for locale " + srcLocale);
        int i;
        int j;
        UrlElement u;
        ArgElement a;

        for (i = 0; i < srcUrls.length; i++)
        {
            u = srcUrls[i];
            System.out.println(u.getLabel() + " " + u.getType() + " "
                    + u.getHref());
            for (j = 0; j < u.getNumArgs(); j++)
            {
                a = u.getArg(j);
                System.out.print("\t" + a.getParameter());
                System.out.print(" = " + a.getValue());
                System.out.print(" (" + a.isParameterSubstitutable());
                System.out.println("," + a.isValueSubstitutable() + ")");
            }
        }

        System.out.println("Target URLs for locale " + tgtLocale);
        for (i = 0; i < srcUrls.length; i++)
        {
            u = srcUrls[i];
            System.out.println(u.getLabel() + " " + u.getType() + " "
                    + u.getHref());
            for (j = 0; j < u.getNumArgs(); j++)
            {
                a = u.getArg(j);
                System.out.print("\t" + a.getParameter());
                System.out.print(" = " + a.getValue());
                System.out.print(" (" + a.isParameterSubstitutable());
                System.out.println("," + a.isValueSubstitutable() + ")");
            }
        }
    }
}
