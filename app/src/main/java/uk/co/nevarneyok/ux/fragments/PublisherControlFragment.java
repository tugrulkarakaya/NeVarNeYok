package uk.co.nevarneyok.ux.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import uk.co.nevarneyok.ux.OpenTokVideoActivity;
import uk.co.nevarneyok.R;

public class PublisherControlFragment extends Fragment implements
        View.OnClickListener {

    private static final String LOGTAG = "pub-control-fragment";
    private static final int ANIMATION_DURATION = 500;
    private static final int PUBLISHER_CONTROLS_DURATION = 7000;

    private ImageButton mPublisherMute;
    private ImageButton mSwapCamera;
    private Button mEndCall;

    private PublisherCallbacks mCallbacks = sOpenTokCallbacks;
    private OpenTokVideoActivity openTokActivity;
    private boolean mPubControlWidgetVisible = false;
    private RelativeLayout mPublisherContainer;

    public interface PublisherCallbacks {
        public void onMutePublisher();

        public void onSwapCamera();

        public void onEndCall();
    }

    private static PublisherCallbacks sOpenTokCallbacks = new PublisherCallbacks() {

        @Override
        public void onMutePublisher() {
        }

        @Override
        public void onSwapCamera() {
        }

        @Override
        public void onEndCall(){
        }
    };

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        Log.i(LOGTAG, "On attach Publisher control fragment");
        openTokActivity = (OpenTokVideoActivity) activity;
        if (!(activity instanceof PublisherCallbacks)) {
            throw new IllegalStateException(
                    "Activity must implement fragment's callback");
        }

        mCallbacks = (PublisherCallbacks) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.layout_fragment_pub_control,
                container, false);

        mPublisherContainer = (RelativeLayout) openTokActivity
                .findViewById(R.id.fragment_pub_container);

        mPublisherMute = (ImageButton) rootView
                .findViewById(R.id.mutePublisher);
        mPublisherMute.setOnClickListener(this);

        mSwapCamera = (ImageButton) rootView.findViewById(R.id.swapCamera);
        mSwapCamera.setOnClickListener(this);

        mEndCall = (Button) rootView.findViewById(R.id.endCall);
        mEndCall.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.i(LOGTAG, "On detach Publisher control fragment");
        mCallbacks = sOpenTokCallbacks;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.mutePublisher:
                mutePublisher();
                break;

            case R.id.swapCamera:
                swapCamera();
                break;

            case R.id.endCall:
                endCall();
                break;
        }
    }

    public void mutePublisher() {
        mCallbacks.onMutePublisher();

        mPublisherMute.setImageResource(openTokActivity.getPublisher()
                .getPublishAudio() ? R.mipmap.unmute_pub
                : R.mipmap.mute_pub);
    }

    public void swapCamera() {
        mCallbacks.onSwapCamera();
    }

    public void endCall() {
        mCallbacks.onEndCall();
    }

    public void initPublisherUI() {
        if ( openTokActivity != null ) {
            openTokActivity.getHandler()
                    .removeCallbacks(mPublisherWidgetTimerTask);
            openTokActivity.getHandler().postDelayed(mPublisherWidgetTimerTask,
                    PUBLISHER_CONTROLS_DURATION);
            mPublisherMute.setImageResource(openTokActivity.getPublisher()
                    .getPublishAudio() ? R.mipmap.unmute_pub
                    : R.mipmap.mute_pub);
        }
    }

    private Runnable mPublisherWidgetTimerTask = new Runnable() {
        @Override
        public void run() {
            showPublisherWidget(false);
            openTokActivity.setPubViewMargins();
        }
    };

    public void publisherClick() {
        if (!mPubControlWidgetVisible) {
            showPublisherWidget(true);
        } else {
            showPublisherWidget(false);
        }
        initPublisherUI();
    }

    public void showPublisherWidget(boolean show) {
        showPublisherWidget(show, true);
    }

    private void showPublisherWidget(boolean show, boolean animate) {
        if (mPublisherContainer != null) {
            mPublisherContainer.clearAnimation();
            mPubControlWidgetVisible = show;
            float dest = show ? 1.0f : 0.0f;
            AlphaAnimation aa = new AlphaAnimation(1.0f - dest, dest);
            aa.setDuration(animate ? ANIMATION_DURATION : 1);
            aa.setFillAfter(true);
            mPublisherContainer.startAnimation(aa);

            if (show) {
                mEndCall.setClickable(true);
                mSwapCamera.setClickable(true);
                mPublisherMute.setClickable(true);
                mPublisherContainer.setVisibility(View.VISIBLE);
            } else {
                mEndCall.setClickable(false);
                mSwapCamera.setClickable(false);
                mPublisherMute.setClickable(false);
                mPublisherContainer.setVisibility(View.GONE);
            }
        }
    }

    public boolean isPubControlWidgetVisible() {
        return mPubControlWidgetVisible;
    }

    public RelativeLayout getPublisherContainer() {
        return mPublisherContainer;
    }

}
