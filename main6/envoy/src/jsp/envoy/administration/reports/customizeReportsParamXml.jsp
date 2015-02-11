<%@ page contentType="text/xml; charset=UTF-8"
         import="com.globalsight.everest.webapp.pagehandler.administration.reports.customize.param.Param"
         session="true"
%><?xml version="1.0" encoding="UTF-8"?>
<jobInfoParamXml>
<category id="<%=Param.JOB_ID%>" >
</category>
<category id="<%=Param.JOB_DETAIL%>" >
    <param id="<%=Param.JOB_NAME%>" />
    <param id="<%=Param.PROJECT_DESCRIPTION%>" />
    <param id="<%=Param.JOB_CREATIONDATE%>" />
    <param id="<%=Param.PAGE_COUNT%>" />
    <param id="<%=Param.JOB_PRIORITY%>" />
    <param id="<%=Param.JOB_PROJECT_NAME%>" />
    <param id="<%=Param.JOB_PROJECT_MANAGER%>" />
    <param id="<%=Param.JOB_SOURCE_LOCALE%>" />
    <param id="<%=Param.JOB_TARGET_LOCALE%>" />
    <param id="<%=Param.ESTIMATED_COST%>" />
    <param id="<%=Param.ESTIMATED_BILLING_CHARGES%>" />
    <param id="<%=Param.LOCALIZATION_PROFILE%>" />
    <param id="<%=Param.FILE_PROFILE%>" />
</category>
<category id="<%=Param.STATUS%>" >
    <param id="<%=Param.JOB_STATUS%>" />
    <param id="<%=Param.WORKFLOW_STATUS%>" />
    <param id="<%=Param.ESTIMATED_JOB_COMPLETION%>" />
    <param id="<%=Param.ACTUAL_JOB_COMPLETION%>" />
</category>
<category id="<%=Param.TM_MATCHES%>" >
    <param id="<%=Param.TM_MATCHES_WORD_COUNTS%>" >
        <param id="<%=Param.TM_MATCHES_WORD_COUNTS_INTERNAL_REPS%>" />
        <param id="<%=Param.TM_MATCHES_WORD_COUNTS_EXACT_MATCHES%>" />
        <param id="<%=Param.TM_MATCHES_WORD_COUNTS_IN_CONTEXT_MATCHES%>" />
        <param id="<%=Param.TM_MATCHES_WORD_COUNTS_FUZZY_MATCHES%>" />
        <param id="<%=Param.TM_MATCHES_WORD_COUNTS_NEW_WORDS%>" />
        <param id="<%=Param.TM_MATCHES_WORD_COUNTS_TOTAL%>" />
    </param>
    <param id="<%=Param.TM_MATCHES_INVOICE%>" >
        <param id="<%=Param.TM_MATCHES_INVOICE_INTERNAL_REPS%>" />
        <param id="<%=Param.TM_MATCHES_INVOICE_EXACT_MATCHES%>" />
        <param id="<%=Param.TM_MATCHES_INVOICE_IN_CONTEXT_MATCHES%>" />
        <param id="<%=Param.TM_MATCHES_INVOICE_FUZZY_MATCHES%>" />
        <param id="<%=Param.TM_MATCHES_INVOICE_NEW_WORDS%>" />
        <param id="<%=Param.TM_MATCHES_INVOICE_JOB_TOTAL%>" />
    </param>
</category>
<category id="<%=Param.TRADOS_MATCHES%>" >
    <param id="<%=Param.TRADOS_MATCHES_WORD_COUNTS%>" >
        <param id="<%=Param.TRADOS_MATCHES_WORD_COUNTS_PER100%>" />
        <param id="<%=Param.TRADOS_MATCHES_WORD_COUNTS_PER95%>" />
        <param id="<%=Param.TRADOS_MATCHES_WORD_COUNTS_PER85%>" />
        <param id="<%=Param.TRADOS_MATCHES_WORD_COUNTS_PER75%>" />
        <param id="<%=Param.TRADOS_MATCHES_WORD_COUNTS_NOMATCH%>" />
        <param id="<%=Param.TRADOS_MATCHES_WORD_COUNTS_REPETITION%>" />
        <param id="<%=Param.TRADOS_MATCHES_WORD_COUNTS_PERINCONTEXT%>" />
        <param id="<%=Param.TRADOS_MATCHES_WORD_COUNTS_TOTAL%>" />
    </param>
    <param id="<%=Param.TRADOS_MATCHES_INVOICE%>" >
        <param id="<%=Param.TRADOS_MATCHES_INVOICE_PER100%>" />
        <param id="<%=Param.TRADOS_MATCHES_INVOICE_PER95%>" />
        <param id="<%=Param.TRADOS_MATCHES_INVOICE_PER85%>" />
        <param id="<%=Param.TRADOS_MATCHES_INVOICE_PER75%>" />
        <param id="<%=Param.TRADOS_MATCHES_INVOICE_NOMATCH%>" />
        <param id="<%=Param.TRADOS_MATCHES_INVOICE_REPETITION%>" />
        <param id="<%=Param.TRADOS_MATCHES_INVOICE_PERINCONTEXT%>" />
        <param id="<%=Param.TRADOS_MATCHES_INVOICE_JOB_TOTAL%>" />
    </param>
</category>
</jobInfoParamXml>