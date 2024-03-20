uniform mat4 u_MVPMatrix;
uniform mat4 u_MVMatrix;
uniform vec4 u_Color;				// in: color del objeto
uniform sampler2D u_TextureUnit;	// in: Unidad de Textura

attribute vec4 a_Position;
attribute vec3 a_Normal;
attribute vec2 a_UV;

varying vec2 v_UV;
varying vec3 v_Normal;
varying vec3 v_Position;

varying vec4 v_Color;				// out: Color de salida al fragment shader
//varying float atenuacion;

void main()
{
	float ambient  = 0.3;									// 15% de intensidad ambiente
	vec4  specularColor = vec4(1, 1, 1, 1);					// Color especular (brillos blancos)

	vec3 LightPos0 = vec3( 2,  5, 3);						// Posición de la luz 0 [fija]
	vec3 LightPos1 = vec3(-4, -5, 3);						// Posición de la luz 1 [fija]

	vec3 P = vec3(u_MVMatrix * a_Position);					// Posición del vértice
	vec3 N = vec3(u_MVMatrix * vec4(a_Normal, 0.0));    	// Normal del vértice

	// Primera Luz
	float d = length(P - LightPos0);						// distancia
	vec3  L = normalize(P - LightPos0);						// Vector Luz
	vec3  V = normalize(P);	  								// Vector Visión (Eye)
	vec3  R = normalize(reflect(-L, N));					// Vector reflejado R=2N(N.L)-L

	//attenuation = 1.0/(0.3+(0.1*d)+(0.01*d*d)); 		// Cálculo de la atenuación

	float diffuse  = max(dot(N, L), 0.0);					// Cálculo de la intensidad difusa
	float specular = pow(max(dot(V, R), 0.0), 200.0);		// Exponente de Phong (200)

	//v_Color = u_Color*ambient+attenuation*(u_Color*texture2D(u_TextureUnit, a_UV)*diffuse + specularColor*specular);

	// Segunda Luz
	d = length(P - LightPos1);								// distancia
	L = normalize(P - LightPos1);							// Vector Luz
	V = normalize(P);	  									// Vector Visión (Eye)
	R = normalize(reflect(-L, N));							// Vector reflejado R=2N(N.L)-L

	//attenuation = 1.0/(0.3+(0.1*d)+(0.01*d*d)); 			// Cálculo de la atenuación

	diffuse  = max(dot(N, L), 0.0);							// Cálculo de la intensidad difusa
	specular = pow(max(dot(V, R), 0.0), 200.0);				// Exponente de Phong (200)

	//v_Color += attenuation*(u_Color*texture2D(u_TextureUnit, a_UV)*diffuse + specularColor*specular);

	v_UV = a_UV;
	v_Normal = normalize(vec3(u_MVMatrix * vec4(a_Normal, 0.0)));
	v_Position = vec3(u_MVMatrix * a_Position);
	gl_Position = u_MVPMatrix * a_Position;
}