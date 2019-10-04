
package com.kie.tts;

import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import java.util.HashMap;
import java.util.Locale;

public class KieTTSModule extends ReactContextBaseJavaModule {

    private final ReactApplicationContext reactContext;
    private TextToSpeech textToSpeech = null;
    private String utteranceId = "kiebot";
    private int index = 0;
    private String[] sentences;
    private HashMap<String, String> params = new HashMap<>();
    private boolean isPaused = false, isStopped = false;
    final ReactApplicationContext reactContext;
    private Promise voicePromise;
    private static final String TTS_ERROR = "TTS_ERROR";


    public KieTTSModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;

        params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId);
        textToSpeech = new TextToSpeech(reactContext, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                textToSpeech.setLanguage(Locale.US);
            }
        });

        textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String s) {
            }

            @Override
            public void onDone(String s) {
                if (index < sentences.length && !isPaused && !isStopped) {
                    speak();
                }
            }

            @Override
            public void onError(String s) {
                voicePromise.reject(TTS_ERROR, "Error converting text to speech");
            }
        });
    }

    private void speak() {
        if (Build.VERSION.SDK_INT >= 21) {
            if (index < sentences.length) {
                textToSpeech.speak(sentences[index], TextToSpeech.QUEUE_FLUSH, null, utteranceId);
                index++;
            }
        } else {
            if (index < sentences.length) {
                textToSpeech.speak(sentences[index], TextToSpeech.QUEUE_FLUSH, params);
                index++;
            }
        }
    }

    @ReactMethod
    public void setText(String text, Promise promise) {
        this.voicePromise = promise;
        sentences = text.split("\\.");
        voicePromise.resolve("");
    }

    @ReactMethod
    public void play(Promise promise) {
        this.voicePromise = promise;
        isStopped = false;
        isPaused = false;
        index = 0;
        speak();
        voicePromise.resolve("");
    }

    @ReactMethod
    public void pause(Promise promise) {
        this.voicePromise = promise;
        if (textToSpeech != null) {
            textToSpeech.stop();
            isPaused = true;
            if (index > 0)
                index--;
            voicePromise.resolve("");
        }
    }

    @ReactMethod
    public void resume(Promise promise) {
        this.voicePromise = promise;
        isStopped = false;
        isPaused = false;
        speak();
        voicePromise.resolve("");
    }

    @ReactMethod
    public void stop(Promise promise) {
        this.voicePromise = promise;
        if (textToSpeech != null) {
            textToSpeech.stop();
            index = 0;
            isPaused = false;
            isStopped = true;
            voicePromise.resolve("");
        }
    }

    @Override
    public String getName() {
        return "KieTTS";
    }
}