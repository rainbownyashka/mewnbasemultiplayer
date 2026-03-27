/*
 * Decompiled with CFR 0.152.
 */
package org.lwjgl.system;

import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.Pointer;
import org.lwjgl.system.libc.LibCString;

final class MultiReleaseMemCopy {
    private MultiReleaseMemCopy() {
    }

    static void copy(long src, long dst, long bytes) {
        if (bytes < 384L) {
            int s = (int)src;
            int d = (int)dst;
            if (Pointer.BITS64) {
                if ((s & 7) == 0 && (d & 7) == 0) {
                    MemoryUtil.memCopyAligned64(src, dst, (int)bytes & 0x1FF);
                    return;
                }
            } else if ((s & 3) == 0 && (d & 3) == 0) {
                MemoryUtil.memCopyAligned32(s, d, (int)bytes & 0x1FF);
                return;
            }
            MemoryUtil.UNSAFE.copyMemory(null, src, null, dst, bytes);
            return;
        }
        LibCString.nmemcpy(dst, src, bytes);
    }
}

