package com.example.jinhyukkim.landiary_and;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

public class landmark_Gallery extends AppCompatActivity {
    Gallery Imagegallery;
    String path = Environment.getExternalStorageDirectory()+"/Android/data/com.example.jinhyukkim.landiary_and/files/Pictures";

    //ArrayList<String> GalleryImagesList;
    //ArrayList<Uri> GalleryImageUri;
    ArrayList<File> GalleryImageUri;
    ImageView imgGalleryImage;

    TextView imgT;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landmark__gallery);

        imgT = findViewById(R.id.imgT);
        //GalleryImagesList = new ArrayList<>();
        GalleryImageUri = new ArrayList<>();

        if ( Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            //String path = Environment.getExternalStorageDirectory()+"/android/data/pe.berabue.maptools/.image";
            File file = new File(path);
            String str;
            int num = 0;

            int imgCount = file.listFiles().length;	// 파일 총 갯수 얻어오기
            //map = new Bitmap[imgCount];

            if ( file.listFiles().length > 0 )
                for ( File f : file.listFiles() ) {
                    str = f.getName();				// 파일 이름 얻어오기
                    //      map[num] = BitmapFactory.decodeFile(path+"/"+str);
                    Log.e("File=", num +"/"+str);
                    //GalleryImagesList.add(str);
                    //Uri uri = Uri.parse("file://"+path+"/"+GalleryImagesList.get(num));
                    //Uri uri = Uri.parse("file://"+path+"/"+str);
                    File now_file = f;
                    Log.e("file Uri = ", num + "/" + now_file.getAbsolutePath());
                    GalleryImageUri.add(now_file);
                    num++;
                }
        }

        imgGalleryImage = (ImageView)findViewById(R.id.imgGalleryImage);
//        imgGalleryImage.setImageResource(R.drawable.ic_launcher_foreground);

        Imagegallery = (Gallery)findViewById(R.id.gallery);
        Imagegallery.setAdapter(new ImageAdapter(getApplicationContext()));
        Imagegallery.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id)
            {
                File file = new File(String.valueOf(GalleryImageUri.get(position)));
                Date lastDate = new Date(file.lastModified());
                Log.e("File = ", String.valueOf(GalleryImageUri.get(position)) +"/"+ lastDate.toString());
                //imgT.setText(GalleryImageUri.get(position).getAbsolutePath());
                imgT.setText(file.getName() +"/"+ lastDate.toString());
                Bitmap bitmap = BitmapFactory.decodeFile(GalleryImageUri.get(position).getAbsolutePath());
                //BitmapFactory.Options opt = new BitmapFactory.Options();
                //opt.inSampleSize = 4;
                //Bitmap orgImg = BitmapFactory.decodeFile(String.valueOf(GalleryImageUri.get(position)), opt);
                //Bitmap resize = Bitmap.createScaledBitmap(orgImg, 640, 480, true);
                //imgGalleryImage.setImageBitmap(resize);
                //imgGalleryImage.setImageURI(GalleryImageUri.get(position));
                imgGalleryImage.setImageBitmap(bitmap);
            }
        });
    }

    private class ImageAdapter extends BaseAdapter
    {
        Context context;
        public ImageAdapter(Context context)
        {
            this.context = context;
        }
        @Override
        public int getCount()
        {
            //return GalleryImagesList.size();
            return GalleryImageUri.size();
        }

        @Override
        public Object getItem(int position)
        {
            //return GalleryImagesList.get(position);
            return GalleryImageUri.get(position);
        }

        @Override
        public long getItemId(int position)
        {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            ImageView imageView = new ImageView(this.context);
            String value = String.valueOf(GalleryImageUri.get(position));
            Log.e("getView = ", String.valueOf(Uri.parse(value)));
            BitmapFactory.Options opt = new BitmapFactory.Options();
            opt.inSampleSize = 16;
            Bitmap orgimg = BitmapFactory.decodeFile(GalleryImageUri.get(position).getAbsolutePath(), opt);
            //Bitmap resize = Bitmap.createScaledBitmap(orgimg, 300, 400, true);
            //Bitmap resize = Bitmap.createScaledBitmap(orgimg, 300, 400, false);
//            imageView.setImageBitmap(resize);
            imageView.setImageBitmap(orgimg);
            imageView.setLayoutParams(new Gallery.LayoutParams(300, 400));
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);

            return imageView;
        }

    }
}
