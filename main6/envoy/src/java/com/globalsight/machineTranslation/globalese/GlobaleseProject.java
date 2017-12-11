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
package com.globalsight.machineTranslation.globalese;

public class GlobaleseProject
{

    private long id; // project id
    private String name;
    private String source;
    private String target;
    private int group;
    private long engine;
    private String status;

    GlobaleseProject()
    {
        // no-args constructor
    }

    public long getId()
    {
        return this.id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getSource()
    {
        return source;
    }

    public void setSource(String source)
    {
        this.source = source;
    }

    public String getTarget()
    {
        return target;
    }

    public void setTarget(String target)
    {
        this.target = target;
    }

    public int getGroup()
    {
        return group;
    }

    public void setGroup(int group)
    {
        this.group = group;
    }

    public long getEngine()
    {
        return engine;
    }

    public void setEngine(long engine)
    {
        this.engine = engine;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

}
