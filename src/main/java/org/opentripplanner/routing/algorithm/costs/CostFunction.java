package org.opentripplanner.routing.algorithm.costs;

import org.opentripplanner.routing.core.State;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public abstract class CostFunction {

    private static Map<String, Class<? extends CostFunction>> registeredImplementations = new HashMap<>();

    public static void register(String registeredName, Class<? extends CostFunction> implementation) {
        registeredImplementations.put(registeredName, implementation);
    }

    public static CostFunction getInstance(String registeredName) {
        CostFunction instance = null;
        try {
            Class<? extends CostFunction> implementingClass = registeredImplementations.get(registeredName);
            if (Objects.nonNull(implementingClass)) {
                instance = implementingClass.getConstructor().newInstance();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return instance;
    }

    public abstract double getCost(State state);

}
