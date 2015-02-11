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
//Source file: y:/iflow/dev/src/java/com/fujitsu/iflow/gui/util/WindowUtil.java

package com.globalsight.everest.webapp.applet.admin.graphicalworkflow.gui.util;

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.Dimension;
import java.awt.Container;
import java.awt.BorderLayout;
import java.awt.Window;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JOptionPane;
import javax.swing.JDialog;
import javax.swing.JFrame;

/**
 * Provides tools for obtaining and using window information
 */
public class WindowUtil
{

    /**
       message type specification which determines the icon to display
     */
    public static final int TYPE_INFO = 1;

    /**
       message type specification which determines the icon to display
     */
    public static final int TYPE_WARNING = 2;

    /**
       message type specification which determines the icon to display
     */
    public static final int TYPE_ERROR = 3;

    /**
       message type specification which determines the icon to display
     */
    public static final int TYPE_QUESTION = 4;

    /**
       message type specification which determines the icon to display
     */
    public static final int TYPE_PLAIN = 5;

    /**
       specifies that the dialog should contain an "OK" button
     */
    public static final int OPTION_OK = 1;

    /**
       specifies that the dialog should contain a "Cancel" button
     */
    public static final int OPTION_CANCEL = 2;

    /**
       specifies that the dialog should contain an "OK" and a "Cancel" button
     */
    public static final int OPTION_OKCANCEL = 3;

    /**
       specifies that the dialog should contain a "Yes" and a "No" button
     */
    public static final int OPTION_YESNO = 4;

    /**
       specifies that the dialog should contain a "Yes", a "No", and a "Cancel" button
     */
    public static final int OPTION_YESNOCANCEL = 5;

    /**
       return value if the "OK" button was clicked
     */
    public static final int RESULT_OK = 1;

    /**
       return value if the "Yes" button was clicked
     */
    public static final int RESULT_YES = 1;

    /**
       return value if the "No" button was clicked
     */
    public static final int RESULT_NO = 3;

    /**
       return value if the "Cancel" button was clicked
     */
    public static final int RESULT_CANCEL = 4;

    /**
       return value if the dialog was closed without using any option buttons
     */
    public static final int RESULT_MSGDLG_CLOSED = 5;

    /**
       return value if the dialog was not available
     */
    public static final int RESULT_MSGDLG_NOTAVAILABLE = 6;
    private static final int INVALIDVALUE = - 37;
    private static Object __lockObj;
    private static JFrame __frame = new JFrame (); //shailaja. Changed Frame to JFrame
    private static JDialog __modalDlg;
    private static ComponentManager __compMgr;

    /**
     * Sets the argument window to the center of the screen
     * @param win    the window object to be placed in the center of
       the screen
     * @roseuid 372F675601E9
     */
    public static void center(Window win)
    {
        //Log.println(Log.LEVEL3, "WindowUtil.center: " + win.getSize());
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        win.setLocation(screenSize.width/2 - win.getSize().width/2,
                        screenSize.height/2 - win.getSize().height/2);
    }

    //----------------------------------------------------------------

    public static void setComponentsFont(Container cont, Font font)
    {
        Component[] comps = cont.getComponents();
        int count = comps.length;
        for (int i = 0; i < count; i++)
            comps[i].setFont(font);
    }


    /**
     * Returns the root window frame of the argument component
     * @param c    the component whose root window frame is to be retrieved
     *
       @return the root window frame of the argument component
     * @roseuid 372F675601EB
     */
    public static JFrame getFrame(Component c)
    { //shailaja. Changed Frame to JFrame
        JFrame frame = null ; //shailaja
        while ((c = c.getParent()) != null)
        {
            if (c instanceof JFrame) //shailaja
                frame = (JFrame)c ; //shailaja
        }
        return frame;
    }

    /**
       @roseuid 372F675601ED
     */
    public static JFrame getFrame()
    { //shailaja
        if (__frame == null)
            __frame = new JFrame(); //shailaja

        return __frame;

    }

    /**
       @roseuid 372F675601EE
     */
    public static void setComponentManager(ComponentManager compMgr)
    {
        __compMgr = compMgr;

    }

