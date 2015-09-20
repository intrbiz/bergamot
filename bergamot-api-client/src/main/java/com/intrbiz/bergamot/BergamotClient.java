package com.intrbiz.bergamot;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.http.client.fluent.Response;

import com.intrbiz.bergamot.credentials.ClientCredentials;
import com.intrbiz.bergamot.model.message.AlertMO;
import com.intrbiz.bergamot.model.message.AuthTokenMO;
import com.intrbiz.bergamot.model.message.CheckMO;
import com.intrbiz.bergamot.model.message.ClusterMO;
import com.intrbiz.bergamot.model.message.CommandMO;
import com.intrbiz.bergamot.model.message.CommentMO;
import com.intrbiz.bergamot.model.message.ContactMO;
import com.intrbiz.bergamot.model.message.DowntimeMO;
import com.intrbiz.bergamot.model.message.GroupMO;
import com.intrbiz.bergamot.model.message.HostMO;
import com.intrbiz.bergamot.model.message.LocationMO;
import com.intrbiz.bergamot.model.message.ResourceMO;
import com.intrbiz.bergamot.model.message.ServiceMO;
import com.intrbiz.bergamot.model.message.TeamMO;
import com.intrbiz.bergamot.model.message.TimePeriodMO;
import com.intrbiz.bergamot.model.message.TrapMO;
import com.intrbiz.bergamot.model.message.reading.CheckReadingMO;
import com.intrbiz.bergamot.model.message.state.CheckStateMO;
import com.intrbiz.bergamot.model.message.state.CheckTransitionMO;

public class BergamotClient extends BaseBergamotClient
{

    public BergamotClient(String baseURL, ClientCredentials credentials)
    {
        super(baseURL, credentials);
    }

    public BergamotClient(String baseURL, String username, String password)
    {
        super(baseURL, username, password);
    }

    public BergamotClient(String baseURL, String token)
    {
        super(baseURL, token);
    }

    public BergamotClient(String baseURL)
    {
        super(baseURL);
    }



    public static class ChangePasswordCall extends BergamotAPICall<Boolean>
    {

        private String currentPassword;

        private String newPassword;

        public ChangePasswordCall(BaseBergamotClient client)
        {
            super(client);
        }


        public ChangePasswordCall currentPassword(String currentPassword)
        {
            this.currentPassword = currentPassword;
            return this;
        }

        public ChangePasswordCall newPassword(String newPassword)
        {
            this.newPassword = newPassword;
            return this;
        }

        public Boolean execute()
        {
            try
            {
                Response response = execute(
                    post(url("/api/change-password"))
                    .addHeader(authHeader())
                    .bodyForm(
                        param("current-password", this.currentPassword),
                        param("new-password", this.newPassword)
                    )
                );
                return transcoder().decodeFromString(response.returnContent().asString(), Boolean.class);
            }
            catch (IOException e)
            {
                throw new BergamotAPIException("Error calling Bergamot Monitoring API", e);
            }
        }

    }


    public ChangePasswordCall callChangePassword()
    {
        return new ChangePasswordCall(this);
    }



    public static class GetAppAuthTokenCall extends BergamotAPICall<AuthTokenMO>
    {

        private String appName;

        private String username;

        private String password;

        public GetAppAuthTokenCall(BaseBergamotClient client)
        {
            super(client);
        }


        public GetAppAuthTokenCall appName(String appName)
        {
            this.appName = appName;
            return this;
        }

        public GetAppAuthTokenCall username(String username)
        {
            this.username = username;
            return this;
        }

        public GetAppAuthTokenCall password(String password)
        {
            this.password = password;
            return this;
        }

        public AuthTokenMO execute()
        {
            try
            {
                Response response = execute(
                    post(url("/api/app/auth-token"))
                    .addHeader(authHeader())
                    .bodyForm(
                        param("app", this.appName),
                        param("username", this.username),
                        param("password", this.password)
                    )
                );
                return transcoder().decodeFromString(response.returnContent().asString(), AuthTokenMO.class);
            }
            catch (IOException e)
            {
                throw new BergamotAPIException("Error calling Bergamot Monitoring API", e);
            }
        }

    }


    public GetAppAuthTokenCall callGetAppAuthToken()
    {
        return new GetAppAuthTokenCall(this);
    }



    public static class ExtendAuthTokenCall extends BergamotAPICall<AuthTokenMO>
    {

        private String token;

        public ExtendAuthTokenCall(BaseBergamotClient client)
        {
            super(client);
        }


        public ExtendAuthTokenCall token(String token)
        {
            this.token = token;
            return this;
        }

        public AuthTokenMO execute()
        {
            try
            {
                Response response = execute(
                    post(url("/api/extend-auth-token"))
                    .addHeader(authHeader())
                    .bodyForm(
                        param("auth-token", this.token)
                    )
                );
                return transcoder().decodeFromString(response.returnContent().asString(), AuthTokenMO.class);
            }
            catch (IOException e)
            {
                throw new BergamotAPIException("Error calling Bergamot Monitoring API", e);
            }
        }

    }


    public ExtendAuthTokenCall callExtendAuthToken()
    {
        return new ExtendAuthTokenCall(this);
    }



    public static class GetAlertsCall extends BergamotAPICall<List<AlertMO>>
    {

        public GetAlertsCall(BaseBergamotClient client)
        {
            super(client);
        }


        public List<AlertMO> execute()
        {
            try
            {
                Response response = execute(
                    get(url("/api/alert/"))
                    .addHeader(authHeader())
                );
                return transcoder().decodeListFromString(response.returnContent().asString(), AlertMO.class);
            }
            catch (IOException e)
            {
                throw new BergamotAPIException("Error calling Bergamot Monitoring API", e);
            }
        }

    }


    public GetAlertsCall callGetAlerts()
    {
        return new GetAlertsCall(this);
    }



    public static class GetAlertCall extends BergamotAPICall<AlertMO>
    {

        private UUID id;

        public GetAlertCall(BaseBergamotClient client)
        {
            super(client);
        }


        public GetAlertCall id(UUID id)
        {
            this.id = id;
            return this;
        }

        public AlertMO execute()
        {
            try
            {
                Response response = execute(
                    get(url("/api/alert/id/" + this.id + ""))
                    .addHeader(authHeader())
                );
                return transcoder().decodeFromString(response.returnContent().asString(), AlertMO.class);
            }
            catch (IOException e)
            {
                throw new BergamotAPIException("Error calling Bergamot Monitoring API", e);
            }
        }

    }


    public GetAlertCall callGetAlert()
    {
        return new GetAlertCall(this);
    }



    public static class GetAlertsForCheckCall extends BergamotAPICall<List<AlertMO>>
    {

        private UUID id;

        public GetAlertsForCheckCall(BaseBergamotClient client)
        {
            super(client);
        }


        public GetAlertsForCheckCall id(UUID id)
        {
            this.id = id;
            return this;
        }

        public List<AlertMO> execute()
        {
            try
            {
                Response response = execute(
                    get(url("/api/alert/for-check/id/" + this.id + ""))
                    .addHeader(authHeader())
                );
                return transcoder().decodeListFromString(response.returnContent().asString(), AlertMO.class);
            }
            catch (IOException e)
            {
                throw new BergamotAPIException("Error calling Bergamot Monitoring API", e);
            }
        }

    }


    public GetAlertsForCheckCall callGetAlertsForCheck()
    {
        return new GetAlertsForCheckCall(this);
    }



    public static class GetCurrentAlertForCheckCall extends BergamotAPICall<AlertMO>
    {

        private UUID id;

        public GetCurrentAlertForCheckCall(BaseBergamotClient client)
        {
            super(client);
        }


        public GetCurrentAlertForCheckCall id(UUID id)
        {
            this.id = id;
            return this;
        }

        public AlertMO execute()
        {
            try
            {
                Response response = execute(
                    get(url("/api/alert/current/for-check/id/" + this.id + ""))
                    .addHeader(authHeader())
                );
                return transcoder().decodeFromString(response.returnContent().asString(), AlertMO.class);
            }
            catch (IOException e)
            {
                throw new BergamotAPIException("Error calling Bergamot Monitoring API", e);
            }
        }

    }


    public GetCurrentAlertForCheckCall callGetCurrentAlertForCheck()
    {
        return new GetCurrentAlertForCheckCall(this);
    }



    public static class AcknowledgeAlertCall extends BergamotAPICall<AlertMO>
    {

        private UUID id;

        private String summary;

        private String comment;

        public AcknowledgeAlertCall(BaseBergamotClient client)
        {
            super(client);
        }


        public AcknowledgeAlertCall id(UUID id)
        {
            this.id = id;
            return this;
        }

        public AcknowledgeAlertCall summary(String summary)
        {
            this.summary = summary;
            return this;
        }

        public AcknowledgeAlertCall comment(String comment)
        {
            this.comment = comment;
            return this;
        }

        public AlertMO execute()
        {
            try
            {
                Response response = execute(
                    post(url("/api/alert/id/" + this.id + "/acknowledge"))
                    .addHeader(authHeader())
                    .bodyForm(
                        param("summary", this.summary),
                        param("comment", this.comment)
                    )
                );
                return transcoder().decodeFromString(response.returnContent().asString(), AlertMO.class);
            }
            catch (IOException e)
            {
                throw new BergamotAPIException("Error calling Bergamot Monitoring API", e);
            }
        }

    }


    public AcknowledgeAlertCall callAcknowledgeAlert()
    {
        return new AcknowledgeAlertCall(this);
    }



    public static class GetHostByNameCall extends BergamotAPICall<HostMO>
    {

        private String name;

        public GetHostByNameCall(BaseBergamotClient client)
        {
            super(client);
        }


        public GetHostByNameCall name(String name)
        {
            this.name = name;
            return this;
        }

        public HostMO execute()
        {
            try
            {
                Response response = execute(
                    get(url("/api/host/name/" + this.name + ""))
                    .addHeader(authHeader())
                );
                return transcoder().decodeFromString(response.returnContent().asString(), HostMO.class);
            }
            catch (IOException e)
            {
                throw new BergamotAPIException("Error calling Bergamot Monitoring API", e);
            }
        }

    }


    public GetHostByNameCall callGetHostByName()
    {
        return new GetHostByNameCall(this);
    }



    public static class ExecuteHostCall extends BergamotAPICall<String>
    {

        private UUID id;

        public ExecuteHostCall(BaseBergamotClient client)
        {
            super(client);
        }


        public ExecuteHostCall id(UUID id)
        {
            this.id = id;
            return this;
        }

        public String execute()
        {
            try
            {
                Response response = execute(
                    get(url("/api/host/id/" + this.id + "/execute"))
                    .addHeader(authHeader())
                );
                return transcoder().decodeFromString(response.returnContent().asString(), String.class);
            }
            catch (IOException e)
            {
                throw new BergamotAPIException("Error calling Bergamot Monitoring API", e);
            }
        }

    }


    public ExecuteHostCall callExecuteHost()
    {
        return new ExecuteHostCall(this);
    }



    public static class ExecuteServicesOnHostCall extends BergamotAPICall<String>
    {

        private UUID id;

        public ExecuteServicesOnHostCall(BaseBergamotClient client)
        {
            super(client);
        }


        public ExecuteServicesOnHostCall id(UUID id)
        {
            this.id = id;
            return this;
        }

        public String execute()
        {
            try
            {
                Response response = execute(
                    get(url("/api/host/id/" + this.id + "/execute-services"))
                    .addHeader(authHeader())
                );
                return transcoder().decodeFromString(response.returnContent().asString(), String.class);
            }
            catch (IOException e)
            {
                throw new BergamotAPIException("Error calling Bergamot Monitoring API", e);
            }
        }

    }


    public ExecuteServicesOnHostCall callExecuteServicesOnHost()
    {
        return new ExecuteServicesOnHostCall(this);
    }



    public static class SuppressServicesOnHostCall extends BergamotAPICall<String>
    {

        private UUID id;

        public SuppressServicesOnHostCall(BaseBergamotClient client)
        {
            super(client);
        }


        public SuppressServicesOnHostCall id(UUID id)
        {
            this.id = id;
            return this;
        }

