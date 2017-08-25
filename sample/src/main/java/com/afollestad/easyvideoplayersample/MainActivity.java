package com.afollestad.easyvideoplayersample;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;
import com.afollestad.easyvideoplayer.EasyVideoCallback;
import com.afollestad.easyvideoplayer.EasyVideoPlayer;
import com.afollestad.materialdialogs.MaterialDialog;
import com.segment.analytics.Analytics;
import com.segment.analytics.Properties;
import com.segment.analytics.android.integrations.nielsendcr.NielsenDCRIntegration;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements EasyVideoCallback {

  private EasyVideoPlayer player;
  private Timer playheadTimer;

  public void startPlayheadTimer() {
    playheadTimer = new Timer();

    TimerTask monitorHeadPos =
        new TimerTask() {
          @Override public void run() {
            Analytics.with(player.getContext())
                .track("Video Content Playing", new Properties().putValue("assetId", 7890)
                    .putValue("adType", "post-roll")
                    .putValue("totalLength", player.getDuration())
                    .putValue("videoPlayer", "youtube")
                    .putValue("playbackPosition", player.getCurrentPosition())
                    .putValue("fullScreen", false)
                    .putValue("bitrate", 500)
                    .putValue("sound", 80));
          }
        };
    playheadTimer.schedule(monitorHeadPos, 0, TimeUnit.SECONDS.toMillis(10));
  }

  private void stopPlayheadTimer() {
    if (playheadTimer != null) {
      playheadTimer.cancel();
      playheadTimer = null;
    }
  }


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    player = (EasyVideoPlayer) findViewById(R.id.player);
    assert player != null;
    player.setCallback(this);
    // All further configuration is done from the XML layout.
    Analytics analytics =
        new Analytics.Builder(this, "QDjpO9jNyjJGAMnH55VlEpPgbOvSAcP9")
            .use(NielsenDCRIntegration.FACTORY)
            .flushQueueSize(1)
            .trackApplicationLifecycleEvents()
            .recordScreenViews()
            .logLevel(Analytics.LogLevel.VERBOSE)
            .build();

    // Set the initialized instance as a globally accessible instance.
    Analytics.setSingletonInstance(analytics);
  }

  @Override
  protected void onPause() {
    super.onPause();
    if (isFinishing()) player.release();
    Analytics.with(this)
        .track(
            "Video Playback Paused",
            new Properties()
                .putValue("assetId", 1234)
                .putValue("adType", "mid-roll")
                .putValue("totalLength", player.getDuration())
                .putValue("videoPlayer", "vimeo")
                .putValue("playbackPosition", 10)
                .putValue("fullScreen", true)
                .putValue("bitrate", 50)
                .putValue("sound", 80));
    player.pause();
    stopPlayheadTimer();
  }

  @Override
  public void onStarted(EasyVideoPlayer player) {
    super.onStart();
    if (player.isPlaying()) {
      startPlayheadTimer();
      Analytics.with(this)
          .track(
              "Video Playback Started",
              new Properties()
                  .putValue("assetId", 1234)
                  .putValue("adType", "pre-roll")
                  .putValue("totalLength", player.getDuration())
                  .putValue("videoPlayer", "youtube")
                  .putValue("sound", 80)
                  .putValue("bitrate", 40)
                  .putValue("fullScreen", true));

      Analytics.with(this)
          .track(
              "Video Content Started",
              new Properties()
                  .putValue("assetId", 123214)
                  .putValue("title", "Look Who's Purging Now")
                  .putValue("season", 2)
                  .putValue("episode", 9)
                  .putValue("genre", "cartoon")
                  .putValue("program", "Rick and Morty")
                  .putValue("channel", "cartoon network")
                  .putValue("publisher", "Turner Broadcasting System")
                  .putValue("fullEpisode", true)
                  .putValue("podId", "segment A")
                  .putValue("playbackPosition", player.getCurrentPosition()));
    }

  }

  @Override
  public void onPaused(EasyVideoPlayer player) {
  stopPlayheadTimer();
    Analytics.with(this)
        .track(
            "Video Playback Paused",
            new Properties()
                .putValue("assetId", 1234)
                .putValue("adType", "mid-roll")
                .putValue("totalLength", player.getDuration())
                .putValue("videoPlayer", "vimeo")
                .putValue("playbackPosition", player.getCurrentPosition())
                .putValue("fullScreen", true)
                .putValue("bitrate", 50)
                .putValue("sound", 80));
  }

  @Override
  public void onPreparing(EasyVideoPlayer player) {

    stopPlayheadTimer();
    Log.d("EVP-Sample", "onPreparing()");
  }

  @Override
  public void onPrepared(EasyVideoPlayer player) {
    stopPlayheadTimer();
    Log.d("EVP-Sample", "onPrepared()");
  }

  @Override
  public void onBuffering(int percent) {
    Log.d("EVP-Sample", "onBuffering(): " + percent + "%");
  }

  @Override
  public void onError(EasyVideoPlayer player, Exception e) {
    stopPlayheadTimer();
    Log.d("EVP-Sample", "onError(): " + e.getMessage());
    new MaterialDialog.Builder(this)
        .title(R.string.error)
        .content(e.getMessage())
        .positiveText(android.R.string.ok)
        .show();
  }

  @Override
  public void onCompletion(EasyVideoPlayer player) {
  stopPlayheadTimer();
    Analytics.with(this)
        .track(
            "Video Content Completed",
            new Properties()
                .putValue("assetId", 7890)
                .putValue("adType", "post-roll")
                .putValue("totalLength", player.getDuration())
                .putValue("videoPlayer", "youtube")
                .putValue("playbackPosition", player.getCurrentPosition())
                .putValue("fullScreen", false)
                .putValue("bitrate", 500)
                .putValue("sound", 80));
  }

  @Override
  public void onRetry(EasyVideoPlayer player, Uri source) {
    stopPlayheadTimer();
    Toast.makeText(this, "Retry", Toast.LENGTH_SHORT).show();
  }

  @Override
  public void onSubmit(EasyVideoPlayer player, Uri source) {
    Toast.makeText(this, "Submit", Toast.LENGTH_SHORT).show();
  }

  @Override
  public void onClickVideoFrame(EasyVideoPlayer player) {
    Toast.makeText(this, "Click video frame.", Toast.LENGTH_SHORT).show();
  }
}
