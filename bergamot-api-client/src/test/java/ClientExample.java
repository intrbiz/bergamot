import java.util.Scanner;

import com.intrbiz.bergamot.BergamotClient;


public class ClientExample
{
    public static void main(String[] args) throws Exception
    {
        System.out.println("Enter your key: ");
        Scanner inp = new Scanner(System.in);
        String key = inp.nextLine();
        //
        System.setProperty("ssl.SocketFactory.provider", "");
        //
        BergamotClient client = new BergamotClient("https://bergamot.local", key);
        // test api calls
        System.out.println(client.callHelloWorld().execute());
        System.out.println(client.callHelloYou().execute());
        // auth api calls
        System.out.println(client.callGetAuthToken().execute());
        // contacts
        System.out.println(client.callGetContacts().execute());
        System.out.println(client.callGetContactByName().name("chris.ellis").execute());
        // config
        System.out.println(client.callBuildSiteConfig().execute());
        //
        inp.close();
    }
}
