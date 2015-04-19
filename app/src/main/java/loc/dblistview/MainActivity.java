package loc.dblistview;
import android.app.*;
import android.content.*;
import android.database.*;
import android.net.*;
import android.os.*;
import android.provider.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import android.widget.ExpandableListView.*;
import java.util.*;

public class MainActivity extends Activity
{

    private static final String TAG="MainActivity";
    ExpandableListAdapter listAdapter;
    ExpandableListView expListView;
    List<String> listDataHeader;
	ArrayList<Contact> person;
	ArrayList<Contactable> data;
	Random r;
	Context ctxt = this;
	InternalContactMgr intDB;

	@Override
	protected void onDestroy()
	{
		// TODO: Implement this method
		super.onDestroy();
		flushContactsToStorage();
	}

	@Override
	protected void onStop()
	{
		// TODO: Implement this method
		super.onStop();
	}

    @Override
    protected void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
		Log.i(TAG, "ContentView up.");
		r = new Random(System.currentTimeMillis());
        // get the listview
        expListView = (ExpandableListView) findViewById(R.id.lvExp);
        Log.i(TAG, "Listview resource found.");
        // preparing list data
		Log.w(TAG, "Calling List Prep method...");
		intDB = new InternalContactMgr(this);
        prepareListData();
		Log.i(TAG, "Sending to ListAdapter..."); // Now we link our data to the ListAdapter
		try
		{
            listAdapter = new ExpandableListAdapter(this, person);
            Log.i(TAG, "Trying to allocate to ListView...");
            expListView.setAdapter(listAdapter);
			Log.i(TAG, "ListView ready.");
		}
		catch (Exception zzz)
		{
            zzz.printStackTrace();
		}

