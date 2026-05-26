@mainframe
Feature: NSLC menu access control

  A user without TCS access must be warned when selecting that option,
  instead of being allowed in.

  Scenario: Selecting option 3 without access shows a warning
    Given I am signed on to the mainframe
    When I select menu option "3"
    Then the mainframe warns "Your ID is not authorized for TCS"
