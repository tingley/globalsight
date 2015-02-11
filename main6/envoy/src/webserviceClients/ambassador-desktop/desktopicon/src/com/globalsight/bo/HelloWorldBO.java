package com.globalsight.bo;

import java.rmi.RemoteException;

import com.globalsight.util.WebClientHelper;
import com.globalsight.webservices.WebServiceException;
import com.globalsight.www.webservices.Ambassador;

public class HelloWorldBO {

    public String helloWord() throws Exception
    {
        String hello;
            Ambassador abmassador = WebClientHelper.getAmbassador();
            hello = abmassador.helloWorld();
        return hello;
    }
}
