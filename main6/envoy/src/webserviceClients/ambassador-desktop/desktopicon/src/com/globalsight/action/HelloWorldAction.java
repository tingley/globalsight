package com.globalsight.action;

import com.globalsight.bo.HelloWorldBO;

public class HelloWorldAction extends Action{

    public String execute(String args[]) throws Exception
    {
        HelloWorldBO helloWorldBO = new HelloWorldBO();
        return helloWorldBO.helloWord();
    }
}
