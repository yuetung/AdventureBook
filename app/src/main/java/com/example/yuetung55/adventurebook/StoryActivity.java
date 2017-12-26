package com.example.yuetung55.adventurebook;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Random;

public class StoryActivity extends AppCompatActivity {

    private TextView storyText;
    private Button[] buttons=new Button[5];
    private TextView resourceText;
    ResourceManager resourceManager;
    StoryManager storyManager;
    User user;
    StoryNode currentStory;
    HashMap<String,Resource> resources;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story);
        // Initialize all textViews and Buttons
        storyText=(TextView) findViewById(R.id.storyText);
        resourceText=(TextView) findViewById(R.id.resourceText);
        buttons[0]=(Button) findViewById(R.id.button1);
        buttons[1]=(Button) findViewById(R.id.button2);
        buttons[2]=(Button) findViewById(R.id.button3);
        buttons[3]=(Button) findViewById(R.id.button4);
        buttons[4]=(Button) findViewById(R.id.button5);
        //Initialize User, Resources and Stories, call resetResources(), resetStory() or resetAll() to reset
        user=User.getUser(getApplicationContext());
        resourceManager=ResourceManager.getInstance(getApplicationContext());
        storyManager=StoryManager.getInstance(getApplicationContext());
        if (getIntent().getBooleanExtra("reset",false)) {
            resetAll();
        }
        refresh();
        //Button listeners
        buttons[0].setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        StoryPaths storyPath=currentStory.getStoryPaths().get(0);
                        checkBeforeMoving(storyPath);
                    }
                }
        );
        buttons[1].setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        StoryPaths storyPath=currentStory.getStoryPaths().get(1);
                        checkBeforeMoving(storyPath);
                    }
                }
        );
        buttons[2].setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        StoryPaths storyPath=currentStory.getStoryPaths().get(2);
                        checkBeforeMoving(storyPath);
                    }
                }
        );
        buttons[3].setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        StoryPaths storyPath=currentStory.getStoryPaths().get(3);
                        checkBeforeMoving(storyPath);
                    }
                }
        );
        buttons[4].setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        StoryPaths storyPath=currentStory.getStoryPaths().get(4);
                        checkBeforeMoving(storyPath);
                    }
                }
        );
    }

    /* Reset all resources without updating story. Useful for debugging. */
    private void resetResources() {
        resourceManager.resetAllResources();
        resourceManager=ResourceManager.getInstance(getApplicationContext());  //need update resourceManager upon reset
    }

    /* Reset and update story into database. changes user's current story node to 0 */
    private void resetStory() {
        storyManager.resetAllStories();
        user.setCurrentStoryNode(0);
    }

    /* Reset both resources and story. */
    private void resetAll() {
        resetResources();
        resetStory();
        refresh();
    }

    /* re-populate all textViews and buttons based on user data */
    private void refresh() {
        System.out.println("refresh is called");
        currentStory=storyManager.getStory(user.getCurrentStoryNode());
        resources=resourceManager.getResources();
        storyText.setText(currentStory.getText());
        // Set button texts, add foreground locks for options without conditions met
        for (int i=0; i<currentStory.getStoryPaths().size(); i++) {
            StoryPaths storyPath=currentStory.getStoryPaths().get(i);
            Resource resourceNeeded=storyPath.getResourceNeeded();
            if (resourceNeeded!=null) {
                int amountNeeded=storyPath.getAmountNeeded();
                if (resourceNeeded.getDepletable()) {
                    buttons[i].setText(storyPath.getOptionText() + " (Consume " + amountNeeded + " " + resourceNeeded.getName() + ")");
                } else {
                    buttons[i].setText(storyPath.getOptionText() + " (Require " +resourceNeeded.getName() + ")");
                }
                if (resourceNeeded.getStock() < amountNeeded) {
                    buttons[i].setForeground(getDrawable(R.drawable.button_box_lock_sample));
                } else {
                    buttons[i].setForeground(null);
                }
            } else {
                buttons[i].setText(storyPath.getOptionText());
                buttons[i].setForeground(null);
            }
            buttons[i].setVisibility(View.VISIBLE);
            updateResourceText();
        }
        // Make the other buttons disappear
        for (int i=currentStory.getStoryPaths().size(); i<5; i++) {
            buttons[i].setVisibility(View.GONE);
        }
    }

    //TODO: if possible print out resources in different fragment (a bag or something)
    /* Prints out all available resources that are non-zero */
    private void updateResourceText() {
        String resourceTextString="Available resources: \n";
        boolean none=true;
        for (Resource resource :resources.values()) {
            if (resource.getStock()>0) {
                resourceTextString+=resource.getName()+": "+resource.getStock()+"\n";
                none=false;
            }
        }
        if (none) {resourceTextString="";}
        resourceText.setText(resourceTextString);
    }

    /* check resource requirements and chance event dialogue before moving to next story node */
    private void checkBeforeMoving(final StoryPaths storyPath) {
        // Check resource requirements
        Resource resourceNeeded=storyPath.getResourceNeeded();
        if (resourceNeeded!=null) {
            int amountNeeded=storyPath.getAmountNeeded();
            if (resourceNeeded.getStock() < amountNeeded) {
                //TODO: change resourceNeeded to resourcesNeeded to support multiple resource requirements. need modify StoryPaths parsing.
                Toast.makeText(getApplicationContext(),"Requires: "+amountNeeded+" "+resourceNeeded.getName(),Toast.LENGTH_SHORT).show();
                return;
            }
        }
        // Create chance event prompt
        if (storyPath.isChanceEvent()) {
            double chance = storyPath.getChance();
            AlertDialog.Builder builder;
            builder = new AlertDialog.Builder(StoryActivity.this);
            builder.setTitle("CHANCE EVENT!")
                    .setMessage("You estimate that you have a " + Math.round((1-chance) * 100) + "% chance of succeeding in this action. Continue?")
                    .setPositiveButton("CONTINUE", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            moveToNextStoryNode(storyPath);
                        }
                    })
                    .setNegativeButton("BACK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // do nothing
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        } else {
            moveToNextStoryNode(storyPath);
        }
    }


    /* Move to next StoryNode */
    private void moveToNextStoryNode(StoryPaths storyPath) {
        System.out.println("moving to next storynode");  //debug
        //Depletes resources
        Resource resourceNeeded=storyPath.getResourceNeeded();
        if (resourceNeeded!=null) {
            int amountNeeded=storyPath.getAmountNeeded();
            if (resourceNeeded.getDepletable()) {
                resourceNeeded.decreaseStock(amountNeeded);
                resourceManager.updateDatabase(resourceNeeded);
            }
        }
        int nextPage=storyPath.getNextPage();
        // for chane event: decide success or failure
        if (storyPath.isChanceEvent()) {
            double chance=storyPath.getChance();
            boolean failure=Math.random()<chance;
            if (failure) {
                nextPage=storyPath.getNextPage2();
                Toast.makeText(getApplicationContext(),"Failure",Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(),"Succeed",Toast.LENGTH_SHORT).show();
            }
        }
        // Move to next story node
        user.setCurrentStoryNode(nextPage);
        user.updateDatabase();
        refresh();
        //Obtain new items
        //TODO: let user click something to obtain the item instead of obtaining automatically
        Resource resourceObtained=currentStory.getResourceGained();
        if (resourceObtained!=null) {
            int amountGained = currentStory.getAmountGained();
            resourceObtained.increaseStock(amountGained);
            resourceManager.updateDatabase(resourceObtained);
        }
        updateResourceText();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        resourceManager.close();
        storyManager.close();
    }
}
