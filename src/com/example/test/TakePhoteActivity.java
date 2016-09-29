package com.example.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.test.camare.CameraPreview;
import com.example.test.camare.FocusView;
import com.example.test.camare.ReferenceLine;
import com.example.test.cropper.CropImageView;
import com.example.test.utils.Utils;
public class TakePhoteActivity extends Activity implements
		CameraPreview.OnCameraStatusListener, SensorEventListener {
	private static final String TAG = "TakePhoteActivity";
	public static final Uri IMAGE_URI = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
	public static final String PATH = Environment.getExternalStorageDirectory()
			.toString() + "/AndroidMedia/";
	CameraPreview mCameraPreview;
	CropImageView mCropImageView;
	RelativeLayout mTakePhotoLayout;
	LinearLayout mCropperLayout;
	private ImageView iv_flash;
	private TextView tv_toggle, tv_button;
	private ReferenceLine line_view;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_take_phote);
		mCropImageView = (CropImageView) findViewById(R.id.CropImageView);
		mCameraPreview = (CameraPreview) findViewById(R.id.cameraPreview);
		FocusView focusView = (FocusView) findViewById(R.id.view_focus);
		mTakePhotoLayout = (RelativeLayout) findViewById(R.id.take_photo_layout);
		mCropperLayout = (LinearLayout) findViewById(R.id.cropper_layout);
		iv_flash = (ImageView) findViewById(R.id.iv_flash);
		tv_toggle = (TextView) findViewById(R.id.tv_toggle);
		tv_button = (TextView) findViewById(R.id.tv_button);
		line_view = (ReferenceLine) findViewById(R.id.line_view);

		mCameraPreview.setFocusView(focusView);
		mCameraPreview.setOnCameraStatusListener(this);
		mCropImageView.setGuidelines(2);

		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mAccel = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

	}

	boolean isRotated = false;

	@Override
	protected void onResume() {
		super.onResume();
		mSensorManager.registerListener(this, mAccel,
				SensorManager.SENSOR_DELAY_UI);
	}

	@Override
	protected void onPause() {
		super.onPause();
		mSensorManager.unregisterListener(this);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		Log.e(TAG, "onConfigurationChanged");
		super.onConfigurationChanged(newConfig);
	}

	private boolean isFlashing = false;

	public void toggleFlash(View view) {
		if (!isFlashing) {
			isFlashing = true;
			iv_flash.setImageResource(R.drawable.flash_select);
			tv_toggle.setText("关闭");
			// 开启闪光灯
			mCameraPreview.openFlash();
		} else {
			isFlashing = false;
			iv_flash.setImageResource(R.drawable.flash_normal);
			tv_toggle.setText("开启");
			// 关闭闪光灯
			mCameraPreview.closeFlash();
		}
	}

	private boolean lineViewShow = true;

	// toggle网格
	public void toggleLine(View view) {

		if (lineViewShow) {
			line_view.setVisibility(View.GONE);
			tv_button.setText("开启网格");
			lineViewShow = false;
		} else {
			line_view.setVisibility(View.VISIBLE);
			tv_button.setText("关闭网格");
			lineViewShow = true;
		}
	}

	public void takePhoto(View view) {
		if (mCameraPreview != null) {
			mCameraPreview.takePicture();
		}
	}

	private final int GET_PIC_FORM_LOCAL = 100;

	/**
	 * 从相册获取
	 * 
	 * @param view
	 */
	public void selectLocalPic(View view) {
		Intent intent = new Intent(Intent.ACTION_PICK);
		intent.setType("image/*");
		startActivityForResult(intent, GET_PIC_FORM_LOCAL);
	}

	public void close(View view) {
		finish();
	}

	public void roteTo(View view) {
		mCropImageView.rotateImage(90);
	}

	public void reRoteTo(View view) {
		mCropImageView.rotateImage(-90);
	}

	/**
	 * 关闭裁剪窗口
	 * 
	 * @param view
	 */
	public void closeCropper(View view) {
		showTakePhotoLayout();
	}

	/**
	 * 保存截图
	 * 
	 * @param view
	 */
	public void startCropper(View view) {
		CropperImage cropperImage = mCropImageView.getCroppedImage();
		Bitmap bitmap = cropperImage.getBitmap();
		long dateTaken = System.currentTimeMillis();
		String filename = DateFormat.format("yyyy-MM-dd kk.mm.ss", dateTaken)
				.toString() + ".jpg";
		Uri uri = insertImage(getContentResolver(), filename, dateTaken, PATH,
				filename, bitmap, null);
		cropperImage.getBitmap().recycle();
		cropperImage.setBitmap(null);
		Intent intent = new Intent(this, ShowCropperedActivity.class);
		intent.setData(uri);
		startActivity(intent);
		bitmap.recycle();
		finish();
		super.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
	}

	/**
	 * data为拍摄的照片
	 */
	@Override
	public void onCameraStopped(byte[] data) {
		Log.i("TAG", "==onCameraStopped==");
		Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
		long dateTaken = System.currentTimeMillis();
		String filename = DateFormat.format("yyyy-MM-dd kk.mm.ss", dateTaken)
				.toString() + ".jpg";
		Uri source = insertImage(getContentResolver(), filename, dateTaken,
				PATH, filename, bitmap, data);
		try {
			mCropImageView.setImageBitmap(MediaStore.Images.Media.getBitmap(
					this.getContentResolver(), source));
			// mCropImageView.rotateImage(90);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		showCropperLayout();
	}

	private Uri insertImage(ContentResolver cr, String name, long dateTaken,
			String directory, String filename, Bitmap source, byte[] jpegData) {
		OutputStream outputStream = null;
		String filePath = directory + filename;
		try {
			File dir = new File(directory);
			if (!dir.exists()) {
				dir.mkdirs();
			}
			File file = new File(directory, filename);
			if (file.createNewFile()) {
				outputStream = new FileOutputStream(file);
				if (source != null) {
					source.compress(Bitmap.CompressFormat.JPEG, 100,
							outputStream);
				} else {
					outputStream.write(jpegData);
				}
			}
		} catch (FileNotFoundException e) {
			Log.e(TAG, e.getMessage());
			return null;
		} catch (IOException e) {
			Log.e(TAG, e.getMessage());
			return null;
		} finally {
			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (Throwable t) {
				}
			}
		}
		ContentValues values = new ContentValues(7);
		values.put(MediaStore.Images.Media.TITLE, name);
		values.put(MediaStore.Images.Media.DISPLAY_NAME, filename);
		values.put(MediaStore.Images.Media.DATE_TAKEN, dateTaken);
		values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
		values.put(MediaStore.Images.Media.DATA, filePath);
		return cr.insert(IMAGE_URI, values);
	}

	private void showTakePhotoLayout() {
		mTakePhotoLayout.setVisibility(View.VISIBLE);
		mCropperLayout.setVisibility(View.GONE);
		cropperLayoutShow = false;
	}

	private boolean cropperLayoutShow = false;

	private void showCropperLayout() {
		cropperLayoutShow = true;
		mTakePhotoLayout.setVisibility(View.GONE);
		mCropperLayout.setVisibility(View.VISIBLE);
		mCameraPreview.start();
	}

	private float mLastX = 0;
	private float mLastY = 0;
	private float mLastZ = 0;
	private boolean mInitialized = false;
	private SensorManager mSensorManager;
	private Sensor mAccel;

	@Override
	public void onSensorChanged(SensorEvent event) {

		float x = event.values[0];
		float y = event.values[1];
		float z = event.values[2];
		if (!mInitialized) {
			mLastX = x;
			mLastY = y;
			mLastZ = z;
			mInitialized = true;
		}
		float deltaX = Math.abs(mLastX - x);
		float deltaY = Math.abs(mLastY - y);
		float deltaZ = Math.abs(mLastZ - z);

		if (deltaX > 0.8 || deltaY > 0.8 || deltaZ > 0.8) {
			mCameraPreview.setFocus();
		}
		mLastX = x;
		mLastY = y;
		mLastZ = z;
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (cropperLayoutShow) {
				showTakePhotoLayout();
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		if (requestCode == GET_PIC_FORM_LOCAL) {
			Uri uri = data.getData();
			try {
				mCropImageView.setImageBitmap(MediaStore.Images.Media
						.getBitmap(this.getContentResolver(), uri));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				Toast.makeText(TakePhoteActivity.this, "文件不存在", 0).show();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Toast.makeText(TakePhoteActivity.this, "未知错误", 0).show();
				e.printStackTrace();
			}
			showCropperLayout();
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
}
