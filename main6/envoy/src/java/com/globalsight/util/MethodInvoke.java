/**
 *  Copyright 2009 Welocalize, Inc. 
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  
 *  You may obtain a copy of the License at 
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  
 */

package com.globalsight.util;

import org.apache.log4j.Logger;

import com.globalsight.log.GlobalSightCategory;
import com.globalsight.everest.workflow.WorkflowHelper;
import com.globalsight.util.CollectionHelper;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Array;
import java.util.StringTokenizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.SortedSet;
import java.util.Set;
import java.util.Collection;
import java.util.List;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.Vector;

/**
 * Standalone utility for invoking a static method, or invoking
 * a method on an object.
 */
public class MethodInvoke 
{
    private static final Logger CATEGORY =
            Logger.getLogger(
            MethodInvoke.class.getName());

    /**
     * A string representation of a null object used as a
     * method argument value.
     */
    public static final String NULL_ARG = "null";

    /**
     * An array specifier string as a suffix on a 
     * class name of type array.
     */
    public static final String ARRAY_SPECIFIER_SUFFIX = "[]";

    /**
     * A help message on usage of the main method.
     */
    public static final String HELP_MESSAGE = 
            "USAGE:\tjava " 
                    + MethodInvoke.class.getName() 
                    + " <fully qualified class name>"
                    + " [ <method name>"
                    + " [ <colon delimited method arguments>"
                    + " <colon delimited method parameter class names"
                    + " in order> ] ]"
                    + "\nSpecifying only the fully qualified class name returns this help "
                    + "\nmessage and a display of methods and constructors for the class. "
                    + "\nIn the colon delimited method arguments, a single argument that "
                    + "\nis a Collection or array of multiple values can be specified with a "
                    + "\ncoma delimited list of values.  In the colon delimited method "
                    + "arguments, a null value can be specified with the string '" + NULL_ARG + "' "
                    + "\nThe classes in method parameter class names must have constructors "
                    + "that take one argument of type Class, Object, Collection, List, Set, "
                    + "\nSortedSet Vector, ArrayList, HashSet, TreeSet, array, String, Integer, "
                    + "\nLong, Boolean, Short, Double, Float, Character, Byte, int, long, "
                    + "\nboolean, short, double, float, char, byte or array of these types. "
                    + "\nThe classes in method parameter class names can be arrays, specified "
                    + "\nwith the component type name followed by "
                    + ARRAY_SPECIFIER_SUFFIX                    
                    + "\nThe number of colon delimited method arguments and colon delimited "
                    + "\nmethod parameter class names must be the same. "
                    + "\nSee java.lang.Method.invoke(Object obj, Object[] args) "
                    ;

	/**
	 * Standalone utility for invoking a static method, or invoking
     * a method on an object.  @See HELP_MESSAGE.
	 */ 
    public static void main(String[] args) throws Exception
	{
        if (args.length < 2 || args.length == 3 || args.length > 4) 
		{
            System.out.println(HELP_MESSAGE);
            if (args.length == 1)
            {
                Class theClass = Class.forName(args[0]);
                System.out.println(getAllMethodsDisplayString(theClass));
                System.out.println(getAllConstructorsDisplayString(theClass));
            }
            return;
        }
        if (args == null)
        {
            args = new String [] {""};
        }
        System.out.println("command line arguments=" 
                + Arrays.asList(args).toString());
        if (CATEGORY.isDebugEnabled())
        {
            CATEGORY.debug("command line arguments=" 
                    + Arrays.asList(args).toString());
        }
        Class [] parameterClasses = new Class[] {};
        Object [] arguments = new Object [] {};
        Method method = null;
        Object resultObject = null;
        try
        {
            if (args.length == 2)
            { 
                method = getMethod(args[0], args[1], null);
                resultObject = invokeMethod(method, null, null);
            }
            if (args.length == 4)
            { 
                String [] argStrings = tokenizeString(args[2], ":"); 
                String [] parameterTypeStrings = tokenizeString(
                        args[3], ":");
                if (argStrings.length != parameterTypeStrings.length)
                {
                    System.out.println("number of method arguments"
                            + " not the same as the number of" 
                            + " parameter classes " 
                            + Arrays.asList(argStrings).toString()
                            + "  " 
                            + Arrays.asList(parameterTypeStrings
                            ).toString());
                    return;
                }             
                parameterClasses = forName(
                        parameterTypeStrings);
                arguments = constructArguments(argStrings,
                        parameterClasses);  
                method = getMethod(args[0], args[1], parameterClasses);
                resultObject = invokeMethod(method, null, arguments); 
            }
            String outputMessage = "Invocation of method "
                    + method.toString() + " returned:\n";
            if (resultObject != null)
            { 
                System.out.println(outputMessage
                        + 
                        WorkflowHelper.toDebugString(
                        resultObject).toString());
            }
            else
            {
                System.out.println(outputMessage + "a null object");
            }
        }
        catch (Exception e)
        {
            CATEGORY.error("command line ="
                    + Arrays.asList(args).toString() 
                    + " arguments=" 
                    + Arrays.asList(arguments).toString()
                    + " parameterClasses=" 
                    + Arrays.asList(parameterClasses).toString()
                    + " method=" + (method != null?method.toString()
                    :"null")
                    + " " + e.toString(), e);
        }
     }

