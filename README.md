# Transaction Application
Description: This is a spring boot application which exposes apis to manage transactions

## API List
BaseUrl: http://localhost:8080
### Save a new transaction:
#### Endpoint:  `POST /transactions`
#### Request Body
```
    {
        "customer_id": 10001,
        "transaction_time": "2023-12-12 10:08:02",
        "quantity": 5,
        "product_code": "PRODUCT_003"
    }
```
#### Response Body
```
    {
        "quantity": 5,
        "transaction_id": 1,
        "customer_id": 10001,
        "transaction_time": "2023-12-12 10:08:02",
        "product_code": "PRODUCT_003"
    }
```

### Save bulk transaction through json file:
##### (saves valid transactions and ignores invalid ones)
#### Endpoint:  `POST /transactions/bulk-upload`
#### Request
`request body type: form-data`

`Key: file (type file)`

`Value: A valid json file as form-data containing transaction details (sample file present as transactions.json under /src/main/resources)`
#### Response Body
```
[
    {
        "quantity": 5,
        "transaction_id": 1,
        "customer_id": 10001,
        "transaction_time": "2023-12-12 10:08:02",
        "product_code": "PRODUCT_003"
    },
    {
        ...
    },
    ...
]   
```
### Get total transaction cost per customer :
##### (fetches total transaction cost per customer for all customers if request param is not sent)
#### Endpoint:  `GET /transactions/customers-cost`
#### Request param 
`customer_id(optional): 10001 (if customer_id is not passed, data will be shown for all customers)`
#### Response Body
```
[
    {
        "customer_id": 10001,
        "customer_name": "Tony Stark",
        "email": "tony.stark@gmail.com",
        "location": "Australia",
        "total_transaction_cost": 1050.0
    },
    {
        ...
    },
    ...
]
```

### Get total transactions cost per product
##### (fetches total transaction cost per product for all products if request param is not sent)
#### Endpoint: `Get /transactions/products-cost`
#### Request param
`product_code(optional): PRODUCT_001 (if product_code is not passed, data will be shown for all products)`
#### Response Body
```
[
    {
        "product_code": "PRODUCT_001",
        "cost": 50.0,
        "status": "Active",
        "total_transaction_cost": 50.0
    },
    {
        ...
    },
]
```

### Get transaction count for location
##### (fetches transaction count for a location provided in path)
#### Endpoint: `Get /transactions/count/{location}`
#### Path param
`location: Australia`
#### Response Body
```
    {
        "transactionCount": 5
    }

```

## Steps to run the application
* Clone the repository. Application uses maven as the build tool. 
* This application requires java 17 to build and run it. If java 17 is not installed you can install openjdk17 following this url: https://java.tutorials24x7.com/blog/how-to-install-openjdk-17-on-mac
* Run `mvn clean install` from the base directory of this repo to build the code. This will run the unit test cases as well. Additionally `mvn clean test` can be run to execute unit cases explicitly.
* The resulting jar file would be present in target directory
* Run `java -jar target/transactions-0.0.1-SNAPSHOT.jar` to run the application
* Application runs on port `8080`


## Additional Information about the application
* Spring security is added to the application. Details for login are `username: admin, password: password`. You would need to use Authorization token `Authourization: Basic YWRtaW46cGFzc3dvcmQ=` in request headers to access the apis from postman
* Application uses controller advice TransactionControllerAdvice to handle all exceptions
* Get total transaction cost per customer/product APIs provide flexibility to fetch data for a particular customer/product by adding query param (customer_id/product_code) to filter the response
* Get transaction count for Australia API is generalized to work for any location by passing location as path parameter
* Validations are added to make sure transaction_time, quantity and product_code are present in the request body and also to validate if customer_id and product_code are valid and present in customer and product table respectively
* H2 file based database is used to persist transactions. Files `tabcorp.mv.db` and `tabcorp.trace.db` will be created in the project root folder when application is started to store the transactions
* schema.sql and data.sql scripts are added in resources directory which will be automatically picked and executed at startup for database schema creation and adding the sample data to the Customer, Product and Transaction table
* Bulk upload (binary data) API requires request body as "form-data" and file type param "file" having value of a well-formed json file. A sample file is present in src/main/resource/transactions.json. All the valid transactions in the file will be saved and transactions failing validations will be ignored

**_NOTE:_** You would need to use Authorization header for making post call to save transaction using postman or curl command. A sample transaction is added in db at startup with transaction_id=1 if you do not wish to make post call and directly want to test get API. Token details: `Authourization: Basic YWRtaW46cGFzc3dvcmQ=`
