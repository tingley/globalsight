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

package com.globalsight.everest.page.pageimport;

import java.util.HashMap;

import com.globalsight.everest.tuv.Tu;
import com.globalsight.everest.tuv.TuImpl;
import com.globalsight.everest.tuv.Tuv;

public interface IXliffMatchesProcessor
{
    public boolean addTargetTuvToTu(TuImpl tu, 
                                    Tuv sourceTuv, 
                                    Tuv targetTuv,
                                    String src,
                                    String trg,
                                    HashMap<Tu, Tuv> p_appliedTuTuvMap, 
                                    float tmScore, 
                                    long tmProfileThreshold,
                                    boolean isPO);
}
