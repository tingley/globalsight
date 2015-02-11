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
package com.globalsight.util;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Node;

/**
 * Used for transforming xml to html for xml preview.
 */
public class XmlTransformer
{
    /**
     * Transforms xml to html.
     */
    public static String transform(Source xsl, Source xml) throws Exception
    {
        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer transformer = null;
        if (xsl != null)
        {
            transformer = factory.newTransformer(xsl);
        }
        else
        {
            transformer = factory.newTransformer();
        }
        StringWriter out = new StringWriter();
        StreamResult result = new StreamResult(out);
        transformer.setOutputProperty("encoding", "UTF-8");
        transformer.setOutputProperty("indent", "no");
        transformer.transform(xml, result);

        return result.getWriter().toString();
    }

    public static String transform(InputStream xsl, InputStream xml)
            throws Exception
    {
        return transform(new StreamSource(xsl), new StreamSource(xml));
    }

    public static String transform(InputStream xsl, Reader xml)
            throws Exception
    {
        return transform(new StreamSource(xsl), new StreamSource(xml));
    }

    public static String transform(Source xml) throws Exception
    {
        return transform(null, xml);
    }

    /**
     * Use com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl
     * to convert
     * 
     * @param node
     * @return
     * @throws TransformerFactoryConfigurationError
     * @throws TransformerException
     * @throws UnsupportedEncodingException
     */
    public static String convertNodeToString(Node node, String encoding)
            throws TransformerFactoryConfigurationError, TransformerException,
            UnsupportedEncodingException
    {
        String cname = "com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl";
        Transformer transformer = TransformerFactory.newInstance(cname,
                String.class.getClassLoader()).newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

        DOMSource source = new DOMSource(node);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        transformer.transform(source, new StreamResult(baos));

        return baos.toString(encoding);
    }
}
