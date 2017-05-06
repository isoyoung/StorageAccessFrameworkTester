package isoyoung.com.storageaccessframeworktester;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.support.v4.provider.DocumentFile;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_DIR_CODE = 1;

    private Button mOpenDirButton; // ディレクトリを開くButton
    private Button mCreateDirButton; // ディレクトリを作るButton
    private Button mCreateFileButton; // ファイルを作るButton
    private Button mReadFileButton; // ファイルを読み込むButton
    private Button mAllDeleteButton; // 全てのファイルを削除するButton
    private Button mSaveUriButton; // Uriを永続化するButton
    private Button mShowResultButton; // 永続化されているURIのコンテンツを取得し、表示するためのボタン
    private Button mCopyFileButton;
    private Button mShareButton;

    private TextView mResultTextView; // テキストファイルの中身
    private TextView mPermanentResultTextView; // SharedPreference中のURIが指すTextViewの中身
    private TextView mSelectedDirNameTextView; // 選択中のディレクリのパス
    private TextView mSdCardMountStateTextView; // SDカードのマウント/アンマウント状態

    private DirectoryInformationAdapter mAdapter;
    private ListView mListView;

    private Uri mCurrentDirUri; // 現在のディレクトリのURI

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSelectedDirNameTextView = (TextView) findViewById(R.id.selected_dir_name_textview);
        mPermanentResultTextView = (TextView) findViewById(R.id.permanent_result_textview);
        mResultTextView = (TextView) findViewById(R.id.result_textview);
        mSdCardMountStateTextView = (TextView) findViewById(R.id.sdcard_mound_state_textview);

        mOpenDirButton = (Button) findViewById(R.id.open_dir_button);
        mOpenDirButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDir();
            }
        });

        mCreateDirButton = (Button) findViewById(R.id.create_dir_button);
        mCreateDirButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCurrentDirUri != null) {
                    createDir(mCurrentDirUri, "SAF Checker");
                    updateDirInfo(mCurrentDirUri);
                } else {
                    Toast.makeText(MainActivity.this, "Never open directory...", Toast.LENGTH_LONG).show();
                }
            }
        });

        mCreateFileButton = (Button) findViewById(R.id.create_file_button);
        mCreateFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCurrentDirUri != null) {
                    createTextFile(mCurrentDirUri, "sony.mp4");
                    updateDirInfo(mCurrentDirUri);
                } else {
                    Toast.makeText(MainActivity.this, "Never open directory...", Toast.LENGTH_LONG).show();
                }
            }
        });

        mReadFileButton = (Button) findViewById(R.id.read_file_button);
        mReadFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCurrentDirUri != null) {
                    // ディレクトリ内の最初のファイルの中身を表示する(おそらく最初のファイルがテキストファイルじゃないとうまくいかない)
                    DocumentFile[] docList = getFileListFromUri(mCurrentDirUri);
                    try {
                        String text = readTextFromUri(docList[0].getUri());
                        mResultTextView.setText(text);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Never open directory...", Toast.LENGTH_LONG).show();
                }
            }
        });

        mAllDeleteButton = (Button) findViewById(R.id.delete_all_file_button);
        mAllDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCurrentDirUri != null) {
                    updateDirInfo(mCurrentDirUri);
                    DocumentFile[] docList = getFileListFromUri(mCurrentDirUri);
                    deleteAllFileFromUri(docList);

                    //削除後の表示を更新するために必要
                    updateDirInfo(mCurrentDirUri);
                } else {
                    Toast.makeText(MainActivity.this, "Never open directory...", Toast.LENGTH_LONG).show();
                }

            }
        });

        mSaveUriButton = (Button) findViewById(R.id.save_uri_button);
        mSaveUriButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCurrentDirUri != null) {
                    setSp(mCurrentDirUri.toString()); // ディレクリファイルのURI
                } else {
                    Toast.makeText(MainActivity.this, "Never open directory...", Toast.LENGTH_LONG).show();
                }
            }
        });
        mShowResultButton = (Button) findViewById(R.id.show_result_uri_button);
        mShowResultButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getSp() == null) {
                    Toast.makeText(MainActivity.this, "Never save uri...", Toast.LENGTH_LONG).show();
                } else {
                    Uri uri = Uri.parse(getSp());//このURIはディレクトリのURI

                    // ディレクトリ内の最初のファイルの中身を表示する(おそらく最初のファイルがテキストファイルじゃないとうまくいかない)
                    DocumentFile[] docList = getFileListFromUri(uri);
                    try {
                        String text = readTextFromUri(docList[0].getUri());
                        mPermanentResultTextView.setText(text);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        mCopyFileButton = (Button) findViewById(R.id.copy_file_button);
        mCopyFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                copyFile(mCurrentDirUri);
            }
        });

        mShareButton = (Button) findViewById(R.id.share_button);
        mShareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareSns();
            }
        });

        mListView = (ListView) findViewById(R.id.storage_contents_listview);
        mAdapter = new DirectoryInformationAdapter(this);
        mListView.setAdapter(mAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // SDカードがマウントされているかどうか?
        if (isMountSdCard()) {
            mSdCardMountStateTextView.setText("Mount");
        } else {
            mSdCardMountStateTextView.setText("UnMount");
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_DIR_CODE && resultCode == Activity.RESULT_OK) {

            Uri uri = data.getData();

            Log.d(TAG + ":URI", uri.toString());
            Log.d(TAG + "getAuthority", uri.getAuthority());
            Log.d(TAG + "getHost", uri.getHost());
            //Log.d(TAG + "getFragment", uri.getFragment());
            Log.d(TAG + "getLastPathSegment", uri.getLastPathSegment());
            Log.d(TAG + "getScheme", uri.getScheme());
            //Log.d(TAG + "getUserInfo", uri.getUserInfo());
            Log.d(TAG + "getEncodedPath", uri.getEncodedPath());
            Log.d(TAG + "getPath", uri.getPath());
            Log.d(TAG + ":getEncordedAuthority", uri.getEncodedAuthority());
//            Log.d(TAG+":get",uri.getQuery());

            updateDirInfo(data.getData());
            mAdapter.notifyDataSetChanged();

            int takeFlags = data.getFlags();
            takeFlags &= (Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            getContentResolver().takePersistableUriPermission(data.getData(), takeFlags);
        }
    }

    // SAFのSystem Pickerを使って、任意のディレクトリを開く
    private void openDir() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        startActivityForResult(intent, REQUEST_DIR_CODE);
    }

    // ディレクトリを作る
    private void createDir(Uri uri, String dirName) {
        ContentResolver contentResolver = getContentResolver();
        // ディレクトリの木構造を表現するデータを作る
        Uri docUri = DocumentsContract.buildDocumentUriUsingTree(uri,
                DocumentsContract.getTreeDocumentId(uri));
        // ディレクトリを作成する
        DocumentsContract.createDocument(contentResolver, docUri, DocumentsContract.Document.MIME_TYPE_DIR, dirName);
    }

    // URIのディレクトリ直下にテキストファイルを作成する
    private void createTextFile(Uri uri, String fileName) {
        DocumentFile pickedDir = DocumentFile.fromTreeUri(this, uri);
        //DocumentFile newFile = pickedDir.createFile("text/plain", fileName);
        DocumentFile newFile = pickedDir.createFile("*/*", fileName);

        try {
            OutputStream out = getContentResolver().openOutputStream(newFile.getUri());
            String contents = "hello,world!!"; // テキストファイルに書き込む内容
            out.write(contents.getBytes());
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // TODO: 2017/05/04 これで画像のコピーはできるようになった。
    private void copyFile(Uri treeUti) {
        // filesの中身は全てJpegファイルであることを期待
        DocumentFile[] files = getFileListFromUri(treeUti);

        DocumentFile headFile = files[0];

        DocumentFile pickedDir = DocumentFile.fromTreeUri(this, treeUti);
        //DocumentFile newFile = pickedDir.createFile("image/jpeg", "isoyoung.jpg");
        // 拡張子を指定しないのは、動画も対応したいから(でも、同じファイル名だと、拡張子がおかしくなる)
        DocumentFile newFile = pickedDir.createFile("*/*", "hiromoon.jpg"); // TODO: 2017/05/04 これでいける?

        InputStream inputStream = null;
        OutputStream outputStream = null;

        BufferedInputStream bufferedInputStream = null;
        BufferedOutputStream bufferedOutputStream = null;

        try {
            inputStream = getContentResolver().openInputStream(headFile.getUri());
            outputStream = getContentResolver().openOutputStream(newFile.getUri());

            // BufferedなんとかStreamを使うのは、高速化のため
            bufferedInputStream = new BufferedInputStream(inputStream);
            bufferedOutputStream = new BufferedOutputStream(outputStream);

            bufferedOutputStream.write(readAll(bufferedInputStream));
            outputStream.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String readTextFromUri(Uri uri) throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(uri);
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                inputStream));
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line);
        }
        //fileInputStream.close();
        //parcelFileDescriptor.close();
        return stringBuilder.toString();
    }

    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor =
                getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return image;
    }

    private void setSp(String path) {
        SharedPreferences sharedPreferences = getSharedPreferences("DataSave", Context.MODE_PRIVATE);
        sharedPreferences.edit().putString("DataPath", path).apply();
    }

    private String getSp() {
        SharedPreferences sharedPreferences = getSharedPreferences("DataSave", Context.MODE_PRIVATE);
        return sharedPreferences.getString("DataPath", null);
    }

    private byte[] readAll(InputStream inputStream) throws IOException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024 * 64];
        while (true) {
            int len = inputStream.read(buffer);
            if (len < 0) {
                break;
            }
            bout.write(buffer, 0, len);
        }
        return bout.toByteArray();
    }

    /**
     * ディレクトリ内の情報を更新する
     */
    private void updateDirInfo(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        Uri docUri = DocumentsContract.buildDocumentUriUsingTree(uri,
                DocumentsContract.getTreeDocumentId(uri));
        Uri childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(uri,
                DocumentsContract.getTreeDocumentId(uri));

        Cursor docCursor = contentResolver.query(docUri, new String[]{
                DocumentsContract.Document.COLUMN_DISPLAY_NAME, DocumentsContract.Document.COLUMN_MIME_TYPE}, null, null, null);
        try {
            while (docCursor.moveToNext()) {
                mCurrentDirUri = uri;
                mSelectedDirNameTextView.setText(docCursor.getString(0));
            }
        } finally {
            closeQuietly(docCursor);
        }

        Cursor childCursor = contentResolver.query(childrenUri, new String[]{
                DocumentsContract.Document.COLUMN_DISPLAY_NAME, DocumentsContract.Document.COLUMN_MIME_TYPE, DocumentsContract.Document.COLUMN_DOCUMENT_ID}, null, null, null);
        try {
            List<DirectoryInformationDto> directoryInfomationDtos = new ArrayList<>();
            while (childCursor.moveToNext()) {
                DirectoryInformationDto dto = new DirectoryInformationDto();
                dto.fileName = childCursor.getString(0);
                dto.mimeType = childCursor.getString(1);
                dto.filePath = childCursor.getString(2);

                directoryInfomationDtos.add(dto);
            }
            mAdapter.setDirectoryInformation(directoryInfomationDtos);
            mAdapter.notifyDataSetChanged();
        } finally {
            closeQuietly(childCursor);
        }

    }

    public void closeQuietly(AutoCloseable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (RuntimeException rethrown) {
                throw rethrown;
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * ディレクトリのURIから、ディレクトリに含まれるファイル/ディレクトリのURIのリストを取得する
     *
     * @param dirUri
     */
    private DocumentFile[] getFileListFromUri(Uri dirUri) {

        DocumentFile documentFile = DocumentFile.fromTreeUri(this, dirUri);
        return documentFile.listFiles();

        // Log.d(TAG,docFile.getUri()); // ファイルのURI
        //Log.d(TAG, docFile.getUri().getPath()); //ファイルのパス
//            try {
//                //Log.d(TAG, readTextFromUri(docFile.getUri()));
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
    }


    /**
     * 指定したURIのファイルを削除する
     *
     * @param uri
     */
    private void deleteFileFromUri(Uri uri) {
        DocumentsContract.deleteDocument(getContentResolver(), uri);
    }


    /**
     * URIリスト全てのファイルを削除する
     *
     * @param documentFiles
     */
    private void deleteAllFileFromUri(DocumentFile[] documentFiles) {
        for (DocumentFile docFile : documentFiles) {
            deleteFileFromUri(docFile.getUri());
        }
    }

    /**
     * SDカードがマウントされているかどうか?
     *
     * @return
     */
    private boolean isMountSdCard() {
        //File[] dirs = ContextCompat.getExternalFilesDirs(this, null);
        File[] dirs = getExternalFilesDirs(null);

        if (dirs == null) {
            return false;
        }

        for (File dir : dirs) {

            if (dir == null) {
                return false;
            }
            if (Environment.isExternalStorageRemovable(dir)) {
                return true;
            }
        }

        return false;

    }

    private void shareSns() {
        DocumentFile[] docList = null;

        if (mCurrentDirUri != null) {
            docList = getFileListFromUri(mCurrentDirUri);
        }
        Uri uri = docList[1].getUri();

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setPackage("com.twitter.android");
        intent.setType("image/jpeg");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        startActivity(intent);

    }

}

