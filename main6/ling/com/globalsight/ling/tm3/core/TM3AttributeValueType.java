package com.globalsight.ling.tm3.core;

/**
 * The value type for an attribute.
 */

public interface TM3AttributeValueType {
    String getSqlType();
    void checkValue(Object value, String name) throws IllegalArgumentException;

    // we store only the class name in the database, so create a subclass that
    // encodes the length and nullability
    public static abstract class StringType implements TM3AttributeValueType {
        private int maxLength;
        private boolean required;
        protected StringType(int maxLength, boolean required) {
            this.maxLength = maxLength;
            this.required = required;
        }

        public String getSqlType() {
            return "varchar(" + maxLength + ")" +
                (required ? " NOT NULL" : "");
        }
        public void checkValue(Object value, String name)
                throws IllegalArgumentException {
            if (value != null)
            {
                if (!(value instanceof String))
                {
                    throw new IllegalArgumentException("attr " + name
                            + " value " + value + " not of type String");
                }
                String s = (String) value;
                if (s.length() > maxLength && !".sid".equals(name))
                {
                    throw new IllegalArgumentException("attr " + name
                            + " value " + value + " longer than " + maxLength);
                }
            }
        }
    }
    public static class CustomType extends StringType {
        public CustomType() {
            super(StorageInfo.MAX_ATTR_VALUE_LEN, true);
        }
    }

    public static class BooleanType implements TM3AttributeValueType {
        private boolean required;
        protected BooleanType(boolean required) {
            this.required = required;
        }

        public String getSqlType() {
            return "boolean" + (required ? " NOT NULL" : "");
        }
        public void checkValue(Object value, String name)
                throws IllegalArgumentException {
            if (! (value instanceof Boolean)) {
                throw new IllegalArgumentException(
                    "attr " + name + " value " + value + " not of type Boolean");
            }
        }
    }
}
