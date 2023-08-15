*** Settings ***
Library           Collections
Library           RequestsLibrary
Test Timeout      30 seconds

Suite Setup    Create Session    localhost    http://localhost:8080

*** Test Cases ***
addAuthorPass
    ${headers}=    Create Dictionary    Content-Type=application/json
    ${params}=    Create Dictionary    author=George Orwell    authorId=nm1
    ${resp}=    PUT On Session    localhost    /api/book/addAuthor    json=${params}    headers=${headers}    expected_status=200

addAuthorFail
    ${headers}=    Create Dictionary    Content-Type=application/json
    ${params}=    Create Dictionary    name=Devin
    ${resp}=    PUT On Session    localhost    /api/book/addAuthor    json=${params}    headers=${headers}    expected_status=400

addTitlePass
    ${headers}=    Create Dictionary    Content-Type=application/json
    ${params}=    Create Dictionary    title=1984    movieId=tt1
    ${resp}=    PUT On Session    localhost    /api/book/addTitle    json=${params}    headers=${headers}    expected_status=200

addTitleFail
    ${headers}=    Create Dictionary    Content-Type=application/json
    ${params}=    Create Dictionary    name=Animal Farm    movieId=tt2
    ${resp}=    PUT On Session    localhost    /api/book/addTitle    json=${params}    headers=${headers}    expected_status=400

addBookPass
    ${headers}=    Create Dictionary    Content-Type=application/json
    ${params}=    Create Dictionary    author=George Orwell    title=1984
    ${resp}=    PUT On Session    localhost    /api/book/addBook    json=${params}    headers=${headers}    expected_status=200

addBookFail
    ${headers}=    Create Dictionary    Content-Type=application/json
    ${params}=    Create Dictionary    author=George Orwell    name=Animal Farm
    ${resp}=    PUT On Session    localhost    /api/book/addBook    json=${params}    headers=${headers}    expected_status=400

printBookPass
    ${headers}=    Create Dictionary    Content-Type=application/json
    ${params}=    Create Dictionary    author=George Orwell    title=1984
    ${resp}=    GET On Session    localhost    /api/book/printBook    params=${params}    headers=${headers}    expected_status=200
    Dictionary Should Contain Value    ${resp.json()}    George Orwell wrote 1984