package ProxyGUI;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Hashtable;

/**
 * The HTTPResponse creates the response
 *
 * @author  Trent Killinger
 * @version 1.0
 * @since   9-22-2017
 */
public class HTTPResponse {

    /*General Header*/
    private String generalHeader;
    /*Collection of headers*/
    private Hashtable<String,String> responseHeaders;
    /*Response Body*/
    private byte[] responseBodyBinary;
    /*Response Body*/
    private String responseBodyString;
    /*Binary Mode True*/
    private boolean binaryMode;

    /**
     * Class constructor
     * @param none
     * @return Nothing.
     */
    public HTTPResponse() {
        responseHeaders = new Hashtable<>();
        responseBodyBinary = new byte[0];
        responseBodyString = "";
        binaryMode = false;
    }

    /**
     * Adds Header to response
     * @param key
     * @param value
     * @return Nothing.
     */
    public void AddHeader(String key,String value) {
        responseHeaders.put(key,value);
    }

    /**
     * Sets the body
     * @param body
     * @return Nothing.
     */
    public void Body(byte[] body) {
        responseBodyBinary = body;
        responseBodyString ="";
        binaryMode = true;
    }

    /**
     * Sets the body
     * @param body
     * @return Nothing.
     */
    public void Body(String body) {
        responseBodyBinary = new byte[0];
        responseBodyString = body;
        binaryMode = false;
    }

    /**
     * Sets the generalHeader
     * @param header
     * @return Nothing.
     */
    public void GeneralHeader(String header) {
        generalHeader = header;
    }

    /**
     * Sends the response based on object data
     * @param stream
     * @return Nothing.
     */
    public boolean SendResponse(OutputStream stream) {

        PrintStream response = new PrintStream(stream);
        response.print(generalHeader + "\r\n");
        for (String key: responseHeaders.keySet()) {
            response.print(key + ": " + responseHeaders.get(key) + "\r\n");
        }
        response.print("\r\n");
        if(binaryMode) {
            response.write(responseBodyBinary,0,responseBodyBinary.length);
        } else {
            response.print(responseBodyString);
        }
        response.close();
        return true;
    }
}
