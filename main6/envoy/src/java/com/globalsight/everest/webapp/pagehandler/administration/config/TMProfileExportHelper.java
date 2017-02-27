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
package com.globalsight.everest.webapp.pagehandler.administration.config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import java.util.Vector;

import com.globalsight.cxe.entity.customAttribute.TMPAttribute;
import com.globalsight.cxe.entity.segmentationrulefile.SegmentationRuleFile;
import com.globalsight.everest.projecthandler.LeverageProjectTM;
import com.globalsight.everest.projecthandler.ProjectTM;
import com.globalsight.everest.projecthandler.TranslationMemoryProfile;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.AmbFileStoragePathUtils;

/**
 * Exports translation memory files.
 */
public class TMProfileExportHelper implements ConfigConstants
{
    private final static String NEW_LINE = "\r\n";

    public static File createPropertyfile(String userName, long companyId)
    {
        StringBuffer filePath = new StringBuffer();
        filePath.append(AmbFileStoragePathUtils.getFileStorageDirPath(companyId))
                .append(File.separator).append("GlobalSight").append(File.separator)
                .append("config").append(File.separator).append("export").append(File.separator)
                .append("Translation Memory Profiles");

        File file = new File(filePath.toString());
        file.mkdirs();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String fileName = TM_PROFILE_FILE_NAME + userName + "_" + sdf.format(new Date())
                + ".properties";
        File propertiesFile = new File(file, fileName);

        return propertiesFile;
    }

