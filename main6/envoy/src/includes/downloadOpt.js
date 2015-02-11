
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
    $("select[name='formatSelector']").change(formatSelection)
	tmxTypeSelection();	
	$("#tmxTypeSelector").change(tmxTypeSelection)

}

function tmxTypeSelection(){
	var val=$("#tmxTypeSelector").val();
	if(val.match("resInsTmx")){
		$(".tmxTypeSelector").show();
	}else{
		$(".tmxTypeSelector").hide();
	}
}

function formatSelection(){
	$("#ptagSelector").attr("disabled",false); 
	$(".unOmegaT").show();
	$(".OmegaT").show();
	$(".TTX").show();
	var resTermSelector=$("#resTermSelector");
	//$(".formatAcces").hide();
	var val=$("select[name='formatSelector']").val();
	
	var key=optionKey[val];
	key();
}

var populateShow=function(){
	disOmegaT();
	$(".formatAcces").hide();
	$('#populate100').show();
}
var populateHide=function(){
	$(".formatAcces").show();
	$('#populatefuzzy').hide();
}
var populate=function(){
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
					populateShow();
					disPtag();
					var ttx=$(".TTX");
					ttx.each(function(){
						$(this).hide();
						if($(this).val()==$("#tmxTypeSelector").val()){
						$("#tmxTypeSelector").val("resInsTmx14b");
						$(".tmxTypeSelector").show();
					}
					})
					
				},
				"xlf12":function(){
					populateHide();
					disPtag();
					disOmegaT();
				},
				"OmegaT":function(){
					populateHide();
					$('#populate100').hide();
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
    

    if (dwnldOpt.changeCreationIdForMT == 'yes')
    {
    	document.getElementById("changeCreationIdForMT").checked = true;
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
	
	if (dwnldOpt.needConsolidate)
	{
		if(dwnldOpt.needConsolidate == 'false' || dwnldOpt.needConsolidate == 'no')
		{
			document.getElementById("needConsolidate").checked = false;
		}
	}
	
	if (dwnldOpt.preserveSourceFolder)
	{
		if(dwnldOpt.preserveSourceFolder == 'false' || dwnldOpt.preserveSourceFolder == 'no')
		{
			document.getElementById("preserveSourceFolder").checked = false;
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
		if(document.getElementById("preserveSourceFolder").checked == true)
			document.getElementById("needConsolidate").checked = false;
	}
	else if(obj == 'needConsolidate')
	{
		if(document.getElementById("needConsolidate").checked == true)
			document.getElementById("preserveSourceFolder").checked = false;
	}
}