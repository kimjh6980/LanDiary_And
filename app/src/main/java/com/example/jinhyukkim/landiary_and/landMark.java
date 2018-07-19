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
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
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
    TextView landmark_t, landmark_t2;

    private VisionServiceClient client;
    private Uri mImageUri;
    private Uri mUriPhotoTaken;
    private File mFilePhotoTaken;

    String clientId;
    String clientSecret;

    String landmarkUrl;
    String path;

    Button Btn_Detail, Btn_Save, Btn_Cancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_land_mark);

        if (client==null){
            client = new VisionServiceRestClient(getString(R.string.subscription_key), getString(R.string.subscription_apiroot));
        }

        clientId = getString(R.string.Client_ID);//애플리케이션 클라이언트 아이디값";
        clientSecret = getString(R.string.Client_Secret);//애플리케이션 클라이언트 시크릿값";

        camimg = findViewById(R.id.camimg);
        landmark_t = findViewById(R.id.landmark_t);
        landmark_t2 = findViewById(R.id.landmark_t2);

        sendTakePhotoIntent();
        path = Environment.getExternalStorageDirectory()+"/Android/data/com.example.jinhyukkim.landiary_and/files/Pictures";
        //ResultValue("test");

        Btn_Detail = findViewById(R.id.Btn_Detail);
        Btn_Detail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Landmark_WebView.class);
                intent.putExtra("url", landmarkUrl);
                startActivity(intent);
            }
        });
        Btn_Save = findViewById(R.id.Btn_Save);
        Btn_Save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        Btn_Cancel = findViewById(R.id.Btn_Cancel);
        Btn_Cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    private String imageFilePath;
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "TempFile";
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

    String LandmarkName;
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
        try {
            Log.e("Json Obj = ", land_a);
        } catch (NullPointerException e)  {
            Log.e("Json Obj = ", "NULL??");
        }

            JSONObject json = null;
            LandmarkName = "No init";
            try {
                json = new JSONObject(land_a);
                JSONArray t1 = (JSONArray) json.get("landmarks");
                try {
                    JSONObject t2 = (JSONObject) t1.getJSONObject(0);
                    LandmarkName = t2.getString("name");
                    Log.e("File === ", imageFilePath +"/"+ path +"///"+ LandmarkName);
                    File filePre = new File(imageFilePath);
                    File fileNow = new File(path, LandmarkName+".jpg");
                    String title = "Title";
                    if(filePre.renameTo(fileNow)){
                        title = "Image Created";
                        papago(LandmarkName);
                    }else{
                        title = "TempFile delete";
                        TempFile_Delete(1);
                    }
                    final String finalTitle = title;
                }   catch (JSONException e)    {
                    LandmarkName = "no landmark";
                    TempFile_Delete(2);
                }
            } catch (JSONException e1) {
                Log.e("No Data", land_a);
                //e1.printStackTrace();
                LandmarkName = "no Data";
                TempFile_Delete(3);
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    landmark_t.setText(LandmarkName);
                }
            });


    }

    private void TempFile_Delete(int where) {
        File f = new File(imageFilePath);
        if(f.delete())  {
            Log.e("TempFile", where +"/"+path+"/"+imageFilePath+"/"+ "TempFile Delete");
        }   else    {
            Log.e("TempFile", where+path+"/"+imageFilePath+"/"+ "Delete Fail");
        }
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
        }
    }

    public void papago(String name) {
        try {
            String text = URLEncoder.encode(name, "UTF-8");
            String apiURL = "https://openapi.naver.com/v1/papago/n2mt";
            URL url = new URL(apiURL);
            HttpURLConnection con = (HttpURLConnection)url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("X-Naver-Client-Id", clientId);
            con.setRequestProperty("X-Naver-Client-Secret", clientSecret);
            // post request
            String postParams = "source=en&target=ko&text=" + text;
            con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(postParams);
            wr.flush();
            wr.close();
            int responseCode = con.getResponseCode();
            BufferedReader br;
            if(responseCode==200) { // 정상 호출
                br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            } else {  // 에러 발생
                br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
            }
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = br.readLine()) != null) {
                response.append(inputLine);
            }
            br.close();
            JSONObject result = new JSONObject(response.toString());
            JSONObject papago_msg = new JSONObject(result.getString("message"));
            JSONObject papago_result = new JSONObject(papago_msg.getString("result"));
            final String papago_translatedText = papago_result.getString("translatedText");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    landmark_t.setText(papago_translatedText);
                }
            });
            encyc(papago_translatedText);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void encyc(String name)    {
        try {
            String text = URLEncoder.encode(name, "UTF-8");
            String apiURL = "https://openapi.naver.com/v1/search/encyc?query="+ text;
            URL url = new URL(apiURL);
            HttpURLConnection con = (HttpURLConnection)url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("X-Naver-Client-Id", clientId);
            con.setRequestProperty("X-Naver-Client-Secret", clientSecret);
            int responseCode = con.getResponseCode();
            BufferedReader br;
            if(responseCode==200) { // 정상 호출
                br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            } else {  // 에러 발생
                br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
            }
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = br.readLine()) != null) {
                response.append(inputLine);
            }
            br.close();
            JSONObject RspObj = new JSONObject(response.toString());
            JSONArray Rsp_items = new JSONArray(RspObj.getString("items"));
            JSONObject rsp_Value = Rsp_items.getJSONObject(1);
            landmarkUrl = rsp_Value.getString("link");
            final String Value = rsp_Value.getString("description");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    landmark_t2.setText(Value);
                }
            });
        } catch (Exception e) {
            Log.e("Error = ", String.valueOf(e));
        }
    }
}
