/*
 * DraggableGridView
 * 
 * Portions adapted from
 * http://blogs.sonyericsson.com/wp/2010/05/20/android-tutorial-making-your-own-3d-list-part-1/
 * 
 * TO DO:
 * Improve timer performance (especially on Eee Pad)
 * Improve child rearranging
 */
package com.animoto.android.views;

import java.util.Collections;
import java.util.ArrayList;
import java.util.LinkedList;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Handler;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.view.MotionEvent;
import android.view.View.OnTouchListener;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;


public class DraggableGridView extends AdapterView
							   implements OnTouchListener, OnClickListener, OnLongClickListener {
    /** Represents an invalid child index */
    private static final int INVALID_INDEX = -1;

    /** Distance to drag before we intercept touch events */
    private static final int TOUCH_SCROLL_THRESHOLD = 10;

    /** Children added with this layout mode will be added below the last child */
    private static final int LAYOUT_MODE_BELOW = 0;

    /** Children added with this layout mode will be added above the first child */
    private static final int LAYOUT_MODE_ABOVE = 1;

    /** User is not touching the list */
    private static final int TOUCH_STATE_RESTING = 0;

    /** User is touching the list and right now it's still a "click" */
    private static final int TOUCH_STATE_CLICK = 1;

    /** User is scrolling the list */
    private static final int TOUCH_STATE_SCROLL = 2;

    /**
	 * Adapter with all the data.
	 */
	private Adapter adapter; 

	// Layout
	// Size of grid cell relative to grid cell plus spacing.
	public static float childRatio = .9f;
    protected int columnCount;
    protected int childSize;
    protected int padding;
    protected int dpi;
    protected int scroll = 0;
    protected float lastDelta = 0;
    protected Handler handler = new Handler();

    // Scrolling
    protected int touchStartY;
    protected int listTopStart;
    protected int listTop;
    protected int listTopOffset;

    /** The adapter position of the first visible item */
    private int firstItemPosition;

    /** The adapter position of the last visible item */
    private int lastItemPosition;

    /** A list of cached (re-usable) item views */
    private final LinkedList<View> cachedItemViews = new LinkedList<View>();

    /** Used to check for long press actions */
    private Runnable longPressRunnable;

    /** Reusable rectangle */
    private Rect rectangle;


    // Dragging
    protected int dragged = -1, lastX = -1, lastY = -1, lastTarget = -1;
    protected boolean enabled = true, touching = false;

    // Animation
    public static int animT = 25;
    protected ArrayList<Integer> newPositions = new ArrayList<Integer>();
   
    // Listeners
    protected OnRearrangeListener onRearrangeListener;
    protected OnClickListener secondaryOnClickListener;
    private OnItemClickListener onItemClickListener;
    
    /**
     * Constructor
     * @param context Context information
     * @param attrs attributes of grid view
     */
    public DraggableGridView (Context context, AttributeSet attrs) {

    	super(context, attrs);

    	setListeners();
        // handler.removeCallbacks(updateTask);
        // handler.postAtTime(updateTask, SystemClock.uptimeMillis() + 500);
        setChildrenDrawingOrderEnabled(true);

        DisplayMetrics metrics = new DisplayMetrics();
        ((Activity)context).getWindowManager().getDefaultDisplay().getMetrics(metrics);
		this.dpi = metrics.densityDpi;
    }

    protected void setListeners() {
    	setOnTouchListener(this);
    	//super.setOnClickListener(this);
        setOnLongClickListener(this);
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
    	secondaryOnClickListener = l;
    }

    /*
    protected Runnable updateTask = new Runnable() {
        public void run()
        {
            if (dragged != -1)
            {
            	if (lastY < padding * 3 && scroll > 0)
            		scroll -= 20;
            	else if (lastY > getBottom() - getTop() - (padding * 3) && scroll < getMaxScroll())
            		scroll += 20;
            }
            else if (lastDelta != 0 && !touching)
            {
            	scroll += lastDelta;
            	lastDelta *= .9;
            	if (Math.abs(lastDelta) < .25)
            		lastDelta = 0;
            }
            clampScroll();
            onLayout(true, getLeft(), getTop(), getRight(), getBottom());
        
            handler.postDelayed(this, 25);
        }
    };
    */


    /**
     * Assign a new adapter.
     * Clear all views and request a layout to get and position the new adapter's views.
     *
     * @param adapter Bridge between data and AdapterView layout
     */
    @Override
    public void setAdapter(Adapter adapter) {
    	this.adapter = adapter;
    	removeAllViewsInLayout();
    	requestLayout();
    }
   
    @Override
    public Adapter getAdapter() {
    	return this.adapter;
    }
   
    @Override
    public void setSelection(int position) {
      throw new UnsupportedOperationException("Not supported");
    }
   
    @Override
    public View getSelectedView() {
      throw new UnsupportedOperationException("Not supported");
    }

    /*
     * Lay out views on the screen.
     * 
     * @param changed
     * @param left
     * @param top
     * @param right
     * @param bottom
     * 
     * @see android.widget.AdapterView#onLayout(boolean, int, int, int, int)
     */
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {

    	super.onLayout(changed, left, top, right, bottom);

        // if we don't have an adapter, we don't need to do anything
    	if (this.adapter == null) {
    		return;
    	}

    	// Compute width of view.
        float width = (right - left) / (this.dpi / 160f);

/********
        float w = width;
        int colCount = 2;
        int sub = 240;
        w -= 280;
        while (w > 0)
        {
        	colCount++;
        	w -= sub;
        	sub += 40;
        }
**********/
        // Determine number of columns (at least 2).
        int initialColumnWidth = 95;
        int initialGutterWidth = 5;
        this.columnCount = (int)Math.max(2f, (width + initialGutterWidth) / (initialColumnWidth + initialGutterWidth));

        // Determine childSize and padding, in px.
        this.childSize = (right - left) / this.columnCount;
        this.childSize = Math.round(this.childSize * DraggableGridView.childRatio);
        this.padding = ((right - left) - (this.childSize * this.columnCount)) / (this.columnCount + 1);

        /*
        int childLeft = left;
        int childRight = left + this.childSize;
        int childTop = top;
        int childBottom = top + this.childSize;
        int childAndGutterSize = this.childSize + gutterWidth;
        int itemCount = adapter.getCount();

        for (int i = 0; i < this.adapter.getCount(); i++) {
    	    View child = obtainView(i);
    	    childLeft = left + (i % columnCount) * childAndGutterSize;
    	    childRight = childLeft + this.childSize;
    	    childTop = top + (i / columnCount) * childAndGutterSize;
    	    childBottom = childTop + this.childSize;
    	    child.layout(childLeft, childTop, childRight, childBottom);
    	    addViewInLayout(child, i, child.getLayoutParams(), true);
    	}
  	    this.invalidate();
  	    */

    	// Add child views if we don't have any yet.
    	if (getChildCount() == 0) {
    		int offsetFromTop = 0;
    		this.lastItemPosition = -1;
            fillListDown(this.listTop, offsetFromTop);
        }
    	else {
            final int offset = this.listTop + this.listTopOffset - getChildAt(0).getTop();
            removeNonVisibleViews(offset);
            fillList(offset);
        }
    	// Position the views.
    	positionItems();
    	invalidate();
    }
  
     protected View obtainView(int position) {
    	View child = this.adapter.getView(position, null, this);
    	return child;
	}

    /**
     * Removes view that are outside of the visible part of the list. Will not
     * remove all views.
     * 
     * @param offset Offset of the visible area
     */
    private void removeNonVisibleViews(final int offset) {
        // We need to keep close track of the child count in this function. We
        // should never remove all the views, because if we do, we loose track
        // of were we are.
        int childCount = getChildCount();

        // if we are not at the bottom of the list and have more than one child
        if (this.lastItemPosition != this.adapter.getCount() - 1 && childCount > 1) {
            // check if we should remove any views in the top
            View firstChild = getChildAt(0);
            while (firstChild != null && firstChild.getBottom() + offset < 0) {
                // remove the top view
                removeViewInLayout(firstChild);
                childCount--;
                this.cachedItemViews.addLast(firstChild);
                this.firstItemPosition++;

                // update the list offset (since we've removed the top child)
                this.listTopOffset += firstChild.getMeasuredHeight();

                // Continue to check the next child only if we have more than
                // one child left
                if (childCount > 1) {
                    firstChild = getChildAt(0);
                } else {
                    firstChild = null;
                }
            }
        }

        // if we are not at the top of the list and have more than one child
        if (this.firstItemPosition != 0 && childCount > 1) {
            // check if we should remove any views in the bottom
            View lastChild = getChildAt(childCount - 1);
            while (lastChild != null && lastChild.getTop() + offset > getHeight()) {
                // remove the bottom view
                removeViewInLayout(lastChild);
                childCount--;
                this.cachedItemViews.addLast(lastChild);
                this.lastItemPosition--;

                // Continue to check the next child only if we have more than
                // one child left
                if (childCount > 1) {
                    lastChild = getChildAt(childCount - 1);
                } else {
                    lastChild = null;
                }
            }
        }
    }

    /**
     * Fills the list with child-views
     * 
     * @param offset Offset of the visible area
     */
    private void fillList(final int offset) {
        final int bottomEdge = getChildAt(getChildCount() - 1).getBottom();
        fillListDown(bottomEdge, offset);

        final int topEdge = getChildAt(0).getTop();
        fillListUp(topEdge, offset);
    }

    /**
     * Starts at the top and adds children until we've passed the list bottom
     * 
     * @param bottomEdge The bottom edge of the currently last child
     * @param offset Distance of the visible area from the top of the grid
     */
    private void fillListDown(int bottomEdge, final int offset) {
    	
    	int viewHeight = getHeight();
    	int itemCount = this.adapter.getCount();

  	    while (bottomEdge + offset < viewHeight &&
        	   this.lastItemPosition < itemCount - 1) {
            this.lastItemPosition++;
            Point topLeft = getCoordinatesFromIndex(lastItemPosition);
            final View newBottomChild = this.adapter.getView(this.lastItemPosition, getCachedView(), this);
            addAndMeasureChild(newBottomChild, LAYOUT_MODE_BELOW);
            newBottomChild.layout(topLeft.x, topLeft.y, topLeft.x + this.childSize, topLeft.y + childSize);
            bottomEdge = newBottomChild.getTop() + newBottomChild.getHeight();
        }
    }

    /**
     * Starts at the top and adds children until we've passed the list top
     * 
     * @param topEdge The top edge of the currently first child
     * @param offset Offset of the visible area
     */
    private void fillListUp(int topEdge, final int offset) {
        while (topEdge + offset > 0 && this.firstItemPosition > 0) {
            this.firstItemPosition--;
            final View newTopChild = this.adapter.getView(this.firstItemPosition, getCachedView(), this);
            addAndMeasureChild(newTopChild, LAYOUT_MODE_ABOVE);
            final int childHeight = newTopChild.getMeasuredHeight();
            topEdge -= childHeight;

            // update the list offset (since we added a view at the top)
            this.listTopOffset -= childHeight;
        }
    }

    
    /**
     * Add a view as a child and measure it.
     *
     * @param child The view to add
     * @param layoutMode Either LAYOUT_MODE_ABOVE or LAYOUT_MODE_BELOW
     */
    private void addAndMeasureChild(final View child, final int layoutMode) {

    	ViewGroup.LayoutParams params = child.getLayoutParams();
        if (params == null) {
            params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        }
        final int index = layoutMode == LAYOUT_MODE_ABOVE ? 0 : -1;
        addViewInLayout(child, index, params, true);

        // final int itemWidth = getWidth();
        // child.measure(MeasureSpec.EXACTLY | itemWidth, MeasureSpec.UNSPECIFIED);
        child.measure(this.childSize, this.childSize);
    }

    @Override
    public boolean onInterceptTouchEvent(final MotionEvent event) {
    /*
      switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
          startTouch(event);
          return false;
     
        case MotionEvent.ACTION_MOVE:
          return startScrollIfNeeded(event);
     
        default:
          endTouch();
          return false;
      }
      */
      return super.onInterceptTouchEvent(event);
    }
    
    /**
     * Position the child views.
     */
    private void positionItems() {

    	int top = 0;
     
    	for (int index = 0; index < getChildCount(); index++) {
    		View child = getChildAt(index);
    		Point topLeft = getCoordinatesFromIndex(index);
     
    		// int width = child.getMeasuredWidth();
    		// int height = child.getMeasuredHeight();

    		child.layout(topLeft.x, topLeft.y, topLeft.x + this.childSize, topLeft.y + this.childSize);
    		top += this.childSize;
    	}
    }

    
    /**
     * Allow scrolling by touching the screen.
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
      if (getChildCount() == 0) {
        return false;
      }
      switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
          this.touchStartY = (int)event.getY();
          this.listTopStart = getChildAt(0).getTop();
          break;
     
        case MotionEvent.ACTION_MOVE:
          int scrolledDistance = (int)event.getY() - this.touchStartY;
          this.listTop = this.listTopStart + scrolledDistance;
          requestLayout();
          break;
     
        default:
          break;
      }
      return true;
    }
    
    
    //LAYOUT
    /*
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
    	//compute width of view, in dp
        float w = (r - l) / (dpi / 160f);
        
        //determine number of columns, at least 2
        colCount = 2;
        int sub = 240;
        w -= 280;
        while (w > 0)
        {
        	colCount++;
        	w -= sub;
        	sub += 40;
        }
        
        //determine childSize and padding, in px
        childSize = (r - l) / colCount;
        childSize = Math.round(childSize * childRatio);
        padding = ((r - l) - (childSize * colCount)) / (colCount + 1);
    	
        for (int i = 0; i < getChildCount(); i++)
        	if (i != dragged)
        	{
	            Point xy = getCoorFromIndex(i);
	            getChildAt(i).layout(xy.x, xy.y, xy.x + childSize, xy.y + childSize);
        	}
    }
*/
    
    @Override
    public void addView(View child) {
    	super.addView(child);
    	newPositions.add(-1);
    };
    @Override
    public void removeViewAt(int index) {
    	super.removeViewAt(index);
    	newPositions.remove(index);
    };
    
    @Override
    protected int getChildDrawingOrder(int childCount, int i) {
    	if (dragged == -1)
    		return i;
    	else if (i == childCount - 1)
    		return dragged;
    	else if (i >= dragged)
    		return i + 1;
    	return i;
    }
    public int getIndexFromCoor(int x, int y)
    {
        int col = getColOrRowFromCoor(x), row = getColOrRowFromCoor(y + scroll); 
        if (col == -1 || row == -1) //touch is between columns or rows
            return -1;
        int index = row * this.columnCount + col;
        if (index >= getChildCount())
            return -1;
        return index;
    }
    protected int getColOrRowFromCoor(int coor)
    {
        coor -= padding;
        for (int i = 0; coor > 0; i++)
        {
            if (coor < childSize)
                return i;
            coor -= (childSize + padding);
        }
        return -1;
    }
    protected int getTargetFromCoor(int x, int y)
    {
        if (getColOrRowFromCoor(y + scroll) == -1) //touch is between rows
            return -1;
        //if (getIndexFromCoor(x, y) != -1) //touch on top of another visual
            //return -1;
        
        int leftPos = getIndexFromCoor(x - (childSize / 4), y);
        int rightPos = getIndexFromCoor(x + (childSize / 4), y);
        if (leftPos == -1 && rightPos == -1) //touch is in the middle of nowhere
            return -1;
        if (leftPos == rightPos) //touch is in the middle of a visual
        	return -1;
        
        int target = -1;
        if (rightPos > -1)
            target = rightPos;
        else if (leftPos > -1)
            target = leftPos + 1;
        if (dragged < target)
            return target - 1;
        
        //Toast.makeText(getContext(), "Target: " + target + ".", Toast.LENGTH_SHORT).show();
        return target;
    }
    
    
    /**
     * Get the position of a view based on its array index.
     * 
     * @return top left position
     */
    protected Point getCoordinatesFromIndex(int index)
    {
        int column = index % this.columnCount;
        int row    = index / this.columnCount;
        return new Point(padding + (this.childSize + padding) * column,
                         padding + (this.childSize + padding) * row - scroll);
    }
    
    
    public int getIndexOf(View child)
    {
    	for (int i = 0; i < getChildCount(); i++)
    		if (getChildAt(i) == child)
    			return i;
    	return -1;
    }
    
    //EVENT HANDLERS
    public void onClick(View view) {
    	if (enabled)
    	{
    		if (secondaryOnClickListener != null)
    			secondaryOnClickListener.onClick(view);
    		if (onItemClickListener != null && getLastIndex() != -1)
    			onItemClickListener.onItemClick(null,
    											getChildAt(getLastIndex()),
    											getLastIndex(),
    											getLastIndex() / this.columnCount);
    	}
    }
    public boolean onLongClick(View view)
    {
    	if (!enabled)
    		return false;
        int index = getLastIndex();
        if (index != -1)
        {
            dragged = index;
            animateDragged();
            return true;
        }
        return false;
    }
    public boolean onTouch(View view, MotionEvent event)
    {
        int action = event.getAction();
           switch (action & MotionEvent.ACTION_MASK) {
               case MotionEvent.ACTION_DOWN:
            	   enabled = true;
                   lastX = (int) event.getX();
                   lastY = (int) event.getY();
                   touching = true;
                   break;
               case MotionEvent.ACTION_MOVE:
            	   int delta = lastY - (int)event.getY();
                   if (dragged != -1)
                   {
                       //change draw location of dragged visual
                       int x = (int)event.getX(), y = (int)event.getY();
                       int l = x - (3 * childSize / 4), t = y - (3 * childSize / 4);
                       getChildAt(dragged).layout(l, t, l + (childSize * 3 / 2), t + (childSize * 3 / 2));
                       
                       //check for new target hover
                       int target = getTargetFromCoor(x, y);
                       if (lastTarget != target)
                       {
                           if (target != -1)
                           {
                               animateGap(target);
                               lastTarget = target;
                           }
                       }
                   }
                   else
                   {
                	   scroll += delta;
                	   clampScroll();
                	   if (Math.abs(delta) > 2)
                    	   enabled = false;
                	   onLayout(true, getLeft(), getTop(), getRight(), getBottom());
                   }
                   lastX = (int) event.getX();
                   lastY = (int) event.getY();
                   lastDelta = delta;
                   break;
               case MotionEvent.ACTION_UP:
                   if (dragged != -1)
                   {
                	   View v = getChildAt(dragged);
                       if (lastTarget != -1)
                           reorderChildren();
                       else
                       {
                           Point xy = getCoordinatesFromIndex(dragged);
                           v.layout(xy.x, xy.y, xy.x + childSize, xy.y + childSize);
                       }
                       v.clearAnimation();
                       if (v instanceof ImageView)
                    	   ((ImageView)v).setAlpha(255);
                       lastTarget = -1;
                       dragged = -1;
                   }
                   touching = false;
                   break;
           }
        if (dragged != -1)
        	return true;
        return false;
    }
    
    //EVENT HELPERS
    protected void animateDragged()
    {
    	View v = getChildAt(dragged);
    	int x = getCoordinatesFromIndex(dragged).x + childSize / 2, y = getCoordinatesFromIndex(dragged).y + childSize / 2;
        int l = x - (3 * childSize / 4), t = y - (3 * childSize / 4);
    	v.layout(l, t, l + (childSize * 3 / 2), t + (childSize * 3 / 2));
    	AnimationSet animSet = new AnimationSet(true);
		ScaleAnimation scale = new ScaleAnimation(.667f, 1, .667f, 1, childSize * 3 / 4, childSize * 3 / 4);
		scale.setDuration(animT);
		AlphaAnimation alpha = new AlphaAnimation(1, .5f);
		alpha.setDuration(animT);

		animSet.addAnimation(scale);
		animSet.addAnimation(alpha);
		animSet.setFillEnabled(true);
		animSet.setFillAfter(true);
		
		v.clearAnimation();
		v.startAnimation(animSet);
    }
    protected void animateGap(int target)
    {
    	for (int i = 0; i < getChildCount(); i++)
    	{
    		View v = getChildAt(i);
    		if (i == dragged)
	    		continue;
    		int newPos = i;
    		if (dragged < target && i >= dragged + 1 && i <= target)
    			newPos--;
    		else if (target < dragged && i >= target && i < dragged)
    			newPos++;
    		
    		//animate
    		int oldPos = i;
    		if (newPositions.get(i) != -1)
    			oldPos = newPositions.get(i);
    		if (oldPos == newPos)
    			continue;
    		
    		Point oldXY = getCoordinatesFromIndex(oldPos);
    		Point newXY = getCoordinatesFromIndex(newPos);
    		Point oldOffset = new Point(oldXY.x - v.getLeft(), oldXY.y - v.getTop());
    		Point newOffset = new Point(newXY.x - v.getLeft(), newXY.y - v.getTop());
    		
    		TranslateAnimation translate = new TranslateAnimation(Animation.ABSOLUTE, oldOffset.x,
																  Animation.ABSOLUTE, newOffset.x,
																  Animation.ABSOLUTE, oldOffset.y,
																  Animation.ABSOLUTE, newOffset.y);
			translate.setDuration(animT);
			translate.setFillEnabled(true);
			translate.setFillAfter(true);
			v.clearAnimation();
			v.startAnimation(translate);
    		
			newPositions.set(i, newPos);
    	}
    }
    protected void reorderChildren()
    {
        //FIGURE OUT HOW TO REORDER CHILDREN WITHOUT REMOVING THEM ALL AND RECONSTRUCTING THE LIST!!!
    	if (onRearrangeListener != null)
    		onRearrangeListener.onRearrange(dragged, lastTarget);
        ArrayList<View> children = new ArrayList<View>();
        for (int i = 0; i < getChildCount(); i++)
        {
        	getChildAt(i).clearAnimation();
            children.add(getChildAt(i));
        }
        removeAllViews();
        while (dragged != lastTarget)
            if (lastTarget == children.size()) // dragged and dropped to the right of the last element
            {
                children.add(children.remove(dragged));
                dragged = lastTarget;
            }
            else if (dragged < lastTarget) // shift to the right
            {
                Collections.swap(children, dragged, dragged + 1);
                dragged++;
            }
            else if (dragged > lastTarget) // shift to the left
            {
                Collections.swap(children, dragged, dragged - 1);
                dragged--;
            }
        for (int i = 0; i < children.size(); i++)
        {
        	newPositions.set(i, -1);
            addView(children.get(i));
        }
        onLayout(true, getLeft(), getTop(), getRight(), getBottom());
    }
    public void scrollToTop()
    {
    	scroll = 0;
    }
    public void scrollToBottom()
    {
    	scroll = Integer.MAX_VALUE;
    	clampScroll();
    }
    protected void clampScroll()
    {
    	int stretch = 3, overreach = getHeight() / 2;
    	int max = getMaxScroll();
    	max = Math.max(max, 0);
    	
    	if (scroll < -overreach)
    	{
    		scroll = -overreach;
    		lastDelta = 0;
    	}
    	else if (scroll > max + overreach)
    	{
    		scroll = max + overreach;
    		lastDelta = 0;
    	}
    	else if (scroll < 0)
    	{
	    	if (scroll >= -stretch)
	    		scroll = 0;
	    	else if (!touching)
	    		scroll -= scroll / stretch;
    	}
    	else if (scroll > max)
    	{
    		if (scroll <= max + stretch)
    			scroll = max;
    		else if (!touching)
    			scroll += (max - scroll) / stretch;
    	}
    }
    protected int getMaxScroll()
    {
    	int rowCount = (int)Math.ceil((double)getChildCount() / this.columnCount),
    			                      max = rowCount * childSize + (rowCount + 1) * padding - getHeight();
    	return max;
    }
    public int getLastIndex()
    {
    	return getIndexFromCoor(lastX, lastY);
    }
    
    //OTHER METHODS
    public void setOnRearrangeListener(OnRearrangeListener l)
    {
    	this.onRearrangeListener = l;
    }
    public void setOnItemClickListener(OnItemClickListener l)
    {
    	this.onItemClickListener = l;
    }

    /**
     * Check for a cached view that may be used.
     * 
     * @return A cached view or, if none was found, null
     */
    private View getCachedView() {
        if (this.cachedItemViews.size() != 0) {
            return this.cachedItemViews.removeFirst();
        }
        return null;
    }

}