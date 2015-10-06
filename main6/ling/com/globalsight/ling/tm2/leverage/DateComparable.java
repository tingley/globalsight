package com.globalsight.ling.tm2.leverage;

import java.sql.Timestamp;

public interface DateComparable 
{
	public Timestamp getModifyDate();

	public Timestamp getLastUsageDate();
}
