package com.example.yuetung55.adventurebook;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private Button continueButton;
    private Button newGameButton;
    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        continueButton=(Button) findViewById(R.id.continueGame);
        newGameButton=(Button) findViewById(R.id.newGame);
        //Set button listeners
        continueButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Context context=view.getContext();
                        Intent intent=new Intent(context,StoryActivity.class);
                        startActivity(intent);
                    }
                }
        );
        newGameButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (user.noExistingInfo()) {
                            user.resetUserInfo();
                            Context context = view.getContext();
                            Intent intent = new Intent(context, StoryActivity.class);
                            intent.putExtra("reset", true);
                            startActivity(intent);
                            user.setExistingInfo(false);
                        }
                        else {
                            AlertDialog.Builder builder;
                            builder = new AlertDialog.Builder(MainActivity.this);
                            builder.setTitle("RESET GAME")
                                    .setMessage("Are you sure you want to reset the game? All existing data will be erased.")
                                    .setPositiveButton("GO AHEAD", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            Context context = getApplicationContext();
                                            Intent intent = new Intent(context, StoryActivity.class);
                                            intent.putExtra("reset", true);
                                            startActivity(intent);
                                            user.setExistingInfo(false);
                                        }
                                    })
                                    .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            // do nothing
                                        }
                                    })
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .show();
                        }
                    }
                }
        );

    }

    @Override
    protected void onStart() {
        //Hide continue Button if user is new
        user=User.getUser(getApplicationContext());
        if (user.noExistingInfo()) {
            continueButton.setVisibility(View.GONE);
            newGameButton.setText("New Game");
        } else {
            continueButton.setVisibility(View.VISIBLE);
            newGameButton.setText("Reset Game");
        }
        super.onStart();
    }
}
