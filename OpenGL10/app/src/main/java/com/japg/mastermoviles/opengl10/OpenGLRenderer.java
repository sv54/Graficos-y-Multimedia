package com.japg.mastermoviles.opengl10;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.Matrix;
import android.util.Log;

import com.japg.mastermoviles.opengl10.util.LoggerConfig;
import com.japg.mastermoviles.opengl10.util.Resource3DSReader;
import com.japg.mastermoviles.opengl10.util.ShaderHelper;
import com.japg.mastermoviles.opengl10.util.TextResourceReader;
import com.japg.mastermoviles.opengl10.util.TextureHelper;

import java.nio.Buffer;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES10.GL_AMBIENT;
import static android.opengl.GLES10.GL_DIFFUSE;
import static android.opengl.GLES10.GL_FRONT;
import static android.opengl.GLES10.GL_LIGHT0;
import static android.opengl.GLES10.GL_LIGHTING;
import static android.opengl.GLES10.GL_POSITION;
import static android.opengl.GLES10.glLightfv;
import static android.opengl.GLES10.glLoadMatrixf;
import static android.opengl.GLES10.glMaterialfv;
import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_DEPTH_BUFFER_BIT;
import static android.opengl.GLES20.GL_DEPTH_TEST;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_MAX_TEXTURE_IMAGE_UNITS;
import static android.opengl.GLES20.GL_MAX_VERTEX_TEXTURE_IMAGE_UNITS;
import static android.opengl.GLES20.GL_NEAREST;
import static android.opengl.GLES20.GL_ONE_MINUS_SRC_ALPHA;
import static android.opengl.GLES20.GL_SRC_ALPHA;
import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_TEXTURE_MAG_FILTER;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glBlendFunc;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnable;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetFloatv;
import static android.opengl.GLES20.glGetIntegerv;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glLineWidth;
import static android.opengl.GLES20.glTexParameterf;
import static android.opengl.GLES20.glTexParameteri;
import static android.opengl.GLES20.glUniform1f;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glUniform4f;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;
import static android.opengl.GLES20.glViewport;
import static android.opengl.Matrix.frustumM;
import static android.opengl.Matrix.multiplyMM;
import static android.opengl.Matrix.rotateM;
import static android.opengl.Matrix.scaleM;
import static android.opengl.Matrix.setIdentityM;
import static android.opengl.Matrix.translateM;


public class OpenGLRenderer implements Renderer {
	private static final String TAG = "OpenGLRenderer";

	// Para paralela
	//private static final float TAM = 1.0f;
	// Para perspectiva
	//private static final float TAM = 1.0f;

	private static final int BYTES_PER_FLOAT = 4;

	private final Context context;
	private int program;

	// Nombre de los uniform
	private static final String U_MVPMATRIX 		= "u_MVPMatrix";
	private static final String U_MVMATRIX 			= "u_MVMatrix";
	private static final String U_COLOR 			= "u_Color";
	private static final String U_TEXTURE 			= "u_TextureUnit";

	// Nombre de los attribute
	private static final String A_POSITION = "a_Position";
	private static final String A_NORMAL   = "a_Normal";
	private static final String A_UV       = "a_UV";

	// Handles para los shaders
	private int uMVPMatrixLocation;
	private int uMVMatrixLocation;
	private int uColorLocation;
	private int uTextureUnitLocation;
	private int aPositionLocation;
	private int aNormalLocation;
	private int aUVLocation;

	private int	texture;
	private int	textureBlack;
	private int	textureRuedas;
	private int	textureRuedasTraseras;

	// Rotación alrededor de los ejes
	private float rX = 0f;
	private float rY = 0f;

	// Escalado alrdeddor de los ejes
	private float sX = 1f;
	private float sY = 1f;

	private float rrX = 1;
	private float rrY = 1;

	private static final int POSITION_COMPONENT_COUNT = 3;
	private static final int NORMAL_COMPONENT_COUNT = 3;
	private static final int UV_COMPONENT_COUNT = 2;
	// C?lculo del tama?o de los datos (3+3+2 = 8 floats)
	private static final int STRIDE =
			(POSITION_COMPONENT_COUNT + NORMAL_COMPONENT_COUNT + UV_COMPONENT_COUNT) * BYTES_PER_FLOAT;

	// Matrices de proyección y de vista
	private final float[] projectionMatrix = new float[16];
	private final float[] modelMatrix = new float[16];
	private final float[] MVP = new float[16];

