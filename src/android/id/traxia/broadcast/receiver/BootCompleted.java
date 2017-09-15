package id.traxia.broadcast.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import id.traxia.backgroundservice.TRAXIASmartLocation;

/**
 * Created by perdi on 9/7/2017.
 */

public class BootCompleted extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent){
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())){
            context.startService(new Intent(context, TRAXIASmartLocation.class));
        }
    }
}
