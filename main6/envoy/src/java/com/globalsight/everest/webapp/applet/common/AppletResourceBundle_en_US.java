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
package com.globalsight.everest.webapp.applet.common;

import java.util.ListResourceBundle;
import java.io.Serializable;

/**
 * A resource bundle for en_US labesl and message.
 */
public class AppletResourceBundle_en_US extends ListResourceBundle implements Serializable
{
    private static final long serialVersionUID = -2264352964932592081L;

    public Object[][] getContents()
    {
        return contents;
    }

    static final Object[][] contents = {
        //labels
        {"action", "Action"},
        {"ACTIVE_STATE_KEY", "ACTIVE_STATE_KEY"},
        {"activityNode", "Activity Node"},
        {"andNode", "And Node"},
        {"andNodeDialog", "AND Node Dialog"},
        {"andNodeProperties", "AND Node Properties"},
        {"arrow", "Arrow"},
        {"browse", "Browse..."},
        {"close", "Close"},
        {"COMPLETE_STATE_KEY", "COMPLETE_STATE_KEY"},
        {"Cancel", "Cancel"},
        {"conditionNode", "Condition Node"},
        {"conditionalNodeProperties", "Conditional Node Properties"},
        {"description", "Description:"},
        {"decisions", "Decisions"},
        {"docTitle", "My Document Title"},
        {"down", "Down"},
        {"emailAddress", "E-mail address:"},
        {"enterScriptName", "Enter script name"},
        {"epilogue", "Epilogue:"},
        {"EVENT_PENDING_KEY", "EVENT_PENDING_KEY"},
        {"exit" , "Exit"},
        {"exitNode", "Exit Node"},
        {"file", " File"},
        {"general", "General"},
        {"if", "If"},
        {"INACTIVE_STATE_KEY", "INACTIVE_STATE_KEY"},
        {"is", "is"},
        {"name", "Name:"},
        {"notification", "Notification"},
        {"OK", "OK"},
        {"OK_KEY", "OK_KEY"},
        {"orNode", "Or Node"},
        {"orNodeDialog", "OR Node Dialog"},
        {"orNodeProperties", "OR Node Properties "},
        {"path", " Path"},
        {"pointer", "Pointer"},
        {"print", "Print"},
        {"prologue", "Prologue:"},
        {"properties" , "Properties"},
        {"save", "Save"},
        {"saveNode", "Save Node"},
        {"script", "Script:"},
        {"scripting", "Scripting"},
        {"selectDmsDir", "Select default DMS directory"},
        {"selectScript", "Select Script..."},
        {"start" , "Start"},
        {"startNodeProperties", "Start Node Properties"},
        {"then", "then"},
        {"title", " Title"},
        {"togglesEmailNotification", "Toggles e-mail notification"},
        {"togglesNotification", "Toggles notification"},
        {"up", "Up"},

        //messages
        {"msg_arrowEmptyName","Please enter a name for the arrow."},
        {"msg_arrowMaxLength","The arrow's name can't exceed 30 characters\n Please correct the arrow's name,"},
        {"msg_arrowWithPendingEvents","This arrow has pending events.  Continue?"},
        {"msg_conditionToCondition","You cannot have a condition node\n connecting to another condition node."},
        {"msg_dataConversionError","Data Conversion Error!"},
        {"msg_eventPendingWarning","Event Pending Warning!"},
        {"msg_exitNode","A worklfow should have at least one activiy node and an exit node."},
        {"msg_fileExtension", "File Extension Error!"},
        {"msg_incorrectDataType","The value data does not match the defined data type."},
        {"msg_invalidWorkflow",
            "The workflow is invalid. Make sure:\n 1. Each node has incoming/outgoing arrow(s).\n 2. The required info for a node is populated. \n"},
        {"msg_noDirSelection", " Selection cannot be a directory."},
        {"msg_noDupArrowError", "Can not have duplicate arrow labels from this node\n The arrow's old name has been restored\n Feel free to change it as long as no duplicates occur"},
        {"msg_pathField", "Path field can not be blank!"},
        {"msg_pathFieldError", "Path Field Error!"},
        {"msg_selectionError", " File Selection Error"},
        {"msg_twoArrows","You cannot have two arrows\n connecting to the same nodes"},
        {"msg_twoOutgoingArrows","You cannot have two outgoing arrows\r\n from the start and activity nodes."}
    };
}
