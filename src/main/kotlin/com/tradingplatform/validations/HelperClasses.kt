package com.tradingplatform.validations

import com.tradingplatform.data.UserRepo
import com.tradingplatform.model.User
import io.micronaut.json.tree.JsonObject

const val maxLimitForWallet = 10000000
const val maxLimitForInventory = 100000000

class UserValidation {
    private val emailRegex =
        "([a-zA-Z0-9]+([+._-]?[a-zA-z0-9])*)[@]([a-zA-Z]+([-]?[a-zA-z0-9])+[.])+([a-zA-Z]+([-]?[a-zA-z0-9])+)"
    private val userNameRegex = "([a-zA-Z]+[(a-zA-z0-9)|_]*){3,}"
    private val nameRegex = "^[a-zA-z ]*\$"
    private val phoneNumberRegex = "^[+]+[0-9]{1,3}[0-9]{10}\$"


    private fun isEmailAsPerRegex(email: String): List<String> {
        if (!(email.isNotEmpty() && emailRegex.toRegex().matches(email))) {
            return listOf("Invalid email format")
        }
        return emptyList()
    }

    private fun isEmailInSpecifiedLength(email: String): List<String> {
        val errorList = arrayListOf<String>()
        val parts = email.split("@")
        val subDomains = parts[1].split(".")
        if (subDomains.size > 4 || parts[0].length > 64 || parts[1].length > 255 || subDomains[subDomains.size - 1].length < 2) {
            return listOf("Invalid email format")
        }
        for (subDomain in subDomains) {
            if (subDomain.length > 63) {
                return listOf("Invalid email format")
            }
        }
        return errorList
    }

    fun isEmailValid(email: String): List<String> {
        val errorList = arrayListOf<String>()
        errorList.addAll(isEmailAsPerRegex(email))
        if (errorList.isNotEmpty()) return errorList
        errorList.addAll(isEmailInSpecifiedLength(email))
        if (errorList.isNotEmpty()) return errorList
        errorList.addAll(isEmailUnique(email))
        return errorList
    }

    private fun isEmailUnique(email: String): List<String> {
        val errorList = arrayListOf<String>()
        for (user in UserRepo.users) {
            if (user.value.email == email) {
                errorList.add("email is already registered")
            }
        }
        return errorList
    }

    fun isPhoneValid(list: ArrayList<String>, phoneNumber: String): Boolean {
        if (!(phoneNumber.isNotEmpty() && phoneNumberRegex.toRegex().matches(phoneNumber))) {
            list.add("Invalid phoneNumber format")
            return false
        } else if (!isPhoneUnique(phoneNumber)) {
            list.add("phoneNumber already registered")
            return false
        }
        return true
    }

    private fun isPhoneUnique(phoneNumber: String): Boolean {
        for (user in UserRepo.users) {
            if (user.value.phoneNumber == phoneNumber) {
                return false
            }
        }
        return true
    }


    fun isUserNameValid(list: ArrayList<String>, userName: String): Boolean {
        if (!(userName.isNotEmpty() && userNameRegex.toRegex().matches(userName))) {
            list.add("Invalid Username format")
            return false
        } else if (!isUnameUnique(userName)) {
            list.add("Username already registered")
            return false
        }
        return true
    }

    private fun isUnameUnique(userName: String): Boolean {
        for (user in UserRepo.users) {
            if (user.value.userName == userName) {
                return false
            }
        }
        return true
    }


    fun isNameValid(list: ArrayList<String>, name: String): Boolean {
        if (!(name.isNotEmpty() && nameRegex.toRegex().matches(name))) {
            list.add("Invalid Name format")
            return false
        }
        return true
    }

    fun isFieldExists(fieldName: String, body: JsonObject): Boolean {
        return body[fieldName] == null
    }

}


class OrderValidation {
    fun isValidAmount(list: ArrayList<String>, amount: Int): Boolean {
        if (amount <= 0) {
            list.add("Enter a positive amount")
            return false
        } else if (amount > maxLimitForWallet) {

            list.add("Enter amount between 0 to $maxLimitForWallet")
            return false
        }
        return true

    }

    fun isValidQuantity(list: ArrayList<String>, amount: Int): Boolean {
        if (amount <= 0 || amount > maxLimitForInventory) {
            list.add("Quantity is not valid. Range between 1 and $maxLimitForInventory")
            return false
        }
        return true
    }

    fun isWalletAmountWithinLimit(list: ArrayList<String>, user: User, amount: Int): Boolean {
        if (!user.wallet.isWalletAmountWithinLimit(amount)) {
            list.add("Cannot place the order. Wallet amount will exceed $maxLimitForWallet")
            return false
        }
        return true
    }


}


