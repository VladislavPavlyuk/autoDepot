package com.example.autodepot.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DriverCreateDTO {
    private String name;

    @JsonAlias("license_year")
    private Integer licenseYear;

    @JsonAlias("license_categories")
    @JsonDeserialize(using = LicenseCategoriesDeserializer.class)
    private List<String> licenseCategories;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getLicenseYear() {
        return licenseYear;
    }

    public void setLicenseYear(Integer licenseYear) {
        this.licenseYear = licenseYear;
    }

    public List<String> getLicenseCategories() {
        return licenseCategories;
    }

    public void setLicenseCategories(List<String> licenseCategories) {
        this.licenseCategories = licenseCategories;
    }

    /** Can take array or single string. */
    public static class LicenseCategoriesDeserializer extends JsonDeserializer<List<String>> {
        @Override
        public List<String> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            switch (p.currentToken()) {
                case VALUE_STRING:
                    String s = p.getText();
                    return s == null || s.isBlank() ? new ArrayList<>() : Collections.singletonList(s.trim().toUpperCase());
                case START_ARRAY:
                    List<String> list = new ArrayList<>();
                    while (p.nextToken() != com.fasterxml.jackson.core.JsonToken.END_ARRAY) {
                        if (p.currentToken() == com.fasterxml.jackson.core.JsonToken.VALUE_STRING) {
                            String v = p.getText();
                            if (v != null && !v.isBlank()) {
                                String cat = v.trim().toUpperCase();
                                if (!list.contains(cat)) list.add(cat);
                            }
                        }
                    }
                    return list;
                default:
                    return new ArrayList<>();
            }
        }
    }
}
