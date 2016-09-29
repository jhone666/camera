package com.example.test.camare;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.example.test.utils.Utils;
/**
 * 
 * @author win
 * 这个是风格参考线，粗细和颜色的话可以自己去调整下
 *
 */
public class ReferenceLine extends View {

	private Paint mLinePaint;

	public ReferenceLine(Context context) {
		super(context);
		init();
	}

	public ReferenceLine(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public ReferenceLine(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	private void init() {
		mLinePaint = new Paint();
		mLinePaint.setAntiAlias(true);
		mLinePaint.setColor(Color.parseColor("#45FFFFFF"));//这里有带apha值
		mLinePaint.setStrokeWidth(2);//这里设置为两个px
	}



	/**
	 * 这里我画的是九宫格线，均分宽和高，而你的UI图是12个格子，而且边上略小，不均分，觉得不好看，如果你
	 * 一定要UI上的效果，到时候跟我说，我改下，或者你自己这里改下，都是很简单的
	 */
	@Override
	protected void onDraw(Canvas canvas) {
		int screenWidth = Utils.getScreenWH(getContext()).widthPixels;
		int screenHeight = Utils.getScreenWH(getContext()).heightPixels;

		int width = screenWidth/3;
		int height = screenHeight/3;

		for (int i = width, j = 0;i < screenWidth && j<2;i += width, j++) {
			canvas.drawLine(i, 0, i, screenHeight, mLinePaint);
		}
		for (int j = height,i = 0;j < screenHeight && i < 2;j += height,i++) {
			canvas.drawLine(0, j, screenWidth, j, mLinePaint);
		}
	}


}
