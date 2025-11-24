package use_case.underperforming;

import use_case.underperforming.UnderperformingInputData;
import use_case.underperforming.UnderperformingOutputData;

public interface UnderperformingInputBoundary {
    UnderperformingOutputData execute(UnderperformingInputData inputData);
}
