package com.webkiosk;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

	WebView webV;
	TextView label;
	String buffer, buffer2;
	Button upLeft, upRight, downLeft, downRight;
	Button quit, save, changePattern, full;
	EditText txtUrl;
	View LView;
	SharedPreferences prefs;
	SharedPreferences.Editor editor;
	
	private final List<Integer> lockSequence = new ArrayList<Integer>();
	private final List<Integer> newlockSequence = new ArrayList<Integer>();
	int lockCount=0, confirmCount=0;
	int pressedButton=0, newpressedButton=0;
	boolean configMode=false, confirmedPattern=false, initialConfig=false;
	float alfa=0.0f;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        getWindow().getDecorView().setSystemUiVisibility(8);

        setContentView(R.layout.activity_main);

        View v = findViewById(R.id.my_view);
		v.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

		//Cargamos el archivo de preferencias
		prefs = getSharedPreferences("MisPreferencias",Context.MODE_PRIVATE);
		editor = prefs.edit();
		
		editor.clear();
		editor.commit();
		
		//si la aplicación es nueva, agregamos patrón 1,2,3,4
		if(!prefs.contains("key1")){
			initialDialog();
		}else if(!prefs.contains("url")){
			Log.v("no hay url", "no hay url");
		}

		//Codigo del webView
		webV = (WebView) findViewById(R.id.webView);
		upLeft = (Button) findViewById(R.id.buttonUpLeft);
		upRight = (Button) findViewById(R.id.buttonUpRight);
		downLeft = (Button) findViewById(R.id.buttonDownLeft);
		downRight = (Button) findViewById(R.id.buttonDownRight);
		LView = findViewById(R.id.my_view);
		
		webV.setWebChromeClient(new WebChromeClient());
		webV.setWebViewClient(new WebViewClient());
		webV.getSettings().setJavaScriptEnabled(true);
		// webV.getSettings().setPluginsEnabled(true);

		webV.loadUrl(prefs.getString("url", null));

		//Cargamos el patrón desde shared preferences
		lockSequence.clear();
		for(int i = 1; i <= 4; i++){
			lockSequence.add(prefs.getInt("key"+i, 99));
			Log.v("el numero", String.valueOf(lockSequence.get(i-1)));
		}

		LView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
		    @Override
		    public void onSystemUiVisibilityChange(int visibility) {
		        if (visibility == View.SYSTEM_UI_FLAG_VISIBLE) {
		        	LView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
		        	LView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
		        }
		    }
		});
	}
	
	public void onClickTouch(View v){

		//Codigo de reconocimiento de patrón
		if(lockCount < lockSequence.size() && !configMode){
			pressedButton = recogniseButton(v);
			if(lockSequence.get(lockCount) == pressedButton){
				Log.v("Log del lock count con if del otro", String.valueOf(lockSequence.get(lockCount) + String.valueOf(pressedButton)) );
				Log.v("Numero Acertado", String.valueOf(lockSequence.get(lockCount)));
				lockCount++;
				
				if(lockCount == lockSequence.size()){
					//Aqui se escribe el codigo a ejecutar			
					lockCount=0;
					configDialog();
					//finish();
				}
			}else{
				lockCount=0;
				Log.v("Numero Fallado", "Reiniciando patron");
			}
		}
		
		if(configMode){
			//if(confirmedPattern=false){
				
			newpressedButton = recogniseButton(v);
			
			if(!confirmedPattern){
				if(newlockSequence.size() <= 3){
					Log.v("Ha entrado","Ha entrado al bucle");
					Log.v("valor del apretado",String.valueOf(newpressedButton));
					newlockSequence.add(newpressedButton);
				}
				
				if(newlockSequence.size()==4){
					confirmedPattern=true;
					showToast(this.getString(R.string.touchRepeatPatter));
				}
			}else if(confirmedPattern){
				//if(confirmCount<=3){
					Log.v("Valor apretado",String.valueOf(newpressedButton));
					Log.v("Valor de la secuencia que toca",String.valueOf(newlockSequence.get(confirmCount)));
					
					if(newlockSequence.get(confirmCount)==newpressedButton){
						confirmCount++;
					}else{
						newlockSequence.clear();
						//configMode=false;
						confirmedPattern=false;
						confirmCount=0;
						showToast(this.getString(R.string.touchPatternFailed));
						//changePattern.setText(this.getString(R.string.changePatInitAgain));
						//initialDialogPattern();
						configMode=false;
						
						
						if (initialConfig){
							initialConfig=false;
							changePattern.setText(this.getString(R.string.changePatInitAgain));
							initialDialogPattern();
						}else{
							configMode=false;
							configDialog();
						}
							
					}
				//}
								
				if(confirmCount==4){
					//guardado
					lockSequence.clear();
					
					for(int i = 1; i <= 4; i++){
						editor.remove("key"+i);
						editor.commit();
						editor.putInt("key"+i, newlockSequence.get(i-1));
						editor.commit();
						lockSequence.add(prefs.getInt("key"+i, i));
					}
					newlockSequence.clear();
					//configMode=false;
					confirmedPattern=false;
					confirmCount=0;
					showToast(this.getString(R.string.touchSavedPattern));
					configMode=false;

					if (initialConfig){
						initialConfig=false;
						alfa=1.0f;
						//changePattern.setText("Pattern Configured");
						changePattern.setActivated(false);
						save.setEnabled(true); 
						initialDialogPattern();
					}else{
						configDialog();
					}
				}			
			}
		}
	}

	public void initialDialog(){
		final Dialog dialog = new Dialog(this);
		
		dialog.setCancelable(false);

		dialog.setContentView(R.layout.initial_layout);
		
		//dialog.setTitle("Initial Setup");
		dialog.setTitle(this.getString(R.string.titleDialog1));
		label = (TextView) dialog.findViewById(R.id.label);
		quit = (Button) dialog.findViewById(R.id.finish);
		
		quit.setText(this.getString(R.string.next));

		dialog.show();
		
		quit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
				dialog.cancel();
				initialDialogURL();
			}
		});
	}
	
	public void initialDialogURL(){
		final Dialog dialog = new Dialog(this);
		
		dialog.setCancelable(false);
		dialog.setContentView(R.layout.initial_url);
		dialog.setTitle(this.getString(R.string.titleDialog2));
		txtUrl = (EditText) dialog.findViewById(R.id.url);
		save = (Button) dialog.findViewById(R.id.next1);
		
		dialog.show();

		txtUrl.setOnKeyListener(new OnKeyListener() {
 			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// TODO Auto-generated method stub
				if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (keyCode == EditorInfo.IME_ACTION_DONE)) {
					saveURL();
					 
					dialog.dismiss();
					initialDialogPattern();
		        }
				return false;
			}
		});
		
		save.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				saveURL();

				dialog.dismiss();
				initialDialogPattern();
			}
		});
	} 
	
	public void initialDialogPattern(){
		final Dialog dialog = new Dialog(this);
		
		dialog.setCancelable(false);
		dialog.setContentView(R.layout.initial_pattern);
		dialog.setTitle(this.getString(R.string.titleDialog3));
		changePattern= (Button) dialog.findViewById(R.id.changePatInit);
		save = (Button) dialog.findViewById(R.id.next2);
		
		buffer=this.getString(R.string.touchNewPatter);
		buffer2=this.getString(R.string.touchFirstPatter);
 
		//save.setAlpha(alfa);
		
		if(!prefs.contains("key1")){
			save.setEnabled(false);
		}else{
			save.setEnabled(true); 
		}
		
		dialog.show();
		
		changePattern.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				configMode=true;
				initialConfig=true;
				dialog.dismiss();
				showToast(buffer);
			}
		});
		
		save.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(!prefs.contains("key1")){
					showToast(buffer2);
				}else{
					dialog.dismiss();
					initialDialogFinal();
				}
			}
		});
	}
	
	public void initialDialogFinal(){
		final Dialog dialog = new Dialog(this);
		
		dialog.setCancelable(false);
		dialog.setContentView(R.layout.final_layout);
		dialog.setTitle(this.getString(R.string.titleDialog4));
		label = (TextView) dialog.findViewById(R.id.label);
        full=(Button) dialog.findViewById(R.id.btnFull);
		quit = (Button) dialog.findViewById(R.id.finish);

		label.setText(this.getString(R.string.lblDialog4));
		quit.setText(this.getString(R.string.finish));
		
		dialog.show();

        full.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                final String appPackageName = "de.tsorn.FullScreen&hl=es"; // Can also use getPackageName(), as below
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
            }
        });
		
		quit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
	}
	
	
	public void configDialog(){
		final Dialog dialog = new Dialog(this);
		
		//View v1 = findViewById(R.id.my_view1);
		//v1.setSystemUiVisibility(View.STATUS_BAR_HIDDEN);
		
		//dialog.setCancelable(false);
		dialog.setContentView(R.layout.dialog_layout);
		dialog.setTitle(this.getString(R.string.titleDialog5));
		
		txtUrl = (EditText) dialog.findViewById(R.id.url);
		txtUrl.setText(prefs.getString("url", null));

		dialog.show();
 
		save = (Button) dialog.findViewById(R.id.save);
		changePattern= (Button) dialog.findViewById(R.id.changePat);
		quit = (Button) dialog.findViewById(R.id.exit);
		
		buffer=this.getString(R.string.touchNewPatter);
		
		txtUrl.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				save.setEnabled(true);
			}
		});
		
		txtUrl.setOnKeyListener(new OnKeyListener() {
 			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// TODO Auto-generated method stub
				if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (keyCode == EditorInfo.IME_ACTION_DONE)) {
					saveURL();
					showToast(getString(R.string.touchURLModify));
		        }
				return false;
			}
		});
		
		
		
		save.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				Log.v("la url es modificada es:", prefs.getString("url", null));
			}
		});
		
		changePattern.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				configMode=true;
				dialog.dismiss();
				showToast(buffer);
			}
		});
		
		quit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showToast(getString(R.string.touchClosing));
				finishApp();
			}
		});

	}
	
	public void showToast(String toast){
		Toast toast1 = Toast.makeText(this, toast, Toast.LENGTH_SHORT);
		toast1.show();
	}
	
	public int recogniseButton(View v){
		 int pressedButton=0;
		if(v.getId() == upLeft.getId()){
			return pressedButton=1;
		}
		if(v.getId() == upRight.getId()){
			return pressedButton=2;
		}
		if(v.getId() == downLeft.getId()){
			return pressedButton=3;
		}
		if(v.getId() == downRight.getId()){
			return pressedButton=4;
		}
		return pressedButton;
	}
	
	public void finishApp(){
		finish();
	}

	public void saveURL(){
		//Comprobamos si empieza por www y agregamos http://
		if(txtUrl.getText().toString().startsWith("www")){
			txtUrl.setText("http://" + txtUrl.getText().toString());
		}
		if(!txtUrl.getText().toString().contains("http://")){
			txtUrl.setText("http://" + txtUrl.getText().toString());
			// showToast("URL incorrecta. Debe contener 'http://' delante.");
		}
        //Si no se escribe una URL
		if (txtUrl.getText().toString().isEmpty()) {
			txtUrl.setText(" ");
		}
		editor.remove("url");
		editor.commit();
		editor.putString("url", txtUrl.getText().toString());
		editor.commit();
		webV.loadUrl(txtUrl.getText().toString());
	}
	
	@Override
	public void onBackPressed() {

	}


        //=====================================================================================
    /*Permite relanzar el boton Recent
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (!hasFocus) {
            windowCloseHandler.postDelayed(windowCloserRunnable, 10);
            Intent closeDialog = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
            sendBroadcast(closeDialog);
        }
    }

    private void toggleRecents() {
        Intent closeRecents = new Intent("com.android.systemui.recent.action.TOGGLE_RECENTS");
        closeRecents.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        ComponentName recents = new ComponentName("com.android.systemui", "com.android.systemui.recent.RecentsActivity");
        closeRecents.setComponent(recents);
        this.startActivity(closeRecents);
    }

    private Handler windowCloseHandler = new Handler();
    private Runnable windowCloserRunnable = new Runnable() {
        @Override
        public void run() {
            ActivityManager am = (ActivityManager)getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
            ComponentName cn = am.getRunningTasks(1).get(0).topActivity;

            if (cn != null && cn.getClassName().equals("com.android.systemui.recent.RecentsActivity")) {
                toggleRecents();
            }
        }
    };*/
    //=====================================================================================

/*
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
*/
}

