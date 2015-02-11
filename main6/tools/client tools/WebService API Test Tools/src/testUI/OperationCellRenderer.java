/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package testUI;
import javax.swing.*;
import java.awt.*;
import util.Operation;
/**
 *
 * @author likun
 */
public class OperationCellRenderer extends JLabel implements ListCellRenderer{
    public Component getListCellRendererComponent(
            JList list,
            Object value,
            int index,
            boolean isSelected,
            boolean cellHasFocus){
        Operation operation = (Operation) value;
        setText(operation.getName());
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
