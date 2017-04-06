package ru.dedoxyribose.yandexschooltest.ui.translate;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
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
import ru.dedoxyribose.yandexschooltest.model.entity.Word;
import ru.dedoxyribose.yandexschooltest.model.viewmodel.DefTitle;
import ru.dedoxyribose.yandexschooltest.model.viewmodel.ExItem;
import ru.dedoxyribose.yandexschooltest.model.viewmodel.ListItem;
import ru.dedoxyribose.yandexschooltest.model.viewmodel.TrItem;
import ru.dedoxyribose.yandexschooltest.ui.fail.FailActivity;
import ru.dedoxyribose.yandexschooltest.ui.main.MainActivity;
import ru.dedoxyribose.yandexschooltest.ui.standard.StandardFragment;
import ru.dedoxyribose.yandexschooltest.util.Utils;
import ru.dedoxyribose.yandexschooltest.widget.EditTextMultilineDone;
import ru.dedoxyribose.yandexschooltest.widget.TintableImageView;
import ru.yandex.speechkit.Recognizer;


public class TranslateFragment extends StandardFragment implements TranslateView {

    public static final int REQ_CODE_GET_LANG=1;
    public static final int REQUEST_CODE_RECOGNIZE = 2;

    @InjectPresenter
    TranslatePresenter mPresenter;

    private EditTextMultilineDone mEtText;
    private TintableImageView mIvMic, mIvSpeak, mIvSpeakTrsl, mIvShare, mIvBig;
    private ImageView mIvClear, mIvExchange, mIvFavorite;
    private TextView mTvMainText, mTvErrorTitle, mTvErrorText, mTvFrom, mTvTo, mTvDetAut;
    private Button mbRepeat;
    private MaterialProgressBar mPbSpeak, mPbSpeakTrsl;
    private RecyclerView mRvList;
    private RelativeLayout mRlLoading, mRlError, mRlContainer;
    private LinearLayout mLlFrom;

    private DefListAdapter mrAdapter;


    private List<ListItem> mDefList = new ArrayList<>();

    private boolean mIsFavorite=false;
    private boolean mAreButtonsEnabled=false;

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

        mEtText=(EditTextMultilineDone)view.findViewById(R.id.editText);
        mIvBig=(TintableImageView) view.findViewById(R.id.ivBig);
        mIvMic=(TintableImageView)view.findViewById(R.id.ivMic);
        mIvSpeak=(TintableImageView)view.findViewById(R.id.ivSpeak);
        mIvSpeakTrsl=(TintableImageView)view.findViewById(R.id.ivSpeakTrsl);
        mIvFavorite=(ImageView) view.findViewById(R.id.ivFavorite);
        mIvShare=(TintableImageView)view.findViewById(R.id.ivShare);
        mIvClear=(ImageView)view.findViewById(R.id.ivClear);
        mIvExchange=(ImageView)view.findViewById(R.id.ivExchange);
        mTvMainText=(TextView)view.findViewById(R.id.tvMainText);
        mTvErrorTitle=(TextView)view.findViewById(R.id.tvErrorTitle);
        mTvErrorText=(TextView)view.findViewById(R.id.tvErrorText);
        mTvFrom=(TextView)view.findViewById(R.id.tvFrom);
        mTvTo=(TextView)view.findViewById(R.id.tvTo);
        mTvDetAut=(TextView)view.findViewById(R.id.tvDetAut);
        mbRepeat=(Button) view.findViewById(R.id.bRepeat);
        mPbSpeak=(MaterialProgressBar) view.findViewById(R.id.pbSpeak);
        mPbSpeakTrsl=(MaterialProgressBar)view.findViewById(R.id.pbSpeakTrsl);
        mRvList=(RecyclerView) view.findViewById(R.id.rvDefs);
        mRlLoading=(RelativeLayout) view.findViewById(R.id.rlLoading);
        mRlError=(RelativeLayout) view.findViewById(R.id.rlError);
        mRlContainer=(RelativeLayout) view.findViewById(R.id.rlContainer);
        mLlFrom=(LinearLayout) view.findViewById(R.id.llFrom);

