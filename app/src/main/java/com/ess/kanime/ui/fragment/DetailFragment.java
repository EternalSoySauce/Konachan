package com.ess.kanime.ui.fragment;

import android.content.Context;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.TextView;

import com.ess.kanime.bean.MsgBean;
import com.ess.kanime.bean.PoolBean;
import com.ess.kanime.bean.PostBean;
import com.ess.kanime.bean.ThumbBean;
import com.ess.kanime.global.Constants;
import com.ess.kanime.ui.activity.ImageDetailActivity;
import com.ess.kanime.utils.FileUtils;
import com.ess.kanime.R;
import com.ess.kanime.bean.ImageBean;
import com.ess.kanime.bean.TagBean;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class DetailFragment extends Fragment {

    private ImageDetailActivity mActivity;
    private ThumbBean mThumbBean;
    private ImageBean mImageBean;

    private View mRootView;
    private SwipeRefreshLayout mSwipeRefresh;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (ImageDetailActivity) context;
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // 防止软件进入后台过久被系统回收导致切换回来时产生空指针异常
        outState.putParcelable(Constants.THUMB_BEAN, mThumbBean);
        outState.putParcelable(Constants.IMAGE_BEAN, mImageBean);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mThumbBean = savedInstanceState.getParcelable(Constants.THUMB_BEAN);
            mImageBean = savedInstanceState.getParcelable(Constants.IMAGE_BEAN);
        } else {
            mThumbBean = mActivity.getThumbBean();
            mImageBean = mThumbBean.imageBean;
        }
        mRootView = inflater.inflate(R.layout.fragment_detail, container, false);
        initView();
        return mRootView;
    }

    private void initView() {
        mSwipeRefresh = (SwipeRefreshLayout) mRootView.findViewById(R.id.swipe_refresh_layout);
        mSwipeRefresh.setEnabled(false);

        if (mImageBean != null) {
            loadDetail(mImageBean);
        } else {
            mSwipeRefresh.setRefreshing(true);
            mSwipeRefresh.getChildAt(0).setVisibility(View.GONE);
        }
    }

    // 图片详情
    private void loadDetail(ImageBean imageBean) {
        /****************** Posts ******************/
        PostBean postBean = imageBean.posts[0];
        // 图片Id
        setText(R.id.post_id, R.string.detail_post_id, postBean.id);

        // 上传时间
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss", Locale.getDefault());
        String postCreatedTime = dateFormat.format(new Date(postBean.createdTime * 1000));
        setText(R.id.post_created_time, R.string.detail_post_created_time, postCreatedTime);

        // 用户Id
        setText(R.id.post_creator_id, R.string.detail_post_creator_id, postBean.creatorId);

        // 用户名
        setText(R.id.post_author, R.string.detail_post_author, postBean.author);

        // 图片分辨率
        String size = postBean.jpegWidth + " x " + postBean.jpegHeight;
        setText(R.id.post_size, R.string.detail_post_size, size);

        // 图片大小
        String fileSize;
        if (postBean.jpegFileSize != 0) {
            fileSize = FileUtils.computeFileSize(postBean.jpegFileSize);
        } else {
            fileSize = FileUtils.computeFileSize(postBean.fileSize);
        }
        setText(R.id.post_file_size, R.string.detail_post_file_size, fileSize);

        // 图片来源
        String source = postBean.source;
        boolean hyperlinkValue = true;
        if (TextUtils.isEmpty(source)) {
            source = getString(R.string.detail_post_no_source);
            hyperlinkValue = false;
        }
        setText(R.id.post_source, R.string.detail_post_source, source, hyperlinkValue);

        // 图片原作者
        ArrayList<String> artistList = imageBean.tags.artist;
        String artist = artistList.isEmpty() ? getString(R.string.detail_post_no_artist) : artistList.get(0);
        setText(R.id.post_artist, R.string.detail_post_artist, artist);

        // 图片评级
        String rating;
        switch (postBean.rating) {
            case Constants.RATING_S:
                rating = getString(R.string.detail_post_rating_s);
                break;
            case Constants.RATING_E:
                rating = getString(R.string.detail_post_rating_e);
                break;
            case Constants.RATING_Q:
            default:
                rating = getString(R.string.detail_post_rating_q);
                break;
        }
        setText(R.id.post_rating, R.string.detail_post_rating, rating);

        // 图片评分
        String score = String.valueOf(postBean.score);
        setText(R.id.post_score, R.string.detail_post_score, score);

        /****************** Tags ******************/
        ViewGroup layoutTag = (ViewGroup) mRootView.findViewById(R.id.layout_tag);
        TagBean tagBean = imageBean.tags;
        addTagViews(layoutTag, tagBean.copyright, R.color.color_copyright);
        addTagViews(layoutTag, tagBean.character, R.color.color_character);
        addTagViews(layoutTag, tagBean.artist, R.color.color_artist);
        addTagViews(layoutTag, tagBean.circle, R.color.color_circle);
        addTagViews(layoutTag, tagBean.style, R.color.color_style);
        addTagViews(layoutTag, tagBean.general, R.color.color_general);

        /****************** Pools ******************/
        if (imageBean.pools.length == 0) {
            return;
        }
        PoolBean poolBean = imageBean.pools[0];
        ViewStub viewStub = (ViewStub) mRootView.findViewById(R.id.view_stub_detail_pool);
        viewStub.inflate();

        // 图集Id
        setText(R.id.pool_id, R.string.detail_pool_id, poolBean.id);

        // 图集名称
        setText(R.id.pool_name, R.string.detail_pool_name, poolBean.name.replace("_", " "));

        // 创建时间
        String poolCreatedTime = poolBean.createdTime;
        poolCreatedTime = poolCreatedTime.substring(0, poolCreatedTime.lastIndexOf(".")).replace("T", " ");
        setText(R.id.pool_created_time, R.string.detail_pool_created_time, poolCreatedTime);

        // 最后更新时间
        String poolUpdatedTime = poolBean.updatedTime;
        poolUpdatedTime = poolUpdatedTime.substring(0, poolUpdatedTime.lastIndexOf(".")).replace("T", " ");
        setText(R.id.pool_updated_time, R.string.detail_pool_updated_time, poolUpdatedTime);

        // 图集简介
        String description = poolBean.description;
        if (TextUtils.isEmpty(description)) {
            description = getString(R.string.detail_pool_no_description);
        }
        setText(R.id.pool_description, R.string.detail_pool_description, description);
    }

    private void setText(int layoutId, int key, String value) {
        setText(layoutId, key, value, false);
    }

    private void setText(int layoutId, int key, String value, boolean hyperlinkValue) {
        ViewGroup layout = (ViewGroup) mRootView.findViewById(layoutId);

        TextView tvKey = (TextView) layout.findViewById(R.id.tv_key);
        tvKey.setText(key);

        TextView tvValue = (TextView) layout.findViewById(R.id.tv_value);
        tvValue.setText(value);
        if (hyperlinkValue) {
            tvValue.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
            tvValue.setAutoLinkMask(Linkify.ALL);
            tvValue.setLinkTextColor(getResources().getColor(R.color.color_text_unselected));
            tvValue.setMovementMethod(LinkMovementMethod.getInstance());
        }
    }

    public void addTagViews(ViewGroup parentLayout, ArrayList<String> tagList, int colorId) {
        for (String tag : tagList) {
            View view = View.inflate(mActivity, R.layout.layout_detail_item, null);
            TextView tvTag = (TextView) view.findViewById(R.id.tv_key);
            tvTag.setText(tag);
            tvTag.setTextColor(getResources().getColor(colorId));
            parentLayout.addView(view);
        }
    }

    //获取到图片详细信息后收到的通知，obj 为 Json (String)
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getImageDetail(MsgBean msgBean) {
        if (msgBean.msg.equals(Constants.GET_IMAGE_DETAIL)) {
            String json = (String) msgBean.obj;
            ImageBean imageBean = ImageBean.getImageDetailFromJson(json);
            if (mThumbBean.thumbUrl.equals(imageBean.posts[0].previewUrl)) {
                loadDetail(imageBean);
                mSwipeRefresh.setRefreshing(false);
                mSwipeRefresh.getChildAt(0).setVisibility(View.VISIBLE);
            }
        }
    }

    public static DetailFragment newInstance(String title) {
        DetailFragment fragment = new DetailFragment();
        Bundle bundle = new Bundle();
        bundle.putString(Constants.PAGE_TITLE, title);
        fragment.setArguments(bundle);
        return fragment;
    }
}
