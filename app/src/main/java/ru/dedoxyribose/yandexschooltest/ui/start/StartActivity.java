package ru.dedoxyribose.yandexschooltest.ui.start;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v7.view.menu.MenuView;
import android.util.SparseArray;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.arellomobile.mvp.presenter.InjectPresenter;

import me.zhanghai.android.materialprogressbar.MaterialProgressBar;
import ru.dedoxyribose.yandexschooltest.R;
import ru.dedoxyribose.yandexschooltest.ui.main.MainActivity;
import ru.dedoxyribose.yandexschooltest.ui.main.MainPresenter;
import ru.dedoxyribose.yandexschooltest.ui.main.MainView;
import ru.dedoxyribose.yandexschooltest.ui.standard.StandardActivity;
import ru.dedoxyribose.yandexschooltest.ui.translate.TranslateFragment;
import ru.dedoxyribose.yandexschooltest.widget.NonSwipeableViewPager;

public class StartActivity extends StandardActivity implements StartView {

    @InjectPresenter
    StartPresenter mPresenter;

    private MaterialProgressBar mPbLoading;
    private LinearLayout mLlError;
    private TextView mTvError;
    private Button mbRepeat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        mPbLoading=(MaterialProgressBar)findViewById(R.id.pbLoad);
        mLlError=(LinearLayout)findViewById(R.id.llError);
        mTvError=(TextView)findViewById(R.id.tvError);
        mbRepeat=(Button)findViewById(R.id.bRepeat);

        mbRepeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPresenter.repeatClicked();
            }
        });

    }

    @Override
    public void showError(boolean show, String text) {
        mLlError.setVisibility(show?View.VISIBLE:View.GONE);
        mTvError.setText(text);
    }

    @Override
    public void showLoading(boolean show) {
        mPbLoading.setVisibility(show?View.VISIBLE:View.GONE);
    }

    @Override
    public void proceedToMain() {
        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
