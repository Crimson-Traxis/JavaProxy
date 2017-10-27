package ProxyGUI;

import java.io.*;
import java.util.ArrayList;
import java.util.Hashtable;

/**
 * The HTTPObject stores the request
 *
 * @author  Trent Killinger
 * @version 1.0
 * @since   9-22-2017
 */
public class HTTPObject {
    /*General Header*/
    private String generalHeader;
    /*Collection of headers*/
    private Hashtable<String,String> requestHeaders;
    /*Request Body*/
    private byte[] body;
    private String headerSection;

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

        BufferedReader httpRequestStream = new BufferedReader(new InputStreamReader(stream));

        String line;
        while((line = httpRequestStream.readLine()) != null) {
            if(line.length() == 0) {
                break;
            }
            headerSection += line + "\r\n";
        }
        headerSection += "\r\n";

        BufferedReader reader = new BufferedReader( new StringReader(headerSection));

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

        try {
            ByteArrayOutputStream bodyBytes = new ByteArrayOutputStream();
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

                }
            }
            else {

                int read = httpRequestStream.read();
                while (read != -1) {
                    bodyBytes.write(read);
                    read = httpRequestStream.read();
                }
            }
            body = bodyBytes.toByteArray();
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
     * Gets Body
     * @param none
     * @return byte[]
     */
    public byte[] Body() {
        return body;
    }

    public String Method() {return generalHeader.split(" ")[0];}

    public String URI() {return  generalHeader.split(" ")[1];}

    public byte[] GetBytes() {
        byte[] combinedHeaderandBody = new byte[headerSection.getBytes().length + body.length];

        System.arraycopy(headerSection.getBytes(),0,combinedHeaderandBody,0,headerSection.getBytes().length);
        System.arraycopy(body,0,combinedHeaderandBody,headerSection.getBytes().length,body.length);

        return combinedHeaderandBody;
    }
}
