package edu.purdue.joshrado;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.util.Date;
import java.util.TimeZone;
import android.app.Fragment;
import android.view.View.OnClickListener;
import android.os.Bundle;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * This fragment is the "page" where the application display the log from the
 * server and wait for a match.
 *
 * @author jradocho
 */
public class MatchFragment extends Fragment implements OnClickListener {

	//private static final String DEBUG_TAG = "DEBUG";

	/**
	 * Activity which have to receive call backs.
	 */
	private StartOverCallbackListener activity;

	/**
	 * AsyncTask sending the request to the server.
	 */
	private Client client;

	/**
	 * Coordinate of the server.
	 */
	private String host;
	private int port;

	/**
	 * Command the user should send.
	 */
	private String command;

	// TODO: your own class fields here
	private String response;
	
	private View viewMatch;

	// Class methods
	/**
	 * Creates a MatchFragment
	 * 
	 * @param activity
	 *            activity to notify once the user click on the start over
	 *            Button.
	 * @param host
	 *            address or IP address of the server.
	 * @param port
	 *            port number.
	 * 
	 * @param command
	 *            command you have to send to the server.
	 * 
	 * @return the fragment initialized.
	 */
	// TODO: you can add more parameters, follow the way we did it.
	// ** DO NOT CREATE A CONSTRUCTOR FOR MatchFragment **
	public static MatchFragment newInstance(StartOverCallbackListener activity,
			String host, int port, String command) {
		MatchFragment f = new MatchFragment();

		f.activity = activity;
		f.host = host;
		f.port = port;
		f.command = command;
		
		return f;
	}

	private String getTimeToday()
	{
		String[] tzs = TimeZone.getAvailableIDs();
		DateFormat.getDateTimeInstance().setTimeZone(TimeZone.getTimeZone(tzs[167]));
		String time = DateFormat.getDateTimeInstance().format(new Date());
		return time;
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

		viewMatch = inflater.inflate(R.layout.match_fragment_layout, container,
				false);

		/**
		 * Register this fragment to be the OnClickListener for the startover
		 * button.
		 */
		viewMatch.findViewById(R.id.bu_start_over).setOnClickListener(this);

		// TODO: import your Views from the layout here. See example in
		// ServerFragment.
		

		

		/**
		 * Launch the AsyncTask
		 */
		this.client = new Client();
		this.client.execute("");

		return viewMatch;
	}

	private String infoPreferenceRequest(String pref)
	{
		if (pref.equals("0"))
		{
			return ", with no prefernce of being matched with a voluteer or requester,";
		}
		else if (pref.equals("1"))
		{
			return ", with a preference of being matched with volunteers only,";
		}
		else
		{
			return ", with a preference of being matched with requesters only,";
		}
	}
	/**
	 * Callback function for the OnClickListener interface.
	 */
	@Override
	public void onClick(View v) {
		/**
		 * Close the AsyncTask if still running.
		 */
		try {
			this.client.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		/**
		 * Notify the Activity.
		 */
		this.activity.onStartOver();
	}

	class Client extends AsyncTask<String, String, String> implements Closeable {

		/**
		 * NOTE: you can access MatchFragment field from this class:
		 * 
		 * Example: The statement in doInBackground will print the message in
		 * the Eclipse LogCat view.
		 */

		/**
		 * The system calls this to perform work in a worker thread and delivers
		 * it the parameters given to AsyncTask.execute()
		 */
		
		ServerSocket serverSocket;
		Socket socket;
		PrintWriter pw;
		BufferedReader br;
		boolean onstartover = true;
		
		protected String doInBackground(String... params) {

			/**
			 * TODO: Your Client code here.
			 */
			try 
			{
				socket = new Socket(host, port);
				
				TextView connectionSuccess = (TextView) viewMatch.findViewById(R.id.textView1);
				connectionSuccess.setText("[" + getTimeToday() + "]: Connection to the server is successful.");

				pw = new PrintWriter(socket.getOutputStream(), true);
				br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				
				pw.println(command);
				
				while (true)
				{
					response = br.readLine();
					response = response.substring(10);
					break;
				}
				socket.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			
			return response;
		}
		public void close() throws IOException
		{
            // TODO: Clean up the client
			onstartover = false;
			socket.close();
			pw.close();
			br.close();
			
			closeTextViews();
			
		}
		
		private void closeTextViews()
		{			
			TextView pairFound = (TextView) viewMatch.findViewById(R.id.textView3);
			pairFound.setText("");
			
			TextView partnerName = (TextView) viewMatch.findViewById(R.id.textView7);
			partnerName.setText("");
			
			TextView fromPlace = (TextView) viewMatch.findViewById(R.id.textView8);
			fromPlace.setText("");
			
			TextView toPlace = (TextView) viewMatch.findViewById(R.id.textView9);
			toPlace.setText("");
			
			TextView requestText = (TextView) viewMatch.findViewById(R.id.textView2);
			requestText.setText("");
		}

		/**
		 * The system calls this to perform work in the UI thread and delivers
		 * the result from doInBackground()
		 */

		private void setTextViews()
		{
			String[] info;
			if (onstartover)
			{
				info = response.split(",");
			}
			else
			{
				info = new String[]{"", "", "", ""};
			}
			
			TextView pairFound = (TextView) viewMatch.findViewById(R.id.textView3);
			pairFound.setText("[" + getTimeToday() + "]: A pair has been found by the server.");
			
			TextView partnerName = (TextView) viewMatch.findViewById(R.id.textView7);
			partnerName.setText(info[0]);
			
			TextView fromPlace = (TextView) viewMatch.findViewById(R.id.textView8);
			fromPlace.setText(info[1]);
			
			TextView toPlace = (TextView) viewMatch.findViewById(R.id.textView9);
			toPlace.setText(info[2]);
		}
		
		// TODO: use the following method to update the UI.
		// ** DO NOT TRY TO CALL UI METHODS FROM doInBackground!!!!!!!!!! **

		/**
		 * Method executed just before the task.
		 */
		@Override
		protected void onPreExecute() 
		{
			String[] infoCMD = command.split(",");
			TextView requestText = (TextView) viewMatch.findViewById(R.id.textView2);
			requestText.setText("[" + getTimeToday() + "]:" + infoCMD[0] + infoPreferenceRequest(infoCMD[3]) + 
					" sent a request to move from " + infoCMD[1] + " to " + infoCMD[2] + ".");
		}

		/**
		 * Method executed once the task is completed.
		 */
		@Override
		protected void onPostExecute(String result)
		{
			setTextViews();
		}

		/**
		 * Method executed when progressUpdate is called in the doInBackground
		 * function.
		 */
		@Override
		protected void onProgressUpdate(String... result) 
		{
			
		}
	}

	
}
