package com.japg.mastermoviles.opengl10;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


public class OpenGLActivity extends AppCompatActivity {
    private GLSurfaceView glSurfaceView;
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
            float lastDistance = -1;

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
                    } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
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

                            if (distance < lastDistance){
                                aumentamosEscala = false;
                            }
                            else if (lastDistance < distance){
                                aumentamosEscala = true;
                            }


                            float scaleFactor = (distance / lastDistance);
                            //Log.w("tagg", "" + scaleFactor);
//                            openGLRenderer.handleScale(scaleFactor);
                            //Log.w("tagg", "" + aumentamosEscala);

                            openGLRenderer.handleScale2(aumentamosEscala);
                            lastDistance = distance;


						/*

						float distance = calculateDistance(event);
						// Si es el primer movimiento con dos dedos
						if (lastDistance == -1) {
							lastDistance = distance;

						}
							float scaleFactor = distance / lastDistance;

							if(lastDistance - distance < 1){
								Log.w("tagg","Aumentamos");
							}

							if(lastDistance - distance > 1){
								Log.w("tagg","Disminuimos");
							}
//							Log.w("tagg", "current scale [" +scaleFactor+"]");
//							Log.w("tagg", "distancia [" +distance+"]");
//							Log.w("tagg", "lastDistance [" +lastDistance+"]");

							// Llama a la función de escalado en el renderizador OpenGL
							openGLRenderer.handleScale(scaleFactor);

						lastDistance = distance;*/

                        }

//        		}else if (event.getAction() == MotionEvent.ACTION_MOVE && event.getPointerCount() == 2){
//
//					float distance = calculateDistance(event);
//					// Si es el primer movimiento con dos dedos
//					if (lastDistance == -1) {
//						lastDistance = distance;
//					} else {
//						float scaleFactor = distance / lastDistance;
//						// Llama a la función de escalado en el renderizador OpenGL
//						openGLRenderer.handleScale(scaleFactor);

//						glSurfaceView.queueEvent(new Runnable() {
//							@Override
//							public void run() {
//								openGLRenderer.handleScale(scaleFactor);
//							}
//						});
//						lastDistance = distance;
//					}
                        //return true;
                    } else {
                        lastDistance = -1; // Reinicia la distancia si no hay dos dedos tocando la pantalla
                        return false;
                    }
                    return true;
                } else {
                    return false;
                }
            }

            //			class ScaleGestureListener extends ScaleGestureDetector.SimpleOnScaleGestureListener{
//				@Override
//				public boolean onScale(@NonNull ScaleGestureDetector detector){
//					float scaleFactor = detector.getScaleFactor();
//					glSurfaceView.queueEvent(new Runnable() {
//						@Override
//						public void run() {
//							openGLRenderer.handleTouchPinch(scaleFactor);
//						}
//					});
//					return true;
//				}
//			}
            private static final long TIMER_DELAY = 100L;

            private long lastTime = 0;

            private float calculateDistance(MotionEvent event) {
                float x1 = event.getX(0);
                float y1 = event.getY(0);
                float x2 = event.getX(1);
                float y2 = event.getY(1);
                return (float) Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
            }

        });
        setContentView(glSurfaceView);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (rendererSet) {
            glSurfaceView.onPause();
        }
    }

//	@Override
//	public boolean onTouchEvent(MotionEvent event){
//		return true;
//	}

//	@Override
//	public boolean onScaleEvent(){
//		return true;
//	}

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
