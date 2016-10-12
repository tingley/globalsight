/*
 * Debex.java
 *
 * Created on May 3, 2004, 9:18 PM
 */

package debex.ui.view;

/**
 *
 * @author  cvdlaan
 */
public class Debex extends javax.swing.JFrame
{
    
    /** Creates new form Debex */
    public Debex ()
    {
        initComponents ();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents()//GEN-BEGIN:initComponents
    {
        buttonGroup1 = new javax.swing.ButtonGroup();
        lbFile = new javax.swing.JLabel();
        filename = new javax.swing.JTextField();
        btnBrowseFile = new javax.swing.JButton();
        lbType2 = new javax.swing.JLabel();
        filetype = new javax.swing.JComboBox();
        lbRules = new javax.swing.JLabel();
        rulefilename = new javax.swing.JTextField();
        btnBrowseRule = new javax.swing.JButton();
        lbLocale = new javax.swing.JLabel();
        encoding = new javax.swing.JComboBox();
        lbEncoding = new javax.swing.JLabel();
        locale = new javax.swing.JComboBox();
        lbSegmentation = new javax.swing.JLabel();
        segmentationSentence = new javax.swing.JRadioButton();
        segmentationParagraph = new javax.swing.JRadioButton();
        btnExtract = new javax.swing.JButton();
        lbResult = new javax.swing.JLabel();
        btnViewGxml = new javax.swing.JButton();
        btnEditErrors = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        result = new javax.swing.JTextArea();
        jMenuBar1 = new javax.swing.JMenuBar();
        mn_file = new javax.swing.JMenu();
        mi_fileExit = new javax.swing.JMenuItem();
        mn_help = new javax.swing.JMenu();
        mi_helpHelp = new javax.swing.JMenuItem();
        mi_helpAbout = new javax.swing.JMenuItem();

        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Debex");
        lbFile.setDisplayedMnemonic('I');
        lbFile.setLabelFor(filename);
        lbFile.setText("File:");
        getContentPane().add(lbFile, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 30, 20));

        filename.setNextFocusableComponent(btnBrowseFile);
        getContentPane().add(filename, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 10, 280, -1));

        btnBrowseFile.setMnemonic('B');
        btnBrowseFile.setText("Browse...");
        btnBrowseFile.setNextFocusableComponent(rulefilename);
        getContentPane().add(btnBrowseFile, new org.netbeans.lib.awtextra.AbsoluteConstraints(330, 10, -1, -1));

