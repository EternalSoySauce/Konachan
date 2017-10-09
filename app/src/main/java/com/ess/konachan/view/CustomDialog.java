package com.ess.konachan.view;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.ess.konachan.R;

import static com.afollestad.materialdialogs.DialogAction.NEGATIVE;
import static com.afollestad.materialdialogs.DialogAction.NEUTRAL;
import static com.afollestad.materialdialogs.DialogAction.POSITIVE;

public class CustomDialog extends MaterialDialog.Builder {

    public CustomDialog(@NonNull Context context) {
        super(context);
    }

    @UiThread
    public MaterialDialog build() {
        MaterialDialog dialog = super.build();
        int colorText = context.getResources().getColor(R.color.color_dialog_text);
        dialog.getTitleView().setTextColor(colorText);
        TextView tvContent = dialog.getContentView();
        if (tvContent != null) {
            tvContent.setTextColor(colorText);
        }

        int colorButton = context.getResources().getColor(R.color.color_dialog_button);
        dialog.getActionButton(NEGATIVE).setTextColor(colorButton);
        dialog.getActionButton(POSITIVE).setTextColor(colorButton);
        dialog.getActionButton(NEUTRAL).setTextColor(colorButton);

        int colorBg = context.getResources().getColor(R.color.color_dialog_bg);
        ColorDrawable drawable = new ColorDrawable(colorBg);
        dialog.getWindow().setBackgroundDrawable(drawable);
        return dialog;
    }
}
