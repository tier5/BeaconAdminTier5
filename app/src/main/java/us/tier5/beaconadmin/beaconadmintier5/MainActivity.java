package us.tier5.beaconadmin.beaconadmintier5;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Nearable;
import com.estimote.sdk.Region;
import com.estimote.sdk.SystemRequirementsChecker;
import com.estimote.sdk.Utils;
import com.estimote.sdk.telemetry.EstimoteTelemetry;

import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    //beacon variables
    private BeaconManager beaconManager;
    private Region region;
    private String scanId;

    //view variables
    TextView tvuuid;
    TextView tvmajor;
    TextView tvminor;
    TextView tvdistance;
    TextView tvNearableId;
    TextView tvNearableDistance;
    TextView tvNearableStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvuuid = (TextView) findViewById(R.id.tvuuid);
        tvmajor = (TextView) findViewById(R.id.tvmajor);
        tvminor = (TextView) findViewById(R.id.tvminor);
        tvdistance = (TextView) findViewById(R.id.tvdistance);
        tvNearableId = (TextView) findViewById(R.id.tvNearableId);
        tvNearableDistance = (TextView) findViewById(R.id.tvNearableDistance);
        tvNearableStatus = (TextView) findViewById(R.id.tvNearableStatus);

        beaconManager = new BeaconManager(getApplicationContext());
        region = new Region("ranged region",
                UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D"), null, null);

        beaconManager.setNearableListener(new BeaconManager.NearableListener() {
            @Override public void onNearablesDiscovered(List<Nearable> nearables) {
                if(nearables.size()>0) {
                    Nearable nearestNearable = nearables.get(0);

                    Utils.Proximity nearestNearableDistance = Utils.computeProximity(nearestNearable);
                    //Log.i("kingsukmajumder","Nearable distance"+nearestNearable.toString());

                    tvNearableDistance.setText("Nearest Sticker's Distance: "+nearestNearableDistance.toString());
                    if(nearestNearableDistance.toString().equals("IMMEDIATE"))
                    {
                        tvNearableId.setText(nearestNearable.identifier.toString());
                        if(nearestNearable.isMoving)
                        {
                            tvNearableStatus.setText("Moving");
                        }
                        else
                        {
                            tvNearableStatus.setText("Static");
                        }
                    }
                    else
                    {
                        tvNearableId.setText("");
                        tvNearableStatus.setText("");
                    }
                }
            }
        });

        beaconManager.setTelemetryListener(new BeaconManager.TelemetryListener() {
            @Override
            public void onTelemetriesFound(List<EstimoteTelemetry> telemetries) {
                if(!telemetries.isEmpty())
                {
                    EstimoteTelemetry nearestTelemetries = telemetries.get(0);
                    Log.d("kingsukmajumder", ""+nearestTelemetries.GPIO[0]+" "+nearestTelemetries.GPIO[1]+" "+nearestTelemetries.GPIO[2]+" "+nearestTelemetries.GPIO[3]);
                }


            }
        });



        beaconManager.setRangingListener(new BeaconManager.RangingListener() {
            @Override
            public void onBeaconsDiscovered(Region region, List<Beacon> list) {
                if (!list.isEmpty()) {
                    Beacon nearestBeacon = list.get(0);
                    Double beaconDistance = Utils.computeAccuracy(nearestBeacon);
                    //Log.i("kingsukmajumder",beaconDistance.toString());

                    tvdistance.setText("Nearest Beacon's Distance: "+beaconDistance.toString());

                    if(beaconDistance<1.00)
                    {
                        tvuuid.setText(nearestBeacon.getProximityUUID().toString());
                        tvmajor.setText(String.valueOf(nearestBeacon.getMajor()));
                        tvminor.setText(String.valueOf(nearestBeacon.getMinor()));
                    }
                    else
                    {
                        tvuuid.setText("");
                        tvmajor.setText("");
                        tvminor.setText("");
                    }

                }
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();

        SystemRequirementsChecker.checkWithDefaultDialogs(this);

        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                beaconManager.startRanging(region);
                scanId = beaconManager.startNearableDiscovery();
                scanId = beaconManager.startTelemetryDiscovery();
            }
        });
    }

    @Override
    protected void onPause() {
        beaconManager.stopRanging(region);
        beaconManager.stopNearableDiscovery(scanId);

        super.onPause();
    }
}
