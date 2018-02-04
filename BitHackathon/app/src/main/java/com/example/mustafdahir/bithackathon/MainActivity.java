package com.example.mustafdahir.bithackathon;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.media.Image;
import android.net.Uri;

import java.util.Locale;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;

import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.content.Intent;

public class MainActivity extends AppCompatActivity {
    private int MY_DATA_CHECK_CODE = 0;
    private TextToSpeech tts;
    private ImageButton cameraButton;
    private Button synthButton;
    private EditText textBox;
    private ImageView mImageView;
    private Camera mCamera;
    int REQUEST_IMAGE_CAPTURE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //camera permission check
        ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, 100);

        cameraButton = (ImageButton) findViewById(R.id.cameraButton);
        mImageView = (ImageView) findViewById(R.id.imageView);
        synthButton = (Button) findViewById(R.id.synthButton);
        textBox = (EditText) findViewById(R.id.textBox);

        Intent checkTTSIntent = new Intent();
        checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);
        setUpCamera();

    }


    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }
    public void startSynth(CharSequence text){
        Log.e("ok", ""+text);
    }

    public void setUpSynth() {
        //TODO
    }

    public void setUpCamera() {
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("ok", "pressed");
                dispatchTakePictureIntent();

            }

        });
        synthButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("ok", "pressed");
                Log.e( "yes", ""+textBox.getText());
                tts.speak(textBox.getText().toString(), TextToSpeech.QUEUE_FLUSH, null);
                startSynth(textBox.getText());
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            mImageView.setImageBitmap(imageBitmap);
           getStringImage(imageBitmap);
        }
        if (requestCode == MY_DATA_CHECK_CODE) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                tts = new TextToSpeech(MainActivity.this, new TextToSpeech.OnInitListener() {

                    @Override
                    public void onInit(int status) {

                        Log.d("text to speech", "init");

                        if(status != TextToSpeech.ERROR)
                        {
                            Log.d("text to speech", "condition");

                            tts.setPitch(1.1f);

                            tts.setSpeechRate(0.4f);

                            tts.setLanguage(Locale.US);
                        }}});
            }
            else {
                Intent installTTSIntent = new Intent();
                installTTSIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installTTSIntent);
            }
        }
    }

    public String getStringImage(Bitmap image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        Log.d("tag", encodedImage);
        return encodedImage;
    }
}
