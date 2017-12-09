package com.nercms.receive;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.nio.ByteBuffer;

public class Videoplay extends View
{
//	public int width = 500;
//	public int height = 500;
	public int width = 352;
	public int height = 288;
	public byte [] mPixel = new byte [width*height*2];
	public ByteBuffer buffer = ByteBuffer.wrap( mPixel );
	public Bitmap VideoBit = Bitmap.createBitmap(width, height, Config.RGB_565);
	public Videoplay(Context context, AttributeSet attrs) //构造函数
	{
		super(context, attrs);
	}
	//	@Override
//    protected void onDraw(Canvas canvas)
//    {
//		super.onDraw(canvas);
//        VideoBit.copyPixelsFromBuffer(buffer);
//        canvas.drawBitmap(VideoBit, 0, 0, null);
//    }
	@Override
	protected void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);
		Log.d("TIEJIANG", "Videoplay---onDraw");
		VideoBit.copyPixelsFromBuffer(buffer);
		buffer.position(0);
		RectF rectF = new RectF(0, 0, width, height);   //w和h分别是屏幕的宽和高，也就是你想让图片显示的宽和高

		canvas.drawBitmap(VideoBit, null, rectF, null);
	}
}