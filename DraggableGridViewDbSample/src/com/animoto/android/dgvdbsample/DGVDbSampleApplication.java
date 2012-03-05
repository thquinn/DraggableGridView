package com.animoto.android.dgvdbsample;

import com.animoto.android.dgvdbsample.model.ORMHelper;
import android.app.Application;
import android.content.Context;
import android.util.Log;

public class DGVDbSampleApplication extends Application {
	@Override
	public void onCreate()
	{
		super.onCreate();
		Context context = getApplicationContext();
		
		ORMHelper.setDefaultOrmHelper(new ORMHelper(context)); 
		Log.i("dgv", "Custom Application Initialization Complete");
	}
	
}

