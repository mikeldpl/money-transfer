### money-transfer

RESTful API for money transfer between accounts.

Use cases:
Create money transfer and approve it. Sequence diagram: https://drive.google.com/file/d/1_oRwRatUM2Q8hhl_xLM5CTkobuGUbTlj/view?usp=sharing

Endpoints:
* `GET /money-transfer/1/accounts`
* `POST /money-transfer/1/accounts`
* `GET /money-transfer/1/accounts/:id`
* `GET /money-transfer/1/accounts/:account_id/transfers`
* `POST /money-transfer/1/accounts/:account_id/transfers`
* `GET /money-transfer/1/accounts/:account_id/transfers/:id`
* `GET /money-transfer/1/accounts/:account_id/transfers/:transfer_id/actions`
* `POST /money-transfer/1/accounts/:account_id/transfers/:transfer_id/actions`
* `GET /money-transfer/1/accounts/:account_id/transfers/:transfer_id/actions/:id`



Features:
* Make money transactions between accounts;
* Clean up idle not approved transfers;

###### Skipped features (to keep it simple): HATEOAS, Pagination, Authentication.

---
Build and create native and not native images: `./mvnw clean install`

Run container : `docker run -it -p 4567:4567 --rm --name=mikeldpl.temp mikeldpl/moneytransfer`

Build native image: `docker build -t mikeldpl/moneytransfer-native -f docker/Dockerfile.native --build-arg JAR_FILE=money-transfer-1.0.jar .`
Run container within native app: `docker run -it -p 4567:4567 --rm --name=mikeldpl.temp.native mikeldpl/moneytransfer-native`
