package com.zjca.qqdialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


public class ActionSheetDialog {
    private Context mContext;
    private Dialog mDialog;
    private TextView mTitle;
    private TextView mCancel;
    private LinearLayout mLinearLayout;
    private ScrollView mScrollView;
    private boolean showTitle = false;
    private List<SheetItem> sheetItemList;
    private Display mDisplay;
    private View mUnderLine;
    private OnCancelClickListener cancelListener;

    public ActionSheetDialog(Context context) {
        this.mContext = context;
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        mDisplay = windowManager.getDefaultDisplay();
    }

    public ActionSheetDialog builder() {
        // 获取Dialog布局
        View view = LayoutInflater.from(mContext).inflate(R.layout.qq_dialog_layout, null);
        // 设置Dialog最小宽度为屏幕宽度
        view.setMinimumWidth(mDisplay.getWidth());

        // 获取自定义Dialog布局中的控件
        mScrollView = (ScrollView) view.findViewById(R.id.scrollview);
        mLinearLayout = (LinearLayout) view.findViewById(R.id.ll_content);
        mTitle = (TextView) view.findViewById(R.id.tv_title);
        mUnderLine = (View) view.findViewById(R.id.title_underline);
        mCancel = (TextView) view.findViewById(R.id.tv_cancel);
        mCancel.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mDialog.dismiss();
                if (cancelListener != null) {
                    cancelListener.onCancelClick();
                }
            }
        });

        // 定义Dialog布局和参数
        mDialog = new Dialog(mContext, R.style.ActionSheetDialogStyle);
        mDialog.setContentView(view);
        mDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if (cancelListener != null) {
                    cancelListener.onCancelClick();
                }
            }
        });
        Window dialogWindow = mDialog.getWindow();
        dialogWindow.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM);
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.x = 0;
        lp.y = 0;
        dialogWindow.setAttributes(lp);
        return this;
    }

    public ActionSheetDialog setCancelListener(OnCancelClickListener cancelListener) {
        this.cancelListener = cancelListener;
        return this;
    }

    public ActionSheetDialog setDismissListener(DialogInterface.OnDismissListener dismissListener) {
        mDialog.setOnDismissListener(dismissListener);
        return this;
    }

    public ActionSheetDialog setTitle(String title) {
        showTitle = true;
        mTitle.setVisibility(View.VISIBLE);
        mTitle.setText(title);
        return this;
    }

    public ActionSheetDialog setCancelable(boolean cancel) {
        mDialog.setCancelable(cancel);
        return this;
    }

    public ActionSheetDialog setCanceledOnTouchOutside(boolean cancel) {
        mDialog.setCanceledOnTouchOutside(cancel);
        return this;
    }

    /**
     * @param strItem  条目名称
     * @param color    条目字体颜色，设置null则默认蓝色
     * @param listener
     * @return
     */
    public ActionSheetDialog addSheetItem(String strItem, SheetItemColor color, OnSheetItemClickListener listener) {
        if (sheetItemList == null) {
            sheetItemList = new ArrayList();
        }
        sheetItemList.add(new SheetItem(strItem, color, listener));
        return this;
    }

    /**
     * 设置条目布局
     */

    private void setSheetItems() {
        if (sheetItemList == null || sheetItemList.size() <= 0) {
            return;
        }
        mLinearLayout.removeAllViews();
        int size = sheetItemList.size();
        // TODO 高度控制，非最佳解决办法
        // 添加条目过多的时候控制高度
        if (size >= 7) {
            LinearLayout.LayoutParams params = (LayoutParams) mScrollView.getLayoutParams();
            params.height = mDisplay.getHeight() / 2;
            mScrollView.setLayoutParams(params);
        }

        // 循环添加条目
        for (int i = 1; i <= size; i++) {
            final int index = i;
            SheetItem sheetItem = sheetItemList.get(i - 1);
            String strItem = sheetItem.name;
            SheetItemColor color = sheetItem.color;
            final OnSheetItemClickListener listener = sheetItem.itemClickListener;
            TextView textView = new TextView(mContext);
            textView.setText(strItem);
            textView.setTextSize(18);
            textView.setGravity(Gravity.CENTER);
            // 背景图片
            if (size == 1) {
                if (showTitle) {
                    mUnderLine.setVisibility(View.VISIBLE);
                    textView.setBackgroundResource(R.drawable.actionsheet_bottom_selector);
                } else {
                    textView.setBackgroundResource(R.drawable.actionsheet_single_selector);
                }

            } else {

                if (showTitle) {
                    mUnderLine.setVisibility(View.VISIBLE);
                    if (i >= 1 && i < size) {
                        textView.setBackgroundResource(R.drawable.actionsheet_middle_selector);
                    } else {
                        textView.setBackgroundResource(R.drawable.actionsheet_bottom_selector);
                    }

                } else {
                    if (i == 1) {
                        textView.setBackgroundResource(R.drawable.actionsheet_top_selector);
                    } else if (i < size) {
                        textView.setBackgroundResource(R.drawable.actionsheet_middle_selector);
                    } else {
                        textView.setBackgroundResource(R.drawable.actionsheet_bottom_selector);
                    }

                }

            }

            // 字体颜色
            if (color == null) {
                textView.setTextColor(Color.parseColor(SheetItemColor.Blue.getName()));
            } else {
                textView.setTextColor(Color.parseColor(color.getName()));
            }
            // 高度
            float scale = mContext.getResources().getDisplayMetrics().density;
            int height = (int) (45 * scale + 0.5f);
            textView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, height));
            // 点击事件
            textView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onClick(index);
                    mDialog.dismiss();
                }
            });
            mLinearLayout.addView(textView);
        }
    }

    public void show() {
        setSheetItems();
        mDialog.show();
    }

    public interface OnSheetItemClickListener {
        void onClick(int which);
    }

    /**
     * 点击取消事件监听
     */
    public interface OnCancelClickListener {
        void onCancelClick();
    }

    public class SheetItem {
        String name;
        OnSheetItemClickListener itemClickListener;
        SheetItemColor color;

        public SheetItem(String name, SheetItemColor color, OnSheetItemClickListener itemClickListener) {
            this.name = name;
            this.color = color;
            this.itemClickListener = itemClickListener;
        }

    }

    public enum SheetItemColor {

        Blue("#037BFF"), Red("#FD4A2E");
        private String name;

        private SheetItemColor(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
