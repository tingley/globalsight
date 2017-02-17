package com.globalsight.everest.webapp.pagehandler.administration.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.globalsight.cxe.entity.customAttribute.Attribute;
import com.globalsight.cxe.entity.customAttribute.TMPAttribute;
import com.globalsight.cxe.entity.segmentationrulefile.SegmentationRuleFile;
import com.globalsight.everest.projecthandler.LeverageProjectTM;
import com.globalsight.everest.projecthandler.ProjectTM;
import com.globalsight.everest.projecthandler.TranslationMemoryProfile;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.webapp.pagehandler.administration.config.attribute.AttributeManager;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.StringUtil;

public class TMProfileImporter implements ConfigConstants
{
    private static final Logger logger = Logger.getLogger(TMProfileImporter.class);
    private String currentCompanyId;
    private String sessionId;
    private String importToCompId;

    public TMProfileImporter(String sessionId, String currentCompanyId, String importToCompId)
    {
        this.sessionId = sessionId;
        this.currentCompanyId = currentCompanyId;
        this.importToCompId = importToCompId;
    }

    public void analysisAndImport(File uploadedFile)
    {
        Map<String, Map<String, String>> map = new HashMap<String, Map<String, String>>();

        try
        {
            String[] keyArr = null;
            String key = null;
            String strKey = null;
            String strValue = null;
            InputStream is;
            is = new FileInputStream(uploadedFile);
            BufferedReader bf = new BufferedReader(new InputStreamReader(is));
            Properties prop = new Properties();
            prop.load(bf);
            Enumeration enum1 = prop.propertyNames();
            while (enum1.hasMoreElements())
            {
                // The key profile
                strKey = (String) enum1.nextElement();
                key = strKey.substring(0, strKey.lastIndexOf('.'));
                keyArr = strKey.split("\\.");
                // Value in the properties file
                strValue = prop.getProperty(strKey);
                Set<String> keySet = map.keySet();
                if (keySet.contains(key))
                {
                    Map<String, String> valueMap = map.get(key);
                    Set<String> valueKey = valueMap.keySet();
                    if (!valueKey.contains(keyArr[2]))
                    {
                        valueMap.put(keyArr[2], strValue);
                    }
                }
                else
                {
                    Map<String, String> valueMap = new HashMap<String, String>();
                    valueMap.put(keyArr[2], strValue);
                    map.put(key, valueMap);
                }
            }
            // Data analysis
            analysisData(map);
        }
        catch (Exception e)
        {
            logger.error("Failed to parse the file", e);
        }
    }

    private void analysisData(Map<String, Map<String, String>> map)
    {
        if (map.isEmpty())
            return;

        Map<String, List> dataMap = new HashMap<String, List>();
        List<TranslationMemoryProfile> tmProfileList = new ArrayList<TranslationMemoryProfile>();
        Set<String> keySet = map.keySet();
        Iterator it = keySet.iterator();
        while (it.hasNext())
        {
            String key = (String) it.next();
            String[] keyArr = key.split("\\.");
            if ("LeverageProjectTM".equalsIgnoreCase(keyArr[0]))
            {

            }
            Map<String, String> valueMap = map.get(key);
            if (!valueMap.isEmpty())
            {
                if ("TranslationMemoryProfile".equalsIgnoreCase(keyArr[0]))
                {
                    TranslationMemoryProfile tmProfile = putDataIntoTMP(valueMap);
                    tmProfileList.add(tmProfile);
                }
            }
        }

        if (tmProfileList.size() > 0)
            dataMap.put("TranslationMemoryProfile", tmProfileList);

        // Storing data
        storeDataToDatabase(dataMap);
    }

    private void storeDataToDatabase(Map<String, List> dataMap)
    {
        if (dataMap.isEmpty())
            return;

        try
        {
            if (dataMap.containsKey("TranslationMemoryProfile"))
            {
                storeTMPData(dataMap);
            }

            addMessage("<b> Done importing Translation Memory Profiles.</b>");
        }
        catch (Exception e)
        {
            logger.error("Failed to import Translation Memory Profiles.", e);
            addToError(e.getMessage());
        }
    }

