package use_case.starting_lineup;


public class StartingLineupInteractor implements StartingLineupInputBoundary {

    private final StartingLineupOutputBoundary outputBoundary;

    public StartingLineupInteractor(StartingLineupOutputBoundary outputBoundary) {
        this.outputBoundary = outputBoundary;
    }

    @Override
    public void execute() {
        outputBoundary.presentLineup(new StartingLineupOutputData());
    }
}