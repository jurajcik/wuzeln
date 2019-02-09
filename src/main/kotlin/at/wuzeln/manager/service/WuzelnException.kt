package at.wuzeln.manager.service

class WuzelnException: Exception{

    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)
}