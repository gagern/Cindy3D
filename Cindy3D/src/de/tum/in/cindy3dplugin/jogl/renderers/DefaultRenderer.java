package de.tum.in.cindy3dplugin.jogl.renderers;

import java.util.logging.Level;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLException;
import de.tum.in.cindy3dplugin.jogl.ModelViewerCamera;
import de.tum.in.cindy3dplugin.jogl.RenderHints;
import de.tum.in.cindy3dplugin.jogl.Util;
import de.tum.in.cindy3dplugin.jogl.lighting.LightManager;
import de.tum.in.cindy3dplugin.jogl.primitives.Circle;
import de.tum.in.cindy3dplugin.jogl.primitives.Line;
import de.tum.in.cindy3dplugin.jogl.primitives.Mesh;
import de.tum.in.cindy3dplugin.jogl.primitives.Sphere;
import de.tum.in.cindy3dplugin.jogl.primitives.Polygon;
import de.tum.in.cindy3dplugin.jogl.primitives.Scene;
import de.tum.in.cindy3dplugin.jogl.primitives.renderers.PrimitiveRenderer;
import de.tum.in.cindy3dplugin.jogl.primitives.renderers.PrimitiveRendererFactory;
import de.tum.in.cindy3dplugin.jogl.renderers.JOGLRenderState.CullMode;

/**
 * Organizes primitive renderers and global rendering states. The default
 * renderer does not support any kind of multi- or supersampling.
 */
public class DefaultRenderer extends JOGLRenderer {
	/**
	 * Sphere rendeer
	 */
	private PrimitiveRenderer<Sphere> sphereRenderer;
	/**
	 * Circle renderer
	 */
	private PrimitiveRenderer<Circle> circleRenderer;
	/**
	 * Line renderer
	 */
	private PrimitiveRenderer<Line> lineRenderer;
	/**
	 * Polygon renderer
	 */
	private PrimitiveRenderer<Polygon> polygonRenderer;
	/**
	 * mesh Renderer
	 */
	private PrimitiveRenderer<Mesh> meshRenderer;

	/**
	 * Creates a new renderer with the given parameters.
	 * 
	 * @param renderHints
	 *            render hints that should be fulfilled. Only the screen space
	 *            error component is used as sampling is disabled in the default
	 *            renderer.
	 * @param scene
	 *            scene to be rendered
	 * @param camera
	 *            camera object
	 * @param lightManager
	 *            light manager for scene lighting
	 * @param prf
	 *            factory that is used to create primitive renderers
	 */
	public DefaultRenderer(RenderHints renderHints, Scene scene,
			ModelViewerCamera camera, LightManager lightManager,
			PrimitiveRendererFactory prf) {
		super(renderHints, scene, camera, lightManager);
		sphereRenderer = prf.createSphereRenderer();
		circleRenderer = prf.createCircleRenderer();
		lineRenderer = prf.createLineRenderer();
		polygonRenderer = prf.createPolygonRenderer();
		meshRenderer = prf.createMeshRenderer();
	}

	/* (non-Javadoc)
	 * @see javax.media.opengl.GLEventListener#init(javax.media.opengl.GLAutoDrawable)
	 */
	@Override
	public void init(GLAutoDrawable drawable) {
		Util.getLogger().info("init()");
		
		try {
			// drawable.setGL(new DebugGL2(drawable.getGL().getGL2()));
			GL2 gl = drawable.getGL().getGL2();
			
			Util.getLogger().info("Chosen caps: " + drawable.getChosenGLCapabilities());
			Util.getLogger().info("GL_VENDOR: " + gl.glGetString(GL2.GL_VENDOR));
			Util.getLogger().info("GL_RENDERER: " + gl.glGetString(GL2.GL_RENDERER));
			Util.getLogger().info("GL_VERSION: " + gl.glGetString(GL2.GL_VERSION));
			
			gl.glMatrixMode(GL2.GL_PROJECTION);
			gl.glLoadIdentity();
			gl.glMatrixMode(GL2.GL_MODELVIEW);
			gl.glLoadIdentity();
	
			gl.glEnable(GL2.GL_DEPTH_TEST);
			gl.glDepthFunc(GL2.GL_LEQUAL);
			gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
			
			gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_SPECULAR, new float[] { 0.5f,
					0.5f, 0.5f, 0.5f }, 0);
			gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_AMBIENT, new float[] { 0.2f,
					0.2f, 0.2f, 0.2f }, 0);
			gl.glMaterialf(GL2.GL_FRONT_AND_BACK, GL2.GL_SHININESS, 60.0f);
	
			gl.glLightModeli(GL2.GL_LIGHT_MODEL_TWO_SIDE, GL2.GL_TRUE);
			gl.glEnable(GL2.GL_LIGHTING);
			
