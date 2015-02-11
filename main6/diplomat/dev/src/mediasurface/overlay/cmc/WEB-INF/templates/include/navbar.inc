<%--**************************************************************************
******************************************************************************
%MS_COPYRIGHT%
%MS_NAME%
%MS_ADDRESS_LONG1%
%MS_ADDRESS_LONG2%
%MS_ADDRESS_LONG3%
%MS_ADDRESS_LONG4%
%MS_TEL%
%MS_FAX%
%MS_WEB%
%MS_EMAIL%
******************************************************************************
File Information
================

MS2:NAVBAR_INC.A-INC;5_0_0#1 14-MAY-2004 10:30:47 WILLIAMD
navbar.inc - CMC source file

******************************************************************************
**************************************************************************--%>
<tr>
    <td>

<!-- old banner code, now part of navbar -->
<%
	response.getWriter().flush();
%>
<%

  String currentItem = (String)request.getAttribute(IAttributeNames.ITEMID);
  String currentType = (String)request.getAttribute(IAttributeNames.TYPEID);
  Integer currentTypeID = (Integer)request.getAttribute(IAttributeNames.TYPE_SYSTEMID);

  String selectedView = (String)request.getAttribute(IAttributeNames.VIEW);
  if (selectedView == null)
  {
  	selectedView = "Standard";
  }
  String selectedJsp = (String)request.getParameter("forwardjsp");
  IItem storedItem = (IItem) pageContext.getAttribute(Utils.CMC_EDITITEM,PageContext.SESSION_SCOPE);
  String currentView = null;
  String modelPath = (String)pageContext.getAttribute(IAttributeNames.APP_MODEL_PREFIX,PageContext.APPLICATION_SCOPE);
  Vector itemTabs = new Vector();
  Vector itemOnClicks = new Vector();
  Vector itemHelpTexts = new Vector();
  Vector itemEnabled = new Vector();
  StringBuffer itemlocator = new StringBuffer();
  boolean itemsOptionSelected = false;				// indicates "Items" navbar option selected
  boolean assetTransferOptionSelected = false;		// indicates "Asset Transfer" navbar option selected
  boolean optionsOptionSelected = true;			// indicates "Options" navbar option selected
  boolean newItem = false;
  String[] globalTabs = {};
  String[] globalURLs = {};
  String[] globalHelpTexts = null;
  boolean[] globalEnables = null;

  if (currentTypeID != null)
  {
    optionsOptionSelected = false;
  }
  else
  {
    // we are not in the item area - no current item is relevant.
    currentItem = null;
  }

  // Decide which navbar option should be selected
  if (currentType != null)
  {
  	if (currentType.equals(IAttributeNames.MODEL_TRANSFER_BRANCH))
    {
      assetTransferOptionSelected = true;
      optionsOptionSelected = false;
    }
    else if (currentType.equals(IAttributeNames.MODEL_ITEM_BRANCH))
    {
      itemsOptionSelected = true;
      optionsOptionSelected = false;
    }
    if (currentType.equals(IAttributeNames.MODEL_USER_BRANCH))
    {
      optionsOptionSelected = true;
    }
  }


  if (currentItem != null)
  {
    itemlocator.append(currentType).append("/").append(currentItem);
  }
  else if (storedItem != null)
  {
    try
    {
        itemlocator.append(IAttributeNames.MODEL_SYSTEMID_PREFIX).append(storedItem.getType().getKey()).append("/").append(IAttributeNames.MODEL_SYSTEMID_PREFIX).append(storedItem.getKey());
    }
    catch (IllegalStateException ex)
    {
        // It's a new item - assume it doesn't exist. It must have a type though
        // so the locator will still be OK.
    }
  }
  IUser currentUser = ms.getUser(ctx);
  String preferencesURL = Utils.makeLinkURL(request,"ms-edit", null, null, null, currentUser.getUserName(), IAttributeNames.MODEL_USER_BRANCH);
%>

<!-- Set up the drop down menu -->
<div id="ddmenupane" class="menuStyle">
  <a class="menuUnselected" href="<%= Utils.makeLinkURL(request, Utils.getSetting(request, "entrypage","ms-browse"), null, null, null, null, IAttributeNames.MODEL_ITEM_BRANCH)%>"><fmt:message bundle="${applicationScope.CMCMessages}" key="items"/></a><br/>
  <a class="menuUnselected" href="<%= preferencesURL %>"><fmt:message bundle="${applicationScope.CMCMessages}" key="options"/></a><br/>
