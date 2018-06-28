package de.tum.in.cindy3dplugin.jogl.renderers;

import com.jogamp.opengl.GLEventListener;
import de.tum.in.cindy3dplugin.jogl.ModelViewerCamera;
import de.tum.in.cindy3dplugin.jogl.RenderHints;
import de.tum.in.cindy3dplugin.jogl.lighting.LightManager;
import de.tum.in.cindy3dplugin.jogl.primitives.Scene;

/**
 * Base class for rendering a scene with a specific light and camera setting
 * based on JOGL and OpenGL.
 */
public abstract class JOGLRenderer implements GLEventListener {
	/**
	 * Scene that is rendered
	 */
	protected Scene scene;
	/**
	 * Camera setting used for rendering
	 */
	protected ModelViewerCamera camera;
	/**
	 * Light manager containing the current light setting
	 */
	protected LightManager lightManager;
	/**
	 * Render hints which ought to be fulfilled
	 */
	protected RenderHints renderHints;
	
	/**
	 * Creates a new renderer object using the given parameters.
	 * 
	 * @param renderHints
	 *            render hints that should be applied.
	 * @param scene
	 *            scene which should be rendered
	 * @param camera
	 *            camera used for rendering
	 * @param lightManager
	 *            light manager containing the light setting
	 */
	public JOGLRenderer(RenderHints renderHints, Scene scene,
			ModelViewerCamera camera, LightManager lightManager) {
		this.renderHints = renderHints;
		this.scene = scene;
		this.camera = camera;
		this.lightManager = lightManager;
	}
	
	/**
	 * @return requested render hints
	 */
	public RenderHints getRenderHints() {
		return renderHints;
	}

	/**
	 * Sets the scene to be rendered.
	 * @param scene scene to be rendered.
	 */
	public void setScene(Scene scene) {
		this.scene = scene;
	}

	/**
	 * @return scene currently used for rendering
	 */
	public Scene getScene() {
		return scene;
	}

	/**
	 * Sets camera object used for rendering.
	 * @param camera new camera object
	 */
	public void setCamera(ModelViewerCamera camera) {
		this.camera = camera;
	}

	/**
	 * @return camera currently used for rendering
	 */
	public ModelViewerCamera getCamera() {
		return camera;
	}
	
	/**
	 * @return currently used light manager
	 */
	public LightManager getLightManager() {
		return lightManager;
	}
}
