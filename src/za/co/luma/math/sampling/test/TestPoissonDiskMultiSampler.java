/*
 * Decompiled with CFR 0.152.
 */
package za.co.luma.math.sampling.test;

import java.util.List;
import junit.framework.TestCase;
import za.co.iocom.math.MathUtil;
import za.co.luma.geom.Vector2DDouble;
import za.co.luma.geom.Vector2DInt;
import za.co.luma.math.sampling.PoissonDiskMultiSampler;

public class TestPoissonDiskMultiSampler
extends TestCase {
    double DELTA = 1.0E-4;

    private static void pr(String message) {
        System.out.println(message);
    }

    public void testSample() {
        MathUtil.random.setSeed(0L);
        double[] minDist = new double[]{30.0};
        double[] minRadii = new double[]{15.0};
        double[] maxRadii = new double[]{15.0};
        double[] cellSize = new double[]{minDist[0] / Math.sqrt(2.0)};
        double[] gridWidth = new double[]{(int)(1024.0 / cellSize[0]) + 1};
        double[] gridHeight = new double[]{(int)(1024.0 / cellSize[0]) + 1};
        PoissonDiskMultiSampler sampler = new PoissonDiskMultiSampler(0.0, 0.0, 1024.0, 1024.0, minDist, minRadii, maxRadii, null, false);
        List<PoissonDiskMultiSampler.Circle>[] points = sampler.sample();
        TestPoissonDiskMultiSampler.assertEquals(points.length, 1);
        Vector2DDouble origin = new Vector2DDouble(0.0, 0.0);
        List<PoissonDiskMultiSampler.Circle>[][] grid = sampler.grids.get(0);
        for (PoissonDiskMultiSampler.Circle point : points[0]) {
            int k = 0;
            double fraction = 1.0;
            boolean tooClose = false;
            Vector2DInt qIndex = PoissonDiskMultiSampler.pointDoubleToInt(point, origin, cellSize[k]);
            int i = Math.max(0, qIndex.x - 2);
            while ((double)i < Math.min(gridWidth[k], (double)(qIndex.x + 3)) && !tooClose) {
                int j = Math.max(0, qIndex.y - 2);
                while ((double)j < Math.min(gridHeight[k], (double)(qIndex.y + 3)) && !tooClose) {
                    for (PoissonDiskMultiSampler.Circle gridPoint : grid[i][j]) {
                        double space;
                        double distance = Vector2DDouble.distance(gridPoint, point);
                        if (distance < minDist[k] * fraction) {
                            TestPoissonDiskMultiSampler.assertEquals("px " + point.x + " py " + point.y + " gpx " + gridPoint.x + " gpy " + gridPoint.y, point.x, gridPoint.x, this.DELTA);
                            TestPoissonDiskMultiSampler.assertEquals(point.y, gridPoint.y, this.DELTA);
                            TestPoissonDiskMultiSampler.assertEquals(point.getRadius(), gridPoint.getRadius(), this.DELTA);
                        }
                        if (!((space = distance - point.getRadius() - gridPoint.getRadius()) < 0.0) || !(Math.abs(point.x - gridPoint.x) > this.DELTA) && !(Math.abs(point.y - gridPoint.y) > this.DELTA)) continue;
                        TestPoissonDiskMultiSampler.pr("space: " + space + " r");
                        TestPoissonDiskMultiSampler.fail();
                    }
                    ++j;
                }
                ++i;
            }
        }
    }

    public void testGenerateRandomAround() {
        Vector2DDouble centre = new Vector2DDouble(10.0, 20.0);
        double minDist = 20.0;
        double minRadius = 5.0;
        double maxRadius = 7.0;
        double distanceScale = 0.0;
        double angleScale = 0.0;
        double radiusScale = 0.0;
        double expectedDistance = minDist;
        double expectedRadius = minRadius;
        double expectedX = centre.x + minDist;
        double expectedY = centre.y;
        this.checkCaseGenerateAround(centre, minDist, minRadius, maxRadius, distanceScale, angleScale, radiusScale, expectedDistance, expectedRadius, expectedX, expectedY);
        distanceScale = 1.0;
        expectedDistance = 2.0 * minDist;
        expectedRadius = minRadius;
        expectedX = centre.x + 2.0 * minDist;
        expectedY = centre.y;
        this.checkCaseGenerateAround(centre, minDist, minRadius, maxRadius, distanceScale, angleScale, radiusScale, expectedDistance, expectedRadius, expectedX, expectedY);
        distanceScale = 0.5;
        angleScale = 0.0;
        radiusScale = 0.0;
        expectedDistance = 1.5 * minDist;
        expectedRadius = minRadius;
        expectedX = centre.x + 1.5 * minDist;
        expectedY = centre.y;
        this.checkCaseGenerateAround(centre, minDist, minRadius, maxRadius, distanceScale, angleScale, radiusScale, expectedDistance, expectedRadius, expectedX, expectedY);
        distanceScale = 0.0;
        angleScale = 0.0;
        radiusScale = 1.0;
        expectedDistance = minDist;
        expectedRadius = maxRadius;
        expectedX = centre.x + minDist;
        expectedY = centre.y;
        this.checkCaseGenerateAround(centre, minDist, minRadius, maxRadius, distanceScale, angleScale, radiusScale, expectedDistance, expectedRadius, expectedX, expectedY);
        distanceScale = 0.0;
        angleScale = 0.0;
        radiusScale = 0.5;
        expectedDistance = minDist;
        expectedRadius = minRadius + 0.5 * (maxRadius - minRadius);
        expectedX = centre.x + minDist;
        expectedY = centre.y;
        this.checkCaseGenerateAround(centre, minDist, minRadius, maxRadius, distanceScale, angleScale, radiusScale, expectedDistance, expectedRadius, expectedX, expectedY);
        distanceScale = 0.0;
        angleScale = 0.5;
        radiusScale = 0.0;
        expectedDistance = minDist;
        expectedRadius = minRadius;
        expectedX = centre.x - minDist;
        expectedY = centre.y;
        this.checkCaseGenerateAround(centre, minDist, minRadius, maxRadius, distanceScale, angleScale, radiusScale, expectedDistance, expectedRadius, expectedX, expectedY);
        distanceScale = 0.0;
        angleScale = 0.25;
        radiusScale = 0.0;
        expectedDistance = minDist;
        expectedRadius = minRadius;
        expectedX = centre.x;
        expectedY = centre.y + minDist;
        this.checkCaseGenerateAround(centre, minDist, minRadius, maxRadius, distanceScale, angleScale, radiusScale, expectedDistance, expectedRadius, expectedX, expectedY);
        distanceScale = 0.0;
        angleScale = 0.75;
        radiusScale = 0.0;
        expectedDistance = minDist;
        expectedRadius = minRadius;
        expectedX = centre.x;
        expectedY = centre.y - minDist;
        this.checkCaseGenerateAround(centre, minDist, minRadius, maxRadius, distanceScale, angleScale, radiusScale, expectedDistance, expectedRadius, expectedX, expectedY);
        distanceScale = 0.5;
        angleScale = 0.75;
        radiusScale = 0.0;
        expectedDistance = 1.5 * minDist;
        expectedRadius = minRadius;
        expectedX = centre.x;
        expectedY = centre.y - 1.5 * minDist;
        this.checkCaseGenerateAround(centre, minDist, minRadius, maxRadius, distanceScale, angleScale, radiusScale, expectedDistance, expectedRadius, expectedX, expectedY);
    }

    private void checkCaseGenerateAround(Vector2DDouble centre, double minDist, double minRadius, double radius, double distanceScale, double angleScale, double radiusScale, double expectedDistance, double expectedRadius, double expectedX, double expectedY) {
        PoissonDiskMultiSampler.Circle point = PoissonDiskMultiSampler.generateAround(centre, minDist, minRadius, radius, distanceScale, angleScale, radiusScale);
        double distance = point.distance(centre);
        TestPoissonDiskMultiSampler.assertEquals(expectedDistance, distance, this.DELTA);
        TestPoissonDiskMultiSampler.assertEquals(expectedRadius, point.getRadius(), this.DELTA);
        TestPoissonDiskMultiSampler.assertEquals(expectedX, point.x, this.DELTA);
        TestPoissonDiskMultiSampler.assertEquals(expectedY, point.y, this.DELTA);
    }
}

