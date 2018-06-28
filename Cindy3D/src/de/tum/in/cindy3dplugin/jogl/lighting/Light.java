package de.tum.in.cindy3dplugin.jogl.lighting;

import java.awt.Color;

import com.jogamp.opengl.GL2;
import de.tum.in.cindy3dplugin.LightModificationInfo.LightFrame;
import de.tum.in.cindy3dplugin.LightModificationInfo.LightType;

/**
 * Base class representing a light with all the general properties.
 */
public abstract class Light {
	/**
	 * Ambient light color, default ambient color is black.
	 */
	private Color ambientColor = new Color(0.0f, 0.0f, 0.0f);
	/**
	 * Diffuse light color, default diffuse color is white.
	 */
	private Color diffuseColor = new Color(1.0f, 1.0f, 1.0f);
	/**
	 * Specular light color, default specular color is white.
	 */
	private Color specularColor = new Color(1.0f, 1.0f, 1.0f);
	/**
	 * Frame of reference for this light.
	 */
	protected LightFrame frame = LightFrame.CAMERA;
	/**
	 * Indicates if light is enabled
	 */
	protected boolean enabled = false;
	
	/**
	 * Sets all parameters and states needed for rendering.
	 * 
	 * Enables light for rendering and sets the light colors and additional,
	 * light type specific properties and states needed during shading.
	 * 
	 * @param gl GL handle
	 * @param light
	 *            light index. <code>light</code> specifies the light index
	 *            ranging from 0 (inclusive) to
	 *            {@value de.tum.in.cindy3dplugin.Cindy3DViewer#MAX_LIGHTS}
	 *            (exclusive). This light is bound to the specified light index
	 *            in the rendering pipeline.
	 */
	public void setGLState(GL2 gl, int light) {
		if (enabled) {
			gl.glEnable(light);
		} else {
			gl.glDisable(light);
		}

		gl.glLightfv(light, GL2.GL_AMBIENT,
				ambientColor.getComponents(null), 0);

		gl.glLightfv(light, GL2.GL_DIFFUSE,
				diffuseColor.getComponents(null), 0);

		gl.glLightfv(light, GL2.GL_SPECULAR,
				specularColor.getComponents(null), 0);
	}
	
	/**
	 * Sets the ambient color of the light.
	 * @param ambient new ambient color
	 */
	public void setAmbientColor(Color ambient) {
		ambientColor = ambient;
	}
	
	/**
	 * Sets the diffuse color of the light.
	 * @param diffuse new diffuse color
	 */
	public void setDiffuseColor(Color diffuse) {
		diffuseColor = diffuse;
	}
	
	/**
	 * Sets the specular color of the light.
	 * @param specular new specular color
	 */
	public void setSpecularColor(Color specular) {
		specularColor = specular;
	}
	
	/**
	 * Sets the frame of reference for the light.
	 * @param frame new frame of reference
	 */
	public void setLightFrame(LightFrame frame) {
		this.frame = frame;
	}

	/**
	 * Enables or disables light.
	 * @param enabled new enable state
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	/**
	 * @return <code>true</code> if light is enabled, <code>false</code>
	 *         otherwise
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * Method to build up a string that is inserted into GLSL shader code.
	 * 
	 * The string inserted into GLSL shading section causes the shading to
	 * consider this particular light.
	 * 
	 * @param light
	 *            light index. <code>light</code> specifies the light index
	 *            ranging from 0 (inclusive) to
	 *            {@value de.tum.in.cindy3dplugin.Cindy3DViewer#MAX_LIGHTS}
	 *            (exclusive). This light is bound to the specified light index
	 *            in the rendering pipeline.
	 * @return GLSL code
	 */
	public abstract String getShaderFillIn(int light);
	
	/**
	 * @return type of the light source
	 */
	public abstract LightType getType();
}
