package com.example.mustafdahir.bithackathon;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;

import java.net.URL;
import java.net.URLEncoder;
import java.util.*;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;

import android.widget.ImageButton;
import android.widget.ImageView;

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

import android.speech.tts.TextToSpeech;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import java.io.*;
import java.util.Arrays;
import javax.net.*;
import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {
    private int MY_DATA_CHECK_CODE = 0;
    private TextToSpeech mTextToSpeech;
    private ImageButton mCameraButton;
    private ImageButton mSynthButton;
    private ImageButton mTranslateButton;
    private TextView mTextBox;
    private static final String API_KEY = "AIzaSyBK41jlj84lFA83tCPz8JAlBMgXm8lK7Gc";
    private static final String MICROSOFT_API_KEY = "2252afd5d18245e5815d95012c3df3f5";
    private static final String TAG = "tag";
    private static final int REQUEST_TAKE_PHOTO = 1;
    private ImageView mImageView;
    private int REQUEST_IMAGE_CAPTURE = 1;
    private String mImageText;
    private ProgressBar mSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //camera permission check
        ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, 100);

        mSynthButton =  findViewById(R.id.synthButton);
        mTextBox =  findViewById(R.id.textBox);
        mImageView = findViewById(R.id.imageView);
        mCameraButton = findViewById(R.id.cameraButton);
        mTranslateButton = findViewById(R.id.translate_button);

        mSpinner = findViewById(R.id.progressBar1);
        mSpinner.setVisibility(View.GONE);

        mTextBox.setMovementMethod(new ScrollingMovementMethod());

        Intent checkTTSIntent = new Intent();
        checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);
        setUpCamera();
        setUpSynth();
        setUpTranslate();

    }

    public void setUpSynth() {
        mSynthButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mTextBox.getText().toString().isEmpty())
                    mTextToSpeech.speak(mTextBox.getText().toString(), TextToSpeech.QUEUE_FLUSH, null);
                else
                    Toast.makeText(MainActivity.this, "Text Box is empty", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void setUpCamera() {
        mCameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("ok", "pressed");
                dispatchTakePictureIntent();
            }

        });
    }

    public void setUpTranslate() {
        mTranslateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (!mTextBox.getText().toString().isEmpty()) {
                        AsyncTask<String, Void, String> task = new TranslationTask();
                        task.execute(mTextBox.getText().toString());
                    } else {
                        Toast.makeText(MainActivity.this, "Text Box is empty", Toast.LENGTH_SHORT).show();
                    }

                } catch (Exception e) {
                    Log.d(TAG, "Translation error" + e.toString());
                }
            }
        });
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
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
        else if (requestCode == MY_DATA_CHECK_CODE) {
                if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                    mTextToSpeech = new TextToSpeech(MainActivity.this, new TextToSpeech.OnInitListener() {

                        @Override
                        public void onInit(int status) {
                            Log.d("text to speech", "init");
                            if(status != TextToSpeech.ERROR) {
                                Log.d("text to speech", "condition");
                                mTextToSpeech.setPitch(1.0f);
                                mTextToSpeech.setSpeechRate(0.8f);
                                mTextToSpeech.setLanguage(Locale.US);
                            }}});
                } else {
                    Intent installTTSIntent = new Intent();
                    installTTSIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                    startActivity(installTTSIntent);
                }
        }

    }

    public byte[] getStringImage(Bitmap image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        return baos.toByteArray();
    }

    private class VisionTask extends AsyncTask<byte[], Void, String> {

        @Override
        protected String doInBackground(byte[]... bytes) {
            if (bytes.length != 1) {
                throw new IllegalArgumentException("One byte array argument required.");
            }

            mSpinner.setVisibility(View.VISIBLE);
            mCameraButton.setVisibility(View.GONE);
            mSynthButton.setVisibility(View.GONE);
            mImageView.setVisibility(View.GONE);
            mTextBox.setVisibility(View.GONE);
            mTranslateButton.setVisibility(View.GONE);

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
                desiredFeature.setType("DOCUMENT_TEXT_DETECTION");

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

                Log.d(TAG, "cloud vision return:" + text.getText());
                mImageText = text.getText();

            } catch (GoogleJsonResponseException e) {
                Log.d(TAG, "failed to make API request because " + e.getContent());
            } catch (IOException e) {
                Log.d(TAG, "failed to make API request because of other IOException " +
                        e.getMessage());
            }
                return "Cloud Vision API request failed. Check logs for details.";
        }

        protected void onPostExecute(String result) {
            mTextBox.setText(mImageText);
            mSpinner.setVisibility(View.GONE);
            mCameraButton.setVisibility(View.VISIBLE);
            mImageView.setVisibility(View.VISIBLE);
            mSynthButton.setVisibility(View.VISIBLE);
            mTextBox.setVisibility(View.VISIBLE);
            mTranslateButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onPause() {
        if (mTextToSpeech != null) {
            mTextToSpeech.stop();
        }

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (mTextToSpeech != null) {
            mTextToSpeech.stop();
            mTextToSpeech.shutdown();
        }

        super.onDestroy();
    }


    public static String translate (String text) throws Exception {
        String host = "https://api.microsofttranslator.com";
        String path = "/V2/Http.svc/Translate";
        String target = "en";

//        String encoded_query = URLEncoder.encode (text, "UTF-8");
        String params = "?to=" + target + "&text=" + text;
        URL url = new URL (host + path + params);

        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Ocp-Apim-Subscription-Key", MICROSOFT_API_KEY);
        connection.setDoOutput(true);

        StringBuilder response = new StringBuilder ();
        BufferedReader in = new BufferedReader(
                new InputStreamReader(connection.getInputStream()));
        String line;
        while ((line = in.readLine()) != null) {
            response.append(line);
        }
        in.close();

        return response.toString();
    }

    private class TranslationTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            if (strings.length != 1) {
                throw new IllegalArgumentException("One string argument required.");
            }

            String host = "https://api.microsofttranslator.com";
            String path = "/V2/Http.svc/Translate";
            String target = "fr-fr";

//            mSpinner.setVisibility(View.VISIBLE);
//            mCameraButton.setVisibility(View.GONE);
//            mSynthButton.setVisibility(View.GONE);
//            mImageView.setVisibility(View.GONE);
//            mTextBox.setVisibility(View.GONE);
//            mTranslateButton.setVisibility(View.GONE);

            try {
                String text2 = strings[0];
////                String encoded_query = URLEncoder.encode (text, "UTF-8");
//                String params = "?to=" + target + "&text=" + text;
//                URL url = new URL (host + path + params);
//
//                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
//                connection.setRequestMethod("GET");
//                connection.setRequestProperty("Ocp-Apim-Subscription-Key", MICROSOFT_API_KEY);
//                connection.setDoOutput(true);
//
//                StringBuilder response = new StringBuilder ();
//                BufferedReader in = new BufferedReader(
//                        new InputStreamReader(connection.getInputStream()));
//                String line;
//                while ((line = in.readLine()) != null) {
//                    response.append(line);
//                }
//                in.close();
//
//                mImageText = response.toString();
//                Log.d(TAG, "Microsoft Translation:" + response.toString());
                // Get the access token
// The key got from Azure portal, please see https://docs.microsoft.com/en-us/azure/cognitive-services/cognitive-services-apis-create-account
//                String key = "<your translator account key>";
                String authenticationUrl = "https://api.cognitive.microsoft.com/sts/v1.0/issueToken";
                HttpsURLConnection authConn = (HttpsURLConnection) new URL(authenticationUrl).openConnection();
                authConn.setRequestMethod("POST");
                authConn.setDoOutput(true);
                authConn.setRequestProperty("Ocp-Apim-Subscription-Key", MICROSOFT_API_KEY);
                IOUtils.write("", authConn.getOutputStream(), "UTF-8");
                String token = IOUtils.toString(authConn.getInputStream(), "UTF-8");
//                System.out.println(token);
                Log.d(TAG, "Microsoft Translation Token: " + token);

// Using the access token to build the appid for the request url
                String appId = URLEncoder.encode("Bearer "+token, "UTF-8");
                String text = URLEncoder.encode(text2, "UTF-8");
                String to = "en";
                String translatorTextApiUrl = String.format("https://api.microsofttranslator.com/v2/http.svc/Translate?appid=%s&text=%s&to=%s", appId, text, to);
                HttpsURLConnection translateConn = (HttpsURLConnection) new URL(translatorTextApiUrl).openConnection();
                translateConn.setRequestMethod("GET");
                translateConn.setRequestProperty("Accept", "application/xml");
                String resp = IOUtils.toString(translateConn.getInputStream(), "UTF-8");
//                System.out.println(resp);


                Log.d(TAG, "Microsoft Translation Response: " + resp);

                int beg = "<string xmlns=\"http://schemas.microsoft.com/2003/10/Serialization/\">".length();
//                String end = "</string>";

                String newResp[] = resp.split("<+string+>")[0].split("</+string+>");
                String newResp2 = newResp[0].substring(beg);

                Log.d(TAG, "Microsoft Translation Response: " + newResp2);
                mImageText = newResp2;


            } catch (Exception e) {
                Log.d(TAG, "Exception in Translation:" + e.toString());
            }


            return "Microsoft Translation API request failed. Check logs for details.";
        }

        protected void onPostExecute(String result) {
            mTextBox.setText(mImageText);
//            mSpinner.setVisibility(View.GONE);
//            mCameraButton.setVisibility(View.VISIBLE);
//            mImageView.setVisibility(View.VISIBLE);
//            mSynthButton.setVisibility(View.VISIBLE);
//            mTextBox.setVisibility(View.VISIBLE);
//            mTranslateButton.setVisibility(View.VISIBLE);
        }
    }
}