#Betaplay SDK#

* is simple ACRA extension witch uses HttpClient to POST json data to server
* you can use JsonSender class and create your own custom mapping required by your own server
* or you can try to use betaplayapp

**Example how to use JsonSender:**

<pre>

public static void init(Application app) {
		
		ACRA.init(app);
		
		Map<ReportField, String> mapping = new HashMap<ReportField, String>();
		mapping.put(ReportField.APP_VERSION_CODE, "appVersionCode");
		
		ACRA.getErrorReporter().removeAllReportSenders();
        ACRA.getErrorReporter().addReportSender(new JsonSender(mapping));

</pre>

**Example how to use Betaplay:**

<pre>

@ReportsCrashes(formKey = "{your_form_key_here}", formUri = "https://betaplayapp.com/api/log")
public class AtcApplication extends Application {
	@Override
	public void onCreate() {
		Betaplay.init(this);
		super.onCreate();
	}
}

</pre>