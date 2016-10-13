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
package com.util;

import java.awt.Font;

import javax.swing.JPanel;
import javax.swing.UIManager;

import org.apache.log4j.Logger;

public class UIUtil
{
    private static Logger log = Logger.getLogger(UIUtil.class);
    
    private static int FONT_SIZE = 12;
    public static int DEFAULT_HEIGHT = 22;
    public static int DEFAULT_WIDTH = 80;

    public static Font getFrameFont()
    {
    	Font f = (new JPanel()).getFont().deriveFont(Font.PLAIN, FONT_SIZE);
        return f;
    }

    public static void setLookAndFeel()
    {
        try
        {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
    }
}
