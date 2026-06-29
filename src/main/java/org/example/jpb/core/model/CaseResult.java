package org.example.jpb.core.model;

public record CaseResult(String caseName, Object expected, Object actual, boolean passed) {}