    /**
     * Gets translation memory files info.
     */
    public static File propertiesInputTMP(File tmpPropertyFile, String p_tmProfileId)
    {
        try
        {
            TranslationMemoryProfile tmProfile = ServerProxy.getProjectHandler().getTMProfileById(
                    Long.parseLong(p_tmProfileId), true);
            StringBuffer buffer = new StringBuffer();
            buffer.append("##TranslationMemoryProfile.").append(tmProfile.getCompanyId())
                    .append(".").append(tmProfile.getId()).append(".begin").append(NEW_LINE);
            buffer.append("TranslationMemoryProfile.").append(tmProfile.getId()).append(".ID=")
                    .append(tmProfile.getId()).append(NEW_LINE);
            buffer.append("TranslationMemoryProfile.").append(tmProfile.getId()).append(".NAME=")
                    .append(tmProfile.getName()).append(NEW_LINE);
            buffer.append("TranslationMemoryProfile.").append(tmProfile.getId())
                    .append(".DESCRIPTION=").append(tmProfile.getDescription()).append(NEW_LINE);
            String tmName = HibernateUtil.get(ProjectTM.class, tmProfile.getProjectTmIdForSave())
                    .getName();
            buffer.append("TranslationMemoryProfile.").append(tmProfile.getId())
                    .append(".PROJECT_TM_NAME_FOR_SAVE=").append(tmName).append(NEW_LINE);
            buffer.append("TranslationMemoryProfile.").append(tmProfile.getId())
                    .append(".IS_UNLOC_SEG_SAVED_TO_PROJ_TM=")
                    .append(tmProfile.isSaveUnLocSegToProjectTM()).append(NEW_LINE);
            buffer.append("TranslationMemoryProfile.").append(tmProfile.getId())
                    .append(".IS_LOC_SEG_SAVED_TO_PROJ_TM=")
                    .append(tmProfile.isSaveLocSegToProjectTM()).append(NEW_LINE);
            buffer.append("TranslationMemoryProfile.").append(tmProfile.getId())
                    .append(".IS_MTED_SEG_SAVED_TO_PROJ_TM=")
                    .append(tmProfile.isSaveMTedSegToProjectTM()).append(NEW_LINE);
            buffer.append("TranslationMemoryProfile.").append(tmProfile.getId())
                    .append(".IS_WHOLLY_INTERNAL_TEXT_TO_PROJ_TM=")
                    .append(tmProfile.isSaveWhollyInternalTextToProjectTM()).append(NEW_LINE);
            buffer.append("TranslationMemoryProfile.").append(tmProfile.getId())
                    .append(".IS_APPROVED_SEG_SAVED_TO_PROJ_TM=")
                    .append(tmProfile.isSaveApprovedSegToProjectTM()).append(NEW_LINE);
            buffer.append("TranslationMemoryProfile.").append(tmProfile.getId())
                    .append(".IS_EXACT_MATCH_SEG_SAVED_TO_PROJ_TM=")
                    .append(tmProfile.isSaveExactMatchSegToProjectTM()).append(NEW_LINE);
            buffer.append("TranslationMemoryProfile.").append(tmProfile.getId())
                    .append(".IS_UNLOC_SEG_SAVED_TO_PAGE_TM=")
                    .append(tmProfile.isSaveUnLocSegToPageTM()).append(NEW_LINE);
            buffer.append("TranslationMemoryProfile.").append(tmProfile.getId())
                    .append(".JOB_EXCLUDE_TU_TYPES=")
                    .append(tmProfile.getJobExcludeTuTypesAsString()).append(NEW_LINE);
            buffer.append("TranslationMemoryProfile.").append(tmProfile.getId())
                    .append(".IS_LEVERAGE_LOCALIZABLE=").append(tmProfile.isLeverageLocalizable())
                    .append(NEW_LINE);
            buffer.append("TranslationMemoryProfile.").append(tmProfile.getId())
                    .append(".IS_EXACT_MATCH_LEVERAGING=")
                    .append(tmProfile.getIsExactMatchLeveraging()).append(NEW_LINE);
            buffer.append("TranslationMemoryProfile.").append(tmProfile.getId())
                    .append(".IS_CONTEXT_MATCH=").append(tmProfile.getIsContextMatchLeveraging())
                    .append(NEW_LINE);
            buffer.append("TranslationMemoryProfile.").append(tmProfile.getId())
                    .append(".ICE_PROMOTION_RULES=").append(tmProfile.getIcePromotionRules())
                    .append(NEW_LINE);
            buffer.append("TranslationMemoryProfile.").append(tmProfile.getId())
                    .append(".IS_TYPE_SENSITIVE_LEVERAGING=")
                    .append(tmProfile.getIsTypeSensitiveLeveraging()).append(NEW_LINE);
            buffer.append("TranslationMemoryProfile.").append(tmProfile.getId())
                    .append(".TYPE_DIFFERENCE_PENALTY=")
                    .append(tmProfile.getTypeDifferencePenalty()).append(NEW_LINE);
            buffer.append("TranslationMemoryProfile.").append(tmProfile.getId())
                    .append(".IS_CASE_SENSITIVE_LEVERAGING=")
                    .append(tmProfile.getIsCaseSensitiveLeveraging()).append(NEW_LINE);
            buffer.append("TranslationMemoryProfile.").append(tmProfile.getId())
                    .append(".CASE_DIFFERENCE_PENALTY=")
                    .append(tmProfile.getCaseDifferencePenalty()).append(NEW_LINE);
            buffer.append("TranslationMemoryProfile.").append(tmProfile.getId())
                    .append(".IS_WS_SENSITIVE_LEVERAGING=")
                    .append(tmProfile.getIsWhiteSpaceSensitiveLeveraging()).append(NEW_LINE);
            buffer.append("TranslationMemoryProfile.").append(tmProfile.getId())
                    .append(".WHITESPACE_DIFFERENCE_PENALTY=")
                    .append(tmProfile.getWhiteSpaceDifferencePenalty()).append(NEW_LINE);
            buffer.append("TranslationMemoryProfile.").append(tmProfile.getId())
                    .append(".IS_CODE_SENSITIVE_LEVERAGING=")
                    .append(tmProfile.getIsCodeSensitiveLeveraging()).append(NEW_LINE);
            buffer.append("TranslationMemoryProfile.").append(tmProfile.getId())
                    .append(".CODE_DIFFERENCE_PENALTY=")
                    .append(tmProfile.getCodeDifferencePenalty()).append(NEW_LINE);
            buffer.append("TranslationMemoryProfile.").append(tmProfile.getId())
                    .append(".IS_MULTILINGUAL_LEVERAGING=")
                    .append(tmProfile.getIsMultiLingualLeveraging()).append(NEW_LINE);
            buffer.append("TranslationMemoryProfile.").append(tmProfile.getId())
                    .append(".AUTO_REPAIR=").append(tmProfile.isAutoRepair()).append(NEW_LINE);
            buffer.append("TranslationMemoryProfile.").append(tmProfile.getId())
                    .append(".MULTIPLE_EXACT_MATCHES=").append(tmProfile.getMultipleExactMatches())
                    .append(NEW_LINE);
            buffer.append("TranslationMemoryProfile.").append(tmProfile.getId())
                    .append(".MULTIPLE_EXACT_MATCHES_PENALTY=")
                    .append(tmProfile.getMultipleExactMatchesPenalty()).append(NEW_LINE);
            buffer.append("TranslationMemoryProfile.").append(tmProfile.getId())
                    .append(".FUZZY_MATCHES_THRESHOLD=").append(tmProfile.getFuzzyMatchThreshold())
                    .append(NEW_LINE);
            buffer.append("TranslationMemoryProfile.").append(tmProfile.getId())
                    .append(".NUMBER_OF_MATCHES_RETURNED=")
                    .append(tmProfile.getNumberOfMatchesReturned()).append(NEW_LINE);
            buffer.append("TranslationMemoryProfile.").append(tmProfile.getId())
                    .append(".IS_LATEST_MATCH_FOR_REIMPORT=")
                    .append(tmProfile.isLatestMatchForReimport()).append(NEW_LINE);
            buffer.append("TranslationMemoryProfile.").append(tmProfile.getId())
                    .append(".IS_TYPE_LEV_FOR_REIMPORT=")
                    .append(tmProfile.isTypeSensitiveLeveragingForReimp()).append(NEW_LINE);
            buffer.append("TranslationMemoryProfile.").append(tmProfile.getId())
                    .append(".TYPE_DIFF_PENALTY_REIMPORT=")
                    .append(tmProfile.getTypeDifferencePenaltyForReimp()).append(NEW_LINE);
            buffer.append("TranslationMemoryProfile.").append(tmProfile.getId())
                    .append(".IS_MULT_MATCHES_FOR_REIMP=")
                    .append(tmProfile.getIsMultipleMatchesForReimp()).append(NEW_LINE);
            buffer.append("TranslationMemoryProfile.").append(tmProfile.getId())
                    .append(".MULTIPLE_MATCHES_PENALTY=")
                    .append(tmProfile.getMultipleMatchesPenalty()).append(NEW_LINE);
            buffer.append("TranslationMemoryProfile.").append(tmProfile.getId())
                    .append(".DYN_LEV_FROM_GOLD_TM=").append(tmProfile.getDynLevFromGoldTm())
                    .append(NEW_LINE);
            buffer.append("TranslationMemoryProfile.").append(tmProfile.getId())
                    .append(".DYN_LEV_FROM_IN_PROGRESS_TM=")
                    .append(tmProfile.getDynLevFromInProgressTm()).append(NEW_LINE);
            buffer.append("TranslationMemoryProfile.").append(tmProfile.getId())
                    .append(".DYN_LEV_FROM_POPULATION_TM=")
                    .append(tmProfile.getDynLevFromPopulationTm()).append(NEW_LINE);
            buffer.append("TranslationMemoryProfile.").append(tmProfile.getId())
                    .append(".DYN_LEV_FROM_REFERENCE_TM=")
                    .append(tmProfile.getDynLevFromReferenceTm()).append(NEW_LINE);
            buffer.append("TranslationMemoryProfile.").append(tmProfile.getId())
                    .append(".DYN_LEV_STOP_SEARCH=").append(tmProfile.getDynLevStopSearch())
                    .append(NEW_LINE);
            buffer.append("TranslationMemoryProfile.").append(tmProfile.getId())
                    .append(".IS_MATCH_PERCENTAGE=").append(tmProfile.isMatchPercentage())
                    .append(NEW_LINE);
            buffer.append("TranslationMemoryProfile.").append(tmProfile.getId())
                    .append(".IS_TM_PROCENDENCE=").append(tmProfile.isTmProcendence())
                    .append(NEW_LINE);
            buffer.append("TranslationMemoryProfile.").append(tmProfile.getId())
                    .append(".IS_REF_TM=").append(tmProfile.getSelectRefTm()).append(NEW_LINE);
            buffer.append("TranslationMemoryProfile.").append(tmProfile.getId())
                    .append(".REF_TM_PENALTY=").append(tmProfile.getRefTmPenalty())
                    .append(NEW_LINE);
            buffer.append("TranslationMemoryProfile.").append(tmProfile.getId())
                    .append(".REF_TMS=").append(tmProfile.getRefTMsToLeverageFrom())
                    .append(NEW_LINE);
            buffer.append("TranslationMemoryProfile.").append(tmProfile.getId())
                    .append(".IS_OLD_TUV_MATCH=").append(tmProfile.isOldTuvMatch())
                    .append(NEW_LINE);
            buffer.append("TranslationMemoryProfile.").append(tmProfile.getId())
                    .append(".OLD_TUV_MATCH_PENALTY=").append(tmProfile.getOldTuvMatchPenalty())
                    .append(NEW_LINE);
            buffer.append("TranslationMemoryProfile.").append(tmProfile.getId())
                    .append(".OLD_TUV_MATCH_DAY=").append(tmProfile.getOldTuvMatchDay())
                    .append(NEW_LINE);
            buffer.append("TranslationMemoryProfile.").append(tmProfile.getId())
                    .append(".GET_UNIQUE_FROM_MULT_TRANS=")
                    .append(tmProfile.isUniqueFromMultipleTranslation()).append(NEW_LINE);
            buffer.append("TranslationMemoryProfile.").append(tmProfile.getId())
                    .append(".CHOICE_IF_ATT_NOT_MATCH=").append(tmProfile.getChoiceIfAttNotMatch())
                    .append(NEW_LINE);
            buffer.append("TranslationMemoryProfile.").append(tmProfile.getId())
                    .append(".TU_ATT_NOT_MATCH_PENALTY=")
                    .append(tmProfile.getTuAttNotMatchPenalty()).append(NEW_LINE);
            buffer.append("TranslationMemoryProfile.").append(tmProfile.getId())
                    .append(".COMPANY_ID=").append(tmProfile.getCompanyId()).append(NEW_LINE);
            // exports segmentation rule
            SegmentationRuleFile segmentationRule = ServerProxy.getSegmentationRuleFilePersistenceManager()
                    .getSegmentationRuleFileByTmpid(p_tmProfileId);
            buffer.append("TranslationMemoryProfile.").append(tmProfile.getId())
                    .append(".SEGMENTATION_RULE_NAME=").append(segmentationRule.getName()).append(NEW_LINE);
            // exports leverage project tm
            Vector<LeverageProjectTM> levProTMList = tmProfile.getProjectTMsToLeverageFrom();
            StringBuffer sb = new StringBuffer();
            if (levProTMList != null && levProTMList.size() > 0)
            {
                for (LeverageProjectTM levTM : levProTMList)
                {
                    ProjectTM projectTM = HibernateUtil
                            .get(ProjectTM.class, levTM.getProjectTmId());
                    sb.append(projectTM.getName()).append(",");
                }
                if (sb.length() > 1)
                    sb.deleteCharAt(sb.length() - 1);
            }
            buffer.append("TranslationMemoryProfile.").append(tmProfile.getId())
                    .append(".LEVERAGE_PROJECT_TM_NAMES=").append(sb).append(NEW_LINE);
            buffer.append("##TranslationMemoryProfile.").append(tmProfile.getCompanyId())
                    .append(".").append(tmProfile.getId()).append(".end").append(NEW_LINE)
                    .append(NEW_LINE);
            writeToFile(tmpPropertyFile, buffer.toString().getBytes());
            
            String companyId = String.valueOf(tmProfile.getCompanyId());
            // exports leverage project tm
            Vector<LeverageProjectTM> levTMList = tmProfile.getProjectTMsToLeverageFrom();
            if (levTMList != null && levTMList.size() > 0)
            {
                for (LeverageProjectTM levTM : levTMList)
                {
                    propertiesInputLevProjectTM(tmpPropertyFile, levTM, companyId);
                }
            }
            // exports TMPAttribute
            Set<TMPAttribute> tmpAttrList = tmProfile.getAttributes();
            if (tmpAttrList != null && tmpAttrList.size() > 0)
            {
                for (TMPAttribute tmpAttribute : tmpAttrList)
                {
                    propertiesInputTMPAttribute(tmpPropertyFile, tmpAttribute, companyId);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return tmpPropertyFile;
    }

    private static void propertiesInputTMPAttribute(File tmpPropertyFile,
            TMPAttribute tmpAttribute, String companyId)
    {
        if (tmpAttribute == null)
            return;

        StringBuffer buffer = new StringBuffer();
        buffer.append("##TMPAttribute.").append(companyId).append(".").append(tmpAttribute.getId())
                .append(".begin").append(NEW_LINE);
        buffer.append("TMPAttribute.").append(tmpAttribute.getId()).append(".ID=")
                .append(tmpAttribute.getId()).append(NEW_LINE);
        buffer.append("TMPAttribute.").append(tmpAttribute.getId()).append(".TMP_ID=")
                .append(tmpAttribute.getTmprofile().getId()).append(NEW_LINE);
        buffer.append("TMPAttribute.").append(tmpAttribute.getId()).append(".ATT_NAME=")
                .append(tmpAttribute.getAttributeName()).append(NEW_LINE);
        buffer.append("TMPAttribute.").append(tmpAttribute.getId()).append(".OPERATOR=")
                .append(tmpAttribute.getOperator()).append(NEW_LINE);
        buffer.append("TMPAttribute.").append(tmpAttribute.getId()).append(".VALUE_TYPE=")
                .append(tmpAttribute.getValueType()).append(NEW_LINE);
        buffer.append("TMPAttribute.").append(tmpAttribute.getId()).append(".VALUE_DATA=")
                .append(tmpAttribute.getValueData()).append(NEW_LINE);
        buffer.append("TMPAttribute.").append(tmpAttribute.getId()).append(".AND_OR=")
                .append(tmpAttribute.getAndOr()).append(NEW_LINE);
        buffer.append("TMPAttribute.").append(tmpAttribute.getId()).append(".PRIORITY_ORDER=")
                .append(tmpAttribute.getOrder()).append(NEW_LINE);
        buffer.append("##TMPAttribute.").append(companyId).append(".").append(tmpAttribute.getId())
                .append(".end").append(NEW_LINE).append(NEW_LINE);
        writeToFile(tmpPropertyFile, buffer.toString().getBytes());
    }

    private static void propertiesInputLevProjectTM(File tmpPropertyFile, LeverageProjectTM levTM,
            String companyId)
    {
        if (levTM == null)
            return;

        StringBuffer buffer = new StringBuffer();
        buffer.append("##LeverageProjectTM.").append(companyId).append(".").append(levTM.getId())
                .append(".begin").append(NEW_LINE);
        buffer.append("LeverageProjectTM.").append(levTM.getId()).append(".ID=")
                .append(levTM.getId()).append(NEW_LINE);
        buffer.append("LeverageProjectTM.").append(levTM.getId()).append(".TM_PROFILE_ID=")
                .append(levTM.getTMProfile().getId()).append(NEW_LINE);
        ProjectTM projectTM = HibernateUtil.get(ProjectTM.class, levTM.getProjectTmId());
        buffer.append("LeverageProjectTM.").append(levTM.getId()).append(".PROJECT_TM_NAME=")
                .append(projectTM.getName()).append(NEW_LINE);
        buffer.append("LeverageProjectTM.").append(levTM.getId()).append(".PROJECT_TM_INDEX=")
                .append(levTM.getProjectTmIndex()).append(NEW_LINE);
        buffer.append("##LeverageProjectTM.").append(companyId).append(".").append(levTM.getId())
                .append(".end").append(NEW_LINE).append(NEW_LINE);
        writeToFile(tmpPropertyFile, buffer.toString().getBytes());
    }

    private static void writeToFile(File tmpPropertyFile, byte[] bytes)
    {
        tmpPropertyFile.getParentFile().mkdirs();

        FileOutputStream fos = null;
        try
        {
            fos = new FileOutputStream(tmpPropertyFile, true);
            fos.write(bytes);
        }
        catch (Exception e)
        {
        }
        finally
        {
            try
            {
                fos.close();
            }
            catch (IOException e)
            {
            }
        }
    }

}
