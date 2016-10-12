package com.globalsight.terminology.importer;

import static org.junit.Assert.*;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.ElementHandler;
import org.dom4j.ElementPath;
import org.dom4j.io.SAXReader;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;

public class ImportUtilTest
{
    private static String sourceRoot = getResourcePath("term/00.xml");
    
    @Before
    public void init()
    {
        System.out.println("Before :: init()");
    }

    @After
    public void clear()
    {
        System.out.println("After :: clear()");
    }
    
    @Test
    public void testFilterateXmlIllegal()
    {
        try{
            analyzeXml(sourceRoot);
        }
        catch(Exception e) {
            fail("fiterate terminology xml failed!");  
        }
        
        assertTrue(true);
    }
    
    private static String getResourcePath(String relativePath) {
        return ImportUtilTest.class.getResource(relativePath).getFile();
    }
    
    private void analyzeXml(String p_url) throws Exception
    {
        SAXReader reader = new SAXReader();
        reader.setXMLReaderClassName("org.apache.xerces.parsers.SAXParser");

        // enable element complete notifications to conserve memory
        reader.addHandler("/entries/conceptGrp", new ElementHandler()
        {
            public void onStart(ElementPath path)
            {
            }

            public void onEnd(ElementPath path)
            {
                Element element = path.getCurrent();

                // prune the current element to reduce memory
                element.detach();
            }
        });

        ImportUtil.filterateXmlIllegal(p_url, null);
        Document document = reader.read(p_url);

        // all done
    }

}
