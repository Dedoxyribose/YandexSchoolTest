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

        mRlContainer.measure(0,0);

        Log.d(TAG, "got="+Utils.calculateTextViewHeight(getActivity(), text, 16,
                Utils.getScreenWidth(getActivity())-Utils.dpToPx(20), Typeface.DEFAULT, 0));

        Log.d(TAG, "height="+mRlContainer.getMeasuredHeight());

        if (Utils.calculateTextViewHeight(getActivity(), text, 16,
                Utils.getScreenWidth(getActivity())-Utils.dpToPx(20), Typeface.DEFAULT, 0)>mRlContainer.getMeasuredHeight()) {
            Log.d(TAG, "in");
            mTvTextScrollable.setText(text);
        }
        else  mTvText.setText(text);

    }
}
