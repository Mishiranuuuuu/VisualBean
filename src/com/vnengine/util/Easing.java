package com.vnengine.util;

public enum Easing {
    LINEAR,
    EASE_IN_QUAD,
    EASE_OUT_QUAD,
    EASE_IN_OUT_QUAD,
    EASE_IN_CUBIC,
    EASE_OUT_CUBIC,
    EASE_IN_OUT_CUBIC,
    EASE_OUT_BOUNCE,
    EASE_OUT_ELASTIC,
    EASE_IN_OUT_SINE;

    public float apply(float t) {
        switch (this) {
            case LINEAR:
                return t;

            case EASE_IN_QUAD:
                return t * t;
            case EASE_OUT_QUAD:
                return t * (2 - t);
            case EASE_IN_OUT_QUAD:
                return t < .5 ? 2 * t * t : -1 + (4 - 2 * t) * t;

            case EASE_IN_CUBIC:
                return t * t * t;
            case EASE_OUT_CUBIC:
                return (--t) * t * t + 1;
            case EASE_IN_OUT_CUBIC:
                return t < .5 ? 4 * t * t * t : (t - 1) * (2 * t - 2) * (2 * t - 2) + 1;

            case EASE_OUT_BOUNCE:
                float n1 = 7.5625f;
                float d1 = 2.75f;
                if (t < 1 / d1) {
                    return n1 * t * t;
                } else if (t < 2 / d1) {
                    return n1 * (t -= 1.5 / d1) * t + 0.75f;
                } else if (t < 2.5 / d1) {
                    return n1 * (t -= 2.25 / d1) * t + 0.9375f;
                } else {
                    return n1 * (t -= 2.625 / d1) * t + 0.984375f;
                }

            case EASE_OUT_ELASTIC:
                float c4 = (2 * (float) Math.PI) / 3;
                return t == 0 ? 0
                        : t == 1 ? 1 : (float) Math.pow(2, -10 * t) * (float) Math.sin((t * 10 - 0.75) * c4) + 1;

            case EASE_IN_OUT_SINE:
                return -(float) Math.cos(Math.PI * t) / 2 + 0.5f;

            default:
                return t;
        }
    }
}
