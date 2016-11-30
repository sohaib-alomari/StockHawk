package com.sam_chordas.android.stockhawk.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.gcm.TaskParams;
import com.sam_chordas.android.stockhawk.R;

/**
 * Created by sam_chordas on 10/1/15.
 */
public class StockIntentService extends IntentService {




  public StockIntentService(){
    super(StockIntentService.class.getName());
  }

  public StockIntentService(String name) {
    super(name);
  }

  @Override protected void onHandleIntent(Intent intent) {
    Log.d(StockIntentService.class.getSimpleName(), getString(R.string.Stock_Intent));
    StockTaskService stockTaskService = new StockTaskService(this);
    Bundle args = new Bundle();
    if (intent.getStringExtra(getString(R.string.Tag)).equals(getString(R.string.ADD))){
      args.putString(getString(R.string.Symbol), intent.getStringExtra(getString(R.string.Symbol)));
    }
    // We can call OnRunTask from the intent service to force it to run immediately instead of
    // scheduling a task.
    try {
      stockTaskService.onRunTask(new TaskParams(intent.getStringExtra(getString(R.string.Tag)), args));
    }
    catch(Exception e)
    {

     Log.v(getString(R.string.Why),e.getMessage());
      //Used hander because this is not the UI class
      e.getMessage();
      new Handler(Looper.getMainLooper()).post(new Runnable() {
        @Override
        public void run() {
         // Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
          Toast.makeText(getApplicationContext(), R.string.Wrong_Symbol, Toast.LENGTH_LONG).show();Toast.makeText(getApplicationContext(), R.string.In_Try, Toast.LENGTH_LONG).show();


        }
      });

    }
  }
}
