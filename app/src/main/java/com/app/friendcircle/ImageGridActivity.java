package com.app.friendcircle;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import com.app.R;
import com.app.friendcircle.ImageGridAdapter.TextCallback;
import com.app.publish.event.ChooseImageResultEvent;
import com.punuo.sys.app.activity.BaseSwipeBackActivity;
import com.punuo.sys.app.util.StatusBarUtil;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

/**
 * 选择图片
 */
public class ImageGridActivity extends BaseSwipeBackActivity {
    // ArrayList<Entity> dataList;//用来装载数据源的列表
    private ArrayList<ImageItem> dataList;
    private ImageGridAdapter mImageGridAdapter;
    private Button mOK;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_grid);
        StatusBarUtil.translucentStatusBar(this, Color.TRANSPARENT, false);
        View statusBar = findViewById(R.id.status_bar);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            statusBar.setVisibility(View.VISIBLE);
            statusBar.getLayoutParams().height = StatusBarUtil.getStatusBarHeight(this);
            statusBar.requestLayout();
        }

        AlbumHelper helper = AlbumHelper.getHelper();
        helper.init(getApplicationContext());

        dataList = getIntent().getParcelableArrayListExtra(ChoosePictureActivity.EXTRA_IMAGE_LIST);

        initView();
        TextView cancel = (TextView) findViewById(R.id.tv_cancel);
        cancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mOK = (Button) findViewById(R.id.bt);
        mOK.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                EventBus.getDefault().post(new ChooseImageResultEvent(mImageGridAdapter.mList));
                finish();
            }

        });
    }

    /**
     * 初妾化view视图
     */
    private void initView() {
        GridView gridView = (GridView) findViewById(R.id.grid_view);
        gridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
        mImageGridAdapter = new ImageGridAdapter(this, dataList);
        gridView.setAdapter(mImageGridAdapter);
        mImageGridAdapter.setTextCallback(new TextCallback() {
            public void onListen(int count) {
                mOK.setText("完成" + "(" + count + ")");
            }
        });
    }
}
