package com.globalsight.ui.attribute;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class DateChooserJButton extends JButton
{
    private static final long serialVersionUID = 3060639306140351470L;
    private static final String DEFAULT_FORMAT = "MM/dd/yyyy HH:mm:ss";
    private String format;
    private final String TITEL = "Select Time";
    private DateChooser dateChooser = null;
    private String preLabel = "";
    
    private List<DateUpdateLister> listers = new ArrayList<DateUpdateLister>();
    
    private void fireDateUpdate()
    {
        for (DateUpdateLister lister : listers)
        {
            lister.update(getDate());
        }
    }
    
    public void addDateListers(DateUpdateLister lister)
    {
        listers.add(lister);
    }

    public DateChooserJButton()
    {
        this(getNowDate());
    }

    public DateChooserJButton(SimpleDateFormat df, String dateString)
    {
        this();
        setText(df, dateString);
    }

    public DateChooserJButton(Date date)
    {
        this("", date);
    }

    public DateChooserJButton(String preLabel, Date date)
    {
        if (preLabel != null)
            this.preLabel = preLabel;
        setDate(date);
        setBorder(null);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        super.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                if (dateChooser == null)
                    dateChooser = new DateChooser();
                Point p = getLocationOnScreen();
                p.y = p.y + 30;
                dateChooser.showDateChooser(p);
            }
        });
    }

    private static Date getNowDate()
    {
        return Calendar.getInstance().getTime();
    }

    private SimpleDateFormat getDefaultDateFormat()
    {
        if (format == null)
        {
            format = DEFAULT_FORMAT;
        }
        return new SimpleDateFormat(format);
    }

    public void setText(String s)
    {
        Date date;
        try
        {
            date = getDefaultDateFormat().parse(s);
        }
        catch (ParseException e)
        {
            date = getNowDate();
        }
        setDate(date);
    }

    public void setText(SimpleDateFormat df, String s)
    {
        Date date;
        try
        {
            date = df.parse(s);
        }
        catch (ParseException e)
        {
            date = getNowDate();
        }
        setDate(date);
    }
    
    private String getDateLabel(Date date)
    {
        if (date == null)
            return "";
        
        return getDefaultDateFormat().format(date);
    }

    public void setDate(Date date)
    {
        super.setText(getDateLabel(date));
        fireDateUpdate();
    }

    public Date getDate()
    {
        String dateString = getText().substring(preLabel.length());
        try
        {
            return getDefaultDateFormat().parse(dateString);
        }
        catch (ParseException e)
        {
            return getNowDate();
        }

    }

    public void addActionListener(ActionListener listener)
    {
    }

    private class DateChooser extends JPanel implements ActionListener,
            ChangeListener
    {
        int startYear = 1980; 
        int lastYear = 2050;
        int width = 500;
        int height = 350;

        Color backGroundColor = Color.gray; 
        Color palletTableColor = Color.white; 
        Color todayBackColor = Color.orange;
        Color weekFontColor = Color.blue;
        Color dateFontColor = Color.black;
        Color weekendFontColor = Color.red;

        Color controlLineColor = Color.pink;
        Color controlTextColor = Color.white;

        Color rbFontColor = Color.white;
        Color rbBorderColor = Color.red;
        Color rbButtonColor = Color.pink;
        Color rbBtFontColor = Color.red;

        JDialog dialog;
        JSpinner yearSpin;
        JSpinner monthSpin;
        JSpinner hourSpin;
        JSpinner minuteSpin;
        JSpinner secSpin;
        JTextField timeField;
        JButton[][] daysButton = new JButton[6][7];

        DateChooser()
        {
            setLayout(new BorderLayout());
            setBorder(new LineBorder(backGroundColor, 2));
            setBackground(backGroundColor);

            add(createYearAndMonthPanal(), BorderLayout.NORTH);
            add(createWeekAndDayPanal(), BorderLayout.CENTER);
        }

        private JPanel createYearAndMonthPanal()
        {
            Calendar c = getCalendar();
            int currentYear = c.get(Calendar.YEAR);
            int currentMonth = c.get(Calendar.MONTH) + 1;
            int currentHour = c.get(Calendar.HOUR_OF_DAY);
            int currentMin = c.get(Calendar.MINUTE);
            int currentSec = c.get(Calendar.SECOND);

            JPanel result = new JPanel();
            result.setLayout(new FlowLayout());
            result.setBackground(controlLineColor);

            monthSpin = new JSpinner(new SpinnerNumberModel(currentMonth, 1,
                    12, 1));
            monthSpin.setPreferredSize(new Dimension(35, 20));
            monthSpin.setName("Month");
            monthSpin.addChangeListener(this);
            result.add(monthSpin);
            
            yearSpin = new JSpinner(new SpinnerNumberModel(currentYear,
                    startYear, lastYear, 1));
            yearSpin.setPreferredSize(new Dimension(48, 20));
            yearSpin.setName("Year");
            yearSpin.setEditor(new JSpinner.NumberEditor(yearSpin, "####"));
            yearSpin.addChangeListener(this);
            result.add(yearSpin);

            JLabel yearLabel = new JLabel("Month/Year");
            yearLabel.setForeground(controlTextColor);
            result.add(yearLabel);

            hourSpin = new JSpinner(new SpinnerNumberModel(currentHour, 0, 23,
                    1));
            hourSpin.setPreferredSize(new Dimension(35, 20));
            hourSpin.setName("Hour");
            hourSpin.addChangeListener(this);
            result.add(hourSpin);
            
            JLabel hourLabel = new JLabel(":");
            hourLabel.setForeground(controlTextColor);
            result.add(hourLabel);
            
            minuteSpin = new JSpinner(new SpinnerNumberModel(currentMin, 0, 59,
                    1));
            minuteSpin.setPreferredSize(new Dimension(35, 20));
            minuteSpin.setName("Minute");
            minuteSpin.addChangeListener(this);
            result.add(minuteSpin);
            
            JLabel minuteLabel = new JLabel(":");
            hourLabel.setForeground(controlTextColor);
            result.add(minuteLabel);
            
            secSpin = new JSpinner(new SpinnerNumberModel(currentSec, 0, 59,
                    1));
            secSpin.setPreferredSize(new Dimension(35, 20));
            secSpin.setName("sec");
            secSpin.addChangeListener(this);
            result.add(secSpin);
            
            JLabel timeLable = new JLabel("HH:mm:ss");
            timeLable.setForeground(controlTextColor);
            result.add(timeLable);

            return result;
        }
        
        private void updateDayColor()
        {
            for (int i = 0; i < 6; i++)
            {
                for (int j = 0; j < 7; j++)
                {
                    JButton numberButton = daysButton[i][j];
                    numberButton.setBackground(palletTableColor);
                    numberButton.setForeground(dateFontColor);
                    if (j == 0 || j == 6)
                        numberButton.setForeground(weekendFontColor);
                    else
                        numberButton.setForeground(dateFontColor);
                    daysButton[i][j] = numberButton;
                }
             }
        }

        private JPanel createWeekAndDayPanal()
        {
            String colname[] =
            {
                "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"
            };
            JPanel result = new JPanel();
            result.setFont(new Font("\u5b8b\u4f53", 0, 16));
            result.setLayout(new GridLayout(7, 7));
            result.setBackground(Color.white);
            JLabel cell;

            for (int i = 0; i < 7; i++)
            {
                cell = new JLabel(colname[i]);
                cell.setHorizontalAlignment(JLabel.CENTER);
                if (i == 0 || i == 6)
                    cell.setForeground(weekendFontColor);
                else
                    cell.setForeground(weekFontColor);
                result.add(cell);
            }

            int actionCommandId = 0;
            for (int i = 0; i < 6; i++)
                for (int j = 0; j < 7; j++)
                {
                    JButton numberButton = new JButton();
                    numberButton.setBorder(null);
                    numberButton.setHorizontalAlignment(SwingConstants.CENTER);
                    numberButton.setActionCommand(String
                            .valueOf(actionCommandId));
                    numberButton.addActionListener(this);
                    daysButton[i][j] = numberButton;
                    result.add(numberButton);
                    actionCommandId++;
                }

            updateDayColor();
            return result;
        }

        private JDialog createDialog(Component owner)
        {
            JDialog result;
            if (owner instanceof Frame)
            {
                result = new JDialog((Frame) owner, TITEL, true);
            }
            else
            {
                result = new JDialog((JDialog) owner, TITEL, true);
            }

            result.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
            result.getContentPane().add(this, BorderLayout.CENTER);
            result.pack();
            result.setSize(width, height);
            return result;
        }

        void showDateChooser(Point position)
        {
            Component owner = SwingUtilities
                    .getWindowAncestor(DateChooserJButton.this);
            if (dialog == null || dialog.getOwner() != owner)
                dialog = createDialog(owner);
            dialog.setLocation(getAppropriateLocation(owner, position));
            flushWeekAndDay();
            dialog.setVisible(true);
        }

        Point getAppropriateLocation(Component owner, Point position)
        {
            Point result = new Point(position);
            Point p = owner.getLocation();
            int offsetX = (position.x + width) - (p.x + owner.getWidth());
            int offsetY = (position.y + height) - (p.y + owner.getHeight());

            if (offsetX > 0)
            {
                result.x -= offsetX;
            }

            if (offsetY > 0)
            {
                result.y -= offsetY;
            }

            return result;

        }

        private Calendar getCalendar()
        {
            Calendar result = Calendar.getInstance();
            result.setTime(getDate());
            return result;
        }

        private int getSelectedYear()
        {
            return ((Integer) yearSpin.getValue()).intValue();
        }

        private int getSelectedMonth()
        {
            return ((Integer) monthSpin.getValue()).intValue();
        }

        private int getSelectedHour()
        {
            return ((Integer) hourSpin.getValue()).intValue();
        }
        
        private int getSelectedMinute()
        {
            return ((Integer) minuteSpin.getValue()).intValue();
        }
        
        private int getSelectedSec()
        {
            return ((Integer) secSpin.getValue()).intValue();
        }

        private void dayColorUpdate(boolean isOldDay)
        {
            updateDayColor();
            
            Calendar c = getCalendar();
            int day = c.get(Calendar.DAY_OF_MONTH);
            c.set(Calendar.DAY_OF_MONTH, 1);
            int actionCommandId = day - 2 + c.get(Calendar.DAY_OF_WEEK);
            int i = actionCommandId / 7;
            int j = actionCommandId % 7;
            if (isOldDay)
                daysButton[i][j].setForeground(dateFontColor);
            else
                daysButton[i][j].setForeground(todayBackColor);
        }

        private void flushWeekAndDay()
        {
            Calendar c = getCalendar();
            c.set(Calendar.DAY_OF_MONTH, 1);
            int maxDayNo = c.getActualMaximum(Calendar.DAY_OF_MONTH);
            int dayNo = 2 - c.get(Calendar.DAY_OF_WEEK);
            for (int i = 0; i < 6; i++)
            {
                for (int j = 0; j < 7; j++)
                {
                    String s = "";
                    if (dayNo >= 1 && dayNo <= maxDayNo)
                        s = String.valueOf(dayNo);
                    daysButton[i][j].setText(s);
                    dayNo++;
                }
            }
            dayColorUpdate(false);
        }

        public void stateChanged(ChangeEvent e)
        {
            JSpinner source = (JSpinner) e.getSource();
            Calendar c = getCalendar();
            if (source.getName().equals("Hour"))
            {
                c.set(Calendar.HOUR_OF_DAY, getSelectedHour());
                setDate(c.getTime());
                return;
            }
            
            if (source.getName().equals("Minute"))
            {
                c.set(Calendar.MINUTE, getSelectedMinute());
                setDate(c.getTime());
                return;
            }
            
            if (source.getName().equals("sec"))
            {
                c.set(Calendar.SECOND, getSelectedSec());
                setDate(c.getTime());
                return;
            }

            if (source.getName().equals("Year"))
                c.set(Calendar.YEAR, getSelectedYear());
            else
                // (source.getName().equals("Month"))
                c.set(Calendar.MONTH, getSelectedMonth() - 1);
            
            dayColorUpdate(true);
            setDate(c.getTime());
            flushWeekAndDay();
        }

        public void actionPerformed(ActionEvent e)
        {
            JButton source = (JButton) e.getSource();
            if (source.getText().length() == 0)
                return;
            
            source.setForeground(todayBackColor);
            int newDay = Integer.parseInt(source.getText());
            Calendar c = getCalendar();
            c.set(Calendar.DAY_OF_MONTH, newDay);
            setDate(c.getTime());
            
            dayColorUpdate(true);
            dialog.setVisible(false);
        }
    }

    public String getFormat()
    {
        return format;
    }

    public void setFormat(String format)
    {
        this.format = format;
    }

}
