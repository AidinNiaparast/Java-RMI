package client;

/**
 * Created by Aidin on 7/23/2017.
 */

@ImplementedBy("server.A")
public interface ARemote extends RemoteInterface{
    @RemoteReturn
    public BRemote addTo(BRemote addTo);
}

