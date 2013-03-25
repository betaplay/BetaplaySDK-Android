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
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
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

		DefaultHttpClient client = sslClient(new DefaultHttpClient());
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
	
	/**
	 * solving problems with ssl
	 * 
	 * @param client
	 * @return
	 */
	private DefaultHttpClient sslClient(org.apache.http.client.HttpClient client) {
	    try {
	        X509TrustManager tm = new X509TrustManager() { 
	            public void checkClientTrusted(X509Certificate[] xcs, String string) throws CertificateException {}
	            public void checkServerTrusted(X509Certificate[] xcs, String string) throws CertificateException {}
	            public X509Certificate[] getAcceptedIssuers() {return null;}
	        };
	        SSLContext ctx = SSLContext.getInstance("TLS");
	        ctx.init(null, new TrustManager[]{tm}, null);
	        SSLSocketFactory ssf = new CustomSSLSocketFactory(ctx);
	        ssf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
	        ClientConnectionManager ccm = client.getConnectionManager();
	        SchemeRegistry sr = ccm.getSchemeRegistry();
	        sr.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
	        sr.register(new Scheme("https", ssf, 443));
	        return new DefaultHttpClient(ccm, client.getParams());
	    } catch (Exception ex) {
	        return null;
	    }
	}
	
	private class CustomSSLSocketFactory extends SSLSocketFactory {
	     SSLContext sslContext = SSLContext.getInstance("TLS");

	     public CustomSSLSocketFactory(KeyStore truststore) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
	         super(truststore);

	         TrustManager tm = new X509TrustManager() {
	             public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {}
	             public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {}
	             public X509Certificate[] getAcceptedIssuers() {return null;}
	         };

	         sslContext.init(null, new TrustManager[] { tm }, null);
	     }

	     public CustomSSLSocketFactory(SSLContext context) throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException {
	        super(null);
	        sslContext = context;
	     }

	     @Override
	     public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException, UnknownHostException {
	         return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
	     }

	     @Override
	     public Socket createSocket() throws IOException {
	         return sslContext.getSocketFactory().createSocket();
	     }
	}
}