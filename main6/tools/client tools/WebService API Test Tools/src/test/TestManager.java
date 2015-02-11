package test;

import java.util.ArrayList;
import java.util.HashMap;

import util.*;

public interface TestManager
{
    public String getRealToken();

    public ArrayList<Operation> getOperationList();

    public String testOperation(Operation operation, HashMap params) throws Exception;//!!!!!!!!!!!!!!!!!

    public Account getAccount();
}
