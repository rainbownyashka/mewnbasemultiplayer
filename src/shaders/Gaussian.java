/*
 * Decompiled with CFR 0.152.
 */
package shaders;

import box2dLight.RayHandler;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public class Gaussian {
    public static ShaderProgram createBlurShader(int width, int heigth) {
        String FBO_W = Integer.toString(width);
        String FBO_H = Integer.toString(heigth);
        String rgb = RayHandler.isDiffuse ? ".rgb" : "";
        String vertexShader = "attribute vec4 a_position;\nuniform vec2  dir;\nattribute vec2 a_texCoord;\nvarying vec2 v_texCoords0;\nvarying vec2 v_texCoords1;\nvarying vec2 v_texCoords2;\nvarying vec2 v_texCoords3;\nvarying vec2 v_texCoords4;\n#define FBO_W " + FBO_W + ".0\n#define FBO_H " + FBO_H + ".0\nconst vec2 futher = vec2(3.2307692308 / FBO_W, 3.2307692308 / FBO_H );\nconst vec2 closer = vec2(1.3846153846 / FBO_W, 1.3846153846 / FBO_H );\nvoid main()\n{\nvec2 f = futher * dir;\nvec2 c = closer * dir;\nv_texCoords0 = a_texCoord - f;\nv_texCoords1 = a_texCoord - c;\nv_texCoords2 = a_texCoord;\nv_texCoords3 = a_texCoord + c;\nv_texCoords4 = a_texCoord + f;\ngl_Position = a_position;\n}\n";
        String fragmentShader = "#ifdef GL_ES\nprecision lowp float;\n#define MED mediump\n#else\n#define MED \n#endif\nuniform sampler2D u_texture;\nvarying MED vec2 v_texCoords0;\nvarying MED vec2 v_texCoords1;\nvarying MED vec2 v_texCoords2;\nvarying MED vec2 v_texCoords3;\nvarying MED vec2 v_texCoords4;\nconst float center = 0.2270270270;\nconst float close  = 0.3162162162;\nconst float far    = 0.0702702703;\nvoid main()\n{\t \ngl_FragColor" + rgb + " = far    * texture2D(u_texture, v_texCoords0)" + rgb + "\n\t      \t\t+ close  * texture2D(u_texture, v_texCoords1)" + rgb + "\n\t\t\t\t+ center * texture2D(u_texture, v_texCoords2)" + rgb + "\n\t\t\t\t+ close  * texture2D(u_texture, v_texCoords3)" + rgb + "\n\t\t\t\t+ far    * texture2D(u_texture, v_texCoords4)" + rgb + ";\n}\n";
        ShaderProgram.pedantic = false;
        ShaderProgram blurShader = new ShaderProgram(vertexShader, fragmentShader);
        if (!blurShader.isCompiled() && !(blurShader = new ShaderProgram("#version 330 core\n" + vertexShader, "#version 330 core\n" + fragmentShader)).isCompiled()) {
            Gdx.app.log("ERROR", blurShader.getLog());
        }
        return blurShader;
    }
}

