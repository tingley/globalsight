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
package com.ui;

import com.ui.swing.SwingUI;
import com.ui.text.TextUI;
import com.util.ServerUtil;

/**
 * Factory class, providing a faculty to get a <code>UI</code>.
 * <p>
 * Which <class>UI</class> implement will be return according to system is
 * windows or linux.
 * <p>
 * More information can be get from <code>UI</code> and
 * <code>ServerUtil.isInLinux()</code>
 */
public class UIFactory
{
    private static UI UI;

    /**
     * Gets a <code>UI</code> implement. 
     * <p>
     * Which <class>UI</class> implement will be return according to system is
     * windows or linux.
     * 
     * @return <code>UI</code> implement.
     */
    public static UI getUI()
    {
        if (UI == null)
        {
            if (ServerUtil.isInLinux())
            {
                UI = new TextUI();
            }
            else
            {
                UI = new SwingUI();
            }
        }

        return UI;
    }
}
