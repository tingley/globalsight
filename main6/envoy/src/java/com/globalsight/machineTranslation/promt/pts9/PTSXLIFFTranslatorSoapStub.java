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
package com.globalsight.machineTranslation.promt.pts9;

public class PTSXLIFFTranslatorSoapStub extends org.apache.axis.client.Stub
        implements PTSXLIFFTranslatorSoap
{
    private java.util.Vector cachedSerClasses = new java.util.Vector();
    private java.util.Vector cachedSerQNames = new java.util.Vector();
    private java.util.Vector cachedSerFactories = new java.util.Vector();
    private java.util.Vector cachedDeserFactories = new java.util.Vector();

    static org.apache.axis.description.OperationDesc[] _operations;

    static
    {
        _operations = new org.apache.axis.description.OperationDesc[4];
        _initOperationDesc1();
    }

    private static void _initOperationDesc1()
    {
        org.apache.axis.description.OperationDesc oper;
        org.apache.axis.description.ParameterDesc param;
        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("TranslateFormattedText");
        param = new org.apache.axis.description.ParameterDesc(
                new javax.xml.namespace.QName(
                        "http://Promt.Pits.Translator/Services/", "DirId"),
                org.apache.axis.description.ParameterDesc.IN,
                new javax.xml.namespace.QName(
                        "http://www.w3.org/2001/XMLSchema", "int"), int.class,
                false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(
                new javax.xml.namespace.QName(
                        "http://Promt.Pits.Translator/Services/", "TplId"),
                org.apache.axis.description.ParameterDesc.IN,
                new javax.xml.namespace.QName(
                        "http://www.w3.org/2001/XMLSchema", "string"),
                java.lang.String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(
                new javax.xml.namespace.QName(
                        "http://Promt.Pits.Translator/Services/", "strText"),
                org.apache.axis.description.ParameterDesc.IN,
                new javax.xml.namespace.QName(
                        "http://www.w3.org/2001/XMLSchema", "string"),
                java.lang.String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(
                new javax.xml.namespace.QName(
                        "http://Promt.Pits.Translator/Services/", "FileType"),
                org.apache.axis.description.ParameterDesc.IN,
                new javax.xml.namespace.QName(
                        "http://www.w3.org/2001/XMLSchema", "string"),
                java.lang.String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName(
                "http://www.w3.org/2001/XMLSchema", "string"));
        oper.setReturnClass(java.lang.String.class);
        oper.setReturnQName(new javax.xml.namespace.QName(
                "http://Promt.Pits.Translator/Services/",
                "TranslateFormattedTextResult"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[0] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("TranslateFormattedText2");
        param = new org.apache.axis.description.ParameterDesc(
                new javax.xml.namespace.QName(
                        "http://Promt.Pits.Translator/Services/", "iDirId"),
                org.apache.axis.description.ParameterDesc.IN,
                new javax.xml.namespace.QName(
                        "http://www.w3.org/2001/XMLSchema", "int"), int.class,
                false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(
                new javax.xml.namespace.QName(
                        "http://Promt.Pits.Translator/Services/", "strTplId"),
                org.apache.axis.description.ParameterDesc.IN,
                new javax.xml.namespace.QName(
                        "http://www.w3.org/2001/XMLSchema", "string"),
                java.lang.String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(
                new javax.xml.namespace.QName(
                        "http://Promt.Pits.Translator/Services/",
                        "strSourceText"),
                org.apache.axis.description.ParameterDesc.IN,
                new javax.xml.namespace.QName(
                        "http://www.w3.org/2001/XMLSchema", "string"),
                java.lang.String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(
                new javax.xml.namespace.QName(
                        "http://Promt.Pits.Translator/Services/", "strFileType"),
                org.apache.axis.description.ParameterDesc.IN,
                new javax.xml.namespace.QName(
                        "http://www.w3.org/2001/XMLSchema", "string"),
                java.lang.String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(
                new javax.xml.namespace.QName(
                        "http://Promt.Pits.Translator/Services/",
                        "TranslateFormattedText2Result"),
                org.apache.axis.description.ParameterDesc.OUT,
                new javax.xml.namespace.QName(
                        "http://www.w3.org/2001/XMLSchema", "string"),
                java.lang.String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(
                new javax.xml.namespace.QName(
                        "http://Promt.Pits.Translator/Services/", "strError"),
                org.apache.axis.description.ParameterDesc.OUT,
                new javax.xml.namespace.QName(
                        "http://www.w3.org/2001/XMLSchema", "string"),
                java.lang.String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        oper.setReturnType(org.apache.axis.encoding.XMLType.AXIS_VOID);
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[1] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("Directions");
        oper.setReturnType(new javax.xml.namespace.QName(
                "http://Promt.Pits.Translator/Services/", "ArrayOfAnyType"));
        oper.setReturnClass(java.lang.Object[].class);
        oper.setReturnQName(new javax.xml.namespace.QName(
                "http://Promt.Pits.Translator/Services/", "DirectionsResult"));
        param = oper.getReturnParamDesc();
        param.setItemQName(new javax.xml.namespace.QName(
                "http://Promt.Pits.Translator/Services/", "anyType"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[2] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("Templates");
        param = new org.apache.axis.description.ParameterDesc(
                new javax.xml.namespace.QName(
                        "http://Promt.Pits.Translator/Services/", "iDirID"),
                org.apache.axis.description.ParameterDesc.IN,
                new javax.xml.namespace.QName(
                        "http://www.w3.org/2001/XMLSchema", "int"), int.class,
                false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName(
                "http://Promt.Pits.Translator/Services/", "ArrayOfAnyType"));
        oper.setReturnClass(java.lang.Object[].class);
        oper.setReturnQName(new javax.xml.namespace.QName(
                "http://Promt.Pits.Translator/Services/", "TemplatesResult"));
        param = oper.getReturnParamDesc();
        param.setItemQName(new javax.xml.namespace.QName(
                "http://Promt.Pits.Translator/Services/", "anyType"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[3] = oper;

    }

    public PTSXLIFFTranslatorSoapStub() throws org.apache.axis.AxisFault
    {
        this(null);
    }

    public PTSXLIFFTranslatorSoapStub(java.net.URL endpointURL,
            javax.xml.rpc.Service service) throws org.apache.axis.AxisFault
    {
        this(service);
        super.cachedEndpoint = endpointURL;
    }

    public PTSXLIFFTranslatorSoapStub(javax.xml.rpc.Service service)
            throws org.apache.axis.AxisFault
    {
        if (service == null)
        {
            super.service = new org.apache.axis.client.Service();
        }
        else
        {
            super.service = service;
        }
        ((org.apache.axis.client.Service) super.service)
                .setTypeMappingVersion("1.2");
        java.lang.Class cls;
        javax.xml.namespace.QName qName;
        javax.xml.namespace.QName qName2;
        java.lang.Class beansf = org.apache.axis.encoding.ser.BeanSerializerFactory.class;
        java.lang.Class beandf = org.apache.axis.encoding.ser.BeanDeserializerFactory.class;
        java.lang.Class enumsf = org.apache.axis.encoding.ser.EnumSerializerFactory.class;
        java.lang.Class enumdf = org.apache.axis.encoding.ser.EnumDeserializerFactory.class;
        java.lang.Class arraysf = org.apache.axis.encoding.ser.ArraySerializerFactory.class;
        java.lang.Class arraydf = org.apache.axis.encoding.ser.ArrayDeserializerFactory.class;
        java.lang.Class simplesf = org.apache.axis.encoding.ser.SimpleSerializerFactory.class;
        java.lang.Class simpledf = org.apache.axis.encoding.ser.SimpleDeserializerFactory.class;
        java.lang.Class simplelistsf = org.apache.axis.encoding.ser.SimpleListSerializerFactory.class;
        java.lang.Class simplelistdf = org.apache.axis.encoding.ser.SimpleListDeserializerFactory.class;
        qName = new javax.xml.namespace.QName(
                "http://Promt.Pits.Translator/Services/", ">Directions");
        cachedSerQNames.add(qName);
        cls = Directions.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

        qName = new javax.xml.namespace.QName(
                "http://Promt.Pits.Translator/Services/", ">DirectionsResponse");
        cachedSerQNames.add(qName);
        cls = DirectionsResponse.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

        qName = new javax.xml.namespace.QName(
                "http://Promt.Pits.Translator/Services/", ">Templates");
        cachedSerQNames.add(qName);
        cls = com.globalsight.machineTranslation.promt.pts9.Templates.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

        qName = new javax.xml.namespace.QName(
                "http://Promt.Pits.Translator/Services/", ">TemplatesResponse");
        cachedSerQNames.add(qName);
        cls = com.globalsight.machineTranslation.promt.pts9.TemplatesResponse.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

        qName = new javax.xml.namespace.QName(
                "http://Promt.Pits.Translator/Services/",
                ">TranslateFormattedText");
        cachedSerQNames.add(qName);
        cls = TranslateFormattedText.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

        qName = new javax.xml.namespace.QName(
                "http://Promt.Pits.Translator/Services/",
                ">TranslateFormattedText2");
        cachedSerQNames.add(qName);
        cls = TranslateFormattedText2.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

        qName = new javax.xml.namespace.QName(
                "http://Promt.Pits.Translator/Services/",
                ">TranslateFormattedText2Response");
        cachedSerQNames.add(qName);
        cls = TranslateFormattedText2Response.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

        qName = new javax.xml.namespace.QName(
                "http://Promt.Pits.Translator/Services/",
                ">TranslateFormattedTextResponse");
        cachedSerQNames.add(qName);
        cls = TranslateFormattedTextResponse.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

        qName = new javax.xml.namespace.QName(
                "http://Promt.Pits.Translator/Services/", "ArrayOfAnyType");
        cachedSerQNames.add(qName);
        cls = java.lang.Object[].class;
        cachedSerClasses.add(cls);
        qName = new javax.xml.namespace.QName(
                "http://www.w3.org/2001/XMLSchema", "anyType");
        qName2 = new javax.xml.namespace.QName(
                "http://Promt.Pits.Translator/Services/", "anyType");
        cachedSerFactories
                .add(new org.apache.axis.encoding.ser.ArraySerializerFactory(
                        qName, qName2));
        cachedDeserFactories
                .add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

        qName = new javax.xml.namespace.QName(
                "http://Promt.Pits.Translator/Services/", "DictionaryEntry");
        cachedSerQNames.add(qName);
        cls = DictionaryEntry.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

    }

    protected org.apache.axis.client.Call createCall()
            throws java.rmi.RemoteException
    {
        try
        {
            org.apache.axis.client.Call _call = super._createCall();
            if (super.maintainSessionSet)
            {
                _call.setMaintainSession(super.maintainSession);
            }
            if (super.cachedUsername != null)
            {
                _call.setUsername(super.cachedUsername);
            }
            if (super.cachedPassword != null)
            {
                _call.setPassword(super.cachedPassword);
            }
            if (super.cachedEndpoint != null)
            {
                _call.setTargetEndpointAddress(super.cachedEndpoint);
            }
            if (super.cachedTimeout != null)
            {
                _call.setTimeout(super.cachedTimeout);
            }
            if (super.cachedPortName != null)
            {
                _call.setPortName(super.cachedPortName);
            }
            java.util.Enumeration keys = super.cachedProperties.keys();
            while (keys.hasMoreElements())
            {
                java.lang.String key = (java.lang.String) keys.nextElement();
                _call.setProperty(key, super.cachedProperties.get(key));
            }
            // All the type mapping information is registered
            // when the first call is made.
            // The type mapping information is actually registered in
            // the TypeMappingRegistry of the service, which
            // is the reason why registration is only needed for the first call.
            synchronized (this)
            {
                if (firstCall())
                {
                    // must set encoding style before registering serializers
                    _call.setEncodingStyle(null);
                    for (int i = 0; i < cachedSerFactories.size(); ++i)
                    {
                        java.lang.Class cls = (java.lang.Class) cachedSerClasses
                                .get(i);
                        javax.xml.namespace.QName qName = (javax.xml.namespace.QName) cachedSerQNames
                                .get(i);
                        java.lang.Object x = cachedSerFactories.get(i);
                        if (x instanceof Class)
                        {
                            java.lang.Class sf = (java.lang.Class) cachedSerFactories
                                    .get(i);
                            java.lang.Class df = (java.lang.Class) cachedDeserFactories
                                    .get(i);
                            _call
                                    .registerTypeMapping(cls, qName, sf, df,
                                            false);
                        }
                        else if (x instanceof javax.xml.rpc.encoding.SerializerFactory)
                        {
                            org.apache.axis.encoding.SerializerFactory sf = (org.apache.axis.encoding.SerializerFactory) cachedSerFactories
                                    .get(i);
                            org.apache.axis.encoding.DeserializerFactory df = (org.apache.axis.encoding.DeserializerFactory) cachedDeserFactories
                                    .get(i);
                            _call
                                    .registerTypeMapping(cls, qName, sf, df,
                                            false);
                        }
                    }
                }
            }
            return _call;
        }
        catch (java.lang.Throwable _t)
        {
            throw new org.apache.axis.AxisFault(
                    "Failure trying to get the Call object", _t);
        }
    }

    public java.lang.String translateFormattedText(int dirId,
            java.lang.String tplId, java.lang.String strText,
            java.lang.String fileType) throws java.rmi.RemoteException
    {
        if (super.cachedEndpoint == null)
        {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[0]);
        _call.setUseSOAPAction(true);
        _call
                .setSOAPActionURI("http://Promt.Pits.Translator/Services/TranslateFormattedText");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR,
                Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS,
                Boolean.FALSE);
        _call
                .setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName(
                "http://Promt.Pits.Translator/Services/",
                "TranslateFormattedText"));

        setRequestHeaders(_call);
        setAttachments(_call);
        try
        {
            java.lang.Object _resp = _call.invoke(new java.lang.Object[]
            { new java.lang.Integer(dirId), tplId, strText, fileType });

            if (_resp instanceof java.rmi.RemoteException)
            {
                throw (java.rmi.RemoteException) _resp;
            }
            else
            {
                extractAttachments(_call);
                try
                {
                    return (java.lang.String) _resp;
                }
                catch (java.lang.Exception _exception)
                {
                    return (java.lang.String) org.apache.axis.utils.JavaUtils
                            .convert(_resp, java.lang.String.class);
                }
            }
        }
        catch (org.apache.axis.AxisFault axisFaultException)
        {
            throw axisFaultException;
        }
    }

    public void translateFormattedText2(int iDirId, java.lang.String strTplId,
            java.lang.String strSourceText, java.lang.String strFileType,
            javax.xml.rpc.holders.StringHolder translateFormattedText2Result,
            javax.xml.rpc.holders.StringHolder strError)
            throws java.rmi.RemoteException
    {
        if (super.cachedEndpoint == null)
        {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[1]);
        _call.setUseSOAPAction(true);
        _call
                .setSOAPActionURI("http://Promt.Pits.Translator/Services/TranslateFormattedText2");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR,
                Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS,
                Boolean.FALSE);
        _call
                .setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName(
                "http://Promt.Pits.Translator/Services/",
                "TranslateFormattedText2"));

        setRequestHeaders(_call);
        setAttachments(_call);
        try
        {
            java.lang.Object _resp = _call.invoke(new java.lang.Object[]
            { new java.lang.Integer(iDirId), strTplId, strSourceText,
                    strFileType });

            if (_resp instanceof java.rmi.RemoteException)
            {
                throw (java.rmi.RemoteException) _resp;
            }
            else
            {
                extractAttachments(_call);
                java.util.Map _output;
                _output = _call.getOutputParams();
                try
                {
                    translateFormattedText2Result.value = (java.lang.String) _output
                            .get(new javax.xml.namespace.QName(
                                    "http://Promt.Pits.Translator/Services/",
                                    "TranslateFormattedText2Result"));
                }
                catch (java.lang.Exception _exception)
                {
                    translateFormattedText2Result.value = (java.lang.String) org.apache.axis.utils.JavaUtils
                            .convert(_output.get(new javax.xml.namespace.QName(
                                    "http://Promt.Pits.Translator/Services/",
                                    "TranslateFormattedText2Result")),
                                    java.lang.String.class);
                }
                try
                {
                    strError.value = (java.lang.String) _output
                            .get(new javax.xml.namespace.QName(
                                    "http://Promt.Pits.Translator/Services/",
                                    "strError"));
                }
                catch (java.lang.Exception _exception)
                {
                    strError.value = (java.lang.String) org.apache.axis.utils.JavaUtils
                            .convert(_output.get(new javax.xml.namespace.QName(
                                    "http://Promt.Pits.Translator/Services/",
                                    "strError")), java.lang.String.class);
                }
            }
        }
        catch (org.apache.axis.AxisFault axisFaultException)
        {
            throw axisFaultException;
        }
    }

    public java.lang.Object[] directions() throws java.rmi.RemoteException
    {
        if (super.cachedEndpoint == null)
        {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[2]);
        _call.setUseSOAPAction(true);
        _call
                .setSOAPActionURI("http://Promt.Pits.Translator/Services/Directions");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR,
                Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS,
                Boolean.FALSE);
        _call
                .setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName(
                "http://Promt.Pits.Translator/Services/", "Directions"));

        setRequestHeaders(_call);
        setAttachments(_call);
        try
        {
            java.lang.Object _resp = _call.invoke(new java.lang.Object[]
            {});

            if (_resp instanceof java.rmi.RemoteException)
            {
                throw (java.rmi.RemoteException) _resp;
            }
            else
            {
                extractAttachments(_call);
                try
                {
                    return (java.lang.Object[]) _resp;
                }
                catch (java.lang.Exception _exception)
                {
                    return (java.lang.Object[]) org.apache.axis.utils.JavaUtils
                            .convert(_resp, java.lang.Object[].class);
                }
            }
        }
        catch (org.apache.axis.AxisFault axisFaultException)
        {
            throw axisFaultException;
        }
    }

    public java.lang.Object[] templates(int iDirID)
            throws java.rmi.RemoteException
    {
        if (super.cachedEndpoint == null)
        {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[3]);
        _call.setUseSOAPAction(true);
        _call
                .setSOAPActionURI("http://Promt.Pits.Translator/Services/Templates");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR,
                Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS,
                Boolean.FALSE);
        _call
                .setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName(
                "http://Promt.Pits.Translator/Services/", "Templates"));

        setRequestHeaders(_call);
        setAttachments(_call);
        try
        {
            java.lang.Object _resp = _call.invoke(new java.lang.Object[]
            { new java.lang.Integer(iDirID) });

            if (_resp instanceof java.rmi.RemoteException)
            {
                throw (java.rmi.RemoteException) _resp;
            }
            else
            {
                extractAttachments(_call);
                try
                {
                    return (java.lang.Object[]) _resp;
                }
                catch (java.lang.Exception _exception)
                {
                    return (java.lang.Object[]) org.apache.axis.utils.JavaUtils
                            .convert(_resp, java.lang.Object[].class);
                }
            }
        }
        catch (org.apache.axis.AxisFault axisFaultException)
        {
            throw axisFaultException;
        }
    }

}
