/*
 * Decompiled with CFR 0.152.
 */
package za.co.luma.math.sampling;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import za.co.iocom.math.MathUtil;
import za.co.luma.geom.Vector2DDouble;
import za.co.luma.geom.Vector2DInt;
import za.co.luma.math.function.RealFunction2DDouble;

public class PoissonDiskMultiSampler {
    private static final int DEFAULT_POINTS_TO_GENERATE = 30;
    private final int pointsToGenerate;
    private final Vector2DDouble p0;
    private final Vector2DDouble p1;
    private final Vector2DDouble dimensions;
    private final double[] cellSize;
    private final double[] minDist;
    private final double[] radii;
    private final double[] minRadii;
    private final int[] gridWidth;
    private final int[] gridHeight;
    private int layerCount;
    private boolean multiLayer;
    public static final int MAX_POINTS = 100000;
    private RealFunction2DDouble distribution;
    public List<List<Circle>[][]> grids;

    public PoissonDiskMultiSampler(double x0, double y0, double x1, double y1, double[] minDist, double[] minRadii, double[] radii, RealFunction2DDouble distribution, boolean multiLayer, int pointsToGenerate) {
        this.layerCount = minDist.length;
        if (minRadii.length != this.layerCount) {
            throw new RuntimeException("minRadii[] must have the same length as minDist[]");
        }
        if (radii.length != this.layerCount) {
            throw new RuntimeException("radii[] must have the same length as minDist[]");
        }
        this.p0 = new Vector2DDouble(x0, y0);
        this.p1 = new Vector2DDouble(x1, y1);
        this.dimensions = new Vector2DDouble(x1 - x0, y1 - y0);
        this.minDist = minDist;
        this.radii = radii;
        this.minRadii = minRadii;
        this.distribution = distribution;
        this.pointsToGenerate = pointsToGenerate;
        this.multiLayer = multiLayer;
        this.cellSize = new double[this.layerCount];
        this.gridWidth = new int[this.layerCount];
        this.gridHeight = new int[this.layerCount];
        for (int k = 0; k < this.layerCount; ++k) {
            this.cellSize[k] = minDist[k] / Math.sqrt(2.0);
            this.gridWidth[k] = (int)(this.dimensions.x / this.cellSize[k]) + 1;
            this.gridHeight[k] = (int)(this.dimensions.y / this.cellSize[k]) + 1;
        }
    }

    public PoissonDiskMultiSampler(double x0, double y0, double x1, double y1, double[] minDist, double[] minRadii, double[] radii, RealFunction2DDouble distribution, boolean multiLayer) {
        this(x0, y0, x1, y1, minDist, radii, minRadii, distribution, multiLayer, 30);
    }

    public List<Circle>[] sample() {
        int k;
        List[] pointList = new List[this.layerCount];
        this.grids = new LinkedList<List<Circle>[][]>();
        for (k = 0; k < this.layerCount; ++k) {
            LinkedList<Circle> activeList = new LinkedList<Circle>();
            List[][] grid = new List[this.gridWidth[k]][this.gridHeight[k]];
            this.grids.add(grid);
            pointList[k] = new LinkedList();
            for (int i = 0; i < this.gridWidth[k]; ++i) {
                for (int j = 0; j < this.gridHeight[k]; ++j) {
                    grid[i][j] = new LinkedList();
                }
            }
            this.addFirstPoint(grid, activeList, pointList[k], k);
            while (!activeList.isEmpty() && pointList[k].size() < 100000) {
                int listIndex = MathUtil.random.nextInt(activeList.size());
                Circle point = (Circle)activeList.get(listIndex);
                boolean found = false;
                for (int m = 0; m < this.pointsToGenerate; ++m) {
                    found |= this.addNextPoint(grid, activeList, pointList[k], point, k);
                }
                if (found) continue;
                activeList.remove(listIndex);
            }
        }
        if (this.multiLayer) {
            for (k = 1; k < this.layerCount; ++k) {
                ListIterator pointItr = pointList[k].listIterator();
                while (pointItr.hasNext()) {
                    Circle point = (Circle)pointItr.next();
                    if (!this.checkPoint(point, k, this.grids)) continue;
                    point.x *= -1.0;
                }
            }
        }
        return pointList;
    }

