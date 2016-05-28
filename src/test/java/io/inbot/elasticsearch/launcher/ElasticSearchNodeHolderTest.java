package io.inbot.elasticsearch.launcher;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.commons.lang3.RandomUtils;
import org.elasticsearch.cluster.health.ClusterHealthStatus;
import org.testng.annotations.Test;

@Test
public class ElasticSearchNodeHolderTest {

    public void shouldLaunchElasticSearch() {
        ElasticSearchNodeHolder nodeholder = ElasticSearchNodeHolder.createWithDefaults("target/index_"+RandomUtils.nextInt(0, Integer.MAX_VALUE), 9299, false);
        nodeholder.start();
        assertThat(nodeholder.node().client().admin().cluster().prepareHealth().get().getStatus()).as("es should be green immediately after start() returns").isEqualTo(ClusterHealthStatus.GREEN);
    }
}
