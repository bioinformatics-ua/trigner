package pt.ua.tm.trigner.evaluation;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 16/01/13
 * Time: 11:07
 * To change this template use File | Settings | File Templates.
 */
public class Evaluation {

    private int tp, fp, fn;

    public Evaluation() {
        this.tp = 0;
        this.fp = 0;
        this.fn = 0;
    }

    public int getTP() {
        return tp;
    }

    public int getFP() {
        return fp;
    }

    public int getFN() {
        return fn;
    }

    public void setTP(int tp) {
        this.tp = tp;
    }

    public void setFP(int fp) {
        this.fp = fp;
    }

    public void setFN(int fn) {
        this.fn = fn;
    }

    public void addTP() {
        this.tp++;
    }

    public void addFP() {
        this.fp++;
    }

    public void addFN() {
        this.fn++;
    }

    public double getPrecision() {
        if (tp == 0 && fp == 0) {
            return 0.0;
        }
        return ((double) (tp) / (double) (tp + fp)) * 100.0;
    }

    public double getRecall() {
        if (tp == 0 && fn == 0) {
            return 0.0;
        }
        return ((double) (tp) / (double) (tp + fn)) * 100.0;
    }

    public double getF1() {
        double p = getPrecision();
        double r = getRecall();

        if (p == 0 && r == 0) {
            return 0.0;
        }

        return 2.0 * ((p * r) / (p + r));
    }
}
