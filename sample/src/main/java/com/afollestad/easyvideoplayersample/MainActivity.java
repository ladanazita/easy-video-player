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

public class MainActivity extends AppCompatActivity implements EasyVideoCallback {

  private EasyVideoPlayer player;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    player = (EasyVideoPlayer) findViewById(R.id.player);
    assert player != null;
    player.setCallback(this);
    // All further configuration is done from the XML layout.
    Analytics analytics = new Analytics.Builder(this, "QDjpO9jNyjJGAMnH55VlEpPgbOvSAcP9")
        .use(NielsenDCRIntegration.FACTORY)
        .flushQueueSize(1)
        .trackApplicationLifecycleEvents()
        .recordScreenViews()
        .build();

    // Set the initialized instance as a globally accessible instance.
    Analytics.setSingletonInstance(analytics);
  }

  @Override
  protected void onPause() {
    Analytics.with(this).track("Video Playback Paused", new Properties()
        .putValue("assetId", 1234)
        .putValue("adType", "mid-roll")
        .putValue("totalLength", 100)
        .putValue("videoPlayer", "vimeo")
        .putValue("playbackPosition", 10)
        .putValue("fullScreen", true)
        .putValue("bitrate", 50)
        .putValue("sound", 80));
    super.onPause();
    player.pause();
  }

  @Override
  public void onStarted(EasyVideoPlayer player) {
    Analytics.with(this).track("Video Playback Started", new Properties()
        .putValue("assetId", 1234)
        .putValue("adType", "pre-roll")
        .putValue("totalLength", 120)
        .putValue("videoPlayer", "youtube")
        .putValue("sound", 80)
        .putValue("bitrate", 40)
        .putValue("fullScreen", true)
    );

    Analytics.with(this).track("Video Content Started", new Properties()
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
        .putValue("playbackPosition", 70)
    );
  }

  @Override
  public void onPaused(EasyVideoPlayer player) {
    Analytics.with(this).track("Video Playback Paused", new Properties()
        .putValue("assetId", 1234)
        .putValue("adType", "mid-roll")
        .putValue("totalLength", 100)
        .putValue("videoPlayer", "vimeo")
        .putValue("playbackPosition", 10)
        .putValue("fullScreen", true)
        .putValue("bitrate", 50)
        .putValue("sound", 80));
  }

  @Override
  public void onPreparing(EasyVideoPlayer player) {
    Log.d("EVP-Sample", "onPreparing()");
  }

  @Override
  public void onPrepared(EasyVideoPlayer player) {
    Log.d("EVP-Sample", "onPrepared()");
  }

  @Override
  public void onBuffering(int percent) {
      Analytics.with(this).track("Video Playback Buffer Started", new Properties()
              .putValue("assetId", 7890)
              .putValue("adType", "post-roll")
              .putValue("totalLength", 700)
              .putValue("videoPlayer", "youtube")
              .putValue("playbackPosition", 20)
              .putValue("fullScreen", false)
              .putValue("bitrate", 500)
              .putValue("sound", 80));
    Log.d("EVP-Sample", "onBuffering(): " + percent + "%");
  }

  @Override
  public void onError(EasyVideoPlayer player, Exception e) {
    Log.d("EVP-Sample", "onError(): " + e.getMessage());
    new MaterialDialog.Builder(this)
        .title(R.string.error)
        .content(e.getMessage())
        .positiveText(android.R.string.ok)
        .show();
  }

  @Override
  public void onCompletion(EasyVideoPlayer player) {
    Analytics.with(this).track("Video Content Completed", new Properties()
        .putValue("assetId", 7890)
        .putValue("adType", "post-roll")
        .putValue("totalLength", 700)
        .putValue("videoPlayer", "youtube")
        .putValue("playbackPosition", 20)
        .putValue("fullScreen", false)
        .putValue("bitrate", 500)
        .putValue("sound", 80));
    Log.d("EVP-Sample", "onCompletion()");
  }

  @Override
  public void onRetry(EasyVideoPlayer player, Uri source) {
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
