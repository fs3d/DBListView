package loc.dblistview;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.Data;
import android.util.Log;

import java.util.ArrayList;
import android.support.annotation.*;

/**
 * Created by peteb_000 on 18/02/2015.
 * DataManager class. This is intended to handle transactions to and from the internal database
 * via the DBMaster class.
 */
public class InternalContactMgr {
	private static final String TAG = "dataManager";
	protected Context ctxt;
	private String conName;
	private int conId;
	private DBMaster data;
	private Cursor crsr, ecrsr;
	private SQLiteDatabase db;
	private boolean rw = false;

	public InternalContactMgr(Context ctxt){
		this.ctxt = ctxt.getApplicationContext();
		rw=false;
	}

	public InternalContactMgr(Context ctxt, int conId, String conName) {
		this.conName = conName;
		this.conId = conId;
		this.ctxt = ctxt.getApplicationContext();
		rw=true;
	}

	public ArrayList<Contact> pullFromDisk()
	{
		// TODO: Implement this method
		return null;
	}

	public void connectDatabase() {
		// Open connection to database
		data = new DBMaster(ctxt);
		if (rw)
			db = data.getWritableDatabase();
		else
			db = data.getReadableDatabase();
	}

	public void closeDatabase() {
		// Close and exit database
		if (db.isOpen())
			db.close();
		data.close();
	}
	
	public void flushToDisk(ArrayList<Contact> outputData){
		// This should tie in a bunch of functions related to writing records to disk.
		int last = outputData.size();
		Contact current;
		for(int i=0;i<last;i++){
			current = outputData.get(i);
			String txtid = String.valueOf(current.uid);
			String dName = current.dispName;
			for(int subi=0;subi<current.data.size();subi++){
				Contactable currentdata = current.data.get(subi);
				// Extract data from Contact ArrayLists
				String dValue = currentdata.getValue();
				String dLbl = currentdata.getLabel();
				String dCat = String.valueOf(currentdata.getCat());
				String dType = String.valueOf(currentdata.getType());
				String dSwitch = String.valueOf(currentdata.getStatus());
				// Put the data into a record.
				ContentValues cv = new ContentValues();
				cv.put(data.COL_UID,txtid);
				cv.put(data.COL_DISPLAYNAME,dName);
				cv.put(data.COL_CATEGORY,dCat);
				cv.put(data.COL_TYPE,dType);
				cv.put(data.COL_LABEL,dLbl);
				cv.put(data.COL_VALUE,dValue);
				cv.put(data.COL_SWITCH,dSwitch);
				// Send data record to DB
				db.insertOrThrow(data.TBLMASTER,null,cv);
				Log.i(TAG,"Iterating "+String.valueOf(subi)+" of "+current.data.size()+" in top level record "+String.valueOf(i)+" of "+String.valueOf(last)+"...");
			}
		}
		
	}

	public boolean checkDataExists(int dataType, String dataLabel, String dataValue, String activated) {
		/* Access entry for its' existence.
		 * This will return a boolean value to determine if a given record exists. */
		String sel = DBMaster.COL_UID + " LIKE ? AND " + DBMaster.COL_VALUE + " LIKE ? AND "
			+ DBMaster.COL_TYPE + " LIKE ? AND " + DBMaster.COL_LABEL + " LIKE ? AND "
			+ DBMaster.COL_SWITCH + " LIKE ?";
		String[] selArgs = new String[]{String.valueOf(conId), dataValue, String.valueOf(dataType), dataLabel, String.valueOf(activated)};
		Cursor crsr = db.query(DBMaster.TBLMASTER, null, sel, selArgs, null, null, null);
		if (crsr.moveToFirst()) {
			Log.w(TAG, "Cursor has returned results. This is because the data exists exactly as matched and already exists in the database.");
			return true;
		} else {
			Log.d(TAG, "Cursor is empty. Records not found. This is to be expected if you are trying to add this data and it's not present yet.");
			return false;
		}
	}

