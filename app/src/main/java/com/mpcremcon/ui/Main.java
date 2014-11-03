package com.mpcremcon.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.mpcremcon.R;
import com.mpcremcon.localdb.LocalSettings;
import com.mpcremcon.network.BGService;
import com.mpcremcon.network.Commands;
import com.mpcremcon.network.MediaStatus;
import com.mpcremcon.network.SConnection;

/**
 * Main Activity
 *
 * Created by Oleh Chaplya on 3.10.2014.
 */
public class Main extends Activity {

    /**
     * General UI
     */
    ImageButton play;
    ImageButton stop;
    ImageButton fullscreen;
    ImageButton prev;
    ImageButton next;
    ImageButton mute;
    TextView tvCurTime;
    TextView tvFullTime;
    SeekBar timeSeekBar;
    SeekBar volumeBar;

    // additional controls
    public static Boolean isPaused = false;
    ImageView snapshot;

    SConnection serviceConnection;
    Handler uiHandler;
    Handler snapshotHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LocalSettings.init(getApplicationContext());

        // start background service for communication with player
        tvCurTime = (TextView) findViewById(R.id.tvCurrentTime);
        tvFullTime = (TextView) findViewById(R.id.tvFullTime);
        timeSeekBar = (SeekBar) findViewById(R.id.timeSeekBar);
        volumeBar = (SeekBar) findViewById(R.id.volumeBar);
        snapshot = (ImageView) findViewById(R.id.snapshot);

        initUiHandler();

        // initiate buttons
        play = (ImageButton)findViewById(R.id.ibPlay);
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                serviceConnection.sendReq(Commands.PLAY_PAUSE);
            }
        });

        stop = (ImageButton)findViewById(R.id.ibStop);
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                play.setBackground( getResources().getDrawable(android.R.drawable.ic_media_pause));
                serviceConnection.sendReq(Commands.Stop);
            }
        });

        prev = (ImageButton)findViewById(R.id.ibPrev);
        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                serviceConnection.sendReq(Commands.Prev);
            }
        });

        next = (ImageButton)findViewById(R.id.ibNext);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                serviceConnection.sendReq(Commands.Next);
            }
        });

        fullscreen = (ImageButton)findViewById(R.id.ibFullscreen);
        fullscreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                serviceConnection.sendReq(Commands.Fullscreen);
            }
        });

        mute = (ImageButton)findViewById(R.id.ibMute);
        mute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                serviceConnection.sendReq(Commands.Mute);
            }
        });

        // initiate time seek bar
        timeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                /*if(b) {
                    int v = (100*i)/seekBar.getMax();
                    if(v < 0) v = 0;
                    if(v >= 100) v = 99;
                    serviceConnection.setPosition(v);
                }*/
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int v = (100*seekBar.getProgress())/seekBar.getMax();
                if(v < 0) v = 0;
                if(v >= 100) v = 99;
                serviceConnection.setPosition(v);
            }
        });

        // set volume bar
        volumeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                /*if(b) {
                    serviceConnection.setVolume(i);
                }*/
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                serviceConnection.setVolume(seekBar.getProgress());
            }
        });
    }

    @Override
    protected void onPause() {

        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent p = new Intent(getApplicationContext(), PrefActivity.class);
            startActivity(p);
            return true;
        }
        if (id == R.id.action_exit) {
            try {
                unbindService(serviceConnection);
                finish();
            } catch(Exception e) {}
        }
        return super.onOptionsItemSelected(item);
    }

    private void ImageViewAnimatedChange(Context c, final ImageView v, final Bitmap new_image) {
        final Animation anim_out = AnimationUtils.loadAnimation(c, android.R.anim.fade_out);
        final Animation anim_in  = AnimationUtils.loadAnimation(c, android.R.anim.fade_in);
        anim_out.setAnimationListener(new Animation.AnimationListener()
        {
            @Override public void onAnimationStart(Animation animation) {}
            @Override public void onAnimationRepeat(Animation animation) {}
            @Override public void onAnimationEnd(Animation animation)
            {
                v.setImageBitmap(new_image);
                anim_in.setAnimationListener(new Animation.AnimationListener() {
                    @Override public void onAnimationStart(Animation animation) {}
                    @Override public void onAnimationRepeat(Animation animation) {}
                    @Override public void onAnimationEnd(Animation animation) {}
                });
                v.startAnimation(anim_in);
            }
        });
        v.startAnimation(anim_out);
    }

    private void initUiHandler() {
        Intent i = new Intent(this, BGService.class);
        startService(i);

        snapshotHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch(msg.what) {
                    case Commands.SNAPSHOT: {
                        try {
                            Bitmap bmp = (Bitmap) msg.obj;
                            if (bmp != null) {
                                //snapshot.setImageBitmap(bmp);
                                ImageViewAnimatedChange(getApplicationContext(), snapshot, bmp);
                            }
                        } catch (Exception e) {
                            Log.d("Main", "Error loading image");
                        }
                        break;
                    }
                }
            }
        };

        // update media time
        uiHandler = new Handler() {
            public void handleMessage(Message msg) {
            switch(msg.what) {
                case Commands.MEDIADATA: {
                    try {
                        MediaStatus ms = (MediaStatus) msg.obj;
                        if (ms != null) {
                            tvCurTime.setText(ms.getPositionString());
                            tvFullTime.setText(ms.getDurationString());

                            timeSeekBar.setMax(ms.getDuration());
                            timeSeekBar.setProgress(ms.getPosition());
                            setTitle(ms.getFilename() + " - Connected");

                            Main.isPaused = ms.getPlayState();

                            if(isPaused) {
                                play.setBackground( getResources().getDrawable(android.R.drawable.ic_media_pause));
                            } else {
                                play.setBackground( getResources().getDrawable(android.R.drawable.ic_media_play));
                            }

                            volumeBar.setProgress(ms.getVolumeLevel());
                        }
                    } catch(Exception e) {
                        Log.d("Main", e.getMessage());
                    }
                    break;
                }
                case Commands.DISCONNECTED: {
                    String oldTitle = getTitle().toString();
                    if(!oldTitle.endsWith("Disconnected"))
                        setTitle(getTitle() + " - Disconnected");
                }
            }
        };
    };

        serviceConnection = new SConnection(uiHandler, snapshotHandler);
        bindService(i, serviceConnection, Context.BIND_AUTO_CREATE);
    }
}
