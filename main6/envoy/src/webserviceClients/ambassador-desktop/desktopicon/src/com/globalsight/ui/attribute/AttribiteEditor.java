package com.globalsight.ui.attribute;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractCellEditor;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellEditor;

import com.globalsight.ui.attribute.vo.DateJobAttributeVo;
import com.globalsight.ui.attribute.vo.FileJobAttributeVo;
import com.globalsight.ui.attribute.vo.FloatJobAttributeVo;
import com.globalsight.ui.attribute.vo.IntJobAttributeVo;
import com.globalsight.ui.attribute.vo.JobAttributeVo;
import com.globalsight.ui.attribute.vo.ListJobAttributeVo;
import com.globalsight.ui.attribute.vo.TextJobAttributeVo;
import com.globalsight.util.Constants;
import com.globalsight.util.SwingHelper;

public class AttribiteEditor extends AbstractCellEditor implements
        TableCellEditor, ActionListener
{
    private static final long serialVersionUID = 3183874486060373420L;
    private JList jList;
    private JTextField jTextField;
    private DateChooserJButton dateButton;
    private JobAttributeVo attribute;
    private Object currentObject;
    private Font font;
    private JPopupMenu menu;

    public Component getTableCellEditorComponent(JTable table, Object value,
            boolean isSelected, int row, int column)
    {
        font = table.getFont();
        if (value instanceof ListJobAttributeVo)
        {
            ListJobAttributeVo listAttribute = (ListJobAttributeVo) value;
            return getListEditor(listAttribute);
        }
        else if (value instanceof DateJobAttributeVo)
        {
            DateJobAttributeVo attribute = (DateJobAttributeVo) value;
            return getDateEditor(attribute);
        }
        else if (value instanceof IntJobAttributeVo)
        {
            IntJobAttributeVo attribute = (IntJobAttributeVo) value;
            return getIntEditor(attribute);
        }
        else if (value instanceof FloatJobAttributeVo)
        {
            FloatJobAttributeVo attribute = (FloatJobAttributeVo) value;
            return getFloatEditor(attribute);
        }
        else if (value instanceof FileJobAttributeVo)
        {
            FileJobAttributeVo attribute = (FileJobAttributeVo) value;
            return getFileEditor(attribute);
        }
        else if (value instanceof TextJobAttributeVo)
        {
            TextJobAttributeVo attribute = (TextJobAttributeVo) value;
            return getTextEditor(attribute);
        }

        return jList;
    }

    private JComponent getListEditor(ListJobAttributeVo listAttribute)
    {
        attribute = listAttribute;

        DefaultListModel model = new DefaultListModel();
        List<Integer> selectOptionIndexs = new ArrayList<Integer>();
        List<String> selectedOptions = listAttribute.getSelectedOptions();
        int i = 0;
        for (String option : listAttribute.getOptions())
        {
            model.addElement(option);
            if (selectedOptions != null && selectedOptions.contains(option))
            {
                selectOptionIndexs.add(i);
            }

            i++;
        }

        jList = new JList(model);
        jList.setFont(font);

        if (!listAttribute.isMultiple())
        {
            jList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        }

        int[] indexs = new int[selectOptionIndexs.size()];
        for (int j = 0; j < selectOptionIndexs.size(); j++)
        {
            indexs[j] = selectOptionIndexs.get(j);
        }

        jList.setSelectedIndices(indexs);

        jList.addListSelectionListener(new ListSelectionListener()
        {
            public void valueChanged(ListSelectionEvent e)
            {
                ListJobAttributeVo listAttribute = (ListJobAttributeVo) attribute;
                listAttribute.setSelectedOptions(jList
                        .getSelectedIndices());
                if (!listAttribute.isMultiple())
                {
                    menu.setVisible(false);
                    stopCellEditing();
                }
            }
        });

        JLabel label = new JLabel();
        label.setFont(font);
        label.setText(listAttribute.getLabel());
        label.addMouseListener(new MouseListener()
        {

            public void mouseClicked(MouseEvent e)
            {

            }

            public void mouseEntered(MouseEvent e)
            {
                JLabel label = (JLabel) e.getSource();
                menu = new JPopupMenu();
                JScrollPane pane = new JScrollPane();
                pane.setViewportView(jList);
                pane.setBorder(null);
                menu.setPreferredSize(new Dimension(label.getSize().width, 80));
                menu.setLayout(new BorderLayout());
                menu.add(pane, BorderLayout.CENTER);
                menu.show(label, 0, 19);
            }

            public void mouseExited(MouseEvent e)
            {
                stopCellEditing();
            }

            public void mousePressed(MouseEvent e)
            {
            }

            public void mouseReleased(MouseEvent e)
            {
            }
        });

        return label;
    }

    private JComponent getFileEditor(FileJobAttributeVo fileAttribute)
    {
        attribute = fileAttribute;

        JLabel label = new JLabel();
        label.setFont(font);
        label.setText(fileAttribute.getLabel());
        label.addMouseListener(new MouseListener()
        {
            public void mouseClicked(MouseEvent e)
            {

            }

            public void mouseEntered(MouseEvent e)
            {
                FileUpdateDialog dialog = new FileUpdateDialog(SwingHelper
                        .getMainFrame(), (FileJobAttributeVo) attribute);
                dialog.addFileUpdateLister(new FileUpdateLister()
                {
                    public void update(List<String> files)
                    {
                        Set<String> names = new HashSet<String>();
                        for (String file : files)
                        {
                            File f = new File(file);
                            names.add(f.getName());
                        }

                        if (names.size() != files.size())
                        {
                            throw new IllegalArgumentException(
                                    Constants.MSG_FILE_NAME_REPEAT);
                        }

                        FileJobAttributeVo fileAttribute = (FileJobAttributeVo) attribute;
                        fileAttribute.setFiles(files);
                        stopCellEditing();
                    }
                });

                dialog.setVisible(true);
            }

            public void mouseExited(MouseEvent e)
            {
                stopCellEditing();
            }

            public void mousePressed(MouseEvent e)
            {
            }

            public void mouseReleased(MouseEvent e)
            {
            }
        });

        return label;
    }

    private JComponent getDateEditor(DateJobAttributeVo dateAttribute)
    {
        attribute = dateAttribute;
        dateButton = new DateChooserJButton(dateAttribute.getValue());
        dateButton.setBorderPainted(false);
        dateButton.setFont(font);
        dateButton.addDateListers(new DateUpdateLister()
        {
            public void update(Date date)
            {
                DateJobAttributeVo dateVo = (DateJobAttributeVo) attribute;
                dateVo.setValue(date);
            }
        });

        return dateButton;
    }

    private JComponent getIntEditor(IntJobAttributeVo intAttribute)
    {
        IntTextField field = new IntTextField();
        field.setText(intAttribute.getLabel());
        field.setIntAttribute(intAttribute);

        field.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                IntTextField field = (IntTextField) e.getSource();
                String value = field.getText();
                IntJobAttributeVo intVo = field.getIntAttribute();
                intVo.setValue(intVo.convertedToInteger(value));
                stopCellEditing();
            }
        });

        field.addFocusListener(new FocusListener()
        {
            public void focusGained(FocusEvent e)
            {
                // Do nothing.
            }

            public void focusLost(FocusEvent e)
            {
                IntTextField field = (IntTextField) e.getSource();
                String value = field.getText();
                IntJobAttributeVo intVo = field.getIntAttribute();
                intVo.setValue(intVo.convertedToInteger(value));
            }
        });

        return field;
    }

    private JComponent getFloatEditor(FloatJobAttributeVo floatAttribute)
    {
        FloatTextField field = new FloatTextField();
        field.setText(floatAttribute.getLabel());
        field.setFloatAttribute(floatAttribute);

        field.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                FloatTextField field = (FloatTextField) e.getSource();
                String value = field.getText();
                FloatJobAttributeVo floatVo = field.getFloatAttribute();
                floatVo.setValue(floatVo.convertedToFloat(value));
                stopCellEditing();
            }
        });

        field.addFocusListener(new FocusListener()
        {
            public void focusGained(FocusEvent e)
            {
                // Do nothing.
            }

            public void focusLost(FocusEvent e)
            {
                FloatTextField field = (FloatTextField) e.getSource();
                String value = field.getText();
                FloatJobAttributeVo floatVo = field.getFloatAttribute();
                floatVo.setValue(floatVo.convertedToFloat(value));
            }
        });

        return field;
    }

    private JComponent getTextEditor(TextJobAttributeVo textAttribute)
    {
        StringTextField field = new StringTextField();
        field.setText(textAttribute.getLabel());
        field.setTextAttribute(textAttribute);

        field.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                StringTextField field = (StringTextField) e.getSource();
                String value = field.getText();
                TextJobAttributeVo textVo = field.getTextAttribute();
                textVo.setValue(textVo.convertedToText(value));
                stopCellEditing();
            }
        });

        field.addFocusListener(new FocusListener()
        {
            public void focusGained(FocusEvent e)
            {
                // Do nothing.
            }

            public void focusLost(FocusEvent e)
            {
                StringTextField field = (StringTextField) e.getSource();
                String value = field.getText();
                TextJobAttributeVo textVo = field.getTextAttribute();
                textVo.setValue(textVo.convertedToText(value));
            }
        });

        return field;
    }

    public Object getCellEditorValue()
    {
        return currentObject;
    }

    public void actionPerformed(ActionEvent e)
    {
        // TODO Auto-generated method stub

    }

}