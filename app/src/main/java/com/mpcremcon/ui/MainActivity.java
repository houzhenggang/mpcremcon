package com.mpcremcon.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.mpcremcon.R;
import com.mpcremcon.filebrowser.MediaEntity;
import com.mpcremcon.filebrowser.MediaEntityList;
import com.mpcremcon.filebrowser.MediaFormats;
import com.mpcremcon.filebrowser.MediaListAdapter;
import com.mpcremcon.localdb.LocalSettings;
import com.mpcremcon.network.BackgroundService;
import com.mpcremcon.network.Commands;
import com.mpcremcon.network.Connection;
import com.mpcremcon.network.MediaStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * Main Activity
 *
 * Created by Oleh Chaplya on 3.10.2014.
 */
public class MainActivity extends Activity {

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
    ImageView snapshot;
    ListView mediaList;
    DrawerLayout mDrawerLayout;
    DrawerLayout mDrawerLayoutEmptyList;
    ActionBarDrawerToggle mDrawerToggle;
    ActionBarDrawerToggle mDrawerToggleEmptyList;
    ActionBar actionBar;
    ImageView animUpdate;
    Animation rotation;
    MenuItem updateItem;

    // list drawer state controls
    public static Boolean IS_DATA_UPDATING = false;
    private static Boolean CAN_TITLE_CHANGE = false;
    private static Boolean IS_CONNECTED = false;
    private static Double LEFT_LIST_SIZE_PERCENT = 0.7;
    private static int MAX_LEFT_LIST_SIZE = 400;
    private static int MIX_LEFT_LIST_SIZE = 320;

    MediaListAdapter adapter;
    private List<MediaEntity> dataList = new ArrayList<MediaEntity>();

    // additional state controls
    public static Boolean isProgressBarMoving = false;
    public static Boolean isVolumeBarMoving = false;
    public static Boolean isPaused = false;

    public static Intent service;
    Connection serviceConnection;
    Handler uiHandler;

    String lastDirPath = "";
    private Menu actionMenu;

    Bitmap lastDownloadedBitmap = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LocalSettings.init(getApplicationContext());

        initViewIds();
        initHandlers();
        initActionBarDrawer();
        initDrawerList();
        initUiListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            if (service != null && serviceConnection != null)
                bindService(service, serviceConnection, Context.BIND_AUTO_CREATE);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            if(serviceConnection != null)
                unbindService(serviceConnection);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if(serviceConnection != null)
                unbindService(serviceConnection);
                uiHandler.removeCallbacksAndMessages(null);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        actionMenu = menu;

        LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        animUpdate = (ImageView)inflater.inflate(R.layout.iv_refresh, null);
        rotation = AnimationUtils.loadAnimation(this, R.anim.rotate_refresh);
        rotation.setRepeatCount(Animation.INFINITE);

