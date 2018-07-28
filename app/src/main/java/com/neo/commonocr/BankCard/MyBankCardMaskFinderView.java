package com.neo.commonocr.BankCard;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;

import com.hex.mocr.ui.MaskFinderView;

/**
 * Created by hex on 16/1/23.
 */
public class MyBankCardMaskFinderView extends MaskFinderView {
    public MyBankCardMaskFinderView(Context context) {
        super(context);
    }

    public MyBankCardMaskFinderView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyBankCardMaskFinderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void init() {

        /**
         * 4个角点线的颜色
         */
        this.colorCorner = Color.BLUE;

        /**
         * 找到卡片边框时绘制颜色
         */
        this.colorFoundBorder = 0xa000ff00;

        /**
         * 边框默认颜色
         */
        this.colorBorder = 0x80ffffff;

        /**
         * 透明遮罩颜色
         */
        this.colorMask = 0x80000000;

        /**
         * 线宽
         */
        this.lineStrokeWidth=10f;


        //提示文本
        this.tips="请将银行卡正面置于此区域，并对齐扫描框边缘";

        /**
         * 文本字体大小
         */
        this.tipsTextSize =40;


        /**
         * 角点线长度（单位为预览高度的百分比）
         */
        this.cornerLength =0.1f;


        super.init();

        /**
         * 提示文本颜色
         */
        this.getPaintText().setColor(Color.WHITE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }


    /**
     * 画透明遮罩
     * @param canvas
     */
    @Override
    protected void drawMask(Canvas canvas) {
        super.drawMask(canvas);
    }

    /**
     * 画4个角点线
     * @param canvas
     */
    @Override
    protected void drawCorner(Canvas canvas) {
        super.drawCorner(canvas);
    }

    /**
     * 画边框
     * @param canvas
     */
    @Override
    protected void drawBorder(Canvas canvas) {
        //super.drawBorder(canvas);
        canvas.drawRect(maskRect, this.paintBorderLine);

        int l = maskRect.left;
        int t = maskRect.top;
        int r = maskRect.right;
        int b = maskRect.bottom;

        if (this.leftBorderLine) {
            canvas.drawLine(l, t, l, b, this.paintFoundBorderLine);
        }
        if (this.rightBorderLine) {
            canvas.drawLine(r, t, r, b, this.paintFoundBorderLine);
        }
        if (this.topBorderLine) {
            canvas.drawLine(l, t, r, t, this.paintFoundBorderLine);
        }
        if (this.bottomBorderLine) {
            canvas.drawLine(l, b, r, b, this.paintFoundBorderLine);
        }
    }

    /**
     * 画提示文本
     * @param canvas
     */
    @Override
    protected void drawTextTips(Canvas canvas) {
        //super.drawTextTips(canvas);
        if (null == tips || tips.length()==0) {
            return;
        }

        Paint.FontMetrics fontMetrics = paintText.getFontMetrics();
        //计算文字高度
        float fontHeight = fontMetrics.bottom - fontMetrics.top;

        //计算文字baseline
        float textBaseY = maskRect.bottom + fontHeight/2;
        if (textBaseY < this.getHeight()){
            textBaseY += (this.getHeight()-textBaseY)/2;
        }

        canvas.drawText(tips, maskRect.centerX(), textBaseY, this.paintText);
    }
}
