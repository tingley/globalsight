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

import com.globalsight.ling.common.DiplomatBasicParser;
import com.globalsight.ling.common.DiplomatBasicParserException;
import com.globalsight.ling.tw.internal.EmbedOnlineInternalTag;
import com.globalsight.ling.tw.internal.InternalTag;
import com.globalsight.ling.tw.internal.XliffInternalTag;

/**
 * Main class for executing Tmx and Pseudo string conversions.
 */
public class TmxPseudo
{
    private static String m_lastPseudo2TmxNativeResult = null;
    private static final int NOMAL = 0;
    public static final int ONLINE_EDIT = 1;
    public static final int OFFLINE_TM = 2;

    /**
     * Construct Tmx string from pseudo data.
     * 
     * @return java.lang.String
     * @param pseudoData
     *            - data mappings for the conversion.
     */
    public static String pseudo2Tmx(PseudoData pseudoData)
            throws PseudoParserException
    {
        PseudoCodec codec = new PseudoCodec();
        XmlEntities xmlEncoder = new XmlEntities();
        String segment = xmlEncoder.encodeString(pseudoData
                .getWrappedPTagTargetString());

        Pseudo2TmxHandler pseudo2TmxHandler = new Pseudo2TmxHandler(pseudoData);
        PseudoParser pseudoParser = new PseudoParser(pseudo2TmxHandler);
        pseudoParser.tokenize(segment);

        m_lastPseudo2TmxNativeResult = codec.decode(pseudo2TmxHandler
                .getNativeResult());

        return codec.decode(pseudo2TmxHandler.getResult());
    }

    /**
     * Construct pseudo data from a tmx string.
     * 
     * @return PseudoData - data resulting from the conversion.
     * @param p_strTmxString
     *            java.lang.String
     * @param p_hTagMapping
     *            HashTable
     */
    public static PseudoData tmx2Pseudo(String p_strTmxString,
            PseudoData p_PseudoData) throws DiplomatBasicParserException
    {
        return tmx2Pseudo(p_strTmxString, p_PseudoData, NOMAL);
    }

    private static InternalTag getInternalTagByState(int state)
    {
        if (state == ONLINE_EDIT)
            return new EmbedOnlineInternalTag();

        if (state == OFFLINE_TM)
            return new XliffInternalTag();

        return null;
    }

    /**
     * Construct pseudo data from a tmx string.
     * 
     * @return PseudoData - data resulting from the conversion.
     * @param p_strTmxString
     *            java.lang.String
     * @param p_hTagMapping
     *            HashTable
     */
    public static PseudoData tmx2Pseudo(String p_strTmxString,
            PseudoData p_PseudoData, int state)
            throws DiplomatBasicParserException
    {
        InternalTag internalTag = getInternalTagByState(state);
        p_PseudoData.reset();

        PseudoCodec codec = new PseudoCodec();
        Tmx2PseudoHandler eventHandler = new Tmx2PseudoHandler(p_PseudoData);
        // GBS-3722
        if (p_PseudoData.isFromSourceTargetPanel())
        {
            eventHandler.setMTIdentifiers(p_strTmxString);
        }
        String segment = eventHandler.preProcessInternalText(
                codec.encode(p_strTmxString), internalTag);
        DiplomatBasicParser parser = new DiplomatBasicParser(eventHandler);

        parser.parse(segment);

        return eventHandler.getResult();
    }

    /**
     * Get the native strig as a result of last pseudo2Tmx conversion.
     * 
     * @return Stirng - native string resulting from the conversion.
     */
    public static String getLastPseudo2TmxNativeResult()
    {
        return m_lastPseudo2TmxNativeResult;
    }
}
