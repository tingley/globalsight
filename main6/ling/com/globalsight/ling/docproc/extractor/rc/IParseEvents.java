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
package com.globalsight.ling.docproc.extractor.rc;

public interface IParseEvents
{
    public void handleStart();
    public void handleFinish();
    public void addLocalizable(String s);
    public void addSkeleton(String s);
    public void addStringTableValue(String s);
    public void addCaption(String s);
    public void addAuto3stateText(String s);
    public void addAutoCheckBox(String s);
    public void addPushBox(String s);
    public void addState3Text(String s);
    public void addCheckBoxText(String s);
    public void addControlText(String s);
    public void addctext(String s);
    public void addDefPushButton(String s);
    public void addGroupBoxText(String s);
    public void addLtext(String s);
    public void addPushButtonText(String s);
    public void addRadioButtonText(String s);
    public void addRtext(String s);
    public void addMenuItemText(String s);
    public void addPopupText(String s);
}
