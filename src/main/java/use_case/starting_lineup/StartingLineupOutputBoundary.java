package use_case.starting_lineup;

public interface StartingLineupOutputBoundary {

    /**
     * Present starting lineup to user.
     *
     * @param outputData the output data containing the starting lineup information.
     */
    void presentLineup(StartingLineupOutputData outputData);
}
