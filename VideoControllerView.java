package craptv.dekatku.com.craptvboy;



import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.Formatter;
import java.util.Locale;

import craptv.dekatku.com.craptvboy.Overlay.AllOverlay;

public class VideoControllerView extends FrameLayout {
    private static final String TAG = "VideoControllerView";

    private MediaPlayerControl mPlayer;
    private Context mContext;
    private ViewGroup mAnchor;
    private View mRoot;
    private ProgressBar mProgress;
    private TextView mEndTime, mCurrentTime;
    private boolean mShowing;
    private boolean mDragging;
    private static final int sDefaultTimeout = 3000;
    private static final int FADE_OUT = 1;
    private static final int SHOW_PROGRESS = 2;
    private boolean mUseFastForward;

    StringBuilder mFormatBuilder;
    Formatter mFormatter;

    ImageView mPauseButton;
    ImageButton mFullscreenButton;
    ImageView actionBtn;

    ImageView infoBtn;

    AllOverlay allOverlay;
    serviceActivity ser;
    long newposition;


    private Handler mHandler = new MessageHandler(this);

    public VideoControllerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mRoot = null;
        mContext = context;
        mUseFastForward = true;
        allOverlay = new AllOverlay(mContext);

        Log.i(TAG, TAG);
    }

    public VideoControllerView(Context context, boolean useFastForward) {
        super(context);
        mContext = context;
        mUseFastForward = useFastForward;

        System.out.println("--------------------------- mcontext = " + mContext);

        Log.i(TAG, TAG);
    }

    public VideoControllerView(Context context) {
        this(context, true);
        allOverlay = new AllOverlay(mContext);

        Log.i(TAG, TAG);
    }

    @Override
    public void onFinishInflate() {
        if (mRoot != null)
            initControllerView(mRoot);
    }

    public void setMediaPlayer(MediaPlayerControl player) {
        mPlayer = player;
        updatePausePlay();
//        updateFullScreen();
    }

    public void setAnchorView(ViewGroup view) {
        mAnchor = view;

        FrameLayout.LayoutParams frameParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN,
                Gravity.CENTER
        );

        removeAllViews();
        View v = makeControllerView();
        addView(v, frameParams);
    }

    protected View makeControllerView() {
        LayoutInflater inflate = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mRoot = inflate.inflate(R.layout.overlay_play, null);

        initControllerView(mRoot);

        return mRoot;
    }


    private void initControllerView(View v) {
        mPauseButton = (ImageView) v.findViewById(R.id.play);
        if (mPauseButton != null) {
            mPauseButton.requestFocus();
            mPauseButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    doPauseResume();
                    show(sDefaultTimeout);
                }
            });
        }

        RelativeLayout actionBtnLayout = (RelativeLayout) v.findViewById(R.id.actionBtnLayout);
        actionBtn = (ImageView) v.findViewById(R.id.actionBtn);
        if (actionBtn != null) {
            actionBtn.requestFocus();
            actionBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("CT", "-------click!");
                    mPlayer.showPopUp(Constants.basicPopUp);
                }
            });
        }

        if(actionBtnLayout!=null){
            actionBtnLayout.requestFocus();
            actionBtnLayout.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mPlayer.showPopUp(Constants.basicPopUp);
                }
            });
        }

        mFullscreenButton = (ImageButton) v.findViewById(R.id.fullscreen);
        if (mFullscreenButton != null) {
            mFullscreenButton.requestFocus();
            mFullscreenButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    doToggleFullscreen();
                    show(sDefaultTimeout);
                }
            });
        }

        mProgress = (SeekBar) v.findViewById(R.id.mediacontroller_progress);

        if (mProgress != null) {
            if (mProgress instanceof SeekBar) {
                SeekBar seeker = (SeekBar) mProgress;
                seeker.setOnSeekBarChangeListener(mSeekListener);
            }
            mProgress.setMax(1000);
        }

        infoBtn = (ImageView) v.findViewById(R.id.info);
        if (infoBtn != null){
            infoBtn.requestFocus();
            infoBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("CT", "------");
                    mPlayer.showInfoPopup(2);

                }
            });
        }

        mEndTime = (TextView) v.findViewById(R.id.time);
        mCurrentTime = (TextView) v.findViewById(R.id.time_current);
        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());

    }

    public void show() {
        show(sDefaultTimeout);
    }

    private void disableUnsupportedButtons() {
        if (mPlayer == null) {
            return;
        }

        try {
            if (mPauseButton != null && !mPlayer.canPause()) {
                mPauseButton.setEnabled(false);
            }
        } catch (IncompatibleClassChangeError ex) {

        }
    }

    public void show(int timeout) {
        if (!mShowing && mAnchor != null) {
            setProgress();
            if (mPauseButton != null) {
                mPauseButton.requestFocus();
            }
            disableUnsupportedButtons();

            FrameLayout.LayoutParams tlp = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN,
                    Gravity.BOTTOM
            );

            mAnchor.addView(this, tlp);
            mShowing = true;
        }
        updatePausePlay();
