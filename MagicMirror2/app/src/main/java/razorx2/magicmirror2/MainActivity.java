package razorx2.magicmirror2;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        TextView Date = (TextView)findViewById(R.id.Date);
        TextView Time = (TextView)findViewById(R.id.Time);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Date.setText(getCurrentDate());

    }
    private String getCurrentDate(){
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat mdformat = new SimpleDateFormat("yyyy / MM / dd ");
        String strDate = "Current Date : " + mdformat.format(calendar.getTime());
        return strDate;
    }

}
