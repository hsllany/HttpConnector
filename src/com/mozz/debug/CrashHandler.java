package com.mozz.debug;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.NameValuePair;

import com.mozz.utils.SystemInfo;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

public class CrashHandler implements UncaughtExceptionHandler {

	public static final String TAG = "CrashHandler";

	// ϵͳĬ�ϵ�UncaughtException������
	private Thread.UncaughtExceptionHandler mDefaultHandler;
	// CrashHandelrʵ��
	private static CrashHandler m_crashHandler = null;
	// ����Context����
	private Context mContext;
	// �����洢�豸��Ϣ���쳣��Ϣ
	private Map<String, String> infos = new HashMap<String, String>();

	// �û���ʽ�����ڣ���Ϊ��־�ļ�����һ����
	private SimpleDateFormat formatter = new SimpleDateFormat(
			"yyyy-MM-dd-HH-mm-ss");

	private String Path = null;

	public static CrashHandler getInstance() {
		if (m_crashHandler == null) {
			m_crashHandler = new CrashHandler();
		}
		return m_crashHandler;
	}

	private CrashingListener listener;

	public void init(Context context, CrashingListener listener) {
		this.listener = listener;
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			Path = Environment.getExternalStorageDirectory().getAbsolutePath()
					.toString()
					+ "/CrashLog/";
		} else {
			Path = "/data/data/CrashLog/";
		}

		this.mContext = context;
		// ��ȡϵͳĬ�ϵ�UncaughtException������
		mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
		// ���ø�CrashHandlerΪ�����Ĭ�ϵĴ�����
		Thread.setDefaultUncaughtExceptionHandler(this);
	}

	public void setCrashPath(String crash_file_path) {
		if (crash_file_path != null && !crash_file_path.equals("")) {
			Path = crash_file_path;
			File dir = new File(Path);
			if (!dir.exists()) {
				dir.mkdirs();
			}
		}
	}

	/**
	 * ��UncaughtException�����ǵ���
	 */
	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		if (!handleException(ex) && mDefaultHandler != null) {
			// ����û�û�д�������ϵͳĬ�ϵ��쳣������������
			mDefaultHandler.uncaughtException(thread, ex);
		} else {
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				Log.e(TAG, "error : ", e);
			}
			// �˳�����
			android.os.Process.killProcess(android.os.Process.myPid());
			System.exit(1);
		}
	}

	private boolean handleException(Throwable ex) {
		if (null == ex) {
			return false;
		}

		// Toast��ʾ�쳣��Ϣ
		// new Thread() {
		// @Override
		// public void run() {
		// Looper.prepare();
		// Toast.makeText(mContext, "��Ǹ�������쳣,2���ϵͳ���Զ��ر�",
		// Toast.LENGTH_SHORT).show();
		// System.exit(0);
		// Looper.loop();
		// }
		// }.start();
		// �ռ��豸������Ϣ
		collectDeviceInfo(mContext);
		// ������־�ļ�
		String fileName = saveCrashInfo2File(ex);
		// ������־��������
		if (listener != null) {
			listener.onCrashLogSaved(fileName);
		}
		// sendLogToServer(fileName);
		return true;
	}

	private void collectDeviceInfo(Context ctx) {
		String versionName = SystemInfo.getPackageVersionName(ctx) == null ? "null"
				: SystemInfo.getPackageVersionName(ctx);
		String versionCode = SystemInfo.getPackageVersionCode(ctx) + "";
		infos.put("versionName", versionName);
		infos.put("versionCode", versionCode);
		Field[] fields = Build.class.getDeclaredFields();
		for (Field field : fields) {
			field.setAccessible(true);
			try {
				infos.put(field.getName(), field.get(null).toString());
				Log.d(TAG, field.getName() + " : " + field.get(null));
			} catch (IllegalArgumentException e) {
				Log.e(TAG, "an error occured when collect package info", e);
			} catch (IllegalAccessException e) {
				Log.e(TAG, "an error occured when collect package info", e);
			}

		}

	}

	/**
	 * ���������Ϣ���ļ���
	 * 
	 * @param ex
	 * @return �����ļ�����,���ڽ��ļ����͵�������
	 */
	private String saveCrashInfo2File(Throwable ex) {

		StringBuffer sb = new StringBuffer();
		for (Map.Entry<String, String> entry : infos.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
			sb.append(key + "=" + value + "\n");
		}

		Writer writer = new StringWriter();
		PrintWriter printWriter = new PrintWriter(writer);
		ex.printStackTrace(printWriter);
		Throwable cause = ex.getCause();
		while (cause != null) {
			cause.printStackTrace(printWriter);
			cause = cause.getCause();
		}
		printWriter.close();
		String result = writer.toString();
		sb.append(result);
		try {
			long timestamp = System.currentTimeMillis();
			String time = formatter.format(new Date());
			String fileName = "crash-" + time + "_" + timestamp + ".log";
			if (Environment.getExternalStorageState().equals(
					Environment.MEDIA_MOUNTED)) {
				File dir = new File(Path);
				if (!dir.exists()) {
					dir.mkdirs();
				}
				FileOutputStream fos = new FileOutputStream(Path + fileName);
				fos.write(sb.toString().getBytes());
				fos.close();
			}
			return Path + fileName;
		} catch (Exception e) {
			Log.e(TAG, "an error occured while writing file...", e);
		}
		return null;
	}

	public interface CrashingListener {
		public void onCrashLogSaved(String fileName);
	}
}
