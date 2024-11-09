package com.dld.bluewaves.trash.repository
//
//import com.dld.bluewaves.data.UniqueEmailValidationResponse
//import com.dld.bluewaves.data.ValidateEmailBody
//import com.dld.bluewaves.utils.APIConsumer
//import com.dld.bluewaves.utils.RequestStatus
//import com.dld.bluewaves.utils.SimplifiedMessage
//import kotlinx.coroutines.flow.flow
//
//class AuthRepository(private val consumer: APIConsumer) {
//    fun validateEmailAddress(body: ValidateEmailBody) = flow<RequestStatus<UniqueEmailValidationResponse>>{
//        emit(RequestStatus.Waiting)
//        val response = consumer.validateEmailAddress(body)
//        if (response.isSuccessful) {
//            emit(RequestStatus.Success(response.body()!!))
//        } else {
//            emit(RequestStatus.Error(SimplifiedMessage.get(response.errorBody()!!.byteStream().reader().readText()
//                    )
//                )
//            )
//
//        }
//    }
//}