	public void modifyData(int dataType, String dataLabel, int colcat, String dataValue, String activated) {
		/* Access entry ands modify its' content.
		 * Argument 1 (String) is the type of data in String format from the resource provided by the current locale.
		 *            This will contain a string value for the label if it is custom.
		 * Argument 2 (String) is the value of the data in normalised or raw form, depending on the type.
		 * Argument 3 (boolean) is the switch to determine if the data will be used by the Service.
		 *            0 = Dormant. 1 = Active.
		 */
		if (rw){
			if (!db.isOpen()) {
			} else {
				// Check for existing records matching the above criteria...
				String sel = DBMaster.COL_UID + " LIKE ? AND " + DBMaster.COL_VALUE + " LIKE ? AND "
					+ DBMaster.COL_TYPE + " LIKE ? AND " + DBMaster.COL_LABEL + " LIKE ? AND " + DBMaster.COL_SWITCH + " LIKE ?";
				String[] selArgs = new String[]{String.valueOf(conId), dataValue, String.valueOf(dataType), dataLabel, String.valueOf(activated)};
				Cursor crsr = db.query(DBMaster.TBLMASTER, null, sel, selArgs, null, null, null);
				if (crsr.moveToFirst()) {
					// Exists already. We can proceed.
					ContentValues cv = new ContentValues();
					cv.put(data.COL_UID, conId);
					cv.put(data.COL_DISPLAYNAME, conName);
					cv.put(data.COL_CATEGORY, colcat);
					cv.put(data.COL_TYPE, dataType);
					cv.put(data.COL_LABEL, dataLabel);
					cv.put(data.COL_VALUE, dataValue);
					cv.put(data.COL_SWITCH, activated);
					db.update(DBMaster.TBLMASTER, cv, DBMaster.COL_UID + " = ? AND " + DBMaster.COL_VALUE + " = ? AND "
							  + DBMaster.COL_TYPE + " = ? AND " + DBMaster.COL_LABEL + " = ?",
							  new String[]{String.valueOf(conId), dataValue, String.valueOf(dataType), dataLabel});
				}
				crsr.close();
			}
		}
	}

	public String addData(int dataType, String dataLabel, int colcat, String dataValue, String activated, boolean override) {
		if ((dataLabel.equals("_") && dataValue.equals("_")))
			return "INVALID_DATA";
		// Append a new entry to the database
		if (dataValue == null)
			return "INVALID_DATA";
		if (db.isOpen()) {
			if (db.isReadOnly())
				return "READ_ONLY";
			// Can write to database.
			ContentValues cv = new ContentValues();
			cv.put(data.COL_UID, conId);
			cv.put(data.COL_DISPLAYNAME, conName);
			cv.put(data.COL_CATEGORY, colcat);
			cv.put(data.COL_TYPE, dataType);
			cv.put(data.COL_LABEL, dataLabel);
			cv.put(data.COL_VALUE, dataValue);
			cv.put(data.COL_SWITCH, String.valueOf(activated));
			if (checkDataExists(dataType, dataLabel, dataValue, activated)) {
				// If the boolean result of this call is true, then we need to decide what to do next.
				if (!override) {
					// No override. Fail code 1.
					return "EXISTS";
				} else {
					// Override. Modify record.
					modifyData(dataType, dataLabel, colcat, dataValue, activated);
					return "EXISTS_OK";
				}
			}
			db.insert(data.TBLMASTER, null, cv);
		} else {
			Log.e(TAG, "addData method execution failure: Database has not been opened.");
			return "NO_DB";
		}
		return "OK";
	}

