CREATE TABLE COMPANY {
  COMPANY_ID INTEGER PRIMARY KEY UNIQUE,
  COMPANY_NAME VARCHAR(160) UNIQUE,
  COMPANY_ADDRESS VARCHAR(240)
}

CREATE TABLE PRODUCT {
  PRODUCT_ID INTEGER PRIMARY KEY NOT NULL,
  PRODUCT_NAME VARCHAR(210) UNIQUE NOT NULL
}
