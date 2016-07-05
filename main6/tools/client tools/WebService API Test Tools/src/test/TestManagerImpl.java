package test;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import util.Account;
import util.Operation;
import util.TypeMap;
import util.WebServiceConstants;
import util.WriteLog;

public class TestManagerImpl extends AbstractTestManager
{
    private static Logger logger = Logger.getLogger(TestManagerImpl.class);

    public TestManagerImpl() {
        super();
    }

    public TestManagerImpl(Account account) {
        super(account);
    }

    /*
     * Test method
     * For different type of parameters
     */
    @SuppressWarnings("unchecked")
    public String testOperation(Operation operation, HashMap params) throws Exception {
        //Write Log Info
        logger.info("Test Operation: " + operation.getName());
        logger.info("Parameter Types: " + operation.getParaList());
        logger.info("Parameter Input: " + getInputParams(params));

        //get parameterTypes
        int paramSize = operation.getParaList().size();
        Class[] parameterTypes = new Class[paramSize];
        Object[] realParams = new Object[paramSize];
        for (int i = 0; i < paramSize; i++) {
            String typeName = operation.getParaList().get(i).getType().toLowerCase();
            String Name = operation.getParaList().get(i).getName();//the name of the parameter
            String type = operation.getParaList().get(i).getType();//the type of the parameter
            String dataNotMap = null;
            if (TypeMap.TYPE_INFO.containsKey(typeName)) {
                parameterTypes[i] = TypeMap.TYPE_INFO.get(typeName);
            } else {
                parameterTypes[i] = TypeMap.TYPE_INFO.get("others");
            }
            if (!"map".equalsIgnoreCase(typeName))
                dataNotMap = params.get(Name + "(" + type + ")").toString();

            if ("string".equalsIgnoreCase(typeName)) {
                realParams[i] = "".equals(dataNotMap) ? null : dataNotMap;
            } else if ("arrayof_soapenc_string".equalsIgnoreCase(typeName)) {
                if ("".equals(dataNotMap)) {
                    realParams[i] = null;
                } else {
                    StringTokenizer str = new StringTokenizer(dataNotMap, ",");
                    List strList = new ArrayList();
                    while (str.hasMoreTokens()) {
                        strList.add(str.nextToken().trim());
                    }
                    String[] strs = new String[strList.size()];
                    for (int k = 0; k < strList.size(); k++)//Get the data of the parameter in map and create the map
                    {
                        strs[k] = strList.get(k).toString();
                    }
                    realParams[i] = strs;
                }
            } else if ("int".equalsIgnoreCase(typeName)) {
                try {
                    realParams[i] = "".equals(dataNotMap) ? null : (new Integer(dataNotMap)).intValue();
                }
                catch (Exception e) {
                    JOptionPane.showMessageDialog(null, "Type error: Please input an 'int' type in the int type fileld", "Type error", JOptionPane.ERROR_MESSAGE);
                    return null;
                }
            } else if ("long".equalsIgnoreCase(typeName)) {
                try {
                    realParams[i] = "".equals(dataNotMap) ? null : (new Long(dataNotMap)).longValue();
                }
                catch (Exception e) {
                    JOptionPane.showMessageDialog(null, "Type error: Please input an 'long' type in the long type fileld", "Type error", JOptionPane.ERROR_MESSAGE);
                    return null;
                }
            } else if ("double".equalsIgnoreCase(typeName)) {
                realParams[i] = "".equals(dataNotMap) ? null : (new Double(dataNotMap)).doubleValue();
            } else if ("base64binary".equalsIgnoreCase(typeName)) {
                File file = new File(dataNotMap);
                byte[] content = new byte[(int) file.length()];
                if (!file.exists()) {
                    logger.info(file.getAbsolutePath() + " does not exist.");
                }
                BufferedInputStream inputStream = null;
                try {
                    inputStream = new BufferedInputStream(new FileInputStream(file));
                    inputStream.read(content, 0, content.length);
                }
                catch (Exception e) {
                    logger.error("Failed to read content from file " + file.getAbsolutePath(), e);
                }
                realParams[i] = content;
            } else if ("boolean".equalsIgnoreCase(typeName)) {
                realParams[i] = "".equals(dataNotMap) ? null : (new Boolean(dataNotMap)).booleanValue();
            } else if ("map".equalsIgnoreCase(typeName)) {
                //For map types
                HashMap args = new HashMap();

                Properties p = WriteLog.getResourceFile();
                String parameterList = p.getProperty(operation.getName());//get the method name then get the parameters of the map in the method in properties
                StringTokenizer st = new StringTokenizer(parameterList, ",");
                List parametersList = new ArrayList();
                while (st.hasMoreTokens()) {
                    parametersList.add(st.nextToken().trim());
                }
                for (int j = 0; j < parametersList.size(); j = j + 2)//Get the data of the parameter in map and create the map
                {
                    String typeInMap = parametersList.get(j).toString().toLowerCase();//the type of parameter in the map
                    String data = params.get(parametersList.get(j + 1) + "(" + parametersList.get(j) + ")").toString();//the data of the parameter
                    if ("string".equalsIgnoreCase(typeInMap)) {
                        args.put(parametersList.get(j + 1), "".equals(data) ? null : data);
                    } else if ("int".equalsIgnoreCase(typeInMap)) {
                        try {
                            args.put(parametersList.get(j + 1), "".equals(data) ? null : (new Integer(data)).intValue());
                        }
                        catch (Exception e) {
                            JOptionPane.showMessageDialog(null, "Type error: Please input an 'int' type in the int type fileld", "Type error", JOptionPane.ERROR_MESSAGE);
                            return null;
                        }
                    } else if ("long".equalsIgnoreCase(typeInMap)) {
                        try {
                            args.put(parametersList.get(j + 1), "".equals(data) ? null : (new Long(data)).longValue());
                        }
                        catch (Exception e) {
                            JOptionPane.showMessageDialog(null, "Type error: Please input an 'long' type in the long type fileld", "Type error", JOptionPane.ERROR_MESSAGE);
                            return null;
                        }
                    } else if ("double".equalsIgnoreCase(typeInMap)) {
                        args.put(parametersList.get(j + 1), "".equals(data) ? null : (new Double(data)).doubleValue());
                    } else if ("base64binary".equalsIgnoreCase(typeInMap)) {
                        File file = new File(data.toString());
                        byte[] content = new byte[(int) file.length()];
                        if (!file.exists()) {
                            logger.info(file.getAbsolutePath() + " does not exist.");
                        }
                        BufferedInputStream inputStream = null;
                        try {
                            inputStream = new BufferedInputStream(new FileInputStream(file));
                            inputStream.read(content, 0, content.length);
                        }
                        catch (Exception e) {
                            logger.error("Failed to read content from file " + file.getAbsolutePath(), e);
                        }
                        args.put(parametersList.get(j + 1), content);
                    } else if ("boolean".equalsIgnoreCase(typeInMap)) {
                        args.put(parametersList.get(j + 1), "".equals(data) ? null : (new Boolean(data)).booleanValue());
                    } else {
                        args.put(parametersList.get(j + 1), "".equals(data) ? null : (new Boolean(data)).booleanValue());
                    }
                }
                realParams[i] = args;
            } else {
                realParams[i] = "".equals(dataNotMap) ? null : dataNotMap;
            }
        }

        //get method
        Class cls = null;
        Method method = null;
        try {
            cls = Class.forName(defaultWSDLClass);
        }
        catch (Exception e) {
            WriteLog.info(WebServiceConstants.IS_LOG, "class.forName() Exception in TestMangerImpl.testOpeation()");
            logger.error("Cannot find the class " + defaultWSDLClass, e);
        }
        try {
            method = cls.getMethod(operation.getName(), parameterTypes);
        }
        catch (Exception e) {
            for (int i = 0; i < parameterTypes.length; i++) {
                if (parameterTypes[i].equals(int.class)) {
                    parameterTypes[i] = Integer.class;
                }
                else if (parameterTypes[i].equals(long.class)) {
                    parameterTypes[i] = Long.class;
                }
                else if (parameterTypes[i].equals(double.class)) {
                    parameterTypes[i] = Double.class;
                }
                else if (parameterTypes[i].equals(boolean.class)) {
                    parameterTypes[i] = Boolean.class;
                }
            }
            try {
                method = cls.getMethod(operation.getName(), parameterTypes);
            }
            catch (Exception e1) {
                logger.error("Failed to get method info.", e1);
            }
        }

        // involk method
        String result = null;
        if (operation.getReturnType() != null) {
            result = String.valueOf(method.invoke(this.getProxy(), realParams));
            logger.info("Return content for method " + method.getName() + "\r\n" + result);
        }

        return result;
    }

    private static String getInputParams(HashMap obs) {
        String result = "[ ";
        Iterator iter = obs.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            Object val = entry.getValue();
            if (val != null) {
                result = result + val.toString() + "  ";
            }
        }
        result = result + " ]";
        return result;
    }

}

