package com.cbg.test;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class test1 {
    @Test
    public void test2() {
        // 已知的知识来解决需求
        List<String> list1 = new ArrayList<>();
        list1.add("张老三");
        list1.add("张小三");
        list1.add("李四");
        list1.add("赵五");
        list1.add("张六");
        list1.add("王八");
        ArrayList<String> list2 = new ArrayList<>();
        ArrayList<String> list3 = new ArrayList<>();
        for (String name : list1) {
            if (name.startsWith("张")) {
                list2.add(name);
                if (name.length() == 3)
                    list3.add(name);
            }
        }

        System.out.println("list2:" + list2);
        System.out.println("list3:" + list3);
        System.out.println("集合中的元素个数是：" + list1.stream().count());

        //过滤其中姓张且三个字的 等同于
        list1.stream().filter((String name) -> {
            return name.startsWith("张");
        }).filter((String name) -> {
            return name.length() == 3;
        }).forEach((String name) -> {
            System.out.printf(name + " ");
        });

    }
}
