import java.util.Scanner;

import com.intrbiz.bergamot.BergamotClient;


public class ClientExample
{
    public static void main(String[] args) throws Exception
    {
        System.out.println("Enter your password: ");
        Scanner inp = new Scanner(System.in);
        String password = inp.nextLine();
        //
        System.setProperty("ssl.SocketFactory.provider", "");
        //
        BergamotClient client = new BergamotClient("https://bergamot.local/api/", "chris.ellis", password);
        // auth api calls
        System.out.println(client.callGetAuthToken().username("chris.ellis").password(password).execute());
        System.out.println(client.callExtendAuthToken().token(client.getAuthToken().getToken()).execute());
        System.out.println(client.callGetAppAuthToken().appName("Test Client").username("chris.ellis").password(password).execute());
        // test api calls
        System.out.println(client.callHelloWorld().execute());
        System.out.println(client.callHelloYou().execute());
        /*
        System.out.println(client.goodbyeCruelWorld().execute());
        System.out.println(client.lookingForSomething().execute());
        */
        // contacts
        System.out.println(client.callGetContacts().execute());
        System.out.println(client.callGetContactByName().name("chris.ellis").execute());
        // config
        System.out.println(client.callBuildSiteConfig().execute());
        //
        inp.close();
    }
}