<%

  // The link to the Asset Transfer module only appears if both the licence key and the logged in user have access to it
  if (ms.hasModuleAccess(IProductModule.ASSET_TRANSFER) && currentUser.hasModuleAccess(IProductModule.ASSET_TRANSFER))
  {
%>
    <a class="menuUnselected" href="<%=Utils.makeLinkURL(request, null, null, null, null, null, IAttributeNames.MODEL_TRANSFER_BRANCH)%>"><fmt:message bundle="${applicationScope.CMCMessages}" key="assetTransfer"/></a><br/>
<%
  }
%>
<script language="JavaScript">
  document.onclick=hideMenu
</script>
</div>

<table class="banner" width="100%" cellSpacing="0" cellPadding="0" border="0">
        <TBODY>
        <tr>
          <td align="left" width="100%" rowspan="2"><IMG src="<%=ctxPath%>/resources/images/logo.gif" border="0"></td>
<!--        <td align="right" class="foregroundDark" height="26" width="119"><IMG src="<%=ctxPath%>/resources/images/divider.gif" border="0" width="25" height="26"></td>-->
            <td align="right" class="headerMenuPane" nowrap="nowrap">&nbsp;&nbsp;&nbsp;
             <span class="menuTop" onClick="showMenu(event, 'ddmenupane');event.cancelBubble=true;return false;"><fmt:message bundle="${applicationScope.CMCMessages}" key="view"/></span>&nbsp;&nbsp;&nbsp;
            </td>
            <td align="right" class="headerMenuPane" nowrap="nowrap">
            &nbsp;&nbsp;&nbsp;<a href="<%=Utils.makeLinkURL(request,"ms-cmc", null, "logout", null, null, null)%>"><fmt:message bundle="${applicationScope.CMCMessages}" key="logout"/></a>&nbsp;&nbsp;&nbsp;</td>
            <td align="right" class="headerMenuPane" nowrap="nowrap">
            &nbsp;&nbsp;&nbsp;
            <a href="#" onclick="javascript:openNewWindow('<%=ctxPath%>/resources/help/index.html', 'CMCHelp', 800, 600);return false;"><fmt:message bundle="${applicationScope.CMCMessages}" key="help"/></a>&nbsp;&nbsp;&nbsp;</td>
        </tr>
        <tr>
          <td align="left" height="26">&nbsp;</td>
        </tr>
        </TBODY>
</TABLE>

  </td>
</tr>
<tr>
    <td>