        public String execute()
        {
            try
            {
                Response response = execute(
                    get(url("/api/host/id/" + this.id + "/suppress-services"))
                    .addHeader(authHeader())
                );
                return transcoder().decodeFromString(response.returnContent().asString(), String.class);
            }
            catch (IOException e)
            {
                throw new BergamotAPIException("Error calling Bergamot Monitoring API", e);
            }
        }

    }


    public SuppressServicesOnHostCall callSuppressServicesOnHost()
    {
        return new SuppressServicesOnHostCall(this);
    }



    public static class UnsuppressServicesOnHostCall extends BergamotAPICall<String>
    {

        private UUID id;

        public UnsuppressServicesOnHostCall(BaseBergamotClient client)
        {
            super(client);
        }


        public UnsuppressServicesOnHostCall id(UUID id)
        {
            this.id = id;
            return this;
        }

        public String execute()
        {
            try
            {
                Response response = execute(
                    get(url("/api/host/id/" + this.id + "/unsuppress-services"))
                    .addHeader(authHeader())
                );
                return transcoder().decodeFromString(response.returnContent().asString(), String.class);
            }
            catch (IOException e)
            {
                throw new BergamotAPIException("Error calling Bergamot Monitoring API", e);
            }
        }

    }


    public UnsuppressServicesOnHostCall callUnsuppressServicesOnHost()
    {
        return new UnsuppressServicesOnHostCall(this);
    }



    public static class GetHostsCall extends BergamotAPICall<List<HostMO>>
    {

        public GetHostsCall(BaseBergamotClient client)
        {
            super(client);
        }


        public List<HostMO> execute()
        {
            try
            {
                Response response = execute(
                    get(url("/api/host/"))
                    .addHeader(authHeader())
                );
                return transcoder().decodeListFromString(response.returnContent().asString(), HostMO.class);
            }
            catch (IOException e)
            {
                throw new BergamotAPIException("Error calling Bergamot Monitoring API", e);
            }
        }

    }


    public GetHostsCall callGetHosts()
    {
        return new GetHostsCall(this);
    }



    public static class SuppressCall extends BergamotAPICall<String>
    {

        private UUID id;

        public SuppressCall(BaseBergamotClient client)
        {
            super(client);
        }


        public SuppressCall id(UUID id)
        {
            this.id = id;
            return this;
        }

        public String execute()
        {
            try
            {
                Response response = execute(
                    get(url("/api/host/id/" + this.id + "/suppress"))
                    .addHeader(authHeader())
                );
                return transcoder().decodeFromString(response.returnContent().asString(), String.class);
            }
            catch (IOException e)
            {
                throw new BergamotAPIException("Error calling Bergamot Monitoring API", e);
            }
        }

    }


    public SuppressCall callSuppress()
    {
        return new SuppressCall(this);
    }



    public static class UnsuppressCall extends BergamotAPICall<String>
    {

        private UUID id;

        public UnsuppressCall(BaseBergamotClient client)
        {
            super(client);
        }


        public UnsuppressCall id(UUID id)
        {
            this.id = id;
            return this;
        }

        public String execute()
        {
            try
            {
                Response response = execute(
                    get(url("/api/host/id/" + this.id + "/unsuppress"))
                    .addHeader(authHeader())
                );
                return transcoder().decodeFromString(response.returnContent().asString(), String.class);
            }
            catch (IOException e)
            {
                throw new BergamotAPIException("Error calling Bergamot Monitoring API", e);
            }
        }

    }


    public UnsuppressCall callUnsuppress()
    {
        return new UnsuppressCall(this);
    }



    public static class GetHostStateByNameCall extends BergamotAPICall<CheckStateMO>
    {

        private String name;

        public GetHostStateByNameCall(BaseBergamotClient client)
        {
            super(client);
        }


        public GetHostStateByNameCall name(String name)
        {
            this.name = name;
            return this;
        }

        public CheckStateMO execute()
        {
            try
            {
                Response response = execute(
                    get(url("/api/host/name/" + this.name + "/state"))
                    .addHeader(authHeader())
                );
                return transcoder().decodeFromString(response.returnContent().asString(), CheckStateMO.class);
            }
            catch (IOException e)
            {
                throw new BergamotAPIException("Error calling Bergamot Monitoring API", e);
            }
        }

    }


    public GetHostStateByNameCall callGetHostStateByName()
    {
        return new GetHostStateByNameCall(this);
    }



    public static class GetHostStateCall extends BergamotAPICall<CheckStateMO>
    {

        private UUID id;

        public GetHostStateCall(BaseBergamotClient client)
        {
            super(client);
        }


        public GetHostStateCall id(UUID id)
        {
            this.id = id;
            return this;
        }

        public CheckStateMO execute()
        {
            try
            {
                Response response = execute(
                    get(url("/api/host/id/" + this.id + "/state"))
                    .addHeader(authHeader())
                );
                return transcoder().decodeFromString(response.returnContent().asString(), CheckStateMO.class);
            }
            catch (IOException e)
            {
                throw new BergamotAPIException("Error calling Bergamot Monitoring API", e);
            }
        }

    }


    public GetHostStateCall callGetHostState()
    {
        return new GetHostStateCall(this);
    }



    public static class GetHostServicesByNameCall extends BergamotAPICall<List<ServiceMO>>
    {

        private String name;

        public GetHostServicesByNameCall(BaseBergamotClient client)
        {
            super(client);
        }


        public GetHostServicesByNameCall name(String name)
        {
            this.name = name;
            return this;
        }

        public List<ServiceMO> execute()
        {
            try
            {
                Response response = execute(
                    get(url("/api/host/name/" + this.name + "/services"))
                    .addHeader(authHeader())
                );
                return transcoder().decodeListFromString(response.returnContent().asString(), ServiceMO.class);
            }
            catch (IOException e)
            {
                throw new BergamotAPIException("Error calling Bergamot Monitoring API", e);
            }
        }

    }


    public GetHostServicesByNameCall callGetHostServicesByName()
    {
        return new GetHostServicesByNameCall(this);
    }



    public static class GetHostServicesCall extends BergamotAPICall<List<ServiceMO>>
    {

        private UUID id;

        public GetHostServicesCall(BaseBergamotClient client)
        {
            super(client);
        }


        public GetHostServicesCall id(UUID id)
        {
            this.id = id;
            return this;
        }

        public List<ServiceMO> execute()
        {
            try
            {
                Response response = execute(
                    get(url("/api/host/id/" + this.id + "/services"))
                    .addHeader(authHeader())
                );
                return transcoder().decodeListFromString(response.returnContent().asString(), ServiceMO.class);
            }
            catch (IOException e)
            {
                throw new BergamotAPIException("Error calling Bergamot Monitoring API", e);
            }
        }

    }


    public GetHostServicesCall callGetHostServices()
    {
        return new GetHostServicesCall(this);
    }



    public static class GetHostTrapsByNameCall extends BergamotAPICall<List<TrapMO>>
    {

        private String name;

        public GetHostTrapsByNameCall(BaseBergamotClient client)
        {
            super(client);
        }


        public GetHostTrapsByNameCall name(String name)
        {
            this.name = name;
            return this;
        }

        public List<TrapMO> execute()
        {
            try
            {
                Response response = execute(
                    get(url("/api/host/name/" + this.name + "/traps"))
                    .addHeader(authHeader())
                );
                return transcoder().decodeListFromString(response.returnContent().asString(), TrapMO.class);
            }
            catch (IOException e)
            {
                throw new BergamotAPIException("Error calling Bergamot Monitoring API", e);
            }
        }

    }


    public GetHostTrapsByNameCall callGetHostTrapsByName()
    {
        return new GetHostTrapsByNameCall(this);
    }



    public static class GetHostTrapsCall extends BergamotAPICall<List<TrapMO>>
    {

        private UUID id;

        public GetHostTrapsCall(BaseBergamotClient client)
        {
            super(client);
        }


        public GetHostTrapsCall id(UUID id)
        {
            this.id = id;
            return this;
        }

        public List<TrapMO> execute()
        {
            try
            {
                Response response = execute(
                    get(url("/api/host/id/" + this.id + "/traps"))
                    .addHeader(authHeader())
                );
                return transcoder().decodeListFromString(response.returnContent().asString(), TrapMO.class);
            }
            catch (IOException e)
            {
                throw new BergamotAPIException("Error calling Bergamot Monitoring API", e);
            }
        }

    }


    public GetHostTrapsCall callGetHostTraps()
    {
        return new GetHostTrapsCall(this);
    }



    public static class SuppressTrapsOnHostCall extends BergamotAPICall<String>
    {

        private UUID id;

        public SuppressTrapsOnHostCall(BaseBergamotClient client)
        {
            super(client);
        }


        public SuppressTrapsOnHostCall id(UUID id)
        {
            this.id = id;
            return this;
        }

        public String execute()
        {
            try
            {
                Response response = execute(
                    get(url("/api/host/id/" + this.id + "/suppress-traps"))
                    .addHeader(authHeader())
                );
                return transcoder().decodeFromString(response.returnContent().asString(), String.class);
            }
            catch (IOException e)
            {
                throw new BergamotAPIException("Error calling Bergamot Monitoring API", e);
            }
        }

    }


    public SuppressTrapsOnHostCall callSuppressTrapsOnHost()
    {
        return new SuppressTrapsOnHostCall(this);
    }



    public static class UnsuppressTrapsOnHostCall extends BergamotAPICall<String>
    {

        private UUID id;

        public UnsuppressTrapsOnHostCall(BaseBergamotClient client)
        {
            super(client);
        }


        public UnsuppressTrapsOnHostCall id(UUID id)
        {
            this.id = id;
            return this;
        }

        public String execute()
        {
            try
            {
                Response response = execute(
                    get(url("/api/host/id/" + this.id + "/unsuppress-traps"))
                    .addHeader(authHeader())
                );
                return transcoder().decodeFromString(response.returnContent().asString(), String.class);
            }
            catch (IOException e)
            {
                throw new BergamotAPIException("Error calling Bergamot Monitoring API", e);
            }
        }

    }


    public UnsuppressTrapsOnHostCall callUnsuppressTrapsOnHost()
    {
        return new UnsuppressTrapsOnHostCall(this);
    }



    public static class SuppressAllOnHostCall extends BergamotAPICall<String>
    {

        private UUID id;

        public SuppressAllOnHostCall(BaseBergamotClient client)
        {
            super(client);
        }


        public SuppressAllOnHostCall id(UUID id)
        {
            this.id = id;
            return this;
        }

        public String execute()
        {
            try
            {
                Response response = execute(
                    get(url("/api/host/id/" + this.id + "/suppress-all"))
                    .addHeader(authHeader())
                );
                return transcoder().decodeFromString(response.returnContent().asString(), String.class);
            }
            catch (IOException e)
            {
                throw new BergamotAPIException("Error calling Bergamot Monitoring API", e);
            }
        }

    }


    public SuppressAllOnHostCall callSuppressAllOnHost()
    {
        return new SuppressAllOnHostCall(this);
    }



    public static class UnsuppressAllOnHostCall extends BergamotAPICall<String>
    {

        private UUID id;

        public UnsuppressAllOnHostCall(BaseBergamotClient client)
        {
            super(client);
        }


        public UnsuppressAllOnHostCall id(UUID id)
        {
            this.id = id;
            return this;
        }

        public String execute()
        {
            try
            {
                Response response = execute(
                    get(url("/api/host/id/" + this.id + "/unsuppress-all"))
                    .addHeader(authHeader())
                );
                return transcoder().decodeFromString(response.returnContent().asString(), String.class);
            }
            catch (IOException e)
            {
                throw new BergamotAPIException("Error calling Bergamot Monitoring API", e);
            }
        }

    }


    public UnsuppressAllOnHostCall callUnsuppressAllOnHost()
    {
        return new UnsuppressAllOnHostCall(this);
    }



    public static class GetHostCall extends BergamotAPICall<HostMO>
    {

        private UUID id;

        public GetHostCall(BaseBergamotClient client)
        {
            super(client);
        }


        public GetHostCall id(UUID id)
        {
            this.id = id;
            return this;
        }

        public HostMO execute()
        {
            try
            {
                Response response = execute(
                    get(url("/api/host/id/" + this.id + ""))
                    .addHeader(authHeader())
                );
                return transcoder().decodeFromString(response.returnContent().asString(), HostMO.class);
            }
            catch (IOException e)
            {
                throw new BergamotAPIException("Error calling Bergamot Monitoring API", e);
            }
        }

    }


