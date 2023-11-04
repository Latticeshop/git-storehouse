package com.cbg.test;

import org.junit.jupiter.api.Test;

public class UploadFileTest {

    @Test
    public void Test1() {
        String fileName = "hello.jpg";
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        //输出带点
        System.out.println(suffix);
    }
}
