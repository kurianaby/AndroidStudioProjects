package uk.co.sparcit.trainruntimechecker;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Intent;
import android.content.Context;
import android.net.Uri;
import android.os.PowerManager;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
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
    private static final String ACTION_TIMECHECK = "uk.co.sparcit.trainruntimechecker.action.TIMECHECK";


    // TODO: Rename parameters
    private static final String FROMLOC = "uk.co.sparcit.trainruntimechecker.extra.fromLocation";
    private static final String TOLOC = "uk.co.sparcit.trainruntimechecker.extra.toLocation";

    private static String SOAP_ACTION = "http://thalesgroup.com/RTTI/2015-05-14/ldb/GetArrBoardWithDetails";
    private static String NAMESPACE = "http://thalesgroup.com/RTTI/2015-05-14/ldb/";
    private static String HEADERNAMESPACE = "http://thalesgroup.com/RTTI/2013-11-28/Token/types";
    private static String METHOD_NAME = "GetArrBoardWithDetails";
    private static String URL = "https://lite.realtime.nationalrail.co.uk/OpenLDBWS/wsdl.aspx?ver=2015-05-14";
//    private static String URL = "https://lite.realtime.nationalrail.co.uk/OpenLDBWS/ldb7.asmx";
    private static String strTokenValue = "3edf5ad4-1fb5-4c79-801b-cacf820cc182";

    private GregorianCalendar nxtAlarmTime;
    AlarmReceiver aRec = new AlarmReceiver();

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionTimeCheck(Context context, String fromLoc, String toLoc) {
        Intent intent = new Intent(context, RunTimesCheckService.class);
        intent.setAction(ACTION_TIMECHECK);
        intent.putExtra(FROMLOC, fromLoc);
        intent.putExtra(TOLOC, toLoc);
        context.startService(intent);
    }


    public RunTimesCheckService() {
        super("RunTimesCheckService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        PowerManager pm = (PowerManager) this.getApplication().getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
        wl.acquire();
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_TIMECHECK.equals(action)) {
                final String from  = intent.getStringExtra(FROMLOC);
                final String to = intent.getStringExtra(TOLOC);
                //handleActionFoo(param1, param2);
                makeSoapRequest(from,to);


            }
        }
        wl.release();
    }


    /**
     * Make the timecheck request with the given source and destination parameters
     * @param from
     * @param to
     */
    public void makeSoapRequest(String from, String to){
                String CRS = to;
                String filterCrs = from;
                String SOAP_ACTION = "http://thalesgroup.com/RTTI/2015-05-14/ldb/GetArrBoardWithDetails";
                String NAMESPACE = "http://thalesgroup.com/RTTI/2015-05-14/ldb/";
                String HEADERNAMESPACE = "http://thalesgroup.com/RTTI/2013-11-28/Token/types";
                //  String METHOD_NAME = "GetArrBoardWithDetails";
                String METHOD_NAME = "GetArrBoardWithDetailsRequest";
                //  String URL = "https://lite.realtime.nationalrail.co.uk/OpenLDBWS/wsdl.aspx?ver=2015-05-14";
                String URL = "https://lite.realtime.nationalrail.co.uk/OpenLDBWS/ldb7.asmx";
                String strTokenValue = "3edf5ad4-1fb5-4c79-801b-cacf820cc182";
                GregorianCalendar tmpAlarmTime;


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

                    SoapObject GetStationBoardResult,trainServices, servicetoProcess, secondService;
                    String generatedAt = null;


                    if (result != null) {
                        //TODO see if other soap objects can be traversed by using hasProperty as well
                        if (result.hasProperty("GetStationBoardResult")) {
                            GetStationBoardResult = (SoapObject) result.getPropertySafely("GetStationBoardResult");
                            if (GetStationBoardResult.getProperty(0) instanceof SoapPrimitive)
                                generatedAt = GetStationBoardResult.getProperty(0).toString();
                                for (int i = 0; i < GetStationBoardResult.getPropertyCount(); i++) {
                                    if (GetStationBoardResult.getProperty(i) instanceof SoapObject ){
                                        trainServices = (SoapObject) GetStationBoardResult.getProperty(i);
                                        for (int j = 0; j < trainServices.getPropertyCount(); j++){
                                        if (trainServices.getProperty(j) instanceof SoapObject ){
                                            servicetoProcess = (SoapObject) trainServices.getProperty(j);
                                            Log.i("Property Name :", ((SoapObject)trainServices.getProperty(j)).getName());
                                            Log.i("Property Namespace :", ((SoapObject) trainServices.getProperty(j)).getNamespace());
                                            Log.i("Property :", trainServices.getProperty(j).toString());

                                            tmpAlarmTime = processTrainService(servicetoProcess, CRS, filterCrs);
                                            if (tmpAlarmTime != null) {
                                                if (nxtAlarmTime != null) {
                                                    if (nxtAlarmTime.after(tmpAlarmTime))
                                                        nxtAlarmTime = tmpAlarmTime;
                                                } else
                                                    nxtAlarmTime = tmpAlarmTime;
                                            }
                                        }
                                    }
                                    // TODO check if the alarmreciever below is needed of if it will work with the class
                                    //TODO leve declaration used now
                                    // AlarmReceiver aRec = new AlarmReceiver();
                                    aRec.schdeuleNextAlarm(this,nxtAlarmTime);
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

    /**
     * Process the trainservice soap oject and store and notfy if there is a cancellation
     * or delay more than the notification cutoff interval specified
     * Returns a greg calendar object to use to set the alarm if there is more than 2
     * @param trainService parsed out of the xml response
     * @param CRS to sation code
     * @param filterCrs from station code
     * @return greg calendar object to use to set the alarm if there is more than 2
     * minutes to go till either the expected time of arrival or the schedule time of
     * arrival. If not will return a null object which has to be processed
     */
 public GregorianCalendar processTrainService( SoapObject trainService, String CRS, String filterCrs)
 {
     String generatedAt = null;
     final int YESCANCELLED = 1;  //https://www.sqlite.org/datatype3.html sqlite boolean
     final int NOTCANCELLED = 0;
     final Long NOTIFICATIONCUTOFF = 5l;
     Uri URIfromInsert;
     int UpdatedRows;
     GregorianCalendar tmpReturnTime =  null;

     if (trainService.getProperty(0) instanceof SoapPrimitive ) {
         try {

             String strfirstServiceSTA = trainService.getProperty(0).toString();
             String strfirstServiceETA = trainService.getProperty(1).toString();
             Log.i("STA :", strfirstServiceSTA);
             Log.i("ETA :", strfirstServiceETA);
             DateFormat dffirstServiceSTA = new SimpleDateFormat("hh:mm");
             Date datfirstServiceSTA = dffirstServiceSTA.parse(strfirstServiceSTA);
             GregorianCalendar calfirstServiceSTA = new GregorianCalendar();
             calfirstServiceSTA.setTime(datfirstServiceSTA);
             if (strfirstServiceETA.compareTo("On time") != 0) {
                 if (strfirstServiceETA.compareTo("Cancelled") == 0) {
                     ContentValues newRow = new ContentValues();
                     newRow.put(DBTableContract.TrainDelayRec.Fld_GeneratedAt, generatedAt);
                     newRow.put(DBTableContract.TrainDelayRec.Fld_To, CRS);
                     newRow.put(DBTableContract.TrainDelayRec.Fld_From, filterCrs);
                     newRow.put(DBTableContract.TrainDelayRec.Fld_Scehduled, strfirstServiceSTA);
                     newRow.put(DBTableContract.TrainDelayRec.Fld_Cancelled, YESCANCELLED);
                     String selection = DBTableContract.TrainDelayRec.Fld_Scehduled + " = \"" + strfirstServiceSTA + "\"";
                     //Check if row currently exists if so update otherwise insert
                     UpdatedRows = getContentResolver().update(DBContentProvider.CONTENT_URI, newRow, selection, null); //http://stackoverflow.com/questions/14142908/insert-or-update-in-sqlite-and-android-using-the-database-query
                     if (UpdatedRows < 1) {
                         URIfromInsert = getContentResolver().insert(DBContentProvider.CONTENT_URI, newRow);
                         displayNotification(strfirstServiceSTA + " from " + filterCrs + " to " + CRS + " is Cancelled");
                     }
                 } else {
                     DateFormat dffirstServiceETA = new SimpleDateFormat("hh:mm");
                     Date datfirstServiceETA = dffirstServiceETA.parse(strfirstServiceETA);
                     GregorianCalendar calfirstServiceETA = new GregorianCalendar();
                     calfirstServiceETA.setTime(datfirstServiceETA);
                     long delay = getDateDiff(calfirstServiceSTA.getTime(), calfirstServiceETA.getTime(), TimeUnit.MINUTES);
                     if (delay > NOTIFICATIONCUTOFF) {
                         ContentValues newRow = new ContentValues();
                         newRow.put(DBTableContract.TrainDelayRec.Fld_GeneratedAt, generatedAt);
                         newRow.put(DBTableContract.TrainDelayRec.Fld_To, CRS);
                         newRow.put(DBTableContract.TrainDelayRec.Fld_From, filterCrs);
                         newRow.put(DBTableContract.TrainDelayRec.Fld_Scehduled, strfirstServiceSTA);
                         newRow.put(DBTableContract.TrainDelayRec.Fld_Expected, strfirstServiceETA);
                         newRow.put(DBTableContract.TrainDelayRec.Fld_Cancelled, NOTCANCELLED);
                         String selection = DBTableContract.TrainDelayRec.Fld_Scehduled + " = \"" + strfirstServiceSTA + "\"";
                         //Check if row currently exists if so update otherhwise insert
                         UpdatedRows = getContentResolver().update(DBContentProvider.CONTENT_URI, newRow, selection, null); //http://stackoverflow.com/questions/14142908/insert-or-update-in-sqlite-and-android-using-the-database-query
                         //http://www.techotopia.com/index.php/An_Android_Content_Provider_Tutorial
                         if (UpdatedRows < 1){
                             URIfromInsert = getContentResolver().insert(DBContentProvider.CONTENT_URI, newRow);
                            displayNotification(strfirstServiceSTA + " from " + filterCrs + " to " + CRS + " is delayed by " + delay + " minutes ");
                         }
                     }
                     //if there is yet 2+ minutes to the ETA
                     if (getDateDiff(new GregorianCalendar().getTime(), calfirstServiceETA.getTime(), TimeUnit.MINUTES) > 2l)
                         tmpReturnTime = calfirstServiceETA;
                 }

             }
             //if there is yet 2+ minutes to the STA
             if (getDateDiff(new GregorianCalendar().getTime(), calfirstServiceSTA.getTime(), TimeUnit.MINUTES) > 2l)
                 return calfirstServiceSTA;

         } catch (ParseException p) {

         }
     }
     return tmpReturnTime;
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

