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
package com.betaplay.sdk.sender;

import static org.acra.ACRA.LOG_TAG;

import java.net.URL;
import java.util.Map;

import org.acra.ACRA;
import org.acra.collector.CrashReportData;
import org.acra.ReportField;
import org.acra.sender.ReportSender;
import org.acra.sender.ReportSenderException;

import android.util.Log;
import org.json.JSONObject;
import org.json.JSONException;

import com.betaplay.sdk.http.HttpClient;

/**
 * sends json formated data based on provided mapping 
 * 
 * @author Tomas T.
 */
public class JsonSender implements ReportSender {

	private static final String LOG_NAME = JsonSender.class.getSimpleName();

	private Map<ReportField, String> mMapping = null;

	/**
	 * Constructor Instantiate with betaplay custom mapping provided as argument
	 * 
	 * @param mapping
	 */
	public JsonSender(Map<ReportField, String> mapping) {
		mMapping = mapping;
	}

	@Override
	public void send(CrashReportData report) throws ReportSenderException {
		try {
			final URL reportUrl = new URL(ACRA.getConfig().formUri()
					.concat("/").concat(ACRA.getConfig().formKey())
					.concat(".json"));
			// Log.d(LOG_TAG, "Connect to " + reportUrl.toString());
			final JSONObject json = createJSON(report);
			sendJson(json.toString(), reportUrl);
		} catch (Exception e) {
			throw new ReportSenderException(
					"Error sending report to BetaPlay.", e);
		}
	}

	/**
	 * creates JSONObject by iterating over ACRA report fields comparing to
	 * betaplay mapping
	 * 
	 * @param report
	 * @return
	 */
	private JSONObject createJSON(Map<ReportField, String> report) {
		final JSONObject logWrap = new JSONObject();
		if (mMapping != null) {
			final JSONObject json = new JSONObject();

			ReportField[] fields = ACRA.getConfig().customReportContent();
			if (fields.length == 0) {
				fields = ACRA.DEFAULT_REPORT_FIELDS;
			}
			
			for (ReportField field : fields) {
				try {
					if (mMapping.get(field) != null && report.get(field) != null) {
						json.put(mMapping.get(field), report.get(field));
					}
				} catch (JSONException e) {
					Log.e(LOG_NAME, "Error creating JSON", e);
				}
			}
			
			try {
				logWrap.put("log", json);
			} catch (JSONException e) {
				Log.e(LOG_NAME, "Error wrapping JSON", e);
			}
		}
		return logWrap;
	}

	/**
	 * send out data to betaplay
	 * 
	 * @param data
	 * @param url
	 */
	private void sendJson(String data, URL url) {
		HttpClient client = new HttpClient(url.toString());
		client.setJsonBody(data);
		try {
			client.execute();
		} catch (Exception e) {
			Log.e(LOG_TAG, "Unable to send to server", e);
		}
	}
}