	/**
	 * Invoke a method on an object, or a null object (which is
     * is static method).
     * @see java.lang.reflect.Method#invoke 
	 * @param p_method Method to invoke. 
     * @param p_object object on which to invoke the method.  
     * May be null, in which case it must be a static method. 
     * @param p_args arguments to the method.  May be null. 
     * @returns the object that is the result of invoking
     * the method.  It may be null. 
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
     * @throws InvocationTargetException 
	 */
    public static Object invokeMethod(Method p_method, 
            Object p_object, Object[] p_args) 
            throws IllegalAccessException, IllegalArgumentException,
            InvocationTargetException
	{ 
        if (p_args == null)
        {
            p_args = new Object[] {};
        }
        return p_method.invoke(p_object, p_args);
    }


    /**
	 * Return a Method.
     * @see java.lang.reflect.Method#invoke
	 * @param p_fullClassName fully qualified class name of 
     * the class the method is on.
	 * @param p_methodName method name.  
     * @param p_parameterTypes classes of the arguments to the method.  
     * May be null.
     * @returns a Method on the class with the method signature
     * matching the parameters.
     * @throws ClassNotFoundException
     * @throws NoSuchMethodException
	 */
    public static Method getMethod(String p_fullClassName, 
            String p_methodName,
            Class[] p_parameterTypes) 
            throws ClassNotFoundException,
            NoSuchMethodException
	{
        Class theClass = Class.forName(p_fullClassName); 
        Method method = null; 
        try
        { 
            method = theClass.getMethod(p_methodName, 
                    p_parameterTypes);
        }
        catch (NoSuchMethodException nsme)
        {
            CATEGORY.error(nsme + " " + p_methodName
                    + " parameter types=" 
                    + (p_parameterTypes!=null?
                    Arrays.asList(p_parameterTypes).toString()
                    :"null")
                    + "\n" + getAllMethodsDisplayString(theClass), nsme); 
            throw nsme; 
        }
        if (method == null)
        {
            NoSuchMethodException nsme = new NoSuchMethodException(
                    "method null " + theClass.toString() 
                    + " " + p_methodName); 
            CATEGORY.error(nsme + " " + p_methodName
                    + " parameter types=" 
                    + (p_parameterTypes!=null?
                    Arrays.asList(p_parameterTypes).toString()
                    :"null")
                    + "\n" + getAllMethodsDisplayString(theClass), nsme); 
            throw nsme; 
        }
        return method;
    }


    /**
	 * Return true if a method exists for a class with the given
     * name and parameter types.
     * @see java.lang.reflect.Method#invoke
	 * @param p_fullClassName fully qualified class name of 
     * the class the method is on.
	 * @param p_methodName method name.  
     * @param p_parameterTypes classes of the arguments to the method.  
     * May be null.
     * @returns true if a method exists for a class with the given
     * name and parameter types.
     */
    public static boolean hasMethod(String p_fullClassName, 
            String p_methodName, Class[] p_parameterTypes)
    {
        try
        {
            Class theClass = Class.forName(p_fullClassName);
            theClass.getMethod(p_methodName, 
                    p_parameterTypes);
        }
        catch (Exception e)
        {
            return false;
        }
        return true;
    }
            

    /**
     * Convert a String of delimited tokens into an array of
     * Strings.
     * @param p_string a String of delimited tokens.
     * @param p_delimiter delimiter used in the string p_string.
     * @returns array of Strings.
     * @throws StringIndexOutOfBoundsException
     */
    public static String[] tokenizeString(String p_string, 
            String p_delimiter)
            throws StringIndexOutOfBoundsException 
    {
        StringTokenizer tokenizer = new StringTokenizer(p_string, 
                p_delimiter);
        ArrayList list = new ArrayList();
        while (tokenizer.hasMoreElements()) 
        {
            list.add((String)tokenizer.nextElement());
        }
        return (String [])list.toArray(new String[list.size()]);
    }


    /**
     * Returns an array of component types for each of the 
     * Strings in the
     * array of component type names.  Handles classes, primitive types,
     * and array types.
     * @see java.lang.Class#forName
     * @param p_componentTypeNames array of component type names (i.e.
     * a class name, primitve type name, or array type name).
     * @returns array of Classes.
     * @throws ClassNotFoundException
     */
    public static Class[] forName(String[] p_componentTypeNames)
            throws ClassNotFoundException
    {
        Class[] classes = new Class[p_componentTypeNames.length];
        for (int i = 0; i < p_componentTypeNames.length; i++)
        {
            classes[i] = forName(p_componentTypeNames[i]); 
        }
        return classes;
    } 


