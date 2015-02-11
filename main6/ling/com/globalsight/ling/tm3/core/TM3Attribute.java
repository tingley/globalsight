package com.globalsight.ling.tm3.core;

/**
 * An attribute that is defined for a TM.
 */
public class TM3Attribute {
    private long id;
    private String name;
    private BaseTm tm;
    private TM3AttributeValueType valueType;
    private String columnName;
    private boolean affectsIdentity = true;
    
    TM3Attribute() { }
    
    /**
     * Used to add a new custom attribute.
     */
    TM3Attribute(BaseTm tm, String name) {
        this.tm = tm;
        this.name = name;
        this.valueType = new TM3AttributeValueType.CustomType();
    }

    /**
     * Used to "declare" a new inline attribute when creating a TM or storage
     * pool.
     */
    public TM3Attribute(String name, TM3AttributeValueType valueType,
            String columnName, boolean affectsIdentity) {
        this.name = name;
        this.valueType = valueType;
        this.columnName = columnName;
        this.affectsIdentity = affectsIdentity;
    }
    
    long getId() {
        return id;
    }
    
    void setId(long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    public TM3AttributeValueType getValueType() {
        return valueType;
    }

    private String getValueTypeClass() {
        return valueType.getClass().getName();
    }

    private void setValueTypeClass(String valueTypeClass) {
        try {
            Class<?> c = Class.forName(valueTypeClass);
            this.valueType = (TM3AttributeValueType) c.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Can't initialize attribute", e);
        }
    }

    public String getColumnName() {
        return columnName;
    }

    public boolean isInline() {
        return columnName != null;
    }

    public boolean isCustom() {
        return columnName == null;
    }

    private void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public boolean getAffectsIdentity() {
        return affectsIdentity;
    }
    
    private void setAffectsIdentity(boolean affectsIdentity) {
        this.affectsIdentity = affectsIdentity;
    }
    
    public TM3Tm getTm() {
        return tm;
    }
    
    // called by DefaultManager to attach a "declaration" to a TM
    public void setTm(BaseTm tm) {
        this.tm = tm;
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof TM3Attribute)) {
            return false;
        }
        return ((TM3Attribute)o).getName().equals(name);
    }
    
    @Override
    public int hashCode() {
        return name.hashCode();
    }
    
    @Override
    public String toString() {
        return getName();
    }
}
