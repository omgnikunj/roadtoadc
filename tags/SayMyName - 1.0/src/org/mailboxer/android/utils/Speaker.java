package org.mailboxer.android.utils;

import android.content.Context;
import android.util.Log;

import com.google.tts.TTS;
import com.google.tts.TTS.InitListener;

public class Speaker implements InitListener {
	private boolean ready;
	private final TTS talker;

	private final Object waitObject = new Object();

	public Speaker(final Context context) {
		talker = new TTS(context, this, true);
		Log.v("SMN", "tts created");
	}

	@Override
	public void onInit(final int status) {
		Log.v("SMN", "tts init");

		synchronized (waitObject) {
			ready = true;
			Log.e("SMN", "notify");

			waitObject.notify();
		}
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
			talker.speak(text, 1, null);
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

			talker.speak(text, 1, null);
		}
	}
}