<%

  String[] globalTabs2  = {Dispatcher.GetTextMessage("reports"),Dispatcher.GetTextMessage("browse"),Dispatcher.GetTextMessage("toDo")};
  String[] globalURLs2 =  {Utils.makeLinkURL(request, "ms-search", null, null, null, null, IAttributeNames.MODEL_ITEM_BRANCH)+"\" onclick=\"return checkEditPane();",
                                       Utils.makeLinkURL(request, "ms-browse", null, null, null, null, IAttributeNames.MODEL_ITEM_BRANCH)+"\" onclick=\"return checkEditPane();",
                                       Utils.makeLinkURL(request, "ms-todo", null, null, null, null, IAttributeNames.MODEL_ITEM_BRANCH)+"\" onclick=\"return checkEditPane();" };
	if (assetTransferOptionSelected)
	{
		/*
		 * Determine which is the selected tab for views that do not
		 * map directly to a tab
		 */
		if (selectedView.indexOf("ms-select-") != -1)
		{
			selectedView = "ms-select-resources";
		}
		else if (selectedView.indexOf("ms-start-") != -1 || selectedView.equals("ms-perform-cancel") || selectedView.equals("ms-view-audit-history"))
		{
			selectedView="ms-open";
		}
		else if (selectedView.equals("ms-create-transfer") || selectedView.equals("ms-delete-transfer"))
		{
			selectedView = "Standard";
		}

		ITransfer current = (ITransfer) request.getAttribute(IAttributeNames.TRANSFER_TARGET);
		String selectTransferURL = Utils.makeLinkURL(request, "Standard", null, null, null, null, IAttributeNames.MODEL_TRANSFER_BRANCH);
		String adminURL = Utils.makeLinkURL(request, "ms-admin", null, null, null, null, IAttributeNames.MODEL_TRANSFER_BRANCH);
		String[] globalTabsAT = { "Transfers", "Administration" };
		String[] globalURLsAT = { selectTransferURL, adminURL };
		String[] globalHelpTextsAT = { "Starting screen displaying all previous transfers.", "Asset Transfer system administration" };
		boolean[] globalEnablesAT = { true, true };
		globalTabs = globalTabsAT;
		globalURLs = globalURLsAT;
		globalHelpTexts = globalHelpTextsAT;
		globalEnables = globalEnablesAT;

		itemTabs.add("Transfer Control");
		itemTabs.add("Select Resources");
		itemTabs.add("View Impact Report");
		itemTabs.add("Monitor Progress");
		itemHelpTexts.add("The transfer opened main screen, where transfers can be started");
		itemHelpTexts.add("Select new resources to transfer");
		itemHelpTexts.add("View the generated impact report of what is to be transfered");
		itemHelpTexts.add("Monitor the progress of the running transfer or impact report");
		if (current != null)
		{
			String openTransferURL = Utils.makeLinkURL(request,"ms-open", null, null, null, current.getKey());
			String selectResourcesURL = Utils.makeLinkURL(request,"ms-select-resources", null, null, null, current.getKey());
			String monitorProgressURL = Utils.makeLinkURL(request,"ms-monitor-progress", null, null, null, current.getKey());
			String viewImpactReportURL = Utils.makeLinkURL(request,"ms-view-report", null, null, null, current.getKey());
			itemOnClicks.add(openTransferURL);
			itemOnClicks.add(selectResourcesURL);
			itemOnClicks.add(viewImpactReportURL);
			itemOnClicks.add(monitorProgressURL);
			itemEnabled.add(new Boolean(current.isCurrentTransfer()));
			itemEnabled.add(new Boolean(TransferStates.canSelectResources(current.getState()) && current.isCurrentTransfer()));
			itemEnabled.add(new Boolean(TransferStates.canViewImpactReport(current.getState())));
			itemEnabled.add(new Boolean(TransferStates.canMonitorProgress(current.getState()) && current.isCurrentTransfer()));
		}
		else
		{
			itemOnClicks.add("");
			itemOnClicks.add("");
			itemOnClicks.add("");
			itemOnClicks.add("");
			itemEnabled.add(new Boolean(false));
			itemEnabled.add(new Boolean(false));
			itemEnabled.add(new Boolean(false));
			itemEnabled.add(new Boolean(false));
		}
	}
  else if (optionsOptionSelected)
  {
      String locksURL = Utils.makeLinkURL(request,"ms-cmc", null, null, "forwardjsp=locksform", null, null);
      String usageURL = Utils.makeLinkURL(request,"ms-cmc", null, null, "forwardjsp=usageform", null, null);
      String changesiteURL = Utils.makeLinkURL(request,"ms-cmc", null, null, "forwardjsp=cmcsiteselector", null, null);
      itemTabs.add(Dispatcher.GetTextMessage("preferences"));
      itemTabs.add(Dispatcher.GetTextMessage("locks"));
      itemTabs.add(Dispatcher.GetTextMessage("usage"));
      itemTabs.add(Dispatcher.GetTextMessage("changeSite"));
      itemOnClicks.add(preferencesURL);
      itemOnClicks.add(locksURL);
      itemOnClicks.add(usageURL);
      itemOnClicks.add(changesiteURL);

      if (Utils.isAdminUser(request))
      {
        String administrationURL = Utils.makeLinkURL(request,"ms-cmc", null, null, "forwardjsp=administrationform", null, null);
        String typeIconsURL = Utils.makeLinkURL(request,"ms-cmc", null, null, "forwardjsp=typeicons", null, null);
        itemTabs.add(1,Dispatcher.GetTextMessage("administration"));
        itemOnClicks.add(1,administrationURL);
        itemTabs.add(1,Dispatcher.GetTextMessage("icons"));
        itemOnClicks.add(1,typeIconsURL);
      }
  }
  else
  {
      globalTabs = globalTabs2;
      globalURLs = globalURLs2;
      String newURL = Utils.makeLinkURL(request, "ms-add", null, null, null, null, IAttributeNames.MODEL_ITEM_BRANCH)+"\" onclick=\" return checkEditPane();";
      String editURL = "#\" onclick=\"return getformelements('"+ctxPath+modelPath+"/','ms-edit','"+itemlocator.toString()+"', null, false)";
      String workflowURL = "#\" onclick=\"return getformelements('"+ctxPath+modelPath+"/','ms-workflow','"+itemlocator.toString()+"', null, true)";
      String historyURL = "#\" onclick=\"return getformelements('"+ctxPath+modelPath+"/','ms-history','"+itemlocator.toString()+"', null, false)";
      String utilisationURL = "#\" onclick=\"return getformelements('"+ctxPath+modelPath+"/','ms-utilisation','"+itemlocator.toString()+"', null, false)";
      String localizeURL = "#\" onclick=\"return getformelements('"+ctxPath+modelPath+"/','ms-localize','"+itemlocator.toString()+"', null, false)";
      String deleteURL = "#\" onclick=\"return getformelements('"+ctxPath+modelPath+"/','ms-delete','"+itemlocator.toString()+"', null, false)";
      String previewURL = "#\" onclick=\"return getformelements('"+ctxPath+modelPath+"/','ms-view','"+itemlocator.toString()+"', 'preview', false)";
      itemTabs.add(Dispatcher.GetTextMessage("new"));
      itemTabs.add(Dispatcher.GetTextMessage("edit"));
      itemTabs.add(Dispatcher.GetTextMessage("workflow"));
      itemTabs.add(Dispatcher.GetTextMessage("history"));
      itemTabs.add(Dispatcher.GetTextMessage("usedBy"));
      itemTabs.add(Dispatcher.GetTextMessage("delete"));
      itemTabs.add(Dispatcher.GetTextMessage("localize"));
      itemTabs.add(Dispatcher.GetTextMessage("preview"));
      itemOnClicks.add(newURL);
      itemOnClicks.add(editURL);
      itemOnClicks.add(workflowURL);
      itemOnClicks.add(historyURL);
      itemOnClicks.add(utilisationURL);
      itemOnClicks.add(deleteURL);
      itemOnClicks.add(localizeURL);
      itemOnClicks.add(previewURL);
  }

  int selectedIndex = -1;
  int totalTabs = globalTabs.length+itemTabs.size();

  for (int i=0; i<totalTabs; i++)
  {
    if (i < globalTabs.length)
    {
        if (globalURLs[i].indexOf(selectedView) >= 0)
        {
            selectedIndex = i;
            break;
        }
    }
    else
    {
    	String clickString = (String)itemOnClicks.elementAt(i-globalTabs.length);
    	if (selectedView.equals("ms-cmc"))
    	{
    	    if ((selectedJsp != null) && (clickString.indexOf(selectedJsp) >= 0))
	        {
	            selectedIndex = i;
	            break;
            }
    	}
    	else
    	{
          if (clickString.indexOf(selectedView) >= 0)
          {
            selectedIndex = i;
            break;
          }
        }
    }
  }
