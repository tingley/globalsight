<%@ page contentType="text/xml; charset=UTF-8"
    import="com.globalsight.everest.servlet.util.ServerProxy,
    com.globalsight.everest.comment.Issue,
    com.globalsight.util.edit.EditUtil,
    com.globalsight.everest.page.TargetPage,
    org.apache.log4j.Logger,
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
    ArrayList statusList = new ArrayList();
    statusList.add(Issue.STATUS_OPEN);
    statusList.add(Issue.STATUS_QUERY);

    for (int i=0; i < targetPageIds.length; i++)
    {
    long tpId = Long.parseLong(targetPageIds[i]);
    TargetPage tp = ServerProxy.getPageManager().getTargetPage(tpId);
    String tpName = tp.getExternalPageId();
//    System.out.println("On targetPg: " + tpId + " " + tpName);

    //find out how many open issues there are for this target page
    String logicalKey = targetPageIds[i] + "_";
    int numOpen = ServerProxy.getCommentManager().getIssueCount(
        Issue.TYPE_SEGMENT, logicalKey, statusList);
//    System.out.println("Got numopen: " + numOpen);
    if (numOpen > 0)
    {
    %><targetPage numOpen="<%=numOpen%>"><%=EditUtil.encodeXmlEntities(tpName)%></targetPage>
<%  } //endif
    } //endor
    }
    
} //endtry
catch (Throwable e)
{
    CATEGORY.error("Failed to query open issues for target pages from openTaskIssuesXml.jsp", e);
}
%></openTaskIssues>

