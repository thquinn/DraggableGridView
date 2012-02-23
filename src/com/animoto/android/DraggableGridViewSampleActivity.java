package com.animoto.android;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;

import com.animoto.android.views.*;
// import com.sonyericsson.tutorial.list1.MyListView;
// import com.sonyericsson.tutorial.list1.TestActivity.MyAdapter;
// import com.markupartist.android.widget.actionbar.R;

public class DraggableGridViewSampleActivity extends Activity {

	static Random random = new Random();
	private DraggableGridView gridView;
	private Button addButton;
	private Button clearButton;
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
		this.gridView = (DraggableGridView)findViewById(R.id.draggable_grid_view);
        this.addButton = ((Button)findViewById(R.id.add_button));
        this.clearButton = ((Button)findViewById(R.id.clear_button));
        this.viewButton = ((Button)findViewById(R.id.view_button));
        this.addButton.getBackground().setAlpha(210);
        this.clearButton.getBackground().setAlpha(210);
        this.viewButton.getBackground().setAlpha(210);
        
        String wordsString = getApplication().getString(R.string.words);
        String[] wordArray = wordsString.split(" ");
        List<String> wordList = Arrays.asList(wordArray);
        allWords = new ArrayList<String>(wordList);

        // final ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplication(), R.layout.grid_item);
        this.poem = new WordAdapter(this, new ArrayList<String>());

        // @TODO Apparently this is supposed to be a ListAdapter, not an ArrayAdapter
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
				poem.notifyDataSetChanged();
				gridView.requestLayout();
			}
		});

    	this.clearButton.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				poem.clear();
				// poem.notifyDataSetChanged();
				gridView.removeAllViewsInLayout();
				gridView.invalidate();
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

    /**
     * 
     * @param position index of view
     * @param text word to display
     * @return
     */
    private static Bitmap getThumb(int position, String text)
	{
    	int bitmapSize = 80;
    	int indexSize = 12;
    	int textSize = 18;
    	Bitmap bmp = Bitmap.createBitmap(bitmapSize, bitmapSize, Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(bmp);
	    Paint paint = new Paint();
	    
	    paint.setColor(Color.rgb(random.nextInt(128), random.nextInt(128), random.nextInt(128)));
	    paint.setFlags(Paint.ANTI_ALIAS_FLAG);
	    canvas.drawRect(new Rect(0, 0, bitmapSize, bitmapSize), paint);
	    paint.setColor(Color.WHITE);

	    paint.setTextSize(textSize);
	    paint.setTextAlign(Paint.Align.CENTER);
	    canvas.drawText(text, bitmapSize/2, bitmapSize/2 + textSize*2/5, paint);

	    paint.setTextSize(indexSize);
	    paint.setTextAlign(Paint.Align.LEFT);
	    canvas.drawText(Integer.toString(position), 5, 16, paint);
	    
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
    	// private int lastPosition = -1;

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

        /**
         * Create a view for a data item or a recycled view, if one is passed in.
         * 
         * @param position    The position of the item within the adapter's data set.
         * @param convertView An old view to reuse, if possible.
         * @param parent      The enclosing view
         */
        @Override
        public View getView(final int position, final View convertView, final ViewGroup parent) {

        	ImageView imageView = null;

        	if (convertView instanceof ImageView) {
        		imageView = (ImageView)convertView;
        	}

        	if (imageView == null) {
        		imageView = new ImageView(this.context);
        		// @TODO Instantiate LayoutParams only once.
        		imageView.setLayoutParams(new GridView.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        	}

        	String word = this.words.get(position);
        	imageView.setImageBitmap( DraggableGridViewSampleActivity.getThumb(position, word) );

           return imageView;
        }
    }
}