        /* The following methods within the remainder of this onCreate method are Listeners
         * intended to handle taps, expand and collapse events.
         */
		try
		{
			// Listview Group click listener
			expListView.setOnGroupClickListener(new OnGroupClickListener() {

					@Override
					public boolean onGroupClick(ExpandableListView parent, View v,
												int groupPosition, long id)
					{
						return false;
					}
				});
			// Listview child click listener
			expListView.setOnChildClickListener(new OnChildClickListener() {

					@Override
					public boolean onChildClick(ExpandableListView parent, View v,
												int groupPosition, int childPosition, long id)
					{
						return false;
					}
				});
			// Listview Group expanded listener
			expListView.setOnGroupExpandListener(new OnGroupExpandListener() {

					@Override
					public void onGroupExpand(int groupPosition)
					{
						Toast.makeText(getApplicationContext(),
									   person.get(groupPosition).dispName + " Expanded",
									   Toast.LENGTH_SHORT).show();
					}
				});
			// Listview Group collasped listener
			expListView.setOnGroupCollapseListener(new OnGroupCollapseListener() {

					@Override
					public void onGroupCollapse(int groupPosition)
					{
						Toast.makeText(getApplicationContext(),
									   person.get(groupPosition).dispName + " Collapsed",
									   Toast.LENGTH_SHORT).show();

					}
				});
			// Long click listener
			expListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
					@Override
					public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
					{
						Toast.makeText(getApplicationContext(),
									   person.get(position).dispName + " Long Click event detected",
									   Toast.LENGTH_SHORT).show();
						// This is intended to determine if a contact is to be removed.
						final int selectedPerson = position;
						AlertDialog.Builder alertDialog2 = new AlertDialog.Builder(MainActivity.this);
						alertDialog2.setTitle("Confirm Delete..."); // Setting Dialog Title
						alertDialog2.setMessage("Are you sure you want delete this file?"); // Setting Dialog Message
						alertDialog2.setIcon(R.drawable.image_1); // Setting Icon to Dialog
						alertDialog2.setPositiveButton("Delete",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									// Write your code here to execute after dialog
									execDelete(selectedPerson);
								}
							});
						alertDialog2.setNegativeButton("Cancel",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									// Write your code here to execute after dialog
									dialog.cancel();
								}
							});
						alertDialog2.show();
						return true;
					}
				});
		}
		catch (Exception zz)
		{
			zz.printStackTrace();
		}
    }
	
	public void execDelete(int position){
		person.remove(position);
		listAdapter.notifyDataSetChanged();
	}
	
	public void flushContactsToStorage(){
		// Sends all contacts to DB
		intDB.connectDatabase();
		intDB.flushToDisk(person);
		intDB.closeDatabase();
	}
	
	public void getContactsFromStorage(){
		// Gets all contacts from DB
		intDB.connectDatabase();
		person = intDB.pullFromDisk();
		intDB.closeDatabase();
	}

    /*
     * Preparing the list data - This is done via internal ArrayList data.
     * I need to try and adapt this for use with an SQLite Database.
     */
    private void prepareListData()
	{
		Log.w(TAG, "Arraylists allocating");
        person = new ArrayList<>();
		data = new ArrayList<>();
		Log.i(TAG, "Adding data..."); // Testing follows with dummy data
		getContactsFromStorage();
    }
	
	public void addFromContactsApp(View v){
		Log.i(TAG,"ContactPicker launching...");
		Toast.makeText(this,"Contact Picker: Select one.",Toast.LENGTH_SHORT).show();
		try{
		Intent contactPickerIntent = new Intent(Intent.ACTION_PICK,
												ContactsContract.Contacts.CONTENT_URI);
		startActivityForResult(contactPickerIntent, 1001);
		}catch(Exception x){
			x.printStackTrace();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		// TODO: Implement this method
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode==Activity.RESULT_OK){
			Toast.makeText(this,"Result processing...",Toast.LENGTH_SHORT).show();
			Uri contactData = data.getData();
			Cursor c = managedQuery(contactData, null, null, null, null);
			if (c.moveToFirst()) {
				String name = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
				String uid = c.getString(c.getColumnIndex(ContactsContract.Contacts._ID));
				Toast.makeText(this,name+" ("+uid+")",Toast.LENGTH_SHORT).show();
				Contact ctt = pullFromContactPicker(uid,name);
				person.add(ctt);
				listAdapter.notifyDataSetChanged();
			}
		} else {
			Toast.makeText(this,"Cancelled by user.",Toast.LENGTH_LONG).show();
		}
	}
	
	public Contact pullFromContactPicker(String uid, String dispname){
		Contact output = new Contact(Integer.parseInt(uid), dispname);
		// Modes 1=Phone, 2,Email.
		Log.i(TAG,"========  Check for errors  ========");
		Cursor c;
		Uri conURI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
		String dtp,dlb,dval,did,dnm;
		dlb="";
		did="";
		dnm="";
		String sel;
		String[] selArgs;
		int ctcat,ci_dt,ci_dl,ci_dv;
		dtp="";
		dval="";
		ctcat=1;
		sel="";
		selArgs=new String[]{""};
		do{
			if(ctcat==1) {
				dtp=ContactsContract.CommonDataKinds.Phone.TYPE;
				dval=ContactsContract.CommonDataKinds.Phone.NUMBER;
				dlb=ContactsContract.CommonDataKinds.Phone.LABEL;
				did=ContactsContract.CommonDataKinds.Phone.CONTACT_ID;
				dnm=ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME;
				conURI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
				sel=ContactsContract.CommonDataKinds.Phone.CONTACT_ID+" = ?";
				selArgs = new String[]{uid};
			}
			if(ctcat==2) {
				dtp=ContactsContract.CommonDataKinds.Email.TYPE;
				dval=ContactsContract.CommonDataKinds.Email.ADDRESS;
				dlb=ContactsContract.CommonDataKinds.Email.LABEL;
				did=ContactsContract.CommonDataKinds.Email.CONTACT_ID;
				dnm=ContactsContract.CommonDataKinds.Email.DISPLAY_NAME;
				conURI = ContactsContract.CommonDataKinds.Email.CONTENT_URI;
				sel=ContactsContract.CommonDataKinds.Email.CONTACT_ID+" = ?";
				selArgs = new String[]{uid};
			}
			if(ctcat==3){
				dtp=ContactsContract.CommonDataKinds.Im.PROTOCOL;
				dlb=ContactsContract.CommonDataKinds.Im.CUSTOM_PROTOCOL;
				dval=ContactsContract.CommonDataKinds.Im.DATA;
				did=ContactsContract.CommonDataKinds.Im.CONTACT_ID;
				dnm=ContactsContract.CommonDataKinds.Im.DISPLAY_NAME;
				conURI = ContactsContract.Data.CONTENT_URI;
				sel=ContactsContract.Data.CONTACT_ID+" = ? AND "+ContactsContract.Data.MIMETYPE+" = ?";
				selArgs = new String[]{uid,ContactsContract.CommonDataKinds.Im.CONTENT_ITEM_TYPE};
			}
			c = this.getContentResolver().query(conURI,null,sel,selArgs,null);
			ci_dt = c.getColumnIndex(dtp);
			ci_dv = c.getColumnIndex(dval);
			ci_dl = c.getColumnIndex(dlb);
			Log.i(TAG,"Cursor has returned "+String.valueOf(c.getCount()));
			Log.i(TAG,sel);
			Log.i(TAG,selArgs[0]);
			Log.w(TAG,conURI.toString());
			if(c.moveToFirst()){
				do{
					output.addEntry(ctcat,c.getInt(ci_dt),c.getString(ci_dv),c.getString(ci_dl),true);
					Log.w(TAG,"============== Entry "+String.format("%02d",c.getPosition())+" ==============");
					Log.i(TAG,"Category       :"+ctcat);
					Log.i(TAG,"Raw Data Type  :"+c.getInt(ci_dt));
					Log.i(TAG,"Data Label     :"+c.getString(ci_dl));
					Log.i(TAG,"Raw Data Value :"+c.getString(ci_dv));
					Log.w(TAG,"======================================");
				}while(c.moveToNext());
			} else {
				Log.w(TAG,"Nothing returned.");
			}
			c.close();
			ctcat++;
		}while(ctcat<4);
		Toast.makeText(this,"Task complete",Toast.LENGTH_SHORT).show();
		// Toast.makeText(this,output.dispName + output.data.size(),Toast.LENGTH_SHORT).show();
		return output;
	}
}
