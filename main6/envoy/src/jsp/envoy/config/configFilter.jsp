<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ page contentType="text/html; charset=UTF-8"
         errorPage="/envoy/common/activityError.jsp"
         import="com.globalsight.everest.servlet.util.SessionManager,
                 com.globalsight.util.edit.EditUtil,
                 com.globalsight.everest.webapp.WebAppConstants,
                 com.globalsight.everest.webapp.javabean.NavigationBean,
                 com.globalsight.everest.webapp.pagehandler.PageHandler,
                 com.globalsight.everest.webapp.pagehandler.administration.company.CompanyConstants,
                 com.globalsight.everest.company.Company,
                 com.globalsight.everest.webapp.webnavigation.LinkHelper,
                 com.globalsight.everest.servlet.util.ServerProxy,
                 com.globalsight.everest.servlet.EnvoyServletException,
                 com.globalsight.everest.util.system.SystemConfigParamNames,
                 com.globalsight.everest.webapp.pagehandler.edit.inctxrv.pdf.PreviewPDFHelper,
                 com.globalsight.util.GeneralException,
                 java.text.MessageFormat,
                 com.globalsight.util.StringUtil,
                 java.util.*"
          session="true"
%>
<%
    ResourceBundle bundle = PageHandler.getBundle(session);
%>
<html>

<body leftmargin="0" rightrmargin="0" topmargin="0" marginwidth="0"
 marginheight="0" onload="">