    /**
     * Returns a component type for the 
     * component type name.  Handles classes, primitive types,
     * and array types.
     * @see java.lang.Class#forName
     * @param p_componentTypeName component type name (i.e.
     * a class name, primitve type name, or array type name).
     * @returns a Class.
     * @throws ClassNotFoundException
     */
    public static Class forName(String p_componentTypeName)
            throws ClassNotFoundException
    {
        // if it's a primitive, don't do Class.forName
        Class theClass = primitiveForName(p_componentTypeName);
        if (theClass == null)
        {
            // if it's an array, don't do Class.forName
            theClass = arrayForName(p_componentTypeName);
        }
        if (theClass == null)
        {
            theClass = Class.forName(p_componentTypeName);
        }
        return theClass;
    } 

   
    /**
     * Construct method arguments from arguments that are Strings.
     * The actual method arguments may not be Strings, and so must
     * be constructed from the Strings.
     * @see #findBestConstructor
     * @param p_argStrings method arguments as Strings.
     * The actual method arguments may not be Strings, and so must
     * be constructed from the Strings.
     * @param p_parameterClasses method parameter classes.  These are
     * the classes of the method arguments.
     * @returns the method arguments.
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     */ 
    public static Object [] constructArguments(String[] p_argStrings,
            Class [] p_parameterClasses)
            throws InstantiationException,
            IllegalAccessException, IllegalArgumentException,
            InvocationTargetException, ClassNotFoundException
    {
        // ArrayList supports the set method.
        List argStrings = new ArrayList(Arrays.asList(p_argStrings));
        for (int i = 0; i < argStrings.size(); i++)
        {
            String arg = (String)argStrings.get(i);
            if (arg.indexOf(",") != -1)
            {
                String[] multipleArgs = tokenizeString(
                        arg, ",");
                // replace the delimited strings with a List
                List multipleArgsList = Arrays.asList(multipleArgs);
                CollectionHelper.removeNulls(multipleArgsList);
                argStrings.set(i, multipleArgsList);
            }
        }
        Object [] args = new Object[p_argStrings.length];
        for (int i = 0; i < p_parameterClasses.length; i++)
        {
            args[i] = constructObjectFromValue(
                    argStrings.get(i), p_parameterClasses[i]);
            if (CATEGORY.isDebugEnabled())
            { 
                CATEGORY.debug("for arg "
                        + Integer.toString(i)
                        + " parameterClass " 
                        + p_parameterClasses[i].toString()
                        + " argStrings=" 
                        + argStrings.get(i).toString() 
                        + " constructed=" + args[i].toString()
                        );
            } 
        }
        return args;
    } 


    /**
     * Construct an Object of class p_class from a String. 
     * @see #findBestConstructor
     * @param p_value String or List of String values 
     * to construct an Object with. 
     * @param p_class the class the the object to construct.
     * @returns an object of class p_class constructed from p_value.
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     */ 
    public static Object constructObjectFromValue(
            Object p_value, Class p_class)
            throws InstantiationException,
            IllegalAccessException, IllegalArgumentException,
            InvocationTargetException, ClassNotFoundException
    {
        try
        {
            if (p_value.equals(NULL_ARG))
            {
                return null;       
            }    
            if (p_class.equals(Class.class))
            {
                return Class.forName((String)p_value);
            }
            if (p_class.equals(Object.class))
            {
                return (String)p_value;
            }
            if (p_class.isPrimitive())
            {
                return constructPrimitive(p_class, (String)p_value);
            } 
            if (p_class.isArray())
            {
                return constructArray(
                        getArrayComponentTypeElementType(p_class), 
                        (List)p_value);
            } 
            Constructor bestConstructor = null;
            Constructor [] constructors = null;    
            constructors = p_class.getConstructors();
            // Can't construct interfaces, use an implementing class
            if (p_class.equals(Collection.class)
                    || p_class.equals(List.class))
            {
                constructors = ArrayList.class.getConstructors();
            }
            if (p_class.equals(Set.class))
            {
                constructors = HashSet.class.getConstructors();
            }
            if (p_class.equals(SortedSet.class))
            {
                constructors = TreeSet.class.getConstructors();
            }
            if (Collection.class.isAssignableFrom(p_class))
            {
                bestConstructor = 
                        findBestArrayConstructor(constructors,
                        null);
                if (bestConstructor == null)
                {
                    bestConstructor = 
                            findBestCollectionConstructor(constructors,
                            p_value.getClass());
                }
            }
            else
            {
                bestConstructor = 
                        findBestStringConstructor(constructors, null);
            }
            if (bestConstructor == null)
            {
                String message = "constructObjectFromValue: no best constructor"
                        + " for p_value " 
                        + p_value.toString() 
                        + " and class " 
                        + p_class.toString()  
                        + " constructors=" 
                        +  (constructors!=null?
                        Arrays.asList(constructors).toString():"null")
                        + "\n"
                        + getAllConstructorsDisplayString(
                        p_class);
                CATEGORY.error(message);
                throw new  IllegalArgumentException(message);
            }
            return bestConstructor.newInstance(
                    new Object[] {p_value}); 
        }
        catch (InstantiationException ie)
        {
            CATEGORY.error("constructObjectFromValue: " + ie.toString() 
                    + GlobalSightCategory.getLineContinuation()
                    + "p_value=" + p_value.toString()
                    + " p_class=" + p_class.toString(), ie);
            throw ie;
        }
        catch (IllegalAccessException iae)
        {
            CATEGORY.error("constructObjectFromValue: " + iae.toString() 
                    + GlobalSightCategory.getLineContinuation()
                    + "p_value=" + p_value.toString()
                    + " p_class=" + p_class.toString(), iae);
            throw iae;
        }
        catch (IllegalArgumentException iare)
        {
            CATEGORY.error("constructObjectFromValue: " + iare.toString() 
                    + GlobalSightCategory.getLineContinuation()
                    + "p_value=" + p_value.toString()
                    + " p_class=" + p_class.toString(), iare);
            throw iare;
        }
        catch (InvocationTargetException ite)
        {
            CATEGORY.error("constructObjectFromValue: " + ite.toString() 
                    + GlobalSightCategory.getLineContinuation()
                    + "p_value=" + p_value.toString()
                    + " p_class=" + p_class.toString(), ite);
            throw ite;
        }
        catch (ClassNotFoundException cnfe)
        {
            CATEGORY.error("constructObjectFromValue: " + cnfe.toString() 
                    + GlobalSightCategory.getLineContinuation()
                    + "p_value=" + p_value.toString()
                    + " p_class=" + p_class.toString(), cnfe);
            throw cnfe;
        }
    }


