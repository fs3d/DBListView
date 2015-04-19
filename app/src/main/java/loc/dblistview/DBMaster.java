package loc.dblistview;

import android.content.*;
import android.database.sqlite.*;

public class DBMaster extends SQLiteOpenHelper
{
	private static final String DBNAME = "InternalContacts.db";
	private static final int DBVersion = 1;
	private SQLiteDatabase db;
	@Override
	public void onCreate(SQLiteDatabase p1)
	{
		// TODO: Implement this method
		p1.execSQL(TBL_CREATE);
		this.db=p1;
	}

	@Override
	public void onOpen(SQLiteDatabase db)
	{
		// TODO: Implement this method
		super.onOpen(db);
		this.db=db;
	}

	@Override
	public void onUpgrade(SQLiteDatabase p1, int p2, int p3)
	{
		// TODO: Implement this method
		p1.execSQL(TBL_DELETE);
		onCreate(p1);
	}
	
	public DBMaster(Context ctxt){
		super(ctxt, DBNAME,null,DBVersion);
	}
	
	public static final String TBLMASTER = "people";
	public static final String COL_UID = "uid";
	public static final String COL_DISPLAYNAME = "display_name";
	public static final String COL_CATEGORY = "data_category"; // 1 Phone, 2 Mail, 3 IM
	public static final String COL_TYPE = "data_type"; // Data type int from ContactsContract.CommonDataKinds.???.Type
	public static final String COL_LABEL = "data_label";
	public static final String COL_VALUE = "data_value";
	public static final String COL_SWITCH = "data_switch"; // Boolean 1=On, 0=Off.
	
	private static final String TBL_DELETE = "drop table if exists "+TBLMASTER;
	private static final String TBL_CREATE = "create table if not exists "+TBLMASTER
		+ "(" + COL_UID + " INTEGER PRIMARY KEY, "
		+ COL_DISPLAYNAME + " TEXT NOT NULL, "
		+ COL_CATEGORY + " INTEGER, "
		+ COL_TYPE + " INTEGER, "
		+ COL_LABEL + " TEXT, "
		+ COL_VALUE + " TEXT NOT NULL, "
		+ COL_SWITCH + " INTEGER);";
	
	public void clearTable(){
		db.execSQL(TBL_DELETE);
		db.execSQL(TBL_CREATE);
	}
}
