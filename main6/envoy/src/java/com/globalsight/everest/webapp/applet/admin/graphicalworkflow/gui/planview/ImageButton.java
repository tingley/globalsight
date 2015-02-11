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

import javax.swing.JPanel; 
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Image;
import java.awt.AWTEventMulticaster;
import java.awt.Dimension;
import java.awt.geom.AffineTransform;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.ImageObserver;

import com.globalsight.everest.webapp.applet.common.AppletHelper;
import com.globalsight.everest.webapp.applet.common.MessageCatalog;

class ImageButton extends JPanel implements MouseListener
{
    static final int NO_TYPE        = 0;
    static final int ACTIVITY_TYPE  = 1;
    static final int AND_TYPE       = 2;
    static final int ARROW_TYPE     = 3;
    static final int CONDITION_TYPE = 4;
    static final int EXIT_TYPE      = 5;
    static final int OR_TYPE        = 6;
    static final int SUB_TYPE       = 8;
    static final int SELECTION_TYPE = 9;
    static final int POINTER_TYPE   = 10;
    static final int NO_OP_TYPE     = 10;
    static final int CHAINED_TYPE   = 11;
    static final int DELAY_TYPE     = 12;
    static final int SAVE_TYPE      = 13;
    static final int CANCEL_TYPE    = 14;
    static final int PRINT_TYPE    = 15;

    private boolean isDown = false;
    private boolean actionPending = false;
    private boolean fixedSize;
    private int type = NO_TYPE;
    private static final int GAP = 35;
    private MessageCatalog msgCat = new MessageCatalog (GraphicalPane.localeName);


    static final String ACTIVITY_NAME  = "lb_activity_node";     
    static final String AND_NAME       = "lb_and_node";
    static final String OR_NAME        = "lb_or_node";
    static final String SUB_NAME       = "Sub Processor Node";  
    static final String CONDITION_NAME = "lb_condition_node";
    static final String EXIT_NAME      = "lb_exit_node";
    static final String ARROW_NAME     = "lb_arrow";      
    static final String POINTER_NAME   = "lb_pointer"; 
    static final String CHAINED_NAME   = "Chained Node";        
    static final String DELAY_NAME     = "Delay Node";   
    static final String BUTTON_DOWN_ACTION = "button down";
    static final String BUTTON_UP_ACTION   = "button up"; 
    static final String SAVE_NAME      ="lb_save";
    static final String PRINT_NAME      ="lb_print";
    static final String CANCEL_NAME    ="lb_cancel";
    private Image buttonImage;
    private ActionListener actionListener = null;   
    AffineTransform at; 

    public ImageButton(Image buttonImage)
    {
        // Creates an image button with buttonImage as the button's image and a fixed size
        at = new AffineTransform();
        this.buttonImage = buttonImage;
        this.isDown = false;
        this.fixedSize = true;
        addMouseListener (this);    

        SymMouse aSymMouse = new SymMouse();
        this.addMouseListener(aSymMouse);       
    }   

    public ImageButton(Image buttonImage, boolean fixedSize)
    {
        // Creates an image button with buttonImage as the button's
        // image. If fixedSize is true, the button's size is fixed. Otherwise,
        // the button's size can be adjusted. 
        at = new AffineTransform();
        this.buttonImage = buttonImage;
        this.isDown = false;
        this.fixedSize = fixedSize;
    }

    public void setImage(Image new_image)
    {
        buttonImage = new_image;
    }   

    public void setType(int _type)
    {
        type = _type;

        switch (type)
        {
            case ACTIVITY_TYPE:
                {
                    setToolTipTextByKey(ACTIVITY_NAME);              
                    break;
                }
            case EXIT_TYPE:
                {
                    setToolTipTextByKey(EXIT_NAME);                
                    break;
                }
            case CONDITION_TYPE:
                {
                    setToolTipTextByKey(CONDITION_NAME);                 
                    break;
                }
            case OR_TYPE:
                {
                    setToolTipTextByKey(OR_NAME);              
                    break;
                }
            case AND_TYPE:
                {
                    setToolTipTextByKey(AND_NAME);               
                    break;
                }
            case ARROW_TYPE:
                {
                    setToolTipTextByKey(ARROW_NAME);               
                    break;
                }
                /* case CHAINED_TYPE:    
                     {
                         setToolTipText(CHAINED_NAME); 
                         break;
                     }*/
            /*case SUB_TYPE:  
            {
                 setToolTipText(SUB_NAME); 
                 break;
            } */
                 /*case DELAY_TYPE:   
                     {
                         setToolTipText(DELAY_NAME); 
                         break;
                     } */
            case POINTER_TYPE:
                {
                    setToolTipTextByKey(POINTER_NAME);               
                    break;
                }
            case SAVE_TYPE:
                {
                    setToolTipTextByKey(SAVE_NAME);               
                    break;
                }
            case PRINT_TYPE:
                {
                    setToolTipTextByKey(PRINT_NAME);
                    break;
                }
            case CANCEL_TYPE:
                {
                    setToolTipTextByKey(CANCEL_NAME);               
                    break;
                }

            default:
                break;
        }
    }
    
    @Deprecated
    /**
     * Use #setToolTipTextByKey instead, for applet i18n
     */
    public void setToolTipText(String text)
    {
        super.setToolTipText(msgCat.getMsg(text));
    }
    
    public void setToolTipTextByKey(String key)
    {
        super.setToolTipText(AppletHelper.getI18nContent(key));
    }
    
