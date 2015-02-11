package com.globalsight.terminology;

public interface TermbaseTestConstants
{
    public static String m_user   = "JUnitUser";
    public static String m_userRole = "Admin";
    public static String m_TBName = "JUnitTBName";
    public static String m_TBDesc = "JUnitTBDescription";
    public static String m_TBDefi = "<definition>" 
            + "<name>" + m_TBName + "</name>"
            + "<description>" + m_TBDesc + "</description>"
            + "<languages>"
            + "<language><name>English</name><locale>en</locale><hasterms>true</hasterms></language>"
            + "<language><name>French</name><locale>fr</locale><hasterms>true</hasterms></language>"
            + "<language><name>German</name><locale>de</locale><hasterms>true</hasterms></language>"
            + "<language><name>Spanish</name><locale>es</locale><hasterms>true</hasterms></language>"
            + "<language><name>Chinese (China)</name><locale>zh_CN</locale><hasterms>true</hasterms></language>"
            + "</languages>"
            + "<fields>"
            + "<field><name>Field_text</name><type>text-field_text</type><system>false</system><indexed>false</indexed><values></values></field>"
            + "<field><name>Field_attr</name><type>attr-field_attr</type><system>false</system><indexed>false</indexed><values>*user-defined*</values></field>"
            + "<field><name>Field_status</name><type>status</type><system>false</system><indexed>false</indexed><values>*user-defined*</values></field>"
            + "<field><name>Field_domain</name><type>domain</type><system>false</system><indexed>false</indexed><values>*user-defined*</values></field>"
            + "<field><name>Field_proj</name><type>project</type><system>false</system><indexed>false</indexed><values>*user-defined*</values></field>"
            + "<field><name>Field_definition</name><type>definition</type><system>false</system><indexed>false</indexed><values></values></field>"
            + "</fields>"
            + "<indexes>"
            + "<index><languagename>English</languagename><locale>en</locale><type>fuzzy</type></index><index><languagename>English</languagename><locale>en</locale><type>fulltext</type></index>"
            + "<index><languagename>French</languagename><locale>fr</locale><type>fuzzy</type></index><index><languagename>French</languagename><locale>fr</locale><type>fulltext</type></index>"
            + "<index><languagename>German</languagename><locale>de</locale><type>fuzzy</type></index><index><languagename>German</languagename><locale>de</locale><type>fulltext</type></index>"
            + "<index><languagename>Spanish</languagename><locale>es</locale><type>fuzzy</type></index><index><languagename>Spanish</languagename><locale>es</locale><type>fulltext</type></index>"
            + "<index><languagename>Chinese (China)</languagename><locale>zh_CN</locale><type>fuzzy</type></index><index><languagename>Chinese (China)</languagename><locale>zh_CN</locale><type>fulltext</type></index>"
            + "</indexes></definition>";
    
    public String m_termContentEN   = "AAAJUnit_EN";
    public String m_termContentFR   = "AAAJUnit_FR";
    public String m_termContentDE   = "AAAJUnit_DE";
    public String m_termContentZHCN = "AAAJUnit_ZH_CN";
    public String m_entry_FR = "<conceptGrp><concept></concept>"
            + "<languageGrp><language name=\"English\" locale=\"en\"/><termGrp><term termId=\"-1000\">"+ m_termContentEN + "</term></termGrp></languageGrp>"
            + "<languageGrp><language name=\"French\" locale=\"fr\"/><termGrp><term termId=\"-1000\">" + m_termContentFR + "</term></termGrp></languageGrp>"
            + "</conceptGrp>";
    public String m_entry_ZH = "<conceptGrp><concept></concept>"
            + "<languageGrp><language name=\"English\" locale=\"en\"/><termGrp><term termId=\"-1000\">"+ m_termContentEN + "</term></termGrp></languageGrp>"
            + "<languageGrp><language name=\"Chinese (China)\" locale=\"zh_CN\"/><termGrp><term termId=\"-1000\">" + m_termContentZHCN + "</term></termGrp></languageGrp>"
            + "</conceptGrp>";
    public String m_entry_FR_TBX = "<termEntry id=\"82\">"
            + "<langSet xml:lang=\"en\"><ntig><termGrp><term>"+ m_termContentEN + "</term></termGrp></ntig></langSet>"
            + "<langSet xml:lang=\"fr\"><ntig><termGrp><term>aaa_FR_TBX</term></termGrp></ntig></langSet>"
            + "</termEntry>";
    
    public String m_permissionSetString = "|1|2|3|4|5|6|7|8|9|10|11|12|13|14|17|18|19|20|21|22|23|24|25|26|27|28|29|30|31|32|33|34|35|36|37|38|39|40|41|42|43|44|45|46|47|48|49|50"
            + "|51|52|53|54|55|56|57|58|59|60|61|62|63|64|65|66|67|68|69|70|71|72|73|74|75|76|77|78|79|80|81|82|83|84|85|86|87|88|89|90|91|92|93|94|95|96|97|98|99|100"
            + "|101|102|103|104|117|118|119|120|121|123|124|125|126|127|128|129|130|131|132|133|134|135|136|137|138|139|140|142|143|144|145|146|147|148|149|150"
            + "|151|152|154|155|156|157|158|159|160|162|163|164|165|166|167|168|169|170|171|172|173|174|188|190|191|192|193|194|195|196|197|198|199|200"
            + "|201|202|203|204|205|206|208|209|210|212|213|214|215|216|217|218|219|220|221|223|224|225|226|227|228|229|230|231|232|233|234|235|236|237|238|239|240|242|243|244|245|246|247|248|249|250"
            + "|251|252|253|254|255|256|257|258|259|260|261|262|263|264|265|266|267|268|269|270|271|272|273|274|275|276|277|278|279|280|281|282|283|284|285|286|287|288|289|290|291|292|293|294|295|296|297|298|299|300"
            + "|301|302|303|304|305|306|307|308|309|310|311|313|314|315|316|317|318|319|320|321|322|323|324|325|326|327|329|330|331|332|333|334|335|336|337|338|339|340|341|342|343|344|345|346|347|348|349|350"
            + "|351|352|353|354|355|356|357|358|359|360|";
    
    static String FIELD_CONCEPT_LEVEL_STR = "conceptFieldLevelStr";
    static String FIELD_LANGUAGE_LEVEL_STR = "languageFieldLevelStr";
    static String FIELD_TERM_LEVEL_STR = "termFieldLevelStr";
}
