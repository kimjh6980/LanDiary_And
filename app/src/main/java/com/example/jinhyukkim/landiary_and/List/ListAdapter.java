package com.example.jinhyukkim.landiary_and.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.jinhyukkim.landiary_and.R;

import java.util.ArrayList;

/**
 * Created by 20134833 on 2018-07-12.
 */

public class ListAdapter extends BaseAdapter{

    LayoutInflater inflater = null;
    private ArrayList<ItemData> Arr_Data = null;
    private int listCount = 0;

    public ListAdapter(ArrayList<ItemData> arr) {
        Arr_Data = arr;
        listCount = Arr_Data.size();
    }

    @Override
    public int getCount() {
        return listCount;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public String getM(int position)    {
        return Arr_Data.get(position).MainT;
    }

    public Drawable getA(int position)    {
        //Drawable drawable = Arr_Data.get(position).arrow;
        Drawable drawable = Arr_Data.get(position).arrow;
        return drawable;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null)
        {
            final Context context = parent.getContext();
            if (inflater == null)
            {
                inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            }
            convertView = inflater.inflate(R.layout.item_list, parent, false);
        }

        TextView mt = (TextView) convertView.findViewById(R.id.mainT);
        TextView xt = (TextView) convertView.findViewById(R.id.geoX);
        TextView yt = (TextView) convertView.findViewById(R.id.geoY);
        ImageView img = (ImageView) convertView.findViewById(R.id.imageView);

        mt.setText(Arr_Data.get(position).MainT);
        xt.setText(Arr_Data.get(position).getX);
        yt.setText(Arr_Data.get(position).getY);
        img.setImageDrawable(Arr_Data.get(position).arrow);
        //img.setImageBitmap(Arr_Data.get(position).arrow);
        return convertView;
    }

}
