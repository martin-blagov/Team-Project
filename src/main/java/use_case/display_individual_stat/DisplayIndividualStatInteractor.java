package use_case.display_individual_stat;

public class DisplayIndividualStatInteractor implements DisplayIndividualStatInputBoundary {
    private final DisplayIndividualStatOutputBoundary presenter;

    public DisplayIndividualStatInteractor(DisplayIndividualStatOutputBoundary presenter) {
        this.presenter = presenter;
    }

    public void execute() {
        DisplayIndividualStatOutputData outputData= new DisplayIndividualStatOutputData();
        presenter.presentView(outputData);
    }
}
