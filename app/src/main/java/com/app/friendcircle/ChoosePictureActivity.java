package com.app.friendcircle;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.TextView;

import com.app.R;
import com.punuo.sys.app.activity.BaseSwipeBackActivity;
import com.punuo.sys.app.util.StatusBarUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 选择相册集
 */
public class ChoosePictureActivity extends BaseSwipeBackActivity {
    private List<ImageBucket> dataList = new ArrayList<>();
    private AlbumHelper helper;
    public static final String EXTRA_IMAGE_LIST = "image_list";
    public static Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_bucket);
        StatusBarUtil.translucentStatusBar(this, Color.TRANSPARENT, false);
        View statusBar = findViewById(R.id.status_bar);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            statusBar.setVisibility(View.VISIBLE);
            statusBar.getLayoutParams().height = StatusBarUtil.getStatusBarHeight(this);
            statusBar.requestLayout();
        }
        helper = AlbumHelper.getHelper();
        helper.init(getApplicationContext());

        initData();
        initView();
    }

    /**
     * 初始化数据
     */
    private void initData() {
        dataList = helper.getImagesBucketList(false);
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.icon_addpic_unfocused);
    }

    /**
     * 初始化view视图
     */
    private void initView() {
        GridView gridView = (GridView) findViewById(R.id.grid_view);
        // 自定义的适配器
        ImageBucketAdapter imageBucketAdapter = new ImageBucketAdapter(ChoosePictureActivity.this, dataList);
        gridView.setAdapter(imageBucketAdapter);
        TextView cancel = (TextView) findViewById(R.id.tv_cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        gridView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(ChoosePictureActivity.this, ImageGridActivity.class);
                intent.putParcelableArrayListExtra(ChoosePictureActivity.EXTRA_IMAGE_LIST,
                        dataList.get(position).imageList);
                startActivity(intent);
                finish();
            }
        });
    }
}
