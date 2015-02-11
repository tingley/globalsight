
<html>
<head>
<script language="JavaScript" src="/globalsight/includes/setStyleSheet.js"></script>
</head>
<script language="JavaScript">
var docObj = window.opener.document;
var formObj = docObj.designerForm;
var sections = docObj.getElementById("sectionTableBody");
</script>

<form>
<br>
<div class="mainHeading">
    <script>document.write("Vendors - " + formObj.pageTitle.value);</script>
</div>
<br>
<form>
<table cellspacing="0" cellpadding="1" border="0" class="detailText" width="75%">
<script>
// item 0 is table header, item 1 is div
rows=sections.getElementsByTagName("tr");
for (var i = 2; i < rows.length; i++)
{
    cols = rows[i].getElementsByTagName("td");
    for (var j = 0; j < cols.length-1; j++)
    {
        document.writeln("<tr><td style='background:D6CFB2; font-weight:bold; font-size:larger' colspan=3>");
        document.writeln(cols[j+1].childNodes.item(0).data);
        document.writeln("</td></tr>");
        table = docObj.getElementById("table"+rows[i].id);
        fRows = table.getElementsByTagName("tr");
        for (var k = 1; k < fRows.length; k++)
        {
            document.writeln("<tr>");
            document.writeln("<td width=20%>");
            fCols = fRows[k].getElementsByTagName("td");
            document.writeln(fCols[1].childNodes.item(0).data +":");
            document.writeln("</td>");
            document.writeln("<td>");
            if (fCols[2].childNodes.item(0).data == "Text")
            {
                document.writeln("<input type='text'>");
            }
            else if (fCols[2].childNodes.item(0).data == "Checkbox")
            {
                document.writeln("<input type='checkbox'>");
            }
            else if (fCols[2].childNodes.item(0).data == "Radio")
            {
                document.writeln("<input type='radio'>");
            }
            document.writeln("</td></tr>");
        }
        document.writeln("<tr><td>&nbsp</td></tr>");
    }
}
</script>
</table>

<p>
<input type="button" value="Close" onclick="javascript:window.close()">
</form>
</html>
