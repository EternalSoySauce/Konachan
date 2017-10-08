package com.ess.konachan.view;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.ess.konachan.R;

public class CustomDialog extends MaterialDialog.Builder {

    public CustomDialog(@NonNull Context context) {
        super(context);
        MaterialDialog dialog = build();
        int colorText = context.getResources().getColor(R.color.color_dialog_text);
        dialog.getTitleView().setTextColor(colorText);
        TextView tvContent = dialog.getContentView();
        if (tvContent != null) {
            tvContent.setTextColor(colorText);
        }

        int colorButton = context.getResources().getColor(R.color.color_dialog_button);
        dialog.getActionButton(DialogAction.NEGATIVE).setTextColor(colorButton);
        dialog.getActionButton(DialogAction.POSITIVE).setTextColor(colorButton);
        dialog.getActionButton(DialogAction.NEUTRAL).setTextColor(colorButton);

        int colorBg = context.getResources().getColor(R.color.color_dialog_bg);
        ColorDrawable drawable = new ColorDrawable(colorBg);
        dialog.getWindow().setBackgroundDrawable(drawable);
    }
}
