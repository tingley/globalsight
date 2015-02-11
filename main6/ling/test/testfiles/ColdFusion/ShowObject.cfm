<CFSETTING ENABLECFOUTPUTONLY="YES">

<!--- Show full object --->


<!--- Pour this object's data into an associative array.

	For example, Object 132 might have the following content:
	
	ObjectID   Type    Data
	---------  ------  -----------
	132		   Title   "This Story's Title"
	132	       Teaser  "Read this interesting article"
	132        Body    "The main story would go here."

    This is in query form.  The corresponding associative
	array would look like this:
	
	Key     Value
	------  -----------
	Title   "This Story's Title"
	Teaser  "Read this interesting article"
	Body    "The main story would go here."
	
	This enables us to access the different content
	by referring to StructName.Title, StructName.Teaser,
	etc. rather than doing a new query each time we 
	want to access a Title, Teaser, etc. --->

<!--- This query retrieves all content information
	for the current object --->
<CFQUERY datasource="cfx" NAME="GetContent">
SELECT PubContent.*, PubContentTypes.*
FROM PubContent, PubContentTypes
WHERE PubContent.TypeID = PubContentTypes.TypeID
	AND ObjectID = #Attributes.ObjectID#
</CFQUERY>

<!--- This is the struct that will hold our object --->
<CFSET Object = StructNew()>

<!--- Loop over the GetContent query; for each row, insert
	a row into the Content struct, with TypeName as the key
	and Data as the value --->
<CFLOOP QUERY="GetContent">
	<CFSET Temp = StructInsert(Object, TypeName, Data)>
</CFLOOP>

<!--- The ObjectID should also be part of the struct. --->
<CFSET Object.ObjectID = Attributes.ObjectID>

<!--- The CurrObject struct now contains all the content for the
	current object, so it is easily accessible. Now the only
	thing left to do is render the content to HTML.
	Note that different classes of objects (text, file,
	link) are outputted in different ways; if you were
	to create your own custom object classes, you'd have
	to hard-code how they are rendered, just like the
	classes below. --->
	
<CFOUTPUT>

	<DIV CLASS="HeadlineFull">#Object.Headline#</DIV>

	&nbsp;<BR>

	<CFIF IsDefined("Object.Image")>
		<P><IMG SRC="binarydata/#Object.Image#">
	</CFIF>

	<CFIF IsDefined("Object.Body")><DIV CLASS="BodyFull">
		<CFIF IsDefined("Object.InlineImage")>
			<IMG SRC="binarydata/#Object.InlineImage#" ALIGN="LEFT" HSPACE="10">
		</CFIF>
		#Object.Body#</DIV>
	</CFIF>

	&nbsp;<BR>

	<CFIF IsDefined("Object.HREF")>
		<DIV CLASS="LinkFull"><B>Go to:</B> <A HREF="#Object.HREF#">#Object.HREF#</A><BR></DIV>
	</CFIF>
	<CFIF IsDefined("Object.File")>
		<DIV CLASS="LinkFull"><B>Download:</B> <A HREF="binarydata/#Replace(Replace(URLEncodedFormat(Object.File),"%2E",".","ALL"),"+","%20","ALL")#">#Object.File#</A><BR></DIV>
	</CFIF>

	<!--- If browser is in Admin mode, display editing icons --->
	<CFIF IsDefined("Cookie.PubAdminMode")>
		<BR><A HREF="admin/properties.cfm?ObjectID=#Attributes.ObjectID#"><IMG SRC="open.gif" WIDTH=16 HEIGHT=14 BORDER=0 ALT="Open" ALIGN="TOP"></A> <A HREF="admin/deleteobject.cfm?ObjectID=#Attributes.ObjectID#" onClick="return confirm('This will PERMANENTLY delete this object!\n\nSure you want to continue?')"><IMG SRC="delete.gif" WIDTH=15 HEIGHT=16 BORDER=0 ALT="Delete" ALIGN="TOP"></A>
	</CFIF>

</CFOUTPUT>

<CFSETTING ENABLECFOUTPUTONLY="NO">
