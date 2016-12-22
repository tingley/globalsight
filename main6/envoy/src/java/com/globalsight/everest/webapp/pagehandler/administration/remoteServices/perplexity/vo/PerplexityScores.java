/**
 * Copyright 2009 Welocalize, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 * 
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 */

package com.globalsight.everest.webapp.pagehandler.administration.remoteServices.perplexity.vo;

import java.util.ArrayList;
import java.util.List;

/**
 * <code>PerplexityScores</code> is used in <code>PerplexityScoreHelper</code>
 * <p>
 * For GBS-4495 perplexity score on MT.
 */
public class PerplexityScores
{
    private List<Double> sources = new ArrayList<Double>();
    private List<Double> targets = new ArrayList<Double>();

    public List<Double> getSources()
    {
        return sources;
    }

    public void setSources(List<Double> sources)
    {
        this.sources = sources;
    }

    public void addSource(Double source)
    {
        this.sources.add(source);
    }

    public List<Double> getTargets()
    {
        return targets;
    }

    public void setTargets(List<Double> targets)
    {
        this.targets = targets;
    }

    public void addTarget(Double target)
    {
        this.targets.add(target);
    }
}
