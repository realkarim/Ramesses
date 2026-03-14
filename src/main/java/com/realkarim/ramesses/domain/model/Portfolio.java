package com.realkarim.ramesses.domain.model;

public class Portfolio {
    private final double budget;
    private final double ethHoldings;
    private final PortfolioStep step;

    public Portfolio(double budget, double ethHoldings, PortfolioStep step) {
        this.budget = budget;
        this.ethHoldings = ethHoldings;
        this.step = step;
    }

    public static Portfolio initial(double budget) {
        return new Portfolio(budget, 0.0, PortfolioStep.BUY_NEXT);
    }

    public double getBudget() { return budget; }
    public double getEthHoldings() { return ethHoldings; }
    public PortfolioStep getStep() { return step; }
}
