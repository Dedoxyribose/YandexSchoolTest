package ru.dedoxyribose.yandexschooltest.ui.recordlist;

import android.app.Activity;
import android.content.DialogInterface;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;

import android.view.View;
import android.view.ViewGroup;

import android.widget.EditText;
import android.widget.ImageView;

import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.arellomobile.mvp.presenter.InjectPresenter;

import java.util.ArrayList;
import java.util.List;

import me.zhanghai.android.materialprogressbar.MaterialProgressBar;
import ru.dedoxyribose.yandexschooltest.R;
import ru.dedoxyribose.yandexschooltest.model.entity.Record;

import ru.dedoxyribose.yandexschooltest.ui.histories.HistoriesFragment;
import ru.dedoxyribose.yandexschooltest.ui.standard.StandardFragment;



public class RecordListFragment extends StandardFragment implements RecordListView {

    public static final String ARG_TYPE="ARG_TYPE";

    public static final int TYPE_HISTORY = 0;
    public static final int TYPE_FAVORITE = 1;

    private int mType;

    private EditText mEtSearch;
    private TextView mTvEmpty;
    private ImageView mIvSearchClear;
    private MaterialProgressBar mPbLoad;
    private RecyclerView mRvList;
    private LinearLayoutManager mLayoutManager;

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
        mTvEmpty = (TextView) view.findViewById(R.id.tvEmpty);

        mrAdapter=new RecyclerAdapter();
        mRvList.setAdapter(mrAdapter);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRvList.setLayoutManager(mLayoutManager);
        mRvList.getItemAnimator().setChangeDuration(0);


        mEtSearch.post(new Runnable() {
            @Override
            public void run() {
                updateClearButtonState();
            }
        });

        mEtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                mPresenter.searchTextChanged(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        mIvSearchClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPresenter.clearSearchClicked();
            }
        });

        mRvList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (mLayoutManager.findLastCompletelyVisibleItemPosition()>=mrAdapter.getItemCount()-RecordListPresenter.MAX_PER_PAGE/3)
                {
                    mPresenter.onEndReached();
                }

            }
        });



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
        mrAdapter.notifyItemChanged(mRecordList.size()-1-i);
    }

    @Override
    public void notifyItemRemoved(int i) {
        mrAdapter.notifyItemRemoved(mRecordList.size()-1-i);
    }

    @Override
    public void notifyItemInserted(int i) {
        mrAdapter.notifyItemInserted(mRecordList.size()-1-i);
    }

    @Override
    public void showLoading(boolean show) {
        mPbLoad.setVisibility(show?View.VISIBLE:View.GONE);
        mRvList.setVisibility((!show)?View.VISIBLE:View.GONE);
    }

    @Override
    public void showOptionsDialog(final int num) {
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        alert.setItems(new String[]{getString(R.string.Delete)}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mPresenter.removeClicked(num);
            }
        });
        alert.create().show();
    }

    @Override
    public void showAlertDelete() {
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        alert.setMessage(getString(mType==TYPE_FAVORITE?R.string.WannaClearFavorite:R.string.WannaClearHistory));
        alert.setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        alert.setPositiveButton(R.string.Clear, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mPresenter.clearPositive();
            }
        });
        alert.create().show();
    }

    @Override
    public void showEmpty(boolean show) {
        mTvEmpty.setVisibility(show?View.VISIBLE:View.GONE);
    }

    @Override
    public void scrollToPosition(int i) {
        mRvList.scrollToPosition(i);
    }

    @Override
    public void updateClearButtonState() {
        ((HistoriesFragment)getParentFragment()).updateClearButtonState();
    }

    @Override
    public void showSearchClearButton(boolean show) {
        mIvSearchClear.setVisibility(show?View.VISIBLE:View.GONE);
    }

    @Override
    public void clearSearchText() {
        mEtSearch.setText("");
    }

    public void update() {
        mPresenter.update();
    }

    public void clearClicked() {
        mPresenter.clearClicked();
    }

    public boolean getClearButtonState() {
        return mPresenter.getClearButtonState();
    }

    public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.RecycleViewHolder> {


        @Override
        public RecycleViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.record_item, viewGroup, false);
            return new RecycleViewHolder(v);
        }

        @Override
        public void onBindViewHolder(final RecycleViewHolder viewHolder, int i) {

            viewHolder.setViews(mRecordList.get(mRecordList.size()-i-1));

        }

        @Override
        public int getItemCount() {
            return mRecordList.size();
        }



        public class RecycleViewHolder extends RecyclerView.ViewHolder{

            private RelativeLayout mRlContent;
            private TextView mTvText, mTvTranslation, mTvDirection;
            private ImageView mIvIcon;

            public RecycleViewHolder(View itemView) {
                super(itemView);

                mTvText=(TextView)itemView.findViewById(R.id.tvText);
                mTvTranslation=(TextView)itemView.findViewById(R.id.tvTranslation);
                mTvDirection=(TextView)itemView.findViewById(R.id.tvDir);
                mIvIcon=(ImageView) itemView.findViewById(R.id.ivIcon);
                mRlContent=(RelativeLayout) itemView.findViewById(R.id.rlContent);

            }

            public void setViews(final Record record) {

                String text=record.getText();
                if (text.length()>100) text=text.substring(0,100)+"...";
                mTvText.setText(text);

                text=record.getTranslation();
                if (text.length()>100) text=text.substring(0,100)+"...";
                mTvTranslation.setText(text);

                mTvDirection.setText(record.getDirection());

                mIvIcon.setColorFilter(ContextCompat.getColor(getActivity(),
                        record.isInFavorite()?R.color.colorAccent:R.color.colorGrayPic));

                mIvIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mPresenter.iconClicked(mRecordList.indexOf(record));
                    }
                });

                mRlContent.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mPresenter.singleClick(mRecordList.indexOf(record));
                    }
                });

                mRlContent.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        mPresenter.longClick(mRecordList.indexOf(record));
                        return true;
                    }
                });
            }


        }

    }

}

