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
package com.globalsight.machineTranslation;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.apache.log4j.Logger;

import com.globalsight.everest.edit.online.OnlineEditorManagerLocal;
import com.globalsight.everest.page.ExtractedSourceFile;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.projecthandler.MachineTranslationProfile;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.tuv.Tuv;
import com.globalsight.everest.tuv.TuvManager;
import com.globalsight.everest.webapp.pagehandler.administration.mtprofile.MTProfileHandlerHelper;
import com.globalsight.everest.webapp.pagehandler.edit.online.EditorState;
import com.globalsight.ling.common.XmlEntities;
import com.globalsight.util.gxml.GxmlElement;


public class MTHelper2
{
    private static final Logger logger = Logger.getLogger(MTHelper2.class);

    public static final String ENGINE_NAME = "ENGINE_NAME";
    public static final String SHOW_IN_EDITOR = "SHOW_IN_EDITOR";
    public static final String MT_TRANSLATION = "MT_TRANSLATION";

    public static final String ACTION_GET_MT_TRANSLATION = "getMtTranslation";
    public static final String ACTION_GET_MT_TRANSLATION_MESSAGE = "getMtTranslationMessage";

    public static final String MT_TRANSLATION_DIV = "translatedString_replaced_div";

    /**
     * Retrieve source page by source page ID.
     * 
     * @return
     */
    public static SourcePage getSourcePage(long sourcePageId)
    {
        SourcePage sp = null;
        try
        {
            sp = ServerProxy.getPageManager().getSourcePage(sourcePageId);
        }
        catch (Exception e)
        {
            if (logger.isDebugEnabled())
            {
                logger.error("Failed to get source page by pageID : "
                        + sourcePageId + ";" + e.getMessage());
            }
        }

        return sp;
    }

    /**
     * If the source page data type is XLF, before hit MT, need revert the
     * wrapped segment.
     * 
     * @return boolean
     */
    public static boolean isXlf(long sourcePageId)
    {
        String spDataType = getDataType(sourcePageId);
        if ("xlf".equalsIgnoreCase(spDataType)
                || "xliff".equalsIgnoreCase(spDataType))
        {
            return true;
        }

        return false;
    }

    /**
     * Get source page file format such as "XLF", "XML".
     * 
     * @param sourcePageId
     * @return String
     */
    public static String getDataType(long sourcePageId)
    {
        SourcePage sp = getSourcePage(sourcePageId);
        String spDataType = null;
        if (sp != null)
        {
            ExtractedSourceFile esf = (ExtractedSourceFile) sp
                    .getExtractedFile();
            spDataType = esf.getDataType();
        }

        return spDataType;
    }

	public static Map getMtTranslationMessage(EditorState p_state)
	{
		Map result = new HashMap();
		long sourcePageId = p_state.getSourcePageId();
		// MT: SHOW_IN_EDITOR
		MachineTranslationProfile mtProfile = MTProfileHandlerHelper
				.getMtProfileBySourcePageId(sourcePageId,
						p_state.getTargetLocale());
		MachineTranslator mt = null;
		if (mtProfile != null)
		{
			String mtEngine = mtProfile.getMtEngine();
			mt = MTHelper.initMachineTranslator(mtEngine);
			result.put(ENGINE_NAME, mt.getEngineName());
		}
		return result;
	}
    /**
     * If "show_in_editor" is checked on TM profile >> MT Options UI, get MT
     * translation for current segment.
     * 
     * @param p_sessionMgr
     * @param p_state
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static Map getMtTranslationForSegEditor(EditorState p_state)
	{
		Map result = new HashMap();

		long sourcePageId = p_state.getSourcePageId();
		// MT: SHOW_IN_EDITOR
		MachineTranslationProfile mtProfile = MTProfileHandlerHelper
				.getMtProfileBySourcePageId(sourcePageId,
						p_state.getTargetLocale());
		MachineTranslator mt = null;
		if (mtProfile != null)
		{
			String mtEngine = mtProfile.getMtEngine();
			mt = MTHelper.initMachineTranslator(mtEngine);
			HashMap hashMap = mtProfile.getParamHM();
			if (MachineTranslator.ENGINE_MSTRANSLATOR.equalsIgnoreCase(mt
					.getEngineName())
					&& p_state.getTargetLocale().getLanguage()
							.equalsIgnoreCase("sr"))
			{
				String srLang = mtProfile.getPreferedLangForSr(p_state
						.getTargetLocale().toString());
				hashMap.put(MachineTranslator.SR_LANGUAGE, srLang);
			}
			mt.setMtParameterMap(hashMap);
		}

		// MT: get MT result
		String mtString = null;
		try
		{
			SourcePage sp = null;
			try
			{
				sp = ServerProxy.getPageManager().getSourcePage(sourcePageId);
			}
			catch (Exception e)
			{
				logger.error("Could not get source page by source page ID : "
						+ sourcePageId, e);
			}
			if (mt != null)
			{
				TuvManager tuvMananger = ServerProxy.getTuvManager();
				Tuv sourceTuv = tuvMananger.getTuvForSegmentEditor(p_state
						.getTuId(), p_state.getSourceLocale().getIdAsLong(), sp
						.getJobId());

				String sourceString = null;
				long subId = p_state.getSubId();
				if (OnlineEditorManagerLocal.DUMMY_SUBID.equals(String
						.valueOf(subId)))
				{
					sourceString = sourceTuv.getGxmlElement().getTextValue();
				}
				else
				{
					GxmlElement subEle = sourceTuv
							.getSubflowAsGxmlElement(String.valueOf(subId));
					sourceString = subEle.getTextValue();
				}

				// translate segment
				if (sourceString != null && sourceString.trim().length() > 0)
				{
					mtString = mt
							.translate(p_state.getSourceLocale().getLocale(),
									p_state.getTargetLocale().getLocale(),
									sourceString);
				}

				if (mtString != null && !"".equals(mtString))
				{
					// Encode the translation before sent to web page.
					XmlEntities xe = new XmlEntities();
					mtString = xe.encodeStringBasic(mtString);
				}
				result.put(MT_TRANSLATION, mtString);
			}
		}
		catch (Exception e)
		{
		}

		return result;
	}

    // For GBS-3454
    /**
     * When hit MT, cache word count from MT engine which will be stored into
     * "target_page" table.
     * 
     * key: sourcePageId_targetLocaleId e.g. "1000_87"; value is MT engine
     * word count sum.
     */
    public static Hashtable<String, Integer> mtEngineWordCountCache = new Hashtable<String, Integer>();

    public static void putValue(String key, int increament)
    {
        Integer value = mtEngineWordCountCache.get(key);
        if (value == null)
        {
            mtEngineWordCountCache.put(key, new Integer(increament));
        }
        else
        {
            mtEngineWordCountCache.put(key, value + increament);
        }
    }

    public static Integer getValue(String key)
    {
        return mtEngineWordCountCache.get(key);
    }

    public static void removeValue(String key)
    {
        mtEngineWordCountCache.remove(key);
    }
}
