package ethens.arduinopro;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.apptakk.http_request.HttpRequest;
import com.apptakk.http_request.HttpRequestTask;
import com.apptakk.http_request.HttpResponse;

public class MainActivity extends Activity implements View.OnClickListener {

  public final static String PREF_IP = "PREF_IP_ADDRESS";
  public final static String PREF_PORT = "PREF_PORT_NUMBER";
  // declare buttons and text inputs
  private Button submit;
  private EditText editTextIPAddress, editTextPortNumber, submitKey, submitValue;
  // shared preferences objects used to save the IP address and port so that the user doesn't have to
  // type them next time he/she opens the app.
  SharedPreferences.Editor editor;
  SharedPreferences sharedPreferences;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    sharedPreferences = getSharedPreferences("HTTP_HELPER_PREFS", Context.MODE_PRIVATE);
    editor = sharedPreferences.edit();

    // assign buttons
    submit = (Button) findViewById(R.id.submit);

    submitKey = findViewById(R.id.submit_key);

    submitValue = findViewById(R.id.submit_text);

    // assign text inputs
    editTextIPAddress = (EditText) findViewById(R.id.editTextIPAddress);
    editTextPortNumber = (EditText) findViewById(R.id.editTextPortNumber);

    // set button listener (this class)
    submit.setOnClickListener(this);

    // get the IP address and port number from the last time the user used the app,
    // put an empty string "" is this is the first time.
    editTextIPAddress.setText(sharedPreferences.getString(PREF_IP, ""));
    editTextPortNumber.setText(sharedPreferences.getString(PREF_PORT, ""));
  }

  @Override public void onClick(View view) {

    // get the ip address
    String ipAddress = editTextIPAddress.getText().toString().trim();
    // get the port number
    String portNumber = editTextPortNumber.getText().toString().trim();

    String key = submitKey.getText().toString().trim();

    String value = submitValue.getText().toString().trim();

    // save the IP address and port for the next time the app is used
    editor.putString(PREF_IP, ipAddress); // set the ip address value to save
    editor.putString(PREF_PORT, portNumber); // set the port number to save
    editor.commit(); // save the IP and PORT

    // execute HTTP request
    if (ipAddress.length() > 0 && portNumber.length() > 0) {
      new HttpRequestAsyncTask(view.getContext(), value, ipAddress, portNumber, key).execute();
    }
  }

  /**
   * Description: Send an HTTP Get request to a specified ip address and port.
   * Also send a parameter "parameterName" with the value of "parameterValue".
   *
   * @param parameterValue the pin number to toggle
   * @param ipAddress the ip address to send the request to
   * @param portNumber the port number of the ip address
   * @return The ip address' reply text, or an ERROR message is it fails to receive one
   */
  public String sendRequest(String parameterValue, String ipAddress, String portNumber,
      String parameterName) {
    final String[] serverResponse = { "ERROR" };
    ipAddress =
        "http://" + ipAddress + ":" + portNumber + "/?" + parameterName + "=" + parameterValue;

    new HttpRequestTask(new HttpRequest(ipAddress, HttpRequest.POST), new HttpRequest.Handler() {
      @Override public void response(HttpResponse response) {
        if (response.code == 200) {
          serverResponse[0] = response.body;
        }
      }
    }).execute();
    // return the server's reply/response text
    return serverResponse[0];
  }

  /**
   * An AsyncTask is needed to execute HTTP requests in the background so that they do not
   * block the user interface.
   */
  private class HttpRequestAsyncTask extends AsyncTask<Void, Void, Void> {

    // declare variables needed
    private String requestReply, ipAddress, portNumber;
    private Context context;
    private AlertDialog alertDialog;
    private String parameter;
    private String parameterValue;

    /**
     * Description: The asyncTask class constructor. Assigns the values used in its other methods.
     *
     * @param context the application context, needed to create the dialog
     * @param parameterValue the pin number to toggle
     * @param ipAddress the ip address to send the request to
     * @param portNumber the port number of the ip address
     */
    public HttpRequestAsyncTask(Context context, String parameterValue, String ipAddress,
        String portNumber, String parameter) {
      this.context = context;

      alertDialog = new AlertDialog.Builder(this.context).setTitle("HTTP Response From IP Address:")
          .setCancelable(true)
          .create();

      this.ipAddress = ipAddress;
      this.parameterValue = parameterValue;
      this.portNumber = portNumber;
      this.parameter = parameter;
    }

    /**
     * Name: doInBackground
     * Description: Sends the request to the ip address
     */
    @Override protected Void doInBackground(Void... voids) {
      alertDialog.setMessage("Data sent, waiting for reply from server...");
      if (!alertDialog.isShowing()) {
        alertDialog.show();
      }
      requestReply = sendRequest(parameterValue, ipAddress, portNumber, parameter);
      return null;
    }

    /**
     * Name: onPostExecute
     * Description: This function is executed after the HTTP request returns from the ip address.
     * The function sets the dialog's message with the reply text from the server and display the
     * dialog
     * if it's not displayed already (in case it was closed by accident);
     *
     * @param aVoid void parameter
     */
    @Override protected void onPostExecute(Void aVoid) {
      alertDialog.setMessage(requestReply);
      if (!alertDialog.isShowing()) {
        alertDialog.show(); // show dialog
      }
    }

    /**
     * Name: onPreExecute
     * Description: This function is executed before the HTTP request is sent to ip address.
     * The function will set the dialog's message and display the dialog.
     */
    @Override protected void onPreExecute() {
      alertDialog.setMessage("Sending data to server, please wait...");
      if (!alertDialog.isShowing()) {
        alertDialog.show();
      }
    }
  }
}
