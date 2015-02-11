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
package com.globalsight.ling.inprogresstm;

import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.tuv.Tuv;
import com.globalsight.ling.tm.LingManagerException;
import com.globalsight.ling.tm2.BaseTmTuv;
import com.globalsight.ling.tm2.leverage.LeverageOptions;
import com.globalsight.util.GlobalSightLocale;

import java.rmi.RemoteException;

/**
 * InProgressTmManager is responsible for population of in-progress TM
 * and dynamic leveraging.
 */
public interface InProgressTmManager
{
    /**
     * Performs a dynamic leveraging for a translatable text. It first
     * queries in the in-progress TM and then in the Gold TM. It
     * returns the leverage results in DynamicLeverageResults
     * object. All leverage options are taken from the system objects
     * retrieved based on the given source page id.
     *
     * The source text parameter can be either a top level text or a
     * subflow. If it is a top level text, it may or may not include
     * subflows. However, the method leverages only the top level text
     * and the leverage results don't include subflows even if the
     * source includes subflows. Subflows must be leveraged
     * separately.
     *
     * When leveraging a subflow, segment type parameter must be a
     * type of the subflow (e.g. url-a if the subflow is a href value
     * in <a> tag).
     *
     * @param p_sourceText Source text in GXML format. The top
     *        <segment> tag may or may not present.
     * @param p_targetLocale target locale
     * @param p_segmentType segment type (text, string, etc)
     * @param p_sourcePageId source page id
     * @return DynamicLeverageResults object
     */
    DynamicLeverageResults leverageTranslatable(String p_sourceText,
        GlobalSightLocale p_targetLocale, String p_segmentType,
        long p_sourcePageId)
        throws RemoteException, LingManagerException;


    /**
     * Performs a dynamic leveraging for a localizable text. It first
     * queries in the in-progress TM and then in the Gold TM. It
     * returns the leverage results in DynamicLeverageResults
     * object. All leverage options are taken from the system objects
     * retrieved based on the given source page id.
     *
     * The source text parameter can be either a top level text or a
     * subflow. If it is a top level text, it may or may not include
     * subflows. However, the method leverages only the top level text
     * and the leverage results don't include subflows even if the
     * source includes subflows. Subflows must be leveraged
     * separately.
     *
     * When leveraging a subflow, segment type parameter must be a
     * type of the subflow (e.g. url-a if the subflow is a href value
     * in <a> tag).
     *     
     * @param p_sourceText Source text in GXML format. The top
     *        <localizable> tag may or may not present.
     * @param p_targetLocale target locale
     * @param p_segmentType segment type (css-color, img-height, etc)
     * @param p_sourcePageId source page id
     * @return DynamicLeverageResults object
     */
    DynamicLeverageResults leverageLocalizable(String p_sourceText,
        GlobalSightLocale p_targetLocale, String p_segmentType,
        long p_sourcePageId)
        throws RemoteException, LingManagerException;


    /**
     * Saves a source and target translatable segment pair to the
     * in-progress TM.
     *
     * The source and target text parameter can be either a top level
     * text or a subflow. If it is a top level text, it may or may not
     * include subflows. However, the method saves only the top level
     * text and subflows are discarded. Subflows must be saved
     * separately.
     *
     * When saving a subflow, segment type parameter must be a
     * type of the subflow (e.g. url-a if the subflow is a href value
     * in <a> tag).
     *     
     * When saving a merged segment, both source and target text must
     * be merged text and the TU id must be of the first TU.
     *
     * @param p_sourceText Source text in GXML format. The top
     *        <segment> tag may or may not present.
     * @param p_targetText Target text in GXML format. The top
     *        <segment> tag may or may not present.
     * @param p_tuId TU id of the segment
     * @param p_targetLocale target locale
     * @param p_segmentType segment type
     * @param p_sourcePageId Source page id
     */
    void saveTranslatable(String p_sourceText, String p_targetText,
        long p_tuId, GlobalSightLocale p_targetLocale,
        String p_segmentType, long p_sourcePageId)
        throws RemoteException, LingManagerException;


    /**
     * Saves a source and target localizable segment pair to the
     * in-progress TM.
     *
     * The source and target text parameter can be either a top level
     * text or a subflow. If it is a top level text, it may or may not
     * include subflows. However, the method saves only the top level
     * text and subflows are discarded. Subflows must be saved
     * separately.
     *
     * When saving a subflow, segment type parameter must be a
     * type of the subflow (e.g. url-a if the subflow is a href value
     * in <a> tag).
     *     
     * When saving a merged segment, both source and target text must
     * be merged text and the TU id must be of the first TU.
     *
     * @param p_sourceText Source text in GXML format. The top
     *        <localizable> tag may or may not present.
     * @param p_targetText Target text in GXML format. The top
     *        <localizable> tag may or may not present.
     * @param p_tuId TU id of the segment
     * @param p_targetLocale target locale
     * @param p_segmentType segment type
     * @param p_sourcePageId Source page id
     */
    void saveLocalizable(String p_sourceText, String p_targetText,
        long p_tuId, GlobalSightLocale p_targetLocale,
        String p_segmentType, long p_sourcePageId)
        throws RemoteException, LingManagerException;



    /**
     * Performs a dynamic leveraging. It first queries in the
     * in-progress TM and then in the Gold TM. It returns the leverage
     * results in DynamicLeverageResults object. All leverage options
     * are taken from the system objects retrieved based on the given
     * source page id.
     *
     * The source TUV must be a complete segment which includes a top
     * level text as well as all subflows. This method leverages only
     * specified part of the segment, a top level text or a certain
     * subflow. The sub id indicates which part of the segment is
     * leveraged. To leverage a top level text, the sub id should be
     * com.globalsight.ling.tm2.SegmentTmTu.ROOT.
     *
     * When leveraging a merged segment, the source TUV must be a
     * merged source TUV and subflow ids must be adjusted so they are
     * unique in the segment.
     *
     * @param p_sourceTuv source TUV object
     * @param p_subId Subflow id
     * @param p_targetLocale target locale
     * @param p_sourcePageId source page id
     * @return DynamicLeverageResults object
     */
    DynamicLeverageResults leverage(Tuv p_sourceTuv, String p_subId,
        GlobalSightLocale p_targetLocale, long p_sourcePageId)
        throws RemoteException, LingManagerException;


    /**
     * Saves a source and target segment pair to the in-progress TM.
     *
     * The source and target TUVs must be a complete segment which
     * includes a top level text as well as all subflows. This method
     * saves only specified part of the segments, a top level text or
     * a certain subflow. The sub id indicates which part of the
     * segment is saved. To save a top level text, the sub id should
     * be com.globalsight.ling.tm2.SegmentTmTu.ROOT.
     *
     * When saving a merged segment, both source and target TUVs must
     * be merged TUVs and subflow ids must be adjusted so they are
     * unique in the segment.
     *
     * @param p_sourceTuv source TUV
     * @param p_targetTuv target TUV
     * @param p_subId subflow id
     * @param p_sourcePageId Source page id
     */
    void save(Tuv p_sourceTuv, Tuv p_targetTuv,
        String p_subId, long p_sourcePageId)
        throws RemoteException, LingManagerException;


    /**
     * Delete segments and their indexes that belong to a job from the
     * in-progress TM.
     *
     * @param p_jobId job id
     */
    void deleteSegments(long p_jobId)
        throws RemoteException, LingManagerException;
    
    LeverageOptions getLeverageOptions(SourcePage p_sourcePage,
            GlobalSightLocale p_targetLocale);
}
