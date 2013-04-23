package com.paleomobile.moodle;

import android.app.Application;

import com.paleomobile.helpers.Student;
import com.paleomobile.helpers.Course;
import com.paleomobile.helpers.Assignment;
import com.paleomobile.helpers.HttpHelper;

public class Globals extends Application
{
	public Student    student    = null;
	public Course     course     = null;
	public Assignment assignment = null;
	public HttpHelper httphelper = new HttpHelper("http://paleomobile.appspot.com/api/");
}
