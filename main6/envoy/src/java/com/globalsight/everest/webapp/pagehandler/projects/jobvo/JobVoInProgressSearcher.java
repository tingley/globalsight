package com.globalsight.everest.webapp.pagehandler.projects.jobvo;

public class JobVoInProgressSearcher extends JobVoStateSearcher {

	@Override
	protected String getStateSql() {
		return " AND j.STATE = 'DISPATCHED' ";
	}

}
