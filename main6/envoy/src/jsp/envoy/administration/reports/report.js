/**
 * The Object JobInfo is used for storing Job data.
 * 
 * @param p_jobId			int
 * @param p_jobName			String
 * @param p_projectId		int
 * @param p_jobStatus		String
 * @param p_targetLocals	String(target local array string)
 * @returns {JobInfo}
 */
function JobInfo(p_jobId, p_jobName, p_projectId, p_jobStatus, p_targetLocals) 
{
	this.jobId = p_jobId;
	this.jobName = p_jobName;
	this.projectId = p_projectId;
	this.jobStatus = p_jobStatus;
	this.targetLocals = p_targetLocals.split(",");

	this.addTargetLocal = function(p_targetLocal) {
		this.targetLocals[this.targetLocals.length] = p_targetLocal;
	};
}

function GlobalSightLocale(p_id, p_displayName){
	this.id = p_id;
	this.displayName= p_displayName;
}

// If array contains item, then return true.
function contains(array, item){
	for(var i=0; i<array.length; i++){
		if(array[i]=="*" || array[i]==item){
			return true;
		}
	}
	return false;
}

// If array1 contains any value of array2, then return true.
function containsArray(array1, array2){
	for(var i=0; i<array2.length; i++){
		if(contains(array1, array2[i])){
			return true;
		}
	}
	return false;
}

/**
 * Disable/Enable TR element
 * 
 * @param trId
 *            The id of TR item
 * @param isDisabled
 *            Disable/Enable flag
 */
function setDisableTR(trId, isDisabled) {
	var trElem = document.getElementById(trId);
	var color;
	if (isDisabled) {
		color = "gray";
	} else {
		color = "black";
	}
	trElem.style.color = color;

	// Operate select elements
	var elems = trElem.getElementsByTagName("select");
	for ( var i = 0; i < elems.length; i++) {
		elems[i].disabled = isDisabled;
		elems[i].style.color = color;
		
		//for Chrome and IE9 display normal.
		var elemsOption = trElem.getElementsByTagName("option");
		for (var j = 0; j < elemsOption.length; j++) {
			if( trElem.getElementsByTagName("option")[j].selected == true ) {
				trElem.getElementsByTagName("option")[j].style.color = "#FFF";
				trElem.getElementsByTagName("option")[j].style.backgroundColor = "#CCC";
			}
		}
	}
	
	// Operate text elements
	elems = trElem.getElementsByTagName("input");
	for ( var i = 0; i < elems.length; i++) {
		if ("text" == elems[i].type) {
			elems[i].disabled = isDisabled;
			elems[i].style.color = color;
		}
	}
}

/**
 * Adds option to select element.
 * 
 * @param selId
 *            the id of select element
 * @param text
 *            text/display name
 * @param value
 */
function addOption(selId, text, value) {
	var item = new Option(text, value);
	item.setAttribute("title", text);
	document.getElementById(selId).options.add(item);
}

/**
 * The input jobID should be valid.
 * @param jobIDArr 		input jobID array
 * @param jobInfos 		jobInfo array.If need't check in jobInfos, then set Null.
 */
function validateIDS(jobIDArr, jobInfos){	
	if(!jobIDArr[0] || jobIDArr.length == 0){
		return false;
	}
	
	var patrn = /^\s*\d*\s*$/;
	for(var i=0; i<jobIDArr.length; i++){
		if(!patrn.test(jobIDArr[i]))
			return false;
		if(jobInfos!=null && getJobInfosIndex(jobInfos, jobIDArr[i])== -1)
			return false;
	}
	
	return true;
}

/**
 * Target locales must be valid for every jobId.
 * @param jobIDArr 		input job id array
 * @param tlIDStr		selected target locales ID array
 * @param jobInfos		jobInfo array
 */
function isContainValidTargetLocale(jobIDArr, tlIDArr, jobInfos){
	for(var i=0; i<jobIDArr.length; i++)
	{
		var index = getJobInfosIndex(jobInfos, jobIDArr[i]);
		if(index==-1 || !containsArray(tlIDArr, jobInfos[index].targetLocals))
		{
			return true;
		}
	}	
		
	return false;
}

// Gets the jobInfo index from jobInfo array by jobID.
function getJobInfosIndex(jobInfos, jobID)
{
	for(var i=0; i<jobInfos.length; i++)
	{
		if(jobInfos[i].jobId == jobID)
			return i;
	}
	
	return -1;
}

// Finds the selected id array from selected element.
function getSelValueArr(selElemID){
	var result = new Array();
	var selElem = document.getElementById(selElemID);
	for (i=0; i<selElem.options.length; i++) 
	{
		if (selElem.options[i].selected) 
		{
			result.push(selElem.options[i].value);
		}
	}
	
	return result;
}