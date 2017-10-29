package com.ess.konachan.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.ess.konachan.R;
import com.ess.konachan.global.Data;

public class CustomDialog extends MaterialDialog.Builder {

    public CustomDialog(@NonNull Context context) {
        super(context);
        this.titleColorRes(R.color.color_dialog_text)
                .contentColorRes(R.color.color_dialog_text)
                .positiveColorRes(R.color.color_dialog_button)
                .negativeColorRes(R.color.color_dialog_button)
                .neutralColorRes(R.color.color_dialog_button)
                .backgroundColorRes(R.color.color_dialog_bg);
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
        tvContent.setText(Data.getTagTypeHelpDoc(context));

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
        tvContent.setText(Data.getAdvancedSearchDoc(context));

        MaterialDialog dialog = new CustomDialog(context)
                .title(R.string.dialog_doc_advanced_search_title)
                .customView(view, false)
                .positiveText(R.string.dialog_doc_sure)
                .show();
    }

    public interface OnDialogActionListener {
        void onPositive();
    }
}
