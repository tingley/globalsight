package com.globalsight.ling.tm3.integration.segmenttm;

import java.util.Set;
import java.util.HashSet;

import com.globalsight.ling.tm3.core.TM3Attribute;
import com.globalsight.ling.tm3.core.TM3AttributeValueType;
import com.globalsight.ling.tm3.core.TM3AttributeValueType.BooleanType;
import com.globalsight.ling.tm3.core.TM3AttributeValueType.StringType;

/**
 * Attributes for TM3 segment TM TUs.  
 * 
 * In TM2, only two attributes affected identity: translatable/localizable 
 * (because they are stored in separate TMs), and the 'type' flag.  TM3 
 * models this behavior but adds SID as identity-specifying.
 */
public enum SegmentTmAttribute {
    TRANSLATABLE(".translatable", new TranslatableType(), "translatable", true),
    TYPE(".type", new TypeType(), "type", true),
    FORMAT(".format", new FormatType(), "format", false),
    SID(".sid", new SidType(), "sid", true),
    FROM_WORLDSERVER(".from_ws", new FromWsType(), "from_ws", false),
    UPDATED_BY_PROJECT(".project", new ProjectType(), "project", false);

    private String key;
    private TM3AttributeValueType valueType;
    private String columnName;
    private boolean affectsIdentity;
    
    private SegmentTmAttribute(String key, TM3AttributeValueType valueType,
            String columnName, boolean affectsIdentity) {
        this.key = key;
        this.valueType = valueType;
        this.columnName = columnName;
        this.affectsIdentity = affectsIdentity;
    }
    
    public String getKey() {
        return key;
    }

    private TM3Attribute toAttr() {
        return new TM3Attribute(key, valueType, columnName, affectsIdentity);
    }

    public static Set<TM3Attribute> inlineAttributes() {
        // NB: we need to create fresh TM3Attributes each time this is called,
        // because they will be associated with a particular TM and be
        // persisted.
        Set<TM3Attribute> r = new HashSet<TM3Attribute>();
        r.add(TRANSLATABLE.toAttr());
        r.add(TYPE.toAttr());
        r.add(FORMAT.toAttr());
        r.add(SID.toAttr());
        r.add(FROM_WORLDSERVER.toAttr());
        r.add(UPDATED_BY_PROJECT.toAttr());
        return r;
    }

    public static class TranslatableType extends BooleanType {
        public TranslatableType() {
            super(true);
        }
    }
    public static class FromWsType extends BooleanType {
        public FromWsType() {
            super(true);
        }
    }
    public static class FormatType extends StringType {
        public FormatType() {
            super(20, false);
        }
    }
    public static class TypeType extends StringType {
        public TypeType() {
            super(50, true);
        }
    }
    public static class ProjectType extends StringType {
        public ProjectType() {
            super(40, false);
        }
    }
    public static class SidType extends StringType {
        public SidType() {
            super(255, false);
        }
    }
}
