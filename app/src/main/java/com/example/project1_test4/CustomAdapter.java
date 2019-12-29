package com.example.project1_test4;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.nfc.Tag;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import java.util.ArrayList;

import static android.content.ContentValues.TAG;


public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.CustomViewHolder> {

    private ArrayList<Dictionary> mList;
    private Context mContext;

    public class CustomViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {
        protected TextView id;
        protected TextView phoneNumber;
        protected TextView name;


        public CustomViewHolder(View view) {
            super(view);
            this.id = view.findViewById(R.id.id_listitem);
            this.phoneNumber = view.findViewById(R.id.phone_listitem);
            this.name = view.findViewById(R.id.name_listitem);

            view.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            MenuItem Edit = menu.add(Menu.NONE, 1001, 1, "편집");
            MenuItem Delete = menu.add(Menu.NONE, 1002, 2, "삭제");
            Edit.setOnMenuItemClickListener(onEditMenu);
            Delete.setOnMenuItemClickListener(onEditMenu);
        }

        private final MenuItem.OnMenuItemClickListener onEditMenu = new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case 1001:
                        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                        View view = LayoutInflater.from(mContext).inflate(R.layout.edit_box, null, false);
                        builder.setView(view);
                        final Button ButtonSubmit = view.findViewById(R.id.button_dialog_submit);
                        final EditText editTextID = view.findViewById(R.id.edittext_dialog_id);
                        final EditText editTextNAME = view.findViewById(R.id.edittext_dialog_name);
                        final EditText editTextPHONE = view.findViewById(R.id.edittext_dialog_phone);

                        editTextID.setText(mList.get(getAdapterPosition()).getId());
                        editTextNAME.setText(mList.get(getAdapterPosition()).getUser_Name());
                        editTextPHONE.setText(mList.get(getAdapterPosition()).getUser_phNumber());

                        editTextPHONE.addTextChangedListener(new PhoneNumberFormattingTextWatcher());

                        final AlertDialog dialog = builder.create();
                        ButtonSubmit.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String strID = editTextID.getText().toString();
                                String strNAME = editTextNAME.getText().toString();
                                String strPHONE = editTextPHONE.getText().toString();

                                Dictionary dict = new Dictionary(mList.get(getAdapterPosition()).getPersonId(), strID, strNAME, strPHONE);
                                System.out.println("dict의 personId : " + dict.getPersonId());
                                mList.set(getAdapterPosition(), dict);
                                System.out.println("mList personId : " + mList.get(getAdapterPosition()).getPersonId());
                                notifyItemChanged(getAdapterPosition());
                                dialog.dismiss();

                                updateContact3(mContext.getContentResolver(), mList.get(getAdapterPosition()), mList.get(getAdapterPosition()).getPersonId());
                            }
                        });

                        dialog.show();

                        break;

                    case 1002:
                        long mid = mList.get(getAdapterPosition()).getPersonId();
                        System.out.println("mid 값 : " + mid);
                        deleteContact(mContext.getContentResolver(), mid);

                        mList.remove(getAdapterPosition());
                        notifyItemRemoved(getAdapterPosition());
                        notifyItemRangeChanged(getAdapterPosition(), mList.size());

                        break;
                }
                return true;


            }
        };
    }


    private static void deleteContact(ContentResolver contactHelper, long getContactId) {
        System.out.println("Contact ID : " + getContactId);
        String where = ContactsContract.RawContacts.CONTACT_ID + " = " + getContactId;
        contactHelper.delete(ContactsContract.RawContacts.CONTENT_URI, where, null);
    }

    private static void updateContact2(ContentResolver contactHelper, Dictionary dictionary, long getContactId) {
        String where = ContactsContract.RawContacts.CONTACT_ID + " = " + getContactId;
        ContentValues values = new ContentValues();
        System.out.println("name: " + dictionary.getUser_Name());
        System.out.println("getContactId : " + getContactId);
        values.put(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, dictionary.getUser_Name());
        values.put(ContactsContract.CommonDataKinds.Phone.NUMBER, dictionary.getUser_phNumber());
        contactHelper.update(ContactsContract.Data.CONTENT_URI, values, where, null);
    }

    private static void updateContact3(ContentResolver contactHelper, Dictionary dictionary, long getContactId) {
        String where = ContactsContract.RawContacts.CONTACT_ID + " = " + getContactId;
        String nameWhere = ContactsContract.Data.CONTACT_ID + " = " + getContactId + " AND " + ContactsContract.Data.MIMETYPE + " = " + ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE;
        String phoneWhere = ContactsContract.Data.CONTACT_ID + " = " + getContactId + " AND " + ContactsContract.Data.MIMETYPE + " = " + ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE;
        String[] nameArgs = new String[] {String.valueOf(getContactId), String.valueOf(ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)};
        String[] phoneArgs = new String[] {String.valueOf(getContactId), String.valueOf(ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)};
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();
        ContentProviderOperation.Builder op = ContentProviderOperation.newUpdate(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null);
        ops.add(op.build());
        op = ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                .withSelection(where, null)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, dictionary.getUser_Name());

        ops.add(op.build());
        op = ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                .withSelection(where, null)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, dictionary.getUser_phNumber())
                .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE);
        ops.add(op.build());

        System.out.println("name: " + dictionary.getUser_Name());
        System.out.println("getContactId : " + getContactId);
        System.out.println("display name : " + ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME);
        Log.d(TAG, "Creating contact : " + dictionary.getUser_Name());
        try {
            contactHelper.applyBatch(ContactsContract.AUTHORITY, ops);
        } catch (Exception e) {
            Log.e(TAG, "Exception encountered while updating contact: " + e);
        }

    }

    private static void updateContact4(ContentResolver contactHelper, Dictionary dictionary, long getContactId) {
        String where = ContactsContract.RawContacts.CONTACT_ID + " = " + getContactId;
        String nameWhere = ContactsContract.Data.CONTACT_ID + " = " + getContactId + " AND " + ContactsContract.Data.MIMETYPE + " = " + ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE;
        String phoneWhere = ContactsContract.Data.CONTACT_ID + " = " + getContactId + " AND " + ContactsContract.Data.MIMETYPE + " = " + ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE;
        String[] nameArgs = new String[] {String.valueOf(getContactId), String.valueOf(ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)};
        String[] phoneArgs = new String[] {String.valueOf(getContactId), String.valueOf(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)};
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();
        ops.add(ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                .withSelection(where, null)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, dictionary.getUser_Name()).build());
        ops.add(ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                .withSelection(where, null)
                .withValue(ContactsContract.CommonDataKinds.Phone.DATA, dictionary.getUser_phNumber()).build());

        Log.d(TAG, "Creating contact : " + dictionary.getUser_Name());
        try {
            contactHelper.applyBatch(ContactsContract.AUTHORITY, ops);
        } catch (Exception e) {
            Log.e(TAG, "Exception encountered while updating contact: " + e);
        }

    }

    private static void updateContactName(ContentResolver contactHelper, String name, long getContactId) {
        String where = ContactsContract.RawContacts.CONTACT_ID + " = " + getContactId;
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();
        ops.add(ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                .withSelection(where, null)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name).build());

        Log.d(TAG, "Creating contact : " + name);
        try {
            contactHelper.applyBatch(ContactsContract.AUTHORITY, ops);
        } catch (Exception e) {
            Log.e(TAG, "Exception encountered while updating contact: " + e);
        }
    }

    private static void updateContactPhone(ContentResolver contactHelper, String phone, long getContactId) {
        String where = ContactsContract.RawContacts.CONTACT_ID + " = " + getContactId;
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();

        ops.add(ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                .withSelection(where, null)
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phone).build());

        Log.d(TAG, "Creating contact number : " + phone);
        try {
            contactHelper.applyBatch(ContactsContract.AUTHORITY, ops);
        } catch (Exception e) {
            Log.e(TAG, "Exception encountered while updating contact: " + e);
        }
    }



//    public CustomAdapter(ArrayList<Dictionary> list) {
//        this.mList = list;
//    }



    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_list, viewGroup, false);

        CustomViewHolder viewHolder = new CustomViewHolder(view);

        return viewHolder;
    }

    public CustomAdapter(Context context, ArrayList<Dictionary> list) {
        mList = list;
        mContext = context;
    }




    @Override
    public void onBindViewHolder(@NonNull CustomViewHolder viewholder, int position) {

        viewholder.id.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        viewholder.phoneNumber.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        viewholder.name.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);

        viewholder.id.setGravity(Gravity.CENTER);
        viewholder.phoneNumber.setGravity(Gravity.CENTER);
        viewholder.name.setGravity(Gravity.CENTER);

        viewholder.id.setText(mList.get(position).getId());
        viewholder.phoneNumber.setText(mList.get(position).getUser_phNumber());
        viewholder.name.setText(mList.get(position).getUser_Name());
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

}