    public GetHostCall callGetHost()
    {
        return new GetHostCall(this);
    }



    public static class GetRootLocationsCall extends BergamotAPICall<List<LocationMO>>
    {

        public GetRootLocationsCall(BaseBergamotClient client)
        {
            super(client);
        }


        public List<LocationMO> execute()
        {
            try
            {
                Response response = execute(
                    get(url("/api/location/roots"))
                    .addHeader(authHeader())
                );
                return transcoder().decodeListFromString(response.returnContent().asString(), LocationMO.class);
            }
            catch (IOException e)
            {
                throw new BergamotAPIException("Error calling Bergamot Monitoring API", e);
            }
        }

    }


    public GetRootLocationsCall callGetRootLocations()
    {
        return new GetRootLocationsCall(this);
    }



    public static class GetLocationByNameCall extends BergamotAPICall<LocationMO>
    {

        private String name;

        public GetLocationByNameCall(BaseBergamotClient client)
        {
            super(client);
        }


        public GetLocationByNameCall name(String name)
        {
            this.name = name;
            return this;
        }

        public LocationMO execute()
        {
            try
            {
                Response response = execute(
                    get(url("/api/location/name/" + this.name + ""))
                    .addHeader(authHeader())
                );
                return transcoder().decodeFromString(response.returnContent().asString(), LocationMO.class);
            }
            catch (IOException e)
            {
                throw new BergamotAPIException("Error calling Bergamot Monitoring API", e);
            }
        }

    }


    public GetLocationByNameCall callGetLocationByName()
    {
        return new GetLocationByNameCall(this);
    }



    public static class ExecuteHostsInLocationCall extends BergamotAPICall<String>
    {

        private UUID id;

        public ExecuteHostsInLocationCall(BaseBergamotClient client)
        {
            super(client);
        }


        public ExecuteHostsInLocationCall id(UUID id)
        {
            this.id = id;
            return this;
        }

        public String execute()
        {
            try
            {
                Response response = execute(
                    get(url("/api/location/id/" + this.id + "/execute-all-hosts"))
                    .addHeader(authHeader())
                );
                return transcoder().decodeFromString(response.returnContent().asString(), String.class);
            }
            catch (IOException e)
            {
                throw new BergamotAPIException("Error calling Bergamot Monitoring API", e);
            }
        }

    }


    public ExecuteHostsInLocationCall callExecuteHostsInLocation()
    {
        return new ExecuteHostsInLocationCall(this);
    }



    public static class GetLocationsCall extends BergamotAPICall<List<LocationMO>>
    {

        public GetLocationsCall(BaseBergamotClient client)
        {
            super(client);
        }


        public List<LocationMO> execute()
        {
            try
            {
                Response response = execute(
                    get(url("/api/location/"))
                    .addHeader(authHeader())
                );
                return transcoder().decodeListFromString(response.returnContent().asString(), LocationMO.class);
            }
            catch (IOException e)
            {
                throw new BergamotAPIException("Error calling Bergamot Monitoring API", e);
            }
        }

    }


    public GetLocationsCall callGetLocations()
    {
        return new GetLocationsCall(this);
    }



    public static class GetLocationChildrenByNameCall extends BergamotAPICall<List<LocationMO>>
    {

        private String name;

        public GetLocationChildrenByNameCall(BaseBergamotClient client)
        {
            super(client);
        }


        public GetLocationChildrenByNameCall name(String name)
        {
            this.name = name;
            return this;
        }

        public List<LocationMO> execute()
        {
            try
            {
                Response response = execute(
                    get(url("/api/location/name/" + this.name + "/children"))
                    .addHeader(authHeader())
                );
                return transcoder().decodeListFromString(response.returnContent().asString(), LocationMO.class);
            }
            catch (IOException e)
            {
                throw new BergamotAPIException("Error calling Bergamot Monitoring API", e);
            }
        }

    }


    public GetLocationChildrenByNameCall callGetLocationChildrenByName()
    {
        return new GetLocationChildrenByNameCall(this);
    }



    public static class GetLocationHostsByNameCall extends BergamotAPICall<List<HostMO>>
    {

        private String name;

        public GetLocationHostsByNameCall(BaseBergamotClient client)
        {
            super(client);
        }


        public GetLocationHostsByNameCall name(String name)
        {
            this.name = name;
            return this;
        }

        public List<HostMO> execute()
        {
            try
            {
                Response response = execute(
                    get(url("/api/location/name/" + this.name + "/hosts"))
                    .addHeader(authHeader())
                );
                return transcoder().decodeListFromString(response.returnContent().asString(), HostMO.class);
            }
            catch (IOException e)
            {
                throw new BergamotAPIException("Error calling Bergamot Monitoring API", e);
            }
        }

    }


    public GetLocationHostsByNameCall callGetLocationHostsByName()
    {
        return new GetLocationHostsByNameCall(this);
    }



    public static class GetLocationChildrenCall extends BergamotAPICall<List<LocationMO>>
    {

        private UUID id;

        public GetLocationChildrenCall(BaseBergamotClient client)
        {
            super(client);
        }


        public GetLocationChildrenCall id(UUID id)
        {
            this.id = id;
            return this;
        }

        public List<LocationMO> execute()
        {
            try
            {
                Response response = execute(
                    get(url("/api/location/id/" + this.id + "/children"))
                    .addHeader(authHeader())
                );
                return transcoder().decodeListFromString(response.returnContent().asString(), LocationMO.class);
            }
            catch (IOException e)
            {
                throw new BergamotAPIException("Error calling Bergamot Monitoring API", e);
            }
        }

    }


    public GetLocationChildrenCall callGetLocationChildren()
    {
        return new GetLocationChildrenCall(this);
    }



    public static class GetLocationHostsCall extends BergamotAPICall<List<HostMO>>
    {

        private UUID id;

        public GetLocationHostsCall(BaseBergamotClient client)
        {
            super(client);
        }


        public GetLocationHostsCall id(UUID id)
        {
            this.id = id;
            return this;
        }

        public List<HostMO> execute()
        {
            try
            {
                Response response = execute(
                    get(url("/api/location/id/" + this.id + "/hosts"))
                    .addHeader(authHeader())
                );
                return transcoder().decodeListFromString(response.returnContent().asString(), HostMO.class);
            }
            catch (IOException e)
            {
                throw new BergamotAPIException("Error calling Bergamot Monitoring API", e);
            }
        }

    }


    public GetLocationHostsCall callGetLocationHosts()
    {
        return new GetLocationHostsCall(this);
    }



    public static class GetLocationCall extends BergamotAPICall<LocationMO>
    {

        private UUID id;

        public GetLocationCall(BaseBergamotClient client)
        {
            super(client);
        }


        public GetLocationCall id(UUID id)
        {
            this.id = id;
            return this;
        }

        public LocationMO execute()
        {
            try
            {
                Response response = execute(
                    get(url("/api/location/id/" + this.id + ""))
                    .addHeader(authHeader())
                );
                return transcoder().decodeFromString(response.returnContent().asString(), LocationMO.class);
            }
            catch (IOException e)
            {
                throw new BergamotAPIException("Error calling Bergamot Monitoring API", e);
            }
        }

    }


    public GetLocationCall callGetLocation()
    {
        return new GetLocationCall(this);
    }



    public static class ExecuteChecksInGroupCall extends BergamotAPICall<String>
    {

        private UUID id;

        public ExecuteChecksInGroupCall(BaseBergamotClient client)
        {
            super(client);
        }


        public ExecuteChecksInGroupCall id(UUID id)
        {
            this.id = id;
            return this;
        }

        public String execute()
        {
            try
            {
                Response response = execute(
                    get(url("/api/group/id/" + this.id + "/execute-all-checks"))
                    .addHeader(authHeader())
                );
                return transcoder().decodeFromString(response.returnContent().asString(), String.class);
            }
            catch (IOException e)
            {
                throw new BergamotAPIException("Error calling Bergamot Monitoring API", e);
            }
        }

    }


    public ExecuteChecksInGroupCall callExecuteChecksInGroup()
    {
        return new ExecuteChecksInGroupCall(this);
    }



    public static class GetGroupsCall extends BergamotAPICall<List<GroupMO>>
    {

        public GetGroupsCall(BaseBergamotClient client)
        {
            super(client);
        }


        public List<GroupMO> execute()
        {
            try
            {
                Response response = execute(
                    get(url("/api/group/"))
                    .addHeader(authHeader())
                );
                return transcoder().decodeListFromString(response.returnContent().asString(), GroupMO.class);
            }
            catch (IOException e)
            {
                throw new BergamotAPIException("Error calling Bergamot Monitoring API", e);
            }
        }

    }


    public GetGroupsCall callGetGroups()
    {
        return new GetGroupsCall(this);
    }



    public static class GetRootGroupsCall extends BergamotAPICall<List<GroupMO>>
    {

        public GetRootGroupsCall(BaseBergamotClient client)
        {
            super(client);
        }


        public List<GroupMO> execute()
        {
            try
            {
                Response response = execute(
                    get(url("/api/group/roots"))
                    .addHeader(authHeader())
                );
                return transcoder().decodeListFromString(response.returnContent().asString(), GroupMO.class);
            }
            catch (IOException e)
            {
                throw new BergamotAPIException("Error calling Bergamot Monitoring API", e);
            }
        }

    }


    public GetRootGroupsCall callGetRootGroups()
    {
        return new GetRootGroupsCall(this);
    }



    public static class GetGroupByNameCall extends BergamotAPICall<GroupMO>
    {

        private String name;

        public GetGroupByNameCall(BaseBergamotClient client)
        {
            super(client);
        }


        public GetGroupByNameCall name(String name)
        {
            this.name = name;
            return this;
        }

        public GroupMO execute()
        {
            try
            {
                Response response = execute(
                    get(url("/api/group/name/" + this.name + ""))
                    .addHeader(authHeader())
                );
                return transcoder().decodeFromString(response.returnContent().asString(), GroupMO.class);
            }
            catch (IOException e)
            {
                throw new BergamotAPIException("Error calling Bergamot Monitoring API", e);
            }
        }

    }


    public GetGroupByNameCall callGetGroupByName()
    {
        return new GetGroupByNameCall(this);
    }



    public static class GetGroupCall extends BergamotAPICall<GroupMO>
    {

        private UUID id;

        public GetGroupCall(BaseBergamotClient client)
        {
            super(client);
        }


        public GetGroupCall id(UUID id)
        {
            this.id = id;
            return this;
        }

        public GroupMO execute()
        {
            try
            {
                Response response = execute(
                    get(url("/api/group/id/" + this.id + ""))
                    .addHeader(authHeader())
                );
                return transcoder().decodeFromString(response.returnContent().asString(), GroupMO.class);
            }
            catch (IOException e)
            {
                throw new BergamotAPIException("Error calling Bergamot Monitoring API", e);
            }
        }

    }


    public GetGroupCall callGetGroup()
    {
        return new GetGroupCall(this);
    }



    public static class GetGroupChildrenByNameCall extends BergamotAPICall<List<GroupMO>>
    {

        private String name;

        public GetGroupChildrenByNameCall(BaseBergamotClient client)
        {
            super(client);
        }


        public GetGroupChildrenByNameCall name(String name)
        {
            this.name = name;
            return this;
        }

        public List<GroupMO> execute()
        {
            try
            {
                Response response = execute(
                    get(url("/api/group/name/" + this.name + "/children"))
                    .addHeader(authHeader())
                );
                return transcoder().decodeListFromString(response.returnContent().asString(), GroupMO.class);
            }
            catch (IOException e)
            {
                throw new BergamotAPIException("Error calling Bergamot Monitoring API", e);
            }
        }

    }


    public GetGroupChildrenByNameCall callGetGroupChildrenByName()
    {
        return new GetGroupChildrenByNameCall(this);
    }



    public static class GetGroupChecksByNameCall extends BergamotAPICall<List<CheckMO>>
    {

        private String name;

        public GetGroupChecksByNameCall(BaseBergamotClient client)
        {
            super(client);
        }


        public GetGroupChecksByNameCall name(String name)
        {
            this.name = name;
            return this;
        }

