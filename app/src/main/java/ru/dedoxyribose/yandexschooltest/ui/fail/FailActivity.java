package ru.dedoxyribose.yandexschooltest.ui.fail;


import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lb.auto_fit_textview.AutoResizeTextView;

import ru.dedoxyribose.yandexschooltest.R;
import ru.dedoxyribose.yandexschooltest.ui.standard.StandardActivity;


//активити, показываемая в случае фатальной ошибки, например отсутствия языков в БД
public class FailActivity extends StandardActivity {

    private static final String TAG = "FailActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fail);

        getDaoSession().getLangDao().deleteAll();


    }
}
