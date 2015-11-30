
function showHourglass()
{
    downloadForm.apply.disabled = true;
    downloadForm.cancel.disabled = true;
    idBody.style.cursor = "wait";
}

function checkForm()
{
    showHourglass();

    return true;
}


function init()
{
    doOnLoad();

    window.focus();
    formatSelection();

    loadGuides();
    $("select[name='formatSelector']").change(formatSelection);
	tmxTypeSelection();	
	$("#tmxTypeSelector").change(tmxTypeSelection);
}

function tmxTypeSelection(){
	var val=$("#tmxTypeSelector").val();
	var format=$("select[name='formatSelector']").val();
	if(val.match("resInsTmx")){
		$(".tmxTypeSelector").show();
		if("OmegaT" == format){
		   $("#penalizedReferenceTm").hide();
		   $("#separateTMfileTD").hide();
	    }else{
		   $("#penalizedReferenceTm").show();
		   $("#separateTMfileTD").show();
	    }
	}else{
		$(".tmxTypeSelector").hide();
		$("#penalizedReferenceTm").hide();
	}
}

function formatSelection(){
	$("#ptagSelector").attr("disabled",false); 
	$(".unOmegaT").show();
	$(".OmegaT").show();
	$(".TTX").show();
	var resTermSelector=$("#resTermSelector");
	var val=$("select[name='formatSelector']").val();
	var tmxType=$("#tmxTypeSelector").val();
	if("OmegaT" == val){
		$("#penalizedReferenceTm").hide();
		$("#separateTMfileTD").hide();
	}else{
	    if(tmxType.match("resInsTmx")){
		   $("#penalizedReferenceTm").show();
		}else{
		   $("#penalizedReferenceTm").hide();
		}
	    $("#separateTMfileTD").show();
	}
	$("#tmxTypeSelector").children("span").each(function(){
		 $(this).children().clone().replaceAll($(this)); 
		});
	var key=optionKey[val];
	key();
}

var populateShow=function(){
	disOmegaT();
	$("#includeXmlNodeContextInformationBox").hide();
	$(".formatAcces").hide();
	$('#populate100').show();
}
var populateHide=function(){
	$(".formatAcces").show();
	$('#populatefuzzy').hide();
}
var populate=function(){
	$("#includeXmlNodeContextInformationBox").hide();
	$(".formatAcces").show();
	disOmegaT();
}
var disPtag=function(){
	$("#ptagSelector").val('compact');
	$("#ptagSelector").attr("disabled",true); 
}
var disOmegaT=function(){
	var OmegaT=$(".OmegaT");
	OmegaT.each(function(){
				$(this).hide();
				if($(this).val()==$("#resTermSelector").val()){
					$("#resTermSelector").val("termHtml");
				}
			}							
	)
}

var optionKey={"rtfTradosOptimized":populate,"rtf":populateShow,
				"TTX":function(){
					$("#includeXmlNodeContextInformationBox").hide();
					$("#separateTMfileTD").show();
					populateShow();
					disPtag();
					var ttx=$(".TTX");
					ttx.each(function(){
						$(this).hide();
						$(this).wrap("<span style='display:none'></span>");
						if($(this).val()==$("#tmxTypeSelector").val()){
						$("#tmxTypeSelector").val("resInsTmx14b");
						$(".tmxTypeSelector").show();
					}
					})
					
				},
				"xlf12":function(){
					$("#includeXmlNodeContextInformationBox").show();
					$("#separateTMfileTD").show();
					populateHide();
					disPtag();
					disOmegaT();
				},
				"Xliff 2.0":function(){
					$("#includeXmlNodeContextInformationBox").show();
					$("#separateTMfileTD").show();
					populateHide();
					disPtag();
					disOmegaT();
				},
				"OmegaT":function(){
					$("#includeXmlNodeContextInformationBox").show();
					populateHide();
					$('#populate100').hide();
					$("#separateTMfileTD").hide();
					disPtag();	
					var unOmegaT=$(".unOmegaT");
					unOmegaT.each(function(){
								$(this).hide();
								if($(this).val()==$("#resTermSelector").val()){
									$("#resTermSelector").val("tbx");
								}
							}							
					)
				}
			};

