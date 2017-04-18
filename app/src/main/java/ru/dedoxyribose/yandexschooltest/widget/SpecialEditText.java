package ru.dedoxyribose.yandexschooltest.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.EditText;

/**
 * Created by Ryan on 09.04.2017.
 */

public class SpecialEditText extends EditText {

    private boolean mIsDoneAction;

    public boolean isDoneAction() {
        return mIsDoneAction;
    }

    public void setDoneAction(boolean doneAction) {
        this.mIsDoneAction = doneAction;
    }

    private OnKeyboardCloseListener mOnKeyboardCloseListener;

    public interface OnKeyboardCloseListener {
        public void OnKeyboardClose();
    }

    public SpecialEditText(Context context) {
        super(context);
    }

    public SpecialEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SpecialEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setOnKeyboardCloseListener(OnKeyboardCloseListener onKeyboardCloseListener) {
        this.mOnKeyboardCloseListener = onKeyboardCloseListener;
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (keyCode==KeyEvent.KEYCODE_BACK && event.getAction()==KeyEvent.ACTION_UP) {
            mOnKeyboardCloseListener.OnKeyboardClose();
            return false;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        InputConnection connection = super.onCreateInputConnection(outAttrs);

        if (mIsDoneAction) {
            int imeActions = outAttrs.imeOptions&EditorInfo.IME_MASK_ACTION;
            if ((imeActions&EditorInfo.IME_ACTION_DONE) != 0) {
                // clear the existing action
                outAttrs.imeOptions ^= imeActions;
                // set the DONE action
                outAttrs.imeOptions |= EditorInfo.IME_ACTION_DONE;
            }
            if ((outAttrs.imeOptions&EditorInfo.IME_FLAG_NO_ENTER_ACTION) != 0) {
                outAttrs.imeOptions &= ~EditorInfo.IME_FLAG_NO_ENTER_ACTION;
            }
        }

        return connection;
    }
}
