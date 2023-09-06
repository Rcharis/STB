package com.ta.stb_03;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class Graph extends AppCompatActivity {

    EditText yValue;
    TextView dateEditText;
    Button submitBtn, selectDateButton, reloadButton;
    LineChart lineChart;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference myRef;
    LineDataSet lineDataSet;
    ArrayList<ILineDataSet> iLineDataSet = new ArrayList<>();
    LineData lineData;
    int xValue = 1; // Initial X value
    int currentXValue = 0;
    Calendar selectedDate = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);
        yValue = findViewById(R.id.yTextView);
        dateEditText = findViewById(R.id.dateEditText);
        submitBtn = findViewById(R.id.button);
        selectDateButton = findViewById(R.id.selectDateButton);
        reloadButton = findViewById(R.id.reloadButton);
        lineChart = findViewById(R.id.lineChart);
        firebaseDatabase = FirebaseDatabase.getInstance();
        myRef = firebaseDatabase.getReference("Trash");

        // Initialize LineDataSet
        lineDataSet = new LineDataSet(null, null);

        // Initialize LineData
        lineData = new LineData();

        selectDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePickerDialog();
            }
        });

        reloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Panggil metode retrieveData() untuk memuat ulang data dari Firebase Realtime Database
                retrieveData();
            }
        });

        insertData();
    }

    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                        // Set the selected date
                        selectedDate.set(Calendar.YEAR, year);
                        selectedDate.set(Calendar.MONTH, month);
                        selectedDate.set(Calendar.DAY_OF_MONTH, day);

                        // Update the dateEditText with the selected date
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                        String selectedDateString = dateFormat.format(selectedDate.getTime());
                        dateEditText.setText(selectedDateString);

                        // Retrieve data for the selected date
                        retrieveDataForSelectedDate();
                    }
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void insertData() {
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int y = Integer.parseInt(yValue.getText().toString());

                // Dapatkan tanggal hari ini
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                String todayDateString = dateFormat.format(new Date());

                // Buat objek DataPoint dengan tanggal hari ini
                DataPoint dataPoint = new DataPoint(todayDateString, xValue, y);

                // Gunakan tanggal hari ini sebagai bagian dari kunci data
                String id = todayDateString + "_" + xValue;

                myRef.child(id).setValue(dataPoint);

                // Retrieve data after inserting
                retrieveData();

                // Update X value
                xValue++;

                // Reset X value jika melebihi batas (misalnya, 5)
                if (xValue > 5) {
                    xValue = 1;
                }
            }
        });
    }



    private void retrieveData() {
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<Entry> dataVals = new ArrayList<>();

                for (DataSnapshot myDataSnapshot : dataSnapshot.getChildren()) {
                    DataPoint dataPoint = myDataSnapshot.getValue(DataPoint.class);
                    dataVals.add(new Entry(dataPoint.getxValue(), dataPoint.getyValue()));
                }

                // Update the chart with the retrieved data
                showChart(dataVals);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void retrieveDataForSelectedDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String selectedDateString = dateFormat.format(selectedDate.getTime());

        myRef.orderByChild("date").equalTo(selectedDateString).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<Entry> dataVals = new ArrayList<>();

                for (DataSnapshot myDataSnapshot : dataSnapshot.getChildren()) {
                    DataPoint dataPoint = myDataSnapshot.getValue(DataPoint.class);
                    dataVals.add(new Entry(dataPoint.getxValue(), dataPoint.getyValue()));
                }

                // Update the chart with the retrieved data
                showChart(dataVals);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void showChart(ArrayList<Entry> dataVals) {
        // Set data values to the LineDataSet
        lineDataSet.setValues(dataVals);
        lineDataSet.setLabel("Dataset 1");

        // Clear previous data and add the updated LineDataSet to LineData
        lineData.clearValues();
        lineData.addDataSet(lineDataSet);

        // Set LineData to the LineChart
        lineChart.setData(lineData);

        // Redraw the chart
        lineChart.invalidate();
    }
}