        updateItem = actionMenu.findItem(R.id.action_update);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        int id = item.getItemId();
        switch(id) {

            case R.id.action_settings: {
                Intent p = new Intent(this, SettingsActivity.class);
                startActivity(p);
                break;
            }
            case R.id.action_close_mpc: {
                serviceConnection.execCommand(Commands.EXIT_PLAYER);
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void ImageViewAnimatedChange(Context c, final ImageView v, final Bitmap new_image) {
        final Animation anim_out = AnimationUtils.loadAnimation(c, R.anim.fade_out);
        final Animation anim_in  = AnimationUtils.loadAnimation(c, R.anim.fade_in);
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

    private void startAnimUpdate() {
        if(updateItem != null) {
            updateItem.setActionView(animUpdate);
            updateItem.getActionView().startAnimation(rotation);
            updateItem.setVisible(true);
            updateItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }
    }

    private void stopAnimUpdate() {
        try {
            if(updateItem != null) {
                updateItem.getActionView().clearAnimation();
                updateItem.setVisible(false);
                updateItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *  Called whenever we call invalidateOptionsMenu()
     *  Hides actionbar menu buttons
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mediaList);
        menu.findItem(R.id.action_close_mpc).setVisible(!drawerOpen);
        menu.findItem(R.id.action_settings).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    private void initViewIds() {
        tvCurTime = (TextView) findViewById(R.id.tvCurrentTime);
        tvFullTime = (TextView) findViewById(R.id.tvFullTime);
        timeSeekBar = (SeekBar) findViewById(R.id.timeSeekBar);
        volumeBar = (SeekBar) findViewById(R.id.volumeBar);
        snapshot = (ImageView) findViewById(R.id.snapshot);
        mediaList = (ListView) findViewById(R.id.left_drawer);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        actionBar = getActionBar();
    }

    private void initUiListeners() {
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

    private void initActionBarDrawer() {
        // normal list drawer
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.arrows_left, R.drawable.arrows_left_double_32, R.drawable.arrows_right_double_31) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                CAN_TITLE_CHANGE = true;
                stopAnimUpdate();
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                CAN_TITLE_CHANGE = false;
                queryMediaBrowser(lastDirPath);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onBackPressed() {
        if(mDrawerLayout.isDrawerOpen(mediaList))
            mDrawerLayout.closeDrawers();
        else super.onBackPressed();
    }

    /**
     * Create and set list adapter
     */
    private void initDrawerList() {
        adapter = new MediaListAdapter(this, dataList);

        // add adapter
        mediaList.setAdapter(adapter);
        mediaList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id) {
                MediaEntity i = adapter.getItem(pos);
                if(!IS_DATA_UPDATING) {
                    if(i.getMediaType() == MediaFormats.Type.AUDIO || i.getMediaType() == MediaFormats.Type.VIDEO) {
                        mDrawerLayout.closeDrawers();
                        queryMediaBrowser(i.getDirPath());
                    } else {
                        lastDirPath = i.getDirPath();
                        queryMediaBrowser(i.getDirPath());
                    }
                }
            }
        });

        // set max left drawer width
        int width = (int) Math.round(getResources().getDisplayMetrics().widthPixels * LEFT_LIST_SIZE_PERCENT);
        DrawerLayout.LayoutParams params = (android.support.v4.widget.DrawerLayout.LayoutParams) mediaList.getLayoutParams();

        if(width > MAX_LEFT_LIST_SIZE) params.width = MAX_LEFT_LIST_SIZE;
        else if(width <= MIX_LEFT_LIST_SIZE) params.width = MIX_LEFT_LIST_SIZE;
        else params.width = width;

        mediaList.setLayoutParams(params);
    }

    /**
     * Init handlers
     */
    private void initHandlers() {
        service = new Intent(this, BackgroundService.class);
        startService(service);

        // update media time
        uiHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch(msg.what) {
                    case Commands.MEDIADATA: {
                        try {
                            MediaStatus ms = (MediaStatus) msg.obj;
                            if (ms != null) {
                                if(CAN_TITLE_CHANGE) setTitle(ms.getFilename());
                                tvCurTime.setText(ms.getPositionString());
                                tvFullTime.setText(ms.getDurationString());
                                timeSeekBar.setMax(ms.getDuration());

                                if(!isProgressBarMoving) {
                                    timeSeekBar.setProgress(ms.getPosition());
                                    timeSeekBar.setSecondaryProgress(ms.getPosition());
                                }

                                // check pause state
                                MainActivity.isPaused = ms.getPlayState();

                                if(isPaused) {
                                    play.setBackground( getResources().getDrawable(R.drawable.music_pause_button));
                                } else {
                                    play.setBackground( getResources().getDrawable(R.drawable.music_play_button));
                                }

                                // check mute status
                                if(ms.getIsMuted()) {
                                    mute.setBackground( getResources().getDrawable(R.drawable.music_mute));
                                } else {
                                    mute.setBackground( getResources().getDrawable(R.drawable.music_volume_up));
                                }

                                if(!isVolumeBarMoving) {
                                    volumeBar.setProgress(ms.getVolumeLevel());
                                    volumeBar.setSecondaryProgress(ms.getPosition());
                                }
                            }
                        } catch(Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                    case Commands.DISCONNECTED: {
                        // change title and side drawer
                        if(CAN_TITLE_CHANGE) setTitle("Disconnected");
                        break;
                    }
                    case Commands.SNAPSHOT: {
                        try {
                            Bitmap newBitmap = (Bitmap) msg.obj;
                            //if (newBitmap != null)
                            ImageViewAnimatedChange(getApplicationContext(), snapshot, newBitmap);
                            /*if (newBitmap != null) {
                                if(lastDownloadedBitmap == null) {
                                    lastDownloadedBitmap = newBitmap;
                                    ImageViewAnimatedChange(getApplicationContext(), snapshot, newBitmap);
                                } else {
                                    if(!lastDownloadedBitmap.sameAs(newBitmap))
                                        ImageViewAnimatedChange(getApplicationContext(), snapshot, newBitmap);
                                }*/
                                //ImageViewAnimatedChange(getApplicationContext(), snapshot, newBitmap);
                                //snapshot.setImageBitmap(bmp);
                            //}
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                    case Commands.NEWDATA: {
                        // update ui dataList and change data to recent
                        MediaEntityList data = (MediaEntityList) msg.obj;
                        setTitle(data.getPath());
                        updateListData(data);
                        break;
                    }
                    case Commands.ERROR: {
                        break;
                    }
                    case Commands.NOTIFY_DATA_CHANGED: {
                        adapter.notifyDataSetChanged();
                        stopAnimUpdate();
                        break;
                    }
                }
            };
        };

        // init connection to service
        serviceConnection = new Connection(uiHandler);
        bindService(service, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    /**
     * update data in media browser
     * @param data
     */
    private void updateListData(final MediaEntityList data) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // update list
                dataList.clear();
                dataList.addAll(data.getList());
                uiHandler.sendEmptyMessage(Commands.NOTIFY_DATA_CHANGED);
            }
        }).start();
    }

    /**
     * Sends requests to get current state of media browser
     */
    private void queryMediaBrowser(final String path) {
        startAnimUpdate();
        serviceConnection.queryMediaBrowser(path);
    }
}

