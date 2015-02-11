package com.globalsight.entity;

public class Host
{
	private String name = null;

	private int port = 80;

	public static String separator = ":";

	public Host(String p_name, int p_port)
	{
		name = p_name.trim();
		port = p_port;
	}

	public Host(String p_name, String p_port)
	{
		name = p_name.trim();
		port = Integer.parseInt(p_port.trim());
	}

	public String getName()
	{
		return name;
	}

	public int getPort()
	{
		return port;
	}

	public String toString()
	{
		return name + separator + port;
	}

	public String getFullName()
	{
		return name + separator + port;
	}

	/**
     * return true if hostname and host port are all same
     */
	public boolean equals(Object anObj)
	{

		if (anObj == this)
		{
			return true;
		}

		if (anObj instanceof Host)
		{
			Host h = (Host) anObj;
			return (h.name != null && this.name != null && h.getFullName()
					.equals(this.getFullName()));
		}

		return false;
	}

	public String getPortString()
	{
		return String.valueOf(port);
	}
}
