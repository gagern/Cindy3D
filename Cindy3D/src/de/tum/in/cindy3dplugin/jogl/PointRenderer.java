package de.tum.in.cindy3dplugin.jogl;

import java.nio.IntBuffer;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;

class PointRenderer extends PrimitiveRenderer<Point> {
	private ShaderProgram program = null;
	private int centerLoc;
	private int colorLoc;
	private int radiusLoc;
	private int modeLoc;
	
	private float renderMode;

	public PointRenderer() {
	}

	@Override
	public boolean init(GL gl) {
		GL2 gl2 = gl.getGL2();

		program = new ShaderProgram();
		ShaderCode vertexShader = Util.loadShader(GL2.GL_VERTEX_SHADER,
				"sphere.vert");
		if (!vertexShader.compile(gl2))
			return false;
		ShaderCode fragmentShader = Util.loadShader(GL2.GL_FRAGMENT_SHADER,
				"sphere.frag");
		if (!fragmentShader.compile(gl2))
			return false;

		if (!program.add(vertexShader))
			return false;
		if (!program.add(fragmentShader))
			return false;
		if (!program.link(gl.getGL2(), null))
			return false;

		centerLoc = gl2.glGetUniformLocation(program.program(), "sphereCenter");
		colorLoc = gl2.glGetUniformLocation(program.program(), "sphereColor");
		radiusLoc = gl2.glGetUniformLocation(program.program(), "sphereRadius");
		modeLoc = gl2.glGetUniformLocation(program.program(), "sphereMode");

		return true;
	}

	@Override
	public void dispose(GL gl) {
		if (program != null)
			program.destroy(gl.getGL2());
	}

	@Override
	public void postRender(JOGLRenderState jrs) {
		GL2 gl2 = jrs.gl.getGL2();
		gl2.glUseProgram(0);
	}

	@Override
	public void preRender(JOGLRenderState jrs) {
		GL2 gl2 = jrs.gl.getGL2();
		gl2.glUseProgram(program.program());
		
		IntBuffer intBuffer = IntBuffer.allocate(1);
		gl2.glGetIntegerv(GL2.GL_CULL_FACE_MODE, intBuffer);
		if (intBuffer.get(0) == GL2.GL_FRONT)
			renderMode = 0;
		else
			renderMode = 1;
	}

	@Override
	protected void render(JOGLRenderState jrs, Point point) {
		GL2 gl2 = jrs.gl.getGL2();
		gl2.glUniform3f(centerLoc, (float) point.x, (float) point.y,
				(float) point.z);
		gl2.glUniform4fv(colorLoc, 1, point.color.getComponents(null), 0);
		gl2.glUniform1f(radiusLoc, (float) point.size);
		
		// gl2.glFlush();
		gl2.glUniform1f(modeLoc, renderMode);
			
		gl2.glBegin(GL2.GL_QUADS);
		gl2.glVertex2f(-1, -1);
		gl2.glVertex2f(1, -1);
		gl2.glVertex2f(1, 1);
		gl2.glVertex2f(-1, 1);
		gl2.glEnd();
//		}
//		else {
//		
//		gl2.glUniform1f(modeLoc, 1);
//		gl2.glBegin(GL2.GL_QUADS);
//		gl2.glVertex2f(-1, -1);
//		gl2.glVertex2f(1, -1);
//		gl2.glVertex2f(1, 1);
//		gl2.glVertex2f(-1, 1);
//		gl2.glEnd();
//		}
	}
}