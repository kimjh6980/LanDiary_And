package com.example.jinhyukkim.landiary_and;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class landMark extends AppCompatActivity {

    Bitmap ImageBitmap;
    ImageView camimg;
    TextView landmark_t;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_land_mark);

        cameraShoot();

        camimg = findViewById(R.id.camimg);
        landmark_t = findViewById(R.id.landmark_t);
    }

    private void cameraShoot() {
        Intent cam = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cam, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 100)  {
            try {
                ImageBitmap = (Bitmap)data.getExtras().get("data");
                camimg.setImageBitmap(ImageBitmap);
                doAnalyze();
            } catch (Exception e)   {
                return;
            }
        }
    }

    private void doAnalyze() {
        try {
            new doRequest().execute();
        } catch (Exception e)
        {
            landmark_t.setText("Error encountered. Exception is: " + e.toString());
        }
    }

    private String process() throws VisionServiceException, IOException {
        Gson gson = new Gson();
        String[] features = {"ImageType", "Color", "Faces", "Adult", "Categories"};
        String[] details = {"celebrities","landmarks"};

        // Put the image into an input stream for detection.
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(output.toByteArray());

        AnalysisResult v = this.client.analyzeImage(inputStream, features, details);

        String result = gson.toJson(v);
        Log.d("result", result);

        return result;
    }

    private class doRequest extends AsyncTask<String, String, String> {
        // Store error message
        private Exception e = null;

        public doRequest() {
        }

        @Override
        protected String doInBackground(String... args) {
            try {
                return process();
            } catch (Exception e) {
                this.e = e;    // Store error
            }

            return null;
        }

        @Override
        protected void onPostExecute(String data) {
            super.onPostExecute(data);
            // Display based on error existence

            mEditText.setText("");
            if (e != null) {
                mEditText.setText("Error: " + e.getMessage());
                this.e = null;
            } else {
                Gson gson = new Gson();
                //--------------------------------GSON테스트
                /*
                String TT = data;
                mEditText.append(data);
                */
                AnalysisResult result = gson.fromJson(data, AnalysisResult.class);
                Log.e("Result = ", String.valueOf(result.categories));

                mEditText.append("Image format: " + result.metadata.format + "\n");
                mEditText.append("Image width: " + result.metadata.width + ", height:" + result.metadata.height + "\n");
                mEditText.append("Clip Art Type: " + result.imageType.clipArtType + "\n");
                mEditText.append("Line Drawing Type: " + result.imageType.lineDrawingType + "\n");
                mEditText.append("Is Adult Content:" + result.adult.isAdultContent + "\n");
                mEditText.append("Adult score:" + result.adult.adultScore + "\n");
                mEditText.append("Is Racy Content:" + result.adult.isRacyContent + "\n");
                mEditText.append("Racy score:" + result.adult.racyScore + "\n\n") ;

                for (Category category: result.categories) {
                    mEditText.append("Category: " + category.name + ", score: " + category.score + "\n");
                    mEditText.append(("detail : "+category.detail+"\n"));
                }

                mEditText.append("\n");
                int faceCount = 0;
                for (Face face: result.faces) {
                    faceCount++;
                    mEditText.append("face " + faceCount + ", gender:" + face.gender + "(score: " + face.genderScore + "), age: " + + face.age + "\n");
                    mEditText.append("    left: " + face.faceRectangle.left +  ",  top: " + face.faceRectangle.top + ", width: " + face.faceRectangle.width + "  height: " + face.faceRectangle.height + "\n" );
                }
                if (faceCount == 0) {
                    mEditText.append("No face is detected");
                }
                mEditText.append("\n");

                mEditText.append("\nDominant Color Foreground :" + result.color.dominantColorForeground + "\n");
                mEditText.append("Dominant Color Background :" + result.color.dominantColorBackground + "\n");

                mEditText.append("\n--- Raw Data ---\n\n");
                mEditText.append(data);
                mEditText.setSelection(0);
            }

            mButtonSelectImage.setEnabled(true);
        }
    }
}
