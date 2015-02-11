package com.globalsight.util;

import java.util.TimerTask;

public class Task extends TimerTask {

	private String token;
	
	public Task(String token)
	{
		this.token = token;
	}

	@Override
	public void run() {
		LoginUtil.TOKENS.remove(token);
	}

}
