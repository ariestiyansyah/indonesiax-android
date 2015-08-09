package org.edx.indonesiax.module.registration.view;

import android.text.InputType;
import android.view.View;

import org.edx.indonesiax.R;
import org.edx.indonesiax.module.registration.model.RegistrationFormField;
import org.edx.indonesiax.util.InputValidationUtil;

/**
 * Created by rohan on 2/11/15.
 */
class RegistrationEmailView extends RegistrationEditTextView {

    public RegistrationEmailView(RegistrationFormField field, View view) {
        super(field, view);
        mInputView.setInputType(InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
    }

    @Override
    public boolean isValidInput() {
        boolean isValidInput = super.isValidInput();
        if(isValidInput){
            if(!InputValidationUtil.isValidEmail(getCurrentValue().getAsString())){
                handleError(getView().getResources().getString(R.string.error_invalid_email));
                isValidInput = false;
            }
        }
        return isValidInput;
    }
}
