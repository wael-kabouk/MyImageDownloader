package tr.edu.mu.ceng.wk.myimagedownloader;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class MainActivity extends AppCompatActivity {

    EditText txtURL;
    Button btnDownload;
    ImageView imgView;

    //Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txtURL = (EditText) findViewById(R.id.txtURL);
        btnDownload = (Button) findViewById(R.id.btnDownload);
        imgView = (ImageView) findViewById(R.id.imgView);

        btnDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int permission = ActivityCompat.checkSelfPermission(
                        MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if (permission != PackageManager.PERMISSION_GRANTED) {
                    // We don't have permission so prompt the user
                    ActivityCompat.requestPermissions(
                            MainActivity.this,
                            PERMISSIONS_STORAGE,
                            REQUEST_EXTERNAL_STORAGE
                    );
                }
                DownloadTask task = new DownloadTask();
                String[] urls = new String[1];
                urls[0] = txtURL.getText().toString();
                task.execute(urls);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_EXTERNAL_STORAGE) {
            if (grantResults.length == 2 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                AsyncTask backgroundTask = new DownloadTask();
                String[] urls = new String[1];
                urls[0] = txtURL.getText().toString();
                backgroundTask.execute(urls);
            }
        }
    }


    void preview(String imagePath) {
        Bitmap image = BitmapFactory.decodeFile(imagePath);
        float w = image.getWidth();
        float h = image.getHeight();
        int W = 400;
        int H = (int) ((h * W) / w);
        Bitmap b = Bitmap.createScaledBitmap(image, W, H, false);
        imgView.setImageBitmap(b);
    }

    class DownloadTask extends AsyncTask<String, Integer, Bitmap> {
        ProgressDialog PD;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            PD = new ProgressDialog(MainActivity.this);
            PD.setMax(100);
            PD.setIndeterminate(false);
            PD.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            PD.setTitle("Downloading");
            PD.setMessage("Please wait..");
            PD.show();
        }

        protected void onProgressUpdate(Integer... progress) {
            PD.setProgress(progress[0]);
        }

        void downloadFile(String strURL, String imagePath) {
            try {
                URL url = new URL(strURL);
                URLConnection connection = url.openConnection();
                connection.connect();
                int fileSize = connection.getContentLength();
                InputStream input = new BufferedInputStream(url.openStream(), 8192);
                OutputStream output = new FileOutputStream(imagePath);
                byte data[] = new byte[1024];

                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    output.write(data, 0, count);
                    total += count;

                    publishProgress((int) ((total * 100) / fileSize));
                }
                output.flush();
                output.close();
                input.close();

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        @Override
        protected Bitmap doInBackground(String... urls) {
            String fileName = "temp.jpg";
            String imagePath = (Environment.getExternalStoragePublicDirectory
                    (Environment.DIRECTORY_DOWNLOADS)).toString()
                    + "/" + fileName;
            downloadFile(urls[0], imagePath);
            Bitmap image = BitmapFactory.decodeFile(imagePath);
            float w = image.getWidth();
            float h = image.getHeight();
            int W = 400;
            int H = (int) ((h * W) / w);
            Bitmap b = Bitmap.createScaledBitmap(image, W, H, false);
            return b;
        }

        @Override
        protected void onPostExecute(Bitmap b) {
            imgView.setImageBitmap(b);
        }
    }
}