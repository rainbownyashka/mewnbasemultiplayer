/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.PerformanceCounter;
import com.cairn4.moonbase.tests.DummyObject;
import com.cairn4.moonbase.tests.GdxTest;
import java.util.ArrayList;

public class ArrayTest
extends GdxTest {
    PerformanceCounter performanceCounter1;
    PerformanceCounter performanceCounter2;
    ArrayList<DummyObject> list1 = new ArrayList();
    Array<DummyObject> list2 = new Array();

    public ArrayTest() {
        this.performanceCounter1 = new PerformanceCounter("List2");
        this.performanceCounter2 = new PerformanceCounter("List2");
        this.list1.clear();
        this.list2.clear();
        for (int i = 0; i < 100000; ++i) {
            DummyObject d = new DummyObject();
            this.list1.add(d);
            this.list2.add(d);
        }
    }

    @Override
    public void create() {
        System.out.println("start mem: " + Gdx.app.getJavaHeap() / 100000L);
        for (int i = 0; i < 50000; ++i) {
            this.doTest();
        }
        System.out.println("end mem: " + Gdx.app.getJavaHeap() / 100000L);
    }

    private void doTest() {
        this.testArrayList();
    }

    public void testArrayList() {
        this.performanceCounter1.start();
        for (DummyObject d : this.list1) {
            ++d.value;
        }
        this.performanceCounter1.stop();
        this.performanceCounter1.tick();
    }

    public void testArray() {
        this.performanceCounter2.start();
        int size = this.list2.size;
        for (int i = 0; i < size; ++i) {
            this.list2.get((int)i).value = 1;
        }
        this.performanceCounter2.stop();
        this.performanceCounter2.tick();
    }

    @Override
    public void render() {
        if (Gdx.input.justTouched()) {
            System.out.println("---");
            this.doTest();
        }
    }
}