        lbType2.setDisplayedMnemonic('T');
        lbType2.setLabelFor(filetype);
        lbType2.setText("Type:");
        getContentPane().add(lbType2, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 70, -1, -1));

        filetype.setNextFocusableComponent(locale);
        getContentPane().add(filetype, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 70, 110, -1));

        lbRules.setDisplayedMnemonic('U');
        lbRules.setLabelFor(rulefilename);
        lbRules.setText("Rules:");
        getContentPane().add(lbRules, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 40, -1, -1));

        rulefilename.setNextFocusableComponent(btnBrowseRule);
        getContentPane().add(rulefilename, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 40, 280, -1));

        btnBrowseRule.setMnemonic('R');
        btnBrowseRule.setText("Browse...");
        btnBrowseRule.setNextFocusableComponent(filetype);
        getContentPane().add(btnBrowseRule, new org.netbeans.lib.awtextra.AbsoluteConstraints(330, 40, -1, -1));

        lbLocale.setDisplayedMnemonic('L');
        lbLocale.setLabelFor(locale);
        lbLocale.setText("Locale:");
        getContentPane().add(lbLocale, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 100, -1, -1));

        encoding.setNextFocusableComponent(segmentationSentence);
        getContentPane().add(encoding, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 130, 110, -1));

        lbEncoding.setDisplayedMnemonic('E');
        lbEncoding.setLabelFor(encoding);
        lbEncoding.setText("Encoding:");
        getContentPane().add(lbEncoding, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 130, -1, -1));

        locale.setNextFocusableComponent(encoding);
        getContentPane().add(locale, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 100, 110, -1));

        lbSegmentation.setText("Segmentation:");
        getContentPane().add(lbSegmentation, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 70, -1, -1));

        segmentationSentence.setMnemonic('S');
        segmentationSentence.setText("Sentence");
        buttonGroup1.add(segmentationSentence);
        segmentationSentence.setNextFocusableComponent(segmentationParagraph);
        getContentPane().add(segmentationSentence, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 70, -1, -1));

        segmentationParagraph.setMnemonic('P');
        segmentationParagraph.setText("Paragraph");
        buttonGroup1.add(segmentationParagraph);
        segmentationParagraph.setNextFocusableComponent(btnExtract);
        getContentPane().add(segmentationParagraph, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 90, -1, -1));

        btnExtract.setFont(new java.awt.Font("MS Sans Serif", 1, 11));
        btnExtract.setMnemonic('X');
        btnExtract.setText("Extract!");
        btnExtract.setNextFocusableComponent(filename);
        getContentPane().add(btnExtract, new org.netbeans.lib.awtextra.AbsoluteConstraints(330, 130, 80, -1));

        lbResult.setText("Extraction Result:");
        getContentPane().add(lbResult, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 180, 90, -1));

        btnViewGxml.setMnemonic('V');
        btnViewGxml.setText("View GXML");
        btnViewGxml.setEnabled(false);
        getContentPane().add(btnViewGxml, new org.netbeans.lib.awtextra.AbsoluteConstraints(320, 310, -1, -1));

        btnEditErrors.setMnemonic('D');
        btnEditErrors.setText("Edit Errors");
        btnEditErrors.setEnabled(false);
        getContentPane().add(btnEditErrors, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 310, -1, -1));

        result.setLineWrap(true);
        result.setWrapStyleWord(true);
        jScrollPane1.setViewportView(result);

        getContentPane().add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 200, 400, 100));

        mn_file.setMnemonic('F');
        mn_file.setText("File");
        mi_fileExit.setMnemonic('X');
        mi_fileExit.setText("Exit");
        mn_file.add(mi_fileExit);

        jMenuBar1.add(mn_file);

        mn_help.setMnemonic('H');
        mn_help.setText("Help");
        mi_helpHelp.setMnemonic('H');
        mi_helpHelp.setText("Help");
        mn_help.add(mi_helpHelp);

        mi_helpAbout.setMnemonic('A');
        mi_helpAbout.setText("About");
        mn_help.add(mi_helpAbout);

        jMenuBar1.add(mn_help);

        setJMenuBar(jMenuBar1);

        pack();
    }//GEN-END:initComponents
    
    /** Exit the Application */    
    /**
     * @param args the command line arguments
     */
    public static void main (String args[])
    {
        new Debex ().show ();
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    public javax.swing.JButton btnBrowseFile;
    public javax.swing.JButton btnBrowseRule;
    public javax.swing.JButton btnEditErrors;
    public javax.swing.JButton btnExtract;
    public javax.swing.JButton btnViewGxml;
    public javax.swing.ButtonGroup buttonGroup1;
    public javax.swing.JComboBox encoding;
    public javax.swing.JTextField filename;
    public javax.swing.JComboBox filetype;
    public javax.swing.JMenuBar jMenuBar1;
    public javax.swing.JScrollPane jScrollPane1;
    public javax.swing.JLabel lbEncoding;
    public javax.swing.JLabel lbFile;
    public javax.swing.JLabel lbLocale;
    public javax.swing.JLabel lbResult;
    public javax.swing.JLabel lbRules;
    public javax.swing.JLabel lbSegmentation;
    public javax.swing.JLabel lbType2;
    public javax.swing.JComboBox locale;
    public javax.swing.JMenuItem mi_fileExit;
    public javax.swing.JMenuItem mi_helpAbout;
    public javax.swing.JMenuItem mi_helpHelp;
    public javax.swing.JMenu mn_file;
    public javax.swing.JMenu mn_help;
    public javax.swing.JTextArea result;
    public javax.swing.JTextField rulefilename;
    public javax.swing.JRadioButton segmentationParagraph;
    public javax.swing.JRadioButton segmentationSentence;
    // End of variables declaration//GEN-END:variables
    
}
