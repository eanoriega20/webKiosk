package com.webkiosk;

import android.util.Log;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

public class JSONParse {
	
	//Para el Drawer
    //ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
    //        android.R.layout.simple_list_item_1, mTitles);
    //mDrawerList.setAdapter(adapter);
    //draw.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
	
	//Creamos la instancia de carga de patrón desde el sistema
	//JSONParse fileLoad = new JSONParse();
	//fileLoad.loadPatronus(lockSequence);
	
	
	
	private List<Integer> defaultPass = new ArrayList<Integer>();
	
	public JSONParse(){
		defaultPass.add(1);
		defaultPass.add(2);
		defaultPass.add(3);
		defaultPass.add(4);
		
	}

	public void saveNewPatronus(List<Integer> newlockSequence){
		//Guarda un nuevo patron que hayamos configurado en newlockSequence
		JSONArray jsArray = new JSONArray(newlockSequence);
		
        FileOutputStream fOut = null; 
        OutputStreamWriter osw = null;

    	try {
    		FileWriter file = new FileWriter("/data/data/com.example/files/patronus.txt");
    		
    		
    		
    		file.write(jsArray.toString());
    		file.flush();
    		file.close();
    	} catch (IOException e) {
    		e.printStackTrace();
    	}
	}
	
	public void loadPatronus(List<Integer> lockSequence) {
		//Codigo que carga el patron de un archivo TXT en codigo JSON
		try {
			Log.v("Patronus",getStringFromFile());
			//JsonArray jArray = new JsonParser().parse(json).getAsJsonArray();
			JSONArray jsonArray = new JSONArray(getStringFromFile());
			//Log.v("Hola --",String.valueOf(jsonArray.getInt(0)));
			
			//Log.v("Longitud del array",String.valueOf(jsonArray.length()));
			lockSequence.clear();
			for(int i = 0; i < jsonArray.length(); i++){
				lockSequence.add(jsonArray.getInt(i));
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			saveNewPatronus(defaultPass);
			loadPatronus(lockSequence);
			
		}
		
		
		
		
	}
	
	public String convertStreamToString(InputStream is) throws Exception {
	    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
	    StringBuilder sb = new StringBuilder();
	    String line = null;
	    while ((line = reader.readLine()) != null) {
	      sb.append(line).append("\n");
	    }
	    return sb.toString();
	}

	public String getStringFromFile () throws Exception {
	    File fl = new File("/data/data/com.example/files/patronus.txt");
	    FileInputStream fin = new FileInputStream(fl);
	    String ret = convertStreamToString(fin);
	    //Make sure you close all streams.
	    fin.close();        
	    return ret;
	}
	
	
	
	
	
	
	
	
	
	
}
