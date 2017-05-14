package plu.red.reversi.core.easing;

public class PolynomialEasing implements Easing {

    float power;
    EaseType type;

    public PolynomialEasing(float power, EaseType type) {
        this.power = power;
        this.type = type;
    }

    public float ease(float t,float b , float c, float d) {
        switch (type) {
            case EASE_IN:
                return c * (float)Math.pow(t / d, power) + b;
            case EASE_OUT:
                if(power % 2 == 1)
                    return c * ((float)Math.pow(t / d - 1, power) + 1) + b;
                else
                    return -c * ((float)Math.pow(t / d - 1, power) - 1) + b;
            case EASE_IN_OUT:
                if ((t/=d/2) < 1) return c / 2 * (float)Math.pow(t, power) + b;
                return -c/2*((float)Math.pow(t - 2, power) + 2) + b;
        }

        // should not happen.
        return 0;
    }
}
