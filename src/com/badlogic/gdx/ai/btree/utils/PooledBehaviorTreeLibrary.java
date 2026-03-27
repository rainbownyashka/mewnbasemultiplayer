/*
 * Decompiled with CFR 0.152.
 */
package com.badlogic.gdx.ai.btree.utils;

import com.badlogic.gdx.ai.btree.BehaviorTree;
import com.badlogic.gdx.ai.btree.utils.BehaviorTreeLibrary;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Pool;

public class PooledBehaviorTreeLibrary
extends BehaviorTreeLibrary {
    protected ObjectMap<String, Pool<BehaviorTree>> pools = new ObjectMap();

    protected Pool<BehaviorTree> getPool(final String treeReference) {
        Pool<BehaviorTree> treePool = this.pools.get(treeReference);
        if (treePool == null) {
            treePool = new Pool<BehaviorTree>(){

                @Override
                protected BehaviorTree newObject() {
                    return PooledBehaviorTreeLibrary.this.newBehaviorTree(treeReference);
                }
            };
            this.pools.put(treeReference, treePool);
        }
        return treePool;
    }

    protected <T> BehaviorTree<T> newBehaviorTree(String treeReference) {
        return super.createBehaviorTree(treeReference, null);
    }

    @Override
    public <T> BehaviorTree<T> createBehaviorTree(String treeReference, T blackboard) {
        Pool<BehaviorTree> pool = this.getPool(treeReference);
        BehaviorTree tree = pool.obtain();
        tree.setObject(blackboard);
        return tree;
    }

    @Override
    public void disposeBehaviorTree(String treeReference, BehaviorTree<?> behaviorTree) {
        Pool<BehaviorTree> pool = this.getPool(treeReference);
        pool.free(behaviorTree);
    }

    public void clear(String treeReference) {
        Pool<BehaviorTree> treePool = this.pools.get(treeReference);
        if (treePool != null) {
            treePool.clear();
        }
    }

    public void clear() {
        for (ObjectMap.Entry entry : this.pools.entries()) {
            ((Pool)entry.value).clear();
        }
        this.pools.clear();
    }
}

