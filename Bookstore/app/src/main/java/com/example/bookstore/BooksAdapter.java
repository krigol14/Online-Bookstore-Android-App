package com.example.bookstore;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;

// BooksAdapter class is used in order to show the data from BookData.java in the row.xml cardView file
public class BooksAdapter extends FirebaseRecyclerAdapter<BookData, BooksAdapter.booksViewHolder> {
    // FirebaseRecyclerAdapter is a class provided by FirebaseUI in order to bind,
    // adapt and show database contents in a RecyclerView

    // constructor
    public BooksAdapter(@NonNull FirebaseRecyclerOptions<BookData> options) {
        super(options);
    }

    // sub class to create references of the views in row.xml
    static class booksViewHolder extends RecyclerView.ViewHolder {
        TextView title, isbn, price, description, quantity;
        ImageView image;

        // constructor
        public booksViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.row_title);
            description = itemView.findViewById(R.id.row_description);
            isbn = itemView.findViewById(R.id.row_isbn);
            price = itemView.findViewById(R.id.row_price);
            image = itemView.findViewById(R.id.row_image);
            quantity = itemView.findViewById(R.id.row_quantity);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent details = new Intent(view.getContext(), BookDetails.class);

                    // image processing in order to send it to the new BookDetails activity
                    Bitmap bitmap = ((BitmapDrawable)image.getDrawable()).getBitmap();
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    byte[] byteArray = stream.toByteArray();

                    // pass the data of the book clicked to the new BookDetails activity
                    details.putExtra("title", title.getText());
                    details.putExtra("isbn", isbn.getText());
                    details.putExtra("price", price.getText());
                    details.putExtra("description", description.getText());
                    details.putExtra("quantity", quantity.getText());
                    details.putExtra("bytes", byteArray);

                    view.getContext().startActivity(details);
                }
            });
        }
    }

    // function to bind the view in row.xml with the data in BookData.java file
    @Override
    protected void onBindViewHolder(@NonNull booksViewHolder holder, int position, @NonNull BookData model) {
        holder.title.setText(model.getTitle());
        holder.price.setText(model.getPrice());
        holder.isbn.setText(model.getIsbn());
        holder.description.setText(model.getDescription());
        holder.quantity.setText(String.valueOf(model.getQuantity()));
        ImageView mImage = holder.image;
        Picasso.get().load(model.getImage()).into(mImage);
    }

    // function to tell the class about the row.xml file, in which the data will be shown
    @NonNull
    @Override
    public booksViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row, parent, false);
        return new booksViewHolder(view);
    }
}
