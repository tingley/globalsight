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
package com.globalsight.ling.tm2.leverage;

import com.globalsight.ling.tm2.BaseTmTu;
import com.globalsight.ling.tm2.BaseTmTuv;
import com.globalsight.util.GlobalSightLocale;

/**
 * LeveragedTu is an interface that defines interfaces of Translation Unit of
 * leveraged segments. The implementation of this class has match state and
 * match score fields. They serve as default values for each target locales
 * Tuvs. Different Tuvs in this Tu may have different values for those fields
 * when they have multiple translations.
 */

public interface LeveragedTu extends BaseTmTu
{
    static final int PAGE_TM = 1;
    static final int SEGMENT_TM = 2;
    static final int IN_PROGRESS_TM = 3;
    static final int PAGE_JOB_TABLE = 4;

    GlobalSightLocale getSourceLocale();

    void setSourceLocale(GlobalSightLocale p_sourceLocale);

    MatchState getMatchState();

    void setMatchState(MatchState p_state);

    float getScore();

    void setScore(float p_score);

    int getMatchTableType();

    void setMatchTableType(int p_matchTableType);

    BaseTmTuv getSourceTuv();

//    public boolean isFromWorldServer();
//
//    public void setFromWorldServer(boolean fromWorldServer);
}
