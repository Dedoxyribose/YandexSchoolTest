package ru.dedoxyribose.yandexschooltest.ui.chooselang;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.arellomobile.mvp.presenter.InjectPresenter;

import java.util.ArrayList;
import java.util.List;

import ru.dedoxyribose.yandexschooltest.R;
import ru.dedoxyribose.yandexschooltest.model.entity.Lang;
import ru.dedoxyribose.yandexschooltest.ui.standard.StandardActivity;
import ru.dedoxyribose.yandexschooltest.util.Utils;

public class ChooseLangActivity extends StandardActivity implements ChooseLangView {

    public static final String ARG_LANG_POSITION="ARG_LANG_POSITION";
    public static final String ARG_CUR_LANG="ARG_CUR_LANG";

    public static final String RES_ARG_CHOSEN_LANG_CODE="RES_ARG_CHOSEN_LANG_CODE";

    public static final int LANG_POSITION_FROM=0;
    public static final int LANG_POSITION_TO=1;

    @InjectPresenter
    ChooseLangPresenter mPresenter;

    private ImageView mIvBack;
    private RecyclerView mRvList;
    private TextView mTvTitle;

    private List<Lang> mAllLangs = new ArrayList<>();
    private List<Lang> mRecentLangs = new ArrayList<>();
    private boolean mShowDetermineLang=false;
    private int mCurLangPos=-1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_lang);

        mIvBack = (ImageView) findViewById(R.id.ivBack);
        mRvList = (RecyclerView) findViewById(R.id.rvList);
        mTvTitle = (TextView) findViewById(R.id.tvTitle);

        mPresenter.setIntent(getIntent());

        mIvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPresenter.backClicked();
            }
        });

    }


    @Override
    public void setTitle(String text) {
        mTvTitle.setText(text);
    }

    @Override
    public void finishWithIntent(int code, Intent intent) {
        setResult(code, intent);
        finish();
    }

    @Override
    public void setData(boolean showDetermineLang, List<Lang> recentLangs, List<Lang> allLangs, int curLangPos) {
        mShowDetermineLang=showDetermineLang;
        mRecentLangs=recentLangs;
        mAllLangs=allLangs;
        mCurLangPos=curLangPos;
    }


    public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.RecycleViewHolder> {

        final int TYPE_SECTION=0, TYPE_LANG=1;

        @Override
        public int getItemViewType(int position) {

            if (position==0 && mShowDetermineLang) return TYPE_LANG;

            if (mShowDetermineLang) position--;

            if (mRecentLangs.size()==0) return TYPE_LANG;

            if (position==0 || position==mRecentLangs.size()+1) return TYPE_SECTION;

            return TYPE_LANG;

        }

        @Override
        public RecycleViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(i==TYPE_SECTION?R.layout.lang_section:R.layout.lang_item, viewGroup, false);
            return new RecycleViewHolder(v);
        }

        @Override
        public void onBindViewHolder(final RecycleViewHolder viewHolder, int i) {

            if (i==0 && mShowDetermineLang){
                viewHolder.setViewsForLang(null, false);
                return;
            }

            if (mShowDetermineLang) i--;

            if (mRecentLangs.size()!=0) {

                if (i==0) {
                    viewHolder.setViewsForSection(getString(R.string.RecentLangs));
                    return;
                }
                if (i==mRecentLangs.size()+1) {
                    viewHolder.setViewsForSection(getString(R.string.AllLangs));
                    return;
                }
                if (i<mRecentLangs.size()+1) {
                    viewHolder.setViewsForLang(mRecentLangs.get(i-1), false);
                    return;
                }
                i=i-mRecentLangs.size()-2;

            }

            viewHolder.setViewsForLang(mAllLangs.get(i), mCurLangPos==i);

        }

        @Override
        public int getItemCount() {
            return mAllLangs.size()+(mRecentLangs.size()>0?(mRecentLangs.size()+2):0)+(mShowDetermineLang?1:0);
        }


        public class RecycleViewHolder extends RecyclerView.ViewHolder{

            private TextView mTvName;
            private ImageView mIvChecked;
            private RelativeLayout mRlContent;

            public RecycleViewHolder(View itemView) {
                super(itemView);

                mTvName=(TextView)itemView.findViewById(R.id.tvName);
                mIvChecked=(ImageView)itemView.findViewById(R.id.ivChecked);
                mRlContent=(RelativeLayout)itemView.findViewById(R.id.rlContent);

            }

            public void setViewsForSection(String name){
                mTvName.setText(name);
            }

            public void setViewsForLang(final Lang lang, boolean selected) {

                if (lang!=null)
                    mTvName.setText(lang.getName());
                else mTvName.setText(getString(R.string.DetermineLang));

                if (selected){
                    mRlContent.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.colorLightGray));
                    mIvChecked.setVisibility(View.VISIBLE);
                }
                else{
                    mRlContent.setBackgroundColor(ContextCompat.getColor(getActivity(), android.R.color.white));
                    mIvChecked.setVisibility(View.GONE);
                }

                mRlContent.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mPresenter.langClicked(lang);
                    }
                });
            }

        }

    }
}
