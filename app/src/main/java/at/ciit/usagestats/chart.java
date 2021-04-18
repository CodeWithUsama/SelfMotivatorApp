package at.ciit.usagestats;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Pie;

import java.util.ArrayList;
import java.util.List;

public class chart extends AppCompatActivity {

    ArrayList<String> appsit;
    ArrayList<Integer> tm;
    AnyChartView anyChartView;

int[] earnings = {500,800,2000};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);
anyChartView = findViewById(R.id.pieChart);
appsit = getIntent().getStringArrayListExtra("apps");
tm = getIntent().getIntegerArrayListExtra("minutes");

//ArrayAdapter<Integer> adapter = new ArrayAdapter<Integer>(chart.this, android.R.layout.simple_list_item_1,tm);
if(appsit.size() == tm.size()){
    Log.d("TAG","s");
}
Pie pie = AnyChart.pie();
        List<DataEntry> dataEntries = new ArrayList<>();
        for(int i=0;i<appsit.size();i++){

            dataEntries.add(new ValueDataEntry(appsit.get(i),tm.get(i)));
        }
        pie.data(dataEntries);
        anyChartView.setChart(pie);

    }

}