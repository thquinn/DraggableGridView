package com.animoto.android;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.animoto.android.views.*;
// import com.sonyericsson.tutorial.list1.MyListView;
// import com.sonyericsson.tutorial.list1.TestActivity.MyAdapter;
// import com.markupartist.android.widget.actionbar.R;

public class DraggableGridViewSampleActivity extends Activity {

	static Random random = new Random();
	private DraggableGridView gridView;
	private Button addButton;
	private Button viewButton;
	// ArrayList<String> poem = new ArrayList<String>();
	WordAdapter poem;
	static ArrayList<String> allWords;

	static {
//		String wordsString = Resources.getSystem().getString(R.string.button1Text);
//		String[] wordArray = wordsString.split("\\s+");
//    	List<String> wordList = Arrays.asList(wordArray);
//		words = new ArrayList<String>(wordList);
	}

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        Context context = getApplication();
		this.gridView = (DraggableGridView)findViewById(R.id.dgv);
        //this.gridView = new DraggableGridView(context, null);
        this.addButton = ((Button)findViewById(R.id.add_button));
        this.viewButton = ((Button)findViewById(R.id.view_button));

        String wordsString = getApplication().getString(R.string.words);
        String[] wordArray = wordsString.split(" ");
        List<String> wordList = Arrays.asList(wordArray);
        allWords = new ArrayList<String>(wordList);

        // final ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplication(), R.layout.grid_item);
        this.poem = new WordAdapter(this, new ArrayList<String>());
        this.gridView.setAdapter(poem);
        
        setListeners();
    }

    private void setListeners()
    {
    	this.gridView.setOnRearrangeListener(new OnRearrangeListener() {
			public void onRearrange(int oldIndex, int newIndex) {
				String item = poem.getItem(oldIndex);
				poem.remove(item);
				if (oldIndex < newIndex) {
					poem.insert(item, newIndex);
				}
				else {
					poem.add(item);
					gridView.requestLayout();
				}
			}
		});

    	this.gridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				DraggableGridViewSampleActivity.this.gridView.removeViewAt(position);
				poem.remove( poem.getItem(position) );
			}
		});
 
    	this.addButton.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				String word = allWords.get(random.nextInt(allWords.size()));
				poem.add(word);
				// poem.notifyDataSetChanged();
				gridView.requestLayout();
			}
		});

    	this.viewButton.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				String finishedPoem = "";
				for (int i = 0; i < poem.getCount(); i++) {
					finishedPoem += poem.getItem(i) + " ";
				}
				new AlertDialog.Builder(DraggableGridViewSampleActivity.this)
			    	.setTitle("Here's your poem!")
			    	.setMessage(finishedPoem).show();
			}
		});
    }
    
    private Bitmap getThumb(String s)
	{
		Bitmap bmp = Bitmap.createBitmap(150, 150, Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(bmp);
	    Paint paint = new Paint();
	    
	    paint.setColor(Color.rgb(random.nextInt(128), random.nextInt(128), random.nextInt(128)));
	    paint.setTextSize(24);
	    paint.setFlags(Paint.ANTI_ALIAS_FLAG);
	    canvas.drawRect(new Rect(0, 0, 150, 150), paint);
	    paint.setColor(Color.WHITE);
	    paint.setTextAlign(Paint.Align.CENTER);
	    canvas.drawText(s, 75, 75, paint);
	    
		return bmp;
	}

    public WordAdapter getAdapter() {
    	return this.poem;
    }
    
    
    /**
     * Adapter class to use for the list
     */
    private static class WordAdapter extends ArrayAdapter<String> {

    	private Context context;
    	ArrayList<String> words;
    	/**
         * Constructor
         * 
         * @param context The context
         * @param contacts The list of contacts
         */
        public WordAdapter(final Context context, final ArrayList<String> words) {
            super(context, 0, words);
            this.context = context;
            this.words = words;
        }

        @Override
        public View getView(final int position, final View convertView, final ViewGroup parent) {
            View view = convertView;
            if (view == null) {
            	// LayoutInflater inflater = LayoutInflater.from( this.context.getApplicationContext() );
            	LayoutInflater inflater =
            		(LayoutInflater)this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = (View)inflater.inflate(R.layout.grid_item, null);
            }

            final TextView name = (TextView)view.findViewById(R.id.grid_item_text);
            name.setText( this.words.get(position) );

           return view;
        }
    }
}