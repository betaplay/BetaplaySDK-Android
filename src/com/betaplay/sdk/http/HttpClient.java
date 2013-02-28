/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.betaplay.sdk.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

/**
 * simple httpclient use to POST acra logs to server in JSON format
 * 
 * @author Tomas T.
 *
 */
public class HttpClient {

	private String url;

	public HttpClient(String url) {
		this.url = url;
	}

	// ----------------------------------------------------------------------------
	// get set methods
	// ----------------------------------------------------------------------------

	private String mMessage;
	
	public String getErrorMessage() {
		return mMessage;
	}
	
	// ---
	
	private String mResponse;

	public String getResponse() {
		return mResponse;
	}
	
	// ---

	private int mResponseCode;
	
	public int getResponseCode() {
		return mResponseCode;
	}
	
	// ---
	
	private String mJsonBody;

	/**
	 * provide here json string witch send in request body
	 * 
	 * @param data
	 */
	public void setJsonBody(String data) {
		mJsonBody = data;
	}
	
	// ----------------------------------------------------------------------------
	// add params methods
	// ----------------------------------------------------------------------------
	
	private HttpUriRequest addBodyParams(HttpUriRequest request) throws Exception {
		if (mJsonBody != null) {
			request.addHeader("Content-Type", "application/json");
			if (request instanceof HttpPost) {
				((HttpPost) request).setEntity(new StringEntity(mJsonBody, "UTF-8"));
			}
		}
		return request;
	}
	
	// ----------------------------------------------------------------------------
	// execute methods
	// ----------------------------------------------------------------------------
	
	public void execute() throws Exception {
		HttpPost request = new HttpPost(url);
		request = (HttpPost) addBodyParams(request);
		executeRequest(request, url);
	}

	private void executeRequest(HttpUriRequest request, String url) {

		DefaultHttpClient client = new DefaultHttpClient();
		HttpParams params = client.getParams();

		// timeout 40 sec
		HttpConnectionParams.setConnectionTimeout(params, 40 * 1000);
		HttpConnectionParams.setSoTimeout(params, 40 * 1000);

		HttpResponse httpResponse;

		try {
			httpResponse = client.execute(request);
			mResponseCode = httpResponse.getStatusLine().getStatusCode();
			mMessage = httpResponse.getStatusLine().getReasonPhrase();

			HttpEntity entity = httpResponse.getEntity();

			if (entity != null) {

				InputStream instream = entity.getContent();
				mResponse = convertStreamToString(instream);
				
				instream.close();
			}

		} catch (ClientProtocolException e) {
			client.getConnectionManager().shutdown();
			e.printStackTrace();
		} catch (IOException e) {
			client.getConnectionManager().shutdown();
			e.printStackTrace();
		}
	}

	private static String convertStreamToString(InputStream is) {

		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}
	
	
}