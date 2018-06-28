package de.tum.in.cindy3dplugin.jogl.primitives.renderers.shader;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import org.apache.commons.math.geometry.Vector3D;

import com.jogamp.opengl.util.glsl.ShaderProgram;

import de.tum.in.cindy3dplugin.jogl.Util;
import de.tum.in.cindy3dplugin.jogl.primitives.Sphere;
import de.tum.in.cindy3dplugin.jogl.primitives.renderers.PrimitiveRenderer;
import de.tum.in.cindy3dplugin.jogl.renderers.JOGLRenderState;
import de.tum.in.cindy3dplugin.jogl.renderers.JOGLRenderState.CullMode;

/**
 * Sphere renderer using shaders for rendering spheres. Using shaders results in per fragment shading and lighting. Spheres have a
 * perfectly smooth form as a quad as proxy bounding geometry is rendered
 * followed by per fragment raycasting.
 */
public class SphereRenderer extends PrimitiveRenderer<Sphere> {
	private ShaderProgram program = null;
	private int centerLoc;
	private int radiusLoc;
	private int modeLoc;
	
	private float renderMode;

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
		
		program = Util.loadShaderProgram(gl2, "sphere.vert", "sphere.frag");
		if (program == null) {
			return false;
		}

		centerLoc = gl2.glGetUniformLocation(program.program(), "sphereCenter");
		radiusLoc = gl2.glGetUniformLocation(program.program(), "sphereRadius");
		modeLoc = gl2.glGetUniformLocation(program.program(), "sphereMode");

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
	 * @see de.tum.in.cindy3dplugin.jogl.primitives.renderers.PrimitiveRenderer#postRender(de.tum.in.cindy3dplugin.jogl.renderers.JOGLRenderState)
	 */
	@Override
	protected void postRender(JOGLRenderState jrs) {
		GL2 gl2 = jrs.getGLHandle().getGL2();
		gl2.glUseProgram(0);
	}

	/* (non-Javadoc)
	 * @see de.tum.in.cindy3dplugin.jogl.primitives.renderers.PrimitiveRenderer#preRender(de.tum.in.cindy3dplugin.jogl.renderers.JOGLRenderState)
	 */
	@Override
	protected void preRender(JOGLRenderState jrs) {
		GL2 gl2 = jrs.getGLHandle().getGL2();
		gl2.glUseProgram(program.program());
		
		if (jrs.getCullMode() == CullMode.CULL_FRONT) {
			renderMode = 0;
		} else if (jrs.getCullMode() == CullMode.CULL_BACK) {
			renderMode = 1;
		} else if (jrs.getCullMode() == CullMode.CULL_NONE) {
			renderMode = 2;
		}
	}

	/* (non-Javadoc)
	 * @see de.tum.in.cindy3dplugin.jogl.primitives.renderers.PrimitiveRenderer#render(de.tum.in.cindy3dplugin.jogl.renderers.JOGLRenderState, de.tum.in.cindy3dplugin.jogl.primitives.Primitive)
	 */
	@Override
	protected void render(JOGLRenderState jrs, Sphere sphere) {
		GL2 gl2 = jrs.getGLHandle().getGL2();
		
		Vector3D viewSpaceCenter = Util.transformPoint(
				jrs.getCamera().getTransform(), sphere.getCenter());
		
		gl2.glUniform3fv(centerLoc, 1,
				Util.vectorToFloatArray(viewSpaceCenter), 0);
		gl2.glUniform1f(radiusLoc, (float) sphere.getRadius());
		
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
