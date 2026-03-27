/*
 * Decompiled with CFR 0.152.
 */
package shaders;

import box2dLight.RayHandler;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public final class LightShader {
    public static final ShaderProgram createLightShader() {
        String gamma = "";
        if (RayHandler.getGammaCorrection()) {
            gamma = "sqrt";
        }
        String vertexShader = "attribute vec4 vertex_positions;\nattribute vec4 quad_colors;\nattribute float s;\nuniform mat4 u_projTrans;\nvarying vec4 v_color;\nvoid main()\n{\n   v_color = s * quad_colors;\n   gl_Position =  u_projTrans * vertex_positions;\n}\n";
        String fragmentShader = "#ifdef GL_ES\nprecision lowp float;\n#define MED mediump\n#else\n#define MED \n#endif\nvarying vec4 v_color;\nvoid main()\n{\n  gl_FragColor = " + gamma + "(v_color);\n}";
        ShaderProgram.pedantic = false;
        ShaderProgram lightShader = new ShaderProgram("attribute vec4 vertex_positions;\nattribute vec4 quad_colors;\nattribute float s;\nuniform mat4 u_projTrans;\nvarying vec4 v_color;\nvoid main()\n{\n   v_color = s * quad_colors;\n   gl_Position =  u_projTrans * vertex_positions;\n}\n", fragmentShader);
        if (!lightShader.isCompiled() && !(lightShader = new ShaderProgram("#version 330 core\nattribute vec4 vertex_positions;\nattribute vec4 quad_colors;\nattribute float s;\nuniform mat4 u_projTrans;\nvarying vec4 v_color;\nvoid main()\n{\n   v_color = s * quad_colors;\n   gl_Position =  u_projTrans * vertex_positions;\n}\n", "#version 330 core\n" + fragmentShader)).isCompiled()) {
            Gdx.app.log("ERROR", lightShader.getLog());
        }
        return lightShader;
    }
}

