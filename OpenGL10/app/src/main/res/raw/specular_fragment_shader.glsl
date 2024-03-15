precision mediump float;

varying vec2 v_UV;
varying vec3 v_Normal;
varying vec3 v_Position;

uniform sampler2D u_TextureUnit;
uniform vec3 u_LightPos;
uniform vec4 u_Color;

varying vec4 v_Color;

const float shininess = 64.0; // Ajusta el brillo especular aquí

void main()
{
	vec4 texColor = texture2D(u_TextureUnit, v_UV);

	vec3 lightDir = normalize(u_LightPos - v_Position);
	vec3 viewDir = normalize(-v_Position);
	vec3 reflectDir = reflect(-lightDir, v_Normal);

	float spec = pow(max(dot(viewDir, reflectDir), 0.0), shininess);
	vec3 specular = vec3(1.0, 1.0, 1.0) * spec; // Color especular blanco

	float diff = max(dot(v_Normal, lightDir), 0.0);
	vec3 diffuse = vec3(diff);

	vec3 ambient = vec3(1); // Luz ambiental mínima

	vec3 result = (ambient + diffuse) * texColor.rgb + specular;

	gl_FragColor = vec4(result, texColor.a) * v_Color;
}