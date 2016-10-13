package com.plug.Version_7_1_6_0;

import java.io.File;
import java.sql.SQLException;

import com.util.PropertyUtil;

public class InitialHtmlFilter
{
    private long companyId;
    private File tagsProperties;
    private DbServer dbServer = new DbServer();

    public InitialHtmlFilter(long companyId, File tagsProperties)
    {
        this.companyId = companyId;
        this.tagsProperties = tagsProperties;
    }

    public long getCompanyId()
    {
        return companyId;
    }

    public void setCompanyId(long companyId)
    {
        this.companyId = companyId;
    }

    public File getTagsProperties()
    {
        return tagsProperties;
    }

    public void setTagsProperties(File tagsProperties)
    {
        this.tagsProperties = tagsProperties;
    }

    public String insert() throws SQLException
    {
        String filterName = "HTML_Filter(Default)";
        String filterDescription = "The default html filter.";
        String placeHolderTrim = "embeddable_tags";
        String jsFunctionText = "l10n";
        String embeddableTags = this.getValueByKey("InlineTag_html");
        String defaultEmbeddableTags = embeddableTags;
        String pairedTags = this.getValueByKey("PairedTag_html");
        String defaultPairedTags = pairedTags;
        String unpairedTags = this.getValueByKey("UnpairedTag_html");
        String defaultUnpairedTags = unpairedTags;
        String switchTagMap = getValueByKey("SwitchTagMap_html");
        String defaultSwitchTagMap = switchTagMap;
        String whitePreservingTag = getValueByKey("WhitePreservingTag_html");
        String defaultWhitePreservingTag = whitePreservingTag;
        String nonTranslatableMetaAttribute = getValueByKey("NonTranslatableMetaAttribute_html");
        String defaultNonTranslatableMetaAttribute = nonTranslatableMetaAttribute;
        String translatableAttribute = getValueByKey("TranslatableAttribute_html");
        String defaultTranslatableAttribute = translatableAttribute;
        String localizableAttributeMap = getValueByKey("LocalizableAttributeMap_html");
        String defaultLocalizableAttributeMap = localizableAttributeMap;
        String convertHtmlEntity = getValueByKey("convertHtmlEntity");
        String ignoreInvalidHtmlTags = getValueByKey("IgnoreInvalidHtmlTags");

        StringBuilder insertSql = new StringBuilder(
                "insert into html_filter(FILTER_NAME, FILTER_DESCRIPTION, EMBEDDABLE_TAGS, "
                        + "PLACEHOLD_TRIMMING, COMPANY_ID, DEFAULT_EMBEDDABLE_TAGS, CONVERT_HTML_ENTRY, "
                        + "IGNORE_INVALIDE_HTML_TAGS, JS_FUNCTION_FILTER, DEFAULT_PAIRED_TAGS, PAIRED_TAGS, "
                        + "DEFAULT_UNPAIRED_TAGS, UNPAIRED_TAGS, DEFAULT_SWITCH_TAG_MAPS, SWITCH_TAG_MAPS, "
                        + "DEFAULT_WHITE_PRESERVING_TAGS, WHITE_PRESERVING_TAGS, DEFAULT_NON_TRANSLATABLE_META_ATTRIBUTES, "
                        + "NON_TRANSLATABLE_META_ATTRIBUTES, DEFAULT_TRANSLATABLE_ATTRIBUTES, TRANSLATABLE_ATTRIBUTES, "
                        + "DEFAULT_LOCALIZABLE_ATTRIBUTE_MAPS, LOCALIZABLE_ATTRIBUTE_MAPS) values ");
        if (!dbServer.checkExist(FilterConstants.HTML_TABLENAME, filterName,
                companyId))
        {
            insertSql.append("(");
            insertSql.append("'").append(filterName).append("'").append(",");
            insertSql.append("'").append(filterDescription).append("'").append(
                    ",");
            insertSql.append("'").append(embeddableTags).append("'")
                    .append(",");
            insertSql.append("'").append(placeHolderTrim).append("'").append(
                    ",");
            insertSql.append(companyId).append(",");
            insertSql.append("'").append(defaultEmbeddableTags).append("'")
                    .append(",");
            insertSql.append("'").append(
                    "true".equals(convertHtmlEntity) ? "Y" : "N").append("'")
                    .append(",");
            insertSql.append("'").append(
                    "true".equals(ignoreInvalidHtmlTags) ? "Y" : "N").append(
                    "'").append(",");
            insertSql.append("'").append(jsFunctionText).append("'")
                    .append(",");
            insertSql.append("'").append(defaultPairedTags).append("'").append(
                    ",");
            insertSql.append("'").append(pairedTags).append("'").append(",");
            insertSql.append("'").append(defaultUnpairedTags).append("'")
                    .append(",");
            insertSql.append("'").append(unpairedTags).append("'").append(",");
            insertSql.append("'").append(defaultSwitchTagMap).append("'")
                    .append(",");
            insertSql.append("'").append(switchTagMap).append("'").append(",");
            insertSql.append("'").append(defaultWhitePreservingTag).append("'")
                    .append(",");
            insertSql.append("'").append(whitePreservingTag).append("'")
                    .append(",");
            insertSql.append("'").append(defaultNonTranslatableMetaAttribute)
                    .append("'").append(",");
            insertSql.append("'").append(nonTranslatableMetaAttribute).append(
                    "'").append(",");
            insertSql.append("'").append(defaultTranslatableAttribute).append(
                    "'").append(",");
            insertSql.append("'").append(translatableAttribute).append("'")
                    .append(",");
            insertSql.append("'").append(defaultLocalizableAttributeMap)
                    .append("'").append(",");
            insertSql.append("'").append(localizableAttributeMap).append("'");
            insertSql.append(")");
        }
        if (insertSql.indexOf("'") != -1)
        {
            dbServer.insert(insertSql.toString());
        }
        return insertSql.toString();
    }

    private String getValueByKey(String key)
    {
        return PropertyUtil.get(tagsProperties, key);
    }
}