        mrAdapter=new DefListAdapter();
        mRvList.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRvList.setAdapter(mrAdapter);


        mIvClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPresenter.clearClicked();
            }
        });

        mEtText.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    mPresenter.returnPressed();
                    return true;
                }
                return false;
            }
        });

        mEtText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                mPresenter.textChanged(charSequence);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        mbRepeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPresenter.repeatClicked();
            }
        });

        mLlFrom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPresenter.fromClicked();
            }
        });

        mTvTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPresenter.toClicked();
            }
        });

        mIvExchange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPresenter.exchangeClicked();
            }
        });

        mRlContainer.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                mPresenter.outsideTouch();
                return false;
            }
        });

        mRvList.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                mPresenter.outsideTouch();
                return false;
            }
        });

        /*mRvList.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                mPresenter.outsideTouch();
                return false;
            }
        });

        mEtText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (!b) mPresenter.textLostFocus();
            }
        });*/

        mEtText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (!b) mPresenter.textLostFocus();
            }
        });

        mIvMic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPresenter.micClicked();
            }
        });

        mIvSpeak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPresenter.speakClicked();
            }
        });

        mIvSpeakTrsl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPresenter.speakTrslClicked();
            }
        });

        mIvBig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPresenter.bigClicked();
            }
        });

        mTvMainText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPresenter.mainTextClicked();
            }
        });

        mIvShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPresenter.shareClicked();
            }
        });

        mIvFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPresenter.favoriteClicked();
            }
        });

    }

    @Override
    public void hideSoftKeyboard() {
        InputMethodManager inputMethodManager =
                (InputMethodManager) getActivity().getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(
                getActivity().getCurrentFocus().getWindowToken(), 0);
    }

    @Override
    public void clearTextFocus() {
        mEtText.clearFocus();
    }

    @Override
    public void setRecognitionEnabled(boolean enabled) {
        mIvMic.setImageResource(enabled?R.drawable.ic_mic_black_24dp:R.drawable.ic_mic_off_black_24dp);
        mIvMic.setEnabled(enabled);
    }

    @Override
    public void startRecognition(Intent intent) {
        startActivityForResult(intent, REQUEST_CODE_RECOGNIZE);
    }

    @Override
    public void setTextSpeechStatus(boolean visible, boolean enabled, boolean loading) {
        mIvSpeak.setImageResource(enabled?R.drawable.ic_volume_up_black_24dp:R.drawable.ic_volume_off_black_24dp);;
        mIvSpeak.setEnabled(enabled);

        mIvSpeak.setVisibility((loading || !visible)?View.GONE:View.VISIBLE);
        mPbSpeak.setVisibility(loading?View.VISIBLE:View.GONE);
    }

    @Override
    public void setTranslateSpeechStatus(boolean enabled, boolean loading) {
        mIvSpeakTrsl.setImageResource(enabled?R.drawable.ic_volume_up_black_24dp:R.drawable.ic_volume_off_black_24dp);
        mIvSpeakTrsl.setEnabled(enabled);

        mIvSpeakTrsl.setVisibility(loading?View.GONE:View.VISIBLE);
        mPbSpeakTrsl.setVisibility(loading?View.VISIBLE:View.GONE);
    }

    @Override
    public void startFullscreen(Intent intent) {
        startActivity(intent);
    }

    @Override
    public void showToast(String text) {
        Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void share(String text) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, text);
        sendIntent.setType("text/plain");
        startActivity(sendIntent);
    }

    @Override
    public void setTranslationButtonsEnabled(boolean enabled) {

        Log.d(TAG, "setTranslationButtonsEnabled, enabled="+enabled);

        mAreButtonsEnabled=enabled;

        mIvFavorite.setEnabled(enabled);
        mIvShare.setEnabled(enabled);
        mIvBig.setEnabled(enabled);

        if (!mAreButtonsEnabled) {
            mIvFavorite.setColorFilter(ContextCompat.getColor(getActivity(), R.color.colorLightGray));
            mIvFavorite.setContentDescription("off");
        }
        else  {
            mIvFavorite.setColorFilter(ContextCompat.getColor(getActivity(), mIsFavorite?R.color.colorAccent:R.color.colorGrayPic));
            mIvFavorite.setContentDescription(mIsFavorite?"on":"off");
        }
    }

    @Override
    public void setFavoriteOn(boolean on) {
        Log.d(TAG, "setFavoriteOn, on="+on);

        mIsFavorite=on;

        if (mAreButtonsEnabled) {
            mIvFavorite.setColorFilter(ContextCompat.getColor(getActivity(), on ? R.color.colorAccent : R.color.colorGrayPic));
            mIvFavorite.setContentDescription(mIsFavorite?"on":"off");
        }
        else {
            mIvFavorite.setColorFilter(R.color.colorLightGray);
            mIvFavorite.setContentDescription("off");
        }
    }

    @Override
    public void toFailActivity() {
        Intent intent=new Intent(getActivity(), FailActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        getActivity().finish();
    }

    @Override
    public void incrementIdling() {
        ((MainActivity)getActivity()).incrementIdlingResource();
    }

    @Override
    public void decrementIdling() {
        ((MainActivity)getActivity()).decrementIdlingResource();
    }

    @Override
    public void setDefData(List<ListItem> list) {
        mDefList=list;
        mrAdapter.notifyDataSetChanged();
        Log.d(TAG, "list.size="+list.size());
    }

    @Override
    public void setText(String text) {
        mEtText.setText(text);
        if (text.length()>0) mEtText.setSelection(text.length());
    }

    @Override
    public void showLoading(boolean show) {
        mRlLoading.setVisibility(show?View.VISIBLE:View.GONE);
    }

    @Override
    public void setMainText(String text) {
        mTvMainText.setText(text);
    }

    @Override
    public void showError(boolean showError, String title, String text, boolean showRepeat) {
        mRlError.setVisibility(showError?View.VISIBLE:View.GONE);
        mbRepeat.setVisibility(showRepeat?View.VISIBLE:View.GONE);
        mTvErrorTitle.setText(title);
        mTvErrorText.setText(text);
    }

    @Override
    public void openChooseLang(Intent intent) {
        startActivityForResult(intent, REQ_CODE_GET_LANG);
    }

    @Override
    public void showLangs(String from, String to, boolean determined) {
        mTvFrom.setText(from);
        mTvTo.setText(to);

        mTvDetAut.setVisibility(determined?View.VISIBLE:View.GONE);
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


            if (getItemViewType(i)==TYPE_DEF)
                ((DefTitleViewHolder)viewHolder).setViews((DefTitle)mDefList.get(i));
            else if (getItemViewType(i)==TYPE_TR)
                ((TrItemViewHolder)viewHolder).setViews((TrItem) mDefList.get(i));
            else ((ExItemViewHolder)viewHolder).setViews((ExItem) mDefList.get(i));

        }

        @Override
        public int getItemCount() {
            return mDefList.size();
        }


        private class DefTitleViewHolder extends RecyclerView.ViewHolder{

            private TextView mTvWord;
            private TextView mTvInfo;

            private String mColorBlack;
            private String mColorGray;
            private String mColorPink;

            public DefTitleViewHolder(View itemView) {
                super(itemView);

                mColorBlack="#"+Integer.toHexString(ContextCompat.getColor(getActivity(), R.color.colorBlackText)).substring(2);
                mColorGray="#"+Integer.toHexString(ContextCompat.getColor(getActivity(), R.color.colorGrayText)).substring(2);
                mColorPink="#"+Integer.toHexString(ContextCompat.getColor(getActivity(), R.color.colorLightPink)).substring(2);

                mTvWord=(TextView)itemView.findViewById(R.id.tvWord);
                mTvInfo=(TextView)itemView.findViewById(R.id.tvInfo);
            }

            public void setViews(DefTitle defTitle) {
                mTvInfo.setText(defTitle.getWord().getPos());

                String mainStr = Utils.getColoredSpanned(defTitle.getWord().getText(), mColorBlack);
                if (!Utils.isEmpty(defTitle.getWord().getTs())) {
                    mainStr += Utils.getColoredSpanned(" ["+defTitle.getWord().getTs()+"]", mColorGray);
                }
                if (!Utils.isEmpty(defTitle.getWord().getGen())) {
                    mainStr += Utils.getColoredSpanned(" "+defTitle.getWord().getGen(), mColorGray);
                }

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    mTvWord.setText(Html.fromHtml(mainStr, Html.FROM_HTML_MODE_LEGACY), TextView.BufferType.SPANNABLE);
                } else {
                    mTvWord.setText(Html.fromHtml(mainStr), TextView.BufferType.SPANNABLE);
                }
            }
        }

        private class ExItemViewHolder extends RecyclerView.ViewHolder{

            private TextView mTvText;

            public ExItemViewHolder(View itemView) {
                super(itemView);

                mTvText=(TextView)itemView.findViewById(R.id.tvText);
            }

            public void setViews(ExItem exItem) {
                mTvText.setText(exItem.getText());
            }
        }

        private class TrItemViewHolder extends RecyclerView.ViewHolder{

            private TextView mTvNum;
            private TextView mTvWords;
            private TextView mTvMeans;

            private String mColorBlack;
            private String mColorGray;

            public TrItemViewHolder(View itemView) {
                super(itemView);

                mColorBlack="#"+Integer.toHexString(ContextCompat.getColor(getActivity(), R.color.colorBlackText)).substring(2);
                mColorGray="#"+Integer.toHexString(ContextCompat.getColor(getActivity(), R.color.colorGrayText)).substring(2);

                mTvNum=(TextView)itemView.findViewById(R.id.tvNum);
                mTvWords=(TextView)itemView.findViewById(R.id.tvWords);
                mTvMeans=(TextView)itemView.findViewById(R.id.tvMeans);
            }

            public void setViews(TrItem trItem) {
                mTvNum.setText(trItem.getNum());

                SpannableStringBuilder spannableStringBuilder=new SpannableStringBuilder();
                for (Word word:trItem.getWords()){

                    if (spannableStringBuilder.length()>0) spannableStringBuilder.append(", ");

                    int oldLen=spannableStringBuilder.length();
                    spannableStringBuilder.append(word.getText());
                    final String finalWord = word.getText();
                    spannableStringBuilder.setSpan(new ClickableSpan() {
                        @Override
                        public void onClick(View view) {
                            Log.d(TAG, "spanClicked(), word="+ finalWord);
                            mPresenter.synonymClicked(finalWord);
                        }

                        @Override
                        public void updateDrawState(TextPaint ds) {
                            ds.setUnderlineText(false);
                            ds.setColor(ContextCompat.getColor(getActivity(), R.color.colorLightBlue));
                        }
                    }, oldLen, spannableStringBuilder.length(), Spannable.SPAN_INTERMEDIATE);

                    String wordsStr="";

                    if (word.getGen()!=null) wordsStr += Utils.getColoredSpanned(" "+word.getGen(), mColorGray);
                    if (word.getNum()!=null) wordsStr += Utils.getColoredSpanned(" "+word.getNum(), mColorGray);

                    Spanned spanned;

                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                        spanned=Html.fromHtml(wordsStr, Html.FROM_HTML_MODE_LEGACY);
                    } else {
                        spanned=Html.fromHtml(wordsStr);
                    }

                    if (spanned.length()>0) spannableStringBuilder.append(" ");
                    spannableStringBuilder.append(spanned);


                }

                mTvWords.setText(spannableStringBuilder, TextView.BufferType.SPANNABLE);
                mTvWords.setMovementMethod(LinkMovementMethod.getInstance());


                String meansStr="(";
                for (Word word:trItem.getMeans()){
                    if (meansStr.length()>1) meansStr+=", ";
                    meansStr+=word.getText();
                }
                meansStr+=")";

                mTvMeans.setText(meansStr);

                if (trItem.getMeans().size()==0) mTvMeans.setVisibility(View.GONE); else mTvMeans.setVisibility(View.VISIBLE);
            }
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG, "onActivityResult, requestCode="+requestCode+" resultCode="+resultCode);
        mPresenter.activityResult(requestCode, resultCode, data);
    }
}

