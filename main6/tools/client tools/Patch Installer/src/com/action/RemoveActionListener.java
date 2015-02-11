package com.action;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import com.demo.Hotfix;

public class RemoveActionListener implements ActionListener {

	private List<Hotfix> hs = null;

	public RemoveActionListener(List<Hotfix> hs) {
		super();
		this.hs = hs;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (hs != null)
		{
			for (Hotfix h : hs)
			{
				h.remove();
			}
		}
	}
}
