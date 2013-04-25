package com.paleomobile.moodle;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;
import java.util.Date;

import com.paleomobile.moodle.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
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
	private File outputFile;
	private String filename;
	private String PATHD;
	ProgressDialog mProgressDialog;
	
	private class DownloadFile extends AsyncTask<String, Integer, String> {
	    @Override
	    protected String doInBackground(String... sUrl) {
	        try {
	            URL url = new URL(sUrl[0]);
	            URLConnection connection = url.openConnection();
	            String PATHCOOKIE = Environment.getExternalStorageDirectory().toString()+"/PaleoMobile/cookie";
	            File cookie = new File(PATHCOOKIE);
                FileInputStream cookiestream = new FileInputStream(cookie);
                String ret = convertStreamToString(cookiestream);
                cookiestream.close();        
                ret = ret.substring(8);
                connection.setRequestProperty("Cookie", ret);
                connection.setDoOutput(true);
	            connection.connect();
	            int fileLength = connection.getContentLength();
	            InputStream input =  connection.getInputStream();
	            OutputStream output = new FileOutputStream(PATHD+filename);
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
	        	Log.e("Log_tag","Error downloading the file!");
	        }
            outputFile = new File (PATHD+filename);
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

	
	private void showDialog(String stuff)
	{ Toast.makeText(this, stuff,Toast.LENGTH_LONG).show(); }
	
	private String pulisciStringa(String stringa) {
		try {
			 //Delete first two and last two characters
			stringa = stringa.substring(2,stringa.length()-2);
			//Delete backslash
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
		final String decisore = pulisciStringa(app.assignment.getAvailableFrom());
		if (decisore.contains("http")) {	
			//Adapt layout to hide unuseful textbox
	        View button = findViewById(R.assignment.button1);
	        button.setVisibility(View.VISIBLE);
	        findViewById(R.assignment.info_ava).setVisibility(View.GONE);
	        findViewById(R.assignment.info_due).setVisibility(View.GONE);
	        findViewById(R.assignment.info_turn).setVisibility(View.GONE);
	        findViewById(R.assignment.info_grade).setVisibility(View.GONE);
	        
	        description.setText(app.assignment.getDescription());
	        if (decisore.contains("resource")) {
	            PATHD= Environment.getExternalStorageDirectory().toString()+"/PaleoMobile/downloads";
	            File PathDir = new File (PATHD);
	            PathDir.mkdir();
	        	filename=decisore.substring(decisore.lastIndexOf("/"));
	 
	        	final Button pulsante = (Button) findViewById(R.assignment.button1);
	        	//Check if the file has already been downloaded
	        	File file = new File (PATHD+filename);
				if(file.exists()) {
		        	pulsante.setText("File scaricato");
	            	pulsante.setEnabled(false);
  	                outputFile = file;
	        	}
	        	else {
		        	pulsante.setText("Scarica file");
		            pulsante.setOnClickListener(new View.OnClickListener() {
		        	public void onClick(View view) {        			  
		  	            mProgressDialog = new ProgressDialog(AssignmentActivity.this);
		  	            mProgressDialog.setMessage("Download in corso");
		  	            mProgressDialog.setIndeterminate(false);
		  	            mProgressDialog.setMax(100);
		  	            mProgressDialog.setCancelable(false);
		  	            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			  	        DownloadFile downloadFile = new DownloadFile();
			  	        downloadFile.execute(decisore);
			  	        pulsante.setText("File scaricato");
			            pulsante.setEnabled(false);
		        	  }
		        	});
	        		}
		        	Button apri = (Button) findViewById(R.assignment.button2);
		        	apri.setVisibility(View.VISIBLE);
		            apri.setOnClickListener(new View.OnClickListener() {
						public void onClick(View view) { 
								Intent intent = new Intent();
					            intent.setAction(android.content.Intent.ACTION_VIEW);
				                try {
				                	intent.setData(Uri.fromFile(outputFile));			      
				                	startActivity(intent);
				                }
				                catch (NullPointerException e) {
				                	showDialog("È necessario scaricare prima il file!");
				                }
				                catch (Exception exc) {
				        			showDialog("Nessun applicazione installata può aprire questo file.");		        	 
				                }
	
			        	  }
		            });

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
			//It may be one of those strange URLs, in fact they're only an autoredirect
			//I don't see any way to get the URL.. otherwise need to find a way to get
			//URL from previous activity
			if (pulisciStringa(app.assignment.getAvailableFrom())=="") {
				description.setText("URL non gestito dall'applicazione. Indagini in corso...");
				findViewById(R.assignment.info_des).setVisibility(View.GONE);
				findViewById(R.assignment.info_ava).setVisibility(View.GONE);
		        findViewById(R.assignment.info_due).setVisibility(View.GONE);
		        findViewById(R.assignment.info_turn).setVisibility(View.GONE);
		        findViewById(R.assignment.info_grade).setVisibility(View.GONE);
			}
			else {
				//It's an assigment, so dispaly additional informations
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
	
	private boolean setReminder()
	{
		Calendar now = Calendar.getInstance();
		Calendar due = Calendar.getInstance();

		Date dueDate = new Date(app.assignment.getDue());
		due.setTime(dueDate);

		Intent intent = new Intent(Intent.ACTION_EDIT);
		intent.setType("vnd.android.cursor.item/event");
		
		if (due.compareTo(now)!=-1)
		{
			intent.putExtra("endTime", due.getTimeInMillis());
			due.set(Calendar.DAY_OF_YEAR, due.get(Calendar.DAY_OF_YEAR)-1);
			due.set(Calendar.HOUR, 12);
			due.set(Calendar.MINUTE, 0);
			intent.putExtra("beginTime", due.getTimeInMillis());
		}
		
		intent.putExtra("allDay", false);
		intent.putExtra("title", app.assignment.getTitle());
		startActivity(intent);
		return true;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		if (!app.assignment.getDone() && !app.assignment.getNoDate())
		{
			MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.assignment_menu, menu);
		}
        return super.onCreateOptionsMenu(menu);
    }
	
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case (R.menu.ReminderMenuItem):
			{
				setReminder();
				return true;
			}
		}
		return super.onOptionsItemSelected(item);
	}
}
