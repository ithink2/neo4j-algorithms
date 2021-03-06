package com.graphaware.module.algo.generator;

import com.graphaware.common.util.SameTypePair;
import com.graphaware.module.algo.generator.config.BasicGeneratorConfig;
import com.graphaware.module.algo.generator.config.ErdosRenyiConfig;
import com.graphaware.module.algo.generator.config.GeneratorConfiguration;
import com.graphaware.module.algo.generator.node.SocialNetworkNodeCreator;
import com.graphaware.module.algo.generator.relationship.ErdosRenyiRelationshipGenerator;
import com.graphaware.module.algo.generator.relationship.SocialNetworkRelationshipCreator;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.test.TestGraphDatabaseFactory;
import org.neo4j.unsafe.batchinsert.BatchInserter;
import org.neo4j.unsafe.batchinsert.BatchInserters;

import java.io.IOException;
import java.util.List;

import static com.graphaware.common.util.IterableUtils.count;
import static org.junit.Assert.assertEquals;
import static org.neo4j.tooling.GlobalGraphOperations.at;

/**
 * Integration test for {@link com.graphaware.module.algo.generator.Neo4jGraphGenerator} and {@link com.graphaware.module.algo.generator.BatchGraphGenerator} with
 * {@link com.graphaware.module.algo.generator.relationship.ErdosRenyiRelationshipGenerator}.
 */
public class ErdosRenyiGeneratorTest {

    @Test
    public void shouldGenerateCorrectNumberOfNodesAndRelationships() throws Exception {
        assertUsingDatabase(100, 200);
        assertUsingDatabase(100, 300);
        assertUsingDatabase(100, 1000);
        assertUsingDatabase(100, 5);
        assertUsingDatabase(10, 11);
        assertUsingDatabase(10, 23);
        assertUsingDatabase(20, 190);

        assertUsingBatchInserter(100, 1000);
    }

    @Test(timeout = 60 * 1000)
    @Ignore
    public void shouldGenerateRelationshipsForLargeGraphInAReasonableAmountOfTime() {
        new ErdosRenyiRelationshipGenerator(new ErdosRenyiConfig(500_000, 10_000_000)).generateEdges();
    }

    @Test(timeout = 60 * 1000)
    @Ignore
    public void shouldGenerateRelationshipsForLargeGraphInAReasonableAmountOfTime2() {
        new ErdosRenyiRelationshipGenerator(new ErdosRenyiConfig(10000, 25_000_000)).generateEdges();
    }

    @Test(timeout = 5 * 60 * 1000)
    @Ignore
    public void shouldGenerateLargeGraphInAReasonableAmountOfTime() throws IOException {
        TemporaryFolder folder = new TemporaryFolder();
        folder.create();

        BatchInserter batchInserter = BatchInserters.inserter(folder.getRoot().getAbsolutePath());

        new BatchGraphGenerator(batchInserter).generateGraph(getGeneratorConfiguration(100_000, 5_000_000));

        folder.delete();
    }

    private void assertUsingDatabase(int numberOfNodes, int numberOfEdges) {
        GraphDatabaseService database = new TestGraphDatabaseFactory().newImpermanentDatabase();

        new Neo4jGraphGenerator(database).generateGraph(getGeneratorConfiguration(numberOfNodes, numberOfEdges));

        assertCorrectNumberOfNodesAndRelationships(database, numberOfNodes, numberOfEdges);

        database.shutdown();
    }

    private void assertUsingBatchInserter(int numberOfNodes, int numberOfEdges) throws IOException {
        TemporaryFolder folder = new TemporaryFolder();
        folder.create();

        BatchInserter batchInserter = BatchInserters.inserter(folder.getRoot().getAbsolutePath());

        new BatchGraphGenerator(batchInserter).generateGraph(getGeneratorConfiguration(numberOfNodes, numberOfEdges));

        GraphDatabaseService database = new GraphDatabaseFactory().newEmbeddedDatabase(folder.getRoot().getAbsolutePath());

        assertCorrectNumberOfNodesAndRelationships(database, numberOfNodes, numberOfEdges);

        database.shutdown();

        folder.delete();
    }

    private void assertCorrectNumberOfNodesAndRelationships(GraphDatabaseService database, int numberOfNodes, int numberOfEdges) {
        try (Transaction tx = database.beginTx()) {
            assertEquals(numberOfNodes, count(at(database).getAllNodes()));
            assertEquals(numberOfEdges, count(at(database).getAllRelationships()));

            tx.success();
        }
    }

    private GeneratorConfiguration getGeneratorConfiguration(int numberOfNodes, int numberOfEdges) {
        return new BasicGeneratorConfig(
                new ErdosRenyiRelationshipGenerator(new ErdosRenyiConfig(numberOfNodes, numberOfEdges)),
                SocialNetworkNodeCreator.getInstance(),
                SocialNetworkRelationshipCreator.getInstance()
        );
    }
}
