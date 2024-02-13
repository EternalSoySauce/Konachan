package com.ess.anime.wallpaper.ui.fragment;

import android.content.Context;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.util.Linkify;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.TextView;

import com.ess.anime.wallpaper.R;
import com.ess.anime.wallpaper.adapter.RecyclerDetailTagMorePopupAdapter;
import com.ess.anime.wallpaper.bean.ImageBean;
import com.ess.anime.wallpaper.bean.MsgBean;
import com.ess.anime.wallpaper.bean.PoolBean;
import com.ess.anime.wallpaper.bean.PostBean;
import com.ess.anime.wallpaper.bean.TagBean;
import com.ess.anime.wallpaper.bean.ThumbBean;
import com.ess.anime.wallpaper.database.GreenDaoUtils;
import com.ess.anime.wallpaper.global.Constants;
import com.ess.anime.wallpaper.listener.FlingEffector;
import com.ess.anime.wallpaper.model.entity.DetailTagMoreItem;
import com.ess.anime.wallpaper.model.helper.TagOperationHelper;
import com.ess.anime.wallpaper.ui.activity.ImageDetailActivity;
import com.ess.anime.wallpaper.ui.activity.web.HyperlinkActivity;
import com.ess.anime.wallpaper.ui.view.CustomDialog;
import com.ess.anime.wallpaper.ui.view.image.ToggleImageView;
import com.ess.anime.wallpaper.utils.FileUtils;
import com.ess.anime.wallpaper.utils.SystemUtils;
import com.ess.anime.wallpaper.utils.TimeFormat;
import com.ess.anime.wallpaper.utils.UIUtils;
import com.ess.anime.wallpaper.utils.WebLinkMethod;
import com.jiang.android.indicatordialog.IndicatorBuilder;
import com.jiang.android.indicatordialog.IndicatorDialog;
import com.qmuiteam.qmui.util.QMUIDeviceHelper;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import butterknife.BindView;

public class DetailFragment extends BaseFragment {

    @BindView(R.id.view_touch)
    View mTouchView;
    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout mSwipeRefresh;
    @BindView(R.id.layout_detail_container)
    ViewGroup mLayoutDetailContainer;

    private ImageDetailActivity mActivity;
    private ThumbBean mThumbBean;
    private ImageBean mImageBean;

