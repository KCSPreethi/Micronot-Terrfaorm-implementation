package com.tradingplatform.controller

import com.tradingplatform.model.*
import com.tradingplatform.validations.maxLimitForInventory
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.restassured.specification.RequestSpecification
import org.hamcrest.Matchers
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@MicronautTest
class OrderControllerTest
{

    @BeforeEach
    fun `Remove all the Users and Orders`() {
        CompletedOrders.clear()
        BuyOrders.clear()
        SellOrders.clear()
        Users.clear()
    }
    @Test
    fun `check if order request has missing quantity,type and price fields`(spec: RequestSpecification) {
        spec.`when`()
            .header("Content-Type", "application/json")
            .body("{}")
            .post("/user/username/order")
            .then()
            .statusCode(400).and().body("error", Matchers.contains("Enter the quantity field","Enter the type field","Enter the price field"))
    }
    @Test
    fun `check if order request has missing type and price fields`(spec: RequestSpecification) {
        spec.`when`()
            .header("Content-Type", "application/json")
            .body("""{"quantity":1}""")
            .post("/user/username/order")
                .then()
            .statusCode(400).and().body("error", Matchers.contains("Enter the type field","Enter the price field"))
    }

    @Test
    fun `check if order request quantity is integer`(spec: RequestSpecification) {
        spec.`when`()
            .header("Content-Type", "application/json")
            .body("""
                {
                    "quantity": "dkfg",
                    "type": "BUY",
                    "price": 20
                }
            """.trimIndent())
            .post("/user/username/order")
            .then()
            .statusCode(400).and().body("error", Matchers.contains("Quantity is not valid"))
    }
    @Test
    fun `check if order request type is string`(spec: RequestSpecification) {
        spec.`when`()
            .header("Content-Type", "application/json")
            .body("""
                {
                    "quantity": 1,
                    "type": 1,
                    "price": 20
                }
            """.trimIndent())
            .post("/user/username/order")
            .then()
            .statusCode(400).and().body("error", Matchers.contains("Order Type is not valid"))
    }
    @Test
    fun `Check if error message is returned if order is valid but user not exist `(spec: RequestSpecification) {
        spec.`when`()
            .header("Content-Type", "application/json")
            .body("""
                {
                    "quantity": 1,
                    "type": "BUY",
                    "price": 20
                }
            """.trimIndent())
            .post("/user/atul/order")
            .then()
            .statusCode(400).and().body("error",Matchers.contains("User doesn't exist"))

    }

    @Test
    fun `Check if successful order is placed if order request is valid`(spec: RequestSpecification) {
        val user=User("Atul","Tiwri","+91999999999","atul@sahaj.ai","atul")
        Users[user.userName]=user
        user.walletFree=100

        spec.`when`()
            .header("Content-Type", "application/json")
            .body("""
                {
                    "quantity": 1,
                    "type": "BUY",
                    "price": 20
                }
            """.trimIndent())
            .post("/user/${user.userName}/order")
            .then()
            .statusCode(200).and()
            .body("orderId",Matchers.equalTo(0))
            .body("quantity",Matchers.equalTo(1))
            .body("type",Matchers.equalTo("BUY"))
            .body("price",Matchers.equalTo(20))
    }


    @Test
    fun `Check if error is returned if free wallet balance is insufficent`(spec: RequestSpecification) {
        val user=User("Atul","Tiwri","+91999999999","atul@sahaj.ai","atul")
        Users[user.userName]=user
        user.walletFree=10

        spec.`when`()
            .header("Content-Type", "application/json")
            .body("""
                {
                    "quantity": 1,
                    "type": "BUY",
                    "price": 20
                }
            """.trimIndent())
            .post("/user/${user.userName}/order")
            .then()
            .statusCode(400).and()
            .body("error",Matchers.contains("Insufficient funds in wallet"))
    }



    @Test
    fun `Check if error is returned if inventory is insufficent`(spec: RequestSpecification) {
        val user=User("Atul","Tiwri","+91999999999","atul@sahaj.ai","atul")
        Users[user.userName]=user


        spec.`when`()
            .header("Content-Type", "application/json")
            .body("""
                {
                    "quantity": 1,
                    "type": "SELL",
                    "price": 20
                }
            """.trimIndent())
            .post("/user/${user.userName}/order")
            .then()
            .statusCode(400).and()
            .body("error",Matchers.contains("Insufficient Normal ESOPs in inventory"))
    }


    @Test
    fun `Check if error is returned if performance inventory is insufficent`(spec: RequestSpecification) {
        val user=User("Atul","Tiwri","+91999999999","atul@sahaj.ai","atul")
        Users[user.userName]=user


        spec.`when`()
            .header("Content-Type", "application/json")
            .body("""
                {
                    "quantity": 100,
                    "type": "SELL",
                    "price": 20,
                    "esopType" : "PERFORMANCE"
                }
            """.trimIndent())
            .post("/user/${user.userName}/order")
            .then()
            .statusCode(400).and()
            .body("error",Matchers.contains("Insufficient Performance ESOPs in inventory"))
    }



    @Test
    fun `check if error message is added if price exceed specified limit`(spec: RequestSpecification) {
        spec.`when`()
            .header("Content-Type", "application/json")
            .body("""
                {
                    "quantity": 1,
                    "type": "BUY",
                    "price": 10000000000
                }
            """.trimIndent())
            .post("/user/ll/order")
            .then()
            .statusCode(400).and().body("error",Matchers.contains("Price is not valid"))

    }

    @Test
    fun `check if error message is added if quantity exceed specified limit`(spec: RequestSpecification) {
        spec.`when`()
            .header("Content-Type", "application/json")
            .body("""
                {
                    "quantity": 1000000000,
                    "type": "BUY",
                    "price": 20
                }
            """.trimIndent())
            .post("/user/ll/order")
            .then()
            .statusCode(400).and().body("error",Matchers.contains("Quantity is not valid. Range between 1 and ${maxLimitForInventory}"))
    }

    @Test
    fun `check if error message is returned when order type is invalid`(spec: RequestSpecification) {
        spec.`when`()
            .header("Content-Type", "application/json")
            .body("""
                {
                    "quantity": 1,
                    "type": "BUYYYY",
                    "price": 20
                }
            """.trimIndent())
            .post("/user/ll/order")
            .then()
            .statusCode(400).and().body("error",Matchers.contains("Order Type is not valid"))
    }
}