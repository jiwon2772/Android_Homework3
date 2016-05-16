package com.example.jayden.hw3;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    String numberString;
    WebView browser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        browser = (WebView) findViewById(R.id.webView);
        browser.getSettings().setJavaScriptEnabled(true); // allow scripts
        browser.setWebViewClient(new WebViewClient());  // for using this webview as a browser insted of defalt browser

        browser.addJavascriptInterface(new JavaScriptInterface(this), "Android");

        browser.loadUrl("file:///android_asset/messageSender.html"); // point 'messageSender.html'

        numberString = ""; // initialize numberString
    }

    public class JavaScriptInterface {
        Context mContext;

        /**
         * Instantiate the interface and set the context
         */
        JavaScriptInterface(Context c) {
            mContext = c;
        }

        /**
         * Show a toast from the web page
         */
        @JavascriptInterface
        public void showToast(String toastMsg) {
            Toast.makeText(mContext, toastMsg, Toast.LENGTH_SHORT).show();
        }

        @JavascriptInterface
        public String getNumberFromWeb(String number) {
            numberString = numberString + number;    //java script로 부터 입력된 번호를 java에서 변수로 관리하고 기존에 입력된 String에 더해준다.
            return numberString;                      //다시 html파일로 전송
        }

        @JavascriptInterface
        public void sendMessageToNumber(String num, String msg) {
            PendingIntent sentIntent = PendingIntent.getBroadcast(mContext, 0, new Intent("SMS_SENT_ACTION"), 0);
            PendingIntent deliveredIntent = PendingIntent.getBroadcast(mContext, 0, new Intent("SMS_DELIVERED_ACTION"), 0);

            if(num.length() > 12 ) {     // 입력된 번호를 확인한 후 12자리가 넘으면 토스트를 띄워준다.
                Toast.makeText(mContext, "잘못된 번호입니다. (12자리가 넘는 번호)", Toast.LENGTH_SHORT).show();
                return;
            }

            registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    switch (getResultCode()) {
                        case Activity.RESULT_OK:
                            // 전송 성공
                            Toast.makeText(mContext, "전송 완료", Toast.LENGTH_SHORT).show();
                            break;
                        case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                            // 전송 실패
                            Toast.makeText(mContext, "전송 실패", Toast.LENGTH_SHORT).show();
                            break;
                        case SmsManager.RESULT_ERROR_NO_SERVICE:
                            // 서비스 지역 아님
                            Toast.makeText(mContext, "서비스 지역이 아닙니다", Toast.LENGTH_SHORT).show();
                            break;
                        case SmsManager.RESULT_ERROR_RADIO_OFF:
                            // 무선 꺼짐
                            Toast.makeText(mContext, "무선(Radio)가 꺼져있습니다", Toast.LENGTH_SHORT).show();
                            break;
                        case SmsManager.RESULT_ERROR_NULL_PDU:
                            // PDU 실패
                            Toast.makeText(mContext, "PDU Null", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            }, new IntentFilter("SMS_SENT_ACTION"));

            registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    switch (getResultCode()) {
                        case Activity.RESULT_OK:
                            // 도착 완료
                            Toast.makeText(mContext, "SMS 도착 완료", Toast.LENGTH_SHORT).show();
                            break;
                        case Activity.RESULT_CANCELED:
                            // 도착 안됨
                            Toast.makeText(mContext, "SMS 도착 실패", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            }, new IntentFilter("SMS_DELIVERED_ACTION"));

            SmsManager mSmsManager = SmsManager.getDefault();
            mSmsManager.sendTextMessage(num, null, msg, sentIntent, deliveredIntent);

            numberString = "";  //입력 번호 초기화
        }
    }
}
