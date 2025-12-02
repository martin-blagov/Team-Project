package use_case.display_individual_stat;

/**
 * The output boundary for the Individual Stats Use Case.
 */
public interface DisplayIndividualStatOutputBoundary {
    /**
     * Prepares the success view for the Individual Stats Use Case.
     * @param outputData the output data displaying stats
     */
    void presentView(DisplayIndividualStatOutputData outputData);
}