//        updateFullScreen();

        mHandler.sendEmptyMessage(SHOW_PROGRESS);

        Message msg = mHandler.obtainMessage(FADE_OUT);
        if (timeout != 0) {
            mHandler.removeMessages(FADE_OUT);
            mHandler.sendMessageDelayed(msg, timeout);
        }
    }

    public boolean isShowing() {
        return mShowing;
    }

    /**
     * Remove the controller from the screen.
     */
    public void hide() {
        if (mAnchor == null || mPlayer.isShowPopUp()) {
            return;
        }

        try {
            mAnchor.removeView(this);
            mHandler.removeMessages(SHOW_PROGRESS);
        } catch (IllegalArgumentException ex) {
            Log.w("MediaController", "already removed");
        }
        mShowing = false;
    }

    private String stringForTime(int timeMs) {
        int totalSeconds = timeMs / 1000;

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        mFormatBuilder.setLength(0);
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    private int setProgress() {


        if (mPlayer == null || mDragging) {
            return 0;
        }


        /*

        long duration = mPlayer.getDuration();

            long newposition = (duration * progress) / 1000L;
         */

        int position = mPlayer.getCurrentPosition();
        int duration = mPlayer.getDuration();
        if (mProgress != null) {

            //original
            if (duration > 0) {
                // use long to avoid overflow
                if(!AppController.getInstance().isOnAir()){
                    long pos = 1000L * position / duration;
                    System.out.println("-------------- mPlayr duration = " + mPlayer.getDuration() + " pos = " + pos);
                    mProgress.setProgress((int) pos);



                }
                else{
                    mProgress.setProgress(Constants.maximumDuration);
                }
            }

//            original
            int percent = mPlayer.getBufferPercentage();
            mProgress.setSecondaryProgress(percent * 10);

            if (mEndTime != null)
                mEndTime.setText(stringForTime(duration));
            if (mCurrentTime != null){
                mCurrentTime.setText(stringForTime(position));
            }

        }



        return position;

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        show(sDefaultTimeout);
        return true;
    }

    @Override
    public boolean onTrackballEvent(MotionEvent ev) {
        show(sDefaultTimeout);
        return false;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (mPlayer == null) {
            return true;
        }

        int keyCode = event.getKeyCode();
        final boolean uniqueDown = event.getRepeatCount() == 0
                && event.getAction() == KeyEvent.ACTION_DOWN;
        if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK
                || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
                || keyCode == KeyEvent.KEYCODE_SPACE) {
            if (uniqueDown) {
                doPauseResume();
                show(sDefaultTimeout);
                if (mPauseButton != null) {
                    mPauseButton.requestFocus();
                }
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY) {
            if (uniqueDown && !mPlayer.isPlaying()) {
                mPlayer.start();
                updatePausePlay();
                show(sDefaultTimeout);
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP
                || keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE) {
            if (uniqueDown && mPlayer.isPlaying()) {
                mPlayer.pause();
                updatePausePlay();
                show(sDefaultTimeout);
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN
                || keyCode == KeyEvent.KEYCODE_VOLUME_UP
                || keyCode == KeyEvent.KEYCODE_VOLUME_MUTE) {
            // don't show the controls for volume adjustment
            return super.dispatchKeyEvent(event);
        } else if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_MENU) {
            if (uniqueDown) {
                hide();
            }
            return true;
        }

        show(sDefaultTimeout);
        return super.dispatchKeyEvent(event);
    }

    public void updatePausePlay() {
        if (mRoot == null || mPauseButton == null || mPlayer == null) {
            return;
        }

        if (mPlayer.isPlaying()) {
            mPauseButton.setImageResource(R.drawable.button_pause);
        } else {
            mPauseButton.setImageResource(R.drawable.button_play);
        }
    }
//
//    public void updateFullScreen() {
//        if (mRoot == null || mFullscreenButton == null || mPlayer == null) {
//            return;
//        }
//
//        if (mPlayer.isFullScreen()) {
//            mFullscreenButton.setImageResource(R.drawable.ic_media_fullscreen_shrink);
//        }
//        else {
//            mFullscreenButton.setImageResource(R.drawable.ic_media_fullscreen_stretch);
//        }
//    }

    private void doPauseResume() {
        if (mPlayer == null) {
            return;
        }

        if (mPlayer.isPlaying()) {
            mPlayer.pause();
        } else {
            mPlayer.start();
        }
        updatePausePlay();
    }

    private void doToggleFullscreen() {
        if (mPlayer == null) {
            return;
        }

        mPlayer.toggleFullScreen();
    }

    private SeekBar.OnSeekBarChangeListener mSeekListener = new SeekBar.OnSeekBarChangeListener() {
        public void onStartTrackingTouch(SeekBar bar) {
            show(3600000);

            mDragging = true;

            mHandler.removeMessages(SHOW_PROGRESS);
        }

        public void onProgressChanged(SeekBar bar, int progress, boolean fromuser) {
            if (mPlayer == null) {
                return;
            }

            if (!fromuser) {

                return;
            }

//            //original
            long duration;
            if (AppController.getInstance().isOnAir()) {
                duration = Constants.maximumDuration;
            } else {
                duration = mPlayer.getDuration();
            }


            newposition = (duration * progress) / 1000L;

            Log.d("CT----", "duration: " + duration + " progress: " + progress + " newposition: " + newposition);
            mPlayer.seekTo((int) newposition);
//            if (mCurrentTime != null)
//                mCurrentTime.setText(stringForTime((int) newposition));
//

        }

        public void onStopTrackingTouch(SeekBar bar) {
            mDragging = false;
            setProgress();
            updatePausePlay();
            show(sDefaultTimeout);

            mHandler.sendEmptyMessage(SHOW_PROGRESS);

        }
    };

    @Override
    public void setEnabled(boolean enabled) {
        if (mPauseButton != null) {
            mPauseButton.setEnabled(enabled);
        }
        if (mProgress != null) {
            mProgress.setEnabled(enabled);
        }
        disableUnsupportedButtons();
        super.setEnabled(enabled);
    }

    private View.OnClickListener mRewListener = new View.OnClickListener() {
        public void onClick(View v) {
            if (mPlayer == null) {
                return;
            }

            int pos = mPlayer.getCurrentPosition();
            pos -= 5000; // milliseconds
            mPlayer.seekTo(pos);
            setProgress();

            show(sDefaultTimeout);
        }
    };

    private View.OnClickListener mFfwdListener = new View.OnClickListener() {
        public void onClick(View v) {
            if (mPlayer == null) {
                return;
            }

            int pos = mPlayer.getCurrentPosition();
            pos += 15000; // milliseconds
            mPlayer.seekTo(pos);
            setProgress();

            show(sDefaultTimeout);
        }
    };

    public interface MediaPlayerControl {
        void start();

        void pause();

        int getDuration();

        int getCurrentPosition();

        void seekTo(int pos);

        boolean isPlaying();

        int getBufferPercentage();

        boolean canPause();

        boolean canSeekBackward();

        boolean canSeekForward();

        boolean isFullScreen();

        void toggleFullScreen();

        void showPopUp(int popUp);

        void getPosButton(int x, int y);

        void showVolum();

        boolean isShowPopUp();

        public void showInfoPopup(int popup);


    }

    private static class MessageHandler extends Handler {
        private final WeakReference<VideoControllerView> mView;

        MessageHandler(VideoControllerView view) {
            mView = new WeakReference<VideoControllerView>(view);
        }

        @Override
        public void handleMessage(Message msg) {
            VideoControllerView view = mView.get();
            if (view == null || view.mPlayer == null) {
                return;
            }

            int pos;
            switch (msg.what) {
                case FADE_OUT:
                    view.hide();
                    break;
                case SHOW_PROGRESS:
                    pos = view.setProgress();
                    if (!view.mDragging && view.mShowing && view.mPlayer.isPlaying()) {
                        msg = obtainMessage(SHOW_PROGRESS);
                        sendMessageDelayed(msg, 1000 - (pos % 1000));
                    }
                    break;
            }
        }
    }
}