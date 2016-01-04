
var left_off = new Image();
left_off.src = "/gs-images/wizard_cell_gray_left.gif";
var left_on = new Image();
left_on.src = "/gs-images/wizard_cell_red_left.gif";
       
var right_off = new Image();
right_off.src = "/gs-images/wizard_cell_gray_right.gif";
var right_on = new Image();
right_on.src = "/gs-images/wizard_cell_red_right.gif";




function loadUrl(url, wizard) 
{
   opener.location= url;
}


function toggleStyle(obj, wizard)
{
    var style = 'background';
    var colorOn = 'red';
    var colorOff = '#708EB3';

    // Get all the table cell objects in the page that we 
    // want to be toggle-able
    obj_localePairs = getRef('localePairs');
    obj_activityType = getRef('activityType');
    obj_users = getRef('users');
    obj_tm = getRef('tm');
    obj_projects = getRef('projects');
    obj_locProfiles = getRef('locProfiles');

    var objects = new Array(obj_localePairs, 
                            obj_activityType, 
                            obj_users,
                            obj_tm,
                            obj_projects,
                            obj_locProfiles);
    if(costing)
    {
        obj_rate = getRef('rate');
        objects.push(obj_rate);
    }
   
    
    // Only load objects for the wizard you are in
    if (wizard == 'normalfiles') {
              obj_fileProfiles = getRef('fileProfiles');
              obj_normalFilesImport = getRef('normalFilesImport');
              objects.push(obj_fileProfiles,
                           obj_normalFilesImport);
    }
    else if (wizard == 'xmlfiles') 
    {
         obj_xmlRules = getRef('xmlRules');
         obj_xmlFileExtensions = getRef('xmlFileExtensions');
         obj_xmlFileProfiles = getRef('xmlFileProfiles');
         obj_xmlImport = getRef('xmlImport');
         objects.push(obj_xmlRules,
                      obj_xmlFileExtensions,
                      obj_xmlFileProfiles,
                      obj_xmlImport);
    }
    else if (wizard == 'database')
    {
       obj_dbConnections = getRef('dbConnections');
       obj_dbImportSettings = getRef('dbImportSettings');
       obj_dbPreview = getRef('dbPreview');
       obj_dbProfiles = getRef('dbProfiles');
       obj_dbImport = getRef('dbImport');
       objects.push(obj_dbConnections,
                    obj_dbImportSettings,
                    obj_dbPreview,
                    obj_dbImport,
                    obj_dbImport);
    } 
       
    
    // the table cell that was clicked on 
    obj= getRef(obj); 

    // Turn off all the cells except for the one that was
    // clicked on
    for (var i in objects)
    {
       var imgLeft = objects[i].id + "ImgLeft";
       var imgRight = objects[i].id + "ImgRight";

       if (objects[i].id == obj.id)
       {
          // This was the one that was clicked on, so make it red
          obj.style[style] = colorOn;
          document[imgLeft].src = left_on.src;
          document[imgRight].src = right_on.src;
       }
       else 
       {
          // Turn the background color off
          objects[i].style['background'] = colorOff;
          document[imgLeft].src = left_off.src;
          document[imgRight].src = right_off.src;
       }
    }
}

function getRef(obj)
{
    return(typeof obj == "string") ? document.getElementById(obj) : obj;
}

