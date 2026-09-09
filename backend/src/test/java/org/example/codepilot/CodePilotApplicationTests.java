package org.example.codepilot;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class CodePilotApplicationTests {

    @Test
    void applicationClassLoads() {
        assertDoesNotThrow(() -> Class.forName("org.example.codepilot.CodePilotApplication"));
    }

}
