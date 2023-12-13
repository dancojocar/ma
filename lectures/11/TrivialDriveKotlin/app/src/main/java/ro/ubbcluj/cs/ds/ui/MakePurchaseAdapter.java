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

import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import ro.ubbcluj.cs.ds.MakePurchaseViewModel;
import ro.ubbcluj.cs.ds.databinding.InventoryHeaderBinding;
import ro.ubbcluj.cs.ds.databinding.InventoryItemBinding;

import ro.ubbcluj.cs.ds.R;

import java.util.List;

/**
 * Basic implementation of RecyclerView adapter with header and content views.
 */
public class MakePurchaseAdapter extends RecyclerView.Adapter<MakePurchaseAdapter.ViewHolder> {
    static public final int VIEW_TYPE_HEADER = 0;
    static public final int VIEW_TYPE_ITEM = 1;
    private final List<Item> inventoryList;
    private final MakePurchaseViewModel makePurchaseViewModel;
    private final MakePurchaseFragment makePurchaseFragment;

    public MakePurchaseAdapter(@NonNull List<Item> inventoryList,
            @NonNull MakePurchaseViewModel makePurchaseViewModel,
            @NonNull MakePurchaseFragment makePurchaseFragment) {
        this.inventoryList = inventoryList;
        this.makePurchaseViewModel = makePurchaseViewModel;
        this.makePurchaseFragment = makePurchaseFragment;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        InventoryItemBinding inventoryItemBinding = null;
        InventoryHeaderBinding inventoryHeaderBinding = null;
        switch (viewType) {
            case VIEW_TYPE_HEADER:
                inventoryHeaderBinding = DataBindingUtil.inflate(layoutInflater, R.layout.inventory_header, parent,
                        false);
                view = inventoryHeaderBinding.getRoot();
                break;
            default: // VIEW_TYPE_ITEM
                inventoryItemBinding = DataBindingUtil.inflate(layoutInflater, R.layout.inventory_item, parent,
                        false);
                view = inventoryItemBinding.getRoot();
                break;
        }
        return new ViewHolder(view, viewType, inventoryHeaderBinding, inventoryItemBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Item item = inventoryList.get(position);
        holder.bind(item, makePurchaseViewModel, makePurchaseFragment);
    }

    @Override
    public int getItemCount() {
        return inventoryList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return inventoryList.get(position).viewType;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final InventoryHeaderBinding inventoryHeaderBinding;
        final InventoryItemBinding inventoryItemBinding;

        public ViewHolder(View v, int viewType, InventoryHeaderBinding inventoryHeaderBinding,
                InventoryItemBinding inventoryItemBinding
        ) {
            super(v);
            this.inventoryHeaderBinding = inventoryHeaderBinding;
            this.inventoryItemBinding = inventoryItemBinding;
        }

        void bind(Item item,
                MakePurchaseViewModel makePurchaseViewModel,
                MakePurchaseFragment makePurchaseFragment) {
            switch (item.viewType) {
                case VIEW_TYPE_HEADER:
                    inventoryHeaderBinding.headerTitle.setText(item.getTitleOrSku());
                    inventoryHeaderBinding.headerTitle.setMovementMethod(LinkMovementMethod.getInstance());
                    inventoryHeaderBinding.setLifecycleOwner(makePurchaseFragment);
                    inventoryHeaderBinding.executePendingBindings();
                    break;
                default:
                    inventoryItemBinding.setSku(item.getTitleOrSku().toString());
                    inventoryItemBinding.setSkuDetails(
                            makePurchaseViewModel.getSkuDetails(item.getTitleOrSku().toString()));
                    inventoryItemBinding.skuTitle.setMovementMethod(LinkMovementMethod.getInstance());
                    inventoryItemBinding.setMakePurchaseFragment(makePurchaseFragment);
                    inventoryItemBinding.setLifecycleOwner(makePurchaseFragment);
                    inventoryItemBinding.executePendingBindings();
                    break;
            }
        }
    }

    /**
     * An item to be displayed in our RecyclerView. Each item contains a single string: either
     * the title of a header or a reference to a SKU, depending on what the type of the view is.
     */
    static class Item {
        public Item(@NonNull CharSequence titleOrSku, int viewType) {
            this.titleOrSku = titleOrSku;
            this.viewType = viewType;
        }

        public @NonNull
        CharSequence getTitleOrSku() {
            return titleOrSku;
        }

        public int getViewType() {
            return viewType;
        }

        private final @NonNull
        CharSequence titleOrSku;
        private final int viewType;
    }
}