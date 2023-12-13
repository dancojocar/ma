/*
 * Copyright (C) 2021 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ro.ubbcluj.cs.ds.ui;

import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.URLSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import ro.ubbcluj.cs.ds.MakePurchaseViewModel;
import ro.ubbcluj.cs.ds.TrivialDriveApplication;
import ro.ubbcluj.cs.ds.TrivialDriveRepository;
import ro.ubbcluj.cs.ds.databinding.FragmentMakePurchaseBinding;
import ro.ubbcluj.cs.ds.R;


import java.util.ArrayList;
import java.util.List;

/**
 * This Fragment is simply a wrapper for the inventory (i.e. items for sale). Here again there is
 * no complicated billing logic. All the billing logic reside inside the [BillingRepository].
 * The [BillingRepository] provides a so-called [AugmentedSkuDetails] object that shows what
 * is for sale and whether the user is allowed to buy the item at this moment. E.g. if the user
 * already has a full tank of gas, then they cannot buy gas at this moment.
 */
public class MakePurchaseFragment extends Fragment {
    String TAG = "MakePurchaseFragment";
    private static final String PLAY_STORE_SUBSCRIPTION_DEEPLINK_URL = "https://play.google.com/store/account/subscriptions?sku=%s&package=%s";

    private MakePurchaseViewModel makePurchaseViewModel;
    private FragmentMakePurchaseBinding binding;
    private final List<MakePurchaseAdapter.Item> inventoryList = new ArrayList<>();

    /**
     * While this list is hard-coded here, it could just as easily come from a server, allowing
     * you to add new SKUs to your app without having to update your app.
     */
    void makeInventoryList() {
        inventoryList.add(new MakePurchaseAdapter.Item(
                getText(R.string.header_fuel_your_ride), MakePurchaseAdapter.VIEW_TYPE_HEADER
        ));
        inventoryList.add(new MakePurchaseAdapter.Item(
                TrivialDriveRepository.SKU_GAS, MakePurchaseAdapter.VIEW_TYPE_ITEM
        ));
        inventoryList.add(new MakePurchaseAdapter.Item(
                getText(R.string.header_go_premium), MakePurchaseAdapter.VIEW_TYPE_HEADER
        ));
        inventoryList.add(new MakePurchaseAdapter.Item(
                TrivialDriveRepository.SKU_PREMIUM, MakePurchaseAdapter.VIEW_TYPE_ITEM
        ));
        inventoryList.add(new MakePurchaseAdapter.Item(
                getText(R.string.header_subscribe), MakePurchaseAdapter.VIEW_TYPE_HEADER
        ));
        inventoryList.add(new MakePurchaseAdapter.Item(
                TrivialDriveRepository.SKU_INFINITE_GAS_MONTHLY, MakePurchaseAdapter.VIEW_TYPE_ITEM
        ));
        inventoryList.add(new MakePurchaseAdapter.Item(
                TrivialDriveRepository.SKU_INFINITE_GAS_YEARLY, MakePurchaseAdapter.VIEW_TYPE_ITEM
        ));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_make_purchase, container, false);
        // This allows data binding to automatically observe any LiveData we pass in
        binding.setLifecycleOwner(this);
        makeInventoryList();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MakePurchaseViewModel.MakePurchaseViewModelFactory makePurchaseViewModelFactory =
                new MakePurchaseViewModel.MakePurchaseViewModelFactory(
                        ((TrivialDriveApplication)getActivity().getApplication()).getAppContainer()
                                .getTrivialDriveRepository());
        makePurchaseViewModel = new ViewModelProvider(this,makePurchaseViewModelFactory)
                .get(MakePurchaseViewModel.class);

        binding.setMpvm(makePurchaseViewModel);
        binding.inappInventory.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.inappInventory.setAdapter(new MakePurchaseAdapter(inventoryList, makePurchaseViewModel, this));
    }

    public void makePurchase(@NonNull String sku) {
        makePurchaseViewModel.buySku(getActivity(), sku);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public LiveData<Boolean> canBuySku(String sku) {
        return makePurchaseViewModel.canBuySku(sku);
    }

    private void combineTitleSkuAndIsPurchasedData(
            MediatorLiveData<CharSequence> result,
            LiveData<String> skuTitleLiveData,
            LiveData<Boolean> isPurchasedLiveData,
            @NonNull String sku
    ) {
        String skuTitle = skuTitleLiveData.getValue();
        Boolean isPurchased = isPurchasedLiveData.getValue();
        // don't emit until we have all of our data
        if (null == skuTitle || null == isPurchased) {
            return;
        }
        if ( isPurchased && ( sku.equals(TrivialDriveRepository.SKU_INFINITE_GAS_MONTHLY) ||
                sku.equals(TrivialDriveRepository.SKU_INFINITE_GAS_YEARLY))) {
            // add URL to the Play store to allow user to unsubscribe if the user already has
            // purchased a subscription
            SpannableString titleSpannable = new SpannableString(skuTitle);
            titleSpannable.setSpan(new URLSpan(String.format(
                    PLAY_STORE_SUBSCRIPTION_DEEPLINK_URL,
                    sku,
                    getContext().getPackageName())), 0, titleSpannable.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            result.setValue(titleSpannable);
        } else {
            // empty SpannableString needs to be used to clear spannables
            SpannableString titleSpannable = new SpannableString(skuTitle);
            result.setValue(titleSpannable);
        }
    }

    public LiveData<CharSequence> skuTitle(final @NonNull String sku) {
        MakePurchaseViewModel.SkuDetails skuDetails = makePurchaseViewModel.getSkuDetails(sku);
        final LiveData<String> skuTitleLiveData = skuDetails.getTitle();
        final LiveData<Boolean> isPurchasedLiveData = makePurchaseViewModel.isPurchased(sku);
        final MediatorLiveData<CharSequence> result = new MediatorLiveData<>();
        result.addSource(skuTitleLiveData, title ->
                combineTitleSkuAndIsPurchasedData(result, skuTitleLiveData, isPurchasedLiveData, sku ));
        result.addSource(isPurchasedLiveData, isPurchased ->
                combineTitleSkuAndIsPurchasedData(result, skuTitleLiveData, isPurchasedLiveData, sku ));
        return result;
    }
}
