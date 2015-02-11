package com.globalsight.ling.common;

/**
 * Simple implementation to extract text for exact matching.
 */
public class ExactMatchFormatHandler extends TuvSegmentBaseHandler {
    private StringBuffer m_content = new StringBuffer(200);

    // Overridden method
    public void handleText(String p_text)
    {
        // accumulate all the text
        m_content.append(m_xmlDecoder.decodeStringBasic(p_text));
    }

    public String toString()
    {
        return m_content.toString();
    }
}
