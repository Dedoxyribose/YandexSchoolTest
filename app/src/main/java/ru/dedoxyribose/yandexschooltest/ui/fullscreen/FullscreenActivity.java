package ru.dedoxyribose.yandexschooltest.ui.fullscreen;


import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lb.auto_fit_textview.AutoResizeTextView;

import me.grantland.widget.AutofitTextView;
import ru.dedoxyribose.yandexschooltest.R;
import ru.dedoxyribose.yandexschooltest.ui.standard.StandardActivity;
import ru.dedoxyribose.yandexschooltest.util.Utils;


public class FullscreenActivity extends StandardActivity {

    public static final String ARG_TEXT="ARG_TEXT";
    private static final String TAG = "FullscreenActivity";

    private AutoResizeTextView mTvText;
    private TextView mTvTextScrollable;
    private ImageView mIvClose;
    private RelativeLayout mRlContainer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen);

        mTvText = (AutoResizeTextView) findViewById(R.id.tvText);
        mTvTextScrollable = (TextView) findViewById(R.id.tvTextScrollable);
        mIvClose = (ImageView) findViewById(R.id.ivClose);
        mRlContainer = (RelativeLayout) findViewById(R.id.rlContainer);

        mIvClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        String text=getIntent().getStringExtra(ARG_TEXT);

        if (text==null) text="";

        mTvTextScrollable.setText(text);

        //если текст очень длинный, показываем в TextView внутри ScrollView с фиксированным textSize;
        //иначе оставляем в AutoResizeTextView

        final String finalText = text;
        mTvTextScrollable.post(new Runnable() {
            @Override
            public void run() {
                if (mTvTextScrollable.getHeight()>mRlContainer.getHeight()) {
                    mTvTextScrollable.setVisibility(View.VISIBLE);
                }
                else {
                    mTvTextScrollable.setVisibility(View.GONE);
                    mTvText.setVisibility(View.VISIBLE);
                    mTvText.setText(finalText);
                }
            }
        });

    }
}
