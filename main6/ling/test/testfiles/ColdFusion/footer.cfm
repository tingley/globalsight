<!--- The footer will contain a link to all the departments. --->

<!--- First we do a query to fetch the departments. --->
<CFQUERY NAME="GetFooterLinks" datasource="cfx">
SELECT strDptName, strDptID
FROM tblDpt
</CFQUERY>

<!--- Then we set up some formatting... --->
<TR>
	<TD COLSPAN="3">

        <TABLE BORDER="0" CELLSPACING="0" CELLPADDING="0" ALIGN="CENTER" BGCOLOR="#000066" WIDTH=620>
		<TR>
			<TD HEIGHT="25" COLSPAN="4" ALIGN="CENTER" BGCOLOR="#999999">
				<FONT FACE="Myriad Web, Verdana, Helvetica" SIZE="-1" COLOR="#333333">
				<!--- Now we CFOUTPUT over the query we just executed. --->
				<CFOUTPUT QUERY="GetFooterLinks">
                	<!--- Notice we're passing the DepartmentID in the URL... --->
					<A HREF="department.cfm?DepartmentID=#strDptID#">#strDptName#</A>
					<!--- The following line inserts a | if the current record does Not EQual the last record. --->
                    <CFIF CurrentRow NEQ RecordCount>|</CFIF>
				</CFOUTPUT>
				</FONT>
			</TD>
		</TR>

        <TR>
			<TD HEIGHT="25" COLSPAN="4" ALIGN="CENTER" BGCOLOR="666666">
				<FONT FACE="Myriad Web, Verdana, Helvetica" SIZE="-1" COLOR="#333333">
					<A HREF="insertform.cfm">Add New Employee</A> | 
                    <A HREF="list.cfm">Complete Employee Listing</A> | 
                    <A HREF="searchform.cfm">Directory Search</A> 
				</FONT>
			</TD>
		</TR>
		</TABLE>

    </TD>
</TR>
</TABLE>
