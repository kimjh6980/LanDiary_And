package com.example.jinhyukkim.landiary_and;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.google.gson.Gson;
import com.microsoft.projectoxford.vision.VisionServiceClient;
import com.microsoft.projectoxford.vision.VisionServiceRestClient;
import com.microsoft.projectoxford.vision.contract.AnalysisResult;
import com.microsoft.projectoxford.vision.contract.Category;
import com.microsoft.projectoxford.vision.contract.Face;
import com.microsoft.projectoxford.vision.rest.VisionServiceException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class landMark extends Activity {

    Bitmap ImageBitmap;
    ImageView camimg;
    TextView landmark_t;

    private VisionServiceClient client;
    private Uri mImageUri;
    private Uri mUriPhotoTaken;
    private File mFilePhotoTaken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_land_mark);

        if (client==null){
            client = new VisionServiceRestClient(getString(R.string.subscription_key), getString(R.string.subscription_apiroot));
        }

        camimg = findViewById(R.id.camimg);
        landmark_t = findViewById(R.id.landmark_t);

        sendTakePhotoIntent();
        //ResultValue("test");


    }

    private String imageFilePath;
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "TEST_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,      /* prefix */
                ".jpg",         /* suffix */
                storageDir          /* directory */
        );
        imageFilePath = image.getAbsolutePath();
        return image;
    }

    private Uri photoUri;
    private void sendTakePhotoIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }

            if (photoFile != null) {
                photoUri = FileProvider.getUriForFile(this, getPackageName(), photoFile);

                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(takePictureIntent, 100);
            }
        }
    }

    private int exifOrientationToDegrees(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270;
        }
        return 0;
    }

    private Bitmap rotate(Bitmap bitmap, float degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    //---------------
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == RESULT_OK) {
            Bitmap bitmap = BitmapFactory.decodeFile(imageFilePath);
            /*
            ExifInterface exif = null;

            try {
                exif = new ExifInterface(imageFilePath);
            } catch (IOException e) {
                e.printStackTrace();
            }

            int exifOrientation;
            int exifDegree;

            if (exif != null) {
                exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                exifDegree = exifOrientationToDegrees(exifOrientation);
            } else {
                exifDegree = 0;
            }
*/
            ((ImageView)findViewById(R.id.camimg)).setImageBitmap(bitmap);
            //camimg.setImageBitmap(rotate(bitmap, exifDegree));

            Drawable d = camimg.getDrawable();
            ImageBitmap = ((BitmapDrawable)d).getBitmap();
            doAnalyze();
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

    String result;

    private String process() throws VisionServiceException, IOException {
        Gson gson = new Gson();
        String[] features = {"ImageType", "Color", "Faces", "Adult", "Categories"};
        String[] details = {"celebrities","landmarks"};

        // Put the image into an input stream for detection.
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(output.toByteArray());

        AnalysisResult v = this.client.analyzeImage(inputStream, features, details);

        result = gson.toJson(v);
        Log.d("result", result);//------------------------------------------------------여기서 값이 뽑히네?

        ResultValue(result);
        return result;
    }

    private void ResultValue(String data) {
        Log.e("ResultValue = ", data);
            Gson gson = new Gson();
            String land_a = null;
            //--------------------------------GSON테스트
            AnalysisResult data2 = gson.fromJson(data, AnalysisResult.class);
            Log.e("Analysis Result = ", data2.toString());
            Log.e("Result = ", String.valueOf(data2.categories));

            for (Category category: data2.categories) {
//                    landmark_t.setText(("detail : "+category.detail+"\n"));
                land_a = String.valueOf(category.detail);
            }
            //land_a = "{landmarks=[{name=Namdaemun, confidence=0.9203275442123413}]}";
            Log.e("Json Obj = ", land_a);
            JSONObject json = null;
            String t3 = "No init";
            try {
                json = new JSONObject(land_a);
                JSONArray t1 = (JSONArray) json.get("landmarks");
                try {
                    JSONObject t2 = (JSONObject) t1.getJSONObject(0);
                     t3 = t2.getString("name");
                }   catch (JSONException e)    {
                    t3 = "no landmark";
                }
            } catch (JSONException e1) {
                Log.e("No Data", land_a);
                //e1.printStackTrace();
                t3 = "no Data";
            }
            final String result = t3;

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    landmark_t.setText(String.valueOf(result));
                }
            });
    }

    private class doRequest extends AsyncTask<String, String, String> {
        // Store error message
        private Exception e = null;

        public doRequest() {
        }

        @Override
        protected String doInBackground(String... args) {
            String data = null;
            try {
                data = process();
            } catch (VisionServiceException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }

            return data;
        }

        @Override
        protected void onPostExecute(String data) {
            super.onPostExecute(data);
            // Display based on error existence

            //ResultValue(data);

            /*
            landmark_t.setText("");
            if (e != null) {
                landmark_t.setText("Error: " + e.getMessage());
                this.e = null;
            } else {
                Gson gson = new Gson();
                //--------------------------------GSON테스트

                String TT = data;
                landmark_t.append(data);

                AnalysisResult result = gson.fromJson(data, AnalysisResult.class);
                Log.e("Result = ", String.valueOf(result.categories));

                String land_a = null;
                for (Category category: result.categories) {
//                    landmark_t.setText(("detail : "+category.detail+"\n"));
                    land_a = String.valueOf(category.detail);
                }
                Log.e("Json Obj = ", land_a);
                JSONObject json = null;
                try {
                    json = new JSONObject(land_a);
                    JSONArray t1 = (JSONArray) json.get("landmarks");
                    JSONObject t2 = (JSONObject) t1.getJSONObject(0);
                    String t3 = t2.getString("name");
                    landmark_t.setText(String.valueOf(t3));
                } catch (JSONException e1) {
                    Log.e("No Data", land_a);
                    e1.printStackTrace();
                }
                //String t1 = String.valueOf(json.get("landmarks"));
/*
                landmark_t.append("Image format: " + result.metadata.format + "\n");
                landmark_t.append("Image width: " + result.metadata.width + ", height:" + result.metadata.height + "\n");
                landmark_t.append("Clip Art Type: " + result.imageType.clipArtType + "\n");
                landmark_t.append("Line Drawing Type: " + result.imageType.lineDrawingType + "\n");
                landmark_t.append("Is Adult Content:" + result.adult.isAdultContent + "\n");
                landmark_t.append("Adult score:" + result.adult.adultScore + "\n");
                landmark_t.append("Is Racy Content:" + result.adult.isRacyContent + "\n");
                landmark_t.append("Racy score:" + result.adult.racyScore + "\n\n") ;
                for (Category category: result.categories) {
                    landmark_t.append("Category: " + category.name + ", score: " + category.score + "\n");
                    landmark_t.append(("detail : "+category.detail+"\n"));
                }

                landmark_t.append("\n");
                int faceCount = 0;
                for (Face face: result.faces) {
                    faceCount++;
                    landmark_t.append("face " + faceCount + ", gender:" + face.gender + "(score: " + face.genderScore + "), age: " + + face.age + "\n");
                    landmark_t.append("    left: " + face.faceRectangle.left +  ",  top: " + face.faceRectangle.top + ", width: " + face.faceRectangle.width + "  height: " + face.faceRectangle.height + "\n" );
                }
                if (faceCount == 0) {
                    landmark_t.append("No face is detected");
                }
                landmark_t.append("\n");
                landmark_t.append("\nDominant Color Foreground :" + result.color.dominantColorForeground + "\n");
                landmark_t.append("Dominant Color Background :" + result.color.dominantColorBackground + "\n");

                landmark_t.append("\n--- Raw Data ---\n\n");
                landmark_t.append(data);
                */
//            }
        }
    }
}
