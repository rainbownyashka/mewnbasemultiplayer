/*
 * Decompiled with CFR 0.152.
 */
package de.golfgl.gdx.controllers;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.UIUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pools;
import com.badlogic.gdx.utils.viewport.Viewport;
import de.golfgl.gdx.controllers.IControllerActable;
import de.golfgl.gdx.controllers.IControllerManageFocus;
import de.golfgl.gdx.controllers.IControllerScrollable;

public class ControllerMenuStage
extends Stage {
    private static final float INITIAL_DIRECTION_EMPH_FACTOR = 3.1f;
    private final Vector2 controllerTempCoords = new Vector2();
    private Array<Actor> focusableActors = new Array();
    private boolean isPressed;
    private boolean focusOnTouchdown = true;
    private boolean sendMouseOverEvents = true;
    private Actor focusedActor;
    private Actor escapeActor;
    private float directionEmphFactor = 3.1f;

    public ControllerMenuStage(Viewport viewport) {
        super(viewport);
    }

    public ControllerMenuStage(Viewport viewport, Batch batch) {
        super(viewport, batch);
    }

    public boolean isFocusOnTouchdown() {
        return this.focusOnTouchdown;
    }

    public ControllerMenuStage setFocusOnTouchdown(boolean focusOnTouchdown) {
        this.focusOnTouchdown = focusOnTouchdown;
        return this;
    }

    public boolean isSendMouseOverEvents() {
        return this.sendMouseOverEvents;
    }

    public ControllerMenuStage setSendMouseOverEvents(boolean sendMouseOverEvents) {
        this.sendMouseOverEvents = sendMouseOverEvents;
        return this;
    }

    public Actor getEscapeActor() {
        return this.escapeActor;
    }

    public void setEscapeActor(Actor escapeActor) {
        this.escapeActor = escapeActor;
    }

    public void addFocusableActor(Actor actor) {
        this.focusableActors.add(actor);
    }

    public void addFocusableActors(Array<Actor> actors) {
        for (int i = 0; i < actors.size; ++i) {
            this.addFocusableActor(actors.get(i));
        }
    }

    public void clearFocusableActors() {
        this.setFocusedActor(null);
        this.focusableActors.clear();
    }

    public void removeFocusableActorsNotOnStage() {
        for (int i = this.focusableActors.size - 1; i >= 0; --i) {
            if (this.focusableActors.get(i).getStage() == this) continue;
            this.focusableActors.removeIndex(i);
        }
    }

    public void removeFocusableActor(Actor actor) {
        this.focusableActors.removeValue(actor, true);
    }

    public void removeFocusableActors(Array<Actor> actors) {
        for (int i = 0; i < actors.size; ++i) {
            this.removeFocusableActor(actors.get(i));
        }
    }

    public Array<Actor> getFocusableActors() {
        return this.focusableActors;
    }

    public boolean setFocusedActor(Actor actor) {
        if (this.focusedActor == actor) {
            return true;
        }
        if (actor != null && !this.isActorFocusable(actor)) {
            return false;
        }
        Actor oldFocused = this.focusedActor;
        if (oldFocused != null) {
            this.focusedActor = null;
            this.onFocusLost(oldFocused, actor);
        }
        if (this.focusedActor == null) {
            this.focusedActor = actor;
            if (this.focusedActor != null) {
                this.onFocusGained(this.focusedActor, oldFocused);
            }
            return true;
        }
        return false;
    }

    public Actor getFocusedActor() {
        return this.focusedActor;
    }

    protected void onFocusGained(Actor focusedActor, Actor oldFocused) {
        if (this.sendMouseOverEvents) {
            this.fireEventOnActor(focusedActor, InputEvent.Type.enter, -1, oldFocused);
        }
        this.setKeyboardFocus(focusedActor);
        this.setScrollFocus(focusedActor);
    }

    protected void onFocusLost(Actor focusedActor, Actor newFocused) {
        if (this.isPressed) {
            this.cancelTouchFocus();
            this.isPressed = false;
        }
        if (this.sendMouseOverEvents) {
            this.fireEventOnActor(focusedActor, InputEvent.Type.exit, -1, newFocused);
        }
    }

    protected boolean isActorFocusable(Actor actor) {
        if (!this.focusableActors.contains(actor, true)) {
            return false;
        }
        if (!actor.isVisible()) {
            return false;
        }
        if (!actor.isTouchable() && !(actor instanceof IControllerActable)) {
            return false;
        }
        return actor.getStage() == this;
    }

    protected boolean isActorHittable(Actor actor) {
        Vector2 center = actor.localToStageCoordinates(new Vector2(actor.getWidth() / 2.0f, actor.getHeight() / 2.0f));
        Actor hitActor = this.hit(center.x, center.y, true);
        return hitActor != null && hitActor.isDescendantOf(actor);
    }

    protected boolean isActorInViewportArea(Actor actor) {
        Vector2 leftBottom = actor.localToStageCoordinates(new Vector2(0.0f, 0.0f));
        Vector2 rightTop = actor.localToStageCoordinates(new Vector2(actor.getWidth(), actor.getHeight()));
        return !(leftBottom.x > this.getWidth() || leftBottom.y > this.getHeight() || rightTop.x < 0.0f || rightTop.y < 0.0f);
    }

    @Override
    public boolean keyDown(int keyCode) {
        boolean handled;
        if (this.isGoDownKeyCode(keyCode)) {
            handled = this.moveFocusByDirection(MoveFocusDirection.south);
        } else if (this.isGoUpKeyCode(keyCode)) {
            handled = this.moveFocusByDirection(MoveFocusDirection.north);
        } else if (this.isGoLeftKeyCode(keyCode)) {
            handled = this.moveFocusByDirection(MoveFocusDirection.west);
        } else if (this.isGoRightKeyCode(keyCode)) {
            handled = this.moveFocusByDirection(MoveFocusDirection.east);
        } else if (this.isGoNextKeyCode(keyCode)) {
            handled = this.moveFocusByList(true);
        } else if (this.isGoBackKeyCode(keyCode)) {
            handled = this.moveFocusByList(false);
        } else if (this.isDefaultActionKeyCode(keyCode)) {
            this.isPressed = handled = this.triggerActionOnActor(true, this.focusedActor);
        } else if (this.isEscapeActionKeyCode(keyCode) && this.escapeActor != null) {
            this.isPressed = handled = this.triggerActionOnActor(true, this.escapeActor);
        } else {
            handled = false;
        }
        if (!handled) {
            handled = super.keyDown(keyCode);
        }
        return handled;
    }

    protected boolean triggerActionOnActor(boolean keyDown, Actor actor) {
        if (actor instanceof IControllerActable && keyDown) {
            return ((IControllerActable)((Object)actor)).onControllerDefaultKeyDown();
        }
        if (actor instanceof IControllerActable) {
            return ((IControllerActable)((Object)actor)).onControllerDefaultKeyUp();
        }
        return this.fireEventOnActor(actor, keyDown ? InputEvent.Type.touchDown : InputEvent.Type.touchUp, 0, null);
    }

    @Override
    public boolean keyUp(int keyCode) {
        boolean handled;
        if (this.isDefaultActionKeyCode(keyCode)) {
            this.isPressed = false;
            handled = this.triggerActionOnActor(false, this.focusedActor);
        } else if (this.isEscapeActionKeyCode(keyCode) && this.escapeActor != null) {
            this.isPressed = handled = this.triggerActionOnActor(false, this.escapeActor);
        } else {
            handled = false;
        }
        if (!handled) {
            handled = super.keyUp(keyCode);
        }
        return handled;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (this.isFocusOnTouchdown()) {
            this.screenToStageCoordinates(this.controllerTempCoords.set(screenX, screenY));
            Actor target = this.hit(this.controllerTempCoords.x, this.controllerTempCoords.y, true);
            if (target != null) {
                if (this.isActorFocusable(target)) {
                    this.setFocusedActor(target);
                } else {
                    for (Actor actor : this.getFocusableActors()) {
                        if (!target.isDescendantOf(actor)) continue;
                        this.setFocusedActor(actor);
                    }
                }
            }
        }
        return super.touchDown(screenX, screenY, pointer, button);
    }

    public boolean isDefaultActionKeyCode(int keyCode) {
        switch (keyCode) {
            case 23: 
            case 66: {
                return true;
            }
        }
        return false;
    }

    public boolean isEscapeActionKeyCode(int keyCode) {
        switch (keyCode) {
            case 4: 
            case 67: 
            case 131: {
                return true;
            }
        }
        return false;
    }

    public boolean isGoRightKeyCode(int keyCode) {
        return keyCode == 22;
    }

    public boolean isGoLeftKeyCode(int keyCode) {
        return keyCode == 21;
    }

    public boolean isGoUpKeyCode(int keyCode) {
        return keyCode == 19;
    }

    public boolean isGoDownKeyCode(int keyCode) {
        return keyCode == 20;
    }

    public boolean isGoNextKeyCode(int keyCode) {
        return keyCode == 61 && !UIUtils.shift();
    }

    public boolean isGoBackKeyCode(int keyCode) {
        return keyCode == 61 && UIUtils.shift();
    }

    protected boolean fireEventOnActor(Actor actor, InputEvent.Type type, int pointer, Actor related) {
        if (actor == null || !this.isActorFocusable(actor) || !this.isActorHittable(actor)) {
            return false;
        }
        InputEvent event = Pools.obtain(InputEvent.class);
        event.setType(type);
        event.setStage(this);
        event.setRelatedActor(related);
        event.setPointer(pointer);
        event.setButton(pointer);
        event.setStageX(0.0f);
        event.setStageY(0.0f);
        actor.fire(event);
        boolean handled = event.isHandled();
        Pools.free(event);
        return handled;
    }

    protected boolean moveFocusByList(boolean next) {
        if (this.focusedActor == null) {
            return false;
        }
        Actor nextActor = null;
        if (this.focusedActor instanceof IControllerManageFocus) {
            nextActor = ((IControllerManageFocus)((Object)this.focusedActor)).getNextFocusableActor(next);
        }
        if (nextActor == null) {
            int index;
            for (index = this.focusableActors.indexOf(this.focusedActor, true); !next && index > 0 && nextActor == null; --index) {
                if (!this.isActorFocusable(this.focusableActors.get(index - 1))) continue;
                nextActor = this.focusableActors.get(index - 1);
            }
            while (next && index < this.focusableActors.size - 1 && nextActor == null) {
                if (this.isActorFocusable(this.focusableActors.get(index + 1))) {
                    nextActor = this.focusableActors.get(index + 1);
                }
                ++index;
            }
        }
        if (nextActor != null) {
            return this.setFocusedActor(nextActor);
        }
        return false;
    }

    protected boolean moveFocusByDirection(MoveFocusDirection direction) {
        boolean hasScrolled;
        if (this.focusedActor == null) {
            return false;
        }
        Actor nextFocusedActor = null;
        if (this.focusedActor instanceof IControllerManageFocus) {
            nextFocusedActor = ((IControllerManageFocus)((Object)this.focusedActor)).getNextFocusableActor(direction);
        }
        if (nextFocusedActor == null) {
            nextFocusedActor = this.findNearestFocusableNeighbour(direction);
        }
        if (!(hasScrolled = this.checkForScrollable(direction, nextFocusedActor)) && nextFocusedActor != null) {
            return this.setFocusedActor(nextFocusedActor);
        }
        return hasScrolled;
    }

    private Actor findNearestFocusableNeighbour(MoveFocusDirection direction) {
        Vector2 focusedPosition = this.focusedActor.localToStageCoordinates(new Vector2(direction == MoveFocusDirection.east ? this.focusedActor.getWidth() : (direction == MoveFocusDirection.west ? 0.0f : this.focusedActor.getWidth() / 2.0f), direction == MoveFocusDirection.north ? this.focusedActor.getHeight() : (direction == MoveFocusDirection.south ? 0.0f : this.focusedActor.getHeight() / 2.0f)));
        Actor nearestInDirection = null;
        float distance = Float.MAX_VALUE;
        for (int i = 0; i < this.focusableActors.size; ++i) {
            float currentDist;
            Actor currentActor = this.focusableActors.get(i);
            if (currentActor == this.focusedActor || !this.isActorFocusable(currentActor) || !this.isActorInViewportArea(currentActor)) continue;
            Vector2 currentActorPos = currentActor.localToStageCoordinates(new Vector2(direction == MoveFocusDirection.west ? currentActor.getWidth() : (direction == MoveFocusDirection.east ? 0.0f : currentActor.getWidth() / 2.0f), direction == MoveFocusDirection.south ? currentActor.getHeight() : (direction == MoveFocusDirection.south ? 0.0f : currentActor.getHeight() / 2.0f)));
            boolean isInDirection = false;
            boolean bl = isInDirection = direction == MoveFocusDirection.south && currentActorPos.y <= focusedPosition.y || direction == MoveFocusDirection.north && currentActorPos.y >= focusedPosition.y || direction == MoveFocusDirection.west && currentActorPos.x <= focusedPosition.x || direction == MoveFocusDirection.east && currentActorPos.x >= focusedPosition.x;
            if (!isInDirection || !this.isActorHittable(currentActor) || !((currentDist = this.calcNeighbourDistance(direction, focusedPosition, currentActorPos)) < distance)) continue;
            nearestInDirection = currentActor;
            distance = currentDist;
        }
        return nearestInDirection;
    }

    protected float calcNeighbourDistance(MoveFocusDirection direction, Vector2 focusedPosition, Vector2 currentActorPos) {
        float horizontalDist = currentActorPos.x - focusedPosition.x;
        float verticalDist = currentActorPos.y - focusedPosition.y;
        if (direction == MoveFocusDirection.south || direction == MoveFocusDirection.north) {
            horizontalDist *= this.directionEmphFactor;
        } else {
            verticalDist *= this.directionEmphFactor;
        }
        return horizontalDist * horizontalDist + verticalDist * verticalDist;
    }

    public float getDirectionEmphFactor() {
        return this.directionEmphFactor;
    }

    public void setDirectionEmphFactor(float directionEmphFactor) {
        this.directionEmphFactor = directionEmphFactor;
    }

    protected boolean checkForScrollable(MoveFocusDirection direction, Actor nearestInDirection) {
        Actor findScrollable = this.focusedActor;
        boolean didScroll = false;
        while (!didScroll) {
            if (findScrollable == null) {
                return false;
            }
            if (findScrollable instanceof IControllerScrollable) {
                if (nearestInDirection != null) {
                    Actor nearestNeighboursParent;
                    for (nearestNeighboursParent = nearestInDirection; nearestNeighboursParent != null && nearestNeighboursParent != findScrollable; nearestNeighboursParent = nearestNeighboursParent.getParent()) {
                    }
                    if (nearestNeighboursParent == findScrollable) {
                        return false;
                    }
                }
                didScroll = ((IControllerScrollable)((Object)findScrollable)).onControllerScroll(direction);
            }
            findScrollable = findScrollable.getParent();
        }
        return didScroll;
    }

    @Override
    public void unfocusAll() {
        super.unfocusAll();
        this.setFocusedActor(null);
    }

    @Override
    public void unfocus(Actor actor) {
        super.unfocus(actor);
        if (actor == this.focusedActor) {
            this.setFocusedActor(null);
        }
    }

    public static enum MoveFocusDirection {
        west,
        north,
        east,
        south;

    }
}

