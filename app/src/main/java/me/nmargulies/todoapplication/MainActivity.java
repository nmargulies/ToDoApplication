package me.nmargulies.todoapplication;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    // a numeric code to identify the edit activity
    public static final int EDIT_REQUEST_CODE = 20;

    //keys used for passing data between activities
    public static final String ITEM_TEXT = "itemText";
    public static final String ITEM_POSITION = "itemPosition";

    ArrayList<String> items;
    ArrayAdapter<String> itemsAdapter;
    ListView lvItems;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // obtain reference to ListView created with the layout
        lvItems = (ListView) findViewById(R.id.lvItems);

        // initialize the items list
        readItems();

        //initialize the adapter using the items list
        itemsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items);

        // wire the adapter to the view
        lvItems.setAdapter(itemsAdapter);

        //add some mock items to the list
        //items.add("First");
        //items.add("Second");

        // setup the listener on creation
        setupListViewListener();
    }

    public void onAddItem(View v){
        // obtain a reference to the EditText created with the layout
        EditText etNewItem = (EditText) findViewById(R.id.etNewItem);

        // grab the EditText's content as a String
        String itemText = etNewItem.getText().toString();

        // add the item to the list via the adapter
        itemsAdapter.add(itemText);

        // clear the EditText  by setting it to an empty string
        etNewItem.setText("");

        // store the updated list
        writeItems();

        // display a notification to the user
        Toast.makeText(getApplicationContext(), "Item added to list", Toast.LENGTH_SHORT).show();

    }

    private void setupListViewListener() {
        // logging
        Log.i("MainActivity", "Removed item");

        // set the ListView's itemLongClickListener
        lvItems.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
               // remove the item in the list at the index given by position
               items.remove(position);

               // notify the adapter that the underlying dataset changed
               itemsAdapter.notifyDataSetChanged();

               // store the updated list
                writeItems();

               // return true to tell the framework that the long click was consumed
                return true;
            }
        });

        // set up item listener for edit (regular click)
        lvItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id){
                // first parameter is the context, second is the class of the activity to launch
                Intent i = new Intent(MainActivity.this, EditItemActivity.class);
                // put "extras" into the bundle for access in the edit activity
                i.putExtra(ITEM_TEXT, items.get(position));
                i.putExtra(ITEM_POSITION, position);
                // brings up the edit activity with the expectation of a result
                startActivityForResult(i, EDIT_REQUEST_CODE);
            }
        });
    }

    // handle results from edit activity


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // if the edit activity completed ok
        if (resultCode == RESULT_OK && requestCode == EDIT_REQUEST_CODE) {
            // extract updated item text from result intent extras
            String updatedItem = data.getExtras().getString(ITEM_TEXT);
            // extract original position of edited item
            int position = data.getExtras().getInt(ITEM_POSITION);
            // update the model with the new item text at the editd position
            items.set(position, updatedItem);
            // notify the adapter that the model changed
            itemsAdapter.notifyDataSetChanged();
            // persist the changed model
            writeItems();
            // notify the user the operation completed ok
            Toast.makeText(this, "Item updated successfully", Toast.LENGTH_SHORT).show();
        }
    }

    // returns the file in which the data is stored
    private File getDataFile() {
        return new File(getFilesDir(), "todo.txt");
    }

    // read the items from the file system
    private void readItems() {
        try {
            // create the array using the content in the file
            items = new ArrayList<>(FileUtils.readLines(getDataFile(), Charset.defaultCharset()));
        } catch (IOException e) {
            // print the error to the console
            Log.e("MainActivity", "Error reading file", e);
            // just load an empty list
            items = new ArrayList<>();
        }
    }

    private void writeItems() {
        try {
            FileUtils.writeLines(getDataFile(), items);
        } catch (IOException e) {
            Log.e("MainActivity", "Error writing file", e);
        }
    }

}