    /**
     * Find the best constructor that takes a single String argument
     * from the array of constructors.
     * <p>
     * The best constructor is defined by order of:
     * <ol>
     * <li>takes a single String argument
     * <li>takes a single Integer or int argument
     * <li>takes a single Long or long argument
     * <li>takes a single Boolean or boolean argument
     * <li>takes one of the above four arguments in order, 
     * but with the minimum
     * number of other arguments.
     * Goes through the the above list for 1 additional argument, 
     * then 2, and so on.
     * @param p_constructors array of Constructors to choose from.
     * @param p_parameterClass best choice is a Constructor that
     * takes this class.  May be null.
     */
    public static Constructor findBestStringConstructor(
            Constructor [] p_constructors,
            Class p_parameterClass) 
    {   
        Constructor bestConstructor = null; 
        int numberOfArguments = 1;     
        for (int j = 0; j < p_constructors.length &&
                 bestConstructor == null; 
                j++, numberOfArguments++)
        {
            Class [] parameterTypes = 
                    p_constructors[j].getParameterTypes();
            for (int k = 0; k < numberOfArguments 
                    && k < parameterTypes.length; k++)
            {
                if (p_parameterClass != null)
                {
                    if (parameterTypes[k].equals(p_parameterClass))
                    {
                        bestConstructor =  p_constructors[j];
                        break;
                    }
                    continue;
                }   
                if (parameterTypes[k].equals(String.class))
                {
                    bestConstructor =  p_constructors[j];
                    break;
                } 
                if (parameterTypes[k].equals(Integer.class))
                {
                    bestConstructor =  p_constructors[j];
                    break;
                }
                if (parameterTypes[k].equals(Long.class))
                {
                    bestConstructor =  p_constructors[j];
                    break;
                }        
                if (parameterTypes[k].equals(Boolean.class))
                {
                    bestConstructor =  p_constructors[j];
                    break;
                } 
                if (parameterTypes[k].equals(Short.class))
                {
                    bestConstructor =  p_constructors[j];
                    break;
                }
                if (parameterTypes[k].equals(Double.class))
                {
                    bestConstructor =  p_constructors[j];
                    break;
                }
                if (parameterTypes[k].equals(Float.class))
                {
                    bestConstructor =  p_constructors[j];
                    break;
                }
                if (parameterTypes[k].equals(Character.class))
                {
                    bestConstructor =  p_constructors[j];
                    break;
                } 
                if (parameterTypes[k].equals(Byte.class))
                {
                    bestConstructor =  p_constructors[j];
                    break;
                }   
            }
        }
        if (bestConstructor == null && p_parameterClass != null)
        {
            // try again without the hint of a p_parameterClass
            bestConstructor = findBestStringConstructor(
                    p_constructors, null);
        }
        return bestConstructor;
    }


    /**
     * Find the best constructor that takes a single array argument
     * from the array of constructors.
     * <p>
     * The best array constructor is defined by order of:
     * <ol>
     * <li>takes a single String[] argument
     * <li>takes a single Integer[] or int[] argument
     * <li>takes a single Long[] or long[] argument
     * <li>takes a single Boolean[] or boolean[] argument
     * <li>takes a single Character[] or char[] argument
     * <li>takes a single Byte[] or byte[] argument
     * <li>takes a single Short[] or short[] argument
     * <li>takes a single Double[] or double[] argument
     * <li>takes a single Float[] or float[] argument
     * <li>takes one of the above four arguments in order, 
     * but with the minimum
     * number of other arguments.
     *</ol>
     * Goes through the the above list for 1 additional argument, 
     * then 2, and so on.
     * @param p_constructors array of Constructors to choose from.
     * @param p_parameterClass best choice is a Constructor that
     * takes this class.  May be null.
     */
    public static Constructor findBestArrayConstructor(
            Constructor [] p_constructors,
            Class p_parameterClass) 
    {   
        Constructor bestConstructor = null; 
        int numberOfArguments = 1;     
        for (int j = 0; j < p_constructors.length &&
                 bestConstructor == null; 
                j++, numberOfArguments++)
        {
            Class [] parameterTypes = 
                    p_constructors[j].getParameterTypes();
            for (int k = 0; k < numberOfArguments 
                    && k < parameterTypes.length; k++)
            {
                if (p_parameterClass != null)
                {
                    if (parameterTypes[k].equals(p_parameterClass))
                    {
                        bestConstructor =  p_constructors[j];
                        break;
                    }
                    continue;
                }   
                if (parameterTypes[k].equals(String[].class))
                {
                    bestConstructor =  p_constructors[j];
                    break;
                } 
                if (parameterTypes[k].equals(Integer[].class))
                {
                    bestConstructor =  p_constructors[j];
                    break;
                }
                if (parameterTypes[k].equals(int[].class))
                {
                    bestConstructor =  p_constructors[j];
                    break;
                }
                if (parameterTypes[k].equals(Long[].class))
                {
                    bestConstructor =  p_constructors[j];
                    break;
                } 
                if (parameterTypes[k].equals(long[].class))
                {
                    bestConstructor =  p_constructors[j];
                    break;
                }        
                if (parameterTypes[k].equals(Boolean[].class))
                {
                    bestConstructor =  p_constructors[j];
                    break;
                } 
                if (parameterTypes[k].equals(boolean[].class))
                {
                    bestConstructor =  p_constructors[j];
                    break;
                } 
                if (parameterTypes[k].equals(Short[].class))
                {
                    bestConstructor =  p_constructors[j];
                    break;
                } 
                if (parameterTypes[k].equals(short[].class))
                {
                    bestConstructor =  p_constructors[j];
                    break;
                }  
                if (parameterTypes[k].equals(Double[].class))
                {
                    bestConstructor =  p_constructors[j];
                    break;
                } 
                if (parameterTypes[k].equals(double[].class))
                {
                    bestConstructor =  p_constructors[j];
                    break;
                }  
                if (parameterTypes[k].equals(Float[].class))
                {
                    bestConstructor =  p_constructors[j];
                    break;
                } 
                if (parameterTypes[k].equals(float[].class))
                {
                    bestConstructor =  p_constructors[j];
                    break;
                }  
                if (parameterTypes[k].equals(Character[].class))
                {
                    bestConstructor =  p_constructors[j];
                    break;
                } 
                if (parameterTypes[k].equals(char[].class))
                {
                    bestConstructor =  p_constructors[j];
                    break;
                }  
                if (parameterTypes[k].equals(Byte[].class))
                {
                    bestConstructor =  p_constructors[j];
                    break;
                } 
                if (parameterTypes[k].equals(byte[].class))
                {
                    bestConstructor =  p_constructors[j];
                    break;
                }  
            }
        }
        if (bestConstructor == null && p_parameterClass != null)
        {
            // try again without the hint of a p_parameterClass
            bestConstructor = findBestStringConstructor(
                    p_constructors, null);
        }
        return bestConstructor;
    }


