package com.example.delivery_best_c;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.text.InputType;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.example.delivery_best_c.R;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class SignupActivity extends AppCompatActivity {
    private static String IP_ADDRESS = "ALB-was-878316183.ap-northeast-2.elb.amazonaws.com/Client/Signup.php";
    private static String TAG = "phpsignup";

    private EditText mEditTextID;
    private EditText mEditTextPassword;
    private EditText mEditTextAddress;
    private EditText mEditTextPhone;
    private TextView mTextViewResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mEditTextID = (EditText)findViewById(R.id.et_id);
        mEditTextPassword = (EditText)findViewById(R.id.et_pass);
        mEditTextPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        mEditTextAddress = (EditText)findViewById(R.id.et_userAddress);
        mEditTextPhone = (EditText)findViewById(R.id.et_phone);
        mTextViewResult = (TextView)findViewById(R.id.textView_result);
        mTextViewResult.setMovementMethod(new ScrollingMovementMethod());

        Button buttonInsert = (Button)findViewById(R.id.btn_register);
        buttonInsert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ID = mEditTextID.getText().toString();
                String Password = mEditTextPassword.getText().toString();
                String Address = mEditTextAddress.getText().toString();
                String Phone = mEditTextPhone.getText().toString();

                InsertData task = new InsertData();
                task.execute("http://" + IP_ADDRESS + "/insert.php", ID, Password, Address, Phone);

                mEditTextID.setText("");
                mEditTextPassword.setText("");
                mEditTextAddress.setText("");
                mEditTextPhone.setText("");
            }
        });
    }

    class InsertData extends AsyncTask<String, Void, String> {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(SignupActivity.this, "Please Wait", null, true, true);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            progressDialog.dismiss();
            Log.d(TAG, "POST response - " + result);

            if ("DUPLICATE_ID".equals(result.trim())) {
                showDuplicateIDDialog();
            } else if ("SUCCESS".equals(result.trim())) {
                showSuccessDialog();
            } else {
                mTextViewResult.setText(result);
            }
        }

        @Override
        protected String doInBackground(String... params) {
            String userID = (String)params[1];
            String userPassword = (String)params[2];
            String userAddress = (String)params[3];
            String phoneNumber = (String)params[4];

            String serverURL = (String)params[0];
            String postParameters = "userID=" + userID + "&userPassword=" + userPassword + "&userAddress=" + userAddress + "&phoneNumber=" + phoneNumber;

            try {
                URL url = new URL(serverURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.connect();

                OutputStream outputStream = httpURLConnection.getOutputStream();
                outputStream.write(postParameters.getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();

                int responseStatusCode = httpURLConnection.getResponseCode();
                Log.d(TAG, "POST response code - " + responseStatusCode);

                InputStream inputStream;
                if(responseStatusCode == HttpURLConnection.HTTP_OK) {
                    inputStream = httpURLConnection.getInputStream();
                } else {
                    inputStream = httpURLConnection.getErrorStream();
                }

                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuilder sb = new StringBuilder();
                String line;
                while((line = bufferedReader.readLine()) != null){
                    sb.append(line);
                }

                bufferedReader.close();
                return sb.toString();

            } catch (Exception e) {
                Log.d(TAG, "InsertData: Error ", e);
                return new String("Error: " + e.getMessage());
            }
        }
    }

    private void showSuccessDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("회원가입 성공")
                .setMessage("회원가입이 성공적으로 완료되었습니다.")
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                        startActivity(intent);
                    }
                })
                .show();
    }

    private void showDuplicateIDDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(SignupActivity.this);
        builder.setTitle("회원가입 실패")
                .setMessage("중복된 ID가 있습니다.\n다른 ID를 사용해주세요.")
                .setPositiveButton("확인", null)
                .show();
    }
}