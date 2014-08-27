import com.intrbiz.bergamot.BergamotClient;


public class ClientExample
{
    public static void main(String[] args) throws Exception
    {
        BergamotClient client = new BergamotClient("http://bergamot.local/api/", "chris.ellis", "bergamot");
        // auth api calls
        System.out.println(client.authToken().username("chris.ellis").password("bergamot").execute());
        System.out.println(client.extendAuthToken().token(client.getAuthToken().getToken()).execute());
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
    }
}