			if (!sphereRenderer.init(gl))
				Util.getLogger().severe("Point renderer initialization failed");
			if (!circleRenderer.init(gl))
				Util.getLogger().severe("Circle renderer initialization failed");
			if (!lineRenderer.init(gl))
				Util.getLogger().severe("Line renderer initialization failed");
			if (!polygonRenderer.init(gl))
				Util.getLogger().severe("Polygon renderer initialization failed");
			if (!meshRenderer.init(gl))
				Util.getLogger().severe("Mesh renderer initialization failed");
		} catch (GLException e) {
			Util.getLogger().log(Level.SEVERE, e.toString(), e);
		}
	}

	/* (non-Javadoc)
	 * @see javax.media.opengl.GLEventListener#reshape(javax.media.opengl.GLAutoDrawable, int, int, int, int)
	 */
	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width,
			int height) {
		Util.getLogger().info("reshape(" + x + "," + y + "," + width + "," + height + ")");
		GL2 gl = drawable.getGL().getGL2();
		camera.setPerspective(camera.getFieldOfView(), width, height, camera
				.getZNear(), camera.getZFar());
		
		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glLoadMatrixf(Util.matrixToFloatArrayTransposed(camera
				.getPerspectiveTransform()), 0);
		
		display(drawable);
	}
	
	/**
	 * Renders all primtives that the scene contains
	 * 
	 * @param jrs
	 *            current render state
	 */
	private void renderPrimitives(JOGLRenderState jrs) {
		if (!jrs.renderOpaque()) {
			jrs.setCullMode(CullMode.CULL_FRONT);
			sphereRenderer.render(jrs, scene.getSpheres());
			jrs.setCullMode(CullMode.CULL_NONE);
		}
		
		circleRenderer.render(jrs, scene.getCircles());
		lineRenderer.render(jrs, scene.getLines());
		polygonRenderer.render(jrs, scene.getPolygons());
		meshRenderer.render(jrs, scene.getMeshes());

		jrs.setCullMode(CullMode.CULL_BACK);
		sphereRenderer.render(jrs, scene.getSpheres());
		jrs.setCullMode(CullMode.CULL_NONE);
	}

	/* (non-Javadoc)
	 * @see javax.media.opengl.GLEventListener#display(javax.media.opengl.GLAutoDrawable)
	 */
	@Override
	public void display(GLAutoDrawable drawable) {
//		Util.getLogger().info("display()");

		GL2 gl = drawable.getGL().getGL2();
		float[] backgroundColor = new float[4];
		scene.getBackgroundColor().getRGBComponents(backgroundColor);
		gl.glClearColor(backgroundColor[0], backgroundColor[1],
				backgroundColor[2], backgroundColor[3]);
		gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
		
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glLoadMatrixf(
				Util.matrixToFloatArrayTransposed(camera.getTransform()), 0);
			
		JOGLRenderState jrs = new JOGLRenderState(gl, camera, true,
				CullMode.CULL_NONE, renderHints);

		if (lightManager.hasLightSettingChanged()) {
			Util.setShaderLightFillIn(lightManager.getShaderFillIn());
			
			if (!sphereRenderer.reloadShaders(gl))
				Util.getLogger().severe("Point renderer shader loading failed");
			if (!circleRenderer.reloadShaders(gl))
				Util.getLogger().severe("Circle renderer shader loading failed");
			if (!lineRenderer.reloadShaders(gl))
				Util.getLogger().severe("Line renderer shader loading failed");
			if (!polygonRenderer.reloadShaders(gl))
				Util.getLogger().severe("Polygon renderer shader loading failed");
			if (!meshRenderer.reloadShaders(gl))
				Util.getLogger().severe("Mesh renderer shader loading failed");
			
			lightManager.resetLightSettingChanged();
		}
		
		lightManager.setGLState(gl);
		
		gl.glEnable(GL2.GL_BLEND);
		gl.glDepthMask(false);
		jrs.setRenderOpaque(false);
		renderPrimitives(jrs);
		renderPrimitives(jrs);
		gl.glDepthMask(true);
		gl.glDisable(GL2.GL_BLEND);
		
		jrs.setRenderOpaque(true);
		renderPrimitives(jrs);
		
		gl.glEnable(GL2.GL_BLEND);
		jrs.setRenderOpaque(false);
		renderPrimitives(jrs);
		gl.glDisable(GL2.GL_BLEND);
	}

	/* (non-Javadoc)
	 * @see javax.media.opengl.GLEventListener#dispose(javax.media.opengl.GLAutoDrawable)
	 */
	@Override
	public void dispose(GLAutoDrawable drawable) {
		sphereRenderer.dispose(drawable.getGL());
		circleRenderer.dispose(drawable.getGL());
		lineRenderer.dispose(drawable.getGL());
		polygonRenderer.dispose(drawable.getGL());
		meshRenderer.dispose(drawable.getGL());
	}
}
