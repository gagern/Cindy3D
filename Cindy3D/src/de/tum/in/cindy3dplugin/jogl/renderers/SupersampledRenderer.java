package de.tum.in.cindy3dplugin.jogl.renderers;


import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import de.tum.in.cindy3dplugin.jogl.ModelViewerCamera;
import de.tum.in.cindy3dplugin.jogl.RenderHints;
import de.tum.in.cindy3dplugin.jogl.Util;
import de.tum.in.cindy3dplugin.jogl.lighting.LightManager;
import de.tum.in.cindy3dplugin.jogl.primitives.Scene;
import de.tum.in.cindy3dplugin.jogl.primitives.renderers.PrimitiveRendererFactory;

/**
 * Organizes primitive renderers and global rendering states. Additionally, the
 * supersampled renderer supports supersampling.
 */
public class SupersampledRenderer extends DefaultRenderer {
	/**
	 * Current width of regular, not supersampled, framebuffer / screen
	 */
	private int width;
	/**
	 * Current height of regular, not supersampled, framebuffer / screen
	 */
	private int height;
	/**
	 * Supersample factor that is requested. As this is only a request, it might
	 * not be fulfilled if this supersample factor is not supported / too high.
	 */
	private int requestedSuperSampleFactor = 2;
	/**
	 * Actual current supersample factor used for rendering. Might differ from
	 * {@link #requestedSuperSampleFactor} if the
	 * {@link #requestedSuperSampleFactor} is not supported.
	 */
	private int superSampleFactor;
	/**
	 * Color texture id. The color texture is part of the supersampled
	 * framebuffer.
	 */
	private int colorTexture = 0;
	/**
	 * Depth texture id. The depth texture is part of the supersampled
	 * framebuffer.
	 */
	private int depthBuffer = 0;
	/**
	 * Supersampled framebuffer id.
	 */
	private int framebuffer = 0;

	/**
	 * Creates a new supersampling renderer with the given parameters.
	 * 
	 * @param renderHints
	 *            render hints that should be fulfilled. Especially the
	 *            supersampling rate is used to define the
	 *            {@link #requestedSuperSampleFactor}.
	 * @param scene
	 *            scene to be rendered
	 * @param camera
	 *            camera object
	 * @param lightManager
	 *            light manager for scene lighting
	 * @param prf
	 *            factory that is used to create primitive renderers
	 */
	public SupersampledRenderer(RenderHints renderHints, Scene scene,
			ModelViewerCamera camera, LightManager lightManager,
			PrimitiveRendererFactory prf) {
		super(renderHints, scene, camera, lightManager, prf);
		this.requestedSuperSampleFactor = renderHints.getSamplingRate();
		width = height = 1;
	}

	/**
	 * @return currently used supersample factor
	 */
	public int getSuperSampleFactor() {
		return superSampleFactor;
	}
	
