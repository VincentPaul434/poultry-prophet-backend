package com.poultryprophet.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Bound from {@code poultry.report.*}. */
@ConfigurationProperties(prefix = "poultry.report")
public class ReportProperties {

    private String outputDir = "./reports";

    public String getOutputDir() {
        return outputDir;
    }

    public void setOutputDir(String outputDir) {
        this.outputDir = outputDir;
    }
}
