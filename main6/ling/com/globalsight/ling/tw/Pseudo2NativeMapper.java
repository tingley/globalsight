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
package com.globalsight.ling.tw;


import com.globalsight.ling.common.DiplomatBasicParserException;
import com.globalsight.ling.common.DiplomatBasicParser;
import com.globalsight.ling.common.DiplomatBasicHandler;
import com.globalsight.ling.docproc.TmxTagGenerator;


import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Locale;
import java.util.MissingResourceException;


public class Pseudo2NativeMapper
    implements DiplomatBasicHandler
{

    private StringBuffer m_strNative = new StringBuffer();
    private Hashtable m_Native2AlternateMap = mapNative2Alternate();

    /**
     * Constructor.
     */
    public Pseudo2NativeMapper()
    {
    }

    /**
     * Builds and sets the Pseudo to Native map.
     * @param p_PData - PsuedoData object
     * @exception com.globalsight.ling.common.DiplomatBasicParserException
     */
    public void buildMap(PseudoData p_PData)
        throws DiplomatBasicParserException
    {
        XmlEntities XMLcodec = new XmlEntities();
        Hashtable Pseudo2NativeMap = new Hashtable();
        Hashtable Pseudo2TmxMap = p_PData.getPseudo2TmxMap();

        // get shared resources from p_PData.
        remapNative2Alternate(p_PData.getResources());

        // add all unique (numbered) PTags (numbered and therefore not
        // addable)
        for (Enumeration e = Pseudo2TmxMap.keys() ; e.hasMoreElements() ;)
        {
            String key = (String)e.nextElement();
            String val = (String) Pseudo2TmxMap.get(key);

            DiplomatBasicParser parser = new DiplomatBasicParser(this);

            parser.parse(val);
            String strNative = XMLcodec.decodeString(getResult());

            key = PseudoConstants.PSEUDO_OPEN_TAG + key +
                PseudoConstants.PSEUDO_CLOSE_TAG;

            Pseudo2NativeMap.put(key, strNative);
        }


        /*
          // Add mappings for HTML addables
          // NOTE: This was a quickie to get addables in the map.
          //       Right now we only deal with one format and its hardcoded below.
          //       When there are more addable formats, a format designator should
          //       be added to the OverrideMap and this should become a method call
          //       on PseudoData object.

          NOTE: we decided to turn them off in the mapping table so the user does not see them.
          This hash is only used for display.

          if( p_PData.getAddableMode() == PseudoConstants.ADDABLES_AS_HTML )        {
          PseudoOverrideMapItem POMI;

          // bold
          POMI = p_PData.getOverrideMapItem(TmxTagGenerator.getInlineTypeName(TmxTagGenerator.BOLD));
          String tag = p_PData.isModeCompact() ? POMI.m_strCompact : POMI.m_strVerbose;
          String BptContent = (String)POMI.m_hAttributes.get( PseudoConstants.ADDABLE_HTML_CONTENT );
          String EptContent = (String)POMI.m_hAttributes.get( PseudoConstants.ADDABLE_ENDPAIR_HTML_CONTENT );
          Pseudo2NativeMap.put( ""+ PseudoConstants.PSEUDO_OPEN_TAG
          + tag
          + PseudoConstants.PSEUDO_CLOSE_TAG,
          XMLcodec.decodeString(BptContent));
          Pseudo2NativeMap.put( ""+PseudoConstants.PSEUDO_OPEN_TAG
          + PseudoConstants.PSEUDO_END_TAG_MARKER
          + tag
          + PseudoConstants.PSEUDO_CLOSE_TAG,
          XMLcodec.decodeString(EptContent));

          // italic
          POMI = p_PData.getOverrideMapItem(TmxTagGenerator.getInlineTypeName(TmxTagGenerator.ITALIC));
          tag = p_PData.isModeCompact() ? POMI.m_strCompact : POMI.m_strVerbose;
          BptContent = (String)POMI.m_hAttributes.get( PseudoConstants.ADDABLE_HTML_CONTENT );
          EptContent = (String)POMI.m_hAttributes.get( PseudoConstants.ADDABLE_ENDPAIR_HTML_CONTENT );
          Pseudo2NativeMap.put( ""+PseudoConstants.PSEUDO_OPEN_TAG
          + tag
          + PseudoConstants.PSEUDO_CLOSE_TAG,
          XMLcodec.decodeString(BptContent));
          Pseudo2NativeMap.put( ""+PseudoConstants.PSEUDO_OPEN_TAG
          + PseudoConstants.PSEUDO_END_TAG_MARKER
          + tag
          + PseudoConstants.PSEUDO_CLOSE_TAG,
          XMLcodec.decodeString(EptContent));

          // underline
          POMI = p_PData.getOverrideMapItem(TmxTagGenerator.getInlineTypeName(TmxTagGenerator.UNDERLINE));
          tag = p_PData.isModeCompact() ? POMI.m_strCompact : POMI.m_strVerbose;
          BptContent = (String)POMI.m_hAttributes.get( PseudoConstants.ADDABLE_HTML_CONTENT );
          EptContent = (String)POMI.m_hAttributes.get( PseudoConstants.ADDABLE_ENDPAIR_HTML_CONTENT );
          Pseudo2NativeMap.put( ""+PseudoConstants.PSEUDO_OPEN_TAG
          + tag
          + PseudoConstants.PSEUDO_CLOSE_TAG,
          XMLcodec.decodeString(BptContent));
          Pseudo2NativeMap.put( ""+PseudoConstants.PSEUDO_OPEN_TAG
          + PseudoConstants.PSEUDO_END_TAG_MARKER
          + tag
          + PseudoConstants.PSEUDO_CLOSE_TAG,
          XMLcodec.decodeString(EptContent));
          }
        */


        p_PData.setPseudo2NativeMap(Pseudo2NativeMap);
    }

    /**
     * Returns the results of a Tmx to a Native string conversion.
     * @return java.lang.String - A native string.
     */
    public String getResult()
    {
        // look up possible alternates..
        String temp = m_strNative.toString();
        String alternate = (String) m_Native2AlternateMap.get(temp);

        if (alternate != null)
        {
            return alternate;
        }

        return temp;
    }

    /**
     * Handles diplomat basic parser Start event.
     */
    public void handleStart()
    {
        m_strNative.setLength(0);
        return;
    }

    /**
     * Handles diplomat basic parser Stop event.
     */
    public void handleStop()
    {
        // do nothing
    }

    /**
     * Process a end Tag (called by the framework).
     * @param p_strName - Tag name
     * @param p_strOriginalTag - full tag with delimiters.
     */
    public void handleEndTag(String p_strName, String p_strOriginalTag)
    {
        // do nothing
    }

    /**
     * Start tag event handler.
     * @param p_strName - The literal tag name.
     * @param p_hAtributes - Attribute list
     * @param p_strOriginalString - The complete token from the parser.
     */
    public void handleStartTag(String p_strName, Properties p_hAtributes,
        String p_strOriginalString)
    {
        // do nothing
    }

    /**
     * Process a Text (called by the framework).
     * @param p_strText - the text
     */
    public void handleText(String p_strText)
    {
        m_strNative.append(p_strText);
    }

    /**
     * Internal default mappings.  Maps native codes to alternate
     * display strings.  These alternate strings will appear in the
     * editors Pseudo to Native list box instead of the native code.
     * @return Hashtable.
     */
    private Hashtable mapNative2Alternate()
    {
        // These are only compared to content between tags.  E.g. It
        // does not apply to a plain tab that is not tagged
        Hashtable Nat2Alt = new Hashtable();

        Nat2Alt.put(" ", "a single space");
        Nat2Alt.put("\t", "a single tab");
        Nat2Alt.put("\\t", "a single tab");
        Nat2Alt.put("\f", "a single form-feed");
        Nat2Alt.put("\\f", "a single form-feed");
        Nat2Alt.put("\n", "a single line-break");
        Nat2Alt.put("\\n", "a single line-break");
        Nat2Alt.put("\r", "a single line-break");
        Nat2Alt.put("\\r", "a single line-break");

        return Nat2Alt;
    }

    /**
     * Maps native codes to alternate display strings based on
     * PseudoData locale.  These alternate strings will appear in the
     * editors Pseudo to Native list box instead of the native code.
     * @return Hashtable.
     */
    private void remapNative2Alternate(ResourceBundle p_resources)
        throws MissingResourceException
    {

        // These are only compared to content between tags.
        // e.g. It does not apply to a plain tab that is not tagged
        Hashtable Nat2Alt = new Hashtable();

        Nat2Alt.put(" ", p_resources.getString("NativeAlternateForSpace"));
        Nat2Alt.put("\t", p_resources.getString("NativeAlternateForTab"));
        Nat2Alt.put("\\t", p_resources.getString("NativeAlternateForTab"));

        String tmp = p_resources.getString("NativeAlternateForFF");
        Nat2Alt.put("\f", tmp);
        Nat2Alt.put("\\f", tmp);

        tmp = p_resources.getString("NativeAlternateForLB");
        Nat2Alt.put("\n", tmp);
        Nat2Alt.put("\\n", tmp);
        Nat2Alt.put("\r", tmp);
        Nat2Alt.put("\\r", tmp);

        m_Native2AlternateMap = Nat2Alt;
    }
}
