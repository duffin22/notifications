package ly.generalassemb.drewmahrt.stockpriceclient;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.google.gson.Gson;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private CursorAdapter mCursorAdapter;

    private TextView mUpdatedTextView;

    public static final String AUTHORITY = "ly.generalassemb.drewmahrt.stockpriceclient.StockPriceContentProvider";

    // Account type
    public static final String ACCOUNT_TYPE = "example.com";
    // Account
    public static final String ACCOUNT = "default_account";

    private Account mAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAccount = createSyncAccount(this);

        getContentResolver().registerContentObserver(StockPriceContentProvider.CONTENT_URI,true,new StockContentObserver(new Handler()));

        Cursor existingStocksCursor = getContentResolver().query(StockPriceContentProvider.CONTENT_URI,null,null,null,"portfolio DESC");

        //cursorAdapter = new SimpleCursorAdapter(this,android.R.layout.simple_list_item_2,existingStocksCursor,new String[]{StockDBHelper.COLUMN_STOCK_SYMBOL,StockDBHelper.COLUMN_STOCK_PRICE},new int[]{android.R.id.text1,android.R.id.text2},0);
        mCursorAdapter = new CursorAdapter(this,existingStocksCursor,0) {
            @Override
            public View newView(Context context, Cursor cursor, ViewGroup parent) {
                return LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_2,parent,false);
            }

            @Override
            public void bindView(View view, Context context, Cursor cursor) {
                TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                TextView text2 = (TextView) view.findViewById(android.R.id.text2);

                if(cursor.getString(cursor.getColumnIndex("portfolio")).equals("1"))
                    view.setBackgroundColor(Color.GREEN);
                else
                    view.setBackgroundResource(android.R.color.background_light);

                String name = cursor.getString(cursor.getColumnIndex(StockDBHelper.COLUMN_STOCK_NAME));
                String symbol = cursor.getString(cursor.getColumnIndex(StockDBHelper.COLUMN_STOCK_SYMBOL));
                String price = cursor.getString(cursor.getColumnIndex(StockDBHelper.COLUMN_STOCK_PRICE));

                text1.setText(name+" ("+symbol+")");
                text2.setText("$"+price);
            }
        };


        ListView listView = (ListView) findViewById(R.id.stock_price_list);
        listView.setAdapter(mCursorAdapter);


        mUpdatedTextView = (TextView) findViewById(R.id.updated_text);

//        StockAsyncTask newTask = new StockAsyncTask();
//        newTask.execute("GOOG","IBM","AAPL");

        Bundle settingsBundle = new Bundle();
        settingsBundle.putBoolean(
                ContentResolver.SYNC_EXTRAS_MANUAL, true);
        settingsBundle.putBoolean(
                ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
                /*
                 * Request the sync for the default account, authority, and
                 * manual sync settings
                 */
        ContentResolver.requestSync(mAccount, AUTHORITY, settingsBundle);

        ContentResolver.setSyncAutomatically(mAccount,AUTHORITY,true);
        ContentResolver.addPeriodicSync(
                mAccount,
                AUTHORITY,
                Bundle.EMPTY,
                60);
    }

    public class StockContentObserver extends ContentObserver {

        /**
         * Creates a content observer.
         *
         * @param handler The handler to run {@link #onChange} on, or null if none.
         */
        public StockContentObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            //do stuff on UI thread
            Log.d(MainActivity.class.getName(),"Changed observed at "+uri);

            mCursorAdapter.swapCursor(getContentResolver().query(StockPriceContentProvider.CONTENT_URI, null, null, null, "portfolio DESC"));

            String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
            mUpdatedTextView.setText("Last updated: "+currentDateTimeString);
        }
    }

    private void addStockBySymbol(String symbol){
        String data ="";
        try {
            URL url = new URL("http://dev.markitondemand.com/MODApis/Api/v2/Quote/json?symbol="+symbol);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.connect();
            InputStream inStream = connection.getInputStream();
            data = getInputData(inStream);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        Gson gson = new Gson();
        Stock stock = gson.fromJson(data,Stock.class);

        ContentValues values = new ContentValues();
        values.put(StockDBHelper.COLUMN_STOCK_NAME,stock.getName());
        values.put(StockDBHelper.COLUMN_STOCK_SYMBOL,stock.getSymbol());
        values.put(StockDBHelper.COLUMN_STOCK_PRICE, stock.getLastPrice());

        Uri uri = getContentResolver().insert(StockPriceContentProvider.CONTENT_URI,values);
        Log.d(MainActivity.class.getName(),"Inserted at: "+uri);
    }

    private String getInputData(InputStream inStream) throws IOException {
        StringBuilder builder = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inStream));

        String data = null;

        while ((data = reader.readLine()) != null){
            builder.append(data);
        }

        reader.close();

        return builder.toString();
    }

    private class StockAsyncTask extends AsyncTask<String,Void,Void>{

        @Override
        protected Void doInBackground(String... params) {
            for (int i = 0; i < params.length; i++) {
                addStockBySymbol(params[i]);
            }
            return null;
        }
    }

    public static Account createSyncAccount(Context context) {
        // Create the account type and default account
        Account newAccount = new Account(
                ACCOUNT, ACCOUNT_TYPE);
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(
                        ACCOUNT_SERVICE);
        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
        if (accountManager.addAccountExplicitly(newAccount, null, null)) {
          /*
           * If you don't set android:syncable="true" in
           * in your <provider> element in the manifest,
           * then call context.setIsSyncable(account, AUTHORITY, 1)
           * here.
           */
        } else {
            /*
             * The account exists or some other error occurred. Log this, report it,
             * or handle it internally.
             */
        }
        return newAccount;
    }
}
