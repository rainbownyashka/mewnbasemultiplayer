/*
 * Decompiled with CFR 0.152.
 */
package box2dLight;

import box2dLight.RayHandler;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.badlogic.gdx.utils.Disposable;

public abstract class Light
implements Disposable {
    static final Color DefaultColor = new Color(0.75f, 0.75f, 0.5f, 0.75f);
    static final float zeroColorBits = Color.toFloatBits(0.0f, 0.0f, 0.0f, 0.0f);
    static final int MIN_RAYS = 3;
    protected final Color color = new Color();
    protected final Vector2 tmpPosition = new Vector2();
    protected RayHandler rayHandler;
    protected boolean active = true;
    protected boolean soft = true;
    protected boolean xray = false;
    protected boolean staticLight = false;
    protected boolean culled = false;
    protected boolean dirty = true;
    protected boolean ignoreBody = false;
    protected int rayNum;
    protected int vertexNum;
    protected float distance;
    protected float direction;
    protected float colorF;
    protected float softShadowLength = 2.5f;
    protected Mesh lightMesh;
    protected Mesh softShadowMesh;
    protected float[] segments;
    protected float[] mx;
    protected float[] my;
    protected float[] f;
    protected int m_index = 0;
    private static Filter globalFilterA = null;
    private Filter filterA = null;
    final RayCastCallback ray = new RayCastCallback(){

        @Override
        public final float reportRayFixture(Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
            if (globalFilterA != null && !Light.this.globalContactFilter(fixture)) {
                return -1.0f;
            }
            if (Light.this.filterA != null && !Light.this.contactFilter(fixture)) {
                return -1.0f;
            }
            if (Light.this.ignoreBody && fixture.getBody() == Light.this.getBody()) {
                return -1.0f;
            }
            Light.this.mx[Light.this.m_index] = point.x;
            Light.this.my[Light.this.m_index] = point.y;
            Light.this.f[Light.this.m_index] = fraction;
            return fraction;
        }
    };

    public Light(RayHandler rayHandler, int rays, Color color, float distance, float directionDegree) {
        rayHandler.lightList.add(this);
        this.rayHandler = rayHandler;
        this.setRayNum(rays);
        this.setColor(color);
        this.setDistance(distance);
        this.setSoftnessLength(distance * 0.1f);
        this.setDirection(directionDegree);
    }

    abstract void update();

    abstract void render();

    public abstract void setDistance(float var1);

    public abstract void setDirection(float var1);

    public abstract void attachToBody(Body var1);

    public abstract Body getBody();

    public abstract void setPosition(float var1, float var2);

    public abstract void setPosition(Vector2 var1);

    public abstract float getX();

    public abstract float getY();

    public Vector2 getPosition() {
        return this.tmpPosition;
    }

    public void setColor(Color newColor) {
        if (newColor != null) {
            this.color.set(newColor);
        } else {
            this.color.set(DefaultColor);
        }
        this.colorF = this.color.toFloatBits();
        if (this.staticLight) {
            this.dirty = true;
        }
    }

    public void setColor(float r, float g, float b, float a) {
        this.color.set(r, g, b, a);
        this.colorF = this.color.toFloatBits();
        if (this.staticLight) {
            this.dirty = true;
        }
    }

    public void add(RayHandler rayHandler) {
        this.rayHandler = rayHandler;
        if (this.active) {
            rayHandler.lightList.add(this);
        } else {
            rayHandler.disabledLights.add(this);
        }
    }

    public void remove() {
        this.remove(true);
    }

    public void remove(boolean doDispose) {
        if (this.active) {
            this.rayHandler.lightList.removeValue(this, false);
        } else {
            this.rayHandler.disabledLights.removeValue(this, false);
        }
        this.rayHandler = null;
        if (doDispose) {
            this.dispose();
        }
    }

    @Override
    public void dispose() {
        this.lightMesh.dispose();
        this.softShadowMesh.dispose();
    }

    public boolean isActive() {
        return this.active;
    }

    public void setActive(boolean active) {
        if (active == this.active) {
            return;
        }
        this.active = active;
        if (this.rayHandler == null) {
            return;
        }
        if (active) {
            this.rayHandler.lightList.add(this);
            this.rayHandler.disabledLights.removeValue(this, true);
        } else {
            this.rayHandler.disabledLights.add(this);
            this.rayHandler.lightList.removeValue(this, true);
        }
    }

    public boolean isXray() {
        return this.xray;
    }

    public void setXray(boolean xray) {
        this.xray = xray;
        if (this.staticLight) {
            this.dirty = true;
        }
    }

    public boolean isStaticLight() {
        return this.staticLight;
    }

    public void setStaticLight(boolean staticLight) {
        this.staticLight = staticLight;
        if (staticLight) {
            this.dirty = true;
        }
    }

    public boolean isSoft() {
        return this.soft;
    }

    public void setSoft(boolean soft) {
        this.soft = soft;
        if (this.staticLight) {
            this.dirty = true;
        }
    }

    public float getSoftShadowLength() {
        return this.softShadowLength;
    }

    public void setSoftnessLength(float softShadowLength) {
        this.softShadowLength = softShadowLength;
        if (this.staticLight) {
            this.dirty = true;
        }
    }

    public Color getColor() {
        return this.color;
    }

    public float getDistance() {
        return this.distance / RayHandler.gammaCorrectionParameter;
    }

    public float getDirection() {
        return this.direction;
    }

    public boolean contains(float x, float y) {
        return false;
    }

    public void setIgnoreAttachedBody(boolean flag) {
        this.ignoreBody = flag;
    }

    public boolean getIgnoreAttachedBody() {
        return this.ignoreBody;
    }

    void setRayNum(int rays) {
        if (rays < 3) {
            rays = 3;
        }
        this.rayNum = rays;
        this.vertexNum = rays + 1;
        this.segments = new float[this.vertexNum * 8];
        this.mx = new float[this.vertexNum];
        this.my = new float[this.vertexNum];
        this.f = new float[this.vertexNum];
    }

    public int getRayNum() {
        return this.rayNum;
    }

    boolean contactFilter(Fixture fixtureB) {
        Filter filterB = fixtureB.getFilterData();
        if (this.filterA.groupIndex != 0 && this.filterA.groupIndex == filterB.groupIndex) {
            return this.filterA.groupIndex > 0;
        }
        return (this.filterA.maskBits & filterB.categoryBits) != 0 && (this.filterA.categoryBits & filterB.maskBits) != 0;
    }

    public void setContactFilter(Filter filter) {
        this.filterA = filter;
    }

    public void setContactFilter(short categoryBits, short groupIndex, short maskBits) {
        this.filterA = new Filter();
        this.filterA.categoryBits = categoryBits;
        this.filterA.groupIndex = groupIndex;
        this.filterA.maskBits = maskBits;
    }

    boolean globalContactFilter(Fixture fixtureB) {
        Filter filterB = fixtureB.getFilterData();
        if (Light.globalFilterA.groupIndex != 0 && Light.globalFilterA.groupIndex == filterB.groupIndex) {
            return Light.globalFilterA.groupIndex > 0;
        }
        return (Light.globalFilterA.maskBits & filterB.categoryBits) != 0 && (Light.globalFilterA.categoryBits & filterB.maskBits) != 0;
    }

    public static void setGlobalContactFilter(Filter filter) {
        globalFilterA = filter;
    }

    public static void setGlobalContactFilter(short categoryBits, short groupIndex, short maskBits) {
        globalFilterA = new Filter();
        Light.globalFilterA.categoryBits = categoryBits;
        Light.globalFilterA.groupIndex = groupIndex;
        Light.globalFilterA.maskBits = maskBits;
    }
}