function setClientDwnldOptions(formSent)
{
   

    // get selectors
    var formatSelect = formSent.formatSelector ;
    var ptagSelect = formSent.ptagSelector;
    var resInsModeSelect = formSent.resInsertionSelector;
    // Set user defaults
	var terminologySelect = formSent.termSelector;
    var tmEditTypeSelector=formSent.TMEditType;
    // We only set format/editor/encoding if we have all three values
    if (dwnldOpt.format.length > 0)
    {
	// Set FileFormat
	for(i = 0; i < formatSelect.length; i++)
	{
            if (formatSelect.options[i].value == dwnldOpt.format)
            {
                formatSelect.selectedIndex = i;
				break;
            }
	}
	
    }
    // Set Placeholder
    if (dwnldOpt.placeholder)
    {
      for(i = 0; i < ptagSelect.length; i++)
      {
         if (ptagSelect.options[i].value == dwnldOpt.placeholder)
         {
            ptagSelect.selectedIndex = i;
            break;
         }
      }
    }
    // Set resource insertion mode
    if (dwnldOpt.resInsSelector)
    {
      for(i = 0; i < resInsModeSelect.length; i++)
      {
            if (resInsModeSelect.options[i].value == dwnldOpt.resInsSelector)
            {
                resInsModeSelect.selectedIndex = i;
                break;
            }
      }
    }
    

  //  if (dwnldOpt.changeCreationIdForMT == 'yes')
 //   {
 //   	document.getElementById("changeCreationIdForMT").checked = true;
//    }
    
    if (dwnldOpt.separateTMfile == 'yes')
    {
    	document.getElementById("separateTMfile").checked = true;
    }
    
    if (dwnldOpt.penalizedReferenceTmPre == 'yes')
    {
    	$("#penalizedReferenceTmPre").attr("checked", true);
    	$("#penalizedReferenceTmPer").attr("checked", false);
    }
    
    if (dwnldOpt.penalizedReferenceTmPer == 'yes')
    {
    	$("#penalizedReferenceTmPer").attr("checked", true);
    	$("#penalizedReferenceTmPre").attr("checked", false);
    }

	if(dwnldOpt.termSelector)
	{
		for(var i = 0; i < terminologySelect.length; i++)
		{
			//alert(terminologySelect.options[i].value);
			if(terminologySelect.options[i].value == dwnldOpt.termSelector)
			{
				terminologySelect.selectedIndex = i; 
				break;
			}
		}
	}
	
	if(dwnldOpt.TMEditType)
	{
		if (tmEditTypeSelector) {
			for(var i = 0; i < tmEditTypeSelector.length; i++)
			{
				if(tmEditTypeSelector.options[i].value == dwnldOpt.TMEditType)
				{
					tmEditTypeSelector.selectedIndex = i; 
					break;
				}
			}
		}
	}

	if (dwnldOpt.populate100)
	{
		if(dwnldOpt.populate100 == 'false' || dwnldOpt.populate100 == 'no')
		{
			document.getElementById("populate100CheckBox").checked = false;
		}
	}

	if (dwnldOpt.populatefuzzy)
	{
		if(dwnldOpt.populatefuzzy == 'false' || dwnldOpt.populatefuzzy == 'no')
		{
			document.getElementById("populatefuzzyCheckBox").checked = false;
		}
	}
	
	if (dwnldOpt.consolidateFileType)
	{
		$("#consolidateFileType").val(dwnldOpt.consolidateFileType);
		setWordCountDisplay();
		if (dwnldOpt.wordCountForDownload)
		{
			$("#wordCountForDownload").val(dwnldOpt.wordCountForDownload);
		}
		else
		{
			$("#wordCountForDownload").val('1000');
		}
	}
	
	if (dwnldOpt.preserveSourceFolder)
	{
		if(dwnldOpt.preserveSourceFolder == 'false' || dwnldOpt.preserveSourceFolder == 'no')
		{
			document.getElementById("preserveSourceFolder").checked = false;
		}
		else
		{
			if($("#consolidateFileType").val() == "consolidate")
			{
				$("#consolidateFileType").val("notConsolidate");
			}
		}
	}
	
	if (dwnldOpt.includeRepetitions)
	{
		if(dwnldOpt.includeRepetitions == 'false' || dwnldOpt.includeRepetitions == 'no')
		{
			document.getElementById("includeRepetitions").checked = false;
		}
	}
	
	if (dwnldOpt.excludeFullyLeveragedFiles)
	{
		if(dwnldOpt.excludeFullyLeveragedFiles == 'false' || dwnldOpt.excludeFullyLeveragedFiles == 'no')
		{
			document.getElementById("excludeFullyLeveragedFiles").checked = false;
		}
	}
	
	if (dwnldOpt.includeXmlNodeContextInformation)
	{
		if(dwnldOpt.includeXmlNodeContextInformation == 'false' || dwnldOpt.includeXmlNodeContextInformation == 'no')
		{
			document.getElementById("includeXmlNodeContextInformation").checked = false;
		}
	}
}

