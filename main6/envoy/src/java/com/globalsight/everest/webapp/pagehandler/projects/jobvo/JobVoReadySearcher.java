package com.globalsight.everest.webapp.pagehandler.projects.jobvo;

public class JobVoReadySearcher extends JobVoStateSearcher {

	@Override
	protected String getStateSql() {
		return " AND j.STATE = 'READY_TO_BE_DISPATCHED' ";
	}

}
