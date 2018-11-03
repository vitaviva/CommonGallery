package com.vitaviva.commongallery.demo;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.common.base.Strings;
import com.vitaviva.commongallery.GalleryHelper;
import com.vitaviva.commongallery.data.DataManger;
import com.vitaviva.commongallery.glide.GlideApp;
import com.vitaviva.commongallery.glide.GlideRequests;
import com.vitaviva.commongallery.listener.GalleryLoadListenerFactory;
import com.vitaviva.commongallery.listener.IGalleryClickListener;
import com.vitaviva.commongallery.model.GalleryItemWrapper;
import com.vitaviva.commongallery.util.ToastUtil;

import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private GridView gridView;
    private List<Map<String, Object>> dataList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        gridView = findViewById(R.id.gridview);

        gridView.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return DataManger.getInstance().getDataCount();
            }

            @Override
            public Object getItem(int i) {
                return DataManger.getInstance().getData(i);
            }

            @Override
            public long getItemId(int i) {
                return i;
            }

            @Override
            public View getView(int i, View view, ViewGroup viewGroup) {
                if (view == null) {
                    view = LayoutInflater.from(MainActivity.this).inflate(R.layout.gridview_item, null);
                    view.setTag(R.id.tag_txt, view.findViewById(R.id.text));
                    view.setTag(R.id.tag_img, view.findViewById(R.id.img));
                }
                ((TextView) view.getTag(R.id.tag_txt)).setText(i + "");
                ImageView iv = ((ImageView) view.getTag(R.id.tag_img));
                iv.setImageResource(R.drawable.image_loading);
                GlideRequests request = GlideApp.with(view.getContext());
                request.clear(iv);
                request.load(DataManger.getInstance().getData(i))
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .placeholder(R.drawable.image_loading)
                        .error(R.drawable.image_load_err)
                        .override(100, 100)
                        .transition(new DrawableTransitionOptions().crossFade())
                        .centerCrop()
                        .into(iv);
                return view;
            }
        });

        gridView.setOnItemClickListener((adapterView, view, i, l) -> {
            if (!Strings.isNullOrEmpty(DataManger.getInstance().getData(i))) {
                GalleryHelper.open(view.findViewById(R.id.img), GalleryLoadListenerFactory.create(i), new IGalleryClickListener() {
                    @Override
                    public void onClicked(Context context, GalleryItemWrapper item) {
                        ToastUtil.toast(view.getContext(), "item(" + i + ") onClicked");
                    }

                    @Override
                    public void onLongClicked(Context context, GalleryItemWrapper item) {
                        ToastUtil.toast(view.getContext(), "item(" + i + ") onLongClicked");
                    }
                });
            } else {
                ToastUtil.toast(view.getContext(), "broken data");
            }

        });
    }

}
