package uk.co.sparcit.trainruntimechecker;

import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
//import android.app.ActionBar;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.ksoap2.SoapEnvelope;
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
import java.util.concurrent.TimeUnit;


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
                //makeSoapRequest();
                /*Intent intent = new Intent(getApplicationContext(), RunTimesCheckService.class);
                // add infos for the service which file to download and where to store
                intent.putExtra(DownloadService.FILENAME, "index.html");
                intent.putExtra(DownloadService.URL,
                        "http://www.vogella.com/index.html");
                startService(intent); */
                //TODO https://guides.codepath.com/android/Starting-Background-Services - Use this as a reference
                //ToDO http://stackoverflow.com/questions/4459058/alarm-manager-example
                RunTimesCheckService.startActionTimeCheck(v.getContext(),"dummy1","dummy2");
                Toast.makeText(v.getContext(), "Run Time Check Service Started", Toast.LENGTH_LONG).show();
                btnStart.setEnabled(false);
                if (!btnStop.isEnabled()){
                    btnStop.setEnabled(true);
                }

            }
        });

        btnStop = (Button) findViewById(R.id.btnStop);
        btnStop.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Do something in response to button click
                AlarmReceiver ar = new AlarmReceiver();
                ar.CancelAlarm(v.getContext());
                btnStop.setEnabled(false);
                if (!btnStart.isEnabled()){
                    btnStart.setEnabled(true);
                }
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
                final String CRS = "CHX";
                final String filterCrs = "ELE";
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
                request.addProperty("crs", CRS);
                request.addProperty("filterCrs", filterCrs);
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
                    String generatedAt = null;
                    final int YESCANCELLED = 1;  //https://www.sqlite.org/datatype3.html sqlite boolean
                    final int NOTCANCELLED = 0;
                    Uri URIfromInsert;
                    int UpdatedRows;


                    if (result != null) {
                        //TODO see if other soap objects can be traversed by using hasProperty as well
                        if (result.hasProperty("GetStationBoardResult")) {
                            GetStationBoardResult = (SoapObject) result.getPropertySafely("GetStationBoardResult");
                            if (GetStationBoardResult.getProperty(0) instanceof SoapPrimitive )
                                generatedAt = GetStationBoardResult.getProperty(0).toString();
                            for (int i = 0; i < GetStationBoardResult.getPropertyCount(); i++) {
                                if (GetStationBoardResult.getProperty(i) instanceof SoapObject ){
                                    trainServices = (SoapObject) GetStationBoardResult.getProperty(i);
                                    for (int j = 0; j < trainServices.getPropertyCount(); j++){
                                        if (trainServices.getProperty(j) instanceof SoapObject ){
                                            firstService = (SoapObject) trainServices.getProperty(j);
                                            Log.i("Property Name :", ((SoapObject)trainServices.getProperty(j)).getName());
                                            Log.i("Property Namespace :", ((SoapObject) trainServices.getProperty(j)).getNamespace());
                                            Log.i("Property :", trainServices.getProperty(j).toString());

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
                                                            ContentValues newRow = new ContentValues();
                                                            newRow.put(DBTableContract.TrainDelayRec.Fld_GeneratedAt, generatedAt);
                                                            newRow.put(DBTableContract.TrainDelayRec.Fld_To, CRS);
                                                            newRow.put(DBTableContract.TrainDelayRec.Fld_From, filterCrs);
                                                            newRow.put(DBTableContract.TrainDelayRec.Fld_Scehduled, strfirstServiceSTA);
                                                            newRow.put(DBTableContract.TrainDelayRec.Fld_Cancelled, YESCANCELLED);
                                                            String selection = DBTableContract.TrainDelayRec.Fld_Scehduled +" = \"" + strfirstServiceSTA + "\"";
                                                            //Check if row currently exists if so update otherwise insert
                                                            UpdatedRows = getContentResolver().update(DBContentProvider.CONTENT_URI, newRow, selection, null); //http://stackoverflow.com/questions/14142908/insert-or-update-in-sqlite-and-android-using-the-database-query
                                                            if (UpdatedRows  < 1)
                                                                URIfromInsert = getContentResolver().insert(DBContentProvider.CONTENT_URI, newRow);
                                                            displayNotification(strfirstServiceSTA +" from "+ filterCrs +" to "+ CRS +" is Cancelled");
                                                        } else {
                                                            DateFormat dffirstServiceETA = new SimpleDateFormat("hh:mm");
                                                            Date datfirstServiceETA = dffirstServiceETA.parse(strfirstServiceETA);
                                                            GregorianCalendar calfirstServiceETA = new GregorianCalendar();
                                                            calfirstServiceETA.setTime(datfirstServiceETA);
                                                            long delay = getDateDiff(calfirstServiceSTA.getTime(),calfirstServiceETA.getTime(),TimeUnit.MINUTES);
                                                            if (delay > 25l)
                                                            {
                                                                ContentValues newRow = new ContentValues();
                                                                newRow.put(DBTableContract.TrainDelayRec.Fld_GeneratedAt, generatedAt);
                                                                newRow.put(DBTableContract.TrainDelayRec.Fld_To, CRS);
                                                                newRow.put(DBTableContract.TrainDelayRec.Fld_From, filterCrs);
                                                                newRow.put(DBTableContract.TrainDelayRec.Fld_Scehduled, strfirstServiceSTA);
                                                                newRow.put(DBTableContract.TrainDelayRec.Fld_Expected, strfirstServiceETA);
                                                                newRow.put(DBTableContract.TrainDelayRec.Fld_Cancelled, NOTCANCELLED);
                                                                String selection = DBTableContract.TrainDelayRec.Fld_Scehduled +" = \"" + strfirstServiceSTA + "\"";
                                                                //Check if row currently exists if so update otherhwise insert
                                                                UpdatedRows = getContentResolver().update(DBContentProvider.CONTENT_URI, newRow, selection, null); //http://stackoverflow.com/questions/14142908/insert-or-update-in-sqlite-and-android-using-the-database-query
                                                                //http://www.techotopia.com/index.php/An_Android_Content_Provider_Tutorial
                                                                if (UpdatedRows  < 1)
                                                                URIfromInsert = getContentResolver().insert(DBContentProvider.CONTENT_URI, newRow);
                                                                displayNotification(strfirstServiceSTA +" from "+ filterCrs +" to "+ CRS +" is dealyed by "+ delay +" minutes ");
                                                            }

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

    /**
     * Get a diff between two dates
     * @param date1 the oldest date
     * @param date2 the newest date
     * @param timeUnit the unit in which you want the diff
     * @return the diff value, in the provided unit
     */
    public static long getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
        long diffInMillies = date2.getTime() - date1.getTime();
        return timeUnit.convert(diffInMillies,TimeUnit.MILLISECONDS);
    }

    private static int notificationID = 123;
    private static int numNotificationMsgs = 0;
    /**
     * Display the notification
     * @param notification
     */
    public void displayNotification(String notification){

        // Invoking the default notification service
            android.support.v4.app.NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_lightbulb_outline)
                        .setContentTitle("Train Delayed or Cancelled")
                        .setContentText(notification);
        // Increase notification number every time a new notification arrives
        mBuilder.setNumber(++numNotificationMsgs);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // notificationID allows you to update the notification later on.
        mNotificationManager.notify(notificationID, mBuilder.build());


    }
}
