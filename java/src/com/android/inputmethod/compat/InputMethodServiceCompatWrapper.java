/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.inputmethod.compat;

import com.android.inputmethod.deprecated.LanguageSwitcherProxy;
import com.android.inputmethod.latin.SubtypeSwitcher;

import android.inputmethodservice.InputMethodService;
import android.view.inputmethod.InputMethodSubtype;

public class InputMethodServiceCompatWrapper extends InputMethodService {
    // CAN_HANDLE_ON_CURRENT_INPUT_METHOD_SUBTYPE_CHANGED needs to be false if the API level is 10
    // or previous. Note that InputMethodSubtype was added in the API level 11.
    // For the API level 11 or later, LatinIME should override onCurrentInputMethodSubtypeChanged().
    // For the API level 10 or previous, we handle the "subtype changed" events by ourselves
    // without having support from framework -- onCurrentInputMethodSubtypeChanged().
    public static final boolean CAN_HANDLE_ON_CURRENT_INPUT_METHOD_SUBTYPE_CHANGED = true;

    private InputMethodManagerCompatWrapper mImm;

    @Override
    public void onCreate() {
        super.onCreate();
        mImm = InputMethodManagerCompatWrapper.getInstance(this);
    }

    // When the API level is 10 or previous, notifyOnCurrentInputMethodSubtypeChanged should
    // handle the event the current subtype was changed. LatinIME calls
    // notifyOnCurrentInputMethodSubtypeChanged every time LatinIME
    // changes the current subtype.
    // This call is required to let LatinIME itself know a subtype changed
    // event when the API level is 10 or previous.
    @SuppressWarnings("unused")
    public void notifyOnCurrentInputMethodSubtypeChanged(InputMethodSubtypeCompatWrapper subtype) {
        // Do nothing when the API level is 11 or later
        // and FORCE_ENABLE_VOICE_EVEN_WITH_NO_VOICE_SUBTYPES is not true
        if (CAN_HANDLE_ON_CURRENT_INPUT_METHOD_SUBTYPE_CHANGED && !InputMethodManagerCompatWrapper.
                FORCE_ENABLE_VOICE_EVEN_WITH_NO_VOICE_SUBTYPES) {
            return;
        }
        if (subtype == null) {
            subtype = mImm.getCurrentInputMethodSubtype();
        }
        if (subtype != null) {
            if (!InputMethodManagerCompatWrapper.FORCE_ENABLE_VOICE_EVEN_WITH_NO_VOICE_SUBTYPES
                    && !subtype.isDummy()) return;
            if (!InputMethodManagerCompatWrapper.SUBTYPE_SUPPORTED) {
                LanguageSwitcherProxy.getInstance().setLocale(subtype.getLocale());
            }
            SubtypeSwitcher.getInstance().updateSubtype(subtype);
        }
    }

    //////////////////////////////////////
    // Functions using API v11 or later //
    //////////////////////////////////////
    @Override
    public void onCurrentInputMethodSubtypeChanged(InputMethodSubtype subtype) {
        // Do nothing when the API level is 10 or previous
        if (!CAN_HANDLE_ON_CURRENT_INPUT_METHOD_SUBTYPE_CHANGED) return;
        SubtypeSwitcher.getInstance().updateSubtype(
                new InputMethodSubtypeCompatWrapper(subtype));
    }

    protected static void setTouchableRegionCompat(InputMethodService.Insets outInsets,
            int x, int y, int width, int height) {
        outInsets.touchableInsets = InputMethodService.Insets.TOUCHABLE_INSETS_REGION;
        outInsets.touchableRegion.set(x, y, width, height);
    }
}