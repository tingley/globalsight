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
package com.globalsight.everest.webapp.applet.admin.graphicalworkflow.gui.api;


/**
 * GraphicalView is an interface of objects providing support for
   editing a plan
 */
public interface GraphicalView extends SelectionSource
{
    int NODESTATE_RUNNING = 1;
    int NODESTATE_COMPLETED = 2;

    /**
     * Invoked when entering the template editing mode	   
     */
    abstract void startEdit() throws Exception;

    /**
     * Invoked when restoring the previous template specification prior
     * to the prior to the last startEdit
         */
    abstract void cancelEdit() throws Exception;

    /**
     * Invoked when saving the changes made on the template specification
     * since the last startEdit
         */
    abstract void commitEdit() throws Exception;

    /**
 * Sets the state of the selected node when editing a process.
 */
    void setSelectedNodeState(int state) throws Exception;  
}
