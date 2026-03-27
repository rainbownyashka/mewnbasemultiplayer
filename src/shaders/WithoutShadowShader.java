/*
 * Decompiled with CFR 0.152.
 */
package shaders;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public final class WithoutShadowShader {
    public static final ShaderProgram createShadowShader() {
        String vertexShader = "attribute vec4 a_position;\nattribute vec2 a_texCoord;\nvarying vec2 v_texCoords;\n\nvoid main()\n{\n   v_texCoords = a_texCoord;\n   gl_Position = a_position;\n}\n";
        String fragmentShader = "#ifdef GL_ES\nprecision lowp float;\n#define MED mediump\n#else\n#define MED \n#endif\nvarying MED vec2 v_texCoords;\nuniform sampler2D u_texture;\nvoid main()\n{\ngl_FragColor = texture2D(u_texture, v_texCoords);\n}\n";
        ShaderProgram.pedantic = false;
        ShaderProgram woShadowShader = new ShaderProgram("attribute vec4 a_position;\nattribute vec2 a_texCoord;\nvarying vec2 v_texCoords;\n\nvoid main()\n{\n   v_texCoords = a_texCoord;\n   gl_Position = a_position;\n}\n", "#ifdef GL_ES\nprecision lowp float;\n#define MED mediump\n#else\n#define MED \n#endif\nvarying MED vec2 v_texCoords;\nuniform sampler2D u_texture;\nvoid main()\n{\ngl_FragColor = texture2D(u_texture, v_texCoords);\n}\n");
        if (!woShadowShader.isCompiled() && !(woShadowShader = new ShaderProgram("#version 330 core\nattribute vec4 a_position;\nattribute vec2 a_texCoord;\nvarying vec2 v_texCoords;\n\nvoid main()\n{\n   v_texCoords = a_texCoord;\n   gl_Position = a_position;\n}\n", "#version 330 core\n#ifdef GL_ES\nprecision lowp float;\n#define MED mediump\n#else\n#define MED \n#endif\nvarying MED vec2 v_texCoords;\nuniform sampler2D u_texture;\nvoid main()\n{\ngl_FragColor = texture2D(u_texture, v_texCoords);\n}\n")).isCompiled()) {
            Gdx.app.log("ERROR", woShadowShader.getLog());
        }
        return woShadowShader;
    }
}

