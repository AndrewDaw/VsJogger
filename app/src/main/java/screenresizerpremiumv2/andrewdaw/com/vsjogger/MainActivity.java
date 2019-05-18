package screenresizerpremiumv2.andrewdaw.com.vsjogger;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    void doJog(View view){
        Intent intent = new Intent(this, JogActivity.class);
        startActivity(intent);
    }

    void doStats(View view){
        Intent intent = new Intent(this, StatsActivity.class);
        startActivity(intent);
    }

    void doVersion(View view){
        Intent intent = new Intent(this, VersionActivity.class);
        startActivity(intent);
    }
}