        public List<CheckMO> execute()
        {
            try
            {
                Response response = execute(
                    get(url("/api/group/name/" + this.name + "/checks"))
                    .addHeader(authHeader())
                );
                return transcoder().decodeListFromString(response.returnContent().asString(), CheckMO.class);
            }
            catch (IOException e)
            {
                throw new BergamotAPIException("Error calling Bergamot Monitoring API", e);
            }
        }

    }


    public GetGroupChecksByNameCall callGetGroupChecksByName()
    {
        return new GetGroupChecksByNameCall(this);
    }



    public static class GetGroupChildrenCall extends BergamotAPICall<List<GroupMO>>
    {

        private UUID id;

        public GetGroupChildrenCall(BaseBergamotClient client)
        {
            super(client);
        }


        public GetGroupChildrenCall id(UUID id)
        {
            this.id = id;
            return this;
        }

        public List<GroupMO> execute()
        {
            try
            {
                Response response = execute(
                    get(url("/api/group/id/" + this.id + "/children"))
                    .addHeader(authHeader())
                );
                return transcoder().decodeListFromString(response.returnContent().asString(), GroupMO.class);
            }
            catch (IOException e)
            {
                throw new BergamotAPIException("Error calling Bergamot Monitoring API", e);
            }
        }

    }


    public GetGroupChildrenCall callGetGroupChildren()
    {
        return new GetGroupChildrenCall(this);
    }



    public static class GetGroupChecksCall extends BergamotAPICall<List<CheckMO>>
    {

        private UUID id;

        public GetGroupChecksCall(BaseBergamotClient client)
        {
            super(client);
        }


        public GetGroupChecksCall id(UUID id)
        {
            this.id = id;
            return this;
        }

        public List<CheckMO> execute()
        {
            try
            {
                Response response = execute(
                    get(url("/api/group/id/" + this.id + "/checks"))
                    .addHeader(authHeader())
                );
                return transcoder().decodeListFromString(response.returnContent().asString(), CheckMO.class);
            }
            catch (IOException e)
            {
                throw new BergamotAPIException("Error calling Bergamot Monitoring API", e);
            }
        }

    }


    public GetGroupChecksCall callGetGroupChecks()
    {
        return new GetGroupChecksCall(this);
    }



    public static class GetClusterByNameCall extends BergamotAPICall<ClusterMO>
    {

        private String name;

        public GetClusterByNameCall(BaseBergamotClient client)
        {
            super(client);
        }


        public GetClusterByNameCall name(String name)
        {
            this.name = name;
            return this;
        }

        public ClusterMO execute()
        {
            try
            {
                Response response = execute(
                    get(url("/api/cluster/name/" + this.name + ""))
                    .addHeader(authHeader())
                );
                return transcoder().decodeFromString(response.returnContent().asString(), ClusterMO.class);
            }
            catch (IOException e)
            {
                throw new BergamotAPIException("Error calling Bergamot Monitoring API", e);
            }
        }

    }


    public GetClusterByNameCall callGetClusterByName()
    {
        return new GetClusterByNameCall(this);
    }



    public static class GetClusterCall extends BergamotAPICall<ClusterMO>
    {

        private UUID id;

        public GetClusterCall(BaseBergamotClient client)
        {
            super(client);
        }


        public GetClusterCall id(UUID id)
        {
            this.id = id;
            return this;
        }

        public ClusterMO execute()
        {
            try
            {
                Response response = execute(
                    get(url("/api/cluster/id/" + this.id + ""))
                    .addHeader(authHeader())
                );
                return transcoder().decodeFromString(response.returnContent().asString(), ClusterMO.class);
            }
            catch (IOException e)
            {
                throw new BergamotAPIException("Error calling Bergamot Monitoring API", e);
            }
        }

    }


    public GetClusterCall callGetCluster()
    {
        return new GetClusterCall(this);
    }



    public static class SuppressClusterCall extends BergamotAPICall<String>
    {

        private UUID id;

        public SuppressClusterCall(BaseBergamotClient client)
        {
            super(client);
        }


        public SuppressClusterCall id(UUID id)
        {
            this.id = id;
            return this;
        }

        public String execute()
        {
            try
            {
                Response response = execute(
                    get(url("/api/cluster/id/" + this.id + "/suppress"))
                    .addHeader(authHeader())
                );
                return transcoder().decodeFromString(response.returnContent().asString(), String.class);
            }
            catch (IOException e)
            {
                throw new BergamotAPIException("Error calling Bergamot Monitoring API", e);
            }
        }

    }


    public SuppressClusterCall callSuppressCluster()
    {
        return new SuppressClusterCall(this);
    }



    public static class UnsuppressClusterCall extends BergamotAPICall<String>
    {

        private UUID id;

        public UnsuppressClusterCall(BaseBergamotClient client)
        {
            super(client);
        }


        public UnsuppressClusterCall id(UUID id)
        {
            this.id = id;
            return this;
        }

        public String execute()
        {
            try
            {
                Response response = execute(
                    get(url("/api/cluster/id/" + this.id + "/unsuppress"))
                    .addHeader(authHeader())
                );
                return transcoder().decodeFromString(response.returnContent().asString(), String.class);
            }
            catch (IOException e)
            {
                throw new BergamotAPIException("Error calling Bergamot Monitoring API", e);
            }
        }

    }


    public UnsuppressClusterCall callUnsuppressCluster()
    {
        return new UnsuppressClusterCall(this);
    }



    public static class GetClustersCall extends BergamotAPICall<List<ClusterMO>>
    {

        public GetClustersCall(BaseBergamotClient client)
        {
            super(client);
        }


        public List<ClusterMO> execute()
        {
            try
            {
                Response response = execute(
                    get(url("/api/cluster/"))
                    .addHeader(authHeader())
                );
                return transcoder().decodeListFromString(response.returnContent().asString(), ClusterMO.class);
            }
            catch (IOException e)
            {
                throw new BergamotAPIException("Error calling Bergamot Monitoring API", e);
            }
        }

    }


    public GetClustersCall callGetClusters()
    {
        return new GetClustersCall(this);
    }



    public static class GetClusterStateByNameCall extends BergamotAPICall<CheckStateMO>
    {

        private String name;

        public GetClusterStateByNameCall(BaseBergamotClient client)
        {
            super(client);
        }


        public GetClusterStateByNameCall name(String name)
        {
            this.name = name;
            return this;
        }

        public CheckStateMO execute()
        {
            try
            {
                Response response = execute(
                    get(url("/api/cluster/name/" + this.name + "/state"))
                    .addHeader(authHeader())
                );
                return transcoder().decodeFromString(response.returnContent().asString(), CheckStateMO.class);
            }
            catch (IOException e)
            {
                throw new BergamotAPIException("Error calling Bergamot Monitoring API", e);
            }
        }

    }


    public GetClusterStateByNameCall callGetClusterStateByName()
    {
        return new GetClusterStateByNameCall(this);
    }



    public static class GetClusterStateCall extends BergamotAPICall<CheckStateMO>
    {

        private UUID id;

        public GetClusterStateCall(BaseBergamotClient client)
        {
            super(client);
        }


        public GetClusterStateCall id(UUID id)
        {
            this.id = id;
            return this;
        }

        public CheckStateMO execute()
        {
            try
            {
                Response response = execute(
                    get(url("/api/cluster/id/" + this.id + "/state"))
                    .addHeader(authHeader())
                );
                return transcoder().decodeFromString(response.returnContent().asString(), CheckStateMO.class);
            }
            catch (IOException e)
            {
                throw new BergamotAPIException("Error calling Bergamot Monitoring API", e);
            }
        }

    }


    public GetClusterStateCall callGetClusterState()
    {
        return new GetClusterStateCall(this);
    }



    public static class GetClusterResourcesByNameCall extends BergamotAPICall<List<ResourceMO>>
    {

        private String name;

        public GetClusterResourcesByNameCall(BaseBergamotClient client)
        {
            super(client);
        }


        public GetClusterResourcesByNameCall name(String name)
        {
            this.name = name;
            return this;
        }

        public List<ResourceMO> execute()
        {
            try
            {
                Response response = execute(
                    get(url("/api/cluster/name/" + this.name + "/resources"))
                    .addHeader(authHeader())
                );
                return transcoder().decodeListFromString(response.returnContent().asString(), ResourceMO.class);
            }
            catch (IOException e)
            {
                throw new BergamotAPIException("Error calling Bergamot Monitoring API", e);
            }
        }

    }


    public GetClusterResourcesByNameCall callGetClusterResourcesByName()
    {
        return new GetClusterResourcesByNameCall(this);
    }



    public static class GetClusterResourcesCall extends BergamotAPICall<List<ResourceMO>>
    {

        private UUID id;

        public GetClusterResourcesCall(BaseBergamotClient client)
        {
            super(client);
        }


        public GetClusterResourcesCall id(UUID id)
        {
            this.id = id;
            return this;
        }

        public List<ResourceMO> execute()
        {
            try
            {
                Response response = execute(
                    get(url("/api/cluster/id/" + this.id + "/resources"))
                    .addHeader(authHeader())
                );
                return transcoder().decodeListFromString(response.returnContent().asString(), ResourceMO.class);
            }
            catch (IOException e)
            {
                throw new BergamotAPIException("Error calling Bergamot Monitoring API", e);
            }
        }

    }


    public GetClusterResourcesCall callGetClusterResources()
    {
        return new GetClusterResourcesCall(this);
    }



    public static class GetClusterReferencesByNameCall extends BergamotAPICall<List<CheckMO>>
    {

        private String name;

        public GetClusterReferencesByNameCall(BaseBergamotClient client)
        {
            super(client);
        }


        public GetClusterReferencesByNameCall name(String name)
        {
            this.name = name;
            return this;
        }

        public List<CheckMO> execute()
        {
            try
            {
                Response response = execute(
                    get(url("/api/cluster/name/" + this.name + "/references"))
                    .addHeader(authHeader())
                );
                return transcoder().decodeListFromString(response.returnContent().asString(), CheckMO.class);
            }
            catch (IOException e)
            {
                throw new BergamotAPIException("Error calling Bergamot Monitoring API", e);
            }
        }

    }


    public GetClusterReferencesByNameCall callGetClusterReferencesByName()
    {
        return new GetClusterReferencesByNameCall(this);
    }



    public static class GetClusterReferencesCall extends BergamotAPICall<List<CheckMO>>
    {

        private UUID id;

        public GetClusterReferencesCall(BaseBergamotClient client)
        {
            super(client);
        }


        public GetClusterReferencesCall id(UUID id)
        {
            this.id = id;
            return this;
        }

        public List<CheckMO> execute()
        {
            try
            {
                Response response = execute(
                    get(url("/api/cluster/id/" + this.id + "/references"))
                    .addHeader(authHeader())
                );
                return transcoder().decodeListFromString(response.returnContent().asString(), CheckMO.class);
            }
            catch (IOException e)
            {
                throw new BergamotAPIException("Error calling Bergamot Monitoring API", e);
            }
        }

    }


    public GetClusterReferencesCall callGetClusterReferences()
    {
        return new GetClusterReferencesCall(this);
    }



    public static class SuppressResourcesOnClusterCall extends BergamotAPICall<String>
    {

        private UUID id;

        public SuppressResourcesOnClusterCall(BaseBergamotClient client)
        {
            super(client);
        }


        public SuppressResourcesOnClusterCall id(UUID id)
        {
            this.id = id;
            return this;
        }

        public String execute()
        {
            try
            {
                Response response = execute(
                    get(url("/api/cluster/id/" + this.id + "/suppress-resources"))
                    .addHeader(authHeader())
                );
                return transcoder().decodeFromString(response.returnContent().asString(), String.class);
            }
            catch (IOException e)
            {
                throw new BergamotAPIException("Error calling Bergamot Monitoring API", e);
            }
        }

    }


    public SuppressResourcesOnClusterCall callSuppressResourcesOnCluster()
    {
        return new SuppressResourcesOnClusterCall(this);
    }



    public static class UnsuppressResourcesOnClusterCall extends BergamotAPICall<String>
    {

        private UUID id;

        public UnsuppressResourcesOnClusterCall(BaseBergamotClient client)
        {
            super(client);
        }


        public UnsuppressResourcesOnClusterCall id(UUID id)
        {
            this.id = id;
            return this;
        }