%>


<%
	// Used when looping over elements to keep track of whether a tab is
	// enabled, disabled, selected or not selected etc
        String styleClass = null;
%>
<div id="Menubar">
<table width="100%" BORDER=0 CELLSPACING=0 CELLPADDING=0 class="banner">
	<tr height="3">
		<td colspan="<%=(totalTabs+7)%>">
		</td>
	</tr>

<%-- FIRST ROW DRAWS THE TOP LINES OF THE TABS --%>
	<tr>
      <td width="5"><IMG height=1 src="<%=ctxPath%>/resources/images/minispacer.gif" width=5></td> <!-- Gap -->
<%
      for (int i=0; i<totalTabs; i++)
      {
      	// insert spacer after global tabs
        if (i == globalTabs.length)
        {
%>
          <td width="100%"><IMG src="<%=ctxPath%>/resources/images/minispacer.gif" height="1"></td> <!-- Big Gap -->
<%
        }
        // Work out the style to use for this tab, which depends on whether it
        // is selected, not selected or disabled...
        if (selectedIndex == i)
        {
        	styleClass = "tabSelected";
        }
        else
        {
        	styleClass = "tabNotSelected";
	        if (i < globalTabs.length)
	        {
	        	if (globalEnables != null && globalEnables.length > i)
	        	{
		            	if (!globalEnables[i])
		            	{
		            		styleClass = "tabDisabled";
		            	}
	        	}
	        }
	        else if (itemEnabled.size() > (i-globalTabs.length))
	        {
	            	if (!((Boolean)itemEnabled.elementAt(i-globalTabs.length)).booleanValue())
	            	{
	            		styleClass = "tabDisabled";
	            	}
	        }
        }
%>
		  <td width="1" height="2"><IMG height=2 src="<%=ctxPath%>/resources/images/minispacer.gif" width=1></td>
		  <td background="<%=ctxPath%>/resources/images/top_middle.gif" width="3" height="2"><IMG height=2 src="<%=ctxPath%>/resources/images/minispacer.gif" width=3></td>
		  <td background="<%=ctxPath%>/resources/images/top_middle.gif" height="2" class="<%= styleClass %>"><IMG height=2 src="<%=ctxPath%>/resources/images/minispacer.gif" width=1></td>
		  <td background="<%=ctxPath%>/resources/images/top_middle.gif" width="3" height="2"><IMG height=2 src="<%=ctxPath%>/resources/images/minispacer.gif" width=3></td>
		  <td width="1" height="2"><IMG height=2 src="<%=ctxPath%>/resources/images/minispacer.gif" width=1></td>
		  <td width="1" height="2"><IMG height=2 src="<%=ctxPath%>/resources/images/minispacer.gif" width=1></td>
<%
      }
