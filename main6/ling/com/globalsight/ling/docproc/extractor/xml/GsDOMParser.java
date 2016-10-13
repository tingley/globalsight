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
package com.globalsight.ling.docproc.extractor.xml;

import java.io.File;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.validation.Schema;

import org.apache.xerces.impl.Constants;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;

public class GsDOMParser
{
    public static String FACTORY_CLASS = "org.apache.xerces.jaxp.GSDocumentBuilderFactoryImpl";

    private DocumentBuilderFactory factory;

    private DocumentBuilder builder;

    public GsDOMParser() throws Exception
    {
        factory = DocumentBuilderFactory.newInstance(FACTORY_CLASS, null);
        // setFeature - AbstractMethodError
        // factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setExpandEntityReferences(true);
        factory.setFeature(Constants.XERCES_FEATURE_PREFIX
                + Constants.UNPARSED_ENTITY_CHECKING_FEATURE, false);
        factory.setFeature(Constants.XERCES_FEATURE_PREFIX
                + Constants.CREATE_ENTITY_REF_NODES_FEATURE, true);
        factory.setFeature(Constants.XERCES_FEATURE_PREFIX
                + Constants.CREATE_CDATA_NODES_FEATURE, true);
        factory.setFeature(Constants.XERCES_FEATURE_PREFIX
                + Constants.CONTINUE_AFTER_FATAL_ERROR_FEATURE, true);

        createBuilder();
    }

    public GsDOMParser(String param) throws Exception
    {
        factory = DocumentBuilderFactory.newInstance(param, null);
        factory.setNamespaceAware(true);
        factory.setExpandEntityReferences(true);
        factory.setFeature(Constants.XERCES_FEATURE_PREFIX
                + Constants.UNPARSED_ENTITY_CHECKING_FEATURE, false);
        factory.setFeature(Constants.XERCES_FEATURE_PREFIX
                + Constants.CREATE_ENTITY_REF_NODES_FEATURE, true);
        factory.setFeature(Constants.XERCES_FEATURE_PREFIX
                + Constants.CREATE_CDATA_NODES_FEATURE, true);
        factory.setFeature(Constants.XERCES_FEATURE_PREFIX
                + Constants.CONTINUE_AFTER_FATAL_ERROR_FEATURE, true);
        
        createBuilder();
    }

    private void createBuilder() throws Exception
    {
        builder = factory.newDocumentBuilder();
    }

    public Document parse(File file) throws Exception
    {
        return builder.parse(file);
    }

    public Document parse(InputSource is) throws Exception
    {
        return builder.parse(is);
    }

    public Document parse(InputStream is) throws Exception
    {
        return builder.parse(is);
    }

    public Document parse(String uri) throws Exception
    {
        return builder.parse(uri);
    }

    public void setValidating(boolean validating) throws Exception
    {
        factory.setValidating(validating);
        // refresh the builder
        createBuilder();
    }

    public void setAttribute(String name, Object value) throws Exception
    {
        factory.setAttribute(name, value);
        // refresh the builder
        createBuilder();
    }

    public void setSchema(Schema schema) throws Exception
    {
        factory.setSchema(schema);
        // refresh the builder
        createBuilder();
    }

    public void setEntityResolver(EntityResolver er)
    {
        builder.setEntityResolver(er);
    }

    public void setErrorHandler(ErrorHandler eh)
    {
        builder.setErrorHandler(eh);
    }
}
