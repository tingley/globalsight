
var ar0 = "/gs/Common/header_inc";
var ar1 = "/gs/cms/folderListing";
var ar2 = "/gs/cms/tabFrame";

var str = location.search;
var pos = str.indexOf("&");
if (pos != -1) {
  var num = str.substring(pos + 1, str.length);
  window["ar" + num] = str.substring(1, pos);
}


