--  This file is used to populate the pre-designed template structure 
--  into table 'template_format'

delete from template_format;

--  New Record: 'record_start', 'STD', 'PRS' 
-- 
INSERT INTO TEMPLATE_FORMAT (NAME, TEMPLATE_TYPE, SOURCE_TYPE, TEXT)
values('record_start', 'STD', 'PRS',
'<TABLE WIDTH="100%" CELLSPACING="0" CELLPADDING="0" BORDER="0">
<TR><TD VALIGN="MIDDLE" BGCOLOR="#CCCCFF"><SCRIPT>
var tuvids<<ID>> = "<<TUVS<<ID>>>>";</SCRIPT><IMG 
SRC="/gs-images/editor/previewl.gif"><!-- Preview-- ><A 
 href="javascript:Preview(tuvids<<ID>>)"><SCRIPT>document.write(lb_preview)</SCRIPT></A
><!-- EndPreview-- ><IMG SRC="/gs-images/editor/previewr.gif"></TD>
<TD WIDTH="100%"></TD>
</TR>
</TABLE>
<TABLE WIDTH="100%" CELLSPACING="0" CELLPADDING="4" BORDER="0" BGCOLOR="#CCCCFF">');

--  New Record: 'record_end', 'STD', 'PRS' 
-- 
INSERT INTO TEMPLATE_FORMAT (NAME, TEMPLATE_TYPE, SOURCE_TYPE, TEXT)
values('record_end', 'STD', 'PRS',
'</TABLE>
<BR>');

--  New Record: 'record_start', 'DTL', 'PRS' 
-- 
INSERT INTO TEMPLATE_FORMAT (NAME, TEMPLATE_TYPE, SOURCE_TYPE, TEXT)
values('record_start', 'DTL', 'PRS',
'<TABLE WIDTH="100%" CELLSPACING="0" CELLPADDING="0" BORDER="0">
<TR><TD VALIGN="MIDDLE" BGCOLOR="#CCCCFF"><SCRIPT>
var tuvids<<ID>> = "<<TUVS<<ID>>>>";</SCRIPT><IMG 
SRC="/gs-images/editor/previewl.gif"><!-- Preview-- ><A 
 href="javascript:Preview(tuvids<<ID>>)"><SCRIPT>document.write(lb_preview)</SCRIPT></A
><!-- EndPreview-- ><IMG SRC="/gs-images/editor/previewr.gif"></TD>
<TD WIDTH="100%"></TD>
</TR>
</TABLE>
<TABLE WIDTH="100%" CELLSPACING="0" CELLPADDING="4" BORDER="0" BGCOLOR="#CCCCFF">');

--  New Record: 'record_end', 'DTL', 'PRS' 
-- 
INSERT INTO TEMPLATE_FORMAT (NAME, TEMPLATE_TYPE, SOURCE_TYPE, TEXT)
values('record_end', 'DTL', 'PRS',
'</TABLE>
<BR>');

--  New Record: 'template_start', 'STD', 'GXML'
-- 
INSERT INTO TEMPLATE_FORMAT (NAME, TEMPLATE_TYPE, SOURCE_TYPE, TEXT)
values('template_start', 'STD', 'GXML', ' ');

--  New Record: 'template_end', 'STD', 'GXML'
-- 
INSERT INTO TEMPLATE_FORMAT (NAME, TEMPLATE_TYPE, SOURCE_TYPE, TEXT)
values('template_end', 'STD', 'GXML', ' ');

--  New Record: 'template_start', 'DTL', 'GXML'
-- 
INSERT INTO TEMPLATE_FORMAT (NAME, TEMPLATE_TYPE, SOURCE_TYPE, TEXT)
values('template_start', 'DTL', 'GXML', '<PRE><SPAN CLASS="editorStandardText">');

--  New Record: 'template_end', 'DTL', 'GXML'
-- 
INSERT INTO TEMPLATE_FORMAT (NAME, TEMPLATE_TYPE, SOURCE_TYPE, TEXT)
values('template_end', 'DTL', 'GXML', '</SPAN></PRE>');

commit;