    /**
     * Find the best constructor that takes a single Collection argument
     * from the array of constructors.
     * <p>
     * The best constructor is defined by order of:
     * <ol>
     * <li>takes a single SortedSet argument
     * <li>takes a single List
     * <li>takes a single Set argument
     * <li>takes a single Collection argument
     * <li>takes one of the above four arguments in order, 
     * but with the minimum
     * number of other arguments.
     * Goes through the the above list for 1 additional argument, 
     * then 2, and so on.
     * @param p_constructors array of Constructors to choose from.
     * @param p_parameterCollectionClass best choice is a Constructor that
     * takes this class.  May be null.
     */
    public static Constructor findBestCollectionConstructor(
            Constructor [] p_constructors, 
            Class p_parameterCollectionClass) 
    {   
        Constructor bestConstructor = null; 
        int numberOfArguments = 1;     
        for (int j = 0; j < p_constructors.length &&
                 bestConstructor == null; 
                j++, numberOfArguments++)
        {
            Class [] parameterTypes = 
                    p_constructors[j].getParameterTypes();
            for (int k = 0; k < numberOfArguments 
                    && k < parameterTypes.length; k++)
            {
                if (p_parameterCollectionClass != null)
                {
                    if (parameterTypes[k].equals(
                            p_parameterCollectionClass))
                    {
                        bestConstructor =  p_constructors[j];
                        break;
                    }
                    continue;
                }   
                if (parameterTypes[k].equals(SortedSet.class))
                {
                    bestConstructor =  p_constructors[j];
                    break;
                } 
                if (parameterTypes[k].equals(List.class))
                {
                    bestConstructor =  p_constructors[j];
                    break;
                }
                if (parameterTypes[k].equals(Set.class))
                {
                    bestConstructor =  p_constructors[j];
                    break;
                }        
                if (parameterTypes[k].equals(Collection.class))
                {
                    bestConstructor =  p_constructors[j];
                    break;
                } 
                if (parameterTypes[k].equals(Vector.class))
                {
                    bestConstructor =  p_constructors[j];
                    break;
                } 
                if (parameterTypes[k].equals(ArrayList.class))
                {
                    bestConstructor =  p_constructors[j];
                    break;
                } 
                if (parameterTypes[k].equals(HashSet.class))
                {
                    bestConstructor =  p_constructors[j];
                    break;
                }
                if (parameterTypes[k].equals(TreeSet.class))
                {
                    bestConstructor =  p_constructors[j];
                    break;
                }
            }
        }
        if (bestConstructor == null 
                && p_parameterCollectionClass != null)
        {
            // try again without the hint of a p_parameterCollectionClass
            bestConstructor = findBestCollectionConstructor(
                    p_constructors, null);
        }
        return bestConstructor;
    }


    /**
     * Return a string appropriate for display of all the methods
     * for the class.
     */
    public static String getAllMethodsDisplayString(Class p_class)
    {
        StringBuffer buff = new StringBuffer(400);
        buff.append(p_class.toString() + " methods:\n");
        Method[] methods = p_class.getMethods();
        for (int i = 0; i < methods.length; i++)
        {
            buff.append(methods[i].toString() + "\n");
        }
        return buff.toString();    
    }

    /**
     * Return a string appropriate for display of all the constructors
     * for the class.
     */
    public static String getAllConstructorsDisplayString(Class p_class)
    {
        StringBuffer buff = new StringBuffer(400);
        buff.append(p_class.toString() + " constructors:\n");
        Constructor[] constructors = p_class.getConstructors();
        for (int i = 0; i < constructors.length; i++)
        {
            buff.append(constructors[i].toString() + "\n");
        }
        return buff.toString();    
    }


