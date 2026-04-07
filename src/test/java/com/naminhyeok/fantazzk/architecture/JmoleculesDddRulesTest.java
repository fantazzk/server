package com.naminhyeok.fantazzk.architecture;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.jmolecules.archunit.JMoleculesDddRules;

@AnalyzeClasses(packages = "com.naminhyeok.fantazzk")
class JmoleculesDddRulesTest {
    @ArchTest
    static final ArchRule dddRules = JMoleculesDddRules.all();
}
