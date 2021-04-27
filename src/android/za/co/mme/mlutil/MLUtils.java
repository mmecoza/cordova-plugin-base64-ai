package za.co.mme.mlutil;

import android.util.Log;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.content.pm.PackageManager;

import java.util.Base64;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;
import org.apache.cordova.PermissionHelper;

import com.google.mlkit.vision.barcode.Barcode;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;

public class MLUtils extends CordovaPlugin {
    private static final String DECODE = "decode";
    private static final String DATA = "data";
    private static final String LOG_TAG = "MLUtils";

    private CallbackContext callbackContext;
    private BarcodeScannerOptions options;

    public MLUtils() {}
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) {
        this.callbackContext = callbackContext;
        this.options = new BarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_QR_CODE, Barcode.FORMAT_AZTEC).build();
        if (action.equals(DECODE)) {
            JSONObject obj = args.optJSONObject(0);

            if (obj != null) {
                String data = obj.optString(DATA);
                if (data == null) {
                    callbackContext.error("User did not specify data to encode");
                    return true;
                }
                cordova.getThreadPool().execute(new Runnable(){  public void run() {
                    decode(data);
                }});
            }
            return true;
        }
        return false;
    }

    public void decode(String data) {
        JSONObject results = new JSONObject();
        try {
            InputImage
            results.put("Data", data);
            this.callbackContext.success(results);
        } catch (Exception e) {
            this.callbackContext.error(errorHandle(results, e));
        }
    }
    private JSONObject errorHandle(JSONObject jsonObject, Exception exc){
        try {
            jsonObject.put("BigError", "Adding Details");
            jsonObject.put("StackTrace", exc.getStackTrace().toString());
//            jsonObject.put("More", String.valueOf(exc.getStackTrace()[0].getLineNumber()));
            jsonObject.put("extra", exc.toString());
        } catch (JSONException e) {
            Log.e(LOG_TAG, "JSON Error");
        }
        return jsonObject;
    }

}