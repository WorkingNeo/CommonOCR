package com.neo.commonocr.BankCard;

import android.content.Context;
import android.graphics.Rect;
import android.hardware.Camera;
import android.util.AttributeSet;

import com.hex.mocr.ui.MaskFinderView;
import com.hex.mocr.ui.bankcard.BankCardCaptureView;

import java.util.List;

/**
 * 自定义银行卡识别界面
 */
public class MyBankCardCaptureView extends BankCardCaptureView {
    public MyBankCardCaptureView(Context context) {
        super(context);
    }

    public MyBankCardCaptureView(Context context, AttributeSet set) {
        super(context, set);
    }

    public MyBankCardCaptureView(Context context, AttributeSet set, int defStyle) {
        super(context, set, defStyle);
    }

    @Override
    protected MaskFinderView initMaskFinderView(Context context) {

        //使用自定义识别框显示视图
        return new MyBankCardMaskFinderView(context);
    }


    /**
     * 设置摄像头参数 (如不设置，则由系统自动决定预览大小）
     * @param camera
     */
    @Override
    protected void onInitCamera(Camera camera) {
        List<Camera.Size> previewSizes = camera.getParameters().getSupportedPreviewSizes();
        Camera.Size size=previewSizes.get(0);

        float sw= this.getHeight()*1.0f/this.getWidth();

        for (Camera.Size previewSize : previewSizes) {
            if (previewSize.height>=720){
                float preSW= (float)previewSize.height / previewSize.width;
                if (Math.abs(preSW-sw)<0.1){
                    size=previewSize;
                    break;
                }
            }
        }

        camera.getParameters().setPreviewSize(size.width,size.height);
        super.onInitCamera(camera);
    }

    /**
     *  获得OCR识别范围
     * @param previewWidth 当前摄像头预览的宽度
     * @param previewHeight  当前摄像头预览的高度
     * @return
     */
    @Override
    protected Rect getOcrRect(int previewWidth, int previewHeight) {
        //使用父类处理逻辑：
        // top=previewHeight-getOcrMarginTopBottom ,  bottom=previewHeight-top
        // 根据银行卡高宽比例，计算出cardWidth=  ((bottom-top) * 1.58577D)
        // left = (previewWidth-cardWidth)/2 , right= previewWidth-left
        return super.getOcrRect(previewWidth, previewHeight);
    }

    /**
     * 获得OCR识别范围顶部预留的尺寸
     * @param previewWidth 当前摄像头预览的宽度
     * @param previewHeight  当前摄像头预览的高度
     * @return
     */
    @Override
    protected int getOcrMarginTopBottom(int previewWidth, int previewHeight) {
        //return super.getOcrMarginTopBottom(previewWidth, previewHeight);
        // 16% of previewHeight
        return (int) (previewHeight * 16 / 100.0);
    }
}
