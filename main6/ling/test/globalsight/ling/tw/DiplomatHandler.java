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
package test.globalsight.ling.tw;


import java.util.Hashtable;
import java.util.Enumeration;
import com.globalsight.ling.common.DiplomatBasicParserException;
import com.globalsight.ling.tw.PseudoData;
import com.globalsight.ling.tw.PseudoErrorChecker;
import com.globalsight.ling.tw.PseudoConstants;
import com.globalsight.ling.tw.TmxPseudo;
import java.io.FileWriter;
import java.util.Properties;

/**
 * Event handler for parsing diplomat input test file.
 */
public class DiplomatHandler implements com.globalsight.ling.common.DiplomatBasicHandler
{private boolean m_bInTranslatable;
	
    private StringBuffer m_strOutput;

    private StringBuffer m_strBuffer;

    private com.globalsight.ling.tw.TmxPseudo m_Cvt;
	
    private java.lang.String m_strStartTag;

    /**
     * constructor.
     */
    public DiplomatHandler() {
        super();

        m_strOutput = new StringBuffer();
        m_strBuffer = new StringBuffer();
        m_Cvt = new TmxPseudo();
    }

    /**
     * Returns the results accumulated by the event handlers.
     * @return - String.
     */
    public String getResult() {
        return m_strOutput.toString();
    }

    /**
     * Handles diplomat basic parser Start event.
     */
    public void handleStart()
        throws DiplomatBasicParserException
    {
        return;
    }

    /**
     * Handles diplomat basic parser Stop event.
     */
    public void handleStop()
        throws DiplomatBasicParserException
    {
        return;
    }

    /**
     * Handles diplomat basic parser EndTag event.
     * @param p_strName java.lang.String
     * @param p_strOriginalTag java.lang.String
     */
    public void handleEndTag(String p_name, String p_originalTag)
        throws DiplomatBasicParserException
    {
        if( m_bInTranslatable && (p_name.equals("translatable") ||
            p_name.equals("localizable") ))
        {
            m_bInTranslatable = false;


            try
            {
                // process a segment
                PseudoData srcPD = new PseudoData();
                srcPD.setMode(PseudoConstants.PSEUDO_COMPACT);
                srcPD.setAddables("HTML");
                PseudoErrorChecker err = new PseudoErrorChecker();

                srcPD = m_Cvt.tmx2Pseudo(m_strBuffer.toString(), srcPD);
                srcPD.setPTagTargetString(srcPD.getPTagSourceString());
                String errmsg = err.check(srcPD);
                String TmxOut = m_Cvt.pseudo2Tmx(srcPD);

                m_strOutput.append("\r\n======================\r\n\r\nTMX_IN:\r\n" + m_strStartTag + "\r\n"+
                                    m_strBuffer.toString() + "\r\n");
                String TmxIn = m_strBuffer.toString();
                if(TmxOut.equals(TmxIn))
                {
                    m_strOutput.append("\r\nTMX_OUT: String Compare OK.\r\n"  +
                                    TmxOut + "\r\n");
                }
                else
                {
                    m_strOutput.append("\r\nTMX_OUT: ERROR String Compare Failed.\r\n" + TmxOut +"\r\n");
                    //throw new DiplomatBasicParserException("COMPARE FAILED! " + m_strStartTag + "\r\n" + TmxIn + "\r\n" + TmxOut);
                }

                if( errmsg != null )
                {
                    m_strOutput.append("\r\nERROR Checker: " +errmsg + "\n\n");
                }
                else
                {
                    m_strOutput.append("\r\nNo errors from error checker\n\n");
                }


                m_strOutput.append("\r\nPSEUDO:\r\n" + srcPD.getPTagSourceString() + "\r\n");
                m_strOutput.append("\r\nPSEUDO to TMX MAPING:\r\n");
                Hashtable h = srcPD.getPseudo2TmxMap();
                for (Enumeration e = h.keys() ; e.hasMoreElements() ;)
                {
                    String key = (String)e.nextElement();
                    String val = (String) h.get(key);
                    m_strOutput.append( key + "-->" + val + "\r\n");
                }

                m_strOutput.append("\r\nPSEUDO to NATIVE MAPING:\r\n");
                Hashtable Tmx2NativeMap = srcPD.getPseudo2NativeMap();
                for (Enumeration e = Tmx2NativeMap.keys() ; e.hasMoreElements() ;)
                {
                    String key = (String)e.nextElement();
                    String val = (String) Tmx2NativeMap.get(key);
                    m_strOutput.append( key + "-->" + val + "\r\n");
                }
            }
            catch( Exception e )
            {
                System.out.println(e + "\r\n");
                System.exit(1);
            }

        }
        else
        {
            handleText(p_originalTag);
        }
    }

    /**
     * Handles diplomat basic parser StartTag event.
     * @param p_strName - literal tag name
     * @param p_hAtributes - tag attributres as a hashtable
     * @param p_strOriginalString - the complete token from the parser
     */
    public void handleStartTag(String p_name, Properties p_atributes, String p_originalString)
    {
        if( p_name.equals("translatable") || p_name.equals("localizable"))
        {
            m_bInTranslatable = true;
            m_strBuffer = new StringBuffer();
            m_strStartTag = p_originalString;
        }
        else if(m_bInTranslatable)
        {
            handleText(p_originalString);
        }
    }

    /**
     * Handles diplomat basic parser Text event.
     * @param p_strText - next text chunk from between the tags
     */
    public void handleText(String p_text)
    {
        if(m_bInTranslatable)
        {
            m_strBuffer.append(p_text);
        }
    } 
}