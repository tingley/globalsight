// This function returns XMLHttpRequest object which can be used for
// AJAX call. Note that the object needs to be created per AJAX
// call.

function getXmlHttpRequest()
{
    var xmlhttp=false;

    /*@cc_on @*/
    /*@if (@_jscript_version >= 5)
        
        // JScript gives us Conditional compilation, we can cope with old
        // IE versions and security blocked creation of the objects.

        try {
            xmlhttp = new ActiveXObject("Msxml2.XMLHTTP");
        } catch (e) {
            try {
                xmlhttp = new ActiveXObject("Microsoft.XMLHTTP");
            } catch (E) {
                xmlhttp = false;
            }
        }
    @end @*/

    if (!xmlhttp && typeof XMLHttpRequest!='undefined')
    {
        xmlhttp = new XMLHttpRequest();
    }

    return xmlhttp;
}
