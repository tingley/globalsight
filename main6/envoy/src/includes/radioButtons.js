function isRadioChecked(radioButtonArray)
{
   var checked = false;

   // If more than one radio button is displayed, the length attribute of the 
   // radio button array will be non-zero, so find which 
   // one is checked
   if (radioButtonArray.length)
   {
      for (i = 0; i < radioButtonArray.length; i++) 
      {
         if (radioButtonArray[i].checked == true) 
         {
            checked = true;
            break;
         }
      }
   }
   // If only one is displayed, there is no radio button array, so
   // just check if the single radio button is checked
   else 
   {
      if (radioButtonArray.checked == true)
      {
         checked = true;
      }
   }

   if (!checked) 
   {
      alert(msg_select_row);
      return false;
   }
   else 
   {
      return true;
   }
   
}

// Return the value of the selected radio button.  Return null if none selected.
function getRadioValue(radioButtonArray)
{

   // If more than one radio button is displayed, the length attribute of the
   // radio button array will be non-zero, so find which
   // one is checked
   if (radioButtonArray.length)
   {
      for (i = 0; i < radioButtonArray.length; i++)
      {
         if (radioButtonArray[i].checked == true)
         {
            return radioButtonArray[i].value;
         }
      }
   }
   // If only one is displayed, there is no radio button array, so
   // just check if the single radio button is checked
   else
   {
      if (radioButtonArray.checked == true)
      {
         return radioButtonArray.value;
      }
   }

  return null;
}

function getRadioValues(str)
{
   // Use regex to get the jobId and the jobState values
   // from the radio button that was selected
   var radioButtonValueRegex = /^jobId=([0-9]*)&jobState=(\w*)$/;
   radioButtonValueRegex.test(str);
   var jobId = RegExp.$1;
   var jobState = RegExp.$2;

   var valuesArray = [jobId, jobState]; 
   
   return valuesArray;
}

function getRadioValuesConGroupId(str)
{
   // Use regex to get the jobId and the jobState values
   // from the radio button that was selected
   var radioButtonValueRegex = /^jobId=([0-9]*)&jobState=(\w*)&jobGroupId=([0-9]*)$/;
   radioButtonValueRegex.test(str);
   var jobId = RegExp.$1;
   var jobState = RegExp.$2;
   var jobGroupId = RegExp.$3;
   var valuesArray = [jobId, jobState,jobGroupId]; 
   
   return valuesArray;
}

function getRadioValuesWf(str)
{
   // Use regex to get the jobId and the jobState values
   // from the radio button that was selected
   var radioButtonValueRegex = /^wfId=([0-9]*)&wfState=(\w*)&wfIsEditable=(\w*)$/;
   radioButtonValueRegex.test(str);
   var wfId = RegExp.$1;
   var wfState = RegExp.$2;
   var wfIsEditable = RegExp.$3;

   var valuesArray = [wfId, wfState, wfIsEditable]; 
   
   return valuesArray;
}
