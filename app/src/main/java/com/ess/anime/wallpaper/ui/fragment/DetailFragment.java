package com.ess.anime.wallpaper.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.util.Linkify;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.TextView;
import android.widget.Toast;

import com.ess.anime.wallpaper.R;
import com.ess.anime.wallpaper.bean.ImageBean;
import com.ess.anime.wallpaper.bean.MsgBean;
import com.ess.anime.wallpaper.bean.PoolBean;
import com.ess.anime.wallpaper.bean.PostBean;
import com.ess.anime.wallpaper.bean.TagBean;
import com.ess.anime.wallpaper.bean.ThumbBean;
import com.ess.anime.wallpaper.database.GreenDaoUtils;
import com.ess.anime.wallpaper.database.SearchTagBean;
import com.ess.anime.wallpaper.global.Constants;
import com.ess.anime.wallpaper.ui.activity.HyperlinkActivity;
import com.ess.anime.wallpaper.ui.activity.ImageDetailActivity;
import com.ess.anime.wallpaper.ui.activity.MainActivity;
import com.ess.anime.wallpaper.utils.FileUtils;
import com.ess.anime.wallpaper.utils.SystemUtils;
import com.ess.anime.wallpaper.utils.TimeFormat;
import com.ess.anime.wallpaper.utils.WebLinkMethod;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Arrays;
import java.util.List;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import butterknife.BindView;

public class DetailFragment extends BaseFragment {

    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout mSwipeRefresh;
    @BindView(R.id.layout_tag)
    ViewGroup mLayoutTag;

