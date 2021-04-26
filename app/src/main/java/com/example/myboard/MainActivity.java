package com.example.myboard;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import com.example.myboard.view.PikassoView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private PikassoView pikassoView;
    private MaterialAlertDialogBuilder currentAlertDialog;
    private ImageView widthImageView;
    private AlertDialog dialogLineWidth;
    private AlertDialog colorDialog;
    private SeekBar alphaSeekBar;
    private SeekBar redSeekBar;
    private SeekBar greenSeekBar;
    private SeekBar blueSeekBar;
    private View colorView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pikassoView = findViewById(R.id.pikasso_view);
        pikassoView.setBackgroundColor(Color.WHITE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.clear:
                pikassoView.clear();
                break;
            case R.id.save:
                pikassoView.saveToInternalStorage();
                break;
            case R.id.lineWidth:
                showLineWidthDialog();
                break;
            case R.id.color:
                showColorDialog();
                break;
        }


        return super.onOptionsItemSelected(item);
    }

    public void showColorDialog(){
        currentAlertDialog = new MaterialAlertDialogBuilder(this);
        View view = getLayoutInflater().inflate(R.layout.color_dialog,null);

         alphaSeekBar = view.findViewById(R.id.alphaSeekBar);
         redSeekBar = view.findViewById(R.id.redSeekBar);
         greenSeekBar = view.findViewById(R.id.greenSeekBar);
         blueSeekBar = view.findViewById(R.id.blueSeekBar);

         colorView = view.findViewById(R.id.colorView);

        //register seekBar event listeners
        alphaSeekBar.setOnSeekBarChangeListener(colorSeekBarChanged);
        redSeekBar.setOnSeekBarChangeListener(colorSeekBarChanged);
        greenSeekBar.setOnSeekBarChangeListener(colorSeekBarChanged);
        blueSeekBar.setOnSeekBarChangeListener(colorSeekBarChanged);

        int color = pikassoView.getDrawingColor();
        alphaSeekBar.setProgress(Color.alpha(color));
        redSeekBar.setProgress(Color.red(color));
        greenSeekBar.setProgress(Color.green(color));
        blueSeekBar.setProgress(Color.blue(color));

        Button setColorButton = view.findViewById(R.id.setColorButton);
        setColorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pikassoView.setDrawingColor(Color.argb(
                        alphaSeekBar.getProgress(),
                        redSeekBar.getProgress(),
                        greenSeekBar.getProgress(),
                        blueSeekBar.getProgress()
                ));
                colorDialog.dismiss();
                currentAlertDialog = null;
            }
        });
        currentAlertDialog.setView(view);
        currentAlertDialog.setTitle("Set Color");
        colorDialog = currentAlertDialog.create();
        colorDialog.show();
    }

    private final SeekBar.OnSeekBarChangeListener colorSeekBarChanged = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            pikassoView.setBackgroundColor(Color.argb(
                    alphaSeekBar.getProgress(),
                    redSeekBar.getProgress(),
                    greenSeekBar.getProgress(),
                    blueSeekBar.getProgress()
            ));

            //display the current color
            colorView.setBackgroundColor(Color.argb(
                    alphaSeekBar.getProgress(),
                    redSeekBar.getProgress(),
                    greenSeekBar.getProgress(),
                    blueSeekBar.getProgress()
            ));
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    public void showLineWidthDialog(){
        currentAlertDialog = new MaterialAlertDialogBuilder(this);
        View view = getLayoutInflater().inflate(R.layout.width_dialogue,null);

        SeekBar widthSeekBar = view.findViewById(R.id.widthSeekBar);
        Button setLineWidthButton = view.findViewById(R.id.widthDialogButton);
        widthImageView = view.findViewById(R.id.imageView);

        setLineWidthButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pikassoView.setLineWidth(widthSeekBar.getProgress());
                dialogLineWidth.dismiss();
                currentAlertDialog = null;
            }
        });

        widthSeekBar.setOnSeekBarChangeListener(widthSeekBarChanged);
        widthSeekBar.setProgress(pikassoView.getLineWidth());

        currentAlertDialog.setView(view);
        dialogLineWidth = currentAlertDialog.create();
        dialogLineWidth.setTitle("Set Line Width");
        dialogLineWidth.show();
    }

    private SeekBar.OnSeekBarChangeListener widthSeekBarChanged = new SeekBar.OnSeekBarChangeListener() {

        Bitmap bitmap = Bitmap.createBitmap(400,100, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            Paint p = new Paint();
            p.setColor(pikassoView.getDrawingColor());
            p.setStrokeWidth(progress);
            p.setStrokeCap(Paint.Cap.ROUND);

            bitmap.eraseColor(Color.WHITE);
            canvas.drawLine(30,50,370,50,p);
            widthImageView.setImageBitmap(bitmap);

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {


        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

}