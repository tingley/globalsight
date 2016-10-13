package com.globalsight.everest.webapp.pagehandler.projects.jobvo;

public class JobVoPendingSearcher extends JobVoStateSearcher {

	@Override
	protected String getStateSql() {
		return " AND j.STATE in ( 'PENDING', 'BATCH_RESERVED', 'ADDING_FILES', 'IMPORT_FAILED' ) ";
	}

}
