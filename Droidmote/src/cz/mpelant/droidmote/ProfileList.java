package cz.mpelant.droidmote;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

/**
 * Displays the profile list.
 */
public class ProfileList extends Activity {

	/** The profiles. */
	private ArrayList<ProfileItem> profiles;

	/** The data provider. */
	private DataProvider data;

	/** The TAG. */
	public static String TAG = "droidmote";

	/** The list view. */
	private ListView listView;

	/**
	 * The Class ProfileItem for the Adapter which holds the data for a specific profile.
	 */
	class ProfileItem {

		/** The id of the profile. */
		public long id;

		/** The name of the profile. */
		public String name;

		/**
		 * Instantiates a new profile item.
		 * 
		 * @param id the id
		 * @param name the name
		 */
		public ProfileItem(long id, String name) {
			this.id = id;
			this.name = name;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return name;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object o) {
			ProfileItem item = (ProfileItem) o;
			if (item.id == id)
				return true;
			return false;
		}
	}

	/**
	 * The listener interface for receiving editItem events. The class that is interested in processing a editItem event implements this interface, and the object created with that
	 * class is registered with a component using the component's <code>addEditItemListener<code> method. When
	 * the editItem event occurs, that object's appropriate
	 * method is invoked.
	 * 
	 * @see EditItemEvent
	 */
	class EditItemListener implements AdapterView.OnItemLongClickListener {

		/** The item. */
		private ProfileItem item;

		/**
		 * Start edit activity.
		 */
		private void startEditActivity() {
			Intent i = new Intent(ProfileList.this, ProfileEdit.class);
			i.putExtra(ProfileEdit.EXTRA_PROFILE_ID, item.id);
			startActivityForResult(i, 0);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.widget.AdapterView.OnItemLongClickListener#onItemLongClick(android.widget.AdapterView, android.view.View, int, long)
		 */
		@Override
		public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int position, long id) {
			item = profiles.get(position);
			AlertDialog.Builder alertDialog = new AlertDialog.Builder(ProfileList.this);
			alertDialog.setPositiveButton(getResources().getString(R.string.edit), new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					startEditActivity();
				}
			});

			alertDialog.setNegativeButton(getResources().getString(R.string.delete), new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					data.open();
					data.deleteProfile(item.id);
					data.close();
					if (PreferenceManager.getDefaultSharedPreferences(ProfileList.this).getLong(SuperActivity.DATA_LAST_PROFILE, -1) == item.id) {
						Editor editor = PreferenceManager.getDefaultSharedPreferences(ProfileList.this).edit();
						editor.remove(SuperActivity.DATA_LAST_PROFILE);
						editor.commit();
					}
					loadData();
				}
			});
			alertDialog.show();
			return true;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.profile_list);
		data = new DataProvider(this);
		listView = (ListView) findViewById(R.id.list);
		loadData();

		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
				ProfileItem item = profiles.get(position);
				Intent i = new Intent(ProfileList.this, ProfileView.class);
				i.putExtra(ProfileEdit.EXTRA_PROFILE_ID, item.id);
				startActivityForResult(i, 0);
			}
		});

		listView.setOnItemLongClickListener(new EditItemListener());

		Button btAdd = (Button) findViewById(R.id.btAdd);
		btAdd.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent i = new Intent(ProfileList.this, ProfileEdit.class);
				startActivityForResult(i, 0);
			}
		});
		super.onCreate(savedInstanceState);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		loadData();
		super.onActivityResult(requestCode, resultCode, data);
	}

	/**
	 * Load data.
	 */
	private void loadData() {
		profiles = new ArrayList<ProfileItem>();
		data.open();
		Cursor cursor = data.fetchAllProfiles();

		if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
			int columnID = cursor.getColumnIndex(DataProvider.PROFILES_ROWID);
			int columnName = cursor.getColumnIndex(DataProvider.PROFILES_NAME);
			do {
				profiles.add(new ProfileItem(cursor.getLong(columnID), cursor.getString(columnName)));
			} while (cursor.moveToNext());
		}
		data.close();
		listView.setAdapter(new ArrayAdapter<ProfileItem>(this, android.R.layout.simple_list_item_1, profiles));
	}
}
