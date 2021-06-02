package com.mohamed_amgd.ayzeh.ViewModels;

import android.app.Application;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.mohamed_amgd.ayzeh.R;
import com.mohamed_amgd.ayzeh.Views.Fragments.SignUpFragment;
import com.mohamed_amgd.ayzeh.Views.Fragments.UserInfoFragment;
import com.mohamed_amgd.ayzeh.repo.Repository;
import com.mohamed_amgd.ayzeh.repo.RepositoryResult;
import com.mohamed_amgd.ayzeh.repo.Util;

public class SignInViewModel extends AndroidViewModel {

    FragmentManager mFragmentManager;

    public SignInViewModel(@NonNull Application application, FragmentManager fragmentManager) {
        super(application);
        mFragmentManager = fragmentManager;
    }

    public void signUpAction() {
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_layout, new SignUpFragment());
        transaction.commit();
    }

    public void signInAction(EditText emailEditText, EditText passwordEditText) {
        boolean inputError = false;
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        if (!Util.getInstance().isEmailValid(email)) {
            inputError = true;
            emailEditText.setError(getApplication().getString(R.string.email_input_error));
        }
        if (!Util.getInstance().isPasswordValid(password)) {
            inputError = true;
            passwordEditText.setError(getApplication().getString(R.string.password_input_error));
        }
        if (inputError) return;

        RepositoryResult<Boolean> result =
                Repository.getInstance().signInUser(email, password);
        result.getIsLoadingLiveData().observeForever(isLoading -> {
            if (result.isFinishedSuccessfully()) {
                FragmentTransaction transaction = mFragmentManager.beginTransaction();
                transaction.replace(R.id.fragment_layout, new UserInfoFragment());
                transaction.commit();
            } else if (result.isFinishedWithError()) {
                // TODO: 5/26/2021  show cannot sign in user error
                Toast.makeText(getApplication()
                        , "Error code:" + result.getErrorCode(), Toast.LENGTH_LONG).show();

            } else {
                // TODO: 6/2/2021 show loading
                Toast.makeText(getApplication()
                        , "Loading", Toast.LENGTH_SHORT).show();
            }
        });
    }


    public static class Factory extends ViewModelProvider.NewInstanceFactory {

        @NonNull
        private final Application mApplication;

        @NonNull
        private final FragmentManager mFragmentManager;


        public Factory(@NonNull Application application, @NonNull FragmentManager fragmentManager) {
            mApplication = application;
            mFragmentManager = fragmentManager;
        }

        @SuppressWarnings("unchecked")
        @Override
        @NonNull
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new SignInViewModel(mApplication, mFragmentManager);
        }
    }
}