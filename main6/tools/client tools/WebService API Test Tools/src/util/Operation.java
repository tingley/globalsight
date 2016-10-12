package util;

import java.util.ArrayList;


public class Operation
{
    private String name;
    private String returnType;
    ArrayList<Parameter> paraList;

    public Operation() {
    }

    public Operation(String name, String reutrnType, ArrayList<Parameter> paraList) {
        this.name = name;
        this.returnType = reutrnType;
        this.paraList = paraList;
    }

    public String toString() {
        return returnType + " " + name + paraList;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the returnType
     */
    public String getReturnType() {
        return returnType;
    }

    /**
     * @return the paraList
     */
    public ArrayList<Parameter> getParaList() {
        return paraList;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @param returnType the returnType to set
     */
    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    /**
     * @param paraList the paraList to set
     */
    public void setParaList(ArrayList<Parameter> paraList) {
        this.paraList = paraList;
    }


}
