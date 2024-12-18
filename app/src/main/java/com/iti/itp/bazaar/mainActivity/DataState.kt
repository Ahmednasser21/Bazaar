package com.iti.itp.bazaar.mainActivity

sealed class DataState {
    class OnSuccess<E>(val data:E): DataState()
    class OnFailed(val msg:Throwable): DataState()
    object Loading : DataState()
}