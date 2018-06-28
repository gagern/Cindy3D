package de.tum.in.cindy3dplugin.jogl;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;

import java.awt.Color;

/**
 * Material properties of a primitive.
 */
public class Material {
	/**
	 * Surface color
	 */
	private Color color;
	
	/**
	 * Shininess
	 */
	private double shininess;

	/**
	 * Constructs a new material.
	 * 
	 * @param color
	 *            surface color
	 * @param shininess
	 *            shininess
	 * @param alpha
	 *            alpha value
	 */
	public Material(Color color, double shininess, double alpha) {
		this.color = new Color(color.getRed(), color.getGreen(), color
				.getBlue(), (int) (alpha * 255));
		this.shininess = shininess;
	}
	
	/**
	 * @return surface color
	 */
	public Color getColor() {
		return color;
	}
	
	/**
	 * @return shininess
	 */
	public double getShininess() {
		return shininess;
	}

	/**
	 * Sets the GL state for this material.
	 * 
	 * @param gl GL handle
	 */
	public void setGLState(GL gl) {
		gl.getGL2().glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_DIFFUSE,
				color.getComponents(null), 0);
		gl.getGL2().glMaterialf(GL2.GL_FRONT_AND_BACK, GL2.GL_SHININESS,
				(float) shininess);
	}
}
