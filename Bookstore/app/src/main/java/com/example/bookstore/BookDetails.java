package com.example.bookstore;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class BookDetails extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference reference, reference2;
    TextView d_title, d_price, d_isbn, d_description, d_quantity, d_want;
    ImageView d_image;
    Button cart_button, arrow_up, arrow_down;
    int count = 1;
    MyTTS tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_details);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        reference = FirebaseDatabase.getInstance().getReference("Cart");
        reference2 = FirebaseDatabase.getInstance().getReference("Books");
        tts = new MyTTS(this);

        d_title = findViewById(R.id.row_title);
        d_price = findViewById(R.id.row_price);
        d_isbn = findViewById(R.id.row_isbn);
        d_description = findViewById(R.id.row_description);
        d_quantity = findViewById(R.id.row_quantity);
        d_image = findViewById(R.id.row_image);
        cart_button = findViewById(R.id.cart_button);
        arrow_up = findViewById(R.id.arrow_up);
        arrow_down = findViewById(R.id.arrow_down);
        d_want = findViewById(R.id.quantity_wanted);

        // get the data of the book the user pressed, passed from the Books activity
        Intent details = getIntent();
        String title = details.getStringExtra("title");
        String isbn = details.getStringExtra("isbn");
        String price = details.getStringExtra("price");
        String description = details.getStringExtra("description");
        String quantity = details.getStringExtra("quantity");
        // image processing in order to render the book's image
        byte[] bytes = details.getByteArrayExtra("bytes");
        Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

        d_title.setText(title);
        d_isbn.setText(isbn);
        d_price.setText(price);
        d_description.setText(description);
        d_quantity.setText(quantity);
        d_image.setImageBitmap(bmp);

        reference2.child(title).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                BookData data = snapshot.getValue(BookData.class);
                int available_quantity = data.getQuantity();

                // if the book is out of stock don't show the buttons about the cart and the wanted quantity of books
                if (available_quantity == 0) {
                    cart_button.setVisibility(View.INVISIBLE);
                    arrow_up.setVisibility(View.INVISIBLE);
                    arrow_down.setVisibility(View.INVISIBLE);
                    d_want.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // increment the quantity of books user wants
        arrow_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                count ++;
                d_want.setText(count + "");
                // get the available quantity of books so that we don't allow the user to select more than there are available
                reference2.child(title).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        BookData data = snapshot.getValue(BookData.class);
                        assert data != null;
                        int available_quantity = data.getQuantity();

                        // don't allow the user to select more books than enough to add to his cart
                        if (Integer.parseInt(d_want.getText().toString()) > available_quantity){
                            Toast.makeText(BookDetails.this, "No more books available", Toast.LENGTH_SHORT).show();
                            // reset the number of wanted books so that it doesn't surpass the available quantity
                            count--;
                            d_want.setText(count + "");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });

        // decrement the quantity of books the user wants
        arrow_down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                count--;
                d_want.setText(count + "");
                // don't allow the user to choose negative amount of books
                if (Integer.parseInt(d_want.getText().toString()) < 0) {
                    count++;
                    d_want.setText(count + "");
                }
            }
        });

        // set onClickListener on the cart button in order to add a book to the cart if the user presses it
        cart_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // get the available quantity of the specific book
                reference2.child(title).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        BookData data = snapshot.getValue(BookData.class);
                        assert data != null;
                        int available_quantity = data.getQuantity();
                        String books_wanted = d_want.getText().toString();
                        int amount_wanted = Integer.parseInt(books_wanted);

                        // if books available > 0, let the user add it to his cart
                        if (available_quantity > 0) {
                            // add book to a separate firebase reference (cart) when the user clicks the cart button
                            // the cart will only show the title and price of the book
                            reference.child(title).child("title").setValue(title);
                            reference.child(title).child("price").setValue(price);
                            reference.child(title).child("quantity").setValue(amount_wanted);
                            Toast.makeText(BookDetails.this, "Added to cart", Toast.LENGTH_SHORT).show();
                            tts.speak("The book has been added to your cart!");
                        }
                        // if the book quantity is 0 notify the user that the book is out of stock
                        else {
                            Toast.makeText(BookDetails.this, "Out of stock", Toast.LENGTH_SHORT).show();
                            tts.speak("Sorry, the book is out of stock.");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });
    }

    // when the user presses the back button redirect user to the books activity
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            startActivity(new Intent(BookDetails.this, Books.class));
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    // the following code is used for the menu bar of the application
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.logoutMenu){logout();}
        if (id == R.id.cartMenu){cart();}

        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        firebaseAuth.signOut();
        finish();
        startActivity(new Intent(BookDetails.this, MainActivity.class));
        Toast.makeText(BookDetails.this,"LOGOUT SUCCESSFUL", Toast.LENGTH_SHORT).show();
        // delete the items of the cart once the user logs out
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Cart");
        reference.removeValue();
    }

    private void cart() {
        startActivity(new Intent(BookDetails.this, Cart.class));
        Toast.makeText(BookDetails.this,"VIEW CART", Toast.LENGTH_SHORT).show();
    }
}