<table border="0" cellspacing="2" cellpadding="2" class="standardText">
  <tr valign="top">
    <td>
      <table border="0" class="standardText" cellpadding="2">
  		<tr valign="top">
    		<td colspan=3>
                <br/>        
                <div id="toShowBaseFilter" style="cursor:pointer;display:inline-block;" onclick="initConfigShow('toShowBaseFilter','baseFilterPanel')">
                    <span style="display:show;"><img src="/globalsight/images/enlarge.jpg"/></span>
                    <b><%=bundle.getString("lb_filter_basefilter")%></b>
                </div>
                <div id="baseFilterPanel" style="display:none;">
                
      			<table border="0" class="standardText" cellpadding="2">
      			<tr>
      				<td>
      					<span><%=bundle.getString("lb_all") + " "+ bundle.getString("lb_filter_basefilter")%>s
      				</td>
      				<td>&nbsp;</td>
      				<td>
      					<span><%=bundle.getString("lb_selected") + " "+ bundle.getString("lb_filter_basefilter")%>s
      				</td>
      			</tr>
        		<tr>
        			<td>
        				<select id="baseFilterFrom" name="baseFilterFrom" multiple class="standardText" size="10" style="width:250">
        				<c:forEach var="op" items="${basefilter}">
	      					<option title="${op.filterName}" value="filter-base_filter-${op.id}">${op.filterName}</option>
	    				</c:forEach>
        				</select>
        			</td>
        			<td>
        				<table>
						<tr>
		              	<td>
		                	<input type="button" name="addButton" value=" >> "
		                    onclick="move('baseFilterFrom','baseFilterTo')"><br>
		              	</td>
		            	</tr>
		            	<tr><td>&nbsp;</td></tr>
		            	<tr>
		                	<td>
		                	<input type="button" name="removedButton" value=" << "
		                    onclick="move('baseFilterTo','baseFilterFrom')">
							</td>
						</tr>
						</table>
        			</td>
        			<td>
        				<select id="baseFilterTo" name="baseFilterTo" multiple class="standardText" size="10" style="width:250">
        				</select>
        			</td>
        		</tr>
				</table>
                </div>
    		</td>
  		</tr>
  		
  		<tr valign="top">
    		<td colspan=3>
                <br/>        
                <div id="toShowfmFilter" style="cursor:pointer;display:inline-block;" onclick="initConfigShow('toShowfmFilter','fmFilterPanel')">
                    <span style="display:show;"><img src="/globalsight/images/enlarge.jpg"/></span>
                    <b><%=bundle.getString("lb_config_filter_fmfilter")%></b>
                </div>
                <div id="fmFilterPanel" style="display:none;">
                
      			<table border="0" class="standardText" cellpadding="2">
      			<tr>
      				<td>
      					<span><%=bundle.getString("lb_all") + " "+ bundle.getString("lb_config_filter_fmfilter")%>s
      				</td>
      				<td>&nbsp;</td>
      				<td>
      					<span><%=bundle.getString("lb_selected") + " "+ bundle.getString("lb_config_filter_fmfilter")%>s
      				</td>
      			</tr>
        		<tr>
        			<td>
        				<select id="fmFilterFrom" name="fmFilterFrom" multiple class="standardText" size="10" style="width:250">
        				<c:forEach var="op" items="${fmfilter}">
	      					<option title="${op.filterName}" value="filter-frame_maker_filter-${op.id}">${op.filterName}</option>
	    				</c:forEach>
        				</select>
        			</td>
        			<td>
        				<table>
						<tr>
		              	<td>
		                	<input type="button" name="addButton" value=" >> "
		                    onclick="move('fmFilterFrom','fmFilterTo')"><br>
		              	</td>
		            	</tr>
		            	<tr><td>&nbsp;</td></tr>
		            	<tr>
		                	<td>
		                	<input type="button" name="removedButton" value=" << "
		                    onclick="move('fmFilterTo','fmFilterFrom')">
							</td>
						</tr>
						</table>
        			</td>
        			<td>
        				<select id="fmFilterTo" name="fmFilterTo" multiple class="standardText" size="10" style="width:250">
        				</select>
        			</td>
        		</tr>
				</table>
                </div>
    		</td>
  		</tr>
  		<tr valign="top">
    		<td colspan=3>
                <br/>        
                <div id="toShowHtmlFilter" style="cursor:pointer;display:inline-block;" onclick="initConfigShow('toShowHtmlFilter','htmlFilterPanel')">
                    <span style="display:show;"><img src="/globalsight/images/enlarge.jpg"/></span>
                    <b><%=bundle.getString("lb_config_filter_htmlfilter")%></b>
                </div>
                <div id="htmlFilterPanel" style="display:none;">
                
      			<table border="0" class="standardText" cellpadding="2">
      			<tr>
      				<td>
      					<span><%=bundle.getString("lb_all") + " "+ bundle.getString("lb_config_filter_htmlfilter")%>s
      				</td>
      				<td>&nbsp;</td>
      				<td>
      					<span><%=bundle.getString("lb_selected") + " "+ bundle.getString("lb_config_filter_htmlfilter")%>s
      				</td>
      			</tr>
        		<tr>
        			<td>
        				<select id="htmlFilterFrom" name="htmlFilterFrom" multiple class="standardText" size="10" style="width:250">
        				<c:forEach var="op" items="${htmlfilter}">
	      					<option title="${op.filterName}" value="filter-html_filter-${op.id}">${op.filterName}</option>
	    				</c:forEach>
        				</select>
        			</td>
        			<td>
        				<table>
						<tr>
		              	<td>
		                	<input type="button" name="addButton" value=" >> "
		                    onclick="move('htmlFilterFrom','htmlFilterTo')"><br>
		              	</td>
		            	</tr>
		            	<tr><td>&nbsp;</td></tr>
		            	<tr>
		                	<td>
		                	<input type="button" name="removedButton" value=" << "
		                    onclick="move('htmlFilterTo','htmlFilterFrom')">
							</td>
						</tr>
						</table>
        			</td>
        			<td>
        				<select id="htmlFilterTo" name="htmlFilterTo" multiple class="standardText" size="10" style="width:250">
        				</select>
        			</td>
        		</tr>
				</table>
                </div>
    		</td>
  		</tr>
  		<tr valign="top">
    		<td colspan=3>
                <br/>        
                <div id="toShowInddFilter" style="cursor:pointer;display:inline-block;" onclick="initConfigShow('toShowInddFilter','inddFilterPanel')">
                    <span style="display:show;"><img src="/globalsight/images/enlarge.jpg"/></span>
                    <b><%=bundle.getString("lb_filter_inddFilter")%></b>
                </div>
                <div id="inddFilterPanel" style="display:none;">
                
      			<table border="0" class="standardText" cellpadding="2">
      			<tr>
      				<td>
      					<span><%=bundle.getString("lb_all") + " "+ bundle.getString("lb_filter_inddFilter")%>s
      				</td>
      				<td>&nbsp;</td>
      				<td>
      					<span><%=bundle.getString("lb_selected") + " "+ bundle.getString("lb_filter_inddFilter")%>s
      				</td>
      			</tr>
        		<tr>
        			<td>
        				<select id="inddFilterFrom" name="inddFilterFrom" multiple class="standardText" size="10" style="width:250">
        				<c:forEach var="op" items="${inddfilter}">
	      					<option title="${op.filterName}" value="filter-indd_filter-${op.id}">${op.filterName}</option>
	    				</c:forEach>
        				</select>
        			</td>
        			<td>
        				<table>
						<tr>
		              	<td>
		                	<input type="button" name="addButton" value=" >> "
		                    onclick="move('inddFilterFrom','inddFilterTo')"><br>
		              	</td>
		            	</tr>
		            	<tr><td>&nbsp;</td></tr>
		            	<tr>
		                	<td>
		                	<input type="button" name="removedButton" value=" << "
		                    onclick="move('inddFilterTo','inddFilterFrom')">
							</td>
						</tr>
						</table>
        			</td>
        			<td>
        				<select id="inddFilterTo" name="inddFilterTo" multiple class="standardText" size="10" style="width:250">
        				</select>
        			</td>
        		</tr>
				</table>
                </div>
    		</td>
  		</tr>
  		<tr valign="top">
    		<td colspan=3>
                <br/>        
                <div id="toShowJavapropertiesFilter" style="cursor:pointer;display:inline-block;" onclick="initConfigShow('toShowJavapropertiesFilter','javapropertiesFilterPanel')">
                    <span style="display:show;"><img src="/globalsight/images/enlarge.jpg"/></span>
                    <b><%=bundle.getString("lb_filter_javapropertiesfilter")%></b>
                </div>
                <div id="javapropertiesFilterPanel" style="display:none;">
                
      			<table border="0" class="standardText" cellpadding="2">
      			<tr>
      				<td>
      					<span><%=bundle.getString("lb_all") + " "+ bundle.getString("lb_filter_javapropertiesfilter")%>s
      				</td>
      				<td>&nbsp;</td>
      				<td>
      					<span><%=bundle.getString("lb_selected") + " "+ bundle.getString("lb_filter_javapropertiesfilter")%>s
      				</td>
      			</tr>
        		<tr>
        			<td>
        				<select id="javapropertiesFilterFrom" name="javapropertiesFilterFrom" multiple class="standardText" size="10" style="width:250">
        				<c:forEach var="op" items="${javapropertiesfilter}">
	      					<option title="${op.filterName}" value="filter-java_properties_filter-${op.id}">${op.filterName}</option>
	    				</c:forEach>
        				</select>
        			</td>
        			<td>
        				<table>
						<tr>
		              	<td>
		                	<input type="button" name="addButton" value=" >> "
		                    onclick="move('javapropertiesFilterFrom','javapropertiesFilterTo')"><br>
		              	</td>
		            	</tr>
		            	<tr><td>&nbsp;</td></tr>
		            	<tr>
		                	<td>
		                	<input type="button" name="removedButton" value=" << "
		                    onclick="move('javapropertiesFilterTo','javapropertiesFilterFrom')">
							</td>
						</tr>
						</table>
        			</td>
        			<td>
        				<select id="javapropertiesFilterTo" name="javapropertiesFilterTo" multiple class="standardText" size="10" style="width:250">
        				</select>
        			</td>
        		</tr>
				</table>
                </div>
    		</td>
  		</tr>
  		<tr valign="top">
    		<td colspan=3>
                <br/>        
                <div id="toShowJavascriptFilter" style="cursor:pointer;display:inline-block;" onclick="initConfigShow('toShowJavascriptFilter','javascriptFilterPanel')">
                    <span style="display:show;"><img src="/globalsight/images/enlarge.jpg"/></span>
                    <b><%=bundle.getString("lb_config_filter_javascriptfilter")%></b>
                </div>
                <div id="javascriptFilterPanel" style="display:none;">
                
      			<table border="0" class="standardText" cellpadding="2">
      			<tr>
      				<td>
      					<span><%=bundle.getString("lb_all") + " "+ bundle.getString("lb_config_filter_javascriptfilter")%>s
      				</td>
      				<td>&nbsp;</td>
      				<td>
      					<span><%=bundle.getString("lb_selected") + " "+ bundle.getString("lb_config_filter_javascriptfilter")%>s
      				</td>
      			</tr>
        		<tr>
        			<td>
        				<select id="javascriptFilterFrom" name="javascriptFilterFrom" multiple class="standardText" size="10" style="width:250">
        				<c:forEach var="op" items="${javascriptfilter}">
	      					<option title="${op.filterName}" value="filter-java_script_filter-${op.id}">${op.filterName}</option>
	    				</c:forEach>
        				</select>
        			</td>
        			<td>
        				<table>
						<tr>
		              	<td>
		                	<input type="button" name="addButton" value=" >> "
		                    onclick="move('javascriptFilterFrom','javascriptFilterTo')"><br>
		              	</td>
		            	</tr>
		            	<tr><td>&nbsp;</td></tr>
		            	<tr>
		                	<td>
		                	<input type="button" name="removedButton" value=" << "
		                    onclick="move('javascriptFilterTo','javascriptFilterFrom')">
							</td>
						</tr>
						</table>
        			</td>
        			<td>
        				<select id="javascriptFilterTo" name="javascriptFilterTo" multiple class="standardText" size="10" style="width:250">
        				</select>
        			</td>
        		</tr>
				</table>
                </div>
    		</td>
  		</tr>
  		<tr valign="top">
    		<td colspan=3>
                <br/>        
                <div id="toShowjsonFilter" style="cursor:pointer;display:inline-block;" onclick="initConfigShow('toShowjsonFilter','jsonFilterPanel')">
                    <span style="display:show;"><img src="/globalsight/images/enlarge.jpg"/></span>
                    <b><%=bundle.getString("lb_filter_jsonfilter")%></b>
                </div>
                <div id="jsonFilterPanel" style="display:none;">
                
      			<table border="0" class="standardText" cellpadding="2">
      			<tr>
      				<td>
      					<span><%=bundle.getString("lb_all") + " "+ bundle.getString("lb_filter_jsonfilter")%>s
      				</td>
      				<td>&nbsp;</td>
      				<td>
      					<span><%=bundle.getString("lb_selected") + " "+ bundle.getString("lb_filter_jsonfilter")%>s
      				</td>
      			</tr>
        		<tr>
        			<td>
        				<select id="jsonFilterFrom" name="jsonFilterFrom" multiple class="standardText" size="10" style="width:250">
        				<c:forEach var="op" items="${jsonfilter}">
	      					<option title="${op.filterName}" value="filter-filter_json-${op.id}">${op.filterName}</option>
	    				</c:forEach>
        				</select>
        			</td>
        			<td>
        				<table>
						<tr>
		              	<td>
		                	<input type="button" name="addButton" value=" >> "
		                    onclick="move('jsonFilterFrom','jsonFilterTo')"><br>
		              	</td>
		            	</tr>
		            	<tr><td>&nbsp;</td></tr>
		            	<tr>
		                	<td>
		                	<input type="button" name="removedButton" value=" << "
		                    onclick="move('jsonFilterTo','jsonFilterFrom')">
							</td>
						</tr>
						</table>
        			</td>
        			<td>
        				<select id="jsonFilterTo" name="jsonFilterTo" multiple class="standardText" size="10" style="width:250">
        				</select>
        			</td>
        		</tr>
				</table>
                </div>
    		</td>
  		</tr>
  		<tr valign="top">
    		<td colspan=3>
                <br/>        
                <div id="toShowjspFilter" style="cursor:pointer;display:inline-block;" onclick="initConfigShow('toShowjspFilter','jspFilterPanel')">
                    <span style="display:show;"><img src="/globalsight/images/enlarge.jpg"/></span>
                    <b><%=bundle.getString("lb_config_filter_jspfilter")%></b>
                </div>
                <div id="jspFilterPanel" style="display:none;">
                
      			<table border="0" class="standardText" cellpadding="2">
      			<tr>
      				<td>
      					<span><%=bundle.getString("lb_all") + " "+ bundle.getString("lb_config_filter_jspfilter")%>s
      				</td>
      				<td>&nbsp;</td>
      				<td>
      					<span><%=bundle.getString("lb_selected") + " "+ bundle.getString("lb_config_filter_jspfilter")%>s
      				</td>
      			</tr>
        		<tr>
        			<td>
        				<select id="jspFilterFrom" name="jspFilterFrom" multiple class="standardText" size="10" style="width:250">
        				<c:forEach var="op" items="${jspfilter}">
	      					<option title="${op.filterName}" value="filter-jsp_filter-${op.id}">${op.filterName}</option>
	    				</c:forEach>
        				</select>
        			</td>
        			<td>
        				<table>
						<tr>
		              	<td>
		                	<input type="button" name="addButton" value=" >> "
		                    onclick="move('jspFilterFrom','jspFilterTo')"><br>
		              	</td>
		            	</tr>
		            	<tr><td>&nbsp;</td></tr>
		            	<tr>
		                	<td>
		                	<input type="button" name="removedButton" value=" << "
		                    onclick="move('jspFilterTo','jspFilterFrom')">
							</td>
						</tr>
						</table>
        			</td>
        			<td>
        				<select id="jspFilterTo" name="jspFilterTo" multiple class="standardText" size="10" style="width:250">
        				</select>
        			</td>
        		</tr>
				</table>
                </div>
    		</td>
  		</tr>
  		<tr valign="top">
    		<td colspan=3>
                <br/>        
                <div id="toShowo2010Filter" style="cursor:pointer;display:inline-block;" onclick="initConfigShow('toShowo2010Filter','o2010FilterPanel')">
                    <span style="display:show;"><img src="/globalsight/images/enlarge.jpg"/></span>
                    <b><%=bundle.getString("lb_filter_o2010Filter")%></b>
                </div>
                <div id="o2010FilterPanel" style="display:none;">
                
      			<table border="0" class="standardText" cellpadding="2">
      			<tr>
      				<td>
      					<span><%=bundle.getString("lb_all") + " "+ bundle.getString("lb_filter_o2010Filter")%>s
      				</td>
      				<td>&nbsp;</td>
      				<td>
      					<span><%=bundle.getString("lb_selected") + " "+ bundle.getString("lb_filter_o2010Filter")%>s
      				</td>
      			</tr>
        		<tr>
        			<td>
        				<select id="o2010FilterFrom" name="o2010FilterFrom" multiple class="standardText" size="10" style="width:250">
        				<c:forEach var="op" items="${office2010filter}">
	      					<option title="${op.filterName}" value="filter-office2010_filter-${op.id}">${op.filterName}</option>
	    				</c:forEach>
        				</select>
        			</td>
        			<td>
        				<table>
						<tr>
		              	<td>
		                	<input type="button" name="addButton" value=" >> "
		                    onclick="move('o2010FilterFrom','o2010FilterTo')"><br>
		              	</td>
		            	</tr>
		            	<tr><td>&nbsp;</td></tr>
		            	<tr>
		                	<td>
		                	<input type="button" name="removedButton" value=" << "
		                    onclick="move('o2010FilterTo','o2010FilterFrom')">
							</td>
						</tr>
						</table>
        			</td>
        			<td>
        				<select id="o2010FilterTo" name="o2010FilterTo" multiple class="standardText" size="10" style="width:250">
        				</select>
        			</td>
        		</tr>
				</table>
                </div>
    		</td>
  		</tr>
  		<tr valign="top">
    		<td colspan=3>
                <br/>        
                <div id="toShowMsdocFilter" style="cursor:pointer;display:inline-block;" onclick="initConfigShow('toShowMsdocFilter','msdocFilterPanel')">
                    <span style="display:show;"><img src="/globalsight/images/enlarge.jpg"/></span>
                    <b><%=bundle.getString("lb_filter_msdocfilter")%></b>
                </div>
                <div id="msdocFilterPanel" style="display:none;">
                
      			<table border="0" class="standardText" cellpadding="2">
      			<tr>
      				<td>
      					<span><%=bundle.getString("lb_all") + " "+ bundle.getString("lb_filter_msdocfilter")%>s
      				</td>
      				<td>&nbsp;</td>
      				<td>
      					<span><%=bundle.getString("lb_selected") + " "+ bundle.getString("lb_filter_msdocfilter")%>s
      				</td>
      			</tr>
        		<tr>
        			<td>
        				<select id="msdocFilterFrom" name="msdocFilterFrom" multiple class="standardText" size="10" style="width:250">
        				<c:forEach var="op" items="${msdocfilter}">
	      					<option title="${op.filterName}" value="filter-ms_office_doc_filter-${op.id}">${op.filterName}</option>
	    				</c:forEach>
        				</select>
        			</td>
        			<td>
        				<table>
						<tr>
		              	<td>
		                	<input type="button" name="addButton" value=" >> "
		                    onclick="move('msdocFilterFrom','msdocFilterTo')"><br>
		              	</td>
		            	</tr>
		            	<tr><td>&nbsp;</td></tr>
		            	<tr>
		                	<td>
		                	<input type="button" name="removedButton" value=" << "
		                    onclick="move('msdocFilterTo','msdocFilterFrom')">
							</td>
						</tr>
						</table>
        			</td>
        			<td>
        				<select id="msdocFilterTo" name="msdocFilterTo" multiple class="standardText" size="10" style="width:250">
        				</select>
        			</td>
        		</tr>
				</table>
                </div>
    		</td>
  		</tr>
  		<tr valign="top">
    		<td colspan=3>
                <br/>        
                <div id="toShowMsexcelFilter" style="cursor:pointer;display:inline-block;" onclick="initConfigShow('toShowMsexcelFilter','msexcelFilterPanel')">
                    <span style="display:show;"><img src="/globalsight/images/enlarge.jpg"/></span>
                    <b><%=bundle.getString("lb_filter_msexcelfilter")%></b>
                </div>
                <div id="msexcelFilterPanel" style="display:none;">
                
      			<table border="0" class="standardText" cellpadding="2">
      			<tr>
      				<td>
      					<span><%=bundle.getString("lb_all") + " "+ bundle.getString("lb_filter_msexcelfilter")%>s
      				</td>
      				<td>&nbsp;</td>
      				<td>
      					<span><%=bundle.getString("lb_selected") + " "+ bundle.getString("lb_filter_msexcelfilter")%>s
      				</td>
      			</tr>
        		<tr>
        			<td>
        				<select id="msexcelFilterFrom" name="msexcelFilterFrom" multiple class="standardText" size="10" style="width:250">
        				<c:forEach var="op" items="${msexcelfilter}">
	      					<option title="${op.filterName}" value="filter-ms_office_excel_filter-${op.id}">${op.filterName}</option>
	    				</c:forEach>
        				</select>
        			</td>
        			<td>
        				<table>
						<tr>
		              	<td>
		                	<input type="button" name="addButton" value=" >> "
		                    onclick="move('msexcelFilterFrom','msexcelFilterTo')"><br>
		              	</td>
		            	</tr>
		            	<tr><td>&nbsp;</td></tr>
		            	<tr>
		                	<td>
		                	<input type="button" name="removedButton" value=" << "
		                    onclick="move('msexcelFilterTo','msexcelFilterFrom')">
							</td>
						</tr>
						</table>
        			</td>
        			<td>
        				<select id="msexcelFilterTo" name="msexcelFilterTo" multiple class="standardText" size="10" style="width:250">
        				</select>
        			</td>
        		</tr>
				</table>
                </div>
    		</td>
  		</tr>
  		<tr valign="top">
    		<td colspan=3>
                <br/>        
                <div id="toShowMspptFilter" style="cursor:pointer;display:inline-block;" onclick="initConfigShow('toShowMspptFilter','mspptFilterPanel')">
                    <span style="display:show;"><img src="/globalsight/images/enlarge.jpg"/></span>
                    <b><%=bundle.getString("lb_filter_mspptfilter")%></b>
                </div>
                <div id="mspptFilterPanel" style="display:none;">
                
      			<table border="0" class="standardText" cellpadding="2">
      			<tr>
      				<td>
      					<span><%=bundle.getString("lb_all") + " "+ bundle.getString("lb_filter_mspptfilter")%>s
      				</td>
      				<td>&nbsp;</td>
      				<td>
      					<span><%=bundle.getString("lb_selected") + " "+ bundle.getString("lb_filter_mspptfilter")%>s
      				</td>
      			</tr>
        		<tr>
        			<td>
        				<select id="mspptFilterFrom" name="mspptFilterFrom" multiple class="standardText" size="10" style="width:250">
        				<c:forEach var="op" items="${mspptfilter}">
	      					<option title="${op.filterName}" value="filter-ms_office_ppt_filter-${op.id}">${op.filterName}</option>
	    				</c:forEach>
        				</select>
        			</td>
        			<td>
        				<table>
						<tr>
		              	<td>
		                	<input type="button" name="addButton" value=" >> "
		                    onclick="move('mspptFilterFrom','mspptFilterTo')"><br>
		              	</td>
		            	</tr>
		            	<tr><td>&nbsp;</td></tr>
		            	<tr>
		                	<td>
		                	<input type="button" name="removedButton" value=" << "
		                    onclick="move('mspptFilterTo','mspptFilterFrom')">
							</td>
						</tr>
						</table>
        			</td>
        			<td>
        				<select id="mspptFilterTo" name="mspptFilterTo" multiple class="standardText" size="10" style="width:250">
        				</select>
        			</td>
        		</tr>
				</table>
                </div>
    		</td>
  		</tr>
  		<tr valign="top">
    		<td colspan=3>
                <br/>        
                <div id="toShowooFilter" style="cursor:pointer;display:inline-block;" onclick="initConfigShow('toShowooFilter','ooFilterPanel')">
                    <span style="display:show;"><img src="/globalsight/images/enlarge.jpg"/></span>
                    <b><%=bundle.getString("lb_filter_ooFilter")%></b>
                </div>
                <div id="ooFilterPanel" style="display:none;">
                
      			<table border="0" class="standardText" cellpadding="2">
      			<tr>
      				<td>
      					<span><%=bundle.getString("lb_all") + " "+ bundle.getString("lb_filter_ooFilter")%>s
      				</td>
      				<td>&nbsp;</td>
      				<td>
      					<span><%=bundle.getString("lb_selected") + " "+ bundle.getString("lb_filter_ooFilter")%>s
      				</td>
      			</tr>
        		<tr>
        			<td>
        				<select id="ooFilterFrom" name="ooFilterFrom" multiple class="standardText" size="10" style="width:250">
        				<c:forEach var="op" items="${oofilter}">
	      					<option title="${op.filterName}" value="filter-openoffice_filter-${op.id}">${op.filterName}</option>
	    				</c:forEach>
        				</select>
        			</td>
        			<td>
        				<table>
						<tr>
		              	<td>
		                	<input type="button" name="addButton" value=" >> "
		                    onclick="move('ooFilterFrom','ooFilterTo')"><br>
		              	</td>
		            	</tr>
		            	<tr><td>&nbsp;</td></tr>
		            	<tr>
		                	<td>
		                	<input type="button" name="removedButton" value=" << "
		                    onclick="move('ooFilterTo','ooFilterFrom')">
							</td>
						</tr>
						</table>
        			</td>
        			<td>
        				<select id="ooFilterTo" name="ooFilterTo" multiple class="standardText" size="10" style="width:250">
        				</select>
        			</td>
        		</tr>
				</table>
                </div>
    		</td>
  		</tr>
  		<tr valign="top">
    		<td colspan=3>
                <br/>        
                <div id="toShowPlaintextFilter" style="cursor:pointer;display:inline-block;" onclick="initConfigShow('toShowPlaintextFilter','plaintextFilterPanel')">
                    <span style="display:show;"><img src="/globalsight/images/enlarge.jpg"/></span>
                    <b><%=bundle.getString("lb_filter_plaintextfilter")%></b>
                </div>
                <div id="plaintextFilterPanel" style="display:none;">
                
      			<table border="0" class="standardText" cellpadding="2">
      			<tr>
      				<td>
      					<span><%=bundle.getString("lb_all") + " "+ bundle.getString("lb_filter_plaintextfilter")%>s
      				</td>
      				<td>&nbsp;</td>
      				<td>
      					<span><%=bundle.getString("lb_selected") + " "+ bundle.getString("lb_filter_plaintextfilter")%>s
      				</td>
      			</tr>
        		<tr>
        			<td>
        				<select id="plaintextFilterFrom" name="plaintextFilterFrom" multiple class="standardText" size="10" style="width:250">
        				<c:forEach var="op" items="${plainfilter}">
	      					<option title="${op.filterName}" value="filter-plain_text_filter-${op.id}">${op.filterName}</option>
	    				</c:forEach>
        				</select>
        			</td>
        			<td>
        				<table>
						<tr>
		              	<td>
		                	<input type="button" name="addButton" value=" >> "
		                    onclick="move('plaintextFilterFrom','plaintextFilterTo')"><br>
		              	</td>
		            	</tr>
		            	<tr><td>&nbsp;</td></tr>
		            	<tr>
		                	<td>
		                	<input type="button" name="removedButton" value=" << "
		                    onclick="move('plaintextFilterTo','plaintextFilterFrom')">
							</td>
						</tr>
						</table>
        			</td>
        			<td>
        				<select id="plaintextFilterTo" name="plaintextFilterTo" multiple class="standardText" size="10" style="width:250">
        				</select>
        			</td>
        		</tr>
				</table>
                </div>
    		</td>
  		</tr>
  		<tr valign="top">
    		<td colspan=3>
                <br/>        
                <div id="toShowpoFilter" style="cursor:pointer;display:inline-block;" onclick="initConfigShow('toShowpoFilter','poFilterPanel')">
                    <span style="display:show;"><img src="/globalsight/images/enlarge.jpg"/></span>
                    <b><%=bundle.getString("lb_filter_pofilter")%></b>
                </div>
                <div id="poFilterPanel" style="display:none;">
                
      			<table border="0" class="standardText" cellpadding="2">
      			<tr>
      				<td>
      					<span><%=bundle.getString("lb_all") + " "+ bundle.getString("lb_filter_pofilter")%>s
      				</td>
      				<td>&nbsp;</td>
      				<td>
      					<span><%=bundle.getString("lb_selected") + " "+ bundle.getString("lb_filter_pofilter")%>s
      				</td>
      			</tr>
        		<tr>
        			<td>
        				<select id="poFilterFrom" name="poFilterFrom" multiple class="standardText" size="10" style="width:250">
        				<c:forEach var="op" items="${pofilter}">
	      					<option title="${op.filterName}" value="filter-po_filter-${op.id}">${op.filterName}</option>
	    				</c:forEach>
        				</select>
        			</td>
        			<td>
        				<table>
						<tr>
		              	<td>
		                	<input type="button" name="addButton" value=" >> "
		                    onclick="move('poFilterFrom','poFilterTo')"><br>
		              	</td>
		            	</tr>
		            	<tr><td>&nbsp;</td></tr>
		            	<tr>
		                	<td>
		                	<input type="button" name="removedButton" value=" << "
		                    onclick="move('poFilterTo','poFilterFrom')">
							</td>
						</tr>
						</table>
        			</td>
        			<td>
        				<select id="poFilterTo" name="poFilterTo" multiple class="standardText" size="10" style="width:250">
        				</select>
        			</td>
        		</tr>
				</table>
                </div>
    		</td>
  		</tr>
  		<tr valign="top">
    		<td colspan=3>
                <br/>        
                <div id="toShowqaFilter" style="cursor:pointer;display:inline-block;" onclick="initConfigShow('toShowqaFilter','qaFilterPanel')">
                    <span style="display:show;"><img src="/globalsight/images/enlarge.jpg"/></span>
                    <b><%=bundle.getString("lb_filter_qafilter")%></b>
                </div>
                <div id="qaFilterPanel" style="display:none;">
                
      			<table border="0" class="standardText" cellpadding="2">
      			<tr>
      				<td>
      					<span><%=bundle.getString("lb_all") + " "+ bundle.getString("lb_filter_qafilter")%>s
      				</td>
      				<td>&nbsp;</td>
      				<td>
      					<span><%=bundle.getString("lb_selected") + " "+ bundle.getString("lb_filter_qafilter")%>s
      				</td>
      			</tr>
        		<tr>
        			<td>
        				<select id="qaFilterFrom" name="qaFilterFrom" multiple class="standardText" size="10" style="width:250">
        				<c:forEach var="op" items="${qafilter}">
	      					<option title="${op.filterName}" value="filter-qa_filter-${op.id}">${op.filterName}</option>
	    				</c:forEach>
        				</select>
        			</td>
        			<td>
        				<table>
						<tr>
		              	<td>
		                	<input type="button" name="addButton" value=" >> "
		                    onclick="move('qaFilterFrom','qaFilterTo')"><br>
		              	</td>
		            	</tr>
		            	<tr><td>&nbsp;</td></tr>
		            	<tr>
		                	<td>
		                	<input type="button" name="removedButton" value=" << "
		                    onclick="move('qaFilterTo','qaFilterFrom')">
							</td>
						</tr>
						</table>
        			</td>
        			<td>
        				<select id="qaFilterTo" name="qaFilterTo" multiple class="standardText" size="10" style="width:250">
        				</select>
        			</td>
        		</tr>
				</table>
                </div>
    		</td>
  		</tr>
  		<tr valign="top">
    		<td colspan=3>
                <br/>        
                <div id="toShowxmlFilter" style="cursor:pointer;display:inline-block;" onclick="initConfigShow('toShowxmlFilter','xmlFilterPanel')">
                    <span style="display:show;"><img src="/globalsight/images/enlarge.jpg"/></span>
                    <b><%=bundle.getString("lb_config_filter_xmlfilter")%></b>
                </div>
                <div id="xmlFilterPanel" style="display:none;">
                
      			<table border="0" class="standardText" cellpadding="2">
      			<tr>
      				<td>
      					<span><%=bundle.getString("lb_all") + " "+ bundle.getString("lb_config_filter_xmlfilter")%>s
      				</td>
      				<td>&nbsp;</td>
      				<td>
      					<span><%=bundle.getString("lb_selected") + " "+ bundle.getString("lb_config_filter_xmlfilter")%>s
      				</td>
      			</tr>
        		<tr>
        			<td>
        				<select id="xmlFilterFrom" name="xmlFilterFrom" multiple class="standardText" size="10" style="width:250">
        				<c:forEach var="op" items="${xmlfilter}">
	      					<option title="${op.filterName}" value="filter-xml_rule_filter-${op.id}">${op.filterName}</option>
	    				</c:forEach>
        				</select>
        			</td>
        			<td>
        				<table>
						<tr>
		              	<td>
		                	<input type="button" name="addButton" value=" >> "
		                    onclick="move('xmlFilterFrom','xmlFilterTo')"><br>
		              	</td>
		            	</tr>
		            	<tr><td>&nbsp;</td></tr>
		            	<tr>
		                	<td>
		                	<input type="button" name="removedButton" value=" << "
		                    onclick="move('xmlFilterTo','xmlFilterFrom')">
							</td>
						</tr>
						</table>
        			</td>
        			<td>
        				<select id="xmlFilterTo" name="xmlFilterTo" multiple class="standardText" size="10" style="width:250">
        				</select>
        			</td>
        		</tr>
				</table>
                </div>
    		</td>
  		</tr>
	</table>
</td>
</tr>
</table>
  
</body>
</html>
