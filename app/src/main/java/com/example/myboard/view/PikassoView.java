package com.example.myboard.view;

import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;

public class PikassoView extends View {

    public static final float TOUCH_TOLERANCE = 10;
    private Bitmap bitmap;
    private Canvas bitmapCanvas;
    private Paint paintScreen;
    private Paint paintLine;
    private HashMap<Integer, Path> pathMap;
    private HashMap<Integer, Point> previousPointMap;

    public PikassoView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    void init(){

        paintScreen = new Paint();

        paintLine = new Paint();
        paintLine.setAntiAlias(true);
        paintLine.setColor(Color.BLUE);
        paintLine.setStyle(Paint.Style.STROKE);
        paintLine.setStrokeWidth(7);
        paintLine.setStrokeCap(Paint.Cap.ROUND);

        pathMap = new HashMap<>();
        previousPointMap = new HashMap<>();

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        bitmapCanvas = new Canvas(bitmap);
        bitmap.eraseColor(Color.WHITE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(bitmap,0,0,paintScreen);

        for(Integer key:pathMap.keySet()){
            canvas.drawPath(pathMap.get(key),paintLine);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int action = event.getActionMasked(); //event type;
        int actionIndex = event.getActionIndex(); //pointer (finger ,mouse..)

        if(action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_UP){

            touchStarted(event.getX(actionIndex),
                    event.getY(actionIndex),
                    event.getPointerId(actionIndex));



        } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP){

            touchEnded(event.getPointerId(actionIndex));
        } else {

            touchMoved(event);
        }
         invalidate(); //redraws the screen

        return true;
    }

    private void touchMoved(MotionEvent event) {

        for (int i=0; i< event.getPointerCount(); i++){

            int pointerId = event.getPointerId(i);
            int pointerIndex = event.findPointerIndex(pointerId);

            if (pathMap.containsKey(pointerId)){
                float newX = event.getX(pointerIndex);
                float newY = event.getY(pointerIndex);

                Path path = pathMap.get(pointerId);
                Point point = previousPointMap.get(pointerId);

                //Calculate how far the user moved from the last point
                float deltaX = Math.abs(newX - point.x);
                float deltaY = Math.abs(newY - point.y);

                //if the distance is significant enough to be considered as a movement , then..
                if (deltaX >= TOUCH_TOLERANCE || deltaY >= TOUCH_TOLERANCE){

                    //move the path to the new location
                    path.quadTo(point.x , point.y , (newX + point.x)/2, (newY + point.y)/2);

                    //store the new coordinates
                    point.x = (int) newX;
                    point.y = (int) newY;
                }
            }
        }
    }

    public void setDrawingColor(int color){
        paintLine.setColor(color);
    }
    public int getDrawingColor(){
       return paintLine.getColor();
    }
    public void setLineWidth(int width){
        paintLine.setStrokeWidth(width);
    }
    public int getLineWidth(){
        return (int) paintLine.getStrokeWidth();
    }

    public void clear(){
        pathMap.clear(); //removes all the previous paths
        previousPointMap.clear();
        bitmap.eraseColor(Color.WHITE);
        invalidate(); //refresh the screen
    }

    private void touchEnded(int pointerId) {

        Path path = pathMap.get(pointerId); //Get the corresponding path
        bitmapCanvas.drawPath(path, paintLine); //Draw to bitmapCanvas
        path.reset();

    }

    private void touchStarted(float x, float y, int pointerId) {

        Path path; //store the path for given touch
        Point point; //store the last point in path

        if (pathMap.containsKey(pointerId)){
            path = pathMap.get(pointerId);
            point = previousPointMap.get(pointerId);
        } else {
            path = new Path();
            pathMap.put(pointerId, path);

            point = new Point();
            previousPointMap.put(pointerId,point);
        }
        //move to the coordinates of the touch
        path.moveTo(x,y);
        point.x = (int) x;
        point.y = (int) y;
    }

    public void saveImage() throws IOException {
        String fileName = "Picasso" + System.currentTimeMillis();

        //configure the new image data into Content Values
        ContentValues values = new ContentValues();

        values.put(MediaStore.Images.Media.TITLE, fileName);
        values.put(MediaStore.Images.Media.DATE_ADDED,System.currentTimeMillis());
        values.put(MediaStore.Images.Media.MIME_TYPE,"image/jpg");

        //get a URI for the location to save the file
        Uri uri = getContext().getContentResolver().insert(MediaStore.Images.Media.INTERNAL_CONTENT_URI,values);

         try {
             OutputStream outputStream = getContext().getContentResolver().openOutputStream(uri);

             //copy the bitmap to the outputStream
             bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream); //this is our image

             try {
                 outputStream.flush();
                 outputStream.close();

                 Toast.makeText(getContext(), "Image Saved!", Toast.LENGTH_LONG).show();
             }
             catch (IOException e){
                 Toast.makeText(getContext(),"Image not saved!",Toast.LENGTH_LONG).show();
             }

         } catch (FileNotFoundException e){
             Toast.makeText(getContext(),"Image not saved!",Toast.LENGTH_LONG).show();
         }
    }

    public void  saveToInternalStorage(){
        ContextWrapper cw = new ContextWrapper(getContext());
        String fileName = "Picasso" + System.currentTimeMillis();
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath=new File(directory,fileName+".jpg");

        FileOutputStream fos = null;

        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            Toast.makeText(getContext(),"Image saved!"+directory.getAbsolutePath(),Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            Toast.makeText(getContext(),"Image not saved!",Toast.LENGTH_LONG).show();

        } finally {
            try {
                fos.flush();
                fos.close();
                Log.d("Image:",directory.getAbsolutePath());
                Toast.makeText(getContext(),"Image saved!"+directory.getAbsolutePath(),Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                Toast.makeText(getContext(),"Image not saved!",Toast.LENGTH_LONG).show();
            }
        }
//        return directory.getAbsolutePath();
    }

    private void loadImageFromStorage(String path)
    {

        try {
            File f=new File(path, "profile.jpg");
            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
//            ImageView img=(ImageView)findViewById(R.id.imgPicker);
//            img.setImageBitmap(b);
        }
        catch (FileNotFoundException e)
        {
            Toast.makeText(getContext(),"Image not saved!",Toast.LENGTH_LONG).show();
        }

    }
}
