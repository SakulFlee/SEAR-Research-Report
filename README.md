# SEAR Research Report

This repository holds the content of our SEAR research report and all connected assets.

## Main research question

What are the potential risks and threads of utilizing Java Dependency-Injections in an application?

## Secondary research questions

- What is Java Dependency-Injection?
- What kind of Java Dependency-Injection are there?
  'wanted dependency injection' (Plugin-System)
  'abuse of wanted Dependency-Injection'
  'unwanted Dependency-Injection' [-> only mention]
- What are the positive benefits vs. negative impacts?
- How does Java Dependency-Injection work?
  - Implemented Plugin-System
  - External dependencies

## Hypothesis

Increasing security measures of a packaged application and plugin-system will reduce potential threads and risks of Java Dependency-Injection.

### Definition

X: Security Measures of the packaged application and plugin-system
Y: Threads and Risks of Java Dependency-Injection
P: Java + source of plugins/libraries/dependencies
I: User-error (e.g. manual overwrite of dependencies or plugins; OUTSIDE of the system)

### Capture hypothesis

P: Security knowledge
X: System/Environment security
I: "User (security) Error/Issue"
Y: Potential damage / risk / threads

Security Knowledge → System Security → Potential damage / risk / threads
                                     ↑
                        "User (security) Error/Issue"

### How to capture hypothesis

Showing difference in secure applications and insecure applications.
How and which potential risks are possible?

### Method

- Experiment
- Empirical
- Qualitative
