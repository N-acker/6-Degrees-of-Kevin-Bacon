*** Settings ***
Library           Collections
Library           RequestsLibrary
Test Timeout      30 seconds

Suite Setup    Create Session    localhost    http://localhost:8080

*** Test Cases ***
addActorPass
    ${headers}=    Create Dictionary    Content-Type=application/json
    ${params}=    Create Dictionary    name=Jennifer Lawrence    actorId=nm0000044
    ${resp}=    PUT On Session    localhost    /api/v1/addActor   json=${params}    headers=${headers}    expected_status=200

addActorFail
    ${headers}=    Create Dictionary    Content-Type=application/json
    ${params}=    Create Dictionary    name=Zendaya
    ${resp}=    PUT On Session    localhost    /api/v1/addActor    json=${params}    headers=${headers}    expected_status=400

addMoviePass
    ${headers}=    Create Dictionary    Content-Type=application/json
    ${params}=    Create Dictionary    name=Hunger Games   movieId=nm0009836
    ${resp}=    PUT On Session    localhost    /api/v1/addMovie    json=${params}    headers=${headers}    expected_status=200

addMovieFail
    ${headers}=    Create Dictionary    Content-Type=application/json
    ${params}=    Create Dictionary    name=Barnyard
    ${resp}=    PUT On Session    localhost    /api/v1/addMovie    json=${params}    headers=${headers}    expected_status=400

addRelationshipPass
    ${headers}=    Create Dictionary    Content-Type=application/json
    ${params}=    Create Dictionary    actorId=nm0000044    movieId=nm0009836
    ${resp}=    PUT On Session    localhost    /api/v1/addRelationship    json=${params}    headers=${headers}    expected_status=200

addRelationshipFail
    ${headers}=    Create Dictionary    Content-Type=application/json
    ${params}=    Create Dictionary    actorId=Jennifer Lawrence    name=nm0009836
    ${resp}=    PUT On Session    localhost    /api/v1/addRelationship    json=${params}    headers=${headers}    expected_status=404

getActorPass
    ${headers}=    Create Dictionary    Content-Type=application/json
    ${params}=    Create Dictionary    actorId=nm0000044
    ${resp}=    GET On Session    localhost    /api/v1/getActor    params=${params}    headers=${headers}
    Should Be Equal As Strings    ${resp.status_code}    200
    ${json}=    Evaluate    ${resp.json()}
    Should Contain    ${json}    Jennifer Lawrence

getActorFail
    ${headers}=    Create Dictionary    Content-Type=application/json
    ${params}=    Create Dictionary    actorId=non_existent_id
    ${resp}=    GET On Session    localhost    /api/v1/getActor    params=${params}    headers=${headers}
    Should Be Equal As Strings    ${resp.status_code}    404

getMoviePass
    ${headers}=    Create Dictionary    Content-Type=application/json
    ${params}=    Create Dictionary    movieId=nm0009836
    ${resp}=    GET On Session    localhost    /api/v1/getMovie    params=${params}    headers=${headers}
    Should Be Equal As Strings    ${resp.status_code}    200
    ${json}=    Evaluate    ${resp.json()}
    Should Contain    ${json}    Hunger Games

getMovieFail
    ${headers}=    Create Dictionary    Content-Type=application/json
    ${params}=    Create Dictionary    movieId=non_existent_id
    ${resp}=    GET On Session    localhost    /api/v1/getMovie    params=${params}    headers=${headers}
    Should Be Equal As Strings    ${resp.status_code}    404

hasRelationshipPass
    ${headers}=    Create Dictionary    Content-Type=application/json
    ${params}=    Create Dictionary    actorId=nm0000044    movieId=nm0009836
    ${resp}=    GET On Session    localhost    /api/v1/hasRelationship    params=${params}    headers=${headers}
    Should Be Equal As Strings    ${resp.status_code}    200
    ${json}=    Evaluate    ${resp.json()}
    Should Contain    ${json}    true

hasRelationshipFail
    ${headers}=    Create Dictionary    Content-Type=application/json
    ${params}=    Create Dictionary    actorId=nm0000044    movieId=non_existent_id
    ${resp}=    GET On Session    localhost    /api/v1/hasRelationship    params=${params}    headers=${headers}
    Should Be Equal As Strings    ${resp.status_code}    404

computeBaconNumberPass
    ${headers}=    Create Dictionary    Content-Type=application/json
    ${params}=    Create Dictionary    actorId=nm0000044
    ${resp}=    GET On Session    localhost    /api/v1/computeBaconNumber    params=${params}    headers=${headers}
    Should Be Equal As Strings    ${resp.status_code}    200
    ${json}=    Evaluate    ${resp.json()}
    Should Contain    ${json}    baconNumber:

computeBaconNumberFail
    ${headers}=    Create Dictionary    Content-Type=application/json
    ${params}=    Create Dictionary    actorId=non_existent_id
    ${resp}=    GET On Session    localhost    /api/v1/computeBaconNumber    params=${params}    headers=${headers}
    Should Be Equal As Strings    ${resp.status_code}    404

computeBaconPathPass
    ${headers}=    Create Dictionary    Content-Type=application/json
    ${params}=    Create Dictionary    actorId=nm0000044
    ${resp}=    GET On Session    localhost    /api/v1/computeBaconPath    params=${params}    headers=${headers}
    Should Be Equal As Strings    ${resp.status_code}    200
    ${json}=    Evaluate    ${resp.json()}
    Should Contain    ${json}    baconPath:

computeBaconPathFail
    ${headers}=    Create Dictionary    Content-Type=application/json
    ${params}=    Create Dictionary    actorId=non_existent_id
    ${resp}=    GET On Session    localhost    /api/v1/computeBaconPath    params=${params}    headers=${headers}
    Should Be Equal As Strings    ${resp.status_code}    404