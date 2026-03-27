/*
 * Decompiled with CFR 0.152.
 */
package com.badlogic.gdx.ai.btree.branch;

import com.badlogic.gdx.ai.btree.BranchTask;
import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.ai.btree.annotation.TaskAttribute;
import com.badlogic.gdx.utils.Array;

public class Parallel<E>
extends BranchTask<E> {
    @TaskAttribute
    public Policy policy;
    @TaskAttribute
    public Orchestrator orchestrator;
    private boolean noRunningTasks;
    private Boolean lastResult;
    private int currentChildIndex;

    public Parallel() {
        this(new Array<Task<E>>());
    }

    public Parallel(Task<E> ... tasks) {
        this(new Array<Task<E>>(tasks));
    }

    public Parallel(Array<Task<E>> tasks) {
        this(Policy.Sequence, tasks);
    }

    public Parallel(Policy policy) {
        this(policy, new Array<Task<E>>());
    }

    public Parallel(Policy policy, Task<E> ... tasks) {
        this(policy, new Array<Task<E>>(tasks));
    }

    public Parallel(Policy policy, Array<Task<E>> tasks) {
        this(policy, Orchestrator.Resume, tasks);
    }

    public Parallel(Orchestrator orchestrator, Array<Task<E>> tasks) {
        this(Policy.Sequence, orchestrator, tasks);
    }

    public Parallel(Orchestrator orchestrator, Task<E> ... tasks) {
        this(Policy.Sequence, orchestrator, new Array<Task<E>>(tasks));
    }

    public Parallel(Policy policy, Orchestrator orchestrator, Array<Task<E>> tasks) {
        super(tasks);
        this.policy = policy;
        this.orchestrator = orchestrator;
        this.noRunningTasks = true;
    }

    @Override
    public void run() {
        this.orchestrator.execute(this);
    }

    @Override
    public void childRunning(Task<E> task, Task<E> reporter) {
        this.noRunningTasks = false;
    }

    @Override
    public void childSuccess(Task<E> runningTask) {
        this.lastResult = this.policy.onChildSuccess(this);
    }

    @Override
    public void childFail(Task<E> runningTask) {
        this.lastResult = this.policy.onChildFail(this);
    }

    @Override
    public void resetTask() {
        super.resetTask();
        this.noRunningTasks = true;
    }

    @Override
    protected Task<E> copyTo(Task<E> task) {
        Parallel parallel = (Parallel)task;
        parallel.policy = this.policy;
        parallel.orchestrator = this.orchestrator;
        return super.copyTo(task);
    }

    public void resetAllChildren() {
        int n = this.getChildCount();
        for (int i = 0; i < n; ++i) {
            Task child = this.getChild(i);
            child.reset();
        }
    }

    @Override
    public void reset() {
        this.policy = Policy.Sequence;
        this.orchestrator = Orchestrator.Resume;
        this.noRunningTasks = true;
        this.lastResult = null;
        this.currentChildIndex = 0;
        super.reset();
    }

    public static enum Policy {
        Sequence{

            @Override
            public Boolean onChildSuccess(Parallel<?> parallel) {
                switch (parallel.orchestrator) {
                    case Join: {
                        return ((Parallel)parallel).noRunningTasks && ((Task)((Parallel)parallel).children.get(((Parallel)parallel).children.size - 1)).getStatus() == Task.Status.SUCCEEDED ? Boolean.TRUE : null;
                    }
                }
                return ((Parallel)parallel).noRunningTasks && ((Parallel)parallel).currentChildIndex == ((Parallel)parallel).children.size - 1 ? Boolean.TRUE : null;
            }

            @Override
            public Boolean onChildFail(Parallel<?> parallel) {
                return Boolean.FALSE;
            }
        }
        ,
        Selector{

            @Override
            public Boolean onChildSuccess(Parallel<?> parallel) {
                return Boolean.TRUE;
            }

            @Override
            public Boolean onChildFail(Parallel<?> parallel) {
                return ((Parallel)parallel).noRunningTasks && ((Parallel)parallel).currentChildIndex == ((Parallel)parallel).children.size - 1 ? Boolean.FALSE : null;
            }
        };


        public abstract Boolean onChildSuccess(Parallel<?> var1);

        public abstract Boolean onChildFail(Parallel<?> var1);
    }

    public static enum Orchestrator {
        Resume{

            @Override
            public void execute(Parallel<?> parallel) {
                ((Parallel)parallel).noRunningTasks = true;
                ((Parallel)parallel).lastResult = null;
                ((Parallel)parallel).currentChildIndex = 0;
                while (((Parallel)parallel).currentChildIndex < ((Parallel)parallel).children.size) {
                    Task child = (Task)((Parallel)parallel).children.get(((Parallel)parallel).currentChildIndex);
                    if (child.getStatus() == Task.Status.RUNNING) {
                        child.run();
                    } else {
                        child.setControl(parallel);
                        child.start();
                        if (child.checkGuard(parallel)) {
                            child.run();
                        } else {
                            child.fail();
                        }
                    }
                    if (((Parallel)parallel).lastResult != null) {
                        ((Parallel)parallel).cancelRunningChildren(((Parallel)parallel).noRunningTasks ? ((Parallel)parallel).currentChildIndex + 1 : 0);
                        if (((Parallel)parallel).lastResult.booleanValue()) {
                            parallel.success();
                        } else {
                            parallel.fail();
                        }
                        return;
                    }
                    ((Parallel)parallel).currentChildIndex++;
                }
                parallel.running();
            }
        }
        ,
        Join{

            @Override
            public void execute(Parallel<?> parallel) {
                ((Parallel)parallel).noRunningTasks = true;
                ((Parallel)parallel).lastResult = null;
                ((Parallel)parallel).currentChildIndex = 0;
                while (((Parallel)parallel).currentChildIndex < ((Parallel)parallel).children.size) {
                    Task child = (Task)((Parallel)parallel).children.get(((Parallel)parallel).currentChildIndex);
                    switch (child.getStatus()) {
                        case RUNNING: {
                            child.run();
                            break;
                        }
                        case SUCCEEDED: 
                        case FAILED: {
                            break;
                        }
                        default: {
                            child.setControl(parallel);
                            child.start();
                            if (child.checkGuard(parallel)) {
                                child.run();
                                break;
                            }
                            child.fail();
                        }
                    }
                    if (((Parallel)parallel).lastResult != null) {
                        ((Parallel)parallel).cancelRunningChildren(((Parallel)parallel).noRunningTasks ? ((Parallel)parallel).currentChildIndex + 1 : 0);
                        parallel.resetAllChildren();
                        if (((Parallel)parallel).lastResult.booleanValue()) {
                            parallel.success();
                        } else {
                            parallel.fail();
                        }
                        return;
                    }
                    ((Parallel)parallel).currentChildIndex++;
                }
                parallel.running();
            }
        };


        public abstract void execute(Parallel<?> var1);
    }
}

