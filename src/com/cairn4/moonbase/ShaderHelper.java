/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.cairn4.moonbase.MoonBase;

public class ShaderHelper {
    public static ShaderProgram colorFlashShader;
    public static final String colorFlashVertex = "shaders/colorflash/colorflash.vs";
    public static final String colorFlashFragment = "shaders/colorflash/colorflash.fs";
    public static ShaderProgram outlineShader;
    public static final String outlineVertex = "shaders/outline/outline.vs";
    public static final String outlineFragment = "shaders/outline/outline.fs";
    public static ShaderProgram scanlineShader;
    public static final String crtVertex = "shaders/crt/crt.vs";
    public static final String crtFragment = "shaders/crt/crt.fs";

    public static void load() {
        MoonBase.log("Compiling shaders...");
        colorFlashShader = ShaderHelper.compile(colorFlashVertex, colorFlashFragment);
        scanlineShader = ShaderHelper.compile(crtVertex, crtFragment);
    }

    public static void dispose() {
        scanlineShader.dispose();
    }

    public static ShaderProgram compile(String vertex, String fragment) {
        return ShaderHelper.compile(ShaderHelper.fHandle(vertex), ShaderHelper.fHandle(fragment));
    }

    public static ShaderProgram compile(FileHandle vertex, FileHandle fragment) {
        ShaderProgram shader = new ShaderProgram(vertex, fragment);
        ShaderProgram.pedantic = false;
        if (!shader.isCompiled()) {
            Gdx.app.log("MewnBase", "Could not compile the shader: " + shader.getLog());
        }
        return shader;
    }

    public static FileHandle fHandle(String path) {
        return Gdx.files.internal(path);
    }

    public static void setShader(ShaderProgram shader, Batch batch) {
        batch.setShader(shader);
    }
}

