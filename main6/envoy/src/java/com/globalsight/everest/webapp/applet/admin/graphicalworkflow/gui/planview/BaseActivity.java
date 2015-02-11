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

import java.awt.Point;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Font;
import java.awt.Rectangle;
import javax.swing.JScrollPane;
import javax.swing.JLabel;
import javax.swing.ScrollPaneConstants;
import com.globalsight.everest.workflow.WorkflowTaskInstance;
import com.globalsight.everest.workflow.WorkflowTask;




class BaseActivity extends UIObject
{

    private static int WIDTH = 60;
    private static int HEIGHT = 70;     
    //package scope for subclass access in the package
    private boolean enterPressed = false;
    // change to JLable
    // JTextArea roleNameField = new JTextArea(0, 0);
    // JTextArea nameField = new JTextArea(2, 15);
    JLabel roleNameField = new JLabel();
    JLabel nameField = new JLabel();


    JScrollPane rolePane = new JScrollPane(roleNameField,
                                           ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER, 
                                           ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
                                          );
    JScrollPane namePane = new JScrollPane(nameField,
                                           ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER, 
                                           ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
                                          ); 

    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D)g;        
        ((GRectangle)shape).paint(g2, pos, color, gPane.zoomRatio, selected);        
    }

    public BaseActivity(Point p, GraphicalPane inGPane)
    {
        super(p, inGPane);

        Font ft = new Font("SansSerif", Font.PLAIN, 10);        
        roleNameField.setFont(ft);
        // roleNameField.setBackground(COLOR_INACTIVE);
        //nameField.setBackground(COLOR_INACTIVE);

        nameField.setFont(ft);        
        //roleNameField.setLineWrap(true);
        //roleNameField.setWrapStyleWord(true);
        //nameField.setLineWrap(true);    
        //nameField.setWrapStyleWord(true);
        // objectName = "ActivityName";
        //get label from page handler
        roleName = "    ";
        objectName = "    ";

        shape = new GRectangle(p);   
        area = new Rectangle(pos.x, pos.y, UIActivity.getStdWidth(), UIActivity.getStdHeight());
        pos.translate(UIActivity.getStdWidth()/2, UIActivity.getStdHeight()/2);   

        /*roleNameField.addKeyListener(new KeyAdapter()
                                     {
                                         public void keyTyped(KeyEvent e)
                                         {
                                             validateName(roleNameField, "role");
                                         }          
                                         public void keyPressed(KeyEvent e)
                                         {
                                             if (e.getKeyCode() == KeyEvent.VK_ENTER)
                                                 enterPressed = true;
                                         }  

                                     });     

        nameField.addKeyListener(new KeyAdapter()
                                 {
                                     public void keyTyped(KeyEvent e)
                                     {
                                         validateName(nameField, "activity");
                                     }  
                                     public void keyPressed(KeyEvent e)
                                     {
                                         if (e.getKeyCode() == KeyEvent.VK_ENTER)
                                             enterPressed = true;
                                     }  

                                 });  */
    }

    //////////////////////////////////////////////////////////////////////
    //  Begin: Implementation of UIObject's Abstract Methods
    //////////////////////////////////////////////////////////////////////
    /**
     * Get the node's default width.
     */
    public int getNodeWidth() 
    {
        return WIDTH;
    }
	
    /**
     * Get the node's default height.
     */
    public int getNodeHeight() 
    {
        return HEIGHT;
    }
    //////////////////////////////////////////////////////////////////////
    //  End: Implementation of UIObject's Abstract Methods
    //////////////////////////////////////////////////////////////////////

    JLabel getRoleNameField()
    {
        return roleNameField;
    }

    JLabel getNameField()
    {
        return nameField;
    }  

    //@author:Hamid
    // A new utility method to gently control the size of the activity name:
    // comments this methodas not using 
    /*private void validateName(JTextArea textArea, String name)
    {
        String text = textArea.getText();

        if (text != null)// to eleminate the \n character.
            if ((text.length() == 1)&&(text.equals("\n")))
                textArea.setText("");

        if ( text == null || text.trim().length() == 0 ) return;
        if ( text.length() >= WorkflowConstants.MAX_NODE_NAME_LENGTH )
        {
            JOptionPane.showMessageDialog(null, 
                                          "The " + name + "'s name can't exceed 30 characters\n" +
                                          "You may want to correct it");    //Parag
            textArea.requestFocus(); 
            text = text.substring(0, text.length() - 2);           
            textArea.setText(text);      
        }

        if (enterPressed)
        {
            String nameValue = "";
            for (int i=0;i<text.length();i++)
            {
                String temp = text.substring(i,i+1);
                if (!(temp.equals("\n")))
                    nameValue = nameValue + temp;
            }

            textArea.setText(nameValue);  
            enterPressed = false;
        }
    }  */
}