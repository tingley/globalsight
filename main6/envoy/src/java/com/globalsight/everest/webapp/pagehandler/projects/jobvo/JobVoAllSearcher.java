package com.globalsight.everest.webapp.pagehandler.projects.jobvo;


public class JobVoAllSearcher extends JobVoStateSearcher {

	@Override
	protected String getStateSql() {
		return " AND j.STATE in ('PENDING', 'BATCH_RESERVED', 'READY_TO_BE_DISPATCHED', "
				+ "'DISPATCHED', 'LOCALIZED', 'EXPORTING', 'SKIPPING', 'EXPORTED', "
				+ "'EXPORT_FAILED', 'ARCHIVED', 'ADDING_FILES', 'PROCESSING', "
				+ "'CALCULATING-WORD-COUNTS','IMPORT_FAILED', 'UPLOADING', "
				+ "'IN_QUEUE', 'EXTRACTING', 'LEVERAGING') ";
	}

}