	Resource3DSReader obj3DS;
	Resource3DSReader obj3DS_ruedaIzq;
	Resource3DSReader obj3DS_ruedasTraseras;
	float[] tablaVertices = {
		// Abanico de triángulos, x, y, R, G, B
		 0.0f, 0.0f, 1.0f, 1.0f, 1.0f,
		-0.5f,-0.8f, 0.7f, 0.7f, 0.7f,
		 0.5f,-0.8f, 0.7f, 0.7f, 0.7f,
		 0.5f, 0.8f, 1.0f, 1.0f, 1.0f,
		-0.5f, 0.8f, 0.7f, 0.7f, 0.7f,
		-0.5f,-0.8f, 0.7f, 0.7f, 0.7f,

		// L?nea 1, x, y, R, G, B
		-0.5f, 0f, 1.0f, 0.0f, 0.0f,
		 0.5f, 0f, 1.0f, 0.0f, 0.0f
	};

	void frustum(float[] m, int offset, float l, float r, float b, float t, float n, float f)
	{
		frustumM(m, offset, l, r, b, t, n, f);
		// Corrección del bug de Android
		m[8] /= 2;
	}

    void perspective(float[] m, int offset, float fovy, float aspect, float n, float f)
    {	final float d = f-n;
    	final float angleInRadians = (float) (fovy * Math.PI / 180.0);
    	final float a = (float) (1.0 / Math.tan(angleInRadians / 2.0));

    	m[0] = a/aspect;
        m[1] = 0f;
        m[2] = 0f;
        m[3] = 0f;

        m[4] = 0f;
        m[5] = a;
        m[6] = 0f;
        m[7] = 0f;

        m[8] = 0;
        m[9] = 0;
        m[10] = (n - f) / d;
        m[11] = -1f;

        m[12] = 0f;
        m[13] = 0f;
        m[14] = -2*f*n/d;
        m[15] = 0f;

    }

	void perspective2(float[] m, int offset, float fovy, float aspect, float n, float f)
	{	float fH, fW;

		fH = (float) Math.tan( fovy / 360 * Math.PI ) * n;
		fW = fH * aspect;
		frustum(m, offset, -fW, fW, -fH, fH, n, f);

	}
	void frustum2(float[] m, int offset, float l, float r, float b, float t, float n, float f)
	{
		float d1 = r-l;
		float d2 = t-b;
		float d3 = f-n;

		m[0] = 2*n/d1;
		m[1] = 0f;
		m[2] = 0f;
		m[3] = 0f;

		m[4] = 0f;
		m[5] = 2*n/d2;
		m[6] = 0f;
		m[7] = 0f;

		m[8] = (r+l)/d1;
		m[9] = (t+b)/d2;
		m[10] = (n-f)/d3;
		m[11] = -1f;

		m[12] = 0f;
		m[13] = 0f;
		m[14] = -2*f*n/d3;
		m[15] = 0f;
	}

	public OpenGLRenderer(Context context) {
		this.context = context;

		// Lee un archivo 3DS desde un recurso
		obj3DS = new Resource3DSReader();
		obj3DS.read3DSFromResource(context, R.raw.f1);
		//sSystem.out.println("numMeshes: " + obj3DS.numMeshes);
//
//		obj3DS_ruedaIzq = new Resource3DSReader();
//		obj3DS_ruedaIzq.read3DSFromResource(context, R.raw.f1_rueda_izq);
//
//		obj3DS_ruedasTraseras = new Resource3DSReader();
//		obj3DS_ruedasTraseras.read3DSFromResource(context, R.raw.f1_ruedas_traseras);


	}

