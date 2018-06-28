package de.tum.in.cindy3dplugin.jogl.primitives.renderers;

import java.util.Collection;

import com.jogamp.opengl.GL;
import de.tum.in.cindy3dplugin.jogl.primitives.Primitive;
import de.tum.in.cindy3dplugin.jogl.renderers.JOGLRenderState;

/**
 * Generic base renderer for a specific time of primitives.
 * 
 * @param <T>
 *            primitive type to be rendered by the renderer
 */
public abstract class PrimitiveRenderer<T extends Primitive> {
	/**
	 * Initializes renderer.
	 * 
	 * @param gl
	 *            GL handle
	 * @return <code>true</code> if initialization was successful,
	 *         <code>false</code> otherwise
	 */
	public abstract boolean init(GL gl);
	
	/**
	 * Reloads shader.
	 * 
	 * @param gl
	 *            GL handle
	 * @return <code>true</code if shaders could be reloaded successfully,
	 *         <code>false</code> otherwise
	 */
	public boolean reloadShaders(GL gl) {
		return true;
	}

	/**
	 * Disposes all class members which need to be disposes explicitly.
	 * 
	 * @param gl GL handle
	 */
	public abstract void dispose(GL gl);
	
	/**
	 * Method to prepare for rendering. Is called before calling
	 * {@link #render(JOGLRenderState, Primitive)} to set all states and
	 * parameters needed during rendering.
	 * 
	 * @param jrs
	 *            current render state
	 */
	protected abstract void preRender(JOGLRenderState jrs);

	/**
	 * Method to finalize rendering. Is called after calling
	 * {@link #render(JOGLRenderState, Primitive)} to reset states and clean up.
	 * 
	 * @param jrs
	 *            current render state
	 */
	protected abstract void postRender(JOGLRenderState jrs);

	/**
	 * Renders a specific primitive.
	 * 
	 * @param jrs
	 *            current render state
	 * @param primitive
	 *            primitive to be rendered
	 */
	protected abstract void render(JOGLRenderState jrs, T primitive);

	/**
	 * Renders a whole set of primitives of the same type. This method renders
	 * all primitives after setting all needed render states and parameters.
	 * 
	 * @param jrs
	 *            current render state
	 * @param c
	 *            collection of primitives to be rendered
	 */
	public void render(JOGLRenderState jrs, Collection<T> c) {
		if (c.isEmpty()) {
			return;
		}

		preRender(jrs);
		for (T primitive : c) {
			try {
				if (primitive.isOpaque() == jrs.renderOpaque()) {
					primitive.getMaterial().setGLState(jrs.getGLHandle());
					render(jrs, primitive);
				}
			} catch (Exception e) {

			}
		}
		postRender(jrs);
	}
}