function doOnLoad()
{
    loadGuides();
    setClientDwnldOptions(document.downloadForm);
}


function in_array(stringToSearch, arrayToSearch) {
	for (s = 0; s < arrayToSearch.length; s++) {
		thisEntry = arrayToSearch[s].toString();
		if (thisEntry == stringToSearch) {
			return true;
		}
	}
	return false;
}





function submitForm()
{
	if($("#consolidateFileType").val() == "consolidateByWordCount")
	{
		tmp =$("#wordCountForDownload").val().trim();
		if (tmp == "") {
			alert("Please input word count.");
			return;
		}
		
		if (!isAllDigits(tmp)) {
			alert("Invalid word count, only integer numbers are allowed.");
			return;
		}
		
		if(Number(tmp) < 1000){
			alert("The minimum word count is 1000.");
			return;
		}
	}
	
	saveUserOptions(downloadForm);
}


function saveUserOptions(formSent)
{
    //var duration = 12; // months
    var formatSelect = formSent.formatSelector;
    var ptagSelect = formSent.ptagSelector;
    var resInsModeSelect = formSent.resInsertionSelector;

	var terminologySelect = formSent.termSelector;

    var cookieValFileFormat = formatSelect.options[formatSelect.selectedIndex].value;
    
    var cookieValPtagFormat = ptagSelect.options[ptagSelect.selectedIndex].value;
    var cookieValResInsMode = resInsModeSelect.options[resInsModeSelect.selectedIndex].value;
    var cookieValTerm = terminologySelect.options[terminologySelect.selectedIndex].value;
    
    
    var editExactSelect = formSent.TMEditType;
    var cookieValEditExact = 0;
	if (editExactSelect) {
		cookieValEditExact = editExactSelect.options[editExactSelect.selectedIndex].value;
	}

	downloadForm.action += buildParams(cookieValFileFormat,  cookieValPtagFormat, cookieValResInsMode,cookieValEditExact, cookieValTerm);
	dsubmit();
}

function buildParams(cookieValFileFormat, cookieValPtagFormat, cookieValResInsMode,cookieValEditExact,  cookieValTerm)
{
	var str = "&";
	str += "format=" + cookieValFileFormat;
	str += "&placeholder=" + cookieValPtagFormat;
	str += "&resInsSelector=" + cookieValResInsMode;
	if(cookieValEditExact){
		str += "&editExact=yes" ;
	}
	str += "&TMEditType=" + cookieValEditExact;
	str += "&termSelector=" + cookieValTerm;
	return str;
}

function uniquenessCheck(obj)
{
	if(obj == 'preserveSourceFolder')
	{
		if(document.getElementById("preserveSourceFolder").checked == true 
				&& $("#consolidateFileType").val() == "consolidate")
		{
			$("#consolidateFileType").val("notConsolidate");
		}
	}
	else if(obj == 'needConsolidate')
	{
		if($("#consolidateFileType").val() == "consolidate")
			document.getElementById("preserveSourceFolder").checked = false;
	}
}