    /**
       @roseuid 372F675601F1
     */
    private static JDialog getModalDlg(JFrame frame, String title)
    { //shailaja
        JDialog dlg;
        if (frame == null)
            dlg = new JDialog(__frame, true);
        else
            if (__modalDlg != null && __modalDlg.isVisible())
            dlg = null;
        else
            dlg = __modalDlg = new JDialog(frame, true);

        dlg.setModal(true);
        dlg.setTitle(title);
        return dlg;
    }

    /**
       Displays a message dialog with the specified error message and title.  The
       dialog also contains an OK button and an icon appropriate for the specified
       type parameter.

       @param frame parent frame for the dialog
       @param msg message string to be displayed
       @param title dialog title
       @param type message type, one of: TYPE_INFO, TYPE_WARNING, TYPE_ERROR,
       TYPE_QUESTION, or TYPE_PLAIN.

       @see #showMsgDlg(Frame, String, String, int, int)
       @see #showErrorDlg
       @roseuid 372F675601F4
     */
    public static void showMsgDlg(JFrame frame, String msg, String title, int type)
    { //shailaja
        showMsgDlg(frame, msg, title, type, OPTION_OK);

    }

    /**
       Displays a message dialog with the specified error message and title.  The
       button(s) and icon within the dialog are determined by the specified options
       and type parameters, respectively.

       @param frame parent frame for the dialog
       @param msg message string to be displayed
       @param title dialog title
       @param type message type, one of: TYPE_INFO, TYPE_WARNING, TYPE_ERROR,
       TYPE_QUESTION, or TYPE_PLAIN.
       @param options button choices available to the user, one of: OPTION_OK,
       OPTION_CANCEL, OPTION_OKCANCEL, OPTION_YESNO, OPTION_YESNOCANCEL.

       @return one of: RESULT_OK, RESULT_YES, RESULT_NO, RESULT_CANCEL, indicating
       that the user clicked the respective button; RESULT_MSGDLG_CLOSED, indicating
       that the user closed the dialog without clicking any of the choice buttons;
       or RESULT_MSGDLG_NOTAVAILABLE, indicating that the message dialog was not
       displayed because another one is up already.

       @see #showMsgDlg(Frame, String, String, int)
       @see #showErrorDlg
       @roseuid 372F675601F9
     */
    public static int showMsgDlg(JFrame frame, String msg, String title, int type, int options)
    {//shailaja
        //Log.println(Log.LEVEL3, "WindowUtil.showMsgDlg()");

        if (__lockObj == null)
            __lockObj = new Object();

        Object value;
        synchronized(__lockObj)
        {
            JDialog dlg = getModalDlg(frame, title);
            if (dlg == null)
                return RESULT_MSGDLG_NOTAVAILABLE;

            JOptionPane pane = new JOptionPane(msg, typeToSwing(type), optionToSwing(options));
            MessageDlgMgr dlgMgr = new MessageDlgMgr(pane, dlg);

            if (__compMgr == null)
                dlg.show();
            else
                __compMgr.displayModal(dlg);

            dlg.dispose();
            value = pane.getValue();
        }
        if (value instanceof Integer)
            return resultFromSwing(((Integer)value).intValue());
        else
            return RESULT_MSGDLG_CLOSED;

    }

    /**
       @roseuid 372F675601FF
     */
    private static int optionToSwing(int option)
    {
        int ret = INVALIDVALUE;
        switch (option)
        {
            case OPTION_OK:
                ret = JOptionPane.DEFAULT_OPTION;
                break;
            case OPTION_CANCEL:
                ret = JOptionPane.CANCEL_OPTION;
                break;
            case OPTION_OKCANCEL:
                ret = JOptionPane.OK_CANCEL_OPTION;
                break;
            case OPTION_YESNO:
                ret = JOptionPane.YES_NO_OPTION;
                break;
            case OPTION_YESNOCANCEL:
                ret = JOptionPane.YES_NO_CANCEL_OPTION;
                break;
        }

        if (ret == INVALIDVALUE)
            throw new IllegalArgumentException();

        return ret;

    }