	public String[][] pullData(int mode) {
		// Retrieve Contact Manager data from Android's built-in Contact Database
		// Private variables
		int cv, ct, recCount;
		String ci_v, ci_t, sel, dV, dT, dL, swtch;
		int colcat = mode;
		String[] selArgs;
		String[][] composite;
		Uri conUri;
		/*  The following switch statements set up the variables for retrieval.
		 * integer switches are: 1=Phone, 2=Email, 3=IM
		 * */
		switch (mode) {
			case 1:
				// Phone retrieval mode.
				ci_t = CommonDataKinds.Phone.TYPE;
				ci_v = CommonDataKinds.Phone.NUMBER;
				conUri = CommonDataKinds.Phone.CONTENT_URI;
				sel = CommonDataKinds.Phone.CONTACT_ID + " = ?";
				selArgs = new String[]{String.valueOf(conId)};
				swtch = "1";
				break;
			case 2:
				// Email retrieval mode.
				ci_t = CommonDataKinds.Email.TYPE;
				ci_v = CommonDataKinds.Email.ADDRESS;
				conUri = CommonDataKinds.Email.CONTENT_URI;
				sel = CommonDataKinds.Email.CONTACT_ID + " = ?";
				selArgs = new String[]{String.valueOf(conId)};
				swtch = "1";
				break;
			case 3:
				// IM retrieval mode.
				ci_t = CommonDataKinds.Im.PROTOCOL;
				ci_v = CommonDataKinds.Im.DATA;
				conUri = Data.CONTENT_URI;
				sel = Data.CONTACT_ID + " = ? AND " + Data.MIMETYPE + " = ?";
				selArgs = new String[]{String.valueOf(conId), CommonDataKinds.Im.CONTENT_ITEM_TYPE};
				swtch = "0";
				break;
			default:
				// This should never be called.
				ci_t = null;
				ci_v = null;
				conUri = null;
				sel = null;
				selArgs = null;
				swtch = "0";
				break;
		}
		// Iterate over data.
		if (sel != null) { // sel has data. We will try to conduct a search with the value.
			ecrsr = ctxt.getContentResolver().query(conUri, null, sel, selArgs, null); // Return cursor at Android Contacts DB selection criteria
			ct = ecrsr.getColumnIndex(ci_t); // Return column index for type or protocol
			cv = ecrsr.getColumnIndex(ci_v); // Return column index for data value
			recCount = ecrsr.getCount(); // Return record count (the number of entries for the selected contact ID)
			composite = new String[recCount][6]; // Re-initialise String array for storing of values (using the correct record count).
			int ctInt;
			if (ecrsr.moveToFirst()) {
				for (int i = 0; i < recCount; i++) {
					// From Contact Database
					ctInt = ecrsr.getInt(ct); // Integer value of Data Type
					String[] dtypes = getDataType(ctInt, mode); // Method to convert Integer into a String Label from Localised data (supports International labels)
					dT = dtypes[0];
					dL = dtypes[1];
					if (dT == null) dT = "";
					if (dL == null) dL = "";
					dV = ecrsr.getString(cv); // String value of Data Value (Phone number, email address, IM username etc)
					// To String array
					composite[i][0] = String.valueOf(conId);   // Contact ID from Phone._ID
					composite[i][1] = conName; // Display Name from Phone.DISPLAY_NAME
					composite[i][2] = dT;      // Localised String literal as passed from getDataType
					composite[i][3] = dL;
					composite[i][4] = dV;      // Data Value as passed above
					composite[i][5] = swtch;   // Whether active (1) or dormant (0)
					ecrsr.moveToNext();
				}
			} else {
				composite = new String[][]{{"EMPTY", "", "", "", "0"}};
			}
		} else {
			// If sel is null, something went wrong.
			composite = new String[][]{{"EMPTY", "", "", "", "0"}};
		}
		ecrsr.close();
		ecrsr = null;
		// Return results
		return composite;
	}

	public String[] getDataType(int conType, int mode) {
		// Get String Label from integer argument
		String conLabel ,clabel;
		String[] reslt = new String[2];
		switch(mode){
			case 1:
				clabel = ecrsr.getString(ecrsr.getColumnIndex(CommonDataKinds.Phone.LABEL));
				conLabel = (String) CommonDataKinds.Phone.getTypeLabel(ctxt.getResources(), conType, "Custom"); // Get type label for specified integer type
				break;
			case 2:
				clabel = ecrsr.getString(ecrsr.getColumnIndex(CommonDataKinds.Email.LABEL));
				conLabel = (String) CommonDataKinds.Phone.getTypeLabel(ctxt.getResources(), conType, "Custom");
				break;
			default:
				clabel = ecrsr.getString(ecrsr.getColumnIndex(CommonDataKinds.Im.CUSTOM_PROTOCOL));
				conLabel = (String) CommonDataKinds.Im.getProtocolLabel(ctxt.getResources(), conType, "Custom");
		}
		Log.i(TAG, "Result of data Type fetch:  " + conLabel);
		Log.i(TAG, "Result of data Label fetch: " + clabel);
		reslt[0] = conLabel;
		reslt[1] = clabel;
		return reslt;
	}

	public String[] getContactNames(){
		ArrayList<String> aList = new ArrayList<>();
		String[] tmp = new String[2];
		if(db.isOpen()){
			String[] sqlcols = new String[]{DBMaster.COL_UID,DBMaster.COL_DISPLAYNAME};
			crsr = db.query(true,
							DBMaster.TBLMASTER,
							sqlcols,
							null,
							null,
							DBMaster.COL_UID,
							null,
							null,
							null);
			if (crsr.moveToFirst()){
				do {
					tmp[0] = crsr.getString(crsr.getColumnIndex(DBMaster.COL_UID));
					tmp[1] = crsr.getString(crsr.getColumnIndex(DBMaster.COL_DISPLAYNAME));
					String build = "["+tmp[0]+"] "+tmp[1];
					aList.add(build);
				} while (crsr.moveToNext());
			}
		}
		tmp = aList.toArray(new String[aList.size()]);
		return tmp;
	}

