package sauerapps.betterbetterrx.features.newsfeed;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import sauerapps.betterbetterrx.R;
import sauerapps.betterbetterrx.features.meditation.audioSection.AudioList;
import sauerapps.betterbetterrx.utils.Constants;

/**
 * Created by andrewsauer on 3/12/16.
 */
public class SummaryUserFragment extends Fragment {

    private static final String TAG = SummaryUserFragment.class.getSimpleName();

    @Bind(R.id.summary_total_sessions_text_view)
    protected TextView mTotalSessions;
    @Bind(R.id.summary_total_time_text_view)
    protected TextView mTotalTime;
    @Bind(R.id.summary_total_journal_text_view)
    protected TextView mTotalJournals;

    @Bind(R.id.last_meditation_time)
    protected TextView mLastMeditationTime;
    @Bind(R.id.last_meditation_date)
    protected TextView mLastMeditationDate;
    @Bind(R.id.last_meditation_track_title)
    protected TextView mLastMeditationTrackTitle;
    @Bind(R.id.last_meditation_track_description)
    protected TextView mLastMeditationTrackDesc;


    private String mEncodedEmail;

    private double mAudioTime;
    private HashMap<String, Object> mAudioDate;

    private Firebase mUserAudioDetailsRef, mUserAudioTimeTotalRef, mUserEntriesTotalRef;
    private ValueEventListener mUserAudioDetailsListener, mUserAudioTimeTotalListener, mUserEntriesTotalListener;


    public SummaryUserFragment() {
        /* Required empty public constructor */
    }

    public static SummaryUserFragment newInstance(String encodedEmail) {
        SummaryUserFragment fragment = new SummaryUserFragment();
        Bundle args = new Bundle();
        args.putString(Constants.KEY_ENCODED_EMAIL, encodedEmail);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

            mEncodedEmail = getArguments().getString(Constants.KEY_ENCODED_EMAIL);

            mUserAudioDetailsRef = new Firebase(Constants.FIREBASE_URL_USER_AUDIO_DETAILS).child(mEncodedEmail);
            mUserAudioTimeTotalRef = new Firebase(Constants.FIREBASE_URL_USER_AUDIO).child(mEncodedEmail);
            mUserEntriesTotalRef = new Firebase(Constants.FIREBASE_URL_USER_LISTS).child(mEncodedEmail);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_summary, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        mUserAudioTimeTotalListener = mUserAudioTimeTotalRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {

                    double totalTime = (Double) dataSnapshot.getValue();

                    String time = String.format("%01d hr %02d min",
                            TimeUnit.MILLISECONDS.toHours((long) totalTime),
                            TimeUnit.MILLISECONDS.toMinutes((long) totalTime)
                                    - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours((long) totalTime)));

                    mTotalTime.setText(time);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.e(TAG,
                        getString(R.string.log_error_the_read_failed) +
                                firebaseError.getMessage());
            }
        });


        mUserAudioDetailsListener = mUserAudioDetailsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {

                    long totalMeditations = dataSnapshot.getChildrenCount();
                    String sessionTotal = Long.toString(totalMeditations);
                    mTotalSessions.setText(sessionTotal);

                    for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {

                        AudioList audioList = postSnapshot.getValue(AudioList.class);

                        mAudioDate = audioList.getTimestampCreated();
                        mAudioTime = audioList.getAudioTime();

                        mLastMeditationTrackDesc.setText(audioList.getTrackDescription());
                        mLastMeditationTrackTitle.setText(audioList.getTrackTitle());

                        getLastMeditationTime();

                        getLastMinuteDate();


                    }
                }
            }
            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.e(TAG,
                        getString(R.string.log_error_the_read_failed) +
                                firebaseError.getMessage());
            }
        });

        mUserEntriesTotalListener = mUserEntriesTotalRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    long totalEntries = dataSnapshot.getChildrenCount();
                    String sessionTotal = Long.toString(totalEntries);
                    mTotalJournals.setText(sessionTotal);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.e(TAG,
                        getString(R.string.log_error_the_read_failed) +
                                firebaseError.getMessage());
            }
        });
    }

    private void getLastMinuteDate() {
        Object userLastDate = mAudioDate.get(Constants.FIREBASE_PROPERTY_TIMESTAMP);

        long dateTime = ((long)userLastDate);

        Date date = new Date(dateTime);
        SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd hh:mm a", Locale.ENGLISH);
        sdf.setTimeZone(TimeZone.getTimeZone("PST"));
        String formattedDate = sdf.format(date);

        mLastMeditationDate.setText(formattedDate);

    }

    private void getLastMeditationTime() {
        mLastMeditationTime.setText(String.format("%01d:%02d",
                TimeUnit.MILLISECONDS.toHours((long) mAudioTime),
                TimeUnit.MILLISECONDS.toMinutes((long) mAudioTime)
                        - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours((long) mAudioTime))));
    }

    @Override
    public void onPause() {
        super.onPause();
        mUserAudioDetailsRef.removeEventListener(mUserAudioDetailsListener);
        mUserAudioTimeTotalRef.removeEventListener(mUserAudioTimeTotalListener);
        mUserEntriesTotalRef.removeEventListener(mUserEntriesTotalListener);
    }
}