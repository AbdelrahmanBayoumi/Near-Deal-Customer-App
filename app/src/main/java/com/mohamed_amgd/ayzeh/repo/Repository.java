package com.mohamed_amgd.ayzeh.repo;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.mohamed_amgd.ayzeh.Models.Filter;
import com.mohamed_amgd.ayzeh.Models.Offer;
import com.mohamed_amgd.ayzeh.Models.Product;
import com.mohamed_amgd.ayzeh.Models.SearchResult;
import com.mohamed_amgd.ayzeh.Models.Shop;
import com.mohamed_amgd.ayzeh.Models.User;
import com.mohamed_amgd.ayzeh.repo.retrofit.RetrofitClient;

import java.io.File;
import java.util.ArrayList;

public class Repository {

    private static final String TAG = "Repository :";
    private static Repository mInstance;
    private final FirebaseClient mFirebaseClient;
    private final RetrofitClient mRetrofitClient;

    private Repository() {
        mFirebaseClient = FirebaseClient.getInstance();
        mRetrofitClient = RetrofitClient.getInstance();
    }

    public static Repository getInstance() {
        if (mInstance == null) {
            mInstance = new Repository();
        }
        return mInstance;
    }

    public MutableLiveData<ArrayList<Product>> getHotDeals() {
        // TODO: 5/8/2021 use retrofit client to get hot deals from api
        return new MutableLiveData<>();
    }

    public MutableLiveData<ArrayList<Offer>> getProductOffers(String productId) {
        // TODO: 5/8/2021 use retrofit client to get offers of a product using its id from api
        return new MutableLiveData<>();
    }

    public SearchResult getSearchResult(String query, Filter filter) {
        // TODO: 5/9/2021 use retrofit client to get search result of query with filter
        return new SearchResult(new MutableLiveData<>(), 0f, 1000f);
    }

    public RepositoryResult<Shop> getShopById(String shopId) {
        // TODO: 5/12/2021 use retrofit client to get shop details using its shopId
        return mRetrofitClient.getShop(shopId);
    }

    public MutableLiveData<ArrayList<Product>> getShopProductsByShopId(String shopId) {
        // TODO: 5/12/2021 use retrofit client to get shop's products using shopId
        return new MutableLiveData<>();
    }

    public RepositoryResult<ArrayList<Shop>> getNearbyShops(double userLat, double userLon) {
        // TODO: 5/15/2021 use retrofit to get nearby shops to user's location by userLat,userLon
        return mRetrofitClient.getNearbyShops(userLat, userLon, 50);
    }

    public RepositoryResult<ArrayList<Shop>> getNearbyShops(double userLat, double userLon, String query) {
        // TODO: 5/18/2021 use retrofit to get nearby shops to user's location by userLat,userLon and query
        return mRetrofitClient.searchNearbyShopsByName(userLat, userLon, 50, query);
    }

    public SearchResult getHotDealsSearchResult(String query, Filter filter) {
        // TODO: 5/18/2021 use retrofit client to get hot deals search result of query with filter
        if (query == null) {
            return new SearchResult(getHotDeals(), 0f, 1000f);
        } else {
            return new SearchResult(new MutableLiveData<>(), 0f, 1000f);
        }
    }

    public RepositoryResult<User> getUser() {
        RepositoryResult<User> result;
        if (mFirebaseClient.getCurrentUser() == null) {
            result = new RepositoryResult<>(new MutableLiveData<>());
            result.setFinishedWithError(ErrorHandler.NEED_SIGN_IN_ERROR);
        } else {
            String email = mFirebaseClient.getCurrentUser().getEmail();
            String uid = mFirebaseClient.getCurrentUser().getUid();
            result = mRetrofitClient.getUserData(uid);
            result.getIsLoadingLiveData().observeForever(isLoading -> {
                if (result.isFinishedSuccessfully()) {
                    result.getData().getValue().setEmail(email);
                }
            });
        }
        return result;
    }

    public RepositoryResult<Boolean> createUser(String email, String username, String password, String birthdate) {
        final RepositoryResult<Boolean> result = mFirebaseClient.createNewUser(email, password);

        result.getIsLoadingLiveData().observeForever(isLoading -> {
            if (result.isFinishedSuccessfully()) {
                String uid = mFirebaseClient.getCurrentUser().getUid();
                User data = new User(uid, email, username, birthdate);
                RepositoryResult<Boolean> insertUserDataResult
                        = mRetrofitClient.insertUserData(uid, data);
                insertUserDataResult.getIsLoadingLiveData().observeForever(aBoolean -> {
                    if (insertUserDataResult.isFinishedWithError()) {
                        // updating the main result
                        result.setFinishedWithError(insertUserDataResult.getErrorCode());
                    }
                });
            }
        });
        return result;
    }

    public RepositoryResult<Boolean> signInUser(String email, String password) {
        return mFirebaseClient.signInUser(email, password);
    }

    public RepositoryResult<Boolean> updateUser(String email, String username, String password, String birthdate) {
        String oldEmail = mFirebaseClient.getCurrentUser().getEmail();
        final RepositoryResult<Boolean> result = mFirebaseClient.signInUser(oldEmail, password);
        result.getIsLoadingLiveData().observeForever((isLoading) -> {
            if (result.isFinishedSuccessfully()) {
                Log.i(TAG, "updateUser: signedIn");
                String uid = mFirebaseClient.getCurrentUser().getUid();
                RepositoryResult<Boolean> changeEmailResult = mFirebaseClient.changeUserEmail(email);
                changeEmailResult.getIsLoadingLiveData().observeForever(isLoadingChangeEmail -> {
                    if (changeEmailResult.isFinishedSuccessfully()) {
                        Log.i(TAG, "updateUser: emailChanged");
                        RepositoryResult<Boolean> changePasswordResult = mFirebaseClient.changeUserPassword(password);
                        changePasswordResult.getIsLoadingLiveData().observeForever(isLoadingChangePassword -> {
                            if (changePasswordResult.isFinishedSuccessfully()) {
                                Log.i(TAG, "updateUser: passwordChanged");
                                User data = new User(uid, email, username, birthdate);
                                RepositoryResult<Boolean> updateUserDataResult
                                        = mRetrofitClient.updateUserData(uid, data);
                                updateUserDataResult.getIsLoadingLiveData().observeForever(isLoadingUpdateUserData -> {
                                    if (updateUserDataResult.isFinishedWithError()) {
                                        result.setFinishedWithError(updateUserDataResult.getErrorCode());
                                    }
                                });
                            } else if (changePasswordResult.isFinishedWithError()) {
                                result.setFinishedWithError(changePasswordResult.getErrorCode());
                            }
                        });
                    } else if (changeEmailResult.isFinishedWithError()) {
                        result.setFinishedWithError(changeEmailResult.getErrorCode());
                    }
                });
            }
        });


        return result;
    }

    public RepositoryResult<Boolean> updateUserImage(Context context, String userImagePath) {
        String uid = mFirebaseClient.getCurrentUser().getUid();
        String type = RetrofitClient.UPLOAD_USER_IMAGE;
        File compressedImage = Util.getInstance().getCompressedImageFile(context, userImagePath);
        return mRetrofitClient.uploadImage(uid, type, compressedImage);
    }

    public void logoutUser() {
        mFirebaseClient.signOutUser();
    }
}