	@Override
	public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {
		String vertexShaderSource;
		String fragmentShaderSource;

		glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

		int[]	maxVertexTextureImageUnits = new int[1];
		int[]	maxTextureImageUnits       = new int[1];

		// Comprobamos si soporta texturas en el vertex shader
		glGetIntegerv(GL_MAX_VERTEX_TEXTURE_IMAGE_UNITS, maxVertexTextureImageUnits, 0);
		if (LoggerConfig.ON) {
			Log.w(TAG, "Max. Vertex Texture Image Units: "+maxVertexTextureImageUnits[0]);
		}
		// Comprobamos si soporta texturas (en el fragment shader)
		glGetIntegerv(GL_MAX_TEXTURE_IMAGE_UNITS, maxTextureImageUnits, 0);
		if (LoggerConfig.ON) {
			Log.w(TAG, "Max. Texture Image Units: "+maxTextureImageUnits[0]);
		}
		// Cargamos la textura desde los recursos
		texture = TextureHelper.loadTexture(context, R.drawable.f1_base_color_v3);
		textureBlack = TextureHelper.loadTexture(context, R.drawable.ruedas);


		// Leemos los shaders
		if (maxVertexTextureImageUnits[0]>0) {
			// Textura soportada en el vertex shader
			vertexShaderSource = TextResourceReader
				.readTextFileFromResource(context, R.raw.specular_vertex_shader);
			fragmentShaderSource = TextResourceReader
				.readTextFileFromResource(context, R.raw.specular_fragment_shader);
		} else {
			// Textura no soportada en el vertex shader
			vertexShaderSource = TextResourceReader
				.readTextFileFromResource(context, R.raw.specular_vertex_shader2);
			fragmentShaderSource = TextResourceReader
				.readTextFileFromResource(context, R.raw.specular_fragment_shader2);
		}

		// Compilamos los shaders
		int vertexShader = ShaderHelper.compileVertexShader(vertexShaderSource);
		int fragmentShader = ShaderHelper.compileFragmentShader(fragmentShaderSource);

		// Enlazamos el programa OpenGL
		program = ShaderHelper.linkProgram(vertexShader, fragmentShader);

		// En depuración validamos el programa OpenGL
		if (LoggerConfig.ON) {
			ShaderHelper.validateProgram(program);
		}

		// Activamos el programa OpenGL
		glUseProgram(program);

		// Capturamos los uniforms
		uMVPMatrixLocation = glGetUniformLocation(program, U_MVPMATRIX);
		uMVMatrixLocation = glGetUniformLocation(program, U_MVMATRIX);
		uColorLocation = glGetUniformLocation(program, U_COLOR);
		uTextureUnitLocation = glGetUniformLocation(program, U_TEXTURE);

		// Capturamos los attributes
		aPositionLocation = glGetAttribLocation(program, A_POSITION);
		glEnableVertexAttribArray(aPositionLocation);
		aNormalLocation = glGetAttribLocation(program, A_NORMAL);
		glEnableVertexAttribArray(aNormalLocation);
		aUVLocation = glGetAttribLocation(program, A_UV);
		glEnableVertexAttribArray(aUVLocation);
	}

	@Override
	public void onSurfaceChanged(GL10 glUnused, int width, int height) {
		// Establecer el viewport de  OpenGL para ocupar toda la superficie.
		glViewport(0, 0, width, height);
		final float aspectRatio = width > height ?
				(float) width / (float) height :
				(float) height / (float) width;
		if (width > height) {
				// Landscape
				//orthoM(projectionMatrix, 0, -aspectRatio*TAM, aspectRatio*TAM, -TAM, TAM, -100.0f, 100.0f);
				perspective(projectionMatrix, 0, 45f, aspectRatio, 0.01f, 1000f);
				//frustum(projectionMatrix, 0, -aspectRatio*TAM, aspectRatio*TAM, -TAM, TAM, 1f, 1000.0f);
		} else {
				// Portrait or square
				//orthoM(projectionMatrix, 0, -TAM, TAM, -aspectRatio*TAM, aspectRatio*TAM, -100.0f, 100.0f);
				perspective(projectionMatrix, 0, 45f, 1f/aspectRatio, 0.01f, 1000f);
				//frustum(projectionMatrix, 0, -TAM, TAM, -aspectRatio*TAM, aspectRatio*TAM, 1f, 1000.0f);
		}
	}

