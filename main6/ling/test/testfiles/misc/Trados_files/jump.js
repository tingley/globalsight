function selectCountry(countryForm) {

selectedIndex = countryForm.countryField.selectedIndex;
c = countryForm.countryField.options[selectedIndex].value;



if (c=="Downloads") {
	site = "products/download.asp";

} 


if (c=="Term Corner") {
	site = "products/terminology_corner.asp";
} 

if (c=="Newsletter") {
	site = "today/news.asp";
} 

if (c=="On-line Ordering") {
	site = "products/fl_orderwelcome.asp";
} 


location.href=site;

} //end of fuction


