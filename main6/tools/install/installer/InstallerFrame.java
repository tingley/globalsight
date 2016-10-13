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

package installer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;
import javax.swing.text.JTextComponent;

public class InstallerFrame
    extends JFrame
    implements ActionListener
{
    private static final long serialVersionUID = -4480062507006594801L;

    private static final String INSTALL_FRAME_UI_PROPERTIES = "data/installUI";
    public static final int YES_OPTION = 0;
    public static final int NO_OPTION = 1;

    private ResourceBundle m_installerFrameUI;

    // List of installer screens
    private ArrayList<JComponent> m_screenList = new ArrayList<JComponent>();
    // Panel holding the the currently displayed installer screen
    private JPanel m_centerPanel = new JPanel(new BorderLayout());
    private int m_currentScreen = 0;

    private Border m_border = BorderFactory.createEtchedBorder();
    protected Font m_font;
    protected Color m_bgColor;

    // Globals - GUI controls accessed by actionPerformed
    protected JPanel buttonsPanel;
    protected JButton m_backButton = new JButton();
    protected JButton m_installButton = new JButton();
    protected JButton m_nextButton = new JButton();
    protected JButton m_quitButton = new JButton();

    private int m_topIndent = 50;
    private int m_leftIndent = 50;
    private int m_rightIndent = 50;

    public InstallerFrame()
    {
        setup(Locale.getDefault());
    }

    public InstallerFrame(Locale p_locale)
    {
        setup(p_locale);
    }

    public void setSize(Dimension frameSize)
    {
        super.setSize(frameSize);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screenSize.width - frameSize.width) / 2,
                (screenSize.height - frameSize.height) / 2);
    }

    public void setFirstScreen()
    {
        m_centerPanel.add((JComponent) m_screenList.get(0), BorderLayout.CENTER);
    }

    public void addScreen(JComponent screen)
    {
        m_screenList.add(screen);
    }

    public int getCurrentScreen()
    {
        return m_currentScreen;
    }

    public void setScreenVisible(int screenNumber, boolean visible)
    {
        ((JComponent) m_screenList.get(screenNumber)).setVisible(visible);
    }

    public boolean isScreenVisible(int screenNumber)
    {
        return ((JComponent) m_screenList.get(screenNumber)).isVisible();
    }

    public int getNumberOfScreens()
    {
        return m_screenList.size();
    }

    public void setVerticalGap(int vGap)
    {
        m_topIndent = vGap;
    }

    public void setLeftGap(int lGap)
    {
        m_leftIndent = lGap;
    }

    public void setRightGap(int rGap)
    {
        m_rightIndent = rGap;
    }

    public void setHorizontalGap(int hGap)
    {
        m_leftIndent = hGap;
        m_rightIndent = hGap;
    }

    public int getVerticalGap()
    {
        return m_topIndent;
    }

    public int getLeftGap()
    {
        return m_leftIndent;
    }

    public int getRightGap()
    {
        return m_rightIndent;
    }

    public void loadPropertiesIntoArrayList(String p_propertiesFile, ArrayList<String> p_list)
        throws IOException
    {
        BufferedReader in =
            new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(p_propertiesFile)));
        String str;
        while ((str = in.readLine()) != null)
        {
            if (str.startsWith("#")) // It's a comment
            {
                continue;
            }
            else if (str.trim().length() > 0)
            {
                p_list.add(str);
            }
        }
        in.close();
    }

    public Properties loadProperties(String p_propertyFileName)
        throws IOException
    {
        Properties properties = new Properties();
        InputStream in;
        try {
            // Open the file in its specified location
            in = new FileInputStream(p_propertyFileName);
        }
        catch (FileNotFoundException ex) {
            // File is not found, so try to open it again as a resource.
            // If the file is stored in the same jar file as the class,
            // it will be found this way.
            in = getClass().getResourceAsStream(p_propertyFileName);

            // File is still not found, so pass on the exception
            if (in == null)
                throw ex;
        }

        properties.load(in);
        return properties;
    }

    public void saveProperties(Properties p_properties, String p_fileName)
        throws IOException
    {
        System.out.print("\nSaving your settings to "
            + p_fileName
            + ".\n\n");
        File propertiesFile = new File(p_fileName);
        propertiesFile.getParentFile().mkdirs();
        p_properties.store(new FileOutputStream(p_fileName), null);
    }

    public static ArrayList<String> parseList(String p_listOfValues)
    {
        ArrayList<String> values = new ArrayList<String>();
        parseList(p_listOfValues, values);
        return values;
    }

    /*
     * Parse a list into an array list.  If the list contains new lines (\n),
     * that will be used as delimiters.  Otherwise, break on commas.
     */
    public static void parseList(String p_listOfValues, ArrayList<String> p_values)
    {
        if (p_listOfValues == null)
            return;

        String breakMark = (p_listOfValues.indexOf("\n") != -1 ?
                                "\n" : ",");

        StringTokenizer st = new StringTokenizer(p_listOfValues,  breakMark);
        while (st.hasMoreTokens()) {
            p_values.add(st.nextToken().trim());
        }
    }

    public int showQuestionDialog(String message, int defaultButton)
    {
        String[] options = { m_installerFrameUI.getString("yes"),
                m_installerFrameUI.getString("no") };

        return JOptionPane.showOptionDialog(this,
                message,
                "",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[defaultButton]);
    }

    public void showErrorDialog(String message)
    {
        String[] options = { m_installerFrameUI.getString("ok") };
        JOptionPane.showOptionDialog(this,
                message,
                "",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.ERROR_MESSAGE,
                null,
                options,
                options[0]);
    }

    public void showErrorDialogAndQuit(String message)
    {
        String[] options = { m_installerFrameUI.getString("quit") };
        JOptionPane.showOptionDialog(this,
                message,
                "",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.ERROR_MESSAGE,
                null,
                options,
                options[0]);
        System.exit(1);
    }

    private void setup(Locale p_locale)
    {
        m_installerFrameUI = ResourceBundle.getBundle(INSTALL_FRAME_UI_PROPERTIES, p_locale);

        int fontSize = Integer.parseInt(m_installerFrameUI.getString("font_size"));
        String fontName = m_installerFrameUI.getString("font_family");
        if (fontName.equals("default"))
            m_font = (new JPanel()).getFont().deriveFont(Font.PLAIN, fontSize);
        else
            m_font = new Font(fontName, Font.PLAIN, fontSize);
        //m_bgColor = new Color(204, 204, 204);
        //m_bgColor = Color.white;
        String background = m_installerFrameUI.getString("background_color");
        if (background.equals("black"))
            m_bgColor = Color.black;
        else if (background.equals("blue"))
            m_bgColor = Color.blue;
        else if (background.equals("cyan"))
            m_bgColor = Color.cyan;
        else if (background.equals("gray"))
            m_bgColor = Color.gray;
        else if (background.equals("darkGray"))
            m_bgColor = Color.darkGray;
        else if (background.equals("green"))
            m_bgColor = Color.green;
        else if (background.equals("lightGray"))
            m_bgColor = Color.lightGray;
        else if (background.equals("magenta"))
            m_bgColor = Color.magenta;
        else if (background.equals("orange"))
            m_bgColor = Color.orange;
        else if (background.equals("pink"))
            m_bgColor = Color.pink;
        else if (background.equals("red"))
            m_bgColor = Color.red;
        else if (background.equals("white"))
            m_bgColor = Color.white;
        else if (background.equals("yellow"))
            m_bgColor = Color.yellow;
        else
            m_bgColor = new Color(214, 211, 206); //(new JPanel()).getBackground();

        getContentPane().setLayout(new BorderLayout());
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        // Create the buttons panel and add it to the bottom of the installer, which will always
        // be visible regardless of which screen the center panel is displaying.
        JPanel navigationPanel = createNavigationPanel();
        getContentPane().add(navigationPanel, BorderLayout.SOUTH);
        getContentPane().add(m_centerPanel, BorderLayout.CENTER);

        int windowHeight = Integer.parseInt(m_installerFrameUI.getString("window_height"));
        int windowWidth = Integer.parseInt(m_installerFrameUI.getString("window_width"));
        setSize(new Dimension(windowWidth, windowHeight));
        setFont(m_font);
    }

    private JPanel createNavigationPanel()
    {
        JButton[] buttonList = { m_quitButton,
                m_backButton,
                m_nextButton,
                m_installButton };
        String[] buttonNameList = { m_installerFrameUI.getString("quit"),
                m_installerFrameUI.getString("back"),
                m_installerFrameUI.getString("next"),
                m_installerFrameUI.getString("install") };
        char[] buttonMnemonicList = { 'q', KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT, 'i' };

        buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 20));
        //ImagePanel buttonsPanel = new ImagePanel("../../../images/globe_header.gif");
        //JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 21));

        for (int i = 0; i < buttonList.length; i++) {
            buttonList[i].setText(buttonNameList[i]);
            buttonList[i].setFont(m_font.deriveFont(Font.PLAIN));
            buttonList[i].setMnemonic(buttonMnemonicList[i]);
            buttonsPanel.add(buttonList[i]);
            buttonList[i].addActionListener(this);
            buttonList[i].setBackground(m_bgColor);
        }

        JPanel navigationPanel = new JPanel(new BorderLayout());
        navigationPanel.add(buttonsPanel, BorderLayout.EAST);
        //JPanel images = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        //images.add(new ImagePanel("../../../images/logo_header.gif"));
        //images.add(new JPanel());
        //navigationPanel.add(images, BorderLayout.WEST);

        m_backButton.setEnabled(false);
        m_installButton.setEnabled(false);
        //images.setBackground(m_bgColor); //Color.white);
        navigationPanel.setBackground(m_bgColor); //Color.white);
        buttonsPanel.setBackground(m_bgColor); //Color.white);
        //images.setBackground(Color.white);
        //navigationPanel.setBackground(Color.white);
        //buttonsPanel.setBackground(Color.white);

        return navigationPanel;
    }

    protected JComponent createTextScreen(String text)
    {
        return createTextScreen(text, "text/plain");
    }

    protected JComponent createTextScreen(String text, String type)
    {
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();

        JTextComponent textArea;
        if (type.equals("text/html")) {
            textArea = new JEditorPane(type, text);
        }
        else {
            textArea = new JTextArea(text);
        }
        textArea.setCaretPosition(0);
        textArea.setEditable(false);
        JPanel outterPanel = new JPanel(gridbag);

        c.fill = GridBagConstraints.BOTH;
        gridbag.setConstraints(textArea, c);

        outterPanel.add(textArea);
        textArea.setFont(m_font);
        textArea.setBackground(m_bgColor);
        outterPanel.setBackground(m_bgColor);
        JScrollPane messageScreen = new JScrollPane(outterPanel);

        return messageScreen;
    }

    public void showPostInstallScreen(String text)
    {
        JComponent messageScreen = createTextScreen(text, "text/html");

        m_centerPanel.setVisible(false);
        m_centerPanel.remove((JComponent) m_screenList.get(m_currentScreen));
        m_centerPanel.add(messageScreen, BorderLayout.CENTER);
        m_centerPanel.setVisible(true);
    }

    public void actionPerformed(ActionEvent e)
    {
        Object source = e.getSource();
        if (source instanceof JButton) {
            if (source.equals(m_backButton) || source.equals(m_nextButton)) {
                int increment = source.equals(m_nextButton) ? 1 : -1;
                int nextScreen = m_currentScreen + increment;

                while ( (nextScreen < m_screenList.size() && nextScreen >= 0)
                        && !((JComponent) m_screenList.get(nextScreen)).isVisible() )
                {
                    nextScreen += increment;
                }

                m_centerPanel.setVisible(false);
                m_centerPanel.remove((JComponent) m_screenList.get(m_currentScreen));
                m_centerPanel.add((JComponent) m_screenList.get(nextScreen), BorderLayout.CENTER);
                m_centerPanel.setVisible(true);

                m_currentScreen = nextScreen;
                m_backButton.setEnabled(m_currentScreen != 0);
                m_nextButton.setEnabled(m_currentScreen != m_screenList.size()-1);
                m_installButton.setEnabled(m_currentScreen == m_screenList.size()-1);
            }
            else if (source.equals(m_quitButton)) {
                System.exit(0);
            }
        }
    }

    public void titlePanel(JComponent p_panel, String p_title, int p_fontSize)
    {
        //p_panel.setForeground(Color.black);
        p_panel.setBorder(BorderFactory.createTitledBorder(
                m_border, p_title,
                TitledBorder.LEFT,
                TitledBorder.TOP,
                m_font.deriveFont(Font.BOLD, p_fontSize)));
    }

    public JPanel titleAndPadPanel(JComponent centerPanel, String p_title, String p_helpText)
    {
        JTextArea topPad = new JTextArea();
        topPad.setText(p_helpText);
        topPad.setPreferredSize(new Dimension(10, m_topIndent));
        JPanel leftPad = new JPanel();
        leftPad.setPreferredSize(new Dimension(m_leftIndent, 10));
        JPanel rightPad = new JPanel();
        rightPad.setPreferredSize(new Dimension(m_rightIndent, 10));
        JPanel paddedPanel = new JPanel(new BorderLayout());
        paddedPanel.add(topPad, BorderLayout.NORTH);
        paddedPanel.add(leftPad, BorderLayout.WEST);
        paddedPanel.add(centerPanel, BorderLayout.CENTER);
        paddedPanel.add(rightPad, BorderLayout.EAST);

        titlePanel(paddedPanel, p_title, m_font.getSize()+2);

        topPad.setEditable(false);

        topPad.setFont(m_font.deriveFont(Font.ITALIC, m_font.getSize()-1));
        topPad.setBackground(m_bgColor);
        leftPad.setBackground(m_bgColor);
        rightPad.setBackground(m_bgColor);
        centerPanel.setBackground(m_bgColor);
        paddedPanel.setBackground(m_bgColor);

        return paddedPanel;
    }

    public static void main(String[] args)
    {
        InstallerFrame installer = new InstallerFrame();
        installer.setVisible(true);
    }

}