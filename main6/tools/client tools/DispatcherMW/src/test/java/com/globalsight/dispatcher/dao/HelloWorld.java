package com.globalsight.dispatcher.dao;

import java.util.Date;

public class HelloWorld
{

    public static void main(String[] args)
    {
        String msg = "A,BC,DEF,GH";
        String arr[] = msg.split(",");
        System.out.println(new Date());
        System.out.println(arr);        
    }

}
