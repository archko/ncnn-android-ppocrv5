// Tencent is pleased to support the open source community by making ncnn available.
//
// Copyright (C) 2025 THL A29 Limited, a Tencent company. All rights reserved.
//
// Licensed under the BSD 3-Clause License (the "License"); you may not use this file except
// in compliance with the License. You may obtain a copy of the License at
//
// https://opensource.org/licenses/BSD-3-Clause
//
// Unless required by applicable law or agreed to in writing, software distributed
// under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
// CONDITIONS OF ANY KIND, either express or implied. See the License for the
// specific language governing permissions and limitations under the License.
package com.tencent.ppocrv5ncnn.test;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.tencent.ppocrv5ncnn.BasePolygonResultModel;
import com.tencent.ppocrv5ncnn.OcrOverlayView;
import com.tencent.ppocrv5ncnn.PPOCRv5Ncnn;

import java.util.ArrayList;
import java.util.List;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends Activity implements SurfaceHolder.Callback {
    public static final int REQUEST_CAMERA = 100;
    public static final int REQUEST_IMAGE = 101;

    private PPOCRv5Ncnn ppocrv5ncnn = new PPOCRv5Ncnn();
    private int facing = 0;

    private int current_model = 0;
    private int current_size = 2;
    private int current_cpugpu = 0;

    private SurfaceView cameraView;
    private ImageView imageView;
    private OcrOverlayView ocrOverlayView;
    private ListView resultList;
    private Button tabImage;
    private Button tabCamera;
    private LinearLayout contentImage;
    private LinearLayout contentCamera;
    private boolean isImageTab = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        cameraView = findViewById(R.id.cameraview);
        imageView = findViewById(R.id.imageView);
        ocrOverlayView = findViewById(R.id.ocrOverlayView);
        resultList = findViewById(R.id.resultList);
        tabImage = findViewById(R.id.tabImage);
        tabCamera = findViewById(R.id.tabCamera);
        contentImage = findViewById(R.id.contentImage);
        contentCamera = findViewById(R.id.contentCamera);

        cameraView.getHolder().setFormat(PixelFormat.RGBA_8888);
        cameraView.getHolder().addCallback(this);

        Button buttonSwitchCamera = findViewById(R.id.buttonSwitchCamera);
        buttonSwitchCamera.setOnClickListener(arg0 -> {
            int new_facing = 1 - facing;

            ppocrv5ncnn.closeCamera();

            ppocrv5ncnn.openCamera(new_facing);

            facing = new_facing;
        });

        Button buttonSelectImage = findViewById(R.id.buttonSelectImage);
        buttonSelectImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, REQUEST_IMAGE);
        });

        tabImage.setOnClickListener(v -> {
            if (!isImageTab) {
                isImageTab = true;
                contentImage.setVisibility(View.VISIBLE);
                contentCamera.setVisibility(View.GONE);
                ppocrv5ncnn.closeCamera();
                tabImage.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_light));
                tabCamera.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
            }
        });

        tabCamera.setOnClickListener(v -> {
            if (isImageTab) {
                isImageTab = false;
                contentImage.setVisibility(View.GONE);
                contentCamera.setVisibility(View.VISIBLE);
                ppocrv5ncnn.openCamera(facing);
                tabImage.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
                tabCamera.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_light));
            }
        });

        reload();
    }

    private void reload() {
        boolean ret_init = ppocrv5ncnn.loadModel(getAssets(), current_model, current_size, current_cpugpu);
        if (!ret_init) {
            Log.e("MainActivity", "ppocrv5ncnn loadModel failed");
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        ppocrv5ncnn.setOutputWindow(holder.getSurface());
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImage);
                String result = ppocrv5ncnn.detectAndRecognize(bitmap);
                Log.d("OCR", "Result: " + result);
                imageView.setImageBitmap(bitmap);
                List<BasePolygonResultModel> modelList = parseResult(result);
                Log.d("OCR", "Model count: " + modelList.size());
                ocrOverlayView.setPolygonListInfo(modelList, bitmap.getWidth(), bitmap.getHeight());

                List<String> textList = new ArrayList<>();
                for (BasePolygonResultModel model : modelList) {
                    textList.add(model.getName());
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, textList);
                resultList.setAdapter(adapter);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private List<BasePolygonResultModel> parseResult(String result) {
        List<BasePolygonResultModel> modelList = new ArrayList<>();
        if (result.isEmpty()) return modelList;
        String[] items = result.split(";");
        for (String item : items) {
            String[] parts = item.split(",");
            if (parts.length >= 5) {
                int x = Integer.parseInt(parts[0]);
                int y = Integer.parseInt(parts[1]);
                int w = Integer.parseInt(parts[2]);
                int h = Integer.parseInt(parts[3]);
                String text = parts[4];
                BasePolygonResultModel model = new BasePolygonResultModel();
                model.setRect(new Rect(x, y, x + w, y + h));
                model.setName(text);
                model.setConfidence(1.0f);
                modelList.add(model);
            }
        }

        // Group by lines
        modelList.sort((a, b) -> Integer.compare(a.getRect(1.0f, new Point(0, 0)).top, b.getRect(1.0f, new Point(0, 0)).top));

        List<List<BasePolygonResultModel>> lines = new ArrayList<>();
        for (BasePolygonResultModel model : modelList) {
            boolean added = false;
            for (List<BasePolygonResultModel> line : lines) {
                if (!line.isEmpty()) {
                    int avgY = line.stream().mapToInt(m -> m.getRect(1.0f, new Point(0, 0)).centerY()).sum() / line.size();
                    if (Math.abs(model.getRect(1.0f, new Point(0, 0)).centerY() - avgY) < 30) { // threshold
                        line.add(model);
                        added = true;
                        break;
                    }
                }
            }
            if (!added) {
                List<BasePolygonResultModel> newLine = new ArrayList<>();
                newLine.add(model);
                lines.add(newLine);
            }
        }

        // Combine each line
        List<BasePolygonResultModel> combinedList = new ArrayList<>();
        for (List<BasePolygonResultModel> line : lines) {
            line.sort((a, b) -> Integer.compare(a.getRect(1.0f, new Point(0, 0)).left, b.getRect(1.0f, new Point(0, 0)).left));
            StringBuilder combinedText = new StringBuilder();
            int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, maxX = 0, maxY = 0;
            for (BasePolygonResultModel model : line) {
                Rect r = model.getRect(1.0f, new Point(0, 0));
                minX = Math.min(minX, r.left);
                minY = Math.min(minY, r.top);
                maxX = Math.max(maxX, r.right);
                maxY = Math.max(maxY, r.bottom);
                combinedText.append(model.getName()).append(" ");
            }
            BasePolygonResultModel combinedModel = new BasePolygonResultModel();
            combinedModel.setRect(new Rect(minX, minY, maxX, maxY));
            combinedModel.setName(combinedText.toString().trim());
            combinedModel.setConfidence(1.0f);
            combinedList.add(combinedModel);
        }

        return combinedList;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA);
        }

        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_IMAGE);
        }

        if (!isImageTab) {
            ppocrv5ncnn.openCamera(facing);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        ppocrv5ncnn.closeCamera();
    }
}
