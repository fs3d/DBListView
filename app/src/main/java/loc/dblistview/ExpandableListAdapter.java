package loc.dblistview;

/**
 * Created by peteb_000 on 25/03/2015.
 * From tutorial code on androidhive. Testing for feasibility and execution success.
 */
import android.content.*;
import android.graphics.*;
import android.view.*;
import android.widget.*;
import java.util.*;

public class ExpandableListAdapter extends BaseExpandableListAdapter {

    private Context _context; // header titles
    // child data in format of header title, child title
    private ArrayList<Contact> person;
	
	
    public ExpandableListAdapter(Context context, ArrayList<Contact> listDataHeader) {
        this._context = context;
        this.person = listDataHeader;
    }

    @Override
    public Contactable getChild(int groupPosition, int childPosititon) {
        return this.person.get(groupPosition).data.get(childPosititon);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {

        final Contactable child = getChild(groupPosition, childPosition);

        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.list_contactable, null);
        }

        CheckBox txtListChild = (CheckBox) convertView
                .findViewById(R.id.lblListItem);
	TextView txtType = (TextView) convertView.findViewById(R.id.data_label);

        txtListChild.setText(child.getValue());
	txtListChild.setChecked(child.getStatus());
	txtType.setText(child.getLabel());
	
        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
	int vvv = this.person.get(groupPosition).data.size();
        return vvv;
    }

    @Override
    public Contact getGroup(int groupPosition) {
        return this.person.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return this.person.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        String headerTitle = getGroup(groupPosition).dispName;
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.list_contact, null);
        }

        TextView lblListHeader = (TextView) convertView
                .findViewById(R.id.display_name);
        lblListHeader.setTypeface(null, Typeface.BOLD);
        lblListHeader.setText(headerTitle);
	TextView lblSummary = (TextView) convertView.findViewById(R.id.con_summary);
	int childCount = getChildrenCount(groupPosition);
        lblSummary.setTypeface(Typeface.SANS_SERIF, Typeface.BOLD);
	String output="";
	if(childCount==0) output = "There are no entries.";
	if(childCount==1) output = "There is 1 entry, ";
	if(childCount>=2) output = "There are "+String.valueOf(childCount)+" entries, ";
        int activeCount=0;
	for(int i=0;i<childCount;i++){
	    if(this.person.get(groupPosition).data.get(i).getStatus())
	        activeCount++;
	}
	if(activeCount==0&&childCount>0) output = output + "none active.";
	if(activeCount==1&&childCount==1) output=output+"and it is active.";
	if(activeCount==1&&childCount>1) output = output + "1 of which is active.";
	if(activeCount>1&&activeCount<childCount) output = output + String.valueOf(activeCount)+" of which are active.";
	if(activeCount==childCount&&childCount>1) output=output+"all are active.";
	lblSummary.setText(output);
        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
