package com.paleomobile.moodle;

import android.webkit.MimeTypeMap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import com.paleomobile.moodle.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class AssignmentActivity extends Activity
{
	private TextView   description;
	private TextView   grade;
	private TextView   available_from;
	private TextView   due;
	private TextView   turned_in;
	private Globals app;
	private String filename;
	private String PATHD;
	ProgressDialog mProgressDialog;
	

	private void showDialog(String stuff)
	{ Toast.makeText(this, stuff,Toast.LENGTH_LONG).show(); }
	
	private class DownloadFile extends AsyncTask<String, Integer, String> {
	    @Override
	    protected String doInBackground(String... sUrl) {
	        try {
	            URL url = new URL(sUrl[0]);
	            URLConnection connection = url.openConnection();
	            //Read session-cookie
	            String PATHCOOKIE = Environment.getExternalStorageDirectory().toString()+"/PaleoMobile/cookie";           
	            File cookie = new File(PATHCOOKIE);
	            FileInputStream cookiestream = new FileInputStream(cookie);
	            BufferedReader cookiereader = new BufferedReader(new InputStreamReader(cookiestream));
	            StringBuffer cookieContent = new StringBuffer("");
	            byte[] buffer = new byte[1024];
	            while ((cookiestream.read(buffer)) != -1) {
	            	cookieContent.append(new String(buffer));
	            }
	            String cookie_final = cookieContent.toString();
	            cookie_final = cookie_final.substring(8);
	            cookiereader.close();
	            //Do connection
                connection.setRequestProperty("Cookie", cookie_final);
                connection.setDoOutput(true);
	            connection.connect();
	            int fileLength = connection.getContentLength();
	            InputStream input =  connection.getInputStream();
	            String tmp = PATHD+filename;
	            int getparameterindex = tmp.indexOf("?");
	            if (getparameterindex != -1) //There are GET parameters
	                tmp = tmp.substring(0, getparameterindex);   
	            OutputStream output = new FileOutputStream(tmp);
	            byte data[] = new byte[1024];
	            long total = 0;
	            int count;
	            while ((count = input.read(data)) != -1) {
	                total += count;
	                publishProgress((int) (total * 100 / fileLength));
	                output.write(data, 0, count);
	            }
	            output.flush();
	            output.close();
	            input.close();
	        } catch (Exception e) { 
	        	showDialog("Errore durante il download del file!");
	        }
	    	mProgressDialog.dismiss();
	        return null;
	    }
	    protected void onPreExecute() {
	        super.onPreExecute();
	        mProgressDialog.show();
	    }
	    
	    protected void onProgressUpdate(Integer... progress) {
	        super.onProgressUpdate(progress);
	        mProgressDialog.setProgress(progress[0]);
	    }
	    	    
}

	
	private String pulisciStringa(String stringa) {
		try {
			//Delete first two and last two characters
			stringa = stringa.substring(2,stringa.length()-2);
			//Delete backslashes
			stringa = stringa.replace("\\", "");
			return stringa;
		}
		catch (Exception exc) {
			//If the length is shorter than 4, return a null string.
			return "";
		}
	}
	
	   
	public static String convertStreamToString(InputStream is) throws Exception {
	    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
	    StringBuilder sb = new StringBuilder();
	    String line = null;
	    while ((line = reader.readLine()) != null) {
	      sb.append(line).append("\n");
	    }
	    return sb.toString();
	}
	
	public boolean fileExists(String FilePath){
	    java.io.File file = new java.io.File(FilePath);
	    return file.exists();
	}

	

	
	@SuppressLint("NewApi")
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState); 
		setContentView(R.layout.assignment);
				
		app = ((Globals)getApplicationContext());		
		
		((TextView) findViewById(R.assignment.bar)).setText(app.assignment.getTitle());
		
		if (!app.assignment.wasFetched())
			app.assignment.populateAssignment(app.httphelper);
		if (!app.assignment.wasFetched())
		{
			showDialog("Impossibile recuperare l'elemento.");
			finish();
		}
		
		due = (TextView) findViewById(R.assignment.due);
		description = (TextView) findViewById(R.assignment.description);
		grade = (TextView) findViewById(R.assignment.grade);
		available_from = (TextView) findViewById(R.assignment.available_from);
		turned_in = (TextView) findViewById(R.assignment.turned_in); 
		
		//Create decisore which is the 'switch case' variable
		String tmp = pulisciStringa(app.assignment.getAvailableFrom());
		if (tmp == "")
			tmp = app.assignment.getUrl();
		final String decisore = tmp;
		if (decisore.contains("http")) {	
			//Adapt layout to hide unuseful textbox
	        View button = findViewById(R.assignment.button1);
	        button.setVisibility(View.VISIBLE);
	        findViewById(R.assignment.info_ava).setVisibility(View.GONE);
	        findViewById(R.assignment.info_due).setVisibility(View.GONE);
	        findViewById(R.assignment.info_turn).setVisibility(View.GONE);
	        findViewById(R.assignment.info_grade).setVisibility(View.GONE);
	        if (app.assignment.getDescription().compareTo("") != 0)	//If there's not a description
	        	description.setText(app.assignment.getDescription());
	        else
	        	description.setText("Descrizione non disponibile.");
	        if (decisore.contains("resource")) {
	            PATHD= Environment.getExternalStorageDirectory().toString()+"/PaleoMobile/downloads";
	            File PathDir = new File (PATHD);
	            PathDir.mkdir();
	        	filename=decisore.substring(decisore.lastIndexOf("/"));
	 
	        	final Button pulsante = (Button) findViewById(R.assignment.button1);
	        	
	        	final OnClickListener OpenFileListener = new View.OnClickListener() {
		        	public void onClick(View view) {        	
						Intent intent = new Intent();
						File filetoopen = new File(PATHD+filename);
			            intent.setAction(android.content.Intent.ACTION_VIEW);
		                try {
		                	//Get MIME TYPE
		                	Uri uri = Uri.fromFile(filetoopen);
		                	String fileExtension= MimeTypeMap.getFileExtensionFromUrl(uri.toString());
		                	String mimeType= MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension);
		                	//Start an intent to open the file with obtained MIMETYPE
		                	intent.setDataAndType(Uri.fromFile(filetoopen), mimeType);
		                	startActivity(intent);
		                }
		                catch (Exception exc) {
		        			showDialog("Nessun applicazione installata può aprire questo file.");		        	 
		                }
				}
	        	};
	        	
	        	//Check if the file has already been downloaded
				if(!fileExists(PATHD+filename)) {
		        	pulsante.setText("Scarica file");
		            pulsante.setOnClickListener(new View.OnClickListener() 
		            {
			        	public void onClick(View view) {        			  
			  	            mProgressDialog = new ProgressDialog(AssignmentActivity.this);
			  	            mProgressDialog.setMessage("Download in corso");
			  	            mProgressDialog.setIndeterminate(false);
			  	            mProgressDialog.setMax(100);
			  	            mProgressDialog.setCancelable(false);
			  	            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
				  	        DownloadFile downloadFile = new DownloadFile();
				  	        downloadFile.execute(decisore);
				  	        pulsante.setText("Apri file");
							pulsante.setOnClickListener(OpenFileListener);
			        	}
		            });
				}
				else 
				{
					pulsante.setText("Apri file");
					pulsante.setOnClickListener(OpenFileListener);
				}
			}
	        else {
	        	//Open as URL
	        	Button startBrowser = (Button) findViewById(R.assignment.button1);
	        	startBrowser.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						 Uri uri = Uri.parse(decisore);
	        			 Intent intent = new Intent(Intent.ACTION_VIEW, uri);
	        			 startActivity(intent);	
					}
	        	});
	        } 
		}
		else {
			//It's an assigment, so display additional informations
			due.setText(pulisciStringa(app.assignment.getDue()));
			available_from.setText(pulisciStringa(app.assignment.getAvailableFrom()));
			turned_in.setText(pulisciStringa(app.assignment.getTurnedIn()));
			if (pulisciStringa(app.assignment.getGrade()).length()==0)
				grade.setText("Voto non presente");
			else
				grade.setText(pulisciStringa(app.assignment.getGrade()));
			description.setText(app.assignment.getDescription());
		}	
	}
	
}
