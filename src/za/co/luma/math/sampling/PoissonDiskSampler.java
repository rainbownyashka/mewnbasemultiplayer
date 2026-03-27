/*
 * Decompiled with CFR 0.152.
 */
package za.co.luma.math.sampling;

import java.util.LinkedList;
import java.util.List;
import za.co.iocom.math.MathUtil;
import za.co.luma.geom.Vector2DDouble;
import za.co.luma.geom.Vector2DInt;
import za.co.luma.math.function.RealFunction2DDouble;
import za.co.luma.math.sampling.Sampler;

public class PoissonDiskSampler
implements Sampler<Vector2DDouble> {
    private static final int DEFAULT_POINTS_TO_GENERATE = 30;
    private final int pointsToGenerate;
    private final Vector2DDouble p0;
    private final Vector2DDouble p1;
    private final Vector2DDouble dimensions;
    private final double cellSize;
    private final double minDist;
    private final int gridWidth;
    private final int gridHeight;
    public static final int MAX_POINTS = 100000;
    private RealFunction2DDouble distribution;

    public PoissonDiskSampler(double x0, double y0, double x1, double y1, double minDist, RealFunction2DDouble distribution, int pointsToGenerate) {
        this.p0 = new Vector2DDouble(x0, y0);
        this.p1 = new Vector2DDouble(x1, y1);
        this.dimensions = new Vector2DDouble(x1 - x0, y1 - y0);
        this.minDist = minDist;
        this.distribution = distribution;
        this.pointsToGenerate = pointsToGenerate;
        this.cellSize = minDist / Math.sqrt(2.0);
        this.gridWidth = (int)(this.dimensions.x / this.cellSize) + 1;
        this.gridHeight = (int)(this.dimensions.y / this.cellSize) + 1;
    }

    public PoissonDiskSampler(double x0, double y0, double x1, double y1, double minDist, RealFunction2DDouble distribution) {
        this(x0, y0, x1, y1, minDist, distribution, 30);
    }

    @Override
    public List<Vector2DDouble> sample() {
        LinkedList<Vector2DDouble> activeList = new LinkedList<Vector2DDouble>();
        LinkedList<Vector2DDouble> pointList = new LinkedList<Vector2DDouble>();
        List[][] grid = new List[this.gridWidth][this.gridHeight];
        for (int i = 0; i < this.gridWidth; ++i) {
            for (int j = 0; j < this.gridHeight; ++j) {
                grid[i][j] = new LinkedList();
            }
        }
        this.addFirstPoint(grid, activeList, pointList);
        while (!activeList.isEmpty() && pointList.size() < 100000) {
            int listIndex = MathUtil.random.nextInt(activeList.size());
            Vector2DDouble point = (Vector2DDouble)activeList.get(listIndex);
            boolean found = false;
            for (int k = 0; k < this.pointsToGenerate; ++k) {
                found |= this.addNextPoint(grid, activeList, pointList, point);
            }
            if (found) continue;
            activeList.remove(listIndex);
        }
        return pointList;
    }

    private boolean addNextPoint(List<Vector2DDouble>[][] grid, List<Vector2DDouble> activeList, List<Vector2DDouble> pointList, Vector2DDouble point) {
        boolean found = false;
        double fraction = this.distribution.getDouble((int)point.x, (int)point.y);
        Vector2DDouble q = PoissonDiskSampler.generateRandomAround(point, fraction * this.minDist);
        if (q.x >= this.p0.x && q.x < this.p1.x && q.y > this.p0.y && q.y < this.p1.y) {
            Vector2DInt qIndex = PoissonDiskSampler.pointDoubleToInt(q, this.p0, this.cellSize);
            boolean tooClose = false;
            for (int i = Math.max(0, qIndex.x - 2); i < Math.min(this.gridWidth, qIndex.x + 3) && !tooClose; ++i) {
                for (int j = Math.max(0, qIndex.y - 2); j < Math.min(this.gridHeight, qIndex.y + 3) && !tooClose; ++j) {
                    for (Vector2DDouble gridPoint : grid[i][j]) {
                        if (!(Vector2DDouble.distance(gridPoint, q) < this.minDist * fraction)) continue;
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

    private void addFirstPoint(List<Vector2DDouble>[][] grid, List<Vector2DDouble> activeList, List<Vector2DDouble> pointList) {
        double d = MathUtil.random.nextDouble();
        double xr = this.p0.x + this.dimensions.x * d;
        d = MathUtil.random.nextDouble();
        double yr = this.p0.y + this.dimensions.y * d;
        Vector2DDouble p = new Vector2DDouble(xr, yr);
        Vector2DInt index = PoissonDiskSampler.pointDoubleToInt(p, this.p0, this.cellSize);
        grid[index.x][index.y].add(p);
        activeList.add(p);
        pointList.add(p);
    }

    static Vector2DInt pointDoubleToInt(Vector2DDouble pointDouble, Vector2DDouble origin, double cellSize) {
        return new Vector2DInt((int)((pointDouble.x - origin.x) / cellSize), (int)((pointDouble.y - origin.y) / cellSize));
    }

    static Vector2DDouble generateRandomAround(Vector2DDouble centre, double minDist) {
        double d = MathUtil.random.nextDouble();
        double radius = minDist + minDist * d;
        d = MathUtil.random.nextDouble();
        double angle = Math.PI * 2 * d;
        double newX = radius * Math.sin(angle);
        double newY = radius * Math.cos(angle);
        Vector2DDouble randomPoint = new Vector2DDouble(centre.x + newX, centre.y + newY);
        return randomPoint;
    }
}

