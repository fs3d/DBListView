package loc.dblistview;

import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.Data;
import android.content.*;

public class Contactable
{
	private String dataV, dataL, dataTLoc;
	private int dataT, dataC;
	private boolean checkV;
	
	public Contactable(int dCat, int dT, String dV, String dL,boolean cV){
		this.dataV = dV;
		this.dataL = dL;
		this.dataT = dT;
		this.dataC = dCat;
		this.checkV = cV;
	}
	
	public String getValue(){
	    return this.dataV;
	}
	
	public void setTypeLabel(String label){
		this.dataTLoc = label;
	}
	
	public String getLabel(){
	    return this.dataL;
	}
	
	public int getType(){
	    return this.dataT;
	}
	
	public boolean getStatus(){
	    return this.checkV;
	}
	
	public int getCat(){
		return this.dataC;
	}
}