    private IndicatorDialog mPopup;

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
    }

    @Override
    void updateUI() {
        super.updateUI();
        dismissMoreMenu();
        updateContentLayout();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // 防止软件进入后台过久被系统回收导致切换回来时产生空指针异常
        outState.putParcelable(Constants.THUMB_BEAN, mThumbBean);
        outState.putParcelable(Constants.IMAGE_BEAN, mImageBean);
    }

    private void initView() {
        mSwipeRefresh.setEnabled(false);
        if (mImageBean == null) {
            mSwipeRefresh.setRefreshing(true);
            mSwipeRefresh.getChildAt(0).setVisibility(View.GONE);
            mTouchView.setVisibility(View.VISIBLE);
        } else {
            mTouchView.setVisibility(View.GONE);
        }

        FlingEffector.addFlingEffect(mTouchView, (e1, e2, velocityX, velocityY) -> {
            if (SystemUtils.isActivityActive(mActivity)) {
                mActivity.flingToQuickSwitch(velocityX, velocityY);
            }
        });

        FlingEffector.addFlingEffect(mLayoutDetailContainer, (e1, e2, velocityX, velocityY) -> {
            if (SystemUtils.isActivityActive(mActivity)) {
                mActivity.flingToQuickSwitch(velocityX, velocityY);
            }
        });
    }

    private void updateContentLayout() {
        if (mActivity != null && mLayoutDetailContainer != null) {
            mLayoutDetailContainer.removeAllViews();
            if (UIUtils.isLandscape(mActivity) && QMUIDeviceHelper.isTablet(mActivity)) {
                View.inflate(mActivity, R.layout.fragment_detail_content_landscape, mLayoutDetailContainer);
            } else {
                View.inflate(mActivity, R.layout.fragment_detail_content_portrait, mLayoutDetailContainer);
            }
            showImageDetail(mImageBean);
        }
    }

    // 图片详情
    private void showImageDetail(ImageBean imageBean) {
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
        ViewGroup layoutTag = mLayoutDetailContainer.findViewById(R.id.layout_tag);
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
            // tag内容
            TextView tvTag = view.findViewById(R.id.tv_tag);
            tvTag.setText(tag);
            tvTag.setTextColor(getResources().getColor(colorId));
            // 收藏
            ToggleImageView ivFavorite = view.findViewById(R.id.iv_favorite);
            ivFavorite.setChecked(GreenDaoUtils.isFavoriteTag(tag));
            ivFavorite.setOnClickListener(v -> {
                TagOperationHelper.setTagFavorite(tag, ivFavorite.isChecked());
            });
            // 备注
            view.findViewById(R.id.iv_annotation).setOnClickListener(v -> {
                showTagAnnotation(tag);
            });
            // 更多菜单
            view.findViewById(R.id.iv_more).setOnClickListener(v -> {
                showMoreMenu(v, tag);
            });
            parentLayout.addView(view);
        }
    }

    private void showTagAnnotation(String tag) {
        if (SystemUtils.isActivityActive(mActivity)) {
            CustomDialog.showEditTagAnnotationDialog(mActivity, tag, false, null);
        }
    }

    private void showMoreMenu(View anchorView, String tag) {
        if (SystemUtils.isActivityActive(mActivity)) {
            List<DetailTagMoreItem> items = new ArrayList<>();
            items.add(new DetailTagMoreItem(getString(R.string.detail_tag_more_item_search), R.drawable.ic_tag_search, () -> {
                TagOperationHelper.searchTag(mActivity, tag);
            }));
            items.add(new DetailTagMoreItem(getString(R.string.detail_tag_more_item_copy_clipboard), R.drawable.ic_tag_copy, () -> {
                TagOperationHelper.copyTagToClipboard(mActivity, tag);
            }));
            items.add(new DetailTagMoreItem(getString(R.string.detail_tag_more_item_append_clipboard), R.drawable.ic_tag_append, () -> {
                TagOperationHelper.appendTagToClipboard(mActivity, tag);
            }));
            RecyclerDetailTagMorePopupAdapter adapter = new RecyclerDetailTagMorePopupAdapter(items);

            int[] layoutSize = adapter.measureItemsSize(mActivity);
            int layoutWidth = layoutSize[0];
            int layoutHeight = layoutSize[1];
            int anchorViewBottomPos = UIUtils.getLocationInWindow(anchorView)[1] + anchorView.getHeight();
            int screenHeight = UIUtils.getScreenHeight(mActivity);
            IndicatorDialog popup = new IndicatorBuilder(mActivity)
                    .width(layoutWidth)
                    .height(-1)
                    .bgColor(getResources().getColor(R.color.colorPrimary))
                    .animator(android.R.style.Animation_Dialog)
                    .dimEnabled(false)
                    .gravity(IndicatorBuilder.GRAVITY_RIGHT)
                    .arrowWidth(UIUtils.dp2px(mActivity, 10))
                    .ArrowDirection(anchorViewBottomPos + layoutHeight > screenHeight ? IndicatorBuilder.BOTTOM : IndicatorBuilder.TOP)
                    .ArrowRectage(0.92f)
                    .radius(16)
                    .layoutManager(new LinearLayoutManager(mActivity))
                    .adapter(adapter)
                    .create();

            adapter.setOnItemClickListener((baseQuickAdapter, view, i) -> {
                popup.dismiss();
                DetailTagMoreItem item = adapter.getItem(i);
                if (item != null) {
                    Runnable callback = item.getClickCallback();
                    if (callback != null) {
                        callback.run();
                    }
                }
            });

            popup.setCanceledOnTouchOutside(true);
            popup.getDialog().setOnShowListener(dialog -> UIUtils.setBackgroundAlpha(mActivity, 0.4f));
            popup.getDialog().setOnDismissListener(dialog -> {
                UIUtils.setBackgroundAlpha(mActivity, 1f);
                mPopup = null;
            });
            popup.show(anchorView);
            mPopup = popup;
        }
    }

    private void dismissMoreMenu() {
        if (mPopup != null) {
            mPopup.dismiss();
        }
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
        mImageBean = imageBean;
        mThumbBean.imageBean = imageBean;
        mThumbBean.checkToReplacePostData();
        showImageDetail(imageBean);
        mSwipeRefresh.setRefreshing(false);
        mSwipeRefresh.getChildAt(0).setVisibility(View.VISIBLE);
        mTouchView.setVisibility(View.GONE);
    }

}