    /**
       @roseuid 372F67560201
     */
    private static int resultFromSwing(int option)
    {
        int ret = INVALIDVALUE;
        switch (option)
        {
            case JOptionPane.YES_OPTION:
                ret = RESULT_YES;
                break;
            case JOptionPane.NO_OPTION:
                ret = RESULT_NO;
                break;
            case JOptionPane.CANCEL_OPTION:
                ret = RESULT_CANCEL;
                break;
        }

        if (ret == INVALIDVALUE)
            throw new IllegalArgumentException();

        return ret;

    }

    /**
       @roseuid 372F67560203
     */
    private static int typeToSwing(int type)
    {
        int ret = INVALIDVALUE;
        switch (type)
        {
            case TYPE_INFO:
                ret = JOptionPane.INFORMATION_MESSAGE;
                break;
            case TYPE_WARNING:
                ret = JOptionPane.WARNING_MESSAGE;
                break;
            case TYPE_ERROR:
                ret = JOptionPane.ERROR_MESSAGE;
                break;
            case TYPE_QUESTION:
                ret = JOptionPane.QUESTION_MESSAGE;
                break;
            case TYPE_PLAIN:
                ret = JOptionPane.PLAIN_MESSAGE;
                break;
        }

        if (ret == INVALIDVALUE)
            throw new IllegalArgumentException();

        return ret;

    }

    /**
       Displays a message dialog with the specified error message and the title
       "Error".  The dialog also contains an icon indicating an error condition
       and an OK button.  This method also dumps the exception stack trace to the
       log.

       @param frame the parent frame for the dialog
       @param msg the message string to be displayed
       @param exception the exception associated with this error message

       @see #showMsgDlg(Frame, String, String, int)
       @see #showMsgDlg(Frame, String, String, int, int)
       @roseuid 372F67560205
     */
    public static void showErrorDlg(JFrame frame, String msg, Exception exception)
    { //shailaja
        String msg2;
        /*if (exception instanceof WFInternalException) {
            //Log.printStack(Log.LEVEL0, ((WFInternalException)exception).getSource());
            msg2 = "An error occurred while communicating with the server.";
        }
        else {*/
        //Log.printStack(Log.LEVEL0, exception);
        msg2 = exception.getMessage();
        //}

        if (msg2 != null)
            msg += "\n" + msg2;
        //Error from pagehandler
        showMsgDlg(frame, msg, "Error", TYPE_ERROR);

    }
    /**
       Utility method to size a Dialog box correctly.
       Routine will query dialog box for its preferred size.
       It will size dialog box based on
       - requeted size
       - preferred size
       - screen size
       @author Prakash
       @param dialog the dialog box to be sized
       @param prefWidth - Pref width
       @param prefHeight - Pref height
    */
    public static void sizeDialog(JDialog dialog, int prefWidth, int prefHeight)
    {//HF
        sizeDialog(dialog, prefWidth, prefHeight, false);
    }
    /**
       Utility method to size a Dialog box correctly.
       Routine will query dialog box for its preferred size.
       It will size dialog box based on
       - requeted size
       - preferred size
       - screen size
       @author Prakash
       @param dialog the dialog box to be sized
       @param prefWidth - Pref width
       @param prefHeight - Pref height
       @param shrinkOnly - Flag indicating app should not check dialog boxes preferred size
    */
    public static void sizeDialog(JDialog dialog, int prefWidth, int prefHeight, boolean shrinkOnly)
    {//HF
        //Log.println(Log.LEVEL3, "WindowUtil.sizeDialog: MinSize = " + prefWidth + ", " + prefHeight);
        Dimension prefSize = new Dimension(0,0);
        if (isUsingNetscape() && !shrinkOnly)
        {
            // Get dialog box
            dialog.pack();
            prefSize = dialog.getPreferredSize();
            //Log.println(Log.LEVEL3, "PrefSize after pack: " + prefSize);
        }
        //
        // Now make compute new size (based on prefSize, prefSize)
        int w = Math.max(prefSize.width, prefWidth);
        int h = Math.max(prefSize.height, prefHeight);

        //Log.println(Log.LEVEL3, "Sizing dialog to: " + w + ", " + h);
        //
        // Note: This is a hack to adjust for Netscape's "Signed Applet: " line
        if (isUsingNetscape())
        {
            //Log.println(Log.LEVEL3, "Netscape: Increasing height by : " + dialog.getInsets().bottom);
            h += dialog.getInsets().bottom;
        }
        //
        // Make sure we are note larger than screensize
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        w = Math.min(w, screenSize.width-20);
        h = Math.min(h, screenSize.height-20);
        //
        // We have a size.
        //Log.println(Log.LEVEL3, "WindowUtil.sizeDialog: Setting size to: " + w + ", " + h);
        dialog.setSize(w, h);
    }

