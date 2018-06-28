package de.tum.in.cindy3dplugin.jogl.primitives.renderers.shader;


import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.util.glsl.ShaderProgram;

import de.tum.in.cindy3dplugin.jogl.Util;
import de.tum.in.cindy3dplugin.jogl.primitives.Polygon;
import de.tum.in.cindy3dplugin.jogl.primitives.renderers.PrimitiveRenderer;
import de.tum.in.cindy3dplugin.jogl.renderers.JOGLRenderState;

/**
 * Polygon renderer using shaders for rendering polygon. Using shaders results in
 * per fragment shading and lighting.
 */
public class PolygonRenderer extends PrimitiveRenderer<Polygon> {
	/**
	 * Shader program for the circle shaders
	 */
	private ShaderProgram program = null;

	/* (non-Javadoc)
	 * @see de.tum.in.cindy3dplugin.jogl.primitives.renderers.PrimitiveRenderer#init(javax.media.opengl.GL)
	 */
	@Override
	public boolean init(GL gl) {
		return reloadShaders(gl);
	}
	
	/* (non-Javadoc)
	 * @see de.tum.in.cindy3dplugin.jogl.primitives.renderers.PrimitiveRenderer#reloadShaders(javax.media.opengl.GL)
	 */
	@Override
	public boolean reloadShaders(GL gl) {
		GL2 gl2 = gl.getGL2();
		
		if (program != null) {
			program.destroy(gl2);
		}
		
		program = Util.loadShaderProgram(gl2, "polygon.vert", "polygon.frag");
		if (program == null) {
			return false;
		}

		return true;
	}
	
	/* (non-Javadoc)
	 * @see de.tum.in.cindy3dplugin.jogl.primitives.renderers.PrimitiveRenderer#dispose(javax.media.opengl.GL)
	 */
	@Override
	public void dispose(GL gl) {
		if (program != null) {
			program.destroy(gl.getGL2());
		}
	}
	
	/* (non-Javadoc)
	 * @see de.tum.in.cindy3dplugin.jogl.primitives.renderers.PrimitiveRenderer#preRender(de.tum.in.cindy3dplugin.jogl.renderers.JOGLRenderState)
	 */
	@Override
	protected void preRender(JOGLRenderState jrs) {
		GL2 gl2 = jrs.getGLHandle().getGL2();
		gl2.glUseProgram(program.program());
	}

	/* (non-Javadoc)
	 * @see de.tum.in.cindy3dplugin.jogl.primitives.renderers.PrimitiveRenderer#render(de.tum.in.cindy3dplugin.jogl.renderers.JOGLRenderState, de.tum.in.cindy3dplugin.jogl.primitives.Primitive)
	 */
	@Override
	protected void render(JOGLRenderState jrs, Polygon polygon) {
		GL2 gl2 = jrs.getGLHandle().getGL2();

		gl2.glBegin(GL2.GL_POLYGON);
		for (int i = 0; i < polygon.getPositions().length; ++i) {
			gl2.glNormal3dv(Util.vectorToDoubleArray(polygon.getNormals()[i]), 0);
			gl2.glVertex3dv(Util.vectorToDoubleArray(polygon.getPositions()[i]), 0);
		}
		gl2.glEnd();
	}

	/* (non-Javadoc)
	 * @see de.tum.in.cindy3dplugin.jogl.primitives.renderers.PrimitiveRenderer#postRender(de.tum.in.cindy3dplugin.jogl.renderers.JOGLRenderState)
	 */
	@Override
	protected void postRender(JOGLRenderState jrs) {
		GL2 gl2 = jrs.getGLHandle().getGL2();
		gl2.glUseProgram(0);
	}
}
