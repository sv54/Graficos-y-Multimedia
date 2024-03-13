package com.japg.mastermoviles.opengl10;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


public class OpenGLActivity extends AppCompatActivity {
    private GLSurfaceView glSurfaceView;
    private Button buttonLeft;
    private Button buttonRight;
    private boolean rendererSet = false;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

		/*super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		FloatingActionButton fab = findViewById(R.id.fab);
		fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
						.setAction("Action", null).show();
			}
		});
		*/

        super.onCreate(savedInstanceState);
        glSurfaceView = new GLSurfaceView(this);
        final OpenGLRenderer openGLRenderer = new OpenGLRenderer(this);
        final ActivityManager activityManager =
                (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        final ConfigurationInfo configurationInfo =
                activityManager.getDeviceConfigurationInfo();
        //final boolean supportsEs2 = configurationInfo.reqGlEsVersion >= 0x20000;
        final boolean supportsEs2 =
                configurationInfo.reqGlEsVersion >= 0x20000
                        || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1
                        && (Build.FINGERPRINT.startsWith("generic")
                        || Build.FINGERPRINT.startsWith("unknown")
                        || Build.MODEL.contains("google_sdk")
                        || Build.MODEL.contains("Emulator")
                        || Build.MODEL.contains("Android SDK built for x86")));


        if (supportsEs2) {
            // Request an OpenGL ES 2.0 compatible context.
            glSurfaceView.setEGLContextClientVersion(2);
            // Para que funcione en el emulador
            glSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
            // Asigna nuestro renderer.
            glSurfaceView.setRenderer(openGLRenderer);
            rendererSet = true;
            Toast.makeText(this, "OpenGL ES 2.0 soportado",
                    Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Este dispositivo no soporta OpenGL ES 2.0",
                    Toast.LENGTH_LONG).show();
            return;
        }

        glSurfaceView.setOnTouchListener(new OnTouchListener() {
            float lastDistance = 0f;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event != null) {
                    boolean aumentamosEscala = false;

                    // Convert touch coordinates into normalized device
                    // coordinates, keeping in mind that Android's Y
                    // coordinates are inverted.
                    final float normalizedX = (event.getX() / (float) v.getWidth()) * 2 - 1;
                    final float normalizedY = -((event.getY() / (float) v.getHeight()) * 2 - 1);
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        glSurfaceView.queueEvent(new Runnable() {
                            @Override
                            public void run() {
                                openGLRenderer.handleTouchPress(normalizedX, normalizedY);
                            }
                        });
                    } else if (event.getAction() == MotionEvent.ACTION_UP){
                        Log.w("tagg", "Se levantaron los dedos");   

                    }
                    else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                        if (event.getPointerCount() == 1) {
                            glSurfaceView.queueEvent(new Runnable() {
                                @Override
                                public void run() {
                                    openGLRenderer.handleTouchDrag(normalizedX, normalizedY);
                                }
                            });
                        }
                        if (event.getPointerCount() == 2) {
                            float x1 = event.getX(0);
                            float y1 = event.getY(0);
                            float x2 = event.getX(1);
                            float y2 = event.getY(1);
                            float dx = x2 - x1;
                            float dy = y2 - y1;
                            float distance = (float) Math.sqrt(dx * dx + dy * dy);
                            if(lastDistance == 0f){
                                lastDistance = distance;
                            }
                            float distanceChange = distance - lastDistance;

                            System.out.println("distanceChange: " + distance + "; " + lastDistance);
                            lastDistance = distance;
                            openGLRenderer.handleScale2(distanceChange);
                        }

                    }
                    else if(event.getAction() == MotionEvent.ACTION_POINTER_DOWN)
                    {
                        if (event.getPointerCount() == 2) {
                            float x1 = event.getX(0);
                            float y1 = event.getY(0);
                            float x2 = event.getX(1);
                            float y2 = event.getY(1);
                            float dx = x2 - x1;
                            float dy = y2 - y1;
                            float distance = (float) Math.sqrt(dx * dx + dy * dy);
                            lastDistance = distance;
                        }
                    }
                    else {
                        return false;
                    }
                    return true;
                } else {
                    return false;
                }
            }

        });
        buttonLeft = new Button(this);
        buttonLeft.setText("Left");
        buttonRight = new Button(this);
        buttonRight.setText("Right");

        // Configurar la posición y el tamaño de los botones
        FrameLayout.LayoutParams leftParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        leftParams.gravity = android.view.Gravity.BOTTOM | android.view.Gravity.START;
        leftParams.setMargins(16, 0, 0, 16); // Margen izquierdo y inferior

        FrameLayout.LayoutParams rightParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        rightParams.gravity = android.view.Gravity.BOTTOM | android.view.Gravity.END;
        rightParams.setMargins(0, 0, 16, 16); // Margen derecho y inferior

        // Agregar los botones al FrameLayout
        FrameLayout layout = new FrameLayout(this);
        layout.addView(buttonLeft, leftParams);
        layout.addView(buttonRight, rightParams);
        layout.addView(glSurfaceView);
        // Establecer el FrameLayout como el contenido de la actividad
        setContentView(layout);

        // Agregar listeners a los botones si es necesario
        buttonLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGLRenderer.handleLeft();
            }
        });

        buttonRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGLRenderer.handleRight();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (rendererSet) {
            glSurfaceView.onPause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (rendererSet) {
            glSurfaceView.onResume();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
