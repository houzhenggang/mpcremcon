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
    public static Boolean isProgressBarMoving = false;
    public static Boolean isVolumeBarMoving = false;
    public static Boolean isPaused = false;
    ImageView snapshot;

    SConnection serviceConnection;
    Handler uiHandler;
    Intent service;

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
                serviceConnection.execCommand(Commands.PLAY_PAUSE);
            }
        });

        stop = (ImageButton)findViewById(R.id.ibStop);
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                serviceConnection.execCommand(Commands.STOP);
            }
        });

        prev = (ImageButton)findViewById(R.id.ibPrev);
        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                serviceConnection.execCommand(Commands.PREV);
            }
        });

        next = (ImageButton)findViewById(R.id.ibNext);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                serviceConnection.execCommand(Commands.NEXT);
            }
        });

        fullscreen = (ImageButton)findViewById(R.id.ibFullscreen);
        fullscreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                serviceConnection.execCommand(Commands.FULLSCREEN);
            }
        });

        mute = (ImageButton)findViewById(R.id.ibMute);
        mute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                serviceConnection.execCommand(Commands.MUTE);
            }
        });

        // initiate time seek bar
        timeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isProgressBarMoving = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int v = (100*seekBar.getProgress())/seekBar.getMax();
                if(v < 0) v = 0;
                if(v >= 100) v = 99;
                serviceConnection.setPosition(v);
                isProgressBarMoving = false;
            }
        });

        // set volume bar
        volumeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isVolumeBarMoving = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                serviceConnection.setVolume(seekBar.getProgress());
                isVolumeBarMoving = false;
            }
        });
    }

    @Override
    protected void onResume() {
        try {
            if (service != null && serviceConnection != null)
                bindService(service, serviceConnection, Context.BIND_AUTO_CREATE);
        } catch(Exception e) {
            Log.d("MAIN", "Can't bind to service");
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        try {
            if(serviceConnection != null)
                unbindService(serviceConnection);
        } catch(Exception e) {
            Log.d("MAIN", "Can't unbind the service");
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        uiHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id) {
            case R.id.action_settings: {
                // start settings activity
                Intent p = new Intent(getApplicationContext(), PrefActivity.class);
                startActivity(p);
                break;
            }
            case R.id.action_exit: {
                // exit application and unbind service
                try {
                    unbindService(serviceConnection);
                    finish();
                } catch(Exception e) {}
                break;
            }
            case R.id.action_close_mpc: {
                // close MPC player
                serviceConnection.execCommand(Commands.EXIT_PLAYER);
                break;
            }
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
        service = new Intent(this, BGService.class);
        startService(service);

        // update media time
        uiHandler = new Handler() {
            public void handleMessage(Message msg) {
            switch(msg.what) {
                case Commands.MEDIADATA: {
                    try {
                        MediaStatus ms = (MediaStatus) msg.obj;
                        if (ms != null) {
                            setTitle(ms.getFilename() + " - Connected");
                            tvCurTime.setText(ms.getPositionString());
                            tvFullTime.setText(ms.getDurationString());
                            timeSeekBar.setMax(ms.getDuration());

                            if(!isProgressBarMoving)
                                timeSeekBar.setProgress(ms.getPosition());

                            // check pause state
                            Main.isPaused = ms.getPlayState();

                            if(isPaused) {
                                play.setBackground( getResources().getDrawable(R.drawable.pause));
                            } else {
                                play.setBackground( getResources().getDrawable(R.drawable.play));
                            }

                            // check mute status
                            if(ms.getIsMuted()) {
                                mute.setBackground( getResources().getDrawable(R.drawable.mute_on));
                            } else {
                                mute.setBackground( getResources().getDrawable(R.drawable.mute_off));
                            }

                            if(!isVolumeBarMoving)
                                volumeBar.setProgress(ms.getVolumeLevel());
                        }
                    } catch(Exception e) {
                        Log.d("Main", e.getMessage());
                    }
                    break;
                }
                case Commands.DISCONNECTED: {
                    setTitle("Disconnected");
                    break;
                }
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
        };
    };

        serviceConnection = new SConnection(uiHandler);
        bindService(service, serviceConnection, Context.BIND_AUTO_CREATE);
    }
}
