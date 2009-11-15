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
		// change stream from media to the one from the ringtone
		params.put(TextToSpeech.Engine.KEY_PARAM_STREAM, String.valueOf(AudioManager.STREAM_RING));

		talker = new TextToSpeech(context, this);
	}

	@Override
	public void onInit(int status) {
		ready = true;

		Log.e("SMN", "INIT");
		// tts is ready - start speaking
		query.looper();

		talker.setOnUtteranceCompletedListener(new SpeechFinishedListener());
	}

	public void speak(String text) {
		// speak with an utterance "normal" - OnUtteranceCompletedListener invokes the thread again
		Log.e("SMN", "SPEAK");
		params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "normal");
		talker.speak(text, TextToSpeech.QUEUE_ADD, params);
	}

	public void messageSpeak(String text) {
		// speak with an utterance "message" - OnUtteranceCompletedListener does NOT invoke the thread again
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