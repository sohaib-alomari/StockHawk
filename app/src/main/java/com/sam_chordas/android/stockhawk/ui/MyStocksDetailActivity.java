package com.sam_chordas.android.stockhawk.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.db.chart.Tools;
import com.db.chart.model.LineSet;
import com.db.chart.view.AxisController;
import com.db.chart.view.ChartView;
import com.db.chart.view.LineChartView;
import com.sam_chordas.android.stockhawk.R;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;


public class MyStocksDetailActivity extends AppCompatActivity {
    private ArrayList<String> labels;
    private ArrayList<Float> values;
    private boolean isLoaded = false;
    private String stock_Comp;
    private String stock_symbol;
    private LineChartView lineChartView;
    LineSet dataset;
    TextView high,low;
    public TextView NoNetMsg;
    private View loading,grid;

    int newMax,newMin;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line_graph);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        dataset= new LineSet();

        lineChartView = (LineChartView) findViewById(R.id.linechart);
        loading = findViewById(R.id.progressBar);
        grid=findViewById(R.id.grid_desc);
        high=(TextView)findViewById(R.id.high_text);
        low=(TextView)findViewById(R.id.low_text);














        stock_symbol = getIntent().getStringExtra("symbol");
        if (savedInstanceState == null) {
            downloadStockDetails();
        }


    }

    // Save/Restore activity state
    @Override
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        if (isLoaded) {
            bundle.putString("company_name", stock_Comp);
            bundle.putStringArrayList("labels", labels);

            float[] valuesArray = new float[values.size()];
            for (int i = 0; i < valuesArray.length; i++) {
                valuesArray[i] = values.get(i);
            }
            bundle.putFloatArray("values", valuesArray);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null && savedInstanceState.containsKey("company_name")) {
            isLoaded = true;

            stock_Comp = savedInstanceState.getString("company_name");
            labels = savedInstanceState.getStringArrayList("labels");
            values = new ArrayList<>();

            float[] valuesArray = savedInstanceState.getFloatArray("values");
            for (float f : valuesArray) {
                values.add(f);
            }
            onDownloadCompleted();
        }
        super.onRestoreInstanceState(savedInstanceState);
    }




    private void downloadStockDetails() {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("http://chartapi.finance.yahoo.com/instrument/1.0/" + stock_symbol + "/chartdata;type=quote;range=1y/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(Response response) throws IOException {
                if (response.code() == 200) {
                    try {
                        // Trim response string
                        String result = response.body().string();
                        if (result.startsWith("finance_charts_json_callback( ")) {
                            result = result.substring(29, result.length() - 2);
                        }

                        // Parse JSON
                        JSONObject object = new JSONObject(result);
                        stock_Comp = object.getJSONObject("meta").getString("Company-Name");
                        labels = new ArrayList<>();
                        values = new ArrayList<>();
                        JSONArray series = object.getJSONArray("series");
                        for (int i = 0; i < series.length(); i++) {
                            JSONObject seriesItem = series.getJSONObject(i);
                            SimpleDateFormat srcFormat = new SimpleDateFormat("yyyyMMdd");
                            String date = android.text.format.DateFormat.
                                    getMediumDateFormat(getApplicationContext()).
                                    format(srcFormat.parse(seriesItem.getString("Date")));
                            labels.add(date);
                            values.add(Float.parseFloat(seriesItem.getString("close")));
                        }

                        onDownloadCompleted();
                    } catch (Exception e) {
                        onDownloadFailed();
                        e.printStackTrace();
                    }
                } else {
                    onDownloadFailed();
                }
            }


            @Override
            public void onFailure(Request request, IOException e) {
                onDownloadFailed();
            }
        });


    }

    private void onDownloadFailed() {
        MyStocksDetailActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                loading.setVisibility(View.GONE);
                grid.setVisibility(View.GONE);
                lineChartView.setVisibility(View.GONE);

                new AlertDialog.Builder(MyStocksDetailActivity.this)
                        .setTitle("No Internet...!!!")
                        .setMessage("No Internet Connection, Check You Network?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // continue with delete
                                Intent i= new Intent(MyStocksDetailActivity.this,MyStocksActivity.class);
                                startActivity(i);

                            }
                        })

                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        });


    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else {
            return false;
        }
    }


    private void onDownloadCompleted() {
        Log.d("yes"," Working");
        MyStocksDetailActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setTitle(stock_Comp);


               loading.setVisibility(View.GONE);
                grid.setVisibility(View.VISIBLE);
                lineChartView.setVisibility(View.VISIBLE);



                for (int i = 0; i < labels.size(); i+=50) {
                    dataset.addPoint(labels.get(i), values.get(i));
                }

                findMaxMin();
                high.setText(String.valueOf(newMax));
                low.setText(String.valueOf(newMin));
                initLineChart();
                dataset.setColor(getResources().getColor(R.color.material_blue_700))
                        .setDotsStrokeThickness(Tools.fromDpToPx(1))
                        .setDotsColor(Color.WHITE)


                        .setDotsStrokeColor(getResources().getColor(R.color.material_blue_700))
                        ;
                lineChartView.addData(dataset);
                lineChartView.show();


                isLoaded = true;
            }
        });


    }


    private void initLineChart() {

        Paint gridPaint = new Paint();
        gridPaint.setColor(getResources().getColor(R.color.White));
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setAntiAlias(true);
        gridPaint.setStrokeWidth(Tools.fromDpToPx(3));
        lineChartView.setBorderSpacing(-20)
                .setAxisBorderValues(newMin,newMax)


                .setXLabels(AxisController.LabelPosition.OUTSIDE)
                .setYLabels(AxisController.LabelPosition.OUTSIDE)
                .setLabelsColor(getResources().getColor(R.color.White))
                .setXAxis(true)

                .setYAxis(true)
                .setBorderSpacing(Tools.fromDpToPx(0))
                .setGrid(ChartView.GridType.HORIZONTAL, gridPaint);
    }

    public void findMaxMin()
    {

        newMax = Math.round(Collections.max(values));
        newMin = Math.round(Collections.min(values));
    }





}