package uk.co.sparcit.trainruntimechecker;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.kxml2.kdom.Element;
import org.kxml2.kdom.Node;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class RunTimesCheckService extends IntentService {
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_FOO = "uk.co.sparcit.trainruntimechecker.action.FOO";


    // TODO: Rename parameters
    private static final String EXTRA_PARAM1 = "uk.co.sparcit.trainruntimechecker.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "uk.co.sparcit.trainruntimechecker.extra.PARAM2";

    private static String SOAP_ACTION = "http://thalesgroup.com/RTTI/2015-05-14/ldb/GetArrBoardWithDetails";
    private static String NAMESPACE = "http://thalesgroup.com/RTTI/2015-05-14/ldb/";
    private static String HEADERNAMESPACE = "http://thalesgroup.com/RTTI/2013-11-28/Token/types";
    private static String METHOD_NAME = "GetArrBoardWithDetails";
    private static String URL = "https://lite.realtime.nationalrail.co.uk/OpenLDBWS/wsdl.aspx?ver=2015-05-14";
//    private static String URL = "https://lite.realtime.nationalrail.co.uk/OpenLDBWS/ldb7.asmx";
    private static String strTokenValue = "3edf5ad4-1fb5-4c79-801b-cacf820cc182";



    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionTimeCheck(Context context, String param1, String param2) {
        Intent intent = new Intent(context, RunTimesCheckService.class);
        intent.setAction(ACTION_FOO);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }


    public RunTimesCheckService() {
        super("RunTimesCheckService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_FOO.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionFoo(param1, param2);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionFoo(String param1, String param2) {
        // TODO: Handle action Foo
        //Initialize soap request + add parameters
        SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
        //Use this to add parameters
        request.addProperty("numRows","10");
        request.addProperty("crs","CHX");
        request.addProperty("filterCrs","ELE");
        request.addProperty("filterType","from");
        request.addProperty("timeOffset","0");
        request.addProperty("timeWindow","120");


        //Declare the version of the SOAP request
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);

        //Set the SOAP request's header
        envelope.headerOut = new Element[1];
        envelope.headerOut[0] = buildSoapRequestHeader();
        Log.i("header", "" + envelope.headerOut.toString()); //TODO see what this does


        //Set the soap request body
        envelope.setOutputSoapObject(request);
        Log.i("bodyout", "" + envelope.bodyOut.toString()); //TODO see what this does
        envelope.dotNet = true;

        try {
            HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
            androidHttpTransport.debug = true;

            //this is the actual part that will call the webservice
            androidHttpTransport.call(SOAP_ACTION, envelope);

            // Get the SoapResult from the envelope body.
            SoapObject result = (SoapObject)envelope.bodyIn;
            Log.i("result", result.toString());

            if(result != null)
            {
                //TODO Stuff with the result
            }
            else
            {
                Toast.makeText(getApplicationContext(), "No Response", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private Element buildSoapRequestHeader() {
        Element h = new Element().createElement(HEADERNAMESPACE, "AccessToken");
        Element TokenValue = new Element().createElement(null, "TokenValue"); //TODO or instead of null use HEADERNAMESPACE
        TokenValue.addChild(Node.TEXT, strTokenValue);
        h.addChild(Node.ELEMENT,TokenValue);
        return h;
    }
}
