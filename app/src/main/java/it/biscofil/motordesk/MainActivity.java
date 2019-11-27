package it.biscofil.motordesk;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {

    public View v;

    CheckBox cbLeft;
    CheckBox cbRight;

    ImageView iv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        v = findViewById(R.id.constraintLayout);

        iv = findViewById(R.id.imageView);

        TemporaryButton DownBtn = findViewById(R.id.btnDown);
        TemporaryButton UpBtn = findViewById(R.id.btnUp);

        cbLeft = findViewById(R.id.checkBoxLeft);
        cbRight = findViewById(R.id.checkBoxRight);

        cbLeft.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                updateImage();
            }
        });

        cbRight.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                updateImage();
            }
        });

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

    }

    private void updateImage() {
        if (cbLeft.isChecked() && cbRight.isChecked()) {
            iv.setImageResource(R.drawable.elon_both);
        } else if (cbLeft.isChecked()) {
            iv.setImageResource(R.drawable.elon_left);
        } else if (cbRight.isChecked()) {
            iv.setImageResource(R.drawable.elon_right);
        }
    }

    private void move(String mode) {

        final MainActivity self = this;

        Log.d("BISCOFIL", "request with" + mode);

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

        Log.d("BISCOIL", params.toString());

        client.setMaxRetriesAndTimeout(0, 100);
        client.get("http://192.168.1.204", params, new TextHttpResponseHandler() {

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, String res) {
                        // called when response HTTP status is "200 OK"

                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String res, Throwable t) {
                        // called when response HTTP status is "4XX" (eg. 401, 403, 404)

                        Snackbar mySnackbar = Snackbar.make(self.v,
                                "Errore", Snackbar.LENGTH_SHORT);
                        mySnackbar.show();

                    }

                }
        );

    }
}
