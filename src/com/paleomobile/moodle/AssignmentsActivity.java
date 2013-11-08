package com.paleomobile.moodle;

import java.util.ArrayList;

import com.paleomobile.helpers.Assignment;
import com.paleomobile.moodle.R;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class AssignmentsActivity extends ListActivity
{	
	private ArrayList<String> assignmentNameList;
	private ArrayAdapter<String> assignmentListAdapter;
	private Globals app;
	private ProgressDialog progressDialog;
	private int element;

	private boolean connectedToInternet()
    {
    	ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
    	NetworkInfo ni = cm.getActiveNetworkInfo();
    	if (ni == null) {
    	    // There are no active networks.
    	    return false;
    	}
    	return ni.isConnected();
    }
	
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState); 
		setContentView(R.layout.assignments);
		
		app = ((Globals)getApplicationContext());
		
		((TextView) findViewById(R.id.assignments_bar)).setText(app.course.getTitle());
		
		assignmentNameList    = new ArrayList<String>();
		assignmentListAdapter = new ArrayAdapter<String>(this, R.layout.list_item, assignmentNameList);
		
		for (Assignment a: app.course.getAssignments())
			assignmentNameList.add(a.getTitle());
		
		setListAdapter(assignmentListAdapter);
	}
	
	private void showDialog(String stuff)
	{ Toast.makeText(this, stuff,Toast.LENGTH_LONG).show(); }
	
	private void goAssignment()
	{
		showDialog(element);
		if (app.course.getAssignments()[element].wasFetched())
		{
			Intent i = new Intent(this, AssignmentActivity.class);
			app.assignment = app.course.getAssignments()[element];
			startActivity(i);
		}
		else
			showDialog("Impossibile recuperare l'elemento.");
	}

	
	private final Handler progressHandler = new Handler()
	{
		@Override
		public void handleMessage(android.os.Message msg) 
		{			
			progressDialog.dismiss();
			goAssignment();
		}
    };

	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) 
	{
		super.onListItemClick(l, v, position, id);
		element = position;
		if (connectedToInternet())
			if (!app.course.getAssignments()[element].wasFetched())
			{
				progressDialog = ProgressDialog.show(this, "", "Download elemento...", true);
				new Thread( new Runnable() {
					public void run()
					{
						app.course.getAssignments()[element].populateAssignment(app.httphelper);
						Log.d("tag",app.course.getAssignments()[element].getDescription() );
						progressHandler.sendEmptyMessage(0);
					}
				}).start();
			}
			else
				goAssignment();
		else
			showDialog("Nessuna connessione a internet.");
	}
	
	@Override
	public void onBackPressed()
    {	
		app.course = null;
		finish();
    }
}