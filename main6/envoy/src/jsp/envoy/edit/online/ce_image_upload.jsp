<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="error.jsp"
    import="com.globalsight.util.edit.EditUtil,
            com.globalsight.util.edit.GxmlUtil,
            com.globalsight.everest.edit.online.CommentView,
            com.globalsight.everest.webapp.WebAppConstants,
            com.globalsight.everest.webapp.javabean.NavigationBean,
            com.globalsight.everest.webapp.pagehandler.PageHandler,
            com.globalsight.everest.servlet.util.SessionManager,
            com.globalsight.util.resourcebundle.ResourceBundleConstants,
            com.globalsight.everest.webapp.pagehandler.terminology.management.FileUploadHelper,
            com.globalsight.everest.foundation.User,
            java.io.File,
            java.util.*"
    session="true"
%>
<jsp:useBean id="self" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<%
ResourceBundle bundle = PageHandler.getBundle(session);
String uploadUrl = self.getPageURL() + "&commentUpload=true";
SessionManager sessionMgr = (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
CommentView view =
  (CommentView)sessionMgr.getAttribute(WebAppConstants.COMMENTVIEW);
long tuvId = view.getTuvId();
String termImgPath = FileUploadHelper.DOCROOT + "terminologyImg";
File parentFilePath = new File(termImgPath.toString());
File[] files = parentFilePath.listFiles();
String imgName = "";
String displayDiv = "";//"block"
       
if (files != null && files.length > 0) {
    for (int j = 0; j < files.length; j++) {
        File file = files[j];
        String fileName = file.getName();
                    
        if(fileName.lastIndexOf(".") > 0) {
            String tempName= fileName.substring(0, fileName.lastIndexOf("."));
            String nowImgName =  "tuv_" + Long.toString(tuvId);
                        
            if(tempName.equals(nowImgName)) {
                imgName = fileName;
                displayDiv = "None";
                break;
            }
        }
    }
}  
%>
<script>
function checkExtension(path)
{
  if (path != null && path != "")
  {
    var index = path.lastIndexOf(".");
    if (index < 0)
    {
      return;
    }

    var ext = path.substring(index + 1);
    
    if(ext.toLowerCase() != "jpg" && ext.toLowerCase() != "gif"
        && ext.toLowerCase() != "png" && ext.toLowerCase() != "bmp" 
        && ext.toLowerCase() != "tif" && ext.toLowerCase() != "tiff"
        && ext.toLowerCase() != "jpeg" && ext.toLowerCase() != "jpe"
        && ext.toLowerCase() != "jfif" && ext.toLowerCase() != "dib") {
            
        alert("<%=bundle.getString("lb_terminolog_img_extension")%>");
        // clear the upload file content
        document.uploaderForm.uploadImgFile.value = "";
        document.uploaderForm.uploadImgFile.outerHTML = document.uploaderForm.uploadImgFile.outerHTML;
        
        return false;
   }

  }
}

function doUpload() {
    if(document.uploaderForm.uploadImgFile.value=="") {
        alert("<%=bundle.getString("jsmsg_upload_no_file") %>");
        return false;
    }

    document.uploaderForm.submit();
}

function termImgShow(imgName) {
    window.open ('terminologyImg/'+ imgName,'newwindow','top=0,left=0,toolbar=no,menubar=no,scrollbars=yes, resizable=yes,location=no, status=no') ;
}

function deleteTermImg(imgName) {
    var obj = {termImgName:imgName};
    sendAjax(obj, "deleteTermImg", "deletermImgOver");
}

function deletermImgOver() {
    alert("<%=bundle.getString("lb_delete_termImg_success")%>");
    uploaderTR.style.display = "";
    termImgTR.style.display = "none";
}
</script>
<html>
<head>
<style>
BODY, #idTable, #idTable2 { font-family: verdana; font-size: 10pt; }
BODY { margin: 0; }
#idTable { margin-left: 10px; margin-right: 10px; }

.clickable  { cursor: hand; cursor:pointer; }
.label      { font-weight: bold; }
</style>

<SCRIPT SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT SRC="/globalsight/includes/library.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/Ajax.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/dojo.js"></SCRIPT>
</head>
<body>
  <table id="idTable" width="100%" border=0 >
  <%
      if(!imgName.equals("")) {
  %>
  <tr id="termImgTR">
    <td width=60>
  <span class="label"><%=bundle.getString("lb_uploaded_image") %>:</span>
  </td>
  <td>
     <img id="termImg" class="clickable" border=0 src="terminologyImg/<%=imgName%>" width="50" height="50" onclick="termImgShow('<%=imgName%>')">
     &nbsp;&nbsp;&nbsp;
     <a href="#" onclick="deleteTermImg('<%=imgName%>');">Delete</a>
  </td>
  </tr>
  <%
    }
  %>
  <tr id="uploaderTR" style="display:<%=displayDiv%>">
    <td width=60>
      <span class="label"><%=bundle.getString("lb_tuv_comment_image")%></span>
    </td>
    <td>
    <FORM NAME="uploaderForm" ACTION="<%=uploadUrl%>" ENCTYPE="multipart/form-data" METHOD="post">
      <INPUT TYPE="file" NAME="uploadImgFile" onchange="checkExtension(this.value)" SIZE=40></INPUT> 
      <input type="button" id="idUpload" value=Upload onclick="doUpload()" tabindex="0">
    </FORM>
  </td>
  </tr>
    </table>
</body>
</html>
