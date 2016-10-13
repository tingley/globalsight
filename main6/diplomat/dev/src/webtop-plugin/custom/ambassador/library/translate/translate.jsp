<%@ page import="com.documentum.web.form.Form,
                 com.documentum.webcomponent.navigation.doclist.DocList,
                 com.documentum.web.common.BrandingService,
                 com.documentum.webcomponent.navigation.AbstractNavigation,
                 com.documentum.web.dragdrop.IDropTarget,
                 com.documentum.ambassador.library.translate.Translate"%>
<%
//
%>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page errorPage="/wdk/errorhandler.jsp" %>
<%@ taglib uri="/WEB-INF/tlds/dmform_1_0.tld" prefix="dmf" %>
<%@ taglib uri="/WEB-INF/tlds/dmformext_1_0.tld" prefix="dmfx" %>
<dmf:html>
  <dmf:head>
    <dmf:webform validation='false'/>
    <script language='JavaScript1.2' src='<%=Form.makeUrl(request, "/wdk/include/dynamicAction.js")%>'>
    </script>
  </dmf:head>
  <dmf:body cssclass='contentBackground'>
<dmf:form keepfresh='true'>
<table border="0" cellpadding="0" cellspacing="0">
  <tr height="10" class="spacer">
    <td colspan="3">&nbsp;</td>
  </tr>
<% // %>
  <tr>
    <td nowrap align="right" valign="middle" width="20%">
      <dmf:label cssclass="shortfieldlabel" nlsid="MSG_JOB_NAME_COLON"/>
    </td>
    <td align="left" nowrap class="fieldlabel" width="70%">
      <dmf:text name='<%=Translate.CTRL_TEXT_NAME%>' size="50" id="object_name"
                tooltipnlsid="MSG_JOB_NAME_COLON"/>
      <dmfx:clientenvpanel environment='appintg' reversevisible='true'>&nbsp;
        <dmf:requiredfieldvalidator name="validator"
             controltovalidate='<%=Translate.CTRL_TEXT_NAME%>' 
             nlsid="MSG_MUST_HAVE_NAME"/>
        <dmf:utf8stringlengthvalidator name="validator"
             controltovalidate='<%=Translate.CTRL_TEXT_NAME%>' maxbytelength="255"
             nlsid="MSG_NAME_TOO_LONG"/>
        <dmf:label name="labelnormalcharsneeded" name="validator"
             nlsid="MSG_NORMAL_CHARS_NEEDED" cssclass="validatorMessageStyle"/>
      </dmfx:clientenvpanel>
      <dmfx:clientenvpanel environment='appintg'>
        <br><dmf:requiredfieldvalidator name="validator"
                 controltovalidate='<%=Translate.CTRL_TEXT_NAME%>'
                 nlsid="MSG_MUST_HAVE_NAME" indicator=''/>
        <dmf:utf8stringlengthvalidator name="validator"
             controltovalidate='<%=Translate.CTRL_TEXT_NAME%>' maxbytelength="255"
             nlsid="MSG_NAME_TOO_LONG"/>
        <dmf:label name="labelnormalcharsneeded" nlsid="MSG_NORMAL_CHARS_NEEDED"
             cssclass="validatorMessageStyle"/>
      </dmfx:clientenvpanel>
    </td>
    <td class="defaultcolumnspacer" width="10%">&nbsp;</td>
  </tr>
