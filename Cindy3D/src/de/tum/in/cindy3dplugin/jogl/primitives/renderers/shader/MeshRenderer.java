package de.tum.in.cindy3dplugin.jogl.primitives.renderers.shader;


import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.util.glsl.ShaderProgram;

import de.tum.in.cindy3dplugin.jogl.Util;
import de.tum.in.cindy3dplugin.jogl.primitives.renderers.MeshRendererBase;
import de.tum.in.cindy3dplugin.jogl.renderers.JOGLRenderState;

/**
 * Mesh renderer using shaders for rendering meshes. Using shaders results in
 * per fragment shading and lighting.
 */
public class MeshRenderer extends MeshRendererBase {
	/**
	 * Shader program for the circle shaders
	 */
	private ShaderProgram program = null;

	/* (non-Javadoc)
	 * @see de.tum.in.cindy3dplugin.jogl.primitives.renderers.MeshRendererBase#init(javax.media.opengl.GL)
	 */
	@Override
	public boolean init(GL gl) {
		return super.init(gl) && reloadShaders(gl);
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
	 * @see de.tum.in.cindy3dplugin.jogl.primitives.renderers.MeshRendererBase#dispose(javax.media.opengl.GL)
	 */
	@Override
	public void dispose(GL gl) {
		super.dispose(gl);
		if (program != null) {
			program.destroy(gl.getGL2());
		}
	}

	/* (non-Javadoc)
	 * @see de.tum.in.cindy3dplugin.jogl.primitives.renderers.MeshRendererBase#preRender(de.tum.in.cindy3dplugin.jogl.renderers.JOGLRenderState)
	 */
	@Override
	protected void preRender(JOGLRenderState jrs) {
		super.preRender(jrs);
		jrs.getGLHandle().getGL2().glUseProgram(program.program());
	}

	/* (non-Javadoc)
	 * @see de.tum.in.cindy3dplugin.jogl.primitives.renderers.MeshRendererBase#postRender(de.tum.in.cindy3dplugin.jogl.renderers.JOGLRenderState)
	 */
	@Override
	protected void postRender(JOGLRenderState jrs) {
		super.postRender(jrs);
		jrs.getGLHandle().getGL2().glUseProgram(0);
	}
}
