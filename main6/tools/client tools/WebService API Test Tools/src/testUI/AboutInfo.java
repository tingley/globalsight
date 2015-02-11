package testUI;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 *
 * @author Welocalize
 */
public class AboutInfo extends javax.swing.JDialog {

    /** Creates new form EixtDialog */
    public AboutInfo(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
    }
    
    private void initComponents() {

        nameLabel = new javax.swing.JLabel();
        versionLabel = new javax.swing.JLabel();
        authorLabel = new javax.swing.JLabel();
        textPanel = new javax.swing.JPanel();
        buttonPanel = new javax.swing.JPanel();
        okButton = new javax.swing.JButton();
        okButton.setText("OK");
        okButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				setVisible(false);
			}
        });

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setLocationByPlatform(true);
        setName("Form"); // NOI18N
        setResizable(false);
        

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(testtool.TestToolApp.class).getContext().getResourceMap(Desktop.class);
        nameLabel.setText(resourceMap.getString("helpInfo.name.text")); 
        authorLabel.setText(resourceMap.getString("helpInfo.author.text")); 
        versionLabel.setText(resourceMap.getString("helpInfo.version.text"));
        
        textPanel.add(nameLabel);
        textPanel.add(versionLabel);
        textPanel.add(authorLabel);
        
        getContentPane().add(textPanel, java.awt.BorderLayout.CENTER);
        
        buttonPanel.add(okButton);
        getContentPane().add(buttonPanel, java.awt.BorderLayout.SOUTH);

        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screenSize.width-400)/2, (screenSize.height-300)/2, 230, 150);
    }// </editor-fold>//GEN-END:initComponents

    /**
    * @param args the command line arguments
    */

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel nameLabel;
    private javax.swing.JLabel versionLabel;
    private javax.swing.JLabel authorLabel;
    private javax.swing.JPanel textPanel;
    private javax.swing.JButton okButton;
    private javax.swing.JPanel buttonPanel;

}
