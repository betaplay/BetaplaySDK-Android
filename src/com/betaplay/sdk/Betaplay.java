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
package com.betaplay.sdk;

import java.util.HashMap;
import java.util.Map;

import org.acra.ACRA;
import org.acra.ReportField;

import com.betaplay.sdk.sender.JsonSender;

import android.app.Application;

/**
 * use if like BetaPlayApp to remotely debug your apps
 * 
 * @author Tomas T.
 * @version 0.2.0
 *
 */
public class Betaplay extends ACRA {

	/**
	 * use to initialize betaplay,
	 * it uses custom mapping and sends it to cloud api on server
	 * 
	 * @param app
	 */
	public static void init(Application app) {
		
		ACRA.init(app);
		
		Map<ReportField, String> mapping = new HashMap<ReportField, String>();
        mapping.put(ReportField.APP_VERSION_CODE, "appVersionCode"); 
        mapping.put(ReportField.APP_VERSION_NAME, "appVersionName");
        mapping.put(ReportField.PHONE_MODEL, "phoneModel"); 
        mapping.put(ReportField.BRAND, "brand"); 
        mapping.put(ReportField.ANDROID_VERSION, "androidVersion"); 
        mapping.put(ReportField.TOTAL_MEM_SIZE, "totalMemSize"); 
        mapping.put(ReportField.AVAILABLE_MEM_SIZE, "availableMemSize"); 
        mapping.put(ReportField.CUSTOM_DATA, "customData"); 
        mapping.put(ReportField.STACK_TRACE, "stackTrace"); 
        mapping.put(ReportField.DISPLAY, "display"); 
        mapping.put(ReportField.USER_COMMENT, "userComment"); 
        mapping.put(ReportField.USER_EMAIL, "userEmail"); 
        mapping.put(ReportField.USER_CRASH_DATE, "userCrashDate"); 
        
        ACRA.getErrorReporter().removeAllReportSenders();
        ACRA.getErrorReporter().addReportSender(new JsonSender(mapping));
	}

}
