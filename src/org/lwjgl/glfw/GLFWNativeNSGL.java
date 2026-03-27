/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package org.lwjgl.glfw;

import javax.annotation.Nullable;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.APIUtil;
import org.lwjgl.system.Checks;
import org.lwjgl.system.FunctionProvider;
import org.lwjgl.system.JNI;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.NativeType;
import org.lwjgl.system.macosx.MacOSXLibraryBundle;

public class GLFWNativeNSGL {
    protected GLFWNativeNSGL() {
        throw new UnsupportedOperationException();
    }

    @NativeType(value="id")
    public static long glfwGetNSGLContext(@NativeType(value="GLFWwindow *") long window) {
        long __functionAddress = Functions.GetNSGLContext;
        if (Checks.CHECKS) {
            Checks.check(window);
        }
        return JNI.invokePP(window, __functionAddress);
    }

    public static void setFrameworkLWJGL() {
        FunctionProvider fp = GL.getFunctionProvider();
        if (!(fp instanceof MacOSXLibraryBundle)) {
            APIUtil.apiLog("GLFW OpenGL path override not set: OpenGL function provider is not a framework.");
            return;
        }
        GLFWNativeNSGL.setFramework(((MacOSXLibraryBundle)fp).getName());
    }

    public static void setFramework(@Nullable String path) {
        long override = GLFW.getLibrary().getFunctionAddress("_glfw_opengl_library");
        if (override == 0L) {
            APIUtil.apiLog("GLFW OpenGL path override not set: Could not resolve override symbol.");
            return;
        }
        long a = MemoryUtil.memGetAddress(override);
        if (a != 0L) {
            MemoryUtil.nmemFree(a);
        }
        MemoryUtil.memPutAddress(override, path == null ? 0L : MemoryUtil.memAddress(MemoryUtil.memUTF8(path)));
    }

    public static final class Functions {
        public static final long GetNSGLContext = APIUtil.apiGetFunctionAddress(GLFW.getLibrary(), "glfwGetNSGLContext");

        private Functions() {
        }
    }
}

