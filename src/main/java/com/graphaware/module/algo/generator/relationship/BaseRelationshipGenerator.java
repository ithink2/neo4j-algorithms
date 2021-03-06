package com.graphaware.module.algo.generator.relationship;

import com.graphaware.common.util.SameTypePair;
import com.graphaware.module.algo.generator.config.InvalidConfigException;
import com.graphaware.module.algo.generator.config.RelationshipGeneratorConfig;

import java.util.List;

/**
 * Abstract base-class for {@link RelationshipGenerator} implementations.
 *
 * @param <T> type of accepted configuration.
 */
public abstract class BaseRelationshipGenerator<T extends RelationshipGeneratorConfig> implements RelationshipGenerator<T> {

    private final T configuration;

    /**
     * Construct a new relationship generator.
     *
     * @param configuration to base the generation on
     */
    protected BaseRelationshipGenerator(T configuration) {
        this.configuration = configuration;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumberOfNodes() {
        return configuration.getNumberOfNodes();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<SameTypePair<Integer>> generateEdges() throws InvalidConfigException {
        if (!configuration.isValid()) {
            throw new InvalidConfigException("The supplied config is not valid");
        }

        return doGenerateEdges();
    }

    /**
     * Perform the actual edge generation.
     *
     * @return generated edges as pair of node IDs that should be connected.
     */
    protected abstract List<SameTypePair<Integer>> doGenerateEdges();

    /**
     * Get the configuration of this generator.
     *
     * @return configuration.
     */
    protected T getConfiguration() {
        return configuration;
    }
}
