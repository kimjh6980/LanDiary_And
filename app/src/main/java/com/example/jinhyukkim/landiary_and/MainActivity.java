package com.example.jinhyukkim.landiary_and;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jinhyukkim.landiary_and.List.ItemData;
import com.example.jinhyukkim.landiary_and.List.ListAdapter;
import com.skt.Tmap.TMapData;
import com.skt.Tmap.TMapData.ConvertGPSToAddressListenerCallback;
import com.skt.Tmap.TMapData.FindPathDataListenerCallback;
import com.skt.Tmap.TMapData.TMapPathType;
import com.skt.Tmap.TMapGpsManager;
import com.skt.Tmap.TMapLabelInfo;
import com.skt.Tmap.TMapMarkerItem;
import com.skt.Tmap.TMapPOIItem;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapPolyLine;
import com.skt.Tmap.TMapView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class MainActivity extends BaseActivity implements TMapGpsManager.onLocationChangedCallback {

    TMapPoint Current_Point;
    double getCurrent_long;
    double getCurrent_lat;

    int point_count = 0;
    ArrayList<Double> pointlist_lat = new ArrayList<>();
    ArrayList<Double> pointlist_lon = new ArrayList<>();
    ArrayList<Integer> pointlist_img = new ArrayList<>();
    double set_long;
    double set_lat;

    @Override
    public void onLocationChange(Location location) {
        LogManager.printLog("onLocationChange :::> " + location.getLatitude() +  " " +
                location.getLongitude() + " " + location.getSpeed() + " " + location.getAccuracy());
        if(m_bTrackingMode)
        {
            mMapView.setLocationPoint(location.getLongitude(), location.getLatitude());
            mMapView.setCenterPoint(location.getLongitude(), location.getLatitude());

            getCurrent_long = location.getLongitude();
            getCurrent_lat = location.getLatitude();

            Current_Point = new TMapPoint(getCurrent_lat, getCurrent_long);

            LogManager.printLog("getCurrent_lat : " + getCurrent_lat);
            LogManager.printLog("getCurrent_long : " + getCurrent_long);

            Location locA = new Location("current_L");
            locA.setLatitude(getCurrent_lat);
            locA.setLongitude(getCurrent_long);

            Location locB = new Location("dest_L");
            locB.setLatitude(set_lat);
            locB.setLongitude(set_long);

            float distance = locA.distanceTo(locB);
            Log.e("DistanceA_B = ", String.valueOf(distance));  // 두지점 사이의 거리 ( 만약 다음 포인트까지의 거리가 5m이하일 경우, 포인트를 다음 목적 포인트로 넘기는 작업을 할 예정임ㅒㅔ

            Log.e("Point Count ->", point_count +"/"+ set_long +"/"+ set_lat);

            if(distance <= 0.05)    {
                point_count++;
                markerPoint();
                Log.e("Point Count ++ ->", point_count +"/"+ set_long +"/"+ set_lat);
                Toast.makeText(getApplicationContext(), "point+="+point_count, Toast.LENGTH_SHORT).show();
            }
        }
    }



    /**
     *  Field declaration
     */
    private TMapView	mMapView = null;
    private Context 	mContext;
    private TMapMarkerItem CurrentMarker;

    private ArrayList<Bitmap> mOverlayList;
    //private ImageOverlay mOverlay;

    TMapGpsManager gps = null;

    TMapPoint Start_Point = null;
    TMapPoint Destination_Point = null;

    private String Address;

    public static String mApiKey = "38c7269d-5eb5-4739-b305-9886986b658f"; // 발급받은 appKey

    private static final int[] mArrayMapButton = {
            R.id.btnClickDestination,
            R.id.btnSearchDestination,
            R.id.btnStartGuidance,
            R.id.btnSetCompassMode
    };

    private 	int 		m_nCurrentZoomLevel = 0;
    private 	double 		m_Latitude  = 0;
    private     double  	m_Longitude = 0;
    private 	boolean 	m_bShowMapIcon = false;
    private 	boolean 	m_bTrafficeMode = false;
    private 	boolean 	m_bSightVisible = false;
    private 	boolean 	m_bTrackingMode = false;
    private 	boolean 	m_bOverlayMode = false;

    Button Capture_btn;
    ImageView arrowimg;
    TextView dest_t;
    TextView arrowT;

    ArrayList<String> mArrayID;

    ArrayList<String>		mArrayLineID;
    private static 	int 	mLineID;

    ArrayList<String>       mArrayMarkerID;
    private static int 		mMarkerID;

    private ListView A_List = null;
    //----------------------통신
    PathJson pathJson = new PathJson();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        mContext = this;

        // TmapView setting
        mMapView = new TMapView(this);
        configureMapView(); // API Key 확인
        vieweSetting();     // 지도뷰 특성 세팅
        initView();         // 지도뷰 초기화
        addView(mMapView);  // 지도 뷰 생성

        // ID arraylist
        mArrayID = new ArrayList<String>();
        mArrayLineID = new ArrayList<String>();
        mLineID = 0;
        mArrayMarkerID	= new ArrayList<String>();
        mMarkerID = 0;

        // Gps Open
        gps = new TMapGpsManager(MainActivity.this);
        gps.setMinTime(500);
        gps.setMinDistance(2);
        gps.setProvider(gps.NETWORK_PROVIDER);    //네트워크 검색
        //gps.setProvider(gps.GPS_PROVIDER);
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED){

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 1); //위치권한 탐색 허용 관련 내용
            }
            return;
        }
        gps.OpenGps();

        // Insert A SKT Logo on the Tmap.
        mMapView.setTMapLogoPosition(TMapView.TMapLogoPositon.POSITION_BOTTOMRIGHT);

        Capture_btn = (Button)findViewById(R.id.capture_BTN);
        A_List = (ListView)findViewById(R.id.itemlistview);
        arrowimg = (ImageView)findViewById(R.id.ArrowImg);
        arrowT = (TextView)findViewById(R.id.ArrowT);
        dest_t = (TextView)findViewById(R.id.Dest_t);

        Capture_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent landmark_class = new Intent(getApplicationContext(), landMark.class);
                startActivity(landmark_class);
            }
        });
    }

    /**
     * setSKPMapApiKey()에 ApiKey를 입력 한다.
     */
    private void configureMapView() {
        mMapView.setSKPMapApiKey(mApiKey);
    }


    // set ths Map's properties
    private void vieweSetting() {
        setMapIcon();       // 현재 맵 위에 표시할 아이콘 설정
        mMapView.setLanguage(TMapView.LANGUAGE_KOREAN);
        mMapView.setZoomLevel(15);
        mMapView.setMapType(TMapView.MAPTYPE_STANDARD);
    }

    /**
     * setMapIcon
     * 현재위치로 표시될 아이콘을 설정한다.
     */
    public void setMapIcon() {
        CurrentMarker = new TMapMarkerItem();

        Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.poi_here);
        mMapView.setIcon(bitmap);
        mMapView.addMarkerItem("CurrentMarker", CurrentMarker);

        mMapView.setIconVisibility(true);
    }

    /**
     * initView - 버튼에 대한 리스너를 등록한다.
     */
    private void initView() {
        for (int btnMapView : mArrayMapButton) {
            Button ViewButton = (Button)findViewById(btnMapView);
            ViewButton.setOnClickListener(this);
        }

        mMapView.setOnApiKeyListener(new TMapView.OnApiKeyListenerCallback() {
            @Override
            public void SKPMapApikeySucceed() {
                LogManager.printLog("MainActivity SKPMapApikeySucceed");
            }

            @Override
            public void SKPMapApikeyFailed(String errorMsg) {
                LogManager.printLog("MainActivity SKPMapApikeyFailed " + errorMsg);
            }
        });


        mMapView.setOnEnableScrollWithZoomLevelListener(new TMapView.OnEnableScrollWithZoomLevelCallback() {
            @Override
            public void onEnableScrollWithZoomLevelEvent(float zoom, TMapPoint centerPoint) {
                LogManager.printLog("MainActivity onEnableScrollWithZoomLevelEvent " + zoom + " " + centerPoint.getLatitude() + " " + centerPoint.getLongitude());
            }
        });

        mMapView.setOnDisableScrollWithZoomLevelListener(new TMapView.OnDisableScrollWithZoomLevelCallback() {
            @Override
            public void onDisableScrollWithZoomLevelEvent(float zoom, TMapPoint centerPoint) {
                LogManager.printLog("MainActivity onDisableScrollWithZoomLevelEvent " + zoom + " " + centerPoint.getLatitude() + " " + centerPoint.getLongitude());
            }
        });

        mMapView.setOnClickListenerCallBack(new TMapView.OnClickListenerCallback() {
            @Override
            public boolean onPressUpEvent(ArrayList<TMapMarkerItem> markerlist,ArrayList<TMapPOIItem> poilist, TMapPoint point, PointF pointf) {
                LogManager.printLog("MainActivity onPressUpEvent " + markerlist.size());
                return false;
            }

            @Override
            public boolean onPressEvent(ArrayList<TMapMarkerItem> markerlist,ArrayList<TMapPOIItem> poilist, TMapPoint point, PointF pointf) {
                LogManager.printLog("MainActivity onPressEvent " + markerlist.size());

                for (int i = 0; i < markerlist.size(); i++) {
                    TMapMarkerItem item = markerlist.get(i);
                    LogManager.printLog("MainActivity onPressEvent " + item.getName() + " " + item.getTMapPoint().getLatitude() + " " + item.getTMapPoint().getLongitude());
                }
                return false;
            }
        });

        mMapView.setOnLongClickListenerCallback(new TMapView.OnLongClickListenerCallback() {
            @Override
            public void onLongPressEvent(ArrayList<TMapMarkerItem> markerlist, ArrayList<TMapPOIItem> poilist, TMapPoint point) {
                LogManager.printLog("MainActivity onLongPressEvent " + markerlist.size());
            }
        });

        mMapView.setOnCalloutRightButtonClickListener(new TMapView.OnCalloutRightButtonClickCallback() {
            @Override
            public void onCalloutRightButton(TMapMarkerItem markerItem) {
                String strMessage = "";
                strMessage = "ID: " + markerItem.getID() + " " + "Title " + markerItem.getCalloutTitle();
                Common.showAlertDialog(MainActivity.this, "Callout Right Button", strMessage);
            }
        });

        mMapView.setOnClickReverseLabelListener(new TMapView.OnClickReverseLabelListenerCallback() {
            @Override
            public void onClickReverseLabelEvent(TMapLabelInfo findReverseLabel) {
                if(findReverseLabel != null) {
                    LogManager.printLog("MainActivity setOnClickReverseLabelListener " + findReverseLabel.id + " / " + findReverseLabel.labelLat
                            + " / " + findReverseLabel.labelLon + " / " + findReverseLabel.labelName);

                }
            }
        });

        m_nCurrentZoomLevel = -1;
        m_bShowMapIcon = true;
        m_bTrafficeMode = false;
        m_bSightVisible = true;
        m_bTrackingMode = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        gps.CloseGps();
        mMapView.removeTMapPath();
        if(mOverlayList != null){
            mOverlayList.clear();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnClickDestination   :   ClickDestination();  break;
            case R.id.btnSearchDestination  :   SearchDestination(); break;
            case R.id.btnStartGuidance      :   StartGuidance();     break;
            case R.id.btnSetCompassMode     : 	setCompassMode();    break;
        }
    }

    /////////////////////////////////Each BUtton's Method//////////////////////////////////////////////////////////////
    /**
     *  도착지주소 지정 메소드
     */
    public void ClickDestination() {
        Toast.makeText(MainActivity.this, "원하시는 도착 지점을 터치한 후 길안내 시작버튼을 눌러주세요.", Toast.LENGTH_SHORT).show();

        mMapView.setOnClickListenerCallBack(new TMapView.OnClickListenerCallback() {
            @Override
            public boolean onPressEvent(ArrayList<TMapMarkerItem> arrayList,
                                        ArrayList<TMapPOIItem> arrayList1, TMapPoint tMapPoint, PointF pointF) {

                TMapData tMapData = new TMapData();
                tMapData.convertGpsToAddress(tMapPoint.getLatitude(), tMapPoint.getLongitude(),
                        new ConvertGPSToAddressListenerCallback() {
                            @Override
                            public void onConvertToGPSToAddress(String strAddress) {
                                Address = strAddress;
                                LogManager.printLog("선택한 위치의 주소는 " + strAddress);
                            }
                        });

                Toast.makeText(MainActivity.this, "선택하신 위치의 주소는 " + Address + " 입니다.", Toast.LENGTH_SHORT).show();

                return false;
            }

            @Override
            public boolean onPressUpEvent(ArrayList<TMapMarkerItem> arrayList,
                                          ArrayList<TMapPOIItem> arrayList1, TMapPoint tMapPoint, PointF pointF) {
                Destination_Point = tMapPoint;

                return false;
            }
        });

    }

    /** SearchDestination
     *  도착지주소 검색 메소드
     */
    public void SearchDestination() {
        // 검색창에 입력받음
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("POI 통합 검색");

        final EditText input = new EditText(this);
        builder.setView(input);

        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final String strData = input.getText().toString();
                TMapData tMapData = new TMapData();

                tMapData.findAllPOI(strData, new TMapData.FindAllPOIListenerCallback() {
                    @Override
                    public void onFindAllPOI(ArrayList<TMapPOIItem> poiItem) {
                        for(int i=0; i<poiItem.size(); i++){
                            TMapPOIItem item = poiItem.get(i);

                            LogManager.printLog("POI Name: " + item.getPOIName().toString() + ", " +
                                    "Address: " + item.getPOIAddress().replace("null", "")  + ", " +
                                    "Point: " + item.getPOIPoint().toString());

                            Address = item.getPOIAddress();
                            Destination_Point = item.getPOIPoint();
                        }
                    }
                });
            }
        });

        Toast.makeText(this, "입력하신 주소는 " + Address + " 입니다.", Toast.LENGTH_SHORT).show();
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    /** StartGuidance()
     *  경로 그리기 메소드
     */
    public void StartGuidance() {
        mMapView.removeTMapPath();

        pointlist_init();

        setTrackingMode();

        TMapPoint point1 = mMapView.getLocationPoint();
        TMapPoint point2 = Destination_Point;

        TMapData tmapdata = new TMapData();

        tmapdata.findPathDataWithType(TMapPathType.CAR_PATH, point1, point2, new FindPathDataListenerCallback() {
            @Override
            public void onFindPathData(TMapPolyLine polyLine) {
                polyLine.setLineColor(Color.BLUE);
                mMapView.addTMapPath(polyLine);
            }
        });

        Bitmap start = BitmapFactory.decodeResource(mContext.getResources(),R.drawable.poi_start);
        Bitmap end = BitmapFactory.decodeResource(mContext.getResources(),R.drawable.poi_end);
        mMapView.setTMapPathIcon(start, end);

        mMapView.zoomToTMapPoint(point1, point2);

        //pathJson.PathPoint_Asycn(point1.getLongitude(), point1.getLatitude(), point2.getLongitude(), point2.getLatitude());
        pathJson.PathPoint_Asycn(point2.getLongitude(), point2.getLatitude(), point1.getLongitude(), point1.getLatitude());
    }

    private void pointlist_init() {
        point_count = 0;
        pointlist_lat.clear();
        pointlist_lon.clear();

    }

    /**
     * setCompassMode
     * 단말의 방항에 따라 움직이는 나침반모드로 설정한다.
     */
    public void setCompassMode() {
        mMapView.setCompassMode(!mMapView.getIsCompass());
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////


    /**
     * getIsCompass
     * 나침반모드의 사용여부를 반환한다.
     */
    public void getIsCompass() {
        Boolean bGetIsCompass = mMapView.getIsCompass();
        Common.showAlertDialog(this, "", "현재 나침반 모드는 : " + bGetIsCompass.toString() );
    }

    /**
     * getLocationPoint
     * 현재위치로 표시될 좌표의 위도, 경도를 반환한다.
     */
    public void getLocationPoint() {
        TMapPoint point = mMapView.getLocationPoint();

        double Latitude = point.getLatitude();
        double Longitude = point.getLongitude();

        m_Latitude  = Latitude;
        m_Longitude = Longitude;

        LogManager.printLog("Latitude " + Latitude + " Longitude " + Longitude);

        String strResult = String.format("Latitude = %f Longitude = %f", Latitude, Longitude);

        Common.showAlertDialog(this, "", strResult);
    }

    /**
     * setTrackingMode
     * 화면중심을 단말의 현재위치로 이동시켜주는 트래킹모드로 설정한다.
     */
    public void setTrackingMode() {
        mMapView.setTrackingMode(mMapView.getIsTracking());
    }

    /**
     * getIsTracking
     * 트래킹모드의 사용여부를 반환한다.
     */
    public void getIsTracking() {
        Boolean bIsTracking = mMapView.getIsTracking();
        Common.showAlertDialog(this, "", "현재 트래킹모드 사용 여부  : " + bIsTracking.toString() );
    }

    //---------------------------------------------------------------------------------------------------------------------------------------

    ListAdapter adapter;

    public void mapzoom_in(View view) {
        mMapView.MapZoomIn();
    }
    public void mapzoom_out(View view) {
        mMapView.MapZoomOut();
    }

    public void test(View view) {

    }

    public class PathJson{

        public final String url = "https://api2.sktelecom.com/tmap/routes?version=1&format=json";
        OkHttpClient client = new OkHttpClient();
        String responseBody = null;

        View view;
        LayoutInflater li;

        public PathJson() {
        }

        public void PathPoint_Asycn(final double ex, final double ey, final double sx, final double sy) {
            (new AsyncTask<MainActivity, Void, String>() {
                @Override
                protected String doInBackground(MainActivity... mainActivities) {
                    Log.e("Do In Back", "Start");
                    ConnectServer connectServer = new ConnectServer();
                    connectServer.requestPost(url, ex, ey, sx, sy);
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
            ArrayList<ItemData> A_Data = new ArrayList<>();
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

                        ItemData idata = new ItemData();

                        if(description != "")
                            idata.MainT = description;
                        else
                            idata.MainT = name;

                        idata.getX = String.valueOf(coord.getDouble(0));
                        idata.getY = String.valueOf(coord.getDouble(1));
                        Drawable drawableimg;

                        switch (turntype)   {
                            case "0":
                                drawableimg = getResources().getDrawable(R.drawable.a_0);
                                break;
                            case "11":
                                drawableimg = getResources().getDrawable(R.drawable.a_11);
                                break;
                            case "12":
                                drawableimg = getResources().getDrawable(R.drawable.a_12);
                                break;
                            case "13":
                                drawableimg = getResources().getDrawable(R.drawable.a_13);
                                break;
                            case "14":
                                drawableimg = getResources().getDrawable(R.drawable.a_14);
                                break;
                            default:
                                drawableimg = getResources().getDrawable(R.drawable.a_etc);
                                break;
                        }
                        //idata.arrow.setImageDrawable(drawableimg);
                        idata.arrow = drawableimg;
                        A_Data.add(idata);

                        pointlist_lat.add(coord.getDouble(0));
                        pointlist_lon.add(coord.getDouble(1));
                        pointlist_img.add(Integer.valueOf(turntype));

                        //popupListItems.add(new PopupListItem(name, description, false, points));----------------------------------------------------------------------------어댑터
                    }
                    /*else if (type.equals("LineString")) {
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
                    */

                    // 의미없는 정보 제거----------------------------------------------------------------------------어댑터
                /*
                if (popupListItems.get(popupListItems.size()-1).mainText.startsWith(","))
                    popupListItems.remove(popupListItems.size()-1);
                    */

                }
            } catch(Exception ex) {
                Log.d("EXC:", "an error occur while parsing json / " + ex.toString());
            }

            adapter = new ListAdapter(A_Data);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    A_List.setAdapter(adapter);
                }
            });

            markerPoint();
        }
    }

    void markerPoint()    {
        //double lat2 = Double.parseDouble(String.format("%.6f",lat));
        //double lon2 = Double.parseDouble(String.format("%.6f",lon));

        set_long = pointlist_lon.get(point_count);
        set_lat = pointlist_lat.get(point_count);

        TMapMarkerItem mapMarkerItem = new TMapMarkerItem();

        TMapPoint tMapPoint = new TMapPoint(set_long, set_lat);

        Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_map_no_02);
        mapMarkerItem.setIcon(bitmap);
        mapMarkerItem.setPosition(0.5f, 0.5f);
        mapMarkerItem.setTMapPoint(tMapPoint);
        mapMarkerItem.setName(String.valueOf(point_count));
        mMapView.addMarkerItem("mapMarkerItem", mapMarkerItem);

        final Drawable drawableimg;

        switch (String.valueOf(pointlist_img.get(point_count)))   {
            case "0":
                drawableimg = getResources().getDrawable(R.drawable.a_0);
                break;
            case "11":
                drawableimg = getResources().getDrawable(R.drawable.a_11);
                break;
            case "12":
                drawableimg = getResources().getDrawable(R.drawable.a_12);
                break;
            case "13":
                drawableimg = getResources().getDrawable(R.drawable.a_13);
                break;
            case "14":
                drawableimg = getResources().getDrawable(R.drawable.a_14);
                break;
            default:
                drawableimg = getResources().getDrawable(R.drawable.a_etc);
                break;
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                arrowT.setText(adapter.getM(point_count));
                arrowimg.setImageDrawable(drawableimg);
            }
        });


    }
}
