package edu.purdue.joshrado;

//import edu.purdue.jradocho.R;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;

/**
 * This fragment is the "page" where the user inputs information about the
 * request, he/she wishes to send.
 *
 * @author jradocho
 */
public class ClientFragment extends Fragment implements OnClickListener {

	/**
	 * Activity which have to receive callbacks.
	 */
	private SubmitCallbackListener activity;

	/**
	 * Creates a ProfileFragment
	 * 
	 * @param activity
	 *            activity to notify once the user click on the submit Button.
	 * 
	 *            ** DO NOT CREATE A CONSTRUCTOR FOR MatchFragment **
	 * 
	 * @return the fragment initialized.
	 */
	// ** DO NOT CREATE A CONSTRUCTOR FOR ProfileFragment **
	public static ClientFragment newInstance(SubmitCallbackListener activity) {
		ClientFragment f = new ClientFragment();

		f.activity = activity;
		return f;
	}

	/**
	 * Called when the fragment will be displayed.
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (container == null) {
			return null;
		}

		View view = inflater.inflate(R.layout.client_fragment_layout,
				container, false);

		/**
		 * Register this fragment to be the OnClickListener for the submit
		 * Button.
		 */
		view.findViewById(R.id.bu_submit).setOnClickListener(this);
		view9 = view;
		

		return view;
	}
	
	Spinner spinnerTo;
	Spinner spinnerFrom;
	RadioGroup priority;
	EditText nameEditText;
	
	String name;
	String locationTo;
	String locationFrom;
	int type;
	
	View view9;
	
	public String getName()
	{
		return name;
	}
	
	public String getToResponse()
	{
		return locationTo;
	}
	
	public String getFromResponse()
	{
		return locationFrom;
	}

	public int getSelectedPriority()
	{
		return type;
	}

	/**
	 * Callback function for the OnClickListener interface.
	 */
	@Override
	public void onClick(View v) 
	{
		spinnerTo = (Spinner) view9.findViewById(R.id.to);
		locationTo = spinnerTo.getSelectedItem().toString();
		
		spinnerFrom = (Spinner) view9.findViewById(R.id.from);
		locationFrom = spinnerFrom.getSelectedItem().toString();
		
		RadioGroup priority = (RadioGroup) view9.findViewById(R.id.radioGroup2);
		type = getRadioID(priority.getCheckedRadioButtonId());
		
		EditText nameEditText = (EditText) view9.findViewById(R.id.name);
		name = nameEditText.getText().toString();
		
		this.activity.onSubmit();
	}
	
	private int getRadioID(int hexaType)
	{
		if (hexaType == 0x7f0a000a)
		{
			return 0;
		}
		else if (hexaType == 0x7f0a000b)
		{
			return 1;
		}
		else if (hexaType == 0x7f0a000c)
		{
			return 2;
		}
		else
		{
			return -1;
		}
	}
}


















