Feature: Kogito Serverless workflow - Play To Win

  Background:
    Given HTTP server timeout is 15000 ms
    Given Knative event consumer timeout is 20000 ms
    Given Knative broker default is running
    Given variable user is "krisv"

  Scenario: Create Http server
    Given HTTP server "kogito-test"
    Given create Kubernetes service kogito-test

  Scenario: Create prize event consumer service
    Given Knative service port 8081
    Given create Knative event consumer service prize-service
    Given create Knative trigger prize-service-trigger on service prize-service with filter on attributes
      | type   | prizes |

  Scenario: Create new participant event
    Given Knative event data: {"username": "${user}"}
    Then send Knative event
      | type            | participants |
      | source          | /test/participant |
      | subject         | New participant |
      | id              | citrus:randomUUID() |

  Scenario: Verify score service called
    Given HTTP server "kogito-test"
    When receive POST /scores
    Then HTTP response body: {"result": true}
    And HTTP response header: Content-Type="application/json"
    And send HTTP 200 OK

  Scenario: Verify get employee details service called
    Given HTTP server "kogito-test"
    When receive GET /employee/${user}
    Then HTTP response body: {"firstName": "Kris", "lastName":"Verlaenen", "address":"Castle 12, Belgium"}
    And HTTP response header: Content-Type="application/json"
    And send HTTP 200 OK

  Scenario: Verify prize won event
    Given Knative service "prize-service"
    Then expect Knative event data
    """
      {
        "username": "${user}",
        "result": true,
        "firstName": "Kris",
        "lastName":"Verlaenen",
        "address":"Castle 12, Belgium",
        "prize": "Lego Mindstorms"
      }
    """
    And verify Knative event
      | id              | @ignore@ |
      | type            | prizes |
      | source          | /process/PlayToWin_ServerlessWorkflow |

  Scenario: Remove resources
    Given delete Kubernetes service prize-service
    Given delete Kubernetes service kogito-test
