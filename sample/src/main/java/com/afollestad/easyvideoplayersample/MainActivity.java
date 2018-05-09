package com.afollestad.easyvideoplayersample;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;
import com.afollestad.easyvideoplayer.EasyVideoCallback;
import com.afollestad.easyvideoplayer.EasyVideoPlayer;
import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.iab.omid.library.segment.Omid;
import com.iab.omid.library.segment.adsession.Partner;
import com.segment.analytics.Analytics;
import com.segment.analytics.Properties;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements EasyVideoCallback {

  Map metadata;
  private EasyVideoPlayer player;
  private Timer playheadTimer;
  private TimerTask monitorHeadPos;

  public void startPlayheadTimer() {
    if (playheadTimer != null) {
      return;
    }
    playheadTimer = new Timer();
    final int currentPosition = player.getCurrentPosition();
    final long seconds = TimeUnit.MILLISECONDS.toSeconds(currentPosition);

    final int totalLength = player.getDuration();
    final long totalLengthInSeconds = TimeUnit.MILLISECONDS.toSeconds(totalLength);

    monitorHeadPos =
        new TimerTask() {
          @Override
          public void run() {
            Analytics.with(player.getContext())
                .track(
                    "Video Content Playing",
                    new Properties()
                        .putValue("assetId", metadata.get("assetId"))
                        .putValue("adType", metadata.get("adType"))
                        .putValue("totalLength", totalLengthInSeconds)
                        .putValue("videoPlayer", metadata.get("videoPlayer"))
                        .putValue("position", seconds)
                        .putValue("fullScreen", metadata.get("fullscreen"))
                        .putValue("bitrate", metadata.get("bitrate"))
                        .putValue("sound", metadata.get("sound")));
          }
        };
    playheadTimer.schedule(monitorHeadPos, 0, TimeUnit.SECONDS.toMillis(10));
  }

  private void stopPlayheadTimer() {
    if (playheadTimer != null) {
      playheadTimer.cancel();
      monitorHeadPos.cancel();
      playheadTimer = null;
    }
  }

  private static final String TEST_URL =
      "https://ia800201.us.archive.org/12/items/BigBuckBunny_328/BigBuckBunny_512kb.mp4";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    player = (EasyVideoPlayer) findViewById(R.id.player);
    assert player != null;
    player.setCallback(this);
    // https://segment.com/ladanazita/sources/android_video_app/overview
    Analytics analytics =
        new Analytics.Builder(this, "QDjpO9jNyjJGAMnH55VlEpPgbOvSAcP9")
            .flushQueueSize(1)
            .trackApplicationLifecycleEvents()
            .recordScreenViews()
            .logLevel(Analytics.LogLevel.VERBOSE)
            .build();

    // Set the initialized instance as a globally accessible instance.
    Analytics.setSingletonInstance(analytics);

    // 1. Activate the SDK
    Context context = getApplicationContext();
    try {
      boolean activated = Omid.activateWithOmidApiVersion(Omid.getVersion(), context);
      if (!activated) {
        Log.e("SDK failed to activate", "Handle appropriately");
      }
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
    }

    // 2. Fetch the OMID JS library.
    String OMID_JS_SERVICE_URL = context.getString(R.string.omid_js_service);
    StringRequest stringRequest = new StringRequest(Request.Method.GET, OMID_JS_SERVICE_URL,
        new Response.Listener<String>() {
          public String OMID_JS_SERVICE_CONTENT;

          @Override
          public void onResponse(String response) {
            OMID_JS_SERVICE_CONTENT = response;
          }
        },
        new Response.ErrorListener() {
          @Override
          public void onErrorResponse(VolleyError error) {

          }
        });
    RequestQueue queue = Volley.newRequestQueue(context);
    queue.add(stringRequest);


    // 3. Identify your integration.
    try {
      Partner partner = Partner.createPartner("Segment", "1.0.0");
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
    }

    player.setSource(Uri.parse(TEST_URL));
    metadata = new LinkedHashMap<>();
    metadata.put("assetId", 1234);
    metadata.put("adType", "mid-roll");
    metadata.put("videoPlayer", "vimeo");
    metadata.put("fullScreen", true);
    metadata.put("bitrate", 50);
    metadata.put("sound", 80);
    metadata.put("title", "Big Buck Bunny: Peach");
    metadata.put("season", 2);
    metadata.put("episode", 9);
    metadata.put("genre", "cartoon");
    metadata.put("program", "Big Buck Bunny");
    metadata.put("channel", "Creative Commons");
    metadata.put("publisher", "Blender Foundation");
    metadata.put("fullEpisode", true);
    metadata.put("podId", "segment A");
    metadata.put("livestream", false);
  }

  @Override
  protected void onPause() {
    super.onPause();
    if (isFinishing()) player.release();
    final int currentPosition = player.getCurrentPosition();
    long seconds = TimeUnit.MILLISECONDS.toSeconds(currentPosition);

    final int totalLength = player.getDuration();
    long totalLengthInSeconds = TimeUnit.MILLISECONDS.toSeconds(totalLength);

    Analytics.with(this)
        .track(
            "Video Playback Paused",
            new Properties()
                .putValue("assetId", metadata.get("assetId"))
                .putValue("adType", metadata.get("adType"))
                .putValue("totalLength", totalLengthInSeconds)
                .putValue("videoPlayer", metadata.get("videoPlayer"))
                .putValue("position", seconds)
                .putValue("fullScreen", metadata.get("fullscreen"))
                .putValue("bitrate", metadata.get("bitrate"))
                .putValue("sound", metadata.get("sound")));
    player.pause();
    stopPlayheadTimer();
  }

  @Override
  public void onStarted(EasyVideoPlayer player) {
    super.onStart();
    if (player.isPlaying()) {
      startPlayheadTimer();
      final int totalLength = player.getDuration();
      long totalLengthInSeconds = TimeUnit.MILLISECONDS.toSeconds(totalLength);

      Analytics.with(this)
          .track(
              "Video Playback Started",
              new Properties()
                  .putValue("assetId", metadata.get("assetId"))
                  .putValue("adType", metadata.get("adType"))
                  .putValue("totalLength", totalLengthInSeconds)
                  .putValue("videoPlayer", metadata.get("videoPlayer"))
                  .putValue("sound", metadata.get("sound"))
                  .putValue("bitrate", metadata.get("bitrate"))
                  .putValue("fullScreen", metadata.get("fullscreen")));

      final int currentPosition = player.getCurrentPosition();
      long seconds = TimeUnit.MILLISECONDS.toSeconds(currentPosition);
      Analytics.with(this)
          .track(
              "Video Content Started",
              new Properties()
                  .putValue("assetId", metadata.get("assetId"))
                  .putValue("title", metadata.get("title"))
                  .putValue("season", metadata.get("season"))
                  .putValue("episode", metadata.get("episode"))
                  .putValue("genre", metadata.get("genre"))
                  .putValue("program", metadata.get("program"))
                  .putValue("channel", metadata.get("channel"))
                  .putValue("publisher", metadata.get("publisher"))
                  .putValue("fullEpisode", metadata.get("fullEpisode"))
                  .putValue("podId", metadata.get("podId"))
                  .putValue("position", seconds));
    }
  }

  @Override
  public void onPaused(EasyVideoPlayer player) {
    final int currentPosition = player.getCurrentPosition();
    long seconds = TimeUnit.MILLISECONDS.toSeconds(currentPosition);
    final int totalLength = player.getDuration();
    long totalLengthInSeconds = TimeUnit.MILLISECONDS.toSeconds(totalLength);

    stopPlayheadTimer();
    Analytics.with(this)
        .track(
            "Video Playback Paused",
            new Properties()
                .putValue("assetId", metadata.get("assetId"))
                .putValue("adType", metadata.get("adType"))
                .putValue("totalLength", totalLengthInSeconds)
                .putValue("videoPlayer", metadata.get("videoPlayer"))
                .putValue("position", seconds)
                .putValue("fullScreen", metadata.get("fullscreen"))
                .putValue("bitrate", metadata.get("bitrate"))
                .putValue("sound", metadata.get("sound")));
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
    final int currentPosition = player.getCurrentPosition();
    long seconds = TimeUnit.MILLISECONDS.toSeconds(currentPosition);
    final int totalLength = player.getDuration();
    long totalLengthInSeconds = TimeUnit.MILLISECONDS.toSeconds(totalLength);

    Analytics.with(this)
        .track(
            "Video Content Completed",
            new Properties()
                .putValue("assetId", metadata.get("assetId"))
                .putValue("adType", metadata.get("adType"))
                .putValue("totalLength", totalLengthInSeconds)
                .putValue("videoPlayer", metadata.get("videoPlayer"))
                .putValue("position", seconds)
                .putValue("fullScreen", metadata.get("fullscreen"))
                .putValue("bitrate", metadata.get("bitrate"))
                .putValue("sound", metadata.get("sound")));
  }

  @Override
  public void onRetry(EasyVideoPlayer player, Uri source) {
    final int currentPosition = player.getCurrentPosition();
    long seconds = TimeUnit.MILLISECONDS.toSeconds(currentPosition);

    final int totalLength = player.getDuration();
    long totalLengthInSeconds = TimeUnit.MILLISECONDS.toSeconds(totalLength);

    stopPlayheadTimer();
    player.stop();
    player.reset();
    source =
        Uri.parse(
            "https://ia800201.us.archive.org/12/items/BigBuckBunny_328/BigBuckBunny_512kb.mp4");
    player.setSource(source);
    Toast.makeText(this, "Big Buck Bunny", Toast.LENGTH_SHORT).show();

    metadata = new LinkedHashMap<>();
    metadata.put("assetId", 1234);
    metadata.put("adType", "mid-roll");
    metadata.put("videoPlayer", "vimeo");
    metadata.put("fullScreen", true);
    metadata.put("bitrate", 50);
    metadata.put("sound", 80);
    metadata.put("title", "Big Buck Bunny: Peach");
    metadata.put("season", 2);
    metadata.put("episode", 9);
    metadata.put("genre", "cartoon");
    metadata.put("program", "Big Buck Bunny");
    metadata.put("channel", "Creative Commons");
    metadata.put("publisher", "Blender Foundation");
    metadata.put("fullEpisode", true);
    metadata.put("podId", "segment A");
    metadata.put("livestream", false);
    metadata.put("position", seconds);
    metadata.put("totalLenght", totalLengthInSeconds);
  }

  @Override
  public void onSubmit(EasyVideoPlayer player, Uri source) {
    final int currentPosition = player.getCurrentPosition();
    long seconds = TimeUnit.MILLISECONDS.toSeconds(currentPosition);

    final int totalLength = player.getDuration();
    long totalLengthInSeconds = TimeUnit.MILLISECONDS.toSeconds(totalLength);

    stopPlayheadTimer();
    player.stop();
    player.reset();
    source =
        Uri.parse(
            "https://ia902606.us.archive.org/15/items/Popeye_Cooking_With_Gags_1954/Popeye_Cookin_with_Gags_512kb.mp4");
    player.setSource(source);

    metadata = new LinkedHashMap<>();
    metadata.put("assetId", 5678);
    metadata.put("adType", "post-roll");
    metadata.put("videoPlayer", "mp4");
    metadata.put("fullScreen", true);
    metadata.put("bitrate", 10);
    metadata.put("sound", 20);
    metadata.put("title", "Cookin With Gags");
    metadata.put("season", 2);
    metadata.put("episode", 25);
    metadata.put("genre", "cartoon");
    metadata.put("program", "Popeye");
    metadata.put("channel", "CBS");
    metadata.put("publisher", "Time Warner");
    metadata.put("fullEpisode", true);
    metadata.put("podId", "segment A");
    metadata.put("livestream", false);
    metadata.put("position", seconds);
    metadata.put("totalLenght", totalLengthInSeconds);
  }

  @Override
  public void onClickVideoFrame(EasyVideoPlayer player) {
    Toast.makeText(this, "Click video frame.", Toast.LENGTH_SHORT).show();
  }
}