    /**
     * Constructs an object equivalent of a primitive that corresponds 
     * to the primitve type class.
     * @param p_primitiveClass the primitive type Class.  One of
     * Boolean.TYPE, Character.TYPE, Byte.TYPE, Short.TYPE, 
     * Integer.TYPE, Long.TYPE, Float.TYPE, Double.TYPE.
     * @param p_arg the value to construct the object with.
     * @returns  an object equivalent of a primitive that corresponds 
     * to the primitve type class.
     * Returns null if the class is not a primitive type class.
     * @throws NumberFormatException
     */ 
    public static Object constructPrimitive(Class p_primitiveClass,
            String p_arg) throws NumberFormatException
    {
        if (p_primitiveClass.equals(Boolean.TYPE))
        {
            return new Boolean(p_arg);
        }
        if (p_primitiveClass.equals(Character.TYPE))
        {
            char[] chars = p_arg.toCharArray();
            if (chars.length == 0)
            {
                return null;
            } 
            new Character(chars[0]);
        }
        if (p_primitiveClass.equals(Byte.TYPE))
        {
            return new Byte(p_arg);
        }
        if (p_primitiveClass.equals(Short.TYPE))
        {
            return new Short(p_arg);
        }
        if (p_primitiveClass.equals(Integer.TYPE))
        {
            return new Integer(p_arg);
        }
        if (p_primitiveClass.equals(Long.TYPE))
        {
            return new Long(p_arg);
        }
        if (p_primitiveClass.equals(Float.TYPE))
        {
            return new Float(p_arg);
        }
        if (p_primitiveClass.equals(Double.TYPE))
        {
            return new Double(p_arg);
        }
        return null;
    }


    /**
     * Does the quivivalent of Class.forName(String) for primitive
     * types.
     * @param p_primitiveTypeName the primitive type name.
     * @returns the primitive Class representation of the
     * primitive type.  One of 
     * Boolean.TYPE, Character.TYPE, Byte.TYPE, Short.TYPE, 
     * Integer.TYPE, Long.TYPE, Float.TYPE, Double.TYPE.
     * Returns null if the string p_primitiveTypeName
     * is not a primitive type name.
     */
    public static Class primitiveForName(String p_primitiveTypeName)
    {
        if (p_primitiveTypeName.equals(Boolean.TYPE.getName()))
        {
            return Boolean.TYPE;
        }
        if (p_primitiveTypeName.equals(Character.TYPE.getName()))
        {
            return Character.TYPE;
        }
        if (p_primitiveTypeName.equals(Byte.TYPE.getName()))
        {
            return Byte.TYPE;
        }
        if (p_primitiveTypeName.equals(Short.TYPE.getName()))
        {
            return Short.TYPE;
        }
        if (p_primitiveTypeName.equals(Integer.TYPE.getName()))
        {
            return Integer.TYPE;
        }
        if (p_primitiveTypeName.equals(Long.TYPE.getName()))
        {
            return Long.TYPE;
        }
        if (p_primitiveTypeName.equals(Float.TYPE.getName()))
        {
            return Float.TYPE;
        }
        if (p_primitiveTypeName.equals(Double.TYPE.getName()))
        {
            return Double.TYPE;
        }
        return null;
    } 


    /**
     * Does the quivivalent of Class.forName(String) for array
     * declaration types.
     * @param p_arrayDeclarationTypeName the class name appended with the 
     * array specifier @see ARRAY_SPECIFIER_SUFFIX.
     * @returns the array Class representation of the
     * array type. 
     * Returns null if the string p_arrayClassName
     * is not a class with an array specifier.
     * @throws NegativeArraySizeException
     * @throws ClassNotFoundException
     */
    public static Class arrayForName(String p_arrayDeclarationTypeName)
            throws NegativeArraySizeException,
            ClassNotFoundException
    { 
        String elementTypeName = 
                getArrayDeclarationTypeNameElementTypeName(
                p_arrayDeclarationTypeName);
        if (elementTypeName == null)
        {
            return null;
        }
        // if it's a primitive, don't do Class.forName
        Class theClass = primitiveForName(elementTypeName);
        if (theClass == null)
        {
            theClass = forName(elementTypeName);
        }
        Object arrayInstance = Array.newInstance(theClass, 0);
        return arrayInstance.getClass();
    } 