	@Override
	public void onDrawFrame(GL10 glUnused) {

		// Clear the rendering surface.
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		glEnable(GL_DEPTH_TEST);
//		glEnable(GL_LIGHTING);
		//glEnable(GL_CULL_FACE);
		//glEnable(GL_BLEND);
//		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		//glEnable(GL_DITHER);
		//glLineWidth(2.0f);


		glUniformMatrix4fv(uMVPMatrixLocation, 1, false, MVP, 0);
		// Env?a la matriz modelMatrix al shader
		glUniformMatrix4fv(uMVMatrixLocation, 1, false, modelMatrix, 0);
		// Actualizamos el color (Marr?n)
		//glUniform4f(uColorLocation, 0.78f, 0.49f, 0.12f, 1.0f);
		glUniform4f(uColorLocation, 1.0f, 1.0f, 1.0f, 1.0f);

		glActiveTexture(GL_TEXTURE0);
		glUniform1f(uTextureUnitLocation, 0);
		for (int i = 0; i < obj3DS.numMeshes; i++) {
			setIdentityM(modelMatrix, 0);
			translateM(modelMatrix, 0, 0f, 0.0f, -7.0f);
			rotateM(modelMatrix, 0, rY, 0f, 1f, 0f);
			rotateM(modelMatrix, 0, rX, 1f, 0f, 0f);
			scaleM(modelMatrix, 0, sX, sY, sX);

			//rueda delantera izq
			if (i == 0) {
				translateM(modelMatrix, 0, -1.45f, -0.50f, 0f);
				glBindTexture(GL_TEXTURE_2D, textureBlack);
				glTexParameterf( GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
				rotateM(modelMatrix, 0, rrY, 0f, 0f, 1f);

			}

			//rueda delantera der
			if (i == 2) {
				translateM(modelMatrix, 0, -1.45f, 0.50f, 0f);
				glBindTexture(GL_TEXTURE_2D, textureBlack);
				glTexParameterf( GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

				rotateM(modelMatrix, 0, rrY, 0f, 0f, 1f);


			}
			if (i == 1) {
				glBindTexture(GL_TEXTURE_2D, texture);
				glTexParameterf( GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
				glActiveTexture(GL_TEXTURE0);
				glBindTexture(GL_TEXTURE_2D, texture);
				glUniform1i(uTextureUnitLocation, 0);
			}

			if (i == 3) {
				glBindTexture(GL_TEXTURE_2D, textureBlack);
				glTexParameterf( GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

			}
			multiplyMM(MVP, 0, projectionMatrix, 0, modelMatrix, 0);
			glUniformMatrix4fv(uMVPMatrixLocation, 1, false, MVP, 0);
			glUniformMatrix4fv(uMVMatrixLocation, 1, false, modelMatrix, 0);



			// Asociando vértices con su attribute
			final Buffer position = obj3DS.dataBuffer[i].position(0);
			glVertexAttribPointer(aPositionLocation, POSITION_COMPONENT_COUNT, GL_FLOAT,
					false, STRIDE, obj3DS.dataBuffer[i]);

			// Asociamos el vector de normales
			obj3DS.dataBuffer[i].position(POSITION_COMPONENT_COUNT);
			glVertexAttribPointer(aNormalLocation, NORMAL_COMPONENT_COUNT, GL_FLOAT,
					false, STRIDE, obj3DS.dataBuffer[i]);

			// Asociamos el vector de UVs
			obj3DS.dataBuffer[i].position(POSITION_COMPONENT_COUNT+NORMAL_COMPONENT_COUNT);
			glVertexAttribPointer(aUVLocation, NORMAL_COMPONENT_COUNT, GL_FLOAT,
					false, STRIDE, obj3DS.dataBuffer[i]);
			glDrawArrays(GL_TRIANGLES, 0, obj3DS.numVertices[i]);
		}


	}

	private float lastNormalizedX;
	private float lastNormalizedY;

	private float currentScale = 1.0f;

	boolean anteriorAumentando = false;

	public void handleLeft(){
		if(rrY >= -20f){
			rrY -= 5f;
		}
	}
	public void handleRight(){
		if(rrY <= 20f){
			rrY += 5f;
		}
	}
	public void handleScale2(float distancia){
		Log.w("tagg", "scaleFactor: " + distancia);
		float zoomSensivity = 0.01f;
		float factor = 1.0f + distancia * zoomSensivity;
		sX = sX * factor;
		sY = sY * factor;
	}


	public void handleTouchPress(float normalizedX, float normalizedY) {
		if (LoggerConfig.ON) {
			// Log.w(TAG, "Touch Press ["+normalizedX+", "+normalizedY+"]");
		}

		lastNormalizedX = normalizedX;
		lastNormalizedY = normalizedY;
	}

	public void handleTouchDrag(float normalizedX, float normalizedY) {
		if (LoggerConfig.ON) {
			//Log.w(TAG, "Touch Drag ["+normalizedX+", "+normalizedY+"]");
		}

		float desplazamientoX  = normalizedX - lastNormalizedX;
		float desplazamientoY  = normalizedY - lastNormalizedY;

		lastNormalizedX = normalizedX;
		lastNormalizedY = normalizedY;

		rX += -desplazamientoY * 180f;
		rY += desplazamientoX  * 180f;
	}
}