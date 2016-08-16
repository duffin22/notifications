package ly.generalassemb.drewmahrt.stockpriceclient;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by drewmahrt on 3/7/16.
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter{
    ContentResolver mResolver;

    private static final String AUTHORITY = "drewmahrt.generalassemb.ly.investingportfolio.MyContentProvider";
    private static final String STOCKS_TABLE = "stocks";
    public static final Uri SYMBOLS_CONTENT_URI = Uri.parse("content://"
            + AUTHORITY + "/" + STOCKS_TABLE + "/symbols");

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mResolver = context.getContentResolver();
    }

    public SyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        mResolver = context.getContentResolver();
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d(SyncAdapter.class.getName(),"Starting sync");
        getPortfolioStocks();
        getNYSEStocks("C");

    }

    private void getPortfolioStocks(){
        Cursor cursor = mResolver.query(SYMBOLS_CONTENT_URI,null,"exchange = 'NYSE'",null,null);
        while(cursor != null && cursor.moveToNext()) {
            updateStockInfo(cursor.getString(0),true);
        }
    }

    private void getNYSEStocks(String input){
        RequestQueue queue = Volley.newRequestQueue(getContext());
        String stockUrl = "http://dev.markitondemand.com/MODApis/Api/v2/Lookup/json?input="+input;

        JsonArrayRequest nasdaqStockRequest = new JsonArrayRequest
                (stockUrl, new Response.Listener<JSONArray>() {

                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject object = (JSONObject)response.get(i);
                                if(object.getString("Exchange").equals("NYSE"))
                                    updateStockInfo(object.getString("Symbol"),false);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub

                    }
                });

        queue.add(nasdaqStockRequest);
    }

    public void updateStockInfo(final String symbol, final boolean isPortfolio){
        RequestQueue queue = Volley.newRequestQueue(getContext());
        String stockUrl = "http://dev.markitondemand.com/MODApis/Api/v2/Quote/json?symbol="+symbol;

        JsonObjectRequest stockJsonRequest = new JsonObjectRequest
                (Request.Method.GET, stockUrl, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Cursor checkCursor = mResolver.query(StockPriceContentProvider.CONTENT_URI,null,"stock_symbol='"+symbol+"'",null,null);
                        Log.d(SyncAdapter.class.getName(),response.toString());
                        try {
                            if (checkCursor.moveToFirst()) {
                                //Stock already exists in database, so an update is needed
                                ContentValues contentValues = new ContentValues();
                                contentValues.put("price", response.getString("LastPrice"));
                                if(isPortfolio){
                                    contentValues.put("portfolio",true);
                                }else {
                                    contentValues.put("portfolio",false);
                                }

                                mResolver.update(StockPriceContentProvider.CONTENT_URI, contentValues, "stock_symbol='" + symbol + "'", null);
                            } else {
                                //Stock is new, so it needs to be inserted into database
                                ContentValues contentValues = new ContentValues();
                                contentValues.put(StockDBHelper.COLUMN_STOCK_PRICE, response.getString("LastPrice"));
                                contentValues.put(StockDBHelper.COLUMN_STOCK_NAME,response.getString("Name"));
                                contentValues.put(StockDBHelper.COLUMN_STOCK_SYMBOL,symbol);

                                if(isPortfolio){
                                    contentValues.put("portfolio",true);
                                }else {
                                    contentValues.put("portfolio",false);
                                }

                                if(!response.getString("LastPrice").equals("0")) {
                                    Uri uri = mResolver.insert(StockPriceContentProvider.CONTENT_URI, contentValues);
                                    Log.d(MainActivity.class.getName(), "Inserted at: " + uri);
                                }
                            }
                        }catch (Exception e){

                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub

                    }
                });

        queue.add(stockJsonRequest);
    }
}


