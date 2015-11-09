package uk.co.sparcit.trainruntimechecker;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
//import android.app.ActionBar;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.kxml2.kdom.Element;
import org.kxml2.kdom.Node;


public class MainActivity extends ActionBarActivity {

    Button btnStart,btnStop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnStart = (Button) findViewById(R.id.btnStart);
        btnStart.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Do something in response to button click
                makeSoapRequest();
            }
        });

        btnStop = (Button) findViewById(R.id.btnStop);
        btnStop.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Do something in response to button click
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void makeSoapRequest(){
        new Thread(new Runnable() {

            @Override
            public void run() {
                String SOAP_ACTION = "http://thalesgroup.com/RTTI/2015-05-14/ldb/GetArrBoardWithDetails";
                String NAMESPACE = "http://thalesgroup.com/RTTI/2015-05-14/ldb/";
                String HEADERNAMESPACE = "http://thalesgroup.com/RTTI/2013-11-28/Token/types";
              //  String METHOD_NAME = "GetArrBoardWithDetails";
                String METHOD_NAME = "GetArrBoardWithDetailsRequest";
              //  String URL = "https://lite.realtime.nationalrail.co.uk/OpenLDBWS/wsdl.aspx?ver=2015-05-14";
                String URL = "https://lite.realtime.nationalrail.co.uk/OpenLDBWS/ldb7.asmx";
                String strTokenValue = "3edf5ad4-1fb5-4c79-801b-cacf820cc182";

                SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
                //Use this to add parameters
                request.addProperty("numRows", "10");
                request.addProperty("crs", "CHX");
                request.addProperty("filterCrs", "ELE");
                request.addProperty("filterType", "from");
                request.addProperty("timeOffset", "0");
                request.addProperty("timeWindow", "120");


                //Declare the version of the SOAP request
                SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);

                //Set the SOAP request's header
                Element h = new Element().createElement(HEADERNAMESPACE, "AccessToken");
                Element TokenValue = new Element().createElement(null, "TokenValue"); //TODO or instead of null use HEADERNAMESPACE
                TokenValue.addChild(Node.TEXT, strTokenValue);
                h.addChild(Node.ELEMENT, TokenValue);
                envelope.headerOut = new Element[1];
                envelope.headerOut[0] = h;
                Log.i("header", "" + envelope.headerOut[0].toString()); //TODO see what this does


                //Set the soap request body
                envelope.setOutputSoapObject(request);
                Log.i("bodyout", "" + envelope.bodyOut.toString()); //TODO see what this does
                envelope.dotNet = true;

                try {
                    HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
                    androidHttpTransport.debug = true;

                    //this is the actual part that will call the webservice
                    androidHttpTransport.call(SOAP_ACTION, envelope);
                    Log.d("dump Request: " ,androidHttpTransport.requestDump);
                    Log.d("dump response: " ,androidHttpTransport.responseDump);

                    // Get the SoapResult from the envelope body.
                    SoapObject result = (SoapObject) envelope.bodyIn;
                    Log.i("result", result.toString());

                    if (result != null) {
                        //TODO Stuff with the result
                    } else {
                        Toast.makeText(getApplicationContext(), "No Response", Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }
}
