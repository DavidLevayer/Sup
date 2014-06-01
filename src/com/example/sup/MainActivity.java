package com.example.sup;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	/**
	 * The container view which has layout change animations turned on. In this sample, this view
	 * is a {@link android.widget.LinearLayout}.
	 */
	private ViewGroup mContainerView;
	
	/**
	 * The list of supItem that is extracted from the XML file
	 * and then kept up to date until closing it
	 */
	private Map<String,SupItem> itemList;
	
	/**
	 * XML file name that we'll manipulate
	 */
	String XMLFileName;
	
	/**
	 * XML file folder where the XML file is located
	 */
	String XMLFolderName;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mContainerView = (ViewGroup) findViewById(R.id.container);

		// Getting the file information for future writings
		XMLFileName = getResources().getString(R.string.supItem_XMLFileName);	
		XMLFolderName = getResources().getString(R.string.app_folder_name);

		// Override the comparator, so the item are sorted from the oldest to the latest
		// Override the comparator, so the item are sorted from the oldest to the latest
		itemList = new TreeMap<String,SupItem>(new Comparator<String>() {
			public int compare(String o1, String o2){
				if(o1.compareTo(o2)<0) return -1;
				else if(o1.compareTo(o2)>0) return 1;
				else return 0;
			}
		});		

		// Loading previous operations
		String line;
		Pattern idPattern = Pattern.compile("id=\"([0-9]*)\"");
		Pattern reasonPattern = Pattern.compile("label=\"([a-zA-Z ]*)\"");
		Pattern amountPattern = Pattern.compile(">([0-9]*.?[0-9]*)<");
		Matcher matcherID, matcherReason, matcherAmount;

		try {

			File file = new File(Environment.getExternalStorageDirectory()+"/"+XMLFolderName,XMLFileName);
			FileInputStream fis = new FileInputStream(file);
			BufferedReader br = new BufferedReader(new InputStreamReader(fis));

			while((line=br.readLine())!=null) {
				matcherID = idPattern.matcher(line);
				matcherReason = reasonPattern.matcher(line);
				matcherAmount = amountPattern.matcher(line);
				matcherID.find();
				matcherReason.find();
				matcherAmount.find();
				addRow(new SupItem(matcherID.group(1), matcherReason.group(1),
						Double.valueOf(matcherAmount.group(1))));
			}

			br.close();
			fis.close();

		} catch (Exception e) {
			//TODO
		}
	}
	
	public void onDestroy() {
		super.onDestroy();

		// Saving the operation in the XML file
		String content= new String();

		for(Entry<String,SupItem> entry : itemList.entrySet()) {
			SupItem c = entry.getValue();
			String line = new String("<item id=\""+c.getID()+"\" label=\""+
					c.getLabel()+"\">"+c.getValue()+"</outcome>\n");
			content = content.concat(line);			
		}
		
		File folder = new File(Environment.getExternalStorageDirectory(), XMLFolderName);
		File file = new File(Environment.getExternalStorageDirectory()+"/"+XMLFolderName,
				XMLFileName);

		if(!folder.exists())
			folder.mkdirs();

		if(!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		try {
			FileWriter fw = new FileWriter(file, false);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(content);
			bw.close();
			fw.close();
		} catch(Exception e){
			Toast.makeText(this, "error during XML edition", Toast.LENGTH_SHORT).show();
		}

	}

	private void addRow(SupItem row) {

		// Hide the "empty" view since there is now at least one item in the list.
		findViewById(android.R.id.empty).setVisibility(View.INVISIBLE);

		// Add it to the outcome list
		final String index = row.getID();
		itemList.put(index, row);

		// Instantiate a new "row" view.
		final ViewGroup newView = (ViewGroup) LayoutInflater.from(this).inflate(
				R.layout.supitem, mContainerView, false);

		if(itemList.size()%2==0)
			newView.setBackgroundColor(getResources().getColor(R.color.row_off_background));

		// Set texts in the new row
		((TextView) newView.findViewById(R.id.label)).setText(row.getLabel());
		((TextView) newView.findViewById(R.id.value)).setText(row.getValue()+ " "+getResources().getString(R.string.currency));

		// Set a click listener for the "X" button in the row that will remove the row.
		newView.findViewById(R.id.delete_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				// Remove the row from its parent (the container view).
				// Because mContainerView has android:animateLayoutChanges set to true,
				// this removal is automatically animated.
				mContainerView.removeView(newView);

				// Remove the specific line from the list
				itemList.remove(index);

				// If there are no rows remaining, show the empty view.
				if (mContainerView.getChildCount() == 0) {
					findViewById(android.R.id.empty).setVisibility(View.VISIBLE);
				}
			}
		});

		// Because mContainerView has android:animateLayoutChanges set to true,
		// adding this view is automatically animated.
		mContainerView.addView(newView, 0);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case R.id.action_add_item:
			// Hide the "empty" view since there is now at least one item in the list.
			findViewById(android.R.id.empty).setVisibility(View.GONE);
			addItem();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	private void addItem() {
		// Pop the dialog
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		LayoutInflater inflater = getLayoutInflater();
		builder
		.setView(inflater.inflate(R.layout.supitemdialog, null))
		.setPositiveButton(R.string.validate, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {

				// Checking the values...
				// TODO

				// ...and add it to the concerned XML file
				SupItem mRow = createItemFromDialog((Dialog)dialog);

				// Add the row to the listview
				addRow(mRow);
				
			}
		})
		.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub

			}
		});
		
		builder.show();
	}
	
	private SupItem createItemFromDialog(Dialog currentDialogView) {
		
		// Getting values of the current countable item
		String mReason = ((EditText) currentDialogView.findViewById(R.id.dialog_label_value)).getText().toString();
		String mValue = ((EditText) currentDialogView.findViewById(R.id.dialog_amount_value)).getText().toString();
		
		//Generation of an unique ID
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss", Locale.FRANCE);
		
		return new SupItem(dateFormat.format(new Date()), mReason, Double.valueOf(mValue));			
	}
	
	


}
