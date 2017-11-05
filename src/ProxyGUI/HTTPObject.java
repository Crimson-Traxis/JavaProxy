package ProxyGUI;

import java.io.*;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;

/**
 * The HTTPObject stores the request/result
 *
 * @author  Trent Killinger
 * @version 1.0
 * @since   9-27-2017
 */
public class HTTPObject {
    /*General Header*/
    private String generalHeader;
    /*Collection of headers*/
    private Hashtable<String,String> requestHeaders;
    /*Body*/
    private byte[] body;
    /*Header Seciont*/
    private String headerSection;
    /*Stream That reads request/response*/
    private BufferedReader readStream;
    /**
     * Class constructor
     * @param requestText request text.
     * @return Nothing.
     * @exception IOException On input error.
     * @see IOException
     */
    public HTTPObject(InputStream stream) throws IOException,Exception{

        headerSection = "";
        generalHeader = "";
        requestHeaders = new Hashtable<>();
        body = new byte[0];

        BufferedReader readStream = new BufferedReader(new InputStreamReader(stream));

        String line;
        while((line = readStream.readLine()) != null) {
            if(line.length() == 0) {
                break;
            }
            headerSection += line + "\r\n";
        }
        headerSection += "\r\n";

        BufferedReader reader = new BufferedReader( new StringReader(headerSection));

        generalHeader = reader.readLine();

        String header = reader.readLine();

        if(header != null) {
            while (header.length() > 0 ) {
                int index = header.indexOf(":");
                if (index != -1) {
                    requestHeaders.put(header.substring(0, index), header.substring(index + 1, header.length()));
                    header = reader.readLine();
                } else {
                    System.out.println("Header parameter is invalid: " + header);
                }
            }
        }

        try {
            boolean found = false;
            for (String key : requestHeaders.keySet()) {
                if(key.equals("Content-Length")) {
                    found = true;
                    break;
                }
            }
            if(found) {
                body = new byte[Integer.parseInt(HeaderParameter("Content-Length").trim())];
                for(int i = 0; i < Integer.parseInt(HeaderParameter("Content-Length").trim());i++) {
                    int readInt = readStream.read();
                    if(readInt != -1) {
                        body[i] = (byte) readInt;
                    }
                }
                System.out.println("All bytes have been added to the array");
            }
        } catch (IOException ex) {
            System.out.println(ex);
        } catch (Exception ex) {
            System.out.println(ex);
        }

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
     * Adds header to request/response
     * @param header
     * @param value
     * @return none.
     */
    public void AddHeaderParameter(String header,String value) {
        requestHeaders.put(header,value);
    }
    
    /**
     * Gets Body
     * @param none
     * @return byte[]
     */
    public byte[] Body() {
        return body;
    }

    /**
     * Sets Body
     * @param body
     * @return none
     */
    public void Body(byte[] body) {
        body = this.body;
    }

    /**
     * Gets Request/Response Method
     * @param none
     * @return String
     */
    public String Method() {return generalHeader.split(" ")[0];}

    /**
     * Gets Request/Response Path
     * @param none
     * @return String
     */
    public String Path() {return generalHeader.split(" ")[1].replace("http://","");}

    /**
     * Gets Request/Response URI
     * @param none
     * @return String
     */
    public String URI() {return  generalHeader.split(" ")[1];}

    /**
     * Attempts to close the stream
     * @param none
     * @return Boolean
     */
    public Boolean CloseStream() {
        try {
            this.readStream.close();
            return true;
        } catch (Exception ex){
            return false;
        }

    }

    /**
     * Formats the headers into a string from the header dictionary
     * @param none
     * @return String
     */
    private String FormatHeaders() {
        String formatedString = generalHeader + "\r\n";
        for (Map.Entry<String,String> entry : requestHeaders.entrySet()) {
            formatedString += entry.getKey() + ": " + entry.getValue() + "\r\n";
        }
        return formatedString + "\r\n";
    }

    /**
     * Gets the byte representaion from the request/response
     * @param none
     * @return byte[]
     */
    public byte[] GetBytes() {
        String headers = FormatHeaders();
        byte[] combinedHeaderandBody = new byte[headers.getBytes().length + body.length];
        System.arraycopy(headers.getBytes(),0,combinedHeaderandBody,0,headers.getBytes().length);
        System.arraycopy(body,0,combinedHeaderandBody,headers.getBytes().length,body.length);

        return combinedHeaderandBody;
    }
}
