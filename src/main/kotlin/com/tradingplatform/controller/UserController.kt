package com.tradingplatform.controller

import OrderValidation
import UserValidation
import com.tradingplatform.model.*
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import com.tradingplatform.model.Users
import io.micronaut.http.MutableHttpResponse
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.QueryValue
import io.micronaut.json.tree.JsonObject
import java.lang.Integer.min
import kotlin.math.ceil
import kotlin.math.roundToInt


@Controller("/user")
class UserController {
    @Post(value = "/register", consumes = [MediaType.APPLICATION_JSON], produces = [MediaType.APPLICATION_JSON])
    fun register(@Body body: JsonObject): MutableHttpResponse<*>? {
   
        val errorList = arrayListOf<String>()
        val errorResponse = mutableMapOf<String, MutableList<String>>();
        var fieldLists= arrayListOf<String>("userName","firstName","lastName","phoneNumber","email")

        //Check for empty fields
        for (field in fieldLists) {
            if (UserValidation().isFieldExists(field, body)) {
                errorList.add("Enter the $field field")
                errorResponse["error"] = errorList
            }else if(body[field]==null||!body[field].isString)
            {
                errorList.add("$field Data type not in valid format")
                errorResponse["error"] = errorList
            }
        }
        
        if (errorList.isNotEmpty()) {
            return HttpResponse.badRequest(errorResponse)
        }

        val userName = body["userName"].stringValue
        val phoneNumber = body["phoneNumber"].stringValue
        val firstName = body["firstName"].stringValue
        val lastName = body["lastName"].stringValue
        val email = body["email"].stringValue

        //Validations on all
        UserValidation().isEmailValid(errorList, email)
        UserValidation().isPhoneValid(errorList, phoneNumber)
        UserValidation().isUserNameValid(errorList, userName)
        UserValidation().isNameValid(errorList, firstName)
        UserValidation().isNameValid(errorList, lastName)


        if (errorList.isNotEmpty()) {
            errorResponse["error"] = errorList
            return HttpResponse.badRequest(errorResponse)
        }

        Users[userName] = User(
            firstName = firstName,
            lastName = lastName,
            userName = userName,
            email = email.lowercase(),
            phoneNumber = phoneNumber
        )

        var okResponse = HashMap<String, String>()
        okResponse.put("message", "User Registered successfully")

        return HttpResponse.ok(okResponse)
    }

    @Post(value = "/{user_name}/order")
    fun createOrder(@Body body: JsonObject, @QueryValue user_name: String): Any {
        val response = mutableMapOf<String, Any>();
        val errorList = arrayListOf<String>()
        var fieldLists = arrayListOf<String>("quantity", "type", "price")
        for (field in fieldLists) {
            if (OrderValidation().isFieldExists(field, body)) {
                errorList.add("Enter the $field field")
            }
        }
        if (errorList.isNotEmpty())
            return HttpResponse.badRequest(response)
        if (body["quantity"]==null || !body["quantity"].isNumber || ceil(body["quantity"].doubleValue).roundToInt()!=body["quantity"].intValue) {
            errorList.add("Quantity is not valid")
            response["error"] = errorList
        }
        if (body["price"]==null || !body["price"].isNumber || ceil(body["price"].doubleValue).roundToInt()!=body["price"].intValue) {
            errorList.add("Price is not valid")
            response["error"] = errorList

        }
        if (body["type"]==null || !body["type"].isString || (body["type"].stringValue!="SELL" && body["type"].stringValue!="BUY")) {
            errorList.add("Order Type is not valid")
            response["error"] = errorList
        }
        if (errorList.isNotEmpty())
            return HttpResponse.badRequest(response)

        var quantity = body["quantity"].intValue
        val type = body["type"].stringValue
        var price = body["price"].intValue
        val esopType = if (body["esopType"] !== null) body["esopType"].stringValue else "NORMAL"




        OrderValidation().isValidAmount(errorList, quantity, "quantity")
        OrderValidation().isValidAmount(errorList, price,"price")
        OrderValidation().isValidEsopType(errorList, esopType)


        if (errorList.isNotEmpty())
        {
            response["error"]=errorList
            return HttpResponse.badRequest(response)
        }


        var newOrder : Order? = null
        if(Users.containsKey(user_name)){
            val user = Users[user_name]!!
            if(type == "BUY"){
                if(quantity * price > user.wallet_free) errorList.add("Insufficient funds in wallet")
                else{
                    user.wallet_free -= quantity * price
                    user.wallet_locked += quantity * price
                    newOrder = Order("BUY", quantity, price, user_name, esopNormal)
                    user.orders.add(newOrder.id)

                }
            }
            else if(type == "SELL"){
                if (esopType == "PERFORMANCE") {
                    if (quantity > user.perf_free) {
                        errorList.add("Insufficient Performance ESOPs in inventory")
                    }
                    else {
                        user.perf_locked += quantity
                        user.perf_free -= quantity
                        newOrder = Order("SELL", quantity, price, user_name, esopPerformance)
                        user.orders.add(newOrder.id)

                    }
                } else if (esopType == "NORMAL") {
                    if (quantity > user.inventory_free) {
                        errorList.add("Insufficient Normal ESOPs in inventory")
                    }
                    else {
                        user.inventory_locked += quantity
                        user.inventory_free -= quantity
                        newOrder = Order("SELL", quantity, price, user_name, esopNormal)
                        user.orders.add(newOrder.id)
                    }
                }
            }
            else
                errorList.add("Invalid type given")
        } else  {
            errorList.add("User doesn't exist")
        }

        response["error"] = errorList
        if (errorList.isNotEmpty()) {
            return HttpResponse.badRequest(response)
        }

        response["orderId"] = newOrder!!.id.first
        response["quantity"] = quantity
        response["type"] = type
        response["price"] = price

        return HttpResponse.ok(response)
    }

