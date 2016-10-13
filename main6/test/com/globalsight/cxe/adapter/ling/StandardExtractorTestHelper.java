package com.globalsight.cxe.adapter.ling;

import org.apache.log4j.Logger;

import com.globalsight.cxe.message.CxeMessage;

public class StandardExtractorTestHelper
{
    public static StandardExtractor getInstance(Logger p_logger,
            CxeMessage p_cxeMessage)
    {
        return new StandardExtractor(p_logger, p_cxeMessage);
    }
}