     /**
     * Constructs an array of objects of the p_componentType component 
     * type.
     * @param p_componentType the component type.  Must be one of
     * <ul>
     * <li>Class.class
     * <li>Object.class
     * <li>Collection.class
     * <li>List.class
     * <li>Set.class
     * <li>SortedSet.class
     * <li>Vector.class
     * <li>ArrayList.class
     * <li>HashSet.class
     * <li>TreeSet.clas
     * <li>String.class
     * <li>Integer.class
     * <li>Long.class
     * <li>Boolean.class
     * <li>Short.class
     * <li>Double.class
     * <li>Float.class
     * <li>Character.class
     * <li>Byte.class
     * <li>Integer.TYPE
     * <li>Long.TYPE
     * <li>Boolean.TYPE
     * <li>Short.TYPE
     * <li>Double.TYPE
     * <li>Float.TYPE
     * <li>Character.TYPE
     * <li>Byte.TYPE
     *</ul>
     * of object or a primitive type class.  A primitive type class
     * is one of
     * Boolean.TYPE, Character.TYPE, Byte.TYPE, Short.TYPE, 
     * Integer.TYPE, Long.TYPE, Float.TYPE, Double.TYPE.
     * @param p_arg the List of String values, or List of 
     * List of String values
     * to construct the object with.
     * @returns  an object array that corresponds 
     * to the p_componentType.
     * Returns null if p_componentType can't be constructed from Strings.
     * @throws NegativeArraySizeException
     */ 
    public static Object constructArray(Class p_componentType,
            List p_arg) throws NumberFormatException, 
            InstantiationException, IllegalAccessException,
            InvocationTargetException, ClassNotFoundException
    {
        Object arrayInstance = Array.newInstance(p_componentType, 
                p_arg.size());
        Class elementType = getArrayComponentTypeElementType(
                    p_componentType.getName());
        for (int i = 0; i < p_arg.size(); i++)
        { 
            Object constructedElement = constructObjectFromValue(
                    p_arg.get(i), elementType);
            if (elementType.equals(Class.class))
            {                   
                ((Class[])arrayInstance)[i] = (Class)constructedElement; 
            }
            if (elementType.equals(Object.class))
            {                   
                ((Object[])arrayInstance)[i] = constructedElement; 
            }
            if (elementType.equals(Collection.class))
            {                   
                ((Collection[])arrayInstance)[i] = 
                        (Collection)constructedElement; 
            }
            if (elementType.equals(List.class))
            {                   
                ((List[])arrayInstance)[i] = (List)constructedElement; 
            }
            if (elementType.equals(Set.class))
            {                   
                ((Set[])arrayInstance)[i] = (Set)constructedElement; 
            }
            if (elementType.equals(SortedSet.class))
            {                   
                ((SortedSet[])arrayInstance)[i] = (SortedSet)constructedElement; 
            }
            if (elementType.equals(Vector.class))
            {                   
                ((Vector[])arrayInstance)[i] = (Vector)constructedElement; 
            }
            if (elementType.equals(ArrayList.class))
            {                   
                ((ArrayList[])arrayInstance)[i] = 
                        (ArrayList)constructedElement; 
            }
            if (elementType.equals(HashSet.class))
            {                   
                ((HashSet[])arrayInstance)[i] = 
                        (HashSet)constructedElement; 
            }
            if (elementType.equals(TreeSet.class))
            {                   
                ((TreeSet[])arrayInstance)[i] = 
                        (TreeSet)constructedElement; 
            }
            if (elementType.equals(String.class))
            {                   
                ((String[])arrayInstance)[i] = (String)constructedElement; 
            }
            if (elementType.equals(Integer.class))
            {                   
                ((Integer[])arrayInstance)[i] = (Integer)constructedElement; 
            }
            if (elementType.equals(Long.class))
            {                   
                ((Long[])arrayInstance)[i] = (Long)constructedElement; 
            }
            if (elementType.equals(Boolean.class))
            {                   
                ((Boolean[])arrayInstance)[i] = 
                        (Boolean)constructedElement; 
            }
            if (elementType.equals(Short.class))
            {                   
                ((Short[])arrayInstance)[i] = (Short)constructedElement; 
            }
            if (elementType.equals(Double.class))
            {                   
                ((Double[])arrayInstance)[i] = (Double)constructedElement; 
            }
            if (elementType.equals(Float.class))
            {                   
                ((Float[])arrayInstance)[i] = (Float)constructedElement; 
            }
            if (elementType.equals(Character.class))
            {                   
                ((Character[])arrayInstance)[i] = 
                        (Character)constructedElement; 
            }
            if (elementType.equals(Byte.class))
            {                   
                ((Byte[])arrayInstance)[i] = (Byte)constructedElement; 
            }
            if (elementType.equals(Integer.TYPE))
            {                   
                ((int[])arrayInstance)[i] = 
                        ((Integer)constructedElement).intValue(); 
            }
            if (elementType.equals(Long.TYPE))
            {                   
                ((long[])arrayInstance)[i] = 
                        ((Long)constructedElement).longValue(); 
            }
            if (elementType.equals(Boolean.TYPE))
            {                   
                ((boolean[])arrayInstance)[i] = 
                        ((Boolean)constructedElement).booleanValue(); 
            }
            if (elementType.equals(Short.TYPE))
            {                   
                ((short[])arrayInstance)[i] = 
                        ((Short)constructedElement).shortValue(); 
            }
            if (elementType.equals(Double.TYPE))
            {                   
                ((double[])arrayInstance)[i] = 
                        ((Double)constructedElement).doubleValue(); 
            }
            if (elementType.equals(Float.TYPE))
            {                   
                ((float[])arrayInstance)[i] = 
                        ((Float)constructedElement).floatValue(); 
            }
            if (elementType.equals(Character.TYPE))
            {                   
                ((char[])arrayInstance)[i] = 
                        ((Character)constructedElement).charValue(); 
            }
            if (elementType.equals(Byte.TYPE))
            {                   
                ((byte[])arrayInstance)[i] = 
                        ((Byte)constructedElement).byteValue(); 
            }
        }
        return arrayInstance;
    }  


