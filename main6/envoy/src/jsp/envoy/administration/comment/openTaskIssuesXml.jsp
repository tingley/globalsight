<%@ page contentType="text/xml; charset=UTF-8"
    import="com.globalsight.everest.servlet.util.ServerProxy,
    com.globalsight.everest.comment.Issue,
    com.globalsight.util.edit.EditUtil,
    com.globalsight.everest.page.TargetPage,
    org.apache.log4j.Logger,
    java.util.HashMap,
    java.util.ArrayList"
    session="true"
%><%!
private static final Logger CATEGORY =
        Logger.getLogger("openTaskIssuesXml.jsp");
%><% 
String[] targetPageIds = request.getParameterValues("targetPgId");
%><?xml version="1.0" encoding="UTF-8" ?>
<openTaskIssues><%   
try {
    if (targetPageIds != null)
    {
	    ArrayList<String> statusList = new ArrayList<String>();
	    statusList.add(Issue.STATUS_OPEN);
	    statusList.add(Issue.STATUS_QUERY);
	    statusList.add(Issue.STATUS_REJECTED);

    	ArrayList<Long> tpIds = new ArrayList<Long>();
	    for (int j = 0; j < targetPageIds.length; j++)
	    {
	    	tpIds.add(Long.parseLong(targetPageIds[j]));
	    }

	    HashMap<Long, Integer> openCounts =
	   		ServerProxy.getCommentManager().getIssueCountPerTargetPage(
				Issue.TYPE_SEGMENT, tpIds, statusList);

    	for (int i=0; i < tpIds.size(); i++)
	    {
		    long tpId = tpIds.get(i);
		    TargetPage tp = ServerProxy.getPageManager().getTargetPage(tpId);
		    String tpName = tp.getExternalPageId();

		    int numOpen = (openCounts.get(tpId) == null ? 0 : openCounts.get(tpId));
	        if (numOpen > 0)
	        {
%>
 	       <targetPage numOpen="<%=numOpen%>"><%=EditUtil.encodeXmlEntities(tpName)%></targetPage>
<%
	        }
	    }
    }
}
catch (Throwable e)
{
    CATEGORY.error("Failed to query open issues for target pages from openTaskIssuesXml.jsp", e);
}
%></openTaskIssues>