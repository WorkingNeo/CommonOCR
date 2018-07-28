package com.neo.commonocr.BankCard;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.hex.mocr.ui.bankcard.BankCardCaptureView;
import com.hex.mocr.ui.bankcard.BankCardFoundEvent;
import com.hex.mocr.ui.bankcard.BankCardFoundListener;
import com.neo.commonocr.R;

public class BankCardActivity extends Activity {

    private BankCardCaptureView bankCardCaptureView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_bank_card);

        bankCardCaptureView=(BankCardCaptureView)findViewById(R.id.captureView);
        bankCardCaptureView.addBankCardFoundEventListener(new BankCardFoundListener() {
            @Override
            public void onBankCardFound(BankCardFoundEvent event) {
                onReadBankCardEnd(event);
            }
        });
    }


    private void onReadBankCardEnd( BankCardFoundEvent evt){
        bankCardCaptureView.stopReadBankCard();
        Intent data=new Intent();


        data.putExtra("ocrResult", evt.getOcrResult());
        data.putExtra("image", evt.getResultImage());
        data.putExtra("cardImageData", evt.getCardImageData());

        setResult(RESULT_OK, data);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        bankCardCaptureView.startReadBankCard();
    }

    @Override
    protected void onStop() {
        super.onStop();
        bankCardCaptureView.stopReadBankCard();
    }
}
