package com.example.delivery_best_c;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.Button;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ListView shopListView;
    ArrayAdapter<String> shopListAdapter;
    ArrayList<String> shopList = new ArrayList<>();
    private static final String TAG = "MainActivity";
    private static final String TAG_SHOP = "shop";
    private static final String TAG_NAME = "shopName";
    private static final String TAG_ADDRESS = "shopAddress";
    private static final String TAG_FOOD = "food";
    private static final String TAG_PRICE = "price";

    String userID, userAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(getIntent().getStringExtra("loginID") == null) {
            Log.d(TAG, "loginID is null");
        }
        if(getIntent().getStringExtra("loginAddress") == null) {
            Log.d(TAG, "loginAddress is null");
        }

        Log.d(TAG, "Received loginID: " + getIntent().getStringExtra("loginID"));
        Log.d(TAG, "Received loginAddress: " + getIntent().getStringExtra("loginAddress"));

        SharedPreferences sharedPreferences = getSharedPreferences("loginInfo", MODE_PRIVATE);
        String retrievedAddress = sharedPreferences.getString("address", "defaultAddress");
        Log.d(TAG, "Retrieved from SharedPreferences address: " + retrievedAddress);

        shopListView = findViewById(R.id.shop_list_view);
        shopListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, shopList);
        shopListView.setAdapter(shopListAdapter);

        // 로그인 액티비티에서 전달받은 userID와 userAddress를 가져옴
        userID = getIntent().getStringExtra("loginID");
        userAddress = getIntent().getStringExtra("loginAddress");

        shopListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showOrderConfirmationDialog(position);
            }
        });

        GetShopData getShopData = new GetShopData();
        getShopData.execute();

        Button logoutButton = findViewById(R.id.btnLogout);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = getSharedPreferences("loginInfo", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.clear();
                editor.apply();

                Toast.makeText(MainActivity.this, "로그아웃 되었습니다", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private class GetShopData extends AsyncTask<Void, Void, String> {
        String serverURL = "http://ALB-was-878316183.ap-northeast-2.elb.amazonaws.com/Client/GetShops.php";

        @Override
        protected String doInBackground(Void... voids) {
            try {
                URL url = new URL(serverURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.connect();

                int responseStatusCode = httpURLConnection.getResponseCode();
                Log.d(TAG, "GET response code - " + responseStatusCode);
                InputStream inputStream;

                if (responseStatusCode == HttpURLConnection.HTTP_OK) {
                    inputStream = httpURLConnection.getInputStream();
                } else {
                    inputStream = httpURLConnection.getErrorStream();
                }

                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                StringBuilder sb = new StringBuilder();
                String line;

                while ((line = bufferedReader.readLine()) != null) {
                    sb.append(line);
                }

                bufferedReader.close();
                return sb.toString().trim();
            } catch (Exception e) {
                Log.d(TAG, "GetData: Error ", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result == null || result.isEmpty()) {
                Log.d(TAG, "No result received from server.");
                return;
            }

            try {
                JSONObject jsonObject = new JSONObject(result);
                JSONArray jsonArray = jsonObject.getJSONArray(TAG_SHOP);

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject item = jsonArray.getJSONObject(i);
                    String shopName = item.getString(TAG_NAME);
                    String shopAddress = item.getString(TAG_ADDRESS);
                    String food = item.getString(TAG_FOOD);
                    String price = item.getString(TAG_PRICE);
                    shopList.add(shopName + " - " + shopAddress + " - " + food + " - " + price);
                }

                shopListAdapter.notifyDataSetChanged();

            } catch (JSONException e) {
                Log.d(TAG, "showResult: ", e);
            }
        }
    }

    private void showOrderConfirmationDialog(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("주문 확인");
        builder.setMessage("주문하시겠습니까?");
        builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String selectedShop = shopList.get(position);
                String[] shopDetails = selectedShop.split(" - ");
                saveOrderToServer(userID, userAddress, shopDetails[0], shopDetails[1]); // 선택된 가게의 이름과 주소를 전달
            }
        });
        builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void orderShop(int position) {
        String selectedShop = shopList.get(position);
        // 선택된 가게와 관련된 추가 처리를 여기에서 할 수 있습니다.
    }

    private void saveOrderToServer(String userID, String userAddress, String shopName, String shopAddress) {
        class SaveOrderTask extends AsyncTask<Void, Void, String> {
            String serverURL = "http://ALB-was-878316183.ap-northeast-2.elb.amazonaws.com/Client/saveOrder.php";

            @Override
            protected String doInBackground(Void... voids) {
                try {
                    URL url = new URL(serverURL);
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.setDoOutput(true);
                    httpURLConnection.setDoInput(true);

                    OutputStream os = httpURLConnection.getOutputStream();
                    BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                    String data = URLEncoder.encode("userID", "UTF-8") + "=" + URLEncoder.encode(userID, "UTF-8") +
                            "&" + URLEncoder.encode("userAddress", "UTF-8") + "=" + URLEncoder.encode(userAddress, "UTF-8") +
                            "&" + URLEncoder.encode("shopName", "UTF-8") + "=" + URLEncoder.encode(shopName, "UTF-8") +
                            "&" + URLEncoder.encode("shopAddress", "UTF-8") + "=" + URLEncoder.encode(shopAddress, "UTF-8");

                    bufferedWriter.write(data);
                    bufferedWriter.flush();
                    bufferedWriter.close();

                    InputStream is = httpURLConnection.getInputStream();
                    is.close();
                    return "주문 성공";
                } catch (Exception e) {
                    return e.getMessage();
                }
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
            }
        }

        SaveOrderTask saveOrderTask = new SaveOrderTask();
        saveOrderTask.execute();
    }
}