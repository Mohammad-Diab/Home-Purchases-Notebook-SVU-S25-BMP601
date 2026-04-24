package com.example.homepurchases.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputConnectionWrapper;

import androidx.appcompat.widget.AppCompatEditText;

public class BackspaceEditText extends AppCompatEditText {

    public interface OnBackspaceEmptyListener {
        void onBackspaceWhenEmpty();
    }

    private OnBackspaceEmptyListener backspaceListener;

    public BackspaceEditText(Context context) { super(context); }
    public BackspaceEditText(Context context, AttributeSet attrs) { super(context, attrs); }
    public BackspaceEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setOnBackspaceEmptyListener(OnBackspaceEmptyListener listener) {
        this.backspaceListener = listener;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (backspaceListener != null
                && event.getAction() == KeyEvent.ACTION_DOWN
                && event.getKeyCode() == KeyEvent.KEYCODE_DEL
                && length() == 0) {
            backspaceListener.onBackspaceWhenEmpty();
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        return new InputConnectionWrapper(super.onCreateInputConnection(outAttrs), true) {

            // When the field is empty any deleteSurroundingText call means backspace
            @Override
            public boolean deleteSurroundingText(int beforeLength, int afterLength) {
                if (backspaceListener != null && BackspaceEditText.this.length() == 0) {
                    backspaceListener.onBackspaceWhenEmpty();
                    return true;
                }
                return super.deleteSurroundingText(beforeLength, afterLength);
            }

            @Override
            public boolean sendKeyEvent(KeyEvent event) {
                if (backspaceListener != null
                        && event.getAction() == KeyEvent.ACTION_DOWN
                        && event.getKeyCode() == KeyEvent.KEYCODE_DEL
                        && BackspaceEditText.this.length() == 0) {
                    backspaceListener.onBackspaceWhenEmpty();
                    return true;
                }
                return super.sendKeyEvent(event);
            }
        };
    }
}
