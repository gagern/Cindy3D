package de.tum.in.cindy3dplugin.jogl.primitives.renderers;

import java.util.HashMap;
import java.util.Iterator;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import de.tum.in.cindy3dplugin.jogl.primitives.Mesh;
import de.tum.in.cindy3dplugin.jogl.renderers.JOGLRenderState;

/**
 * Base class for different kinds of mesh renderers.
 * 
 * This intermediate class contains methods needed by more than one specialized
 * mesh renderer.
 */
public abstract class MeshRendererBase extends PrimitiveRenderer<Mesh> {
	/**
	 * Hash map containing all rendering buffers from all meshes rendered so far.
	 */
	protected HashMap<Integer, MeshBuffer> meshBuffers = null;

	/* (non-Javadoc)
	 * @see de.tum.in.cindy3dplugin.jogl.primitives.renderers.PrimitiveRenderer#init(javax.media.opengl.GL)
	 */
	@Override
	public boolean init(GL gl) {
		meshBuffers = new HashMap<Integer, MeshBuffer>();
		return true;
	}
	
	/* (non-Javadoc)
	 * @see de.tum.in.cindy3dplugin.jogl.primitives.renderers.PrimitiveRenderer#dispose(javax.media.opengl.GL)
	 */
	@Override
	public void dispose(GL gl) { Iterator<MeshBuffer> it = meshBuffers.values().iterator();

		MeshBuffer mb;
		while (it.hasNext()) {
			mb = it.next();
			mb.dispose(gl.getGL2());
		}
	}
	
	/* (non-Javadoc)
	 * @see de.tum.in.cindy3dplugin.jogl.primitives.renderers.PrimitiveRenderer#preRender(de.tum.in.cindy3dplugin.jogl.renderers.JOGLRenderState)
	 */
	@Override
	protected void preRender(JOGLRenderState jrs) {
		GL2 gl2 = jrs.getGLHandle().getGL2();
		gl2.glDisable(GL2.GL_CULL_FACE);
		gl2.glEnableClientState(GL2.GL_VERTEX_ARRAY);
		gl2.glEnableClientState(GL2.GL_NORMAL_ARRAY);
	}

	/* (non-Javadoc)
	 * @see de.tum.in.cindy3dplugin.jogl.primitives.renderers.PrimitiveRenderer#postRender(de.tum.in.cindy3dplugin.jogl.renderers.JOGLRenderState)
	 */
	@Override
	protected void postRender(JOGLRenderState jrs) {
		GL2 gl2 = jrs.getGLHandle().getGL2();
		gl2.glDisableClientState(GL2.GL_VERTEX_ARRAY);
		gl2.glDisableClientState(GL2.GL_NORMAL_ARRAY);
	}

	/**
	 * Returns all rendering buffers belonging to a mesh. The buffer objects are
	 * cached so a new object is only created if it requested for a new mesh.
	 * 
	 * @param gl
	 *            GL handle
	 * @param m
	 *            mesh
	 * @return mesh rendering buffer object holding buffers representing the
	 *         specified mesh <code>m</code>
	 */
	protected MeshBuffer getMeshBuffer(GL gl, Mesh m) {
		MeshBuffer mb;
		mb = meshBuffers.get(m.getIdentifier());
		if (mb == null) {
			mb = new MeshBuffer(gl.getGL2(), m);
			meshBuffers.put(m.getIdentifier(), mb);
		}
		return mb;
	}
	
	/* (non-Javadoc)
	 * @see de.tum.in.cindy3dplugin.jogl.primitives.renderers.PrimitiveRenderer#render(de.tum.in.cindy3dplugin.jogl.renderers.JOGLRenderState, de.tum.in.cindy3dplugin.jogl.primitives.Primitive)
	 */
	@Override
	protected void render(JOGLRenderState jrs, Mesh m) {
		getMeshBuffer(jrs.getGLHandle(), m).render(jrs.getGLHandle());
	}
}
