package com.example.rat;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG =
            MainActivity.class.getSimpleName();
    private boolean start=false;
    JSONArray listData = new JSONArray();
    private int timeconsume=0;
    private int score = 0;
    private String username="";
    private ImageView mouse;
    AnimationDrawable animation ;

    private FrameLayout holder;
    private AnimationDrawable bgDrawable;
    private Handler eventhandler;

    private Handler timehandler = new Handler();
    Thread thread;

    private TextView TIME;
    private TextView SCORE;

    private TextView PLAYER;
    public int[][] position = new int[][] {
            { 360, 250 },{ 750, 250 },{ 1200, 250 },
            { 300, 450 },{ 750, 450 },{ 1250, 430 },
            { 280, 650 },{ 750, 650 },{ 1300, 650 }
    };

    private Runnable timerunnable = new Runnable() {
        @Override
        public void run() {
            timeconsume++;
            TIME.setText(String.valueOf(timeconsume)+"s");
            timehandler.postDelayed(this, 1000);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mouse = findViewById(R.id.mouseholder);



        TIME=findViewById(R.id.time);
        SCORE=findViewById(R.id.score);
        PLAYER=findViewById(R.id.player);

        holder = findViewById(R.id.holder);
        bgDrawable = (AnimationDrawable) holder.getBackground();
        bgDrawable.start();
        makeSideAnimation();

        showUsernameDialog();
        SharedPreferences preferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String listData_str = preferences.getString("listData", null);


        if (listData_str!=null){
            Log.d(LOG_TAG,listData_str);
            try {
                listData = new JSONArray(listData_str);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }



    }
    public void makeSideAnimation(){
        ImageView SIDE1=findViewById(R.id.side1active);
        ImageView SIDE2=findViewById(R.id.side2active);
        ImageView SIDE3=findViewById(R.id.side3active);
        ImageView SIDE4=findViewById(R.id.side4active);
        Animation.AnimationListener animationListener;
        @SuppressLint("ResourceType") Animation side1fadeInOutAnimation= AnimationUtils.loadAnimation(this, R.drawable.side1animation);
        animationListener = new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                SIDE1.startAnimation(side1fadeInOutAnimation);
                SIDE2.startAnimation(side1fadeInOutAnimation);
                SIDE3.startAnimation(side1fadeInOutAnimation);
                SIDE4.startAnimation(side1fadeInOutAnimation);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        };

        side1fadeInOutAnimation.setAnimationListener(animationListener);
        SIDE1.startAnimation(side1fadeInOutAnimation);
        SIDE2.startAnimation(side1fadeInOutAnimation);
        SIDE3.startAnimation(side1fadeInOutAnimation);
        SIDE4.startAnimation(side1fadeInOutAnimation);


    }

    public void killmouse(View v) { animation.stop();
        mouse.setBackgroundResource(R.drawable.attack);

        score++;
        SCORE.setText("Get:"+String.valueOf(score));

    }
    public void makeTread(){
        thread = new Thread(new Runnable() {

            @Override
            public void run() {
                int index = 0;
                while (!Thread.currentThread().isInterrupted()) {
                    if (!start) {

                        mouse.setVisibility(View.INVISIBLE);

                        break;
                    }
                    index = new Random().nextInt(position.length);
                    Message m = eventhandler.obtainMessage();
                    m.what = 0x101;
                    m.arg1 = index;
                    eventhandler.sendMessage(m);


                    try {
                        Thread.sleep(new Random().nextInt(500) +500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }

            }

        });
        thread.start();
    }
    public void makeHandle(){
        eventhandler = new Handler() {

            public void handleMessage(Message msg) {
                int index = 0;

                if (msg.what == 0x101) {
                    index = msg.arg1;
                    mouse.setX(position[index][0]);
                    mouse.setY(position[index][1]);
                    mouse.setVisibility(View.VISIBLE);
                    animation = (AnimationDrawable) getResources().getDrawable(R.drawable.animation_list);
                    mouse.setBackground(animation);
                    animation.stop();
                    animation.start();

                }
                super.handleMessage(msg);
            }
        };
    }

    public void newGame(View view) {
        showUsernameDialog();
    }
    private void showUsernameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter player name");


        EditText input = new EditText(this);
        builder.setView(input);


        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    stopGame();
                }catch (Exception e){

                }

                start=true;
                PLAYER.setText("(player)");
                timeconsume=0;
                score=0;
                SCORE.setText("0");
                TIME.setText("0s");
                username= input.getText().toString();
                PLAYER.setText(username);
                makeHandle();
                makeTread();
                timehandler.postDelayed(timerunnable, 1000);


            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        Dialog dialog = builder.create();
        dialog.show();
    }

    public void stop(View view) {
        if (start){
            stopGame();
        }

    }

    public void stopGame(){
        start=false;

        eventhandler.removeCallbacksAndMessages(null);
        timehandler.removeCallbacks(timerunnable);
        Toast.makeText(MainActivity.this, "You can find the score in rank list",
                Toast.LENGTH_SHORT).show();
        try {
            save();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

    }
    public void save() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("player", PLAYER.getText().toString());
        json.put("time", TIME.getText().toString());
        json.put("score", SCORE.getText().toString());



        JSONArray newArray = new JSONArray();

        newArray.put(json);

        for (int i = 0; i < listData.length(); i++) {
            newArray.put(listData.get(i));
        }

        listData = newArray;



        SharedPreferences preferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString("listData", listData.toString());
        editor.apply();

    }
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        try {
            save();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

    }

    public void rank(View view) {

        ArrayList<JSONObject> dataList = new ArrayList<>();
        for (int i = 0; i < listData.length(); i++) {
            try {
                JSONObject item = listData.getJSONObject(i);
                dataList.add(item);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }


        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);


        ListView listView = new ListView(MainActivity.this);


        Log.d(LOG_TAG,dataList.toString());
        CustomAdapter adapter = new CustomAdapter(MainActivity.this, dataList);
        listView.setAdapter(adapter);


        builder.setView(listView);


        AlertDialog dialog = builder.create();
        dialog.show();

    }

    public class CustomAdapter extends ArrayAdapter<JSONObject> {
        private Context context;
        private ArrayList<JSONObject> data;

        public CustomAdapter(Context context, ArrayList<JSONObject> data) {
            super(context, 0, data);
            this.context = context;
            this.data = data;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.item_layout, parent, false);
            }
            JSONObject currentItem = data.get(position);


            TextView playerTV = convertView.findViewById(R.id.playerTV);
            TextView scoreTV = convertView.findViewById(R.id.scoreTV);
            TextView timeTV = convertView.findViewById(R.id.timeTV);


            try {
                String player = currentItem.getString("player");
                String score = currentItem.getString("score");
                String time = currentItem.getString("time");
                Log.d(LOG_TAG,player);

                playerTV.setText(player);
                scoreTV.setText(score);
                timeTV.setText(time);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return convertView;
        }
    }


}