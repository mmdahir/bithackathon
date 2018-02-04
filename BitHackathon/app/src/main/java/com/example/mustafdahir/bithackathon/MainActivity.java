package com.example.mustafdahir.bithackathon;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;
import com.google.api.services.vision.v1.model.TextAnnotation;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private Button cameraButton;
    private Button synthButon;
    private ImageView mImageView;
    private Camera mCamera;
    int REQUEST_IMAGE_CAPTURE = 1;
    private static final String API_KEY = "AIzaSyBK41jlj84lFA83tCPz8JAlBMgXm8lK7Gc";
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //camera permission check
        ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, 100);

        cameraButton = (Button) findViewById(R.id.cameraButton);
        mImageView = (ImageView) findViewById(R.id.imageView);
        setUpCamera();

    }


    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            mImageView.setImageBitmap(imageBitmap);
            AsyncTask<byte[], Void, String> task = new VisionTask();
            task.execute(getStringImage(imageBitmap));
        }
    }

    public byte[] getStringImage(Bitmap image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
//        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
//        Log.d("tag", encodedImage);
        return imageBytes;
    }

    private class VisionTask extends AsyncTask<byte[], Void, String> {

        @Override
        protected String doInBackground(byte[]... bytes) {
            if (bytes.length != 1) {
                throw new IllegalArgumentException("One Credentials argument required.");
            }

            byte[] image64String = bytes[0];

            try {
                Vision.Builder visionBuilder = new Vision.Builder(
                        new NetHttpTransport(),
                        new AndroidJsonFactory(),
                        null);

                visionBuilder.setVisionRequestInitializer(
                        new VisionRequestInitializer(API_KEY));

                Vision vision = visionBuilder.build();

                Feature desiredFeature = new Feature();
                desiredFeature.setType("TEXT_DETECTION");

                Image inputImage = new Image();
                inputImage.encodeContent(image64String);

                AnnotateImageRequest request = new AnnotateImageRequest();
                request.setImage(inputImage);
                request.setFeatures(Arrays.asList(desiredFeature));

                BatchAnnotateImagesRequest batchRequest =
                        new BatchAnnotateImagesRequest();

                batchRequest.setRequests(Arrays.asList(request));


                BatchAnnotateImagesResponse batchResponse =
                        vision.images().annotate(batchRequest).execute();

                final TextAnnotation text = batchResponse.getResponses()
                        .get(0).getFullTextAnnotation();

//                Toast.makeText(getApplicationContext(),
//                        text.getText(), Toast.LENGTH_LONG).show();
                Log.d(TAG, text.getText());

            } catch (GoogleJsonResponseException e) {
                Log.d(TAG, "failed to make API request because " + e.getContent());
            } catch (IOException e) {
                Log.d(TAG, "failed to make API request because of other IOException " +
                        e.getMessage());
            }
                return "Cloud Vision API request failed. Check logs for details.";
        }

        protected void onPostExecute(String result) {
            //set text
        }
    }
}
