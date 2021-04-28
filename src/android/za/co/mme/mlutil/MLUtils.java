package za.co.mme.mlutil;

import android.util.Log;

import android.util.Base64;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;
import org.apache.cordova.PermissionHelper;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.barcode.Barcode;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;


public class MLUtils extends CordovaPlugin {
    private static final String DECODE = "decode";
    private static final String TEXT = "recognize";
    private static final String DATA = "data";
    private static final String LOG_TAG = "MLUtils";

    private CallbackContext callbackContext;
    private BarcodeScannerOptions options;
    private BarcodeScanner scanner;
    private TextRecognizer textRecognizer;

    public MLUtils() {}
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) {
        this.callbackContext = callbackContext;
        this.options = new BarcodeScannerOptions.Builder().setBarcodeFormats(
                Barcode.FORMAT_CODE_39
                ).build();
        this.scanner = BarcodeScanning.getClient(this.options);
        this.textRecognizer = TextRecognition.getClient();
        if (action.equals(DECODE)) {
            JSONObject obj = args.optJSONObject(0);

            if (obj != null) {
                String data = obj.optString(DATA);
                if (data == null) {
                    callbackContext.error(obj);
                    return true;
                }
                cordova.getThreadPool().execute(new Runnable(){  public void run() {
                    decode(data);
                }});
            }
            return true;
        }
        else if(action.equals(TEXT)) {
            JSONObject obj = args.optJSONObject(0);

            if (obj != null) {
                String data = obj.optString(DATA);
                if (data == null) {
                    callbackContext.error(obj);
                    return true;
                }
                cordova.getThreadPool().execute(new Runnable(){  public void run() {
                    recognizeText(data);
                }});
            }
            return true;
        }
        return false;
    }
    public void recognizeText(String strBase64) {
        JSONObject results = new JSONObject();
        try {
            String inputBase64Str = strBase64.replace("data:image/jpeg;base64,", "");
            inputBase64Str = inputBase64Str.replace("data:image/png;base64,","");
//            results.put("Data", data);
            byte[] bytes = android.util.Base64.decode(inputBase64Str.getBytes(), Base64.DEFAULT);
            results.put("bytes", bytes.length);
            Bitmap bMap = BitmapFactory.decodeByteArray(bytes,0, bytes.length);
            results.put("bMap", bMap.getByteCount());
            InputImage image = InputImage.fromBitmap(bMap,0);
            results.put("image height", image.getHeight());
            Task<Text> task = this.textRecognizer.process(image)
                    .addOnSuccessListener(new OnSuccessListener<Text> () {
                        @Override
                        public void onSuccess(Text barcodes) {
                            // Task completed successfully
                            // [START_EXCLUDE]
                            // [START get_barcodes]
                            if (barcodes.getTextBlocks().isEmpty()){
                                callbackContext.error(addVal(results, "codeserr", barcodes.toString() ));
                            } else
                                callbackContext.success(texBlockIterate(barcodes));
                            // [END get_barcodes]
                            // [END_EXCLUDE]
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Task failed with an exception
                            // ...
                            callbackContext.error(errorHandle(results, e));
                        }
                    });

        } catch (Exception e) {
            this.callbackContext.error(errorHandle(results, e));
        }
    }
    
    private JSONObject texBlockIterate(Text barcodes) {
        JSONObject obj = new JSONObject();
        try{
            int c = 0;
            for (Text.TextBlock textBlock: barcodes.getTextBlocks()){
                obj.put("Item" + c++, textBlock.getText());
            }
        }catch (Exception e){
            return errorHandle(new JSONObject(), e);
        }
        return obj;
    }

    public void decode(String data) {
        JSONObject results = new JSONObject();
        try {
            String inputBase64Str = data.replace("data:image/jpeg;base64,", "");
            inputBase64Str = inputBase64Str.replace("data:image/png;base64,","");
//            results.put("Data", data);
            byte[] bytes = android.util.Base64.decode(inputBase64Str.getBytes(), Base64.DEFAULT);
            results.put("bytes", bytes.length);
            Bitmap bMap = BitmapFactory.decodeByteArray(bytes,0, bytes.length);
            results.put("bMap", bMap.getByteCount());
            InputImage image = InputImage.fromBitmap(bMap,0);
            results.put("image height", image.getHeight());
            Task<List<Barcode>> task = this.scanner.process(image)
                    .addOnSuccessListener(new OnSuccessListener<List<Barcode>>() {
                        @Override
                        public void onSuccess(List<Barcode> barcodes) {
                            // Task completed successfully
                            // [START_EXCLUDE]
                            // [START get_barcodes]
                            if (barcodes.isEmpty()){
                                callbackContext.error(addVal(results, "codes", barcodes.toString()));
                            } else
                            for (Barcode barcode: barcodes) {
//                                Rect bounds = barcode.getBoundingBox();
//                                Point[] corners = barcode.getCornerPoints();

                                String rawValue = barcode.getRawValue();

//                                int valueType = barcode.getValueType();
                                // See API reference for complete list of supported types
                                callbackContext.success(addVal(results, "Raw", rawValue));
                            }
                            // [END get_barcodes]
                            // [END_EXCLUDE]
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Task failed with an exception
                            // ...
                            callbackContext.error(errorHandle(results, e));
                        }
                    });

        } catch (Exception e) {
            this.callbackContext.error(errorHandle(results, e));
        }
    }
    private JSONObject addVal(JSONObject obj, String key, String val){
        try {
            obj.put(key,val);
        } catch (JSONException e) {
            obj = errorHandle(obj, e);
        }
        return  obj;
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