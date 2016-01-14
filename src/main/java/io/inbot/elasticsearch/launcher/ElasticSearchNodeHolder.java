package io.inbot.elasticsearch.launcher;

import java.io.File;
import org.apache.commons.lang3.RandomStringUtils;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;

/**
 * This class provides some convenient logic around creating and managing the lifecycle of embedded elasticsearch
 * instances.
 */
public class ElasticSearchNodeHolder {
    private Node elasticSearchNode = null;
    private final Settings settings;
    private final boolean addShutDownHook;

    /**
     * Creates a new node holder based on the elasticsearch settings provided.
     *
     * @param settings
     *            this will determine how the node behaves
     * @param addShutDownHook
     *            if true, the node will be destroyed on shutdown using a Runtime.getRuntime().addShutdownHook
     */
    public ElasticSearchNodeHolder(Settings settings, boolean addShutDownHook) {
        this.settings = settings;
        this.addShutDownHook = addShutDownHook;
    }

    public Node node() {
        if(elasticSearchNode==null) {
            throw new IllegalStateException("node has not been started yet");
        }
        return elasticSearchNode;
    }

    /**
     * Use this method to fire up a test node with some sensible defaults.
     *
     * @param indexDirectory
     *            place where it should store its index and log files. Tip: put this in target and randomize the index
     *            dir name. This way it gets cleaned up when you run mvn clean and you get a clean index everytime you
     *            spin up a new node.
     * @param port
     *            which http port is the service going to be listening on
     * @param addShutDownHook
     *            should it shutdown when the jvm dies.
     * @return the created holder
     */
    public static ElasticSearchNodeHolder createWithDefaults(String indexDirectory, int port, boolean addShutDownHook) {
        String logDir = indexDirectory + "/logs";
        File file = new File(indexDirectory);
        // ensure the directory actually exists
        file.mkdirs();

        Settings settings = Settings.settingsBuilder().put("path.home", indexDirectory).put("name", "test-node-" + RandomStringUtils.randomAlphabetic(5))
                .put("cluster.name", "linko-dev-cluster-" + RandomStringUtils.randomAlphabetic(5)).put("index.gateway.type", "none")
                .put("discovery.zen.ping.multicast.ping.enabled", "false").put("discovery.zen.ping.multicast.enabled", "false")
                .put("cluster.routing.allocation.node_concurrent_recoveries", "8").put("bootstrap.mlockall", "true").put("indices.fielddata.cache.size", "30%")
                .put("index.number_of_replicas", 0).put("path.data", indexDirectory).put("path.logs", logDir).put("foreground", "true").put("http.port", port)
                .put("http.cors.enabled", "true").put("script.inline", "on")
                .put("http.cors.allow-origin", "/https?:\\/\\/(localhost|kibana.*\\.linko\\.io)(:[0-9]+)?/")
                .put("cluster.routing.allocation.disk.watermark.high", "1000mb") // nice to have a bit higher in
                                                                                 // production :-)
                .put("cluster.routing.allocation.disk.watermark.low", "100mb").build();
        return new ElasticSearchNodeHolder(settings, addShutDownHook);
    }

    /**
     * Create and start the node and register shutdown hook if needed. Blocks until the node is started and reports a
     * green state.
     */
    public void start() {
        NodeBuilder nodeBuilder = NodeBuilder.nodeBuilder().settings(settings);
        elasticSearchNode = nodeBuilder.build();

        if(addShutDownHook) {
            registerShutdownHook();
        }
        elasticSearchNode.start();

        // wait until the shards are ready
        elasticSearchNode.client().admin().cluster().prepareHealth().setWaitForGreenStatus().execute().actionGet();
    }

    public void registerShutdownHook() {
        // register a shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                elasticSearchNode.close();
            }
        });
    }

    /**
     * Close the node manually. Useful if you are not using the shutdown hook.
     */
    public void close() {
        elasticSearchNode.close();
    }
}