        public String execute()
        {
            try
            {
                Response response = execute(
                    get(url("/api/cluster/id/" + this.id + "/unsuppress-resources"))
                    .addHeader(authHeader())
                );
                return transcoder().decodeFromString(response.returnContent().asString(), String.class);
            }
            catch (IOException e)
            {
                throw new BergamotAPIException("Error calling Bergamot Monitoring API", e);
            }
        }

    }


    public UnsuppressResourcesOnClusterCall callUnsuppressResourcesOnCluster()
    {
        return new UnsuppressResourcesOnClusterCall(this);
    }



    public static class GetServiceCall extends BergamotAPICall<ServiceMO>
    {

        private UUID id;

        public GetServiceCall(BaseBergamotClient client)
        {
            super(client);
        }


        public GetServiceCall id(UUID id)
        {
            this.id = id;
            return this;
        }

        public ServiceMO execute()
        {
            try
            {
                Response response = execute(
                    get(url("/api/service/id/" + this.id + ""))
                    .addHeader(authHeader())
                );
                return transcoder().decodeFromString(response.returnContent().asString(), ServiceMO.class);
            }
            catch (IOException e)
            {
                throw new BergamotAPIException("Error calling Bergamot Monitoring API", e);
            }
        }

    }


    public GetServiceCall callGetService()
    {
        return new GetServiceCall(this);
    }



    public static class ExecuteServiceCall extends BergamotAPICall<String>
    {

        private UUID id;

        public ExecuteServiceCall(BaseBergamotClient client)
        {
            super(client);
        }


        public ExecuteServiceCall id(UUID id)
        {
            this.id = id;
            return this;
        }

        public String execute()
        {
            try
            {
                Response response = execute(
                    get(url("/api/service/id/" + this.id + "/execute"))
                    .addHeader(authHeader())
                );
                return transcoder().decodeFromString(response.returnContent().asString(), String.class);
            }
            catch (IOException e)
            {
                throw new BergamotAPIException("Error calling Bergamot Monitoring API", e);
            }
        }

    }


    public ExecuteServiceCall callExecuteService()
    {
        return new ExecuteServiceCall(this);
    }



    public static class SuppressServiceCall extends BergamotAPICall<String>
    {

        private UUID id;

        public SuppressServiceCall(BaseBergamotClient client)
        {
            super(client);
        }


        public SuppressServiceCall id(UUID id)
        {
            this.id = id;
            return this;
        }

        public String execute()
        {
            try
            {
                Response response = execute(
                    get(url("/api/service/id/" + this.id + "/suppress"))
                    .addHeader(authHeader())
                );
                return transcoder().decodeFromString(response.returnContent().asString(), String.class);
            }
            catch (IOException e)
            {
                throw new BergamotAPIException("Error calling Bergamot Monitoring API", e);
            }
        }

    }


    public SuppressServiceCall callSuppressService()
    {
        return new SuppressServiceCall(this);
    }



    public static class UnsuppressServiceCall extends BergamotAPICall<String>
    {

        private UUID id;

        public UnsuppressServiceCall(BaseBergamotClient client)
        {
            super(client);
        }


        public UnsuppressServiceCall id(UUID id)
        {
            this.id = id;
            return this;
        }

        public String execute()
        {
            try
            {
                Response response = execute(
                    get(url("/api/service/id/" + this.id + "/unsuppress"))
                    .addHeader(authHeader())
                );
                return transcoder().decodeFromString(response.returnContent().asString(), String.class);
            }
            catch (IOException e)
            {
                throw new BergamotAPIException("Error calling Bergamot Monitoring API", e);
            }
        }

    }


    public UnsuppressServiceCall callUnsuppressService()
    {
        return new UnsuppressServiceCall(this);
    }



    public static class GetServiceByNameCall extends BergamotAPICall<ServiceMO>
    {

        private String hostName;

        private String name;

        public GetServiceByNameCall(BaseBergamotClient client)
        {
            super(client);
        }


        public GetServiceByNameCall hostName(String hostName)
        {
            this.hostName = hostName;
            return this;
        }

        public GetServiceByNameCall name(String name)
        {
            this.name = name;
            return this;
        }

        public ServiceMO execute()
        {
            try
            {
                Response response = execute(
                    get(url("/api/service/name/" + this.hostName + "/" + this.name + ""))
                    .addHeader(authHeader())
                );
                return transcoder().decodeFromString(response.returnContent().asString(), ServiceMO.class);
            }
            catch (IOException e)
            {
                throw new BergamotAPIException("Error calling Bergamot Monitoring API", e);
            }
        }

    }


    public GetServiceByNameCall callGetServiceByName()
    {
        return new GetServiceByNameCall(this);
    }



    public static class GetServiceStateCall extends BergamotAPICall<CheckStateMO>
    {

        private UUID id;

        public GetServiceStateCall(BaseBergamotClient client)
        {
            super(client);
        }


        public GetServiceStateCall id(UUID id)
        {
            this.id = id;
            return this;
        }

        public CheckStateMO execute()
        {
            try
            {
                Response response = execute(
                    get(url("/api/service/id/" + this.id + "/state"))
                    .addHeader(authHeader())
                );
                return transcoder().decodeFromString(response.returnContent().asString(), CheckStateMO.class);
            }
            catch (IOException e)
            {
                throw new BergamotAPIException("Error calling Bergamot Monitoring API", e);
            }
        }

    }


    public GetServiceStateCall callGetServiceState()
    {
        return new GetServiceStateCall(this);
    }



    public static class GetServiceStateByNameCall extends BergamotAPICall<CheckStateMO>
    {

        private String hostName;

        private String name;

        public GetServiceStateByNameCall(BaseBergamotClient client)
        {
            super(client);
        }


        public GetServiceStateByNameCall hostName(String hostName)
        {
            this.hostName = hostName;
            return this;
        }

        public GetServiceStateByNameCall name(String name)
        {
            this.name = name;
            return this;
        }

        public CheckStateMO execute()
        {
            try
            {
                Response response = execute(
                    get(url("/api/service/name/" + this.hostName + "/" + this.name + "/state"))
                    .addHeader(authHeader())
                );
                return transcoder().decodeFromString(response.returnContent().asString(), CheckStateMO.class);
            }
            catch (IOException e)
            {
                throw new BergamotAPIException("Error calling Bergamot Monitoring API", e);
            }
        }

    }


    public GetServiceStateByNameCall callGetServiceStateByName()
    {
        return new GetServiceStateByNameCall(this);
    }



    public static class GetTrapCall extends BergamotAPICall<TrapMO>
    {

        private UUID id;

        public GetTrapCall(BaseBergamotClient client)
        {
            super(client);
        }


        public GetTrapCall id(UUID id)
        {
            this.id = id;
            return this;
        }

        public TrapMO execute()
        {
            try
            {
                Response response = execute(
                    get(url("/api/trap/id/" + this.id + ""))
                    .addHeader(authHeader())
                );
                return transcoder().decodeFromString(response.returnContent().asString(), TrapMO.class);
            }
            catch (IOException e)
            {
                throw new BergamotAPIException("Error calling Bergamot Monitoring API", e);
            }
        }

    }


    public GetTrapCall callGetTrap()
    {
        return new GetTrapCall(this);
    }



    public static class SuppressTrapCall extends BergamotAPICall<String>
    {

        private UUID id;

        public SuppressTrapCall(BaseBergamotClient client)
        {
            super(client);
        }


        public SuppressTrapCall id(UUID id)
        {
            this.id = id;
            return this;
        }

        public String execute()
        {
            try
            {
                Response response = execute(
                    get(url("/api/trap/id/" + this.id + "/suppress"))
                    .addHeader(authHeader())
                );
                return transcoder().decodeFromString(response.returnContent().asString(), String.class);
            }
            catch (IOException e)
            {
                throw new BergamotAPIException("Error calling Bergamot Monitoring API", e);
            }
        }

    }


    public SuppressTrapCall callSuppressTrap()
    {
        return new SuppressTrapCall(this);
    }



    public static class UnsuppressTrapCall extends BergamotAPICall<String>
    {

        private UUID id;

        public UnsuppressTrapCall(BaseBergamotClient client)
        {
            super(client);
        }


        public UnsuppressTrapCall id(UUID id)
        {
            this.id = id;
            return this;
        }

        public String execute()
        {
            try
            {
                Response response = execute(
                    get(url("/api/trap/id/" + this.id + "/unsuppress"))
                    .addHeader(authHeader())
                );
                return transcoder().decodeFromString(response.returnContent().asString(), String.class);
            }
            catch (IOException e)
            {
                throw new BergamotAPIException("Error calling Bergamot Monitoring API", e);
            }
        }

    }


    public UnsuppressTrapCall callUnsuppressTrap()
    {
        return new UnsuppressTrapCall(this);
    }



    public static class GetTrapByNameCall extends BergamotAPICall<TrapMO>
    {

        private String hostName;

        private String name;

        public GetTrapByNameCall(BaseBergamotClient client)
        {
            super(client);
        }


        public GetTrapByNameCall hostName(String hostName)
        {
            this.hostName = hostName;
            return this;
        }

        public GetTrapByNameCall name(String name)
        {
            this.name = name;
            return this;
        }

        public TrapMO execute()
        {
            try
            {
                Response response = execute(
                    get(url("/api/trap/name/" + this.hostName + "/" + this.name + ""))
                    .addHeader(authHeader())
                );
                return transcoder().decodeFromString(response.returnContent().asString(), TrapMO.class);
            }
            catch (IOException e)
            {
                throw new BergamotAPIException("Error calling Bergamot Monitoring API", e);
            }
        }

    }


    public GetTrapByNameCall callGetTrapByName()
    {
        return new GetTrapByNameCall(this);
    }



    public static class GetTrapStateCall extends BergamotAPICall<CheckStateMO>
    {

        private UUID id;

        public GetTrapStateCall(BaseBergamotClient client)
        {
            super(client);
        }


        public GetTrapStateCall id(UUID id)
        {
            this.id = id;
            return this;
        }

        public CheckStateMO execute()
        {
            try
            {
                Response response = execute(
                    get(url("/api/trap/id/" + this.id + "/state"))
                    .addHeader(authHeader())
                );
                return transcoder().decodeFromString(response.returnContent().asString(), CheckStateMO.class);
            }
            catch (IOException e)
            {
                throw new BergamotAPIException("Error calling Bergamot Monitoring API", e);
            }
        }

    }


    public GetTrapStateCall callGetTrapState()
    {
        return new GetTrapStateCall(this);
    }



    public static class GetTrapStateByNameCall extends BergamotAPICall<CheckStateMO>
    {

        private String hostName;

        private String name;

        public GetTrapStateByNameCall(BaseBergamotClient client)
        {
            super(client);
        }


        public GetTrapStateByNameCall hostName(String hostName)
        {
            this.hostName = hostName;
            return this;
        }

        public GetTrapStateByNameCall name(String name)
        {
            this.name = name;
            return this;
        }

        public CheckStateMO execute()
        {
            try
            {
                Response response = execute(
                    get(url("/api/trap/name/" + this.hostName + "/" + this.name + "/state"))
                    .addHeader(authHeader())
                );
                return transcoder().decodeFromString(response.returnContent().asString(), CheckStateMO.class);
            }
            catch (IOException e)
            {
                throw new BergamotAPIException("Error calling Bergamot Monitoring API", e);
            }
        }

    }


    public GetTrapStateByNameCall callGetTrapStateByName()
    {
        return new GetTrapStateByNameCall(this);
    }



    public static class SubmitTrapStatusCall extends BergamotAPICall<String>
    {

        private UUID id;

        private String status;

        private String output;

        public SubmitTrapStatusCall(BaseBergamotClient client)
        {
            super(client);
        }


        public SubmitTrapStatusCall id(UUID id)
        {
            this.id = id;
            return this;
        }

        public SubmitTrapStatusCall status(String status)
        {
            this.status = status;
            return this;
        }

        public SubmitTrapStatusCall output(String output)
        {
            this.output = output;
            return this;
        }

