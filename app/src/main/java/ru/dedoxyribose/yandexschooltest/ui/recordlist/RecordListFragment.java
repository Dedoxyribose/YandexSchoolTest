package ru.dedoxyribose.yandexschooltest.ui.recordlist;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.arellomobile.mvp.presenter.InjectPresenter;

import java.util.ArrayList;
import java.util.List;

import me.zhanghai.android.materialprogressbar.MaterialProgressBar;
import ru.dedoxyribose.yandexschooltest.R;
import ru.dedoxyribose.yandexschooltest.model.entity.Record;
import ru.dedoxyribose.yandexschooltest.model.entity.Word;
import ru.dedoxyribose.yandexschooltest.model.viewmodel.DefTitle;
import ru.dedoxyribose.yandexschooltest.model.viewmodel.ExItem;
import ru.dedoxyribose.yandexschooltest.model.viewmodel.ListItem;
import ru.dedoxyribose.yandexschooltest.model.viewmodel.TrItem;
import ru.dedoxyribose.yandexschooltest.ui.standard.StandardFragment;
import ru.dedoxyribose.yandexschooltest.ui.translate.TranslatePresenter;
import ru.dedoxyribose.yandexschooltest.ui.translate.TranslateView;
import ru.dedoxyribose.yandexschooltest.util.Utils;
import ru.dedoxyribose.yandexschooltest.widget.EditTextMultilineDone;
import ru.dedoxyribose.yandexschooltest.widget.TintableImageView;


public class RecordListFragment extends StandardFragment implements RecordListView {

    public static final String ARG_TYPE="ARG_TYPE";

    public static final int TYPE_HISTORY = 0;
    public static final int TYPE_FAVORITE = 1;

    private int mType;

    private EditText mEtSearch;
    private ImageView mIvSearchClear;
    private MaterialProgressBar mPbLoad;
    private RecyclerView mRvList;

    private RecyclerAdapter mrAdapter;

    private List<Record> mRecordList = new ArrayList<>();

    @InjectPresenter
    RecordListPresenter mPresenter;

    public RecordListFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mType=getArguments().getInt(ARG_TYPE);
            mPresenter.setType(mType);
        }
    }

    public static RecordListFragment newInstance(int type) {
        RecordListFragment fragment = new RecordListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_TYPE, type);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_record_list, container, false);
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        /*if (activity instanceof StartActivityFragmentInteractionListener) {
            mListener = (StartActivityFragmentInteractionListener) activity;
        } else {
            throw new RuntimeException(activity.toString()
                    + " must implement StartActivityFragmentInteractionListener");
        }*/
    }

    @Override
    public void onDetach() {
        super.onDetach();
        //mListener = null;
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        mEtSearch = (EditText) view.findViewById(R.id.etSearch);
        mIvSearchClear = (ImageView) view.findViewById(R.id.ivSearchClear);
        mPbLoad = (MaterialProgressBar) view.findViewById(R.id.pbLoad);
        mRvList = (RecyclerView) view.findViewById(R.id.rvList);

        mrAdapter=new RecyclerAdapter();
        mRvList.setAdapter(mrAdapter);
        mRvList.setLayoutManager(new LinearLayoutManager(getActivity()));

    }

    @Override
    public void setData(List<Record> list) {
        mRecordList=list;
    }

    @Override
    public void notifyDataSetChanged() {
        mrAdapter.notifyDataSetChanged();
    }

    @Override
    public void notifyItemChanged(int i) {
        mrAdapter.notifyItemChanged(i);
    }

    @Override
    public void notifyItemRemoved(int i) {
        mrAdapter.notifyItemRemoved(i);
    }

    @Override
    public void notifyItemInserted(int i) {
        mrAdapter.notifyItemInserted(i);
    }

    @Override
    public void showLoading(boolean show) {
        mPbLoad.setVisibility(show?View.VISIBLE:View.GONE);
        mRvList.setVisibility((!show)?View.VISIBLE:View.GONE);
    }

    public void update() {
        mPresenter.update();
    }

    public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.RecycleViewHolder> {


        @Override
        public RecycleViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.record_item, viewGroup, false);
            return new RecycleViewHolder(v);
        }

        @Override
        public void onBindViewHolder(final RecycleViewHolder viewHolder, int i) {

            viewHolder.setViews(mRecordList.get(i), i);

        }

        @Override
        public int getItemCount() {
            return mRecordList.size();
        }



        public class RecycleViewHolder extends RecyclerView.ViewHolder{

            private TextView mTvText, mTvTranslation, mTvDirection;
            private ImageView mIvIcon;

            public RecycleViewHolder(View itemView) {
                super(itemView);

                mTvText=(TextView)itemView.findViewById(R.id.tvText);
                mTvTranslation=(TextView)itemView.findViewById(R.id.tvTranslation);
                mTvDirection=(TextView)itemView.findViewById(R.id.tvDir);
                mIvIcon=(ImageView) itemView.findViewById(R.id.ivIcon);

            }

            public void setViews(final Record record, final int i) {
                mTvText.setText(record.getText());
                mTvTranslation.setText(record.getTranslation());
                mTvDirection.setText(record.getDirection());

                mIvIcon.setColorFilter(ContextCompat.getColor(getActivity(),
                        record.isInFavorite()?R.color.colorAccent:R.color.colorGrayPic));

                mIvIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mPresenter.iconClicked(i);
                    }
                });
            }


        }

    }

}