	/**
	 * Creates a new frame buffer and replaces the old. The new frame buffer
	 * size is specified by the given parameters.
	 * 
	 * @param drawable
	 *            GL drawable providing the GL handle
	 * @param width
	 *            width of the new framebuffer
	 * @param height
	 *            height of the new framebuffer
	 * @return <code>true</code> if the requested framebuffer was created
	 *         successfully, otherwise <code>false</code>
	 */
	private boolean createFramebuffer(GLAutoDrawable drawable, int width, int height) {
		GL2 gl = drawable.getGL().getGL2();

	    int[] textures = new int[1];
		gl.glGenTextures(1, textures, 0);
		gl.glBindTexture(GL2.GL_TEXTURE_2D, textures[0]);
		gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_GENERATE_MIPMAP, GL2.GL_TRUE);
		gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);
		gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR_MIPMAP_LINEAR);
		gl.glTexImage2D(GL2.GL_TEXTURE_2D, 0, GL2.GL_RGB, width, height, 0, GL2.GL_RGB, GL2.GL_UNSIGNED_BYTE, null);
		colorTexture = textures[0];
		
		int[] renderbuffers = new int[1];
		gl.glGenRenderbuffers(1, renderbuffers, 0);
		gl.glBindRenderbuffer(GL2.GL_RENDERBUFFER, renderbuffers[0]);
		gl.glRenderbufferStorage(GL2.GL_RENDERBUFFER, GL2.GL_DEPTH_COMPONENT, width, height);
		gl.glBindRenderbuffer(GL2.GL_RENDERBUFFER, 0);
		depthBuffer = renderbuffers[0];
		
		int[] framebuffers = new int[1];
		gl.glGenFramebuffers(1, framebuffers, 0);
		gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, framebuffers[0]);
		gl.glFramebufferTexture2D(GL2.GL_FRAMEBUFFER, GL2.GL_COLOR_ATTACHMENT0, GL2.GL_TEXTURE_2D, colorTexture, 0);
		gl.glFramebufferRenderbuffer(GL2.GL_FRAMEBUFFER, GL2.GL_DEPTH_ATTACHMENT, GL2.GL_RENDERBUFFER, depthBuffer);
		int status = gl.glCheckFramebufferStatus(GL2.GL_FRAMEBUFFER);
		if (status != GL2.GL_FRAMEBUFFER_COMPLETE) {
			Util.getLogger().info("Framebuffer creation failed.");
			destroyFramebuffer(drawable);
			return false;
		}
		gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, 0);		
		framebuffer = framebuffers[0];
		return true;
	}
	
	/**
	 * Destroys the current framebuffer.
	 * 
	 * @param drawable
	 *            GL drawable providing the GL handle
	 */
	private void destroyFramebuffer(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();
		
		int[] textures = { colorTexture };
		gl.glDeleteTextures(1, textures, 0);
		colorTexture = 0;
		
		int[] renderbuffers = { depthBuffer };
		gl.glDeleteRenderbuffers(1, renderbuffers, 0);
		depthBuffer = 0;
		
		int[] framebuffers = { framebuffer };
		gl.glDeleteFramebuffers(1, framebuffers, 0);
		framebuffer = 0;
	}

	/* (non-Javadoc)
	 * @see de.tum.in.cindy3dplugin.jogl.renderers.DefaultRenderer#dispose(javax.media.opengl.GLAutoDrawable)
	 */
	@Override
	public void dispose(GLAutoDrawable drawable) {
		destroyFramebuffer(drawable);
		super.dispose(drawable);
	}

	/* (non-Javadoc)
	 * @see de.tum.in.cindy3dplugin.jogl.renderers.DefaultRenderer#display(javax.media.opengl.GLAutoDrawable)
	 */
	@Override
	public void display(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();
		
		if (framebuffer == 0) {
			createFramebuffer(drawable, width, height);
		}
		
		// Render to framebuffer
		gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, framebuffer);
		gl.glViewport(0, 0, width*superSampleFactor, height*superSampleFactor);
		super.display(drawable);
		gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, 0);
		
		gl.glViewport(0,0, width, height);
		
		// Bring framebuffer to screen by drawing a textured quad		
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glPushMatrix();
		gl.glLoadIdentity();
		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glPushMatrix();
		gl.glLoadIdentity();
		
		gl.glDisable(GL2.GL_LIGHTING);
		gl.glDisable(GL2.GL_DEPTH_TEST);
		
		gl.glEnable(GL2.GL_TEXTURE_2D);
		gl.glBindTexture(GL2.GL_TEXTURE_2D, colorTexture);
		gl.glGenerateMipmap(GL2.GL_TEXTURE_2D);
		
		gl.glBegin(GL2.GL_QUADS);
		gl.glTexCoord2d(0, 0); gl.glVertex3d(-1, -1, 0);
		gl.glTexCoord2d(1, 0); gl.glVertex3d( 1, -1, 0);
		gl.glTexCoord2d(1, 1); gl.glVertex3d( 1,  1, 0);
		gl.glTexCoord2d(0, 1); gl.glVertex3d(-1,  1, 0);
		gl.glEnd();
		
		gl.glDisable(GL2.GL_TEXTURE_2D);
		
		gl.glEnable(GL2.GL_LIGHTING);
		gl.glEnable(GL2.GL_DEPTH_TEST);
		
		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glPopMatrix();
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glPopMatrix();
	}

	/* (non-Javadoc)
	 * @see de.tum.in.cindy3dplugin.jogl.renderers.DefaultRenderer#reshape(javax.media.opengl.GLAutoDrawable, int, int, int, int)
	 */
	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width,
			int height) {
		this.width = width;
		this.height = height;
		
		superSampleFactor = requestedSuperSampleFactor;
		if (framebuffer != 0) {
			destroyFramebuffer(drawable);
		}
		while (!createFramebuffer(drawable, width * superSampleFactor, height
				* superSampleFactor)
				&& superSampleFactor > 1) {
			--superSampleFactor;
		}
		super.reshape(drawable, x, y, width, height);
	}
}
