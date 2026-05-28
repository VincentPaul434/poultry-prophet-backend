package com.poultryprophet.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration-driven analytics tuning (SDD 2.1: "Weights are configuration-driven").
 * Bound from the {@code poultry.analytics.*} section of application.yml.
 */
@ConfigurationProperties(prefix = "poultry.analytics")
public class AnalyticsProperties {

    private Weights weights = new Weights();
    private int windowDays = 14;
    private Map<String, Double> stageTargetTemp = new HashMap<>();

    public Weights getWeights() {
        return weights;
    }

    public void setWeights(Weights weights) {
        this.weights = weights;
    }

    public int getWindowDays() {
        return windowDays;
    }

    public void setWindowDays(int windowDays) {
        this.windowDays = windowDays;
    }

    public Map<String, Double> getStageTargetTemp() {
        return stageTargetTemp;
    }

    public void setStageTargetTemp(Map<String, Double> stageTargetTemp) {
        this.stageTargetTemp = stageTargetTemp;
    }

    /** Target temperature for a lifecycle stage, falling back to a sane default. */
    public double targetTempFor(String stageName) {
        if (stageName == null) {
            return 28.0;
        }
        return stageTargetTemp.getOrDefault(stageName.toLowerCase(), 28.0);
    }

    public static class Weights {
        private double temperature = 0.30;
        private double mortality = 0.35;
        private double feed = 0.20;
        private double water = 0.15;

        public double total() {
            return temperature + mortality + feed + water;
        }

        public double getTemperature() {
            return temperature;
        }

        public void setTemperature(double temperature) {
            this.temperature = temperature;
        }

        public double getMortality() {
            return mortality;
        }

        public void setMortality(double mortality) {
            this.mortality = mortality;
        }

        public double getFeed() {
            return feed;
        }

        public void setFeed(double feed) {
            this.feed = feed;
        }

        public double getWater() {
            return water;
        }

        public void setWater(double water) {
            this.water = water;
        }
    }
}
