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
package com.globalsight.everest.webapp.applet.admin.graphicalworkflow.gui.planview;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import javax.swing.JFrame;
import javax.swing.JTextField;
/**
Class containing common drawing items used by all classes
Prakash
*/
public class DrawInfo
{

    private static Dimension prefTextSize = null;

    /**
    This method returns the preferred size of a TextField. This is needed
    to determine the ideal size of TextField's in UIActivity.
    */
    public static Dimension getPreferredTextSize()
    {
        if (prefTextSize == null)
        {
            //
            // Create textField, render it (because unrendered textbox does
            // not know its pref size
            Dimension minTextSize = new Dimension(30, 15);
            JFrame f = new JFrame(); //shailaja. Changed Frame to JFrame
            f.getContentPane().setLayout(new FlowLayout()); //shailaja
            // render off screen so it doesn't show
            f.setLocation(5000,5000);
            JTextField tf = new JTextField(8);//shailaja
            tf.setFont(new Font("SansSerif", Font.PLAIN, 10)); 
            f.getContentPane().add(tf); //shailaja
            f.setVisible(true);
            prefTextSize = tf.getPreferredSize();
            f.setVisible(false);
            f.dispose();
            if (prefTextSize == null)
            {
                // Bad !!! - should never happen. Just being defensive
                prefTextSize = new Dimension(minTextSize);
            }
            //
            // Dont let size get below some minimum size
            prefTextSize.width = Math.max(prefTextSize.width, minTextSize.width);
            prefTextSize.height = Math.max(prefTextSize.height, minTextSize.height);

        }

        return prefTextSize;
    }

}
