/*
 * Decompiled with CFR 0.152.
 */
package com.aurelienribon.bodyeditor;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BodyEditorLoader {
    private final Model model;
    private final List<Vector2> vectorPool = new ArrayList<Vector2>();
    private final PolygonShape polygonShape = new PolygonShape();
    private final CircleShape circleShape = new CircleShape();
    private final Vector2 vec = new Vector2();

    public BodyEditorLoader(FileHandle file) {
        if (file == null) {
            throw new NullPointerException("file is null");
        }
        this.model = this.readJson(file.readString());
    }

    public BodyEditorLoader(String str) {
        if (str == null) {
            throw new NullPointerException("str is null");
        }
        this.model = this.readJson(str);
    }

    public void attachFixture(Body body, String name, FixtureDef fd, float scale) {
        int i;
        RigidBodyModel rbModel = this.model.rigidBodies.get(name);
        if (rbModel == null) {
            throw new RuntimeException("Name '" + name + "' was not found.");
        }
        Vector2 origin = this.vec.set(rbModel.origin).scl(scale);
        int n = rbModel.polygons.size();
        for (i = 0; i < n; ++i) {
            int ii;
            PolygonModel polygon = rbModel.polygons.get(i);
            Vector2[] vertices = polygon.buffer;
            int nn = vertices.length;
            for (ii = 0; ii < nn; ++ii) {
                vertices[ii] = this.newVec().set(polygon.vertices.get(ii)).scl(scale);
                vertices[ii].sub(origin);
            }
            this.polygonShape.set(vertices);
            fd.shape = this.polygonShape;
            body.createFixture(fd);
            nn = vertices.length;
            for (ii = 0; ii < nn; ++ii) {
                this.free(vertices[ii]);
            }
        }
        n = rbModel.circles.size();
        for (i = 0; i < n; ++i) {
            CircleModel circle = rbModel.circles.get(i);
            Vector2 center = this.newVec().set(circle.center).scl(scale);
            float radius = circle.radius * scale;
            this.circleShape.setPosition(center);
            this.circleShape.setRadius(radius);
            fd.shape = this.circleShape;
            body.createFixture(fd);
            this.free(center);
        }
    }

    public String getImagePath(String name) {
        RigidBodyModel rbModel = this.model.rigidBodies.get(name);
        if (rbModel == null) {
            throw new RuntimeException("Name '" + name + "' was not found.");
        }
        return rbModel.imagePath;
    }

    public Vector2 getOrigin(String name, float scale) {
        RigidBodyModel rbModel = this.model.rigidBodies.get(name);
        if (rbModel == null) {
            throw new RuntimeException("Name '" + name + "' was not found.");
        }
        return this.vec.set(rbModel.origin).scl(scale);
    }

    public Model getInternalModel() {
        return this.model;
    }

    private Model readJson(String str) {
        Model m = new Model();
        JsonValue map = new JsonReader().parse(str);
        for (JsonValue bodyElem = map.getChild("rigidBodies"); bodyElem != null; bodyElem = bodyElem.next()) {
            RigidBodyModel rbModel = this.readRigidBody(bodyElem);
            m.rigidBodies.put(rbModel.name, rbModel);
        }
        return m;
    }

    private RigidBodyModel readRigidBody(JsonValue bodyElem) {
        RigidBodyModel rbModel = new RigidBodyModel();
        rbModel.name = bodyElem.getString("name");
        rbModel.imagePath = bodyElem.getString("imagePath");
        JsonValue originElem = bodyElem.get("origin");
        rbModel.origin.x = originElem.getFloat("x");
        rbModel.origin.y = originElem.getFloat("y");
        for (JsonValue polygonsElem = bodyElem.getChild("polygons"); polygonsElem != null; polygonsElem = polygonsElem.next()) {
            PolygonModel polygon = new PolygonModel();
            rbModel.polygons.add(polygon);
            for (JsonValue vertexElem = polygonsElem.child(); vertexElem != null; vertexElem = vertexElem.next()) {
                float x = vertexElem.getFloat("x");
                float y = vertexElem.getFloat("y");
                polygon.vertices.add(new Vector2(x, y));
            }
            PolygonModel.access$002(polygon, new Vector2[polygon.vertices.size()]);
        }
        for (JsonValue circleElem = bodyElem.getChild("circles"); circleElem != null; circleElem = circleElem.next()) {
            CircleModel circle = new CircleModel();
            rbModel.circles.add(circle);
            circle.center.x = circleElem.getFloat("cx");
            circle.center.y = circleElem.getFloat("cy");
            circle.radius = circleElem.getFloat("r");
        }
        return rbModel;
    }

    private Vector2 newVec() {
        return this.vectorPool.isEmpty() ? new Vector2() : this.vectorPool.remove(0);
    }

    private void free(Vector2 v) {
        this.vectorPool.add(v);
    }

    public static class CircleModel {
        public final Vector2 center = new Vector2();
        public float radius;
    }

    public static class PolygonModel {
        public final List<Vector2> vertices = new ArrayList<Vector2>();
        private Vector2[] buffer;

        static /* synthetic */ Vector2[] access$002(PolygonModel x0, Vector2[] x1) {
            x0.buffer = x1;
            return x1;
        }
    }

    public static class RigidBodyModel {
        public String name;
        public String imagePath;
        public final Vector2 origin = new Vector2();
        public final List<PolygonModel> polygons = new ArrayList<PolygonModel>();
        public final List<CircleModel> circles = new ArrayList<CircleModel>();
    }

    public static class Model {
        public final Map<String, RigidBodyModel> rigidBodies = new HashMap<String, RigidBodyModel>();
    }
}

