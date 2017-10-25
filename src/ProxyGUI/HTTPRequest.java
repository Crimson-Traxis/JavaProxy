package ProxyGUI;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Hashtable;

/**
 * The HTTPRequest stores the request
 *
 * @author  Trent Killinger
 * @version 1.0
 * @since   9-22-2017
 */
public class HTTPRequest {
    /*General Header*/
    private String generalHeader;
    /*Collection of headers*/
    private Hashtable<String,String> requestHeaders;
    /*Request Body*/
    private StringBuffer requestBody;

    /**
     * Class constructor
     * @param requestText request text.
     * @return Nothing.
     * @exception IOException On input error.
     * @see IOException
     */
    public HTTPRequest(String requestText) throws IOException,Exception{
        requestHeaders = new Hashtable<>();
        requestBody = new StringBuffer();

        BufferedReader reader = new BufferedReader( new StringReader(requestText));

        generalHeader = reader.readLine();

        String header = reader.readLine();

        while (header.length() > 0 ) {
            int index = header.indexOf(":");
            if (index == -1) {
                throw new Exception("Header parameter is invalid: " + header);
            }
            requestHeaders.put(header.substring(0, index), header.substring(index + 1, header.length()));
            header = reader.readLine();
        }

        String bodyLine = reader.readLine();
        while (bodyLine != null) {
            requestBody.append(bodyLine).append("\r\n");
            bodyLine = reader.readLine();
        }
    }

    /**
     * Gets path from http request
     * @param none
     * @return Nothing.
     */
    public String Path() {
        return generalHeader.split(" ")[1];
    }

    /**
     * Gets general header
     * @param none
     * @return string.
     */
    public String GeneralHeader() {
        return generalHeader;
    }

    /**
     * Gets header
     * @param header
     * @return string.
     */
    public String HeaderParameter(String header) {
        return requestHeaders.get(header);
    }

    /**
     * Gets Body
     * @param none
     * @return byte[]
     */
    public byte[] Body() {
        return requestBody.toString().getBytes();
    }
}
