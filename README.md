### money-transfer

RESTful API for money transfers between accounts.

Use cases:
Create money transfer and approve. Sequence diagram: https://drive.google.com/file/d/1_oRwRatUM2Q8hhl_xLM5CTkobuGUbTlj/view?usp=sharing

###### Skipped features (to keep it simple): HATEOAS, Pagination, Authentication.

---
Build and create native and not native images: `./mvnw clean install`

Run container : `docker run -it -p 4567:4567 --rm --name=mikeldpl.temp mikeldpl/moneytransfer`

Run container within native app: `docker run -it -p 4567:4567 --rm --name=mikeldpl.temp mikeldpl/moneytransfer-native`
