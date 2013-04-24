package com.paleomobile.helpers;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject; 

public class Assignment
{
	private String title;
	private String link;
	private String description;
	private String grade;
	private String available_from;
	private String due;
	private String turned_in;
	private String status;
	private boolean fetched;
	
	public Assignment(JSONObject assignment)
	{	
		try{
			title = assignment.getString("title");
			link  = assignment.getString("link");
		} catch(JSONException e){}
		
		fetched = false;
	}
	
	private void fetchJSON(HttpHelper httphelper) 
	{	
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair("link", link));
		try {
			httphelper.post("getAssignment", nameValuePairs);
		} catch (Exception e) {}
	}
	
	public void populateAssignment(HttpHelper httphelper)
	{	
		fetched = true;
		
		fetchJSON(httphelper);
		
		JSONObject json = null;
		String tmp = null;
		try{
			tmp = EntityUtils.toString ( httphelper.getHttpResponse().getEntity() );
			json = new JSONObject( tmp );
		} catch (Exception e) {
			fetched = false;
			return;
		}
		
		try
		{  
		    description    = json.getString("description");
		    grade 		   = json.getString("grade");
            available_from = json.getString("available_from");
            due 		   = json.getString("due");
            turned_in	   = json.getString("turned_in");
            status 		   = json.getString("status");
		} catch(JSONException e){
			fetched = false;
			return;
		}
	}
	
	public boolean getDone()
	{
		if (status.contains("Not"))	 return false;
		else						 return true;
	}
	
	public boolean getNoDate()
	{
		try{
			new Date(due);
			return false;
		} catch (Exception e){ return true; }
	}
	
	//Accessors/Setters
	public boolean wasFetched()
	{ return fetched; }

	public String getTitle()
	{ return title; }
	
	public String getLink()
	{ return link; }
	
	public String getDescription()
	{ return description; }
	
	public String getGrade()
	{ return grade; }
	
	public String getAvailableFrom()
	{ return available_from; }
	
	public String getDue()
	{ return due; }
	
	public String getTurnedIn()
	{ return turned_in; }
	
	public String getStatus()
	{ return status; }
}