/*
 * Decompiled with CFR 0.152.
 */
package za.co.luma.math.sampling.test;

import junit.framework.Test;
import junit.framework.TestSuite;
import za.co.luma.math.sampling.test.TestPoissonDiskMultiSampler;

public class AllTests {
    public static Test suite() {
        TestSuite suite = new TestSuite("Test for za.co.luma.math.sampling.test");
        suite.addTestSuite(TestPoissonDiskMultiSampler.class);
        return suite;
    }
}

