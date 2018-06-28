package de.tum.in.cindy3dplugin.jogl;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.swing.JFileChooser;

import com.jogamp.opengl.GL2;
import org.apache.commons.math.geometry.Vector3D;
import org.apache.commons.math.linear.RealMatrix;

import com.jogamp.common.GlueGenVersion;
import com.jogamp.common.jvm.JNILibLoaderBase;
import com.jogamp.common.jvm.JNILibLoaderBase.LoaderAction;
import com.jogamp.opengl.JoglVersion;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;

/**
 * Collection of utility methods.
 */
public class Util {
	/**
	 * Default shader path
	 */
	private static final String SHADER_PATH = "/de/tum/in/cindy3dplugin/resources/shader/";
	/**
	 * Whether to write a log file 
	 */
	private static final boolean FILE_LOGGING = false;
	/**
	 * Size of <code>double</code> in bytes
	 */
	public static final int BYTES_PER_DOUBLE = Double.SIZE / 8;
	/**
	 * Size of <code>int</code> in bytes
	 */
	public static final int BYTES_PER_INT = Integer.SIZE / 8;

	/**
	 * Current shader light fill-in
	 * 
	 * @see #readShaderSource(URL, StringBuffer)
	 */
	private static String shaderLightFillIn = "";
	/**
	 * The global logger instance
	 */
	private static Logger logger;

	/**
	 * Private constructor to disallow instances of this class.
	 * 
	 * @throws UnsupportedOperationException
	 *             always
	 */
	private Util() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Converts a <code>RealMatrix</code> to <code>float[]</code>.
	 * 
	 * @param m
	 *            the matrix to convert
	 * @return array of <code>m</code>'s values in row-major order
	 */
	public static float[] matrixToFloatArray(RealMatrix m) {
		int rows = m.getRowDimension();
		int cols = m.getColumnDimension();

		float[] result = new float[rows * cols];
		double[][] data = m.getData();
		int offset = 0;
		for (int row = 0; row < rows; ++row) {
			for (int col = 0; col < cols; ++col, ++offset) {
				result[offset] = (float) data[row][col];
			}
		}

		return result;
	}

	/**
	 * Converts a <code>RealMatrix</code> to <code>float[]</code>.
	 * 
	 * @param m
	 *            the matrix to convert
	 * @return array of the transpose of <code>m</code>'s values in row-major
	 *         order
	 */
	public static float[] matrixToFloatArrayTransposed(RealMatrix m) {
		int rows = m.getRowDimension();
		int cols = m.getColumnDimension();

		float[] result = new float[rows * cols];
		double[][] data = m.getData();
		int offset = 0;
		for (int row = 0; row < rows; ++row) {
			for (int col = 0; col < cols; ++col, ++offset) {
				result[offset] = (float) data[col][row];
			}
		}

		return result;
	}

	/**
	 * Converts a <code>Vector3D</code> to <code>double[]</code>.
	 * 
	 * @param v
	 *            the vector to convert
	 * @return array of <code>v</code>'s components
	 */
	public static double[] vectorToDoubleArray(Vector3D v) {
		return new double[] { v.getX(), v.getY(), v.getZ() };
	}

	/**
	 * Converts a <code>Vector3D</code> to <code>float[]</code>.
	 * 
	 * @param v
	 *            the vector to convert
	 * @return array of <code>v</code>'s components
	 */
	public static float[] vectorToFloatArray(Vector3D v) {
		return new float[] { (float) v.getX(), (float) v.getY(),
				(float) v.getZ() };
	}

	/**
	 * Converts a <code>double[]</code> to <code>Vector3D</code>.
	 * 
	 * @param vec
	 *            the double array to convert
	 * @return vector or <code>null</code> if the length of <code>vec</code> is
	 *         not 3
	 */
	public static Vector3D doubleArrayToVector(double[] vec) {
		if (vec.length != 3) {
			return null;
		}
		return new Vector3D(vec[0], vec[1], vec[2]);
	}

