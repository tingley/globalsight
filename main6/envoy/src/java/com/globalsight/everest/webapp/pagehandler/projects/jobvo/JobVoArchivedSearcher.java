package com.globalsight.everest.webapp.pagehandler.projects.jobvo;

public class JobVoArchivedSearcher extends JobVoStateSearcher {

	@Override
	protected String getStateSql() {
		return " AND j.STATE = 'ARCHIVED' ";
	}

}