    private void storeTMPData(Map<String, List> dataMap)
    {
        List<TranslationMemoryProfile> tmProfileList = dataMap.get("TranslationMemoryProfile");
        try
        {
            for (int i = 0; i < tmProfileList.size(); i++)
            {
                TranslationMemoryProfile tmp = tmProfileList.get(i);
                List<ProjectTM> tmList = ServerProxy.getProjectHandler().getAllProjectTMs(
                        tmp.getCompanyId());

                // checks project tm for save
                long projectTmId = tmp.getProjectTmIdForSave();
                ProjectTM projectTM = HibernateUtil.get(ProjectTM.class, projectTmId);
                long projectTmIdForSave = -1;
                for (ProjectTM tm : tmList)
                {
                    if (tm.getName().equals(projectTM.getName())
                            || tm.getName().startsWith(projectTM.getName() + "_import"))
                    {
                        projectTmIdForSave = tm.getId();
                        break;
                    }
                }
                tmp.setProjectTmIdForSave(projectTmIdForSave);

                // checks segmentation rule
                long ruleId = -1;
                SegmentationRuleFile segRule = ServerProxy
                        .getSegmentationRuleFilePersistenceManager()
                        .getSegmentationRuleFileByTmpid(tmp.getIdAsLong().toString());
                Collection<SegmentationRuleFile> ruleList = ServerProxy
                        .getSegmentationRuleFilePersistenceManager().getAllSegmentationRuleFiles(
                                tmp.getCompanyId());
                for (SegmentationRuleFile rule : ruleList)
                {
                    if (rule.getName().equals(segRule.getName())
                            || rule.getName().startsWith(segRule.getName() + "_import"))
                    {
                        ruleId = rule.getIdAsLong();
                        break;
                    }
                }
                // checks reference tms
                Set<LeverageProjectTM> projectTMsToLeverageFromSet = new HashSet<LeverageProjectTM>();
                Vector<LeverageProjectTM> leverageProjectTMList = tmp.getProjectTMsToLeverageFrom();
                for (LeverageProjectTM leverageProjectTM : leverageProjectTMList)
                {
                    ProjectTM proTM = HibernateUtil.get(ProjectTM.class,
                            leverageProjectTM.getProjectTmId());
                    for (ProjectTM tm : tmList)
                    {
                        if (tm.getName().equals(proTM.getName())
                                || tm.getName().startsWith(proTM.getName() + "_import"))
                        {
                            LeverageProjectTM levTM = new LeverageProjectTM();
                            levTM.setTMProfile(tmp);
                            levTM.setProjectTmId(tm.getId());
                            levTM.setProjectTmIndex(leverageProjectTM.getProjectTmIndex());
                            projectTMsToLeverageFromSet.add(levTM);
                            break;
                        }
                    }
                }
                tmp.setProjectTMsToLeverageFromSet(projectTMsToLeverageFromSet);

                if (projectTmIdForSave == -1 || projectTMsToLeverageFromSet.size() == 0
                        || ruleId == -1)
                {
                    String msg = "Upload Translation Memory Profile data failed !";
                    logger.warn(msg);
                    addToError(msg);
                }
                else
                {
                    String oldName = tmp.getName();
                    String newName = getMTPNewName(oldName, tmp.getCompanyId());
                    tmp = createNewTmp(newName, tmp);
                    HibernateUtil.save(tmp);
                    ServerProxy.getSegmentationRuleFilePersistenceManager()
                            .createRelationshipWithTmp(String.valueOf(ruleId),
                                    tmp.getIdAsLong().toString());
                    if (oldName.equals(newName))
                    {
                        addMessage("<b>" + newName + "</b> imported successfully.");
                    }
                    else
                    {
                        addMessage(" Translation Memory Profile name <b>" + oldName
                                + "</b> already exists. <b>" + newName
                                + "</b> imported successfully.");
                    }
                }
            }
        }
        catch (Exception e)
        {
            String msg = "Upload Translation Memory Profile data failed !";
            logger.warn(msg);
            addToError(msg);
        }
    }