	/**
	 * Reads the source code of a GLSL shader.
	 * 
	 * These custom directives are resolved while reading:
	 * <ul>
	 * <li><code>#pragma include include-file</code> is replaced by the content
	 * of <code>include-file</code>, which is is either an absolute path or a
	 * path relative to the given <code>url</code>. This is done recursively.
	 * Circular includes are not handled and result in undefined behaviour.
	 * <li><code>#pragma lights</code> is replaced by the string set via
	 * {@link #setShaderLightFillIn(String)}. The purpose of this is to handle
	 * dynamic numbers and types of light sources.
	 * </ul>
	 * 
	 * @param url
	 *            URL to read the shader from
	 * @param result
	 *            buffer to append the source code to
	 * @throws FileNotFoundException
	 *             if an include directive cannot be resolved
	 * @throws RuntimeException
	 *             if an error occurs while reading from <code>url</code>
	 */
	public static void readShaderSource(URL url, StringBuffer result) {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					url.openStream()));
			String line = null;
			while ((line = reader.readLine()) != null) {
				if (line.startsWith("#pragma include ")) {
					String includeFile = line.substring(16).trim();
					// Try relative path first
					URL nextURL = null;
					try {
						nextURL = new URL(url, includeFile);
					} catch (MalformedURLException e) {
					}
					if (nextURL == null) {
						// Try absolute path
						try {
							nextURL = new URL(includeFile);
						} catch (MalformedURLException e) {
						}
					}
					if (nextURL == null) {
						// Fail
						throw new FileNotFoundException(
								"Can't find include file " + includeFile);
					}
					readShaderSource(nextURL, result);
				} else if (line.startsWith("#pragma lights")) {
					result.append(shaderLightFillIn + "\n");
				} else {
					result.append(line + "\n");
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Loads a shader from the default shader path.
	 * 
	 * @param type
	 *            type of shader to be loaded. Must be either
	 *            GL2.GL_VERTEX_SHADER or GL2.GL_FRAGMENT_SHADER
	 * @param fileName
	 *            filename of the shader
	 * @return the loaded shader
	 */
	public static ShaderCode loadShader(int type, String fileName) {
		StringBuffer buffer = new StringBuffer();
		URL url = Util.class.getResource(SHADER_PATH + fileName);
		readShaderSource(url, buffer);
		ShaderCode shader = new ShaderCode(type, 1,
				new String[][] { { buffer.toString() } });
		return shader;
	}

	/**
	 * Loads, compiles and links a shader program from the default shader path.
	 * 
	 * @param gl2
	 *            GL handle
	 * @param vertexShaderFileName
	 *            filename of the vertex shader
	 * @param fragmentShaderFileName
	 *            filename of the fragment shader
	 * @return the compiled and linked shader program or <code>null</code> if
	 *         loading, compiling or linking failed
	 */
	public static ShaderProgram loadShaderProgram(GL2 gl2,
			String vertexShaderFileName, String fragmentShaderFileName) {
		ShaderProgram program = new ShaderProgram();

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(baos);

		ShaderCode vertexShader = loadShader(GL2.GL_VERTEX_SHADER,
				vertexShaderFileName);
		if (!vertexShader.compile(gl2, ps)) {
			Util.getLogger().info(
					"Compile log for '" + vertexShaderFileName + "': "
							+ baos.toString());
			return null;
		}

		ShaderCode fragmentShader = loadShader(GL2.GL_FRAGMENT_SHADER,
				fragmentShaderFileName);
		if (!fragmentShader.compile(gl2, ps)) {
			Util.getLogger().info(
					"Compile log for '" + fragmentShaderFileName + "': "
							+ baos.toString());
			return null;
		}

		if (!program.add(gl2, vertexShader, ps)) {
			Util.getLogger().info(
					"Attach log for '" + vertexShaderFileName + "': "
							+ baos.toString());
			return null;
		}
		if (!program.add(gl2, fragmentShader, ps)) {
			Util.getLogger().info(
					"Attach log for '" + fragmentShaderFileName + "': "
							+ baos.toString());
			return null;
		}
		if (!program.link(gl2, ps)) {
			Util.getLogger().info("Link log: " + baos.toString());
			return null;
		}

		return program;
	}

	/**
	 * Sets the shader lights fill-in.
	 * 
	 * @param shaderLightFillIn
	 *            the shader lights fill-in to set
	 * @see #readShaderSource(URL, StringBuffer)
	 */
	public static void setShaderLightFillIn(String shaderLightFillIn) {
		Util.shaderLightFillIn = shaderLightFillIn;
	}

	/**
	 * Modifies gluegen's class loading to load native libraries from the
	 * current JAR's directory.
	 */
	public static void setupGluegenClassLoading() {
		// Try to get JAR path
		String jarPath = null;

		URL jarURL = null;
		try {
			ProtectionDomain pd = Util.class.getProtectionDomain();
			CodeSource cs = pd.getCodeSource();
			jarURL = cs.getLocation();
			// jarPath = jarURL.toURI().getPath();
			jarPath = jarURL.getPath();
		} catch (SecurityException e) {
			// Can't get protection domain. This is the case if Cindy3D is
			// running inside an applet. But that's ok, as JNLP handles the
			// class and native libraries loading for us.
			Util.getLogger().log(Level.INFO, e.toString(), e);
			return;
		}

		File jarFile = new File(jarPath);
		if (!jarFile.isFile()) {
			// Not loaded from JAR file, do nothing
			Util.getLogger().info("Not loaded from jar");
			return;
		}
		final String basePath = jarFile.getParent();
		Util.getLogger().info("Base path: " + basePath);

		// Try to load native library from JAR directory
		String path = basePath + File.separator
				+ System.mapLibraryName("gluegen-rt");
		System.load(path);

		Util.getLogger().info("Loaded " + path);

		// Next, override the gluegen JNI library loader action
		JNILibLoaderBase.setLoadingAction(new LoaderAction() {
			@Override
			public void loadLibrary(String libname, String[] preload,
					boolean preloadIgnoreError, ClassLoader cl) {
				if (preload != null) {
					for (String preloadLibname : preload) {
						loadLibrary(preloadLibname, preloadIgnoreError, cl);
					}
				}
				loadLibrary(libname, false, cl);
			}

			@Override
			public boolean loadLibrary(String libname, boolean ignoreError, ClassLoader cl) {
				boolean result = true;
				Util.getLogger().info("Requested library " + libname);
				try {
					// Load JNI library from JAR directory
					String path = basePath + File.separator
							+ System.mapLibraryName(libname);
					System.load(path);
					Util.getLogger().info("Loaded " + path);
				} catch (UnsatisfiedLinkError e) {
					// Util.getLogger().log(Level.INFO, e.toString(), e);
					Util.getLogger()
							.info("Library load failed, trying fallback to System.loadLibrary");
					try {
						System.loadLibrary(libname);
						Util.getLogger().info(
								"Loaded system library " + libname);
					} catch (UnsatisfiedLinkError e2) {
						Util.getLogger().info("System library load failed");
						// Util.getLogger().log(Level.INFO, e.toString(), e2);
						result = false;
						if (!ignoreError) {
							throw e2;
						}
					}
				}
				return result;
			}
		});
	}

	/**
	 * Initializes the global logger instance.
	 */
	public static void initLogger() {
		try {
			logger = Logger.getLogger("log");
			if (FILE_LOGGING) {
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setDialogTitle("Select log file");
				int returnVal = fileChooser.showSaveDialog(null);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File logFile = fileChooser.getSelectedFile();
					FileHandler fh = new FileHandler(logFile.getAbsolutePath(),
							false);
					fh.setFormatter(new SimpleFormatter());
					logger.addHandler(fh);
				}
			}
			logger.info("Log started");

			final String nl = System.getProperty("line.separator");

			logger.info("GlueGen version "
					+ GlueGenVersion.getInstance().getImplementationVersion());
			logger.info("JOGL version "
					+ JoglVersion.getInstance().getImplementationVersion());

			Properties p = System.getProperties();
			String props = "";
			for (Object key : p.keySet()) {
				props += key + ": ";
				props += p.get(key);
				props += nl;
			}
			logger.info("System properties:" + nl + props);
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * @return the global logger instance
	 */
	public static Logger getLogger() {
		return logger;
	}

	/**
	 * Transforms a point by a homogeneous 4x4 matrix.
	 * 
	 * <code>point</code> is multiplied to <code>matrix</code> from the right.
	 * 
	 * @param matrix
	 *            4x4 homogeneous transformation matrix
	 * @param point
	 *            point to transform
	 * @return transformed point
	 */
	public static Vector3D transformPoint(RealMatrix matrix, Vector3D point) {
		if (matrix.getColumnDimension() != 4 || matrix.getRowDimension() != 4) {
			throw new IllegalArgumentException("not a 4x4-matrix");
		}

		double[] tmp = matrix.operate(new double[] { point.getX(),
				point.getY(), point.getZ(), 1 });
		return new Vector3D(tmp[0], tmp[1], tmp[2]);
	}

	/**
	 * Transforms a vector by a homogeneous 4x4 matrix.
	 * 
	 * <code>vector</code> is multiplied to <code>matrix</code> from the right.
	 * 
	 * @param matrix
	 *            4x4 homogeneous transformation matrix
	 * @param vector
	 *            vector to transform
	 * @return transformed vector
	 */
	public static Vector3D transformVector(RealMatrix matrix, Vector3D vector) {
		if (matrix.getColumnDimension() != 4 || matrix.getRowDimension() != 4) {
			throw new IllegalArgumentException("not a 4x4-matrix");
		}

		double[] tmp = matrix.operate(new double[] { vector.getX(),
				vector.getY(), vector.getZ(), 0 });
		return new Vector3D(tmp[0], tmp[1], tmp[2]);
	}
}
