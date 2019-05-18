package screenresizerpremiumv2.andrewdaw.com.vsjogger;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import org.w3c.dom.Text;

public class StatsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);
        TextView stats = (TextView) findViewById(R.id.textStats);


        SharedPreferences sharedPreferences = this.getSharedPreferences("global", Context.MODE_PRIVATE);
        if (sharedPreferences.getString("jogs",null) != null) {
            stats.setText(sharedPreferences.getString("jogs",null));
        }


    }


}
