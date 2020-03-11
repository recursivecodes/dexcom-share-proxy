package codes.recursive.dexcomproxy.job;

import codes.recursive.dexcomproxy.client.DexcomShareClient;
import codes.recursive.dexcomproxy.services.DataStore;
import io.micronaut.scheduling.annotation.Scheduled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;

@Singleton
public class KeepSessionsAliveJob {
    private static final Logger LOG = LoggerFactory.getLogger(KeepSessionsAliveJob.class);

    private final DataStore dataStore;
    private final DexcomShareClient dexcomShareClient;

    public KeepSessionsAliveJob(DataStore dataStore, DexcomShareClient dexcomShareClient) {
        this.dataStore = dataStore;
        this.dexcomShareClient = dexcomShareClient;
    }

    /*
        keep all sessions alive every 30 minutes -
        this will keep session tokens hot & ready
        for the next request
     */
    @Scheduled(fixedDelay = "30m")
    public void keepAlive() {
        for (String key : dataStore.sessionIds.keySet()) {
            String sessionId = dataStore.sessionIds.get(key);
            dexcomShareClient.getReadings( sessionId,10, 1 );
        }
        LOG.info("Kept " + dataStore.sessionIds.keySet().size() + " sessions alive");
    }
}
