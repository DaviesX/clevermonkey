package clevermonkey.center;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home);

        //璁剧疆鎺т欢鍝嶅簲鍥炶皟銆
        //鑷姩鎺у埗妯″紡鎸夐挳銆
        ((Button)findViewById(R.id.btn_home_engine)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, EngineActivity.class);
                startActivity(intent);
            }
        });

        //杩滅▼閬ユ帶妯″紡鎸夐挳銆
        ((Button)findViewById(R.id.btn_home_remote)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, RemoteActivity.class);
                startActivity(intent);
            }
        });

        //杩滅▼鐩戞帶妯″紡鎸夐挳銆
        ((Button)findViewById(R.id.btn_home_monitor)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, MonitorActivity.class);
                startActivity(intent);
            }
        });

        //鍏充簬鎸夐挳銆
        ((Button)findViewById(R.id.btn_home_about)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, AboutActivity.class);
                startActivity(intent);
            }
        });
    }
}
