package de.tum.in.cindy3dplugin.jogl.primitives.renderers.shader;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import org.apache.commons.math.geometry.Vector3D;
import org.apache.commons.math.linear.RealMatrix;

import com.jogamp.opengl.util.glsl.ShaderProgram;

import de.tum.in.cindy3dplugin.jogl.Util;
import de.tum.in.cindy3dplugin.jogl.primitives.Line;
import de.tum.in.cindy3dplugin.jogl.primitives.renderers.LineRendererBase;
import de.tum.in.cindy3dplugin.jogl.renderers.JOGLRenderState;

/**
 * Line, ray and line segment renderer using shaders for rendering lines, rays
 * and line segments. Using shaders results in per fragment shading and
 * lighting. Each line is rendered as a tube with a defined radius. Each tube
 * has a perfectly smooth form as a oriented bounding box as proxy bounding
 * geometry is rendered followed by per fragment raycasting.
 */
public class LineRenderer extends LineRendererBase {
	/**
	 * Shader program for the line shaders
	 */
	private ShaderProgram program = null;
	/**
	 * Shader variable id for one end point of the line, ray or line segment
	 */
	private int originLoc;
	/**
	 * Shader variable id for the direction of the line, ray or line segment
	 */
	private int directionLoc;
	/**
	 * Shader variable id for the tube radius, representing the line, ray or
	 * line segment
	 */
	private int radiusLoc;
	/**
	 * Shader variable id for the line, ray or line segment length indicator
	 */
	private int lengthLoc;

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

		program = Util.loadShaderProgram(gl2, "cylinder.vert", "cylinder.frag");
		if (program == null) {
			return false;
		}

		originLoc = gl2
				.glGetUniformLocation(program.program(), "cylinderPoint");
		directionLoc = gl2.glGetUniformLocation(program.program(),
				"cylinderDirection");
		radiusLoc = gl2.glGetUniformLocation(program.program(),
				"cylinderRadius");
		lengthLoc = gl2.glGetUniformLocation(program.program(),
				"cylinderLength");

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
	}

	
	/* (non-Javadoc)
	 * @see de.tum.in.cindy3dplugin.jogl.primitives.renderers.PrimitiveRenderer#render(de.tum.in.cindy3dplugin.jogl.renderers.JOGLRenderState, de.tum.in.cindy3dplugin.jogl.primitives.Primitive)
	 */
	@Override
	protected void render(JOGLRenderState jrs, Line line) {
		GL2 gl2 = jrs.getGLHandle().getGL2();
		// Get the model view matrix
		RealMatrix modelView = jrs.getCamera().getTransform();

		// All computations are made in camera space, so first transform the two
		// points of the line into camera space by multiplying with the
		// modelview matrix
		Vector3D p1 = Util.transformPoint(modelView, line.getFirstPoint());
		Vector3D p2 = Util.transformPoint(modelView, line.getSecondPoint());

		// Compute orientation of the cylinder and its length, assuming a line
		// segment is about to be drawn
		Vector3D direction = p2.subtract(p1);

		double cylinderLength = -1;
		switch (line.getLineType()) {
		case SEGMENT:
			cylinderLength = direction.getNorm();
			break;
		case RAY:
			cylinderLength = 0;
			break;
		case LINE:
			cylinderLength = -1;
			break;
		}
		direction = direction.normalize();

		Endpoints endPoints = clipLineAtFrustum(jrs.getCamera(), p1, p2,
				line.getRadius(), line.getLineType());

		// If line is not visible at all, bail out
		if (endPoints.p1.isNaN()) {
			return;
		}

		// After shifting the end points of the ray/line to the maximal visible
		// positions, the size and orientation for the OBB is needed
		RealMatrix cylinder = buildOBBTransform(endPoints, line.getRadius());

		// Draw unit cube which is transformed into the OBB during vertex
		// processing on the GPU
		gl2.glUniform1f(lengthLoc, (float) cylinderLength);
		gl2.glUniform1f(radiusLoc, (float) line.getRadius());
		gl2.glUniform3f(originLoc, (float) p1.getX(), (float) p1.getY(),
				(float) p1.getZ());
		gl2.glUniform3f(directionLoc, (float) direction.getX(),
				(float) direction.getY(), (float) direction.getZ());
		
		gl2.glMatrixMode(GL2.GL_MODELVIEW);
		gl2.glPushMatrix();
		
		gl2.glLoadTransposeMatrixf(Util.matrixToFloatArray(cylinder), 0);

		// TODO: Stuff this into a vertex buffer
		gl2.glBegin(GL2.GL_QUADS);
			gl2.glVertex3d(-1, -1, -1);
			gl2.glVertex3d(-1, -1, 1);
			gl2.glVertex3d(-1, 1, 1);
			gl2.glVertex3d(-1, 1, -1);

			gl2.glVertex3d(1, -1, 1);
			gl2.glVertex3d(1, -1, -1);
			gl2.glVertex3d(1, 1, -1);
			gl2.glVertex3d(1, 1, 1);

			gl2.glVertex3d(1, -1, 1);
			gl2.glVertex3d(-1, -1, 1);
			gl2.glVertex3d(-1, -1, -1);
			gl2.glVertex3d(1, -1, -1);

			gl2.glVertex3d(-1, 1, 1);
			gl2.glVertex3d(1, 1, 1);
			gl2.glVertex3d(1, 1, -1);
			gl2.glVertex3d(-1, 1, -1);

			gl2.glVertex3d(1, 1, 1);
			gl2.glVertex3d(-1, 1, 1);
			gl2.glVertex3d(-1, -1, 1);
			gl2.glVertex3d(1, -1, 1);

			gl2.glVertex3d(-1, -1, -1);
			gl2.glVertex3d(-1, 1, -1);
			gl2.glVertex3d(1, 1, -1);
			gl2.glVertex3d(1, -1, -1);
		gl2.glEnd();
		
		gl2.glPopMatrix();
	}
}
