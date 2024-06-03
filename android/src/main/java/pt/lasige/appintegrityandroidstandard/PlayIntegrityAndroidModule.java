package pt.lasige.appintegrityandroidstandard;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import com.facebook.react.bridge.Promise;

import java.util.HashMap;
import java.util.Map;
import android.util.Log;

import com.google.android.gms.tasks.Task;

import com.google.android.play.core.integrity.model.IntegrityErrorCode;
import com.google.android.play.core.integrity.*;
import com.google.android.play.core.integrity.StandardIntegrityManager.*;

import java.io.StringWriter;
import java.io.PrintWriter;



public class PlayIntegrityAndroidModule extends ReactContextBaseJavaModule {


  private StandardIntegrityManager standardIntegrityManager;
  private ReactApplicationContext ourReactContext;
  private int LogLevel = 2; /*0=no logging, 1=informational, 2=verbose*/
  private StandardIntegrityTokenProvider integrityTokenProvider;


  public PlayIntegrityAndroidModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.ourReactContext = reactContext;
  }

  @Override
  public String getName() {
    return "AppIntegrityAndroidStandard";
  }

  private void logMyStackTrace(Exception ex) {
    StringWriter sw = new StringWriter();
    ex.printStackTrace(new PrintWriter(sw));
    String stacktrace = sw.toString();
    Log.d("AppIntegrityAndroidStandard", stacktrace);
  }

  private void StandardRequestDoWarmup(String cloudProjectNumberStr, Promise promise) {
    try {

      if (LogLevel >= 1)  { Log.i("AppIntegrityAndroidStandard", "Warmup method called"); }

      // Create an instance of a manager.
      this.standardIntegrityManager = IntegrityManagerFactory.createStandard(ourReactContext);

      long cloudProjectNumber = Long.parseLong(cloudProjectNumberStr);

      // Prepare integrity token. Can be called once in a while to keep internal
      // state fresh.
      this.standardIntegrityManager.prepareIntegrityToken(
          PrepareIntegrityTokenRequest.builder()
              .setCloudProjectNumber(cloudProjectNumber)
              .build())
          .addOnSuccessListener(tokenProvider -> {
              this.integrityTokenProvider = tokenProvider;
              if (LogLevel >= 1)  { Log.i("AppIntegrityAndroidStandard", "DoWarmup completed successfully"); }
              promise.resolve("Success");
          })
          .addOnFailureListener(exception -> {
              if (LogLevel >= 1)  { Log.e("AppIntegrityAndroidStandard", "Exception in PrepareIntegrityTokenRequest task"); }
              if (LogLevel >= 2)  { logMyStackTrace(exception); }
              promise.reject("Error in PrepareIntegrityTokenRequest.builder() task: " + exception.getMessage());
          });  

    } catch(Exception ex) {
      if (LogLevel >= 1)  { Log.e("AppIntegrityAndroidStandard", "Exception occurred during warmup"); }
      if (LogLevel >= 2)  { logMyStackTrace(ex); }
      promise.reject("An error has occurred during warmup: " + ex.getMessage());
    }
  }


  private void StandardRequestGetToken(String requestHash, String cloudProjectNumberStr, Promise promise) {
    if (this.integrityTokenProvider == null) {
      //The client endpoint app developer likely has forgotten to do the warmup. We will do it now.
      if (LogLevel >= 1)  { Log.e("AppIntegrityAndroidStandard", "Error: A standard token was requested without prior warmup."); }
      promise.reject("A standard token was requested without prior warmup.");
    } else {
      Task<StandardIntegrityToken> integrityTokenResponse =
          integrityTokenProvider.request(
              StandardIntegrityTokenRequest.builder()
                  .setRequestHash(requestHash)
                  .build());
          integrityTokenResponse
            .addOnSuccessListener(response -> {
              if (LogLevel >= 1)  { Log.i("AppIntegrityAndroidStandard", "GetToken completed successfully"); }
              promise.resolve(response.token());
            })
            .addOnFailureListener(exception -> {
              if (LogLevel >= 1)  { Log.e("AppIntegrityAndroidStandard", "Exception occurred during token request"); }
              if (LogLevel >= 2)  { logMyStackTrace(exception); }
              promise.reject("An error has occurred during token request: " + exception.getMessage());
            });

    }
  }

/**
 * @param {string} operation: Set to either "DoWarmup" or "GetToken"
 * @param {string} cloudProjectNumberStr: Google Cloud project number, in string format
 * @param {string} requestHash: Hash of the intented user operation (like a nonce). For "DoWarmup" operation you can provide a dummy value
 * @return void
 */
  private synchronized void StandardRequest(String operation, String requestHash, String cloudProjectNumberStr, Promise promise) {  // thread-safe
    if (operation.equals("DoWarmup")) {
      StandardRequestDoWarmup(cloudProjectNumberStr, promise);
    } else if (operation.equals("GetToken")) {
      StandardRequestGetToken(requestHash, cloudProjectNumberStr, promise);
    } else {
      promise.reject("Operation not supported. You need to specify either DoWarmup or GetToken. " + operation + " is not a valid operation.");
    }

  }


  @ReactMethod
  public void DoWarmup(String cloudProjectNumberStr, Promise promise) {
    // When the last parameter of a native module Java/Kotlin method is a Promise, its corresponding JS method will return a JS Promise object
    // https://reactnative.dev/docs/native-modules-android#promises
    // Note: doing a promise.resolve or promise.reject does not stop the execution of the Java code
    StandardRequest("DoWarmup", "", cloudProjectNumberStr, promise);
  }

  @ReactMethod
  public void GetToken(String requestHash, String cloudProjectNumberStr, Promise promise) {
    // When the last parameter of a native module Java/Kotlin method is a Promise, its corresponding JS method will return a JS Promise object
    // https://reactnative.dev/docs/native-modules-android#promises
    // Note: doing a promise.resolve or promise.reject does not stop the execution of the Java code
    StandardRequest("GetToken", requestHash, cloudProjectNumberStr, promise);
  }


}
