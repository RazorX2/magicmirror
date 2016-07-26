package razorx2.magicmirror2;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity {


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final TextView Date = (TextView)findViewById(R.id.Date);
        final TextView Time = (TextView)findViewById(R.id.Time);
        Date.setText(getCurrentDate());
        Time.setText(getCurrentTime());
        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(1000);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                               Date.setText(getCurrentDate());
                               Time.setText(getCurrentTime());
                            }
                        });
                    }
                } catch (InterruptedException e) {
                }
            }
        };

        t.start();

    }
    public String getCurrentDate(){
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat mformat = new SimpleDateFormat("MMMM");
        SimpleDateFormat dformat = new SimpleDateFormat("dd");
        String strDate = "" + mformat.format(calendar.getTime())+" "+ dformat.format(calendar.getTime());
        return strDate;
    }
    public String getCurrentTime(){
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat hformat = new SimpleDateFormat("h");
        SimpleDateFormat tformat = new SimpleDateFormat("mm");
        SimpleDateFormat apformat = new SimpleDateFormat("a");
        String strTime = ""+hformat.format(calendar.getTime())  +   ":" +   tformat.format(calendar.getTime())  +   " " +   apformat.format(calendar.getTime());
        return strTime;

    }
}
//class myTask extends TimerTask{
//    MainActivity main = new MainActivity();
//    TextView Date;
//    TextView Time;
//    public myTask(MainActivity main, TextView date, TextView time){
//        main = main;
//        Date=date;
//        Time=time;
//    }
//    public void run(){
//        main.Update();
//    }
//}

