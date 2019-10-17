Build: mvn clean install

Run: java -jar com.test.money-transfer-1.0-SNAPSHOT.jar

Endpoints:
- List all accounts: GET http://localhost:8081/account/list
- Get an account: GET http://localhost:8081/account?id=3
- Add an account: PUT http://localhost:8081/account ("amount" in form parameters)
- Delete an account: DELETE http://localhost:8081/account?id=2
- Update an account: POST http://localhost:8081/account ("id" and "amount" in form parameters)
- Transfer money: POST http://localhost:8081/account/transfer (Parameters: "fromId", "toId", "amount")