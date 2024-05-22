package com.codexxatransco.user.fregment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.codexxatransco.user.R;
import com.codexxatransco.user.model.PackersMovers;
import com.codexxatransco.user.model.ProductDataItem;
import com.codexxatransco.user.model.SubcatDataItem;
import com.codexxatransco.user.utility.MyDatabaseHelper;

import org.jetbrains.annotations.Nullable;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class CategoryFragment extends Fragment {



    private static final String ARG_CATEGORY = "category";
    private static final String ARG_POSITION = "position";
    @BindView(R.id.recyclerView)
    public RecyclerView recyclerView;
    @BindView(R.id.recyclerView_product)
    public RecyclerView recyclerViewProduct;

    private PackersMovers category;
    ProductAdapter productAdapter;
    SubCategoryPillAdapter subCategoryPillAdapter;
    int positionPos;
    MyDatabaseHelper myDatabaseHelper;
    public static CategoryFragment newInstance(PackersMovers category,int pos) {
        CategoryFragment fragment = new CategoryFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_CATEGORY, category);
        args.putInt(ARG_POSITION, pos);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            category = (PackersMovers) getArguments().getSerializable(ARG_CATEGORY);
            positionPos =getArguments().getInt(ARG_POSITION);
            Log.e("positionPos","EEEE "+positionPos);


        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_category, container, false);
        ButterKnife.bind(this,view);
        StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(3, GridLayoutManager.HORIZONTAL);
        myDatabaseHelper=new MyDatabaseHelper(getActivity());
        recyclerView.setLayoutManager(staggeredGridLayoutManager);

        subCategoryPillAdapter = new SubCategoryPillAdapter(category.getCategoryData().get(positionPos).getSubcatData());
        recyclerView.setAdapter(subCategoryPillAdapter);


        recyclerViewProduct.setLayoutManager(new LinearLayoutManager(getActivity()));
        productAdapter= new ProductAdapter(category.getCategoryData().get(positionPos).getSubcatData().get(0).getProductData());
        recyclerViewProduct.setAdapter(productAdapter);


        return view;
    }


    public class SubCategoryPillAdapter extends RecyclerView.Adapter<SubCategoryPill> {
        private List<SubcatDataItem> pills;
        private int selectedItem = 0;

        public SubCategoryPillAdapter(List<SubcatDataItem> pills) {
            this.pills = pills;
        }

        @Override
        public SubCategoryPill onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.pill_item_layout, parent, false);
            return new SubCategoryPill(view);
        }

        @Override
        public void onBindViewHolder(SubCategoryPill holder, int position) {
            SubcatDataItem pill = pills.get(position);
            holder.pillTextView.setText(pill.getSubcatTitle());
            if (selectedItem == position) {
                holder.pillTextView.setBackground(ContextCompat.getDrawable(getActivity(),R.drawable.boxfill));
            } else {
                holder.pillTextView.setBackground(ContextCompat.getDrawable(getActivity(),R.drawable.box));

            }
            holder.pillTextView.setOnClickListener(v -> {
                productAdapter = new ProductAdapter(category.getCategoryData().get(positionPos).getSubcatData().get(position).getProductData());
                recyclerViewProduct.setAdapter(productAdapter);
                int previousSelectedItem = selectedItem;
                selectedItem = position;
                if (previousSelectedItem != -1) {
                    notifyItemChanged(previousSelectedItem);
                }
                notifyItemChanged(selectedItem);
            });

        }

        @Override
        public int getItemCount() {
            return pills.size();
        }
    }

    public class SubCategoryPill extends RecyclerView.ViewHolder {
        public TextView pillTextView;
        public SubCategoryPill(View itemView) {
            super(itemView);
            pillTextView = (TextView) itemView.findViewById(R.id.pillTextView);
        }
    }


    public class ProductAdapter extends RecyclerView.Adapter<Product> {
        int count=0;
        private List<ProductDataItem> pills;
        public ProductAdapter(List<ProductDataItem> pills) {
            this.pills = pills;
        }

        @Override
        public Product onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.product_item_layout, parent, false);
            return new Product(view);
        }

        @Override
        public void onBindViewHolder(Product holder, int position) {
            ProductDataItem item=pills.get(position);
            count = myDatabaseHelper.getData(item.getProductId());
            holder.productTitle.setText(""+item.getProductTitle());

            holder.countTextview.setText(""+count);

            holder.minusButton.setOnClickListener(v -> {

                count=Integer.parseInt(holder.countTextview.getText().toString());
                if(count==0){
                    holder.countTextview.setText(""+count);
                }else {
                    count=count-1;
                    myDatabaseHelper.updateData(item.getProductId(),count);
                    holder.countTextview.setText(""+count);
                }

            });
            holder.plusButton.setOnClickListener(v -> {

                count=Integer.parseInt(holder.countTextview.getText().toString())+1;

                if(myDatabaseHelper.checkId(item.getProductId())==0){
                    myDatabaseHelper.insertData(item.getProductId(),item.getProductTitle(),item.getProductPrice(),count);
                }else {
                    myDatabaseHelper.updateData(item.getProductId(),count);
                }

                holder.countTextview.setText(""+count);

            });

        }

        @Override
        public int getItemCount() {
            return pills.size();
        }

    }

    public class Product extends RecyclerView.ViewHolder {
        @BindView(R.id.product_title)
        public TextView productTitle;
        @BindView(R.id.minus_button)
        public ImageView minusButton;
        @BindView(R.id.count_textview)
        public TextView countTextview;
        @BindView(R.id.plus_button)
        public ImageView plusButton;
        public Product(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }
    }
}