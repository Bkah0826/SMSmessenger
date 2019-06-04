package nait.bkah1.com.smsmessenger;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Date;
import java.util.jar.Manifest;


public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    ArrayList<String> smsMessagesList = new ArrayList<>();
    ListView messages;
    ArrayAdapter arrayAdapter;
    EditText inputMessage;
    SmsManager smsManager = SmsManager.getDefault();
    Button sendButton;
    private static final int READ_SMS_PERMISSIONS_REQUEST = 1;
    private static final int READ_CONTACTS_PERMISSIONS_REQUEST=1;
    private static MainActivity inst;
    public static  boolean activestatus = false;
    SharedPreferences prefs;
    public static MainActivity instance() {return inst;}
    private static String phone;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sendButton = (Button) findViewById(R.id.send_button);
        messages =(ListView) findViewById(R.id.message_display);
        inputMessage = (EditText) findViewById(R.id.message_input);
        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, smsMessagesList);
        messages.setAdapter(arrayAdapter);

        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED)
        {
            getPermissionToReadContacts();
        }

        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_SMS)!= PackageManager.PERMISSION_GRANTED)
        {
            getReadPermissionSMS();

        }
        else
        {
            refreshSmsInbox();
        }
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);
        phone = prefs.getString("phone_number_value", "" );//Enter phone Number
    }

    @Override
    protected void onStart() {
        super.onStart();
        activestatus = true;
        inst =this;
    }

    public void getReadPermissionSMS()
    {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED);
        {
            if(shouldShowRequestPermissionRationale(android.Manifest.permission.READ_SMS))
            {
                Toast.makeText(this, "Please allow permission", Toast.LENGTH_LONG).show();
            }
            requestPermissions(new String[]{android.Manifest.permission.READ_SMS}, READ_SMS_PERMISSIONS_REQUEST);
        }
    }

    public void getPermissionToReadContacts()
    {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED)
        {
    if(shouldShowRequestPermissionRationale(android.Manifest.permission.READ_CONTACTS))
    {
        Toast.makeText(this, "Please allow me to use your contacts!", Toast.LENGTH_LONG).show();
    }
    requestPermissions(new String[]{android.Manifest.permission.READ_CONTACTS}, READ_CONTACTS_PERMISSIONS_REQUEST);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        if (requestCode == READ_SMS_PERMISSIONS_REQUEST)
        {
            if(grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(this, "Read SMS permission granted", Toast.LENGTH_SHORT).show();
                refreshSmsInbox();
            }
            else
            {
                Toast.makeText(this, "Read SMS permission denied", Toast.LENGTH_SHORT).show();
            }
        }
        else
        {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        }

        if (requestCode == READ_CONTACTS_PERMISSIONS_REQUEST)
        {
            if(grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(this, "Read SMS Contacts permissions granted", Toast.LENGTH_LONG).show();
                refreshSmsInbox();
            }
            else
            {
                Toast.makeText(this, "Read SMS Contacts permissions denied", Toast.LENGTH_LONG).show();
            }
        }
        else
        {
            super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        }
    }
    public void UpdateInboxList (final String smsmessage)
    {
        arrayAdapter.insert(smsmessage,0);
        arrayAdapter.notifyDataSetChanged();
    }
    public void refreshSmsInbox()
    {
        ContentResolver contentResolver = getContentResolver();
        Cursor smsInboxCursor = contentResolver.query(Uri.parse("content://sms/inbox"), null, null, null, null);

        int indexBody = smsInboxCursor.getColumnIndex("body");
        int indexAddress = smsInboxCursor.getColumnIndex("address");
        if (indexBody < 0 || !smsInboxCursor.moveToFirst()) return;
        arrayAdapter.clear();

        do {
                String smsstr = "SMS From: " + getContactName(this, smsInboxCursor.getString(indexAddress))
                        + "\n" + smsInboxCursor.getString(indexBody) + "\n";
                arrayAdapter.add(smsstr);

            }while (smsInboxCursor.moveToNext());


    }

    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = this.getMenuInflater();
        inflater.inflate(R.menu.mainmenu,menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        Intent intent = null;
        switch (item.getItemId())
        {
            case R.id.menuitem_prefs: {
                intent = new Intent(this, prefs.class);
                break;
            }
        }
            startActivity(intent);
            return true;

    }


    public void onSendButtonPressed (View view)
    {
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.SEND_SMS)!= PackageManager.PERMISSION_GRANTED)
        {
            getReadPermissionSMS();

        }
        else
        {
            String text = inputMessage.getText().toString();
               if (text == null || text == "")
               {
                   Toast.makeText(this, "Enter Message", Toast.LENGTH_SHORT).show();

               }
               else
                   {
                   smsManager.sendTextMessage(phone, null, text, null, null);
                   inputMessage.setText("");
                   Toast.makeText(this, "Message sent!", Toast.LENGTH_SHORT).show();
               }
                refreshSmsInbox();


        }
    }

    public static String getContactName(Context context, String phonenumber)
    {
        ContentResolver cr = context.getContentResolver();
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phonenumber));
        Cursor cursor = cr.query(uri ,new  String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);

        if (cursor == null)
        {
            return phonenumber;
        }
        String contactName = phonenumber;
        if (cursor.moveToFirst())
        {
            contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
        }
        if (cursor != null && !cursor.isClosed())
        {
            cursor.close();
        }

    return contactName;


    }
    @Override
    protected void onStop() {
        super.onStop();
        activestatus = false;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
    {

    }
}