<% // %>
  <tr>
    <td nowrap align="right" valign="middle" scope="row">
      <dmf:label cssclass="shortfieldlabel" nlsid="MSG_FILE_PROFILES_COLON"/>
    </td>
    <td align="left" class="fieldlabel">
      <dmf:datadropdownlist
           name='<%=Translate.CTRL_DROP_LIST_NAME%>' tooltipnlsid="MSG_FILE_PROFILES_COLON" >
        <dmf:dataoptionlist>
          <dmf:option datafield='<%=Translate.DATA_DROP_LIST_ID%>'
                      labeldatafield='<%=Translate.DATA_DROP_LIST_LABEL%>' />
        </dmf:dataoptionlist>
      </dmf:datadropdownlist>
      <dmfx:clientenvpanel environment='appintg' reversevisible='true'>&nbsp;
        <dmf:requiredfieldvalidator name="validator"
             controltovalidate='<%=Translate.CTRL_DROP_LIST_NAME%>'
             nlsid="MSG_MUST_HAVE_FILE_PROFILE"/>
        <dmf:utf8stringlengthvalidator name="validator"
             controltovalidate='<%=Translate.CTRL_DROP_LIST_NAME%>'
             maxbytelength="255"
             nlsid="MSG_NAME_TOO_LONG"/>

      </dmfx:clientenvpanel>
      <dmfx:clientenvpanel environment='appintg'>
        <br><dmf:requiredfieldvalidator name="validator"
                 controltovalidate='<%=Translate.CTRL_DROP_LIST_NAME%>'
                 nlsid="MSG_MUST_HAVE_FILE_PROFILE" indicator=''/>
        <dmf:utf8stringlengthvalidator name="validator"
             controltovalidate='<%=Translate.CTRL_DROP_LIST_NAME%>'
             maxbytelength="255"
             nlsid="MSG_NAME_TOO_LONG"/>

      </dmfx:clientenvpanel>
    </td>
    <td class="defaultcolumnspacer">&nbsp;</td>
  </tr>
  <tr height="10" class="spacer">
    <td colspan="3">&nbsp;</td>
  </tr>
<% // %>
<dmfx:actionmultiselect name='<%=Translate.CTRL_MULTI_SELECT_NAME%>'
                        selectionargs='<%=Translate.PARAM_TARGET_LOCALE%>' >
<tr>
<td align="right">&nbsp;</td>
<td>
<dmfx:actionbutton name='filter' label='Filter File Profile' 
                   action='filterfileprofile' showifdisabled='true' 
                   showifinvalid='true' dynamic='multiselect'/>
</td>
<td>&nbsp;</td>
</tr>
  <tr>
    <td nowrap align="right" valign="top" scope="row">
      <dmf:label cssclass="shortfieldlabel" nlsid="MSG_SHOW_TARGET_LANGUAGES"/>
    </td>
    <td>
<dmfx:actionmultiselectcheckall cssclass='doclistbodyDatasortlink'>
  <dmf:argument name='<%=Translate.PARAM_TARGET_LOCALE%>'
                datafield='<%=Translate.LOCALE_SET_CODE%>' />
</dmfx:actionmultiselectcheckall>

<dmf:datagrid name='<%=Translate.CTRL_DATAGRID_NAME%>'
              paged='true' preservesort='false'
              width='100%' cellspacing='0' cellpadding='0' bordersize='0'>
    <dmf:datagridRow tooltipdatafield='<%=Translate.LOCALE_SET_CODE%>'
                     cssclass='defaultDatagridRowStyle' 
                     altclass="defaultDatagridRowAltStyle">
    <tr>
    <td height=24 nowrap class="doclistcheckbox" align='left'>
        <dmfx:actionmultiselectcheckbox name='checkTargetLanguage' value='true'>
          <dmf:argument name='<%=Translate.PARAM_TARGET_LOCALE%>'
                        datafield='<%=Translate.LOCALE_SET_CODE%>' />
        </dmfx:actionmultiselectcheckbox>
    </td>
    <td align='left' nowrap class="doclistlocicon">
      <dmf:label datafield='<%=Translate.LOCALES%>' />
    </td>
    <td>
<dmfx:actionimage name='propact' action='properties'
                  src='icons/info.gif' showifdisabled='true' 
                  showifinvalid='true' dynamic='generic'>
</dmfx:actionimage>
    </td>
    </tr>
    </dmf:datagridRow>
    <dmf:nodataRow>
    <tr>
    <td colspan=3 height=24>
      <dmf:label nlsid='MSG_NO_TARGET_LOCALE'/>
    </td>
    </tr>
    </dmf:nodataRow>
</dmf:datagrid>
</dmfx:actionmultiselect>
</td>
    <td class="defaultcolumnspacer">&nbsp;</td>
  </tr>
<% // %>

</table>
</dmf:form>
</dmf:body>
</dmf:html>
