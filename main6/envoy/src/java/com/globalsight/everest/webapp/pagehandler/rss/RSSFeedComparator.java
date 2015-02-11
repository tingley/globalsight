package com.globalsight.everest.webapp.pagehandler.rss;

import java.util.Locale;

import com.globalsight.everest.util.comparator.StringComparator;


public class RSSFeedComparator extends StringComparator {

	public RSSFeedComparator(Locale p_locale) {
		super(p_locale);
	}

	@Override
	public int compare(Object o1, Object o2) {
		Feed i1 = (Feed)o1;
		Feed i2 = (Feed)o2;
		if (i1 == null || i2 == null)
			return 0;
		
		return this.compareStrings(i1.getChannelTitle(),i2.getChannelTitle());
	}
}
