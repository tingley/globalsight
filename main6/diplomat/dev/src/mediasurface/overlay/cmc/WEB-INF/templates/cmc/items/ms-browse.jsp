<%--
******************************************************************************
 Copyright (C) 1996 - 2003 Mediasurface Europe Ltd. All rights reserved.
 This file must not be copied or distributed, in part or entirety, in any form
 without prior written consent from:
 Mediasurface House
 Newbury Business Park
 London Road, Newbury
 Berkshire RG14 2QA
 <http://www.mediasurface.com>
 ******************************************************************************
 File Information
 ================
 
 MS2:MS_BROWSE_JSP.A-JSP;4_7_0#1 03-JUN-2003 14:51:50 BENM
 ms-browse.jsp - CMC Source file
 ******************************************************************************
--%>
   <%@ include file="../../include/mainpane_top.inc" %>
   <%
   String gsUsername = (String)request.getParameter("gsUsername");
   if (gsUsername != null)
   {
      session.setAttribute("gsUsername", gsUsername);   
   }   
   %>
    <iframe src="<%=Utils.makeLinkURL(request,"ms-browse", "browserframes", null, null)%>" name="editpane" width="100%" height="100%" align="right" frameborder=0 
      hspace=0 vspace=0 scrolling=auto marginwidth=0 marginheight=0>
     Please use a browser that supports IFRAMEs
    </iframe>
   <%@ include file="../../include/mainpane_bottom.inc" %>
