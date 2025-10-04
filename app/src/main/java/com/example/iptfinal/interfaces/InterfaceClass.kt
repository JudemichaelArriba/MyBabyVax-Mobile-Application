package com.example.iptfinal.interfaces

import com.example.iptfinal.models.Users

interface InterfaceClass {


    interface PurokCallback {
        fun onPuroksLoaded(puroks: List<String>)
        fun onError(message: String)
    }
    interface StatusCallback {
        fun onSuccess(message: String)
        fun onError(message: String)
    }

    interface UserCallback {
        fun onUserLoaded(user: Users)
        fun onError(message: String)
    }


}
