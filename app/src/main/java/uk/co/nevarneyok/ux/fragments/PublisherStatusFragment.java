package uk.co.nevarneyok.ux.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import uk.co.nevarneyok.ux.OpenTokVideoActivity;
import uk.co.nevarneyok.R;
import uk.co.nevarneyok.ux.fragments.PublisherControlFragment.PublisherCallbacks;

public class PublisherStatusFragment extends Fragment {

    private static final String LOGTAG = "pub-status-fragment";
    private static final int ANIMATION_DURATION = 500;
    private static final int STATUS_ANIMATION_DURATION = 7000;

    private ImageButton archiving;
    private TextView statusText;
    private OpenTokVideoActivity openTokActivity;
    private boolean mPubStatusWidgetVisible = false;

    private boolean archivingOn = false;

    private RelativeLayout mPubStatusContainer;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        Log.i(LOGTAG, "On attach Publisher status fragment");
        openTokActivity = (OpenTokVideoActivity) activity;
        if (!(activity instanceof PublisherCallbacks)) {
            throw new IllegalStateException(
                    "Activity must implement fragment's callback");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.layout_fragment_pub_status,
                container, false);

        mPubStatusContainer = (RelativeLayout) openTokActivity
                .findViewById(R.id.fragment_pub_status_container);
        archiving = (ImageButton) rootView.findViewById(R.id.archiving);

        statusText = (TextView) rootView.findViewById(R.id.statusLabel);

        if (openTokActivity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) container
                    .getLayoutParams();

            DisplayMetrics metrics = new DisplayMetrics();
            openTokActivity.getWindowManager().getDefaultDisplay()
                    .getMetrics(metrics);

            params.width = metrics.widthPixels - openTokActivity.dpToPx(48);
            container.setLayoutParams(params);
        }

        return rootView;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.i(LOGTAG, "On detach Publisher status fragment");
    }

    private Runnable mPubStatusWidgetTimerTask = new Runnable() {
        @Override
        public void run() {
            showPubStatusWidget(false);
            openTokActivity.setPubViewMargins();
        }
    };

    public void showPubStatusWidget(boolean show) {
        showPubStatusWidget(show, true);
    }

    private void showPubStatusWidget(boolean show, boolean animate) {
        if (mPubStatusContainer != null) {
            mPubStatusContainer.clearAnimation();
            mPubStatusWidgetVisible = show;
            float dest = show ? 1.0f : 0.0f;
            AlphaAnimation aa = new AlphaAnimation(1.0f - dest, dest);
            aa.setDuration(animate ? ANIMATION_DURATION : 1);
            aa.setFillAfter(true);
            mPubStatusContainer.startAnimation(aa);

            if (show && archivingOn) {
                mPubStatusContainer.setVisibility(View.VISIBLE);
            } else {
                mPubStatusContainer.setVisibility(View.GONE);
            }
        }
    }

    public void publisherClick() {
        if (!mPubStatusWidgetVisible) {
            showPubStatusWidget(true);
        } else {
            showPubStatusWidget(false);
        }

        initPubStatusUI();
    }

    public void initPubStatusUI() {
        if ( openTokActivity != null ) {
            openTokActivity.getHandler()
                    .removeCallbacks(mPubStatusWidgetTimerTask);
            openTokActivity.getHandler().postDelayed(mPubStatusWidgetTimerTask,
                    STATUS_ANIMATION_DURATION);
        }
    }

    public void updateArchivingUI(boolean archivingOn) {

        archiving = (ImageButton) openTokActivity.findViewById(R.id.archiving);
        this.archivingOn = archivingOn;
        if (archivingOn) {
            statusText.setText(R.string.archivingOn);
            archiving.setImageResource(R.mipmap.archiving_on);
            showPubStatusWidget(true);
            initPubStatusUI();
        } else {
            showPubStatusWidget(false);
        }
    }

    public boolean isPubStatusWidgetVisible() {
        return mPubStatusWidgetVisible;
    }

    public RelativeLayout getPubStatusContainer() {
        return mPubStatusContainer;
    }

}
