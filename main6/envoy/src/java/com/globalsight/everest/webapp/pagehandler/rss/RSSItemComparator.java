package com.globalsight.everest.webapp.pagehandler.rss;

import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;

public class RSSItemComparator implements Comparator {

	@Override
	public int compare(Object o1, Object o2) {
		Item i1 = (Item)o1;
		Item i2 = (Item)o2;
		Calendar cal1 = i1.getPublishedDate();
		Calendar cal2 = i2.getPublishedDate();
		if (cal1 == null || cal2 == null) {
			return i1.getTitle().toLowerCase().compareTo(i2.getTitle().toLowerCase());
		}
		
		if (cal1.before(cal2))
			return 1;
		else if (cal1.after(cal2))
			return -1;
		else
			return 0;
	}
}
