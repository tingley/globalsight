package test;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
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

    public TestManagerImpl()
    {
        super();
    }

    public TestManagerImpl(Account account)
    {
        super(account);
    }

    /*
     * Test method For different type of parameters
     */
    @SuppressWarnings(
    { "unchecked", "rawtypes" })
    public String testOperation(Operation operation, HashMap params) throws Exception
    {
        // Write Log Info
        logger.info("Test Operation: " + operation.getName());
        logger.info("Parameter Types: " + operation.getParaList());
        logger.info("Parameter Input: " + getInputParams(params));

        // get parameterTypes
        int paramSize = operation.getParaList().size();
        Class[] parameterTypes = new Class[paramSize];
        Object[] realParams = new Object[paramSize];
        for (int i = 0; i < paramSize; i++)
        {
            String typeName = operation.getParaList().get(i).getType().toLowerCase();
            // the name of the parameter
            String Name = operation.getParaList().get(i).getName();
            // the type of the parameter
            String type = operation.getParaList().get(i).getType();
            String dataNotMap = null;
            if (TypeMap.TYPE_INFO.containsKey(typeName))
            {
                parameterTypes[i] = TypeMap.TYPE_INFO.get(typeName);
            }
            else
            {
                parameterTypes[i] = TypeMap.TYPE_INFO.get("others");
            }
            if (!"map".equalsIgnoreCase(typeName))
                dataNotMap = params.get(Name + "(" + type + ")").toString();

            if ("string".equalsIgnoreCase(typeName))
            {
                realParams[i] = "".equals(dataNotMap) ? null : dataNotMap;
            }
            else if ("arrayof_soapenc_string".equalsIgnoreCase(typeName))
            {
                if ("".equals(dataNotMap))
                {
                    realParams[i] = null;
                }
                else
                {
                    StringTokenizer str = new StringTokenizer(dataNotMap, ",");
                    List strList = new ArrayList();
                    while (str.hasMoreTokens())
                    {
                        strList.add(str.nextToken().trim());
                    }
                    String[] strs = new String[strList.size()];
                    // Get the data of the parameter in map and create the map
                    for (int k = 0; k < strList.size(); k++)
                    {
                        strs[k] = strList.get(k).toString();
                    }
                    realParams[i] = strs;
                }
            }
            else if ("int".equalsIgnoreCase(typeName))
            {
                try
                {
                    realParams[i] = "".equals(dataNotMap) ? null
                            : (new Integer(dataNotMap)).intValue();
                }
                catch (Exception e)
                {
                    JOptionPane.showMessageDialog(null,
                            "Type error: Please input an 'int' type in the int type fileld",
                            "Type error", JOptionPane.ERROR_MESSAGE);
                    return null;
                }
            }
            else if ("long".equalsIgnoreCase(typeName))
            {
                try
                {
                    realParams[i] = "".equals(dataNotMap) ? null
                            : (new Long(dataNotMap)).longValue();
                }
                catch (Exception e)
                {
                    JOptionPane.showMessageDialog(null,
                            "Type error: Please input an 'long' type in the long type fileld",
                            "Type error", JOptionPane.ERROR_MESSAGE);
                    return null;
                }
            }
            else if ("double".equalsIgnoreCase(typeName))
            {
                realParams[i] = "".equals(dataNotMap) ? null
                        : (new Double(dataNotMap)).doubleValue();
            }
            else if ("base64binary".equalsIgnoreCase(typeName))
            {
                File file = new File(dataNotMap);
                byte[] content = new byte[(int) file.length()];
                if (!file.exists())
                {
                    logger.info(file.getAbsolutePath() + " does not exist.");
                }
                BufferedInputStream inputStream = null;
                try
                {
                    inputStream = new BufferedInputStream(new FileInputStream(file));
                    inputStream.read(content, 0, content.length);
                }
                catch (Exception e)
                {
                    logger.error("Failed to read content from file " + file.getAbsolutePath(), e);
                }
                realParams[i] = content;
            }
            else if ("boolean".equalsIgnoreCase(typeName))
            {
                realParams[i] = "".equals(dataNotMap) ? null
                        : (new Boolean(dataNotMap)).booleanValue();
            }
            else if ("map".equalsIgnoreCase(typeName))
            {
                // For map types
                HashMap args = new HashMap();

                Properties p = WriteLog.getResourceFile();
                // get the method name then get the parameters of the map in the
                // method in properties
                String parameterList = p.getProperty(operation.getName());
                StringTokenizer st = new StringTokenizer(parameterList, ",");
                List parametersList = new ArrayList();
                while (st.hasMoreTokens())
                {
                    parametersList.add(st.nextToken().trim());
                }
                // Get the data of the parameter in map and create the map
                for (int j = 0; j < parametersList.size(); j = j + 2)
                {
                    // the type of parameter in the map
                    String typeInMap = parametersList.get(j).toString().toLowerCase();
                    // the data of the parameter
                    String data = params
                            .get(parametersList.get(j + 1) + "(" + parametersList.get(j) + ")")
                            .toString();
                    if ("string".equalsIgnoreCase(typeInMap))
                    {
                        args.put(parametersList.get(j + 1), "".equals(data) ? null : data);
                    }
                    else if ("int".equalsIgnoreCase(typeInMap))
                    {
                        try
                        {
                            args.put(parametersList.get(j + 1),
                                    "".equals(data) ? null : (new Integer(data)).intValue());
                        }
                        catch (Exception e)
                        {
                            JOptionPane.showMessageDialog(null,
                                    "Type error: Please input an 'int' type in the int type fileld",
                                    "Type error", JOptionPane.ERROR_MESSAGE);
                            return null;
                        }
                    }
                    else if ("long".equalsIgnoreCase(typeInMap))
                    {
                        try
                        {
                            args.put(parametersList.get(j + 1),
                                    "".equals(data) ? null : (new Long(data)).longValue());
                        }
                        catch (Exception e)
                        {
                            JOptionPane.showMessageDialog(null,
                                    "Type error: Please input an 'long' type in the long type fileld",
                                    "Type error", JOptionPane.ERROR_MESSAGE);
                            return null;
                        }
                    }
                    else if ("double".equalsIgnoreCase(typeInMap))
                    {
                        args.put(parametersList.get(j + 1),
                                "".equals(data) ? null : (new Double(data)).doubleValue());
                    }
                    else if ("base64binary".equalsIgnoreCase(typeInMap))
                    {
                        File file = new File(data.toString());
                        byte[] content = new byte[(int) file.length()];
                        if (!file.exists())
                        {
                            logger.info(file.getAbsolutePath() + " does not exist.");
                        }
                        BufferedInputStream inputStream = null;
                        try
                        {
                            inputStream = new BufferedInputStream(new FileInputStream(file));
                            inputStream.read(content, 0, content.length);
                        }
                        catch (Exception e)
                        {
                            logger.error(
                                    "Failed to read content from file " + file.getAbsolutePath(),
                                    e);
                        }
                        args.put(parametersList.get(j + 1), content);
                    }
                    else if ("boolean".equalsIgnoreCase(typeInMap))
                    {
                        args.put(parametersList.get(j + 1),
                                "".equals(data) ? null : (new Boolean(data)).booleanValue());
                    }
                    else
                    {
                        args.put(parametersList.get(j + 1),
                                "".equals(data) ? null : (new Boolean(data)).booleanValue());
                    }
                }
                realParams[i] = args;
            }
            else
            {
                realParams[i] = "".equals(dataNotMap) ? null : dataNotMap;
            }
        }
        // for GBS-4702 "searchEntriesInBatch" api update, added "p_string"
        // parameter temporarily in UI for testing purpose.
        if ("searchEntriesInBatch".equals(operation.getName()))
        {
            // p_string
            List<util.Parameter> parameters = operation.getParaList();
            util.Parameter pString = operation.getParaList().get(parameters.size() - 1);
            if (pString != null)
            {
                // p_segmentMap
                Map segmentMap = new HashMap();
                segmentMap.put(new Long(-1), realParams[parameters.size() - 1]);
                realParams[2] = segmentMap;
                // remove "p_string" parameter
                parameters.remove(parameters.size() - 1);
                Class[] newParameterTypes = new Class[parameters.size()];
                Object[] newRealParams = new Object[parameters.size()];
                for (int i = 0; i < parameters.size(); i++)
                {
                    newParameterTypes[i] = parameterTypes[i];
                    newRealParams[i] = realParams[i];
                }
                parameterTypes = newParameterTypes;
                realParams = newRealParams;
            }
        }

        // get method
        Class cls = null;
        Method method = null;
        try
        {
            cls = Class.forName(defaultWSDLClass);
        }
        catch (Exception e)
        {
            WriteLog.info(WebServiceConstants.IS_LOG,
                    "class.forName() Exception in TestMangerImpl.testOpeation()");
            logger.error("Cannot find the class " + defaultWSDLClass, e);
        }

        try
        {
            method = cls.getMethod(operation.getName(), parameterTypes);
        }
        catch (Exception e)
        {
            try
            {
                method = findMethod(cls, operation, parameterTypes);
            }
            catch (Exception e1)
            {
                logger.error("Failed to get method info.", e1);
            }
        }
        if (method == null)
        {
            logger.error("Failed to find method '" + operation.getName() + "'.");
        }

        // invoke method
        String result = null;
        if (operation.getReturnType() != null)
        {
            result = String.valueOf(method.invoke(this.getProxy(), realParams));
            logger.info("Return content for method " + method.getName() + "\r\n" + result);
        }

        return result;
    }

    @SuppressWarnings("rawtypes")
    private static String getInputParams(HashMap obs)
    {
        String result = "[ ";
        Iterator iter = obs.entrySet().iterator();
        while (iter.hasNext())
        {
            Map.Entry entry = (Map.Entry) iter.next();
            Object val = entry.getValue();
            if (val != null)
            {
                result = result + val.toString() + "  ";
            }
        }
        result = result + " ]";
        return result;
    }

    @SuppressWarnings("rawtypes")
    private Method findMethod(Class WSDLClass, Operation operation, Class[] parameterTypes)
    {
        Method[] methods = WSDLClass.getMethods();
        String requestMethodName = operation.getName();
        for (Method method : methods)
        {
            if (!requestMethodName.equals(method.getName()))
                continue;

            if (method.getParameterCount() != parameterTypes.length)
                continue;

            boolean isMatched = true;
            Parameter[] ps = method.getParameters();
            for (int i = 0; i < method.getParameterCount(); i++)
            {
                String typeName = ps[i].getType().getName();

                // convert parameter type to basic data type as "parameterTypes"
                // are using that.
                if ("java.lang.Long".equals(typeName))
                    typeName = "long";
                else if ("java.lang.Integer".equals(typeName))
                    typeName = "int";
                else if ("java.lang.Float".equals(typeName))
                    typeName = "float";
                else if ("java.lang.Double".equals(typeName))
                    typeName = "double";
                else if ("java.lang.Boolean".equals(typeName))
                    typeName = "boolean";

                if (!typeName.equals(parameterTypes[i].getName()))
                {
                    isMatched = false;
                    break;
                }
            }

            if (isMatched)
            {
                return method;
            }
        }

        return null;
    }
}
