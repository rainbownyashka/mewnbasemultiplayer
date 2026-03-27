/*
 * Decompiled with CFR 0.152.
 */
package com.badlogic.gdx.ai.msg;

import com.badlogic.gdx.ai.msg.MessageDispatcher;

public final class MessageManager
extends MessageDispatcher {
    private static final MessageManager INSTANCE = new MessageManager();

    private MessageManager() {
    }

    public static MessageManager getInstance() {
        return INSTANCE;
    }
}

