package ru.dedoxyribose.yandexschooltest.ui.translate;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.arellomobile.mvp.presenter.InjectPresenter;

import java.util.ArrayList;
import java.util.List;

import me.zhanghai.android.materialprogressbar.MaterialProgressBar;
import ru.dedoxyribose.yandexschooltest.R;
import ru.dedoxyribose.yandexschooltest.model.entity.Def;
import ru.dedoxyribose.yandexschooltest.model.viewmodel.DefTitle;
import ru.dedoxyribose.yandexschooltest.model.viewmodel.ExItem;
import ru.dedoxyribose.yandexschooltest.model.viewmodel.ListItem;
import ru.dedoxyribose.yandexschooltest.ui.standard.StandardFragment;


public class TranslateFragment extends StandardFragment implements TranslateView {


    @InjectPresenter
    TranslatePresenter mPresenter;

    private EditText mEtText;
    private ImageView mIvMic, mIvSpeak, mIvSpeakTrsl, mIvFavorite, mIvShare, mIvBig, mIvClear;
    private TextView mTvMainText;
    private MaterialProgressBar mPbSpeak, mPbSpeakTrsl;
    private RecyclerView mRvList;

    private DefListAdapter mrAdapter;


    private List<ListItem> mDefList = new ArrayList<>();

    public TranslateFragment() {
        // Required empty public constructor
    }


    public static TranslateFragment newInstance() {
        TranslateFragment fragment = new TranslateFragment();

        return fragment;
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_translate, container, false);
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

        mEtText=(EditText)view.findViewById(R.id.editText);
        mIvBig=(ImageView)view.findViewById(R.id.ivBig);
        mIvMic=(ImageView)view.findViewById(R.id.ivMic);
        mIvSpeak=(ImageView)view.findViewById(R.id.ivSpeak);
        mIvSpeakTrsl=(ImageView)view.findViewById(R.id.ivSpeakTrsl);
        mIvFavorite=(ImageView)view.findViewById(R.id.ivFavorite);
        mIvShare=(ImageView)view.findViewById(R.id.ivShare);
        mIvClear=(ImageView)view.findViewById(R.id.ivClear);
        mTvMainText=(TextView)view.findViewById(R.id.tvMainText);
        mPbSpeak=(MaterialProgressBar) view.findViewById(R.id.pbSpeak);
        mPbSpeakTrsl=(MaterialProgressBar)view.findViewById(R.id.pbSpeakTrsl);
        mRvList=(RecyclerView) view.findViewById(R.id.rvDefs);

        mrAdapter=new DefListAdapter();
        mRvList.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRvList.setAdapter(mrAdapter);

    }

    @Override
    public void setDefData(List<ListItem> list) {
        mDefList=list;
        mrAdapter.notifyDataSetChanged();
    }


    public class DefListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        final int TYPE_DEF=0, TYPE_TR=1, TYPE_EX=2;

        @Override
        public int getItemViewType(int position) {

            if (mDefList.get(position) instanceof ExItem) return TYPE_EX;
            if (mDefList.get(position) instanceof DefTitle) return TYPE_DEF;
            return TYPE_TR;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            if (i==TYPE_DEF) {
                View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_def_item, viewGroup, false);
                return new DefTitleViewHolder(v);
            }
            if (i==TYPE_TR) {
                View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_tr_item, viewGroup, false);
                return new TrItemViewHolder(v);
            }
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_ex_item, viewGroup, false);
            return new ExItemViewHolder(v);
        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, int i) {


        }

        @Override
        public int getItemCount() {
            return mDefList.size();
        }


        private class DefTitleViewHolder extends RecyclerView.ViewHolder{

            private TextView mTvWord;
            private TextView mTvInfo;

            public DefTitleViewHolder(View itemView) {
                super(itemView);

                mTvWord=(TextView)itemView.findViewById(R.id.tvWord);
                mTvInfo=(TextView)itemView.findViewById(R.id.tvInfo);
            }
        }

        private class ExItemViewHolder extends RecyclerView.ViewHolder{

            private TextView mTvText;

            public ExItemViewHolder(View itemView) {
                super(itemView);

                mTvText=(TextView)itemView.findViewById(R.id.tvText);
            }
        }

        private class TrItemViewHolder extends RecyclerView.ViewHolder{

            private TextView mTvNum;
            private TextView mTvWords;
            private TextView mTvMeans;

            public TrItemViewHolder(View itemView) {
                super(itemView);

                mTvNum=(TextView)itemView.findViewById(R.id.tvNum);
                mTvWords=(TextView)itemView.findViewById(R.id.tvWords);
                mTvMeans=(TextView)itemView.findViewById(R.id.tvMeans);
            }
        }

    }


}

