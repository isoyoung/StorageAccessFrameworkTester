package isoyoung.com.storageaccessframeworktester;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * ディレクトリ内の中身を表示するためのAdapterクラス
 */
public class DirectoryInformationAdapter extends BaseAdapter {

    private Context mContext;
    private List<DirectoryInformationDto> mDirectoryInfomations;
    private LayoutInflater mInflater;

    public DirectoryInformationAdapter(Context context) {
        mContext = context;

        mDirectoryInfomations = new ArrayList<>();
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return mDirectoryInfomations.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        convertView = mInflater.inflate(R.layout.listview_item, null);

        TextView fileNameTextView = (TextView) convertView.findViewById(R.id.file_name_textview);
        TextView mimeTypeTextView = (TextView) convertView.findViewById(R.id.mime_type_textview);

        fileNameTextView.setText(mDirectoryInfomations.get(position).fileName);
        mimeTypeTextView.setText(mDirectoryInfomations.get(position).mimeType);

        return convertView;

    }

    public void setDirectoryInformation(List<DirectoryInformationDto> directoryInfomations) {
        mDirectoryInfomations = directoryInfomations;
    }
}
