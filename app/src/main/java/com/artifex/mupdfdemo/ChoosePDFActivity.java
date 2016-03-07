package com.artifex.mupdfdemo;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileObserver;
import android.os.Handler;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.cinread.ebook.TXTActivity;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

enum Purpose {
	PickPDF,
	PickTXT,
	PickKeyFile
}

public class ChoosePDFActivity extends ListActivity {
	static public final String PICK_KEY_FILE = "PICK_KEY_FILE";
	public static final String PICK_PDF = "PICK_PDF";
	public static final String PICK_TXT = "PICK_TXT";
	static private File  mDirectory;
	static private Map<String, Integer> mPositions = new HashMap<String, Integer>();
	private File         mParent;
	private File []      mDirs;
	private File []      mFiles;
	private Handler	     mHandler;
	private Runnable     mUpdateFiles;
	private ChoosePDFAdapter adapter;
	private Purpose      mPurpose;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//注释 区分pdf和txt
		String action = getIntent().getAction();
//		if (action.equals(PICK_KEY_FILE)){
//			mPurpose = Purpose.PickKeyFile;
//		}else if (action.equals(PICK_PDF)){
//			mPurpose = Purpose.PickPDF;
//		}else if (action.equals(PICK_TXT)){
//			mPurpose = Purpose.PickTXT;
//		}
		mPurpose = PICK_KEY_FILE.equals(action) ? Purpose.PickKeyFile : Purpose.PickPDF;


		String storageState = Environment.getExternalStorageState();

		if (!Environment.MEDIA_MOUNTED.equals(storageState)
				&& !Environment.MEDIA_MOUNTED_READ_ONLY.equals(storageState))
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.no_media_warning);
			builder.setMessage(R.string.no_media_hint);
			AlertDialog alert = builder.create();
			alert.setButton(AlertDialog.BUTTON_POSITIVE,getString(R.string.dismiss),
					new OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							finish();
						}
					});
			alert.show();
			return;
		}

		if (mDirectory == null)
//			mDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
			mDirectory = Environment.getExternalStorageDirectory();

		// Create a list adapter...
		adapter = new ChoosePDFAdapter(getLayoutInflater());
		setListAdapter(adapter);

		// ...that is updated dynamically when files are scanned
		mHandler = new Handler();
		mUpdateFiles = new Runnable() {
			public void run() {
				Resources res = getResources();
				String appName = res.getString(R.string.app_name);
				String version = res.getString(R.string.version);
				String title = res.getString(R.string.picker_title_App_Ver_Dir);
				setTitle(String.format(title, appName, version, mDirectory));

				mParent = mDirectory.getParentFile();

				mDirs = mDirectory.listFiles(new FileFilter() {

					public boolean accept(File file) {
						return file.isDirectory();
					}
				});
				if (mDirs == null)
					mDirs = new File[0];

				mFiles = mDirectory.listFiles(new FileFilter() {

					public boolean accept(File file) {
						if (file.isDirectory())
							return false;
						String fname = file.getName().toLowerCase();
						switch (mPurpose) {
						case PickPDF:
							if (fname.endsWith(".pdf"))
								return true;
							if (fname.endsWith(".xps"))
								return true;
							if (fname.endsWith(".cbz"))
								return true;
							if (fname.endsWith(".epub"))
								return true;
							if (fname.endsWith(".png"))
								return true;
							if (fname.endsWith(".jpe"))
								return true;
							if (fname.endsWith(".jpeg"))
								return true;
							if (fname.endsWith(".jpg"))
								return true;
							if (fname.endsWith(".jfif"))
								return true;
							if (fname.endsWith(".jfif-tbnl"))
								return true;
							if (fname.endsWith(".tif"))
								return true;
							if (fname.endsWith(".tiff"))
								return true;
							//注释  显示txt格式文件
							if (fname.endsWith(".txt"))
								return true;
							return false;
						case PickKeyFile:
							if (fname.endsWith(".pfx"))
								return true;
							return false;
						default:
							return false;
						}
					}
				});
				if (mFiles == null)
					mFiles = new File[0];

				Arrays.sort(mFiles, new Comparator<File>() {
					public int compare(File arg0, File arg1) {
						return arg0.getName().compareToIgnoreCase(arg1.getName());
					}
				});

				Arrays.sort(mDirs, new Comparator<File>() {
					public int compare(File arg0, File arg1) {
						return arg0.getName().compareToIgnoreCase(arg1.getName());
					}
				});

				adapter.clear();
				if (mParent != null)
					adapter.add(new ChoosePDFItem(ChoosePDFItem.Type.PARENT, getString(R.string.parent_directory)));
				for (File f : mDirs)
					adapter.add(new ChoosePDFItem(ChoosePDFItem.Type.DIR, f.getName()));
				for (File f : mFiles)
					adapter.add(new ChoosePDFItem(ChoosePDFItem.Type.DOC, f.getName()));

				lastPosition();
			}
		};

		// Start initial file scan...
		mHandler.post(mUpdateFiles);

		// ...and observe the directory and scan files upon changes.
		FileObserver observer = new FileObserver(mDirectory.getPath(), FileObserver.CREATE | FileObserver.DELETE) {
			public void onEvent(int event, String path) {
				mHandler.post(mUpdateFiles);
			}
		};
		observer.startWatching();
	}

	private void lastPosition() {
		String p = mDirectory.getAbsolutePath();
		if (mPositions.containsKey(p))
			getListView().setSelection(mPositions.get(p));
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		mPositions.put(mDirectory.getAbsolutePath(), getListView().getFirstVisiblePosition());

		if (position < (mParent == null ? 0 : 1)) {
			mDirectory = mParent;
			mHandler.post(mUpdateFiles);
			return;
		}

		position -= (mParent == null ? 0 : 1);

		if (position < mDirs.length) {
			mDirectory = mDirs[position];
			mHandler.post(mUpdateFiles);
			return;
		}

		position -= mDirs.length;
		String path = mFiles[position].getAbsolutePath();
		if(path.endsWith("txt")){
			Toast.makeText(ChoosePDFActivity.this, "PickTXT "+path, Toast.LENGTH_SHORT).show();
			//如果点击了txt格式文件，跳转至txt解码器  TODO txt解码
			Intent intent = new Intent(this, TXTActivity.class);
			intent.setAction(Intent.ACTION_VIEW);
			intent.setData(Uri.parse("file://"+path));
			startActivity(intent);
		}else {
			//打开正确的PDF
			Uri uri = Uri.parse(path);
			Intent intent = new Intent(this, MuPDFActivity.class);
			intent.setAction(Intent.ACTION_VIEW);
			intent.setData(uri);
			switch (mPurpose) {
				case PickPDF:
					Toast.makeText(ChoosePDFActivity.this, "PickPDF", Toast.LENGTH_SHORT).show();
					// Start an activity to display the PDF file
					startActivity(intent);
					break;
				case PickKeyFile:
					// Return the uri to the caller
					setResult(RESULT_OK, intent);
					finish();
					break;
			}
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (mDirectory != null)
			mPositions.put(mDirectory.getAbsolutePath(), getListView().getFirstVisiblePosition());
	}
}
