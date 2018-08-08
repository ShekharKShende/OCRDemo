/*
 * Copyright (C) The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package onenetsolution.com.ocrdemo.ocr;

import android.util.Log;
import android.util.SparseArray;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;


import java.util.regex.Pattern;

import onenetsolution.com.ocrdemo.camera.GraphicOverlay;
import onenetsolution.com.ocrdemo.constants.AppConstants;

/**
 * A very simple Processor which receives detected TextBlocks and adds them to the overlay
 * as OcrGraphics.
 */
public class OcrDetectorProcessor implements Detector.Processor<TextBlock> {

    private final String selectedField;
    private GraphicOverlay<OcrGraphic> mGraphicOverlay;
    private OcrCaptureActivity ocrCaptureActivity;
    private boolean isProductNo = true;
    private boolean isTarewt = true;
    private String netWeight = "";
    private String grossWeight = "";
    private String mProductNumber = "";
    private String containerNo = "";
    private String cargoTare = "";
    private boolean isMax = true;
    private boolean isTare = true;
    private String stringMaxgross = "";
    private String maxGross = "";
    private String stringTare = "";

    OcrDetectorProcessor(GraphicOverlay<OcrGraphic> ocrGraphicOverlay, OcrCaptureActivity ocrCaptureActivity, String selectedField) {
        mGraphicOverlay = ocrGraphicOverlay;
        this.ocrCaptureActivity = ocrCaptureActivity;
        this.selectedField = selectedField;
    }

    /**
     * Called by the detector to deliver detection results.
     * If your application called for it, this could be a place to check for
     * equivalent detections by tracking TextBlocks that are similar in location and content from
     * previous frames, or reduce noise by eliminating TextBlocks that have not persisted through
     * multiple detections.
     */
    @Override
    public void receiveDetections(Detector.Detections<TextBlock> detections) {
        mGraphicOverlay.clear();
        SparseArray<TextBlock> items = detections.getDetectedItems();
        for (int i = 0; i < items.size(); ++i) {
            TextBlock item = items.valueAt(i);
            OcrGraphic graphic = new OcrGraphic(mGraphicOverlay, item);
//            String tarewt = stringTare.substring(0, 5);
            Pattern p = Pattern.compile("\\d+");
            if(p.matcher(item.getValue()).matches()){
                ocrCaptureActivity.getCylynderNo(item.getValue());
            }
            mGraphicOverlay.add(graphic);

        }
    }

    private void setGrossNetWt(String netWeight, String grossWeight, String mProductNumber) {
        getProductNo(mProductNumber, netWeight.replaceAll("[^\\d.]", ""), grossWeight.replaceAll("[^\\d.]", ""));
    }


    private void getcontainerInfo(String value) {

        String array[] = value.split("\n");

        if (array.length > 2) {
            array[0] = array[0].toUpperCase();
            array[2] = array[2].toUpperCase();

            int indexOfMaxgross = array[0].indexOf("K");
            if (indexOfMaxgross > 5) {
                stringMaxgross = array[0].substring(indexOfMaxgross - 7, indexOfMaxgross);
                if (stringMaxgross.contains(" ")) {
                    stringMaxgross = stringMaxgross.replaceAll("\\s+", "");
                }
                if (stringMaxgross.contains(".")) {
                    stringMaxgross = stringMaxgross.replaceAll("\\.", "");
                }
                if (stringMaxgross.contains(",")) {
                    stringMaxgross = stringMaxgross.replaceAll("\\,", "");
                }
                //Log.e("ACTAL WEIGHT::", "" + stringMaxgross);
            }

            int indexOfTare = array[2].indexOf("K");
//        Log.e("INDEX OF TARE WEIGHT::", "" + indexOfTare);
            int minusTareState = 0;
            if (indexOfTare > 3) {
                if (indexOfTare == 4) {
                    minusTareState = 4;
                } else {
                    minusTareState = 6;
                }
                stringTare = array[2].substring(indexOfTare - minusTareState, indexOfTare);
                if (stringTare.contains(" ")) {
                    stringTare = stringTare.replaceAll("\\s+", "");
                }
                if (stringTare.contains(".")) {
                    stringTare = stringTare.replaceAll("[^0-9]", "");
                }
                if (stringTare.contains(",")) {
                    stringTare = stringTare.replaceAll("[^0-9]", "");
                }
//            Log.e("ACTAL TARE WEIGHT::", "" + stringTare);
            }


        } else {
//            Log.e("fffffffffffff" , ""+array[0]);
            if (array[0].contains("K") && array[0].replaceAll("[^A-Za-z0-9]", "").length() > 6 && isMax == true) {
                maxGross = array[0].replaceAll("[^A-Za-z0-9]", "");
                String maxgross = maxGross.substring(0, 6);
                Pattern p = Pattern.compile("^3[0-9]{4}K");
                boolean isTrue = p.matcher(maxgross).matches();
                if (isTrue == true) {
                    stringMaxgross = maxGross.substring(0, 5);
                    isMax = false;
//                    Log.e("ffffffffstringMaxgross", "" + stringMaxgross);
                }
            }
            if (array[0].contains("K") && array[0].replaceAll("[^A-Za-z0-9]", "").length() > 5 && isTare == true) {
                stringTare = array[0].replaceAll("[^A-Za-z0-9]", "");
                String tarewt = stringTare.substring(0, 5);
                Pattern p = Pattern.compile("^[0-9]{4}K");
                boolean isTrue = p.matcher(tarewt).matches();
                if (isTrue == true) {
                    stringTare = stringTare.substring(0, 4);
                    isTare = false;
//                    Log.e("fffffffffffffstringTare", "" + stringTare);
                }
            }
        }

        if (stringTare.length() == 4 && stringMaxgross.length() == 5
                && stringMaxgross.matches("[0-9]+") && stringMaxgross.matches("[0-9]+")) {
//            ocrCaptureActivity.getContainerCargoTare(stringMaxgross, stringTare);
            isTarewt = false;
        } else {
            isTarewt = true;
        }
    }

    private boolean isDate(String s) {
        Pattern p = Pattern.compile("^[/]{1}\\d{4}");
        return p.matcher(s).matches();
    }

    private String isContainerId(String containerId) {
        String containe = null;
        containe = containerId.replaceAll("[^A-Za-z0-9]", "");
        if (containe.length() > 11)
            containe = containe.substring(0, 11);

        Pattern p = Pattern.compile("^[A-Z]{4}\\d{6}\\d{1}");
        boolean isTrue = p.matcher(containe).matches();
        ;
        if (isTrue == true)
            return containe;
        else
            return null;
    }

    private void getProductNo(String value, String netWeight, String grossWeight) {
        String prod = value;
        isProductNo = false;
        String array[] = prod.split("\n");
        for (int i = 0; i < array.length; i++) {
            if (array[i].contains("PROD")) {
                if (array[i + 1].length() > 8 && array[i + 1].length() <= 10)
                    if (array[i + 1].startsWith("1")) {
                        StringBuilder myName = new StringBuilder(array[i + 1]);
                        myName.setCharAt(0, 'I');
                        array[i + 1] = String.valueOf(myName);
                    }
//                ocrCaptureActivity.getProductNumber(array[i + 1], netWeight, grossWeight);
//                Log.e("--------------", "" + array[i + 1]);
                break;
            }
        }
    }


    /**
     * Frees the resources associated with this detection processor.
     */
    @Override
    public void release() {
        mGraphicOverlay.clear();
    }
}