        public String execute()
        {
            try
            {
                Response response = execute(
                    post(url("/api/trap/id/" + this.id + "/submit"))
                    .addHeader(authHeader())
                    .bodyForm(
                        param("status", this.status),
                        param("output", this.output)
                    )
                );
                return transcoder().decodeFromString(response.returnContent().asString(), String.class);
            }
            catch (IOException e)
            {
                throw new BergamotAPIException("Error calling Bergamot Monitoring API", e);
            }
        }

    }


    public SubmitTrapStatusCall callSubmitTrapStatus()
    {
        return new SubmitTrapStatusCall(this);
    }



    public static class SuppressResourceCall extends BergamotAPICall<String>
    {

        private UUID id;

        public SuppressResourceCall(BaseBergamotClient client)
        {
            super(client);
        }


        public SuppressResourceCall id(UUID id)
        {
            this.id = id;
            return this;
        }

        public String execute()
        {
            try
            {
                Response response = execute(
                    get(url("/api/resource/id/" + this.id + "/suppress"))
                    .addHeader(authHeader())
                );
                return transcoder().decodeFromString(response.returnContent().asString(), String.class);
            }
            catch (IOException e)
            {
                throw new BergamotAPIException("Error calling Bergamot Monitoring API", e);
            }
        }

    }


    public SuppressResourceCall callSuppressResource()
    {
        return new SuppressResourceCall(this);
    }



    public static class UnsuppressResourceCall extends BergamotAPICall<String>
    {

        private UUID id;

        public UnsuppressResourceCall(BaseBergamotClient client)
        {
            super(client);
        }


        public UnsuppressResourceCall id(UUID id)
        {
            this.id = id;
            return this;
        }

        public String execute()
        {
            try
            {
                Response response = execute(
                    get(url("/api/resource/id/" + this.id + "/unsuppress"))
                    .addHeader(authHeader())
                );
                return transcoder().decodeFromString(response.returnContent().asString(), String.class);
            }
            catch (IOException e)
            {
                throw new BergamotAPIException("Error calling Bergamot Monitoring API", e);
            }
        }

    }


    public UnsuppressResourceCall callUnsuppressResource()
    {
        return new UnsuppressResourceCall(this);
    }



    public static class GetResourceByNameCall extends BergamotAPICall<ResourceMO>
    {

        private String clusterName;

        private String name;

        public GetResourceByNameCall(BaseBergamotClient client)
        {
            super(client);
        }


        public GetResourceByNameCall clusterName(String clusterName)
        {
            this.clusterName = clusterName;
            return this;
        }

        public GetResourceByNameCall name(String name)
        {
            this.name = name;
            return this;
        }

        public ResourceMO execute()
        {
            try
            {
                Response response = execute(
                    get(url("/api/resource/name/" + this.clusterName + "/" + this.name + ""))
                    .addHeader(authHeader())
                );
                return transcoder().decodeFromString(response.returnContent().asString(), ResourceMO.class);
            }
            catch (IOException e)
            {
                throw new BergamotAPIException("Error calling Bergamot Monitoring API", e);
            }
        }

    }


    public GetResourceByNameCall callGetResourceByName()
    {
        return new GetResourceByNameCall(this);
    }



    public static class GetResourceStateCall extends BergamotAPICall<CheckStateMO>
    {

        private UUID id;

        public GetResourceStateCall(BaseBergamotClient client)
        {
            super(client);
        }


        public GetResourceStateCall id(UUID id)
        {
            this.id = id;
            return this;
        }

        public CheckStateMO execute()
        {
            try
            {
                Response response = execute(
                    get(url("/api/resource/id/" + this.id + "/state"))
                    .addHeader(authHeader())
                );
                return transcoder().decodeFromString(response.returnContent().asString(), CheckStateMO.class);
            }
            catch (IOException e)
            {
                throw new BergamotAPIException("Error calling Bergamot Monitoring API", e);
            }
        }

    }


    public GetResourceStateCall callGetResourceState()
    {
        return new GetResourceStateCall(this);
    }



    public static class GetResourceStateByNameCall extends BergamotAPICall<CheckStateMO>
    {

        private String clusterName;

        private String name;

        public GetResourceStateByNameCall(BaseBergamotClient client)
        {
            super(client);
        }


        public GetResourceStateByNameCall clusterName(String clusterName)
        {
            this.clusterName = clusterName;
            return this;
        }

        public GetResourceStateByNameCall name(String name)
        {
            this.name = name;
            return this;
        }

        public CheckStateMO execute()
        {
            try
            {
                Response response = execute(
                    get(url("/api/resource/name/" + this.clusterName + "/" + this.name + "/state"))
                    .addHeader(authHeader())
                );
                return transcoder().decodeFromString(response.returnContent().asString(), CheckStateMO.class);
            }
            catch (IOException e)
            {
                throw new BergamotAPIException("Error calling Bergamot Monitoring API", e);
            }
        }

    }


    public GetResourceStateByNameCall callGetResourceStateByName()
    {
        return new GetResourceStateByNameCall(this);
    }



    public static class GetResourceCall extends BergamotAPICall<ResourceMO>
    {

        private UUID id;

        public GetResourceCall(BaseBergamotClient client)
        {
            super(client);
        }


        public GetResourceCall id(UUID id)
        {
            this.id = id;
            return this;
        }

        public ResourceMO execute()
        {
            try
            {
                Response response = execute(
                    get(url("/api/resource/id/" + this.id + ""))
                    .addHeader(authHeader())
                );
                return transcoder().decodeFromString(response.returnContent().asString(), ResourceMO.class);
            }
            catch (IOException e)
            {
                throw new BergamotAPIException("Error calling Bergamot Monitoring API", e);
            }
        }

    }


    public GetResourceCall callGetResource()
    {
        return new GetResourceCall(this);
    }



    public static class GetTimePeriodCall extends BergamotAPICall<TimePeriodMO>
    {

        private UUID id;

        public GetTimePeriodCall(BaseBergamotClient client)
        {
            super(client);
        }


        public GetTimePeriodCall id(UUID id)
        {
            this.id = id;
            return this;
        }

        public TimePeriodMO execute()
        {
            try
            {
                Response response = execute(
                    get(url("/api/time-period/id/" + this.id + ""))
                    .addHeader(authHeader())
                );
                return transcoder().decodeFromString(response.returnContent().asString(), TimePeriodMO.class);
            }
            catch (IOException e)
            {
                throw new BergamotAPIException("Error calling Bergamot Monitoring API", e);
            }
        }

    }


    public GetTimePeriodCall callGetTimePeriod()
    {
        return new GetTimePeriodCall(this);
    }



    public static class GetTimePeriodsCall extends BergamotAPICall<List<TimePeriodMO>>
    {

        public GetTimePeriodsCall(BaseBergamotClient client)
        {
            super(client);
        }


        public List<TimePeriodMO> execute()
        {
            try
            {
                Response response = execute(
                    get(url("/api/time-period/"))
                    .addHeader(authHeader())
                );
                return transcoder().decodeListFromString(response.returnContent().asString(), TimePeriodMO.class);
            }
            catch (IOException e)
            {
                throw new BergamotAPIException("Error calling Bergamot Monitoring API", e);
            }
        }

    }


    public GetTimePeriodsCall callGetTimePeriods()
    {
        return new GetTimePeriodsCall(this);
    }



    public static class GetTimePeriodByNameCall extends BergamotAPICall<TimePeriodMO>
    {

        private String name;

        public GetTimePeriodByNameCall(BaseBergamotClient client)
        {
            super(client);
        }


        public GetTimePeriodByNameCall name(String name)
        {
            this.name = name;
            return this;
        }

        public TimePeriodMO execute()
        {
            try
            {
                Response response = execute(
                    get(url("/api/time-period/name/" + this.name + ""))
                    .addHeader(authHeader())
                );
                return transcoder().decodeFromString(response.returnContent().asString(), TimePeriodMO.class);
            }
            catch (IOException e)
            {
                throw new BergamotAPIException("Error calling Bergamot Monitoring API", e);
            }
        }

    }


    public GetTimePeriodByNameCall callGetTimePeriodByName()
    {
        return new GetTimePeriodByNameCall(this);
    }



    public static class GetCommandCall extends BergamotAPICall<CommandMO>
    {

        private UUID id;

        public GetCommandCall(BaseBergamotClient client)
        {
            super(client);
        }


        public GetCommandCall id(UUID id)
        {
            this.id = id;
            return this;
        }

        public CommandMO execute()
        {
            try
            {
                Response response = execute(
                    get(url("/api/command/id/" + this.id + ""))
                    .addHeader(authHeader())
                );
                return transcoder().decodeFromString(response.returnContent().asString(), CommandMO.class);
            }
            catch (IOException e)
            {
                throw new BergamotAPIException("Error calling Bergamot Monitoring API", e);
            }
        }

    }


    public GetCommandCall callGetCommand()
    {
        return new GetCommandCall(this);
    }



    public static class GetCommandByNameCall extends BergamotAPICall<CommandMO>
    {

        private String name;

        public GetCommandByNameCall(BaseBergamotClient client)
        {
            super(client);
        }


        public GetCommandByNameCall name(String name)
        {
            this.name = name;
            return this;
        }

        public CommandMO execute()
        {
            try
            {
                Response response = execute(
                    get(url("/api/command/name/" + this.name + ""))
                    .addHeader(authHeader())
                );
                return transcoder().decodeFromString(response.returnContent().asString(), CommandMO.class);
            }
            catch (IOException e)
            {
                throw new BergamotAPIException("Error calling Bergamot Monitoring API", e);
            }
        }

    }


    public GetCommandByNameCall callGetCommandByName()
    {
        return new GetCommandByNameCall(this);
    }



    public static class GetCommandsCall extends BergamotAPICall<List<CommandMO>>
    {

        public GetCommandsCall(BaseBergamotClient client)
        {
            super(client);
        }


        public List<CommandMO> execute()
        {
            try
            {
                Response response = execute(
                    get(url("/api/command/"))
                    .addHeader(authHeader())
                );
                return transcoder().decodeListFromString(response.returnContent().asString(), CommandMO.class);
            }
            catch (IOException e)
            {
                throw new BergamotAPIException("Error calling Bergamot Monitoring API", e);
            }
        }

    }


    public GetCommandsCall callGetCommands()
    {
        return new GetCommandsCall(this);
    }



    public static class GetContactByNameOrEmailCall extends BergamotAPICall<ContactMO>
    {

        private String nameOrEmail;

        public GetContactByNameOrEmailCall(BaseBergamotClient client)
        {
            super(client);
        }


        public GetContactByNameOrEmailCall nameOrEmail(String nameOrEmail)
        {
            this.nameOrEmail = nameOrEmail;
            return this;
        }

        public ContactMO execute()
        {
            try
            {
                Response response = execute(
                    get(url("/api/contact/name-or-email/" + this.nameOrEmail + ""))
                    .addHeader(authHeader())
                );
                return transcoder().decodeFromString(response.returnContent().asString(), ContactMO.class);
            }
            catch (IOException e)
            {
                throw new BergamotAPIException("Error calling Bergamot Monitoring API", e);
            }
        }

    }


    public GetContactByNameOrEmailCall callGetContactByNameOrEmail()
    {
        return new GetContactByNameOrEmailCall(this);
    }



    public static class GetContactCall extends BergamotAPICall<ContactMO>
    {

        private UUID id;

        public GetContactCall(BaseBergamotClient client)
        {
            super(client);
        }


        public GetContactCall id(UUID id)
        {
            this.id = id;
            return this;
        }

        public ContactMO execute()
        {
            try
            {
                Response response = execute(
                    get(url("/api/contact/id/" + this.id + ""))
                    .addHeader(authHeader())
                );
                return transcoder().decodeFromString(response.returnContent().asString(), ContactMO.class);
            }
            catch (IOException e)
            {
                throw new BergamotAPIException("Error calling Bergamot Monitoring API", e);
            }
        }

    }


    public GetContactCall callGetContact()
    {
        return new GetContactCall(this);
    }



    public static class GetContactsCall extends BergamotAPICall<List<ContactMO>>
    {

        public GetContactsCall(BaseBergamotClient client)
        {
            super(client);
        }


        public List<ContactMO> execute()
        {
            try
            {
                Response response = execute(
                    get(url("/api/contact/"))
                    .addHeader(authHeader())
                );
                return transcoder().decodeListFromString(response.returnContent().asString(), ContactMO.class);
            }
            catch (IOException e)
            {
                throw new BergamotAPIException("Error calling Bergamot Monitoring API", e);
            }
        }

    }


