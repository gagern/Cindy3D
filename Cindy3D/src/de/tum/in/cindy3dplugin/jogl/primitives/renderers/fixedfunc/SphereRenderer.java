package de.tum.in.cindy3dplugin.jogl.primitives.renderers.fixedfunc;


import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import de.tum.in.cindy3dplugin.jogl.Util;
import de.tum.in.cindy3dplugin.jogl.primitives.Sphere;
import de.tum.in.cindy3dplugin.jogl.primitives.renderers.PrimitiveRenderer;
import de.tum.in.cindy3dplugin.jogl.renderers.JOGLRenderState;
import de.tum.in.cindy3dplugin.jogl.renderers.JOGLRenderState.CullMode;

/**
 * Sphere renderer using fixed function for rendering.
 */
public class SphereRenderer extends PrimitiveRenderer<Sphere> {
	/**
	 * Number of level of detail meshes used for rendering
	 */
	private static final int LOD_COUNT = 8;
	/**
	 * Meshes, each with a different level of detail
	 */
	private LODMesh[] meshes = new LODMesh[LOD_COUNT];

	/**
	 * Creates a mesh representing a sphere. The sphere's origin is (0, 0, 0)
	 * and its radius 1.
	 * 
	 * @param gl
	 *            GL handle
	 * @param stacks
	 *            number of subdivisions along the z-axis (similar to lines of
	 *            latitude)
	 * @param slices
	 *            number of subdivisions around the z-axis (similar to lines of
	 *            longitude)
	 * @return created mesh
	 */
	private LODMesh createMesh(GL gl, int stacks, int slices) {
		GL2 gl2 = gl.getGL2();
		
		int vertexCount = 2 + slices * (stacks - 1);
		int faceCount = 2 * slices + // stacks at poles, triangles
				(stacks - 2) * slices * 2; // inner stacks loops, quads (2
										   // triangles)
		
		LODMesh mesh = new LODMesh(3, vertexCount, faceCount, true);
		
		/*
		 * Generate vertices
		 */
		double[] vertex = new double[3];
		
		vertex[0] = vertex[1] = 0;
		vertex[2] = -1;
		mesh.putVertex(vertex, vertex);
		
		for (int stack = 1; stack < stacks; ++stack) {
			double latitude = (((double) stack) / stacks - 0.5) * Math.PI;
			for (int slice = 0; slice < slices; ++slice) {
				double longitude = ((double) slice) / slices * Math.PI * 2;
				vertex[0] = Math.cos(longitude)*Math.cos(latitude);
				vertex[1] = Math.sin(longitude)*Math.cos(latitude);
				vertex[2] = Math.sin(latitude);
				mesh.putVertex(vertex, vertex);
				//mesh.putVertex(vertex);
			}
		}
		
		vertex[0] = vertex[1] = 0;
		vertex[2] = 1;
		mesh.putVertex(vertex, vertex);
	
		/*
		 * Generate indices
		 */
		int stackOffset = 0;		// First vertex of current stack
		int nextStackOffset = 1;	// First vertex of next stack
		
		// South pole
		for (int slice = 0; slice < slices; ++slice) {
			mesh.putFace(
					stackOffset,
					nextStackOffset + (slice + 1) % slices,
					nextStackOffset + slice);
		}
		
		for (int stack = 1; stack < stacks - 1; ++stack) {
			stackOffset = nextStackOffset;
			nextStackOffset += slices;
			for (int slice = 0; slice < slices; ++slice) {
				mesh.putFace(
						stackOffset     + slice,
						nextStackOffset + (slice + 1) % slices,
						nextStackOffset + slice);
				mesh.putFace(
						stackOffset     + slice,
						stackOffset     + (slice + 1) % slices,
						nextStackOffset + (slice + 1) % slices);
			}
		}
		
		// North pole
		for (int slice = 0; slice < slices; ++slice) {
			mesh.putFace(
					nextStackOffset + slices,
					nextStackOffset + slice,
					nextStackOffset + (slice + 1) % slices);
		}
	
		mesh.finish(gl2);
		return mesh;
	}

