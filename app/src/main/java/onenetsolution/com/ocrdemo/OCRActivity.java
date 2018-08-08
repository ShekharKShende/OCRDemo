package onenetsolution.com.ocrdemo;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.android.gms.common.api.CommonStatusCodes;

import io.realm.Realm;
import io.realm.RealmResults;
import onenetsolution.com.ocrdemo.ocr.OcrCaptureActivity;

public class OCRActivity extends AppCompatActivity implements View.OnClickListener {
    private Realm realm;
    private TableLayout table;
    private TableRow tr_head;
    private Button reset, capture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocr);
        capture = (Button) findViewById(R.id.btn_capture);
        capture.setOnClickListener(this);
        realm = Realm.getDefaultInstance();
        table = (TableLayout) findViewById(R.id.main_table);
        reset = (Button) findViewById(R.id.btn_reset);
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        realm.clear(Cylynder.class);
                        onResume();
                    }
                });
            }
        });
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent(OCRActivity.this, OcrCaptureActivity.class);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onResume() {
        super.onResume();

        RealmResults<Cylynder> tasks = realm.where(Cylynder.class).findAll();

        table.removeAllViews();
        tr_head = new TableRow(this);
//        tr_head.setId(10);
        tr_head.setBackgroundColor(Color.GRAY);
        tr_head.setLayoutParams(new TableLayout.LayoutParams(
                TableLayout.LayoutParams.FILL_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT));
        TextView label_date = new TextView(this);
//            label_date.setId();
        label_date.setText("Cylynder");
        label_date.setTextColor(Color.WHITE);
        label_date.setPadding(5, 5, 5, 5);
        tr_head.addView(label_date);
        table.addView(tr_head, new TableLayout.LayoutParams(
                TableLayout.LayoutParams.FILL_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT));
        for (Cylynder task : tasks) {
            TableRow tbrow = new TableRow(this);
            tbrow.setBackgroundColor(Color.GRAY);
            tbrow.setLayoutParams(new TableLayout.LayoutParams(
                    TableLayout.LayoutParams.FILL_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT));
            TextView t1v = new TextView(this);
            t1v.setText(task.getCylnderNo());
            t1v.setTextColor(Color.WHITE);
            t1v.setPadding(5, 5, 5, 5);
            tbrow.addView(t1v);
            table.addView(tbrow, new TableLayout.LayoutParams(
                    TableLayout.LayoutParams.FILL_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT));

        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == CommonStatusCodes.SUCCESS) {
            if (data != null) {
                final String text = data.getStringExtra("TEXT_FROM_GROSS");
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        Cylynder cylynder = new Cylynder();
                        cylynder.setCylnderNo(text);
                        realm.copyToRealm(cylynder);
                    }
                });

//                RealmResults<Cylynder> tasks = realm.where(Cylynder.class).findAll();
//
//
//                for (Cylynder task : tasks) {
//                    Log.d("Realm", task.getCylnderNo());
//                }

            }
        }


    }
}
