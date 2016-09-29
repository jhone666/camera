package com.example.test.camare;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.test.utils.Utils;

public class CameraPreview extends SurfaceView implements
		SurfaceHolder.Callback, AutoFocusCallback {
	private static final String TAG = "CameraPreview";

	private int viewWidth = 0;
	private int viewHeight = 0;

	private OnCameraStatusListener listener;

	private SurfaceHolder holder;
	private Camera camera;
	private FocusView mFocusView;

	private PictureCallback pictureCallback = new PictureCallback() {

		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			try {
				camera.stopPreview();
			} catch (Exception e) {
			}
			if (null != listener) {
				listener.onCameraStopped(data);
			}
		}
	};

	public CameraPreview(Context context, AttributeSet attrs) {
		super(context, attrs);
		holder = getHolder();
		holder.addCallback(this);
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		setOnTouchListener(onTouchListener);
	}

	public void surfaceCreated(SurfaceHolder holder) {
		Log.e(TAG, "==surfaceCreated==");
		if(!Utils.checkCameraHardware(getContext())) {
			Toast.makeText(getContext(), "硬件不支持", Toast.LENGTH_SHORT).show();
			return;
		}
		camera = getCameraInstance();
		try {
			camera.setPreviewDisplay(holder);
		} catch (IOException e) {
			e.printStackTrace();
			camera.release();
			camera = null;
		}
		updateCameraParameters();
		if (camera != null) {
			camera.startPreview();
		}
		setFocus();
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.e(TAG, "==surfaceDestroyed==");
		camera.release();
		camera = null;
	}

	public void surfaceChanged(final SurfaceHolder holder, int format, int w,
			int h) {
		try {
			camera.stopPreview();
		} catch (Exception e){
		}
		updateCameraParameters();
		try {
			camera.setPreviewDisplay(holder);
			camera.startPreview();

		} catch (Exception e){
			Log.d(TAG, "Error starting camera preview: " + e.getMessage());
		}
		setFocus();
	}

	OnTouchListener onTouchListener = new OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				int width = mFocusView.getWidth();
				int height = mFocusView.getHeight();
				mFocusView.setX(event.getX() - (width / 2));
				mFocusView.setY(event.getY() - (height / 2));
				mFocusView.beginFocus();
			} else if (event.getAction() == MotionEvent.ACTION_UP) {
				focusOnTouch(event);
			}
			return true;
		}
	};

	private Camera getCameraInstance() {
		Camera c = null;
		try {
			int cameraCount = 0;
			Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
			cameraCount = Camera.getNumberOfCameras();

			for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
				Camera.getCameraInfo(camIdx, cameraInfo); 
				if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
					try {
						c = Camera.open(camIdx);
					} catch (RuntimeException e) {
						Toast.makeText(getContext(), "打开失败", Toast.LENGTH_SHORT).show();
					}
				}
			}
			if (c == null) {
				c = Camera.open(0);
			}
		} catch (Exception e) {
			Toast.makeText(getContext(), "打开失败", Toast.LENGTH_SHORT).show();
		}
		return c;
	}

	private void updateCameraParameters() {
		if (camera != null) {
			Camera.Parameters p = camera.getParameters();
			setParameters(p);
			try {
				camera.setParameters(p);
			} catch (Exception e) {
//				Camera.Size previewSize = findBestPreviewSize(p);
				Camera.Size previewSize = findPreviewSizeByScreen(p);
				p.setPreviewSize(previewSize.width, previewSize.height);
				p.setPictureSize(previewSize.width, previewSize.height);
				try {
					camera.setParameters(p);
				} catch (Exception e2) {
					e2.printStackTrace();
				}
			}
		}
	}

	private void setParameters(Camera.Parameters p) {
		List<String> focusModes = p.getSupportedFocusModes();
		if (focusModes
				.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
			p.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
		}

		long time = new Date().getTime();
		p.setGpsTimestamp(time);
		p.setPictureFormat(PixelFormat.JPEG);
		Camera.Size previewSize = findPreviewSizeByScreen(p);
		p.setPreviewSize(previewSize.width, previewSize.height);
		p.setPictureSize(previewSize.width, previewSize.height);
		p.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
		if (getContext().getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
			camera.setDisplayOrientation(90);
			p.setRotation(90);
		}
	}

	public void takePicture() {
		if (camera != null) {
			try {
				camera.takePicture(null, null, pictureCallback);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void setOnCameraStatusListener(OnCameraStatusListener listener) {
		this.listener = listener;
	}

	@Override
	public void onAutoFocus(boolean success, Camera camera) {

	}

	public void start() {
		if (camera != null) {
			camera.startPreview();
		}
	}

	public void stop() {
		if (camera != null) {
			camera.stopPreview();
		}
	}

	public interface OnCameraStatusListener {
		void onCameraStopped(byte[] data);
	}

	@Override
	protected void onMeasure(int widthSpec, int heightSpec) {
		viewWidth = MeasureSpec.getSize(widthSpec);
		viewHeight = MeasureSpec.getSize(heightSpec);
		super.onMeasure(
				MeasureSpec.makeMeasureSpec(viewWidth, MeasureSpec.EXACTLY),
				MeasureSpec.makeMeasureSpec(viewHeight, MeasureSpec.EXACTLY));
	}

	private Camera.Size findPreviewSizeByScreen(Camera.Parameters parameters) {
		if (viewWidth != 0 && viewHeight != 0) {
			return camera.new Size(Math.max(viewWidth, viewHeight),
					Math.min(viewWidth, viewHeight));
		} else {
			return camera.new Size(Utils.getScreenWH(getContext()).heightPixels,
					Utils.getScreenWH(getContext()).widthPixels);
		}
	}

	private Camera.Size findBestPreviewSize(Camera.Parameters parameters) {

		String previewSizeValueString = null;
		previewSizeValueString = parameters.get("preview-size-values");

		if (previewSizeValueString == null) {
			previewSizeValueString = parameters.get("preview-size-value");
		}

		if (previewSizeValueString == null) { 
			return camera.new Size(Utils.getScreenWH(getContext()).widthPixels,
					Utils.getScreenWH(getContext()).heightPixels);
		}
		float bestX = 0;
		float bestY = 0;

		float tmpRadio = 0;
		float viewRadio = 0;

		if (viewWidth != 0 && viewHeight != 0) {
			viewRadio = Math.min((float) viewWidth, (float) viewHeight)
					/ Math.max((float) viewWidth, (float) viewHeight);
		}

		String[] COMMA_PATTERN = previewSizeValueString.split(",");
		for (String prewsizeString : COMMA_PATTERN) {
			prewsizeString = prewsizeString.trim();

			int dimPosition = prewsizeString.indexOf('x');
			if (dimPosition == -1) {
				continue;
			}

			float newX = 0;
			float newY = 0;

			try {
				newX = Float.parseFloat(prewsizeString.substring(0, dimPosition));
				newY = Float.parseFloat(prewsizeString.substring(dimPosition + 1));
			} catch (NumberFormatException e) {
				continue;
			}

			float radio = Math.min(newX, newY) / Math.max(newX, newY);
			if (tmpRadio == 0) {
				tmpRadio = radio;
				bestX = newX;
				bestY = newY;
			} else if (tmpRadio != 0 && (Math.abs(radio - viewRadio)) < (Math.abs(tmpRadio - viewRadio))) {
				tmpRadio = radio;
				bestX = newX;
				bestY = newY;
			}
		}

		if (bestX > 0 && bestY > 0) {
			return camera.new Size((int) bestX, (int) bestY);
		}
		return null;
	}

	public void focusOnTouch(MotionEvent event) {

		int[] location = new int[2];
		RelativeLayout relativeLayout = (RelativeLayout)getParent();
		relativeLayout.getLocationOnScreen(location);

		Rect focusRect = Utils.calculateTapArea(mFocusView.getWidth(),
				mFocusView.getHeight(), 1f, event.getRawX(), event.getRawY(),
				location[0], location[0] + relativeLayout.getWidth(), location[1],
				location[1] + relativeLayout.getHeight());
		Rect meteringRect = Utils.calculateTapArea(mFocusView.getWidth(),
				mFocusView.getHeight(), 1.5f, event.getRawX(), event.getRawY(),
				location[0], location[0] + relativeLayout.getWidth(), location[1],
				location[1] + relativeLayout.getHeight());

		Camera.Parameters parameters = camera.getParameters();
		parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);

		if (parameters.getMaxNumFocusAreas() > 0) {
			List<Camera.Area> focusAreas = new ArrayList<Camera.Area>();
			focusAreas.add(new Camera.Area(focusRect, 1000));

			parameters.setFocusAreas(focusAreas);
		}

		if (parameters.getMaxNumMeteringAreas() > 0) {
			List<Camera.Area> meteringAreas = new ArrayList<Camera.Area>();
			meteringAreas.add(new Camera.Area(meteringRect, 1000));

			parameters.setMeteringAreas(meteringAreas);
		}

		try {
			camera.setParameters(parameters);
		} catch (Exception e) {
		}
		camera.autoFocus(this);
	}

	public void setFocusView(FocusView focusView) {
		this.mFocusView = focusView;
	}

	public void setFocus() {
		if(!mFocusView.isFocusing()) {
			try {
				camera.autoFocus(this);
				mFocusView.setX((Utils.getWidthInPx(getContext())-mFocusView.getWidth()) / 2);
				mFocusView.setY((Utils.getHeightInPx(getContext())-mFocusView.getHeight()) / 2);
				mFocusView.beginFocus();
			} catch (Exception e) {
			}
		}
	}

	public void openFlash() {
		// TODO Auto-generated method stub
		Camera.Parameters p = camera.getParameters();
		p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
		camera.setParameters(p);
	}

	public void closeFlash() {
		// TODO Auto-generated method stub
		Camera.Parameters p = camera.getParameters();
		p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
		camera.setParameters(p);
	}

}