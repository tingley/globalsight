/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package testUI;
import javax.swing.*;
import java.awt.*;
import util.Account;

/**
 *
 * @author likun
 */
public class AccountCellRenderer extends JLabel implements ListCellRenderer{
    public Component getListCellRendererComponent(
            JList list,
            Object value,
            int index,
            boolean isSelected,
            boolean cellHasFocus){
        Account account = (Account) value;
        setText(account.getUserName()+"@"+account.getHost()+":"+account.getPort()+":https("+account.getHttps()+")");
        if(isSelected){
            setBackground(Color.WHITE);
            setForeground(Color.BLUE);
        }
        else{
            setBackground(Color.WHITE);
            setForeground(Color.BLACK);
        }
        return this;
    }
}