	public ArrayList<Contact> getContactData(String _id){
		// new ArrayList store
		ArrayList<String> aList = new ArrayList<>(); // This is being phased out.
		// The two below lines are part of an ArrayList structure to store data retrieved from the DB, and to parse it back again once the Activity closes.
		ArrayList<Contact> dbList = new ArrayList<>(); // This will replace it.
		ArrayList<Contactable> contactable = new ArrayList<>(); // This will serve as a subtable.
		/* The steps are as follows:
		 *   1- Read Distinct UID and Display Name Entries
		 *   2- Populate Contact ArrayList with UID and Display Names
		 *   3- Iterate and read for each UID, the data columns for each Contactable
		 *   4- With each UID, populate the appropriate Contactable ArrayList
		 *   5- Attach the Contactable ArrayList to the appropriate UID Contact entry in the Contact ArrayList
		 *   6- Repeat with each UID.
		 *
		 * The steps to write data back to the DB are as follows:
		 *   1- Pull each Contact from the top ArrayList
		 *   2- Pull each Contactable from the Contact's ArrayList
		 *   3- Put all of the values into a ContentValues entry.
		 *   4- Insert the ContentValues entry into the DB using an SQL statement.
		 *   5- Repeat for remaining Contactables
		 *   6- Repeat for each Contact.
		 */
		String[] tmp = new String[2];
		String sel = DBMaster.COL_UID + " LIKE ?";
		String[] selArgs = new String[]{_id};
		if(db.isOpen()){
			String[] sqlcols = new String[]{DBMaster.COL_UID, DBMaster.COL_DISPLAYNAME, DBMaster.COL_CATEGORY, DBMaster.COL_TYPE, DBMaster.COL_LABEL, DBMaster.COL_VALUE, DBMaster.COL_SWITCH};
			crsr = db.query(DBMaster.TBLMASTER,sqlcols,sel,selArgs,null,null,null);
			if (crsr.moveToFirst()){
				// The below line will replace the old Array of Strings below.
				Contact currCon = new Contact(crsr.getInt(crsr.getColumnIndexOrThrow(DBMaster.COL_UID)),crsr.getString(crsr.getColumnIndex(DBMaster.COL_DISPLAYNAME)));
				// tmp = new String[6];
				do {
					// The following Array lines are being phased out.
					/*
					 tmp[0] = crsr.getString(crsr.getColumnIndex(DBMaster.COL_UID));
					 tmp[1] = crsr.getString(crsr.getColumnIndex(DBMaster.COL_DISPLAYNAME));
					 crsr.getString(crsr.getColumnIndex(DBMaster.COL_CATEGORY));
					 tmp[2] = crsr.getString(crsr.getColumnIndex(DBMaster.COL_TYPE));
					 tmp[3] = crsr.getString(crsr.getColumnIndex(DBMaster.COL_LABEL));
					 tmp[4] = crsr.getString(crsr.getColumnIndex(DBMaster.COL_VALUE));
					 if (crsr.getString(crsr.getColumnIndex(DBMaster.COL_SWITCH)).equals("1")){
					 tmp[5] = "Active";
					 } else {
					 tmp[5] = "";
					 }
					 Log.d(TAG, "Debug: [" + tmp[0] + ":" + tmp[1] + ":" + tmp[2] + ":" + tmp[3] + ":" + tmp[4] + ":" + tmp[5]);
					 String build = "[" + tmp[0] + "] " + tmp[1] + ", " + tmp[2] + " [" + tmp[3] + "/" + tmp[4] + "] " + tmp[5];
					 aList.add(build);
					 */
					int colA = crsr.getInt(crsr.getColumnIndex(DBMaster.COL_CATEGORY));
					int colB = crsr.getInt(crsr.getColumnIndex(DBMaster.COL_TYPE));
					String colC = crsr.getString(crsr.getColumnIndex(DBMaster.COL_LABEL));
					String colD = crsr.getString(crsr.getColumnIndex(DBMaster.COL_VALUE));
					boolean colE;
					if (crsr.getString(crsr.getColumnIndex(DBMaster.COL_SWITCH)).equals("1")){
						colE = true;
					} else {
						colE = false;
					}
					// The following ArrayList for Contactable data will replace the above.
					currCon.addEntry(colA, colB, colC, colD, colE);

				} while (crsr.moveToNext());
				dbList.add(currCon);
			}
			// Restore complete.
		} else {
			// Database not opened.
			dbList.add(new Contact(0,"EMPTY"));
		}
		tmp = aList.toArray(new String[aList.size()]); // Deprecated.
		return dbList;
	}
}