    private ImageDetailActivity mActivity;
    private ThumbBean mThumbBean;
    private ImageBean mImageBean;
    private Toast mCurrentToast;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (ImageDetailActivity) context;
    }

    @Override
    int layoutRes() {
        return R.layout.fragment_detail;
    }

    @Override
    void init(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mThumbBean = savedInstanceState.getParcelable(Constants.THUMB_BEAN);
            mImageBean = savedInstanceState.getParcelable(Constants.IMAGE_BEAN);
        } else {
            mThumbBean = mActivity.getThumbBean();
            mImageBean = mActivity.getImageBean();
        }
        initView();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // 防止软件进入后台过久被系统回收导致切换回来时产生空指针异常
        outState.putParcelable(Constants.THUMB_BEAN, mThumbBean);
        outState.putParcelable(Constants.IMAGE_BEAN, mImageBean);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }

    private void initView() {
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
        if (imageBean == null || imageBean.posts.length == 0) {
            return;
        }

        /****************** Posts ******************/
        PostBean postBean = imageBean.posts[0];
        // 图片Id
        setText(R.id.post_id, R.string.detail_post_id, postBean.id);

        // 上传时间
        String postCreatedTime = TimeFormat.dateFormat(postBean.createdTime * 1000, "yyyy-MM-dd  HH:mm:ss");
        setText(R.id.post_created_time, R.string.detail_post_created_time, postCreatedTime);

        // 用户Id
        String creatorId = TextUtils.isEmpty(postBean.creatorId)
                ? getString(R.string.unknown)
                : postBean.creatorId;
        setText(R.id.post_creator_id, R.string.detail_post_creator_id, creatorId);

        // 用户名
        String author = TextUtils.isEmpty(postBean.author)
                ? getString(R.string.unknown)
                : postBean.author.replace("_", " ");
        setText(R.id.post_author, R.string.detail_post_author, author);

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
            source = getString(R.string.unknown);
            hyperlinkValue = false;
        }
        setText(R.id.post_source, R.string.detail_post_source, source, hyperlinkValue);

        // 图片原作者
        List<String> artistList = imageBean.tags.artist;
        String artist = artistList.isEmpty()
                ? getString(R.string.unknown)
                : artistList.get(0).replace("_", " ");
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
        TagBean tagBean = imageBean.tags;
        addTagViews(mLayoutTag, tagBean.copyright, R.color.color_copyright);
        addTagViews(mLayoutTag, tagBean.character, R.color.color_character);
        addTagViews(mLayoutTag, tagBean.artist, R.color.color_artist);
        addTagViews(mLayoutTag, tagBean.circle, R.color.color_circle);
        addTagViews(mLayoutTag, tagBean.style, R.color.color_style);
        addTagViews(mLayoutTag, tagBean.general, R.color.color_general);

        /****************** Pools ******************/
        if (imageBean.pools.length == 0) {
            return;
        }
        PoolBean poolBean = imageBean.pools[0];
        ViewStub viewStub = mRootView.findViewById(R.id.view_stub_detail_pool);
        viewStub.inflate();

        // 图集Id
        setText(R.id.pool_id, R.string.detail_pool_id, poolBean.id);

        // 图集名称
        setText(R.id.pool_name, R.string.detail_pool_name, poolBean.name.replace("_", " "));

        // 创建时间
        String poolCreatedTime = poolBean.createdTime;
        poolCreatedTime = TextUtils.isEmpty(poolCreatedTime)
                ? getString(R.string.unknown)
                : formatPoolTime(poolCreatedTime);
        setText(R.id.pool_created_time, R.string.detail_pool_created_time, poolCreatedTime);

        // 最后更新时间
        String poolUpdatedTime = poolBean.updatedTime;
        poolUpdatedTime = TextUtils.isEmpty(poolUpdatedTime)
                ? getString(R.string.unknown)
                : formatPoolTime(poolUpdatedTime);
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
        ViewGroup layout = mRootView.findViewById(layoutId);

        TextView tvKey = layout.findViewById(R.id.tv_key);
        tvKey.setText(key);

        TextView tvValue = layout.findViewById(R.id.tv_value);
        tvValue.setText(value);
        if (hyperlinkValue) {
            tvValue.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
            tvValue.setAutoLinkMask(Linkify.WEB_URLS);
            tvValue.setLinksClickable(false);
            tvValue.setLinkTextColor(getResources().getColor(R.color.color_text_unselected));
            tvValue.setMovementMethod(WebLinkMethod.getInstance().setOnHyperlinkListener(url -> HyperlinkActivity.launch(mActivity, url)));
        }
    }

    private void addTagViews(ViewGroup parentLayout, List<String> tagList, int colorId) {
        for (String tag : tagList) {
            View view = View.inflate(mActivity, R.layout.layout_tag_item, null);
            TextView tvTag = view.findViewById(R.id.tv_tag);
            tvTag.setText(tag);
            tvTag.setTextColor(getResources().getColor(colorId));
            view.findViewById(R.id.iv_search).setOnClickListener(v -> {
                if (SystemUtils.isActivityActive(mActivity)) {
                    int searchMode = Constants.SEARCH_MODE_TAGS;
                    GreenDaoUtils.updateSearchTag(new SearchTagBean(tag, searchMode, System.currentTimeMillis()));
                    Intent intent = new Intent(mActivity, MainActivity.class);
                    intent.putExtra(Constants.SEARCH_TAG, tag);
                    intent.putExtra(Constants.SEARCH_MODE, searchMode);
                    startActivity(intent);
                    mActivity.finish();
                }
            });
            view.findViewById(R.id.iv_copy).setOnClickListener(v -> {
                if (SystemUtils.isActivityActive(mActivity)) {
                    SystemUtils.setClipString(mActivity, tag);
                    cancelCurrentToast();
                    showNewToast(getString(R.string.already_set_clipboard));
                }
            });
            view.findViewById(R.id.iv_add_to).setOnClickListener(v -> {
                if (SystemUtils.isActivityActive(mActivity)) {
                    String firstClipString = SystemUtils.getFirstClipString(mActivity);
                    if (TextUtils.isEmpty(firstClipString)) {
                        firstClipString = tag;
                    } else {
                        String[] tags = firstClipString.split("[,，]");
                        if (!Arrays.asList(tags).contains(tag)) {
                            firstClipString += "," + tag;
                        }
                    }
                    SystemUtils.setClipString(mActivity, firstClipString);
                    cancelCurrentToast();
                    showNewToast(getString(R.string.already_add_to_clipboard));
                }
            });
            parentLayout.addView(view);
        }
    }

    private void cancelCurrentToast() {
        if (mCurrentToast != null) {
            mCurrentToast.cancel();
            mCurrentToast = null;
        }
    }

    private void showNewToast(String toast) {
        mCurrentToast = Toast.makeText(mActivity, toast, Toast.LENGTH_SHORT);
        mCurrentToast.show();
    }

    // konachan,yandere格式：2017-09-19T19:42:58.325Z
    // lolibooru格式：2017-11-23 05:14:44
    private String formatPoolTime(String time) {
        return time.contains(".") ? time.substring(0, time.lastIndexOf(".")).replace("T", " ") : time;
    }

    //获取到图片详细信息后收到的通知，obj 为 Json (String)
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getImageDetail(MsgBean msgBean) {
        if (msgBean.msg.equals(Constants.GET_IMAGE_DETAIL)) {
            String json = (String) msgBean.obj;
            ImageBean imageBean = ImageBean.getImageDetailFromJson(json);
            if (mThumbBean.checkImageBelongs(imageBean) && imageBean.hasPostBean()) {
                setImageDetail(imageBean);
            }
        }
    }

    // PoolPostFragment获取到imageBean后重新根据ID请求tempPost后收到的通知，obj 为 thumbBean
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void reloadDetailById(MsgBean msgBean) {
        if (msgBean.msg.equals(Constants.RELOAD_DETAIL_BY_ID)) {
            ThumbBean thumbBean = (ThumbBean) msgBean.obj;
            if (TextUtils.equals(mThumbBean.linkToShow, thumbBean.linkToShow)) {
                mThumbBean = thumbBean;
                setImageDetail(thumbBean.imageBean);
            }
        }
    }

    private void setImageDetail(ImageBean imageBean) {
        mThumbBean.imageBean = imageBean;
        mThumbBean.checkToReplacePostData();
        loadDetail(imageBean);
        mSwipeRefresh.setRefreshing(false);
        mSwipeRefresh.getChildAt(0).setVisibility(View.VISIBLE);
    }

}
