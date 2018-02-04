package com.example.mustafdahir.bithackathon;

import android.content.Intent;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.example.mustafdahir.bithackathon.fragments.HomeFragment;
import com.example.mustafdahir.bithackathon.fragments.LoginFragment;
import com.example.mustafdahir.bithackathon.fragments.RegisterFragment;
import com.example.mustafdahir.bithackathon.interfaces.OnStartInteractionListener;

public class StartActivity extends AppCompatActivity implements OnStartInteractionListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, new HomeFragment())
                .addToBackStack(null);
        transaction.commit();
    }

    /**
     * Switch case for different interactions
     * @param switchCase the case
     * @param startOptions which option user selects at home page
     * @param response response from async task
     * @param username the user name
     * @param password the password
     */
    @Override
    public void OnInteractionListener(String switchCase, String startOptions,
                                      String response, String username, String password) {
        FragmentTransaction transaction;
        switch(switchCase) {
            case "launch":
                switch(startOptions) {
                    case "login":
                        Log.d("tag", "OnInteractionListener: login in launch");
                        LoginFragment loginFragment = new LoginFragment();
                        transaction = getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.fragmentContainer, loginFragment)
                                .addToBackStack(null);
                        transaction.commit();
                        break;
                    case "register":
                        RegisterFragment registerFragment = new RegisterFragment();
                        transaction = getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.fragmentContainer, registerFragment)
                                .addToBackStack(null);
                        transaction.commit();
                }
                break;
            case "login":
                switch(response) {
                    case "Successful login":
                        //onRegisterFragmentInteraction(response, username, password);
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra("Username", username);
                        startActivity(intent);
                }
                break;
            case "register":
                switch(response) {
                    case "Successful Register":
                        //        Intent intent = new Intent(this, DashboardActivity.class);
                        Intent intent = new Intent(this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra("Username", username);
                        startActivity(intent);
                }
        }
    }
}