    private boolean checkPoint(Circle q, int layerIndex, List<List<Circle>[][]> grids) {
        int k = 0;
        double fraction = 1.0;
        boolean tooClose = false;
        for (k = layerIndex - 1; k >= 0 && !tooClose; --k) {
            List<Circle>[][] grid = grids.get(k);
            Vector2DInt qIndex = PoissonDiskMultiSampler.pointDoubleToInt(q, this.p0, this.cellSize[k]);
            for (int i = Math.max(0, qIndex.x - 2); i < Math.min(this.gridWidth[k], qIndex.x + 3) && !tooClose; ++i) {
                for (int j = Math.max(0, qIndex.y - 2); j < Math.min(this.gridHeight[k], qIndex.y + 3) && !tooClose; ++j) {
                    for (Circle gridPoint : grid[i][j]) {
                        if (!(Vector2DDouble.distance(gridPoint, q) < (q.getRadius() + gridPoint.getRadius()) * fraction)) continue;
                        tooClose = true;
                    }
                }
            }
        }
        return tooClose;
    }

    private boolean addNextPoint(List<Circle>[][] grid, List<Circle> activeList, List<Circle> pointList, Circle point, int layerIndex) {
        boolean found = false;
        double fraction = 1.0;
        Circle q = PoissonDiskMultiSampler.generateAround(point, fraction * this.minDist[layerIndex], this.minRadii[layerIndex], this.radii[layerIndex], MathUtil.random.nextDouble(), MathUtil.random.nextDouble(), MathUtil.random.nextDouble());
        if (q.x >= this.p0.x && q.x < this.p1.x && q.y > this.p0.y && q.y < this.p1.y) {
            Vector2DInt qIndex = PoissonDiskMultiSampler.pointDoubleToInt(q, this.p0, this.cellSize[layerIndex]);
            boolean tooClose = false;
            for (int i = Math.max(0, qIndex.x - 2); i < Math.min(this.gridWidth[layerIndex], qIndex.x + 3) && !tooClose; ++i) {
                for (int j = Math.max(0, qIndex.y - 2); j < Math.min(this.gridHeight[layerIndex], qIndex.y + 3) && !tooClose; ++j) {
                    for (Circle gridPoint : grid[i][j]) {
                        double distance = Circle.distance(gridPoint, q);
                        if (!(distance < this.minDist[layerIndex] * fraction)) continue;
                        tooClose = true;
                    }
                }
            }
            if (!tooClose) {
                found = true;
                activeList.add(q);
                pointList.add(q);
                grid[qIndex.x][qIndex.y].add(q);
            }
        }
        return found;
    }

    private void addFirstPoint(List<Circle>[][] grid, List<Circle> activeList, List<Circle> pointList, int layerIndex) {
        double d = MathUtil.random.nextDouble();
        double xr = this.p0.x + this.dimensions.x * d;
        d = MathUtil.random.nextDouble();
        double yr = this.p0.y + this.dimensions.y * d;
        d = MathUtil.random.nextDouble();
        double rr = this.minRadii[layerIndex] + d * (this.radii[layerIndex] - this.minRadii[layerIndex]);
        Circle p = new Circle(xr, yr, rr);
        Vector2DInt index = PoissonDiskMultiSampler.pointDoubleToInt(p, this.p0, this.cellSize[layerIndex]);
        grid[index.x][index.y].add(p);
        activeList.add(p);
        pointList.add(p);
    }

    public static Vector2DInt pointDoubleToInt(Vector2DDouble pointDouble, Vector2DDouble origin, double cellSize) {
        return new Vector2DInt((int)((pointDouble.x - origin.x) / cellSize), (int)((pointDouble.y - origin.y) / cellSize));
    }

    public static Circle generateAround(Vector2DDouble centre, double minDist, double minRadius, double radius, double distanceScale, double angleScale, double radiusScale) {
        double r = minDist + minDist * distanceScale;
        double angle = Math.PI * 2 * angleScale;
        double newX = r * Math.cos(angle);
        double newY = r * Math.sin(angle);
        double newRadius = minRadius + radiusScale * (radius - minRadius);
        Circle randomPoint = new Circle(centre.x + newX, centre.y + newY, newRadius);
        return randomPoint;
    }

    public static class Circle
    extends Vector2DDouble {
        double radius;

        public Circle(double x, double y, double radius) {
            super(x, y);
            this.radius = radius;
        }

        public double getRadius() {
            return this.radius;
        }
    }
}

