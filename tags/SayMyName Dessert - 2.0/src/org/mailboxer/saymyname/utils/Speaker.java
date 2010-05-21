package org.mailboxer.saymyname.utils;

import java.util.HashMap;
import java.util.Locale;

import android.content.Context;
import android.media.AudioManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;

public class Speaker implements OnInitListener {
	private class SpeechFinishedListener implements TextToSpeech.OnUtteranceCompletedListener {
		@Override
		public void onUtteranceCompleted(final String utteranceId) {
			if (utteranceId.equals("smn")) {
				synchronized (waitObject) {
					waitObject.notify();
				}
			}
		}
	}

	private final HashMap<String, String> params;
	private boolean ready;

	private final TextToSpeech talker;

	private final Object waitObject = new Object();

	public Speaker(final Context context) {
		params = new HashMap<String, String>();

		// if (((AudioManager)
		// context.getSystemService(Context.AUDIO_SERVICE)).getRouting(AudioManager.MODE_RINGTONE)
		// != AudioManager.ROUTE_SPEAKER) {
		// params.put(TextToSpeech.Engine.KEY_PARAM_STREAM,
		// String.valueOf(AudioManager.STREAM_RING));
		// } else {
		params.put(TextToSpeech.Engine.KEY_PARAM_STREAM, String.valueOf(AudioManager.STREAM_RING));
		// }

		params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "smn");

		talker = new TextToSpeech(context, this);
		Log.v("SMN", "tts created");
	}

	@Override
	public void onInit(final int status) {
		Log.v("SMN", "tts init");

		talker.setOnUtteranceCompletedListener(new SpeechFinishedListener());
		talker.setLanguage(Locale.getDefault());
		talker.setSpeechRate(0.95f);

		new Thread() {
			@Override
			public void run() {
				try {
					sleep(500);
				} catch (final InterruptedException e) {
					e.printStackTrace();
				}

				synchronized (waitObject) {
					ready = true;
					Log.e("SMN", "notify");

					waitObject.notify();
				}
			}
		}.start();
	}

	public void shutdown() {
		if (ready) {
			talker.stop();
			talker.shutdown();

			ready = false;
		}
	}

	public void speak(final String text) {
		if (ready) {
			Log.e("SMN", "ready");
			talker.speak(text, TextToSpeech.QUEUE_ADD, params);
		} else {
			do {
				synchronized (waitObject) {
					try {
						waitObject.wait(500);
					} catch (final InterruptedException e) {
						e.printStackTrace();
					}
				}
			} while (!ready);

			Log.e("SMN", "was not ready");

			talker.speak(text, TextToSpeech.QUEUE_ADD, params);
		}

		synchronized (waitObject) {
			try {
				waitObject.wait(5000);
			} catch (final InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}