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
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.kxml2.kdom.Element;
import org.kxml2.kdom.Node;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;


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
                  //  Log.d("dump Request: " ,androidHttpTransport.requestDump);
                  //  Log.d("dump response: ", androidHttpTransport.responseDump);

                    // Get the SoapResult from the envelope body.
                    SoapObject result = (SoapObject) envelope.bodyIn;
                    Log.i("result", result.toString());

                    SoapObject GetStationBoardResult,trainServices, firstService, secondService;
                    //String firstServiceETA;


                    if (result != null) {
                        //TODO Stuff with the result
                        Log.i("Num Properties:", String.valueOf(((SoapObject) result.getProperty(0)).getPropertyCount()));

                        if (result.hasProperty("GetStationBoardResult")) {
                            GetStationBoardResult = (SoapObject) result.getPropertySafely("GetStationBoardResult");
                            for (int i = 0; i < GetStationBoardResult.getPropertyCount(); i++) {
                                if (GetStationBoardResult.getProperty(i) instanceof SoapObject ){
                                    trainServices = (SoapObject) GetStationBoardResult.getProperty(i);
                                    for (int j = 0; i < trainServices.getPropertyCount(); i++){
                                        if (trainServices.getProperty(i) instanceof SoapPrimitive ) {
                                            Log.i("Property Name :", ((SoapPrimitive) trainServices.getProperty(i)).getName());
                                            Log.i("Property Namespace :", ((SoapPrimitive) trainServices.getProperty(i)).getNamespace());
                                            Log.i("Property :", trainServices.getProperty(i).toString());
                                        }
                                        else if (trainServices.getProperty(i) instanceof SoapObject ){
                                            firstService = (SoapObject) trainServices.getProperty(i);
                                            Log.i("Property Name :", ((SoapObject)trainServices.getProperty(i)).getName());
                                            Log.i("Property Namespace :", ((SoapObject) trainServices.getProperty(i)).getNamespace());
                                            Log.i("Property :", trainServices.getProperty(i).toString());

                                            if (firstService.getProperty(0) instanceof SoapPrimitive ) {
                                                try {

                                                    String strfirstServiceSTA = firstService.getProperty(0).toString();
                                                    String strfirstServiceETA = firstService.getProperty(1).toString();
                                                    Log.i("STA :", strfirstServiceSTA);
                                                    Log.i("ETA :", strfirstServiceETA);
                                                   // String strsecondServiceSTA = secondService.getPropertySafely("sta").toString();
                                                    DateFormat dffirstServiceSTA = new SimpleDateFormat("hh:mm");
                                                    Date datfirstServiceSTA = dffirstServiceSTA.parse(strfirstServiceSTA);
                                                    GregorianCalendar calfirstServiceSTA = new GregorianCalendar();
                                                    calfirstServiceSTA.setTime(datfirstServiceSTA);
                                                    if (strfirstServiceSTA.compareTo("On time") != 0) {
                                                        if (strfirstServiceSTA.compareTo("Cancelled") == 0) {
                                                            //TODO Shout it out
                                                        } else {
                                                            DateFormat dffirstServiceETA = new SimpleDateFormat("hh:mm");
                                                            Date datfirstServiceETA = dffirstServiceETA.parse(strfirstServiceETA);
                                                            GregorianCalendar calfirstServiceETA = new GregorianCalendar();
                                                            calfirstServiceETA.setTime(datfirstServiceETA);
                                                            //TODO The rest of the stuff
                                                        }
                                                    }
                                                } catch (ParseException p) {

                                                }
                                            }
                                        }
                                    }
                                }

                            }

                        }

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
