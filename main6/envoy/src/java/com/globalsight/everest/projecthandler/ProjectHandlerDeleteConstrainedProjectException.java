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
package com.globalsight.everest.projecthandler;


/**
 * An exception thrown when attempting a Project that can't
 * be deleted because there is data associated with it. 
 */
public class ProjectHandlerDeleteConstrainedProjectException 
        extends ProjectHandlerException
{
    /*
     * Create a ProjectHandlerException with the specified message.
     * @p_message The message. 
     */
    public ProjectHandlerDeleteConstrainedProjectException(
            String p_message)
    {
        super(p_message);
    }

    /**
     * @see GeneralException#GeneralException(int, int, int, String)
     * @param p_message error message.
     * @param p_originalException original exception:w.
     *
     * @deprecated It doesn't take a raw message any more
     */
    public ProjectHandlerDeleteConstrainedProjectException(
            String p_message, 
            Exception p_originalException)
    {
        super(p_message, p_originalException);
    }
}