    @Get(value = "/{userName}/accountInformation")
    fun getAccountInformation(@PathVariable(name="userName")userName: String): MutableHttpResponse<out Any?>? {

        var response = mutableMapOf<String,Any>();
        var errorList = arrayListOf<String>()
        UserValidation().isUserExists(errorList,userName)
        if(errorList.isNotEmpty()){
            response["error"] = errorList;

            return HttpResponse.badRequest(response)
        }

        val user = Users[userName]

        var wallet = mutableMapOf<String, Int>()
        wallet["free"] = user!!.wallet_free
        wallet["locked"] = user!!.wallet_locked

        var inventory = mutableListOf<InventoryOutput>()

        val normal_inventory = InventoryOutput(user!!.inventory_free, user!!.inventory_locked, "NORMAL")
        val performance_inventory = InventoryOutput(user!!.perf_free, user!!.perf_locked, "PERFORMANCE")

        inventory.add(normal_inventory)
        inventory.add(performance_inventory)
        response["firstName"] = user!!.firstName
        response["lastName"] = user!!.lastName
        response["phoneNumber"] = user!!.phoneNumber
        response["email"] = user!!.email
        response["wallet"] = wallet
        response["inventory"] = inventory

        return HttpResponse.ok(response)
    }

    @Post(value = "/{userName}/inventory")
    fun addInventory(@Body body: JsonObject, @PathVariable(name="userName")userName: String): MutableHttpResponse<out Any>? {
        val response = mutableMapOf<String, MutableList<String>>();
        var errorList = arrayListOf<String>()
        var msg = mutableListOf<String>()

        UserValidation().isUserExists(errorList,userName)
        if(body["quantity"]==null)
        {
            errorList.add("Quantity is missing")
            response["error"] = errorList;
            return HttpResponse.badRequest(response)
        }
        if( !body["quantity"].isNumber || ceil(body["quantity"].doubleValue).roundToInt()!=body["quantity"].intValue) {

            errorList.add("Quantity data type is invalid")
        }
        else
            OrderValidation().isValidQuantity(errorList,body["quantity"].intValue)

        if(body["type"]!=null &&( !body["type"].isString||body["type"].stringValue!="PERFORMANCE"))
        {
            errorList.add("ESOP type is invalid")
        }

        response["error"] = errorList;
        if(errorList.isNotEmpty()) return HttpResponse.badRequest(response)

        if(body["type"]!=null) {
            Users[userName]?.perf_free = Users[userName]?.perf_free?.plus(body["quantity"].intValue)!!
            msg.add("${body["quantity"].intValue} Performance ESOPs added to account")
        }
        else {
            Users[userName]?.inventory_free = Users[userName]?.inventory_free?.plus(body["quantity"].intValue)!!
            msg.add("${body["quantity"].intValue} ESOPs added to account")
        }
        response["message"]=msg
        return HttpResponse.ok(response)
    }


    @Post(value = "/{userName}/wallet")
    fun addWallet(@Body body: JsonObject, @PathVariable userName:String): MutableHttpResponse<out Any>? {
        val responseMap= HashMap<String,String>()
        val errorList = arrayListOf<String>()
        val response = mutableMapOf<String, MutableList<String>>();
        UserValidation().isUserExists(errorList,userName)

        if(body["amount"]==null)
        {
            errorList.add("Enter amount field")
            response["error"] = errorList;
            return HttpResponse.badRequest(response)
        }
        if(!body["amount"].isNumber || (ceil(body["amount"].doubleValue).roundToInt()!=body["amount"].intValue))
            errorList.add("Amount data type is invalid")
        else
            OrderValidation().isValidAmount(errorList,body["amount"].intValue, "amount")

        response["error"] = errorList;
        if(errorList.isNotEmpty()) return HttpResponse.badRequest(response)

        Users[userName]?.wallet_free = Users[userName]?.wallet_free?.plus(body["amount"].intValue)!!
        responseMap["message"] = "${body["amount"].intValue} added to account"
        return HttpResponse.ok(responseMap)
    }

