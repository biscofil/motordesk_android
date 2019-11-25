package it.biscofil.motordesk;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.appcompat.widget.AppCompatButton;

import java.util.Timer;
import java.util.TimerTask;

public class TemporaryButton extends AppCompatButton {

    public GenericListener task;

    public Timer timer;

    class SayHello extends TimerTask {
        public void run() {
            task.onTrigger();
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
                    timer.schedule(new SayHello(), 0, 1000);

                } else if (action == MotionEvent.ACTION_UP) {

                    timer.cancel();

                }

                return false;   //  the listener has NOT consumed the event, pass it on

            }
        });
    }

}
