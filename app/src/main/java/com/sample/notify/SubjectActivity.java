package com.sample.notify;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SpinnerAdapter;

import com.tiper.MaterialSpinner;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SubjectActivity extends AppCompatActivity {
    String przedmiot;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subject);
        final RadioGroup group = findViewById(R.id.rdbGroupZakres);
        MaterialSpinner materialSpinner = findViewById(R.id.material_spinner_przedmiot);
        SpinnerAdapter spinnerAdapter = ArrayAdapter.createFromResource(this,R.array.spinner_przedmiot,android.R.layout.simple_spinner_item);
        ((ArrayAdapter) spinnerAdapter).setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        materialSpinner.setAdapter(spinnerAdapter);
        materialSpinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener(){

            @Override
            public void onItemSelected(@NotNull MaterialSpinner materialSpinner, @Nullable View view, int i, long l) {
                group.setVisibility(View.VISIBLE);
                przedmiot = materialSpinner.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(@NotNull MaterialSpinner materialSpinner) {
                materialSpinner.setError("Wybierz przedmiot!");
            }
        });
        Button button = findViewById(R.id.button_subject);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectedId = group.getCheckedRadioButtonId();
                RadioButton rb=(RadioButton)findViewById(selectedId);
                Intent intent = new Intent(SubjectActivity.this,MapActivity.class);
                if (przedmiot!=null) {
                    intent.putExtra(MapActivity.PRZEDMIOT, przedmiot + " - " + rb.getText());
                    startActivity(intent);
                }else
                    materialSpinner.setError("Wybierz przedmiot!");

            }
        });
    }
}
