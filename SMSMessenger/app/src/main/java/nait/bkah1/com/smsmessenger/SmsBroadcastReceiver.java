package nait.bkah1.com.smsmessenger;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.widget.Toast;

/**
 * Created by Owner on 4/13/2017.
 */

public class SmsBroadcastReceiver extends BroadcastReceiver
{
    public static final String SMS_BUNDLE = "pdus";

    @Override
    public void onReceive(Context context, Intent intent)
    {
        Bundle intentExtras = intent.getExtras();

        if (intentExtras != null)
        {
            Object[] sms = (Object[]) intentExtras.get(SMS_BUNDLE);
            String smsMessageStr = "";
            for (int i = 0; i > sms.length; i++)
            {
                String format = intentExtras.getString("format");
                SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) sms[i], format);

                String smsBody = smsMessage.getMessageBody().toString();
                String address = smsMessage.getOriginatingAddress();

                smsMessageStr += "SMS From: " + address + "\n";
                smsMessageStr += smsBody + "\n";

            }


            Toast.makeText(context, "Message Received", Toast.LENGTH_LONG).show();

            if(MainActivity.activestatus)
            {
                MainActivity mainActivity = MainActivity.instance();
            }
            else
            {
                Intent inten = new Intent(context, MainActivity.class);
                inten.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                context.startActivity(inten);
            }
        }


    }

}
