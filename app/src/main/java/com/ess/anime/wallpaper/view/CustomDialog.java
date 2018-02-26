package com.ess.anime.wallpaper.view;

import android.content.Context;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.ess.anime.wallpaper.R;
import com.ess.anime.wallpaper.bean.MsgBean;
import com.ess.anime.wallpaper.global.Constants;
import com.ess.anime.wallpaper.global.DocData;
import com.ess.anime.wallpaper.http.OkHttp;

import org.greenrobot.eventbus.EventBus;

import java.util.Arrays;
import java.util.List;

public class CustomDialog extends MaterialDialog.Builder {

    public CustomDialog(@NonNull Context context) {
        super(context);
        this.titleColorRes(R.color.color_dialog_text)
                .contentColorRes(R.color.color_dialog_text)
                .positiveColorRes(R.color.color_dialog_button)
                .negativeColorRes(R.color.color_dialog_button)
                .neutralColorRes(R.color.color_dialog_button)
                .backgroundColorRes(R.color.color_dialog_bg)
                .itemsColorRes(R.color.color_dialog_text)
                .choiceWidgetColor(context.getResources().getColorStateList(R.color.dialog_widget_seletor));
    }

    /**
     * 提示图片已存在，询问是否重新下载
     *
     * @param context  上下文
     * @param listener 事件监听器
     */
    public static void showPromptToReloadImage(Context context, final OnDialogActionListener listener) {
        MaterialDialog dialog = new CustomDialog(context)
                .content(R.string.dialog_reload_msg)
                .negativeText(R.string.dialog_reload_no)
                .positiveText(R.string.dialog_reload_yes)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        listener.onPositive();
                    }
                }).show();
    }

    /**
     * 删除收藏图片
     *
     * @param context  上下文
     * @param listener 事件监听器
     */
    public static void showDeleteCollectionDialog(Context context, String msg, final OnDialogActionListener listener) {
        MaterialDialog dialog = new CustomDialog(context)
                .content(msg)
                .negativeText(R.string.dialog_delete_cancel)
                .positiveText(R.string.dialog_delete_sure)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        listener.onPositive();
                    }
                }).show();
    }

    /**
     * 显示标签类型说明文档
     *
     * @param context 上下文
     */
    public static void showTagTypeHelpDialog(Context context) {
        View view = View.inflate(context, R.layout.layout_dialog_scroll_text, null);
        TextView tvContent = (TextView) view.findViewById(R.id.tv_content);
        tvContent.setText(DocData.getTagTypeHelpDoc(context));

        MaterialDialog dialog = new CustomDialog(context)
                .title(R.string.dialog_doc_tag_type_title)
                .customView(view, false)
                .positiveText(R.string.dialog_doc_sure)
                .show();
    }

    /**
     * 显示高级搜索说明文档
     *
     * @param context 上下文
     */
    public static void showAdvancedSearchHelpDialog(Context context) {
        View view = View.inflate(context, R.layout.layout_dialog_scroll_text, null);
        TextView tvContent = (TextView) view.findViewById(R.id.tv_content);
        tvContent.setText(DocData.getAdvancedSearchDoc(context));

        MaterialDialog dialog = new CustomDialog(context)
                .title(R.string.dialog_doc_advanced_search_title)
                .customView(view, false)
                .positiveText(R.string.dialog_doc_sure)
                .show();
    }

    /**
     * 需要SD卡权限时的提示
     *
     * @param context  上下文
     * @param listener 事件监听器
     */
    public static void showNeedStoragePermissionDialog(Context context, final OnDialogActionListener listener) {
        MaterialDialog dialog = new CustomDialog(context)
                .title(R.string.dialog_permission_rationale_title)
                .content(R.string.dialog_permission_rationale_msg)
                .cancelable(false)
                .canceledOnTouchOutside(false)
                .negativeText(R.string.dialog_permission_rationale_deny)
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        listener.onNegative();
                    }
                })
                .positiveText(R.string.dialog_permission_rationale_grant)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        listener.onPositive();
                    }
                }).show();
    }

    /**
     * 跳转至系统权限设置界面提示
     *
     * @param context  上下文
     * @param listener 事件监听器
     */
    public static void showGoToSettingDialog(Context context, final OnDialogActionListener listener) {
        MaterialDialog dialog = new CustomDialog(context)
                .title(R.string.dialog_permission_setting_title)
                .content(R.string.dialog_permission_setting_msg)
                .cancelable(false)
                .canceledOnTouchOutside(false)
                .negativeText(R.string.dialog_permission_setting_cancel)
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        listener.onNegative();
                    }
                })
                .positiveText(R.string.dialog_permission_setting_ok)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        listener.onPositive();
                    }
                }).show();
    }

    /**
     * 切换网站
     *
     * @param context  上下文
     * @param listener 事件监听器
     */
    public static void showChangeBaseUrlDialog(final Context context, final OnDialogActionListener listener) {
        String baseUrl = OkHttp.getBaseUrl(context);
        final List<String> baseList = Arrays.asList(Constants.BASE_URLS);
        final int baseIndex = baseList.indexOf(baseUrl);

        MaterialDialog dialog = new CustomDialog(context)
                .title(R.string.dialog_change_base_url_title)
                .negativeText(R.string.dialog_change_base_url_cancel)
                .items(R.array.website_list_item)
                .itemsCallbackSingleChoice(baseIndex, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                        if (which != baseIndex) {
                            PreferenceManager.getDefaultSharedPreferences(context).edit()
                                    .putString(Constants.BASE_URL, baseList.get(which)).apply();
                            // 发送通知到PostFragment, PoolFragment
                            EventBus.getDefault().post(new MsgBean(Constants.CHANGE_BASE_URL, null));
                        }
                        listener.onPositive();
                        return true;
                    }
                })
                .alwaysCallSingleChoiceCallback()
                .show();
    }

    public interface OnDialogActionListener {
        void onPositive();

        void onNegative();
    }

    public static class SimpleDialogActionListener implements OnDialogActionListener {

        @Override
        public void onPositive() {
        }

        @Override
        public void onNegative() {
        }
    }
}