    public GetContactsCall callGetContacts()
    {
        return new GetContactsCall(this);
    }



    public static class GetContactByNameCall extends BergamotAPICall<ContactMO>
    {

        private String name;

        public GetContactByNameCall(BaseBergamotClient client)
        {
            super(client);
        }


        public GetContactByNameCall name(String name)
        {
            this.name = name;
            return this;
        }

        public ContactMO execute()
        {
            try
            {
                Response response = execute(
                    get(url("/api/contact/name/" + this.name + ""))
                    .addHeader(authHeader())
                );
                return transcoder().decodeFromString(response.returnContent().asString(), ContactMO.class);
            }
            catch (IOException e)
            {
                throw new BergamotAPIException("Error calling Bergamot Monitoring API", e);
            }
        }

    }


    public GetContactByNameCall callGetContactByName()
    {
        return new GetContactByNameCall(this);
    }



    public static class GetContactByEmailCall extends BergamotAPICall<ContactMO>
    {

        private String email;

        public GetContactByEmailCall(BaseBergamotClient client)
        {
            super(client);
        }


        public GetContactByEmailCall email(String email)
        {
            this.email = email;
            return this;
        }

        public ContactMO execute()
        {
            try
            {
                Response response = execute(
                    get(url("/api/contact/email/" + this.email + ""))
                    .addHeader(authHeader())
                );
                return transcoder().decodeFromString(response.returnContent().asString(), ContactMO.class);
            }
            catch (IOException e)
            {
                throw new BergamotAPIException("Error calling Bergamot Monitoring API", e);
            }
        }

    }


    public GetContactByEmailCall callGetContactByEmail()
    {
        return new GetContactByEmailCall(this);
    }



    public static class GetTeamCall extends BergamotAPICall<TeamMO>
    {

        private UUID id;

        public GetTeamCall(BaseBergamotClient client)
        {
            super(client);
        }


        public GetTeamCall id(UUID id)
        {
            this.id = id;
            return this;
        }

        public TeamMO execute()
        {
            try
            {
                Response response = execute(
                    get(url("/api/team/id/" + this.id + ""))
                    .addHeader(authHeader())
                );
                return transcoder().decodeFromString(response.returnContent().asString(), TeamMO.class);
            }
            catch (IOException e)
            {
                throw new BergamotAPIException("Error calling Bergamot Monitoring API", e);
            }
        }

    }


    public GetTeamCall callGetTeam()
    {
        return new GetTeamCall(this);
    }



    public static class GetTeamsCall extends BergamotAPICall<List<TeamMO>>
    {

        public GetTeamsCall(BaseBergamotClient client)
        {
            super(client);
        }


        public List<TeamMO> execute()
        {
            try
            {
                Response response = execute(
                    get(url("/api/team/"))
                    .addHeader(authHeader())
                );
                return transcoder().decodeListFromString(response.returnContent().asString(), TeamMO.class);
            }
            catch (IOException e)
            {
                throw new BergamotAPIException("Error calling Bergamot Monitoring API", e);
            }
        }

    }


    public GetTeamsCall callGetTeams()
    {
        return new GetTeamsCall(this);
    }



    public static class GetTeamByNameCall extends BergamotAPICall<TeamMO>
    {

        private String name;

        public GetTeamByNameCall(BaseBergamotClient client)
        {
            super(client);
        }


        public GetTeamByNameCall name(String name)
        {
            this.name = name;
            return this;
        }

        public TeamMO execute()
        {
            try
            {
                Response response = execute(
                    get(url("/api/team/name/" + this.name + ""))
                    .addHeader(authHeader())
                );
                return transcoder().decodeFromString(response.returnContent().asString(), TeamMO.class);
            }
            catch (IOException e)
            {
                throw new BergamotAPIException("Error calling Bergamot Monitoring API", e);
            }
        }

    }


    public GetTeamByNameCall callGetTeamByName()
    {
        return new GetTeamByNameCall(this);
    }



    public static class GetTeamChildrenByNameCall extends BergamotAPICall<List<TeamMO>>
    {

        private String name;

        public GetTeamChildrenByNameCall(BaseBergamotClient client)
        {
            super(client);
        }


        public GetTeamChildrenByNameCall name(String name)
        {
            this.name = name;
            return this;
        }

        public List<TeamMO> execute()
        {
            try
            {
                Response response = execute(
                    get(url("/api/team/name/" + this.name + "/children"))
                    .addHeader(authHeader())
                );
                return transcoder().decodeListFromString(response.returnContent().asString(), TeamMO.class);
            }
            catch (IOException e)
            {
                throw new BergamotAPIException("Error calling Bergamot Monitoring API", e);
            }
        }

    }


    public GetTeamChildrenByNameCall callGetTeamChildrenByName()
    {
        return new GetTeamChildrenByNameCall(this);
    }



    public static class GetTeamContactsByNameCall extends BergamotAPICall<List<ContactMO>>
    {

        private String name;

        public GetTeamContactsByNameCall(BaseBergamotClient client)
        {
            super(client);
        }


        public GetTeamContactsByNameCall name(String name)
        {
            this.name = name;
            return this;
        }

        public List<ContactMO> execute()
        {
            try
            {
                Response response = execute(
                    get(url("/api/team/name/" + this.name + "/contacts"))
                    .addHeader(authHeader())
                );
                return transcoder().decodeListFromString(response.returnContent().asString(), ContactMO.class);
            }
            catch (IOException e)
            {
                throw new BergamotAPIException("Error calling Bergamot Monitoring API", e);
            }
        }

    }


    public GetTeamContactsByNameCall callGetTeamContactsByName()
    {
        return new GetTeamContactsByNameCall(this);
    }



    public static class GetTeamChildrenCall extends BergamotAPICall<List<TeamMO>>
    {

        private UUID id;

        public GetTeamChildrenCall(BaseBergamotClient client)
        {
            super(client);
        }


        public GetTeamChildrenCall id(UUID id)
        {
            this.id = id;
            return this;
        }

        public List<TeamMO> execute()
        {
            try
            {
                Response response = execute(
                    get(url("/api/team/id/" + this.id + "/children"))
                    .addHeader(authHeader())
                );
                return transcoder().decodeListFromString(response.returnContent().asString(), TeamMO.class);
            }
            catch (IOException e)
            {
                throw new BergamotAPIException("Error calling Bergamot Monitoring API", e);
            }
        }

    }


    public GetTeamChildrenCall callGetTeamChildren()
    {
        return new GetTeamChildrenCall(this);
    }



    public static class GetTeamContactsCall extends BergamotAPICall<List<ContactMO>>
    {

        private UUID id;

        public GetTeamContactsCall(BaseBergamotClient client)
        {
            super(client);
        }


        public GetTeamContactsCall id(UUID id)
        {
            this.id = id;
            return this;
        }

        public List<ContactMO> execute()
        {
            try
            {
                Response response = execute(
                    get(url("/api/team/id/" + this.id + "/contacts"))
                    .addHeader(authHeader())
                );
                return transcoder().decodeListFromString(response.returnContent().asString(), ContactMO.class);
            }
            catch (IOException e)
            {
                throw new BergamotAPIException("Error calling Bergamot Monitoring API", e);
            }
        }

    }


    public GetTeamContactsCall callGetTeamContacts()
    {
        return new GetTeamContactsCall(this);
    }



    public static class GetCommentCall extends BergamotAPICall<CommentMO>
    {

        private UUID id;

        public GetCommentCall(BaseBergamotClient client)
        {
            super(client);
        }


        public GetCommentCall id(UUID id)
        {
            this.id = id;
            return this;
        }

        public CommentMO execute()
        {
            try
            {
                Response response = execute(
                    get(url("/api/comment/id/" + this.id + ""))
                    .addHeader(authHeader())
                );
                return transcoder().decodeFromString(response.returnContent().asString(), CommentMO.class);
            }
            catch (IOException e)
            {
                throw new BergamotAPIException("Error calling Bergamot Monitoring API", e);
            }
        }

    }


    public GetCommentCall callGetComment()
    {
        return new GetCommentCall(this);
    }



    public static class RemoveCommentCall extends BergamotAPICall<Boolean>
    {

        private UUID id;

        public RemoveCommentCall(BaseBergamotClient client)
        {
            super(client);
        }


        public RemoveCommentCall id(UUID id)
        {
            this.id = id;
            return this;
        }

        public Boolean execute()
        {
            try
            {
                Response response = execute(
                    get(url("/api/comment/id/" + this.id + "/remove"))
                    .addHeader(authHeader())
                );
                return transcoder().decodeFromString(response.returnContent().asString(), Boolean.class);
            }
            catch (IOException e)
            {
                throw new BergamotAPIException("Error calling Bergamot Monitoring API", e);
            }
        }

    }


    public RemoveCommentCall callRemoveComment()
    {
        return new RemoveCommentCall(this);
    }



    public static class GetCommentsForObjectCall extends BergamotAPICall<List<CommentMO>>
    {

        private UUID id;

        private Long offset;

        private Long limit;

        public GetCommentsForObjectCall(BaseBergamotClient client)
        {
            super(client);
        }


        public GetCommentsForObjectCall id(UUID id)
        {
            this.id = id;
            return this;
        }

        public GetCommentsForObjectCall offset(Long offset)
        {
            this.offset = offset;
            return this;
        }

        public GetCommentsForObjectCall limit(Long limit)
        {
            this.limit = limit;
            return this;
        }

        public List<CommentMO> execute()
        {
            try
            {
                Response response = execute(
                    get(
                        appendQuery(
                            url("/api/comment/for-object/id/" + this.id + ""),
                        param("offset", this.offset),
                        param("limit", this.limit)
                        )
                    ).addHeader(authHeader())
                );
                return transcoder().decodeListFromString(response.returnContent().asString(), CommentMO.class);
            }
            catch (IOException e)
            {
                throw new BergamotAPIException("Error calling Bergamot Monitoring API", e);
            }
        }

    }


    public GetCommentsForObjectCall callGetCommentsForObject()
    {
        return new GetCommentsForObjectCall(this);
    }



    public static class AddCommentToCheckCall extends BergamotAPICall<CommentMO>
    {

        private UUID id;

        private String summary;

        private String message;

        public AddCommentToCheckCall(BaseBergamotClient client)
        {
            super(client);
        }


        public AddCommentToCheckCall id(UUID id)
        {
            this.id = id;
            return this;
        }

        public AddCommentToCheckCall summary(String summary)
        {
            this.summary = summary;
            return this;
        }

        public AddCommentToCheckCall message(String message)
        {
            this.message = message;
            return this;
        }

        public CommentMO execute()
        {
            try
            {
                Response response = execute(
                    post(url("/api/comment/add-comment-to-check/id/" + this.id + ""))
                    .addHeader(authHeader())
                    .bodyForm(
                        param("summary", this.summary),
                        param("comment", this.message)
                    )
                );
                return transcoder().decodeFromString(response.returnContent().asString(), CommentMO.class);
            }
            catch (IOException e)
            {
                throw new BergamotAPIException("Error calling Bergamot Monitoring API", e);
            }
        }

    }


    public AddCommentToCheckCall callAddCommentToCheck()
    {
        return new AddCommentToCheckCall(this);
    }



    public static class AddCommentToAlertCall extends BergamotAPICall<CommentMO>
    {

        private UUID id;

        private String summary;

        private String message;

        public AddCommentToAlertCall(BaseBergamotClient client)
        {
            super(client);
        }


        public AddCommentToAlertCall id(UUID id)
        {
            this.id = id;
            return this;
        }

        public AddCommentToAlertCall summary(String summary)
        {
            this.summary = summary;
            return this;
        }

        public AddCommentToAlertCall message(String message)
        {
            this.message = message;
            return this;
        }

        public CommentMO execute()
        {
            try
            {
                Response response = execute(
                    post(url("/api/comment/add-comment-to-alert/id/" + this.id + ""))
                    .addHeader(authHeader())
                    .bodyForm(
                        param("summary", this.summary),
                        param("comment", this.message)
                    )
                );
                return transcoder().decodeFromString(response.returnContent().asString(), CommentMO.class);
            }
            catch (IOException e)
            {
                throw new BergamotAPIException("Error calling Bergamot Monitoring API", e);
            }
        }

    }


