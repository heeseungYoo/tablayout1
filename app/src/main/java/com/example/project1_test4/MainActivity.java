package com.example.project1_test4;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ArrayList<Dictionary> mArrayList;
    private CustomAdapter mAdapter;
    private int count = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

         Permission permission = new Permission();
         permission.checkPermissions(MainActivity.this);


        RecyclerView mRecyclerView = findViewById(R.id.recyclerview_main_list);
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);

        mArrayList = new ArrayList<>();
        mAdapter = new CustomAdapter(this, mArrayList);
        mRecyclerView.setAdapter(mAdapter);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(mRecyclerView);


        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),
        mLinearLayoutManager.getOrientation());
        mRecyclerView.addItemDecoration(dividerItemDecoration);


        getDictionaryList();
        mAdapter.notifyDataSetChanged();


        Button buttonInsert = findViewById(R.id.button_main_insert);
        buttonInsert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.edit_box, null, false);
                builder.setView(view);

                final Button ButtonSubmit = view.findViewById(R.id.button_dialog_submit);
                final EditText editTextID = view.findViewById(R.id.edittext_dialog_id);
                final EditText editTextNAME = view.findViewById(R.id.edittext_dialog_name);
                final EditText editTextPHONE = view.findViewById(R.id.edittext_dialog_phone);

                editTextPHONE.addTextChangedListener(new PhoneNumberFormattingTextWatcher());

                ButtonSubmit.setText("삽입");

                final AlertDialog dialog = builder.create();

                // 3. 다이얼로그에 있는 삽입 버튼을 클릭하면

                ButtonSubmit.setOnClickListener(new View.OnClickListener() {

                    public void onClick(View v) {


                        // 4. 사용자가 입력한 내용을 가져와서
                        String strID = editTextID.getText().toString();
                        String strName = editTextNAME.getText().toString();
                        String strPhone = editTextPHONE.getText().toString();

                        insertContact(getContentResolver(), strName, strPhone);
                        //saveContact(getContentResolver(), strName, strPhone);


                        // 5. ArrayList에 추가하고

                        Dictionary dict = new Dictionary(strID, strName, strPhone);
                        //mArrayList.add(0, dict); //첫번째 줄에 삽입됨
                        mArrayList.add(dict); //마지막 줄에 삽입됨


                        // 6. 어댑터에서 RecyclerView에 반영하도록 합니다.

                        //mAdapter.notifyItemInserted(0);
                        mAdapter.notifyDataSetChanged();

                        dialog.dismiss();
                    }
                });

                dialog.show();



            }
        });



    }

    ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            final int position = viewHolder.getAdapterPosition();
            System.out.println("position : " + position);
            System.out.println("mArrayList index : " + mArrayList.get(position).getUser_Name());

            deleteContact(getContentResolver(), mArrayList.get(position).getPersonId());

            mArrayList.remove(position);
            mAdapter.notifyItemRemoved(position);


        }
    };

    private static void deleteContact(ContentResolver contactHelper, long getContactId) {
        System.out.println("Contact ID : " + getContactId);
        String where = ContactsContract.RawContacts.CONTACT_ID + " = " + getContactId;
        System.out.println("where : " + where);
        contactHelper.delete(ContactsContract.RawContacts.CONTENT_URI, where, null);
    }

    public ArrayList<Dictionary> getDictionaryList() {
        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String[] projection = new String[]{
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.RawContacts.CONTACT_ID
        };
        String[] selectionArgs = null;
        String sortOrder = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY + " asc";

        Cursor cursor = getContentResolver().query(uri, projection, null, selectionArgs, sortOrder);

        if (cursor != null && cursor.getCount() > 0) {
            for (cursor.moveToFirst(); !(cursor.isAfterLast()); cursor.moveToNext()) {
                count++;
                long person = cursor.getLong(2);
                Dictionary dictionary = new Dictionary();
                dictionary.setId(count + "");
                dictionary.setUser_Name(cursor.getString(1));
                dictionary.setUser_phNumber(cursor.getString(0));
                dictionary.setPersonId(person);

                if (dictionary.getUser_phNumber().startsWith("01")) {
                    mArrayList.add(dictionary);
                    Log.d("<<CONTACTS", "name=" + dictionary.getUser_Name() + ", phone = " + dictionary.getUser_phNumber() + ", id = " + dictionary.getId() +
                            ", personId = " + dictionary.getPersonId());
                }
            }
            /*
            do {
                count++;
                long person = cursor.getLong(2);
                Dictionary dictionary = new Dictionary();
                dictionary.setId(count+"");
                dictionary.setUser_Name(cursor.getString(1));
                dictionary.setUser_phNumber(cursor.getString(0));
                dictionary.setPersonId(person);

                if(dictionary.getUser_phNumber().startsWith("01")) {
                    mArrayList.add(dictionary);
                    Log.d("<<CONTACTS", "name=" + dictionary.getUser_Name() + ", phone = " + dictionary.getUser_phNumber() + ", id = " + dictionary.getId() +
                            ", personId = " + dictionary.getPersonId());
                }
            } while(cursor.moveToNext());*/
            cursor.close();
        }
        return mArrayList;
    }

    public static void insertContact2(ContentResolver contactHelper, String name, String phoneNumber) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, name);
        contentValues.put(ContactsContract.CommonDataKinds.Phone.NUMBER, phoneNumber);

        contactHelper.insert(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, contentValues);
    }

    public static boolean insertContact(ContentResolver contactHelper, String name, String phoneNumber) {
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();

        ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null).build());

        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, name).build());

        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phoneNumber)
                .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE).build());

        try {
            contactHelper.applyBatch(ContactsContract.AUTHORITY, ops);
        } catch (Exception e) {
            Log.e("ContactsAdder", "Exception: " + e);
            return false;
        }
        return true;
    }

    public static void deleteContact(ContentResolver contactHelper, int adapterPosition) {
        String where = ContactsContract.RawContacts.CONTACT_ID + " = " + adapterPosition;
        contactHelper.delete(ContactsContract.RawContacts.CONTENT_URI, where, null);
    }

    /*
    public static boolean saveContact(ContentResolver contactHelper, String name, String phoneNumber) {
        ContentValues cv = new ContentValues();
        cv.put(ContactsContract.RawContacts.CONTACT_ID, 0);
        cv.put(ContactsContract.RawContacts.AGGREGATION_MODE, ContactsContract.RawContacts.AGGREGATION_MODE_DISABLED);
        Uri rawContactUri = contactHelper.insert(ContactsContract.RawContacts.CONTENT_URI, cv);
        long rawContactId = ContentUris.parseId(rawContactUri);

        try {
            cv.clear();
            cv.put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId);
            cv.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
            cv.put(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE);
            cv.put(ContactsContract.CommonDataKinds.Phone.NUMBER, phoneNumber);
            Uri dataUri = contactHelper.insert(ContactsContract.Data.CONTENT_URI, cv);

            cv.clear();
            cv.put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId);
            cv.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);
            cv.put(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name);
            dataUri = contactHelper.insert(ContactsContract.Data.CONTENT_URI, cv);
        } catch (Exception e) {
            Log.e("ContactSaver : ", "Exception" + e);
            return false;
        }
        return true;
    }*/



}
