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
        System.out.println(client.authToken().username("chris.ellis").password(password).execute());
        System.out.println(client.extendAuthToken().token(client.getAuthToken().getToken()).execute());
        System.out.println(client.appAuthToken().app("Test Client").username("chris.ellis").password(password).execute());
        // test api calls
        System.out.println(client.helloWorld().execute());
        System.out.println(client.helloYou().execute());
        /*
        System.out.println(client.goodbyeCruelWorld().execute());
        System.out.println(client.lookingForSomething().execute());
        */
        // contacts
        System.out.println(client.getContacts().execute());
        System.out.println(client.getContactByName().name("chris.ellis").execute());
        System.out.println(client.getContactConfigByName().name("chris.ellis").execute());
        // config
        System.out.println(client.buildSiteConfig().execute());
        //
        inp.close();
    }
}
