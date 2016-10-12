window.focus();

var url = getDataUrl;
$(
	function()
	{
		updateFileNavigationArrow();
		getDataByFrom(url);
	}
);

function getDataByFrom(url){
	$.getJSON(url+"&action=getPictureData&random="+Math.random(), function(data) {
		if(data != "null" || data != null)
		{
			$("#sourceImg").attr("src",data.sourceImagePath);
			$("#targetImg").attr("src",data.targetImagePath);
			targetImageSuffix = data.targetImageSuffix;
		}
	});
}

function openOriginalImage(ImgD)
{
	var image=new Image();  
	image.src=ImgD.src;  
	var divWidth = image.width;
	var divHeight = image.height;
	
	if(divWidth > 1400)
	{
		divWidth = 1400;
	}
	else
	{
		divWidth = divWidth + 30;
	}
	
	if(divHeight > 700)
	{
		divHeight = 700;
	}
	else
	{
		divHeight = divHeight + 30;
	}
	
	$("#hiddenImg").attr("src",image.src);
	document.getElementById("hiddenDiv").style.display="block";
	$("#hiddenImageDiv").dialog({width:divWidth, height:divHeight,resizable:true});
    
}

function closeDiv()
{
	$('#hiddenImageDiv').dialog('close')
	document.getElementById("hiddenDiv").style.display="none";
}

function scaleImageSize(ImgD)
{
	var iwidth = (window.screen.width-20)/2;
	var iheight =  window.screen.height-200;
	
	var image=new Image();  
	image.src=ImgD.src;  
	if(image.width>0 && image.height>0)
	{
		if(image.width >= iwidth && image.height >= iheight)
		{
			if(image.width/iwidth >= image.height/iheight)
			{
				ImgD.width = iwidth;
				ImgD.height = (image.height * iwidth)/ image.width;
			}
			else
			{
				ImgD.width = (image.width * iheight)/image.height
				ImgD.height = iheight;
			}
		}
		else if(image.width >= iwidth && image.height < iheight)
		{
			ImgD.width = iwidth;
			ImgD.height = (image.height * iwidth)/ image.width;
		}
		else if(image.width < iwidth && image.height >= iheight)
		{
			ImgD.width = (image.width * iheight)/image.height
			ImgD.height = iheight;
		}
	} 
}

function refresh(direction)
 {
	if (direction == '-1' && isPicturePreviousFile == 'false' || isPicturePreviousFile == false)
	{
		if(openEditorType != null && openEditorType != "")
		{
			if(openEditorType == "postReviewEditor")
			{
				window.open(url_previousPostRwEditor, '_self');
			}
			else if(openEditorType == "popupEditor")
			{
				window.open(url_previousPopupEditor, '_self');
			}
		}
	}
	else if(direction == '1' && isPictureNextFile == 'false' || isPictureNextFile == false)
	{
		if(openEditorType == "postReviewEditor")
		{
			window.open(url_nextPostRwEditor,'_self');
		}
		else if(openEditorType == "popupEditor")
		{
			window.open(url_nextPopupEditor, '_self');
		}
	}
	else
	{
		if(w_editor)
	     {
	     	w_editor.close();
	     }
	 	document.location = url_self + "&action=refresh&refresh=" + direction+"&random="+Math.random();
	}
 }
 
 function updateFileNavigationArrow()
 {
 	if (isFirstPage == 'false')
 	{
 		var fileNavPre = "<A HREF='#' onclick='refresh(-1); return false;' onfocus='this.blur()'>"
 			+ "<IMG SRC='/globalsight/images/editorPreviousPage.gif' BORDER=0 HSPACE=2 VSPACE=4></A>";
 		document.getElementById("fileNavPre").innerHTML = fileNavPre;
 	}

 	if (isLastPage == 'false')
 	{
         var fileNavNext = "<A HREF='#' onclick='refresh(1); return false;' onfocus='this.blur()'>"
             + "<IMG SRC='/globalsight/images/editorNextPage.gif' BORDER=0 HSPACE=2 VSPACE=4></A>";
         document.getElementById("fileNavNext").innerHTML = fileNavNext;
 	}
 }
 
 function helpSwitch()
 {
     helpWindow = window.open(helpFile,'helpWindow',
       'resizable=yes,scrollbars=yes,WIDTH=600,HEIGHT=400');
     helpWindow.focus();
 }

 function closeWindow()
 {
 	window.close();
 }
 
 function CanClose()
 {
 	if(w_editor)
     {
     	w_editor.close();
     }
 	return true;
 }