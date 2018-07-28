package com.neo.commonocr;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;

import com.hex.mocr.FormType;
import com.hex.mocr.ui.idcard.IdCardCaptureView;
import com.hex.mocr.ui.idcard.IdCardFoundEvent;
import com.hex.mocr.ui.idcard.IdCardFoundListener;

public class IdCardActivity extends Activity {

    private IdCardCaptureView idCardCaptureView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_id_card);

        idCardCaptureView = (IdCardCaptureView) findViewById(R.id.idcardCaptureView);

        //     idCardCaptureView.setMaskView(null);

        idCardCaptureView.setShowCampImageButton(false);

        //添加识别完成事件
        String pkName = this.getPackageName();
        idCardCaptureView.addIdCardFoundEventListener(new IdCardFoundListener() {
            @Override
            public void onIdCardFound(IdCardFoundEvent event) {
                onReadIdCardEnd(event);
            }
        });

        Intent intent = getIntent();
        int cardType = intent.getIntExtra("CardType",0);
        if(cardType == FormType.IdCard2_Back.ordinal()){
            idCardCaptureView.setTips("请将身份证反面放在识别框内");
            //设置识别证件类型
            idCardCaptureView.setFormType(FormType.IdCard2_Back);//FormType.IdCard2_Back);
        } else{
            idCardCaptureView.setTips("请将身份证正面放在识别框内");
            idCardCaptureView.setFormType(FormType.IdCard2_Front);//FormType.IdCard2_Back);
        }
    }



    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) { //按下的如果是BACK，同时没有重复
            if(!idCardCaptureView.canBack())
            {
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private void onReadIdCardEnd(IdCardFoundEvent evt){
        Log.e("SSSSSSS","==222222222====IdCardFoundEvent====");
        if (evt.getOcrResult().getFormType()== FormType.IdCard2_Front){
            String name=(String)evt.getOcrResult().getCardInfo().get("姓名");
            if (null == name || name.isEmpty()){
                evt.setContinueScan(true);
                return;
            }
        }



        Intent data=new Intent();
        data.putExtra("ocrResult", evt.getOcrResult());
        data.putExtra("imageFile",evt.getResultImage());
        data.putExtra("imageData",evt.getCardImageData());

        setResult(RESULT_OK, data);
        finish();
    }
}
