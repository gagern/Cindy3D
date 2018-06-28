package de.tum.in.cindy3dplugin.jogl.primitives.renderers.fixedfunc;


import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import de.tum.in.cindy3dplugin.jogl.Util;
import de.tum.in.cindy3dplugin.jogl.primitives.Polygon;
import de.tum.in.cindy3dplugin.jogl.primitives.renderers.PrimitiveRenderer;
import de.tum.in.cindy3dplugin.jogl.renderers.JOGLRenderState;

/**
 * Polygon renderer using fixed function for rendering.
 */
public class PolygonRenderer extends PrimitiveRenderer<Polygon> {
	/* (non-Javadoc)
	 * @see de.tum.in.cindy3dplugin.jogl.primitives.renderers.PrimitiveRenderer#init(javax.media.opengl.GL)
	 */
	@Override
	public boolean init(GL gl) {
		return true;
	}

	/* (non-Javadoc)
	 * @see de.tum.in.cindy3dplugin.jogl.primitives.renderers.PrimitiveRenderer#dispose(javax.media.opengl.GL)
	 */
	@Override
	public void dispose(GL gl) {
	}

	/* (non-Javadoc)
	 * @see de.tum.in.cindy3dplugin.jogl.primitives.renderers.PrimitiveRenderer#preRender(de.tum.in.cindy3dplugin.jogl.renderers.JOGLRenderState)
	 */
	@Override
	protected void preRender(JOGLRenderState jrs) {
		GL2 gl = jrs.getGLHandle().getGL2();
		gl.glDisable(GL2.GL_CULL_FACE);
	}

	/* (non-Javadoc)
	 * @see de.tum.in.cindy3dplugin.jogl.primitives.renderers.PrimitiveRenderer#postRender(de.tum.in.cindy3dplugin.jogl.renderers.JOGLRenderState)
	 */
	@Override
	protected void postRender(JOGLRenderState jrs) {
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
}
