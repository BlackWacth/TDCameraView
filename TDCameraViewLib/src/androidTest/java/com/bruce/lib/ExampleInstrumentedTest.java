package com.bruce.lib;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.ArrayMap;

import com.bruce.lib.utils.L;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Iterator;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        L.i("hzw", "##  ArrayMap  ##");
        ArrayMap<String, String> ht = new ArrayMap<String, String>();
        ht.put("5", "OOO");
        ht.put("3", "OOO");
        ht.put("2", "OOO");
        ht.put("1", "OOO");
        ht.put("4", "OOO");

        Iterator<String> it = ht.keySet().iterator();
        while (it.hasNext()) {
            L.i("hzw", it.next());
        }
    }
}
