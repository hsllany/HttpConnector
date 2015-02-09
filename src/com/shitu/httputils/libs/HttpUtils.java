package com.shitu.httputils.libs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Http tools for Android application
 * 
 * @author Yang & Zd <hsllany@163.com & zhangdao@>
 * 
 */
public class HttpUtils {
	/**
	 * The executor for run http request
	 */
	protected final static ExecutorService httpExecutor = Executors
			.newCachedThreadPool();

	public HttpUtils() {

	}

	/**
	 * Http Get function
	 * 
	 * @param url
	 *            , string
	 * @param l
	 *            , HttpListener
	 */
	public void get(String url, HttpListener l) {
		httpExecutor.execute(new HttpGet(url, l));
	}

	/**
	 * Http Post function
	 * 
	 * @param url
	 *            , String
	 * @param l
	 *            , HttpListener
	 * @param parameters
	 *            , HashMap<String, String> that contains name field and value
	 *            field of post data;
	 */
	public void post(String url, HttpListener l,
			HashMap<String, String> parameters) {
		StringBuilder sb = new StringBuilder();
		Iterator<Map.Entry<String, String>> itr = parameters.entrySet()
				.iterator();

		int i = 0;

		while (itr.hasNext()) {
			Map.Entry<String, String> entry = itr.next();
			String key = entry.getKey();
			String value = entry.getValue();
			if (i == 0)
				sb.append(key + "=" + value);
			else
				sb.append("&" + key + "=" + value);

			i++;
		}

		System.out.println(sb.toString());

		httpExecutor.execute(new HttpPost(url, l, sb.toString()));
	}

	class HttpPost implements Runnable {
		public HttpPost(String address, HttpListener l, String parameters) {
			this.mListener = l;
			this.mURL = address;
			this.postData = parameters;
		}

		@Override
		public void run() {
			HttpResponse response = new HttpResponse();
			response.html = null;
			response.status = 101;
			StringBuilder sb = new StringBuilder();

			URL url;
			try {
				url = new URL(mURL);
				HttpURLConnection urlConnection = (HttpURLConnection) url
						.openConnection();
				urlConnection.setDoOutput(true);
				urlConnection.setRequestMethod("POST");
				urlConnection.setUseCaches(false);
				urlConnection.connect();

				PrintWriter printer = new PrintWriter(
						urlConnection.getOutputStream());

				printer.println(postData);
				printer.flush();
				printer.close();

				InputStream in = urlConnection.getInputStream();
				BufferedReader br = new BufferedReader(
						new InputStreamReader(in));

				String line = "";
				while ((line = br.readLine()) != null) {
					sb.append(line);
				}
				br.close();
				in.close();
				urlConnection.disconnect();

				response.html = sb.toString();
				response.status = 200;

				mListener.onGet(response);
			} catch (MalformedURLException e) {
				e.printStackTrace();
				System.out.println("MALFORED");
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("IO");
			}
		}

		private String mURL;
		private HttpListener mListener;
		private String postData;

	}

	class HttpGet implements Runnable {

		public HttpGet(String address, HttpListener l) {
			this.mURL = address;
			this.mListener = l;
		}

		@Override
		public void run() {
			HttpResponse response = new HttpResponse();
			response.html = null;
			response.status = 101;
			StringBuilder sb = new StringBuilder();
			URL url;
			try {
				url = new URL(mURL);
				HttpURLConnection urlConnection = (HttpURLConnection) url
						.openConnection();

				urlConnection.connect();
				InputStream in = urlConnection.getInputStream();
				BufferedReader br = new BufferedReader(
						new InputStreamReader(in));

				String line = "";
				while ((line = br.readLine()) != null) {
					sb.append(line);
				}
				br.close();
				in.close();
				urlConnection.disconnect();

				response.html = sb.toString();
				response.status = 200;

				mListener.onGet(response);
			} catch (MalformedURLException e) {
				System.out.println("MALFORED");
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("IO");
			}

		}

		private String mURL;
		private HttpListener mListener;
	}

}
