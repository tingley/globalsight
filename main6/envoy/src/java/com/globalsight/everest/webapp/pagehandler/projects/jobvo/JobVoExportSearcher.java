package com.globalsight.everest.webapp.pagehandler.projects.jobvo;

public class JobVoExportSearcher extends JobVoStateSearcher {

	@Override
	protected String getStateSql() {
		return " AND (j.STATE = 'EXPORTED' or j.STATE = 'EXPORT_FAILED') ";
	}

}