    public int getType()
    {
        return type;
    }   

    public void setButtonDown(boolean new_state)
    {
        isDown = new_state;
        repaint();
    }   

    public boolean isButtonDown()
    {
        return isDown;
    }       

    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D)g;
        g2.transform(at);
        Dimension currSize = size();
        int width = currSize.width;
        int height = currSize.height;

        int imgHeight = buttonImage.getHeight(this);
        int imgWidth = buttonImage.getWidth(this);

        // Display the shading in the upper left. If the button is up, the
        // upperleft shading is white, otherwise it's black
        if (isDown)
        {
            g2.setPaint(GraphicalPane.DEFAULT_COLOR);
        }
        else
        {
            g2.setPaint(Color.white);
        }
        g2.drawLine(0, 0, width-1, 0);
        g2.drawLine(0, 0, 0, height-1);

        // If the button is up, we draw the image starting at 1,1
        int imgX = 1;
        int imgY = 1;

        if (isDown)
        {
            // If the button is down, move the image right and down one pixel and
            // draw gray shading at 1,1			
            g2.setPaint(Color.gray);
            g2.drawLine(1, 1, width-2, 1);
            g2.drawLine(1, 1, 1, height-2);
            imgX++;
            imgY++;

        }
        else
        {
            // If the button is up, draw gray shading just inside the bottom right shading
            g2.setPaint(Color.gray);
            g2.drawLine(1, height-2, width-1, height-2);
            g2.drawLine(width-2, 1, width-2, height-2);
        }

        // Compare the width of the button to the width of the image, if
        // the button is wider, move the image over to make sure it's centered.
        int xDiff = (width - 3 - imgWidth) / 2;
        if (xDiff > 0) imgX += xDiff;

        // Compare the height of the button to the height of the image, if
        // the button is taller, move the image down to make sure it's centered.
        int yDiff = (height - 3 - imgHeight) / 2;
        if (yDiff > 0) imgY += yDiff;

        g2.drawImage(buttonImage, imgX, imgY, this);

        // Draw the bottom right shading. If the button is up, the shading is
        // black, otherwise it's white.
        if (isDown)
        {
            g2.setPaint(Color.white);
        }
        else
        {
            g2.setPaint(GraphicalPane.DEFAULT_COLOR);
        }
        g2.drawLine(1, height-1, width-1, height-1);
        g2.drawLine(width-1, 1, width-1, height-1);   
    }   

    public Dimension size()
    {
        // If the sized isn't fixed, just say super-size it! (har har)
        if (!fixedSize) return super.size();
        return preferredSize();   
    }

    public boolean imageUpdate(Image img, int flags, int x, int y, int width, int height)
    {
        // We watch the image loading so we can immediately adjust the size
        // when we get a valid image. This allows you to create this button
        // without setting up a media tracker to insure that the image has
        // been loaded.
        // If we have a complete image, resize the button and ask the parent
        // to recompute all the component positions. Good thing this only
        // gets called once!
        if ((flags & ImageObserver.ALLBITS) != 0)
        {
            resize(img.getWidth(this)+3, img.getHeight(this)+3);
            getParent().validate();
        }

        // Let the canvas class handle any other information it was looking for
        return super.imageUpdate(img, flags, x, y, width, height);  
    }   

    public void processActionEvent()
    {
        //     Log.println(Log.LEVEL3, "button action!");

        if (actionListener != null)
        {
            if ( isDown )
            {
                actionListener.actionPerformed (new ActionEvent (this,
                                                                 ActionEvent.ACTION_PERFORMED, BUTTON_DOWN_ACTION));
            }
        }
    }   

    public Dimension minimumSize()
    {
        // The minimum size is the amount of space for the shading around the
        // edges, plus one pixel for the image itself.
        return new Dimension(4, 4);  
    }   

    public void mouseExited(MouseEvent event)
    {
    }

    public void mouseEntered(MouseEvent event)
    {
    } 

    public void mouseClicked(MouseEvent event)
    {
    }

    public Dimension preferredSize()
    {
        // We'd prefer to have just enough space for the shading (shading takes
        // 3 pixels in each direction) plus the size of the image.
        return new Dimension(buttonImage.getWidth(this)+3,
                             buttonImage.getHeight(this)+3);
    }   

    public void mousePressed(MouseEvent event)
    {
        if ( !isDown )
        {
            isDown = true;
            repaint();
        }
    }   

    public void mouseReleased(MouseEvent event)
    {
        if ( isDown & !actionPending )
        {
            actionPending = true;
            processActionEvent();
            actionPending = false;
            if (type == NO_TYPE)
            {
                //other image buttons (form, ...)
                isDown = false;
                repaint();
            }
        }
    }   

    protected boolean isActionPending()
    {
        //to create a true modal button
        return actionPending;
    }   

    public void addActionListener(ActionListener al)
    {
        actionListener = AWTEventMulticaster.add (actionListener, al);  
    }   

    public void removeActionListener(ActionListener al)
    {
        actionListener = AWTEventMulticaster.remove (actionListener, al);   
    }

    class SymMouse extends java.awt.event.MouseAdapter
    {
        public void mouseEntered(java.awt.event.MouseEvent event)
        {
            Object object = event.getSource();
            if (object == ImageButton.this)
                ImageButton_mouseEntered(event);
        }
    }

    void ImageButton_mouseEntered(java.awt.event.MouseEvent event)
    {
        // to do: code goes here.
    }
}
