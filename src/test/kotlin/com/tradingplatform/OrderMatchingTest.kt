package com.tradingplatform

import com.tradingplatform.controller.OrderController
import com.tradingplatform.model.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class OrderMatchingTest {

    @BeforeEach
    fun `Remove all the Users and Orders`() {
        CompletedOrders.clear()
        BuyOrders.clear()
        SellOrders.clear()
        Users.clear()
    }




    @Test
    fun `Check a single buy order`() {
        //Arrange
        val user1 = User("", "", "", "", "atul_1")
        Users[user1.userName] = user1
        user1.walletFree = 100
        val objectOfOrderController = OrderController()

        //Act
        objectOfOrderController.orderHandler(user1.userName, "BUY", 1, 20, "NORMAL")

        //Assert
        Assertions.assertEquals(80, user1.walletFree)
        Assertions.assertEquals(80, user1.walletFree)
    }

    @Test
    fun `Check buy order satisfied partially by sell order`() {
        //Arrange
        val user1 = User("", "", "", "", "atul_1")
        Users[user1.userName] = user1
        user1.walletFree = 100

        val user2 = User("", "", "", "", "atul_2")
        Users[user2.userName] = user2
        user2.inventoryFree = 10

        //Act
        val objectOfOrderController = OrderController()
        objectOfOrderController.orderHandler(user1.userName, "BUY", 5, 20)
        objectOfOrderController.orderHandler(user2.userName, "SELL", 2, 20)


        //Assert
        Assertions.assertEquals(0, user1.walletFree)
        Assertions.assertEquals(2, user1.inventoryFree)
        Assertions.assertEquals(0, user1.inventoryLocked)
        Assertions.assertEquals(60, user1.walletLocked)

        Assertions.assertEquals(39, user2.walletFree)
        Assertions.assertEquals(8, user2.inventoryFree)
        Assertions.assertEquals(0, user2.inventoryLocked)
        Assertions.assertEquals(0, user2.walletLocked)
    }


    @Test
    fun `Check buy order satisfied partially by performance esops of sell order`() {
        //Arrange
        val user1 = User("", "", "", "", "atul_1")
        Users[user1.userName] = user1
        user1.walletFree = 100

        val user2 = User("", "", "", "", "atul_2")
        Users[user2.userName] = user2
        user2.perfFree = 10

        //Act
        val objectOfOrderController = OrderController()
        objectOfOrderController.orderHandler(user2.userName, "SELL", 2, 20, "PERFORMANCE")
        objectOfOrderController.orderHandler(user1.userName, "BUY", 5, 20)


        //Assert
        Assertions.assertEquals(0, user1.walletFree)
        Assertions.assertEquals(2, user1.inventoryFree)
        Assertions.assertEquals(0, user1.inventoryLocked)
        Assertions.assertEquals(60, user1.walletLocked)

        Assertions.assertEquals(40, user2.walletFree)
        Assertions.assertEquals(8, user2.perfFree)
        Assertions.assertEquals(0, user2.perfLocked)
        Assertions.assertEquals(0, user2.walletLocked)
    }


    @Test
    fun `Check sell order satisfied partially by buy order`() {
        //Arrange
        val user1 = User("", "", "", "", "atul_1")
        Users[user1.userName] = user1
        user1.inventoryFree = 11

        val user2 = User("", "", "", "", "atul_2")
        Users[user2.userName] = user2
        user2.walletFree = 100


        //Act
        val objectOfOrderController = OrderController()
        objectOfOrderController.orderHandler(user1.userName, "SELL", 10, 20)
        objectOfOrderController.orderHandler(user2.userName, "BUY", 5, 20)


        //Assert
        Assertions.assertEquals(98, user1.walletFree)
        Assertions.assertEquals(1, user1.inventoryFree)
        Assertions.assertEquals(5, user1.inventoryLocked)
        Assertions.assertEquals(0, user1.walletLocked)

        Assertions.assertEquals(0, user2.walletFree)
        Assertions.assertEquals(5, user2.inventoryFree)
        Assertions.assertEquals(0, user2.inventoryLocked)
        Assertions.assertEquals(0, user2.walletLocked)
    }

    @Test
    fun `Check sell order of performance esops satisfied partially by buy order`() {
        //Arrange
        val user1 = User("", "", "", "", "atul_1")
        Users[user1.userName] = user1
        user1.walletFree = 100

        val user2 = User("", "", "", "", "atul_2")
        Users[user2.userName] = user2
        user2.perfFree = 10

        //Act
        val objectOfOrderController = OrderController()
        objectOfOrderController.orderHandler(user1.userName, "BUY", 5, 20)
        objectOfOrderController.orderHandler(user2.userName, "SELL", 2, 20, "PERFORMANCE")


        //Assert
        Assertions.assertEquals(0, user1.walletFree)
        Assertions.assertEquals(2, user1.inventoryFree)
        Assertions.assertEquals(0, user1.inventoryLocked)
        Assertions.assertEquals(60, user1.walletLocked)

        Assertions.assertEquals(40, user2.walletFree)
        Assertions.assertEquals(8, user2.perfFree)
        Assertions.assertEquals(0, user2.perfLocked)
        Assertions.assertEquals(0, user2.walletLocked)
    }


    @Test
    fun `Check a single sell order of performance esops`() {
        //Arrange
        val user1 = User("", "", "", "", "kcsp")
        Users[user1.userName] = user1
        user1.inventoryFree = 40
        user1.perfFree = 40
        user1.walletFree = 100

        //Act

        OrderController().orderHandler(user1.userName, "SELL", 10, 100, "PERFORMANCE")

        //Assert
        Assertions.assertEquals(40, user1.inventoryFree)
        Assertions.assertEquals(0, user1.inventoryLocked)
        Assertions.assertEquals(100, user1.walletFree)
        Assertions.assertEquals(0, user1.walletLocked)
        Assertions.assertEquals(30, user1.perfFree)
        Assertions.assertEquals(10, user1.perfLocked)
    }


    @Test
    fun `Check buy order after a sell order`() {
        //Arrange
        val user1 = User("", "", "", "", "atul_1")
        Users[user1.userName] = user1
        user1.walletFree = 100
        val user2 = User("", "", "", "", "atul_2")
        Users[user2.userName] = user2
        user2.inventoryFree = 10
        val objectOfOrderController = OrderController()
        objectOfOrderController.orderHandler(user2.userName, "SELL", 2, 20)


        //Act
        objectOfOrderController.orderHandler(user1.userName, "BUY", 2, 20)


        //Assert
        Assertions.assertEquals(60, user1.walletFree)
        Assertions.assertEquals(2, user1.inventoryFree)
        Assertions.assertEquals(0, user1.inventoryLocked)
        Assertions.assertEquals(0, user1.walletLocked)

        Assertions.assertEquals(39, user2.walletFree)
        Assertions.assertEquals(8, user2.inventoryFree)
        Assertions.assertEquals(0, user2.inventoryLocked)
        Assertions.assertEquals(0, user2.walletLocked)
    }

    @Test
    fun `Check sell order after a buy order`() {
        //Arrange
        val user1 = User("", "", "", "", "atul_1")
        Users[user1.userName] = user1
        user1.walletFree = 100
        val user2 = User("", "", "", "", "atul_2")
        Users[user2.userName] = user2
        user2.inventoryFree = 10
        val objectOfOrderController = OrderController()
        objectOfOrderController.orderHandler(user1.userName, "BUY", 2, 20)

        //Act
        objectOfOrderController.orderHandler(user2.userName, "SELL", 2, 20)

        //Assert
        Assertions.assertEquals(60, user1.walletFree)
        Assertions.assertEquals(2, user1.inventoryFree)
        Assertions.assertEquals(0, user1.inventoryLocked)
        Assertions.assertEquals(0, user1.walletLocked)

        Assertions.assertEquals(39, user2.walletFree)
        Assertions.assertEquals(8, user2.inventoryFree)
        Assertions.assertEquals(0, user2.inventoryLocked)
        Assertions.assertEquals(0, user2.walletLocked)
    }


    @Test
    fun `Check match of sell order with 2 buy order , where high price buy order is placed after low price buy`() {
        //Arrange
        val user1 = User("", "", "", "", "atul_1")
        Users[user1.userName] = user1
        user1.walletFree = 100
        val user2 = User("", "", "", "", "atul_2")
        Users[user2.userName] = user2
        user2.inventoryFree = 10
        val objectOfOrderController = OrderController()
        objectOfOrderController.orderHandler(user1.userName, "BUY", 2, 20)
        objectOfOrderController.orderHandler(user1.userName, "BUY", 2, 30)


        //Act
        objectOfOrderController.orderHandler(user2.userName, "SELL", 2, 20)

        //Assert
        Assertions.assertEquals(40, user1.walletLocked)
        Assertions.assertEquals(20, user1.walletFree)
        Assertions.assertEquals(2, user1.inventoryFree)
        Assertions.assertEquals(0, user1.inventoryLocked)

        Assertions.assertEquals(39, user2.walletFree)
        Assertions.assertEquals(8, user2.inventoryFree)
        Assertions.assertEquals(0, user2.inventoryLocked)
        Assertions.assertEquals(0, user2.walletLocked)
    }

    @Test
    fun `Check match of sell order with 2 buy order , where low price buy order is placed after high price buy`() {
        //Arrange
        val user1 = User("", "", "", "", "atul_1")
        Users[user1.userName] = user1
        user1.walletFree = 100
        val user2 = User("", "", "", "", "atul_2")
        Users[user2.userName] = user2
        user2.inventoryFree = 10
        val objectOfOrderController = OrderController()
        objectOfOrderController.orderHandler(user1.userName, "BUY", 2, 30)
        objectOfOrderController.orderHandler(user1.userName, "BUY", 2, 20)


        //Act
        objectOfOrderController.orderHandler(user2.userName, "SELL", 2, 20)

        //Assert
        Assertions.assertEquals(40, user1.walletLocked)
        Assertions.assertEquals(20, user1.walletFree)
        Assertions.assertEquals(2, user1.inventoryFree)
        Assertions.assertEquals(0, user1.inventoryLocked)

        Assertions.assertEquals(39, user2.walletFree)
        Assertions.assertEquals(8, user2.inventoryFree)
        Assertions.assertEquals(0, user2.inventoryLocked)
        Assertions.assertEquals(0, user2.walletLocked)
    }


    @Test
    fun `Check match of sell order with 2 buy order , both buy at same price`() {
        //Arrange
        val user1 = User("", "", "", "", "atul_1")
        Users[user1.userName] = user1
        user1.walletFree = 100
        val user2 = User("", "", "", "", "atul_2")
        Users[user2.userName] = user2
        user2.inventoryFree = 10
        val objectOfOrderController = OrderController()
        objectOfOrderController.orderHandler(user1.userName, "BUY", 2, 20)
        objectOfOrderController.orderHandler(user1.userName, "BUY", 2, 20)


        //Act
        objectOfOrderController.orderHandler(user2.userName, "SELL", 2, 20)


        //Assert
        Assertions.assertEquals(40, user1.walletLocked)
        Assertions.assertEquals(20, user1.walletFree)
        Assertions.assertEquals(2, user1.inventoryFree)
        Assertions.assertEquals(0, user1.inventoryLocked)

        Assertions.assertEquals(39, user2.walletFree)
        Assertions.assertEquals(8, user2.inventoryFree)
        Assertions.assertEquals(0, user2.inventoryLocked)
        Assertions.assertEquals(0, user2.walletLocked)
    }


    @Test
    fun `Check match of buy order with 2 sell order , first sell at low price than second sell price`() {
        //Arrange
        val user1 = User("", "", "", "", "atul_1")
        Users[user1.userName] = user1
        user1.inventoryFree = 10
        val user2 = User("", "", "", "", "atul_2")
        Users[user2.userName] = user2
        user2.walletFree = 100
        val objectOfOrderController = OrderController()
        objectOfOrderController.orderHandler(user1.userName, "SELL", 2, 20)
        objectOfOrderController.orderHandler(user1.userName, "SELL", 2, 30)


        //Act
        objectOfOrderController.orderHandler(user2.userName, "BUY", 2, 20)


        //Assert
        Assertions.assertEquals(0, user1.walletLocked)
        Assertions.assertEquals(39, user1.walletFree)
        Assertions.assertEquals(6, user1.inventoryFree)
        Assertions.assertEquals(2, user1.inventoryLocked)

        Assertions.assertEquals(60, user2.walletFree)
        Assertions.assertEquals(2, user2.inventoryFree)
        Assertions.assertEquals(0, user2.inventoryLocked)
        Assertions.assertEquals(0, user2.walletLocked)
    }



    @Test
    fun `Check match of buy order with 2 sell order , first sell at higher price than second sell price`() {
        //Arrange
        val user1 = User("", "", "", "", "atul_1")
        Users[user1.userName] = user1
        user1.inventoryFree = 10
        val user2 = User("", "", "", "", "atul_2")
        Users[user2.userName] = user2
        user2.walletFree = 100
        val objectOfOrderController = OrderController()
        objectOfOrderController.orderHandler(user1.userName, "SELL", 2, 30)
        objectOfOrderController.orderHandler(user1.userName, "SELL", 2, 20)

        //Act
        objectOfOrderController.orderHandler(user2.userName, "BUY", 2, 20)

        //Assert
        Assertions.assertEquals(0, user1.walletLocked)
        Assertions.assertEquals(39, user1.walletFree)
        Assertions.assertEquals(6, user1.inventoryFree)
        Assertions.assertEquals(2, user1.inventoryLocked)

        Assertions.assertEquals(60, user2.walletFree)
        Assertions.assertEquals(2, user2.inventoryFree)
        Assertions.assertEquals(0, user2.inventoryLocked)
        Assertions.assertEquals(0, user2.walletLocked)
    }

    @Test
    fun `Check match of buy order with 2 sell order , both sell order at same price`() {
        //Arrange
        val user1 = User("", "", "", "", "atul_1")
        Users[user1.userName] = user1
        user1.inventoryFree = 10
        val user2 = User("", "", "", "", "atul_2")
        Users[user2.userName] = user2
        user2.walletFree = 100
        val objectOfOrderController = OrderController()
        objectOfOrderController.orderHandler(user1.userName, "SELL", 2, 20)
        objectOfOrderController.orderHandler(user1.userName, "SELL", 2, 20)

        //Act
        objectOfOrderController.orderHandler(user2.userName, "BUY", 2, 20)

        //Assert
        Assertions.assertEquals(0, user1.walletLocked)
        Assertions.assertEquals(39, user1.walletFree)
        Assertions.assertEquals(6, user1.inventoryFree)
        Assertions.assertEquals(2, user1.inventoryLocked)

        Assertions.assertEquals(60, user2.walletFree)
        Assertions.assertEquals(2, user2.inventoryFree)
        Assertions.assertEquals(0, user2.inventoryLocked)
        Assertions.assertEquals(0, user2.walletLocked)
    }


}