    /**
     * Given an array component type (i.e. a class that Class.isArray()
     * returns true),
     * return the array's element type.
     * @param p_arrayComponentType a component type that is an array. 
     * @returns the array's element component type.
     * Returns null if p_arrayComponentType is not an array.
     */ 
    public static Class getArrayComponentTypeElementType(
            Class p_arrayComponentType) throws ClassNotFoundException
    {
        if ( ! p_arrayComponentType.isArray())
        {
            return null;
        } 
        String elementTypeName = getArrayComponentTypeElementTypeName(
                p_arrayComponentType);
        return forName(elementTypeName);
    }


    /**
     * Given an array declaration type name (i.e. a class name that ends
     * with ARRAY_SPECIFIER_SUFFIX), return the array's element
     * type name (e.g. if the element is a class, return the class name).
     * @param p_arrayDeclarationTypeName an array declaration type name 
     * that ends with 
     * ARRAY_SPECIFIER_SUFFIX
     * @returns the array's element type name.
     * Returns null if p_arrayDeclarationTypeName does not end with
     * ARRAY_SPECIFIER_SUFFIX.
     */ 
    public static String getArrayDeclarationTypeNameElementTypeName(
            String p_arrayDeclarationTypeName)
    {
        String arrayDeclarationTypeName = 
                p_arrayDeclarationTypeName.trim();
        if ( ! arrayDeclarationTypeName.endsWith(ARRAY_SPECIFIER_SUFFIX))
        {
            return null;
        }
        return arrayDeclarationTypeName.substring(
                0, arrayDeclarationTypeName.length() - 
                ARRAY_SPECIFIER_SUFFIX.length());
    }


    /**
     * Given a runtime array component type,
     * return the array's element
     * type name (e.g. if the element type is a long, 
     * returns Long.Type.getName()).
     * See Java Language Specification 20.3.2
     * @param p_arrayComponentType an array component runtime type.  
     * @returns the array's element type name.
     * Returns null if p_arrayComponentType is not an array type.
     */ 
    public static String getArrayComponentTypeElementTypeName(
            Class p_arrayComponentType)
            throws IllegalArgumentException
    {
        if ( ! p_arrayComponentType.isArray())
        {
            return null;
        } 
        String arrayRuntimeSignature = 
                p_arrayComponentType.getName();
        return getArrayComponentTypeElementTypeName(arrayRuntimeSignature);
    }


    /**
     * Given a array component runtime type name,
     * return the array's element
     * type name (e.g. if the element type is a long, 
     * returns Long.Type.getName()).
     * See Java Language Specification 20.3.2
     * @param p_arrayComponentRuntimeTypeName 
     * an array component runtime type name.  
     * @returns the array's element type name.
     * Returns null if p_arrayComponentRuntimeTypeName
     * is not a runtime array type name.
     */ 
    public static String getArrayComponentTypeElementTypeName(
            String p_arrayComponentRuntimeTypeName)
            throws IllegalArgumentException
    { 
        /**
         * If p_arrayComponentRuntimeTypeName begins with
         * multiple open brackets (an array of arrays), 
         * too difficult to construct one so throw exception.
         * If not, replace the first two characters, which are
         * an open bracket followed by the element type name encoding,
         * with the primitve type name.
         */
        String nestedArraySignatureStartsWith = "[[";
        if (p_arrayComponentRuntimeTypeName.startsWith(
                nestedArraySignatureStartsWith))
        {
            String message = "Can't handle constructing arguments "
                    + " that are nested arrays " 
                    + p_arrayComponentRuntimeTypeName; 
            CATEGORY.error(message);
            throw new IllegalArgumentException(message);
        }
        else
        {
            StringBuffer buff = new StringBuffer(
                    p_arrayComponentRuntimeTypeName);
            String encoding =
                    p_arrayComponentRuntimeTypeName.substring(1,2); 
            if (encoding.equals("B"))
            {
                buff.replace(0,2,"byte");
            }
            if (encoding.equals("C"))
            {
                buff.replace(0,2,"char");
            }
            if (encoding.equals("D"))
            {
                buff.replace(0,2,"double");
            }
            if (encoding.equals("F"))
            {
                buff.replace(0,2,"float");
            }
            if (encoding.equals("I"))
            {
                buff.replace(0,2,"int");
            }
            if (encoding.equals("J"))
            {
                buff.replace(0,2,"long");
            }
            if (encoding.equals("L"))
            {
                // class or interface, class name follows
                buff.replace(0,2,"");
                // remove semi-colon at the end
                buff.replace(buff.length()-1, buff.length(), "");
            }
            if (encoding.equals("S"))
            {
                buff.replace(0,2,"short");
            }
            if (encoding.equals("Z"))
            {
                buff.replace(0,2,"boolean");
            }
            return buff.toString();
        }     
    }


    /**
     * Given a array component runtime type name,
     * return the array's element
     * type  (e.g. if the element type is a long, 
     * returns Long.Type).
     * See Java Language Specification 20.3.2
     * @param p_arrayComponentRuntimeTypeName 
     * an array component runtime type name.  
     * @returns the array's element type.
     * Returns null if p_arrayComponentRuntimeTypeName
     * is not a runtime array type name.
     */ 
    public static Class getArrayComponentTypeElementType(
            String p_arrayComponentRuntimeTypeName)
            throws IllegalArgumentException, ClassNotFoundException
    { 
        return forName(getArrayComponentTypeElementTypeName(
                p_arrayComponentRuntimeTypeName));
    } 
}      