    public AddCommentToAlertCall callAddCommentToAlert()
    {
        return new AddCommentToAlertCall(this);
    }



    public static class AddCommentToDowntimeCall extends BergamotAPICall<CommentMO>
    {

        private UUID id;

        private String summary;

        private String message;

        public AddCommentToDowntimeCall(BaseBergamotClient client)
        {
            super(client);
        }


        public AddCommentToDowntimeCall id(UUID id)
        {
            this.id = id;
            return this;
        }

        public AddCommentToDowntimeCall summary(String summary)
        {
            this.summary = summary;
            return this;
        }

        public AddCommentToDowntimeCall message(String message)
        {
            this.message = message;
            return this;
        }

        public CommentMO execute()
        {
            try
            {
                Response response = execute(
                    post(url("/api/comment/add-comment-to-downtime/id/" + this.id + ""))
                    .addHeader(authHeader())
                    .bodyForm(
                        param("summary", this.summary),
                        param("comment", this.message)
                    )
                );
                return transcoder().decodeFromString(response.returnContent().asString(), CommentMO.class);
            }
            catch (IOException e)
            {
                throw new BergamotAPIException("Error calling Bergamot Monitoring API", e);
            }
        }

    }


    public AddCommentToDowntimeCall callAddCommentToDowntime()
    {
        return new AddCommentToDowntimeCall(this);
    }



    public static class AddCommentToObjectCall extends BergamotAPICall<CommentMO>
    {

        private UUID id;

        private String summary;

        private String message;

        public AddCommentToObjectCall(BaseBergamotClient client)
        {
            super(client);
        }


        public AddCommentToObjectCall id(UUID id)
        {
            this.id = id;
            return this;
        }

        public AddCommentToObjectCall summary(String summary)
        {
            this.summary = summary;
            return this;
        }

        public AddCommentToObjectCall message(String message)
        {
            this.message = message;
            return this;
        }

        public CommentMO execute()
        {
            try
            {
                Response response = execute(
                    post(url("/api/comment/add-comment-to-object/id/" + this.id + ""))
                    .addHeader(authHeader())
                    .bodyForm(
                        param("summary", this.summary),
                        param("comment", this.message)
                    )
                );
                return transcoder().decodeFromString(response.returnContent().asString(), CommentMO.class);
            }
            catch (IOException e)
            {
                throw new BergamotAPIException("Error calling Bergamot Monitoring API", e);
            }
        }

    }


    public AddCommentToObjectCall callAddCommentToObject()
    {
        return new AddCommentToObjectCall(this);
    }



    public static class RemoveDowntimeCall extends BergamotAPICall<Boolean>
    {

        private UUID id;

        public RemoveDowntimeCall(BaseBergamotClient client)
        {
            super(client);
        }


        public RemoveDowntimeCall id(UUID id)
        {
            this.id = id;
            return this;
        }

        public Boolean execute()
        {
            try
            {
                Response response = execute(
                    get(url("/api/downtime/id/" + this.id + "/remove"))
                    .addHeader(authHeader())
                );
                return transcoder().decodeFromString(response.returnContent().asString(), Boolean.class);
            }
            catch (IOException e)
            {
                throw new BergamotAPIException("Error calling Bergamot Monitoring API", e);
            }
        }

    }


    public RemoveDowntimeCall callRemoveDowntime()
    {
        return new RemoveDowntimeCall(this);
    }



    public static class GetDowntimeForObjectCall extends BergamotAPICall<List<DowntimeMO>>
    {

        private UUID id;

        private Integer pastDays;

        private Integer futureDays;

        public GetDowntimeForObjectCall(BaseBergamotClient client)
        {
            super(client);
        }


        public GetDowntimeForObjectCall id(UUID id)
        {
            this.id = id;
            return this;
        }

        public GetDowntimeForObjectCall pastDays(Integer pastDays)
        {
            this.pastDays = pastDays;
            return this;
        }

        public GetDowntimeForObjectCall futureDays(Integer futureDays)
        {
            this.futureDays = futureDays;
            return this;
        }

        public List<DowntimeMO> execute()
        {
            try
            {
                Response response = execute(
                    get(
                        appendQuery(
                            url("/api/downtime/for-object/id/" + this.id + ""),
                        param("past", this.pastDays),
                        param("future", this.futureDays)
                        )
                    ).addHeader(authHeader())
                );
                return transcoder().decodeListFromString(response.returnContent().asString(), DowntimeMO.class);
            }
            catch (IOException e)
            {
                throw new BergamotAPIException("Error calling Bergamot Monitoring API", e);
            }
        }

    }


    public GetDowntimeForObjectCall callGetDowntimeForObject()
    {
        return new GetDowntimeForObjectCall(this);
    }



    public static class GetDowntimeCall extends BergamotAPICall<DowntimeMO>
    {

        private UUID id;

        public GetDowntimeCall(BaseBergamotClient client)
        {
            super(client);
        }


        public GetDowntimeCall id(UUID id)
        {
            this.id = id;
            return this;
        }

        public DowntimeMO execute()
        {
            try
            {
                Response response = execute(
                    get(url("/api/downtime/id/" + this.id + ""))
                    .addHeader(authHeader())
                );
                return transcoder().decodeFromString(response.returnContent().asString(), DowntimeMO.class);
            }
            catch (IOException e)
            {
                throw new BergamotAPIException("Error calling Bergamot Monitoring API", e);
            }
        }

    }


    public GetDowntimeCall callGetDowntime()
    {
        return new GetDowntimeCall(this);
    }



    public static class AddDowntimeToCheckCall extends BergamotAPICall<DowntimeMO>
    {

        private UUID id;

        private Date startTime;

        private Date endTime;

        private String summary;

        private String description;

        public AddDowntimeToCheckCall(BaseBergamotClient client)
        {
            super(client);
        }


        public AddDowntimeToCheckCall id(UUID id)
        {
            this.id = id;
            return this;
        }

        public AddDowntimeToCheckCall startTime(Date startTime)
        {
            this.startTime = startTime;
            return this;
        }

        public AddDowntimeToCheckCall endTime(Date endTime)
        {
            this.endTime = endTime;
            return this;
        }

        public AddDowntimeToCheckCall summary(String summary)
        {
            this.summary = summary;
            return this;
        }

        public AddDowntimeToCheckCall description(String description)
        {
            this.description = description;
            return this;
        }

        public DowntimeMO execute()
        {
            try
            {
                Response response = execute(
                    post(url("/api/downtime/add-downtime-to-check/id/" + this.id + ""))
                    .addHeader(authHeader())
                    .bodyForm(
                        param("starts", this.startTime),
                        param("ends", this.endTime),
                        param("summary", this.summary),
                        param("description", this.description)
                    )
                );
                return transcoder().decodeFromString(response.returnContent().asString(), DowntimeMO.class);
            }
            catch (IOException e)
            {
                throw new BergamotAPIException("Error calling Bergamot Monitoring API", e);
            }
        }

    }


    public AddDowntimeToCheckCall callAddDowntimeToCheck()
    {
        return new AddDowntimeToCheckCall(this);
    }



    public static class ObjectExistsCall extends BergamotAPICall<Boolean>
    {

        private String type;

        private String name;

        public ObjectExistsCall(BaseBergamotClient client)
        {
            super(client);
        }


        public ObjectExistsCall type(String type)
        {
            this.type = type;
            return this;
        }

        public ObjectExistsCall name(String name)
        {
            this.name = name;
            return this;
        }

        public Boolean execute()
        {
            try
            {
                Response response = execute(
                    post(url("/api/config/exists/" + this.type + "/" + this.name + ""))
                    .addHeader(authHeader())
                    .bodyForm(

                    )
                );
                return transcoder().decodeFromString(response.returnContent().asString(), Boolean.class);
            }
            catch (IOException e)
            {
                throw new BergamotAPIException("Error calling Bergamot Monitoring API", e);
            }
        }

    }


    public ObjectExistsCall callObjectExists()
    {
        return new ObjectExistsCall(this);
    }



    public static class ListIconsCall extends BergamotAPICall<List<String>>
    {

        public ListIconsCall(BaseBergamotClient client)
        {
            super(client);
        }


        public List<String> execute()
        {
            try
            {
                Response response = execute(
                    post(url("/api/config/icon/"))
                    .addHeader(authHeader())
                    .bodyForm(

                    )
                );
                return transcoder().decodeListFromString(response.returnContent().asString(), String.class);
            }
            catch (IOException e)
            {
                throw new BergamotAPIException("Error calling Bergamot Monitoring API", e);
            }
        }

    }


    public ListIconsCall callListIcons()
    {
        return new ListIconsCall(this);
    }



    public static class GetCheckTransitionsCall extends BergamotAPICall<List<CheckTransitionMO>>
    {

        private UUID id;

        private Long offset;

        private Long limit;

        public GetCheckTransitionsCall(BaseBergamotClient client)
        {
            super(client);
        }


        public GetCheckTransitionsCall id(UUID id)
        {
            this.id = id;
            return this;
        }

        public GetCheckTransitionsCall offset(Long offset)
        {
            this.offset = offset;
            return this;
        }

        public GetCheckTransitionsCall limit(Long limit)
        {
            this.limit = limit;
            return this;
        }

        public List<CheckTransitionMO> execute()
        {
            try
            {
                Response response = execute(
                    post(url("/api/stats/transitions/check/id/" + this.id + ""))
                    .addHeader(authHeader())
                    .bodyForm(
                        param("offset", this.offset),
                        param("limit", this.limit)
                    )
                );
                return transcoder().decodeListFromString(response.returnContent().asString(), CheckTransitionMO.class);
            }
            catch (IOException e)
            {
                throw new BergamotAPIException("Error calling Bergamot Monitoring API", e);
            }
        }

    }


    public GetCheckTransitionsCall callGetCheckTransitions()
    {
        return new GetCheckTransitionsCall(this);
    }



    public static class NewIdCall extends BergamotAPICall<UUID>
    {

        public NewIdCall(BaseBergamotClient client)
        {
            super(client);
        }


        public UUID execute()
        {
            try
            {
                Response response = execute(
                    get(url("/api/util/id/new"))
                    .addHeader(authHeader())
                );
                return transcoder().decodeFromString(response.returnContent().asString(), UUID.class);
            }
            catch (IOException e)
            {
                throw new BergamotAPIException("Error calling Bergamot Monitoring API", e);
            }
        }

    }


    public NewIdCall callNewId()
    {
        return new NewIdCall(this);
    }



    public static class NewIdsCall extends BergamotAPICall<List<UUID>>
    {

        private Integer count;

        public NewIdsCall(BaseBergamotClient client)
        {
            super(client);
        }


        public NewIdsCall count(Integer count)
        {
            this.count = count;
            return this;
        }

        public List<UUID> execute()
        {
            try
            {
                Response response = execute(
                    get(url("/api/util/id/new/" + this.count + ""))
                    .addHeader(authHeader())
                );
                return transcoder().decodeListFromString(response.returnContent().asString(), UUID.class);
            }
            catch (IOException e)
            {
                throw new BergamotAPIException("Error calling Bergamot Monitoring API", e);
            }
        }

    }


    public NewIdsCall callNewIds()
    {
        return new NewIdsCall(this);
    }



    public static class GetReadingsByCheckCall extends BergamotAPICall<List<CheckReadingMO>>
    {

        private UUID id;

        public GetReadingsByCheckCall(BaseBergamotClient client)
        {
            super(client);
        }


        public GetReadingsByCheckCall id(UUID id)
        {
            this.id = id;
            return this;
        }

        public List<CheckReadingMO> execute()
        {
            try
            {
                Response response = execute(
                    post(url("/api/lamplighter/check/id/" + this.id + "/readings"))
                    .addHeader(authHeader())
                    .bodyForm(

                    )
                );
                return transcoder().decodeListFromString(response.returnContent().asString(), CheckReadingMO.class);
            }
            catch (IOException e)
            {
                throw new BergamotAPIException("Error calling Bergamot Monitoring API", e);
            }
        }

    }


    public GetReadingsByCheckCall callGetReadingsByCheck()
    {
        return new GetReadingsByCheckCall(this);
    }

}

