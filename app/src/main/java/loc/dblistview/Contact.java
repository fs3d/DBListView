package loc.dblistview;

import java.util.*;

public class Contact
{
	String dispName;
	int uid;
	ArrayList<Contactable> data;
	
	public Contact(int uid, String dn){
		this.uid = uid;
		this.dispName = dn;
		this.data = new ArrayList<>();
	}

	public void addEntry(int colA, int colB, String colC, String colD, boolean colE)
	{
		// TODO: Implement this method
		data.add(new Contactable(colA,colB,colC,colD,colE));
	}
}
