package com.bruce.lib;

import android.util.ArrayMap;

import org.junit.Test;

import java.util.Iterator;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        System.out.println("##  ArrayMap  ##");
        ArrayMap<String, String> ht = new ArrayMap<>();
        ht.put("1", "OOO");
        ht.put("3", "OOO");
        ht.put("2", "OOO");
        ht.put("5", "OOO");
        ht.put("4", "OOO");

        Iterator<String> it = ht.keySet().iterator();
        while (it.hasNext()) {
            System.out.println(it.next());
        }
    }
}