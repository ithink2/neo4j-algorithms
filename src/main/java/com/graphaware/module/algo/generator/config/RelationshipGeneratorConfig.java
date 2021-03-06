package com.graphaware.module.algo.generator.config;

/**
 * Configuration for a {@link com.graphaware.module.algo.generator.relationship.RelationshipGenerator}.
 */
public interface RelationshipGeneratorConfig {

    /**
     * Get the number of nodes that need to be created before the relationships can be generated and created.
     *
     * @return number of nodes for this configuration.
     */
    int getNumberOfNodes();

    /**
     * Returns true iff the config is valid.
     *
     * @return true if the config is valid.
     */
    boolean isValid();
}
