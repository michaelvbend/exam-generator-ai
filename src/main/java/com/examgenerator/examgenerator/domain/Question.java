package com.examgenerator.examgenerator.domain;

import java.util.List;

public record Question(String question, List<String> options, String answer) {
}
