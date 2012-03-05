package com.animoto.android.dgvdbsample.model;

import java.sql.SQLException;
import java.util.Date;
import java.util.Random;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;


import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.animoto.android.dgvdbsample.model.*;

public class ORMHelper extends OrmLiteSqliteOpenHelper {
	
	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "workspace_db";
	
	private static ORMHelper defaultOrmHelperInstance;
	

	public static PhotoDao photoDao;
	
	public ORMHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);		
		try {

			photoDao = new PhotoDao(getConnectionSource()); 
		} catch (SQLException e) {
			Log.w("dgv", "There was an exception while trying to set up daos: " + e);
		}
		Log.i("ORMHelper", "Daos created.");
    }
	
	
	public static void setDefaultOrmHelper(ORMHelper instance) {
		defaultOrmHelperInstance = instance;
	}
	
	public static ORMHelper getDefaultOrmHelper() {
		if (defaultOrmHelperInstance == null) Log.w("dgv", "No default orm helper instance has been set!");
		return defaultOrmHelperInstance;
	}
	
	
	@Override
	public void onCreate(SQLiteDatabase sqlLiteDatabase, ConnectionSource connectionSource) {
		
		try {
			TableUtils.createTable(connectionSource, Photo.class);
		
		} catch (SQLException e) {
			Log.w("dgv", "Could not create database tables in ORMHelper.onCreate()");
		}
		Log.i("ORMHelper", "Tables created.");
		populateDb1(); //Populates the db with some sample data. 

	}
	
	protected void populateDb1() {
		Random random = new Random();
		int NUM_FILES = 17;
		for (int i = 0; i < 111; i++) {
			Photo p = new Photo();
			p.title = "Photo Number " + i;
			p.created = new Date();
			p.position = i;
			int random_filenum = random.nextInt(NUM_FILES - 1) + 1;
			p.fileName = "sample_img_" + random_filenum;
			try {
				ORMHelper.photoDao.create(p);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
		Log.i("dgv", "finished loading photos");

	}
	
    
    @Override
	public void onUpgrade(SQLiteDatabase arg0, ConnectionSource arg1, int arg2, int arg3) {
		
	}

}