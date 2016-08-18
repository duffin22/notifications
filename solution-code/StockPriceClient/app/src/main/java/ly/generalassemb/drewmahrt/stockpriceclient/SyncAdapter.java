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
//        getNYSEStocks("C");

    }

    private void getPortfolioStocks(){
        StockDBHelper helpy = new StockDBHelper(getContext(), null, null, 1);
        helpy.dropAllTables();

//        Cursor cursor = mResolver.query(SYMBOLS_CONTENT_URI,null,"exchange = 'NYSE'",null,null);
        Cursor cursor = mResolver.query(SYMBOLS_CONTENT_URI,null,null,null,null);
//        while(cursor != null && cursor.moveToNext()) {
//            updateStockInfo(cursor.getString(0),true);
//        }
        updateStockInfo("",true);
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
//        String stockUrl = "http://dev.markitondemand.com/MODApis/Api/v2/Quote/json?symbol="+symbol;

        String redditUrl = "https://www.reddit.com/r/random/.json";

        JsonObjectRequest redditJsonRequest = new JsonObjectRequest
                (Request.Method.GET, redditUrl, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
//                        Cursor checkCursor = mResolver.query(StockPriceContentProvider.CONTENT_URI,null,"stock_symbol='"+symbol+"'",null,null);
//                        Log.d(SyncAdapter.class.getName() + "MATT",response.toString());
                        try {
                            JSONObject data = response.getJSONObject("data");
                            JSONArray children = data.getJSONArray("children");
                            for (int i = 0;i < children.length();i++) {
                                JSONObject listingOne = (JSONObject) children.get(i);
                                JSONObject listingOneData = listingOne.getJSONObject("data");
                                String title = listingOneData.get("title").toString();
                                boolean over18 = (boolean) listingOneData.get("over_18");
                                String subreddit = listingOneData.get("subreddit").toString();
                                int score = (int) listingOneData.get("score");

                                RedditListing post = new RedditListing(subreddit, title, score, over18);

                                Log.d(SyncAdapter.class.getName() + "MATT-TITLE", "" + over18 + ", " + score);

                                ContentValues contentValues = new ContentValues();
                                contentValues.put(StockDBHelper.COLUMN_STOCK_PRICE, "Score: " + score);
                                contentValues.put(StockDBHelper.COLUMN_STOCK_NAME, title);
                                contentValues.put(StockDBHelper.COLUMN_STOCK_SYMBOL, subreddit);
                                contentValues.put("portfolio", true);
                                Uri uri = mResolver.insert(StockPriceContentProvider.CONTENT_URI, contentValues);

                                Log.d(MainActivity.class.getName(), "Inserted at: " + uri);
                            }

                        }catch (Exception e){
                            Log.d(SyncAdapter.class.getName() + "MATT-TITLE","Caught an exception");
                        }
//                        checkCursor.close();
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });
        queue.add(redditJsonRequest);
    }
}


