/* Copyright (C) 2015 haha01haha01

* This Source Code Form is subject to the terms of the Mozilla Public
* License, v. 2.0. If a copy of the MPL was not distributed with this
* file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.haha01haha01.harail;

import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Rect;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.EditText;

public class MirageEditText extends EditText {
	private CharSequence real_text;
	private CharSequence mirage_text;
	private ColorStateList real_color;
	
    boolean is_switching_mirage = false;
    boolean is_mirage = true;
    
    private Lock text_mutex = new ReentrantLock();
	
	public MirageEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.MirageEditText,
                0, 0);

       try {
           mirage_text = a.getString(R.styleable.MirageEditText_mirageText);
       } finally {
           a.recycle();
       }
       real_text = getText();
       real_color = getTextColors();
       switchTexts(isFocused());
    }
	
	ArrayList<TextWatcher> mirage_listeners = null;
	@Override
    public void addTextChangedListener(TextWatcher watcher) {
        if (mirage_listeners == null) {
        	mirage_listeners = new ArrayList<TextWatcher>();
        }

        mirage_listeners.add(watcher);
    }
	
    public void removeTextChangedListener(TextWatcher watcher) {
        if (mirage_listeners != null) {
            int i = mirage_listeners.indexOf(watcher);

            if (i >= 0) {
            	mirage_listeners.remove(i);
            }
        }
    }
	
    private void callTextListeners(CharSequence text, int start, int lengthBefore, int lengthAfter)
    {
		if (mirage_listeners != null) {
            final ArrayList<TextWatcher> list = mirage_listeners;
            final int count = list.size();
            for (int i = 0; i < count; i++) {
            	list.get(i).onTextChanged(text, start, lengthBefore, lengthAfter);
            }
        }
    }
   
	@Override
	protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
		if (!is_switching_mirage && !is_mirage) {
			callTextListeners(text, start, lengthBefore, lengthAfter);
		}
	}
	
	private void switchTexts(boolean focused)
	{
		text_mutex.lock();
		is_switching_mirage = true;
		if (focused) {
			setText(real_text);
			setTextColor(real_color);
			is_mirage = false;
		} else {
			real_color = getTextColors();
			real_text = getText();
			setText(mirage_text);
			setTextColor(Color.GRAY);
			is_mirage = true;
		}
		is_switching_mirage = false;
		text_mutex.unlock();
	}
	
	@Override
	protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
		switchTexts(focused);
		super.onFocusChanged(focused, direction, previouslyFocusedRect);
	}
	
	public CharSequence getRealText()
	{
		return real_text;
	}
	
	public CharSequence getMirageText()
	{
		return mirage_text;
	}
	
	public void setRealText(CharSequence new_text)
	{
		text_mutex.lock();
		if (is_mirage) {
			real_text = new_text;
		} else {
			setText(new_text);
		}
		text_mutex.unlock();
		
		callTextListeners(new_text, 0, 0, 0);
	}
	
	public void setMirageText(CharSequence new_text)
	{
		text_mutex.lock();
		mirage_text = new_text;
		if (is_mirage) {
			setText(new_text);
		}
		text_mutex.unlock();
	}

}
