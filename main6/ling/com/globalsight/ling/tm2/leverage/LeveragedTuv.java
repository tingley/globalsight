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

import com.globalsight.ling.tm2.BaseTmTuv;

/**
 * LeveragedTuv is an interface that defines interfaces of Translation
 * Unit Variant of leveraged segments. The implementation of this
 * class has match state, match score and match order fields.
 */

public interface LeveragedTuv
    extends BaseTmTuv, SidComparable, DateComparable 
{
    BaseTmTuv getSourceTuv();

    MatchState getMatchState();
    
    void setMatchState(MatchState p_state);
    
    float getScore();
    
    void setScore(float p_score);

    int getOrder();
    
    void setOrder(int p_order);
    
    String getOrgSid();
    void setOrgSid(String sid);
}