%>
      <td width="5"><IMG height=1 src="<%=ctxPath%>/resources/images/minispacer.gif" width=5></td> <!-- Gap -->
    </tr>

<%-- SECOND ROW DISPLAYS CONTENTS OF EACH TAB --%>
	<tr>
		<td width="5"><IMG src="<%=ctxPath%>/resources/images/minispacer.gif" width="5" height="1"></td>
<%
      for (int i=0; i<totalTabs; i++)
      {
        String urlPath;
        String name;
        String tabHelpText = "";
        boolean isEnabled = true;
        if (i < globalTabs.length)
        {
            urlPath = globalURLs[i];
            name = globalTabs[i];
            if (globalHelpTexts != null && globalHelpTexts.length > i)
            {
            	tabHelpText = "title=\"" + globalHelpTexts[i] + "\"";
            }
            if (globalEnables != null && globalEnables.length > i)
            {
            	isEnabled = globalEnables[i];
            }
        }
        else
        {
            urlPath = (String)itemOnClicks.elementAt(i-globalTabs.length);
            name = (String)itemTabs.elementAt(i-globalTabs.length);
            if (itemHelpTexts.size() > (i-globalTabs.length))
            {
            	tabHelpText = "title=\"" +(String)itemHelpTexts.elementAt(i-globalTabs.length) + "\"";
            }
            if (itemEnabled.size() > (i-globalTabs.length))
            {
            	isEnabled = ((Boolean)itemEnabled.elementAt(i-globalTabs.length)).booleanValue();
            }
        }
        // Insert spacer after global tabs
        if (i == globalTabs.length)
        {
%>
          <td></td>
<%
        }
        // Work out the style to use for this tab, which depends on whether it
        // is selected, not selected or disabled...
        if (selectedIndex == i)
        {
        	styleClass = "tabSelected";
        }
        else if (!isEnabled)
        {
        	styleClass = "tabDisabled";
        }
        else
        {
        	styleClass = "tabNotSelected";
        }
%>
    		<td bgcolor="#000000" width="1"><IMG height=1 src="<%=ctxPath%>/resources/images/minispacer.gif" width=1></td>
    		<td width="3" class="<%= styleClass %>"><IMG height=2 src="<%=ctxPath%>/resources/images/minispacer.gif" width=1></td>
<%
            if (isEnabled)
            {
%>
    		<td nowrap="nowrap" height="20" class="<%= styleClass %>">&nbsp;&nbsp;<a href="<%=urlPath%>" <%= tabHelpText %>><span class="<%= styleClass %>"><%=name%></span></a>&nbsp;&nbsp;</td>
<%
            }
            else
            {
%>
    		<td nowrap="nowrap" height="20" class="<%= styleClass %>">&nbsp;&nbsp;<span class="<%= styleClass %>"><%=name%></span>&nbsp;&nbsp;</td>
<%
            }
%>
    		<td width="3" class="<%= styleClass %>"><IMG height=2 src="<%=ctxPath%>/resources/images/minispacer.gif" width=1></td>
    		<td bgcolor="#000000" width="1"><IMG height=1 src="<%=ctxPath%>/resources/images/minispacer.gif" width=1></td>
    		<td width="1"><IMG height=1 src="<%=ctxPath%>/resources/images/minispacer.gif" width=1></td>
<%
      }
%>
		<td width="5"><IMG src="<%=ctxPath%>/resources/images/minispacer.gif" width="5" height="1"></td>
    </tr>




<%-- THIRD ROW DRAWS THE BLACK LINE ACROSS THE SCREEN WITH A GAP FOR THE CURRENT TAB --%>
    <tr>
      <td bgcolor="#000000"></td>
<%
      for (int i=0; i<totalTabs; i++)
      {
        // Insert spacer after global tabs
        if (i == globalTabs.length)
        {
%>
          <td bgcolor="#000000"></td>
<%
        }
        if (selectedIndex == i)
        {
%>
          <td bgcolor="#000000"></td>
          <td colspan="3" class="tabSelected"></td>
          <td colspan="2" bgcolor="#000000"></td>
<%
        }
        else
        {
%>
          <td colspan="6" bgcolor="#000000"></td>
<%
        }
      }
%>
      <td bgcolor="#000000"></td>
    </tr>
</table>


   </td>
</tr>
