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
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
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
    private final static String MY_PREFERENCES = "Preferences";
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
	        	FileInputStream cookiestream = openFileInput("cookie");
	            InputStreamReader inputStreamReader = new InputStreamReader(cookiestream);
	            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
	            StringBuilder sb = new StringBuilder();
	            String line;
	            while ((line = bufferedReader.readLine()) != null) {
	                sb.append(line);
	            }
	            String cookie_final = sb.toString();
	            cookie_final = cookie_final.substring(8);
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
	            File FileDownloaded = new File (tmp);
	            OutputStream output = new FileOutputStream(FileDownloaded);
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
	        	Log.e("tag", e.toString());
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
	public static String convertStreamToString(InputStream is) throws Exception {
	    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
	    StringBuilder sb = new StringBuilder();
	    String line = null;
	    while ((line = reader.readLine()) != null) {
	      sb.append(line).append("\n");
	    }
	    return sb.toString();
	}

	
	private String pulisciStringa(String stringa) {
		try {
			//Delete first two and last two characters
			stringa = stringa.substring(2,stringa.length()-2);
			//Delete backslashes
			stringa = stringa.replace("\\", "");
			return stringa;
		}
		catch (Exception exc) { //If the length is shorter than 4, return a null string.
			return "";
		}
	}
		
	public boolean fileExists(String FilePath){
	    java.io.File file = new java.io.File(FilePath);
	    return file.exists();
	}
	
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState); 
		setContentView(R.layout.assignment);
				
		app = ((Globals)getApplicationContext());		
		
		((TextView) findViewById(R.id.assignment_bar)).setText(app.assignment.getTitle());
		
		if (!app.assignment.wasFetched())
		{
			app.assignment.populateAssignment(app.httphelper);
			Log.d("tag","RE-DOWNLOAD");
		}
		else
			Log.d("tag","gia salvato");
		if (!app.assignment.wasFetched())
		{
			showDialog("Impossibile recuperare l'elemento.");
			finish();
		}
		
		due = (TextView) findViewById(R.id.assignment_due);
		description = (TextView) findViewById(R.id.assignment_description);
		grade = (TextView) findViewById(R.id.assignment_grade);
		available_from = (TextView) findViewById(R.id.assignment_available_from);
		turned_in = (TextView) findViewById(R.id.assignment_turned_in); 
		
		//Create "decisore" which is the 'switch case' variable
		String tmp = pulisciStringa(app.assignment.getAvailableFrom());
		if (tmp == "")
			tmp = app.assignment.getUrl();
		final String decisore = tmp;
		Log.d("decisore",decisore);
		if (decisore.contains("http")) {	
			//Adapt layout in a messy way to hide unuseful textboxes
	        View button = findViewById(R.id.assignment_button1);
	        button.setVisibility(View.VISIBLE);
	        findViewById(R.id.assignment_info_ava).setVisibility(View.GONE);
	        findViewById(R.id.assignment_info_due).setVisibility(View.GONE);
	        findViewById(R.id.assignment_info_turn).setVisibility(View.GONE);
	        findViewById(R.id.assignment_info_grade).setVisibility(View.GONE);
	        if (app.assignment.getDescription().compareTo("") != 0)	//If there's not a description
	        	description.setText(app.assignment.getDescription());
	        else
	        	description.setText("Descrizione non disponibile.");
	        if (decisore.contains("resource")) {
	        	if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
	        		PATHD= Environment.getExternalStorageDirectory().toString()+"/PaleoMobile/downloads";
	        	else
	        	{
	        		SharedPreferences prefs = getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);
	                final SharedPreferences.Editor editor = prefs.edit();
	                if (prefs.getBoolean("norepeat", false) != true)
	                {
		        		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		                builder.setTitle("Attenzione");
		                builder.setMessage("Non è stata trovata una scheda esterna. I file scaricati verranno memorizzati nella memoria interna.");
		                builder.setCancelable(false);
		                builder.setNegativeButton("Ok",new DialogInterface.OnClickListener() {
		                    public void onClick(DialogInterface dialog, int id) {
		                        editor.putBoolean("norepeat", true);
		                        editor.commit();
		                        dialog.cancel();
		                    }
		                });
		                AlertDialog alert = builder.create();
		                alert.show();
	                }
	        		PATHD = getBaseContext().getApplicationContext().getFilesDir().getAbsolutePath();
	        	}
	        	Log.d("tag",PATHD);
	            File PathDir = new File (PATHD);
	            PathDir.mkdirs();
	        	filename=decisore.substring(decisore.lastIndexOf("/"));
	        	final Button pulsante = (Button) findViewById(R.id.assignment_button1);
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
	        	Button startBrowser = (Button) findViewById(R.id.assignment_button1);
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
			//It's an assignment, so display additional informations
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
