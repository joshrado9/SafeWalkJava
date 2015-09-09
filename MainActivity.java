package edu.purdue.joshrado;

//import edu.purdue.jradocho.R;
import java.io.IOException;
import java.net.Socket;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity implements SubmitCallbackListener,
		StartOverCallbackListener {

	/**
	 * The ClientFragment used by the activity.
	 */
	private ClientFragment clientFragment;

	/**
	 * The ServerFragment used by the activity.
	 */
	private ServerFragment serverFragment;

	/**
	 * UI component of the ActionBar used for navigation.
	 */
	private Button left;
	private Button right;
	private TextView title;

	/**
	 * Called once the activity is created.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_layout);

		this.clientFragment = ClientFragment.newInstance(this);
		this.serverFragment = ServerFragment.newInstance();

		FragmentTransaction ft = getFragmentManager().beginTransaction();
		ft.add(R.id.fl_main, this.clientFragment);
		ft.commit();
	}

	/**
	 * Creates the ActionBar: - Inflates the layout - Extracts the components
	 */
	@SuppressLint("InflateParams")
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		final ViewGroup actionBarLayout = (ViewGroup) getLayoutInflater()
				.inflate(R.layout.action_bar, null);

		// Set up the ActionBar
		final ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowHomeEnabled(false);
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setDisplayShowCustomEnabled(true);
		actionBar.setCustomView(actionBarLayout);

		// Extract the UI component.
		this.title = (TextView) actionBarLayout.findViewById(R.id.tv_title);
		this.left = (Button) actionBarLayout.findViewById(R.id.bu_left);
		this.right = (Button) actionBarLayout.findViewById(R.id.bu_right);
		this.right.setVisibility(View.INVISIBLE);

		return true;
	}

	/**
	 * Callback function called when the user click on the right button of the
	 * ActionBar.
	 * 
	 * @param v
	 */
	public void onRightClick(View v) {
		FragmentTransaction ft = getFragmentManager().beginTransaction();

		this.title.setText(this.getResources().getString(R.string.client));
		this.left.setVisibility(View.VISIBLE);
		this.right.setVisibility(View.INVISIBLE);
		ft.replace(R.id.fl_main, this.clientFragment);
		ft.commit();
	}

	/**
	 * Callback function called when the user click on the left button of the
	 * ActionBar.
	 * 
	 * @param v
	 */
	public void onLeftClick(View v) {
		FragmentTransaction ft = getFragmentManager().beginTransaction();

		this.title.setText(this.getResources().getString(R.string.server));
		this.left.setVisibility(View.INVISIBLE);
		this.right.setVisibility(View.VISIBLE);
		ft.replace(R.id.fl_main, this.serverFragment);
		ft.commit();

	}

	String name;
	String locationFrom;
	String locationTo;
	int type;

	/**
	 * Callback function called when the user click on the submit button.
	 */
	@Override
	public void onSubmit() {
		name = this.clientFragment.name;
		locationTo = this.clientFragment.locationTo;
		locationFrom = this.clientFragment.locationFrom;
		type = this.clientFragment.type;

		// Server info
		String host = this.serverFragment.getHost(getResources().getString(
				R.string.default_host));
		int port = this.serverFragment.getPort(Integer.parseInt(getResources()
				.getString(R.string.default_port)));
		// TODO: sanity check the results of the previous two dialogs
		if (host != null && !host.equals("")) {
			for (int i = 0; i < host.length(); i++) {
				if (host.charAt(i) == ' ') {
					onStartOver();
				}
			}
		} else {
			onStartOver();
		}

		if (port < 1 && port > 65535) {
			onStartOver();
		}

		if (isValid(host, port)) {
			// TODO: Need to get command from client fragment
			//String command = this.getResources().getString(R.string.default_command);
			String command = name + "," + locationFrom + "," + locationTo + "," + type;

			FragmentTransaction ft = getFragmentManager().beginTransaction();

			this.title.setText(getResources().getString(R.string.match));
			this.left.setVisibility(View.INVISIBLE);
			this.right.setVisibility(View.INVISIBLE);

			// TODO: You may want additional parameters here if you tailor
			// the match fragment
			MatchFragment frag = MatchFragment.newInstance(this, host, port,
					command);

			ft.replace(R.id.fl_main, frag);
			ft.commit();
		}
	}

	public void throwAlertDialog(String message)
	{
		AlertDialog alertDialog = new AlertDialog.Builder(context).create();
		alertDialog.setTitle("Error");
		alertDialog.setMessage(message);
		alertDialog.show();
	}

	/**
	 * Callback function call from MatchFragment when the user want to create a
	 * new request.
	 */
	@Override
	public void onStartOver() {
		onRightClick(null);
	}

	final Context context = this;
	String[] fromLocations = { "LWSN", "PMU", "PUSH", "CL50", "EE" };
	String[] toLocations = { "LWSN", "PMU", "PUSH", "CL50", "EE", "*" };

	public boolean isValid(String host, int port) {
		if (name != null && !name.equals("")) {
			for (int i = 0; i < name.length(); i++) {
				if (name.substring(i, i + 1).equals(",")) {
					System.out.println("entered loop");
					throwAlertDialog("Your name cannot contain a comma.");
					return false;
				}
			}
		} else {
			throwAlertDialog("Please enter a name.");
			return false;
		}

		if (type < 0 && type > 2) {
			throwAlertDialog("Please enter a preference.");
			return false;
		}

		boolean toValid = false;
		for (int i = 0; i < toLocations.length; i++) {
			if (toLocations[i].equals(locationTo)) {
				toValid = true;
			}
		}
		if (!toValid) {
			throwAlertDialog("Your TO location doesn't exist.");
			return false;
		}

		boolean fromValid = false;
		for (int i = 0; i < fromLocations.length; i++) {
			if (fromLocations[i].equals(locationFrom)) {
				fromValid = true;
			}
		}
		if (!fromValid) {
			throwAlertDialog("Your FROM location doesn't exist.");
			return false;
		}

		if (locationFrom.equals(locationTo)) {
			throwAlertDialog("The To and From locations must be different.");
			return false;
		}
		
		if (type == 1 && locationTo.equals("*"))
		{
			throwAlertDialog("A requester cannot have the location *.");
			return false;
		}
		/*
		try
		{
			Socket testerSocket = new Socket(host, port);
		}
		catch(IOException e)
		{
			throwAlertDialog("Invalid IP Address or Port Number.");
			return false;
		}
		*/

		return true;
	}
}
