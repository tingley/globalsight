package com.globalsight.ling.docproc.extractor.fm;

public class Tag
{
    public static final String STRING = "text";
    public static final String STRING_HEAD = "<String";
    public static final String STRING_END = "'>";
    
    public static final String FONT = "Font";
    public static final String FONT_HEAD = "<Font";
    public static final String FONT_END = "> # end of Font";
    
    public static final String PARA = "Para";
    public static final String PARA_HEAD = "<Para";
    public static final String PARA_END = "> # end of Para";
    
    public static final String PARALINE = "ParaLine";
    public static final String PARALINE_HEAD = "<ParaLine";
    public static final String PARALINE_END = "> # end of ParaLine";
    
    public static final String PAGE_HEAD = "<Page";
    public static final String PAGETYPE_HEAD = "<PageType";
    public static final String PAGE_END = "> # end of Page";
    
    public static final String A_TABLE_ID = "<ATbl";
    public static final String TABLE_ID = "<TblID";
    public static final String TABLE_END = "> # end of Tbl";
    
    public static final String FRAME_ID = "<ID";
    public static final String A_FRAME_ID = "<AFrame";
    public static final String FRAME_HEAD = "<Frame";
    public static final String FRAME = "Frame";
    public static final String FRAME_END = "> # end of Frame";
    
    public static final String TEXTRECT_HEAD = "<TextRect";
    public static final String TEXTRECT = "TextRect";
    public static final String TEXTRECT_END = "> # end of TextRect";
    public static final String TEXT_RECT_ID = "<ID";
    public static final String TEXTRECTID_HEAD = "<TextRectID";
    public static final String TEXTRECTID = "TextRectID";
    
    public static final String XREF_HEAD = "<XRef";
    public static final String XREF_END = "> # end of XRef";
    public static final String XREF = "XRef";
    public static final String XREFEND = "XRefEnd";
    public static final String XREFEND_HEAD = "<XRefEnd";
    public static final String XREF_DEF_HEAD = "<XRefDef";
    
    public static final String CHAR = "Char";
    public static final String CHAR_HEAD = "<Char";
    
    public static final String MARKER_HEAD = "<Marker";
    public static final String MARKER_END = "> # end of Marker";
    public static final String MARKER = "Marker";
    
    public static final String MTEXT_HEAD = "<MText";
    public static final String MTYPENAME_HEAD = "<MTypeName";
    public static final String MTYPE_HEAD = "<MType";
    public static final String MCURRPAGE_HEAD = "<MCurrPage";
    public static final String UNIQUE_HEAD = "<Unique";
    
    public static final String GROUP_HEAD = "<Group";
    public static final String GROUP = "Group";
    public static final String GROUP_END = "> # end of Group";
    
    public static final String VARIABLE_HEAD = "<Variable";
    public static final String VARIABLE = "Variable";
    public static final String VARIABLE_END = "> # end of Variable";
    public static final String VARIABLE_DEF_HEAD = "<VariableDef";
    public static final String VARIABLE_DEF = "VariableDef";
    
    public static final String PGF_NUMBER_FORMAT = "<PgfNumFormat";
    public static final String PGF_NUMBER_STRING = "<PgfNumString";
    
    public static final String MIF_FILE_HEAD = "<MIFFile";
    
    public static final String CONDITIONAL_HEAD = "<Conditional";
    public static final String CONDITIONAL = "Conditional";
    public static final String CONDITIONAL_END = "> # end of Conditional";
    public static final String UNCONDITIONAL = "<Unconditional";
    
    public static final String NOTES_HEAD = "<Notes";
    public static final String NOTES_END = "> # end of Notes";
    public static final String NOTES_REF = "<FNote";
    public static final String NOTES = "Notes";
    
    public static final String TEXTFLOW_HEAD = "<TextFlow";
    public static final String TEXTFLOW_END = "> # end of TextFlow";
    public static final String PGFTAG_CALLOUT = "<PgfTag `CA Callout'>";
    
    public static final String PGF_HEAD = "<Pgf";
    public static final String PGF_END = "> # end of Pgf";
    public static final String PGFTAG_HEAD = "<PgfTag";
    
    public static final String PGF_CATALOG_HEAD = "<PgfCatalog";
    public static final String PGF_CATALOG_END = "> # end of PgfCatalog";
    
    public static final String FCOLOR_HEAD = "<FColor ";
    
    public static final String MTYPENAME_HYPERTEXT = "Hypertext";
}
