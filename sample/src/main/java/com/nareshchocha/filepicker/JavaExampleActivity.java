package com.nareshchocha.filepicker;

import static com.nareshchocha.filepicker.MainActivityKt.getClipDataUris;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nareshchocha.filepicker.adapter.MediaAdapter;
import com.nareshchocha.filepicker.databinding.ActivityMainBinding;
import com.nareshchocha.filepickerlibrary.models.DocumentFilePickerConfig;
import com.nareshchocha.filepickerlibrary.models.ImageCaptureConfig;
import com.nareshchocha.filepickerlibrary.models.PickMediaConfig;
import com.nareshchocha.filepickerlibrary.models.PickMediaType;
import com.nareshchocha.filepickerlibrary.models.PopUpConfig;
import com.nareshchocha.filepickerlibrary.models.PopUpType;
import com.nareshchocha.filepickerlibrary.models.VideoCaptureConfig;
import com.nareshchocha.filepickerlibrary.ui.FilePicker;
import com.nareshchocha.filepickerlibrary.utilities.appConst.Const;

import java.io.File;
import java.util.ArrayList;

import timber.log.Timber;

public class JavaExampleActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    ArrayList<Uri> uriList = new ArrayList<Uri>();
    private MediaAdapter mMediaAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setAdapter();

        binding.mbtCaptureImage.setOnClickListener(v -> {
            captureImageResultLauncher.launch(
                    new FilePicker.Builder(this)
                            .imageCaptureBuild(
                                    new ImageCaptureConfig(
                                            null,
                                            null,
                                            null,
                                            null,
                                            null,
                                            null,
                                            null,
                                            null
                                    )
                            )
            );
        });

        binding.mbtCaptureVideo.setOnClickListener(v -> {
            captureImageResultLauncher.launch(
                    new FilePicker.Builder(this)
                            .videoCaptureBuild(
                                    new VideoCaptureConfig(
                                            null,
                                            null,
                                            null,
                                            null,
                                            null,
                                            null,
                                            null,
                                            null,
                                            null,
                                            null,
                                            null
                                    )
                            )
            );
        });

        binding.mbtPickImages.setOnClickListener(v -> {
            captureImageResultLauncher.launch(
                    new FilePicker.Builder(this)
                            .pickMediaBuild(
                                    new PickMediaConfig(
                                            null,
                                            null,
                                            true,
                                            null,
                                            PickMediaType.ImageOnly,
                                            null,
                                            null,
                                            null,
                                            null)
                            )
            );
        });

        binding.mbtPickVideo.setOnClickListener(v -> {
            captureImageResultLauncher.launch(
                    new FilePicker.Builder(this)
                            .pickMediaBuild(
                                    new PickMediaConfig(
                                            null,
                                            null,
                                            true,
                                            3,
                                            PickMediaType.VideoOnly,
                                            null,
                                            null,
                                            null,
                                            null)
                            )
            );
        });
        binding.mbtPickImageVideo.setOnClickListener(v -> {
            captureImageResultLauncher.launch(
                    new FilePicker.Builder(this)
                            .pickMediaBuild(
                                    new PickMediaConfig(
                                            null,
                                            null,
                                            true,
                                            null,
                                            PickMediaType.ImageAndVideo,
                                            null,
                                            null,
                                            null,
                                            null)
                            ));
        });

        binding.mbtPDF.setOnClickListener(v -> {
            ArrayList<String> mMimeTypesList = new ArrayList<>();
            mMimeTypesList.add("application/pdf");
            mMimeTypesList.add("image/*");
            captureImageResultLauncher.launch(
                    new FilePicker.Builder(this)
                            .pickDocumentFileBuild(
                                    new DocumentFilePickerConfig(
                                            null,
                                            null,
                                            true,
                                            null,
                                            mMimeTypesList,
                                            null,
                                            null,
                                            null,
                                            null
                                    )
                            )
            );
        });

        binding.mbtAllOption.setOnClickListener(v -> {
            captureImageResultLauncher.launch(
                    new FilePicker.Builder(this)
                            .setPopUpConfig(
                                    new PopUpConfig(
                                            "Choose Profile",
                                            null,
                                            PopUpType.BOTTOM_SHEET,
                                            RecyclerView.VERTICAL
                                    )
                            )
                            .addPickDocumentFile(null)
                            .addImageCapture(null)
                            .addVideoCapture(null)
                            .addPickMedia(null)
                            .build()
            );
        });

    }


    private void setAdapter() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(
                this,
                RecyclerView.HORIZONTAL,
                false);
        mMediaAdapter = new MediaAdapter(this, uriList);
        binding.rvMedia.setLayoutManager(linearLayoutManager);
        binding.rvMedia.setAdapter(mMediaAdapter);
    }

    private final ActivityResultLauncher captureImageResultLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    new ActivityResultCallback<ActivityResult>() {
                        @Override
                        public void onActivityResult(ActivityResult result) {
                            uriList.clear();
                            try {
                                if (result != null && result.getResultCode() == Activity.RESULT_OK) {
                                    if (result.getData().getData() != null) {
                                        uriList.add(result.getData().getData());
                                        String listData = result.getData().getStringExtra(Const.BundleExtras.FILE_PATH);
                                        File testFile = new File(listData);
                                        Timber.tag("FILE_RESULT").v("Can Read::" + testFile.canRead() + " can Write::" + testFile.canWrite());
                                    } else {
                                        ArrayList<Uri> listData = getClipDataUris(result.getData());
                                        // ArrayList<String> listData = result.getData().getStringArrayListExtra(Const.BundleExtras.FILE_PATH_LIST);
                                        uriList.addAll(listData);
                                    }
                                    Timber.tag("FILE_RESULT").v(result.toString());
                                    Timber.tag("FILE_RESULT").v(result.getData().getExtras().toString());
                                }
                            } catch (Exception e) {
                                Timber.tag("FILE_RESULT").e(e.toString());
                            }
                            mMediaAdapter.notifyItemRangeChanged(0, uriList.size());
                        }
                    });

}