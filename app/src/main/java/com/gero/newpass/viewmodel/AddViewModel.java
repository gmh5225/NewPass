package com.gero.newpass.viewmodel;

import com.gero.newpass.R;
import com.gero.newpass.database.DatabaseHelper;
import com.gero.newpass.database.DatabaseServiceLocator;
import com.gero.newpass.repository.ResourceRepository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class AddViewModel extends ViewModel {

    private final DatabaseHelper databaseHelper;
    private final MutableLiveData<String> messageLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> successLiveData = new MutableLiveData<>();
    private final ResourceRepository resourceRepository;

    public AddViewModel(ResourceRepository resourceRepository) {
        this.databaseHelper = DatabaseServiceLocator.getDatabaseHelper();
        this.resourceRepository = resourceRepository;
    }

    public LiveData<String> getMessageLiveData() {
        return messageLiveData;
    }

    public LiveData<Boolean> getSuccessLiveData() {
        return successLiveData;
    }

    public void addEntry(String name, String email, String password) {

        if (!name.isEmpty() && !email.isEmpty() && password.length() >= 4) {

            if (databaseHelper.checkIfAccountAlreadyExist(name, email)) {
                    messageLiveData.setValue(resourceRepository.getString(R.string.this_account_already_exists));
                    successLiveData.setValue(false);

                } else  {
                    databaseHelper.addEntry(name, email, password);
                    messageLiveData.setValue(resourceRepository.getString(R.string.account_added_successfully));
                    successLiveData.setValue(true);
                }

            } else {
                successLiveData.setValue(false);
                if (name.isEmpty()) {
                    messageLiveData.setValue(resourceRepository.getString(R.string.name_should_not_be_empty));

                } else if (email.isEmpty()) {
                    messageLiveData.setValue(resourceRepository.getString(R.string.email_should_not_be_empty));

                } else {
                    messageLiveData.setValue(resourceRepository.getString(R.string.password_must_be_at_least_4_characters_long));
                }
            }
    }
}
