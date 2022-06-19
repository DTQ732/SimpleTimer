package com.huidong.timer;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

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

        TestEq eq1 = new TestEq();
        eq1.a = 1;
        TestEq eq2 = new TestEq();
        eq2.a = 2;
        TestEq eq3 = eq1;
        eq1 = eq2;
        eq1 = eq2;
    }
}