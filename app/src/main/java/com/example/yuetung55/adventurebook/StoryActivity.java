package com.example.yuetung55.adventurebook;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;

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
        resetAll();
        refresh();
        //Button listeners
        buttons[0].setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        StoryPaths storyPath=currentStory.getStoryPaths().get(0);
                        moveToNextStoryNode(storyPath);
                    }
                }
        );
        buttons[1].setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        StoryPaths storyPath=currentStory.getStoryPaths().get(1);
                        moveToNextStoryNode(storyPath);
                    }
                }
        );
        buttons[2].setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        StoryPaths storyPath=currentStory.getStoryPaths().get(2);
                        moveToNextStoryNode(storyPath);
                    }
                }
        );
        buttons[3].setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        StoryPaths storyPath=currentStory.getStoryPaths().get(3);
                        moveToNextStoryNode(storyPath);
                    }
                }
        );
        buttons[4].setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        StoryPaths storyPath=currentStory.getStoryPaths().get(4);
                        moveToNextStoryNode(storyPath);
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
        currentStory=storyManager.getStory(user.getCurrentStoryNode());
        resources=resourceManager.getResources();
        storyText.setText(currentStory.getText());
        // Set button texts, add foreground locks for options without conditions met
        for (int i=0; i<currentStory.getStoryPaths().size(); i++) {
            StoryPaths storyPath=currentStory.getStoryPaths().get(i);
            buttons[i].setText(storyPath.getOptionText());
            buttons[i].setVisibility(View.VISIBLE);
            Resource resourceNeeded=storyPath.getResourceNeeded();
            if (resourceNeeded!=null) {
                int amountNeeded=storyPath.getAmountNeeded();
                if (resourceNeeded.getStock() < amountNeeded) {
                    buttons[i].setForeground(getDrawable(R.drawable.button_box_lock_sample));
                } else {
                    buttons[i].setForeground(null);
                }
            } else {
                buttons[i].setForeground(null);
            }
            //Prints out all available resources that are non-zero
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

        // Make the other buttons disappear
        for (int i=currentStory.getStoryPaths().size(); i<5; i++) {
            buttons[i].setVisibility(View.GONE);
        }
    }

    /* Move to next StoryNode */
    private void moveToNextStoryNode(StoryPaths storyPath) {
        Resource resourceNeeded=storyPath.getResourceNeeded();
        if (resourceNeeded!=null) {
            int amountNeeded=storyPath.getAmountNeeded();
            if (resourceNeeded.getStock() < amountNeeded) {
                //TODO: change resourceNeeded to resourcesNeeded to support multiple resource requirements. need modify StoryPaths parsing.
                Toast.makeText(getApplicationContext(),"Requires: "+amountNeeded+" "+resourceNeeded.getName(),Toast.LENGTH_SHORT).show();
                return;
            } else {
                if (resourceNeeded.getDepletable()) {
                    resourceNeeded.decreaseStock(amountNeeded);
                    resourceManager.updateDatabase(resourceNeeded.getName());
                }
            }
        }
        int nextPage=storyPath.getNextPage();
        user.setCurrentStoryNode(nextPage);
        //Obtain new items
        //TODO: let user click something to obtain the item instead of obtaining automatically
        StoryNode newStory=storyManager.getStory(user.getCurrentStoryNode());
        Resource resourceObtained=newStory.getResourceGained();
        if (resourceObtained!=null) {
            int amountGained = newStory.getAmountGained();
            resourceObtained.increaseStock(amountGained);
        }
        refresh();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        resourceManager.close();
        storyManager.close();
    }
}
