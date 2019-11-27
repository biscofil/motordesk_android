package it.biscofil.motordesk;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.appcompat.widget.AppCompatButton;

import java.util.Timer;
import java.util.TimerTask;

public class TemporaryButton extends AppCompatButton {

    public GenericListener task;

    public Timer timer;

    class ExecuteAction extends TimerTask {
        public void run() {

            Handler mainHandler = new Handler(Looper.getMainLooper());
            Runnable myRunnable = new Runnable() {
                @Override
                public void run() {

                    task.onTrigger();

                }
            };

            mainHandler.post(myRunnable);

        }
    }

    public void setTask(GenericListener listener) {
        this.task = listener;
    }

    public TemporaryButton(Context context, AttributeSet attrs) {
        super(context, attrs);

        super.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                int action = event.getAction();

                if (action == MotionEvent.ACTION_DOWN) {

                    timer = new Timer();
                    timer.schedule(new ExecuteAction(), 0, 500);

                } else if (action == MotionEvent.ACTION_UP) {

                    timer.cancel();

                }

                return false;   //  the listener has NOT consumed the event, pass it on

            }
        });
    }

}
