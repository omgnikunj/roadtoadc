package org.mailboxer.saymyname;

import java.util.HashMap;

import android.content.Context;
import android.media.AudioManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;

public class Speaker implements OnInitListener {
	private TextToSpeech talker;
	private QueryBuilder query;
	private HashMap<String, String> params;
	private boolean ready;

	public Speaker(Context context, QueryBuilder thread) {
		this.query = thread;

		params = new HashMap<String, String>();
		params.put(TextToSpeech.Engine.KEY_PARAM_STREAM, String.valueOf(AudioManager.STREAM_RING));

		talker = new TextToSpeech(context, this);
	}

	@Override
	public void onInit(int status) {
		ready = true;

		Log.e("SMN", "INIT");
		query.looper();

		talker.setOnUtteranceCompletedListener(new SpeechFinishedListener());
	}

	public void speak(String text) {
		Log.e("SMN", "SPEAK");
		params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "normal");
		talker.speak(text, TextToSpeech.QUEUE_ADD, params);
	}

	public void messageSpeak(String text) {
		params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "message");
		talker.speak(text, TextToSpeech.QUEUE_ADD, params);
	}

	public void shutdown() {
		if (ready) {
			talker.stop();
			talker.shutdown();
		}
	}

	private class SpeechFinishedListener implements TextToSpeech.OnUtteranceCompletedListener {
		@Override
		public void onUtteranceCompleted(String utteranceId) {
			if (utteranceId.equals("normal")) {
				Log.e("SMN", "UTTERANCE");
				query.looper();
			}
		}
	}
}