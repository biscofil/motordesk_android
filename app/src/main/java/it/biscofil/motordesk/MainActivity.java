package it.biscofil.motordesk;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity implements SensorEventListener, View.OnClickListener {

    public View v;
    CheckBox cbLeft;
    CheckBox cbRight;
    ProgressBar pb;
    TextView tvIp;
    ImageButton changeIpBtn;

    public static final String mypreference = "mypref";
    public static final String prefIp = "ip";
    SharedPreferences sharedpreferences;

    String ip = "";

    private SensorManager sensorManager;
    private final float[] accelerometerReading = new float[3];
    private final float[] magnetometerReading = new float[3];
    private final float[] rotationMatrix = new float[9];
    private final float[] orientationAngles = new float[3];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        v = findViewById(R.id.constraintLayout);
        changeIpBtn = findViewById(R.id.changeIpBtn);

        changeIpBtn.setOnClickListener(this);

        tvIp = findViewById(R.id.IpTextView);

        pb = findViewById(R.id.progressBar);

        TemporaryButton DownBtn = findViewById(R.id.btnDown);
        TemporaryButton UpBtn = findViewById(R.id.btnUp);

        cbLeft = findViewById(R.id.checkBoxLeft);
        cbRight = findViewById(R.id.checkBoxRight);

        final MainActivity self = this;

        DownBtn.setTask(new GenericListener() {
            @Override
            public void onTrigger() {
                self.move("d");
            }
        });

        UpBtn.setTask(new GenericListener() {
            @Override
            public void onTrigger() {
                self.move("u");
            }
        });


        // ############ preferences

        sharedpreferences = getApplicationContext().getSharedPreferences(mypreference, Context.MODE_PRIVATE);

        ip = sharedpreferences.getString(prefIp, "192.168.1.204");
        this.setIp(ip);

    }

    private void setIp(String ip) {
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString(prefIp, ip);
        editor.commit(); // commit changes

        this.ip = ip;

        tvIp.setText(ip);
    }

    private void move(String mode) {

        final MainActivity self = this;

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();

        params.put("d", mode);

        String s = "-";

        if (cbLeft.isChecked() && cbRight.isChecked()) {
            s = "b"; // both
        } else if (cbLeft.isChecked()) {
            s = "l"; // left only
        } else if (cbRight.isChecked()) {
            s = "r"; //right only
        } else {
            //neither
            return;
        }
        params.put("s", s); //side

        client.setMaxRetriesAndTimeout(0, 100);
        client.get("http://" + ip, params, new TextHttpResponseHandler() {

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, String res) {
                        // called when response HTTP status is "200 OK"

                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String res, Throwable t) {
                        // called when response HTTP status is "4XX" (eg. 401, 403, 404)

                        Snackbar mySnackbar = Snackbar.make(self.v,
                                "Errror: " + ip + " can't be reached", Snackbar.LENGTH_SHORT);
                        mySnackbar.show();

                    }

                }
        );

    }

    @Override
    protected void onResume() {
        super.onResume();

        // Get updates from the accelerometer and magnetometer at a constant rate.
        // To make batch operations more efficient and reduce power consumption,
        // provide support for delaying updates to the application.
        //
        // In this example, the sensor reporting delay is small enough such that
        // the application receives an update before the system checks the sensor
        // readings again.
        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer,
                    SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
        }
        Sensor magneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (magneticField != null) {
            sensorManager.registerListener(this, magneticField,
                    SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Don't receive any more updates from either sensor.
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
        // You must implement this callback in your code.
    }


    // Get readings from accelerometer and magnetometer. To simplify calculations,
    // consider storing these readings as unit vectors.
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            System.arraycopy(event.values, 0, accelerometerReading,
                    0, accelerometerReading.length);

        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {

            System.arraycopy(event.values, 0, magnetometerReading,
                    0, magnetometerReading.length);

        }

        updateOrientationAngles();
    }

    // Compute the three orientation angles based on the most recent readings from
    // the device's accelerometer and magnetometer.
    public void updateOrientationAngles() {

        // Update rotation matrix, which is needed to update orientation angles.
        SensorManager.getRotationMatrix(rotationMatrix, null,
                accelerometerReading, magnetometerReading);

        // "mRotationMatrix" now has up-to-date information.

        SensorManager.getOrientation(rotationMatrix, orientationAngles);

        // "mOrientationAngles" now has up-to-date information.

        // angle in degree [-180 - 0 - 180] degree
        double azimuth = Math.toDegrees(orientationAngles[2]);

        pb.setProgress((int) (((azimuth * 10) + 180) % 360));

        if (Math.abs((azimuth % 360)) < 0.3) {
            pb.getProgressDrawable().setColorFilter(Color.GREEN, PorterDuff.Mode.SRC_IN);
        } else {
            pb.getProgressDrawable().setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
        }

    }

    @Override
    public void onClick(View view) {

        final MainActivity self = this;

        if (view.getId() == R.id.changeIpBtn) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Set Arduino's Ip");

            // Set up the input
            final EditText input = new EditText(this);

            input.setInputType(InputType.TYPE_CLASS_TEXT);
            input.setText(ip);
            builder.setView(input);

            // Set up the buttons
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    self.setIp(input.getText().toString());
                }
            });

            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builder.show();

        }

    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.info_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.info:

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