    private TranslationMemoryProfile createNewTmp(String newName, TranslationMemoryProfile tmProfile)
    {
        TranslationMemoryProfile tmp = new TranslationMemoryProfile();
        try
        {
            tmp.setName(newName);
            tmp.setDescription(tmProfile.getDescription());
            tmp.setProjectTmIdForSave(tmProfile.getProjectTmIdForSave());
            tmp.setSaveUnLocSegToProjectTM(tmProfile.isSaveUnLocSegToProjectTM());
            tmp.setSaveLocSegToProjectTM(tmProfile.isSaveLocSegToProjectTM());
            tmp.setSaveMTedSegToProjectTM(tmProfile.isSaveMTedSegToProjectTM());
            tmp.setSaveWhollyInternalTextToProjectTM(tmProfile
                    .isSaveWhollyInternalTextToProjectTM());
            tmp.setSaveApprovedSegToProjectTM(tmProfile.isSaveApprovedSegToProjectTM());
            tmp.setSaveExactMatchSegToProjectTM(tmProfile.isSaveExactMatchSegToProjectTM());
            tmp.setSaveUnLocSegToPageTM(tmProfile.isSaveUnLocSegToPageTM());
            tmp.setJobExcludeTuTypesStr(tmProfile.getJobExcludeTuTypesAsString());
            tmp.setLeverageLocalizable(tmProfile.isLeverageLocalizable());
            tmp.setIsExactMatchLeveraging(tmProfile.getIsExactMatchLeveraging());
            tmp.setIsContextMatchLeveraging(tmProfile.getIsContextMatchLeveraging());
            tmp.setIcePromotionRules(tmProfile.getIcePromotionRules());
            tmp.setIsTypeSensitiveLeveraging(tmProfile.getIsTypeSensitiveLeveraging());
            tmp.setTypeDifferencePenalty(tmProfile.getTypeDifferencePenalty());
            tmp.setIsCaseSensitiveLeveraging(tmProfile.getIsCaseSensitiveLeveraging());
            tmp.setCaseDifferencePenalty(tmProfile.getCaseDifferencePenalty());
            tmp.setIsWhiteSpaceSensitiveLeveraging(tmProfile.getIsWhiteSpaceSensitiveLeveraging());
            tmp.setWhiteSpaceDifferencePenalty(tmProfile.getWhiteSpaceDifferencePenalty());
            tmp.setIsCodeSensitiveLeveraging(tmProfile.getIsCodeSensitiveLeveraging());
            tmp.setCodeDifferencePenalty(tmProfile.getCodeDifferencePenalty());
            tmp.setIsMultiLingualLeveraging(tmProfile.getIsMultiLingualLeveraging());
            tmp.setAutoRepair(tmProfile.isAutoRepair());
            tmp.setMultipleExactMatches(tmProfile.getMultipleExactMatches());
            tmp.setMultipleExactMatchesPenalty(tmProfile.getMultipleExactMatchesPenalty());
            tmp.setFuzzyMatchThreshold(tmProfile.getFuzzyMatchThreshold());
            tmp.setNumberOfMatchesReturned(tmProfile.getNumberOfMatchesReturned());
            tmp.setIsLatestMatchForReimport(tmProfile.getIsLatestMatchForReimport());
            tmp.setIsTypeSensitiveLeveragingForReimp(tmProfile
                    .getIsTypeSensitiveLeveragingForReimp());
            tmp.setTypeDifferencePenaltyForReimp(tmProfile.getTypeDifferencePenaltyForReimp());
            tmp.setIsMultipleMatchesForReimp(tmProfile.getIsMultipleMatchesForReimp());
            tmp.setMultipleMatchesPenalty(tmProfile.getMultipleMatchesPenalty());
            tmp.setDynLevFromGoldTm(tmProfile.getDynLevFromGoldTm());
            tmp.setDynLevFromInProgressTm(tmProfile.getDynLevFromInProgressTm());
            tmp.setDynLevFromPopulationTm(tmProfile.getDynLevFromPopulationTm());
            tmp.setDynLevFromReferenceTm(tmProfile.getDynLevFromReferenceTm());
            tmp.setDynLevStopSearch(tmProfile.getDynLevStopSearch());
            tmp.setMatchPercentage(tmProfile.isMatchPercentage());
            tmp.setTmProcendence(tmProfile.isTmProcendence());
            tmp.setSelectRefTm(tmProfile.getSelectRefTm());
            tmp.setRefTmPenalty(tmProfile.getRefTmPenalty());
            tmp.setIsOldTuvMatch(tmProfile.isOldTuvMatch());
            tmp.setOldTuvMatchPenalty(tmProfile.getOldTuvMatchPenalty());
            tmp.setOldTuvMatchDay(tmProfile.getOldTuvMatchDay());
            tmp.setUniqueFromMultipleTranslation(tmProfile.isUniqueFromMultipleTranslation());
            tmp.setChoiceIfAttNotMatch(tmProfile.getChoiceIfAttNotMatch());
            tmp.setTuAttNotMatchPenalty(tmProfile.getTuAttNotMatchPenalty());
            tmp.setCompanyId(tmProfile.getCompanyId());

            // saves leverage tms
            Set<LeverageProjectTM> levProjectTMSet = tmProfile.getProjectTMsToLeverageFromSet();
            Set<LeverageProjectTM> projectTMsToLeverageFromSet = new HashSet<LeverageProjectTM>();
            for (LeverageProjectTM leverageProjectTM : levProjectTMSet)
            {
                leverageProjectTM.setTMProfile(tmp);
                projectTMsToLeverageFromSet.add(leverageProjectTM);
            }
            tmp.setProjectTMsToLeverageFromSet(projectTMsToLeverageFromSet);

            // saves tmAttribute
            Set<TMPAttribute> attributes = new HashSet<TMPAttribute>();
            Set<TMPAttribute> tmpAttributeList = tmProfile.getAttributes();

            for (TMPAttribute tmpAttribute : tmpAttributeList)
            {
                List<Attribute> attributeList = (List<Attribute>) AttributeManager
                        .getAllAttributes(tmProfile.getCompanyId());
                for (Attribute attribute : attributeList)
                {
                    String attributeName = attribute.getName();
                    if (attributeName.equalsIgnoreCase(tmpAttribute.getAttributeName())
                            || attributeName.startsWith(tmpAttribute.getAttributeName()
                                    + "_import_"))
                    {
                        TMPAttribute tmpa = new TMPAttribute();
                        tmpa.setAttributeName(attributeName);
                        tmpa.setOperator(tmpAttribute.getOperator());
                        tmpa.setValueType(tmpAttribute.getValueType());
                        tmpa.setValueData(tmpAttribute.getValueData());
                        tmpa.setAndOr(tmpAttribute.getAndOr());
                        tmpa.setOrder(tmpAttribute.getOrder());
                        tmpa.setTmprofile(tmp);
                        attributes.add(tmpa);
                    }
                }
            }
            tmp.setAttributes(attributes);

            // saves reference TM
            String refTms = tmProfile.getRefTMsToLeverageFrom();
            String msRefToLeverageFrom = "";
            if (refTms.length() > 0)
            {
                String[] refTMs = refTms.split(",");
                for (String refTM : refTMs)
                {
                    ProjectTM projectTM = HibernateUtil.get(ProjectTM.class, Long.parseLong(refTM));
                    List<ProjectTM> tmList = ServerProxy.getProjectHandler().getAllProjectTMs(
                            tmProfile.getCompanyId());
                    for (ProjectTM tm : tmList)
                    {
                        if (tm.getName().equalsIgnoreCase(projectTM.getName())
                                || tm.getName().startsWith(projectTM.getName() + "_import"))
                        {
                            msRefToLeverageFrom += tm.getId() + ",";
                            break;
                        }
                    }
                }
                if (msRefToLeverageFrom.length() > 0)
                    msRefToLeverageFrom.substring(0, msRefToLeverageFrom.length() - 1);
                tmp.setRefTMsToLeverageFrom(msRefToLeverageFrom);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return tmp;
    }

    private String getMTPNewName(String oldName, long companyId)
    {
        String hql = "select tmp.name from TranslationMemoryProfile "
                + "  tmp where tmp.companyId=:companyid";
        Map map = new HashMap();
        map.put("companyid", companyId);
        List itList = HibernateUtil.search(hql, map);

        if (itList.contains(oldName))
        {
            for (int num = 1;; num++)
            {
                String returnStr = null;
                if (oldName.contains("_import_"))
                {
                    returnStr = oldName.substring(0, oldName.lastIndexOf('_')) + "_" + num;
                }
                else
                {
                    returnStr = oldName + "_import_" + num;
                }
                if (!itList.contains(returnStr))
                {
                    return returnStr;
                }
            }
        }
        else
        {
            return oldName;
        }
    }

    private TranslationMemoryProfile putDataIntoTMP(Map<String, String> valueMap)
    {
        TranslationMemoryProfile tmProfile = new TranslationMemoryProfile();
        try
        {
            String keyField = null;
            String valueField = null;
            String tmpId = null;
            String ruleId = null;
            Set<String> valueKey = valueMap.keySet();
            Iterator itor = valueKey.iterator();
            while (itor.hasNext())
            {
                keyField = (String) itor.next();
                valueField = valueMap.get(keyField);
                if ("ID".equalsIgnoreCase(keyField))
                {
                    tmProfile.setId(Long.parseLong(valueField));
                }
                else if ("NAME".equalsIgnoreCase(keyField))
                {
                    tmProfile.setName(valueField);
                }
                else if ("DESCRIPTION".equalsIgnoreCase(keyField))
                {
                    tmProfile.setDescription(valueField);
                }
                else if ("PROJECT_TM_ID_FOR_SAVE".equalsIgnoreCase(keyField))
                {
                    tmProfile.setProjectTmIdForSave(Long.parseLong(valueField));
                }
                else if ("IS_UNLOC_SEG_SAVED_TO_PROJ_TM".equalsIgnoreCase(keyField))
                {
                    tmProfile.setSaveUnLocSegToProjectTM(Boolean.parseBoolean(valueField));
                }
                else if ("IS_LOC_SEG_SAVED_TO_PROJ_TM".equalsIgnoreCase(keyField))
                {
                    tmProfile.setSaveLocSegToProjectTM(Boolean.parseBoolean(valueField));
                }
                else if ("IS_MTED_SEG_SAVED_TO_PROJ_TM".equalsIgnoreCase(keyField))
                {
                    tmProfile.setSaveMTedSegToProjectTM(Boolean.parseBoolean(valueField));
                }
                else if ("IS_WHOLLY_INTERNAL_TEXT_TO_PROJ_TM".equalsIgnoreCase(keyField))
                {
                    tmProfile
                            .setSaveWhollyInternalTextToProjectTM(Boolean.parseBoolean(valueField));
                }
                else if ("IS_APPROVED_SEG_SAVED_TO_PROJ_TM".equalsIgnoreCase(keyField))
                {
                    tmProfile.setSaveApprovedSegToProjectTM(Boolean.parseBoolean(valueField));
                }
                else if ("IS_EXACT_MATCH_SEG_SAVED_TO_PROJ_TM".equalsIgnoreCase(keyField))
                {
                    tmProfile.setSaveExactMatchSegToProjectTM(Boolean.parseBoolean(valueField));
                }
                else if ("IS_APPROVED_SEG_SAVED_TO_PROJ_TM".equalsIgnoreCase(keyField))
                {
                    tmProfile.setSaveApprovedSegToProjectTM(Boolean.parseBoolean(valueField));
                }
                else if ("IS_UNLOC_SEG_SAVED_TO_PAGE_TM".equalsIgnoreCase(keyField))
                {
                    tmProfile.setSaveUnLocSegToPageTM(Boolean.parseBoolean(valueField));
                }
                else if ("JOB_EXCLUDE_TU_TYPES".equalsIgnoreCase(keyField))
                {
                    tmProfile.setJobExcludeTuTypesStr(valueField);
                }
                else if ("IS_LEVERAGE_LOCALIZABLE".equalsIgnoreCase(keyField))
                {
                    tmProfile.setLeverageLocalizable(Boolean.parseBoolean(valueField));
                }
                else if ("IS_EXACT_MATCH_LEVERAGING".equalsIgnoreCase(keyField))
                {
                    tmProfile.setIsExactMatchLeveraging(Boolean.parseBoolean(valueField));
                }
                else if ("IS_CONTEXT_MATCH".equalsIgnoreCase(keyField))
                {
                    tmProfile.setIsContextMatchLeveraging(Boolean.parseBoolean(valueField));
                }
                else if ("ICE_PROMOTION_RULES".equalsIgnoreCase(keyField))
                {
                    tmProfile.setIcePromotionRules(Integer.parseInt(valueField));
                }
                else if ("IS_TYPE_SENSITIVE_LEVERAGING".equalsIgnoreCase(keyField))
                {
                    tmProfile.setIsTypeSensitiveLeveraging(Boolean.parseBoolean(valueField));
                }
                else if ("TYPE_DIFFERENCE_PENALTY".equalsIgnoreCase(keyField))
                {
                    tmProfile.setTypeDifferencePenalty(Long.parseLong(valueField));
                }
                else if ("IS_CASE_SENSITIVE_LEVERAGING".equalsIgnoreCase(keyField))
                {
                    tmProfile.setIsCaseSensitiveLeveraging(Boolean.parseBoolean(valueField));
                }
                else if ("CASE_DIFFERENCE_PENALTY".equalsIgnoreCase(keyField))
                {
                    tmProfile.setCaseDifferencePenalty(Long.parseLong(valueField));
                }
                else if ("IS_WS_SENSITIVE_LEVERAGING".equalsIgnoreCase(keyField))
                {
                    tmProfile.setIsWhiteSpaceSensitiveLeveraging(Boolean.parseBoolean(valueField));
                }
                else if ("WHITESPACE_DIFFERENCE_PENALTY".equalsIgnoreCase(keyField))
                {
                    tmProfile.setWhiteSpaceDifferencePenalty(Long.parseLong(valueField));
                }
                else if ("IS_CODE_SENSITIVE_LEVERAGING".equalsIgnoreCase(keyField))
                {
                    tmProfile.setIsCodeSensitiveLeveraging(Boolean.parseBoolean(valueField));
                }
                else if ("CODE_DIFFERENCE_PENALTY".equalsIgnoreCase(keyField))
                {
                    tmProfile.setCodeDifferencePenalty(Long.parseLong(valueField));
                }
                else if ("IS_MULTILINGUAL_LEVERAGING".equalsIgnoreCase(keyField))
                {
                    tmProfile.setIsMultiLingualLeveraging(Boolean.parseBoolean(valueField));
                }
                else if ("AUTO_REPAIR".equalsIgnoreCase(keyField))
                {
                    tmProfile.setAutoRepair(Boolean.parseBoolean(valueField));
                }
                else if ("MULTIPLE_EXACT_MATCHES".equalsIgnoreCase(keyField))
                {
                    tmProfile.setMultipleExactMatches(valueField);
                }
                else if ("MULTIPLE_EXACT_MATCHES_PENALTY".equalsIgnoreCase(keyField))
                {
                    tmProfile.setMultipleExactMatchesPenalty(Long.parseLong(valueField));
                }
                else if ("FUZZY_MATCHES_THRESHOLD".equalsIgnoreCase(keyField))
                {
                    tmProfile.setFuzzyMatchThreshold(Long.parseLong(valueField));
                }
                else if ("NUMBER_OF_MATCHES_RETURNED".equalsIgnoreCase(keyField))
                {
                    tmProfile.setNumberOfMatchesReturned(Long.parseLong(valueField));
                }
                else if ("IS_LATEST_MATCH_FOR_REIMPORT".equalsIgnoreCase(keyField))
                {
                    tmProfile.setIsLatestMatchForReimport(Boolean.parseBoolean(valueField));
                }
                else if ("IS_TYPE_LEV_FOR_REIMPORT".equalsIgnoreCase(keyField))
                {
                    tmProfile
                            .setIsTypeSensitiveLeveragingForReimp(Boolean.parseBoolean(valueField));
                }
                else if ("TYPE_DIFF_PENALTY_REIMPORT".equalsIgnoreCase(keyField))
                {
                    tmProfile.setTypeDifferencePenaltyForReimp(Long.parseLong(valueField));
                }
                else if ("IS_MULT_MATCHES_FOR_REIMP".equalsIgnoreCase(keyField))
                {
                    tmProfile.setIsMultipleMatchesForReimp(Boolean.parseBoolean(valueField));
                }
                else if ("MULTIPLE_MATCHES_PENALTY".equalsIgnoreCase(keyField))
                {
                    tmProfile.setMultipleMatchesPenalty(Long.parseLong(valueField));
                }
                else if ("DYN_LEV_FROM_GOLD_TM".equalsIgnoreCase(keyField))
                {
                    tmProfile.setDynLevFromGoldTm(Boolean.parseBoolean(valueField));
                }
                else if ("DYN_LEV_FROM_IN_PROGRESS_TM".equalsIgnoreCase(keyField))
                {
                    tmProfile.setDynLevFromInProgressTm(Boolean.parseBoolean(valueField));
                }
                else if ("DYN_LEV_FROM_POPULATION_TM".equalsIgnoreCase(keyField))
                {
                    tmProfile.setDynLevFromPopulationTm(Boolean.parseBoolean(valueField));
                }
                else if ("DYN_LEV_FROM_REFERENCE_TM".equalsIgnoreCase(keyField))
                {
                    tmProfile.setDynLevFromReferenceTm(Boolean.parseBoolean(valueField));
                }
                else if ("DYN_LEV_STOP_SEARCH".equalsIgnoreCase(keyField))
                {
                    tmProfile.setDynLevStopSearch(Boolean.parseBoolean(valueField));
                }
                else if ("IS_MATCH_PERCENTAGE".equalsIgnoreCase(keyField))
                {
                    tmProfile.setMatchPercentage(Boolean.parseBoolean(valueField));
                }
                else if ("IS_TM_PROCENDENCE".equalsIgnoreCase(keyField))
                {
                    tmProfile.setTmProcendence(Boolean.parseBoolean(valueField));
                }
                else if ("IS_REF_TM".equalsIgnoreCase(keyField))
                {
                    tmProfile.setSelectRefTm(Boolean.parseBoolean(valueField));
                }
                else if ("REF_TM_PENALTY".equalsIgnoreCase(keyField))
                {
                    tmProfile.setRefTmPenalty(Long.parseLong(valueField));
                }
                else if ("REF_TMS".equalsIgnoreCase(keyField))
                {
                    tmProfile.setRefTMsToLeverageFrom(valueField);
                }
                else if ("IS_OLD_TUV_MATCH".equalsIgnoreCase(keyField))
                {
                    tmProfile.setIsOldTuvMatch(Boolean.parseBoolean(valueField));
                }
                else if ("OLD_TUV_MATCH_PENALTY".equalsIgnoreCase(keyField))
                {
                    tmProfile.setOldTuvMatchPenalty(Long.parseLong(valueField));
                }
                else if ("OLD_TUV_MATCH_DAY".equalsIgnoreCase(keyField))
                {
                    tmProfile.setOldTuvMatchDay(Long.parseLong(valueField));
                }
                else if ("GET_UNIQUE_FROM_MULT_TRANS".equalsIgnoreCase(keyField))
                {
                    tmProfile.setUniqueFromMultipleTranslation(Boolean.parseBoolean(valueField));
                }
                else if ("CHOICE_IF_ATT_NOT_MATCH".equalsIgnoreCase(keyField))
                {
                    tmProfile.setChoiceIfAttNotMatch(valueField);
                }
                else if ("TU_ATT_NOT_MATCH_PENALTY".equalsIgnoreCase(keyField))
                {
                    tmProfile.setTuAttNotMatchPenalty(Integer.parseInt(valueField));
                }
                else if ("COMPANY_ID".equalsIgnoreCase(keyField))
                {
                    if (importToCompId != null && !importToCompId.equals("-1"))
                    {
                        tmProfile.setCompanyId(Long.parseLong(importToCompId));
                    }
                    else
                    {
                        tmProfile.setCompanyId(Long.parseLong(currentCompanyId));
                    }
                }
                else if ("LEVERAGE_PROJECT_TM_IDS".equalsIgnoreCase(keyField))
                {
                    String[] levProjectTMIds = valueField.split(",");
                    Vector<LeverageProjectTM> projectTMsToLeverageFrom = new Vector<LeverageProjectTM>();
                    if (levProjectTMIds.length > 0)
                    {
                        for (String levProjectTMId : levProjectTMIds)
                        {
                            LeverageProjectTM levProjectTM = HibernateUtil.get(
                                    LeverageProjectTM.class, Long.parseLong(levProjectTMId));
                            projectTMsToLeverageFrom.add(levProjectTM);
                        }
                    }
                    tmProfile.setAllLeverageProjectTMs(projectTMsToLeverageFrom);
                }
                else if ("TMP_IDS".equalsIgnoreCase(keyField))
                {
                    Set<TMPAttribute> attributes = new HashSet<TMPAttribute>();
                    if (StringUtil.isNotEmptyAndNull(valueField))
                    {
                        String[] attributeIds = valueField.split(",");
                        for (String attributeId : attributeIds)
                        {
                            TMPAttribute tmpAttr = HibernateUtil.get(TMPAttribute.class,
                                    Long.parseLong(attributeId));
                            attributes.add(tmpAttr);
                        }
                    }
                    tmProfile.setAttributes(attributes);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return tmProfile;
    }

    private void addToError(String msg)
    {
        String former = config_error_map.get(sessionId) == null ? "" : config_error_map
                .get(sessionId);
        config_error_map.put(sessionId, former + "<p>" + msg);
    }

    private void addMessage(String msg)
    {
        String former = config_error_map.get(sessionId) == null ? "" : config_error_map
                .get(sessionId);
        config_error_map.put(sessionId, former + "<p>" + msg);
    }
}