    @Get(value = "/{userName}/order")
    fun getOrder(@QueryValue userName: String): Any? {
        val errorList = arrayListOf<String>()
        val response = mutableMapOf<String, MutableList<String>>();
        var userOrders: HashMap<Int,OrderHistory> = hashMapOf()
        UserValidation().isUserExists(errorList,userName)
        if(errorList.isNotEmpty())
        {
            response["error"]=errorList
            return HttpResponse.badRequest(response)
        }
        val userOrderIds = Users[userName]!!.orders
        for(orderId in userOrderIds){

            if(CompletedOrders.containsKey(orderId)){

                if(!userOrders.contains(orderId.first))
                {
                    var currOrder=CompletedOrders[orderId];
                    var partialOrderHistory: OrderHistory= OrderHistory(currOrder!!.type,currOrder.qty,currOrder.price,currOrder.createdBy, currOrder.esop_type)
                    partialOrderHistory.id=currOrder.id.first
                    partialOrderHistory.status="filled"
                    partialOrderHistory.timestamp=currOrder.timestamp
                    partialOrderHistory.filledQty=currOrder.filledQty
                    partialOrderHistory.filled=currOrder.filled

                    userOrders.put(partialOrderHistory.id,partialOrderHistory)
                }
                else
                {
                    var currOrder=userOrders[orderId.first]
                    var exisitingOrder=CompletedOrders[orderId];

                    currOrder!!.filledQty+=exisitingOrder!!.filledQty
                    currOrder!!.filled.addAll(exisitingOrder.filled)
                }
            }
        }

        for(order in BuyOrders){
            if(userName == order.createdBy){

                var orderId=order.id



                if(!userOrders.contains(orderId.first))
                {
                    var currOrder=order
                    var partialOrderHistory : OrderHistory= OrderHistory(currOrder!!.type,currOrder.qty,currOrder.price,currOrder.createdBy, currOrder.esop_type)
                    partialOrderHistory.id=currOrder.id.first
                    partialOrderHistory.status="unfilled"
                    partialOrderHistory.timestamp=currOrder.timestamp
                    partialOrderHistory.filledQty=currOrder.filledQty
                    partialOrderHistory.filled=currOrder.filled

                    userOrders[partialOrderHistory.id] = partialOrderHistory
                }
                else
                {
                    var currOrder=userOrders[orderId.first]
                    var exisitingOrder=order

                    if(currOrder!!.status=="filled")
                        currOrder.status="partially filled"

                    currOrder!!.filledQty+=exisitingOrder!!.filledQty
                    currOrder!!.filled.addAll(exisitingOrder.filled)
                }
            }
        }

        for(order in SellOrders){
            if(userName == order.createdBy){
                var orderId=order.id

                if(!userOrders.contains(orderId.first))
                {
                    var currOrder=order

                    var partialOrderHistory : OrderHistory= OrderHistory(currOrder!!.type,currOrder.qty,currOrder.price,currOrder.createdBy, currOrder.esop_type)
                    partialOrderHistory.id=currOrder.id.first
                    partialOrderHistory.status="unfilled"
                    partialOrderHistory.timestamp=currOrder.timestamp
                    partialOrderHistory.filledQty=currOrder.filledQty
                    partialOrderHistory.filled=currOrder.filled

                    userOrders[partialOrderHistory.id] = partialOrderHistory
                }
                else
                {
                    var currOrder=userOrders[orderId.first]
                    var exisitingOrder=order

                    if(currOrder!!.status=="filled")
                        currOrder.status="partially filled"

                    currOrder!!.filledQty+=exisitingOrder!!.filledQty
                    currOrder!!.filled.addAll(exisitingOrder.filled)
                }

            }
        }

        var listOfOrders: MutableCollection<OrderHistory> = userOrders.values
        return HttpResponse.ok(listOfOrders)

    }


}


data class OrderHistory constructor(val type : String, val qty: Int, val price : Int, val createdBy : String, val esop_type: Int) {
    var status = "unfilled"
    var filled = ArrayList<PriceQtyPair>()
    var id: Int = 0
    var timestamp = System.currentTimeMillis()
    var filledQty = 0
}

