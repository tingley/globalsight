package com.globalsight.ui;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;

import org.apache.log4j.Logger;

import com.globalsight.util.Constants;
import com.globalsight.util2.ConfigureHelperV2;

public class AboutDialog extends JDialog
{

	private static final long serialVersionUID = 1314555L;

	static Logger log = Logger.getLogger(AboutDialog.class.getName());

	// start for animate
	JLabel label6;

	private String animate = "Welocalize.com";

	private String sign = " ";

	private int size = 80;

	private boolean wright = true;

	private int sleep_time = 100;

	private String font_name = "Arial";

	private int font_style = Font.BOLD;

	private int font_size = 18;

	// end for animate

	public AboutDialog(Frame p_owner)
	{
		super(p_owner, "About " + Constants.APP_FULL_NAME, true);
		initPanel();
		setSize(new Dimension(400, 300));
	}

	private void initPanel()
	{
		Container contentPane = this.getContentPane();
		contentPane.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		JLabel icon = new JLabel(new ImageIcon(Constants.GLOBALSIGHT_ICON));
		c.insets = new Insets(2, 2, 2, 2);
		c.gridx = 0;
		c.gridy = 0;
		contentPane.add(icon, c);

		JLabel label1 = new JLabel(Constants.APP_FULL_NAME, Label.RIGHT);
		c.gridx = 0;
		c.gridy = 1;
		contentPane.add(label1, c);

		JLabel label2 = new JLabel(Constants.APP_VERSION, Label.RIGHT);
		c.gridx = 0;
		c.gridy = 2;
		contentPane.add(label2, c);

		JLabel label3 = new JLabel(Constants.APP_VERSION_DATE, Label.RIGHT);
		c.gridx = 0;
		c.gridy = 3;
		contentPane.add(label3, c);

		String time = "1";
		try
		{
			time = ConfigureHelperV2.readRuncount();
		}
		catch (Exception e)
		{
			log.error("Statistic the times wrong.");
		}
		String useingTime = "Statistic: " + time;
		JLabel label4 = new JLabel(useingTime, Label.RIGHT);
		label4.setVisible(false);
		c.gridx = 0;
		c.gridy = 4;
		contentPane.add(label4, c);

		JLabel label5 = new JLabel("", Label.RIGHT);
		c.gridx = 0;
		c.gridy = 5;
		contentPane.add(label5, c);

		StringBuffer signs = new StringBuffer("");
		for (int i = 0; i < size; i++)
		{
			signs.append(sign);
		}
		label6 = new JLabel(animate + signs, Label.RIGHT);
		label6.setFont(new Font(font_name, font_style, font_size));
		label6.setForeground(Color.blue);
		c.gridx = 0;
		c.gridy = 6;
		// contentPane.add(label6, c);

		JButton okButton = new JButton("ok");
		okButton.setSize(14, 4);
		c.gridx = 0;
		c.gridy = 7;
		contentPane.add(okButton, c);

		okButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				setVisible(false);
			}
		});

		// amimate2R();
		label5.setText("");
	}

	private void amimateL2R()
	{
		Thread thread = new Thread()
		{
			public void run()
			{
				while (true)
				{
					String text = label6.getText();
					if (text.indexOf(animate) == 0)
					{
						StringBuffer signs = new StringBuffer("");
						for (int i = 0; i < size; i++)
						{
							signs.append(sign);
						}
						text = animate + signs;
						wright = true;
						// label5.setFont(new Font(font_name, font_style,
                        // font_size));
					}
					// label5.setFont(new Font(font_name, font_style,
                    // label5.getFont().getSize()+1));
					if (text.indexOf(animate) == size)
					{
						wright = false;
					}
					if (wright)
					{
						text = text.substring(0, text.length() - 1);
						text = sign + text;
					}
					else
					{
						text = text.substring(1);
						text = text + sign;
					}
					label6.setText(text);

					try
					{
						Thread.sleep(sleep_time);
					}
					catch (InterruptedException e)
					{
						// e.printStackTrace();
						// do nothing, only print the Exception in developing
					}
				}
			}
		};

		thread.start();
	}

	private void amimate2R()
	{
		Thread thread = new Thread()
		{
			public void run()
			{
				while (true)
				{
					String text = label6.getText();

					if (text.indexOf(animate) == size)
					{
						wright = false;
					}
					if (wright)
					{
						text = text.substring(0, text.length() - 1);
						text = sign + text;
					}
					else
					{
						StringBuffer signs = new StringBuffer("");
						for (int i = 0; i < size; i++)
						{
							signs.append(sign);
						}
						text = animate + signs;
						wright = true;
					}
					label6.setText(text);

					try
					{
						Thread.sleep(sleep_time);
					}
					catch (InterruptedException e)
					{
						// e.printStackTrace();
						// do nothing, only print the Exception in developing
					}
				}
			}
		};

		thread.start();
	}
}