    /**
        Utility method to check if os is Solaris or not
    */

    public static boolean isUsingSolaris()
    {
        String osName = System.getProperty("os.name");
        if (!(osName.startsWith("W") || osName.startsWith("w")))
        {
            return true;
        }
        else  return false;
    }

    /**
       Utility method to to check if applet is running in Netscape.
       This is needed because of Netscape's security stuff.
       Method checks if class netscape.security.PrivilegeManager is found.
       @author Prakash
    */
    public static boolean isUsingNetscape()
    {
        if (!checkedForNetscape)
        {
            checkedForNetscape = true;
            String jvmVendor = System.getProperty("java.vendor");
            //Log.println(Log.LEVEL2, "jvmVendor = " + jvmVendor);
            jvmVendor = jvmVendor.toLowerCase();
            if (jvmVendor.indexOf("netscape") >= 0)
            {
                bUsingNetscape = true;
            }
            else
            {
                bUsingNetscape = false;
            }
        }
        //Log.println(Log.LEVEL3, "isUsingNetscape: " + bUsingNetscape);
        return bUsingNetscape;
    }
    private static boolean checkedForNetscape=false;
    private static boolean bUsingNetscape;

    public static boolean isUsingJavaPlugIn()
    {
        if (!checkedForJavaPluigIn)
        {
            checkedForJavaPluigIn = true;
            String jvmVendor = System.getProperty("java.vendor");
            //Log.println(Log.LEVEL2, "jvmVendor = " + jvmVendor);
            jvmVendor = jvmVendor.toLowerCase();
            if (jvmVendor.indexOf("sun") >= 0)
            {
                bUsingJavaPlugIn = true;
            }
            else
            {
                bUsingJavaPlugIn = false;
            }
        }
        //Log.println(Log.LEVEL3, "isUsingJavaPlugIn: " + bUsingJavaPlugIn);
        return bUsingJavaPlugIn;
    }
    private static boolean checkedForJavaPluigIn=false;
    private static boolean bUsingJavaPlugIn;
}

class MessageDlgMgr extends WindowAdapter implements PropertyChangeListener
{
    private JOptionPane _pane;
    private JDialog _dlg;

    /**
       @roseuid 372F67570024
     */
    MessageDlgMgr(JOptionPane pane, JDialog dlg)
    {
        _pane = pane;
        _dlg = dlg;

        _pane.selectInitialValue();

        Container contentPane = _dlg.getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(_pane, BorderLayout.CENTER);

        _dlg.pack();
        WindowUtil.center(_dlg);

        _dlg.addWindowListener(this);
        _pane.addPropertyChangeListener(this);

    }

    /**
       @roseuid 372F67570027
     */
    public void windowClosing(WindowEvent we)
    {
        _pane.setValue(null);

    }

    /**
       @roseuid 372F67570029
     */
    public void windowActivated(WindowEvent we)
    {

    }

    /**
       @roseuid 372F6757002B
     */
    public void propertyChange(PropertyChangeEvent event)
    {
        if (_dlg.isVisible() &&
            (event.getPropertyName().equals(JOptionPane.VALUE_PROPERTY) ||
             event.getPropertyName().equals(JOptionPane.INPUT_VALUE_PROPERTY)))
        {
            _dlg.setVisible(false);
        }

    }
}