	/* (non-Javadoc)
	 * @see de.tum.in.cindy3dplugin.jogl.primitives.renderers.PrimitiveRenderer#init(javax.media.opengl.GL)
	 */
	@Override
	public boolean init(GL gl) {
		for (int lod = 0; lod < LOD_COUNT; ++lod) {
			int stacks = lod + 2;
			int slices = 2 * stacks;
			
			meshes[lod] = createMesh(gl, stacks, slices);
		}
		
		return true;
	}

	/* (non-Javadoc)
	 * @see de.tum.in.cindy3dplugin.jogl.primitives.renderers.PrimitiveRenderer#dispose(javax.media.opengl.GL)
	 */
	@Override
	public void dispose(GL gl) {
		GL2 gl2 = gl.getGL2();
		for (LODMesh mesh : meshes) {
			mesh.dispose(gl2);
		}
	}

	/* (non-Javadoc)
	 * @see de.tum.in.cindy3dplugin.jogl.primitives.renderers.PrimitiveRenderer#preRender(de.tum.in.cindy3dplugin.jogl.renderers.JOGLRenderState)
	 */
	@Override
	protected void preRender(JOGLRenderState jrs) {
		GL2 gl2 = jrs.getGLHandle().getGL2();
		gl2.glEnable(GL2.GL_NORMALIZE);
		gl2.glEnableClientState(GL2.GL_VERTEX_ARRAY);
		gl2.glEnableClientState(GL2.GL_NORMAL_ARRAY);
		if (jrs.getCullMode() == CullMode.CULL_FRONT) {
			gl2.glEnable(GL2.GL_CULL_FACE);
			gl2.glCullFace(GL2.GL_FRONT);
		} else if (jrs.getCullMode() == CullMode.CULL_BACK) {
			gl2.glEnable(GL2.GL_CULL_FACE);
			gl2.glCullFace(GL2.GL_BACK);
		}
	}

	/* (non-Javadoc)
	 * @see de.tum.in.cindy3dplugin.jogl.primitives.renderers.PrimitiveRenderer#postRender(de.tum.in.cindy3dplugin.jogl.renderers.JOGLRenderState)
	 */
	@Override
	protected void postRender(JOGLRenderState jrs) {
		GL2 gl2 = jrs.getGLHandle().getGL2();
		gl2.glDisable(GL2.GL_NORMALIZE);
		gl2.glDisableClientState(GL2.GL_VERTEX_ARRAY);
		gl2.glDisableClientState(GL2.GL_NORMAL_ARRAY);
	}

	/* (non-Javadoc)
	 * @see de.tum.in.cindy3dplugin.jogl.primitives.renderers.PrimitiveRenderer#render(de.tum.in.cindy3dplugin.jogl.renderers.JOGLRenderState, de.tum.in.cindy3dplugin.jogl.primitives.Primitive)
	 */
	@Override
	protected void render(JOGLRenderState jrs, Sphere sphere) {
		GL2 gl2 = jrs.getGLHandle().getGL2();

		double cameraSpaceZ = Util.transformPoint(jrs.getCamera().getTransform(),
				sphere.getCenter()).getZ()
				+ sphere.getRadius();
		double allowedWorldSpaceError = jrs.getCamera().getWorldSpaceError(
				jrs.getRenderHints().getAllowedScreenSpaceError(), cameraSpaceZ);
		LODMesh mesh = meshes[LOD_COUNT - 1];
		int lod;
		for (lod = 0; lod < LOD_COUNT; ++lod) {
			if (meshes[lod].isSufficient(sphere.getRadius(),
					allowedWorldSpaceError)) {
				mesh = meshes[lod];
				break;
			}
		}

		gl2.glMatrixMode(GL2.GL_MODELVIEW);
		gl2.glPushMatrix();
		gl2.glTranslated(sphere.getCenter().getX(), sphere.getCenter().getY(),
				sphere.getCenter().getZ());
		gl2.glScaled(sphere.getRadius(), sphere.getRadius(), sphere.getRadius());

		mesh.render(gl2);

		gl2.glPopMatrix();
	}
}
