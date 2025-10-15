package com.example.iptfinal.interfaces

import com.example.iptfinal.models.Baby
import com.example.iptfinal.models.BabyVaccineDisplay
import com.example.iptfinal.models.BabyVaccineSchedule
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

    interface VaccineCallback {
        fun onVaccinesLoaded(vaccines: List<com.example.iptfinal.models.Vaccine>)
        fun onError(message: String)
    }

    interface DoseCallback {
        fun onDosesLoaded(doses: List<com.example.iptfinal.models.Dose>)
        fun onError(message: String)
    }


    interface BabiesCallback {
        fun onBabiesLoaded(babies: List<Baby>)
        fun onError(message: String?)
    }


    interface BabyVaccineDisplayCallback {
        fun onSchedulesLoaded(list: List<BabyVaccineDisplay>)
        fun onError(error: String)
    }

}
