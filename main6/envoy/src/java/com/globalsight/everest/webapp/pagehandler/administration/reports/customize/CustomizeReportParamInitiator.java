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
package com.globalsight.everest.webapp.pagehandler.administration.reports.customize;

import java.util.ArrayList;
import java.util.List;

import com.globalsight.everest.webapp.pagehandler.administration.reports.customize.param.Param;

public class CustomizeReportParamInitiator {
    private ParamImp params = null;
    
    public CustomizeReportParamInitiator()
    {
        this.params = this.initParams(); 
        
        // Makes JobId always selected.
        this.setParamByName(Param.JOB_ID);
    }
    
    public void setParamByName(String wholeParamName)
    {
        wholeParamName = wholeParamName.replace('.', ',');
        String[] paramNames = wholeParamName.split(",");
        
        ParamImp tempParams = this.params;
        tempParams.setValue(true);
        
        // strip off the first item "jobinfo."
        for (int i = 1; i < paramNames.length; i++)
        {
            tempParams = tempParams.getChildParamImp(paramNames[i]);
            tempParams.setValue(true);
        }
        
    }
    
    public Param getRootParam()
    {
        return this.params;
    }
    
    private ParamImp initParams()
    {
        ParamImp params = this.new ParamImp(Param.ROOT);
        
        for (int i = 0; i < Param.WHOLE_PARAMS.length; i++)
        {
            String[] paramSplitedString = Param.WHOLE_PARAMS[i].replace('.', ',').split(",");
            
            ParamImp tempParams = params;
            
            // strip off the first item "jobinfo."
            for (int j = 1; j < paramSplitedString.length; j++)
            {
                ParamImp childParams = 
                    tempParams.getChildParamImp(paramSplitedString[j]);
                
                if (childParams == null)
                {
                    childParams = 
                        tempParams.addChildParamsByName(paramSplitedString[j]);
                }
                
                tempParams = childParams;
            }
            tempParams.setCompletedName(Param.WHOLE_PARAMS[i]);
        }
        
        return params;
    }
    
    
    // Inner Class
    private class ParamImp implements Param
    {
        private String name = null;
        private String completedName = null;
        private boolean value = false;
        private ArrayList childParams = null;
        
        public ParamImp(String name)
        {
            this.name = name;
            this.value = false;
        }
        
        public void setValue(boolean value)
        {
            this.value = value;
        }
        
        public String getName()
        {
            return this.name;
        }
        
        public void setCompletedName(String completedName)
        {
            this.completedName = completedName;
        }
        
        public String getCompletedName()
        {
            return this.completedName;
        }
        
        public boolean getValue()
        {
            return this.value;
        }
        
        public boolean hasChildren()
        {
            return (this.childParams == null) ? false : true;
        }
        
        public int childrenSize()
        {
            return this.hasChildren() ? this.childParams.size() : 0;
        }
        
        public boolean hasSelectedChildren() 
        {
            return this.hasChildren() && this.getValue();
        }
        
        public int selectedChildrenSize()
        {
            int result = 0;
            
            if (this.getValue() && this.hasChildren())
            {
                Param [] params = this.getChildParams();
                for (int i = 0; i < params.length; i++)
                {
                    if (params[i].getValue())
                    {
                        result++;
                    }
                }
            }
            
            return result;
        }
        
        public ParamImp addChildParamsByName(String name)
        {
            if (this.childParams == null)
            {
                this.childParams = new ArrayList();
            }
            
            ParamImp childParams = new ParamImp(name);
            this.childParams.add(childParams);
            return childParams;
        }
        
        public Param[] getChildParams()
        {
            return (Param[]) this.childParams.toArray(new Param[this.childrenSize()]);
        }
        
        public Param[] getSelectedChildren()
        {
            List result = new ArrayList();
            
            for (int i = 0; i < this.childrenSize(); i++)
            {
                Param childParam = (Param)this.childParams.get(i);
                if (childParam.getValue())
                {
                    result.add(childParam);
                }
            }
            
            return (Param[]) result.toArray(new Param[result.size()]);
        }
        
        public Param getChildParam(String childParamName)
        {
            return (Param)(this.getChildParamImp(childParamName));
        }
        
        public ParamImp getChildParamImp(String childParamName)
        {
            ParamImp result = null;
            
            if (this.childParams != null)
            {
                for (int i = 0; i < this.childParams.size(); i++)
                {
                    ParamImp params = (ParamImp)this.childParams.get(i);
                    if (params.name.equalsIgnoreCase(childParamName))
                    {
                        result = params;
                        break;
                    }
                }
            }
               
            return result;
        }
        
        public boolean equals(Param theOtherParam)
        {
            return this.completedName.equalsIgnoreCase(theOtherParam.getCompletedName());
        }
    }
}

