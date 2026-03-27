/*
 * Decompiled with CFR 0.152.
 */
package org.lwjgl.system;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.APIUtil;
import org.lwjgl.system.Configuration;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.Pointer;
import org.lwjgl.system.jni.JNINativeInterface;

public final class ThreadLocalUtil {
    private static final long JNI_NATIVE_INTERFACE;
    private static final int JNI_NATIVE_INTERFACE_FUNCTION_COUNT;
    private static final long FUNCTION_MISSING_ABORT;
    private static final int CAPABILITIES_OFFSET;

    private ThreadLocalUtil() {
    }

    private static native long getThreadJNIEnv();

    private static native long getFunctionMissingAbort();

    private static native long nsetupEnvData(int var0);

    public static long setupEnvData() {
        return ThreadLocalUtil.nsetupEnvData(JNI_NATIVE_INTERFACE_FUNCTION_COUNT);
    }

    public static void setCapabilities(long capabilities) {
        long env_pp = ThreadLocalUtil.getThreadJNIEnv();
        long env_p = MemoryUtil.memGetAddress(env_pp);
        if (capabilities == 0L) {
            if (env_p != JNI_NATIVE_INTERFACE) {
                MemoryUtil.memPutAddress(env_p + (long)CAPABILITIES_OFFSET, MemoryUtil.memGetAddress(JNI_NATIVE_INTERFACE + (long)CAPABILITIES_OFFSET));
            }
        } else {
            if (env_p == JNI_NATIVE_INTERFACE) {
                ThreadLocalUtil.setupEnvData();
                env_p = MemoryUtil.memGetAddress(env_pp);
            }
            MemoryUtil.memPutAddress(env_p + (long)CAPABILITIES_OFFSET, capabilities);
        }
    }

    public static void setFunctionMissingAddresses(int functionCount) {
        long ptr = JNI_NATIVE_INTERFACE + (long)CAPABILITIES_OFFSET;
        if (functionCount == 0) {
            long missingCaps = MemoryUtil.memGetAddress(ptr);
            if (missingCaps != 0L) {
                MemoryUtil.getAllocator().free(missingCaps);
                MemoryUtil.memPutAddress(ptr, 0L);
            }
        } else {
            long missingCaps = MemoryUtil.getAllocator().malloc(Integer.toUnsignedLong(functionCount) * (long)Pointer.POINTER_SIZE);
            for (int i = 0; i < functionCount; ++i) {
                MemoryUtil.memPutAddress(missingCaps + Integer.toUnsignedLong(i) * (long)Pointer.POINTER_SIZE, FUNCTION_MISSING_ABORT);
            }
            MemoryUtil.memPutAddress(ptr, missingCaps);
        }
    }

    public static PointerBuffer setupAddressBuffer(PointerBuffer addresses) {
        for (int i = addresses.position(); i < addresses.limit(); ++i) {
            if (addresses.get(i) != 0L) continue;
            addresses.put(i, FUNCTION_MISSING_ABORT);
        }
        return addresses;
    }

    public static boolean areCapabilitiesDifferent(PointerBuffer ref, PointerBuffer caps) {
        for (int i = 0; i < ref.remaining(); ++i) {
            if (ref.get(i) == caps.get(i) || caps.get(i) == 0L) continue;
            return true;
        }
        return false;
    }

    static {
        int jniCallCount;
        int reservedCount;
        JNI_NATIVE_INTERFACE = MemoryUtil.memGetAddress(ThreadLocalUtil.getThreadJNIEnv());
        FUNCTION_MISSING_ABORT = ThreadLocalUtil.getFunctionMissingAbort();
        CAPABILITIES_OFFSET = 3 * Pointer.POINTER_SIZE;
        int JNI_VERSION = JNINativeInterface.GetVersion();
        switch (JNI_VERSION) {
            case 65537: {
                reservedCount = 12;
                break;
            }
            default: {
                reservedCount = 4;
            }
        }
        switch (JNI_VERSION) {
            case 65537: {
                jniCallCount = 208;
                break;
            }
            case 65538: {
                jniCallCount = 225;
                break;
            }
            case 65540: {
                jniCallCount = 228;
                break;
            }
            case 65542: 
            case 65544: {
                jniCallCount = 229;
                break;
            }
            case 589824: 
            case 655360: {
                jniCallCount = 230;
                break;
            }
            case 0x130000: 
            case 0x140000: {
                jniCallCount = 231;
                break;
            }
            default: {
                jniCallCount = 231;
                APIUtil.DEBUG_STREAM.println("[LWJGL] [ThreadLocalUtil] Unsupported JNI version detected, this may result in a crash. Please inform LWJGL developers.");
            }
        }
        JNI_NATIVE_INTERFACE_FUNCTION_COUNT = reservedCount + Configuration.JNI_NATIVE_INTERFACE_FUNCTION_COUNT.get(jniCallCount);
    }
}

