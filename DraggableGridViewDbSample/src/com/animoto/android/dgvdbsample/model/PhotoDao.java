package com.animoto.android.dgvdbsample.model;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import com.animoto.android.dgvdbsample.model.Photo;
import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.misc.TransactionManager;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;

public class PhotoDao extends BaseDaoImpl<Photo, Integer> {

	public PhotoDao(ConnectionSource connectionSource) throws SQLException {
		super(connectionSource, Photo.class);
	}
	
	
	public Photo getPhotoWithPosition(int pos) {
		ArrayList<Photo> photos = new ArrayList<Photo>();
		QueryBuilder<Photo, Integer> projectBuilder = this.queryBuilder();
		
		try 
		{
			Where where = projectBuilder.where();
			where.eq("position", pos);
			PreparedQuery<Photo> projectQuery = projectBuilder.prepare();
			photos.addAll(ORMHelper.photoDao.query(projectQuery));
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		if (photos.size() == 1) return photos.get(0);
		else return null; //unexpected number of objects found at the specified position. 
	}


	public void rearrangePhotos(final int oldIndex, final int newIndex) {
		try {
			TransactionManager.callInTransaction(connectionSource, new Callable<Void>() {
				public Void call() throws Exception {
					Photo old = getPhotoWithPosition(oldIndex);
					if (newIndex < oldIndex)
						executeRaw("UPDATE Photo SET position = position + 1 WHERE position >= " + newIndex + " AND position < " + oldIndex);
					else
						executeRaw("UPDATE Photo SET position = position - 1 WHERE position <= " + newIndex + " AND position > " + oldIndex);
					old.position = newIndex;
					update(old);
					return null;
				}
			});
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void printAll()
	{
		try {
			List<Photo> photos = queryForAll();
			for (Photo p : photos)
			{
				
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
