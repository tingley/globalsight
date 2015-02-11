package com.globalsight.ling.common;

import java.util.Properties;

/**
 * No-op base implementation.
 */
public class TuvSegmentBaseHandler implements DiplomatBasicHandler {

    protected XmlEntities m_xmlDecoder = new XmlEntities();
    
    @Override
    public void handleStartTag(String p_name, Properties p_atributes,
            String p_originalString) throws DiplomatBasicParserException
    {
    }
    
    @Override
    public void handleText(String p_text)
    {
    }
    
    @Override
    public void handleEndTag(String p_name, String p_originalTag)
    {
    }
    
    @Override
    public void handleStart()
    {
    }
    
    @Override
    public void handleStop()
    {
    }
}
