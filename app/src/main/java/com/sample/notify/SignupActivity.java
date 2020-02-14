package com.sample.notify;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.chaos.view.PinView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.annotations.NotNull;
import com.hbb20.CountryCodePicker;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.sample.notify.BaseClasses.User;
import com.shuhart.stepview.StepView;
import com.theartofdev.edmodo.cropper.CropImage;
import com.tiper.MaterialSpinner;

import java.util.concurrent.TimeUnit;

public class SignupActivity extends AppCompatActivity {

    private int currentStep = 0;
    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    PhoneAuthCredential mCredential;
    EditText edtPhoneNumber;
    ProgressDialog progressDialog;
    androidx.appcompat.app.ActionBar actionBar;
    private View layout1,layout2,layout3;
    private String verificationCode;
     String phoneNumber,name,poziom,system,klasa;
    private DatabaseReference database;
    StepView stepView;
    private Button sendCodeButton, verifyCodeButton, button3;
    private PinView verifyCodeET;
    AlertDialog profile_dialog;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private TextView phonenumberText;
    CircularImageView imageButton;
    private Uri filePath = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        //initialize UI elements
        edtPhoneNumber = findViewById(R.id.edtPhoneNumber);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        progressDialog = new ProgressDialog(this);

        firebaseAuth = FirebaseAuth.getInstance(); //Initialize cloud firebase authentication
        layout1 = findViewById(R.id.layout1);
        layout2 = findViewById(R.id.layout2);
        layout3 = findViewById(R.id.layout3);
        stepView = findViewById(R.id.step_view);
        stepView.setStepsNumber(3);
        stepView.go(0, true);
        sendCodeButton = (Button) findViewById(R.id.submit1);
        verifyCodeButton = (Button) findViewById(R.id.submit2);
        button3 = (Button) findViewById(R.id.submit3);
        verifyCodeET = (PinView) findViewById(R.id.pinView);
        phonenumberText = (TextView) findViewById(R.id.phonenumberText);
        edtPhoneNumber.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
        getmCallback();
        sendCode();
        verifyCode();
        button3();
        setSpinners();
        userProfilePhoto();
    }
    private void sendCode(){
    sendCodeButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            CountryCodePicker cpp = findViewById(R.id.ccp);
            String number = edtPhoneNumber.getText().toString();
            number = number.replace(" ", "");
            phoneNumber = "+"+cpp.getSelectedCountryCode()+number;
            phonenumberText.setText(phoneNumber);

            if (TextUtils.isEmpty(phoneNumber)) {
                edtPhoneNumber.setError("Enter a Phone Number");
                edtPhoneNumber.requestFocus();
            } else if (edtPhoneNumber.getText().toString().length() !=11) {
                edtPhoneNumber.setError("Please enter a valid phone");
                edtPhoneNumber.requestFocus();
            } else {

            if (currentStep < stepView.getStepCount() - 1) {
                currentStep++;
                stepView.go(currentStep, true);
            } else {
                stepView.done(true);
            }
            PhoneAuthProvider phoneAuthProvider = PhoneAuthProvider.getInstance();
                phoneAuthProvider.verifyPhoneNumber(
                        phoneNumber,
                        60L,
                        TimeUnit.SECONDS,
                        SignupActivity.this,
                        mCallbacks);
            layout1.setVisibility(View.GONE);
            layout2.setVisibility(View.VISIBLE);
            }
        }
    });
    }
    private void verifyCode() {
        verifyCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String verificationAnswer = verifyCodeET.getText().toString();
                if (verificationCode.isEmpty()) {
                    Toast.makeText(SignupActivity.this, "Enter verification code", Toast.LENGTH_SHORT).show();
                } else {
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationCode, verificationAnswer);
                    signInWithPhoneAuthCredential(credential);
                }
                if (currentStep < stepView.getStepCount() - 1) {
                    currentStep++;
                    stepView.go(currentStep, true);
                } else {
                    stepView.done(true);
                }
                layout1.setVisibility(View.GONE);
                layout2.setVisibility(View.GONE);
                layout3.setVisibility(View.VISIBLE);
            }
        });
    }
    private void button3() {
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText f = findViewById(R.id.edtFirstName);
                name = f.getText().toString();
                if(name.isEmpty()){
                    f.setError("Enter first name");
                    f.requestFocus();
                }else{
                if (currentStep < stepView.getStepCount() - 1) {
                    currentStep++;
                    stepView.go(currentStep, true);
                } else {
                    stepView.done(true);
                }
                LayoutInflater inflater = getLayoutInflater();
                View alertLayout = inflater.inflate(R.layout.profile_create_dialog, null);
                AlertDialog.Builder show = new AlertDialog.Builder(SignupActivity.this);
                show.setView(alertLayout);
                show.setCancelable(false);
                profile_dialog = show.create();
                profile_dialog.show();
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        saveData();
                        profile_dialog.dismiss();
                        startActivity(new Intent(SignupActivity.this, MainActivity.class));
                        finish();
                    }
                }, 2000);
                }
            }
        });
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        if (currentStep < stepView.getStepCount() - 1) {
                            currentStep++;
                            stepView.go(currentStep, true);
                        } else {
                            stepView.done(true);
                        }
                        layout1.setVisibility(View.GONE);
                        layout2.setVisibility(View.GONE);
                        layout3.setVisibility(View.VISIBLE);
                        } else {
                        Toast.makeText(SignupActivity.this,"Something wrong",Toast.LENGTH_SHORT).show();
                        Log.w("cos", "signInWithCredential:failure", task.getException());
                        if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                            // The verification code entered was invalid
                        }
                    }
        }});
    }
    private void saveData(){
        user = firebaseAuth.getCurrentUser();
        if(system != null) {
            poziom = klasa + " " + poziom + " - " + system;
        }else {poziom = klasa + " " + poziom;}
        User userProfile = new User(name,poziom);
        database =  FirebaseDatabase.getInstance().getReference();
        database.child("users").child(user.getPhoneNumber()).setValue(userProfile);
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setPhotoUri(filePath).setDisplayName(name).build();
        user.updateProfile(profileUpdates);
    }
    private void getmCallback(){
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                Toast.makeText(getApplicationContext(),"costam ",Toast.LENGTH_SHORT).show();
                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
                Log.d("onVerificationFailed", "onVerificationFailed: "+e.getMessage());
                Toast.makeText(getApplicationContext(),"costam2",Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                verificationCode = verificationId;
                Toast.makeText(getApplicationContext(),"costam3",Toast.LENGTH_SHORT).show();
            }
        };
    }
    private void userProfilePhoto(){
        imageButton = findViewById(R.id.user_profile_photo);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setFixAspectRatio(true)
                        .start(SignupActivity.this);
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                filePath = result.getUri();
                imageButton.setImageURI(filePath);
                imageButton.setScaleType(ImageView.ScaleType.CENTER_CROP);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(this, error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }
    private String[] klasaArrays(){
        String[] array;
        switch (poziom) {
            case "Liceum":
                array = new String[]{"I", "II", "III"};
                break;
            case "Technikum":
                array = new String[]{"I", "II", "III","IV"};
                break;
            case "Szkoła podstawowa":
                array = new String[]{"I", "II", "III","IV","V", "VI", "VII","VIII"};
                break;
            default: array = new String[] {};
        }
        return array;
    }
    private void setSpinners(){

        MaterialSpinner materialSpinner1 = findViewById(R.id.material_spinner_1);
        MaterialSpinner materialSpinner2 = findViewById(R.id.material_spinner_2);
        RadioGroup rdbGroup = findViewById(R.id.rdbGroupSystem);
        //
        SpinnerAdapter spinnerAdapter = ArrayAdapter.createFromResource(this,R.array.spinner_poziom,android.R.layout.simple_spinner_item);
        ((ArrayAdapter) spinnerAdapter).setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        materialSpinner1.setAdapter(spinnerAdapter);
        materialSpinner1.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(@NotNull MaterialSpinner materialSpinner, @Nullable View view, int i, long l) {
                poziom = materialSpinner.getSelectedItem().toString();
                ArrayAdapter<String> spinnerAdapter2 = new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_spinner_item,klasaArrays());
                spinnerAdapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                materialSpinner2.setAdapter(spinnerAdapter2);
                materialSpinner2.setSelection(ListView.INVALID_POSITION);
            }

            @Override
            public void onNothingSelected(@NotNull MaterialSpinner materialSpinner) {
                materialSpinner.setError("Wybierz poziom");
                materialSpinner.requestFocus();
            }
        });
        materialSpinner2.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(@NotNull MaterialSpinner materialSpinner, @Nullable View view, int i, long l) {
                klasa =materialSpinner.getSelectedItem().toString();
                if( (!poziom.equals("Szkoła podstawowa")) && (klasa.equals("I")) ){
                    rdbGroup.setVisibility(View.VISIBLE);
                    int id = rdbGroup.getCheckedRadioButtonId();
                    if(id!=-1){
                        RadioButton radioButton = findViewById(id);
                        system=radioButton.getText().toString();
                    }
                }else if(poziom == null){
                    materialSpinner.setError("Wybierz najpierw przedmiot");
                    klasa = null;
                    system = null;
                }
                else {
                    rdbGroup.setVisibility(View.INVISIBLE);
                    system=null;
                }
            }

            @Override
            public void onNothingSelected(@NotNull MaterialSpinner materialSpinner) {
                if(poziom == null){
                    materialSpinner.setError("Wybierz najpierw przedmiot");
                }
            }
        });
    }
}
