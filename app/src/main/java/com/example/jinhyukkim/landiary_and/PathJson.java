package com.example.jinhyukkim.landiary_and;

import android.os.AsyncTask;
import android.util.Log;

import com.skt.Tmap.TMapPoint;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by 20134833 on 2018-07-12.
 */

public class PathJson{

    public final String url = "https://api2.sktelecom.com/tmap/routes?version=1&format=json";
    OkHttpClient client = new OkHttpClient();
    String responseBody = null;

    public PathJson() {
    }

    public void PathPoint_Asycn(final double ex, final double ey, final double sx, final double sy) {
        (new AsyncTask<MainActivity, Void, String>() {
            @Override
            protected String doInBackground(MainActivity... mainActivities) {
                Log.e("Do In Back", "Start");
                ConnectServer connectServerPost = new ConnectServer();
                connectServerPost.requestPost(url, ex, ey, sx, sy);
                return responseBody;
            }

            @Override
            protected void onPreExecute() {
                 super.onPreExecute();
            }

            @Override
            protected void onPostExecute(String result) {
            }
        }).execute();

        return;
    }

    public class ConnectServer {//Client 생성

        public int requestPost(String url, double ex, double ey, double sx, double sy) {
            double ex2 = Double.parseDouble(String.format("%.6f",ex));
            double ey2 = Double.parseDouble(String.format("%.6f",ey));
            double sx2 = Double.parseDouble(String.format("%.6f",sx));
            double sy2 = Double.parseDouble(String.format("%.6f",sy));

            //Request Body에 서버에 보낼 데이터 작성
            final RequestBody requestBody = new FormBody.Builder()
                    .add("endX", String.valueOf(ex2))
                    .add("endY", String.valueOf(ey2))
                    .add("startX", String.valueOf(sx2))
                    .add("startY", String.valueOf(sy2))
                    /*
                    .add("endX", String.valueOf("129.032714"))
                    .add("endY", String.valueOf("35.106811"))
                    .add("startX", String.valueOf("126.982177"))
                    .add("startY", String.valueOf("37.564686"))
                    */
                    .add("reqCoordType", String.valueOf("WGS84GEO"))
                    .add("resCoordType", String.valueOf("WGS84GEO"))
                    .add("tollgateFareOption", String.valueOf("1"))
                    .add("roadType", String.valueOf("32"))
                    .add("directionOption", String.valueOf("0"))
                    .add("endRpFlag", String.valueOf("16"))
                    .add("endPoiId", String.valueOf("67516"))
                    .add("gpsTime", String.valueOf("10000"))
                    .add("angle", String.valueOf("90"))
                    .add("speed", String.valueOf("60"))
                    .add("uncetaintyP", String.valueOf("3"))
                    .add("uncetaintyA", String.valueOf("3"))
                    .add("uncetaintyAP", String.valueOf("12"))
                    .add("camOption", String.valueOf("0"))
                    .add("carType", String.valueOf("0"))
                    .add("searchOption", String.valueOf("0"))
                    .build();

            //작성한 Request Body와 데이터를 보낼 url을 Request에 붙임
            Request request = new Request
                    .Builder()
                    .url(url)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .addHeader("appKey", "38c7269d-5eb5-4739-b305-9886986b658f")
                    .post(requestBody)
                    .build();

            //request를 Client에 세팅하고 Server로 부터 온 Response를 처리할 Callback 작성
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                }

                @Override
                public void onResponse(Call call, Response response) {
                    try {
                        responseBody = response.body().string();
                        JSONObject obj = new JSONObject(responseBody);
                        new PathJson(obj);

                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
            return 0;
        }
    }

    public PathJson(JSONObject root) {
        try {
            JSONArray features = root.getJSONArray("features");

            for (int i=0; i<features.length(); ++i) {
                JSONObject obj = features.getJSONObject(i);
                String type = obj.getJSONObject("geometry").getString("type");

                JSONArray coord = obj.getJSONObject("geometry").getJSONArray("coordinates");
                JSONObject props = obj.getJSONObject("properties");

                if (type.equals("Point")) {
                    // Point인 경우
                    TMapPoint[] points = new TMapPoint[1];
                    points[0] = new TMapPoint(coord.getDouble(1), coord.getDouble(0));

                    String name, description, turntype;
                    name = props.getString("name");
                    description = props.getString("description");
                    turntype = props.getString("turnType");

                    if (name.equals("")) {
                        name = description;
                        description = "";
                    }

                    Log.e("PathPoint = ", coord.getDouble(1)+"/"+coord.getDouble(0)+"/"+name + "/" + description +"["+turntype);

                    //popupListItems.add(new PopupListItem(name, description, false, points));----------------------------------------------------------------------------어댑터
                } else if (type.equals("LineString")) {
                    // Line String인 경우
                    TMapPoint[] points = new TMapPoint[coord.length()];
                    for (int k=0; k<coord.length(); ++k) {
                        JSONArray innerCoord = coord.getJSONArray(k);

                        points[k] = new TMapPoint(innerCoord.getDouble(1), innerCoord.getDouble(0));
                    }

                    String name, description;
                    name = props.getString("name");
                    description = props.getString("description");

                    if (name.equals("")) {
                        name = description;
                        description = "";
                    }

                    Log.e("LineString = ", name + "/" + description);

                }

                // 의미없는 정보 제거----------------------------------------------------------------------------어댑터
                /*
                if (popupListItems.get(popupListItems.size()-1).mainText.startsWith(","))
                    popupListItems.remove(popupListItems.size()-1);
                    */

            }
        } catch(Exception ex) {
            Log.d("EXC:", "an error occur while parsing json / " + ex.toString());
        }
    }
}
