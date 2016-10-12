package util;

public class Parameter
{
    private String type;
    private String name;

    public Parameter() {
    }

    public Parameter(String type, String name) {
        this.type = type;
        this.name = name;
    }

    public String toString() {
        return type + "   " + name;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }


}
