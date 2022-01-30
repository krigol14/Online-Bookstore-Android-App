package com.example.bookstore;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

// CartAdapter class is used in order to show the data from BookData.java in the row_cart.xml cardView file
public class CartAdapter extends FirebaseRecyclerAdapter<BookData, CartAdapter.cartViewHolder> {
    // FirebaseRecyclerAdapter is a class provided by FirebaseUI in order to bind,
    // adapt and show database contents in a RecyclerView

    // constructor
    public CartAdapter(@NonNull FirebaseRecyclerOptions<BookData> options) {
        super(options);
    }

    // sub class to create references of the views in row_cart.xml
    static class cartViewHolder extends RecyclerView.ViewHolder {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Cart");
        TextView title, price, quantity, part_price;
        Button delete;

        // constructor
        public cartViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.row_title);
            price = itemView.findViewById(R.id.row_price);
            quantity = itemView.findViewById(R.id.row_quantity);
            part_price = itemView.findViewById(R.id.part_price);
            delete = itemView.findViewById(R.id.row_delete);

            // delete the book from the cart reference of the firebase database
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // get the title of the book and use it in order to delete the exact book from the cart
                    String title_text = title.getText().toString();
                    reference.child(title_text).removeValue();
                }
            });
        }
    }

    // function to bind the view in row_cart.xml with the data in BookData.java file
    @SuppressLint("SetTextI18n")
    @Override
    protected void onBindViewHolder(@NonNull cartViewHolder holder, int position, @NonNull BookData model) {
        // find the total price of the particular book the user has in his cart
        // by multiplying its price with the quantity the user has chosen

        // the price in firebase is in such format "15.00$" and we only need the "15.00" so we get the substring
        float one_price = Float.parseFloat(model.getPrice().substring(0, 5));
        float total_price = one_price * model.getQuantity();

        holder.title.setText(model.getTitle());
        holder.part_price.setText(one_price + "$");
        holder.quantity.setText(String.valueOf(model.getQuantity()));
        holder.price.setText(Float.toString(total_price) + "$");
    }

    // function to tell the class about the row_cart.xml file, in which the data will be shown
    @NonNull
    @Override
    public cartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_cart, parent, false);
        return new cartViewHolder(view);
    }
}
